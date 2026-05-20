import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LocationService } from '../../../core/services/location.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-location-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container mt-4">
      <div class="row justify-content-center">
        <div class="col-md-7">
          <div class="card p-4 shadow">
            <h2>{{ isEdit ? 'Edit Location' : 'Add New Location' }}</h2>
            @if (error) { <div class="alert alert-danger">{{ error }}</div> }
            @if (success) { <div class="alert alert-success">{{ success }}</div> }
            <form (ngSubmit)="onSubmit()">
              @if (!isEdit) {
                <div class="mb-3">
                  <label class="form-label">Name *</label>
                  <input type="text" class="form-control" [(ngModel)]="form.name" name="name" required>
                </div>
              }
              <div class="mb-3">
                <label class="form-label">Address *</label>
                <input type="text" class="form-control" [(ngModel)]="form.address" name="address" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Type *</label>
                <select class="form-select" [(ngModel)]="form.type" name="type" required>
                  <option value="">Select type...</option>
                  <option value="Club">Club</option>
                  <option value="Bar">Bar</option>
                  <option value="Restaurant">Restaurant</option>
                  <option value="Concert Hall">Concert Hall</option>
                  <option value="Theater">Theater</option>
                  <option value="Sports Venue">Sports Venue</option>
                  <option value="Park">Park</option>
                  <option value="Other">Other</option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">Description *</label>
                <textarea class="form-control" [(ngModel)]="form.description" name="description" rows="4" required></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label">Image {{ isEdit ? '(optional - leave empty to keep current)' : '*' }}</label>
                <input type="file" class="form-control" (change)="onFileChange($event)" accept="image/*" [required]="!isEdit">
              </div>
              <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary" [disabled]="loading">
                  @if (loading) { <span class="spinner-border spinner-border-sm me-1"></span> }
                  {{ isEdit ? 'Update' : 'Create' }} Location
                </button>
                <button type="button" class="btn btn-outline-secondary" routerLink="/locations">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  `
})
export class LocationFormComponent implements OnInit {
  isEdit = false;
  locationId: number | null = null;
  form = { name: '', address: '', type: '', description: '' };
  selectedFile: File | null = null;
  error = '';
  success = '';
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.locationId = Number(id);
      this.locationService.getLocation(this.locationId).subscribe(loc => {
        this.form = { name: loc.name, address: loc.address, type: loc.type, description: loc.description };
      });
    }
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.selectedFile = input.files[0];
  }

  onSubmit(): void {
    this.loading = true;
    this.error = '';
    const formData = new FormData();
    if (!this.isEdit) formData.append('name', this.form.name);
    formData.append('address', this.form.address);
    formData.append('type', this.form.type);
    formData.append('description', this.form.description);
    if (this.selectedFile) formData.append('image', this.selectedFile);

    const obs = this.isEdit
      ? this.locationService.updateLocation(this.locationId!, formData)
      : this.locationService.createLocation(formData);

    obs.subscribe({
      next: () => {
        this.success = `Location ${this.isEdit ? 'updated' : 'created'} successfully!`;
        this.loading = false;
        setTimeout(() => this.router.navigate(['/locations']), 1500);
      },
      error: err => { this.error = err.error || 'Operation failed'; this.loading = false; }
    });
  }
}

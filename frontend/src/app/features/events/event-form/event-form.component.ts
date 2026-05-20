import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventService } from '../../../core/services/event.service';
import { LocationService } from '../../../core/services/location.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-event-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container mt-4">
      <div class="row justify-content-center">
        <div class="col-md-7">
          <div class="card p-4 shadow">
            <h2>{{ isEdit ? 'Edit Event' : 'Add New Event' }}</h2>
            @if (error) { <div class="alert alert-danger">{{ error }}</div> }
            @if (success) { <div class="alert alert-success">{{ success }}</div> }
            <form (ngSubmit)="onSubmit()">
              <div class="mb-3">
                <label class="form-label">Event Name *</label>
                <input type="text" class="form-control" [(ngModel)]="form.name" name="name" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Location *</label>
                <select class="form-select" [(ngModel)]="form.locationId" name="locationId" required>
                  <option value="">Select location...</option>
                  @for (loc of locations; track loc.id) {
                    <option [value]="loc.id">{{ loc.name }}</option>
                  }
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">Address *</label>
                <input type="text" class="form-control" [(ngModel)]="form.address" name="address" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Type *</label>
                <input type="text" class="form-control" [(ngModel)]="form.type" name="type" placeholder="Concert, Exhibition..." required>
              </div>
              <div class="mb-3">
                <label class="form-label">Date & Time *</label>
                <input type="datetime-local" class="form-control" [(ngModel)]="form.date" name="date" required>
              </div>
              <div class="mb-3 form-check">
                <input type="checkbox" class="form-check-input" [(ngModel)]="form.regular" name="regular" id="regular">
                <label class="form-check-label" for="regular">Regular event</label>
              </div>
              <div class="mb-3 form-check">
                <input type="checkbox" class="form-check-input" [(ngModel)]="form.free" name="free" id="free">
                <label class="form-check-label" for="free">Free admission</label>
              </div>
              @if (!form.free) {
                <div class="mb-3">
                  <label class="form-label">Price (€) *</label>
                  <input type="number" class="form-control" [(ngModel)]="form.price" name="price" min="0" step="0.01">
                </div>
              }
              <div class="mb-3">
                <label class="form-label">Image {{ isEdit ? '(optional)' : '*' }}</label>
                <input type="file" class="form-control" (change)="onFileChange($event)" accept="image/*" [required]="!isEdit">
              </div>
              <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary" [disabled]="loading">
                  @if (loading) { <span class="spinner-border spinner-border-sm me-1"></span> }
                  {{ isEdit ? 'Update' : 'Create' }} Event
                </button>
                <button type="button" class="btn btn-outline-secondary" routerLink="/events">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  `
})
export class EventFormComponent implements OnInit {
  isEdit = false;
  eventId: number | null = null;
  form: any = { name: '', locationId: '', address: '', type: '', date: '', regular: false, free: false, price: null };
  selectedFile: File | null = null;
  locations: any[] = [];
  error = '';
  success = '';
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private locationService: LocationService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.locationService.getLocations().subscribe(locs => this.locations = locs);
    const id = this.route.snapshot.paramMap.get('id');
    const locationId = this.route.snapshot.queryParamMap.get('locationId');
    if (locationId) this.form.locationId = locationId;
    if (id) {
      this.isEdit = true;
      this.eventId = Number(id);
      this.eventService.getEvent(this.eventId).subscribe(e => {
        this.form = {
          name: e.name, locationId: e.locationId, address: e.address, type: e.type,
          date: e.date?.slice(0, 16), regular: e.regular, free: e.free, price: e.price
        };
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
    formData.append('locationId', this.form.locationId);
    formData.append('name', this.form.name);
    formData.append('address', this.form.address);
    formData.append('type', this.form.type);
    formData.append('date', this.form.date);
    formData.append('regular', String(this.form.regular));
    formData.append('free', String(this.form.free));
    if (!this.form.free && this.form.price !== null) formData.append('price', this.form.price);
    if (this.selectedFile) formData.append('image', this.selectedFile);

    const obs = this.isEdit
      ? this.eventService.updateEvent(this.eventId!, formData)
      : this.eventService.createEvent(formData);

    obs.subscribe({
      next: () => {
        this.success = `Event ${this.isEdit ? 'updated' : 'created'} successfully!`;
        this.loading = false;
        setTimeout(() => this.router.navigate(['/events']), 1500);
      },
      error: err => { this.error = err.error || 'Operation failed'; this.loading = false; }
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../core/services/admin.service';
import { LocationService } from '../../core/services/location.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin.component.html'
})
export class AdminComponent implements OnInit {
  requests: any[] = [];
  users: any[] = [];
  locations: any[] = [];
  activeTab = 'requests';
  addManagerForm = { locationId: '', userId: '' };
  error = '';
  success = '';

  constructor(
    private adminService: AdminService,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    this.loadRequests();
    this.loadUsers();
    this.locationService.getLocations().subscribe(locs => this.locations = locs);
  }

  loadRequests(): void {
    this.adminService.getAllRequests().subscribe(r => this.requests = r);
  }

  loadUsers(): void {
    this.adminService.getAllUsers().subscribe(u => this.users = u);
  }

  approve(id: number): void {
    this.adminService.approveRequest(id).subscribe({
      next: () => { this.success = 'Request approved'; this.loadRequests(); this.loadUsers(); },
      error: err => this.error = err.error || 'Failed'
    });
  }

  reject(id: number): void {
    this.adminService.rejectRequest(id).subscribe({
      next: () => { this.success = 'Request rejected'; this.loadRequests(); },
      error: err => this.error = err.error || 'Failed'
    });
  }

  addManager(): void {
    this.error = '';
    this.adminService.addManager(Number(this.addManagerForm.locationId), Number(this.addManagerForm.userId)).subscribe({
      next: () => {
        this.success = 'Manager added successfully';
        this.loadUsers();
        this.locationService.getLocations().subscribe(locs => this.locations = locs);
      },
      error: err => this.error = err.error || 'Failed to add manager'
    });
  }

  removeManager(locationId: number, userId: number): void {
    if (!confirm('Remove this manager?')) return;
    this.adminService.removeManager(locationId, userId).subscribe({
      next: () => {
        this.success = 'Manager removed';
        this.locationService.getLocations().subscribe(locs => this.locations = locs);
        this.loadUsers();
      },
      error: err => this.error = err.error || 'Failed'
    });
  }

  get pendingRequests(): any[] {
    return this.requests.filter(r => r.status === 'PENDING');
  }
}

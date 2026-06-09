import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BehaviorSubject, map, shareReplay, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ApiService } from '../../core/api/api.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin.component.html'
})
export class AdminComponent {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  activeTab = 'requests';
  addManagerForm = { locationId: '', userId: '' };

  private requestsRefresh$ = new BehaviorSubject<void>(undefined);
  private usersRefresh$ = new BehaviorSubject<void>(undefined);
  private locationsRefresh$ = new BehaviorSubject<void>(undefined);

  private requests$ = this.requestsRefresh$.pipe(
    switchMap(() => this.api.admin.getAllRequests().pipe(map(r => r.data ?? []))),
    shareReplay(1)
  );

  requests = toSignal(this.requests$, { initialValue: [] as any[] });

  pendingRequests = toSignal(
    this.requests$.pipe(map(r => r.filter((x: any) => x.status === 'PENDING'))),
    { initialValue: [] as any[] }
  );

  users = toSignal(
    this.usersRefresh$.pipe(
      switchMap(() => this.api.admin.getAllUsers().pipe(map(r => r.data ?? []))),
      shareReplay(1)
    ),
    { initialValue: [] as any[] }
  );

  locations = toSignal(
    this.locationsRefresh$.pipe(
      switchMap(() => this.api.locations.getLocations().pipe(map(r => r.data ?? []))),
      shareReplay(1)
    ),
    { initialValue: [] as any[] }
  );

  approve(id: number): void {
    this.api.admin.approveRequest(id).subscribe({
      next: () => {
        this.toast.success('Request approved');
        this.requestsRefresh$.next();
        this.usersRefresh$.next();
      }
    });
  }

  reject(id: number): void {
    this.api.admin.rejectRequest(id).subscribe({
      next: () => { this.toast.success('Request rejected'); this.requestsRefresh$.next(); }
    });
  }

  addManager(): void {
    this.api.admin.addManager(Number(this.addManagerForm.locationId), Number(this.addManagerForm.userId)).subscribe({
      next: () => {
        this.toast.success('Manager added successfully');
        this.usersRefresh$.next();
        this.locationsRefresh$.next();
      }
    });
  }

  removeManager(locationId: number, userId: number): void {
    if (!confirm('Remove this manager?')) return;
    this.api.admin.removeManager(locationId, userId).subscribe({
      next: () => {
        this.toast.success('Manager removed');
        this.locationsRefresh$.next();
        this.usersRefresh$.next();
      }
    });
  }
}

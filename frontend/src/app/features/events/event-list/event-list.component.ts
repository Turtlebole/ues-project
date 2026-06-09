import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BehaviorSubject, map, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ApiService } from '../../../core/api/api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './event-list.component.html'
})
export class EventListComponent {
  private api = inject(ApiService);
  authService = inject(AuthService);

  filters = { date: '', type: '', locationId: undefined as number | undefined, address: '', maxPrice: undefined as number | undefined };

  private search$ = new BehaviorSubject<void>(undefined);

  events = toSignal(
    this.search$.pipe(
      switchMap(() => this.api.events.searchEvents({
        date: this.filters.date || undefined,
        type: this.filters.type || undefined,
        locationId: this.filters.locationId,
        address: this.filters.address || undefined,
        maxPrice: this.filters.maxPrice,
      }).pipe(map(r => r.data ?? [])))
    ),
    { initialValue: null as any }
  );

  locations = toSignal(
    this.api.locations.getLocations().pipe(map(r => r.data ?? [])),
    { initialValue: [] as any[] }
  );

  loadEvents(): void { this.search$.next(); }

  resetFilters(): void {
    this.filters = { date: '', type: '', locationId: undefined, address: '', maxPrice: undefined };
    this.search$.next();
  }

  getImageUrl(img: string): string { return this.api.events.getImageUrl(img); }
}

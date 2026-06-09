import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BehaviorSubject, map, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ApiService } from '../../../core/api/api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-location-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './location-list.component.html'
})
export class LocationListComponent {
  private api = inject(ApiService);
  authService = inject(AuthService);

  search = { name: '', address: '', type: '' };

  private search$ = new BehaviorSubject<void>(undefined);

  locations = toSignal(
    this.search$.pipe(
      switchMap(() => this.api.locations.getLocations({
        name: this.search.name || undefined,
        address: this.search.address || undefined,
        type: this.search.type || undefined,
      }).pipe(map(r => r.data ?? [])))
    ),
    { initialValue: null as any }
  );

  loadLocations(): void { this.search$.next(); }

  getImageUrl(img: string): string { return this.api.locations.getImageUrl(img); }
}

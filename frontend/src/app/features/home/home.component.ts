import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ApiService } from '../../core/api/api.service';
import { SpinnerComponent } from '../../shared/spinner/spinner.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, SpinnerComponent],
  templateUrl: './home.component.html'
})
export class HomeComponent {
  private api = inject(ApiService);

  todayEvents = toSignal(
    this.api.events.getTodayEvents().pipe(map(r => r.data ?? [])),
    { initialValue: null as any }
  );
  popularLocations = toSignal(
    this.api.locations.getPopularLocations().pipe(map(r => r.data ?? [])),
    { initialValue: null as any }
  );
  recentReviews = toSignal(
    this.api.reviews.getRecentFromPopularLocation().pipe(map(r => r.data ?? [])),
    { initialValue: null as any }
  );

  getImageUrl(img: string): string { return this.api.locations.getImageUrl(img); }
  getEventImageUrl(img: string): string { return this.api.events.getImageUrl(img); }
  getStars(rating: number): string { return rating ? rating.toFixed(1) : 'N/A'; }
}

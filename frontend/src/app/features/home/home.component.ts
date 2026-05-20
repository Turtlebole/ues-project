import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EventService } from '../../core/services/event.service';
import { LocationService } from '../../core/services/location.service';
import { ReviewService } from '../../core/services/review.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  todayEvents: any[] = [];
  popularLocations: any[] = [];
  recentReviews: any[] = [];

  constructor(
    private eventService: EventService,
    private locationService: LocationService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    this.eventService.getTodayEvents().subscribe(events => this.todayEvents = events);
    this.locationService.getPopularLocations().subscribe(locs => this.popularLocations = locs);
    this.reviewService.getRecentFromPopularLocation().subscribe(reviews => this.recentReviews = reviews);
  }

  getImageUrl(img: string): string {
    return this.locationService.getImageUrl(img);
  }

  getEventImageUrl(img: string): string {
    return this.eventService.getImageUrl(img);
  }

  getStars(rating: number): string {
    if (!rating) return 'N/A';
    return rating.toFixed(1);
  }
}

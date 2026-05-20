import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EventService } from '../../../core/services/event.service';
import { LocationService } from '../../../core/services/location.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './event-list.component.html'
})
export class EventListComponent implements OnInit {
  events: any[] = [];
  locations: any[] = [];
  filters = { date: '', type: '', locationId: undefined as number | undefined, address: '', maxPrice: undefined as number | undefined };
  loading = false;

  constructor(
    public eventService: EventService,
    public locationService: LocationService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
    this.locationService.getLocations().subscribe(locs => this.locations = locs);
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.searchEvents(
      this.filters.date || undefined,
      this.filters.type || undefined,
      this.filters.locationId,
      this.filters.address || undefined,
      this.filters.maxPrice
    ).subscribe({
      next: events => { this.events = events; this.loading = false; },
      error: () => this.loading = false
    });
  }

  resetFilters(): void {
    this.filters = { date: '', type: '', locationId: undefined, address: '', maxPrice: undefined };
    this.loadEvents();
  }

  getImageUrl(img: string): string {
    return this.eventService.getImageUrl(img);
  }
}

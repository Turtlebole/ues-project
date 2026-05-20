import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LocationService } from '../../../core/services/location.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-location-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './location-list.component.html'
})
export class LocationListComponent implements OnInit {
  locations: any[] = [];
  search = { name: '', address: '', type: '' };
  loading = false;

  constructor(public locationService: LocationService, public authService: AuthService) {}

  ngOnInit(): void {
    this.loadLocations();
  }

  loadLocations(): void {
    this.loading = true;
    this.locationService.getLocations(this.search.name, this.search.address, this.search.type).subscribe({
      next: locs => { this.locations = locs; this.loading = false; },
      error: () => this.loading = false
    });
  }

  getImageUrl(img: string): string {
    return this.locationService.getImageUrl(img);
  }
}

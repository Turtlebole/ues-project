import { Component, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AnalyticsService } from '../../../core/services/analytics.service';
import { LocationService } from '../../../core/services/location.service';

// Chart.js is loaded via CDN
declare const Chart: any;

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './analytics.component.html'
})
export class AnalyticsComponent implements OnInit, AfterViewInit {
  @ViewChild('eventTypeChart') eventTypeChartRef!: ElementRef;
  @ViewChild('pricingChart') pricingChartRef!: ElementRef;

  locationId!: number;
  location: any = null;
  analytics: any = null;
  period = 'monthly';
  customStart = '';
  customEnd = '';
  loading = false;
  error = '';
  private charts: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private analyticsService: AnalyticsService,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    this.locationId = Number(this.route.snapshot.paramMap.get('locationId'));
    this.locationService.getLocation(this.locationId).subscribe(loc => this.location = loc);
    this.loadAnalytics();
  }

  ngAfterViewInit(): void {}

  loadAnalytics(): void {
    this.loading = true;
    this.error = '';
    this.analyticsService.getAnalytics(
      this.locationId, this.period,
      this.customStart || undefined,
      this.customEnd || undefined
    ).subscribe({
      next: data => {
        this.analytics = data;
        this.loading = false;
        setTimeout(() => this.renderCharts(), 100);
      },
      error: err => {
        this.error = 'Failed to load analytics';
        this.loading = false;
      }
    });
  }

  private destroyCharts(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
  }

  renderCharts(): void {
    this.destroyCharts();
    if (!this.analytics || typeof Chart === 'undefined') return;

    if (this.eventTypeChartRef?.nativeElement) {
      const ctx = this.eventTypeChartRef.nativeElement.getContext('2d');
      this.charts.push(new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: ['Regular', 'Irregular'],
          datasets: [{
            data: [this.analytics.regularEvents, this.analytics.irregularEvents],
            backgroundColor: ['#6610f2', '#adb5bd']
          }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
      }));
    }

    if (this.pricingChartRef?.nativeElement) {
      const ctx = this.pricingChartRef.nativeElement.getContext('2d');
      this.charts.push(new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: ['Free', 'Paid'],
          datasets: [{
            data: [this.analytics.freeEvents, this.analytics.paidEvents],
            backgroundColor: ['#198754', '#0d6efd']
          }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
      }));
    }
  }
}

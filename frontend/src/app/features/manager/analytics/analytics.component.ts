import { Component, OnInit, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService } from '../../../core/api/api.service';

declare const Chart: any;

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './analytics.component.html'
})
export class AnalyticsComponent implements OnInit {
  @ViewChild('eventTypeChart') eventTypeChartRef!: ElementRef;
  @ViewChild('pricingChart') pricingChartRef!: ElementRef;

  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  locationId!: number;
  location = signal<any>(null);
  analytics = signal<any>(null);
  period = 'monthly';
  customStart = '';
  customEnd = '';
  loading = signal(false);
  private charts: any[] = [];

  ngOnInit(): void {
    this.locationId = Number(this.route.snapshot.paramMap.get('locationId'));
    this.api.locations.getLocation(this.locationId).subscribe(r => this.location.set(r.data));
    this.loadAnalytics();
  }

  loadAnalytics(): void {
    this.loading.set(true);
    this.api.analytics.getAnalytics(
      this.locationId, this.period,
      this.customStart || undefined,
      this.customEnd || undefined
    ).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: r => {
        this.analytics.set(r.data);
        setTimeout(() => this.renderCharts(), 100);
      }
    });
  }

  private destroyCharts(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
  }

  renderCharts(): void {
    this.destroyCharts();
    const data = this.analytics();
    if (!data || typeof Chart === 'undefined') return;

    if (this.eventTypeChartRef?.nativeElement) {
      this.charts.push(new Chart(this.eventTypeChartRef.nativeElement.getContext('2d'), {
        type: 'doughnut',
        data: {
          labels: ['Regular', 'Irregular'],
          datasets: [{ data: [data.regularEvents, data.irregularEvents], backgroundColor: ['#6610f2', '#adb5bd'] }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
      }));
    }

    if (this.pricingChartRef?.nativeElement) {
      this.charts.push(new Chart(this.pricingChartRef.nativeElement.getContext('2d'), {
        type: 'doughnut',
        data: {
          labels: ['Free', 'Paid'],
          datasets: [{ data: [data.freeEvents, data.paidEvents], backgroundColor: ['#198754', '#0d6efd'] }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
      }));
    }
  }
}

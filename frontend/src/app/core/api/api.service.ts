import { Injectable, inject } from '@angular/core';
import { ApiClientService } from './api-client.service';
import { EventApi } from './event.api';
import { LocationApi } from './location.api';
import { ReviewApi } from './review.api';
import { UserApi } from './user.api';
import { AdminApi } from './admin.api';
import { AnalyticsApi } from './analytics.api';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private client = inject(ApiClientService);

  readonly events = new EventApi(this.client);
  readonly locations = new LocationApi(this.client);
  readonly reviews = new ReviewApi(this.client);
  readonly users = new UserApi(this.client);
  readonly admin = new AdminApi(this.client);
  readonly analytics = new AnalyticsApi(this.client);
}

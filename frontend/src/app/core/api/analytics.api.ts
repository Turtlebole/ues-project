import { Observable } from 'rxjs';
import { Api } from './api';
import { ApiClient } from './api-client';
import { ApiResponse } from './rest.model';
import { Analytics } from '../models/models';

export class AnalyticsApi extends Api {
  constructor(client: ApiClient) { super(client); }

  getAnalytics(locationId: number, period?: string, startDate?: string, endDate?: string): Observable<ApiResponse<Analytics>> {
    return this.apiClient.get<Analytics>(`/analytics/locations/${locationId}`, {
      params: { period, startDate, endDate },
      authenticated: true,
    });
  }
}

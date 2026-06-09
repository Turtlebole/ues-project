import { Observable } from 'rxjs';
import { Api } from './api';
import { ApiClient } from './api-client';
import { ApiResponse, MultiPart } from './rest.model';
import { Location, Review } from '../models/models';
import { environment } from '../../../config/environment';

export class LocationApi extends Api {
  constructor(client: ApiClient) { super(client); }

  getLocations(params?: { name?: string; address?: string; type?: string }): Observable<ApiResponse<Location[]>> {
    return this.apiClient.get<Location[]>('/locations', { params });
  }

  getLocation(id: number): Observable<ApiResponse<Location>> {
    return this.apiClient.get<Location>(`/locations/${id}`);
  }

  getPopularLocations(): Observable<ApiResponse<Location[]>> {
    return this.apiClient.get<Location[]>('/locations/popular');
  }

  createLocation(parts: MultiPart[]): Observable<ApiResponse<Location>> {
    return this.apiClient.postMultipart<MultiPart[], Location>('/locations', parts, { authenticated: true });
  }

  updateLocation(id: number, parts: MultiPart[]): Observable<ApiResponse<Location>> {
    return this.apiClient.putMultipart<MultiPart[], Location>(`/locations/${id}`, parts, { authenticated: true });
  }

  deleteLocation(id: number): Observable<ApiResponse<void>> {
    return this.apiClient.delete<void>(`/locations/${id}`, { authenticated: true });
  }

  getReviews(locationId: number, sortBy?: string, sortDir?: string): Observable<ApiResponse<Review[]>> {
    return this.apiClient.get<Review[]>(`/locations/${locationId}/reviews`, { params: { sortBy, sortDir } });
  }

  getImageUrl(imageName: string): string {
    return `${environment.uploadsUrl}/${imageName}`;
  }
}

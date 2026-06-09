import { Observable } from 'rxjs';
import { Api } from './api';
import { ApiClient } from './api-client';
import { ApiResponse, MultiPart } from './rest.model';
import { Event } from '../models/models';
import { environment } from '../../../config/environment';

export class EventApi extends Api {
  constructor(client: ApiClient) { super(client); }

  getTodayEvents(): Observable<ApiResponse<Event[]>> {
    return this.apiClient.get<Event[]>('/events/today');
  }

  searchEvents(params: { date?: string; type?: string; locationId?: number; address?: string; maxPrice?: number }): Observable<ApiResponse<Event[]>> {
    return this.apiClient.get<Event[]>('/events', { params });
  }

  getEvent(id: number): Observable<ApiResponse<Event>> {
    return this.apiClient.get<Event>(`/events/${id}`);
  }

  getEventsByLocation(locationId: number): Observable<ApiResponse<Event[]>> {
    return this.apiClient.get<Event[]>(`/events/location/${locationId}`);
  }

  createEvent(parts: MultiPart[]): Observable<ApiResponse<Event>> {
    return this.apiClient.postMultipart<MultiPart[], Event>('/events', parts, { authenticated: true });
  }

  updateEvent(id: number, parts: MultiPart[]): Observable<ApiResponse<Event>> {
    return this.apiClient.putMultipart<MultiPart[], Event>(`/events/${id}`, parts, { authenticated: true });
  }

  deleteEvent(id: number): Observable<ApiResponse<void>> {
    return this.apiClient.delete<void>(`/events/${id}`, { authenticated: true });
  }

  getImageUrl(imageName: string): string {
    return `${environment.uploadsUrl}/${imageName}`;
  }
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EventService {
  private apiUrl = `${environment.apiUrl}/events`;

  constructor(private http: HttpClient) {}

  getTodayEvents(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/today`);
  }

  searchEvents(date?: string, type?: string, locationId?: number, address?: string, maxPrice?: number): Observable<any[]> {
    let params = new HttpParams();
    if (date) params = params.set('date', date);
    if (type) params = params.set('type', type);
    if (locationId) params = params.set('locationId', locationId.toString());
    if (address) params = params.set('address', address);
    if (maxPrice !== undefined && maxPrice !== null) params = params.set('maxPrice', maxPrice.toString());
    return this.http.get<any[]>(this.apiUrl, { params });
  }

  getEvent(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  getEventsByLocation(locationId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/location/${locationId}`);
  }

  createEvent(formData: FormData): Observable<any> {
    return this.http.post<any>(this.apiUrl, formData);
  }

  updateEvent(id: number, formData: FormData): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, formData);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getImageUrl(imageName: string): string {
    return `${environment.uploadsUrl}/${imageName}`;
  }
}

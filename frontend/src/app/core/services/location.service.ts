import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LocationService {
  private apiUrl = `${environment.apiUrl}/locations`;

  constructor(private http: HttpClient) {}

  getLocations(name?: string, address?: string, type?: string): Observable<any[]> {
    let params = new HttpParams();
    if (name) params = params.set('name', name);
    if (address) params = params.set('address', address);
    if (type) params = params.set('type', type);
    return this.http.get<any[]>(this.apiUrl, { params });
  }

  getLocation(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  getPopularLocations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/popular`);
  }

  createLocation(formData: FormData): Observable<any> {
    return this.http.post<any>(this.apiUrl, formData);
  }

  updateLocation(id: number, formData: FormData): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, formData);
  }

  deleteLocation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getReviews(locationId: number, sortBy?: string, sortDir?: string): Observable<any[]> {
    let params = new HttpParams();
    if (sortBy) params = params.set('sortBy', sortBy);
    if (sortDir) params = params.set('sortDir', sortDir);
    return this.http.get<any[]>(`${this.apiUrl}/${locationId}/reviews`, { params });
  }

  getImageUrl(imageName: string): string {
    return `${environment.uploadsUrl}/${imageName}`;
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  getAllRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/requests`);
  }

  getPendingRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/requests/pending`);
  }

  approveRequest(id: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/requests/${id}/approve`, {}, { responseType: 'text' });
  }

  rejectRequest(id: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/requests/${id}/reject`, {}, { responseType: 'text' });
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`);
  }

  addManager(locationId: number, userId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/locations/${locationId}/managers`, { userId }, { responseType: 'text' });
  }

  removeManager(locationId: number, userId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/locations/${locationId}/managers/${userId}`, { responseType: 'text' });
  }
}

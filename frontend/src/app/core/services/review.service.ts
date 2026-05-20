import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private apiUrl = `${environment.apiUrl}/reviews`;

  constructor(private http: HttpClient) {}

  createReview(data: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, data);
  }

  hideReview(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/hide`, {});
  }

  deleteReview(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addComment(reviewId: number, text: string, parentCommentId?: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${reviewId}/comments`, { text, parentCommentId });
  }

  getRecentFromPopularLocation(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/popular-location/recent`);
  }
}

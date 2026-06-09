import { Observable } from 'rxjs';
import { Api } from './api';
import { ApiClient } from './api-client';
import { ApiResponse } from './rest.model';
import { Review } from '../models/models';

export class ReviewApi extends Api {
  constructor(client: ApiClient) { super(client); }

  createReview(data: { locationId: number; eventId: number; comment?: string; performanceRating?: number; soundLightRating?: number; spaceRating?: number; overallRating?: number }): Observable<ApiResponse<Review>> {
    return this.apiClient.post<typeof data, Review>('/reviews', data, { authenticated: true });
  }

  hideReview(id: number): Observable<ApiResponse<void>> {
    return this.apiClient.put<null, void>(`/reviews/${id}/hide`, null, { authenticated: true });
  }

  deleteReview(id: number): Observable<ApiResponse<void>> {
    return this.apiClient.delete<void>(`/reviews/${id}`, { authenticated: true });
  }

  addComment(reviewId: number, text: string, parentCommentId?: number): Observable<ApiResponse<void>> {
    return this.apiClient.post<object, void>(`/reviews/${reviewId}/comments`, { text, parentCommentId }, { authenticated: true });
  }

  getRecentFromPopularLocation(): Observable<ApiResponse<Review[]>> {
    return this.apiClient.get<Review[]>('/reviews/popular-location/recent');
  }
}

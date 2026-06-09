import { Observable } from 'rxjs';
import { Api } from './api';
import { ApiClient } from './api-client';
import { ApiResponse } from './rest.model';
import { User, AccountRequest, Location } from '../models/models';

export class AdminApi extends Api {
  constructor(client: ApiClient) { super(client); }

  getAllRequests(): Observable<ApiResponse<AccountRequest[]>> {
    return this.apiClient.get<AccountRequest[]>('/admin/requests', { authenticated: true });
  }

  approveRequest(id: number): Observable<ApiResponse<void>> {
    return this.apiClient.post<null, void>(`/admin/requests/${id}/approve`, null, { authenticated: true });
  }

  rejectRequest(id: number): Observable<ApiResponse<void>> {
    return this.apiClient.post<null, void>(`/admin/requests/${id}/reject`, null, { authenticated: true });
  }

  getAllUsers(): Observable<ApiResponse<User[]>> {
    return this.apiClient.get<User[]>('/admin/users', { authenticated: true });
  }

  addManager(locationId: number, userId: number): Observable<ApiResponse<void>> {
    return this.apiClient.post<object, void>(`/admin/locations/${locationId}/managers`, { userId }, { authenticated: true });
  }

  removeManager(locationId: number, userId: number): Observable<ApiResponse<void>> {
    return this.apiClient.delete<void>(`/admin/locations/${locationId}/managers/${userId}`, { authenticated: true });
  }
}

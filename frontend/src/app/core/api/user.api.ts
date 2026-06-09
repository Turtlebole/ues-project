import { Observable } from 'rxjs';
import { Api } from './api';
import { ApiClient } from './api-client';
import { ApiResponse, MultiPart } from './rest.model';
import { User } from '../models/models';
import { environment } from '../../../config/environment';

export class UserApi extends Api {
  constructor(client: ApiClient) { super(client); }

  getProfile(): Observable<ApiResponse<User>> {
    return this.apiClient.get<User>('/users/me', { authenticated: true });
  }

  updateProfile(parts: MultiPart[]): Observable<ApiResponse<User>> {
    return this.apiClient.putMultipart<MultiPart[], User>('/users/me', parts, { authenticated: true });
  }

  changePassword(data: { currentPassword: string; newPassword: string; confirmNewPassword: string }): Observable<ApiResponse<void>> {
    return this.apiClient.post<typeof data, void>('/users/me/change-password', data, { authenticated: true });
  }

  getImageUrl(imageName: string): string {
    return `${environment.uploadsUrl}/${imageName}`;
  }
}

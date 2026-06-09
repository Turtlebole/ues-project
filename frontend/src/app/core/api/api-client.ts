import { ApiResponse, MultiPart, RequestConfig } from './rest.model';
import { Observable } from 'rxjs';

export abstract class ApiClient {
  public abstract post<TRequest, TResponse>(path: string, body: TRequest, config?: RequestConfig): Observable<ApiResponse<TResponse>>;
  public abstract put<TRequest, TResponse>(path: string, body: TRequest, config?: RequestConfig): Observable<ApiResponse<TResponse>>;
  public abstract delete<TResponse>(path: string, config?: RequestConfig): Observable<ApiResponse<TResponse>>;
  public abstract get<TResponse>(path: string, config?: RequestConfig): Observable<ApiResponse<TResponse>>;
  public abstract getBlob<TResponse>(path: string, config?: RequestConfig): Observable<ApiResponse<TResponse>>;
  public abstract getFile(path: string, config?: RequestConfig): Observable<ApiResponse<Blob>>;
  public abstract postMultipart<TRequest extends MultiPart[], TResponse>(path: string, body: TRequest, config?: RequestConfig): Observable<ApiResponse<TResponse>>;
  public abstract putMultipart<TRequest extends MultiPart[], TResponse>(path: string, body: TRequest, config?: RequestConfig): Observable<ApiResponse<TResponse>>;
}

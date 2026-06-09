import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { ApiClient } from './api-client';
import { ApiResponse, MultiPart, RequestConfig, RequestHeaders, RequestParamType } from './rest.model';
import { Injectable } from '@angular/core';
import { environment } from '../../../config/environment';

@Injectable({ providedIn: 'root' })
export class ApiClientService extends ApiClient {

  private readonly baseUrl: string;

  constructor(private http: HttpClient) {
    super();
    this.baseUrl = environment.apiUrl;
  }

  post<TRequest, TResponse>(path: string, body: TRequest, config?: RequestConfig): Observable<ApiResponse<TResponse>> {
    return this.http.post<TResponse>(this.createApiEndpoint(path), body, this.formatJsonOptions(config)).pipe(this.mapJsonResponse());
  }

  put<TRequest, TResponse>(path: string, body: TRequest, config?: RequestConfig): Observable<ApiResponse<TResponse>> {
    return this.http.put<TResponse>(this.createApiEndpoint(path), body, this.formatJsonOptions(config)).pipe(this.mapJsonResponse());
  }

  delete<TResponse>(path: string, config?: RequestConfig): Observable<ApiResponse<TResponse>> {
    return this.http.delete<TResponse>(this.createApiEndpoint(path), this.formatJsonOptions(config)).pipe(this.mapJsonResponse());
  }

  get<TResponse>(path: string, config?: RequestConfig): Observable<ApiResponse<TResponse>> {
    if (this.hasJsonResponse(config)) {
      return this.http.get<TResponse>(this.createApiEndpoint(path), this.formatJsonOptions(config)).pipe(this.mapJsonResponse());
    }
    return this.http.get(this.createApiEndpoint(path), {
      headers: this.wrapHeaders(config?.headers, config?.authenticated),
      params: this.beautifyParams(config?.params),
      observe: 'body' as const,
      responseType: 'json' as const,
    }).pipe(map(response => ({ data: response as TResponse, headers: {} })));
  }

  getBlob<TResponse>(path: string, config?: RequestConfig): Observable<ApiResponse<TResponse>> {
    if (this.hasJsonResponse(config)) {
      return this.http.get<TResponse>(this.createApiEndpoint(path), this.formatJsonOptions(config)).pipe(this.mapJsonResponse());
    }
    return this.http.get(this.createApiEndpoint(path), {
      headers: this.wrapHeaders(config?.headers, config?.authenticated),
      params: this.beautifyParams(config?.params),
      observe: 'body' as const,
      responseType: 'blob' as const,
    }).pipe(map(response => ({ data: response as TResponse, headers: {} })));
  }

  getFile(path: string, config?: RequestConfig): Observable<ApiResponse<Blob>> {
    const isInternalUrl = path.startsWith(`{{host}}`);
    const fullPath = isInternalUrl ? `${this.baseUrl}${path.substring(8)}` : path;
    return this.http.get(fullPath, {
      headers: this.wrapHeaders(config?.headers, config?.authenticated),
      params: this.beautifyParams(config?.params),
      observe: 'response' as const,
      responseType: 'blob' as const,
    }).pipe(map(response => {
      if (response.body) {
        const apiResponse: ApiResponse<File> = {
          data: new File([response.body], response.headers.get('Content-Disposition') ?? 'unknown'),
          headers: {},
        };
        response.headers.keys().forEach(name => { apiResponse.headers[name] = response.headers.get(name); });
        return apiResponse;
      }
      throw 'No file found';
    }));
  }

  postMultipart<TRequest extends MultiPart[], TResponse>(path: string, body: TRequest, config?: RequestConfig): Observable<ApiResponse<TResponse>> {
    return this.http.post<TResponse>(this.createApiEndpoint(path), this.buildFormData(body), this.formatJsonOptions(config)).pipe(this.mapJsonResponse());
  }

  putMultipart<TRequest extends MultiPart[], TResponse>(path: string, body: TRequest, config?: RequestConfig): Observable<ApiResponse<TResponse>> {
    return this.http.put<TResponse>(this.createApiEndpoint(path), this.buildFormData(body), this.formatJsonOptions(config)).pipe(this.mapJsonResponse());
  }

  private buildFormData(parts: MultiPart[]): FormData {
    const formData = new FormData();
    parts.forEach(part => {
      if (typeof part.content === 'string') {
        formData.append(part.name, part.content);
      } else {
        formData.append(part.name, part.content, part.content.name);
      }
    });
    return formData;
  }

  private hasJsonResponse(config?: RequestConfig): boolean {
    return !config?.headers?.accept || config?.headers?.accept === 'application/json';
  }

  private createApiEndpoint(path: string): string {
    return path.startsWith('http') ? path : `${this.baseUrl}${path}`;
  }

  private formatJsonOptions(config?: RequestConfig) {
    return {
      headers: this.wrapHeaders(config?.headers, config?.authenticated),
      params: this.beautifyParams(config?.params),
      observe: 'response' as const,
    };
  }

  private wrapHeaders(headers?: RequestHeaders, authenticatedRequest?: boolean): HttpHeaders {
    const headerRecord: Record<string, string> = {
      Accept: headers?.accept ?? 'application/json',
    };
    if (headers?.contentType) headerRecord['Content-Type'] = headers.contentType;
    if (headers?.otherHeaders) {
      Object.entries(headers.otherHeaders).forEach(([k, v]) => { headerRecord[k] = v; });
    }
    return new HttpHeaders(headerRecord);
  }

  private beautifyParams(params?: { [key: string]: RequestParamType }): HttpParams {
    let httpParams = new HttpParams();
    if (!params) return httpParams;
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
          httpParams = httpParams.append(key, value);
        } else if (Array.isArray(value)) {
          value.forEach(v => httpParams = httpParams.append(key, v));
        } else {
          throw new Error(`Cannot convert to parameters: ${JSON.stringify(value)}`);
        }
      }
    });
    return httpParams;
  }

  mapJsonResponse<TResponse>() {
    return map((response: HttpResponse<TResponse>) => {
      const apiResponse: ApiResponse<TResponse> = { data: response.body, headers: {} };
      response.headers.keys().forEach(name => { apiResponse.headers[name] = response.headers.get(name); });
      return apiResponse;
    });
  }
}

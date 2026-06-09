import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const message = err.error?.message;
      switch (err.status) {
        case 400: toast.error(message || 'Invalid request'); break;
        case 401: toast.error('Session expired — please log in again'); break;
        case 403: toast.error('You do not have permission to do this'); break;
        case 404: toast.error('Resource not found'); break;
        default:
          if (err.status >= 500) toast.error('Server error — please try again');
      }
      return throwError(() => err);
    })
  );
};

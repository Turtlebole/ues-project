import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { environment } from '../../../config/environment';

export interface CurrentUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private currentUserSubject = new BehaviorSubject<CurrentUser | null>(this.getStoredUser());
  currentUser$ = this.currentUserSubject.asObservable();

  login(email: string, password: string): Observable<CurrentUser> {
    return this.http.post<any>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        const user: CurrentUser = { id: res.id, email: res.email, firstName: res.firstName, lastName: res.lastName, role: res.role };
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user);
      }),
      map(res => ({ id: res.id, email: res.email, firstName: res.firstName, lastName: res.lastName, role: res.role }))
    );
  }

  register(data: any): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/auth/register`, data);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  get currentUser(): CurrentUser | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.currentUserSubject.value;
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ROLE_ADMIN';
  }

  isManager(): boolean {
    return this.currentUser?.role === 'ROLE_MANAGER' || this.isAdmin();
  }

  private getStoredUser(): CurrentUser | null {
    const stored = localStorage.getItem('currentUser');
    return stored ? JSON.parse(stored) : null;
  }
}

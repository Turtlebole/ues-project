import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-5">
          <div class="card p-4 shadow">
            <h2 class="text-center mb-4"><i class="bi bi-box-arrow-in-right me-2"></i>Login</h2>
            @if (error) {
              <div class="alert alert-danger">{{ error }}</div>
            }
            <form (ngSubmit)="onLogin()" #loginForm="ngForm">
              <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" [(ngModel)]="email" name="email" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Password</label>
                <input type="password" class="form-control" [(ngModel)]="password" name="password" required>
              </div>
              <button type="submit" class="btn btn-primary w-100" [disabled]="loading">
                @if (loading) { <span class="spinner-border spinner-border-sm me-2"></span> }
                Login
              </button>
            </form>
            <p class="text-center mt-3">
              Don't have an account? <a routerLink="/register">Register</a>
            </p>
          </div>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onLogin(): void {
    this.loading = true;
    this.error = '';
    this.authService.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/']),
      error: err => {
        this.error = 'Invalid email or password';
        this.loading = false;
      }
    });
  }
}

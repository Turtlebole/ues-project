import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-5">
          <div class="card p-4 shadow">
            <h2 class="text-center mb-4">Welcome back</h2>
            <form (ngSubmit)="onLogin()">
              <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" [(ngModel)]="email" name="email" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Password</label>
                <input type="password" class="form-control" [(ngModel)]="password" name="password" required>
              </div>
              <button type="submit" class="btn btn-primary w-100" [disabled]="loading()">
                @if (loading()) { <span class="spinner-border spinner-border-sm me-2"></span> }
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
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  email = '';
  password = '';
  loading = signal(false);

  onLogin(): void {
    this.loading.set(true);
    this.authService.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => {
        this.toast.error('Invalid email or password');
        this.loading.set(false);
      }
    });
  }
}

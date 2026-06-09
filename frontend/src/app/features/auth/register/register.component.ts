import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card p-4 shadow">
            <h2 class="text-center mb-4">Join the flow</h2>
            <form (ngSubmit)="onRegister()">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">First Name</label>
                  <input type="text" class="form-control" [(ngModel)]="form.firstName" name="firstName" required>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Last Name</label>
                  <input type="text" class="form-control" [(ngModel)]="form.lastName" name="lastName" required>
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">Username</label>
                <input type="text" class="form-control" [(ngModel)]="form.username" name="username" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" [(ngModel)]="form.email" name="email" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Password</label>
                <input type="password" class="form-control" [(ngModel)]="form.password" name="password" required minlength="6">
              </div>
              <button type="submit" class="btn btn-primary w-100" [disabled]="loading()">
                @if (loading()) { <span class="spinner-border spinner-border-sm me-2"></span> }
                Submit Registration Request
              </button>
            </form>
            <p class="text-center mt-3">
              Already have an account? <a routerLink="/login">Login</a>
            </p>
          </div>
        </div>
      </div>
    </div>
  `
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  form = { firstName: '', lastName: '', username: '', email: '', password: '' };
  loading = signal(false);

  onRegister(): void {
    this.loading.set(true);
    this.authService.register(this.form).subscribe({
      next: () => {
        this.toast.success('Registration request submitted. Please wait for admin approval.');
        this.loading.set(false);
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: err => {
        this.toast.error(err.error?.message || 'Registration failed');
        this.loading.set(false);
      }
    });
  }
}

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card p-4 shadow">
            <h2 class="text-center mb-4"><i class="bi bi-person-plus me-2"></i>Register</h2>
            @if (error) { <div class="alert alert-danger">{{ error }}</div> }
            @if (success) { <div class="alert alert-success">{{ success }}</div> }
            <form (ngSubmit)="onRegister()" #regForm="ngForm">
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
              <button type="submit" class="btn btn-primary w-100" [disabled]="loading">
                @if (loading) { <span class="spinner-border spinner-border-sm me-2"></span> }
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
  form = { firstName: '', lastName: '', username: '', email: '', password: '' };
  error = '';
  success = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onRegister(): void {
    this.loading = true;
    this.error = '';
    this.authService.register(this.form).subscribe({
      next: msg => {
        this.success = msg;
        this.loading = false;
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: err => {
        this.error = err.error || 'Registration failed';
        this.loading = false;
      }
    });
  }
}

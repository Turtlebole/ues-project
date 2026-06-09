import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast-item" [class]="'toast-' + toast.type" (click)="toastService.dismiss(toast.id)">
          {{ toast.message }}
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .toast-item {
      padding: 0.75rem 1.25rem;
      border-radius: 8px;
      color: #fff;
      font-size: 0.9rem;
      cursor: pointer;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      animation: slideIn 0.2s ease;
      max-width: 320px;
    }
    .toast-success { background: #198754; }
    .toast-error   { background: #dc3545; }
    .toast-info    { background: #0d6efd; }
    @keyframes slideIn {
      from { opacity: 0; transform: translateY(8px); }
      to   { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class ToastComponent {
  toastService = inject(ToastService);
}

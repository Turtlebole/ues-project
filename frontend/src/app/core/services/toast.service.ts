import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);

  success(message: string): void { this.show(message, 'success'); }
  error(message: string): void { this.show(message, 'error'); }
  info(message: string): void { this.show(message, 'info'); }

  dismiss(id: number): void {
    this.toasts.update(t => t.filter(x => x.id !== id));
  }

  private show(message: string, type: Toast['type']): void {
    const id = Date.now();
    this.toasts.update(t => [...t, { id, message, type }]);
    setTimeout(() => this.dismiss(id), 4000);
  }
}

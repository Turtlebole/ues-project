import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BehaviorSubject, finalize, map, switchMap, tap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ApiService } from '../../core/api/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { MultiPart } from '../../core/api/rest.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './profile.component.html'
})
export class ProfileComponent {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  authService = inject(AuthService);

  editForm = { firstName: '', lastName: '' };
  passwordForm = { currentPassword: '', newPassword: '', confirmNewPassword: '' };
  selectedFile: File | null = null;
  loading = signal(false);
  activeTab = 'info';

  private refresh$ = new BehaviorSubject<void>(undefined);

  profile = toSignal(
    this.refresh$.pipe(
      switchMap(() => this.api.users.getProfile().pipe(
        map(r => r.data),
        tap(p => { if (p) this.editForm = { firstName: p.firstName, lastName: p.lastName }; })
      ))
    ),
    { initialValue: null as any }
  );

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.selectedFile = input.files[0];
  }

  updateProfile(): void {
    this.loading.set(true);
    const parts: MultiPart[] = [
      { name: 'firstName', content: this.editForm.firstName },
      { name: 'lastName', content: this.editForm.lastName },
    ];
    if (this.selectedFile) parts.push({ name: 'profileImage', content: this.selectedFile });
    this.api.users.updateProfile(parts).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: () => { this.toast.success('Profile updated!'); this.refresh$.next(); }
    });
  }

  changePassword(): void {
    if (this.passwordForm.newPassword !== this.passwordForm.confirmNewPassword) {
      this.toast.error('Passwords do not match');
      return;
    }
    this.api.users.changePassword(this.passwordForm).subscribe({
      next: () => {
        this.toast.success('Password changed successfully!');
        this.passwordForm = { currentPassword: '', newPassword: '', confirmNewPassword: '' };
      }
    });
  }

  getImageUrl(img: string): string { return this.api.users.getImageUrl(img); }
}

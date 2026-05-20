import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  profile: any = null;
  editForm = { firstName: '', lastName: '' };
  passwordForm = { currentPassword: '', newPassword: '', confirmNewPassword: '' };
  selectedFile: File | null = null;
  error = '';
  success = '';
  pwError = '';
  pwSuccess = '';
  loading = false;
  activeTab = 'info';

  constructor(public userService: UserService, public authService: AuthService) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.userService.getProfile().subscribe(p => {
      this.profile = p;
      this.editForm = { firstName: p.firstName, lastName: p.lastName };
    });
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.selectedFile = input.files[0];
  }

  updateProfile(): void {
    this.loading = true;
    this.error = '';
    const formData = new FormData();
    formData.append('firstName', this.editForm.firstName);
    formData.append('lastName', this.editForm.lastName);
    if (this.selectedFile) formData.append('profileImage', this.selectedFile);
    this.userService.updateProfile(formData).subscribe({
      next: () => {
        this.success = 'Profile updated!';
        this.loading = false;
        this.loadProfile();
      },
      error: err => { this.error = err.error || 'Update failed'; this.loading = false; }
    });
  }

  changePassword(): void {
    this.pwError = '';
    if (this.passwordForm.newPassword !== this.passwordForm.confirmNewPassword) {
      this.pwError = 'Passwords do not match';
      return;
    }
    this.userService.changePassword(this.passwordForm).subscribe({
      next: () => {
        this.pwSuccess = 'Password changed successfully!';
        this.passwordForm = { currentPassword: '', newPassword: '', confirmNewPassword: '' };
      },
      error: err => this.pwError = err.error || 'Failed to change password'
    });
  }

  getImageUrl(img: string): string {
    return this.userService.getImageUrl(img);
  }
}

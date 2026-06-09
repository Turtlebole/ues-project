import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BehaviorSubject, map, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ApiService } from '../../../core/api/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-location-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './location-detail.component.html'
})
export class LocationDetailComponent {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private toast = inject(ToastService);
  authService = inject(AuthService);

  private id = Number(this.route.snapshot.paramMap.get('id'));

  sortBy = 'date';
  sortDir = 'desc';
  showReviewForm = false;
  reviewForm: any = { eventId: null, comment: '', performanceRating: null, soundLightRating: null, spaceRating: null, overallRating: null };
  commentText: { [reviewId: number]: string } = {};
  replyText: { [commentId: number]: string } = {};
  showReplyForm: { [commentId: number]: boolean } = {};

  private reviewsRefresh$ = new BehaviorSubject<void>(undefined);

  location = toSignal(
    this.api.locations.getLocation(this.id).pipe(map(r => r.data)),
    { initialValue: null as any }
  );

  reviews = toSignal(
    this.reviewsRefresh$.pipe(
      switchMap(() => this.api.locations.getReviews(this.id, this.sortBy, this.sortDir).pipe(map(r => r.data ?? [])))
    ),
    { initialValue: [] as any[] }
  );

  pastRegularEvents = toSignal(
    this.api.events.getEventsByLocation(this.id).pipe(
      map(r => (r.data ?? []).filter((e: any) => e.regular && new Date(e.date) < new Date()))
    ),
    { initialValue: [] as any[] }
  );

  isManager = computed(() => {
    const loc = this.location();
    if (!loc || !this.authService.isLoggedIn()) return false;
    if (this.authService.isAdmin()) return true;
    return (loc.managers ?? []).some((m: any) => m.id === this.authService.currentUser?.id);
  });

  managerNames = computed(() =>
    (this.location()?.managers ?? []).map((m: any) => `${m.firstName} ${m.lastName}`).join(', ')
  );

  onSortChange(): void { this.reviewsRefresh$.next(); }

  submitReview(): void {
    const data = { locationId: this.id, ...this.reviewForm };
    this.api.reviews.createReview(data).subscribe({
      next: () => {
        this.showReviewForm = false;
        this.reviewForm = { eventId: null, comment: '', performanceRating: null, soundLightRating: null, spaceRating: null, overallRating: null };
        this.reviewsRefresh$.next();
        this.toast.success('Review submitted!');
      }
    });
  }

  hideReview(reviewId: number): void {
    this.api.reviews.hideReview(reviewId).subscribe(() => this.reviewsRefresh$.next());
  }

  deleteReview(reviewId: number): void {
    if (confirm('Delete this review?')) {
      this.api.reviews.deleteReview(reviewId).subscribe(() => this.reviewsRefresh$.next());
    }
  }

  addComment(reviewId: number): void {
    const text = this.commentText[reviewId];
    if (!text) return;
    this.api.reviews.addComment(reviewId, text).subscribe(() => {
      this.commentText[reviewId] = '';
      this.reviewsRefresh$.next();
    });
  }

  addReply(reviewId: number, parentCommentId: number): void {
    const text = this.replyText[parentCommentId];
    if (!text) return;
    this.api.reviews.addComment(reviewId, text, parentCommentId).subscribe(() => {
      this.replyText[parentCommentId] = '';
      this.showReplyForm[parentCommentId] = false;
      this.reviewsRefresh$.next();
    });
  }

  getImageUrl(img: string): string { return this.api.locations.getImageUrl(img); }
  getEventImageUrl(img: string): string { return this.api.events.getImageUrl(img); }
}

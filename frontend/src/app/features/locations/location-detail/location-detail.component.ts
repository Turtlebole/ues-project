import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LocationService } from '../../../core/services/location.service';
import { EventService } from '../../../core/services/event.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-location-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './location-detail.component.html'
})
export class LocationDetailComponent implements OnInit {
  location: any = null;
  events: any[] = [];
  reviews: any[] = [];
  sortBy = 'date';
  sortDir = 'desc';
  loading = false;
  showReviewForm = false;
  reviewForm: any = { eventId: null, comment: '', performanceRating: null, soundLightRating: null, spaceRating: null, overallRating: null };
  reviewError = '';
  reviewSuccess = '';
  commentText: { [reviewId: number]: string } = {};
  replyText: { [commentId: number]: string } = {};
  showReplyForm: { [commentId: number]: boolean } = {};

  constructor(
    private route: ActivatedRoute,
    public locationService: LocationService,
    public eventService: EventService,
    public reviewService: ReviewService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;
    this.locationService.getLocation(id).subscribe(loc => {
      this.location = loc;
      this.loading = false;
    });
    this.eventService.getEventsByLocation(id).subscribe(events => this.events = events);
    this.loadReviews(id);
  }

  loadReviews(id?: number): void {
    const locationId = id || this.location?.id;
    this.locationService.getReviews(locationId, this.sortBy, this.sortDir).subscribe(r => this.reviews = r);
  }

  onSortChange(): void {
    this.loadReviews();
  }

  submitReview(): void {
    this.reviewError = '';
    const data = { locationId: this.location.id, ...this.reviewForm };
    this.reviewService.createReview(data).subscribe({
      next: () => {
        this.reviewSuccess = 'Review submitted!';
        this.showReviewForm = false;
        this.reviewForm = { eventId: null, comment: '', performanceRating: null, soundLightRating: null, spaceRating: null, overallRating: null };
        this.loadReviews();
      },
      error: err => this.reviewError = err.error || 'Failed to submit review'
    });
  }

  hideReview(reviewId: number): void {
    this.reviewService.hideReview(reviewId).subscribe(() => this.loadReviews());
  }

  deleteReview(reviewId: number): void {
    if (confirm('Delete this review?')) {
      this.reviewService.deleteReview(reviewId).subscribe(() => this.loadReviews());
    }
  }

  addComment(reviewId: number): void {
    const text = this.commentText[reviewId];
    if (!text) return;
    this.reviewService.addComment(reviewId, text).subscribe(() => {
      this.commentText[reviewId] = '';
      this.loadReviews();
    });
  }

  addReply(reviewId: number, parentCommentId: number): void {
    const text = this.replyText[parentCommentId];
    if (!text) return;
    this.reviewService.addComment(reviewId, text, parentCommentId).subscribe(() => {
      this.replyText[parentCommentId] = '';
      this.showReplyForm[parentCommentId] = false;
      this.loadReviews();
    });
  }

  getImageUrl(img: string): string {
    return this.locationService.getImageUrl(img);
  }

  getEventImageUrl(img: string): string {
    return this.eventService.getImageUrl(img);
  }

  get pastRegularEvents(): any[] {
    const now = new Date();
    return this.events.filter(e => e.regular && new Date(e.date) < now);
  }

  isManager(): boolean {
    if (!this.authService.isLoggedIn() || !this.location) return false;
    if (this.authService.isAdmin()) return true;
    const managers = this.location.managers || [];
    return managers.some((m: any) => m.id === this.authService.currentUser?.id);
  }
}

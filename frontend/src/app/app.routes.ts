import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { managerGuard } from './core/guards/manager.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent) },
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
  { path: 'locations', loadComponent: () => import('./features/locations/location-list/location-list.component').then(m => m.LocationListComponent) },
  { path: 'locations/new', loadComponent: () => import('./features/locations/location-form/location-form.component').then(m => m.LocationFormComponent), canActivate: [adminGuard] },
  { path: 'locations/:id', loadComponent: () => import('./features/locations/location-detail/location-detail.component').then(m => m.LocationDetailComponent) },
  { path: 'locations/:id/edit', loadComponent: () => import('./features/locations/location-form/location-form.component').then(m => m.LocationFormComponent), canActivate: [managerGuard] },
  { path: 'events', loadComponent: () => import('./features/events/event-list/event-list.component').then(m => m.EventListComponent) },
  { path: 'events/new', loadComponent: () => import('./features/events/event-form/event-form.component').then(m => m.EventFormComponent), canActivate: [managerGuard] },
  { path: 'events/:id/edit', loadComponent: () => import('./features/events/event-form/event-form.component').then(m => m.EventFormComponent), canActivate: [managerGuard] },
  { path: 'profile', loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent), canActivate: [authGuard] },
  { path: 'admin', loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent), canActivate: [adminGuard] },
  { path: 'analytics/:locationId', loadComponent: () => import('./features/manager/analytics/analytics.component').then(m => m.AnalyticsComponent), canActivate: [managerGuard] },
  { path: '**', redirectTo: '' }
];

import { Routes } from '@angular/router';

import { Home } from './pages/home/home';
import { Bookings } from './pages/bookings/bookings';
import { Login } from './pages/login/login';

import { bookingGuard } from './core/guards/booking.guard';
import { adminGuard } from './core/guards/admin.guard';
import { ChatComponent } from './pages/chat/chat';

export const routes: Routes = [
  { path: '', component: Home },

  {
    path: 'bookings',
    component: Bookings,
    canActivate: [bookingGuard],
  },

  {
    path: 'admin',
    loadComponent: () => import('./pages/admin/admin').then((m) => m.Admin),
    canActivate: [adminGuard],
  },

  {
    path: 'chat',
    component: ChatComponent,
    canActivate: [adminGuard],
  },
  { path: 'login', component: Login },

  { path: '**', redirectTo: '' },
];

import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { UserService } from '../services/user.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const userService = inject(UserService);
  const router = inject(Router);

  // Not logged in → redirect to login page with return URL
  if (!userService.isLoggedIn()) {
    router.navigate(['/login'], { queryParams: { redirectTo: state.url } });
    return false;
  }

  // Logged-in but not admin → redirect to home
  if (!userService.isAdmin()) {
    router.navigate(['/']);
    return false;
  }

  // Logged-in admin → allow
  return true;
};

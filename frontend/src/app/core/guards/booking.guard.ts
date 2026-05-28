import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { UserService } from '../services/user.service';

export const bookingGuard: CanActivateFn = (route, state) => {
  const userService = inject(UserService);
  const router = inject(Router);

  // Only allow authenticated users
  if (!userService.isLoggedIn()) {
    router.navigate(['/login'], {
      queryParams: { redirectTo: state.url },
    });

    return false;
  }

  return true;
};

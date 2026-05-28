import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { bookingGuard } from './booking.guard';
import { UserService } from '../services/user.service';

describe('bookingGuard', () => {
  let userServiceMock: any;
  let routerMock: any;

  const executeGuard = (url: string) =>
    TestBed.runInInjectionContext(() => bookingGuard({} as any, { url } as any));

  beforeEach(() => {
    userServiceMock = {
      isLoggedIn: jasmine.createSpy(),
    };

    routerMock = {
      navigate: jasmine.createSpy(),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: UserService, useValue: userServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  //  Allow logged-in user
  it('should allow access when user is logged in', () => {
    userServiceMock.isLoggedIn.and.returnValue(true);

    const result = executeGuard('/bookings');

    expect(result).toBeTrue();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  //  Block and redirect when not logged in
  it('should redirect to login when user is not logged in', () => {
    userServiceMock.isLoggedIn.and.returnValue(false);

    const result = executeGuard('/bookings');

    expect(result).toBeFalse();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { redirectTo: '/bookings' },
    });
  });
});

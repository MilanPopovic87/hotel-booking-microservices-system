import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { adminGuard } from './admin.guard';
import { UserService } from '../services/user.service';

describe('adminGuard', () => {
  let userServiceMock: any;
  let routerMock: any;

  const executeGuard = (url: string) =>
    TestBed.runInInjectionContext(() => adminGuard({} as any, { url } as any));

  beforeEach(() => {
    userServiceMock = {
      isLoggedIn: jasmine.createSpy(),
      isAdmin: jasmine.createSpy(),
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

  //  Not logged in
  it('should redirect to login when user is not logged in', () => {
    userServiceMock.isLoggedIn.and.returnValue(false);

    const result = executeGuard('/admin');

    expect(result).toBeFalse();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { redirectTo: '/admin' },
    });
  });

  //  Logged in but not admin
  it('should redirect to home when user is not admin', () => {
    userServiceMock.isLoggedIn.and.returnValue(true);
    userServiceMock.isAdmin.and.returnValue(false);

    const result = executeGuard('/admin');

    expect(result).toBeFalse();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/']);
  });

  //  Admin user
  it('should allow access for admin user', () => {
    userServiceMock.isLoggedIn.and.returnValue(true);
    userServiceMock.isAdmin.and.returnValue(true);

    const result = executeGuard('/admin');

    expect(result).toBeTrue();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });
});

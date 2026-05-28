import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

import { Bookings } from './bookings';
import { BookingService } from '../../core/services/booking.service';
import { RoomService } from '../../core/services/room.service';
import { UserService } from '../../core/services/user.service';

describe('Bookings Component', () => {
  let component: Bookings;
  let fixture: ComponentFixture<Bookings>;

  let bookingServiceMock: any;
  let roomServiceMock: any;
  let userServiceMock: any;

  beforeEach(async () => {
    bookingServiceMock = {
      getAllBookings: jasmine.createSpy().and.returnValue(of([])),
      getBookingsByUser: jasmine.createSpy().and.returnValue(of([])),
      createBooking: jasmine.createSpy(),
      deleteBooking: jasmine.createSpy().and.returnValue(of({})),
    };

    roomServiceMock = {
      getRoomById: jasmine.createSpy().and.returnValue(of({ id: 1 })),
    };

    userServiceMock = {
      getCurrentUser: jasmine.createSpy().and.returnValue({ id: 1, role: 'USER' }),
      isAdmin: jasmine.createSpy().and.returnValue(false),
    };

    await TestBed.configureTestingModule({
      imports: [Bookings],
      providers: [
        { provide: BookingService, useValue: bookingServiceMock },
        { provide: RoomService, useValue: roomServiceMock },
        { provide: UserService, useValue: userServiceMock },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: () => '1',
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Bookings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  //  Initialization
  it('should initialize booking with user and room', () => {
    expect(component.newBooking().userId).toBe(1);
    expect(component.newBooking().roomId).toBe(1);
  });

  //  Validation
  it('should show error when fields are missing', () => {
    component.newBooking.set({
      userId: 0,
      roomId: 0,
      checkInDate: '',
      checkOutDate: '',
    });

    component.createBooking();

    expect(component.errorMessage()).toBe('Please fill in all fields');
  });

  //  Successful booking
  it('should create booking successfully', () => {
    const mockBooking = { id: 1 };

    bookingServiceMock.createBooking.and.returnValue(of(mockBooking));

    component.newBooking.set({
      userId: 1,
      roomId: 1,
      checkInDate: '2026-12-01',
      checkOutDate: '2026-12-05',
    });

    component.createBooking();

    expect(component.successMessage()).toBe('Booking created successfully');
    expect(component.bookings().length).toBe(1);
  });

  //  Delete booking
  it('should delete booking from list', () => {
    component.bookings.set([{ id: 1 } as any]);

    component.deleteBooking(1);

    expect(component.bookings().length).toBe(0);
  });
});

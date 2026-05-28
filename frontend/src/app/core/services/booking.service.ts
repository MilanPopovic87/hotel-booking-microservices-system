import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from '../config/api.config';

import { BookingResponse } from '../models/booking-response';
import { BookingRequest } from '../models/booking-request';

@Injectable({
  providedIn: 'root',
})
export class BookingService {
  private apiUrl = API.BOOKINGS;

  constructor(private http: HttpClient) {}

  // =========================
  // GET ALL BOOKINGS
  // =========================
  getAllBookings(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(this.apiUrl);
  }

  // =========================
  // GET BOOKING BY ID
  // =========================
  getBookingById(id: number): Observable<BookingResponse> {
    return this.http.get<BookingResponse>(`${this.apiUrl}/${id}`);
  }

  /// =========================
  // GET MY BOOKINGS
  // =========================
  getMyBookings(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.apiUrl}/my`);
  }

  // =========================
  // GET BOOKINGS BY ROOM
  // =========================
  getBookingsByRoom(roomId: number): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.apiUrl}/by-room/${roomId}`);
  }

  // =========================
  // CREATE BOOKING
  // =========================
  createBooking(request: BookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(this.apiUrl, request);
  }

  // =========================
  // UPDATE BOOKING
  // =========================
  updateBooking(id: number, request: BookingRequest): Observable<BookingResponse> {
    return this.http.put<BookingResponse>(`${this.apiUrl}/${id}`, request);
  }

  // =========================
  // DELETE BOOKING
  // =========================
  deleteBooking(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

// src/app/services/user.service.ts
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API } from '../config/api.config';
import { Router } from '@angular/router';
import { ChatService } from './chat.service';
import { UserResponse } from '../models/user-response';
import { AuthResponse } from '../models/auth-response';
import { RegisterRequest } from '../models/register-request';
import { UpdateUserRequest } from '../models/update-user-request';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = API.USERS;
  private authUrl = API.AUTH;

  currentUser = signal<UserResponse | null>(null);
  private logoutTimer?: ReturnType<typeof setTimeout>; // for auto logout

  constructor(
    private http: HttpClient,
    private router: Router,
    private chatService: ChatService,
  ) {
    this.restoreFromStorage();
  }

  // -------------------
  // AUTH
  // -------------------
  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, {
      username,
      password,
    });
  }

  handleLoginSuccess(res: AuthResponse): Observable<UserResponse> {
    // save token
    localStorage.setItem('token', res.token);

    this.setupAutoLogout(res.token);

    // load current user from backend
    return this.getMe().pipe(
      tap((user) => {
        this.currentUser.set(user);
        localStorage.setItem('user', JSON.stringify(user));
      }),
    );
  }

  getMe(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUser.set(null);
    this.chatService.clear();
    this.router.navigate(['/login']);
  }

  private restoreFromStorage() {
    const token = localStorage.getItem('token');

    if (!token) return;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Math.floor(Date.now() / 1000);

      if (!payload.exp || payload.exp < now) {
        this.logout();
        return;
      }

      // Restore cached user immediately
      const userJson = localStorage.getItem('user');

      if (userJson) {
        const cachedUser = JSON.parse(userJson) as UserResponse;
        this.currentUser.set(cachedUser);
      }

      // Setup auto logout
      this.setupAutoLogout(token);

      // Refresh authoritative user from backend
      this.getMe().subscribe({
        next: (user) => {
          this.currentUser.set(user);
          localStorage.setItem('user', JSON.stringify(user));
        },
        error: (err) => {
          console.error('Failed to load user:', err);

          // Only logout if token is invalid
          if (err.status === 401 || err.status === 403) {
            this.logout();
          }
        },
      });
    } catch {
      this.logout();
    }
  }

  private setupAutoLogout(token: string) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiresInMs = payload.exp * 1000 - Date.now();

      if (expiresInMs <= 0) {
        this.logout();
        return;
      }

      if (this.logoutTimer) {
        clearTimeout(this.logoutTimer);
      }

      this.logoutTimer = setTimeout(() => this.logout(), expiresInMs);
    } catch {
      this.logout();
    }
  }

  getToken(): string | null {
    const token = localStorage.getItem('token');
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Math.floor(Date.now() / 1000);

      if (payload.exp < now) {
        this.logout();
        return null;
      }

      return token;
    } catch {
      this.logout();
      return null;
    }
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === 'ADMIN';
  }

  getCurrentUser(): UserResponse | null {
    return this.currentUser();
  }

  // -------------------
  // CRUD methods
  // -------------------
  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.apiUrl);
  }

  getUserById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/${id}`);
  }

  getUserByUsername(username: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/by-username/${username}`);
  }

  register(request: RegisterRequest): Observable<void> {
    return this.http.post<void>(`${this.authUrl}/register`, request);
  }

  updateUser(id: number, request: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

import { environment } from '../../../environments/environment';

export const API = {
  AUTH: `${environment.apiBaseUrl}/auth`,
  USERS: `${environment.apiBaseUrl}/users`,
  ROOMS: `${environment.apiBaseUrl}/rooms`,
  BOOKINGS: `${environment.apiBaseUrl}/bookings`,
};

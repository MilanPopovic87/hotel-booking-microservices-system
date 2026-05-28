export interface UpdateUserRequest {
  username: string;
  password?: string;
  role: 'USER' | 'ADMIN';
}

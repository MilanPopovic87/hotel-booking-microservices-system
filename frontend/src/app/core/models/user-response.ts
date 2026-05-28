export interface UserResponse {
  id: number;
  username: string;
  role: 'USER' | 'ADMIN';
}

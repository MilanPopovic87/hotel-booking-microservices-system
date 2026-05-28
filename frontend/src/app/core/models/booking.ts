export interface Booking {
  id?: number; // optional for new bookings
  checkInDate: string; // YYYY-MM-DD format
  checkOutDate: string;
  userId: number;
  roomId: number;
  roomName?: string; // optional, can use for display
  username?: string; // optional, can use for display
}

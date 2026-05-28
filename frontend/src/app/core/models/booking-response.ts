export interface BookingResponse {
  id: number;
  checkInDate: string;
  checkOutDate: string;

  userId: number;
  roomId: number;

  username: string;
  roomName: string;
}

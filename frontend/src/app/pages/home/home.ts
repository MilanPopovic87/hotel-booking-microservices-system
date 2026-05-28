import { Component, OnInit, signal } from '@angular/core';
import { Room } from '../../core/models/room';
import { RoomService } from '../../core/services/room.service';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [CommonModule, MatCardModule, MatButtonModule],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  rooms = signal<Room[]>([]);
  loading = signal(true);

  constructor(private roomService: RoomService, private router: Router) {}

  ngOnInit(): void {
    this.loadRooms();
  }

  private loadRooms(): void {
    this.roomService.getAllRooms().subscribe({
      next: (data) => {
        this.rooms.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load rooms', err);
        this.loading.set(false);
      },
    });
  }

  trackById(index: number, room: Room) {
    return room.id;
  }

  goToBooking(roomId: number) {
    this.router.navigate(['/bookings'], {
      queryParams: { roomId },
    });
  }
}

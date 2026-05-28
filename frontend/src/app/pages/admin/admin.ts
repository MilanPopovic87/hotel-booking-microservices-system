import { Component, OnInit, signal } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Room } from '../../core/models/room';
import { UserService } from '../../core/services/user.service';
import { RoomService } from '../../core/services/room.service';
import { UserDialog, UserDialogData } from './user-dialog/user-dialog';
import { RoomDialog, RoomDialogData } from './room-dialog/room-dialog';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { UserResponse } from '../../core/models/user-response';

@Component({
  selector: 'app-admin',
  standalone: true,
  templateUrl: './admin.html',
  styleUrls: ['./admin.scss'],
  imports: [
    MatTableModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    CommonModule,
    UserDialog,
    RoomDialog,
  ],
})
export class Admin implements OnInit {
  // Users
  users = signal<UserResponse[]>([]);
  displayedUserColumns = ['id', 'username', 'role', 'actions'];

  // Rooms
  rooms = signal<Room[]>([]);
  displayedRoomColumns = ['id', 'name', 'type', 'price', 'available', 'actions'];

  constructor(
    private userService: UserService,
    private roomService: RoomService,
    private dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.fetchUsers();
    this.fetchRooms();
  }

  // ================= USERS =================

  fetchUsers() {
    this.userService.getAllUsers().subscribe((users) => this.users.set(users));
  }

  openUserDialog(user?: UserResponse) {
    const dialogRef = this.dialog.open(UserDialog, {
      width: '400px',
      data: { user } as UserDialogData,
    });

    dialogRef.afterClosed().subscribe((result: UserResponse | undefined) => {
      if (result) {
        this.fetchUsers(); // users were saved inside dialog
      }
    });
  }

  deleteUser(user: UserResponse) {
    if (confirm(`Delete user "${user.username}"?`)) {
      this.userService.deleteUser(user.id).subscribe({
        next: () => this.fetchUsers(),
        error: (err) => {
          alert(err.error?.message || 'Deletion failed');
        },
      });
    }
  }

  // ================= ROOMS =================

  fetchRooms() {
    this.roomService.getAllRooms().subscribe((rooms) => this.rooms.set(rooms));
  }

  openRoomDialog(room?: Room) {
    const dialogRef = this.dialog.open(RoomDialog, {
      width: '400px',
      data: { room } as RoomDialogData,
    });

    dialogRef.afterClosed().subscribe((result: Room | undefined) => {
      if (result) {
        this.fetchRooms(); // rooms were saved inside dialog
      }
    });
  }

  deleteRoom(room: Room) {
    if (confirm(`Delete room "${room.name}"?`)) {
      this.roomService.deleteRoom(room.id).subscribe({
        next: () => this.fetchRooms(),
        error: (err) => {
          alert(err.error?.message || 'Deletion failed');
        },
      });
    }
  }
}

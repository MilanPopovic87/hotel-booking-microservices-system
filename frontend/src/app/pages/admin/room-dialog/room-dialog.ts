import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { Room } from '../../../core/models/room';
import { RoomService } from '../../../core/services/room.service';
import { CommonModule } from '@angular/common';

export interface RoomDialogData {
  room?: Room; // if present → edit, else → create
}

@Component({
  selector: 'app-room-dialog',
  standalone: true,
  templateUrl: './room-dialog.html',
  styleUrls: ['./room-dialog.scss'],
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    ReactiveFormsModule,
  ],
})
export class RoomDialog {
  roomForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<RoomDialog>,
    @Inject(MAT_DIALOG_DATA) public data: RoomDialogData,
    private roomService: RoomService,
  ) {
    this.roomForm = this.fb.group({
      name: [data.room?.name || '', Validators.required],
      type: [data.room?.type || 'STANDARD', Validators.required],
      price: [data.room?.price || 0, [Validators.required, Validators.min(0)]],
      available: [data.room?.available ?? true],
    });
  }

  private saveRoom(room: Room) {
    if (room.id != null) {
      // Room exists → update
      return this.roomService.updateRoom(room.id, room);
    } else {
      // New room → create
      return this.roomService.createRoom(room);
    }
  }

  save(): void {
    if (!this.roomForm.valid) {
      this.roomForm.markAllAsTouched(); // show validation errors
      return;
    }

    const room: Room = { ...this.data.room, ...this.roomForm.value };

    this.saveRoom(room).subscribe({
      next: (savedRoom) => this.dialogRef.close(savedRoom),
      error: (err) => {
        if (err.status === 409) {
          // Room name conflict → show error under name field
          this.roomForm.get('name')?.setErrors({ exists: true });
        } else {
          // Other errors
          console.error('Error saving room:', err);
        }
      },
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

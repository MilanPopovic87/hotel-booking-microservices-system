import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../core/services/user.service';
import { UserResponse } from '../../../core/models/user-response';
import { UpdateUserRequest } from '../../../core/models/update-user-request';
import { RegisterRequest } from '../../../core/models/register-request';
import { Observable } from 'rxjs';

export interface UserDialogData {
  user?: UserResponse; // if present → edit, else → create
}

@Component({
  selector: 'app-user-dialog',
  standalone: true,
  templateUrl: './user-dialog.html',
  styleUrls: ['./user-dialog.scss'],
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    ReactiveFormsModule,
  ],
})
export class UserDialog {
  userForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<UserDialog>,
    @Inject(MAT_DIALOG_DATA) public data: UserDialogData,
    private userService: UserService,
  ) {
    this.userForm = this.fb.group({
      username: [data.user?.username || '', Validators.required],
      password: ['', data.user ? [] : Validators.required],
      role: [data.user?.role || 'USER', Validators.required],
    });
  }

  private updateUser$(): Observable<UserResponse> {
    const formValue = this.userForm.value;

    const request: UpdateUserRequest = {
      username: formValue.username,
      role: formValue.role,
    };

    if (formValue.password?.trim()) {
      request.password = formValue.password;
    }

    return this.userService.updateUser(this.data.user!.id, request);
  }

  private createUser$(): Observable<void> {
    const formValue = this.userForm.value;

    const request: RegisterRequest = {
      username: formValue.username,
      password: formValue.password,
    };

    return this.userService.register(request);
  }

  save(): void {
    // Validate form
    if (!this.userForm.valid) {
      this.userForm.markAllAsTouched();
      return;
    }

    // EDIT existing user
    if (this.data.user) {
      this.updateUser$().subscribe({
        next: () => this.dialogRef.close(true),

        error: (err: any) => {
          if (err.status === 409) {
            this.userForm.get('username')?.setErrors({ exists: true });
          } else {
            console.error('Error updating user:', err);
          }
        },
      });

      return;
    }

    // CREATE new user
    this.createUser$().subscribe({
      next: () => this.dialogRef.close(true),

      error: (err: any) => {
        if (err.status === 409) {
          this.userForm.get('username')?.setErrors({ exists: true });
        } else {
          console.error('Error creating user:', err);
        }
      },
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

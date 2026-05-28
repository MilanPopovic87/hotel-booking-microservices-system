import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  username = '';
  password = '';
  error: string | null = null;
  redirectTo: string | null = null;

  constructor(
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute,
  ) {
    this.redirectTo = this.route.snapshot.queryParamMap.get('redirectTo');
  }

  onLogin(form: NgForm) {
    this.error = null;

    if (!this.username || !this.password) {
      this.error = 'Username and password are required';
      return;
    }

    this.userService.login(this.username, this.password).subscribe({
      next: (res) => {
        this.userService.handleLoginSuccess(res); // stores token + user
        form.resetForm();
        this.router.navigateByUrl(this.redirectTo ?? '/');
      },
      error: () => {
        this.error = 'Invalid credentials or user does not exist. Contact admin.';
      },
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-logout',
  standalone: false,
  templateUrl: './logout.component.html',
  styleUrl: './logout.component.css'
})
export class LogoutComponent implements OnInit {
isLoggingOut: any;

  constructor(private authService : AuthService) { }
  ngOnInit(): void {
    if(!this.authService.isLoggedIn()){
      this.authService.logout();
    }
  }

  logout(): void {
    const confirmLogout = window.confirm("Are you sure you want to logout?");
    if (confirmLogout) {
      this.authService.logout();
    }
  }
}

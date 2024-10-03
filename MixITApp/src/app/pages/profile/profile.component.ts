import {Component, OnInit} from '@angular/core';
import {GlobalConfiguration} from "../../config/GlobalConfiguration";
import {Router} from "@angular/router";
import {AuthService} from "../../service/auth.service";
import {User} from "../../model/user/User";
import {HotToastService} from "@ngneat/hot-toast";
import {UserService} from "../../service/user.service";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit{
  protected readonly GlobalConfiguration = GlobalConfiguration;
  constructor(
    private router: Router,
    private authService: AuthService,
    private toast: HotToastService,
    private userService: UserService
  ) { }
  isAuthenticated: boolean = false;
  user: User = {};
  async ngOnInit(): Promise<void> {
    this.user = await this.userService.getUserByToken();
    this.isAuthenticated = this.authService.isAuthenticated();
  }
  logout() {
    this.authService.logout()
      .then(res => {
        this.toast.success("Logged out");
        this.router.navigate(['/login']).then();
      })
  }
  goto(url: string) {
    if(url === 'login' && this.isAuthenticated){
      return;
    }
    this.router.navigateByUrl(url).then();
  }
}

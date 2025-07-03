import {Component, OnInit} from '@angular/core';
import {UserService} from "../../service/user.service";
import {User} from "../../model/user/User";
import {ActivatedRoute, Router} from "@angular/router";
import {NavigationService} from "../../service/navigation.service";
import {Post} from "../../model/post/Post";

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit{
  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    public navigationService: NavigationService
  ) { }
  user: User = {badges: [] };
  userId: string = '';
  loggedUserId: string = '';
  userPosts: Post[] = [];
  currentPostPage = 0;
  isLoading = true;
  async ngOnInit(): Promise<void> {
    this.isLoading = true;
    try {
      this.userId = this.route.snapshot.params['id'] as string;
      this.user = await this.userService.getUserById(this.route.snapshot.params['id'] as string)
      const [user, posts, loggedUser] = await Promise.all([
        this.userService.getUserProfileById(this.userId),
        this.userService.getUserPosts(this.userId, this.currentPostPage),
        this.userService.getUserByToken(),
      ]);

      // this.user = user;
      this.userPosts = posts;
      this.loggedUserId = loggedUser.userId!;
    } catch (error) {
      console.error('Error loading profile:', error);
    } finally {
      this.isLoading = false;
    }
  }
  isVisitor(): boolean {
    return this.loggedUserId !== this.userId;
  }
  gotoSettings(): void{
    this.router.navigate(['profile']);
  }
  async loadMorePosts(): Promise<void> {
    this.currentPostPage++;
    let newPosts: Post[] = [];
    newPosts = await this.userService.getUserPosts(this.userId, this.currentPostPage);
    this.userPosts = [...this.userPosts, ...newPosts];
  }
  async toggleFollow(): Promise<void> {
    this.user.isFollowing
      ? await this.userService.unfollowUser(this.user.userId!)
      : await this.userService.followUser(this.user.userId!);

    this.user = await this.userService.getUserProfileById(this.user.userId!);
    location.reload();
  }
  goto(url: string) {
    this.router.navigate([url]);
  }
  gotoFollowing(): void {
    this.router.navigate([`/user-profile/${this.userId}/following`]);
  }
  gotoFollowers(): void {
    this.router.navigate([`/user-profile/${this.userId}/followers`]);
  }
}

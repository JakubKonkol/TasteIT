import {ChangeDetectorRef, Component, DestroyRef, HostListener, inject, OnDestroy, OnInit, ViewContainerRef} from '@angular/core';
import {Router} from "@angular/router";
import {Subject} from "rxjs";
import {Post} from "../../model/post/Post";
import {User} from "../../model/user/User";
import {PostService} from "../../service/post.service";
import {SplashScreenFactoryService} from "../../service/factories/splash-screen-factory.service";
import {BodyScrollService} from "../../service/body-scroll.service";
import {UserService} from "../../service/user.service";
import {ScrollPositionService} from "../../service/scroll-position.service";
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {animate, style, transition, trigger} from "@angular/animations";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('200ms', style({ opacity: 1 }))
      ])
    ])
  ]
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  private readonly destroy$ = new Subject<void>();

  greeting = this.getGreetingDependingOnTime();
  posts: Post[] = [];
  user?: User;

  private page = 0;
  private readonly size = 20;
  protected loading = false;
  targetElement!: Element;

  constructor(
    private router: Router,
    private postService: PostService,
    private splashScreenFactoryService: SplashScreenFactoryService,
    private viewContainerRef: ViewContainerRef,
    private bodyScrollService: BodyScrollService,
    private userService: UserService,
    private scrollPositionService: ScrollPositionService,
    private cdr: ChangeDetectorRef
  ) {
    this.splashScreenFactoryService.setRootViewContainerRef(this.viewContainerRef);
  }
  trackByPostId(_: number, post: Post): string {
    if (!post) {
      console.warn('Received undefined post in trackByPostId');
      return _.toString();
    }
    return post.postId ?? _.toString();
  }
  async ngOnInit(): Promise<void> {
    this.targetElement = document.querySelector('html') as Element;
    try {
      this.user = await this.userService.getUserByToken();

      if (this.user?.firstLogin) {
        this.initializeCompleteAccountSplashScreen();
      }
      await this.initializePosts();
    } catch (error) {
      console.error('Initialization error:', error);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('window:scroll')
  onScroll(): void {
    const scrollPosition = window.scrollY;
    this.scrollPositionService.setScrollPosition(scrollPosition);
  }
  private async initializePosts(): Promise<void> {
    const cachedPosts = this.postService.getFeedState();

    if (cachedPosts.length > 0) {
      this.posts = cachedPosts;
      const savedPosition = this.scrollPositionService.getScrollPosition();
      requestAnimationFrame(() => window.scrollTo(0, savedPosition));
      await this.updateCachedPosts();
    } else {
      await this.refreshPosts();
    }
  }

  async updateCachedPosts(): Promise<void> {
    const updatePromises = this.posts.map(async post => {
      try {
        const updatedPost = await this.postService.getPostById(post.postId!);
        if (this.hasPostChanged(post, updatedPost)) {
          return updatedPost;
        }
        return post;
      } catch (error) {
        console.error(`Error updating post ${post.postId}: `, error);
        return post;
      }
    });

    this.posts = await Promise.all(updatePromises);
    this.cdr.detectChanges();
  }

  private hasPostChanged(oldPost: Post, newPost: Post): boolean {
    return oldPost.likesCount !== newPost.likesCount ||
      oldPost.commentsCount !== newPost.commentsCount;
  }

  async refreshPosts(): Promise<void> {
    this.postService.clearFeedCache();
    this.page = 0;
    this.posts = [];
    await this.loadPost();
    this.scrollPositionService.setScrollPosition(0);
    requestAnimationFrame(() => window.scrollTo(0, 0));
  }

  handleRefreshEvent(event: Subject<any>): void {
    setTimeout(async () => {
      await this.refreshPosts();
      event.next(event);
    }, 500);
  }

  gotoPost(id: string): void {
    this.scrollPositionService.setScrollPosition(window.scrollY);
    this.router.navigate([`/drink/${id}`]);
  }

  getGreetingDependingOnTime(): string {
    const currentHour = new Date().getHours();
    if (currentHour >= 5 && currentHour < 12) return "Good Morning!";
    if (currentHour >= 12 && currentHour < 18) return "Good Afternoon!";
    return "Good Evening!";
  }

  async loadPost(): Promise<void> {
    if (this.loading) return;

    this.loading = true;
    try {
      const newPosts = await this.postService.getFeed(this.page, this.size);
      this.posts = [...this.posts, ...newPosts];
      this.page++;
      this.cdr.detectChanges();
    } catch (error) {
      console.error('Error loading posts:', error);
    } finally {
      this.loading = false;
    }
  }

  async onLikeEvent(postId: string): Promise<void> {
    try {
      const updatedPost = await this.postService.getPostById(postId);
      const postIndex = this.posts.findIndex(post => post.postId === postId);

      if (postIndex !== -1) {
        this.posts[postIndex] = updatedPost;
        this.cdr.detectChanges();
      }
    } catch (error) {
      console.error('Error updating like:', error);
    }
  }

  private initializeCompleteAccountSplashScreen(): void {
    const splashScreenData = {
      title: 'Complete your profile',
      content: [
        {
          icon: 'assets/wand.svg',
          subtitle: 'Personalize your experience',
          text: 'Tailor content to your tastes and discover recipes you\'ll love.'
        },
        {
          icon: 'assets/users.svg',
          subtitle: 'Connect with like-minded foodies',
          text: 'Share your passion and build a community around your favorite dishes.'
        },
        {
          icon: 'assets/unlock.svg',
          subtitle: 'Unlock exclusive features',
          text: 'Get access to personalized recommendations and community-driven culinary insights.'
        }
      ],
      actionButtonLabel: 'Continue',
      closeButtonLabel: 'Maybe later'
    };

    this.bodyScrollService.disableScroll();

    const componentRef = this.splashScreenFactoryService.addDynamicComponent(
      splashScreenData.title,
      splashScreenData.content,
      splashScreenData.actionButtonLabel,
      splashScreenData.closeButtonLabel
    );

    componentRef.instance.actionButton
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.router.navigate(['/setup-profile']);
      });

    componentRef.instance.close
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.splashScreenFactoryService.removeDynamicComponent(componentRef);
        this.bodyScrollService.enableScroll();
      });
  }
}

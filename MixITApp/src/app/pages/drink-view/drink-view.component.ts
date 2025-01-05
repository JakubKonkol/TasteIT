import {Component, OnDestroy, OnInit, ViewContainerRef} from '@angular/core';
import {Post} from "../../model/post/Post";
import {ActivatedRoute, Router} from "@angular/router";
import {HotToastService} from "@ngneat/hot-toast";
import {InstructionsFactoryService} from "../../service/factories/instructions-factory.service";
import {BodyScrollService} from "../../service/body-scroll.service";
import {IngredientViewFactoryService} from "../../service/factories/ingredient-view-factory.service";
import {NavigationService} from "../../service/navigation.service";
import {PostService} from "../../service/post.service";
import {Ingredient} from "../../model/post/Ingredient";
import {Recipe} from "../../model/post/Recipe";
import {CommentsSectionFactoryService} from "../../service/factories/comments-section-factory.service";
import {UserService} from "../../service/user.service";
import {User} from "../../model/user/User";
import {AddToFoodlistFactoryService} from "../../service/factories/add-to-foodlist-factory.service";
import {Subject, takeUntil} from "rxjs";

@Component({
  selector: 'app-drink-view',
  templateUrl: './drink-view.component.html',
  styleUrls: ['./drink-view.component.css']
})
export class DrinkViewComponent implements OnInit, OnDestroy{
 activePost!: Post;
 recipe!: Recipe;
 drinkId!: string;
 isLoaded: boolean = false;
 postAuthor: User = {};
 isPostLikedByCurrentUser: boolean = false;
 currentUserId: string = '';
 destroy$ = new Subject<void>();
 constructor(private route: ActivatedRoute,
             private toast: HotToastService,
             private instructionsFactoryService: InstructionsFactoryService,
             private viewContainerRef: ViewContainerRef,
             private bodyScrollService: BodyScrollService,
             private ingredientViewFactoryService: IngredientViewFactoryService,
             public navigationService: NavigationService,
             private router: Router,
             private postService: PostService,
             private commentsSectionFactoryService: CommentsSectionFactoryService,
             private userService: UserService,
             private addToFoodlistFactoryService: AddToFoodlistFactoryService
 ){
   this.instructionsFactoryService.setRootViewContainerRef(this.viewContainerRef);
   this.ingredientViewFactoryService.setRootViewContainerRef(this.viewContainerRef);
   this.commentsSectionFactoryService.setRootViewContainerRef(this.viewContainerRef);
   this.addToFoodlistFactoryService.setRootViewContainerRef(this.viewContainerRef);
 }
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  async ngOnInit(): Promise<void> {
    this.route.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.drinkId = params['id'];
      });

    try {
      const [user, activePost] = await Promise.all([
        this.userService.getUserByToken(),
        this.postService.getPostById(this.drinkId)
      ]);

      this.currentUserId = user.userId!;
      this.activePost = activePost;

      const [recipe, postAuthor] = await Promise.all([
        this.getRecipe(),
        this.userService.getUserById(this.activePost.postAuthorDto?.userId!)
      ]);
      this.recipe = recipe;
      this.postAuthor = postAuthor;
      this.isPostLikedByCurrentUser = this.activePost.likedByCurrentUser || false;
    } catch (e) {
      this.toast.error("Check your internet connection and try again");
      await this.router.navigate(['/home']);
    } finally {
      this.isLoaded = true;
    }
  }
  gotoProfile(userId: string){
    this.router.navigate(['/user-profile', userId]).then();
  }

  getAuthorName(): string{
   return this.postAuthor.displayName || "Unknown"
  }

   getIngredients(): Ingredient[] {
    return this.recipe.ingredientsWithMeasurements?.map((ingredient) => {
      return ingredient;
    }) || [];
  }

  refreshPost(){
    this.postService.getPostById(this.activePost.postId!).then((post) => {
      this.activePost = post;
    })
  }

  async getRecipe() {
    return await this.postService.getPostRecipe(this.activePost.postId!);
  }
  async toggleLike(){
   if(this.isPostLikedByCurrentUser){
     await this.postService.unlikePost(this.activePost.postId!)
     await this.ngOnInit();
   }else{
      await this.postService.likePost(this.activePost.postId!)
      await this.ngOnInit();
   }
  }
  async initializeInstructionsView(post: Post) {
    const recipe = await this.getRecipe();
    window.scrollTo(0, 0);
    this.bodyScrollService.disableScroll();
    const componentRef = this.instructionsFactoryService.addDynamicComponent(post, recipe);

    componentRef.instance.close.subscribe(() => {
      this.instructionsFactoryService.removeDynamicComponent(componentRef)
      this.bodyScrollService.enableScroll();
    });
  }

   initializeIngredientView(id: number) {
    let ingredient = this.getIngredients().find((ingredient) => ingredient.ingredientId === id);
    if (!ingredient) {
      this.toast.error("Cannot find ingredient with id: " + id)
      return;
    }
    const componentRef = this.ingredientViewFactoryService.addDynamicComponent(ingredient);
    componentRef.instance.close.subscribe(() => {
      this.ingredientViewFactoryService.removeDynamicComponent(componentRef)
    })
  }

  initializeCommentSection(postId: string) {
    window.scrollTo(0, 0);
    this.bodyScrollService.disableScroll();
   const componentRef = this.commentsSectionFactoryService.addDynamicComponent(postId);
   componentRef.instance.close.subscribe(() => {
      this.bodyScrollService.enableScroll();
      this.commentsSectionFactoryService.removeDynamicComponent(componentRef)
   })
    componentRef.instance.refreshPost.subscribe(() => {
      this.refreshPost();
    })
  }
  initializeAddToFoodList(){
   const componentRef = this.addToFoodlistFactoryService.addDynamicComponent(this.currentUserId, this.drinkId);
    componentRef.instance.close.subscribe(() => {
      this.addToFoodlistFactoryService.removeDynamicComponent(componentRef)
    })
  }

}

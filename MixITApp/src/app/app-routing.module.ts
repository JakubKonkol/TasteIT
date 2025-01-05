import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {HomeComponent} from "./pages/home/home.component";
import {DrinkBuilderComponent} from "./pages/drink-builder/drink-builder.component";
import {ProfileComponent} from "./pages/profile/profile.component";
import {SearchComponent} from "./pages/search/search.component";
import {DrinkViewComponent} from "./pages/drink-view/drink-view.component";
import {CategoryViewComponent} from "./pages/category-view/category-view.component";
import {LoginComponent} from "./pages/login/login.component";
import {RegisterComponent} from "./pages/register/register.component";
import {WelcomeComponent} from "./pages/welcome/welcome.component";
import {AuthGuard} from "./guards/AuthGuard";
import {LoginGuard} from "./guards/LoginGuard";
import {SetupProfileComponent} from "./pages/setup-profile/setup-profile.component";
import {FoodlistsComponent} from "./pages/foodlists/foodlists.component";
import {UserProfileComponent} from "./pages/user-profile/user-profile.component";
import {UserLikesComponent} from "./pages/user-likes/user-likes.component";
import {PostBuilderComponent} from "./pages/post-builder/post-builder.component";
import {
  FollowingFollowersListComponent
} from "./components/following-followers-list/following-followers-list.component";
import {TagViewComponent} from "./pages/tag-view/tag-view.component";
import {FoodlistViewComponent} from "./pages/foodlist-view/foodlist-view.component";

const routes: Routes = [
  {path: '', redirectTo:'/welcome', pathMatch: 'full'},
  {path: 'welcome', component: WelcomeComponent, data: {showNav: false}, canActivate: [LoginGuard]},
  {path: 'home', component: HomeComponent, canActivate: [AuthGuard]},
  {path: 'drinkBuilder', component: DrinkBuilderComponent, canActivate: [AuthGuard]},
  {path: 'profile', component: ProfileComponent, canActivate: [AuthGuard]},
  {path: 'search', component: SearchComponent, canActivate: [AuthGuard]},
  {path: "drink/:id", component: DrinkViewComponent, data: {showNav: false}, canActivate: [AuthGuard]},
  {path: "category/:category", component: CategoryViewComponent, canActivate: [AuthGuard]},
  {path: 'login', component: LoginComponent, data: {showNav: false}, canActivate: [LoginGuard]},
  {path: 'register', component: RegisterComponent, data: {showNav: false}, canActivate: [LoginGuard]},
  {path: 'setup-profile', component: SetupProfileComponent, data: {showNav: false}, canActivate: [AuthGuard]},
  {path: 'foodlists', component: FoodlistsComponent, data: {showNav: true}, canActivate: [AuthGuard]},
  {path: 'foodlist/:id', component:FoodlistViewComponent, data: {showNav: true}, canActivate: [AuthGuard]},
  {path: 'user-profile/:id', component: UserProfileComponent, data: {showNav: true}, canActivate: [AuthGuard]},
  {path: 'userLikes', component: UserLikesComponent, data: {showNav: false}, canActivate: [AuthGuard]},
  {path: 'postBuilder', component: PostBuilderComponent, data: {showNav: false}, canActivate: [AuthGuard]},
  {path: 'user-profile/:id/following', component: FollowingFollowersListComponent, data: {showNav: true}, canActivate: [AuthGuard]},
  {path: 'user-profile/:id/followers', component: FollowingFollowersListComponent, data: {showNav: true}, canActivate: [AuthGuard]},
  {path: 'tag/:id', component: TagViewComponent, data: {showNav: true}, canActivate: [AuthGuard]}

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

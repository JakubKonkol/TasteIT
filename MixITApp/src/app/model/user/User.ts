import {Like} from "../post/Like";
import {FoodList} from "../FoodList";
import {Badge} from "./Badge";
import {Tag} from "./Tag";
import {Authentication} from "./Authentication";

export interface User {
  userId?: string;
  email?: string;
  displayName?: string;
  bio?: string;
  profilePicture?: string;
  createdAt?: Date;
  birthDate?: Date;
  firstLogin?: boolean;
  roles?: string[];
  likes?: Like[];
  foodLists?: FoodList[];
  followersCount?: number;
  followingCount?: number;
  badges?: Badge[];
  preferences?: Tag[];
  isFollowing?: boolean;
  authentication?: Authentication;
}


import {PostMedia} from "./PostMedia";
import {Recipe} from "./Recipe";
import {Tag} from "../user/Tag";
import {Like} from "./Like";
import {EPostType} from "./EPostType";
import {PostAuthor} from "./PostAuthor";

export interface Post {
  postId?: string;
  postAuthorDto?: PostAuthor
  userId?: string;
  postType?: EPostType;
  postMedia?: PostMedia;
  tags?: Tag[];
  createdDate?: Date;
  likesCount?: number;
  recipe?: Recipe;
  commentsCount?: number;
  likedByCurrentUser?: boolean;

}

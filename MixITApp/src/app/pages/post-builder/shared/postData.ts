import {PostMedia} from "../../../model/post/PostMedia";
import {Recipe} from "../../../model/post/Recipe";
import {Tag} from "../../../model/user/Tag";
import {EPostType} from "../../../model/post/EPostType";

export type PostData = {
  postMedia: PostMedia;
  recipe: Recipe;
  tags: Tag[];
  postType: EPostType;
}

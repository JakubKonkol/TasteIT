import {Injectable} from "@angular/core";
import {Post} from "../model/post/Post";
import taste_api from "../api/taste_api";
import {EPostType} from "../model/post/EPostType";
import {User} from "../model/user/User";
import {Tag} from "../model/user/Tag";
import {Ingredient} from "../model/post/Ingredient";
import {from, map, Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SearchService {

  async searchPosts(query: string, page?: number, size?: number, type?: EPostType,): Promise<Post[]> {
    try {
      const params: any = { query };
      if (type !== undefined) params.type = type;
      if (page !== undefined) params.page = page;
      if (size !== undefined) params.size = size;

      const res = await taste_api.get('search/posts', { params });
      return res.data.content as Post[];
    } catch (error: any) {
      return Promise.reject(error.response?.data || error);
    }
  }
  async searchUsers(query: string, page?: number, size?: number): Promise<User[]> {
    try {
      const params: any = { query };
      if (page !== undefined) params.page = page;
      if (size !== undefined) params.size = size;

      const res = await taste_api.get('search/users', { params });
      return res.data.content as User[];
    } catch (error: any) {
      return Promise.reject(error.response?.data || error);
    }
  }
  async searchTags(query: string, page?: number, size?: number): Promise<Tag[]> {
    try {
      const params: any = { query };
      if (page !== undefined) params.page = page;
      if (size !== undefined) params.size = size;

      const res = await taste_api.get('search/tags', { params });
      return res.data as Tag[];
    } catch (error: any) {
      return Promise.reject(error.response?.data || error);
    }
  }
  async getPostsByTag(tagId: string, page?: number, size?: number): Promise<Post[]> {
    try {
      const params: any = {tagId};
      if (page !== undefined) params.page = page;
      if (size !== undefined) params.size = size;

      const res = await taste_api.get(`search/tags/posts`, { params });
      return res.data.content as Post[];
    } catch (error: any) {
      return Promise.reject(error.response?.data || error);
    }
  }
  searchIngredients(query: string, page?: number, size?: number): Observable<Ingredient[]> {
    const params: any = { query };
    if (page !== undefined) params.page = page;
    if (size !== undefined) params.size = size;

    return from(taste_api.get('search/ingredients', { params }))
      .pipe(
        map(res => res.data.content as Ingredient[])
      );
  }



}

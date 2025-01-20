import { Injectable } from "@angular/core";
import taste_api from "../api/taste_api";
import { BehaviorSubject } from "rxjs";
import { Post } from "../model/post/Post";
import { Recipe } from "../model/post/Recipe";
import { Comment } from "../model/post/Comment";
import { LoggerService } from "./logger.service";
import {GenericResponse} from "../model/GenericResponse";
import {GlobalConfiguration} from "../config/GlobalConfiguration";

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private feedSubject = new BehaviorSubject<Post[]>([]);
  feed$ = this.feedSubject.asObservable();

  constructor(private logger: LoggerService) {}

  setFeed(feed: Post[]): void {
    this.feedSubject.next(feed);
  }

  getFeedState(): Post[] {
    return this.feedSubject.getValue();
  }

  clearFeedCache(): void {
    this.feedSubject.next([]);
  }

  async getFeed(page: number, size: number): Promise<Post[]> {
    let feed_url;
    GlobalConfiguration.USE_RECOMMENDATION_ALGORITHM ? feed_url = `/feed/ranked-feed?page=${page}&size=${size}` : feed_url = `/post/random-feed?page=${page}&size=${size}`
    try {
      const res = await taste_api.get(feed_url);
      const posts = res.data.content as Post[];
      this.setFeed([...this.getFeedState(), ...posts]);
      return posts;
    } catch (error: any) {
      this.logger.logError('Error fetching feed', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async getPostById(id: string): Promise<Post> {
    try {
      const res = await taste_api.get(`/post/${id}`);
      return res.data as Post;
    } catch (error: any) {
      this.logger.logError(`Error fetching post by ID: ${id}`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async getPostRecipe(id: string): Promise<Recipe> {
    try {
      const res = await taste_api.get(`/post/${id}/recipe`);
      return res.data as Recipe;
    } catch (error: any) {
      this.logger.logError(`Error fetching recipe for post ID: ${id}`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async getPostComments(id: string): Promise<Comment[]> {
    try {
      const res = await taste_api.get(`/post/${id}/comments`);
      return res.data as Comment[];
    } catch (error: any) {
      this.logger.logError(`Error fetching comments for post ID: ${id}`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async createPostComment(id: string, content: string): Promise<Comment> {
    try {
      const res = await taste_api.post(`/post/${id}/comment`, { content });
      return res.data as Comment;
    } catch (error: any) {
      this.logger.logError(`Error creating comment for post ID: ${id}`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async deletePostComment(postId: string, commentId: string): Promise<GenericResponse> {
    try {
      const res = await taste_api.delete(`/post/${postId}/comment/${commentId}`);
      return res.data;
    } catch (error: any) {
      this.logger.logError(`Error deleting comment ID: ${commentId} for post ID: ${postId}`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async likePost(id: string): Promise<GenericResponse> {
    try {
      const res = await taste_api.post(`/post/${id}/like`);
      return res.data;
    } catch (error: any) {
      this.logger.logError(`Error liking post ID: ${id}`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async unlikePost(id: string): Promise<GenericResponse> {
    try {
      const res = await taste_api.delete(`/post/${id}/like`);
      return res.data;
    } catch (error: any) {
      this.logger.logError(`Error unliking post ID: ${id}`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async getLikedPosts(userId: string): Promise<Post[]> {
    try {
      const res = await taste_api.get(`/post/likedby/${userId}`);
      return res.data as Post[];
    } catch (error: any) {
      this.logger.logError(`Error fetching liked posts for user ID: ${userId}`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
  async createPost(post: any): Promise<Post> {
    try {
      const res = await taste_api.post('/post/create', post);
      return res.data as Post;
    } catch (error: any) {
      this.logger.logError('Error creating post', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
  async deletePost(id: string): Promise<GenericResponse> {
    try {
      const res = await taste_api.delete(`/post/${id}`);
      return res.data;
    } catch (error: any) {
      this.logger.logError(`Error deleting post ID: ${id}`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
}

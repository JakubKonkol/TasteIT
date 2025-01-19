import { Injectable } from '@angular/core';
import taste_api from '../api/taste_api';
import { User } from '../model/user/User';
import { UserProfile } from '../model/user/UserProfile';
import { UserTags } from '../model/user/UserTags';
import { LoggerService } from './logger.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private logger: LoggerService) {}

  async getUserById(id: string): Promise<User> {
    try {
      const res = await taste_api.get(`user/${id}`);
      return res.data;
    } catch (error: any) {
      this.logger.logError('Error fetching user by ID', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
  async getUserProfileById(userId: string): Promise<User>{
    try {
      const res = await taste_api.get(`user/profile/${userId}`);
      return res.data;
    } catch (error: any) {
      this.logger.logError('Error fetching user profile by ID', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async getUserByToken(): Promise<User> {
    try {
      const res = await taste_api.get(`user`);
      return res.data;
    } catch (error: any) {
      this.logger.logError('Error fetching user by token', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async updateUserProfile(userId: string, userProfile: UserProfile): Promise<User> {
    try {
      const res = await taste_api.put(`user/update-user-profile`, userProfile);
      return res.data;
    } catch (error: any) {
      this.logger.logError('Error updating user profile', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async changeUserFirstLogin(userId: string): Promise<User> {
    try {
      const res = await taste_api.patch(`user/first-login/${userId}`);
      return res.data;
    } catch (error: any) {
      this.logger.logError('Error changing first login status', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }

  async updateUserTags(userId: string, userTags: UserTags): Promise<User> {
    try {
      const res = await taste_api.patch(`user/tags/${userId}`, userTags);
      return res.data;
    } catch (error: any) {
      this.logger.logError('Error updating user tags', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
  async followUser(targetUserId: string){
    try {
      const res = await taste_api.post(`user/follow/${targetUserId}`);
      return res.data;
    } catch (error: any) {
      this.logger.logError('Error following user', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
  async unfollowUser(targetUserId: string){
    try {
      const res = await taste_api.delete(`user/unfollow/${targetUserId}`);
      return res.data;
    } catch (error: any) {
      this.logger.logError('Error unfollowing user', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
  async getFollowers(userId: string): Promise<User[]> {
    try {
      const res = await taste_api.get(`user/${userId}/followers`);
      return res.data.content;
    } catch (error: any) {
      this.logger.logError('Error getting followers', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
  async getFollowing(userId: string): Promise<User[]> {
    try {
      const res = await taste_api.get(`user/${userId}/following`);
      return res.data.content;
    } catch (error: any) {
      this.logger.logError('Error getting following', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
  async getUserPosts(userId: string, page?: number, size?: number): Promise<any> {
    try {
      const params: any = { };
      if (page !== undefined) params.page = page;
      if (size !== undefined) params.size = size;
      const res = await taste_api.get(`user/${userId}/posts`, { params });
      return res.data.content;
    } catch (error: any) {
      this.logger.logError('Error getting user posts', error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
}

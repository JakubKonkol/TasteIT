import {Injectable} from "@angular/core";
import taste_api from "../api/taste_api";
import {Tag} from "../model/user/Tag";
import {LoggerService} from "./logger.service";

@Injectable({
    providedIn: 'root'
})
export class TagService{
    constructor(private logger: LoggerService) {}

  async getBasicTags(): Promise<Tag[]> {
    try {
      const res = await taste_api.get('tags/basic');
      return res.data as Tag[];
    } catch (error: any) {
      this.logger.logError(`Error fetching basic tags`, error.response?.data || error);
      return Promise.reject(error.response?.data || error);
    }
  }
}

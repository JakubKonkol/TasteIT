import {PostData} from "./postData";
import {BehaviorSubject} from "rxjs";
import {EPostType} from "../../../model/post/EPostType";
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class PostBuilderService {
  private postDataSubject = new BehaviorSubject<PostData>({
    postMedia: {
      title: '',
      description: '',
      pictures: [],
    },
    recipe: {
      steps: new Map<number, string>(),
      pictures: [],
      ingredientsWithMeasurements: []
    },
    postType: EPostType.FOOD,
    tags: [],
  });

  postData$ = this.postDataSubject.asObservable();

  updatePostData(updates: Partial<PostData>) {
    const currentData = this.postDataSubject.value;
    this.postDataSubject.next({
      ...currentData,
      ...updates,
      postMedia: {
        ...currentData.postMedia,
        ...(updates.postMedia || {})
      },
      recipe: {
        ...currentData.recipe,
        ...(updates.recipe || {})
      }
    });
  }

  getCurrentPostData(): PostData {
    return this.postDataSubject.value;
  }

  resetPostData() {
    this.postDataSubject.next({
      postMedia: {
        title: '',
        description: '',
        pictures: [],
      },
      recipe: {
        steps: new Map<number, string>(),
        pictures: [],
        ingredientsWithMeasurements: []
      },
      postType: EPostType.FOOD,
      tags: [],
    });
  }
}

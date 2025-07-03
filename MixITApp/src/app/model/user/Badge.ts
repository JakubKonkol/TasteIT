export interface Badge{
  id: string;
  name: string;
  description: string;
  image: string;
  value: number;
  goalValue: number;
  earned: boolean;
  earnedDate: Date;
}

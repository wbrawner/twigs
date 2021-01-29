import { randomId } from '../shared/utils';

export class Category {
  id: string = randomId();
  title: string;
  description: string;
  amount: number;
  expense: boolean;
  archived: boolean;
  budgetId: string;
}

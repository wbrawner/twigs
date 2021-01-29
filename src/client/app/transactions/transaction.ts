import { randomId } from '../shared/utils';

export class Transaction {
  id: string = randomId();
  title: string;
  description: string = null;
  date: Date = new Date();
  amount: number;
  expense = true;
  categoryId: string;
  budgetId: string;
  createdBy: string;
}

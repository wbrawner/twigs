import { randomId } from '../utils';

export class Budget {
    id: string = randomId();
    name: string;
    description: string;
    currencyCode: string;
}

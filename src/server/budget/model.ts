import { randomId } from '../utils';

export class Budget {
    id: string = randomId();
    name: string;
    description: string;
    currencyCode: string;

    constructor(id: string, name: string, description: string) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}

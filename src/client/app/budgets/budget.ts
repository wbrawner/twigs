import { UserPermission } from '../users/user';
import { randomId } from '../shared/utils';

export class Budget {
    id: string = randomId();
    name: string;
    description: string;
    users: UserPermission[];
}

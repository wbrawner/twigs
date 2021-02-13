import { randomId, twoWeeksFromNow } from '../utils';

export class User {
    id: string = randomId();
    username: string;
    email: string;
    password: string;

    constructor(
        id: string,
        username: string,
        email: string,
        password?: string
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}

export class Session {
    id: string = randomId();
    userId: string;
    token: string = randomId(256);
    expiration: Date = twoWeeksFromNow();

    constructor(userId: string) {
        this.userId = userId;
    }
}
import { randomInt } from 'crypto';
import { Request, Response, NextFunction } from 'express';
import { ParamsDictionary } from 'express-serve-static-core';
import QueryString from 'qs';
import sqlite3 from 'sqlite3';
import { User } from './users/user';

const CHARACTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'

export function randomId(length = 32): string {
    return Array.from(new Array(length), () => CHARACTERS[randomInt(CHARACTERS.length)]).join('');
}

export function authMiddleware(db: sqlite3.Database): (
    req: Request<ParamsDictionary, any, any, QueryString.ParsedQs, Record<string, any>>,
    res: Response<any, Record<string, any>>,
    next: NextFunction
) => void {
    return (req, res, next) => {
        const auth = req.get('Authorization');
        if (!auth) {
            res.status(401).send();
            return;
        }
        const token = auth.substring(7);
        db.prepare('SELECT U.id, U.username, U.email FROM user U INNER JOIN session S ON S.user_id = U.id WHERE S.token = ?')
            .get(token, (err, row) => {
                if (err) {
                    console.error(`Auth error: ${err}`)
                    res.status(401).send();
                } else if (!row) {
                    console.log("Invalid session token")
                    res.status(401).send("Invalid session token")
                } else {
                    console.log(`Found user for token: ${row}`);
                    req.user = new User(
                        row.id,
                        row.username,
                        row.email
                    );    
                    next();
                }
            });
    }
}
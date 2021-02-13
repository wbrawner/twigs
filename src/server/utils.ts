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

export function twoWeeksFromNow(): Date {
    const date = new Date();
    date.setDate(date.getDate() + 14);
    return date;
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
        db.prepare('SELECT U.id, U.username, U.email, S.id as sessionId, S.expiration FROM user U INNER JOIN session S ON S.user_id = U.id WHERE S.token = ?')
            .get(token, (err, row) => {
                if (err) {
                    console.error(`Auth error: ${err}`)
                    res.status(500).send("Internal server error");
                } else if (!row) {
                    console.log("Invalid session token")
                    res.status(401).send("Invalid session token")
                } else {
                    let expiration = new Date(row.expiration);
                    const now = new Date();
                    if (expiration < now) {
                        res.status(401).send("Session expired")
                        return;
                    }
                    expiration = twoWeeksFromNow();
                    db.prepare('UPDATE session SET expiration = ? WHERE id = ?')
                        .run([expiration, row.sessionId])
                        .finalize();
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
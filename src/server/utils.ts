import { randomInt } from 'crypto';
import { Request, Response, NextFunction } from 'express';
import { ParamsDictionary } from 'express-serve-static-core';
import { isAtLeast, Permission } from './permissions/permission';
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

export function firstOfMonth(): Date {
    const date = new Date();
    date.setMilliseconds(0);
    date.setSeconds(0);
    date.setMinutes(0);
    date.setHours(0);
    date.setDate(1);
    return date;
}

export function lastOfMonth(): Date {
    const date = new Date();
    date.setMilliseconds(999);
    date.setSeconds(59);
    date.setMinutes(59);
    date.setHours(23);
    date.setMonth(date.getMonth() + 1);
    date.setDate(0);
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

export function budgetPermissionMiddleware(db: sqlite3.Database, budgetId: string, minPermission: Permission): (
    req: Request<ParamsDictionary, any, any, QueryString.ParsedQs, Record<string, any>>,
    res: Response<any, Record<string, any>>,
    next: NextFunction
) => void {
    return (req, res, next) => {
        db.prepare('SELECT * FROM user_permission WHERE budget_id = ? AND user_id = ?;')
            .get([budgetId, req.user.id], (err?: Error, row?: any) => {
                if (err) {
                    console.error(err)
                    res.status(500).send("Failed to get budget permissions")
                    return;
                }
                if (!row) {
                    res.status(404).send(`No budget found for ID ${budgetId}`);
                    return;
                }
                if (!isAtLeast(row.permission, minPermission)) {
                    res.status(403).send(`Insufficient permissions for budget ${budgetId}`)
                    return;
                }
                db.prepare('SELECT * FROM budget WHERE id = ?')
                    .get(budgetId, (err?: Error, row?: any) => {
                        if (err) {
                            console.error(err)
                            res.status(500).send("Failed to get budget")
                            return;
                        }
                        req.budget = row;
                        next();
                    })
                    .finalize();
            })
            .finalize();
    }
}
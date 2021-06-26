import express from 'express';
import { Permission } from '../permissions/permission';
import sqlite3 from 'sqlite3';
import { authMiddleware, budgetPermissionMiddleware, firstOfMonth, lastOfMonth, randomId } from '../utils';
import { Budget } from './model';

export default function budgetRouter(db: sqlite3.Database): express.Router {
    const router = express.Router()

    router.get('/', authMiddleware(db), (req, res) => {
        db.prepare('SELECT * FROM budget WHERE id IN (SELECT budget_id FROM user_permission WHERE user_id = ?)')
            .all(req.user.id, (err?: Error, rows?: any) => {
                if (err) {
                    console.error(err)
                    res.status(500).send("Failed to get budgets")
                    return;
                }
                console.log(rows);
                res.send(rows || []);
            })
            .finalize();
    });

    router.post('/', authMiddleware(db), (req, res) => {
        const id = (req.body.id && req.body.id.length == 32) ? req.body.id : randomId();
        let budget = new Budget(id, req.body.name, req.body.description);
        const users = req.body.users;
        db.prepare('INSERT INTO budget (id, name, description) VALUES (?, ?, ?);')
            .run([budget.id, budget.name, budget.description], function (err?: Error) {
                if (err) {
                    res.status(400).send(err);
                    return;
                }
                db.serialize(function () {
                    let budgetUsers: any[] = [];
                    const query = db.prepare('INSERT INTO user_permission (budget_id, user_id, permission) values (?, ?, ?);');
                    for (let i = 0; i < users.length; i++) {
                        const user = users[i];
                        query.run([id, user['user'], user['permission']], function (err?: Error) {
                            if (!err) {
                                budgetUsers.push(user);
                                console.log("Added budget user")
                            } else {
                                console.error("Unable to add user to budget", err);
                            }
                            if (i == users.length - 1) {
                                res.send({ ...budget, users: budgetUsers });
                                console.log("Sent budget response")
                            }
                        });
                    }
                    query.finalize();
                });
            })
            .finalize();
    });

    router.get(
        '/:id',
        authMiddleware(db),
        (req, res, next) => budgetPermissionMiddleware(db, req.params['id'], Permission.READ)(req, res, next),
        (req, res) => {
            db.prepare('SELECT id, username, user_permission.permission FROM user INNER JOIN user_permission ON id = user_permission.user_id WHERE id IN (SELECT user_id FROM user_permission WHERE budget_id = ?) AND user_permission.budget_id = ?')
                .all([req.budget.id, req.budget.id], function (err?: Error, rows?: any[]) {
                    if (err) {
                        res.status(500).send(`Failed to fetch users for budget ${req.budget.id}`)
                        return;
                    }
                    res.send({ ...req.budget, users: rows });
                });
        }
    );

    router.get(
        '/:id/balance',
        authMiddleware(db),
        (req, res, next) => budgetPermissionMiddleware(db, req.params['id'], Permission.READ)(req, res, next),
        (req, res) => {
            const from = req.query['from'] || firstOfMonth();
            const to = req.query['to'] || lastOfMonth();
            db.prepare(`SELECT (
                COALESCE(
                    (
                        SELECT SUM(amount) from \`transaction\` WHERE budget_id = ? AND expense = 0 AND date >= ? AND date <= ?
                    ),
                    0
                )
            ) - (
                COALESCE(
                    (
                        SELECT SUM(amount) from \`transaction\` WHERE budget_id = ? AND expense = 1 AND date >= ? AND date <= ?
                    ),
                    0
                )
            );`).get([req.budget.id, from, to, req.budget.id, from, to], function (err?: Error, row?: any) {
                if (err) {
                    console.error('Failed to load budget balance', err);
                    res.status(500).send(`Failed to load budget balance for budget ${req.budget.id}`)
                    return;
                }
                res.send(`${Object.values(row)[0] || 0}`);
            }).finalize();
        }
    );

    router.put(
        '/:id',
        authMiddleware(db),
        (req, res, next) => budgetPermissionMiddleware(db, req.params['id'], Permission.MANAGE)(req, res, next),
        (req, res) => {
            console.log('Reached update')
            const name = req.body.name || req.budget.name;
            const description = req.body.description || req.budget.description;
            // TODO: Allow changing user permissions
            // let users = req.body.users;
            db.prepare('UPDATE budget SET name = ?, description = ? WHERE id = ?')
                .run([name, description, req.budget.id], function (err?: Error) {
                    if (err) {
                        res.status(500).send("Failed to update budget");
                        return;
                    }
                    db.prepare('SELECT id, username FROM user WHERE id IN (SELECT user_id FROM user_permission WHERE budget_id = ?)')
                        .all(req.budget.id, function (err?: Error, rows?: any[]) {
                            if (err) {
                                res.status(500).send(`Failed to fetch users for budget ${req.budget.id}`)
                                return;
                            }
                            res.send({ ...req.budget, name: name, description: description, users: rows });
                        });
                })
                .finalize();
        }
    );

    router.delete(
        '/:id',
        authMiddleware(db),
        (req, res, next) => budgetPermissionMiddleware(db, req.params['id'], Permission.OWNER)(req, res, next),
        (req, res) => {
            db.prepare('DELETE FROM budget WHERE id = ?')
                .run(req.budget.id, function (err?: Error) {
                    if (err) {
                        console.error(err);
                        res.status(500).send('Failed to delete budget')
                    } else {
                        res.status(204).send();
                    }
                })
        }
    );

    return router;
}

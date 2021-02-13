import express from 'express';
import sqlite3 from 'sqlite3';
import { authMiddleware, randomId } from '../utils';
import { hashSync, compareSync } from 'bcrypt';
import { Session } from './user';

const SALT_ROUNDS = 10;

export default function userRouter(db: sqlite3.Database): express.Router {
    const router = express.Router()

    router.post('/login', (req, res) => {
        const username = req.body.username;
        const password = req.body.password;
        db.prepare('SELECT * FROM user WHERE username = ?')
            .get(username, (err?: Error, row?: any) => {
                if (err) {
                    console.error(err)
                    res.status(500).send("Login failed")
                    return;
                }
                if (!row) {
                    res.status(401).send({ 'message': 'Invalid credentials' })
                    return;
                }
                if (!compareSync(password, row.password)) {
                    res.status(401).send({ 'message': 'Invalid credentials' })
                    return;
                }
                const session = new Session(row.id);
                db.prepare('INSERT INTO session (id, user_id, token, expiration) VALUES (?, ?, ?, ?)')
                    .run([session.id, session.userId, session.token, session.expiration], (sessionErr?: Error) => {
                        if (sessionErr) {
                            console.error(err)
                            res.status(500).send("Session creation failed")
                            return;
                        }
                        res.send({
                            token: session.token,
                            expiration: session.expiration.toISOString()
                        });
                    })
                    .finalize();
            })
            .finalize();
    });

    router.get('/', (req, res) => {
        res.status(500).send("GET /");
    });

    router.post('/', (req, res) => {
        if (!req.body.username || !req.body.password) {
            res.status(400).send("Username and password are required fields");
            return;
        }
        let id = req.body.id;
        if (!id || id.length < 32) {
            id = randomId();
        }
        const email = req.body.email;
        const username = req.body.username;
        const password = hashSync(req.body.password, SALT_ROUNDS);
        db.prepare('INSERT INTO user (id, username, email, password) VALUES (?, ?, ?, ?)')
            .run([id, username, email, password], (err?: Error) => {
                if (err) {
                    console.error(err)
                    res.status(500).send("Registration failed")
                    return;
                }
                res.send({
                    id: id,
                    username: username,
                    email: email
                });
            })
            .finalize();
    });

    router.get('/me', authMiddleware(db), (req, res) => {
        res.send({
            id: req.user.id,
            username: req.user.username,
            email: req.user.email
        })
    });

    router.get('/:id', (req, res) => {
        res.status(500).send("GET /:id");
    });

    router.put('/:id', (req, res) => {
        res.status(500).send("PUT /:id");
    });

    router.delete('/:id', (req, res) => {
        res.status(500).send("DELETE /:id");
    });

    return router;
}

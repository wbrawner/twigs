import express from 'express';
import budgetRouter from './budget/controller'
import { router as categoryRouter } from './categories/controller'
import { router as permissionsRouter } from './permissions/controller'
import { router as transactionRouter } from './transactions/controller'
import { userRouter } from './users'
import { db as _db } from './db';

const port = process.env.PORT || 3000;
const app = express();

const dataDir = process.env.TWIGS_DATA || __dirname;
const db = _db(dataDir);

app.use(express.json());

app.use(express.static(__dirname + '/public'));

app.use('/api/budgets', budgetRouter(db));
app.use('/api/categories', categoryRouter);
app.use('/api/permissions', permissionsRouter);
app.use('/api/transactions', transactionRouter);
app.use('/api/users', userRouter(db));

app.get('/*', (req, res) => {
    res.sendFile(__dirname + '/public/index.html');
});

app.listen(port, () => {
    console.log(`Twigs server listening at http://localhost:${port}`)
    console.log(`Serving static content from ${__dirname}/public`)
});
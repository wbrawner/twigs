import express from 'express';
import { router as budgetRouter } from './budget/controller'
import { router as categoryRouter } from './categories/controller'
import { router as permissionsRouter } from './permissions/controller'
import { router as transactionRouter } from './transactions/controller'
import { router as userRouter } from './users/controller'
import { db as _db } from './db';

const port = process.env.PORT || 3000;
const app = express();

const dataDir = process.env.TWIGS_DATA || __dirname;
const db = _db(dataDir);

app.use(express.static(__dirname + '/public'));

// app.get('/', (req, res) => {
//     console.log('hit: /');
//     res.send('test');
// })

app.use('/api/budgets', budgetRouter);
app.use('/api/categories', categoryRouter);
app.use('/api/permissions', permissionsRouter);
app.use('/api/transactions', transactionRouter);
app.use('/api/users', userRouter);

app.get('/*', (req, res) => {
    res.sendFile(__dirname + '/public/index.html');
});

app.listen(port, () => {
    console.log(`Twigs server listening at http://localhost:${port}`)
    console.log(`Serving static content from ${__dirname}/public`)
});
import express from 'express';

const router = express.Router()

router.get('/', (req, res) => {
    res.status(500).send("GET /");
});

router.post('/', (req, res) => {
    res.status(500).send("POST /");
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

export { router };
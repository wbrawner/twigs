import path from 'path';
import sqlite3 from 'sqlite3';
import * as migrations from './migrations';

export function db(dataDir: string): sqlite3.Database {
    const dbPath = path.join(dataDir, "twigs.db");
    console.log(`Initializing database at ${dbPath}`)
    const sqlite3verbose = sqlite3.verbose();
    const db = new sqlite3verbose.Database(dbPath, (err?: Error) => {
        if (err != null) {
            console.error("Failed to open db");
            console.error(err);
            throw 'Failed to open db';
        }
        migrations.migrate(db);
    });
    return db
}
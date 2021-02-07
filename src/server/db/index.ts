import path from 'path';
import sqlite3 from 'sqlite3';
import * as migrations from './migrations';


export function db(dataDir: string): sqlite3.Database {
    const db = new sqlite3.Database(path.join(dataDir, "twigs.db"), (err) => {
        if (err != null) {
            console.error("Failed to open db");
            console.error(err);
            throw 'Failed to open db';
        }
         
    });
    return db
}
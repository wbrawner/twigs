import sqlite3 from 'sqlite3';
import path from 'path';

/**
 * The current desired database version. Used to run migrations
 */
const DESIRED_DB_VERSION = 1;

const migrations: Record<number, (db: sqlite3.Database) => void> = {
  0: (db: sqlite3.Database) => {
    db.serialize(() => {
      db.run(`
        CREATE TABLE user (
          id TEXT PRIMARY KEY,
          email TEXT DEFAULT NULL,
          username TEXT NOT NULL UNIQUE,
          password TEXT NOT NULL
        );
      `);

      db.run(`
        CREATE TABLE budget (
          id TEXT PRIMARY KEY,
          currency_code TEXT DEFAULT NULL,
          description TEXT DEFAULT NULL,
          name TEXT DEFAULT NULL
        );
      `);

      db.run(`
        CREATE TABLE category (
          id TEXT PRIMARY KEY,
          amount UNSIGNED BIG INT NOT NULL,
          description TEXT DEFAULT NULL,
          expense BOOLEAN NOT NULL DEFAULT 0,
          title TEXT NOT NULL,
          budget_id TEXT NOT NULL,
          archived BOOLEAN NOT NULL DEFAULT 0,
          FOREIGN KEY (budget_id) REFERENCES budget(id)
        );
      `);

      db.run(`
        CREATE TABLE password_reset_request (
          id TEXT PRIMARY KEY,
          date DATETIME DEFAULT NULL,
          token TEXT NOT NULL,
          user_id TEXT NOT NULL,
          FOREIGN KEY (user_id) REFERENCES user(id)
        );
      `);

      db.run(`
        CREATE TABLE session (
          id TEXT PRIMARY KEY,
          user_id TEXT NOT NULL,
          token TEXT NOT NULL UNIQUE,
          expiration date NOT NULL,
          FOREIGN KEY (user_id) REFERENCES user(id)
        );
      `);

      db.run(`
        CREATE TABLE 'transaction' (
          id TEXT PRIMARY KEY,
          amount UNSIGNED BIG INT NOT NULL,
          date DATETIME NOT NULL,
          description TEXT DEFAULT NULL,
          expense BOOLEAN NOT NULL,
          title TEXT DEFAULT NULL,
          budget_id TEXT NOT NULL,
          category_id TEXT DEFAULT NULL,
          created_by_id TEXT NOT NULL,
          FOREIGN KEY (budget_id) REFERENCES budget(id),
          FOREIGN KEY (category_id) REFERENCES category(id),
          FOREIGN KEY (created_by_id) REFERENCES user(id)
        );
      `);

      db.run(`
        CREATE TABLE user_permission (
          budget_id TEXT NOT NULL,
          user_id TEXT NOT NULL,
          permission TEXT DEFAULT NULL,
          PRIMARY KEY (budget_id,user_id),
          FOREIGN KEY (budget_id) REFERENCES budget(id),
          FOREIGN KEY (user_id) REFERENCES user(id)
        );
      `);
      db.run('PRAGMA user_version = 1');
    })
  }
};

export function migrate(db: sqlite3.Database) {
  db.get('PRAGMA user_version;', (err?: Error, row?: any) => {
    let dbVersion = row.user_version;
    while (dbVersion < DESIRED_DB_VERSION) {
      console.log(`Migrating database from ${dbVersion} to ${dbVersion + 1}...`);
      migrations[dbVersion](db);
      console.log(`Completed database migration from ${dbVersion} to ${dbVersion + 1}`)
      dbVersion++;
    }
    console.log(`Database is up to date`)
  });
}

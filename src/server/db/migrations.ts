import sqlite3 from 'sqlite3';

function migrate_0_1(db: sqlite3.Database) {
  db.serialize(() => {
    `
      CREATE TABLE user (
          id TEXT PRIMARY KEY,
          email TEXT DEFAULT NULL,
          username TEXT NOT NULL UNIQUE,
          password TEXT NOT NULL,
        );

        CREATE TABLE budget (
            id TEXT PRIMARY KEY,
            currency_code TEXT DEFAULT NULL,
            description TEXT DEFAULT NULL,
            name TEXT DEFAULT NULL
        );

        CREATE TABLE category (
          id TEXT PRIMARY KEY,
          amount UNSIGNED BIG INT NOT NULL,
          description TEXT DEFAULT NULL,
          expense BOOLEAN NOT NULL DEFAULT 0,
          title TEXT DEFAULT NULL,
          budget_id TEXT DEFAULT NULL,
          archived BOOLEAN NOT NULL DEFAULT 0,
          FOREIGN KEY budget_id REFERENCES budget(id)
        );

          CREATE TABLE password_reset_request (
            id TEXT PRIMARY KEY,
            date DATETIME DEFAULT NULL,
            token TEXT NOT NULL,
            user_id TEXT NOT NULL,
            FOREIGN KEY user_id REFERENCES user(id)
          );

          CREATE TABLE session (
            id TEXT PRIMARY KEY,
            user_id TEXT NOT NULL,
            token TEXT NOT NULL UNIQUE,
            expiration date NOT NULL,
            FOREIGN KEY user_id REFERENCES user(id)
          );

          CREATE TABLE transaction (
            id TEXT PRIMARY KEY,
            amount UNSIGNED BIG INT NOT NULL,
            date DATETIME NOT NULL,
            description TEXT DEFAULT NULL,
            expense BOOLEAN NOT NULL,
            title TEXT DEFAULT NULL,
            budget_id TEXT NOT NULL,
            category_id TEXT DEFAULT NULL,
            created_by_id TEXT NOT NULL,
            FOREIGN KEY budget_id REFERENCES budget(id),
            FOREIGN KEY category_id REFERENCES category(id),
            FOREIGN KEY created_by_id REFERENCES user(id)
          );
          
          CREATE TABLE user_permission (
            budget_id TEXT NOT NULL,
            user_id TEXT NOT NULL,
            permission TEXT DEFAULT NULL,
            PRIMARY KEY (budget_id,user_id),
            FOREIGN KEY budget_id REFERENCES budget(id),
            FOREIGN KEY created_by_id REFERENCES user(id)
          );
          `
  })
}
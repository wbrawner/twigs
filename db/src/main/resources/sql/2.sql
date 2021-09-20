CREATE TABLE IF NOT EXISTS recurring_transactions (
    id          TEXT PRIMARY KEY,
    title       TEXT      NOT NULL,
    description TEXT DEFAULT NULL,
    amount      INTEGER   NOT NULL,
    frequency   TEXT      NOT NULL,
    start       TIMESTAMP NOT NULL,
    finish      TIMESTAMP,
    last_run    TIMESTAMP,
    expense     BOOLEAN   NOT NULL,
    created_by  TEXT      NOT NULL,
    category_id TEXT DEFAULT NULL,
    budget_id   TEXT      NOT NULL,
    CONSTRAINT fk_users FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_categories FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
    CONSTRAINT fk_budgets FOREIGN KEY (budget_id) REFERENCES budgets (id) ON DELETE CASCADE
                                                  );

-- Fix idempotency key uniqueness
-- SQL Server UNIQUE constraints only allow ONE NULL row. Replacing the
-- constraint on the nullable idempotency_key column with a filtered unique
-- index allows unlimited NULLs while still enforcing uniqueness on real keys.

IF EXISTS (SELECT * FROM sys.key_constraints
           WHERE name = 'uq_escrow_idempotency_key'
             AND parent_object_id = OBJECT_ID('escrow_transactions'))
BEGIN
    ALTER TABLE escrow_transactions DROP CONSTRAINT uq_escrow_idempotency_key;
END

IF NOT EXISTS (SELECT * FROM sys.indexes
               WHERE name = 'uq_escrow_idempotency_key'
                 AND object_id = OBJECT_ID('escrow_transactions'))
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX uq_escrow_idempotency_key
        ON escrow_transactions(idempotency_key)
        WHERE idempotency_key IS NOT NULL;
END

-- Remove the now-redundant non-unique lookup index (the unique filtered
-- index above serves the same lookup purpose)
IF EXISTS (SELECT * FROM sys.indexes
           WHERE name = 'idx_escrow_idempotency_key'
             AND object_id = OBJECT_ID('escrow_transactions'))
BEGIN
    DROP INDEX idx_escrow_idempotency_key ON escrow_transactions;
END

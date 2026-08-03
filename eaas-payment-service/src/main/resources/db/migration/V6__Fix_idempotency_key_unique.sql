-- Fix idempotency key uniqueness
-- SQL Server UNIQUE constraints only allow ONE NULL row. Replacing the
-- constraint on the nullable idempotency_key column with a filtered unique
-- index allows unlimited NULLs while still enforcing uniqueness on real keys.

IF EXISTS (SELECT * FROM sys.key_constraints
           WHERE name = 'uq_payment_idempotency_key'
             AND parent_object_id = OBJECT_ID('payment_transactions'))
BEGIN
    ALTER TABLE payment_transactions DROP CONSTRAINT uq_payment_idempotency_key;
END

IF NOT EXISTS (SELECT * FROM sys.indexes
               WHERE name = 'uq_payment_idempotency_key'
                 AND object_id = OBJECT_ID('payment_transactions'))
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX uq_payment_idempotency_key
        ON payment_transactions(idempotency_key)
        WHERE idempotency_key IS NOT NULL;
END

-- Remove the now-redundant non-unique lookup index (the unique filtered
-- index above serves the same lookup purpose)
IF EXISTS (SELECT * FROM sys.indexes
           WHERE name = 'idx_payment_idempotency_key'
             AND object_id = OBJECT_ID('payment_transactions'))
BEGIN
    DROP INDEX idx_payment_idempotency_key ON payment_transactions;
END

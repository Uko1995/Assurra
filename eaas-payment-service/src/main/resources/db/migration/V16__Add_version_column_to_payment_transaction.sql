IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='payment_transaction' AND xtype='U')
BEGIN
ALTER TABLE payment_transactions
    ADD version BIGINT;
END
GO

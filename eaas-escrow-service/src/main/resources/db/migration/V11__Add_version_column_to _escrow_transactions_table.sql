IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='escrow_transaction' AND xtype='U')
BEGIN
ALTER TABLE escrow_transactions
    ADD version BIGINT;
END
GO

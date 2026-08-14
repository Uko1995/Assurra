IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='payouts' AND xtype='U')
BEGIN
ALTER TABLE payouts
    ADD version BIGINT;
END
GO
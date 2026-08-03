-- Increase the size of the account_number field in the payouts table to accommodate longer account numbers
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'payouts' AND COLUMN_NAME = 'account_number')
BEGIN
    ALTER TABLE payouts ALTER COLUMN account_number NVARCHAR(100) NULL;
END
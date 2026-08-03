-- Add Interswitch refund reference to payment transactions
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'payment_transactions' AND COLUMN_NAME = 'interswitch_refund_ref')
BEGIN
    ALTER TABLE payment_transactions ADD interswitch_refund_ref NVARCHAR(255) NULL;
END
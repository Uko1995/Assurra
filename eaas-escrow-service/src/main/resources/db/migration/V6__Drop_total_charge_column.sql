-- Drop the total_charge column.
-- The escrow fee is paid by the merchant (deducted from the payout), so the
-- customer is charged exactly the escrow amount. The total_charge field
-- (amount + fee) implied the customer pays the fee on top and is no longer part
-- of the model.

IF EXISTS (SELECT * FROM sys.columns
           WHERE object_id = OBJECT_ID('escrow_transactions')
             AND name = 'total_charge')
BEGIN
    ALTER TABLE escrow_transactions DROP COLUMN total_charge;
END

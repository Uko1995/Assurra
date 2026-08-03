-- V2__Create_payouts_table.sql
-- Store merchant payout transactions

CREATE TABLE payouts (
    id                  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    reference           NVARCHAR(100) NOT NULL,
    escrow_reference    NVARCHAR(100) NOT NULL,
    merchant_id         UNIQUEIDENTIFIER NOT NULL,

    -- Amounts
    amount              DECIMAL(15, 2) NOT NULL,
    fee                 DECIMAL(15, 2) NOT NULL DEFAULT 0,
    net_amount          DECIMAL(15, 2) NOT NULL,
    currency            NVARCHAR(3) DEFAULT 'NGN',

    -- Status
    status              NVARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CONSTRAINT chk_payout_status 
                        CHECK (status IN ('PENDING', 'QUEUED', 'PROCESSING', 'COMPLETED', 'FAILED', 'REVERSED')),
    method              NVARCHAR(20) DEFAULT 'BANK_TRANSFER'
                        CONSTRAINT chk_payout_method 
                        CHECK (method IN ('BANK_TRANSFER', 'WALLET')),

    -- Interswitch Integration
    interswitch_ref     NVARCHAR(255),
    interswitch_batch   NVARCHAR(255),

    -- Bank Details (encrypted in application layer)
    bank_code           NVARCHAR(10),
    bank_name           NVARCHAR(100),
    account_number      NVARCHAR(10),
    account_name        NVARCHAR(255),

    -- Scheduling
    scheduled_at        DATETIME2,
    processed_at        DATETIME2,
    completed_at        DATETIME2,
    failed_at           DATETIME2,
    failure_reason      NVARCHAR(MAX),

    -- Retry Logic
    retry_count         INT DEFAULT 0,
    next_retry_at       DATETIME2,

    -- Metadata
    metadata            NVARCHAR(MAX),

    created_at          DATETIME2 DEFAULT GETDATE(),
    updated_at          DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT uq_payout_reference UNIQUE (reference)
);

-- Indexes
CREATE NONCLUSTERED INDEX idx_payout_reference ON payouts(reference);
CREATE NONCLUSTERED INDEX idx_payout_escrow_ref ON payouts(escrow_reference);
CREATE NONCLUSTERED INDEX idx_payout_merchant_id ON payouts(merchant_id);
CREATE NONCLUSTERED INDEX idx_payout_status ON payouts(status);
CREATE NONCLUSTERED INDEX idx_payout_scheduled_at ON payouts(scheduled_at) 
    WHERE scheduled_at IS NOT NULL;
CREATE NONCLUSTERED INDEX idx_payout_next_retry_at ON payouts(next_retry_at) 
    WHERE next_retry_at IS NOT NULL;
CREATE NONCLUSTERED INDEX idx_payout_interswitch_ref ON payouts(interswitch_ref) 
    WHERE interswitch_ref IS NOT NULL;

-- Extended Properties (Documentation)
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Merchant payout transactions processed via Interswitch',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'payouts';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Internal reference (POUT-XXX format)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'payouts',
    @level2type = N'COLUMN', @level2name = N'reference';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Amount after fees',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'payouts',
    @level2type = N'COLUMN', @level2name = N'net_amount';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Number of retry attempts',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'payouts',
    @level2type = N'COLUMN', @level2name = N'retry_count';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Next scheduled retry time',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'payouts',
    @level2type = N'COLUMN', @level2name = N'next_retry_at';

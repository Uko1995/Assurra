-- V1__Create_payment_transactions_table.sql
-- Store payment transactions from customers

CREATE TABLE payment_transactions (
    id                  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    reference           NVARCHAR(100) NOT NULL,
    escrow_reference    NVARCHAR(100) NOT NULL,
    customer_id         UNIQUEIDENTIFIER NOT NULL,
    merchant_id         UNIQUEIDENTIFIER NOT NULL,

    -- Amounts
    amount              DECIMAL(15, 2) NOT NULL,
    fee                 DECIMAL(15, 2) NOT NULL DEFAULT 0,
    currency            NVARCHAR(3) DEFAULT 'NGN',

    -- Status
    status              NVARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CONSTRAINT chk_payment_status 
                        CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'REFUNDED', 'CANCELLED')),
    channel             NVARCHAR(20)
                        CONSTRAINT chk_payment_channel 
                        CHECK (channel IN ('CARD', 'BANK_TRANSFER', 'USSD', 'QR', 'MOBILE_MONEY')),

    -- Interswitch Integration
    interswitch_ref     NVARCHAR(255),
    interswitch_auth    NVARCHAR(255),

    -- Payment Details
    payment_link        NVARCHAR(500),
    paid_at             DATETIME2,
    failed_at           DATETIME2,
    failure_reason      NVARCHAR(MAX),

    -- Card Details (masked)
    card_last4          NVARCHAR(4),
    card_brand          NVARCHAR(20),

    -- Bank Details
    bank_name           NVARCHAR(100),
    account_number      NVARCHAR(10),

    -- Refund Details
    refunded_at         DATETIME2,
    refund_amount       DECIMAL(15, 2),
    refund_reason       NVARCHAR(MAX),

    -- Metadata
    idempotency_key     NVARCHAR(255),
    metadata            NVARCHAR(MAX),

    created_at          DATETIME2 DEFAULT GETDATE(),
    updated_at          DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT uq_payment_reference UNIQUE (reference),
    CONSTRAINT uq_payment_idempotency_key UNIQUE (idempotency_key)
);

-- Indexes
CREATE NONCLUSTERED INDEX idx_payment_reference ON payment_transactions(reference);
CREATE NONCLUSTERED INDEX idx_payment_escrow_ref ON payment_transactions(escrow_reference);
CREATE NONCLUSTERED INDEX idx_payment_customer_id ON payment_transactions(customer_id);
CREATE NONCLUSTERED INDEX idx_payment_merchant_id ON payment_transactions(merchant_id);
CREATE NONCLUSTERED INDEX idx_payment_status ON payment_transactions(status);
CREATE NONCLUSTERED INDEX idx_payment_interswitch_ref ON payment_transactions(interswitch_ref) 
    WHERE interswitch_ref IS NOT NULL;
CREATE NONCLUSTERED INDEX idx_payment_idempotency_key ON payment_transactions(idempotency_key) 
    WHERE idempotency_key IS NOT NULL;
CREATE NONCLUSTERED INDEX idx_payment_paid_at ON payment_transactions(paid_at) 
    WHERE paid_at IS NOT NULL;

-- Extended Properties (Documentation)
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Customer payment transactions processed via Interswitch',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'payment_transactions';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Internal reference (PAY-XXX format)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'payment_transactions',
    @level2type = N'COLUMN', @level2name = N'reference';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Associated escrow transaction',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'payment_transactions',
    @level2type = N'COLUMN', @level2name = N'escrow_reference';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Interswitch transaction reference',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'payment_transactions',
    @level2type = N'COLUMN', @level2name = N'interswitch_ref';

-- V2__Create_disputes_table.sql
-- Store dispute records between customers and merchants

CREATE TABLE disputes (
    id                  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    reference           NVARCHAR(100) NOT NULL,
    escrow_reference    NVARCHAR(100) NOT NULL,

    -- Parties
    customer_id         UNIQUEIDENTIFIER NOT NULL,
    merchant_id         UNIQUEIDENTIFIER NOT NULL,
    raised_by           UNIQUEIDENTIFIER NOT NULL,

    -- Details
    reason              NVARCHAR(50) NOT NULL
                        CONSTRAINT chk_dispute_reason 
                        CHECK (reason IN ('ITEM_NOT_RECEIVED', 'ITEM_NOT_AS_DESCRIBED', 
                                         'DAMAGED_ITEM', 'WRONG_ITEM', 'LATE_DELIVERY',
                                         'FRAUDULENT_SELLER', 'PAYMENT_ISSUE', 'OTHER')),
    description         NVARCHAR(MAX) NOT NULL,
    desired_outcome     NVARCHAR(MAX),

    -- Status
    status              NVARCHAR(20) NOT NULL DEFAULT 'OPEN'
                        CONSTRAINT chk_dispute_status 
                        CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED_MERCHANT', 
                                         'RESOLVED_CUSTOMER', 'CLOSED')),

    -- Amount at Dispute
    amount_disputed     DECIMAL(15, 2) NOT NULL,
    resolution_amount   DECIMAL(15, 2),

    -- Resolution
    resolution_notes    NVARCHAR(MAX),
    resolved_by         UNIQUEIDENTIFIER,
    resolved_at         DATETIME2,

    -- Timeline
    opened_at           DATETIME2 DEFAULT GETDATE(),
    closed_at           DATETIME2,

    -- Communication
    last_activity_at    DATETIME2 DEFAULT GETDATE(),
    customer_notified   BIT DEFAULT 0,
    merchant_notified   BIT DEFAULT 0,

    -- Metadata
    metadata            NVARCHAR(MAX),

    created_at          DATETIME2 DEFAULT GETDATE(),
    updated_at          DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT uq_disputes_reference UNIQUE (reference)
);

-- Indexes
CREATE NONCLUSTERED INDEX idx_disputes_reference ON disputes(reference);
CREATE NONCLUSTERED INDEX idx_disputes_escrow_ref ON disputes(escrow_reference);
CREATE NONCLUSTERED INDEX idx_disputes_customer_id ON disputes(customer_id);
CREATE NONCLUSTERED INDEX idx_disputes_merchant_id ON disputes(merchant_id);
CREATE NONCLUSTERED INDEX idx_disputes_status ON disputes(status);
CREATE NONCLUSTERED INDEX idx_disputes_opened_at ON disputes(opened_at);
CREATE NONCLUSTERED INDEX idx_disputes_last_activity ON disputes(last_activity_at);

-- Extended Properties (Documentation)
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Customer-Merchant disputes over escrow transactions',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'disputes';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'User ID who opened the dispute',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'disputes',
    @level2type = N'COLUMN', @level2name = N'raised_by';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Portion of escrow amount in dispute',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'disputes',
    @level2type = N'COLUMN', @level2name = N'amount_disputed';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Amount awarded to customer (if any)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'disputes',
    @level2type = N'COLUMN', @level2name = N'resolution_amount';

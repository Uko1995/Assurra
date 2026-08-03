IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='escrow_transactions' AND xtype='U')
BEGIN
    CREATE TABLE escrow_transactions (
        id                      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        reference               NVARCHAR(100) NOT NULL,
        customer_id             UNIQUEIDENTIFIER NOT NULL,
        merchant_id             UNIQUEIDENTIFIER NOT NULL,
        merchant_profile_id     UNIQUEIDENTIFIER NOT NULL,

        -- Amounts
        amount                  DECIMAL(15, 2) NOT NULL,
        escrow_fee              DECIMAL(15, 2) NOT NULL,
        merchant_amount         DECIMAL(15, 2) NOT NULL,
        total_charge            DECIMAL(15, 2) NOT NULL,
        currency                NVARCHAR(3) DEFAULT 'NGN',

        -- Status
        status                  NVARCHAR(20) NOT NULL DEFAULT 'INITIATED'
                                CONSTRAINT chk_escrow_status 
                                CHECK (status IN ('INITIATED', 'FUNDED', 'MERCHANT_NOTIFIED', 'SHIPPED', 'DELIVERED',
                                                 'CONFIRMED', 'DISPUTED', 'UNDER_REVIEW', 'AUTO_RELEASED', 'RELEASED',
                                                 'RESOLVED_MERCHANT', 'RESOLVED_CUSTOMER', 'REFUNDED', 'CANCELLED')),

        -- Product Details
        product_description     NVARCHAR(MAX) NOT NULL,
        product_quantity        INT DEFAULT 1,
        agreed_delivery_days    INT DEFAULT 7,

        -- Payment
        payment_reference       NVARCHAR(255),
        payment_channel         NVARCHAR(50),
        payment_link            NVARCHAR(500),
        funded_at               DATETIME2,
        payment_expires_at      DATETIME2,

        -- Shipping
        tracking_number         NVARCHAR(255),
        logistics_provider      NVARCHAR(100),
        estimated_delivery_date DATE,
        shipped_at              DATETIME2,
        delivered_at            DATETIME2,

        -- Confirmation Window
        confirmation_deadline   DATETIME2,
        auto_release_at         DATETIME2,
        confirmed_at            DATETIME2,

        -- Payout
        payout_reference        NVARCHAR(255),
        paid_out_at             DATETIME2,

        -- Idempotency
        idempotency_key         NVARCHAR(255),
        metadata                NVARCHAR(MAX),

        created_at              DATETIME2 DEFAULT GETDATE(),
        updated_at              DATETIME2 DEFAULT GETDATE(),

        CONSTRAINT uq_escrow_reference UNIQUE (reference),
        CONSTRAINT uq_escrow_idempotency_key UNIQUE (idempotency_key)
    );
END

-- Indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_escrow_reference' AND object_id=OBJECT_ID('escrow_transactions'))
    CREATE NONCLUSTERED INDEX idx_escrow_reference ON escrow_transactions(reference);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_escrow_customer_id' AND object_id=OBJECT_ID('escrow_transactions'))
    CREATE NONCLUSTERED INDEX idx_escrow_customer_id ON escrow_transactions(customer_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_escrow_merchant_id' AND object_id=OBJECT_ID('escrow_transactions'))
    CREATE NONCLUSTERED INDEX idx_escrow_merchant_id ON escrow_transactions(merchant_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_escrow_status' AND object_id=OBJECT_ID('escrow_transactions'))
    CREATE NONCLUSTERED INDEX idx_escrow_status ON escrow_transactions(status);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_escrow_idempotency_key' AND object_id=OBJECT_ID('escrow_transactions'))
    CREATE NONCLUSTERED INDEX idx_escrow_idempotency_key ON escrow_transactions(idempotency_key) 
        WHERE idempotency_key IS NOT NULL;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_escrow_auto_release_at' AND object_id=OBJECT_ID('escrow_transactions'))
    CREATE NONCLUSTERED INDEX idx_escrow_auto_release_at ON escrow_transactions(auto_release_at) 
        WHERE auto_release_at IS NOT NULL;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_escrow_payment_expires_at' AND object_id=OBJECT_ID('escrow_transactions'))
    CREATE NONCLUSTERED INDEX idx_escrow_payment_expires_at ON escrow_transactions(payment_expires_at) 
        WHERE payment_expires_at IS NOT NULL;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_escrow_funded_at' AND object_id=OBJECT_ID('escrow_transactions'))
    CREATE NONCLUSTERED INDEX idx_escrow_funded_at ON escrow_transactions(funded_at) 
        WHERE funded_at IS NOT NULL;

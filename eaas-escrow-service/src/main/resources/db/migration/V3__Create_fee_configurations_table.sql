IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='fee_configurations' AND xtype='U')
BEGIN
    CREATE TABLE fee_configurations (
        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        merchant_id UNIQUEIDENTIFIER NULL,
        fee_type NVARCHAR(20) NOT NULL CHECK (fee_type IN ('PERCENTAGE','FLAT','BLENDED')),
        fee_value DECIMAL(5,4) NOT NULL,
        min_fee DECIMAL(15,2) DEFAULT 500,
        max_fee DECIMAL(15,2) DEFAULT 50000,
        is_active BIT DEFAULT 1,
        effective_from DATETIME2 DEFAULT GETDATE(),
        effective_until DATETIME2 NULL,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );

    -- Seed global default (1.5%)
    INSERT INTO fee_configurations (fee_type, fee_value, min_fee, max_fee)
    VALUES ('PERCENTAGE', 0.0150, 500, 50000);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_fee_config_merchant_active' AND object_id=OBJECT_ID('fee_configurations'))
    CREATE INDEX idx_fee_config_merchant_active ON fee_configurations(merchant_id, is_active, effective_from, effective_until)
    WHERE is_active = 1;

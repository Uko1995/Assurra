-- V2__Create_merchant_profiles_table.sql
-- Create merchant profile table with encrypted sensitive data

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='merchant_profiles' AND xtype='U')
BEGIN
    CREATE TABLE merchant_profiles (
        id                      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        user_id                 UNIQUEIDENTIFIER NOT NULL,
        business_name           NVARCHAR(255) NOT NULL,
        business_type           NVARCHAR(100),
        business_reg_number     NVARCHAR(100),
        bank_account_number     NVARCHAR(500) NOT NULL,
        bank_code               NVARCHAR(10) NOT NULL,
        bank_name               NVARCHAR(100) NOT NULL,
        bvn                     NVARCHAR(500),
        settlement_email        NVARCHAR(255),
        api_key                 NVARCHAR(255),
        api_key_prefix          NVARCHAR(20),
        api_key_identifier      NVARCHAR(128),
        webhook_url             NVARCHAR(500),
        webhook_secret          NVARCHAR(500),
        is_verified             BIT DEFAULT 0,
        kyc_submitted_at        DATETIME2,
        kyc_reviewed_at         DATETIME2,
        kyc_reviewed_by         UNIQUEIDENTIFIER,
        kyc_rejection_reason    NVARCHAR(MAX),
        created_at              DATETIME2 DEFAULT GETDATE(),
        updated_at              DATETIME2 DEFAULT GETDATE(),
        
        CONSTRAINT fk_merchant_profile_user 
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT fk_merchant_reviewed_by 
            FOREIGN KEY (kyc_reviewed_by) REFERENCES users(id),
        CONSTRAINT uq_merchant_api_key UNIQUE (api_key),
        CONSTRAINT uq_merchant_api_key_identifier UNIQUE (api_key_identifier)
    );
END

-- Indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_merchant_profiles_user_lookup' AND object_id=OBJECT_ID('merchant_profiles'))
    CREATE INDEX idx_merchant_profiles_user_lookup ON merchant_profiles(user_id)
    INCLUDE (business_name, business_type, is_verified, api_key_prefix, bank_account_number, bank_code, bank_name);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_merchant_profiles_api_key_lookup' AND object_id=OBJECT_ID('merchant_profiles'))
    CREATE INDEX idx_merchant_profiles_api_key_lookup ON merchant_profiles(api_key)
    WHERE api_key IS NOT NULL;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_merchant_api_key_identifier' AND object_id=OBJECT_ID('merchant_profiles'))
    CREATE INDEX idx_merchant_api_key_identifier ON merchant_profiles(api_key_identifier)
    WHERE api_key_identifier IS NOT NULL;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_merchant_business_name' AND object_id=OBJECT_ID('merchant_profiles'))
    CREATE INDEX idx_merchant_business_name ON merchant_profiles(business_name);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_merchant_is_verified' AND object_id=OBJECT_ID('merchant_profiles'))
    CREATE INDEX idx_merchant_is_verified ON merchant_profiles(is_verified);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_merchant_profiles_kyc_review' AND object_id=OBJECT_ID('merchant_profiles'))
    CREATE INDEX idx_merchant_profiles_kyc_review ON merchant_profiles(kyc_submitted_at)
    WHERE is_verified = 0;

-- Extended Properties (Documentation)
IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'merchant_profiles', NULL, NULL))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Merchant-specific profile information',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'merchant_profiles';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'merchant_profiles', N'COLUMN', N'bank_account_number'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Encrypted bank account number',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'merchant_profiles',
        @level2type = N'COLUMN', @level2name = N'bank_account_number';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'merchant_profiles', N'COLUMN', N'bvn'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Encrypted Bank Verification Number',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'merchant_profiles',
        @level2type = N'COLUMN', @level2name = N'bvn';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'merchant_profiles', N'COLUMN', N'api_key'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Hashed API key for merchant authentication',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'merchant_profiles',
        @level2type = N'COLUMN', @level2name = N'api_key';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'merchant_profiles', N'COLUMN', N'api_key_prefix'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'First few characters of API key for display',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'merchant_profiles',
        @level2type = N'COLUMN', @level2name = N'api_key_prefix';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'merchant_profiles', N'COLUMN', N'webhook_secret'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Secret for signing webhook payloads',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'merchant_profiles',
        @level2type = N'COLUMN', @level2name = N'webhook_secret';

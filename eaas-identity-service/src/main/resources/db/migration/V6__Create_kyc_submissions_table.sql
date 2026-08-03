-- V6__Create_kyc_submissions_table.sql
-- Create KYC submissions table to track merchant KYC verification status

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='kyc_submissions' AND xtype='U')
BEGIN
    CREATE TABLE kyc_submissions (
        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        merchant_id UNIQUEIDENTIFIER NOT NULL,
        status NVARCHAR(20) NOT NULL,
        submitted_at DATETIME2,
        reviewed_at DATETIME2,
        reviewed_by UNIQUEIDENTIFIER,
        rejection_reason TEXT,
        admin_notes TEXT,
        verification_method NVARCHAR(50),
        business_name NVARCHAR(100),
        bank_account_number NVARCHAR(255),
        bank_name NVARCHAR(100),
        bvn NVARCHAR(255),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        CONSTRAINT uq_kyc_submission_merchant UNIQUE (merchant_id),
        CONSTRAINT chk_kyc_status CHECK (status IN ('PENDING', 'UNDER_REVIEW', 'VERIFIED', 'REJECTED'))
    );
END

-- Indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_kyc_submission_status' AND object_id=OBJECT_ID('kyc_submissions'))
    CREATE NONCLUSTERED INDEX idx_kyc_submission_status ON kyc_submissions(status);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_kyc_submissions_pending' AND object_id=OBJECT_ID('kyc_submissions'))
    CREATE INDEX idx_kyc_submissions_pending ON kyc_submissions(status, submitted_at)
    WHERE status = 'PENDING';

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_kyc_submissions_merchant' AND object_id=OBJECT_ID('kyc_submissions'))
    CREATE INDEX idx_kyc_submissions_merchant ON kyc_submissions(merchant_id, submitted_at DESC);

-- Extended Properties (Documentation)
IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'kyc_submissions', NULL, NULL))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Tracks KYC verification submissions and status for merchants',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'kyc_submissions';

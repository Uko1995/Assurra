-- V3__Create_kyc_documents_table.sql
-- Create KYC documents table for merchant verification

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='kyc_documents' AND xtype='U')
BEGIN
    CREATE TABLE kyc_documents (
        id              UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        merchant_id     UNIQUEIDENTIFIER NOT NULL,
        document_type   NVARCHAR(50) NOT NULL
                        CONSTRAINT chk_kyc_document_type 
                        CHECK (document_type IN ('CAC_CERT', 'UTILITY_BILL', 'ID_CARD', 
                                                'PASSPORT', 'DRIVERS_LICENSE', 'BANK_STATEMENT', 'OTHER')),
        file_url        NVARCHAR(500) NOT NULL,
        file_name       NVARCHAR(255),
        file_size_kb    INT,
        mime_type       NVARCHAR(100),
        uploaded_at     DATETIME2 DEFAULT GETDATE(),
        
        CONSTRAINT fk_kyc_docs_merchant 
            FOREIGN KEY (merchant_id) REFERENCES merchant_profiles(id) ON DELETE CASCADE
    );
END

-- Indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_kyc_docs_merchant' AND object_id=OBJECT_ID('kyc_documents'))
    CREATE NONCLUSTERED INDEX idx_kyc_docs_merchant ON kyc_documents(merchant_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_kyc_docs_type' AND object_id=OBJECT_ID('kyc_documents'))
    CREATE NONCLUSTERED INDEX idx_kyc_docs_type ON kyc_documents(document_type);

-- Extended Properties (Documentation)
IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'kyc_documents', NULL, NULL))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'KYC documents uploaded by merchants',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'kyc_documents';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'kyc_documents', N'COLUMN', N'document_type'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Type: CAC_CERT, UTILITY_BILL, ID_CARD, PASSPORT, DRIVERS_LICENSE, BANK_STATEMENT, OTHER',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'kyc_documents',
        @level2type = N'COLUMN', @level2name = N'document_type';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'kyc_documents', N'COLUMN', N'file_url'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'S3 URL or file storage path',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'kyc_documents',
        @level2type = N'COLUMN', @level2name = N'file_url';

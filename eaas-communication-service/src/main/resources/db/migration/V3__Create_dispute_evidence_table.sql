-- V3__Create_dispute_evidence_table.sql
-- Store evidence files for disputes

CREATE TABLE dispute_evidence (
    id                  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    dispute_id          UNIQUEIDENTIFIER NOT NULL,
    uploaded_by         UNIQUEIDENTIFIER NOT NULL,

    -- File Details
    file_name           NVARCHAR(255) NOT NULL,
    original_name       NVARCHAR(255) NOT NULL,
    file_type           NVARCHAR(100) NOT NULL,
    file_size_bytes     BIGINT NOT NULL,
    mime_type           NVARCHAR(100) NOT NULL,

    -- Storage
    s3_bucket           NVARCHAR(100) NOT NULL,
    s3_key              NVARCHAR(500) NOT NULL,
    s3_url              NVARCHAR(1000) NOT NULL,

    -- Description
    description         NVARCHAR(MAX),
    evidence_type       NVARCHAR(50) 
                        CONSTRAINT chk_evidence_type 
                        CHECK (evidence_type IN ('PHOTO', 'RECEIPT', 'CHAT', 'VIDEO', 'AUDIO', 'OTHER')),

    -- Security
    checksum            NVARCHAR(64),
    encrypted           BIT DEFAULT 0,

    -- Lifecycle
    uploaded_at         DATETIME2 DEFAULT GETDATE(),
    expires_at          DATETIME2,
    deleted_at          DATETIME2,
    deleted_by          UNIQUEIDENTIFIER,

    created_at          DATETIME2 DEFAULT GETDATE(),
    updated_at          DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_evidence_dispute 
        FOREIGN KEY (dispute_id) REFERENCES disputes(id) ON DELETE CASCADE
);

-- Indexes
CREATE NONCLUSTERED INDEX idx_evidence_dispute_id ON dispute_evidence(dispute_id);
CREATE NONCLUSTERED INDEX idx_evidence_uploaded_by ON dispute_evidence(uploaded_by);
CREATE NONCLUSTERED INDEX idx_evidence_uploaded_at ON dispute_evidence(uploaded_at);
CREATE NONCLUSTERED INDEX idx_evidence_expires_at ON dispute_evidence(expires_at) 
    WHERE expires_at IS NOT NULL;
CREATE NONCLUSTERED INDEX idx_evidence_not_deleted ON dispute_evidence(dispute_id) 
    WHERE deleted_at IS NULL;

-- Extended Properties (Documentation)
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Files uploaded as evidence in disputes',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'dispute_evidence';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'System-generated unique filename',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'dispute_evidence',
    @level2type = N'COLUMN', @level2name = N'file_name';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'S3 object key for storage',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'dispute_evidence',
    @level2type = N'COLUMN', @level2name = N's3_key';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'When evidence can be deleted (90 days after dispute close)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'dispute_evidence',
    @level2type = N'COLUMN', @level2name = N'expires_at';

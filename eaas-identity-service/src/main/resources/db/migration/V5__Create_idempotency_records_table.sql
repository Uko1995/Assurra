-- V5__Create_idempotency_records_table.sql
-- Create idempotency records table for duplicate request prevention

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='idempotency_records' AND xtype='U')
BEGIN
    CREATE TABLE idempotency_records (
        idempotency_key NVARCHAR(255) NOT NULL,
        user_id         UNIQUEIDENTIFIER NOT NULL,
        endpoint        NVARCHAR(255) NOT NULL,
        request_body    NVARCHAR(MAX),
        response_status INT,
        response_body   NVARCHAR(MAX),
        created_at      DATETIME2 DEFAULT GETDATE(),
        expires_at      DATETIME2 DEFAULT DATEADD(hour, 24, GETDATE()),
        
        CONSTRAINT pk_idempotency_records PRIMARY KEY (idempotency_key),
        CONSTRAINT fk_idempotency_user 
            FOREIGN KEY (user_id) REFERENCES users(id)
    );
END

-- Indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_idempotency_expires' AND object_id=OBJECT_ID('idempotency_records'))
    CREATE NONCLUSTERED INDEX idx_idempotency_expires ON idempotency_records(expires_at);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_idempotency_user' AND object_id=OBJECT_ID('idempotency_records'))
    CREATE NONCLUSTERED INDEX idx_idempotency_user ON idempotency_records(user_id);

-- Extended Properties (Documentation)
IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'idempotency_records', NULL, NULL))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', 
        @value = N'Stores idempotency keys to prevent duplicate processing. Cleanup job: DELETE FROM idempotency_records WHERE expires_at < GETDATE()',
        @level0type = N'SCHEMA', @level0name = N'dbo',
        @level1type = N'TABLE', @level1name = N'idempotency_records';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'idempotency_records', N'COLUMN', N'idempotency_key'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', 
        @value = N'Unique key provided by client',
        @level0type = N'SCHEMA', @level0name = N'dbo',
        @level1type = N'TABLE', @level1name = N'idempotency_records',
        @level2type = N'COLUMN', @level2name = N'idempotency_key';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'idempotency_records', N'COLUMN', N'expires_at'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', 
        @value = N'TTL for automatic cleanup (24 hours)',
        @level0type = N'SCHEMA', @level0name = N'dbo',
        @level1type = N'TABLE', @level1name = N'idempotency_records',
        @level2type = N'COLUMN', @level2name = N'expires_at';

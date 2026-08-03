IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='refresh_tokens' AND xtype='U')
BEGIN
    CREATE TABLE refresh_tokens (
        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        token_hash NVARCHAR(255) NOT NULL,
        user_id UNIQUEIDENTIFIER NOT NULL,
        issued_at DATETIME2 DEFAULT GETDATE(),
        expires_at DATETIME2 NOT NULL,
        used_at DATETIME2 NULL,
        replaced_by UNIQUEIDENTIFIER NULL,
        revoked BIT DEFAULT 0,
        ip_address NVARCHAR(45) NULL,
        user_agent NVARCHAR(500) NULL
    );

    CREATE INDEX idx_refresh_token_hash ON refresh_tokens(token_hash);
    CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
END
GO

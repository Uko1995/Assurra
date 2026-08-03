-- V4__Create_audit_logs_table.sql
-- Create immutable audit log table for compliance

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='audit_logs' AND xtype='U')
BEGIN
    CREATE TABLE audit_logs (
        id              UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        entity_type     NVARCHAR(50) NOT NULL,
        entity_id       UNIQUEIDENTIFIER NOT NULL,
        action          NVARCHAR(100) NOT NULL,
        performed_by    UNIQUEIDENTIFIER,
        performed_by_role NVARCHAR(20) 
                          CONSTRAINT chk_audit_role 
                          CHECK (performed_by_role IN ('CUSTOMER', 'MERCHANT', 'SYSTEM', 'ADMIN')),
        triggered_by    NVARCHAR(20) NOT NULL DEFAULT 'SYSTEM'
                          CONSTRAINT chk_audit_triggered_by 
                          CHECK (triggered_by IN ('CUSTOMER', 'MERCHANT', 'SYSTEM', 'ADMIN')),
        ip_address      NVARCHAR(45),
        user_agent      NVARCHAR(MAX),
        old_values      NVARCHAR(MAX),
        new_values      NVARCHAR(MAX),
        metadata        NVARCHAR(MAX),
        created_at      DATETIME2 DEFAULT GETDATE(),
        
        CONSTRAINT fk_audit_performed_by 
            FOREIGN KEY (performed_by) REFERENCES users(id)
    );
END

-- Indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_audit_logs_user_activity' AND object_id=OBJECT_ID('audit_logs'))
    CREATE INDEX idx_audit_logs_user_activity ON audit_logs(performed_by, created_at DESC)
    INCLUDE (action, entity_type, metadata);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_audit_logs_entity' AND object_id=OBJECT_ID('audit_logs'))
    CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id, created_at DESC);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_audit_logs_recent' AND object_id=OBJECT_ID('audit_logs'))
    CREATE INDEX idx_audit_logs_recent ON audit_logs(created_at DESC)
    INCLUDE (action, performed_by, performed_by_role, entity_type);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_audit_action' AND object_id=OBJECT_ID('audit_logs'))
    CREATE NONCLUSTERED INDEX idx_audit_action ON audit_logs(action);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_audit_logs_ip_action' AND object_id=OBJECT_ID('audit_logs'))
    CREATE INDEX idx_audit_logs_ip_action ON audit_logs(ip_address, action, created_at)
    WHERE action IN ('LOGIN_FAILED', 'USER_LOGGED_IN');

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_audit_logs_failed_login' AND object_id=OBJECT_ID('audit_logs'))
    CREATE INDEX idx_audit_logs_failed_login ON audit_logs(action, created_at)
    WHERE action = 'LOGIN_FAILED';

-- Immutability Triggers
DROP TRIGGER IF EXISTS trg_prevent_audit_log_update;
GO

CREATE TRIGGER trg_prevent_audit_log_update
ON audit_logs
INSTEAD OF UPDATE
AS
BEGIN
    RAISERROR('Audit logs are immutable and cannot be updated.', 16, 1);
END;
GO

DROP TRIGGER IF EXISTS trg_prevent_audit_log_delete;
GO

CREATE TRIGGER trg_prevent_audit_log_delete
ON audit_logs
INSTEAD OF DELETE
AS
BEGIN
    RAISERROR('Audit logs are immutable and cannot be deleted.', 16, 1);
END;
GO

-- Extended Properties (Documentation)
IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'audit_logs', NULL, NULL))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Immutable audit trail for all system actions. This table should be append-only. No updates or deletes allowed.',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'audit_logs';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'audit_logs', N'COLUMN', N'entity_type'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Type of entity: USER, MERCHANT, etc.',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'audit_logs',
        @level2type = N'COLUMN', @level2name = N'entity_type';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'audit_logs', N'COLUMN', N'action'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Action performed: CREATED, UPDATED, STATUS_CHANGED, etc.',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'audit_logs',
        @level2type = N'COLUMN', @level2name = N'action';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'audit_logs', N'COLUMN', N'old_values'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Previous values (JSON)',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'audit_logs',
        @level2type = N'COLUMN', @level2name = N'old_values';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'audit_logs', N'COLUMN', N'new_values'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'New values (JSON)',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'audit_logs',
        @level2type = N'COLUMN', @level2name = N'new_values';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'audit_logs', N'COLUMN', N'metadata'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Additional context (JSON)',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'audit_logs',
        @level2type = N'COLUMN', @level2name = N'metadata';

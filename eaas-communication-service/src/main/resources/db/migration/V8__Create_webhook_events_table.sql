IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='webhook_events' AND xtype='U')
BEGIN
    CREATE TABLE webhook_events (
        id              UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        event_type      NVARCHAR(50) NOT NULL,
        reference       NVARCHAR(100) NOT NULL,
        payload         NVARCHAR(MAX) NOT NULL,
        target_url      NVARCHAR(500) NOT NULL,
        signature       NVARCHAR(255),
        status          NVARCHAR(20) NOT NULL,
        attempt_count   INT DEFAULT 0,
        last_attempt_at DATETIME2,
        last_error      NVARCHAR(MAX),
        next_attempt_at DATETIME2,
        http_status     INT,
        response_body   NVARCHAR(MAX),
        delivered_at    DATETIME2,
        created_at      DATETIME2 DEFAULT GETDATE(),
        updated_at      DATETIME2 DEFAULT GETDATE()
    );
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_webhook_events_status' AND object_id=OBJECT_ID('webhook_events'))
    CREATE INDEX idx_webhook_events_status ON webhook_events(status);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_webhook_events_reference' AND object_id=OBJECT_ID('webhook_events'))
    CREATE INDEX idx_webhook_events_reference ON webhook_events(reference);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_webhook_events_next_attempt' AND object_id=OBJECT_ID('webhook_events'))
    CREATE INDEX idx_webhook_events_next_attempt ON webhook_events(next_attempt_at)
    WHERE next_attempt_at IS NOT NULL;

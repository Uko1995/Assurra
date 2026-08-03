-- V3__Create_webhook_events_table.sql
-- Track webhook delivery attempts

CREATE TABLE webhook_events (
    id                  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    event_type          NVARCHAR(50) NOT NULL,
    reference           NVARCHAR(100) NOT NULL,

    -- Payload
    payload             NVARCHAR(MAX) NOT NULL,
    signature           NVARCHAR(255),

    -- Delivery
    target_url          NVARCHAR(500) NOT NULL,
    status              NVARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CONSTRAINT chk_webhook_status 
                        CHECK (status IN ('PENDING', 'DELIVERED', 'FAILED', 'RETRYING')),

    -- Attempts
    attempt_count       INT DEFAULT 0,
    last_attempt_at     DATETIME2,
    last_error          NVARCHAR(MAX),
    next_attempt_at     DATETIME2,

    -- Response
    http_status         INT,
    response_body       NVARCHAR(MAX),

    delivered_at        DATETIME2,

    created_at          DATETIME2 DEFAULT GETDATE(),
    updated_at          DATETIME2 DEFAULT GETDATE()
);

-- Indexes
CREATE NONCLUSTERED INDEX idx_webhook_event_type ON webhook_events(event_type);
CREATE NONCLUSTERED INDEX idx_webhook_reference ON webhook_events(reference);
CREATE NONCLUSTERED INDEX idx_webhook_status ON webhook_events(status);
CREATE NONCLUSTERED INDEX idx_webhook_next_attempt ON webhook_events(next_attempt_at) 
    WHERE next_attempt_at IS NOT NULL;
CREATE NONCLUSTERED INDEX idx_webhook_created_at ON webhook_events(created_at);

-- Extended Properties (Documentation)
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Webhook delivery attempts for payment/payout events',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'webhook_events';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Payment or payout reference',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'webhook_events',
    @level2type = N'COLUMN', @level2name = N'reference';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'HMAC signature for verification',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'webhook_events',
    @level2type = N'COLUMN', @level2name = N'signature';

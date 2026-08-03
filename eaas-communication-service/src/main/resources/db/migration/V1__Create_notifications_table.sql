-- V1__Create_notifications_table.sql
-- Store all notifications (email, SMS, push, in-app)

CREATE TABLE notifications (
    id                  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    user_id             UNIQUEIDENTIFIER NOT NULL,
    type                NVARCHAR(20) NOT NULL
                        CONSTRAINT chk_notification_type 
                        CHECK (type IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    status              NVARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CONSTRAINT chk_notification_status 
                        CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'READ')),
    priority            NVARCHAR(20) DEFAULT 'NORMAL'
                        CONSTRAINT chk_notification_priority 
                        CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),

    -- Content
    subject             NVARCHAR(255),
    body                NVARCHAR(MAX) NOT NULL,
    body_html           NVARCHAR(MAX),

    -- Recipients
    email_to            NVARCHAR(255),
    phone_number        NVARCHAR(20),
    device_token        NVARCHAR(500),

    -- Template
    template_name       NVARCHAR(100),
    template_data       NVARCHAR(MAX),

    -- Delivery Tracking
    sent_at             DATETIME2,
    delivered_at        DATETIME2,
    read_at             DATETIME2,
    failed_at           DATETIME2,
    failure_reason      NVARCHAR(MAX),

    -- Metadata
    reference_id        NVARCHAR(100),
    reference_type      NVARCHAR(50),
    metadata            NVARCHAR(MAX),

    created_at          DATETIME2 DEFAULT GETDATE(),
    updated_at          DATETIME2 DEFAULT GETDATE()
);

-- Indexes
CREATE NONCLUSTERED INDEX idx_notifications_user_id ON notifications(user_id);
CREATE NONCLUSTERED INDEX idx_notifications_type ON notifications(type);
CREATE NONCLUSTERED INDEX idx_notifications_status ON notifications(status);
CREATE NONCLUSTERED INDEX idx_notifications_reference ON notifications(reference_id, reference_type);
CREATE NONCLUSTERED INDEX idx_notifications_sent_at ON notifications(sent_at) 
    WHERE sent_at IS NOT NULL;
CREATE NONCLUSTERED INDEX idx_notifications_created_at ON notifications(created_at);

-- Extended Properties (Documentation)
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'All user notifications across email, SMS, push, and in-app channels',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'notifications';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Associated entity reference (escrow ref, dispute id, etc.)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'notifications',
    @level2type = N'COLUMN', @level2name = N'reference_id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Type of referenced entity (ESCROW, DISPUTE, etc.)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'notifications',
    @level2type = N'COLUMN', @level2name = N'reference_type';

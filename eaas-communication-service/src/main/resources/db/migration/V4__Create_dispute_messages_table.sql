-- V4__Create_dispute_messages_table.sql
-- Store messages between parties in a dispute

CREATE TABLE dispute_messages (
    id                  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    dispute_id          UNIQUEIDENTIFIER NOT NULL,
    sender_id           UNIQUEIDENTIFIER NOT NULL,
    sender_type         NVARCHAR(20) NOT NULL 
                        CONSTRAINT chk_message_sender_type 
                        CHECK (sender_type IN ('CUSTOMER', 'MERCHANT', 'ADMIN', 'SYSTEM')),

    -- Content
    message             NVARCHAR(MAX) NOT NULL,
    is_internal         BIT DEFAULT 0, -- Internal admin notes

    -- Attachments flag (actual attachments in message_attachments junction table)
    has_attachments     BIT DEFAULT 0,

    -- Status
    read_by_customer    BIT DEFAULT 0,
    read_by_merchant    BIT DEFAULT 0,
    read_at             DATETIME2,

    created_at          DATETIME2 DEFAULT GETDATE(),
    updated_at          DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_messages_dispute 
        FOREIGN KEY (dispute_id) REFERENCES disputes(id) ON DELETE CASCADE
);

-- Indexes
CREATE NONCLUSTERED INDEX idx_messages_dispute_id ON dispute_messages(dispute_id);
CREATE NONCLUSTERED INDEX idx_messages_sender_id ON dispute_messages(sender_id);
CREATE NONCLUSTERED INDEX idx_messages_created_at ON dispute_messages(created_at);
CREATE NONCLUSTERED INDEX idx_messages_not_internal ON dispute_messages(dispute_id) 
    WHERE is_internal = 0;
CREATE NONCLUSTERED INDEX idx_messages_unread_customer ON dispute_messages(dispute_id) 
    WHERE is_internal = 0 AND read_by_customer = 0;
CREATE NONCLUSTERED INDEX idx_messages_unread_merchant ON dispute_messages(dispute_id) 
    WHERE is_internal = 0 AND read_by_merchant = 0;

-- Extended Properties (Documentation)
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Communication thread for each dispute. Attachments are stored in message_attachments junction table.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'dispute_messages';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'If true (1), only visible to admins',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'dispute_messages',
    @level2type = N'COLUMN', @level2name = N'is_internal';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Indicates if message has attachments (see message_attachments table)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'dispute_messages',
    @level2type = N'COLUMN', @level2name = N'has_attachments';

-- V5__Create_message_attachments_table.sql
-- Junction table for message-evidence attachments (replaces PostgreSQL UUID array)

CREATE TABLE message_attachments (
    message_id          UNIQUEIDENTIFIER NOT NULL,
    evidence_id         UNIQUEIDENTIFIER NOT NULL,
    attachment_order    INT DEFAULT 0,
    
    CONSTRAINT pk_message_attachments PRIMARY KEY (message_id, evidence_id),
    CONSTRAINT fk_message_attachments_message 
        FOREIGN KEY (message_id) REFERENCES dispute_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_attachments_evidence 
        FOREIGN KEY (evidence_id) REFERENCES dispute_evidence(id)
);

-- Indexes for efficient lookups
CREATE NONCLUSTERED INDEX idx_msg_attachments_message ON message_attachments(message_id);
CREATE NONCLUSTERED INDEX idx_msg_attachments_evidence ON message_attachments(evidence_id);
CREATE NONCLUSTERED INDEX idx_msg_attachments_order ON message_attachments(message_id, attachment_order);

-- Extended Properties (Documentation)
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Junction table linking dispute messages to evidence files. Replaces PostgreSQL UUID array for MSSQL compatibility.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'message_attachments';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Reference to dispute_messages.id',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'message_attachments',
    @level2type = N'COLUMN', @level2name = N'message_id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Reference to dispute_evidence.id',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'message_attachments',
    @level2type = N'COLUMN', @level2name = N'evidence_id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Order of attachment within message (0 = first)',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'message_attachments',
    @level2type = N'COLUMN', @level2name = N'attachment_order';

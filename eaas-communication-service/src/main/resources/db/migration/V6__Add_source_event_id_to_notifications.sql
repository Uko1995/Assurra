ALTER TABLE notifications ADD source_event_id VARCHAR(255);
GO
CREATE UNIQUE INDEX idx_notification_user_event ON notifications(user_id, source_event_id) WHERE source_event_id IS NOT NULL;

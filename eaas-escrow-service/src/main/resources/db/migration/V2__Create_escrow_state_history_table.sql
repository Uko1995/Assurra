IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='escrow_state_history' AND xtype='U')
BEGIN
    CREATE TABLE escrow_state_history (
        id              UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        escrow_id       UNIQUEIDENTIFIER NOT NULL,
        from_status     NVARCHAR(20) 
                        CONSTRAINT chk_state_history_from_status 
                        CHECK (from_status IN ('INITIATED', 'FUNDED', 'MERCHANT_NOTIFIED', 'SHIPPED', 'DELIVERED',
                                              'CONFIRMED', 'DISPUTED', 'UNDER_REVIEW', 'AUTO_RELEASED', 'RELEASED',
                                              'RESOLVED_MERCHANT', 'RESOLVED_CUSTOMER', 'REFUNDED', 'CANCELLED')),
        to_status       NVARCHAR(20) NOT NULL
                        CONSTRAINT chk_state_history_to_status 
                        CHECK (to_status IN ('INITIATED', 'FUNDED', 'MERCHANT_NOTIFIED', 'SHIPPED', 'DELIVERED',
                                            'CONFIRMED', 'DISPUTED', 'UNDER_REVIEW', 'AUTO_RELEASED', 'RELEASED',
                                            'RESOLVED_MERCHANT', 'RESOLVED_CUSTOMER', 'REFUNDED', 'CANCELLED')),
        triggered_by    NVARCHAR(20) NOT NULL
                        CONSTRAINT chk_state_history_triggered_by 
                        CHECK (triggered_by IN ('CUSTOMER', 'MERCHANT', 'SYSTEM', 'ADMIN')),
        triggered_by_id UNIQUEIDENTIFIER,
        reason          NVARCHAR(MAX),
        metadata        NVARCHAR(MAX),
        created_at      DATETIME2 DEFAULT GETDATE(),
        
        CONSTRAINT fk_state_history_escrow 
            FOREIGN KEY (escrow_id) REFERENCES escrow_transactions(id)
    );
END

-- Indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_state_history_escrow_id' AND object_id=OBJECT_ID('escrow_state_history'))
    CREATE NONCLUSTERED INDEX idx_state_history_escrow_id ON escrow_state_history(escrow_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_state_history_created_at' AND object_id=OBJECT_ID('escrow_state_history'))
    CREATE NONCLUSTERED INDEX idx_state_history_created_at ON escrow_state_history(created_at);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_state_history_to_status' AND object_id=OBJECT_ID('escrow_state_history'))
    CREATE NONCLUSTERED INDEX idx_state_history_to_status ON escrow_state_history(to_status);

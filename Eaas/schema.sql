-- ============================================================
-- EaaS (Escrow as a Service) — Full Database Schema
-- Database: Microsoft SQL Server 2019+
-- Version: 1.0.0
-- ============================================================

-- ============================================================
-- ENABLE UUID GENERATION (NEWID() is built-in for MSSQL)
-- ============================================================

-- ============================================================
-- LOOKUP TABLES (Replacement for PostgreSQL ENUMs)
-- ============================================================

CREATE TABLE lookup_user_roles (
    role_code VARCHAR(20) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255)
);

INSERT INTO lookup_user_roles (role_code, role_name, description) VALUES
('CUSTOMER', 'Customer', 'Buyer making escrow-protected purchases'),
('MERCHANT', 'Merchant', 'Seller receiving escrow-protected payments'),
('ADMIN', 'Admin', 'EaaS platform administrator');

CREATE TABLE lookup_kyc_status (
    status_code VARCHAR(20) PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255)
);

INSERT INTO lookup_kyc_status (status_code, status_name, description) VALUES
('PENDING', 'Pending', 'KYC documents not yet submitted'),
('UNDER_REVIEW', 'Under Review', 'KYC documents submitted, awaiting review'),
('VERIFIED', 'Verified', 'KYC approved, merchant can receive payouts'),
('REJECTED', 'Rejected', 'KYC rejected, resubmission required');

CREATE TABLE lookup_escrow_status (
    status_code VARCHAR(20) PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255),
    is_terminal BIT DEFAULT 0
);

INSERT INTO lookup_escrow_status (status_code, status_name, description, is_terminal) VALUES
('INITIATED', 'Initiated', 'Escrow created, awaiting payment', 0),
('FUNDED', 'Funded', 'Payment received, merchant notification pending', 0),
('MERCHANT_NOTIFIED', 'Merchant Notified', 'Merchant notified to ship product', 0),
('SHIPPED', 'Shipped', 'Product shipped, awaiting delivery', 0),
('DELIVERED', 'Delivered', 'Product delivered, customer confirmation window open', 0),
('CONFIRMED', 'Confirmed', 'Customer confirmed receipt', 0),
('DISPUTED', 'Disputed', 'Customer raised a dispute', 0),
('UNDER_REVIEW', 'Under Review', 'Dispute under admin review', 0),
('AUTO_RELEASED', 'Auto Released', 'Funds auto-released after 72-hour window', 0),
('RELEASED', 'Released', 'Funds released to merchant', 1),
('RESOLVED_MERCHANT', 'Resolved - Merchant', 'Dispute resolved in merchant favor', 0),
('RESOLVED_CUSTOMER', 'Resolved - Customer', 'Dispute resolved in customer favor', 0),
('REFUNDED', 'Refunded', 'Funds refunded to customer', 1),
('CANCELLED', 'Cancelled', 'Escrow cancelled', 1);

CREATE TABLE lookup_dispute_reasons (
    reason_code VARCHAR(30) PRIMARY KEY,
    reason_name VARCHAR(100) NOT NULL,
    description NVARCHAR(500)
);

INSERT INTO lookup_dispute_reasons (reason_code, reason_name, description) VALUES
('ITEM_NOT_RECEIVED', 'Item Not Received', 'Customer did not receive the product'),
('ITEM_NOT_AS_DESCRIBED', 'Item Not As Described', 'Product differs from description'),
('ITEM_DAMAGED', 'Item Damaged', 'Product arrived damaged or defective'),
('WRONG_ITEM', 'Wrong Item', 'Incorrect product was delivered'),
('OTHER', 'Other', 'Other reason (requires description)');

CREATE TABLE lookup_dispute_status (
    status_code VARCHAR(30) PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255)
);

INSERT INTO lookup_dispute_status (status_code, status_name, description) VALUES
('OPEN', 'Open', 'Dispute raised, awaiting merchant response'),
('UNDER_REVIEW', 'Under Review', 'Admin reviewing dispute and evidence'),
('RESOLVED_MERCHANT', 'Resolved - Merchant', 'Dispute resolved in merchant favor'),
('RESOLVED_CUSTOMER', 'Resolved - Customer', 'Dispute resolved in customer favor');

CREATE TABLE lookup_payout_types (
    type_code VARCHAR(30) PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255)
);

INSERT INTO lookup_payout_types (type_code, type_name, description) VALUES
('MERCHANT_RELEASE', 'Merchant Release', 'Payout to merchant after successful transaction'),
('CUSTOMER_REFUND', 'Customer Refund', 'Refund to customer after dispute resolution');

CREATE TABLE lookup_payout_status (
    status_code VARCHAR(20) PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255)
);

INSERT INTO lookup_payout_status (status_code, status_name, description) VALUES
('PENDING', 'Pending', 'Payout queued, awaiting processing'),
('PROCESSING', 'Processing', 'Payout in progress'),
('SUCCESS', 'Success', 'Payout completed successfully'),
('FAILED', 'Failed', 'Payout failed, retry scheduled');

CREATE TABLE lookup_fee_types (
    type_code VARCHAR(20) PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255)
);

INSERT INTO lookup_fee_types (type_code, type_name, description) VALUES
('PERCENTAGE', 'Percentage', 'Fee calculated as percentage of transaction'),
('FLAT', 'Flat', 'Fixed fee amount');

CREATE TABLE lookup_triggered_by (
    triggered_by_code VARCHAR(20) PRIMARY KEY,
    triggered_by_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255)
);

INSERT INTO lookup_triggered_by (triggered_by_code, triggered_by_name, description) VALUES
('CUSTOMER', 'Customer', 'Action performed by customer'),
('MERCHANT', 'Merchant', 'Action performed by merchant'),
('SYSTEM', 'System', 'Action performed automatically by system'),
('ADMIN', 'Admin', 'Action performed by admin');

CREATE TABLE lookup_notification_channels (
    channel_code VARCHAR(20) PRIMARY KEY,
    channel_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255)
);

INSERT INTO lookup_notification_channels (channel_code, channel_name, description) VALUES
('EMAIL', 'Email', 'Notification sent via email'),
('SMS', 'SMS', 'Notification sent via SMS'),
('WEBHOOK', 'Webhook', 'Notification sent via webhook');

CREATE TABLE lookup_notification_status (
    status_code VARCHAR(20) PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255)
);

INSERT INTO lookup_notification_status (status_code, status_name, description) VALUES
('PENDING', 'Pending', 'Notification queued'),
('SENT', 'Sent', 'Notification delivered successfully'),
('FAILED', 'Failed', 'Notification delivery failed');

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    full_name NVARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    email_verified BIT DEFAULT 0,
    email_verify_token VARCHAR(255),
    is_active BIT DEFAULT 1,
    last_login_at DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_users_role FOREIGN KEY (role) REFERENCES lookup_user_roles(role_code),
    CONSTRAINT fk_users_kyc_status FOREIGN KEY (kyc_status) REFERENCES lookup_kyc_status(status_code)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- ============================================================
-- MERCHANT PROFILES
-- ============================================================

CREATE TABLE merchant_profiles (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    business_name NVARCHAR(255) NOT NULL,
    business_type NVARCHAR(100),
    business_reg_number VARCHAR(100),                       -- CAC number
    bank_account_number VARCHAR(255) NOT NULL,              -- encrypted
    bank_code VARCHAR(10) NOT NULL,
    bank_name NVARCHAR(100) NOT NULL,
    bvn VARCHAR(255),                                       -- encrypted
    settlement_email VARCHAR(255),
    api_key VARCHAR(255) UNIQUE,                            -- hashed
    api_key_prefix VARCHAR(20),                             -- sk_live_****XXXX (display only)
    webhook_url NVARCHAR(500),
    webhook_secret VARCHAR(255),                            -- for HMAC signing
    is_verified BIT DEFAULT 0,
    kyc_submitted_at DATETIME2,
    kyc_reviewed_at DATETIME2,
    kyc_reviewed_by UNIQUEIDENTIFIER,
    kyc_rejection_reason NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_merchant_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_merchant_profiles_reviewed_by FOREIGN KEY (kyc_reviewed_by) REFERENCES users(id)
);

CREATE INDEX idx_merchant_user_id ON merchant_profiles(user_id);
CREATE UNIQUE INDEX idx_merchant_api_key ON merchant_profiles(api_key) WHERE api_key IS NOT NULL;

-- ============================================================
-- KYC DOCUMENTS
-- ============================================================

CREATE TABLE kyc_documents (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    merchant_id UNIQUEIDENTIFIER NOT NULL,
    document_type VARCHAR(50) NOT NULL,   -- CAC_CERT | UTILITY_BILL | ID | PASSPORT
    file_url NVARCHAR(500) NOT NULL,      -- S3 URL
    file_name NVARCHAR(255),
    uploaded_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_kyc_docs_merchant FOREIGN KEY (merchant_id) REFERENCES merchant_profiles(id) ON DELETE CASCADE
);

-- ============================================================
-- FEE CONFIGURATIONS
-- ============================================================

CREATE TABLE fee_configurations (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    merchant_id UNIQUEIDENTIFIER,  -- NULL = global default
    fee_type VARCHAR(20) DEFAULT 'PERCENTAGE',
    fee_value DECIMAL(5,2) NOT NULL,                  -- 1.5 = 1.5%
    min_fee DECIMAL(10,2) DEFAULT 500.00,             -- ₦500 minimum
    max_fee DECIMAL(10,2) DEFAULT 50000.00,           -- ₦50,000 maximum
    is_active BIT DEFAULT 1,
    created_by UNIQUEIDENTIFIER,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_fee_config_merchant FOREIGN KEY (merchant_id) REFERENCES merchant_profiles(id),
    CONSTRAINT fk_fee_config_type FOREIGN KEY (fee_type) REFERENCES lookup_fee_types(type_code),
    CONSTRAINT fk_fee_config_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Unique index: only one active default config at a time
CREATE UNIQUE INDEX idx_fee_config_default
    ON fee_configurations(is_active)
    WHERE merchant_id IS NULL AND is_active = 1;

-- ============================================================
-- ESCROW TRANSACTIONS (Core table)
-- ============================================================

CREATE TABLE escrow_transactions (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    reference VARCHAR(100) UNIQUE NOT NULL,       -- TXN-2024-A3F9K2
    customer_id UNIQUEIDENTIFIER NOT NULL,
    merchant_id UNIQUEIDENTIFIER NOT NULL,
    merchant_profile_id UNIQUEIDENTIFIER NOT NULL,

    -- Amounts
    amount DECIMAL(15,2) NOT NULL,
    escrow_fee DECIMAL(15,2) NOT NULL,
    merchant_amount DECIMAL(15,2) NOT NULL,             -- amount - fee (merchant receives)
    currency VARCHAR(3) DEFAULT 'NGN',

    -- Status / State Machine
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',

    -- Product Details
    product_description NVARCHAR(MAX) NOT NULL,
    product_quantity INTEGER DEFAULT 1,
    agreed_delivery_days INTEGER DEFAULT 7,

    -- Payment
    payment_reference VARCHAR(255),                       -- Interswitch reference
    payment_channel VARCHAR(50),                          -- card | bank_transfer | ussd
    payment_link NVARCHAR(500),
    funded_at DATETIME2,
    payment_expires_at DATETIME2,                         -- INITIATED + 24hrs

    -- Shipping
    tracking_number VARCHAR(255),
    logistics_provider NVARCHAR(100),
    estimated_delivery_date DATE,
    shipped_at DATETIME2,
    delivered_at DATETIME2,

    -- Confirmation Window
    confirmation_deadline DATETIME2,                      -- delivered_at + 72hrs
    auto_release_at DATETIME2,
    confirmed_at DATETIME2,

    -- Payout
    payout_reference VARCHAR(255),
    paid_out_at DATETIME2,

    -- Idempotency
    idempotency_key VARCHAR(255) UNIQUE,

    -- Metadata
    metadata NVARCHAR(MAX),  -- JSON stored as NVARCHAR(MAX)
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_escrow_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_escrow_merchant FOREIGN KEY (merchant_id) REFERENCES users(id),
    CONSTRAINT fk_escrow_merchant_profile FOREIGN KEY (merchant_profile_id) REFERENCES merchant_profiles(id),
    CONSTRAINT fk_escrow_status FOREIGN KEY (status) REFERENCES lookup_escrow_status(status_code)
);

CREATE INDEX idx_escrow_reference ON escrow_transactions(reference);
CREATE INDEX idx_escrow_customer ON escrow_transactions(customer_id);
CREATE INDEX idx_escrow_merchant ON escrow_transactions(merchant_id);
CREATE INDEX idx_escrow_status ON escrow_transactions(status);
CREATE INDEX idx_escrow_auto_release ON escrow_transactions(auto_release_at) WHERE status = 'DELIVERED';
CREATE INDEX idx_escrow_created_at ON escrow_transactions(created_at);
CREATE INDEX idx_escrow_idempotency ON escrow_transactions(idempotency_key);

-- ============================================================
-- ESCROW STATE HISTORY (Immutable audit trail)
-- ============================================================

CREATE TABLE escrow_state_history (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    escrow_id UNIQUEIDENTIFIER NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    triggered_by VARCHAR(20) NOT NULL,
    triggered_by_id UNIQUEIDENTIFIER,
    reason NVARCHAR(MAX),
    metadata NVARCHAR(MAX),  -- JSON stored as NVARCHAR(MAX)
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_state_history_escrow FOREIGN KEY (escrow_id) REFERENCES escrow_transactions(id),
    CONSTRAINT fk_state_history_triggered_by FOREIGN KEY (triggered_by) REFERENCES lookup_triggered_by(triggered_by_code),
    CONSTRAINT fk_state_history_from_status FOREIGN KEY (from_status) REFERENCES lookup_escrow_status(status_code),
    CONSTRAINT fk_state_history_to_status FOREIGN KEY (to_status) REFERENCES lookup_escrow_status(status_code),
    CONSTRAINT fk_state_history_user FOREIGN KEY (triggered_by_id) REFERENCES users(id)
);

-- Append-only: no updates or deletes allowed (enforced in application layer)
CREATE INDEX idx_state_history_escrow ON escrow_state_history(escrow_id);
CREATE INDEX idx_state_history_created ON escrow_state_history(created_at);

-- ============================================================
-- DISPUTES
-- ============================================================

CREATE TABLE disputes (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    escrow_id UNIQUEIDENTIFIER NOT NULL,
    raised_by UNIQUEIDENTIFIER NOT NULL,
    reason VARCHAR(30) NOT NULL,
    description NVARCHAR(MAX) NOT NULL,
    status VARCHAR(30) DEFAULT 'OPEN',
    admin_note NVARCHAR(MAX),                                   -- internal note
    resolution_note NVARCHAR(MAX),                              -- shared with parties
    resolved_by UNIQUEIDENTIFIER,                              -- admin
    merchant_responded BIT DEFAULT 0,
    merchant_response NVARCHAR(MAX),
    merchant_responded_at DATETIME2,
    response_deadline DATETIME2,                               -- raised_at + 24hrs
    resolved_at DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_disputes_escrow FOREIGN KEY (escrow_id) REFERENCES escrow_transactions(id),
    CONSTRAINT fk_disputes_raised_by FOREIGN KEY (raised_by) REFERENCES users(id),
    CONSTRAINT fk_disputes_resolved_by FOREIGN KEY (resolved_by) REFERENCES users(id),
    CONSTRAINT fk_disputes_reason FOREIGN KEY (reason) REFERENCES lookup_dispute_reasons(reason_code),
    CONSTRAINT fk_disputes_status FOREIGN KEY (status) REFERENCES lookup_dispute_status(status_code)
);

CREATE INDEX idx_disputes_escrow ON disputes(escrow_id);
CREATE INDEX idx_disputes_status ON disputes(status);
CREATE INDEX idx_disputes_raised_by ON disputes(raised_by);

-- ============================================================
-- DISPUTE EVIDENCE
-- ============================================================

CREATE TABLE dispute_evidence (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    dispute_id UNIQUEIDENTIFIER NOT NULL,
    uploaded_by UNIQUEIDENTIFIER NOT NULL,
    uploaded_by_role VARCHAR(20) NOT NULL,
    file_url NVARCHAR(500) NOT NULL,                      -- S3 URL
    file_name NVARCHAR(255),
    file_type VARCHAR(50),                                -- image | video | pdf
    file_size_kb INTEGER,
    description NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_evidence_dispute FOREIGN KEY (dispute_id) REFERENCES disputes(id) ON DELETE CASCADE,
    CONSTRAINT fk_evidence_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id),
    CONSTRAINT fk_evidence_role FOREIGN KEY (uploaded_by_role) REFERENCES lookup_user_roles(role_code)
);

CREATE INDEX idx_evidence_dispute ON dispute_evidence(dispute_id);

-- ============================================================
-- PAYOUTS
-- ============================================================

CREATE TABLE payouts (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    escrow_id UNIQUEIDENTIFIER NOT NULL,
    recipient_id UNIQUEIDENTIFIER NOT NULL,
    payout_type VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'NGN',
    bank_account VARCHAR(255),                           -- encrypted snapshot
    bank_code VARCHAR(10),
    bank_name NVARCHAR(100),
    psp_reference VARCHAR(255),                          -- Interswitch transfer ref
    psp_response NVARCHAR(MAX),                          -- raw Interswitch response (JSON)
    status VARCHAR(20) DEFAULT 'PENDING',
    failure_reason NVARCHAR(MAX),
    retry_count INTEGER DEFAULT 0,
    next_retry_at DATETIME2,
    initiated_at DATETIME2 DEFAULT GETDATE(),
    completed_at DATETIME2,
    
    CONSTRAINT fk_payouts_escrow FOREIGN KEY (escrow_id) REFERENCES escrow_transactions(id),
    CONSTRAINT fk_payouts_recipient FOREIGN KEY (recipient_id) REFERENCES users(id),
    CONSTRAINT fk_payouts_type FOREIGN KEY (payout_type) REFERENCES lookup_payout_types(type_code),
    CONSTRAINT fk_payouts_status FOREIGN KEY (status) REFERENCES lookup_payout_status(status_code)
);

CREATE INDEX idx_payouts_escrow ON payouts(escrow_id);
CREATE INDEX idx_payouts_recipient ON payouts(recipient_id);
CREATE INDEX idx_payouts_status ON payouts(status);
CREATE INDEX idx_payouts_next_retry ON payouts(next_retry_at) WHERE status = 'FAILED';

-- ============================================================
-- NOTIFICATIONS
-- ============================================================

CREATE TABLE notifications (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    escrow_id UNIQUEIDENTIFIER,
    recipient_id UNIQUEIDENTIFIER NOT NULL,
    channel VARCHAR(20) NOT NULL,
    event_type VARCHAR(100) NOT NULL,                     -- escrow.funded, escrow.shipped etc.
    subject NVARCHAR(255),                               -- email subject
    body NVARCHAR(MAX) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    sent_at DATETIME2,
    failure_reason NVARCHAR(MAX),
    retry_count INTEGER DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_notifications_escrow FOREIGN KEY (escrow_id) REFERENCES escrow_transactions(id),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(id),
    CONSTRAINT fk_notifications_channel FOREIGN KEY (channel) REFERENCES lookup_notification_channels(channel_code),
    CONSTRAINT fk_notifications_status FOREIGN KEY (status) REFERENCES lookup_notification_status(status_code)
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_escrow ON notifications(escrow_id);

-- ============================================================
-- WEBHOOK DELIVERY LOG
-- ============================================================

CREATE TABLE webhook_deliveries (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    merchant_id UNIQUEIDENTIFIER NOT NULL,
    escrow_id UNIQUEIDENTIFIER,
    event_type VARCHAR(100) NOT NULL,
    payload NVARCHAR(MAX) NOT NULL,
    webhook_url NVARCHAR(500) NOT NULL,
    status_code INTEGER,
    response_body NVARCHAR(MAX),
    is_success BIT,
    attempt_number INTEGER DEFAULT 1,
    next_retry_at DATETIME2,
    delivered_at DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_webhook_merchant FOREIGN KEY (merchant_id) REFERENCES merchant_profiles(id),
    CONSTRAINT fk_webhook_escrow FOREIGN KEY (escrow_id) REFERENCES escrow_transactions(id)
);

CREATE INDEX idx_webhook_merchant ON webhook_deliveries(merchant_id);
CREATE INDEX idx_webhook_escrow ON webhook_deliveries(escrow_id);
CREATE INDEX idx_webhook_retry ON webhook_deliveries(next_retry_at) WHERE is_success = 0 OR is_success IS NULL;

-- ============================================================
-- IDEMPOTENCY RECORDS (Fast lookup cache)
-- ============================================================

CREATE TABLE idempotency_records (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    user_id UNIQUEIDENTIFIER NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    response_status INTEGER,
    response_body NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    expires_at DATETIME2 DEFAULT DATEADD(hour, 24, GETDATE()),
    
    CONSTRAINT fk_idempotency_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_idempotency_expires ON idempotency_records(expires_at);

-- ============================================================
-- AUDIT LOG (Immutable system-wide log)
-- ============================================================

CREATE TABLE audit_logs (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    entity_type VARCHAR(50) NOT NULL,                      -- ESCROW | DISPUTE | PAYOUT | USER
    entity_id UNIQUEIDENTIFIER NOT NULL,
    action VARCHAR(100) NOT NULL,                          -- CREATED | STATUS_CHANGED | RESOLVED
    performed_by UNIQUEIDENTIFIER,
    performed_by_role VARCHAR(20),
    ip_address VARCHAR(45),
    user_agent NVARCHAR(MAX),
    old_values NVARCHAR(MAX),                              -- JSON
    new_values NVARCHAR(MAX),                              -- JSON
    metadata NVARCHAR(MAX),                                -- JSON
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_audit_performed_by FOREIGN KEY (performed_by) REFERENCES users(id),
    CONSTRAINT fk_audit_role FOREIGN KEY (performed_by_role) REFERENCES lookup_user_roles(role_code)
);

CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_performed_by ON audit_logs(performed_by);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);

-- ============================================================
-- SEED DATA — Default Fee Configuration
-- ============================================================

INSERT INTO fee_configurations (fee_type, fee_value, min_fee, max_fee, is_active)
VALUES ('PERCENTAGE', 1.50, 500.00, 50000.00, 1);

-- ============================================================
-- STORED PROCEDURES (MSSQL Specific)
-- ============================================================

-- Procedure to clean up expired idempotency records
CREATE PROCEDURE sp_cleanup_expired_idempotency
AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM idempotency_records WHERE expires_at < GETDATE();
END;
GO

-- Procedure to get escrows ready for auto-release
CREATE PROCEDURE sp_get_escrows_for_auto_release
AS
BEGIN
    SET NOCOUNT ON;
    SELECT * FROM escrow_transactions 
    WHERE status = 'DELIVERED' 
    AND auto_release_at <= GETDATE();
END;
GO

-- Procedure to update escrow status with history tracking
CREATE PROCEDURE sp_update_escrow_status
    @escrow_id UNIQUEIDENTIFIER,
    @new_status VARCHAR(20),
    @triggered_by VARCHAR(20),
    @triggered_by_id UNIQUEIDENTIFIER,
    @reason NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @old_status VARCHAR(20);
    
    -- Get current status
    SELECT @old_status = status FROM escrow_transactions WHERE id = @escrow_id;
    
    -- Update escrow status
    UPDATE escrow_transactions 
    SET status = @new_status, 
        updated_at = GETDATE()
    WHERE id = @escrow_id;
    
    -- Record state history
    INSERT INTO escrow_state_history (escrow_id, from_status, to_status, triggered_by, triggered_by_id, reason, created_at)
    VALUES (@escrow_id, @old_status, @new_status, @triggered_by, @triggered_by_id, @reason, GETDATE());
END;
GO

CREATE TABLE aml_alerts (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    payment_id UNIQUEIDENTIFIER NOT NULL,
    customer_id UNIQUEIDENTIFIER NOT NULL,
    merchant_id UNIQUEIDENTIFIER NULL,
    alert_type NVARCHAR(50) NOT NULL,
    amount DECIMAL(15,2),
    currency NVARCHAR(3) DEFAULT 'NGN',
    status NVARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN','UNDER_REVIEW','CLOSED_FALSE_POSITIVE','CONFIRMED_SUSPICIOUS','SAR_FILED')),
    reviewed_by UNIQUEIDENTIFIER NULL,
    reviewed_at DATETIME2 NULL,
    notes NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX idx_aml_alert_status ON aml_alerts(status);
CREATE INDEX idx_aml_alert_customer ON aml_alerts(customer_id);
CREATE INDEX idx_aml_alert_type ON aml_alerts(alert_type);
GO

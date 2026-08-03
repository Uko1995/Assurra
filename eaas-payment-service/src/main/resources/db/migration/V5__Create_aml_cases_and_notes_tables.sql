CREATE TABLE aml_cases (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    alert_id UNIQUEIDENTIFIER NOT NULL,
    assigned_to UNIQUEIDENTIFIER NULL,
    assigned_at DATETIME2 NULL,
    reviewed_at DATETIME2 NULL,
    closed_at DATETIME2 NULL,
    priority NVARCHAR(20) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    status NVARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN','UNDER_REVIEW','ESCALATED','CLOSED_FALSE_POSITIVE','CLOSED_CONFIRMED','SAR_FILED')),
    resolution NVARCHAR(20) NULL,
    sar_reference NVARCHAR(100) NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX idx_aml_case_status ON aml_cases(status);
CREATE INDEX idx_aml_case_assigned ON aml_cases(assigned_to);
CREATE INDEX idx_aml_case_alert ON aml_cases(alert_id);

CREATE TABLE aml_case_notes (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    case_id UNIQUEIDENTIFIER NOT NULL,
    created_by UNIQUEIDENTIFIER NOT NULL,
    note NVARCHAR(MAX) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX idx_aml_case_notes_case ON aml_case_notes(case_id);
GO

-- Store historical versions of privacy policy and terms of service
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='privacy_policies' AND xtype='U')
BEGIN
    CREATE TABLE privacy_policies (
        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        version NVARCHAR(20) NOT NULL,
        title NVARCHAR(255) NOT NULL,
        content NVARCHAR(MAX) NOT NULL,
        effective_from DATETIME2 NOT NULL,
        effective_until DATETIME2 NULL,
        is_active BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE()
    );

    CREATE UNIQUE INDEX idx_privacy_policy_version ON privacy_policies(version);
END

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='terms_of_service' AND xtype='U')
BEGIN
    CREATE TABLE terms_of_service (
        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        version NVARCHAR(20) NOT NULL,
        title NVARCHAR(255) NOT NULL,
        content NVARCHAR(MAX) NOT NULL,
        effective_from DATETIME2 NOT NULL,
        effective_until DATETIME2 NULL,
        is_active BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE()
    );

    CREATE UNIQUE INDEX idx_terms_version ON terms_of_service(version);
END

-- Seed initial privacy policy and terms of service (only if not already seeded)
IF NOT EXISTS (SELECT * FROM privacy_policies WHERE version = '1.0')
    INSERT INTO privacy_policies (version, title, content, effective_from, is_active)
    VALUES ('1.0', 'EaaS Privacy Policy',
        'This privacy policy describes how EaaS collects, uses, and protects your personal data in compliance with the Nigeria Data Protection Regulation (NDPR) and GDPR.\n\n1. Data We Collect: email, phone, BVN, bank account details, transaction history.\n2. How We Use Your Data: to provide escrow services, process payments, comply with legal obligations.\n3. Your Rights: access, rectification, erasure, portability, restriction of processing.\n4. Data Retention: 7 years for transaction records as required by CBN.\n5. Cross-Border Transfers: KYC documents may be stored via Cloudinary. By using our service, you consent to this transfer.',
        GETDATE(), 1);

IF NOT EXISTS (SELECT * FROM terms_of_service WHERE version = '1.0')
    INSERT INTO terms_of_service (version, title, content, effective_from, is_active)
    VALUES ('1.0', 'EaaS Terms of Service',
        'These terms govern your use of the EaaS escrow platform.\n\n1. Eligibility: You must be 18 years or older.\n2. Escrow Services: We hold funds securely until both parties confirm satisfaction.\n3. Fees: 1.5% per transaction (min ₦500, max ₦50,000) unless otherwise configured.\n4. Dispute Resolution: disputes must be raised within 72 hours of delivery confirmation.\n5. Prohibited Use: fraud, money laundering, or any illegal activity.\n6. Limitation of Liability: EaaS liability is limited to the escrow amount.',
        GETDATE(), 1);

GO

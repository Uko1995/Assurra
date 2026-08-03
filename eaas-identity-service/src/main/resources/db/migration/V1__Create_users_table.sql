-- V1__Create_users_table.sql
-- Create the users table for authentication and user management

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
BEGIN
    CREATE TABLE users (
        id                      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        email                   NVARCHAR(500) NOT NULL,
        phone                   NVARCHAR(100),
        full_name               NVARCHAR(255) NOT NULL,
        password_hash           NVARCHAR(255) NOT NULL,
        role                    NVARCHAR(20) NOT NULL 
                                CONSTRAINT chk_users_role 
                                CHECK (role IN ('CUSTOMER', 'MERCHANT', 'ADMIN')),
        kyc_status              NVARCHAR(20) DEFAULT 'PENDING'
                                CONSTRAINT chk_users_kyc_status 
                                CHECK (kyc_status IN ('PENDING', 'UNDER_REVIEW', 'VERIFIED', 'REJECTED')),
        email_verified          BIT DEFAULT 0,
        email_verify_token      NVARCHAR(255),
        is_active               BIT DEFAULT 1,
        last_login_at           DATETIME2,
        failed_login_attempts   INT DEFAULT 0,
        locked_until            DATETIME2,
        last_failed_login_at    DATETIME2,
        consent_given           BIT DEFAULT 0,
        consent_given_at        DATETIME2,
        terms_accepted          BIT DEFAULT 0,
        terms_accepted_at       DATETIME2,
        privacy_policy_version  NVARCHAR(20),
        marketing_consent       BIT DEFAULT 0,
        data_processing_consent BIT DEFAULT 0,
        dpo_contact_notified    BIT DEFAULT 0,
        created_at              DATETIME2 DEFAULT GETDATE(),
        updated_at              DATETIME2 DEFAULT GETDATE(),
        
        CONSTRAINT uq_users_email UNIQUE (email)
    );
END

-- Indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_users_email_lookup' AND object_id=OBJECT_ID('users'))
    CREATE INDEX idx_users_email_lookup ON users(email)
    INCLUDE (id, password_hash, role, kyc_status, is_active, email_verified, full_name, phone, created_at, last_login_at);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_users_role_active' AND object_id=OBJECT_ID('users'))
    CREATE INDEX idx_users_role_active ON users(role, is_active)
    INCLUDE (id, email, full_name, kyc_status);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_users_kyc_status' AND object_id=OBJECT_ID('users'))
    CREATE INDEX idx_users_kyc_status ON users(kyc_status, role)
    WHERE role = 'MERCHANT';

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_users_email_verify_token' AND object_id=OBJECT_ID('users'))
    CREATE INDEX idx_users_email_verify_token ON users(email_verify_token) 
        WHERE email_verify_token IS NOT NULL;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_users_locked' AND object_id=OBJECT_ID('users'))
    CREATE INDEX idx_users_locked ON users(locked_until) 
        WHERE locked_until IS NOT NULL;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_users_failed_logins' AND object_id=OBJECT_ID('users'))
    CREATE INDEX idx_users_failed_logins ON users(failed_login_attempts, last_failed_login_at)
        WHERE failed_login_attempts > 0;

-- Extended Properties (Documentation)
IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', NULL, NULL))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'User accounts for customers, merchants, and admins',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', N'COLUMN', N'email'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Unique email address, used for login',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users',
        @level2type = N'COLUMN', @level2name = N'email';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', N'COLUMN', N'password_hash'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'BCrypt hashed password',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users',
        @level2type = N'COLUMN', @level2name = N'password_hash';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', N'COLUMN', N'role'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'User role: CUSTOMER, MERCHANT, or ADMIN',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users',
        @level2type = N'COLUMN', @level2name = N'role';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', N'COLUMN', N'kyc_status'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'KYC status for merchants only',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users',
        @level2type = N'COLUMN', @level2name = N'kyc_status';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', N'COLUMN', N'email_verified'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Whether email has been verified',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users',
        @level2type = N'COLUMN', @level2name = N'email_verified';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', N'COLUMN', N'is_active'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Soft delete flag',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users',
        @level2type = N'COLUMN', @level2name = N'is_active';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', N'COLUMN', N'failed_login_attempts'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Number of consecutive failed login attempts',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users',
        @level2type = N'COLUMN', @level2name = N'failed_login_attempts';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', N'COLUMN', N'locked_until'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Timestamp until which the account is locked due to failed attempts',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users',
        @level2type = N'COLUMN', @level2name = N'locked_until';

IF NOT EXISTS (SELECT * FROM fn_listextendedproperty(N'MS_Description', N'SCHEMA', N'dbo', N'TABLE', N'users', N'COLUMN', N'last_failed_login_at'))
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', @value = N'Timestamp of the last failed login attempt',
        @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'users',
        @level2type = N'COLUMN', @level2name = N'last_failed_login_at';

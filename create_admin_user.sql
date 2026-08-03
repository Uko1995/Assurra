-- ============================================================
-- Create Admin User directly in identity_db
-- Run this in SSMS or sqlcmd against the identity_db database
--
-- Default credentials:
--   Email:    admin@eaas.local
--   Password: Admin@123
-- ============================================================

USE identity_db;
GO

DECLARE @AdminId UNIQUEIDENTIFIER = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890';

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@eaas.local')
BEGIN
    INSERT INTO users (
        id,
        email,
        phone,
        full_name,
        password_hash,
        role,
        kyc_status,
        email_verified,
        email_verify_token,
        is_active,
        failed_login_attempts,
        created_at,
        updated_at
    )
    VALUES (
        @AdminId,
        'admin@eaas.local',
        '+2348000000000',
        'System Administrator',
        '$2b$10$gRmDZPdztibFSLk1sw9N/OnbZx8us4wyYyJx6LrxtQtcryUA9lSc2',  -- BCrypt hash for 'Admin@123'
        'ADMIN',
        NULL,
        1,      -- email_verified = true
        NULL,   -- no verification token needed
        1,      -- is_active = true
        0,      -- failed_login_attempts
        GETDATE(),
        GETDATE()
    );

    PRINT 'Admin user created successfully.';
    PRINT 'Email:    admin@eaas.local';
    PRINT 'Password: Admin@123';
    PRINT 'User ID:  ' + CAST(@AdminId AS NVARCHAR(36));
END
ELSE
BEGIN
    PRINT 'Admin user with email admin@eaas.local already exists. Skipping.';
END
GO

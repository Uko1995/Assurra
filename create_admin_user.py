#!/usr/bin/env python3
"""
Script to generate BCrypt password hash and SQL for creating an admin user.

Usage:
    python3 create_admin_user.py

Then copy the generated SQL into SSMS or run it directly against the identity_db.
"""

import uuid
import sys


def generate_hash(password: str) -> str:
    """Generate BCrypt hash using Python's bcrypt library."""
    try:
        import bcrypt
        salt = bcrypt.gensalt(rounds=10)
        hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
        return hashed.decode('utf-8')
    except ImportError:
        print("ERROR: 'bcrypt' package is not installed.")
        print("Install it with:  pip install bcrypt")
        sys.exit(1)


def generate_sql(email: str, password: str, full_name: str) -> str:
    user_id = str(uuid.uuid4())
    password_hash = generate_hash(password)

    sql = f"""-- ============================================================
-- Create Admin User directly in identity_db
-- Run this in SSMS or sqlcmd against the identity_db database
-- ============================================================

USE identity_db;
GO

DECLARE @AdminId UNIQUEIDENTIFIER = '{user_id}';

IF NOT EXISTS (SELECT 1 FROM users WHERE email = '{email}')
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
        '{email}',
        '+2348000000000',
        '{full_name}',
        '{password_hash}',
        'ADMIN',
        NULL,
        1,
        NULL,
        1,
        0,
        GETDATE(),
        GETDATE()
    );

    PRINT 'Admin user created successfully.';
    PRINT 'Email:    {email}';
    PRINT 'Password: {password}';
    PRINT 'User ID:  ' + CAST(@AdminId AS NVARCHAR(36));
END
ELSE
BEGIN
    PRINT 'Admin user with email {email} already exists. Skipping.';
END
GO
"""
    return sql


if __name__ == "__main__":
    # Default credentials — change these as needed
    ADMIN_EMAIL = "admin@eaas.local"
    ADMIN_PASSWORD = "Admin@123"
    ADMIN_FULL_NAME = "System Administrator"

    print("=" * 60)
    print("EaaS Admin User Creation Script")
    print("=" * 60)
    print()

    sql = generate_sql(ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN_FULL_NAME)

    print(sql)
    print()
    print("=" * 60)
    print("INSTRUCTIONS:")
    print("1. Copy the SQL block above")
    print("2. Open SSMS or sqlcmd and connect to identity_db")
    print("3. Paste and execute the SQL")
    print("4. Login with email: " + ADMIN_EMAIL)
    print("   and password:    " + ADMIN_PASSWORD)
    print("=" * 60)

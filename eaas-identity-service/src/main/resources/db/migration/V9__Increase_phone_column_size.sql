-- V9__Increase_phone_column_size.sql
-- Encrypted phone values are ~120+ chars, increase from NVARCHAR(100) to NVARCHAR(500)

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'phone')
    AND EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'phone' AND CHARACTER_MAXIMUM_LENGTH = 100)
BEGIN
    ALTER TABLE users ALTER COLUMN phone NVARCHAR(500);
END

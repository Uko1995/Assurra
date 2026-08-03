IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dispute_evidence') AND name = 'cloudinary_public_id')
    ALTER TABLE dispute_evidence ADD cloudinary_public_id NVARCHAR(500) NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dispute_evidence') AND name = 'cloudinary_url')
    ALTER TABLE dispute_evidence ADD cloudinary_url NVARCHAR(1000) NULL;
GO

UPDATE dispute_evidence SET cloudinary_public_id = s3_key, cloudinary_url = s3_url WHERE s3_key IS NOT NULL;
GO

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dispute_evidence') AND name = 'cloudinary_public_id')
    AND EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_NAME = 'dispute_evidence' AND COLUMN_NAME = 'cloudinary_public_id' AND IS_NULLABLE = 'YES')
    ALTER TABLE dispute_evidence ALTER COLUMN cloudinary_public_id NVARCHAR(500) NOT NULL;
GO

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dispute_evidence') AND name = 'cloudinary_url')
    AND EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_NAME = 'dispute_evidence' AND COLUMN_NAME = 'cloudinary_url' AND IS_NULLABLE = 'YES')
    ALTER TABLE dispute_evidence ALTER COLUMN cloudinary_url NVARCHAR(1000) NOT NULL;
GO

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dispute_evidence') AND name = 's3_bucket')
    ALTER TABLE dispute_evidence DROP COLUMN s3_bucket;
GO

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dispute_evidence') AND name = 's3_key')
    ALTER TABLE dispute_evidence DROP COLUMN s3_key;
GO

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dispute_evidence') AND name = 's3_url')
    ALTER TABLE dispute_evidence DROP COLUMN s3_url;
GO

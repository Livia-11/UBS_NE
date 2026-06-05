ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS email_verification_otp VARCHAR(10);

ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS otp_expires_at TIMESTAMP;

ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS customer_id BIGINT;

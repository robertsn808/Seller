-- Initialize the seller_funnel database
-- This script will be run when PostgreSQL container starts for the first time

-- Create database (this is handled by POSTGRES_DB environment variable)
-- CREATE DATABASE seller_funnel;

-- The tables will be created automatically by Hibernate when the application starts
-- due to spring.jpa.hibernate.ddl-auto=update

-- You can add any initial data or custom indexes here if needed
-- For example:

-- CREATE INDEX IF NOT EXISTS idx_buyers_created_at ON buyers(created_at);
-- CREATE INDEX IF NOT EXISTS idx_sellers_created_at ON sellers(created_at);
-- CREATE INDEX IF NOT EXISTS idx_property_photos_seller_id ON property_photos(seller_id);
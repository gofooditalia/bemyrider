--
-- SQL Script for Bemyrider Job Board Feature
-- Created by Gemini - 2024
--

-- 1. Table: jobs
-- This table stores the job posts created by Customers (Esercenti).
CREATE TABLE IF NOT EXISTS `jobs` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `customer_id` INT(11) NOT NULL COMMENT 'FK to users table',
    `title` VARCHAR(150) NOT NULL,
    `description` TEXT NOT NULL,
    `vehicle_required` ENUM('auto', 'moto', 'bici', 'indifferente') DEFAULT 'indifferente',
    `start_at` DATETIME NOT NULL COMMENT 'Start date and time of service',
    `end_at` DATETIME DEFAULT NULL COMMENT 'End date and time of service (optional)',
    `compensation` DECIMAL(10,2) NOT NULL,
    `compensation_type` ENUM('fisso', 'orario') DEFAULT 'fisso',
    `address` VARCHAR(255) NOT NULL,
    `latitude` DOUBLE DEFAULT NULL,
    `longitude` DOUBLE DEFAULT NULL,
    `status` ENUM('open', 'selected', 'completed', 'cancelled') DEFAULT 'open',
    `service_request_id` INT(11) DEFAULT NULL COMMENT 'Link to existing service_requests table once hired',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_customer` (`customer_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_geo` (`latitude`, `longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Table: job_applications
-- This table stores the applications from Riders (Partners).
CREATE TABLE IF NOT EXISTS `job_applications` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `job_id` INT(11) NOT NULL,
    `rider_id` INT(11) NOT NULL COMMENT 'FK to users table',
    `status` ENUM('pending', 'hired', 'rejected') DEFAULT 'pending',
    `applied_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_app` (`job_id`, `rider_id`), -- Prevents double application
    INDEX `idx_job` (`job_id`),
    INDEX `idx_rider` (`rider_id`),
    CONSTRAINT `fk_job_id` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Note for Developer:
-- Ensure that 'customer_id' and 'rider_id' reference the primary key of your main Users/Providers table.
-- The 'service_request_id' should reference your existing bookings table (e.g., service_requests).
--

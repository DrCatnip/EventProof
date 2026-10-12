-- Run against the database named in DB_URL. Requires MySQL 8.0.16+ for enforced CHECKs.
-- Creation only: existing tables are not modified. No credentials or demo rows.
CREATE TABLE IF NOT EXISTS venues (
    venue_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venue_name VARCHAR(120) NOT NULL,
    capacity INT NOT NULL,
    backup_power BOOLEAN NOT NULL,
    backup_internet BOOLEAN NOT NULL,
    CONSTRAINT chk_venue_name CHECK (CHAR_LENGTH(TRIM(venue_name)) > 0),
    CONSTRAINT chk_venue_capacity CHECK (capacity > 0),
    CONSTRAINT chk_venue_power CHECK (backup_power IN (0, 1)),
    CONSTRAINT chk_venue_internet CHECK (backup_internet IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS events (
    event_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_name VARCHAR(120) NOT NULL,
    event_type VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    event_date DATE NOT NULL,
    expected_attendance INT NOT NULL,
    venue_id BIGINT NOT NULL,
    CONSTRAINT chk_event_name CHECK (CHAR_LENGTH(TRIM(event_name)) > 0),
    CONSTRAINT chk_event_type CHECK (event_type IN ('TECHNICAL', 'SPORTS', 'CULTURAL')),
    CONSTRAINT chk_event_attendance CHECK (expected_attendance >= 0),
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues(venue_id) ON DELETE RESTRICT,
    INDEX idx_event_date (event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS incidents (
    incident_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    incident_type VARCHAR(24) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    description VARCHAR(2000) NOT NULL,
    incident_date DATE NOT NULL,
    CONSTRAINT chk_incident_type CHECK (incident_type IN ('POWER_FAILURE', 'INTERNET_FAILURE', 'EQUIPMENT_FAILURE', 'OVERCROWDING', 'OTHER')),
    CONSTRAINT chk_incident_description CHECK (CHAR_LENGTH(TRIM(description)) > 0),
    CONSTRAINT fk_incident_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    INDEX idx_incident_event_date (event_id, incident_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

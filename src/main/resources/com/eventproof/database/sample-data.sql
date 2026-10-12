-- Only executed by DemoDatabaseSetup when ALL three tables are empty.
-- IDs are discovered from generated keys rather than assumed.
INSERT INTO venues (venue_name, capacity, backup_power, backup_internet)
VALUES ('Main Auditorium', 1000, TRUE, TRUE);
SET @demo_auditorium = LAST_INSERT_ID();

INSERT INTO venues (venue_name, capacity, backup_power, backup_internet)
VALUES ('College Grounds', 1500, FALSE, FALSE);
SET @demo_grounds = LAST_INSERT_ID();

INSERT INTO venues (venue_name, capacity, backup_power, backup_internet)
VALUES ('Open-Air Stage', 800, FALSE, FALSE);
SET @demo_stage = LAST_INSERT_ID();

INSERT INTO events (event_name, event_type, event_date, expected_attendance, venue_id)
VALUES ('Tech Fest', 'TECHNICAL', CURRENT_DATE + INTERVAL 5 DAY, 900, @demo_auditorium);
SET @demo_tech = LAST_INSERT_ID();

INSERT INTO events (event_name, event_type, event_date, expected_attendance, venue_id)
VALUES ('Sports Meet', 'SPORTS', CURRENT_DATE + INTERVAL 8 DAY, 1450, @demo_grounds);
SET @demo_sports = LAST_INSERT_ID();

INSERT INTO events (event_name, event_type, event_date, expected_attendance, venue_id)
VALUES ('Cultural Fest', 'CULTURAL', CURRENT_DATE + INTERVAL 11 DAY, 900, @demo_stage);
SET @demo_cultural = LAST_INSERT_ID();

INSERT INTO incidents (event_id, incident_type, description, incident_date) VALUES
(@demo_tech, 'EQUIPMENT_FAILURE', 'Microphone fault during rehearsal. Replacement equipment arranged.', CURRENT_DATE),
(@demo_sports, 'EQUIPMENT_FAILURE', 'Scoreboard display failed during a practice session.', CURRENT_DATE),
(@demo_sports, 'OTHER', 'First-aid supplies were incomplete at the readiness inspection.', CURRENT_DATE),
(@demo_cultural, 'POWER_FAILURE', 'Stage lighting lost power during rehearsal. Backup supply is unavailable.', CURRENT_DATE);

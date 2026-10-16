-- Remove obsolete activation data. Credentials and other preferences remain intact.
ALTER TABLE preferencias DROP COLUMN licensa;
DROP TABLE IF EXISTS licensas;

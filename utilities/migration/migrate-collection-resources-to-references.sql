/*
 * ml-plugin: migrate-collection-resources-to-references.sql
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

CREATE TABLE sets_collection_reference (
    sets_collection_reference_id                   SERIAL PRIMARY KEY NOT NULL,
    sets_collection_id                             VARCHAR(255)       NOT NULL,
    xnat_abstractresource_xnat_abstractresource_id INTEGER            NOT NULL
);

ALTER TABLE ONLY sets_collection_reference ADD CONSTRAINT sets_collection_reference_sets_collection_id_fkey FOREIGN KEY (sets_collection_id) REFERENCES sets_collection (id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY sets_collection_reference ADD CONSTRAINT sets_collection_reference_xnat_abstractresource_xnat_abstr_fkey FOREIGN KEY (xnat_abstractresource_xnat_abstractresource_id) REFERENCES xnat_abstractresource (xnat_abstractresource_id) ON UPDATE CASCADE ON DELETE CASCADE;

CREATE TABLE sets_collection_reference_history (
    history_id                                     SERIAL PRIMARY KEY NOT NULL,
    sets_collection_id                             VARCHAR,
    xnat_abstractresource_xnat_abstractresource_id INTEGER,
    sets_collection_reference_id                   INTEGER
);

INSERT INTO
    sets_collection_reference (sets_collection_reference_id, sets_collection_id, xnat_abstractresource_xnat_abstractresource_id)
SELECT
    sets_collection_resource_id,
    sets_collection_id,
    xnat_abstractresource_xnat_abstractresource_id
FROM
    sets_collection_resource;

SELECT pg_catalog.setval('sets_collection_reference_sets_collection_reference_id_seq', (SELECT max(sets_collection_reference_id) FROM sets_collection_reference), true);

INSERT INTO
    sets_collection_reference_history (history_id, sets_collection_reference_id, sets_collection_id, xnat_abstractresource_xnat_abstractresource_id)
SELECT
    history_id,
    sets_collection_resource_id,
    sets_collection_id,
    xnat_abstractresource_xnat_abstractresource_id
FROM
    sets_collection_resource_history;

SELECT pg_catalog.setval('sets_collection_reference_history_history_id_seq', (SELECT max(history_id) FROM sets_collection_reference_history), false);

DROP TABLE sets_collection_resource_history;
DROP TABLE sets_collection_resource;

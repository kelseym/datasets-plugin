DROP FUNCTION IF EXISTS public.populate_all_projects_resource_json();
DROP FUNCTION IF EXISTS public.populate_project_resource_json(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.scan_resources_with_criteria(projectId VARCHAR(255), criteria TEXT);
DROP FUNCTION IF EXISTS public.scan_resources(projectId VARCHAR(255));

CREATE TABLE IF NOT EXISTS project_resource_json (
    id               SERIAL PRIMARY KEY,
    project_id       VARCHAR(255) NOT NULL,
    subject_id       VARCHAR(255),
    experiment_id    VARCHAR(255),
    scan_id          VARCHAR(255),
    data_type        VARCHAR(255),
    resource_label   VARCHAR(255),
    resource_content VARCHAR(255),
    resource_format  VARCHAR(255),
    resource_json    JSONB);

CREATE OR REPLACE FUNCTION public.scan_resources(projectId VARCHAR(255))
    RETURNS TABLE
    (
        subject_id               VARCHAR(255),
        experiment_id            VARCHAR(255),
        scan_id                  VARCHAR(255),
        resource_id              INTEGER,
        data_type                VARCHAR(255),
        resource_label           VARCHAR(255),
        resource_content         VARCHAR(255),
        resource_format          VARCHAR(255),
        subject_label            VARCHAR(255),
        experiment_label         VARCHAR(255),
        scan_type                VARCHAR(255),
        series_description       VARCHAR(255),
        series_class             VARCHAR(255),
        experiment_last_modified TIMESTAMP,
        resource_last_modified   TIMESTAMP,
        resource_description     VARCHAR(255),
        resource_file_count      INTEGER,
        resource_size            BIGINT)
AS
$_$
BEGIN
    RETURN QUERY
        SELECT
            s.id,
            i.id,
            d.id,
            ar.xnat_abstractresource_id,
            e.element_name,
            ar.label,
            r.content,
            r.format,
            s.label,
            x.label,
            d.type,
            d.series_description,
            d.series_class,
            xmd.last_modified,
            armd.last_modified,
            r.description,
            ar.file_count,
            ar.file_size
        FROM
            xnat_imagesessiondata i
            LEFT JOIN xnat_experimentdata x ON i.id = x.id
            LEFT JOIN xnat_experimentdata_meta_data xmd ON x.experimentdata_info = xmd.meta_data_id
            LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id
            LEFT JOIN xnat_subjectassessordata a ON x.id = a.id
            LEFT JOIN xnat_subjectdata s ON a.subject_id = s.id
            LEFT JOIN xnat_imagescandata d ON i.id = d.image_session_id
            LEFT JOIN xnat_abstractresource ar ON d.xnat_imagescandata_id = ar.xnat_imagescandata_xnat_imagescandata_id
            LEFT JOIN xnat_abstractresource_meta_data armd ON ar.abstractresource_info = armd.meta_data_id
            LEFT JOIN xnat_resource r ON ar.abstractresource_info = r.xnat_abstractresource_id
        WHERE
            x.project = projectId
        ORDER BY
            x.project,
            s.label,
            x.label,
            d.id;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.scan_resources_with_criteria(projectId VARCHAR(255), criteria TEXT)
    RETURNS TABLE
    (
        subject_id               VARCHAR(255),
        experiment_id            VARCHAR(255),
        scan_id                  VARCHAR(255),
        resource_id              INTEGER,
        data_type                VARCHAR(255),
        resource_label           VARCHAR(255),
        resource_content         VARCHAR(255),
        resource_format          VARCHAR(255),
        subject_label            VARCHAR(255),
        experiment_label         VARCHAR(255),
        scan_type                VARCHAR(255),
        series_description       VARCHAR(255),
        series_class             VARCHAR(255),
        experiment_last_modified TIMESTAMP,
        resource_last_modified   TIMESTAMP,
        resource_description     VARCHAR(255),
        resource_file_count      INTEGER,
        resource_size            BIGINT)
AS
$_$
BEGIN
    RETURN QUERY
        EXECUTE format('SELECT ' ||
                       '    subject.id, ' ||
                       '    expt.id, ' ||
                       '    scan.id, ' ||
                       '    abstract.xnat_abstractresource_id, ' ||
                       '    xme.element_name, ' ||
                       '    abstract.label, ' ||
                       '    resource.content, ' ||
                       '    resource.format, ' ||
                       '    subject.label, ' ||
                       '    expt.label, ' ||
                       '    scan.type, ' ||
                       '    scan.series_description, ' ||
                       '    scan.series_class, ' ||
                       '    expt_md.last_modified, ' ||
                       '    abstract_md.last_modified, ' ||
                       '    resource.description, ' ||
                       '    abstract.file_count, ' ||
                       '    abstract.file_size ' ||
                       'FROM ' ||
                       '    xnat_imagesessiondata image ' ||
                       '    LEFT JOIN xnat_experimentdata expt ON image.id = expt.id ' ||
                       '    LEFT JOIN xnat_experimentdata_meta_data expt_md ON expt.experimentdata_info = expt_md.meta_data_id ' ||
                       '    LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id ' ||
                       '    LEFT JOIN xnat_subjectassessordata a ON expt.id = a.id ' ||
                       '    LEFT JOIN xnat_subjectdata subject ON a.subject_id = subject.id ' ||
                       '    LEFT JOIN xnat_imagescandata scan ON image.id = scan.image_session_id ' ||
                       '    LEFT JOIN xnat_abstractresource abstract ON scan.xnat_imagescandata_id = abstract.xnat_imagescandata_xnat_imagescandata_id ' ||
                       '    LEFT JOIN xnat_abstractresource_meta_data abstract_md ON abstract.abstractresource_info = abstract_md.meta_data_id ' ||
                       '    LEFT JOIN xnat_resource resource ON abstract.abstractresource_info = resource.xnat_abstractresource_id ' ||
                       'WHERE ' ||
                       '    expt.project = ''%s'' AND (%s)', projectId, criteria);
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.populate_project_resource_json(projectId VARCHAR(255))
    RETURNS INTEGER
AS
$_$
DECLARE
    totalCount INTEGER := 0;
BEGIN
    DELETE FROM project_resource_json WHERE project_id = projectId;
    RAISE NOTICE 'Populating project resource JSON for project %', projectId;

    DECLARE
        row RECORD;
    BEGIN
        FOR row IN SELECT * FROM scan_resources(projectId)
            LOOP
                totalCount := totalCount + 1;
                INSERT INTO project_resource_json (project_id, subject_id, experiment_id, scan_id, data_type, resource_label, resource_content, resource_format, resource_json)
                VALUES
                (projectId,
                 row.subject_id,
                 row.experiment_id,
                 row.scan_id,
                 row.data_type,
                 row.resource_label,
                 row.resource_content,
                 row.resource_format,
                 row_to_json(row)::JSONB);
            END LOOP;
    END;
    RAISE NOTICE 'Added % rows to project resource JSON for project %', totalCount, projectId;
    RETURN totalCount;
END;
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.populate_all_projects_resource_json()
    RETURNS BIGINT
AS
$_$
DECLARE
    totalCount BIGINT := 0;
BEGIN
    DECLARE
        projectId    VARCHAR(255);
        projectCount INTEGER := 0;
    BEGIN
        FOR projectId IN SELECT id FROM xnat_projectdata
            LOOP
                SELECT * FROM populate_project_resource_json(projectId) INTO projectCount;
                totalCount := totalCount + projectCount;
                RAISE NOTICE 'Added % rows total to project resource JSON', totalCount;
            END LOOP;
    END;
    RETURN totalCount;
END;
$_$
    LANGUAGE plpgsql;

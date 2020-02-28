DROP FUNCTION IF EXISTS public.scan_resources(projectId VARCHAR(255));

CREATE OR REPLACE FUNCTION public.scan_resources(projectId VARCHAR(255))
    RETURNS TABLE (
                      subject_label        VARCHAR(255),
                      expt_label           VARCHAR(255),
                      scan_id              VARCHAR(255),
                      scan_type            VARCHAR(255),
                      series_description   VARCHAR(255),
                      series_class         VARCHAR(255),
                      subject_id           VARCHAR(255),
                      image_session_id     VARCHAR(255),
                      data_type            VARCHAR(255),
                      date                 DATE,
                      resource_label       VARCHAR(255),
                      resource_content     VARCHAR(255),
                      resource_format      VARCHAR(255),
                      resource_description VARCHAR(255),
                      resource_file_count  INTEGER,
                      resource_size        VARCHAR(255))
AS
$_$
BEGIN
    RETURN QUERY
        SELECT
            s.label,
            x.label,
            d.id,
            d.type,
            d.series_description,
            d.series_class,
            s.id,
            i.id,
            e.element_name,
            x.date,
            ar.label,
            r.content,
            r.format,
            r.description,
            ar.file_count,
            pg_size_pretty(ar.file_size)::VARCHAR(255)
        FROM
            xnat_imagesessiondata i
            LEFT JOIN xnat_experimentdata x ON i.id = x.id
            LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id
            LEFT JOIN xnat_subjectassessordata a ON x.id = a.id
            LEFT JOIN xnat_subjectdata s ON a.subject_id = s.id
            LEFT JOIN xnat_imagescandata d ON i.id = d.image_session_id
            LEFT JOIN xnat_abstractresource ar ON d.xnat_imagescandata_id = ar.xnat_imagescandata_xnat_imagescandata_id
            LEFT JOIN xnat_resource r ON ar.abstractresource_info = r.xnat_abstractresource_id
        WHERE
                x.project = projectId
        ORDER BY
            x.project,
            s.label,
            x.label,
            NULLIF(regexp_replace(d.id, '\D', '', 'g'), '')::INT;
END
$_$
    LANGUAGE plpgsql;

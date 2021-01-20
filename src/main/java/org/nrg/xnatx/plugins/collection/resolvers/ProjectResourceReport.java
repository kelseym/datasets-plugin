/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.resolvers.ProjectResourceReport
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.resolvers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

@Data
@Accessors(prefix = "_")
@EqualsAndHashCode(callSuper = true)
public class ProjectResourceReport extends ProjectResource {
    public static List<ProjectResourceReport> getProjectResourceReports(final NamedParameterJdbcTemplate template, final String project, final String where) {
        return template.query(String.format(QUERY_ATTRIBUTE_MATCH_REPORT, project, where), PROJECT_RESOURCE_REPORT_ROW_MAPPER);
    }

    @Override
    public String toString() {
        return RegExUtils.replacePattern(super.toString(), PATTERN, String.format(FORMAT, _tag, _subjectLabelMatches, _experimentLabelMatches, _scanTypeMatches, _seriesDescriptionMatches, _seriesClassMatches, _dataTypeMatches, _resourceLabelMatches, _resourceContentMatches, _resourceFormatMatches, _resourceDescriptionMatches, _resourceLastModifiedMatches, _resourceFileCountMatches, _resourceSizeMatches));
    }

    private static final String                           FORMAT                             = ", \"tag\": \"%s\", \"subjectLabelMatches\": %b, \"experimentLabelMatches\": %b, \"scanTypeMatches\": %b, \"seriesDescriptionMatches\": %b, \"seriesClassMatches\": %b, \"dataTypeMatches\": %b, \"resourceLabelMatches\": %b, \"resourceContentMatches\": %b, \"resourceFormatMatches\": %b, \"resourceDescriptionMatches\": %b, \"resourceLastModifiedMatches\": %b, \"resourceFileCountMatches\": %b, \"resourceSizeMatches\": %b }";
    private static final String                           PATTERN                            = "\\s*}\\s*$";
    private static final RowMapper<ProjectResourceReport> PROJECT_RESOURCE_REPORT_ROW_MAPPER = BeanPropertyRowMapper.newInstance(ProjectResourceReport.class);
    private static final String                           QUERY_ATTRIBUTE_MATCH_REPORT       = "WITH " +
                                                                                               "    all_resources AS ( " +
                                                                                               "        SELECT * " +
                                                                                               "        FROM " +
                                                                                               "            scan_resources('%s') " +
                                                                                               "    ) " +
                                                                                               "SELECT " +
                                                                                               "    subject_id, " +
                                                                                               "    experiment_id, " +
                                                                                               "    scan_id, " +
                                                                                               "    resource_id, " +
                                                                                               "    subject_label, " +
                                                                                               "    experiment_label, " +
                                                                                               "    data_type, " +
                                                                                               "    scan_type, " +
                                                                                               "    series_description, " +
                                                                                               "    series_class, " +
                                                                                               "    resource_label, " +
                                                                                               "    resource_content, " +
                                                                                               "    resource_format, " +
                                                                                               "    resource_description, " +
                                                                                               "    experiment_last_modified, " +
                                                                                               "    resource_last_modified, " +
                                                                                               "    coalesce(resource_file_count, 0) AS resource_file_count, " +
                                                                                               "    coalesce(resource_size, 0) AS resource_size_count, " +
                                                                                               "    %s " +
                                                                                               "FROM " +
                                                                                               "    all_resources";

    private String  _tag;
    private Boolean _subjectLabelMatches;
    private Boolean _experimentLabelMatches;
    private Boolean _scanTypeMatches;
    private Boolean _seriesDescriptionMatches;
    private Boolean _seriesClassMatches;
    private Boolean _dataTypeMatches;
    private Boolean _resourceLabelMatches;
    private Boolean _resourceContentMatches;
    private Boolean _resourceFormatMatches;
    private Boolean _resourceDescriptionMatches;
    private Boolean _resourceLastModifiedMatches;
    private Boolean _resourceFileCountMatches;
    private Boolean _resourceSizeMatches;
}

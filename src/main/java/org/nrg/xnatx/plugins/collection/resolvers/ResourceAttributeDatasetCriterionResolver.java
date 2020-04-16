package org.nrg.xnatx.plugins.collection.resolvers;

import static org.nrg.framework.orm.DatabaseHelper.getFunctionParameterSource;

import com.google.common.base.Function;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Resolver("ResourceAttributes")
@Component
@Slf4j
public class ResourceAttributeDatasetCriterionResolver extends AbstractDatasetCriterionResolver {
    @Autowired
    public ResourceAttributeDatasetCriterionResolver(final DatabaseHelper helper) {
        _helper = helper;
        try {
            _helper.executeScript(BasicXnatResourceLocator.getResource("classpath:META-INF/xnat/dataset-functions.sql"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public List<String> getAttributeNames() {
        return Arrays.asList(ATTRIBUTES);
    }

    @Override
    protected List<Map<String, XnatAbstractresource>> resolveImpl(final UserI user, final String project, final String payload) {
        return new ArrayList<>(getResources(user, project, payload).values());
    }

    @Override
    protected Map<String, List<ProjectResourceReport>> reportImpl(final UserI user, final String project, final String payload) {
        final Map<String, List<ProjectResourceReport>> map = new HashMap<>();
        map.put("", _helper.getParameterizedTemplate().query(String.format(QUERY_ATTRIBUTE_MATCH_REPORT, project, payload), PROJECT_RESOURCE_REPORT_ROW_MAPPER));
        return map;
    }

    protected Map<String, Map<String, XnatAbstractresource>> getResources(final UserI user, final String project, final String payload) {
        final List<ProjectResource> resources = getResources(project, payload);
        log.debug("Found {} resources for the project {} with criterion: '{}'", resources.size(), project, payload);
        return getResourceMap(user, resources);
    }

    protected List<ProjectResource> getResources(final String project, final String payload) {
        final String normalized = StringUtils.replaceEach(payload, ATTRIBUTES, COLUMNS);
        return _helper.callFunction("scan_resources_with_criteria", getFunctionParameterSource("projectId", project, "criteria", normalized), ProjectResource.class);
    }

    protected static Map<String, Map<String, XnatAbstractresource>> getResourceMap(final UserI user, final List<ProjectResource> resources) {
        final ProjectResourceToAbstractResource              function   = new ProjectResourceToAbstractResource(user);
        final Map<String, Map<String, XnatAbstractresource>> sessionMap = new HashMap<>();
        for (final ProjectResource resource : resources) {
            if (!sessionMap.containsKey(resource.getExperimentId())) {
                sessionMap.put(resource.getExperimentId(), new HashMap<String, XnatAbstractresource>());
            }
            sessionMap.get(resource.getExperimentId()).put(resource.getResourceLabel(), function.apply(resource));
        }
        return sessionMap;
    }

    private static class ProjectResourceToAbstractResource implements Function<ProjectResource, XnatAbstractresource> {
        ProjectResourceToAbstractResource(final UserI user) {
            _user = user;
        }

        @Override
        public XnatAbstractresource apply(final ProjectResource resource) {
            return resource != null ? XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(resource.getResourceId(), _user, false) : null;
        }

        private final UserI _user;
    }

    private static final String[] ATTRIBUTES = {"subject_id", "experiment_id", "scan_id", "resource_id", "data_type", "resource_label", "resource_content", "resource_format", "subject_label", "experiment_label", "scan_type", "series_description", "series_class", "experiment_last_modified", "resource_last_modified", "resource_description", "resource_file_count", "resource_size"};
    private static final String[] COLUMNS    = {"subject.id", "expt.id", "scan.id", "abstract.xnat_abstractresource_id", "xme.element_name", "abstract.label", "resource.content", "resource.format", "subject.label", "expt.label", "scan.type", "scan.series_description", "scan.series_class", "expt_md.last_modified", "abstract_md.last_modified", "resource.description", "abstract.file_count", "abstract.file_size"};

    private static final String QUERY_ATTRIBUTE_MATCH_REPORT = "WITH " +
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
                                                               "    resource_file_count, " +
                                                               "    resource_size, " +
                                                               "    %s " +
                                                               "FROM " +
                                                               "    all_resources";

    private static final RowMapper<ProjectResourceReport> PROJECT_RESOURCE_REPORT_ROW_MAPPER = BeanPropertyRowMapper.newInstance(ProjectResourceReport.class);

    private final DatabaseHelper _helper;
}

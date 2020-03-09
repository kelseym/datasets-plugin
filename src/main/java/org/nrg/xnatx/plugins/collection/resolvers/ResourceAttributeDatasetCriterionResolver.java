package org.nrg.xnatx.plugins.collection.resolvers;

import static org.nrg.framework.orm.DatabaseHelper.getFunctionParameterSource;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
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
    protected List<? extends XnatAbstractresource> resolveImpl(final UserI user, final String project, final String payload) {
        final String                normalized = StringUtils.replaceEach(payload, ATTRIBUTES, COLUMNS);
        final List<ProjectResource> resources  = _helper.callFunction("scan_resources_with_criteria", getFunctionParameterSource("projectId", project, "criteria", normalized), ProjectResource.class);
        log.debug("Found {} resources for the project {} with criterion: '{}'", resources.size(), project, payload);
        return getXnatResources(user, resources);
    }

    private static List<? extends XnatAbstractresource> getXnatResources(final UserI user, final List<ProjectResource> resources) {
        return Lists.newArrayList(Iterables.filter(Iterables.transform(resources, new ProjectResourceToAbstractResource(user)), Predicates.<XnatAbstractresource>notNull()));
    }

    @Data
    @Accessors(prefix = "_")
    private static class ProjectResource {
        private String _subjectLabel;
        private String _exptLabel;
        private String _scanId;
        private int    _resourceId;
        private String _scanType;
        private String _seriesDescription;
        private String _seriesClass;
        private String _subjectId;
        private String _imageSessionId;
        private String _dataType;
        private Date   _date;
        private String _resourceLabel;
        private String _resourceContent;
        private String _resourceFormat;
        private String _resourceDescription;
        private int    _resourceFileCount;
        private long   _resourceSize;
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

    private final DatabaseHelper     _helper;
}

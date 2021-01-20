/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.resolvers.ResourceAttributeDatasetCriterionResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.resolvers;

import static org.nrg.framework.orm.DatabaseHelper.getFunctionParameterSource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        map.put("", ProjectResourceReport.getProjectResourceReports(_helper.getParameterizedTemplate(), project, payload));
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
        return resources.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(ProjectResource::getExperimentId, Collectors.toMap(ProjectResource::getScanId, resource -> {
            try {
                return XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(resource.getResourceId(), user, false);
            } catch (ClassCastException e) {
                // This is caused by XNAT-6618, so may not be necessary: "ClassCastException: org.nrg.xft.XFTItem cannot be cast to org.nrg.xdat.om.XnatAbstractresource"
                log.warn("A class cast exception occurred trying to get an abstract resource entry with the ID {} and label {}. Please check that this ID corresponds to a valid abstract resource entry. The full project resource record is: {}", resource.getResourceId(), resource.getResourceLabel(), resource);
                throw e;
            }
        })));
    }

    private static final String[] ATTRIBUTES = {"subject_id", "experiment_id", "scan_id", "resource_id", "data_type", "resource_label", "resource_content", "resource_format", "subject_label", "experiment_label", "scan_type", "series_description", "series_class", "experiment_last_modified", "resource_last_modified", "resource_description", "resource_file_count", "resource_size"};
    private static final String[] COLUMNS    = {"subject.id", "expt.id", "scan.id", "abstract.xnat_abstractresource_id", "xme.element_name", "abstract.label", "resource.content", "resource.format", "subject.label", "expt.label", "scan.type", "scan.series_description", "scan.series_class", "expt_md.last_modified", "abstract_md.last_modified", "resource.description", "abstract.file_count", "abstract.file_size"};

    private final DatabaseHelper _helper;
}

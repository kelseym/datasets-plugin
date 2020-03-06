package org.nrg.xnatx.plugins.collection.resolvers;

import static org.nrg.framework.orm.DatabaseHelper.getFunctionParameterSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.orm.DatabaseHelper;
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
    }

    @SuppressWarnings("unused")
    public List<String> getAttributeNames() {
        return Arrays.asList(ATTRIBUTES);
    }

    @Override
    protected List<? extends XnatAbstractresource> resolveImpl(final UserI user, final String project, final String payload) {
        final List<ProjectResource> resources = _helper.callFunction("scan_resources", getFunctionParameterSource("projectId", project, "criteria", StringUtils.replaceEach(payload, ATTRIBUTES, COLUMNS)), ProjectResource.class);
        log.debug("Found {} resources for the project {} with criterion: '{}'", resources.size(), project, payload);
        return getXnatResources(user, resources);
    }

    protected String getExpressions(final List<String> attributes, final ArrayNode node) {
        final Iterable<Pair<RegexType, String>> pairs = Iterables.transform(node, new Function<JsonNode, Pair<RegexType, String>>() {
            @Override
            public Pair<RegexType, String> apply(final JsonNode node) {
                return getRegexType(node.textValue());
            }
        });
        return StringUtils.join(transform(attributes, pairs), " OR ");
    }

    protected String getExpression(final List<String> attributes, final String value) {
        return transform(attributes, Collections.singletonList(getRegexType(value))).get(0);
    }

    private List<String> transform(final List<String> attributes, final Iterable<Pair<RegexType, String>> pairs) {
        return Lists.newArrayList(Iterables.transform(pairs, new Function<Pair<RegexType, String>, String>() {
            @Override
            public String apply(final Pair<RegexType, String> criteria) {
                return StringUtils.join(Lists.transform(attributes, new Function<String, String>() {
                    @Override
                    public String apply(final String attribute) {
                        return StringUtils.joinWith(" ", attribute, criteria.getKey().operator(), "'%s'");
                    }
                }), " OR ");
            }
        }));
    }

    private Pair<RegexType, String> getRegexType(final String payload) {
        final boolean startsWithSlash = StringUtils.startsWith(payload, "/");
        final boolean endsWithSlash   = StringUtils.endsWith(payload, "/");
        final boolean endsWithSlashI  = StringUtils.endsWith(payload, "/i");
        final boolean containsPercent = StringUtils.contains(StringUtils.remove(payload, "%%"), "%");
        if ((!startsWithSlash || !endsWithSlash && !endsWithSlashI) && !containsPercent) {
            return Pair.of(RegexType.Plain, payload);
        }
        if (startsWithSlash) {
            return Pair.of(endsWithSlash ? RegexType.CaseSensitive : RegexType.CaseInsensitive, StringUtils.unwrap(endsWithSlash ? payload : StringUtils.removeEnd(payload, "i"), '/'));
        }
        return Pair.of(RegexType.Like, payload);
    }

    private enum RegexType {
        Plain("="),
        CaseInsensitive("~*"),
        CaseSensitive("~"),
        Like("LIKE");

        String operator() {
            return _operator;
        }

        RegexType(final String operator) {
            _operator = operator;
        }

        private final String _operator;
    }

    private static List<? extends XnatAbstractresource> getXnatResources(final UserI user, final List<ProjectResource> resources) {
        return Lists.newArrayList(Iterables.filter(Iterables.transform(resources, new ProjectResourceToAbstractResource(user)), Predicates.<XnatAbstractresource>notNull()));
    }

    @Data
    @Accessors(prefix = "_")
    private static class ProjectResource {
        private final String _subjectLabel;
        private final String _exptLabel;
        private final String _scanId;
        private final int    _resourceId;
        private final String _scanType;
        private final String _seriesDescription;
        private final String _seriesClass;
        private final String _subjectId;
        private final String _imageSessionId;
        private final String _dataType;
        private final Date   _date;
        private final String _resourceLabel;
        private final String _resourceContent;
        private final String _resourceFormat;
        private final String _resourceDescription;
        private final int    _resourceFileCount;
        private final long   _resourceSize;
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

    private final DatabaseHelper _helper;
}

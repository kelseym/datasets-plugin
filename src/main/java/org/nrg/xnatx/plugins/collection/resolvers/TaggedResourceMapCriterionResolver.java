/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.resolvers.TaggedResourceMapCriterionResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.resolvers;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Accepts criterion payloads in the form of JSON maps in the following form:
 *
 * <pre>{@code {
 *     "Images": {
 *         "tag": "image",
 *         "SeriesDescription": ["T1%"],
 *         "ResourceFormat": ["NIFTI"],
 *         "ResourceContent": ["/T1./i"],
 *         "ResourceLabel": ["/nifti/i"]
 *     },
 *     "Labels": {
 *         "tag": "label",
 *         "SeriesDescription": ["Segment%"],
 *         "ResourceFormat": ["NIFTI"],
 *         "ResourceContent": ["/Segmentat.{3}/i"],
 *         "ResourceLabel": ["/nifti/i"]
 *     }
 * }}</pre>
 *
 * Fields in the array for the <b>SeriesDescription</b> element are compared against three different properties of the scan object:
 *
 * <ul>
 *     <li>{@code type}</li>
 *     <li>{@code series_description}</li>
 *     <li>{@code series_class}</li>
 * </ul>
 *
 * If any of these three properties matches one of the values in the <b>SeriesDescription</b> element, that is considered a match
 * on that property.
 *
 * Fields in the arrays under the <b>Resources</b> element are matched against the following properties of the resources associated
 * with a scan:
 *
 * <ul>
 *     <li><b>Format</b> values are matched against {@code xnat_resource.format}</li>
 *     <li><b>Content</b> values are matched against {@code xnat_resource.content}</li>
 *     <li><b>Label</b> values are matched against {@code xnat_abstractresource.label}</li>
 * </ul>
 *
 * There are four different types of values that can be specified:
 *
 * <ul>
 *     <li>
 *         Any value that starts and ends with the '/' character is evaluated as a regular expression (rules for regular expressions
 *         in PostgreSQL are described in the link at the bottom of this description)
 *     </li>
 *     <li>Any value that starts with the '/' character and ends with "/i" is evaluated as a case-insensitive regular expression</li>
 *     <li>
 *         Any value that contains the '%' character is evaluated as an SQL <b>LIKE</b> expression, <i>unless</i> the '%' is escaped
 *         with another '%', i.e. {@code "foo%bar"} would be evaluated as a <b>LIKE</b> while {@code "foo%%bar"} would be evaluated
 *         as standard text
 *     </li>
 *     <li>Any value that doesn't meet the criteria above is evaluated as standard text</li>
 * </ul>
 *
 * All of the query filters generated from the same element are OR'ed together, while the filters generated from criterion payload are
 * cumulative: that is, they compose a multi-column <b>WHERE</b> clause, with each column filter narrowing the result set.
 *
 * Once resources have been resolved, they are grouped together by image session, with each resource being labeled based on the <b>tag</b> element
 * associated with the search criteria.
 *
 * @see <a href="https://www.postgresql.org/docs/12/functions-matching.html#FUNCTIONS-POSIX-REGEXP" target="_blank">PostgreSQL's docs on using regular expressions within SQL queries</a>
 */
@Resolver("TaggedResourceMap")
@Component
@Slf4j
public class TaggedResourceMapCriterionResolver extends SeriesAndResourceCriterionResolver {
    @Autowired
    public TaggedResourceMapCriterionResolver(final SerializerService serializer, final DatabaseHelper helper) {
        super(serializer, helper);
    }

    @Override
    protected List<Map<String, XnatAbstractresource>> resolveImpl(final UserI user, final String project, final String payload) {
        final Map<String, Map<String, XnatAbstractresource>> resourceMap = new HashMap<>();

        final JsonNode         json     = translate(payload);
        final Iterator<String> iterator = json.fieldNames();
        while (iterator.hasNext()) {
            final String   fieldName = iterator.next();
            final JsonNode node      = json.get(fieldName);
            final String   tagName   = node.has("tag") ? node.get("tag").textValue() : StringUtils.uncapitalize(fieldName);
            for (final Map.Entry<String, Map<String, XnatAbstractresource>> session : getResources(user, project, node).entrySet()) {
                final String sessionId = session.getKey();
                if (!resourceMap.containsKey(sessionId)) {
                    resourceMap.put(sessionId, new HashMap<>());
                }
                final Map<String, XnatAbstractresource> sessionResources = resourceMap.get(sessionId);
                final Map<String, XnatAbstractresource> foundResources   = session.getValue();
                final XnatAbstractresource insert;
                if (foundResources.size() > 1) {
                    final List<XnatAbstractresource> values = new ArrayList<>(foundResources.values());
                    insert = values.remove(0);
                    log.warn("Found {} resources for the session {} and tag {}. I can only return one, so I'm returning resource with ID {} and ignoring resources with IDs {}.", foundResources.size(), sessionId, tagName, insert.getXnatAbstractresourceId(), values.stream().map(XnatAbstractresource::getXnatAbstractresourceId).map(Object::toString).collect(Collectors.joining(", ")));
                } else {
                    insert = foundResources.values().iterator().next();
                }
                sessionResources.put(tagName, insert);
            }
        }

        return new ArrayList<>(resourceMap.values());
    }

    @Override
    protected Map<String, List<ProjectResourceReport>> reportImpl(final UserI user, final String project, final String payload) {
        final Map<String, List<ProjectResourceReport>> reports  = new HashMap<>();
        final JsonNode                                 json     = translate(payload);
        final Iterator<String>                         iterator = json.fieldNames();
        while (iterator.hasNext()) {
            final String                                   fieldName   = iterator.next();
            final JsonNode                                 node        = json.get(fieldName);
            final String                                   tagName     = node.has("tag") ? node.get("tag").textValue() : StringUtils.uncapitalize(fieldName);
            final Map<String, List<ProjectResourceReport>> nodeReports = super.reportImpl(user, project, node.toString());
            reports.put(tagName, nodeReports.get(""));
        }
        return reports;
    }
}

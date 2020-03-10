package org.nrg.xnatx.plugins.collection.resolvers;

import static org.nrg.xnatx.plugins.collection.resolvers.ExpressionResolver.arrayNodeToStrings;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetCriterionResolverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Accepts criterion payloads in the form of JSON maps in the following form:
 *
 * <pre>{@code {
 *     "SeriesDescription": [
 *         "MPRAGE T1 AX",
 *         "/^.*T2[[:space:]]+FLAIR.*$/"
 *     ],
 *     "ResourceFormat": [
 *         "NIFTI",
 *         "BIDS",
 *         "/^JPE?G$/"
 *     ],
 *     "ResourceContent": [
 *         "/infarct ROIS/i",
 *         "4dfp"
 *     ],
 *     "ResourceLabel": [
 *         "SNAPSHOTS",
 *         "/nifti/i"
 *     ]
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
 * @see <a href="https://www.postgresql.org/docs/12/functions-matching.html#FUNCTIONS-POSIX-REGEXP" target="_blank">PostgreSQL's docs on using regular expressions within SQL queries</a>
 */
@Resolver("SeriesDescription")
@Component
@Getter(AccessLevel.PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public class SeriesAndResourceCriterionResolver extends ResourceAttributeDatasetCriterionResolver {
    @Autowired
    public SeriesAndResourceCriterionResolver(final SerializerService serializer, final DatabaseHelper helper) {
        super(helper);
        _serializer = serializer;
        _resolver = new ExpressionResolver();
    }

    @Override
    protected List<Map<String, XnatAbstractresource>> resolveImpl(final UserI user, final String project, final String payload) {
        return new ArrayList<>(getResources(user, project, payload).values());
    }

    protected Map<String, Map<String, XnatAbstractresource>> getResources(final UserI user, final String project, final String payload) {
        return getResources(user, project, translate(payload));
    }

    protected Map<String, Map<String, XnatAbstractresource>> getResources(final UserI user, final String project, final JsonNode json) {
        final List<String> clauses = new ArrayList<>();
        for (final String element : EXPRESSION_ATTRIBUTES.keySet()) {
            if (json.has(element)) {
                final JsonNode node = json.get(element);
                switch (node.getNodeType()) {
                    case ARRAY:
                        clauses.add(getResolver().getExpressions(EXPRESSION_ATTRIBUTES.get(element), arrayNodeToStrings(node)));
                        break;
                    case STRING:
                    case OBJECT:
                        clauses.add(getResolver().getExpression(EXPRESSION_ATTRIBUTES.get(element), node.textValue()));
                        break;
                    default:
                        log.warn("Skipping unknown JSON node type for {}: {}", element, node.getNodeType());
                }
            }
        }
        final String resolved = getResolver().joinClauses(clauses);
        log.debug("Now getting resources from project {} for user {} on the resolved query clauses: {}", project, user.getUsername(), resolved);
        return super.getResources(user, project, resolved);
    }

    @Nonnull
    protected JsonNode translate(final String payload) {
        final JsonNode json;
        try {
            json = getSerializer().deserializeJson(payload);
        } catch (IOException e) {
            throw new DatasetCriterionResolverException("An error occurred trying to convert the payload to a JSON object: " + payload, e);
        }
        return json;
    }

    private static final String                       SERIES_DESCRIPTION            = "SeriesDescription";
    private static final String                       RESOURCE_FORMAT               = "ResourceFormat";
    private static final String                       RESOURCE_CONTENT              = "ResourceContent";
    private static final String                       RESOURCE_LABEL                = "ResourceLabel";
    private static final List<String>                 SERIES_DESCRIPTION_ATTRIBUTES = Arrays.asList("scan_type", "series_description", "series_class");
    private static final List<String>                 RESOURCE_LABEL_ATTRIBUTES     = Collections.singletonList("resource_label");
    private static final List<String>                 RESOURCE_CONTENT_ATTRIBUTES   = Collections.singletonList("resource_content");
    private static final List<String>                 RESOURCE_FORMAT_ATTRIBUTES    = Collections.singletonList("resource_format");
    private static final ListMultimap<String, String> EXPRESSION_ATTRIBUTES         = ImmutableListMultimap.<String, String>builder().putAll(SERIES_DESCRIPTION, SERIES_DESCRIPTION_ATTRIBUTES)
                                                                                                                                     .putAll(RESOURCE_FORMAT, RESOURCE_FORMAT_ATTRIBUTES)
                                                                                                                                     .putAll(RESOURCE_CONTENT, RESOURCE_CONTENT_ATTRIBUTES)
                                                                                                                                     .putAll(RESOURCE_LABEL, RESOURCE_LABEL_ATTRIBUTES)
                                                                                                                                     .build();

    private final SerializerService  _serializer;
    private final ExpressionResolver _resolver;
}

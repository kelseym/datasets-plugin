package org.nrg.xnatx.plugins.collection.services.impl.xft;

import static org.nrg.xnatx.plugins.collection.resolvers.SeriesAndResourceCriterionResolver.RESOURCE_CONTENT;
import static org.nrg.xnatx.plugins.collection.resolvers.SeriesAndResourceCriterionResolver.RESOURCE_FORMAT;
import static org.nrg.xnatx.plugins.collection.resolvers.SeriesAndResourceCriterionResolver.RESOURCE_LABEL;
import static org.nrg.xnatx.plugins.collection.resolvers.SeriesAndResourceCriterionResolver.SERIES_DESCRIPTION;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.nrg.framework.services.SerializerService;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.model.SetsCriterionI;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetCriterionResolverException;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetDefinitionHandlingException;
import org.nrg.xnatx.plugins.collection.resolvers.DatasetCriterionResolver;
import org.nrg.xnatx.plugins.collection.resolvers.ProjectResourceReport;
import org.nrg.xnatx.plugins.collection.resolvers.ResolutionReport;
import org.nrg.xnatx.plugins.collection.resolvers.SessionReport;
import org.nrg.xnatx.plugins.collection.services.DatasetCollectionService;
import org.nrg.xnatx.plugins.collection.services.DatasetDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class XftDatasetDefinitionService extends AbstractXftDatasetObjectService<SetsDefinition> implements DatasetDefinitionService {
    @Autowired
    public XftDatasetDefinitionService(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final SerializerService serializer, final DatasetCollectionService collections, final List<DatasetCriterionResolver> resolvers) {
        super(service, template);
        _serializer = serializer;
        _collections = collections;
        final ImmutableMap.Builder<String, DatasetCriterionResolver> builder = ImmutableMap.builder();
        for (final DatasetCriterionResolver resolver : resolvers) {
            builder.put(resolver.getResolverId(), resolver);
        }
        _resolvers = builder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException, InsufficientPrivilegesException {
        return resolve(user, findByProjectAndIdOrLabel(user, projectId, idOrLabel));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final String projectId, final String idOrLabel, final SetsCollection collection) throws NotFoundException, InsufficientPrivilegesException {
        return resolve(user, findByProjectAndIdOrLabel(user, projectId, idOrLabel), collection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final String id) throws NotFoundException, InsufficientPrivilegesException {
        return resolve(user, findById(user, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final String id, final SetsCollection collection) throws NotFoundException, InsufficientPrivilegesException {
        return resolve(user, findById(user, id), collection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final SetsDefinition definition) throws InsufficientPrivilegesException {
        final SetsCollection collection = resolveDefinition(user, definition);
        collection.setLabel(generateCollectionLabel(definition.getLabel()));
        return commit(user, collection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final SetsDefinition definition, final SetsCollection collection) throws InsufficientPrivilegesException {
        final SetsCollection resolved = resolveDefinition(user, definition);
        collection.setDefinitionId(resolved.getDefinitionId());
        collection.setProject(resolved.getProject());
        collection.setFilecount(resolved.getFilecount());
        collection.setFilesize(resolved.getFilesize());
        collection.setFiles(resolved.getFiles());
        collection.addResources(resolved.getResources_resource());
        if (StringUtils.isBlank(collection.getLabel())) {
            collection.setLabel(StringUtils.defaultIfBlank(resolved.getLabel(), generateCollectionLabel(definition.getLabel())));
        }
        return commit(user, collection);
    }

    @Override
    public SetsCollection evaluate(final UserI user, final String projectId, final JsonNode payload) throws InsufficientPrivilegesException {
        return evaluate(user, projectId, null, payload);
    }

    @Override
    public SetsCollection evaluate(final UserI user, final String projectId, final String resolver, final JsonNode payload) throws InsufficientPrivilegesException {
        final SetsDefinition definition = getDefinitionFromJson(user, projectId, resolver, payload);
        if (definition == null) {
            return null;
        }

        return resolveDefinition(user, definition);
    }

    @Override
    public ResolutionReport report(final UserI user, final String projectId, final String resolver, final JsonNode payload) throws InsufficientPrivilegesException {
        final SetsDefinition definition = getDefinitionFromJson(user, projectId, resolver, payload);
        if (definition == null) {
            return null;
        }
        return reportDefinition(user, definition);
    }

    private SetsDefinition getDefinitionFromJson(final UserI user, final String projectId, final String resolver, final JsonNode payload) throws InsufficientPrivilegesException {
        try {
            if (!getPermissions().canCreate(user, getProjectXmlPath(), (Object) projectId)) {
                throw new InsufficientPrivilegesException(user.getUsername(), projectId);
            }
        } catch (InsufficientPrivilegesException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred trying to check whether user " + user.getUsername() + " can create dataset definitions in project " + projectId, e);
        }

        final SetsDefinition definition = new SetsDefinition();
        definition.setId(payload.has("id") ? payload.get("id").asText() : "evaluate-" + user.getUsername() + "-" + Calendar.getInstance().getTimeInMillis());
        definition.setProject(projectId);
        definition.setLabel(payload.has("label") ? payload.get("label").asText() : definition.getId());
        if (payload.has("description")) {
            final String description = payload.get("description").asText();
            if (StringUtils.isNotBlank(description)) {
                definition.setDescription(description);
            }
        }

        final String defaultResolver = StringUtils.defaultIfBlank(resolver, "TaggedResourceMap");
        if (payload.has("criteria")) {
            final ArrayNode criteria = (ArrayNode) payload.get("criteria");
            for (final JsonNode criterionNode : criteria) {
                final SetsCriterion criterion = new SetsCriterion();
                criterion.setResolver(criterionNode.has("resolver") ? criterionNode.get("resolver").asText() : defaultResolver);
                final JsonNode payloadNode = criterionNode.get("payload");
                criterion.setPayload(payloadNode.isObject() ? payloadNode.toString() : payloadNode.asText());
                try {
                    definition.addCriteria(criterion);
                } catch (Exception e) {
                    log.error("An error occurred trying to add a criterion object to a temporary definition {}. The results from this operation may not be what you expect.", definition.getId(), e);
                }
            }
        } else {
            final SetsCriterion criterion = new SetsCriterion();
            criterion.setResolver(defaultResolver);
            try {
                criterion.setPayload(_serializer.toJson(payload));
            } catch (IOException e) {
                log.warn("An error occurred trying to convert a JSON payload to string", e);
                return null;
            }
            try {
                definition.addCriteria(criterion);
            } catch (Exception e) {
                log.error("An error occurred trying to add a criterion object to a temporary definition {}. The results from this operation may not be what you expect.", definition.getId(), e);
            }
        }
        return definition;
    }

    private SetsCollection resolveDefinition(final UserI user, final SetsDefinition definition) {
        final String                                  project   = definition.getProject();
        final List<Map<String, XnatAbstractresource>> resources = new ArrayList<>();
        for (final SetsCriterionI criterion : definition.getCriteria()) {
            final DatasetCriterionResolver resolver = validate(criterion);
            if (resolver != null) {
                final List<Map<String, XnatAbstractresource>> resolve = resolver.resolve(user, project, (SetsCriterion) criterion);
                resources.addAll(resolve);
            }
        }

        final AtomicInteger              totalCount    = new AtomicInteger();
        final AtomicLong                 totalSize     = new AtomicLong();
        final List<Map<String, String>>  resourceFiles = new ArrayList<>();
        final List<XnatAbstractresource> baseResources = new ArrayList<>();
        for (final Map<String, XnatAbstractresource> resourceMap : resources) {
            final Map<String, String> sessionResource = new HashMap<>();
            for (final String label : resourceMap.keySet()) {
                final XnatAbstractresource resource = resourceMap.get(label);
                baseResources.add(resource);
                final List<ResourceFile> fileResources = resource.getFileResources("", true);
                final String path = StringUtils.join(Lists.transform(fileResources, new Function<ResourceFile, String>() {
                    @Override
                    public String apply(final ResourceFile resourceFile) {
                        totalCount.incrementAndGet();
                        totalSize.addAndGet(resourceFile.getSize());
                        return resourceFile.getAbsolutePath();
                    }
                }), ", ");
                sessionResource.put(label, fileResources.size() == 1 ? path : "[" + path + "]");
            }
            resourceFiles.add(sessionResource);
        }

        final String json;
        try {
            json = _serializer.toJson(resourceFiles);
        } catch (IOException e) {
            throw new DatasetCriterionResolverException("An error occurred trying to create JSON for the set definition: " + definition.getId());
        }

        final SetsCollection collection = new SetsCollection();
        collection.setDefinitionId(definition.getId());
        collection.setProject(definition.getProject());
        collection.setFilecount(totalCount.get());
        collection.setFilesize(totalSize.get());
        collection.setFiles(json);
        collection.addResources(baseResources);
        return collection;
    }

    private ResolutionReport reportDefinition(final UserI user, final SetsDefinition definition) {
        final ResolutionReport.ResolutionReportBuilder                                                       builder = ResolutionReport.builder().username(user.getUsername()).project(definition.getProject());
        final Map<Pair<String, String>, Pair<Set<String>, Map<Triple<String, String, String>, Set<String>>>> reports = new HashMap<>();

        final String project = definition.getProject();
        for (final SetsCriterionI criterion : definition.getCriteria()) {
            final DatasetCriterionResolver resolver = validate(criterion);
            if (resolver != null) {
                try {
                    builder.criterion((SetsCriterion) criterion);
                    final Map<Pair<String, String>, String> matchers = getTaggedPropertyMatchers(criterion.getPayload());
                    for (final Map.Entry<String, List<ProjectResourceReport>> entry : resolver.report(user, project, (SetsCriterion) criterion).entrySet()) {
                        final String                      file  = entry.getKey();
                        final List<ProjectResourceReport> value = entry.getValue();
                        builder.resource(file, value);
                        for (final ProjectResourceReport report : value) {
                            final Pair<String, String> key    = Pair.of(report.getExperimentId(), report.getExperimentLabel());
                            final String               scanId = report.getScanId();

                            final Map<Triple<String, String, String>, Set<String>> results;
                            if (!reports.containsKey(key)) {
                                results = new HashMap<>();
                                for (final Pair<String, String> matcherKey : matchers.keySet()) {
                                    results.put(Triple.of(matcherKey.getKey(), matcherKey.getValue(), matchers.get(matcherKey)), new HashSet<String>());
                                }
                                final Set<String> scanIds = new HashSet<>();
                                scanIds.add(scanId);
                                reports.put(key, Pair.of(scanIds, results));
                            } else {
                                final Pair<Set<String>, Map<Triple<String, String, String>, Set<String>>> data = reports.get(key);
                                data.getKey().add(scanId);
                                results = data.getValue();
                            }

                            if (matches(report.getScanTypeMatches(), report.getSeriesClassMatches(), report.getSeriesClassMatches())) {
                                addScanToResults(results, scanId, SERIES_DESCRIPTION, file, matchers);
                            }
                            if (matches(report.getResourceLabelMatches())) {
                                addScanToResults(results, scanId, RESOURCE_LABEL, file, matchers);
                            }
                            if (matches(report.getResourceFormatMatches())) {
                                addScanToResults(results, scanId, RESOURCE_FORMAT, file, matchers);
                            }
                            if (matches(report.getResourceContentMatches())) {
                                addScanToResults(results, scanId, RESOURCE_CONTENT, file, matchers);
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("An error occurred trying to deserialize the payload for criterion {}", criterion.getSetsCriterionId(), e);
                }
            }
        }
        for (final Pair<String, String> experiment : reports.keySet()) {
            final SessionReport.SessionReportBuilder                                  session = SessionReport.builder().id(experiment.getKey()).label(experiment.getValue());
            final Pair<Set<String>, Map<Triple<String, String, String>, Set<String>>> data    = reports.get(experiment);
            session.scans(data.getKey());
            final Map<Triple<String, String, String>, Set<String>> results = data.getValue();
            for (final Triple<String, String, String> result : results.keySet()) {
                session.result(SessionReport.CriterionResult.builder().file(result.getLeft()).check(result.getMiddle()).matcher(result.getRight()).scans(results.get(result)).build());
            }
            builder.session(session.build());
        }
        return builder.build();
    }

    private void addScanToResults(final Map<Triple<String, String, String>, Set<String>> results, final String scanId, final String attribute, final String file, final Map<Pair<String, String>, String> matchers) {
        final Triple<String, String, String> triple = Triple.of(file, attribute, matchers.get(Pair.of(file, attribute)));
        final Set<String>                    scans;
        if (results.containsKey(triple)) {
            scans = results.get(triple);
        } else {
            scans = new HashSet<>();
            results.put(triple, scans);
        }
        scans.add(scanId);
    }

    private boolean matches(final Boolean... matches) {
        return Iterables.any(Arrays.asList(matches), new Predicate<Boolean>() {
            @Override
            public boolean apply(@Nullable final Boolean match) {
                return match != null && match;
            }
        });
    }

    private Map<Pair<String, String>, String> getTaggedPropertyMatchers(final String payload) throws IOException {
        final Map<Pair<String, String>, String> matchers = new HashMap<>();
        final JsonNode                          json     = _serializer.deserializeJson(payload);
        final Iterator<String>                  iterator = json.fieldNames();
        while (iterator.hasNext()) {
            final String           fieldName = iterator.next();
            final JsonNode         node      = json.get(fieldName);
            final String           file      = node.has("tag") ? node.get("tag").textValue() : StringUtils.uncapitalize(fieldName);
            final Iterator<String> names     = node.fieldNames();
            while (names.hasNext()) {
                final String name = names.next();
                if (!StringUtils.equals(name, "tag")) {
                    final JsonNode value = node.get(name);
                    final String   converted;
                    switch (value.getNodeType()) {
                        case STRING:
                            converted = value.textValue();
                            break;
                        case ARRAY:
                            switch (value.size()) {
                                case 0:
                                    converted = "[]";
                                    break;
                                case 1:
                                    converted = value.get(0).textValue();
                                    break;
                                default:
                                    converted = value.toString();
                            }
                            break;
                        default:
                            converted = value.toString();
                    }
                    matchers.put(Pair.of(file, name), converted);
                }
            }
        }
        return matchers;
    }

    private DatasetCriterionResolver validate(final SetsCriterionI criterion) {
        if (!_resolvers.containsKey(criterion.getResolver())) {
            log.warn("Can't find a valid resolver for the criterion {}, wants ID {}", criterion.getSetsCriterionId(), criterion.getResolver());
            return null;
        }
        final DatasetCriterionResolver resolver = _resolvers.get(criterion.getResolver());
        if (!resolver.handles(criterion)) {
            throw new DatasetCriterionResolverException("The criterion " + criterion.getSetsCriterionId() + " says it wants the resolver with ID " + criterion.getResolver() + ", but that resolver says it can't handle that criterion");
        }
        return resolver;
    }

    private SetsCollection commit(final UserI user, final SetsCollection collection) throws InsufficientPrivilegesException {
        try {
            return _collections.create(user, collection);
        } catch (ResourceAlreadyExistsException e) {
            throw new DatasetDefinitionHandlingException("Got an exception indicating that a resource already exists, but that shouldn't be the case", e);
        } catch (DataFormatException e) {
            throw new DatasetDefinitionHandlingException("Got an exception indicating a data format error, but these are objects so that shouldn't be the case", e);
        } catch (NotFoundException e) {
            throw new DatasetDefinitionHandlingException("Got an exception indicating an object couldn't be found, but I already have the objects so that shouldn't be the case", e);
        }
    }

    private static String generateCollectionLabel(final String parentLabel) {
        return parentLabel + "-" + Calendar.getInstance().getTimeInMillis();
    }

    private final DatasetCollectionService              _collections;
    private final SerializerService                     _serializer;
    private final Map<String, DatasetCriterionResolver> _resolvers;
}

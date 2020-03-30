package org.nrg.xnatx.plugins.collection.services.impl.xft;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        try {
            if (!getPermissions().canCreate(user, getProjectXmlPath(), projectId)) {
                throw new InsufficientPrivilegesException(user.getUsername(), projectId);
            }
        } catch (InsufficientPrivilegesException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred trying to check whether user " + user.getUsername() + " can create dataset definitions in project " + projectId, e);
        }
        final SetsCriterion criterion = new SetsCriterion();
        criterion.setResolver(StringUtils.defaultIfBlank(resolver, "TaggedResourceMap"));
        try {
            criterion.setPayload(_serializer.toJson(payload));
        } catch (IOException e) {
            log.warn("An error occurred trying to convert a JSON payload to string", e);
            return null;
        }
        final SetsDefinition definition = new SetsDefinition();
        definition.setId("evaluate-" + user.getUsername() + "-" + Calendar.getInstance().getTimeInMillis());
        definition.setProject(projectId);
        try {
            definition.addCriteria(criterion);
        } catch (Exception e) {
            log.error("An error occurred trying to add a criterion object to a temporary definition {}. The results from this operation may not be what you expect.", definition.getId(), e);
        }
        return resolveDefinition(user, definition);
    }

    private SetsCollection resolveDefinition(final UserI user, final SetsDefinition definition) {
        final String                                  project   = definition.getProject();
        final List<Map<String, XnatAbstractresource>> resources = new ArrayList<>();
        for (final SetsCriterionI criterion : definition.getCriteria()) {
            if (!_resolvers.containsKey(criterion.getResolver())) {
                log.warn("Can't find a valid resolver for the criterion {}, wants ID {}", criterion.getSetsCriterionId(), criterion.getResolver());
                continue;
            }

            final DatasetCriterionResolver resolver = _resolvers.get(criterion.getResolver());
            if (!resolver.handles(criterion)) {
                throw new DatasetCriterionResolverException("The criterion " + criterion.getSetsCriterionId() + " says it wants the resolver with ID " + criterion.getResolver() + ", but that resolver says it can't handle that criterion");
            }
            final List<Map<String, XnatAbstractresource>> resolve = resolver.resolve(user, project, (SetsCriterion) criterion);
            resources.addAll(resolve);
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

package org.nrg.xnatx.plugins.collection.services.impl.xft;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
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
import org.nrg.xnatx.plugins.collection.exceptions.DatasetCollectionHandlingException;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetCriterionResolverException;
import org.nrg.xnatx.plugins.collection.resolvers.DatasetCriterionResolver;
import org.nrg.xnatx.plugins.collection.services.DatasetCollectionService;
import org.nrg.xnatx.plugins.collection.services.DatasetDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class XftDatasetCollectionService extends AbstractXftDatasetObjectService<SetsCollection> implements DatasetCollectionService {
    @Autowired
    public XftDatasetCollectionService(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final DatasetDefinitionService definitions, final SerializerService serializer, final List<DatasetCriterionResolver> resolvers) {
        super(service, template);
        _definitions = definitions;
        _serializer = serializer;
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
    public SetsCollection resolve(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException {
        return resolve(user, _definitions.findByProjectAndIdOrLabel(user, projectId, idOrLabel));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final String projectId, final String idOrLabel, final SetsCollection collection) throws NotFoundException, InsufficientPrivilegesException, DataFormatException, ResourceAlreadyExistsException {
        return resolve(user, _definitions.findByProjectAndIdOrLabel(user, projectId, idOrLabel), collection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final String id) throws NotFoundException {
        return resolve(user, _definitions.findById(user, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final String id, final SetsCollection collection) throws NotFoundException, InsufficientPrivilegesException, DataFormatException, ResourceAlreadyExistsException {
        return resolve(user, _definitions.findById(user, id), collection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final SetsDefinition definition) {
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
        collection.setFilecount(totalCount.get());
        collection.setFilesize(totalSize.get());
        collection.setFiles(json);
        collection.addResources(baseResources);
        return collection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetsCollection resolve(final UserI user, final SetsDefinition definition, final SetsCollection collection) throws InsufficientPrivilegesException, NotFoundException, ResourceAlreadyExistsException, DataFormatException {
        final SetsCollection resolved = resolve(user, definition);
        collection.setDefinitionId(resolved.getDefinitionId());
        collection.setFilecount(resolved.getFilecount());
        collection.setFilesize(resolved.getFilesize());
        collection.setFiles(resolved.getFiles());
        collection.addResources(resolved.getResources_resource());
        return create(user, collection);
    }

    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException {
        final SetsCollection collection = findByProjectAndIdOrLabel(user, projectId, idOrLabel);
        final String         files      = collection.getFiles();
        final JsonNode       json;
        try {
            json = _serializer.deserializeJson(files);
        } catch (IOException e) {
            throw new DatasetCollectionHandlingException("An error occurred trying to deserialize the files property on the collection '" + idOrLabel + "' in the project " + projectId + ": " + files, e);
        }
        final ImmutableMap.Builder<String, Map<String, String>> resources = ImmutableMap.builder();
        switch (json.getNodeType()) {
            case ARRAY:

        }
        return null;
    }

    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final String id) throws NotFoundException {
        return null;
    }

    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final SetsCollection collection) throws NotFoundException {
        return null;
    }

    private final DatasetDefinitionService              _definitions;
    private final SerializerService                     _serializer;
    private final Map<String, DatasetCriterionResolver> _resolvers;
}

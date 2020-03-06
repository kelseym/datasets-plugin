package org.nrg.xnatx.plugins.collection.services.impl.xft;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.SerializerService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.SetsCriterionI;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetCriterionResolverException;
import org.nrg.xnatx.plugins.collection.resolvers.DatasetCriterionResolver;
import org.nrg.xnatx.plugins.collection.services.DatasetDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class XftDatasetDefinitionService extends AbstractXftDatasetObjectService<SetsDefinition> implements DatasetDefinitionService {
    @Autowired
    public XftDatasetDefinitionService(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final SerializerService serializer, final List<DatasetCriterionResolver> resolvers) {
        super(service, template);
        _serializer = serializer;
        final ImmutableMap.Builder<String, DatasetCriterionResolver> builder = ImmutableMap.builder();
        for (final DatasetCriterionResolver resolver : resolvers) {
            builder.put(resolver.getResolverId(), resolver);
        }
        _resolvers = builder.build();
    }

    @Override
    public SetsCollection resolve(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException {
        return resolve(user, getIdForProjectAndLabel(projectId, idOrLabel));
    }

    @Override
    public SetsCollection resolve(final UserI user, final String id) throws NotFoundException {
        return resolve(user, findById(user, id));
    }

    @Override
    public SetsCollection resolve(final UserI user, final SetsDefinition definition) {
        final String                     project   = definition.getProject();
        final List<XnatAbstractresource> resources = new ArrayList<>();
        for (final SetsCriterionI criterion : definition.getCriteria()) {
            if (!_resolvers.containsKey(criterion.getResolver())) {
                log.warn("Can't find a valid resolver for the criterion {}, wants ID {}", criterion.getSetsCriterionId(), criterion.getResolver());
                continue;
            }
            resources.addAll(_resolvers.get(criterion.getResolver()).resolve(user, project, (SetsCriterion) criterion));
        }
        final List<ResourceFile> resourceFiles = new ArrayList<>();
        for (final XnatAbstractresource resource : resources) {
            resourceFiles.addAll(resource.getFileResources("", true));
        }

        final AtomicLong totalSize = new AtomicLong();
        final String     json;
        try {
            json = _serializer.toJson(Lists.transform(resourceFiles, new Function<ResourceFile, String>() {
                @Nullable
                @Override
                public String apply(final ResourceFile file) {
                    totalSize.addAndGet(file.getSize());
                    return file.getAbsolutePath();
                }
            }));
        } catch (IOException e) {
            throw new DatasetCriterionResolverException("An error occurred trying to create JSON for the set definition: " + definition.getId());
        }

        final SetsCollection collection = new SetsCollection();
        collection.setDefinitionId(definition.getId());
        collection.setFilecount(resourceFiles.size());
        collection.setFilesize(totalSize.get());
        collection.setFiles(json);
        return collection;
    }

    private final Map<String, DatasetCriterionResolver> _resolvers;
    private final SerializerService                     _serializer;
}

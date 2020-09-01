package org.nrg.xnatx.plugins.collection.services.impl.xft;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetCollectionHandlingException;
import org.nrg.xnatx.plugins.collection.services.DatasetCollectionService;
import org.nrg.xnatx.plugins.collection.services.DatasetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Getter(AccessLevel.PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public class XftDatasetCollectionService extends AbstractXftDatasetObjectService<SetsCollection> implements DatasetCollectionService {

    @Autowired
    public XftDatasetCollectionService(final PermissionsServiceI service, final SerializerService serializer, final NamedParameterJdbcTemplate template) {
        super(service, template);
        _serializer = serializer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException {
        return getResources(user, findByProjectAndIdOrLabel(user, projectId, idOrLabel));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final String id) throws NotFoundException {
        return getResources(user, findById(user, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final SetsCollection collection) {
        final Map<String, Map<String, String>> resources = new HashMap<>();
        for (final XnatAbstractresourceI abstractResource : collection.getResources_resource()) {
            final XnatResource resource = (XnatResource) abstractResource;
            try {
                final XFTItem scan      = resource.getParent().getItem();
                final String  sessionId = scan.getParent().getItem().getIDValue();
                if (!resources.containsKey(sessionId)) {
                    resources.put(sessionId, new HashMap<>());
                }
            } catch (XFTInitException | ElementNotFoundException e) {
                log.error("Got an exception, so sad", e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectNode renderDataset(final UserI user, final String id, final Map<String, Integer> partitions, final ObjectNode template) throws NotFoundException {
        return renderDataset(findById(user, id), partitions, template);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectNode renderDataset(final UserI user, final String projectId, final String idOrLabel, final Map<String, Integer> partitions, final ObjectNode template) throws NotFoundException {
        return renderDataset(findByProjectAndIdOrLabel(user, projectId, idOrLabel), partitions, template);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectNode renderDataset(final SetsCollection dataset, final Map<String, Integer> partitions, final @Nonnull ObjectNode template) {
        final Map<String, List<HashMap<String, String>>> partitioned = partitionDataset(dataset, partitions);
        for (final String key : partitioned.keySet()) {
            final JsonNode value = getSerializer().getObjectMapper().valueToTree(partitioned.get(key));
            template.set(key, value);
            template.put("num" + StringUtils.capitalize(key), partitioned.get(key).size());
        }
        return template;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<HashMap<String, String>>> partitionDataset(final SetsCollection dataset, final Map<String, Integer> partitions) {
        final List<HashMap<String, String>> collection = deserialize(dataset.getFiles());

        // The partition values must total 100 (for percentages) or the exact total of the collection.
        final AtomicInteger accumulator = new AtomicInteger();
        for (final int value : partitions.values()) {
            accumulator.addAndGet(value);
        }
        final int total     = accumulator.get();
        final int itemCount = collection.size();
        if (total != 100 && total != itemCount) {
            throw new DatasetCollectionHandlingException("The requested partition values of " + StringUtils.join(partitions.values(), ", ") + " add up to " + accumulator + ", but must total either 100 (as percentage) or the number of items in the specified data collection " + dataset.getId() + " (" + itemCount + ").");
        }

        // If total isn't the same as the number of items, convert the values to percentages of the number of items in the list.
        if (total != itemCount) {
            // partitions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Math.round(itemCount * ((float) entry.getValue() / 100))))
            // final Integer newTotal = mapped.values().stream().reduce(Integer::sum).orElseThrow(DatasetCollectionHandlingException::new);
            accumulator.set(0);
            final Map<String, Integer> mapped = partitions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                final int value = entry.getValue();
                final int adjusted = Math.round(itemCount * ((float) value / 100));
                accumulator.addAndGet(adjusted);
                return adjusted;
            }));
            if (accumulator.get() != itemCount) {
                final Map.Entry<String, Integer> max = DatasetUtils.findMaxValueEntry(mapped);
                mapped.put(max.getKey(), max.getValue() + itemCount - accumulator.get());
            }
            partitions.clear();
            partitions.putAll(mapped);
        }

        Collections.shuffle(collection);

        final AtomicInteger offset = new AtomicInteger();
        return partitions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> collection.subList(offset.get(), offset.addAndGet(entry.getValue()))));
    }

    protected List<HashMap<String, String>> deserialize(final String serialized) {
        try {
            return getSerializer().deserializeJson(serialized, COLLECTION_REFERENCE);
        } catch (IOException e) {
            throw new DatasetCollectionHandlingException("An error occurred deserializing a value to the type List<HashMap<String, String>>. The value was: " + serialized, e);
        }
    }

    private static final TypeReference<ArrayList<HashMap<String, String>>> COLLECTION_REFERENCE = new TypeReference<ArrayList<HashMap<String, String>>>() {};

    private final SerializerService _serializer;
}

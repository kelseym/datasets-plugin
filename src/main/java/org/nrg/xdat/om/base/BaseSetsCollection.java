package org.nrg.xdat.om.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.base.auto.AutoSetsCollection;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetCollectionHandlingException;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetResourceException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Override of generated implementation of this class to provide JSON conversion and resource
 * management methods.
 */
@Slf4j
public abstract class BaseSetsCollection extends AutoSetsCollection {
    public BaseSetsCollection(final ItemI item) {
        super(item);
    }

    public BaseSetsCollection(final UserI user) {
        super(user);
    }

    /**
     * @deprecated Use BaseSetsCollection(UserI user)
     */
    @Deprecated
    public BaseSetsCollection() {
        log.warn("This method is deprecated, you should use BaseSetsCollection(UserI) instead.");
    }

    public BaseSetsCollection(final Hashtable properties, final UserI user) {
        super(properties, user);
    }

    @SuppressWarnings("unused")
    public static boolean validateCollectionId(final String collectionId) {
        return XDAT.getNamedParameterJdbcTemplate().queryForObject(QUERY_VERIFY_COLLECTION_ID, new MapSqlParameterSource("collectionId", collectionId), Boolean.class);
    }

    /**
     * Overrides base <b>preSave()</b> implementations to remove resource validation, since resources
     * for collections are always outside of the collection itself.
     */
    @Override
    public void preSave() {
        checkIsValidID(getId());

        if (getPrimaryProject(false) == null) {
            throw new DatasetCollectionHandlingException("Unable to identify project for:" + getProject());
        }

        try {
            checkUniqueLabel();
        } catch (Exception e) {
            throw new DatasetCollectionHandlingException("The collection label " + getLabel() + " is not unique in the project " + getProject());
        }
    }

    public <A extends XnatAbstractresourceI> void addResources(final List<A> resources) {
        final List<XnatAbstractresourceI> errors = new ArrayList<>();
        for (final XnatAbstractresourceI resource : resources) {
            try {
                addResources_resource(resource);
            } catch (Exception e) {
                log.warn("An error occurred trying to add the resource {} to the collection {}", resource.getXnatAbstractresourceId(), StringUtils.defaultIfBlank(getId(), getDefinitionId()), e);
                errors.add(resource);
            }
        }
        if (!errors.isEmpty()) {
            throw new DatasetResourceException((errors.size() == 1 ? "An error" : errors.size() + "errors") + " occurred trying to add resources to a collection. Check the server logs for more information. The following resources may or may not have been successfully added: " + errors.stream().map(XnatAbstractresourceI::getXnatAbstractresourceId).map(Object::toString).collect(Collectors.joining(", ")), errors);
        }
    }

    private static final String QUERY_VERIFY_COLLECTION_ID = "SELECT EXISTS(SELECT x.id FROM sets_collection s LEFT JOIN xnat_experimentdata x ON s.id = x.id WHERE s.id = :collectionId)";
}

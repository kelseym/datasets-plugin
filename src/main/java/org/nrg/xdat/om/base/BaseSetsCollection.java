/*
 * ml-plugin: org.nrg.xdat.om.base.BaseSetsCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.base.auto.AutoSetsCollection;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.datasets.exceptions.DatasetCollectionHandlingException;
import org.nrg.xnatx.plugins.datasets.exceptions.DatasetResourceException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.*;
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
    @SuppressWarnings("DeprecatedIsStillUsed")
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

    public static Map<String, String> toMap(final SetsCollection collection) {
        if (collection == null) {
            return Collections.emptyMap();
        }
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("id", collection.getId());
        attributes.put("project", collection.getProject());
        attributes.put("label", collection.getLabel());
        attributes.put("definitionId", collection.getDefinitionId());
        final Integer fileCount = collection.getFilecount();
        if (fileCount != null) {
            attributes.put("fileCount", Integer.toString(fileCount));
        }
        final Object fileSize = collection.getFilesize();
        if (fileSize != null) {
            attributes.put("fileSize", Long.toString((Long) fileSize));
        }
        final List<XnatAbstractresourceI> references = collection.getReferences_resource();
        if (references != null) {
            attributes.put("referenceCount", Integer.toString(references.size()));
        }
        final Date insertDate = collection.getInsertDate();
        if (insertDate != null) {
            attributes.put("insertDate", Long.toString(insertDate.getTime()));
        }
        final UserI insertUser = collection.getInsertUser();
        if (insertUser != null) {
            attributes.put("insertUser", insertUser.getUsername());
        }
        final Date lastModifiedDate = collection.getItem().getLastModified();
        if (lastModifiedDate != null) {
            attributes.put("lastModifiedDate", Long.toString(lastModifiedDate.getTime()));
        }
        final UserI lastModifiedUser = collection.getItem().getUser();
        if (lastModifiedUser != null) {
            attributes.put("lastModifiedUser", lastModifiedUser.getUsername());
        }
        return attributes;
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

    public void addReferencesById(final List<Integer> referenceIds) {
        addReferences(referenceIds.stream().map(id -> XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(id, null, false)).collect(Collectors.toList()));
    }

    public <A extends XnatAbstractresourceI> void addReferences(final List<A> references) {
        final List<XnatAbstractresourceI> errors = new ArrayList<>();
        references.forEach(resource -> {
            try {
                addReferences_resource(resource);
            } catch (Exception e) {
                log.warn("An error occurred trying to add the resource {} to the collection {}", resource.getXnatAbstractresourceId(), StringUtils.defaultIfBlank(getId(), getDefinitionId()), e);
                errors.add(resource);
            }
        });
        if (!errors.isEmpty()) {
            throw new DatasetResourceException((errors.size() == 1 ? "An error" : errors.size() + "errors") + " occurred trying to add references to a collection. Check the server logs for more information. The following resources may or may not have been successfully added: " + errors.stream().map(XnatAbstractresourceI::getXnatAbstractresourceId).map(Object::toString).collect(Collectors.joining(", ")), errors);
        }
    }

    public void addResourcesById(final List<Integer> resourceIds) {
        addResources(resourceIds.stream().map(id -> XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(id, null, false)).collect(Collectors.toList()));
    }

    public <A extends XnatAbstractresourceI> void addResources(final List<A> resources) {
        final List<XnatAbstractresourceI> errors = new ArrayList<>();
        resources.forEach(resource -> {
            try {
                addResources_resource(resource);
            } catch (Exception e) {
                log.warn("An error occurred trying to add the resource {} to the collection {}", resource.getXnatAbstractresourceId(), StringUtils.defaultIfBlank(getId(), getDefinitionId()), e);
                errors.add(resource);
            }
        });
        if (!errors.isEmpty()) {
            throw new DatasetResourceException((errors.size() == 1 ? "An error" : errors.size() + "errors") + " occurred trying to add resources to a collection. Check the server logs for more information. The following resources may or may not have been successfully added: " + errors.stream().map(XnatAbstractresourceI::getXnatAbstractresourceId).map(Object::toString).collect(Collectors.joining(", ")), errors);
        }
    }

    private static final String QUERY_VERIFY_COLLECTION_ID = "SELECT EXISTS(SELECT x.id FROM sets_collection s LEFT JOIN xnat_experimentdata x ON s.id = x.id WHERE s.id = :collectionId)";
}

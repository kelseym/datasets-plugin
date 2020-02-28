/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.services.CollectionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.services;

import java.util.List;
import java.util.Map;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.XnatxDatacollectionI;
import org.nrg.xdat.model.XnatxDatacollectiondefinitionI;
import org.nrg.xft.security.UserI;

/**
 * Provides high-level calls for managing data collection definitions and resolved collections.
 */
public interface DataCollectionService {
    /**
     * Create a new data collection definition.
     *
     * @param definition The definition to create.
     *
     * @return The saved version of the submitted definition.
     */
    XnatxDatacollectiondefinitionI create(final XnatxDatacollectiondefinitionI definition);

    /**
     * Create a new data collection.
     *
     * @param collection The collection to create.
     *
     * @return The saved version of the submitted collection.
     */
    XnatxDatacollectionI create(final XnatxDatacollectionI collection);

    /**
     * Finds all data collection definitions on the system. If there are no definitions,
     * this returns an empty list.
     *
     * @return A list of all data collection definitions on the system.
     */
    List<? extends XnatxDatacollectiondefinitionI> findAllDefinitions() throws NotFoundException;

    /**
     * Finds all data collection definitions associated with the specified project. If there are
     * no definitions for that project, this returns an empty list.
     *
     * @param projectId The ID of the project for which definitions should be retrieved.
     *
     * @return A list of all data collection definitions for the specified project.
     *
     * @throws NotFoundException When the specified project doesn't exist.
     */
    List<? extends XnatxDatacollectiondefinitionI> findDefinitionsByProject(final String projectId) throws NotFoundException;

    /**
     * Finds the data collection definitions with the specified ID. If a definition with the specified
     * ID doesn't exist, this throws an exception.
     *
     * @param id The ID of the definition to retrieve.
     *
     * @return A list of all data collection definitions for the specified project.
     *
     * @throws NotFoundException When the specified project doesn't exist.
     */
    XnatxDatacollectiondefinitionI findDefinitionById(final String id) throws NotFoundException;

    /**
     * Finds the data collection definition associated with the specified project and ID or label. If a
     * definition with that ID or label isn't associated with that project, this throws an exception.
     *
     * @param projectId The ID of the project with which the definition is associated.
     * @param idOrLabel The ID or label of the definition to retrieve.
     *
     * @return The specified data collection definition.
     *
     * @throws NotFoundException When a definition with the specified project and ID or label doesn't exist.
     */
    XnatxDatacollectiondefinitionI findDefinitionByProjectAndIdOrLabel(final String projectId, final String idOrLabel) throws NotFoundException;

    /**
     * Resolves the specified definition into the returned data collection.
     *
     * @param definition The definition to be resolved.
     *
     * @return The resolved {@link XnatxDatacollectionI data collection}.
     */
    XnatxDatacollectionI resolve(final XnatxDatacollectiondefinitionI definition);

    /**
     * Resolves the specified definition into the returned data collection.
     *
     * @param definition The definition to be resolved.
     *
     * @return The resolved {@link XnatxDatacollectionI data collection}.
     */
    XnatxDatacollectionI resolve(final String id);

    List<? extends XnatxDatacollectionI> findAllCollections() throws NotFoundException;

    List<? extends XnatxDatacollectionI> findCollectionsByProject(final String projectId) throws NotFoundException;

    XnatxDatacollectionI findCollectionById(final String id) throws NotFoundException;

    XnatxDatacollectionI findCollectionByProjectAndIdOrLabel(final String projectId, final String idOrLabel) throws NotFoundException;

    Map<String, List<Map<String, String>>> getCollectionResources(final UserI user, final String id) throws NotFoundException;

    Map<String, List<Map<String, String>>> getCollectionResources(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException;
}

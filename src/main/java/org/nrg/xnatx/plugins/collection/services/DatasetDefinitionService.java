/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.services.CollectionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.services;

import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xft.security.UserI;

/**
 * Provides high-level calls for managing data collection definitions and resolved collections.
 */
public interface DatasetDefinitionService extends DatasetObjectService<SetsDefinition> {
    /**
     * Resolves the specified definition into the returned data collection. Note that this method
     * returns an initialized instance of the {@link SetsCollection} class, but the instance is not
     * yet saved. You can create a new collection object by calling the {@link #resolve(UserI, String,
     * String, SetsCollection)} version of this method, which combines the properties of the resolved
     * collection with the submitted collection object and saves that.
     *
     * @param user      The user resolving the dataset definition.
     * @param projectId The ID of the project containing the dataset definition to be resolved.
     * @param idOrLabel The ID or label of the dataset definition to be resolved.
     *
     * @return The resolved {@link SetsCollection data collection}.
     */
    SetsCollection resolve(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Resolves the specified definition into the returned data collection.  This method combines the
     * properties of the resolved collection with the submitted collection object and saves that to
     * the database. You can create a new collection object without saving it by calling the {@link
     * #resolve(UserI, String, String)} version of this method.
     *
     * @param user       The user resolving the dataset definition.
     * @param projectId  The ID of the project containing the dataset definition to be resolved.
     * @param idOrLabel  The ID or label of the dataset definition to be resolved.
     * @param collection A collection object to use as the basis for a new collection object.
     *
     * @return The resolved and saved {@link SetsCollection data collection}.
     */
    SetsCollection resolve(final UserI user, final String projectId, final String idOrLabel, final SetsCollection collection) throws NotFoundException, InsufficientPrivilegesException, DataFormatException, ResourceAlreadyExistsException;

    /**
     * Resolves the specified definition into the returned data collection. Note that this method
     * returns an initialized instance of the {@link SetsCollection} class, but the instance is not
     * yet saved. You can create a new collection object by calling the {@link #resolve(UserI, String,
     * SetsCollection)} version of this method, which combines the properties of the resolved collection
     * with the submitted collection object and saves that.
     *
     * @param user The user resolving the dataset definition.
     * @param id   The ID of the definition to be resolved.
     *
     * @return The resolved {@link SetsCollection data collection}.
     */
    SetsCollection resolve(final UserI user, final String id) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Resolves the specified definition into the returned data collection.  This method combines the
     * properties of the resolved collection with the submitted collection object and saves that to
     * the database. You can create a new collection object without saving it by calling the {@link
     * #resolve(UserI, String)} version of this method.
     *
     * @param user       The user resolving the dataset definition.
     * @param id         The ID of the definition to be resolved.
     * @param collection A collection object to use as the basis for a new collection object.
     *
     * @return The resolved {@link SetsCollection data collection}.
     */
    SetsCollection resolve(final UserI user, final String id, final SetsCollection collection) throws NotFoundException, InsufficientPrivilegesException, DataFormatException, ResourceAlreadyExistsException;

    /**
     * Resolves the submitted definition into the returned data collection. Note that this method
     * returns an initialized instance of the {@link SetsCollection} class, but the instance is not
     * yet saved. You can create a new collection object by calling the {@link #resolve(UserI, SetsDefinition,
     * SetsCollection)} version of this method, which combines the properties of the resolved collection
     * with the submitted collection object and saves that.
     *
     * @param user       The user resolving the dataset definition.
     * @param definition The definition to be resolved.
     *
     * @return The resolved {@link SetsCollection data collection}.
     */
    SetsCollection resolve(final UserI user, final SetsDefinition definition) throws InsufficientPrivilegesException;

    /**
     * Resolves the specified definition into the returned data collection.  This method combines the
     * properties of the resolved collection with the submitted collection object and saves that to
     * the database. You can create a new collection object without saving it by calling the {@link
     * #resolve(UserI, SetsDefinition)} version of this method.
     *
     * @param user       The user resolving the dataset definition.
     * @param definition The definition to be resolved.
     * @param collection A collection object to use as the basis for a new collection object.
     *
     * @return The resolved {@link SetsCollection data collection}.
     */
    SetsCollection resolve(final UserI user, final SetsDefinition definition, final SetsCollection collection) throws InsufficientPrivilegesException;
}

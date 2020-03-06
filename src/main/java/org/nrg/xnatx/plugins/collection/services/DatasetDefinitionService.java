/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.services.CollectionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.services;

import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xft.security.UserI;

/**
 * Provides high-level calls for managing data collection definitions and resolved collections.
 */
public interface DatasetDefinitionService extends DatasetObjectService<SetsDefinition> {
    /**
     * Resolves the specified definition into the returned data collection.
     *
     * @param user      The user resolving the dataset definition.
     * @param projectId The ID of the project containing the dataset definition to be resolved.
     * @param idOrLabel The ID or label of the dataset definition to be resolved.
     *
     * @return The resolved {@link SetsCollection data collection}.
     */
    SetsCollection resolve(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Resolves the {@link SetsDefinition dataset definition} with the specified ID
     * into the returned dataset collection.
     *
     * @param user The user resolving the dataset definition.
     * @param id   The ID of the definition to be resolved.
     *
     * @return The resolved {@link SetsCollection data collection}.
     */
    SetsCollection resolve(final UserI user, final String id) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Resolves the specified definition into the returned data collection.
     *
     * @param user       The user resolving the dataset definition.
     * @param definition The definition to be resolved.
     *
     * @return The resolved {@link SetsCollection data collection}.
     */
    SetsCollection resolve(final UserI user, final SetsDefinition definition) throws InsufficientPrivilegesException;
}

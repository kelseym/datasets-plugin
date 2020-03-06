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
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xft.security.UserI;

/**
 * Provides high-level calls for managing data collection definitions and resolved collections.
 */
public interface DatasetObjectService<T extends XnatExperimentdata> {
    /**
     * Create a new dataset object.
     *
     * @param user   The user creating the dataset object.
     * @param object The object to create.
     *
     * @return The saved version of the submitted object.
     */
    T create(final UserI user, final T object) throws InsufficientPrivilegesException, ResourceAlreadyExistsException, DataFormatException, NotFoundException;

    /**
     * Finds all dataset objects of the parameterized type on the system. If there are no saved objects
     * of that type, this returns an empty list.
     *
     * @param user The user retrieving the dataset objects.
     *
     * @return A list of all dataset objects of the parameterized type on the system.
     */
    List<T> findAll(final UserI user);

    /**
     * Finds all dataset objects of the parameterized type associated with the specified project. If there
     * are no saved objects for that project, this returns an empty list.
     *
     * @param user      The user The user retrieving the dataset objects.
     * @param projectId The ID of the project for which objects should be retrieved.
     *
     * @return A list of all dataset objects of the parameterized type for the specified project.
     *
     * @throws NotFoundException When the specified project doesn't exist.
     */
    List<T> findByProject(final UserI user, final String projectId) throws NotFoundException;

    /**
     * Finds the dataset object of the parameterized type with the specified ID. If a definition with the
     * specified ID doesn't exist, this throws an exception.
     *
     * @param user The user retrieving the dataset object.
     * @param id   The ID of the dataset object to retrieve.
     *
     * @return The requested dataset object.
     *
     * @throws NotFoundException When the specified dataset object doesn't exist.
     */
    T findById(final UserI user, final String id) throws NotFoundException;

    /**
     * Finds the dataset object with the parameterized type associated with the specified project and ID or
     * label. If a dataset object with that ID or label isn't associated with that project, or if the project
     * doesn't exist, this throws an exception.
     *
     * @param user      The user retrieving the dataset object.
     * @param projectId The ID of the project with which the dataset object is associated.
     * @param idOrLabel The ID or label of the dataset object to retrieve.
     *
     * @return The specified dataset object.
     *
     * @throws NotFoundException When a dataset object with the specified project and ID or label doesn't exist.
     */
    T findByProjectAndIdOrLabel(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException;

    /**
     * Updates the submitted dataset object.
     *
     * @param user   The user updating the dataset object.
     * @param object The dataset object to be updated.
     *
     * @return The updated dataset object.
     */
    T update(final UserI user, final T object) throws InsufficientPrivilegesException, ResourceAlreadyExistsException, DataFormatException, NotFoundException;

    /**
     * Deletes the dataset object with the parameterized type with the specified ID. If a definition with the
     * specified ID doesn't exist, this throws an exception.
     *
     * @param id The ID of the dataset object to delete.
     *
     * @throws NotFoundException When the specified dataset object doesn't exist.
     */
    void delete(final UserI user, final String id) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Deletes the dataset object with the parameterized type associated with the specified project and ID or
     * label. If a dataset object with that ID or label isn't associated with that project, or if the project
     * doesn't exist, this throws an exception.
     *
     * @param projectId The ID of the project with which the dataset object is associated.
     * @param idOrLabel The ID or label of the dataset object to delete.
     *
     * @throws NotFoundException When a dataset object with the specified project and ID or label doesn't exist.
     */
    void delete(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException, InsufficientPrivilegesException;
}

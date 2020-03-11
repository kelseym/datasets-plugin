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
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xft.security.UserI;

/**
 * Provides high-level calls for managing data collection definitions and resolved collections.
 */
public interface DatasetCollectionService extends DatasetObjectService<SetsCollection> {
    /**
     * Returns a map of resources for the specified dataset collection.
     *
     * @param user      The user requesting the resource map.
     * @param projectId The ID of the project containing the requested dataset collection.
     * @param idOrLabel The ID or label of the requested dataset collection.
     *
     * @return A map of the resources in the resolved dataset collection.
     */
    Map<String, List<Map<String, String>>> getResources(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException;

    /**
     * Returns a map of resources for the specified dataset collection.
     *
     * @param user The user requesting the resource map.
     * @param id   The ID of the dataset collection to return.
     *
     * @return A map of the resources in the resolved dataset collection.
     */
    Map<String, List<Map<String, String>>> getResources(final UserI user, final String id) throws NotFoundException;

    /**
     * Returns a map of resources for the specified dataset collection.
     *
     * @param user       The user requesting the resource map.
     * @param collection The collection containing the requested dataset.
     *
     * @return A map of the resources in the resolved dataset collection.
     */
    Map<String, List<Map<String, String>>> getResources(final UserI user, final SetsCollection collection) throws NotFoundException;
}

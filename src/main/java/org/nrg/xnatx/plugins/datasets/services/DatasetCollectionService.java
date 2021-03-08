/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.services.DatasetCollectionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.datasets.exceptions.DatasetCollectionHandlingException;

/**
 * Provides high-level calls for managing dataset definitions and collections.
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

    /**
     * Partitions the dataset by calling the {@link #partitionDataset(SetsCollection, Map)} method and converting the resulting
     * map into a JSON node object, which is then merged with the <b>template</b> parameter node.
     *
     * @param user       The user requesting the rendered dataset.
     * @param id         The id of the dataset to be rendered.
     * @param partitions The names and sizes of the partitions.
     * @param template   The template node to merge with the partitioned dataset.
     *
     * @return A JSON object node containing the partitioned dataset merged with the submitted <b>template</b> node.
     *
     * @throws NotFoundException When the ID doesn't represent an existing {@link SetsCollection} instance.
     */
    ObjectNode renderDataset(final UserI user, final String id, final Map<String, Integer> partitions, final ObjectNode template) throws NotFoundException;

    /**
     * Partitions the dataset by calling the {@link #partitionDataset(SetsCollection, Map)} method and converting the resulting
     * map into a JSON node object, which is then merged with the <b>template</b> parameter node.
     *
     * @param user       The user requesting the rendered dataset.
     * @param projectId  The ID of the project containing the dataset to be rendered.
     * @param idOrLabel  The ID or label of the dataset to be rendered.
     * @param partitions The names and sizes of the partitions.
     * @param template   The template node to merge with the partitioned dataset.
     *
     * @return A JSON object node containing the partitioned dataset merged with the submitted <b>template</b> node.
     *
     * @throws NotFoundException When the ID doesn't represent an existing {@link SetsCollection} instance.
     */
    ObjectNode renderDataset(final UserI user, final String projectId, final String idOrLabel, final Map<String, Integer> partitions, final ObjectNode template) throws NotFoundException;

    /**
     * Partitions the dataset by calling the {@link #partitionDataset(SetsCollection, Map)} method and converting the resulting
     * map into a JSON node object, which is then merged with the <b>template</b> parameter node.
     *
     * @param dataset    The dataset to be partitioned.
     * @param partitions The names and sizes of the partitions.
     * @param template   The template node to merge with the partitioned dataset.
     *
     * @return A JSON object node containing the partitioned dataset merged with the submitted <b>template</b> node.
     */
    ObjectNode renderDataset(final SetsCollection dataset, final Map<String, Integer> partitions, final ObjectNode template);

    /**
     * Partitions the {@link SetsCollection#getFiles() dataset files} based on the measures specified in the <b>partitions</b>
     * argument and returns the resulting map. If the sum of the values in the <b>partitions</b> parameter is 100, each value
     * is taken to indicate the percentage of the total number of files in the dataset that should be in that partition. If
     * the sum of the values isn't 100 but matches the total number of items in the dataset, each value is taken to indicate
     * the absolute number of files that should be in that partition. If the sum of the values in the map isn't 100 and doesn't
     * match the total number of items in the dataset, a {@link DatasetCollectionHandlingException} is thrown.
     *
     * @param dataset    The dataset to be partitioned.
     * @param partitions A map containing the names and sizes of the partitions.
     *
     * @return A map containing the {@link SetsCollection#getFiles() dataset files} divided into partitions.
     */
    Map<String, List<HashMap<String, String>>> partitionDataset(final SetsCollection dataset, final Map<String, Integer> partitions);
}

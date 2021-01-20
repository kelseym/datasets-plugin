/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.rest.DatasetCollectionApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.rest;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.services.SerializerService;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.rest.AbstractExperimentXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.collection.rest.permissions.CreateCollection;
import org.nrg.xnatx.plugins.collection.rest.permissions.EditCollection;
import org.nrg.xnatx.plugins.collection.rest.permissions.ReadCollection;
import org.nrg.xnatx.plugins.collection.services.DatasetCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api("XNAT 1.7 Dataset Collection Plugin API")
@XapiRestController
@RequestMapping(value = "/sets/collections")
@Slf4j
public class DatasetCollectionApi extends AbstractExperimentXapiRestController<SetsCollection> {
    @Autowired
    public DatasetCollectionApi(final NamedParameterJdbcTemplate template, final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final DatasetCollectionService collections, final SerializerService serializer) throws IllegalAccessException, NoSuchFieldException {
        super(template, userManagementService, roleHolder);
        _collections = collections;
        _serializer = serializer;
    }

    @ApiOperation(value = "Returns a list of IDs, projects, and labels for all dataset collections on the system.", response = Map.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collections successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access dataset collections on the system."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public List<Map<String, String>> getAll() {
        return _collections.findAll(getSessionUser()).stream().map(SetsCollection::toMap).collect(Collectors.toList());
    }

    @ApiOperation(value = "Returns a list of IDs, projects, and labels for all dataset collections for a project.", response = Map.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collections successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access dataset collections in the requested project."),
                   @ApiResponse(code = 404, message = "The requested project doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadCollection.class)
    public List<Map<String, String>> getByProject(@PathVariable("projectId") final String projectId) throws NotFoundException {
        return _collections.findByProject(getSessionUser(), projectId).stream().map(SetsCollection::toMap).collect(Collectors.toList());
    }

    @ApiOperation(value = "Returns the dataset collection with the submitted ID.", response = SetsCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collection successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access the requested dataset collection."),
                   @ApiResponse(code = 404, message = "The requested dataset collection doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadCollection.class)
    public SetsCollection getById(@PathVariable final String id) throws NotFoundException {
        return _collections.findById(getSessionUser(), id);
    }

    @ApiOperation(value = "Returns the dataset collection with the submitted ID.", response = SetsCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collection successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access the requested dataset collection."),
                   @ApiResponse(code = 404, message = "The requested dataset collection doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}/files", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadCollection.class)
    public JsonNode getFilesById(@PathVariable final String id) throws NotFoundException, IOException {
        return _serializer.deserializeJson(getById(id).getFiles());
    }

    @ApiOperation(value = "Returns the dataset collection with the submitted ID or label in the specified project.", response = SetsCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collection successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access the requested dataset collection."),
                   @ApiResponse(code = 404, message = "The requested dataset collection doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadCollection.class)
    public SetsCollection getByProjectAndIdOrLabel(@PathVariable final String projectId, @PathVariable final String idOrLabel) throws NotFoundException {
        return _collections.findByProjectAndIdOrLabel(getSessionUser(), projectId, idOrLabel);
    }

    @ApiOperation(value = "Returns the dataset collection with the submitted ID or label in the specified project.", response = SetsCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collection successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access the requested dataset collection."),
                   @ApiResponse(code = 404, message = "The requested dataset collection doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}/files", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadCollection.class)
    public JsonNode getFilesByProjectAndIdOrLabel(@PathVariable final String projectId, @PathVariable final String idOrLabel) throws NotFoundException, IOException {
        return _serializer.deserializeJson(getByProjectAndIdOrLabel(projectId, idOrLabel).getFiles());
    }

    @ApiOperation(value = "Creates a new dataset collection.", response = SetsCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collection successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to create the dataset collection."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateCollection.class)
    public SetsCollection create(@RequestBody final SetsCollection entity) throws DataFormatException, InsufficientPrivilegesException, ResourceAlreadyExistsException, NotFoundException {
        return _collections.create(getSessionUser(), entity);
    }

    @ApiOperation(value = "Updates an existing dataset collection.", response = SetsCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collection successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to update the dataset collection."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Authorizer)
    @AuthDelegate(EditCollection.class)
    public SetsCollection update(@PathVariable final String id, @RequestBody final SetsCollection entity) throws DataFormatException, NotFoundException, InsufficientPrivilegesException, ResourceAlreadyExistsException {
        validateEntityId(id, entity);
        return _collections.update(getSessionUser(), entity);
    }

    @ApiOperation(value = "Updates an existing dataset collection.", response = SetsCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collection successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to update the dataset collection."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Authorizer)
    @AuthDelegate(EditCollection.class)
    public SetsCollection update(@PathVariable final String projectId, @PathVariable final String idOrLabel, @RequestBody final SetsCollection entity) throws DataFormatException, NotFoundException, InsufficientPrivilegesException, ResourceAlreadyExistsException {
        validateEntityId(projectId, idOrLabel, entity);
        return update(entity.getId(), entity);
    }

    @ApiOperation(value = "Deletes the specified dataset collection.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collection successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to delete the dataset collection."),
                   @ApiResponse(code = 404, message = "The requested dataset collection doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public void delete(@PathVariable("id") final String id) throws NotFoundException, InsufficientPrivilegesException {
        _collections.delete(getSessionUser(), id);
    }

    @ApiOperation(value = "Deletes the specified dataset collection.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset collection successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to delete the dataset collection."),
                   @ApiResponse(code = 404, message = "The requested dataset collection doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public void delete(@PathVariable final String projectId, @PathVariable final String idOrLabel) throws NotFoundException, InsufficientPrivilegesException {
        _collections.delete(getSessionUser(), projectId, idOrLabel);
    }

    private final DatasetCollectionService _collections;
    private final SerializerService        _serializer;
}

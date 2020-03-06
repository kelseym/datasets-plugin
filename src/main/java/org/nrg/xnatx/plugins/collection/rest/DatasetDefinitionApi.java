/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.rest.CollectionApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.rest;

import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.nrg.xdat.security.helpers.AccessLevel.Read;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.collection.rest.permissions.CreateCollection;
import org.nrg.xnatx.plugins.collection.rest.permissions.CreateDefinition;
import org.nrg.xnatx.plugins.collection.rest.permissions.EditDefinition;
import org.nrg.xnatx.plugins.collection.rest.permissions.ReadDefinition;
import org.nrg.xnatx.plugins.collection.services.DatasetDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api("XNAT 1.7 Dataset Definition Plugin API")
@XapiRestController
@RequestMapping(value = "/sets/definitions")
@Slf4j
public class DatasetDefinitionApi extends AbstractXapiRestController {
    @Autowired
    public DatasetDefinitionApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final DatasetDefinitionService definitions) {
        super(userManagementService, roleHolder);
        _definitions = definitions;
    }

    @ApiOperation(value = "Returns all dataset definitions on the system.", response = SetsDefinition.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definitions successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access dataset definitions on the system."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Read)
    public List<? extends SetsDefinition> getAll() {
        return _definitions.findAll(getSessionUser());
    }

    @ApiOperation(value = "Returns a list of all dataset definitions for a project.", response = SetsDefinition.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definitions successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access dataset definitions in the requested project."),
                   @ApiResponse(code = 404, message = "The requested project doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadDefinition.class)
    public List<? extends SetsDefinition> getByProject(@PathVariable("projectId") final String projectId) throws NotFoundException {
        return _definitions.findByProject(getSessionUser(), projectId);
    }

    @ApiOperation(value = "Returns the dataset definition with the submitted ID.", response = SetsDefinition.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access the requested dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadDefinition.class)
    public SetsDefinition getById(@PathVariable final String id) throws NotFoundException {
        return _definitions.findById(getSessionUser(), id);
    }

    @ApiOperation(value = "Returns the dataset definition with the submitted ID or label in the specified project.", response = SetsDefinition.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access the requested dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadDefinition.class)
    public SetsDefinition getByProjectAndIdOrLabel(@PathVariable final String projectId, @PathVariable final String idOrLabel) throws NotFoundException {
        return _definitions.findByProjectAndIdOrLabel(getSessionUser(), projectId, idOrLabel);
    }

    @ApiOperation(value = "Creates a new dataset definition.", response = SetsDefinition.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to create the dataset definition."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateDefinition.class)
    public SetsDefinition create(@RequestBody final SetsDefinition entity) throws DataFormatException, InsufficientPrivilegesException, ResourceAlreadyExistsException, NotFoundException {
        return _definitions.create(getSessionUser(), entity);
    }

    @ApiOperation(value = "Updates an existing dataset definition.", response = SetsDefinition.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to update the dataset definition."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Authorizer)
    @AuthDelegate(EditDefinition.class)
    public SetsDefinition update(@PathVariable final String id, @RequestBody final SetsDefinition entity) throws DataFormatException, NotFoundException, InsufficientPrivilegesException, ResourceAlreadyExistsException {
        if (!StringUtils.equals(id, entity.getId())) {
            throw new DataFormatException("The submitted dataset definition didn't match the specified ID.");
        }
        return _definitions.update(getSessionUser(), entity);
    }

    @ApiOperation(value = "Updates an existing dataset definition.", response = SetsDefinition.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to update the dataset definition."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Authorizer)
    @AuthDelegate(EditDefinition.class)
    public SetsDefinition update(@PathVariable final String projectId, @PathVariable final String idOrLabel, @RequestBody final SetsDefinition entity) throws DataFormatException, InsufficientPrivilegesException, NotFoundException, ResourceAlreadyExistsException {
        if (!StringUtils.equals(projectId, entity.getProject()) || !StringUtils.equalsAny(idOrLabel, entity.getId(), entity.getLabel())) {
            throw new DataFormatException("The submitted dataset definition didn't match the specified project and ID or label.");
        }
        return update(entity.getId(), entity);
    }

    @ApiOperation(value = "Deletes the specified dataset definition.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to delete the dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public void delete(@PathVariable("id") final String id) throws NotFoundException, InsufficientPrivilegesException {
        _definitions.delete(getSessionUser(), id);
    }

    @ApiOperation(value = "Deletes the specified dataset definition.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to delete the dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public void delete(@PathVariable final String projectId, @PathVariable final String idOrLabel) throws NotFoundException, InsufficientPrivilegesException {
        _definitions.delete(getSessionUser(), projectId, idOrLabel);
    }

    @ApiOperation(value = "Resolves the specified dataset definition.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateCollection.class)
    public void resolve(@PathVariable("id") final String id) throws NotFoundException, InsufficientPrivilegesException {
        _definitions.resolve(getSessionUser(), id);
    }

    @ApiOperation(value = "Resolves the specified dataset definition.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = Authorizer)
    @AuthDelegate(CreateCollection.class)
    public void resolve(@PathVariable final String projectId, @PathVariable final String idOrLabel) throws NotFoundException, InsufficientPrivilegesException {
        _definitions.resolve(getSessionUser(), projectId, idOrLabel);
    }

    private final DatasetDefinitionService _definitions;
}

/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.rest.DatasetDefinitionApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.rest;

import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.nrg.xdat.security.helpers.AccessLevel.Read;
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
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.rest.AbstractExperimentXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.model.SetsCollectionI;
import org.nrg.xdat.model.SetsDefinitionI;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xnatx.plugins.datasets.resolvers.SessionReport;
import org.nrg.xnatx.plugins.datasets.rest.permissions.CreateCollection;
import org.nrg.xnatx.plugins.datasets.rest.permissions.CreateDefinition;
import org.nrg.xnatx.plugins.datasets.rest.permissions.EditDefinition;
import org.nrg.xnatx.plugins.datasets.rest.permissions.ReadDefinition;
import org.nrg.xnatx.plugins.datasets.services.DatasetDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Api("XNAT Dataset Definition API")
@XapiRestController
@RequestMapping(value = "/sets/definitions")
@Slf4j
public class DatasetDefinitionApi extends AbstractExperimentXapiRestController<SetsDefinition> {
    @Autowired
    public DatasetDefinitionApi(final NamedParameterJdbcTemplate template, final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final DatasetDefinitionService definitions) throws ElementNotFoundException, XFTInitException, IllegalAccessException, NoSuchFieldException {
        super(template, userManagementService, roleHolder);
        _definitions = definitions;
    }

    @ApiOperation(value = "Returns all dataset definitions on the system.", response = SetsDefinitionI.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definitions successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access dataset definitions on the system."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Read)
    public List<? extends SetsDefinitionI> getAll() {
        return _definitions.findAll(getSessionUser());
    }

    @ApiOperation(value = "Returns a list of all dataset definitions for a project.", response = SetsDefinitionI.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definitions successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access dataset definitions in the requested project."),
                   @ApiResponse(code = 404, message = "The requested project doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadDefinition.class)
    public List<? extends SetsDefinitionI> getByProject(@PathVariable("projectId") final String projectId) throws NotFoundException {
        return _definitions.findByProject(getSessionUser(), projectId);
    }

    @ApiOperation(value = "Resolves the submitted resolver payload and returns the results.")
    @ApiResponses({@ApiResponse(code = 200, message = "Payload successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the payload."),
                   @ApiResponse(code = 404, message = "The requested project doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "evaluate/{projectId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateDefinition.class)
    public SetsCollection evaluate(final @PathVariable String projectId, @RequestBody final JsonNode payload) throws InsufficientPrivilegesException {
        return evaluate(projectId, null, payload);
    }

    @ApiOperation(value = "Resolves the submitted resolver payload and returns the results.")
    @ApiResponses({@ApiResponse(code = 200, message = "Payload successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the payload."),
                   @ApiResponse(code = 404, message = "The requested project doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "evaluate/{projectId}/{resolver}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateDefinition.class)
    public SetsCollection evaluate(final @PathVariable String projectId, final @PathVariable String resolver, @RequestBody final JsonNode payload) throws InsufficientPrivilegesException {
        return _definitions.evaluate(getSessionUser(), projectId, resolver, payload);
    }

    @ApiOperation(value = "Resolves the submitted resolver payload and returns the results.")
    @ApiResponses({@ApiResponse(code = 200, message = "Payload successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the payload."),
                   @ApiResponse(code = 404, message = "The requested project doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "report/{projectId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateDefinition.class)
    public List<SessionReport> report(final @PathVariable String projectId, @RequestBody final JsonNode payload) throws InsufficientPrivilegesException {
        return report(projectId, null, payload);
    }

    @ApiOperation(value = "Resolves the submitted resolver payload and returns the results.")
    @ApiResponses({@ApiResponse(code = 200, message = "Payload successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the payload."),
                   @ApiResponse(code = 404, message = "The requested project doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "report/{projectId}/{resolver}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateDefinition.class)
    public List<SessionReport> report(final @PathVariable String projectId, final @PathVariable String resolver, @RequestBody final JsonNode payload) throws InsufficientPrivilegesException {
        return _definitions.report(getSessionUser(), projectId, resolver, payload).getSessions();
    }

    @ApiOperation(value = "Returns the dataset definition with the submitted ID.", response = SetsDefinitionI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access the requested dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadDefinition.class)
    public SetsDefinitionI getById(@PathVariable final String id) throws NotFoundException {
        return _definitions.findById(getSessionUser(), id);
    }

    @ApiOperation(value = "Returns the dataset definition with the submitted ID or label in the specified project.", response = SetsDefinitionI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to access the requested dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadDefinition.class)
    public SetsDefinitionI getByProjectAndIdOrLabel(@PathVariable final String projectId, @PathVariable final String idOrLabel) throws NotFoundException {
        return _definitions.findByProjectAndIdOrLabel(getSessionUser(), projectId, idOrLabel);
    }

    @ApiOperation(value = "Creates a new dataset definition.", response = SetsDefinitionI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to create the dataset definition."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateDefinition.class)
    public SetsDefinitionI create(@RequestBody final SetsDefinitionI entity) throws DataFormatException, InsufficientPrivilegesException, ResourceAlreadyExistsException, NotFoundException {
        return _definitions.create(getSessionUser(), (SetsDefinition) entity);
    }

    @ApiOperation(value = "Updates an existing dataset definition.", response = SetsDefinitionI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to update the dataset definition."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Authorizer)
    @AuthDelegate(EditDefinition.class)
    public SetsDefinitionI update(@PathVariable final String id, @RequestBody final SetsDefinitionI entity) throws DataFormatException, NotFoundException, InsufficientPrivilegesException, ResourceAlreadyExistsException {
        SetsDefinition setsDefinition = (SetsDefinition) entity;
        validateEntityId(id, setsDefinition);
        return _definitions.update(getSessionUser(), setsDefinition);
    }

    @ApiOperation(value = "Updates an existing dataset definition.", response = SetsDefinitionI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to update the dataset definition."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Authorizer)
    @AuthDelegate(EditDefinition.class)
    public SetsDefinitionI update(@PathVariable final String projectId, @PathVariable final String idOrLabel, @RequestBody final SetsDefinitionI entity) throws DataFormatException, InsufficientPrivilegesException, NotFoundException, ResourceAlreadyExistsException {
        validateEntityId(projectId, idOrLabel, (SetsDefinition) entity);
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
    public SetsCollection resolve(@PathVariable("id") final String id) throws NotFoundException, InsufficientPrivilegesException {
        return _definitions.resolve(getSessionUser(), id);
    }

    @ApiOperation(value = "Resolves the specified dataset definition.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateCollection.class)
    public SetsCollection resolve(@PathVariable("id") final String id, final @RequestBody SetsCollectionI collection) throws NotFoundException, InsufficientPrivilegesException, DataFormatException, ResourceAlreadyExistsException {
        return _definitions.resolve(getSessionUser(), id, (SetsCollection) collection);
    }

    @ApiOperation(value = "Resolves the specified dataset definition.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateCollection.class)
    public SetsCollection resolve(@PathVariable final String projectId, @PathVariable final String idOrLabel) throws NotFoundException, InsufficientPrivilegesException {
        return _definitions.resolve(getSessionUser(), projectId, idOrLabel);
    }

    @ApiOperation(value = "Resolves the specified dataset definition.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/{idOrLabel}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateCollection.class)
    public SetsCollection resolve(@PathVariable final String projectId, @PathVariable final String idOrLabel, final @RequestBody SetsCollectionI collection) throws NotFoundException, InsufficientPrivilegesException, DataFormatException, ResourceAlreadyExistsException {
        return _definitions.resolve(getSessionUser(), projectId, idOrLabel, (SetsCollection) collection);
    }

    private final DatasetDefinitionService   _definitions;
}

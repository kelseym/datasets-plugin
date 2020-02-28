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
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.model.XnatxDatacollectiondefinitionI;
import org.nrg.xdat.om.XnatxDatacollectiondefinition;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.rest.permissions.CreateCollection;
import org.nrg.xnatx.plugins.collection.rest.permissions.ReadCollection;
import org.nrg.xnatx.plugins.collection.services.DataCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/*

Questions:

* Terminology? Maybe data criteria for definition and data set for resolved definition?
*

*/


@Api("XNAT 1.7 Data Set Plugin API")
@XapiRestController
@RequestMapping(value = "/sets")
@Slf4j
public class DataSetDefinitionApi extends AbstractXapiRestController {
    @Autowired
    public DataSetDefinitionApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final DataCollectionService service, final DatabaseHelper helper) throws IOException {
        super(userManagementService, roleHolder);
        _service = service;
    }

    @ApiOperation(value = "Returns a specific collection.", response = XnatxDatacollectiondefinitionI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Collection successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "definitions", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Read)
    public List<? extends XnatxDatacollectiondefinitionI> getDefinitions() throws NotFoundException {
        return _service.findDefinitionById(id);
    }

    @ApiOperation(value = "Returns a list of all data-set definitions for a project.", response = XnatxDatacollectiondefinitionI.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Collections successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "projects/{projectId}/definitions", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(ReadCollection.class)
    public List<? extends XnatxDatacollectiondefinitionI> getProjectDefinitions(@PathVariable("projectId") final String projectId) throws NotFoundException {
        return _service.findDefinitionsByProject(projectId);
    }

    @ApiOperation(value = "Returns a specific collection.", response = XnatxDatacollectiondefinitionI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Collection successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "definitions/{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Read)
    public XnatxDatacollectiondefinitionI getDefinition(@PathVariable final String id) throws NotFoundException {
        return _service.findDefinitionById(id);
    }

    @ApiOperation(value = "Creates a new collection.", response = XnatxDatacollectiondefinitionI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(CreateCollection.class)
    public XnatxDatacollectiondefinitionI createCollection(@RequestBody final XnatxDatacollectiondefinitionI entity) {
        return _service.create(entity);
    }

    @ApiOperation(value = "Creates a new collection from a set of experiments.", response = XnatxDatacollectiondefinitionI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "createFromSet", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<XnatxDatacollectiondefinitionI> createCollectionFromSet(@RequestBody final DataCollectionModel model) {
        XnatxDatacollectiondefinition collection = new XnatxDatacollectiondefinition();
        collection.setName(model.getName());
        collection.setProjectId(model.getProjectId());
        collection.setDescription(model.getDescription());
        collection.setImageSeriesDescription(model.getImageSeriesDescription());
        collection.setLabelSeriesDescription(model.getLabelSeriesDescription());
        List<String> experimentIds = model.getExperiments();
        Collections.shuffle(experimentIds);
        int    exptCount          = experimentIds.size();
        double fractionTraining   = .7;
        double fractionValidation = .2;
        int    trainingEndIndex   = (int) Math.round(exptCount * fractionTraining);
        int    validationEndIndex = (int) Math.round(exptCount * (fractionValidation + fractionTraining));

        collection.setTrainingExperiments(experimentIds.subList(0, trainingEndIndex));
        collection.setValidationExperiments(experimentIds.subList(trainingEndIndex, validationEndIndex));
        collection.setTestExperiments(experimentIds.subList(validationEndIndex, exptCount));

        final XnatxDatacollectiondefinition created = _service.create(collection);
        return new ResponseEntity<>(created, HttpStatus.OK);
    }

    @ApiOperation(value = "Updates a collection.", response = XnatxDatacollectiondefinition.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "update", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<XnatxDatacollectiondefinition> updateCollection(@RequestBody final XnatxDatacollectiondefinition newCollection) throws NotFoundException {
        XnatxDatacollectiondefinition collection = _service.get(newCollection.getId());
        collection.setName(newCollection.getName());
        collection.setProjectId(newCollection.getProjectId());
        collection.setDescription(newCollection.getDescription());
        collection.setImageSeriesDescription(newCollection.getImageSeriesDescription());
        collection.setLabelSeriesDescription(newCollection.getLabelSeriesDescription());
        collection.setTrainingExperiments(newCollection.getTrainingExperiments());
        collection.setValidationExperiments(newCollection.getValidationExperiments());
        collection.setTestExperiments(newCollection.getTestExperiments());

        _service.update(collection);
        return new ResponseEntity<>(collection, HttpStatus.OK);
    }

    @ApiOperation(value = "Deletes a collection.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Collection successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "delete/{id}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<Boolean> deleteCollection(@PathVariable("id") final long id) {
        _service.delete(id);
        return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the paths to the files in the collection as training, validation, and test data sets.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "JSON returned."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "jsonForCollection/{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public Map<String, List<Map<String, String>>> jsonForCollection(@ApiParam(value = "ID of the collection", required = true) @PathVariable("id") final long id) throws NotFoundException {
        final UserI user = getSessionUser();
        return _service.getCollectionResources(user, id);
    }

    private final DataCollectionService _service;
}

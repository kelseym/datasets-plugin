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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.entities.DataCollection;
import org.nrg.xnatx.plugins.collection.models.DataCollectionModel;
import org.nrg.xnatx.plugins.collection.services.DataCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api("XNAT 1.7 Collection Plugin API")
@XapiRestController
@RequestMapping(value = "/collection")
@Slf4j
public class DataCollectionApi extends AbstractXapiRestController {
    @Autowired
    public DataCollectionApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final DataCollectionService collectionService) {
        super(userManagementService, roleHolder);
        _collectionService = collectionService;
    }

    @ApiOperation(value = "Returns a specific collection.", response = DataCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Collection successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "get/{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<DataCollection> getCollection(@PathVariable("id") final long id) throws NotFoundException {
        return new ResponseEntity<>(_collectionService.get(id), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a list of all collections for a project.", response = DataCollection.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Collections successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "getAllForProject/{projectId}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<List<DataCollection>> getProjectCollections(@PathVariable("projectId") final String projectId) {
        return new ResponseEntity<>(_collectionService.getAllByProject(projectId), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a list of all collections.", response = DataCollection.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Collections successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "getAll", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<List<DataCollection>> getCollections() {
        return new ResponseEntity<>(_collectionService.getAll(), HttpStatus.OK);
    }

    @ApiOperation(value = "Creates a new collection.", response = DataCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<DataCollection> createCollection(@RequestBody final DataCollection entity) {
        final DataCollection created = _collectionService.create(entity);
        return new ResponseEntity<>(created, HttpStatus.OK);
    }

    @ApiOperation(value = "Creates a new collection from a set of experiments.", response = DataCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "createFromSet", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<DataCollection> createCollectionFromSet(@RequestBody final DataCollectionModel model) {
        DataCollection collection = new DataCollection();
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

        final DataCollection created = _collectionService.create(collection);
        return new ResponseEntity<>(created, HttpStatus.OK);
    }

    @ApiOperation(value = "Updates a collection.", response = DataCollection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "update", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.PUT, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<DataCollection> updateCollection(@RequestBody final DataCollection newCollection) throws NotFoundException {
        DataCollection collection = _collectionService.get(newCollection.getId());
        collection.setName(newCollection.getName());
        collection.setProjectId(newCollection.getProjectId());
        collection.setDescription(newCollection.getDescription());
        collection.setImageSeriesDescription(newCollection.getImageSeriesDescription());
        collection.setLabelSeriesDescription(newCollection.getLabelSeriesDescription());
        collection.setTrainingExperiments(newCollection.getTrainingExperiments());
        collection.setValidationExperiments(newCollection.getValidationExperiments());
        collection.setTestExperiments(newCollection.getTestExperiments());

        _collectionService.update(collection);
        return new ResponseEntity<>(collection, HttpStatus.OK);
    }

    @ApiOperation(value = "Deletes a collection.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Collection successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "delete/{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.DELETE, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<Boolean> deleteCollection(@PathVariable("id") final long id) {
        _collectionService.delete(id);
        return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the paths to the files in the collection as training, validation, and test data sets.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "JSON returned."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "jsonForCollection/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public Map<String, List<Map<String, String>>> jsonForCollection(@ApiParam(value = "ID of the collection", required = true) @PathVariable("id") final long id) throws NotFoundException {
        final UserI user = getSessionUser();
        return _collectionService.getCollectionResources(user, id);
    }

    private final DataCollectionService _collectionService;
}

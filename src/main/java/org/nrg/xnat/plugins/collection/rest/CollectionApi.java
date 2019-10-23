/*
 * xnat-template: org.nrg.xnat.plugins.template.rest.TemplatePrefsApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.plugins.collection.rest;

import io.swagger.annotations.*;
import org.apache.commons.io.FilenameUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.Username;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.plugins.collection.entities.Collection;
import org.nrg.xnat.plugins.collection.model.CollectionModel;
import org.nrg.xnat.plugins.collection.services.CollectionService;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.io.File;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;

@Api(description = "XNAT 1.7 Collection Plugin API")
@XapiRestController
@RequestMapping(value = "/collection")
public class CollectionApi extends AbstractXapiRestController {
    @Autowired
    public CollectionApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final CollectionService collectionService) {
        super(userManagementService, roleHolder);
        _collectionService = collectionService;
    }

    @ApiOperation(value = "Returns a specific collection.", response = Collection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Collection successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "get/{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<Collection> getCollection(@PathVariable("id") final long id) throws NotFoundException {
        return new ResponseEntity<>(_collectionService.get(id), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a list of all collections for a project.", response = Collection.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Collections successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "getAllForProject/{projectId}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<List<Collection>> getProjectCollections(@PathVariable("projectId") final String projectId) {
        return new ResponseEntity<>(_collectionService.getAllByProject(projectId), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a list of all collections.", response = Collection.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Collections successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "getAll", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<List<Collection>> getCollections() {
        return new ResponseEntity<>(_collectionService.getAll(), HttpStatus.OK);
    }

    @ApiOperation(value = "Creates a new collection.", response = Collection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<Collection> createCollection(@RequestBody final Collection entity) {
        final Collection created = _collectionService.create(entity);
        return new ResponseEntity<>(created, HttpStatus.OK);
    }

    @ApiOperation(value = "Creates a new collection from a set of experiments.", response = Collection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "createFromSet", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<Collection> createCollectionFromSet(@RequestBody final CollectionModel model) {
        Collection collection = new Collection();
        collection.setName(model.getName());
        collection.setProjectId(model.getProjectId());
        collection.setDescription(model.getDescription());
        List<String> experimentIds = model.getExperiments();
        Collections.shuffle(experimentIds);
        int exptCount = experimentIds.size();
        double fractionTraining = .7;
        double fractionValidation = .2;
        double fractionTest = .1;
        int traningEndIndex = (int)(exptCount*fractionTraining);
        int validationEndIndex = (int)(exptCount*(fractionValidation+fractionTraining));

        collection.setTrainingExperiments(experimentIds.subList(0,traningEndIndex));
        collection.setValidationExperiments(experimentIds.subList(traningEndIndex,validationEndIndex));
        collection.setTestExperiments(experimentIds.subList(validationEndIndex,exptCount));

        final Collection created = _collectionService.create(collection);
        return new ResponseEntity<>(created, HttpStatus.OK);
    }

    @ApiOperation(value = "Updates a collection.", response = Collection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "update", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.PUT, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<Collection> updateCollection(@RequestBody final Collection newCollection) throws NotFoundException {
        Collection collection = _collectionService.get(newCollection.getId());
        collection.setName(newCollection.getName());
        collection.setProjectId(newCollection.getProjectId());
        collection.setDescription(newCollection.getDescription());
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

    @ApiOperation(value = "Returns JSON with the paths to the files in the collection.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "JSON returned."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "jsonForCollection/{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    public ResponseEntity<String> jsonForCollection(@ApiParam(value = "ID of the collection", required = true) @PathVariable("id") final long id) throws NotFoundException {
        final UserI user = getSessionUser();
        final Collection currCollection = _collectionService.get(id);
        Path projectPath = Paths.get(ArcSpecManager.GetInstance().getArchivePathForProject(currCollection.getProjectId()));

        String aggregateString = "{\n\"training\": [";
        try{
            for(String exptID : currCollection.getTrainingExperiments()){
                XnatImagesessiondata expt = (XnatImagesessiondata)XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);
                List<XnatImagescandata> scans = ((XnatImagesessiondataI)expt).getScans_scan();
                XnatImagescandata imagesScan = null;
                XnatImagescandata labelsScan = null;
                for(XnatImagescandata scan : scans) {
                    if(scan.getType().equals("IMAGES")) {
                        imagesScan = scan;
                    }
                    else if(scan.getType().equals("LABELS")) {
                        labelsScan = scan;
                    }
                }
                if(imagesScan!=null && labelsScan!=null) {
                    File niiImageFile = null;
                    for(XnatAbstractresourceI resI: imagesScan.getFile()){
                        XnatAbstractresource imagesRes = (XnatAbstractresource) resI;
                        if(imagesRes!=null) {
                            ArrayList<File> imagesFiles = imagesRes.getCorrespondingFiles(expt.getArchivePath());
                            if(imagesFiles!=null){
                                for(File curr: imagesFiles){
                                    String currPath = curr.getPath();
                                    if(currPath!=null && FilenameUtils.getExtension(currPath).equals("nii")){
                                        niiImageFile = curr;
                                    }
                                }
                            }
                        }
                    }

                    File niiLabelFile = null;
                    for(XnatAbstractresourceI resI: labelsScan.getFile()){
                        XnatAbstractresource labelsRes = (XnatAbstractresource) resI;
                        if(labelsRes!=null) {
                            ArrayList<File> labelsFiles = labelsRes.getCorrespondingFiles(expt.getArchivePath());
                            if(labelsFiles!=null){
                                for(File curr: labelsFiles){
                                    String currPath = curr.getPath();
                                    if(currPath!=null && FilenameUtils.getExtension(currPath).equals("nii")){
                                        niiLabelFile = curr;
                                    }
                                }
                            }
                        }
                    }
                    if(niiImageFile!=null && niiLabelFile!=null){
                        final String imageUri = niiImageFile.getPath();
                        final String labelUri = niiLabelFile.getPath();
                        final Path imagePath = Paths.get(imageUri);
                        final Path labelPath = Paths.get(labelUri);
                        final Path relativeImagePath = projectPath.relativize(imagePath);
                        final Path relativeLabelPath = projectPath.relativize(labelPath);

                        aggregateString += "\n{\n\"image\": \"" + relativeImagePath + "\",\n\"label\": \"" + relativeLabelPath + "\"\n},";
                    }
                }
            }
        }
        catch(Exception e){

        }
        if(aggregateString.endsWith(",")){
            aggregateString = aggregateString.substring(0,aggregateString.length()-1);
        }
        aggregateString+="],";
        aggregateString += "\n\"validation\": [";
        try{
            for(String exptID : currCollection.getValidationExperiments()){
                XnatImagesessiondata expt = (XnatImagesessiondata)XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);
                List<XnatImagescandata> scans = ((XnatImagesessiondataI)expt).getScans_scan();
                XnatImagescandata imagesScan = null;
                XnatImagescandata labelsScan = null;
                for(XnatImagescandata scan : scans) {
                    if(scan.getType().equals("IMAGES")) {
                        imagesScan = scan;
                    }
                    else if(scan.getType().equals("LABELS")) {
                        labelsScan = scan;
                    }
                }
                if(imagesScan!=null && labelsScan!=null) {
                    File niiImageFile = null;
                    for(XnatAbstractresourceI resI: imagesScan.getFile()){
                        XnatAbstractresource imagesRes = (XnatAbstractresource) resI;
                        if(imagesRes!=null) {
                            ArrayList<File> imagesFiles = imagesRes.getCorrespondingFiles(expt.getArchivePath());
                            if(imagesFiles!=null){
                                for(File curr: imagesFiles){
                                    String currPath = curr.getPath();
                                    if(currPath!=null && FilenameUtils.getExtension(currPath).equals("nii")){
                                        niiImageFile = curr;
                                    }
                                }
                            }
                        }
                    }

                    File niiLabelFile = null;
                    for(XnatAbstractresourceI resI: labelsScan.getFile()){
                        XnatAbstractresource labelsRes = (XnatAbstractresource) resI;
                        if(labelsRes!=null) {
                            ArrayList<File> labelsFiles = labelsRes.getCorrespondingFiles(expt.getArchivePath());
                            if(labelsFiles!=null){
                                for(File curr: labelsFiles){
                                    String currPath = curr.getPath();
                                    if(currPath!=null && FilenameUtils.getExtension(currPath).equals("nii")){
                                        niiLabelFile = curr;
                                    }
                                }
                            }
                        }
                    }
                    if(niiImageFile!=null && niiLabelFile!=null){
                        final String imageUri = niiImageFile.getPath();
                        final String labelUri = niiLabelFile.getPath();
                        final Path imagePath = Paths.get(imageUri);
                        final Path labelPath = Paths.get(labelUri);
                        final Path relativeImagePath = projectPath.relativize(imagePath);
                        final Path relativeLabelPath = projectPath.relativize(labelPath);

                        aggregateString += "\n{\n\"image\": \"" + relativeImagePath + "\",\n\"label\": \"" + relativeLabelPath + "\"\n},";
                    }
                }
            }
        }
        catch(Exception e){

        }
        if(aggregateString.endsWith(",")){
            aggregateString = aggregateString.substring(0,aggregateString.length()-1);
        }
        aggregateString+="]\n}";
        return new ResponseEntity<>(aggregateString, HttpStatus.OK);
    }

    private final CollectionService _collectionService;
}
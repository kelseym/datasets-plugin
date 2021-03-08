/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.services.impl.xft.AbstractXftDatasetObjectService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.services.impl.xft;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xnat.utils.WorkflowUtils;
import org.nrg.xnatx.plugins.datasets.exceptions.DatasetDefinitionHandlingException;
import org.nrg.xnatx.plugins.datasets.exceptions.DatasetObjectException;
import org.nrg.xnatx.plugins.datasets.services.DatasetObjectService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter(AccessLevel.PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractXftDatasetObjectService<T extends XnatExperimentdata> implements DatasetObjectService<T> {
    protected AbstractXftDatasetObjectService(final PermissionsServiceI permissions, final NamedParameterJdbcTemplate template) {
        _permissions = permissions;
        _template = template;
        _dataType = Reflection.getParameterizedTypeForClass(getClass());
        try {
            _factoryMethod = _dataType.getMethod("get" + _dataType.getSimpleName() + "sById", Object.class, UserI.class, Boolean.TYPE);
        } catch (NoSuchMethodException e) {
            throw new DatasetObjectException("Got an error trying to get the ItemI constructor for the data type: " + _dataType.getName(), e);
        }
        try {
            _xsiType = (String) _dataType.getField("SCHEMA_ELEMENT_NAME").get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new DatasetObjectException("Got an error trying to get the schema element name for the data type: " + _dataType.getName(), e);
        }
        _xsiXmlPath = _xsiType + "/extension_item/element_name";
        _projectXmlPath = _xsiType + "/project";
    }

    @Override
    public T create(final @Nonnull UserI user, final @Nonnull T object) throws InsufficientPrivilegesException, ResourceAlreadyExistsException, DataFormatException, NotFoundException {
        return createOrUpdateImpl(true, user, object);
    }

    @Override
    public List<T> findAll(final UserI user) {
        return getExperimentsByField(_xsiXmlPath, _xsiType, user);
    }

    @Override
    public List<T> findByProject(final UserI user, final String project) throws NotFoundException {
        if (!testProject(project)) {
            throw new NotFoundException("No project with ID " + project + " can be found.");
        }
        return getExperimentsByField(_projectXmlPath, project, user);
    }

    @Override
    public T findById(final UserI user, final String id) throws NotFoundException {
        return getExperiment(user, id);
    }

    @Override
    public T findByProjectAndIdOrLabel(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException {
        return getExperiment(user, projectId, idOrLabel);
    }

    @Override
    public T update(final UserI user, final T object) throws InsufficientPrivilegesException, ResourceAlreadyExistsException, DataFormatException, NotFoundException {
        return createOrUpdateImpl(false, user, object);
    }

    @Override
    public void delete(final UserI user, final String id) throws NotFoundException, InsufficientPrivilegesException {
        deleteImpl(user, findById(user, id));
    }

    @Override
    public void delete(final UserI user, final String project, final String idOrLabel) throws NotFoundException, InsufficientPrivilegesException {
        deleteImpl(user, findByProjectAndIdOrLabel(user, project, idOrLabel));
    }

    protected T getExperimentSafely(final UserI user, final String id) {
        try {
            return getExperiment(user, id);
        } catch (NotFoundException e) {
            throw new RuntimeException("Got a not found exception when getting experiment ID " + id + " safely: you shouldn't call this method unless you *know* the object exists", e);
        }
    }

    protected T getExperiment(final UserI user, final String id) throws NotFoundException {
        if (!_template.queryForObject(QUERY_EXPT_ID_WITH_DATA_TYPE_EXISTS, new MapSqlParameterSource(PARAM_EXPERIMENT, id).addValue(PARAM_DATA_TYPE, _xsiType), Boolean.class)) {
            throw new NotFoundException("User " + user.getUsername() + " requested " + _xsiType + " experiment with ID " + id + ", but that doesn't exist.");
        }
        try {
            return _dataType.cast(_factoryMethod.invoke(null, id, user, true));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new DatasetDefinitionHandlingException("An error occurred invoking the factory method " + _dataType.getName() + "." + _factoryMethod.getName() + "() for the class implementation of the schema element for XSI type " + _xsiType, e);
        }
    }

    protected T getExperiment(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException {
        return getExperiment(user, getIdForProjectAndLabel(projectId, idOrLabel));
    }

    protected List<T> getExperimentsByField(final String xmlPath, final String value, final UserI user) {
        return XnatExperimentdata.getXnatExperimentdatasByField(xmlPath, value, user, false).stream().map(XnatExperimentdata::getId).map(id -> getExperimentSafely(user, id)).collect(Collectors.toList());
    }

    protected String getIdForProjectAndLabel(final String projectId, final String idOrLabel) {
        return _template.queryForObject(QUERY_EXPT_PROJECT_AND_LABEL, new MapSqlParameterSource(PARAM_PROJECT, projectId).addValue(PARAM_ID_OR_LABEL, idOrLabel), String.class);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean testProject(final String project) {
        return _template.queryForObject(QUERY_PROJECT_ID_EXISTS, new MapSqlParameterSource(PARAM_PROJECT, project), Boolean.class);
    }

    protected boolean testExperimentId(final String id) {
        return StringUtils.isNotBlank(id) && _template.queryForObject(QUERY_EXPT_ID_EXISTS, new MapSqlParameterSource(PARAM_EXPERIMENT, id), Boolean.class);
    }

    protected boolean testExperimentProjectAndLabel(final String project, final String label) {
        return StringUtils.isNoneBlank(project, label) && _template.queryForObject(QUERY_EXPT_PROJECT_AND_LABEL_EXISTS, new MapSqlParameterSource(PARAM_PROJECT, project).addValue(PARAM_ID_OR_LABEL, label), Boolean.class);
    }

    private T createOrUpdateImpl(final boolean isCreate, final UserI user, final T item) throws InsufficientPrivilegesException, ResourceAlreadyExistsException, DataFormatException, NotFoundException {
        return createOrUpdateImpl(isCreate, user, item, Collections.emptyList());
    }

    private T createOrUpdateImpl(final boolean isCreate, final UserI user, final T object, final List<PostSaveOperation> operations) throws InsufficientPrivilegesException, ResourceAlreadyExistsException, DataFormatException, NotFoundException {
        return createOrUpdateImpl(isCreate, user, object, operations, Collections.emptyMap());
    }

    private T createOrUpdateImpl(final boolean isCreate, final UserI user, final T object, final List<PostSaveOperation> operations, final Map<String, String> eventDetails) throws InsufficientPrivilegesException, ResourceAlreadyExistsException, DataFormatException, NotFoundException {
        if (StringUtils.isBlank(object.getId())) {
            try {
                object.setId(XnatExperimentdata.CreateNewID());
            } catch (Exception e) {
                throw new DatasetObjectException("An error occurred trying to create a new experiment ID while creating a new item of type " + object.getXSIType() + " in the project " + object.getProject() + " for user " + user.getUsername(), e);
            }
        }

        final String  username = user.getUsername();
        final XFTItem item     = object.getItem();
        final String  project  = object.getProject();
        final String  id       = object.getId();
        final String  xsiType  = object.getXSIType();
        final String  label    = object.getLabel();

        if (isCreate) {
            if (testExperimentId(id)) {
                throw new ResourceAlreadyExistsException(xsiType, id);
            }
            if (StringUtils.isAnyBlank(project, label)) {
                throw new DataFormatException("New objects must have both a project and label specified");
            }
            if (!testProject(project)) {
                throw new NotFoundException("No project with ID " + project + " can be found.");
            }
            if (testExperimentProjectAndLabel(project, label)) {
                throw new ResourceAlreadyExistsException(xsiType, project + "/" + label);
            }
            try {
                if (!_permissions.canCreate(user, item)) {
                    throw new InsufficientPrivilegesException("The user " + username + " has insufficient privileges to create " + item.getXSIType() + " experiments in project " + project + ".");
                }
            } catch (InsufficientPrivilegesException e) {
                throw e;
            } catch (Exception e) {
                throw new DatasetObjectException("An error occurred trying to test whether the user " + username + " can create new items of type " + item.getXSIType() + " in the project " + project, e);
            }
        } else {
            if (!testExperimentId(id)) {
                if (StringUtils.isBlank(id)) {
                    throw new DataFormatException("The submitted experiment has no ID. This call is for updating existing objects only.");
                }
                throw new NotFoundException("No experiment with the ID " + id + " was found. Can't update something that doesn't exist.");
            }
            if (StringUtils.isAnyBlank(project, label)) {
                throw new DataFormatException("Data objects must have both a project and label specified");
            }
            if (!testExperimentProjectAndLabel(project, label) && !testExperimentProjectAndLabel(project, id)) {
                throw new NotFoundException("The experiment with ID " + id + " does not exist in the project " + project + ". This method currently doesn't support sharing.");
            }
            try {
                if (!_permissions.canEdit(user, item)) {
                    throw new InsufficientPrivilegesException("The user " + username + " has insufficient privileges to edit " + xsiType + " experiments in project " + project + ".");
                }
            } catch (InsufficientPrivilegesException e) {
                throw e;
            } catch (Exception e) {
                throw new DatasetObjectException("An error occurred trying to test whether the user " + username + " can edit items of type " + item.getXSIType() + " in the project " + project, e);
            }
        }

        try {
            final ValidationResults validation = item.validate();
            if (validation != null && !validation.isValid()) {
                throw new DataFormatException(validation.toFullString());
            }
        } catch (Exception e) {
            if (e instanceof DataFormatException) {
                throw (DataFormatException) e;
            }
            throw new DatasetObjectException("An error occurred trying to validate an item of type " + item.getXSIType() + " in the project " + project + " for user " + username, e);
        }

        final EventDetails event = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, EventUtils.getAddModifyAction(item.getXSIType(), isCreate));
        if (eventDetails.containsKey("action")) {
            event.setAction(eventDetails.get("action"));
        }
        if (eventDetails.containsKey("comment")) {
            event.setComment(eventDetails.get("comment"));
        }
        if (eventDetails.containsKey("reason")) {
            event.setReason(eventDetails.get("reason"));
        }

        final PersistentWorkflowI workflow = getWorkflow(isCreate, user, item, event);
        final EventMetaI          meta     = workflow.buildEvent();

        try {
            if (!SaveItemHelper.authorizedSave(item, user, false, true, meta)) {
                throw new Exception("Save method returned false when user " + username + " tried to save object " + item.getIDValue() + " of type " + item.getXSIType());
            }
        } catch (IllegalArgumentException e) {
            throw new DataFormatException("User " + username + " failed to create new object of type " + item.getXSIType() + " in project " + project + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("The {} operation failed for a new item of type {} in the project {} as requested by {}", isCreate ? "create" : "update", item.getXSIType(), project, username, e);
            try {
                WorkflowUtils.fail(workflow, meta);
            } catch (Exception ex) {
                log.error("An error occurred trying to fail the workflow with ID {} for object {} of type {}", workflow.getId(), id, xsiType);
            }
            return null;
        }

        final XFTItem xftItem = item.getItem();

        try {
            if (xftItem.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME) || xftItem.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME) || xftItem.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                XDAT.triggerXftItemEvent(xftItem, isCreate ? XftItemEvent.CREATE : XftItemEvent.UPDATE);
            }
            Users.clearCache(user);
            MaterializedView.deleteByUser(user);
            if (!operations.isEmpty()) {
                final Integer eventId = (Integer) meta.getEventId();
                if (!operations.isEmpty() && !_permissions.canActivate(user, item)) {
                    WorkflowUtils.fail(workflow, workflow.buildEvent());
                    throw new InsufficientPrivilegesException("The user " + user.getUsername() + " has insufficient privileges to perform requested post-save operations on " + item.getXSIType() + " experiments in project " + item.getStringProperty("project") + ": " + StringUtils.join(operations, ", "));
                }
                if (operations.contains(PostSaveOperation.Activate)) {
                    activateItem(user, item, eventId);
                }
                if (operations.contains(PostSaveOperation.Quarantine)) {
                    quarantineItem(user, item, eventId);
                }
                if (operations.contains(PostSaveOperation.Lock)) {
                    lockItem(user, item, eventId);
                }
                if (operations.contains(PostSaveOperation.Unlock)) {
                    unlockItem(user, item, eventId);
                }
                if (operations.contains(PostSaveOperation.Obsolete)) {
                    obsoleteItem(user, item, eventId);
                }
            }
            WorkflowUtils.complete(workflow, meta);
            return getExperiment(user, xftItem.getIDValue());
        } catch (InsufficientPrivilegesException e) {
            throw e;
        } catch (Exception e) {
            try {
                WorkflowUtils.fail(workflow, meta);
            } catch (Exception ex) {
                log.error("An error occurred trying to fail the workflow with ID {} for object {} of type {}", workflow.getId(), id, xsiType);
            }
            throw new DatasetObjectException("An error occurred trying to " + (isCreate ? "create" : "modify") + " item " + id + " of type " + xsiType, e);
        }
    }

    private PersistentWorkflowI getWorkflow(final boolean isCreate, final UserI user, final XFTItem item, final EventDetails event) {
        final PersistentWorkflowI workflow;
        try {
            workflow = PersistentWorkflowUtils.buildOpenWorkflow(user, item, event);
        } catch (PersistentWorkflowUtils.ActionNameAbsent e) {
            throw new DatasetObjectException(formatEventRequirementAbsentMessage("action", isCreate, item.getXSIType()), e);
        } catch (PersistentWorkflowUtils.JustificationAbsent e) {
            throw new DatasetObjectException(formatEventRequirementAbsentMessage("justification", isCreate, item.getXSIType()), e);
        } catch (PersistentWorkflowUtils.IDAbsent e) {
            throw new DatasetObjectException(formatEventRequirementAbsentMessage("ID", isCreate, item.getXSIType()), e);
        }
        return workflow;
    }

    private void deleteImpl(final UserI user, final T object) throws InsufficientPrivilegesException {
        final String  username = user.getUsername();
        final XFTItem item     = object.getItem();
        final String  project  = object.getProject();
        final String  xsiType  = object.getXSIType();
        final String  id       = object.getId();

        try {
            if (!_permissions.canDelete(user, item)) {
                throw new InsufficientPrivilegesException("The user " + username + " has insufficient privileges to delete " + xsiType + " experiments in project " + project + ".");
            }
            SaveItemHelper.authorizedDelete(object.getItem(), user, EventUtils.DEFAULT_EVENT(user, "Deleted dataset object " + id));
        } catch (InsufficientPrivilegesException e) {
            throw e;
        } catch (Exception e) {
            throw new DatasetObjectException("An error occurred when user " + username + " tried to delete the object with " + id + " and type " + xsiType + " in the project " + project, e);
        }
    }

    private void activateItem(final UserI user, final ItemI item, final Integer eventId) throws Exception {
        final EventDetails        event    = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, "Activated");
        final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(eventId, user, item.getItem(), event);
        try {
            item.activate(user);
            WorkflowUtils.complete(workflow, workflow.buildEvent());
        } catch (Exception e) {
            log.error("An error occurred trying to activate the {} item with ID {}", item.getXSIType(), item.getItem().getIDValue(), e);
            WorkflowUtils.fail(workflow, workflow.buildEvent());
        }
    }

    private void quarantineItem(final UserI user, final ItemI item, final Integer eventId) throws Exception {
        final EventDetails        event    = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, "Quarantined");
        final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(eventId, user, item.getItem(), event);
        try {
            item.quarantine(user);
            WorkflowUtils.complete(workflow, workflow.buildEvent());
        } catch (Exception e) {
            log.error("An error occurred trying to quarantine the {} item with ID {}", item.getXSIType(), item.getItem().getIDValue(), e);
            WorkflowUtils.fail(workflow, workflow.buildEvent());
        }
    }

    private void lockItem(final UserI user, final ItemI item, final Integer eventId) throws Exception {
        final EventDetails        event    = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, "Locked");
        final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(eventId, user, item.getItem(), event);
        try {
            item.lock(user);
            WorkflowUtils.complete(workflow, workflow.buildEvent());
        } catch (Exception e) {
            log.error("An error occurred trying to lock the {} item with ID {}", item.getXSIType(), item.getItem().getIDValue(), e);
            WorkflowUtils.fail(workflow, workflow.buildEvent());
        }
    }

    private void unlockItem(final UserI user, final ItemI item, final Integer eventId) throws Exception {
        final EventDetails        event    = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, "Unlocked");
        final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(eventId, user, item.getItem(), event);
        try {
            item.activate(user);
            WorkflowUtils.complete(workflow, workflow.buildEvent());
        } catch (Exception e) {
            log.error("An error occurred trying to unlock the {} item with ID {}", item.getXSIType(), item.getItem().getIDValue(), e);
            WorkflowUtils.fail(workflow, workflow.buildEvent());
        }
    }

    private void obsoleteItem(final UserI user, final ItemI item, final Integer eventId) throws Exception {
        final EventDetails        event    = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, "Obsoleted");
        final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(eventId, user, item.getItem(), event);
        try {
            item.getItem().setStatus(user, ViewManager.OBSOLETE);
            WorkflowUtils.complete(workflow, workflow.buildEvent());
        } catch (Exception e) {
            log.error("An error occurred trying to unlock the {} item with ID {}", item.getXSIType(), item.getItem().getIDValue(), e);
            WorkflowUtils.fail(workflow, workflow.buildEvent());
        }
    }

    private static String formatEventRequirementAbsentMessage(final String requirement, final boolean isCreate, final String xsiType) {
        return String.format(ERROR_EVENT_REQUIREMENT_ABSENT, requirement, isCreate ? "creating" : "modifying", xsiType);
    }

    private static final String PARAM_EXPERIMENT                    = "experiment";
    private static final String PARAM_PROJECT                       = "project";
    private static final String PARAM_ID_OR_LABEL                   = "label";
    private static final String PARAM_DATA_TYPE                     = "dataType";
    private static final String QUERY_EXISTS                        = "SELECT EXISTS(%s)";
    private static final String QUERY_PROJECT_ID                    = "SELECT id FROM xnat_projectdata WHERE id = :" + PARAM_PROJECT;
    private static final String QUERY_EXPT_ID                       = "SELECT id FROM xnat_experimentdata WHERE id = :" + PARAM_EXPERIMENT;
    private static final String QUERY_EXPT_ID_WITH_DATA_TYPE        = "SELECT id FROM xnat_experimentdata e LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id WHERE e.id = :" + PARAM_EXPERIMENT + " AND m.element_name = :" + PARAM_DATA_TYPE;
    private static final String QUERY_VALIDATE_EXPT_DATA_TYPE       = "SELECT e.element_name FROM xnat_experimentdata x LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id WHERE x.id = :" + PARAM_EXPERIMENT;
    private static final String QUERY_EXPT_PROJECT_AND_LABEL        = "SELECT x.id FROM xnat_experimentdata x LEFT JOIN xnat_experimentdata_share s ON x.id = s.sharing_share_xnat_experimentda_id WHERE (x.project = :" + PARAM_PROJECT + " AND (x.id =  :" + PARAM_ID_OR_LABEL + " OR x.label =  :" + PARAM_ID_OR_LABEL + ")) OR (s.project = :" + PARAM_PROJECT + " AND (x.id =  :" + PARAM_ID_OR_LABEL + " OR s.label =  :" + PARAM_ID_OR_LABEL + "))";
    private static final String QUERY_PROJECT_ID_EXISTS             = String.format(QUERY_EXISTS, QUERY_PROJECT_ID);
    private static final String QUERY_EXPT_ID_EXISTS                = String.format(QUERY_EXISTS, QUERY_EXPT_ID);
    private static final String QUERY_EXPT_ID_WITH_DATA_TYPE_EXISTS = String.format(QUERY_EXISTS, QUERY_EXPT_ID_WITH_DATA_TYPE);
    private static final String QUERY_EXPT_PROJECT_AND_LABEL_EXISTS = String.format(QUERY_EXISTS, QUERY_EXPT_PROJECT_AND_LABEL);
    private static final String ERROR_EVENT_REQUIREMENT_ABSENT      = "No %1$s specified while %2$s an object of type %3$s, but %1$s is required";

    private final PermissionsServiceI        _permissions;
    private final NamedParameterJdbcTemplate _template;
    private final String                     _xsiType;
    private final String                     _xsiXmlPath;
    private final String                     _projectXmlPath;
    private final Class<? extends T>         _dataType;
    private final Method                     _factoryMethod;

}

package org.nrg.xnatx.plugins.collection.services.impl.xft;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
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
import org.nrg.xnatx.plugins.collection.services.DatasetObjectService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Slf4j
public abstract class AbstractXftDatasetObjectService<T extends XnatExperimentdata> implements DatasetObjectService<T> {
    protected AbstractXftDatasetObjectService(final PermissionsServiceI service, final NamedParameterJdbcTemplate template) {
        _service = service;
        _template = template;
        final Class<T> dataType = Reflection.getParameterizedTypeForClass(getClass());
        try {
            _xsiType = (String) dataType.getField("SCHEMA_ELEMENT_NAME").get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Got an error trying to get the schema element name for the data type: " + dataType.getName(), e);
        }
        _xsiXmlPath = _xsiType + "/xsiType";
        _projectXmlPath = _xsiType + "/project";
    }

    @Override
    public T create(final @Nonnull UserI user, final @Nonnull T object) throws InsufficientPrivilegesException, ResourceAlreadyExistsException, DataFormatException, NotFoundException {
        return createOrUpdateImpl(true, user, object);
    }

    @Override
    public List<T> findAll(final UserI user) {
        //noinspection unchecked
        return (List<T>) XnatExperimentdata.getXnatExperimentdatasByField(_xsiXmlPath, _xsiType, user, false);
    }

    @Override
    public List<T> findByProject(final UserI user, final String project) throws NotFoundException {
        if (!testProject(project)) {
            throw new NotFoundException("No project with ID " + project + " can be found.");
        }
        //noinspection unchecked
        return (List<T>) XnatExperimentdata.getXnatExperimentdatasByField(_projectXmlPath, project, user, false);
    }

    @Override
    public T findById(final UserI user, final String id) throws NotFoundException {
        //noinspection unchecked
        return (T) XnatExperimentdata.getXnatExperimentdatasById(id, user, false);
    }

    @Override
    public T findByProjectAndIdOrLabel(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException {
        try {
            //noinspection unchecked
            return (T) XnatExperimentdata.getXnatExperimentdatasById(getIdForProjectAndLabel(projectId, idOrLabel), user, false);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Could not find an experiment with ID or label " + idOrLabel + " in the project " + projectId);
        }
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

    protected String getIdForProjectAndLabel(final String projectId, final String idOrLabel) {
        return _template.queryForObject(QUERY_EXPT_PROJECT_AND_LABEL, new MapSqlParameterSource(PARAM_PROJECT, projectId).addValue(PARAM_ID_OR_LABEL, idOrLabel), String.class);
    }

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
        return createOrUpdateImpl(isCreate, user, item, Collections.<PostSaveOperation>emptyList());
    }

    private T createOrUpdateImpl(final boolean isCreate, final UserI user, final T object, final List<PostSaveOperation> operations) throws InsufficientPrivilegesException, ResourceAlreadyExistsException, DataFormatException, NotFoundException {
        final String  username = user.getUsername();
        final XFTItem item     = object.getItem();
        final String  project  = object.getProject();
        final String  id       = object.getId();
        final String  xsiType  = object.getXSIType();
        final String  label    = object.getLabel();

        try {
            final EventDetails        event    = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, EventUtils.getAddModifyAction(item.getXSIType(), isCreate));
            final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(null, user, item.getItem(), event);
            final EventMetaI          meta     = workflow.buildEvent();

            try {
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
                    if (!_service.canCreate(user, item)) {
                        throw new InsufficientPrivilegesException("The user " + username + " has insufficient privileges to create " + item.getXSIType() + " experiments in project " + project + ".");
                    }
                    if (StringUtils.isBlank(id)) {
                        object.setId(XnatExperimentdata.CreateNewID());
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
                    if (!_service.canEdit(user, item)) {
                        throw new InsufficientPrivilegesException("The user " + username + " has insufficient privileges to edit " + xsiType + " experiments in project " + project + ".");
                    }
                }

                final ValidationResults validation = item.validate();
                if (validation != null && !validation.isValid()) {
                    throw new DataFormatException(validation.toFullString());
                }

                if (!SaveItemHelper.authorizedSave(item, user, false, true, meta)) {
                    log.error("The {} operation failed for a new item of type {} in the project {} as requested by {}", isCreate ? "create" : "update", item.getXSIType(), project, username);
                    return null;
                }
                final XFTItem xftItem = item.getItem();
                if (xftItem.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME) || xftItem.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME) || xftItem.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                    XDAT.triggerXftItemEvent(xftItem, isCreate ? XftItemEvent.CREATE : XftItemEvent.UPDATE);
                }
                Users.clearCache(user);
                MaterializedView.deleteByUser(user);
                if (!operations.isEmpty()) {
                    final Integer eventId = (Integer) meta.getEventId();
                    if (!operations.isEmpty() && !_service.canActivate(user, item)) {
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
                //noinspection unchecked
                return (T) XnatExperimentdata.getXnatExperimentdatasById(object.getId(), user, false);
            } catch (InsufficientPrivilegesException | ResourceAlreadyExistsException | DataFormatException | NotFoundException e) {
                throw e;
            } catch (Exception e) {
                WorkflowUtils.fail(workflow, meta);
                throw e;
            }
        } catch (InsufficientPrivilegesException | ResourceAlreadyExistsException | DataFormatException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("An error occurred trying to test whether the user {} can create new items of type {} in the project {}", username, item.getXSIType(), project, e);
            throw new RuntimeException(e);
        }
    }

    private void deleteImpl(final UserI user, final T object) throws InsufficientPrivilegesException {
        final String  username = user.getUsername();
        final XFTItem item     = object.getItem();
        final String  project  = object.getProject();
        final String  xsiType  = object.getXSIType();

        try {
            if (!_service.canDelete(user, item)) {
                throw new InsufficientPrivilegesException("The user " + username + " has insufficient privileges to delete " + xsiType + " experiments in project " + project + ".");
            }
        } catch (InsufficientPrivilegesException e) {
            throw e;
        } catch (Exception e) {
            log.error("An error occurred trying to test whether the user {} can create new items of type {} in the project {}", username, xsiType, project, e);
            throw new RuntimeException(e);
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

    private static final String PARAM_EXPERIMENT                    = "experiment";
    private static final String PARAM_PROJECT                       = "project";
    private static final String PARAM_ID_OR_LABEL                   = "label";
    private static final String QUERY_EXISTS                        = "SELECT EXISTS(%s)";
    private static final String QUERY_PROJECT_ID                    = "SELECT id FROM xnat_projectdata WHERE id = :" + PARAM_PROJECT;
    private static final String QUERY_EXPT_ID                       = "SELECT id FROM xnat_experimentdata WHERE id = :" + PARAM_EXPERIMENT;
    private static final String QUERY_EXPT_PROJECT_AND_LABEL        = "SELECT x.id FROM xnat_experimentdata x LEFT JOIN xnat_experimentdata_share s ON x.id = s.sharing_share_xnat_experimentda_id WHERE (x.project = :" + PARAM_PROJECT + " AND (x.id =  :" + PARAM_ID_OR_LABEL + " OR x.label =  :" + PARAM_ID_OR_LABEL + ")) OR (s.project = :" + PARAM_PROJECT + " AND (s.id =  :" + PARAM_ID_OR_LABEL + " OR s.label =  :" + PARAM_ID_OR_LABEL + "))";
    private static final String QUERY_PROJECT_ID_EXISTS             = String.format(QUERY_EXISTS, QUERY_PROJECT_ID);
    private static final String QUERY_EXPT_ID_EXISTS                = String.format(QUERY_EXISTS, QUERY_EXPT_ID);
    private static final String QUERY_EXPT_PROJECT_AND_LABEL_EXISTS = String.format(QUERY_EXISTS, QUERY_EXPT_PROJECT_AND_LABEL);

    private final PermissionsServiceI        _service;
    private final NamedParameterJdbcTemplate _template;
    private final String                     _xsiType;
    private final String                     _xsiXmlPath;
    private final String                     _projectXmlPath;
}

package org.nrg.xnatx.plugins.collection.rest.permissions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xapi.authorization.AbstractXapiAuthorization;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Slf4j
public abstract class DataSetPermissions<T extends XnatExperimentdataI> extends AbstractXapiAuthorization {
    @Autowired
    protected DataSetPermissions(final PermissionsServiceI permissions, final NamedParameterJdbcTemplate template) {
        _permissions = permissions;
        _template = template;
        _dataType = Reflection.getParameterizedTypeForClass(getClass());
        try {
            _xmlPath = _dataType.getField("SCHEMA_ELEMENT_NAME").get(null) + "/project";
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Got an error trying to get the schema element name for the data type: " + _dataType.getName(), e);
        }
    }

    /**
     * Indicates the action for the implementing permission check.
     *
     * @return The action for the implementing permission check.
     */
    protected abstract String getAction();

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) {
        final List<? extends XnatExperimentdataI> experiments = getParameters(joinPoint, _dataType);
        for (final XnatExperimentdataI experiment : experiments) {
            if (StringUtils.isNoneBlank(experiment.getProject(), experiment.getLabel()) && forbidden(user, experiment.getProject(), _xmlPath, experiment.getLabel())) {
                return false;
            }
        }
        if (_dataType.equals(SetsCollection.class)) {
            final List<? extends XnatExperimentdataI> definitions = getParameters(joinPoint, SetsDefinition.class);
            for (final XnatExperimentdataI experiment : definitions) {
                if (StringUtils.isNoneBlank(experiment.getProject(), experiment.getLabel()) && forbidden(user, experiment.getProject(), _xmlPath, experiment.getLabel())) {
                    return false;
                }
            }
        }
        final Map<String, Integer> parameters = getParametersOfType(((MethodSignature) joinPoint.getSignature()), String.class);
        if (parameters.containsKey("id")) {
            final int                index      = parameters.get("id");
            final XnatExperimentdata experiment = XnatExperimentdata.getXnatExperimentdatasById(joinPoint.getArgs()[index], user, false);
            return !StringUtils.isNoneBlank(experiment.getProject(), experiment.getLabel()) || !forbidden(user, experiment.getProject(), _xmlPath, experiment.getLabel());
        }
        final boolean hasProject   = parameters.containsKey("project");
        final boolean hasProjectId = parameters.containsKey("projectId");
        final boolean hasIdOrLabel = parameters.containsKey("idOrLabel");
        final boolean hasLabel     = parameters.containsKey("label");
        if ((hasProject || hasProjectId) && (hasIdOrLabel || hasLabel)) {
            final int                projectIndex = parameters.get(hasProject ? "project" : "projectId");
            final int                labelIndex   = parameters.get(hasProject ? "idOrLabel" : "label");
            final String id = _template.queryForObject("SELECT id FROM xnat_experimentdata WHERE project = :project AND (id = :idOrLabel OR label = :idOrLabel)", new MapSqlParameterSource("project", joinPoint.getArgs()[projectIndex]).addValue("idOrLabel", joinPoint.getArgs()[labelIndex]), String.class);
            final XnatExperimentdata experiment   = XnatExperimentdata.getXnatExperimentdatasById(id, user, false);
            return !StringUtils.isNoneBlank(experiment.getProject(), experiment.getLabel()) || !forbidden(user, experiment.getProject(), _xmlPath, experiment.getLabel());
        }
        return true;
    }

    protected Map<String, Integer> getParametersOfType(final MethodSignature signature, final Class<?> type) {
        final Map<String, Integer> names = new HashMap<>();
        final int                  size  = signature.getParameterNames().length;
        for (int index = 0; index < size; index++) {
            if (type.isAssignableFrom(signature.getParameterTypes()[index])) {
                names.put(signature.getParameterNames()[index], index);
            }
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean considerGuests() {
        return false;
    }

    private boolean forbidden(final UserI user, final String projectId, final String xmlPath, final String name) {
        try {
            if (!_permissions.can(user, xmlPath, projectId, getAction())) {
                log.info("The user {} wanted to {} a data collection \"{}\" in the project {} but doesn't have adequate permission on collections in that project.", user.getUsername(), getAction(), name, projectId);
                return true;
            }
        } catch (Exception e) {
            log.error("An error occurred trying to check for {}} permissions on the project {} for user {}. Stopping permissions check for this request.", getAction(), projectId, user.getUsername(), e);
            return true;
        }
        return false;
    }

    private final PermissionsServiceI        _permissions;
    private final NamedParameterJdbcTemplate _template;
    private final Class<T>                   _dataType;
    private final String                     _xmlPath;
}

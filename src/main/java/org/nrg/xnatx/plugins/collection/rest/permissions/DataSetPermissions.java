package org.nrg.xnatx.plugins.collection.rest.permissions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xapi.authorization.AbstractXapiAuthorization;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        final Pair<String, String> experiment;
        final Map<String, Integer> parameters   = getParametersOfType(((MethodSignature) joinPoint.getSignature()), String.class);
        final boolean              hasProject   = parameters.containsKey("project");
        final boolean              hasProjectId = parameters.containsKey("projectId");
        final boolean              hasIdOrLabel = parameters.containsKey("idOrLabel");
        final boolean              hasLabel     = parameters.containsKey("label");
        if (parameters.containsKey("id")) {
            experiment = _template.queryForObject(QUERY_EXPT_BY_ID, new MapSqlParameterSource("experimentId", joinPoint.getArgs()[parameters.get("id")]), EXPT_MAPPER_ID_PROJECT_LABEL);
        } else if (hasProject || hasProjectId) {
            final String projectId = (String) joinPoint.getArgs()[parameters.get(hasProject ? "project" : "projectId")];
            if (hasIdOrLabel || hasLabel) {
                final Object idOrLabel = joinPoint.getArgs()[parameters.get(hasIdOrLabel ? "idOrLabel" : "label")];
                experiment = _template.queryForObject(QUERY_EXPT_BY_PROJECT_AND_ID_OR_LABEL, new MapSqlParameterSource("project", projectId).addValue("idOrLabel", idOrLabel), EXPT_MAPPER_ID_PROJECT_LABEL);
            } else {
                experiment = Pair.of(projectId, null);
            }
        } else {
            experiment = null;
        }
        return experiment != null && (StringUtils.isNotBlank(experiment.getValue()) ? !forbidden(user, experiment.getKey(), _xmlPath, experiment.getValue()) : !forbidden(user, experiment.getKey(), _xmlPath));
    }

    @SuppressWarnings("SameParameterValue")
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

    private boolean forbidden(final UserI user, final String projectId, final String xmlPath) {
        return forbidden(user, projectId, xmlPath, null);
    }

    private boolean forbidden(final UserI user, final String projectId, final String xmlPath, final String name) {
        try {
            if (!_permissions.can(user, xmlPath, projectId, getAction())) {
                if (StringUtils.isNotBlank(name)) {
                    log.info("The user {} wanted to {} data collection \"{}\" in the project {} but doesn't have adequate permission on collections in that project.", user.getUsername(), getAction(), name, projectId);
                } else {
                    log.info("The user {} wanted to {} data collections in the project {} but doesn't have adequate permission on collections in that project.", user.getUsername(), getAction(), projectId);
                }
                return true;
            }
        } catch (Exception e) {
            log.error("An error occurred trying to check for {}} permissions on the project {} for user {}. Stopping permissions check for this request.", getAction(), projectId, user.getUsername(), e);
            return true;
        }
        return false;
    }

    private static final RowMapper<Pair<String, String>> EXPT_MAPPER_ID_PROJECT_LABEL          = (results, rowNum) -> Pair.of(results.getString("project"), results.getString("label"));
    private static final String                          QUERY_EXPT_BY_PROJECT_AND_ID_OR_LABEL = "SELECT project, label FROM xnat_experimentdata WHERE project = :project AND (id = :idOrLabel OR label = :idOrLabel)";
    private static final String                          QUERY_EXPT_BY_ID                      = "SELECT project, label FROM xnat_experimentdata WHERE id = :experimentId";

    private final PermissionsServiceI        _permissions;
    private final NamedParameterJdbcTemplate _template;
    private final Class<T>                   _dataType;
    private final String                     _xmlPath;
}

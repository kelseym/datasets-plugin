package org.nrg.xnatx.plugins.collection.rest.permissions;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xapi.authorization.AbstractXapiAuthorization;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class DataSetPermissions<T extends XnatExperimentdataI> extends AbstractXapiAuthorization {
    @Autowired
    protected DataSetPermissions(final PermissionsServiceI permissions) {
        _permissions = permissions;
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
        final List<? extends XnatExperimentdataI> parameters = getParameters(joinPoint, _dataType);
        for (final XnatExperimentdataI experiment : parameters) {
            if (forbidden(user, experiment.getProject(), _xmlPath, experiment.getLabel())) {
                return false;
            }
        }
        return true;
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

    private final PermissionsServiceI _permissions;
    private final Class<T>            _dataType;
    private final String              _xmlPath;
}

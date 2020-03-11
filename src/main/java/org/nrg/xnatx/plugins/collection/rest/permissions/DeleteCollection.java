package org.nrg.xnatx.plugins.collection.rest.permissions;

import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeleteCollection extends DataSetPermissions<SetsCollection> {
    @Autowired
    protected DeleteCollection(final PermissionsServiceI permissions, final NamedParameterJdbcTemplate template) {
        super(permissions, template);
    }

    @Override
    protected String getAction() {
        return SecurityManager.DELETE;
    }
}

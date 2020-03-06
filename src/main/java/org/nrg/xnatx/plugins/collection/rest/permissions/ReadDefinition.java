package org.nrg.xnatx.plugins.collection.rest.permissions;

import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReadDefinition extends DataSetPermissions<SetsDefinition> {
    @Autowired
    protected ReadDefinition(final PermissionsServiceI permissions) {
        super(permissions);
    }

    @Override
    protected String getAction() {
        return SecurityManager.READ;
    }
}

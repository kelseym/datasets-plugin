package org.nrg.xnatx.plugins.collection.rest.permissions;

import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EditCollection extends DataSetPermissions<SetsCollection> {
    @Autowired
    protected EditCollection(final PermissionsServiceI permissions) {
        super(permissions);
    }

    @Override
    protected String getAction() {
        return SecurityManager.EDIT;
    }
}

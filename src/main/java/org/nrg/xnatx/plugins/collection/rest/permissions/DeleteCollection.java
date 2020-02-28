package org.nrg.xnatx.plugins.collection.rest.permissions;

import org.nrg.xdat.model.XnatxDatacollectionI;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteCollection extends DataSetPermissions<XnatxDatacollectionI> {
    @Autowired
    protected DeleteCollection(final PermissionsServiceI permissions) {
        super(permissions);
    }

    @Override
    protected String getAction() {
        return SecurityManager.DELETE;
    }
}

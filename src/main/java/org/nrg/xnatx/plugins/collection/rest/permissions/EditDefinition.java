package org.nrg.xnatx.plugins.collection.rest.permissions;

import org.nrg.xdat.model.XnatxDatacollectiondefinitionI;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EditDefinition extends DataSetPermissions<XnatxDatacollectiondefinitionI> {
    @Autowired
    protected EditDefinition(final PermissionsServiceI permissions) {
        super(permissions);
    }

    @Override
    protected String getAction() {
        return SecurityManager.EDIT;
    }
}

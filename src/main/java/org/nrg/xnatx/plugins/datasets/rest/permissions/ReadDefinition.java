/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.rest.permissions.ReadDefinition
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.rest.permissions;

import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReadDefinition extends DataSetPermissions<SetsDefinition> {
    @Autowired
    protected ReadDefinition(final PermissionsServiceI permissions, final NamedParameterJdbcTemplate template) {
        super(permissions, template);
    }

    @Override
    protected String getAction() {
        return SecurityManager.READ;
    }
}

package org.nrg.xnatx.plugins.collection.services.impl.xft;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xnatx.plugins.collection.services.DatasetDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class XftDatasetDefinitionService extends AbstractXftDatasetObjectService<SetsDefinition> implements DatasetDefinitionService {
    @Autowired
    public XftDatasetDefinitionService(final PermissionsServiceI service, final NamedParameterJdbcTemplate template) {
        super(service, template);
    }
}

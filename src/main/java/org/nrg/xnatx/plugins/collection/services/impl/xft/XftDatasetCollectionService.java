package org.nrg.xnatx.plugins.collection.services.impl.xft;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.services.DatasetCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class XftDatasetCollectionService extends AbstractXftDatasetObjectService<SetsCollection> implements DatasetCollectionService {
    @Autowired
    public XftDatasetCollectionService(final PermissionsServiceI service, final NamedParameterJdbcTemplate template) {
        super(service, template);
    }

    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final String id) throws NotFoundException {
        return null;
    }

    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException {
        return null;
    }
}

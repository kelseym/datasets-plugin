package org.nrg.xnatx.plugins.collection.services.impl.xft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
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
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final String projectId, final String idOrLabel) throws NotFoundException {
        return getResources(user, findByProjectAndIdOrLabel(user, projectId, idOrLabel));
    }

    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final String id) throws NotFoundException {
        return getResources(user, findById(user, id));
    }

    @Override
    public Map<String, List<Map<String, String>>> getResources(final UserI user, final SetsCollection collection) {
        final Map<String, Map<String, String>> resources = new HashMap<>();
        for (final XnatAbstractresourceI abstractResource : collection.getResources_resource()) {
            final XnatResource resource = (XnatResource) abstractResource;
            try {
                final XFTItem scan      = resource.getParent().getItem();
                final String  sessionId = scan.getParent().getItem().getIDValue();
                if (!resources.containsKey(sessionId)) {
                    resources.put(sessionId, new HashMap<String, String>());
                }
            } catch (XFTInitException | ElementNotFoundException e) {
                log.error("Got an exception, so sad", e);
            }
        }
        return null;
    }
}

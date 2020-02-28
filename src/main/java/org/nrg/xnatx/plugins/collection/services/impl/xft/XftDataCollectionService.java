package org.nrg.xnatx.plugins.collection.services.impl.xft;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.XnatxDatacollectionI;
import org.nrg.xdat.model.XnatxDatacollectiondefinitionI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.services.DataCollectionService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class XftDataCollectionService implements DataCollectionService {
    /**
     * {@inheritDoc}
     */
    @Override
    public XnatxDatacollectiondefinitionI create(final XnatxDatacollectiondefinitionI definition) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatxDatacollectionI create(final XnatxDatacollectionI collection) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends XnatxDatacollectiondefinitionI> findAllDefinitions() throws NotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatxDatacollectiondefinitionI findDefinitionById(final String id) throws NotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatxDatacollectiondefinitionI findDefinitionByProjectAndIdOrLabel(final String projectId, final String idOrLabel) throws NotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatxDatacollectionI resolve(final XnatxDatacollectiondefinitionI definition) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends XnatxDatacollectionI> findAllCollections() throws NotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<XnatxDatacollectiondefinitionI> findDefinitionsByProject(final String projectId) throws NotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatxDatacollectionI findCollectionById(final String id) throws NotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatxDatacollectionI findCollectionByProjectAndIdOrLabel(final String projectId, final String idOrLabel) throws NotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends XnatxDatacollectionI> findCollectionsByProject(final String projectId) throws NotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<Map<String, String>>> getCollectionResources(final UserI user, final String id) throws NotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<Map<String, String>>> getCollectionResources(final UserI user, final String projectId, final String definition) throws NotFoundException {
        return null;
    }
}

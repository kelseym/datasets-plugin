/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.services.impl.HibernateCollectionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.services.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnatx.plugins.collection.daos.DataCollectionDAO;
import org.nrg.xnatx.plugins.collection.entities.DataCollection;
import org.nrg.xnatx.plugins.collection.services.DataCollectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@link DataCollection} data objects in Hibernate.
 */
@Service
@Slf4j
public class HibernateDataCollectionService extends AbstractHibernateEntityService<DataCollection, DataCollectionDAO> implements DataCollectionService {
    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public DataCollection findById(final String collectionId) {
        return getDao().findByUniqueProperty("id", collectionId);
    }

    @Transactional
    @Override
    public List<DataCollection> getAllByProject(final String projectId) {
        return getDao().getCollectionsByProjectId(projectId);
    }

    @Transactional
    @Override
    public Map<String, List<Map<String, String>>> getCollectionResources(final UserI user, final long id) throws NotFoundException {
        final DataCollection collection  = get(id);
        final String         imagesTag   = StringUtils.defaultIfBlank(collection.getImageSeriesDescription(), DEFAULT_IMAGES_TAG);
        final String         labelsTag   = StringUtils.defaultIfBlank(collection.getLabelSeriesDescription(), DEFAULT_LABELS_TAG);
        final Path           projectPath = Paths.get(ArcSpecManager.GetInstance().getArchivePathForProject(collection.getProjectId()), "arc001");

        final Map<String, List<Map<String, String>>> resources = new HashMap<>();
        final List<Map<String, String>>              training  = getResourceList(collection.getTrainingExperiments(), user, projectPath, imagesTag, labelsTag);
        if (!training.isEmpty()) {
            resources.put("training", training);
        }
        final List<Map<String, String>> validation = getResourceList(collection.getValidationExperiments(), user, projectPath, imagesTag, labelsTag);
        if (!validation.isEmpty()) {
            resources.put("validation", validation);
        }
        final List<Map<String, String>> test = getResourceList(collection.getTestExperiments(), user, projectPath, imagesTag, labelsTag);
        if (!test.isEmpty()) {
            resources.put("test", test);
        }
        return resources;
    }

    private List<Map<String, String>> getResourceList(final List<String> experiments, final UserI user, final Path projectPath, final String imagesTag, final String labelsTag) {
        return Lists.newArrayList(Iterables.filter(Lists.transform(experiments, new Function<String, Map<String, String>>() {
            @Nullable
            @Override
            public Map<String, String> apply(final String experimentId) {
                return getImageAndLabelResources(user, projectPath, experimentId, imagesTag, labelsTag);
            }
        }), Predicates.notNull()));
    }

    @Nullable
    private Map<String, String> getImageAndLabelResources(final UserI user, final Path projectPath, final String experimentId, final String imagesTag, final String labelsTag) {
        final XnatImagesessiondata experiment = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(experimentId, user, false);
        if (experiment == null) {
            log.error("No experiment found with ID {}", experimentId);
            return null;
        }

        final String archivePath;
        try {
            archivePath = experiment.getArchivePath();
        } catch (BaseXnatExperimentdata.UnknownPrimaryProjectException e) {
            log.warn("Experiment {} doesn't have a primary project set apparently.", experimentId);
            return null;
        }

        final List<XnatImagescandata> scans     = experiment.getScans_scan();
        final XnatImagescandata       imageScan = Iterables.find(scans, new MatchingTypeClassOrDescription(imagesTag));
        if (imageScan == null) {
            log.warn("User {} requested image and label resources for experiment {}, but didn't find an image resource.", user.getUsername(), experimentId);
            return Collections.emptyMap();
        }

        final Map<String, String> resources = new HashMap<>();
        final Path                imagePath = getRelativePathForNiftiInScan(imageScan, archivePath, projectPath);
        resources.put("image", imagePath != null ? imagePath.toString() : "null");

        final XnatImagescandata labelScan = Iterables.find(scans, new MatchingTypeClassOrDescription(labelsTag));
        if (labelScan != null) {
            final Path labelPath = getRelativePathForNiftiInScan(imageScan, archivePath, projectPath);
            resources.put("image", labelPath != null ? labelPath.toString() : "null");
        }
        return resources;
    }

    @Nullable
    private Path getRelativePathForNiftiInScan(final XnatImagescandata scan, final String exptPath, final Path projectPath) {
        for (final XnatAbstractresourceI resI : scan.getFile()) {
            final XnatAbstractresource imagesRes = (XnatAbstractresource) resI;
            if (imagesRes != null) {
                final List<File> imagesFiles = imagesRes.getCorrespondingFiles(exptPath);
                if (imagesFiles != null) {
                    final File nifti = Iterables.find(imagesFiles, new Predicate<File>() {
                        @Override
                        public boolean apply(final File file) {
                            return StringUtils.endsWithAny(file.getPath(), ".nii", ".nii.gz");
                        }
                    });
                    if (nifti != null) {
                        return projectPath.relativize(Paths.get(nifti.getPath()));
                    }
                }
            }
        }
        return null;
    }

    private static class MatchingTypeClassOrDescription implements Predicate<XnatImagescandata> {
        MatchingTypeClassOrDescription(final String value) {
            _value = value;
        }

        @Override
        public boolean apply(final XnatImagescandata scan) {
            return StringUtils.equalsAny(_value, scan.getType(), scan.getSeriesClass(), scan.getSeriesDescription());
        }

        private final String _value;
    }

    private static final String DEFAULT_IMAGES_TAG = "IMAGES";
    private static final String DEFAULT_LABELS_TAG = "LABELS";
}

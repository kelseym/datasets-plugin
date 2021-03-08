/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.resolvers.DatasetCriterionResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.resolvers;

import java.util.List;
import java.util.Map;
import org.nrg.xdat.model.SetsCriterionI;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xft.security.UserI;

public interface DatasetCriterionResolver {
    String getResolverId();

    boolean handles(final SetsCriterionI criterion);

    List<Map<String, XnatAbstractresource>> resolve(final UserI user, final String project, final SetsCriterion criterion);

    Map<String, List<ProjectResourceReport>> report(final UserI user, final String project, final SetsCriterion criterion);
}

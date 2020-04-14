package org.nrg.xnatx.plugins.collection.resolvers;

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
}

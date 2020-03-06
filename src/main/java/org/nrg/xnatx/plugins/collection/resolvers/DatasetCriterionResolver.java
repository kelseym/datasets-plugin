package org.nrg.xnatx.plugins.collection.resolvers;

import java.util.List;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xft.security.UserI;

public interface DatasetCriterionResolver {
    String getResolverId();

    boolean handles(final SetsCriterion criterion);

    List<? extends XnatAbstractresource> resolve(final UserI user, final String project, final SetsCriterion criterion);
}

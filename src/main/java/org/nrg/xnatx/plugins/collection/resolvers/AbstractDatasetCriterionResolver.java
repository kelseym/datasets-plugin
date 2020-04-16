package org.nrg.xnatx.plugins.collection.resolvers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.SetsCriterionI;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetCriterionResolverException;

@Slf4j
public abstract class AbstractDatasetCriterionResolver implements DatasetCriterionResolver {
    protected AbstractDatasetCriterionResolver() {
        final Resolver resolver = getClass().getAnnotation(Resolver.class);
        if (resolver == null) {
            throw new DatasetCriterionResolverException("You must annotate criterion resolvers with the @Resolver annotation, but the resolver class {} isn't annotated.");
        }
        _resolverId = resolver.value();
    }

    protected abstract List<Map<String, XnatAbstractresource>> resolveImpl(final UserI user, final String project, final String payload);

    protected abstract Map<String, List<ProjectResourceReport>> reportImpl(final UserI user, final String project, final String criteria);

    @Override
    public String getResolverId() {
        return _resolverId;
    }

    @Override
    public boolean handles(final SetsCriterionI criterion) {
        return StringUtils.equalsIgnoreCase(_resolverId, criterion.getResolver());
    }

    @Override
    public List<Map<String, XnatAbstractresource>> resolve(final UserI user, final String project, final SetsCriterion criterion) {
        if (!StringUtils.equalsIgnoreCase(_resolverId, criterion.getResolver())) {
            log.info("Got criterion for resolver {} but I am {}, returning empty list.", criterion.getResolver(), _resolverId);
            return Collections.emptyList();
        }
        log.info("Got criterion for resolver {}, which I can handle, so passing to impl.", _resolverId);
        return resolveImpl(user, project, criterion.getPayload());
    }

    @Override
    public Map<String, List<ProjectResourceReport>> report(final UserI user, final String project, final SetsCriterion criterion) {
        if (!StringUtils.equalsIgnoreCase(_resolverId, criterion.getResolver())) {
            log.info("Got criterion for resolver {} but I am {}, returning empty list.", criterion.getResolver(), _resolverId);
            return Collections.emptyMap();
        }
        log.info("Got criterion for resolver {}, which I can handle, so passing to impl.", _resolverId);
        return reportImpl(user, project, criterion.getPayload());
    }

    private final String _resolverId;
}

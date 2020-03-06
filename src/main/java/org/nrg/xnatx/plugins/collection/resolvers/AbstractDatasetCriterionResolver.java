package org.nrg.xnatx.plugins.collection.resolvers;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    protected abstract List<? extends XnatAbstractresource> resolveImpl(final UserI user, final String project, final String payload);

    @Override
    public String getResolverId() {
        return _resolverId;
    }

    @Override
    public boolean handles(final SetsCriterion criterion) {
        return StringUtils.equalsIgnoreCase(_resolverId, criterion.getResolver());
    }

    @Override
    public List<? extends XnatAbstractresource> resolve(final UserI user, final String project, final SetsCriterion criterion) {
        if (!StringUtils.equalsIgnoreCase(_resolverId, criterion.getResolver())) {
            log.info("Got criterion for resolver {} but I am {}, returning empty list.", criterion.getResolver(), _resolverId);
            return Collections.emptyList();
        }
        log.info("Got criterion for resolver {}, which I can handle, so passing to impl.", _resolverId);
        return resolveImpl(user, project, criterion.getPayload());
    }

    private final String _resolverId;
}

package org.nrg.xnatx.plugins.collection.resolvers;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.Accessors;
import org.nrg.xdat.om.SetsCriterion;

@Getter
@Accessors(prefix = "_")
@Builder
public class ResolutionReport {
    @Getter
    @Accessors(prefix = "_")
    @Builder
    private static class ResourceAttributes {
        private final String _experimentId;
        private final String _experimentLabel;
        private final String _scanId;
        private final String _type;
        private final String _seriesDescription;
        private final String _seriesClass;
        private final String _resourceLabel;
        private final String _resourceContent;
        private final String _resourceFormat;
    }

    private final String _project;

    private final String _username;

    @Singular("criterion")
    private final List<SetsCriterion> _criteria;

    @Singular
    private final Map<String, List<ProjectResourceReport>> _resources;

    @Singular
    private final List<SessionReport> _sessions;
}

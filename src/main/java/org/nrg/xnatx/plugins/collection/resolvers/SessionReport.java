package org.nrg.xnatx.plugins.collection.resolvers;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

@Data
@Accessors(prefix = "_")
@Builder
public class SessionReport {
    @Data
    @Accessors(prefix = "_")
    @Builder
    public static class CriterionResult {
        private final String _file;

        private final String _check;

        private final String _matcher;

        @Singular
        private final List<String> _scans;
    }

    private final String _id;

    private final String _label;

    @Singular
    private final List<String> _scans;

    @Singular
    private final List<CriterionResult> _results;
}

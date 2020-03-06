package org.nrg.xnatx.plugins.collection.exceptions;

public class DatasetCriterionResolverException extends RuntimeException {
    public DatasetCriterionResolverException(final String message) {
        super(message);
    }

    public DatasetCriterionResolverException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

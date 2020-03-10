package org.nrg.xnatx.plugins.collection.exceptions;

public abstract class BaseDatasetException extends RuntimeException {
    protected BaseDatasetException(final String message) {
        super(message);
    }

    protected BaseDatasetException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

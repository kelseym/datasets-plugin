package org.nrg.xnatx.plugins.collection.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class DatasetCriterionResolverException extends BaseDatasetException {
    public DatasetCriterionResolverException(final String message) {
        super(message);
    }

    public DatasetCriterionResolverException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

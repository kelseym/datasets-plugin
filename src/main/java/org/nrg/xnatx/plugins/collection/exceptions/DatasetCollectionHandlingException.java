package org.nrg.xnatx.plugins.collection.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DatasetCollectionHandlingException extends BaseDatasetException {
    public DatasetCollectionHandlingException(final String message) {
        super(message);
    }

    public DatasetCollectionHandlingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

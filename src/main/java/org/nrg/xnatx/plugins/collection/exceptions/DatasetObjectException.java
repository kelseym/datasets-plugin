package org.nrg.xnatx.plugins.collection.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DatasetObjectException extends BaseDatasetException {
    public DatasetObjectException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

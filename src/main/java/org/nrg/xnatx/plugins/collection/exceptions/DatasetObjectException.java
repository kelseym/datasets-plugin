/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.exceptions.DatasetObjectException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DatasetObjectException extends BaseDatasetException {
    public DatasetObjectException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

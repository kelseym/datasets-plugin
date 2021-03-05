/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.exceptions.BaseDatasetException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.exceptions;

public abstract class BaseDatasetException extends RuntimeException {
    protected BaseDatasetException(final String message) {
        super(message);
    }

    protected BaseDatasetException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

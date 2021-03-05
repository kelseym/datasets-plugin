/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.services.impl.xft.PostSaveOperation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.services.impl.xft;

public enum PostSaveOperation {
    Activate,
    Quarantine,
    Lock,
    Unlock,
    Obsolete
}

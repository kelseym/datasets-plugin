package org.nrg.xnat.turbine.modules.screens;

import java.io.FileNotFoundException;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.turbine.modules.screens.SecureReport;

@SuppressWarnings("unused")
public class XDATScreen_report_sets_collection extends SecureReport {
    @Override
    public void finalProcessing(final RunData data, final Context context) {
        final SetsCollection om = new SetsCollection(item);
        context.put("om", om);
//        log.debug("Loaded SetsCollection with ID {} as context parameter 'om'.", om.getId());
        setDefaultTabs(DEFAULT_TABS);
        try {
            cacheTabs(context, SETS_COLLECTION_TABS);
        } catch (FileNotFoundException e) {
//            log.warn("An error occurred trying to cache tabs from the folder {}", SETS_COLLECTION_TABS);
        }
    }

    private static final String SETS_COLLECTION_TABS = "sets_collection/tabs";
    private static final String DEFAULT_TABS         = "sets_collection_summary_details";
}

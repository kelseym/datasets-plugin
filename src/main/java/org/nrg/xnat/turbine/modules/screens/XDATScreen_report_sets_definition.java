package org.nrg.xnat.turbine.modules.screens;

import java.io.FileNotFoundException;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xdat.turbine.modules.screens.SecureReport;

@SuppressWarnings("unused")
public class XDATScreen_report_sets_definition extends SecureReport {
    @Override
    public void finalProcessing(final RunData data, final Context context) {
        final SetsDefinition om = new SetsDefinition(item);
        context.put("om", om);
//        log.debug("Loaded SetsDefinition with ID {} as context parameter 'om'.", om.getId());
        setDefaultTabs(DEFAULT_TABS);
        try {
            cacheTabs(context, SETS_DEFINITION_TABS);
        } catch (FileNotFoundException e) {
//            log.warn("An error occurred trying to cache tabs from the folder {}", SETS_DEFINITION_TABS);
        }
    }

    private static final String SETS_DEFINITION_TABS = "sets_definition/tabs";
    private static final String DEFAULT_TABS         = "sets_definition_summary_details";
}

package org.nrg.xdat.om.base;

import java.util.Hashtable;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.base.auto.AutoSetsDefinition;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.exceptions.DatasetDefinitionHandlingException;

/**
 * Override of generated implementation of this class to provide JSON conversion and resource
 * management methods.
 */
@Slf4j
public abstract class BaseSetsDefinition extends AutoSetsDefinition {
    public BaseSetsDefinition(final ItemI item) {
        super(item);
    }

    public BaseSetsDefinition(final UserI user) {
        super(user);
    }

    /**
     * @deprecated Use BaseSetsDefinition(UserI user)
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public BaseSetsDefinition() {
        log.warn("This method is deprecated, you should use BaseSetsDefinition(UserI) instead.");
    }

    public BaseSetsDefinition(final Hashtable properties, final UserI user) {
        super(properties, user);
    }

    /**
     * Overrides base <b>preSave()</b> implementations to remove resource validation, since resources
     * for definitions are always outside of the definition itself.
     */
    @Override
    public void preSave() {
        checkIsValidID(getId());

        if (getPrimaryProject(false) == null) {
            throw new DatasetDefinitionHandlingException("Unable to identify project for:" + getProject());
        }

        try {
            checkUniqueLabel();
        } catch (Exception e) {
            throw new DatasetDefinitionHandlingException("The definition label " + getLabel() + " is not unique in the project " + getProject());
        }
    }
}

package org.marketcetera.core.module;

import java.io.IOException;

import org.marketcetera.api.attributes.ClassVersion;

/* $License$ */
/**
 * Subscribes to refresh requests from {@link ModuleManager#refresh()}.
 * Implementors will receive notifications of refresh requests before the
 * requests are completed.
 *
 * @author anshul@marketcetera.com
 * @version $Id: RefreshListener.java 16063 2012-01-31 18:21:55Z colin $
 * @since 1.0.0
 */
@ClassVersion("$Id: RefreshListener.java 16063 2012-01-31 18:21:55Z colin $") //$NON-NLS-1$
public interface RefreshListener {
    /**
     * Allows the implementor to determine if a refresh initiated by the
     * {@link ModuleManager#refresh()} should continue or not.
     * <p>
     * The method can be used to carry out operations before the module
     * manager refresh, for example, refreshing a classloader that is
     * being used by the module manager to load the modules.
     * <p>
     * The module manager skips the refresh operations if this method
     * returns false. This may be done, if for example the underlying
     * classloader finds no updates upon refresh, in which case it's not
     * useful for the module manager to refresh itself.
     *
     * @return true, if the module manager refresh itself, false if the
     * module manager should skip the refresh.
     *
     * @throws java.io.IOException if the refresh operation encountered
     * errors.
     */
    public boolean refresh() throws IOException;
}

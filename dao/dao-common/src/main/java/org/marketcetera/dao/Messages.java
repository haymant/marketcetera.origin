package org.marketcetera.dao;

import org.marketcetera.core.util.log.*;
import org.marketcetera.core.attributes.ClassVersion;

/* $License$ */

/**
 * Provides messages for the DAO package.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id: Messages.java 82316 2012-03-21 21:13:27Z colin $
 * @since $Release$
 */
@ClassVersion("$Id: Messages.java 82316 2012-03-21 21:13:27Z colin $")
public interface Messages
{
    /**
     * The message provider.
     */
    static final I18NMessageProvider PROVIDER = new I18NMessageProvider("dao", //$NON-NLS-1$
                                                                        Messages.class.getClassLoader());
    /**
     * The logger.
     */
    static final I18NLoggerProxy LOGGER = new I18NLoggerProxy(PROVIDER);
    // the messages
    static final I18NMessage1P CANNOT_ADD_AUTHORITY = new I18NMessage1P(LOGGER,
                                                                        "cannot_add_authority"); //$NON-NLS-1$
    static final I18NMessage1P CANNOT_ADD_GROUP = new I18NMessage1P(LOGGER,
                                                                    "cannot_add_group"); //$NON-NLS-1$
}

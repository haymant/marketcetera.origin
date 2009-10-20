package org.marketcetera.event;

import org.marketcetera.util.misc.ClassVersion;

/* $License$ */

/**
 * Represents a Bid or an Ask for an instrument on an exchange at a particular time.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since 0.5.0
 */
@ClassVersion("$Id$")
public interface QuoteEvent
        extends MarketDataEvent
{
    /**
     * Gets the action value indicating how this quote should be processed.
     *
     * @return a <code>QuoteAction</code> value
     */
    public QuoteAction getAction();
}

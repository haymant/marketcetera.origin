package org.marketcetera.core.event;

import java.math.BigDecimal;

import org.marketcetera.api.attributes.ClassVersion;

/* $License$ */

/**
 * Represents the set of statistics of a specific {@link org.marketcetera.core.trade.Option}.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id: OptionMarketstatEvent.java 16063 2012-01-31 18:21:55Z colin $
 * @since 2.0.0
 */
@ClassVersion("$Id: OptionMarketstatEvent.java 16063 2012-01-31 18:21:55Z colin $")
public interface OptionMarketstatEvent
        extends MarketstatEvent, OptionEvent
{
    /**
     * Gets the change in volume since the previous close.
     *
     * @return a <code>BigDecimal</code> value
     */
    public BigDecimal getVolumeChange();
    /**
     * Gets the change in interest since the previous close.
     *
     * @return a <code>BigDecimal</code> value
     */
    public BigDecimal getInterestChange();
}

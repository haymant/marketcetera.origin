package org.marketcetera.core.marketdata;

import java.util.Set;

import javax.management.MXBean;

import org.marketcetera.core.module.DisplayName;
import org.marketcetera.api.attributes.ClassVersion;

/* $License$ */

/**
 * <code>MXBean</code> interface for an {@link AbstractMarketDataModule} enumerating the <code>MXBean</code>
 * methods available.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id: AbstractMarketDataModuleMXBean.java 16063 2012-01-31 18:21:55Z colin $
 * @since 1.0.0
 */
@ClassVersion("$Id: AbstractMarketDataModuleMXBean.java 16063 2012-01-31 18:21:55Z colin $")
@MXBean(true)
@DisplayName("Management Interface for Marketdata Feeds")
public interface AbstractMarketDataModuleMXBean
{
    /**
     * Returns the status of the underlying feed.
     *
     * @return a <code>String</code> value
     */
    @DisplayName("The feed status for the market data feed")
    public String getFeedStatus();
    /**
     * Reconnects to the feed and resubmits all active queries.
     * 
     * <p>Regardless of the state of the feed, all existing queries will be canceled
     * and resubmitted.
     *
     * @throws IllegalArgumentException if the credentials to connect to the feed have not been specified properly
     * @throws UnsupportedOperationException if the feed does not support this behavior
     */
    @DisplayName("Causes the feed to resubmit existing queries")
    public void reconnect();
    /**
     * Gets the set of capabilities for this market data feed.
     *
     * @return a <code>Set&lt;Capability&gt;</code> value
     */
    @DisplayName("The set of capabilities for this feed")
    public Set<Capability> getCapabilities();
    /**
     * Gets the set of supported asset classes for this market data feed.
     *
     * @return a <code>Set&lt;AssetClass&gt;</code> value
     */
    @DisplayName("The set of supported asset classes for this feed")
    public Set<AssetClass> getAssetClasses();
}

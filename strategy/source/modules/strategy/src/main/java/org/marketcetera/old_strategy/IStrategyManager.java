package org.marketcetera.old_strategy;

import org.marketcetera.core.publisher.ISubscriber;
import org.marketcetera.marketdata.IMarketDataFeed;
import org.marketcetera.marketdata.IMarketDataFeedCredentials;
import org.marketcetera.marketdata.IMarketDataFeedToken;
import org.marketcetera.strategy.IStrategy;

/* $License$ */

/**
 * 
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @since $Release$
 * @version $Id: $
 */
public interface IStrategyManager
{
    /**
     * Registers an <code>IMarketDataFeed</code> as a market data provider.
     * 
     * <p>Once registered, all market data flowing through the given market data provider
     * will be passed to the <code>IStrategyManager</code> as well.
     * 
     * @param <C> an <code>IMarketDataFeedCredentials</code> type
     * @param <T> an <code>IMarketDataFeedToken</code> type
     * @param inDataFeed an <code>IMarketDataFeed&lt;T&gt;,&gt;C&gt;</code> value
     */
    public <C extends IMarketDataFeedCredentials, T extends IMarketDataFeedToken<C>> void registerMarketDataProvider(IMarketDataFeed<T,C> inDataFeed);
    /**
     * Cancels registration to an <code>IMarketDataFeed</code> as a market data provider.
     * 
     * @param <C> an <code>IMarketDataFeedCredentials</code> type
     * @param <T> an <code>IMarketDataFeedToken</code> type
     * @param inDataFeed an <code>IMarketDataFeed&lt;T&gt;,&gt;C&gt;</code> value
     */
    public <C extends IMarketDataFeedCredentials, T extends IMarketDataFeedToken<C>> void unregisterMarketDataProvider(IMarketDataFeed<T,C> inDataFeed);
    /**
     * Registers an <code>IStrategy</code> with the <code>IStrategyManager</code>.
     * 
     * <p>When registered, an <code>IStrategy</code> becomes active, receiving updates from the
     * <code>IStrategyManager</code> as appropriate.  An <code>IStrategy</code> does nothing
     * until registered.
     * 
     * <p>If the <code>IStrategy</code> is already registered, this method does nothing.
     * 
     * @param inStrategy an <code>IStrategy</code> value
     */
    public void registerStrategy(IStrategy inStrategy);
    /**
     * Cancels registration for an <code>IStrategy</code>.
     * 
     * <p>When the registration for an <code>IStrategy</code> occurs, the <code>IStrategy</code>
     * becomes inactive, receiving no further updates from the <code>IStrategyManager</code>.
     * 
     * <p>If the <code>IStrategy</code> is not currently registered, this method does nothing.
     * 
     * @param inStrategy an <code>IStrategy</code> value
     */
    public void unregisterStrategy(IStrategy inStrategy);
    /**
     * Register to receive trade suggestions generated by <code>IStrategy</code> objects.
     * 
     * <p>If the <code>ISubscriber</code> is already subscribed, this method does nothing. 
     * 
     * @param inSubscriber an <code>ISubscriber</code> value
     */
    public void subscribeToSuggestedTrades(ISubscriber inSubscriber);
    /**
     * Cancels subscription to trade suggestions generated by <code>IStrategy</code> objects.
     * 
     * <p>If the <code>ISubscriber</code> is not currently subscribed, this method does nothing.
     * 
     * @param inSubscriber an <code>ISubscriber</code> value
     */
    public void unsubscribeToSuggestedTrades(ISubscriber inSubscriber);
}
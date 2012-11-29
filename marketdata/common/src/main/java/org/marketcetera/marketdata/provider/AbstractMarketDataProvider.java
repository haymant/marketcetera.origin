package org.marketcetera.marketdata.provider;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.marketcetera.core.event.Event;
import org.marketcetera.core.trade.Instrument;
import org.marketcetera.core.util.log.I18NBoundMessage2P;
import org.marketcetera.core.util.log.SLF4JLoggerProxy;
import org.marketcetera.marketdata.Capability;
import org.marketcetera.marketdata.Content;
import org.marketcetera.marketdata.FeedStatus;
import org.marketcetera.marketdata.Messages;
import org.marketcetera.marketdata.cache.MarketdataCache;
import org.marketcetera.marketdata.manager.MarketDataException;
import org.marketcetera.marketdata.manager.MarketDataProviderNotAvailable;
import org.marketcetera.marketdata.manager.MarketDataProviderRegistry;
import org.marketcetera.marketdata.manager.MarketDataRequestFailed;
import org.marketcetera.marketdata.request.MarketDataRequest;
import org.marketcetera.marketdata.request.MarketDataRequestAtom;
import org.marketcetera.marketdata.request.MarketDataRequestToken;
import org.springframework.context.Lifecycle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/* $License$ */

/**
 * Provides common behavior for market data providers.
 * 
 * <p>To create a market data provider, extend this class.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
@ThreadSafe
public abstract class AbstractMarketDataProvider
        implements MarketDataProvider, MarketdataCache
{
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.cache.MarketdataCache#getSnapshot(org.marketcetera.core.trade.Instrument, org.marketcetera.marketdata.Content)
     */
    @Override
    public Event getSnapshot(Instrument inInstrument,
                             Content inContent)
    {
        Lock snapshotLock = marketdataLock.readLock();
        try {
            snapshotLock.lockInterruptibly();
            MarketdataCacheElement cachedData = cachedMarketdata.get(inInstrument);
            if(cachedData != null) {
                return cachedData.getSnapshot(inContent);
            }
            return null;
        } catch (InterruptedException e) {
            Messages.UNABLE_TO_ACQUIRE_LOCK.error(this);
            stop();
            throw new MarketDataRequestFailed(e);
        } finally {
            snapshotLock.unlock();
        }
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#start()
     */
    @Override
    public synchronized void start()
    {
        if(isRunning()) {
            stop();
        }
        try {
            doStart();
            notifier = new EventNotifier();
            notifier.start();
            running.set(true);
            setFeedStatus(FeedStatus.AVAILABLE);
        } catch (Exception e) {
            setFeedStatus(FeedStatus.ERROR);
            throw new MarketDataProviderStartFailed(e);
        }
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#stop()
     */
    @Override
    public synchronized void stop()
    {
        if(!isRunning()) {
            return;
        }
        try {
            doStop();
            setFeedStatus(FeedStatus.OFFLINE);
        } catch (Exception e) {
            setFeedStatus(FeedStatus.ERROR);
        } finally {
            notifier.stop();
            instrumentsBySymbol.clear();
            cachedMarketdata.clear();
            notifications.clear();
            requestsByInstrument.clear();
            requestsByAtom.clear();
            requestsBySymbol.clear();
            running.set(false);
        }
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#isRunning()
     */
    @Override
    public boolean isRunning()
    {
        return running.get();
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.provider.MarketDataProvider#requestMarketData(org.marketcetera.marketdata.request.MarketDataRequestToken, org.marketcetera.api.systemmodel.Subscriber)
     */
    @Override
    public void requestMarketData(MarketDataRequestToken inRequestToken)
    {
        if(!isRunning()) {
            throw new MarketDataProviderNotAvailable();
        }
        Set<MarketDataRequestAtom> atoms = explodeRequest(inRequestToken.getRequest());
        SLF4JLoggerProxy.debug(this,
                               "Received market data request {}, exploded to {}", //$NON-NLS-1$
                               inRequestToken,
                               atoms);
        Lock marketdataRequestLock = marketdataLock.writeLock();
        try {
            marketdataRequestLock.lockInterruptibly();
        } catch (InterruptedException e) {
            Messages.UNABLE_TO_ACQUIRE_LOCK.error(this);
            stop();
            throw new MarketDataRequestFailed(e);
        }
        try {
            for(MarketDataRequestAtom atom : atoms) {
                Capability requiredCapability = necessaryCapabilities.get(atom.getContent());
                if(requiredCapability == null) {
                    throw new UnsupportedOperationException(Messages.UNKNOWN_MARKETDATA_CONTENT.getText(atom.getContent()));
                }
                Set<Capability> capabilities = getCapabilities();
                if(!capabilities.contains(requiredCapability)) {
                    throw new MarketDataRequestFailed(new I18NBoundMessage2P(Messages.UNSUPPORTED_MARKETDATA_CONTENT,
                                                                             atom.getContent(),
                                                                             capabilities.toString()));
                }
                requestsByAtom.put(atom,
                                   inRequestToken);
                requestsBySymbol.put(atom.getSymbol(),
                                     inRequestToken);
                doMarketDataRequest(inRequestToken.getRequest(),
                                    atom);
            }
        } catch (Exception e) {
            try {
                cancelMarketDataRequest(inRequestToken);
            } catch (Exception ignored) {}
            Messages.MARKETDATA_REQUEST_FAILED.warn(this,
                                                    e);
            if(e instanceof MarketDataException) {
                throw (MarketDataException)e;
            }
            throw new MarketDataRequestFailed(e);
        } finally {
            marketdataRequestLock.unlock();
        }
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.provider.MarketDataProvider#cancelMarketDataRequest(org.marketcetera.marketdata.request.MarketDataRequestToken)
     */
    @Override
    public void cancelMarketDataRequest(MarketDataRequestToken inRequestToken)
    {
        // TODO re-exploding the request might cause problems if the request itself changed, better to associate the token ID
        //  with a set of atoms
        Lock cancelLock = marketdataLock.writeLock();
        try {
            cancelLock.lockInterruptibly();
            Set<MarketDataRequestAtom> atoms = explodeRequest(inRequestToken.getRequest());
            for(MarketDataRequestAtom atom : atoms) {
                Collection<MarketDataRequestToken> symbolRequests = requestsByAtom.get(atom);
                if(symbolRequests != null) {
                    symbolRequests.remove(inRequestToken);
                    if(symbolRequests.isEmpty()) {
                        doCancel(atom);
                    }
                }
                Collection<MarketDataRequestToken> requests = requestsBySymbol.get(atom.getSymbol());
                if(requests != null) {
                    requests.remove(inRequestToken);
                }
                Instrument mappedInstrument = instrumentsBySymbol.get(atom.getSymbol());
                if(mappedInstrument != null) {
                    Collection<MarketDataRequestToken> instrumentRequests = requestsByInstrument.get(mappedInstrument);
                    if(instrumentRequests != null) {
                        instrumentRequests.remove(inRequestToken);
                        if(instrumentRequests.isEmpty()) {
                            // no more requests for this instrument, which means this instrument will no longer be updated - clear the cache for it
                            cachedMarketdata.remove(mappedInstrument);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            Messages.UNABLE_TO_ACQUIRE_LOCK.error(this);
            stop();
        } finally {
            cancelLock.unlock();
        }
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.MarketDataProvider#getFeedStatus()
     */
    @Override
    public FeedStatus getFeedStatus()
    {
        return status;
    }
    /**
     * Sets the providerRegistry value.
     *
     * @param inProviderRegistry a <code>MarketDataProviderRegistry</code> value
     */
    public void setProviderRegistry(MarketDataProviderRegistry inProviderRegistry)
    {
        providerRegistry = inProviderRegistry;
    }
    /**
     * Indicates that the given events have been received by the provider and should be sent to interested subscribers.
     *
     * @param inContent a <code>Content</code> value
     * @param inInstrument an <code>Instrument</code> value
     * @param inEvents an <code>Event[]</code> value
     */
    protected void publishEvents(Content inContent,
                                 Instrument inInstrument,
                                 Event...inEvents)
    {
        // TODO validation: make sure each event has the proper content and instrument (don't do this every time, just if the provider requests validation)
        // TODO validation: make sure each instrument has a mapping
        notifications.add(new EventNotification(inContent,
                                                inInstrument,
                                                inEvents));
    }
    /**
     * Creates a link between the given symbol and the given instrument.
     *
     * @param inSymbol a <code>String</code> value
     * @param inInstrument an <code>Instrument</code> value
     */
    protected void addSymbolMapping(String inSymbol,
                                    Instrument inInstrument)
    {
        Lock symbolMappingLock = marketdataLock.writeLock();
        try {
            symbolMappingLock.lockInterruptibly();
            instrumentsBySymbol.put(inSymbol,
                                    inInstrument);
            Collection<MarketDataRequestToken> tokens = requestsBySymbol.get(inSymbol);
            for(MarketDataRequestToken token : tokens) {
                requestsByInstrument.put(inInstrument,
                                         token);
            }
        } catch (InterruptedException e) {
            Messages.UNABLE_TO_ACQUIRE_LOCK.error(this);
            stop();
        } finally {
            symbolMappingLock.unlock();
        }
    }
    /**
     * Sets the feed status value.
     *
     * @param inNewStatus a <code>FeedStatus</code> value
     */
    protected void setFeedStatus(FeedStatus inNewStatus)
    {
        if(inNewStatus != status) {
            status = inNewStatus;
            if(providerRegistry != null) {
                providerRegistry.setStatus(this,
                                           inNewStatus);
            }
        }
    }
    /**
     * Indicates if the provider requests additional validation on the data it produces.
     *
     * <p>Subclasses <em>may</em> override this method to increase validation on its generated
     * event stream. Validation has a minor negative impact on latency. Subclasses should return
     * <code>true</code> during the development phase but should likely return <code>false</code>
     * for production. The default returned value is <code>false</code>.
     *
     * @return a <code>boolean</code> value
     */
    protected boolean doValidation()
    {
        return false;
    }
    /**
     * Starts the market data provider.
     */
    protected abstract void doStart();
    /**
     * Stops the market data provider.
     */
    protected abstract void doStop();
    /**
     * Cancels the market data request represented by the given request atom.
     *
     * @param inAtom a <code>MarketDataRequestAtom</code> value
     */
    protected abstract void doCancel(MarketDataRequestAtom inAtom);
    /**
     * Indicates to the market data provider that it should request market data for the given
     * <code>MarketDataRequestAtom</code>.
     *
     * <p>Note that the overall <code>MarketDataRequest</code> is provided, and can be used
     * for reference, but the provider should respond to the given <code>MarketDataRequestAtom</code>.
     *
     * @param inCompleteRequest a <code>MarketDataRequest</code> value
     * @param inRequestAtom a <code>MarketDataRequestAtom</code> value
     * @throws InterruptedException if the request cannot be executed
     */
    protected abstract void doMarketDataRequest(MarketDataRequest inCompleteRequest,
                                                MarketDataRequestAtom inRequestAtom)
            throws InterruptedException;
    /**
     * Gets the distinct market data request atoms from the given request.
     *
     * @param inRequest a <code>MarketDataRequest</code> value
     * @return a <code>Set&lt;MarketDataRequestAtomg&gt;</code> value
     */
    private Set<MarketDataRequestAtom> explodeRequest(MarketDataRequest inRequest)
    {
        Set<MarketDataRequestAtom> atoms = new HashSet<MarketDataRequestAtom>();
        if(inRequest.getSymbols().isEmpty()) {
            for(String underlyingSymbol : inRequest.getUnderlyingSymbols()) {
                for(Content content : inRequest.getContent()) {
                    atoms.add(new MarketDataRequestAtomImpl(underlyingSymbol,
                                                            inRequest.getExchange(),
                                                            content,
                                                            true));
                }
            }
        } else {
            for(String symbol : inRequest.getSymbols()) {
                for(Content content : inRequest.getContent()) {
                    atoms.add(new MarketDataRequestAtomImpl(symbol,
                                                            inRequest.getExchange(),
                                                            content,
                                                            false));
                }
            }
        }
        return atoms;
    }
    /**
     * Represents a single market data request item.
     *
     * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
     * @version $Id$
     * @since $Release$
     */
    @Immutable
    private static class MarketDataRequestAtomImpl
            implements MarketDataRequestAtom
    {
        /* (non-Javadoc)
         * @see org.marketcetera.marketdata.request.MarketDataRequestAtom#getSymbol()
         */
        @Override
        public String getSymbol()
        {
            return symbol;
        }
        /* (non-Javadoc)
         * @see org.marketcetera.marketdata.request.MarketDataRequestAtom#getExchange()
         */
        @Override
        public String getExchange()
        {
            return exchange;
        }
        /* (non-Javadoc)
         * @see org.marketcetera.marketdata.request.MarketDataRequestAtom#isUnderlyingSymbol()
         */
        @Override
        public boolean isUnderlyingSymbol()
        {
            return isUnderlyingSymbol;
        }
        /* (non-Javadoc)
         * @see org.marketcetera.marketdata.provider.MarketDataRequestAtom#getContent()
         */
        @Override
        public Content getContent()
        {
            return content;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(content).append(" : ").append(symbol); //$NON-NLS-1$
            if(exchange != null) {
                builder.append(" : ").append(exchange); //$NON-NLS-1$
            }
            if(isUnderlyingSymbol) {
                builder.append(" (underlying)"); //$NON-NLS-1$
            }
            return builder.toString();
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            return new HashCodeBuilder().append(content).append(symbol).append(exchange).append(isUnderlyingSymbol).toHashCode();
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof MarketDataRequestAtomImpl)) {
                return false;
            }
            MarketDataRequestAtomImpl other = (MarketDataRequestAtomImpl) obj;
            return new EqualsBuilder().append(symbol,other.symbol)
                                      .append(exchange,other.exchange)
                                      .append(content,other.content)
                                      .append(isUnderlyingSymbol,other.isUnderlyingSymbol).isEquals();
        }
        /**
         * Create a new MarketDataRequestAtomImpl instance.
         *
         * @param inSymbol a <code>String</code> value
         * @param inExchange a <code>String</code> value or <code>null</code>
         * @param inContent a <code>Content</code> value
         * @param inIsUnderlyingSymbol a <code>boolean</code> value
         */
        private MarketDataRequestAtomImpl(String inSymbol,
                                          String inExchange,
                                          Content inContent,
                                          boolean inIsUnderlyingSymbol)
        {
            symbol = inSymbol;
            exchange = inExchange;
            content = inContent;
            isUnderlyingSymbol = inIsUnderlyingSymbol;
        }
        /**
         * symbol value, may be a symbol, an underlying symbol, or a symbol fragment
         */
        private final String symbol;
        /**
         * exchange value or <code>null</code>
         */
        private final String exchange;
        /**
         * indicates if the symbol is supposed to be a symbol or an underlying symbol
         */
        private final boolean isUnderlyingSymbol;
        /**
         * content value of the request
         */
        private final Content content;
    }
    /**
     * Processes events returned by the provider and publishes them to interested subscribers.
     *
     * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
     * @version $Id$
     * @since $Release$
     */
    private class EventNotifier
            implements Runnable, Lifecycle
    {
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run()
        {
            try {
                Collection<MarketDataRequestToken> requests = new ArrayList<MarketDataRequestToken>();
                while(keepAlive.get()) {
                    running.set(true);
                    EventNotification notification = notifications.take();
                    Event[] events = notification.events;
                    if(events != null) {
                        // sort out where to apply these events. the key to the cached market data is the instrument
                        Instrument eventInstrument = notification.instrument;
                        // there is at least one event to process. let the market data cache process each event
                        MarketdataCacheElement marketdataCache = cachedMarketdata.get(eventInstrument);
                        if(marketdataCache == null) {
                            marketdataCache = new MarketdataCacheElement(eventInstrument);
                            cachedMarketdata.put(eventInstrument,
                                                 marketdataCache);
                        }
                        // we now have the market data cache object to use - give it the incoming events
                        Collection<Event> outgoingEvents = marketdataCache.update(notification.content,
                                                                                  events);
                        // find subscribers to this instrument
                        requests.clear();
                        Lock requestLock = marketdataLock.readLock();
                        try {
                            requestLock.lockInterruptibly();
                            // defensive copy to avoid chance of CME if a cancel is called while the processing is ongoing
                            requests.addAll(requestsByInstrument.get(eventInstrument));
                        } finally {
                            requestLock.unlock();
                        }
                        for(MarketDataRequestToken request : requests) {
                            // for each subscriber, determine if the request contents justifies the update
                            if(request.getRequest().getContent().contains(notification.content)) {
                                // enclose the "publishTo" in a try/catch because we're ceding control to unknown code and
                                //  we don't want a misbehaving subscriber to break the market data mechanism
                                try {
                                    for(Event outgoingEvent : outgoingEvents) {
                                        request.getSubscriber().publishTo(outgoingEvent);
                                    }
                                } catch (Exception e) {
                                    Messages.EVENT_NOTIFICATION_FAILED.warn(AbstractMarketDataProvider.this,
                                                                            e,
                                                                            outgoingEvents,
                                                                            request.getSubscriber());
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
            } finally {
                SLF4JLoggerProxy.debug(AbstractMarketDataProvider.this,
                                       "Event notifier for {} shutting down", //$NON-NLS-1$
                                       getProviderName());
                running.set(false);
            }
        }
        /* (non-Javadoc)
         * @see org.springframework.context.Lifecycle#start()
         */
        @Override
        public synchronized void start()
        {
            if(running.get()) {
                return;
            }
            keepAlive.set(true);
            thread = new Thread(this,
                                "Market data notifier thread for " + getProviderName()); //$NON-NLS-1$
            thread.start();
        }
        /* (non-Javadoc)
         * @see org.springframework.context.Lifecycle#stop()
         */
        @Override
        public synchronized void stop()
        {
            if(!running.get()) {
                return;
            }
            keepAlive.set(false);
            if(thread != null) {
                thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException ignored) {}
                thread = null;
            }
        }
        /* (non-Javadoc)
         * @see org.springframework.context.Lifecycle#isRunning()
         */
        @Override
        public boolean isRunning()
        {
            return running.get();
        }
        /**
         * keeps the event notifier running
         */
        private final AtomicBoolean keepAlive = new AtomicBoolean(false);
        /**
         * indicates if the event notifier is running
         */
        private final AtomicBoolean running = new AtomicBoolean(false);
        /**
         * notifier thread
         */
        private volatile Thread thread;
    }
    /**
     * Represents an event notification to be published.
     *
     * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
     * @version $Id$
     * @since $Release$
     */
    private static class EventNotification
    {
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE).append(instrument).append(" ").append(content).append(" [") //$NON-NLS-1$ //$NON-NLS-2$
                                                                             .append(Arrays.toString(events)).append(" ]").toString(); //$NON-NLS-1$
        }
        /**
         * Create a new EventNotification instance.
         *
         * @param inContent a <code>Content</code> value
         * @param inInstrument an <code>Instrument</code> value
         * @param inEvents an <code>Event[]</code> value
         */
        private EventNotification(Content inContent,
                                  Instrument inInstrument,
                                  Event... inEvents)
        {
            events = inEvents;
            content = inContent;
            instrument = inInstrument;
        }
        /**
         * content value
         */
        private final Content content;
        /**
         * instrument value
         */
        private final Instrument instrument;
        /**
         * events to notify
         */
        private final Event[] events;
    }
    /**
     * feed status value
     */
    private volatile FeedStatus status = FeedStatus.UNKNOWN;
    /**
     * indicates if the provider is running
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /**
     * provider registry value with which to register/unregister or <code>null</code>
     */
    private volatile MarketDataProviderRegistry providerRegistry;
    /**
     * notification collection that contains events to publish
     */
    private final BlockingDeque<EventNotification> notifications = new LinkedBlockingDeque<EventNotification>();
    /**
     * processes events to be published and publishes them
     */
    private volatile EventNotifier notifier;
    /**
     * used to protect the market data collections
     */
    private final ReadWriteLock marketdataLock = new ReentrantReadWriteLock();
    /**
     * maps symbols or symbol fragments to the instrument value from the viewpoint of the market data provider
     */
    @GuardedBy("marketdataLock")
    private final Map<String,Instrument> instrumentsBySymbol = new HashMap<String,Instrument>();
    /**
     * tracks market data requests by the instrument in which they are interested
     */
    @GuardedBy("marketdataLock")
    private final Multimap<Instrument,MarketDataRequestToken> requestsByInstrument = HashMultimap.create();
    /**
     * tracks market data requests by the market data request atom created
     */
    @GuardedBy("marketdataLock")
    private final Multimap<MarketDataRequestAtom,MarketDataRequestToken> requestsByAtom = HashMultimap.create();
    /**
     * tracks market data requests by the symbol the request contained
     */
    @GuardedBy("marketdataLock")
    private final Multimap<String,MarketDataRequestToken> requestsBySymbol = HashMultimap.create();
    /**
     * tracks cached market data by the instrument
     */
    @GuardedBy("marketdataLock")
    private final Map<Instrument,MarketdataCacheElement> cachedMarketdata = new HashMap<Instrument,MarketdataCacheElement>();
    /**
     * maps the capabilities needed to honor a request of a particular content type
     */
    private static final Map<Content,Capability> necessaryCapabilities;
    /**
     * provides one-time initialization of static components
     */
    static
    {
        Map<Content,Capability> capabilities = new HashMap<Content,Capability>();
        capabilities.put(Content.AGGREGATED_DEPTH,Capability.AGGREGATED_DEPTH);
        capabilities.put(Content.DIVIDEND,Capability.DIVIDEND);
        capabilities.put(Content.LATEST_TICK,Capability.LATEST_TICK);
        capabilities.put(Content.LEVEL_2,Capability.LEVEL_2);
        capabilities.put(Content.MARKET_STAT,Capability.MARKET_STAT);
        capabilities.put(Content.OPEN_BOOK,Capability.OPEN_BOOK);
        capabilities.put(Content.TOP_OF_BOOK,Capability.TOP_OF_BOOK);
        capabilities.put(Content.TOTAL_VIEW,Capability.TOTAL_VIEW);
        capabilities.put(Content.UNAGGREGATED_DEPTH,Capability.UNAGGREGATED_DEPTH);
        necessaryCapabilities = Collections.unmodifiableMap(capabilities);
    }
}

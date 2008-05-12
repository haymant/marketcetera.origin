package org.marketcetera.marketdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.marketcetera.core.IFeedComponentListener;
import org.marketcetera.core.InMemoryIDFactory;
import org.marketcetera.core.InternalID;
import org.marketcetera.core.LoggerAdapter;
import org.marketcetera.core.MSymbol;
import org.marketcetera.core.MarketceteraException;
import org.marketcetera.core.MessageKey;
import org.marketcetera.core.NoMoreIDsException;
import org.marketcetera.core.publisher.ISubscriber;
import org.marketcetera.core.publisher.PublisherEngine;
import org.marketcetera.event.EventBase;
import org.marketcetera.event.IEventTranslator;
import org.marketcetera.marketdata.IMarketDataFeedToken.Status;
import org.marketcetera.quickfix.FIXMessageUtil;
import org.marketcetera.quickfix.FIXVersion;
import org.marketcetera.quickfix.IMessageTranslator;

import quickfix.Message;
import quickfix.field.SubscriptionRequestType;

/**
 * An abstract base class for all market data feeds. 
 * 
 * <p>Contains logic common to all market data feed implementations with 
 * the mechanics of adding/removing symbol/feed component listeners and keeping track of the feed 
 * status.
 * 
 * <p>Subclasses represent a connection to a specific market data feed.
 * 
 * <p>The generic types required are defined as follows:
 * <ul>
 *   <li>T - The token class for this feed</li>
 *   <li>C - The credentials class for this feed</li>
 *   <li>X - The message translator class for this feed</li>
 *   <li>E - The event translator class for this feed</li>
 *   <li>D - The type returned from {@link IMessageTranslator#translate(Message)}.</li>
 *   <li>F - The market data feed type itself</li>
 * </ul>
 *
 * @author andrei@lissovski.org
 * @author gmiller
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMarketDataFeed<T extends AbstractMarketDataFeedToken<F,C>, 
                                             C extends IMarketDataFeedCredentials, 
                                             X extends IMessageTranslator<D>, 
                                             E extends IEventTranslator,
                                             D,
                                             F extends AbstractMarketDataFeed> 
    implements IMarketDataFeed<T,C> 
{
    /**
     * default FIX message factory to use to construct messages
     */
    public static final FIXVersion DEFAULT_MESSAGE_FACTORY = FIXVersion.FIX44;
    /**
     * the id factory used to generate unique ids within the context of all feeds for this JVM session
     */
    private static final InMemoryIDFactory sIDFactory = new InMemoryIDFactory(0,
                                                                              Long.toString(System.currentTimeMillis()));
    /**
     * the singleton instance that aggregates the actual connections to the server 
     */
    private static ExecutorService sPool = Executors.newFixedThreadPool(10);
    /**
     * the status of the feed
     */
    private FeedStatus mFeedStatus = FeedStatus.UNKNOWN;
    /**
     * the type of the feed
     */
    private final FeedType mFeedType;
    /**
     * the feed ID
     */
    private final InternalID mID;
    /**
     * helper object to track the tokens and associated handles from requests and responses
     */
    private final HandleHolder mHandleHolder = new HandleHolder();
    /**
     * provider name associated with the data feed
     */
    private final String mProviderName;
    /**
     * publish/subscribe engine to manage feed status changes
     */
    private final PublisherEngine mFeedStatusPublisher = new PublisherEngine();
    /**
     * the last set of credentials passed to the feed
     */
    private C mLatestCredentials;
    /**
     * common logging category for messages generated by the data feed themselves
     */
    public final static String DATAFEED_MESSAGES = "marketdatafeed.messages";
    /**
     * Creates a FIX message requesting market data on the given symbols.
     *
     * @param inSymbols a <code>List&lt;MSymbol&gt;</code> value containing the symbols on which to make requests
     * @param inUpdate a <code>boolean</code> value indicating whether the request should be a single snapshot or an
     *  ongoing subscription
     * @return a <code>Message</code> value
     * @throws FeedException if an error occurs constructing the <code>Message</code>
     */
    public static Message marketDataRequest(List<MSymbol> inSymbols,
                                            boolean inUpdate) 
        throws FeedException 
    {
        try {
            // generate a unique ID for this FIX message
            InternalID id = getNextID();
            // generate the message using the current FIXMessageFactory
            Message message = DEFAULT_MESSAGE_FACTORY.getMessageFactory().newMarketDataRequest(id.toString(), 
                                                                                               inSymbols);
            // this little bit determines whether we subscribe to updates or not
            message.setChar(SubscriptionRequestType.FIELD, 
                            inUpdate ? '1' : '0');
            return message;
        } catch (NoMoreIDsException e) {
            throw new FeedException(MessageKey.ERROR_MARKET_DATA_FEED_CANNOT_GENERATE_MESSAGE.getLocalizedMessage(),
                                    e);
        }
    }
    /**
     * Gets the next ID in sequence for assiging unique identifiers to market data feed objects.
     *
     * @return an <code>InternalID</code> value
     * @throws NoMoreIDsException if no more IDs are available
     */
    private static InternalID getNextID() 
        throws NoMoreIDsException
    {
        return new InternalID(sIDFactory.getNext());
    }
    /**
     * Create a new <code>AbstractMarketDataFeed</code> instance.
     *
     * @param inFeedType a <code>FeedType</code> value
     * @param inCredentials a <code>C</code> value or null
     * @throws NoMoreIDsException if no more ids are available to be assigned to this feed
     * @throws NullPointerException if the <code>FeedType</code> is null
     */
    protected AbstractMarketDataFeed(FeedType inFeedType,
                                     String inProviderName, 
                                     C inCredentials) 
        throws NoMoreIDsException
    {
        if(inFeedType == null) {
            throw new NullPointerException();
        }
        if(inProviderName == null) {
            throw new NullPointerException();
        }
        mFeedType = inFeedType;
        mID = getNextID();
        mProviderName = inProviderName;
        mFeedStatus = FeedStatus.OFFLINE;
        mLatestCredentials = inCredentials;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IMarketDataFeed#execute(org.marketcetera.marketdata.AbstractMarketDataFeedCredentials, quickfix.Message, org.marketcetera.core.publisher.ISubscriber)
     */
    public final T execute(MarketDataFeedTokenSpec<C> inTokenSpec)
            throws FeedException
    {
        C credentials = inTokenSpec.getCredentials();
        // the credentials supplied are the best available to us - either given
        //  as part of this query or those originally supplied - either way,
        //  they must at this time be non-null
        if(credentials == null) {
            throw new NullPointerException();
        }
        // record these as the latest credentials
        setLatestCredentials(credentials);
        List<? extends ISubscriber> subscribers = inTokenSpec.getSubscribers();
        // the token is used to track the request and its responses
        // generate a new token for this request
        T token = generateToken(inTokenSpec);
        // it's possible that some messages won't need subscribers, perhaps if the caller doesn't care
        //  about responses.  if the subscriber is null, ignore it.  otherwise, set the token to receive
        //  the responses        
        if(subscribers != null) {
            token.subscribeAll(subscribers);
        }
        // construct the object that will be invoked by the ThreadPool
        ExecutorThread thread = new ExecutorThread(token,
                                                   credentials);        
        try {
            // this command executes the request using the connector.  the connector has all the information it needs
            //  to execute the request because of the token.  this command is asynchronous.
            Future<T> response = sPool.submit(thread);
            // wait for the response to be returned.  this doesn't mean that the results are back yet, just that
            //  the request has been received and acknowledged by the feed service.  if you can dig it, it's an
            //  asynchronous request for an asynchronous response.
            return response.get();
        } catch (Throwable t) {
            throw new FeedException(MessageKey.ERROR_MARKET_DATA_FEED_EXECUTION_FAILED.getLocalizedMessage(),
                                    t);
        }
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IMarketDataFeed#execute(org.marketcetera.marketdata.IMarketDataFeedCredentials, quickfix.Message, org.marketcetera.core.publisher.ISubscriber)
     */
    public T execute(C inCredentials,
                     Message inMessage,
                     ISubscriber inSubscriber) 
        throws FeedException
    {
        return execute(inCredentials,
                       inMessage,
                       Arrays.asList(new ISubscriber[] { inSubscriber } ));
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IMarketDataFeed#execute(org.marketcetera.marketdata.IMarketDataFeedCredentials, quickfix.Message, java.util.List)
     */
    public T execute(C inCredentials,
                     Message inMessage,
                     List<? extends ISubscriber> inSubscribers) 
        throws FeedException
    {
        return execute(MarketDataFeedTokenSpec.generateTokenSpec((inCredentials == null ? getLatestCredentials() : inCredentials), 
                                                                 inMessage, 
                                                                 inSubscribers));
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IMarketDataFeed#execute(quickfix.Message, org.marketcetera.core.publisher.ISubscriber)
     */
    public T execute(Message inMessage,
                     ISubscriber inSubscriber)
            throws FeedException
    {
        return execute(getLatestCredentials(),
                       inMessage,
                       inSubscriber);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IMarketDataFeed#execute(quickfix.Message, java.util.List)
     */
    public T execute(Message inMessage,
                     List<? extends ISubscriber> inSubscribers)
            throws FeedException
    {
        return execute(getLatestCredentials(),
                       inMessage,
                       inSubscribers);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IFeedComponent#getFeedStatus()
     */
    public final FeedStatus getFeedStatus()
    {
        return mFeedStatus;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IFeedComponent#getFeedType()
     */
    public final FeedType getFeedType()
    {
        return mFeedType;
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#isRunning()
     */
    public boolean isRunning()
    {
        return getFeedStatus().isRunning();
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#start()
     */
    public void start()
    {
        setFeedStatus(FeedStatus.AVAILABLE);
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#stop()
     */
    public void stop()
    {
        setFeedStatus(FeedStatus.OFFLINE);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IFeedComponent#getID()
     */
    public final String getID()
    {
        return mID.toString();
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IFeedComponent#addFeedComponentListener(org.marketcetera.core.IFeedComponentListener)
     */
    public final void addFeedComponentListener(IFeedComponentListener inListener)
    {
        mFeedStatusPublisher.subscribe(new FeedComponentListenerWrapper(inListener,
                                                                        this));
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IFeedComponent#removeFeedComponentListener(org.marketcetera.core.IFeedComponentListener)
     */
    public final void removeFeedComponentListener(IFeedComponentListener inListener)
    {
        mFeedStatusPublisher.unsubscribe(new FeedComponentListenerWrapper(inListener,
                                                                          this));
    }
    /*
     * the following methods must be implemented by the feed subclasses and
     * represent the contract between subclass and parent
     */
    /**
     * Generates a token encapsulating the given request.
     * 
     * <p>The object returned is dedicated to the execution of the given message.
     * 
     * @param inTokenSpec a <code>MarketDataFeedTokenSpec&lt;C&gt;</code> value encapsulating the data feed request
     * @return a <code>AbstractMarketDataFeedToken</code> value
     * @throws MarketceteraException if an error occurs
     */
    protected abstract T generateToken(MarketDataFeedTokenSpec<C> inTokenSpec)
        throws FeedException;
    /**
     * Executes the market data request represented by the passed value.
     * 
     * <p>The values returned in the handle list must be unique with respect
     * to the current JVM invocation for this data feed.
     *
     * @param inData a <code>D</code> value containing the data returned by
     *   the corresponding {@link IMessageTranslator}.
     * @return a <code>List&lt;String&gt;</code> value containing the set of
     *   handles to be associated with this request
     * @throws FeedException if the request cannot be transmitted to the feed
     * @see IMessageTranslator#translate(Message)
     */
    protected abstract List<String> doMarketDataRequest(D inData)
        throws FeedException;
    /**
     * Executes the derivative security list request represented by the passed value.
     * 
     * <p>The values returned in the handle list must be unique with respect
     * to the current JVM invocation for this data feed.
     *
     * @param inData a <code>D</code> value containing the data returned by
     *   the corresponding {@link IMessageTranslator}.
     * @return a <code>List&lt;String&gt;</code> value containing the set of
     *   handles to be associated with this request
     * @throws FeedException if the request cannot be transmitted to the feed
     * @see IMessageTranslator#translate(Message)
     */
    protected abstract List<String> doDerivativeSecurityListRequest(D inData)
        throws FeedException;
    /**
     * Executes the security list request represented by the passed value.
     * 
     * <p>The values returned in the handle list must be unique with respect
     * to the current JVM invocation for this data feed.
     *
     * @param inData a <code>D</code> value containing the data returned by
     *   the corresponding {@link IMessageTranslator}.
     * @return a <code>List&lt;String&gt;</code> value containing the set of
     *   handles to be associated with this request
     * @throws FeedException if the request cannot be transmitted to the feed
     * @see IMessageTranslator#translate(Message)
     */
    protected abstract List<String> doSecurityListRequest(D inData)
        throws FeedException;
    /**
     * Returns an instance of {@link IMessageTranslator} appropriate for this feed.
     *
     * <p>The {@link IMessageTranslator} translates a FIX message to a data-type
     * appropriate for this feed.
     * 
     * @return an <code>X</code> value
     */
    protected abstract X getMessageTranslator();
    /**
     * Determines if there exists an active and valid connection to the feed.
     * 
     * <p>Returns true if there exists a connection to the feed and the
     * credentials used to open that connection match the given credentials.
     *
     * @param inCredentials a <code>C</code> value
     * @return a <code>boolean</code> value
     */
    protected abstract boolean isLoggedIn(C inCredentials);
    /**
     * Connects to the feed and supplies the given credentials.
     *
     * @param inCredentials a <code>C</code> value
     * @return a <code>boolean<code> value indicating whether the login
     *   was successful or not
     */
    protected abstract boolean doLogin(C inCredentials);
    /**
     * Disconnect from the feed.
     */
    protected abstract void doLogout();
    /**
     * Cancel the transaction associated with the given handle.
     *
     * @param inHandle a <code>String</code> value containing a handle
     *   meaningful to the feed
     */
    protected abstract void doCancel(String inHandle);
    /**
     * Returns an instance of {@link IEventTranslator} appropriate for this feed.
     *
     * <p>The {@link IEventTranslator} translates data-types appropriate for this feed
     * to subclasses of {@link EventBase}.
     * 
     * @return an <code>E</code> value
     */
    protected abstract E getEventTranslator();
    /*
     * the following methods *may* be overridden by implementing subclasses but
     * have default implementations as documented
     */
    /**
     * Performs any initialization steps necessary before {@link #doLogin(IMarketDataFeedCredentials)}.
     *
     * <p>This implementation does nothing.
     * 
     * @param inToken a <code>T</code> value
     * @return a <code>boolean</code> value indicating if the initialization was valid and
     *   processing may continue
     */
    protected boolean doInitialize(T inToken)
    {
        return true;
    }
    /**
     * Called at the beginning of {@link #doExecute(AbstractMarketDataFeedToken)}. 
     *
     * <p>This implementation returns <code>true</code>.  Subclasses may override this
     * method to abort an execution if necessary.  Return <code>false</code> to abort
     * the execution.
     * 
     * @param inToken a <code>T</code> value
     * @return a <code>boolean</code> value
     */
    protected boolean beforeDoExecute(T inToken)
    {
        return true;
    }
    /**
     * Called at the end of {@link #doExecute(AbstractMarketDataFeedToken)}. 
     *
     * <p>This implementation does nothing.  Subclasses may override this
     * method to implement behavior required at the end of an execution.
     * This method will always be called, regardless of the success or failure
     * of the execution.
     * 
     * @param inToken a <code>T</code> value
     */
    protected void afterDoExecute(T inToken)
    {
    }
    /*
     * the following methods may be called by implementing subclasses but may not
     * be overridden
     */
    /**
     * Sets the status of the feed.
     * 
     * @param inFeedStatus a <code>FeedStatus</code> value
     */
    protected final void setFeedStatus(FeedStatus inFeedStatus)
    {
        if(!(mFeedStatus.equals(inFeedStatus))) {
            mFeedStatus = inFeedStatus;
            mFeedStatusPublisher.publish(this);
        }
    }
    /**
     * Registers data received from the feed in association with the
     * given handle.
     * 
     * <p>Subclasses should call this method to process data received
     * from the data feed.
     *
     * @param inHandle a <code>MarketDataHandle</code> value
     * @param inData an <code>Object</code> value containing the data
     *   to be transmitted to subscribers
     */
    protected final void dataReceived(String inHandle,
                                      Object inData)
    {
        MarketDataHandle mdHandle = compose(inHandle);
        T token = mHandleHolder.getToken(mdHandle);
        if(token == null) {
            if(LoggerAdapter.isWarnEnabled(this)) {
                LoggerAdapter.warn(MessageKey.WARNING_MARKET_DATA_FEED_DATA_IGNORED.getLocalizedMessage(inData),
                                   this);
            }
        } else {
            E eventTranslator = getEventTranslator();
            try {
                List<EventBase> events = eventTranslator.translate(inData);
                for(EventBase event : events) {
                    token.publish(event);
                }
            } catch (Throwable t) {
                if(LoggerAdapter.isWarnEnabled(this)) {
                    LoggerAdapter.warn(MessageKey.WARNING_MARKET_DATA_FEED_DATA_IGNORED.getLocalizedMessage(inData),
                                       t,
                                       this);
                }
            }
        }
    }
    /*
     * the following methods can be called by other package classes
     */
    /**
     * Cancels the active request represented by the given token.
     *
     * @param inToken a <code>T</code> value
     * @throws NullPointerException if <code>inToken</code> is null
     */
    final void cancel(T inToken)
    {
        if(inToken == null) {
            throw new NullPointerException();
        }
        try {
            // translate token to handle or handles
            List<MarketDataHandle> marketDataHandles = mHandleHolder.removeToken(inToken);
            if(marketDataHandles != null) {
                // pass handles to subclass to execute cancel
                for(MarketDataHandle marketDataHandle : marketDataHandles) {
                    try {
                        doCancel(decompose(marketDataHandle));
                    } catch (Throwable t) {
                        if(LoggerAdapter.isWarnEnabled(this)) {
                            LoggerAdapter.warn(MessageKey.WARNING_MARKET_DATA_FEED_CANNOT_CANCEL_SUBSCRIPTION.getLocalizedMessage(marketDataHandle),
                                               t,
                                               this);
                        }
                    }
                }
            }
        } finally {
            inToken.setStatus(Status.CANCELED);
        }
    }
    /*
     * the following methods are helper methods for this class
     */
    /**
     * Performs the execution of the market data request.
     *
     * @param inToken a <code>T</code> value
     * @return a <code>boolean</code> value
     */
    private boolean doExecute(T inToken)
    {
        if(!beforeDoExecute(inToken)) {
            return false;
        }
        // translate fix message to specialized type
        X xlator = getMessageTranslator();
        Message message = inToken.getTokenSpec().getMessage();
        try {
            D data = xlator.translate(message);
            if(FIXMessageUtil.isMarketDataRequest(message)) {
                mHandleHolder.addHandles(inToken,
                                         doMarketDataRequest(data));
                return true;
            }
            if(FIXMessageUtil.isDerivativeSecurityListRequest(message)) {
                mHandleHolder.addHandles(inToken,
                                         doDerivativeSecurityListRequest(data));
                return true;
            }
            if(FIXMessageUtil.isSecurityListRequest(message)) {
                mHandleHolder.addHandles(inToken,
                                         doSecurityListRequest(data));
                return true;
            }
            // Unhandled message type
            if(LoggerAdapter.isErrorEnabled(this)) {
                LoggerAdapter.error(MessageKey.ERROR_MARKET_DATA_FEED_UNKNOWN_MESSAGE_TYPE.getLocalizedMessage(),
                                    this);
            }
            return false;
        } catch (Throwable t) {
            if(LoggerAdapter.isErrorEnabled(this)) {
                LoggerAdapter.error(MessageKey.ERROR_MARKET_DATA_FEED_EXECUTION_FAILED.getLocalizedMessage(),
                                    t,
                                    this);
            }
            return false;
        } finally {
            afterDoExecute(inToken);
        }
    }
    /**
     * Returns a {@link MarketDataHandle} instance encapsulating the given data feed proto-handle.
     *
     * @param inSeed a <code>String</code> value containing the proto-handle returned from
     *   a data feed
     * @return a <code>MarketDataHandle</code> value
     */
    private MarketDataHandle compose(String inSeed)
    {
        return new MarketDataHandle(inSeed);
    }
    /**
     * Returns the data feed proto-handle from the given {@link MarketDataHandle}.
     *
     * @param inHandle a <code>MarketDataHandle</code> value
     * @return a <code>String</code> value
     */
    private String decompose(MarketDataHandle inHandle)
    {
        return inHandle.decompose();
    } 
    /**
     * Get the latestCredentials value.
     *
     * @return a <code>AbstractMarketDataFeed</code> value
     */
    private C getLatestCredentials()
    {
        return mLatestCredentials;
    }
    /**
     * Sets the latestCredentials value.
     *
     * @param a <code>AbstractMarketDataFeed</code> value
     */
    private void setLatestCredentials(C inLatestCredentials)
    {
        mLatestCredentials = inLatestCredentials;
    }
    /*
     * the following are private helper classes
     */
    /**
     * Helper that performs the actual execution in the threadpool. 
     *
     * <p>This class exists for two reasons:
     * <ol>
     *   <li>Implement {@link Callable} which includes a public method for
     *       the benefit of the {@link ExecutorService} (threadpool) but
     *       without requiring {@link AbstactMarketDataFeed}
     *       to implement {@link Callable#call()}, which should not be
     *       called by external classes.</li>
     *   <li>Make sure that a specific pair of {@link T} and {@link C}
     *       corresponds to the {@link Callable#call()} method that uses
     *       them</li>
     * </ol>
     * 
     * <p>Note that this class is intentionally declared <code>non-static</code>
     * because it must have access to the parent's <code>T</code> and <code>C</code>
     * types and must call several non-static methods on the parent.
     *
     * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
     * @version $Id$
     * @since 0.43-SNAPSHOT
     */
    private class ExecutorThread
        implements Callable<T>
    {
        /**
         * the token representing the request and its responses
         */
        private final T mToken;
        /**
         * the credentials provided to fulfill the request
         */
        private final C mCredentials;
        /**
         * Create a new ExecutorThread instance.
         *
         * @param inToken a <code>T</code> value
         * @param inCredentials a <code>C</code> value
         */
        private ExecutorThread(T inToken,
                               C inCredentials)
        {
            mToken = inToken;
            mCredentials = inCredentials;
        }       
        /* (non-Javadoc)
         * @see java.util.concurrent.Callable#call()
         */
        public T call()
                throws Exception
        {
            setFeedStatus(FeedStatus.UNKNOWN);
            C credentials = mCredentials;
            T token = mToken;

            token.setStatus(IMarketDataFeedToken.Status.RUNNING);
            // check to see if we're currently logged in to match
            //  the current credentials
            if(!isLoggedIn(credentials)) {
                // login with the given credentials
                if(!doLogin(credentials)) {
                    // bail out expressing sadness
                    setFeedStatus(FeedStatus.ERROR);
                    token.setStatus(IMarketDataFeedToken.Status.LOGIN_FAILED);
                    return token;
                }            
            }        
            // feed is logged in
            // do any initialization required
            if(!doInitialize(token)) {
                setFeedStatus(FeedStatus.ERROR);
                token.setStatus(IMarketDataFeedToken.Status.INITIALIZATION_FAILED);
                return token;
            }
            setFeedStatus(FeedStatus.AVAILABLE);
            // feed should be ready for commands
            // execute command, wait for status response, not responses to the actual command
            if(!doExecute(token)) {
                setFeedStatus(FeedStatus.ERROR);
                token.setStatus(IMarketDataFeedToken.Status.EXECUTION_FAILED);
                return token;
            }
            token.setStatus(IMarketDataFeedToken.Status.ACTIVE);
            return token;
        }        
    }
    /**
     * Encapsulates the handles and tokens collections.
     *
     * <p>Note that this class is declared non-static intentionally in order
     * to use the parent class's generic types.
     *
     * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
     * @version $Id$
     * @since 0.43-SNAPSHOT
     */    
    private class HandleHolder
    {
        /**
         * stores client response handles by token - note that these two collections must be kept in sync
         */
        private final Hashtable<T,List<MarketDataHandle>> mHandlesByToken = new Hashtable<T,List<MarketDataHandle>>();
        /**
         * stores tokens by handle - note that these two collections must be kept in sync
         */
        private final Hashtable<MarketDataHandle,T> mTokensByHandle = new Hashtable<MarketDataHandle,T>();
        /**
         * object used for synchronization lock for these two collections
         */
        private final Object mLock = new Object();
        /**
         * Create a new HandleHolder instance.
         */
        private HandleHolder()
        {            
        }
        /**
         * Records the given handles as associated to the given token.
         *
         * @param inToken a <code>T</code> value
         * @param inHandles a <code>List&lt;String&gt;</code> value
         */
        private void addHandles(T inToken,
                                List<String> inHandles)
        {
            // by convention, synchronization for mHandlesByToken and mTokensByHandle
            //  is performed on mLock to avoid deadlock - do not access
            //  mTokensByHandle or mHandlesByToken without synchronizing mLock
            synchronized(mLock) {
                List<MarketDataHandle> marketDataHandles = mHandlesByToken.get(inToken);
                if(marketDataHandles == null){
                    marketDataHandles = new ArrayList<MarketDataHandle>();
                }
                for(String handle : inHandles) {
                    MarketDataHandle mdHandle = compose(handle);
                    marketDataHandles.add(mdHandle);
                    mTokensByHandle.put(mdHandle, 
                                        inToken);
                }
                mHandlesByToken.put(inToken, 
                                    marketDataHandles);
            }
        }
        /**
         * Returns the token associated with the given handle.
         *
         * @param inMarketDataHandle a <code>MarketDataHandle</code> value
         * @return a <code>T</code> value or null if no token is associated
         *   with the given handle
         */
        private T getToken(MarketDataHandle inMarketDataHandle)
        {
            // by convention, synchronization for mHandlesByToken and mTokensByHandle
            //  is performed on mLock to avoid deadlock - do not access
            //  mTokensByHandle or mHandlesByToken without synchronizing mLock
            synchronized(mLock) {
                return mTokensByHandle.get(inMarketDataHandle);
            }
        }
        /**
         * Removes the given token and its handles from the handle list.
         *
         * @param inToken a <code>T</code> value
         * @return a <code>List&lt;MarketDataHandle&gt;</code> value or null if no handles are associated
         *   wtih this token
         */
        private List<MarketDataHandle> removeToken(T inToken)
        {
            List<MarketDataHandle> handles;
            // by convention, synchronization for mHandlesByToken and mTokensByHandle
            //  is performed on mLock to avoid deadlock - do not access
            //  mTokensByHandle or mHandlesByToken without synchronizing mLock
            synchronized(mLock) {
                handles = mHandlesByToken.remove(inToken);
                for(MarketDataHandle handle : handles) {
                    mTokensByHandle.remove(handle);
                }
            }
            return handles;
        }
    }
    /**
     * A unique handle to associate data feed requests with responses.
     *
     * <p>The handle created is guranteed to be unique within the scope of all
     * data feeds in the current JVM run iff:
     * <ol>
     *   <li>all proto-handles returned by {@link AbstractMarketDataFeed#doMarketDataRequest(Object)} 
     *       are unique within the scope of the relevant feed in the current JVM run</li>
     *   <li>the set of values returned by {@link IMarketDataFeedFactory#getProviderName()} from all
     *       data feeds contains no duplicates</li>
     * </ol>
     *       
     * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
     * @version $Id$
     * @since 0.43-SNAPSHOT
     */
    private class MarketDataHandle
    {
        /**
         * the handle value
         */
        private final String mHandle;
        /**
         * the handle from the data feed itself
         */
        private final String mProtoHandle;
        /**
         * Create a new MarketDataHandle instance.
         *
         * @param inHandle a <code>String</code> value containing a value
         *   meaningfull to the originating data feed which can be used
         *   to refer to a unique data feed request
         */
        private MarketDataHandle(String inHandle)
        {
            mProtoHandle = inHandle;
            mHandle = String.format("%s-%s",
                                    mProviderName,
                                    inHandle);
        }
        /**
         * Returns the proto-handle used to originally create this object.
         *
         * @return a <code>String</code> value
         */
        private String decompose()
        {
            return new String(mProtoHandle);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return new String(mHandle);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((mHandle == null) ? 0 : mHandle.hashCode());
            return result;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final MarketDataHandle other = (MarketDataHandle) obj;
            if (mHandle == null) {
                if (other.mHandle != null)
                    return false;
            } else if (!mHandle.equals(other.mHandle))
                return false;
            return true;
        }
    }
    /**
     * Wraps the {@link FeedComponentListener} for this feed.
     *
     * <p>The wrapper translates {@link ISubscriber} methods to
     * the {@link IFeedComponentListener} objects.
     * 
     * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
     * @version $Id$
     * @since 0.43-SNAPSHOT
     */
    private static class FeedComponentListenerWrapper
        implements ISubscriber
    {
        /**
         * the feed component listener to which to transmit feed status updates
         */
        private final IFeedComponentListener mListener;
        /**
         * the feed component (data feed) whose status is changing
         */
        private final IFeedComponent mParent;
        /**
         * Create a new FeedComponentListenerWrapper instance.
         *
         * @param inListener an <code>IFeedComponentListener</code> value
         * @param inParent an <code>IFeedComponent</code> value
         */
        private FeedComponentListenerWrapper(IFeedComponentListener inListener,
                                             IFeedComponent inParent)
        {
            mListener = inListener;
            mParent = inParent;
        }
        /* (non-Javadoc)
         * @see org.marketcetera.core.publisher.ISubscriber#isInteresting(java.lang.Object)
         */
        public boolean isInteresting(Object inData)
        {
            return true;
        }
        /* (non-Javadoc)
         * @see org.marketcetera.core.publisher.ISubscriber#publishTo(java.lang.Object)
         */
        public void publishTo(Object inData)
        {
            mListener.feedComponentChanged(mParent);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((mListener == null) ? 0 : mListener.hashCode());
            return result;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final FeedComponentListenerWrapper other = (FeedComponentListenerWrapper) obj;
            if (mListener == null) {
                if (other.mListener != null)
                    return false;
            } else if (!mListener.equals(other.mListener))
                return false;
            return true;
        }
    }
}

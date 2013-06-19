package org.marketcetera.core.marketdata;

import org.marketcetera.core.CoreException;
import org.marketcetera.core.marketdata.IFeedComponent.FeedType;

/**
 * Test implementation of {@link org.marketcetera.core.marketdata.IMarketDataFeedFactory}.
 *
 * @version $Id: MockMarketDataFactory.java 82329 2012-04-10 16:28:13Z colin $
 * @since 0.5.0
 */
public enum MockMarketDataFactory
        implements IMarketDataFeedFactory<MockMarketDataFeed, MockMarketDataFeedCredentials>
{
    INSTANCE;
    private AbstractMarketDataFeedFactory<MockMarketDataFeed, MockMarketDataFeedCredentials> mInnerFactory =
            new AbstractMarketDataFeedFactory<MockMarketDataFeed, MockMarketDataFeedCredentials>() {
        private static final String PROVIDER = "TEST"; //$NON-NLS-1$

        public String getProviderName()
        {
            return PROVIDER;
        }

        public MockMarketDataFeed getMarketDataFeed()
                throws CoreException
        {
            return new MockMarketDataFeed(FeedType.SIMULATED);
        }                
    };
    public String getProviderName()
    {
        return mInnerFactory.getProviderName();
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.IMarketDataFeedFactory#getMarketDataFeed()
     */
    @Override
    public MockMarketDataFeed getMarketDataFeed()
            throws CoreException
    {
        return mInnerFactory.getMarketDataFeed();
    }            
}

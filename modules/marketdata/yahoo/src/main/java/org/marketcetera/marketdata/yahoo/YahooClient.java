package org.marketcetera.marketdata.yahoo;

import org.marketcetera.api.attributes.ClassVersion;
import org.springframework.context.Lifecycle;

/* $License$ */

/**
 * Provides access to the Yahoo data source.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id: YahooClient.java 16063 2012-01-31 18:21:55Z colin $
 * @since 2.1.4
 */
@ClassVersion("$Id: YahooClient.java 16063 2012-01-31 18:21:55Z colin $")
interface YahooClient
        extends Lifecycle
{
    /**
     * Logs in to the Yahoo data source with the given credentials.
     *
     * @param inCredentials a <code>YahooFeedCredentials</code> value
     * @return a <code>boolean</code> value
     */
    boolean login(YahooFeedCredentials inCredentials);
    /**
     * Logs out from the Yahoo data source.
     */
    void logout();
    /**
     * Indicates if the connection is currently logged in or not. 
     *
     * @return a <code>boolean</code> value
     */
    boolean isLoggedIn();
    /**
     * Executes the given request.
     *
     * @param inRequest a <code>YahooRequest</code> value
     */
    void request(YahooRequest inRequest);
    /**
     * Cancels th given request.
     *
     * @param inRequest a <code>YahooRequest</code> value
     */
    void cancel(YahooRequest inRequest);
    /**
     * Gets the current count of requests. 
     *
     * @return a <code>long</code> value
     */
    long getRequestCounter();
    /**
     * Resets the count of requests.
     */
    void resetRequestcounter();
}

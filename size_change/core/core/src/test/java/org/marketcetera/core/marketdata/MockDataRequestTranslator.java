package org.marketcetera.core.marketdata;

import org.marketcetera.core.CoreException;

/**
 * Test implementation of <code>AbstractMessageTranslator</code>.
 *
 * @version $Id: MockDataRequestTranslator.java 82329 2012-04-10 16:28:13Z colin $
 * @since 0.5.0
 */
public class MockDataRequestTranslator
        implements DataRequestTranslator<String>
{
    private static boolean sTranslateThrows = false;
    /**
     * Create a new TestMessageTranslator instance.
     *
     */
    public MockDataRequestTranslator()
    {
    }
    public static boolean getTranslateThrows()
    {
        return sTranslateThrows;
    }
    public static void setTranslateThrows(boolean inTranslateThrows)
    {
        sTranslateThrows = inTranslateThrows;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.DataRequestTranslator#translate(org.marketcetera.module.DataRequest)
     */
    @Override
    public String fromDataRequest(MarketDataRequest inMessage)
            throws CoreException
    {
        if(getTranslateThrows()) {
            throw new NullPointerException("This exception is expected");
        }
        return inMessage.toString();
    }
}

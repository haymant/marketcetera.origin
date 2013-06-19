package org.marketcetera.core.event;

import java.util.Date;

import org.marketcetera.core.event.beans.EventBean;
import org.marketcetera.core.marketdata.MarketDataRequest;

/* $License$ */

/**
 * Test implementation of {@link org.marketcetera.core.event.Event}.
 *
 * @version $Id: MockEvent.java 82331 2012-04-10 16:29:48Z colin $
 * @since 2.0.0
 */
public class MockEvent
        implements Event
{
    /**
     * Create a new MockEvent instance.
     */
    public MockEvent()
    {
        this(System.nanoTime(),
             System.currentTimeMillis());
    }
    /**
     * Create a new MockEvent instance.
     *
     * @param inMessageId
     * @param inTimestamp
     */
    public MockEvent(long inMessageId,
                     long inTimestamp)
    {
        event.setMessageId(inMessageId);
        event.setTimestamp(new Date(inTimestamp));
    }
    /**
     * Create a new MockEvent instance.
     *
     * @param inRequest
     */
    public MockEvent(MarketDataRequest inRequest)
    {
        this(System.nanoTime(),
             System.currentTimeMillis());
        setSource(inRequest);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.event.Event#getMessageId()
     */
    @Override
    public long getMessageId()
    {
        return event.getMessageId();
    }
    /* (non-Javadoc)
     * @see org.marketcetera.event.Event#getSource()
     */
    @Override
    public Object getSource()
    {
        return event.getSource();
    }
    /* (non-Javadoc)
     * @see org.marketcetera.event.Event#getTimestamp()
     */
    @Override
    public Date getTimestamp()
    {
        return event.getTimestamp();
    }
    /* (non-Javadoc)
     * @see org.marketcetera.event.Event#setSource(java.lang.Object)
     */
    @Override
    public void setSource(Object inSource)
    {
        event.setSource(inSource);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.event.TimestampCarrier#getTimeMillis()
     */
    @Override
    public long getTimeMillis()
    {
        Date timestamp = event.getTimestamp();
        if(timestamp == null) {
            return -1;
        }
        return event.getTimestamp().getTime();
    }
    /**
     * 
     */
    private final EventBean event = new EventBean();
    private static final long serialVersionUID = 1L;
}

package org.marketcetera.core.publisher;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.marketcetera.core.MarketceteraTestSuite;

/**
 * Tests {@link PublisherEngine}.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since 0.43-SNAPSHOT
 */
public class PublisherEngineTest
        extends TestCase
{
    private MockPublisher mPublisher;
    
    public PublisherEngineTest(String inArg0)
    {
        super(inArg0);
    }

    public static Test suite() 
    {
        TestSuite suite = new MarketceteraTestSuite(PublisherEngineTest.class);
        return suite;
    }
    
    protected void setUp()
            throws Exception
    {
        super.setUp();
        mPublisher = new MockPublisher();
    }

    public void testInitializeThreadPool()
        throws Exception
    {
        mPublisher.publish(this);
        mPublisher.publish(this);
    }
    
    public void testConstructor()
        throws Exception
    {
        PublisherEngine e = new PublisherEngine();
        e.publish(this);
    }
    
    public void testSubscribe()
        throws Exception
    {
        mPublisher.subscribe(null);
        mPublisher.publishAndWait(this);
        MockSubscriber s = new MockSubscriber();
        mPublisher.subscribe(s);
        assertEquals(0,
                     s.getPublishCount());
        assertNull(s.getData());
        mPublisher.publishAndWait(this);
        assertEquals(this,
                     s.getData());
        assertEquals(1,
                     s.getPublishCount());
        // subscribe again, make sure we get only only publication
        s.setData(null);
        mPublisher.subscribe(s);
        assertEquals(1,
                     s.getPublishCount());
        assertNull(s.getData());
        mPublisher.publishAndWait(this);
        assertEquals(this,
                     s.getData());
        assertEquals(2, // not 3!
                     s.getPublishCount());
    }
    
    public void testUnsubscribe()
        throws Exception
    {
        mPublisher.unsubscribe(null);
        mPublisher.publish(this);
        MockSubscriber s = new MockSubscriber();
        assertEquals(0,
                     s.getPublishCount());
        assertEquals(null,
                     s.getData());
        mPublisher.unsubscribe(s);
        mPublisher.publish(this);
        assertEquals(0,
                     s.getPublishCount());
        assertEquals(null,
                     s.getData());
        mPublisher.subscribe(s);
        mPublisher.publish(this);
        while(s.getPublishCount() == 0) {
            Thread.sleep(100);
        }
        assertEquals(this,
                     s.getData());
        assertEquals(1,
                     s.getPublishCount());
        s.setData(null);
        mPublisher.unsubscribe(s);
        mPublisher.publish(this);
        Thread.sleep(5000);
        assertEquals(1,
                     s.getPublishCount());
        assertEquals(null,
                     s.getData());
    }
    
    public void testParallel()
        throws Exception
    {
        MockPublisher[] publishers = new MockPublisher[50];
        for(int i=0;i<50;i++) {
            publishers[i] = new MockPublisher();
        }
        MockSubscriber[] subscribers = new MockSubscriber[500];
        for(int i=0;i<subscribers.length;i++) {
            subscribers[i] = new MockSubscriber();
        }
        
        Thread[] threads = new Thread[20];
        for(int i=0;i<20;i++) {
            threads[i] = new Thread(new Tester(publishers,
                                               subscribers));
            threads[i].start();
        }
        for(Thread t : threads) {
            t.join();
        }
    }
    
    public static class Tester
        implements Runnable
    {
        private Random r = new Random(System.nanoTime());
        private MockPublisher[] mPublishers;
        private MockSubscriber[] mSubscribers;
        
        public Tester(MockPublisher[] inPublishers,
                      MockSubscriber[] inSubscribers)
        {
            mPublishers = inPublishers;
            mSubscribers = inSubscribers; 
        }
        
        public void run()
        {
            for(int i=0;i<10;i++) {
                for(MockSubscriber s : mSubscribers) {
                    int publisher = r.nextInt(50);
                    int flag = r.nextInt(3);
                    switch(flag) {
                        case 0:
                            mPublishers[publisher].subscribe(s);
                            break;
                        case 1:
                            mPublishers[publisher].unsubscribe(s);
                            break;
                        case 2:
                            mPublishers[publisher].publish(this);
                            break;
                    }
                }
            }        
        }
    }
}

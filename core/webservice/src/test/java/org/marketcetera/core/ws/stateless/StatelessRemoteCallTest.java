package org.marketcetera.core.ws.stateless;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @since 1.0.0
 * @version $Id: StatelessRemoteCallTest.java 82324 2012-04-09 20:56:08Z colin $
 */

/* $License$ */

public class StatelessRemoteCallTest
    extends StatelessRemoteCallTestBase
{
    @Test
    public void all()
        throws Exception
    {
        single
            (new StatelessRemoteCall
             (TEST_VERSION_FILTER,TEST_APP_FILTER,TEST_CLIENT_FILTER),
             new StatelessRemoteCall(null,null,null),
             new StatelessRemoteCall());
        assertNotNull(StatelessRemoteCall.DEFAULT_VERSION_FILTER);
    }
}

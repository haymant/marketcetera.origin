package org.marketcetera.client;

import org.junit.Test;
import org.marketcetera.core.ApplicationVersion;
import org.marketcetera.util.test.TestCaseBase;
import org.marketcetera.util.ws.tags.AppId;

import static org.junit.Assert.*;

/**
 * @author tlerios@marketcetera.com
 * @since 1.5.0
 * @version $Id$
 */

/* $License$ */

public class ClientVersionTest
    extends TestCaseBase
{
    @Test
    public void basics()
    {
        assertNull(ClientVersion.getVersion(null));
        assertNull(ClientVersion.getVersion(new AppId(null)));
        assertNull(ClientVersion.getVersion(new AppId("any")));
        assertNull(ClientVersion.getVersion
                   (new AppId(ClientVersion.APP_ID_VERSION_SEPARATOR)));
        assertEquals("x",ClientVersion.getVersion
                     (new AppId(ClientVersion.APP_ID_VERSION_SEPARATOR+"x")));
        assertEquals(ClientVersion.APP_ID_VERSION,
                     ClientVersion.getVersion(ClientVersion.APP_ID));

        assertFalse(ClientVersion.compatibleVersions
                    (null,"x"));
        assertFalse(ClientVersion.compatibleVersions
                    ("x",null));
        assertFalse(ClientVersion.compatibleVersions
                    ("x","y"));

        assertFalse(ApplicationVersion.DEFAULT_VERSION.equals("x"));
        assertTrue(ClientVersion.compatibleVersions
                   ("x","x"));
        assertTrue(ClientVersion.compatibleVersions
                   ("x",ApplicationVersion.DEFAULT_VERSION));
    }
}

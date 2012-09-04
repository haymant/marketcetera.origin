package org.marketcetera.core.util.l10n;

import java.util.Properties;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @since 0.8.0
 * @version $Id: MessageInfoTest.java 16063 2012-01-31 18:21:55Z colin $
 */

/* $License$ */

public class MessageInfoTest
    extends MessageInfoTestBase
{
    @Test
    public void all()
    {
        assertEquals(0,MessageInfo.getList(new MessageInfo[0]).size());

        Properties p=MessageInfo.getList(new MessageInfo[] {
            TEST_PROPERTY_INFO_PCD,
            TEST_I18N_INFO_KD});
        assertEquals(TEST_KEY+" {0}{1}",p.get(TEST_KEY));
        assertEquals(TEST_KEY_D+" {0}",p.get(TEST_KEY_D));
        assertEquals(2,p.size());
        
        p=MessageInfo.getList(new MessageInfo[] {
            TEST_PROPERTY_INFO_PCD,
            TEST_I18N_INFO});
        assertEquals(TEST_KEY+" {0}",p.get(TEST_KEY));
        assertEquals(1,p.size());
    }
}

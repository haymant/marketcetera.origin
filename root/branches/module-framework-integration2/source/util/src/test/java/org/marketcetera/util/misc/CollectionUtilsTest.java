package org.marketcetera.util.misc;

import java.util.Arrays;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.marketcetera.util.test.TestCaseBase;

import static org.junit.Assert.*;

/**
 * @author tlerios@marketcetera.com
 * @since 0.6.0
 * @version $Id$
 */

/* $License$ */

public class CollectionUtilsTest
    extends TestCaseBase
{
    @Test
    public void getLastNonNull()
    {
        assertNull(CollectionUtils.getLastNonNull(null));
        assertNull(CollectionUtils.getLastNonNull
                   (Arrays.asList(new Integer[] {})));
        assertNull(CollectionUtils.getLastNonNull
                   (Arrays.asList(new Integer[] {null})));
        assertEquals(new Integer(1),CollectionUtils.getLastNonNull
                     (Arrays.asList(1)));
        assertEquals(new Integer(2),CollectionUtils.getLastNonNull
                     (Arrays.asList(1,null,2,null)));
        assertEquals(new Integer(2),CollectionUtils.getLastNonNull
                     (Arrays.asList(1,null,null,2)));
        assertEquals(new Integer(1),CollectionUtils.getLastNonNull
                     (Arrays.asList(1,null,null)));
    }

    @Test
    public void toArray()
    {
        assertNull(CollectionUtils.toArray(null));
        assertArrayEquals
            (ArrayUtils.EMPTY_INT_ARRAY,
             CollectionUtils.toArray(Arrays.asList(new Integer[] {})));
        assertArrayEquals
            (ArrayUtils.EMPTY_INT_ARRAY,
             CollectionUtils.toArray(Arrays.asList(new Integer[] {null})));
        assertArrayEquals
            (new int[] {1},
             CollectionUtils.toArray(Arrays.asList(1)));
        assertArrayEquals
            (new int[] {1,2},
             CollectionUtils.toArray(Arrays.asList(null,1,null,2,null)));
    }
}

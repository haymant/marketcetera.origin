package org.marketcetera.core.trade;

import java.util.Arrays;
import java.util.List;

import org.marketcetera.api.attributes.ClassVersion;

/* $License$ */
/**
 * Base class for testing FIX char value based enums.
 *
 * @param <E> The Enum type.
 *
 * @author anshul@marketcetera.com
 * @version $Id: FIXCharEnumTestBase.java 16063 2012-01-31 18:21:55Z colin $
 * @since 1.0.0
 */
@ClassVersion("$Id: FIXCharEnumTestBase.java 16063 2012-01-31 18:21:55Z colin $") //$NON-NLS-1$
public abstract class FIXCharEnumTestBase<E extends Enum<E>>
        extends FIXEnumTestBase<Character,E>{
    @Override
    protected List<Character> unknownFIXValues() {
        return Arrays.asList(' ', '?', Character.MIN_VALUE,
                Character.MAX_VALUE);
    }
}

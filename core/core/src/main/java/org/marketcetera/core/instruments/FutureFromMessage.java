package org.marketcetera.core.instruments;

import org.marketcetera.api.systemmodel.instruments.Future;
import org.marketcetera.api.systemmodel.instruments.Instrument;
import org.marketcetera.core.trade.FutureImpl;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.CFICode;
import quickfix.field.SecurityType;

/* $License$ */

/**
 * Extracts <code>Future</code> from a <code>Message</code>.
 *
 * @version $Id: FutureFromMessage.java 16063 2012-01-31 18:21:55Z colin $
 * @since 2.1.0
 */
public class FutureFromMessage
        extends InstrumentFromMessage
{
    /* (non-Javadoc)
     * @see org.marketcetera.core.instruments.InstrumentFromMessage#extract(quickfix.Message)
     */
    @Override
    public Instrument extract(Message inMessage)
    {
        // if the symbol already contains the expiry information, go ahead and create it
        // if it doesn't, piece the expiry onto the symbol and use that
        String symbol = getSymbol(inMessage);
        if(symbol == null) {
            return null;
        }
        try {
            return FutureImpl.fromString(symbol);
        } catch (IllegalArgumentException ignored) {}
        // assume the symbol is the underlying, not the full symbol (this is the common case, it seems)
        // therefore, use the multi-argument constructor instead
        String expiry = OptionFromMessage.getExpiry(inMessage);
        if(expiry == null) {
            return null;
        }
        Future future = new FutureImpl(symbol,
                                       expiry);
        return future;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.core.instruments.DynamicInstrumentHandler#isHandled(java.lang.Object)
     */
    @Override
    protected boolean isHandled(Message inValue)
    {
        try {
            return (inValue.isSetField(SecurityType.FIELD) &&
                    SecurityType.FUTURE.equals(inValue.getString(SecurityType.FIELD))) ||
                    (inValue.isSetField(CFICode.FIELD) &&
                            CFICodeUtils.isFuture(inValue.getString(CFICode.FIELD)));
        } catch (FieldNotFound ignore) {
            return false;
        }
    }
}

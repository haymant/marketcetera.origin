package org.marketcetera.core.util.log;

import java.io.Serializable;

/**
 * A bound message, representing the combination of an {@link
 * I18NMessage2P} and its two parameters.
 * 
 * @author tlerios@marketcetera.com
 * @since 0.5.0
 * @version $Id: I18NBoundMessage2P.java 16063 2012-01-31 18:21:55Z colin $
 */

/* $License$ */

public class I18NBoundMessage2P
    extends I18NBoundMessageBase<I18NMessage2P>
{

    // CLASS DATA.

    private static final long serialVersionUID=1L;


    // CONSTRUCTORS.

    /**
     * Constructor mirroring superclass constructor.
     *
     * @see I18NBoundMessageBase#I18NBoundMessageBase(I18NMessage,Serializable...)
     */

    public I18NBoundMessage2P
        (I18NMessage2P message,
         Serializable p1,
         Serializable p2)
    {
        super(message,p1,p2);
    }


    // INSTANCE METHODS.
    
    /**
     * Returns the receiver's first parameter.
     *
     * @return The parameter.
     */

    public Serializable getParam1()
    {
        return getParams()[0];
    }

    /**
     * Returns the receiver's second parameter.
     *
     * @return The parameter.
     */

    public Serializable getParam2()
    {
        return getParams()[1];
    }
}

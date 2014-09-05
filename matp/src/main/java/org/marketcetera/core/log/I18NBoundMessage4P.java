package org.marketcetera.core.log;

import java.io.Serializable;

import org.marketcetera.core.misc.ClassVersion;

/**
 * A bound message, representing the combination of an {@link
 * I18NMessage4P} and its four parameters.
 * 
 * @author tlerios@marketcetera.com
 * @since 0.5.0
 * @version $Id: I18NBoundMessage4P.java 16154 2012-07-14 16:34:05Z colin $
 */

/* $License$ */

@ClassVersion("$Id: I18NBoundMessage4P.java 16154 2012-07-14 16:34:05Z colin $")
public class I18NBoundMessage4P
    extends I18NBoundMessageBase<I18NMessage4P>
{

    // CLASS DATA.

    private static final long serialVersionUID=1L;


    // CONSTRUCTORS.

    /**
     * Constructor mirroring superclass constructor.
     *
     * @see I18NBoundMessageBase#I18NBoundMessageBase(I18NMessage,Serializable...)
     */

    public I18NBoundMessage4P
        (I18NMessage4P message,
         Serializable p1,
         Serializable p2,
         Serializable p3,
         Serializable p4)
    {
        super(message,p1,p2,p3,p4);
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

    /**
     * Returns the receiver's third parameter.
     *
     * @return The parameter.
     */

    public Serializable getParam3()
    {
        return getParams()[2];
    }

    /**
     * Returns the receiver's fourth parameter.
     *
     * @return The parameter.
     */

    public Serializable getParam4()
    {
        return getParams()[3];
    }
}

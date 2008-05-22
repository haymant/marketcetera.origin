package org.marketcetera.util.log;

/**
 * An internationalized message, accepting an arbitrary number of
 * parameters.
 * 
 * @author tlerios@marketcetera.com
 * @since 0.5.0
 * @version $Id$
 */

/* $License$ */

import java.util.Locale;
import org.marketcetera.core.ClassVersion;

@ClassVersion("$Id$")
public class I18NMessageNP
    extends I18NMessage
{

    // CONSTRUCTORS.

    /**
     * Constructor mirroring superclass constructor.
     *
     * @see I18NMessage#I18NMessage(I18NLoggerProxy,String,String)
     */

    public I18NMessageNP
        (I18NLoggerProxy loggerProxy,
         String messageId,
         String entryId)
    {
        super(loggerProxy,messageId,entryId);
    }

    /**
     * Constructor mirroring superclass constructor.
     *
     * @see I18NMessage#I18NMessage(I18NLoggerProxy,String)
     */

    public I18NMessageNP
        (I18NLoggerProxy loggerProxy,
         String messageId)
    {
        super(loggerProxy,messageId);
    }


    // INSTANCE METHODS.

    /**
     * A convenience method for {@link
     * I18NMessageProvider#getText(Locale,I18NMessage,Object...)}.
     */

    public String getText
        (Locale locale,
         Object... ps)
    {
        return getMessageProvider().getText(locale,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NMessageProvider#getText(I18NMessage,Object...)}.
     */

    public String getText
        (Object... ps)
    {
        return getMessageProvider().getText(this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#error(Object,Throwable,I18NMessage,Object...)}.
     */

    public void error
        (Object category,
         Throwable throwable,
         Object... ps)
    {
        getLoggerProxy().error(category,throwable,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#error(Object,I18NMessage,Object...)}.
     */
    
    public void error
        (Object category,
         Object... ps)
    {
        getLoggerProxy().error(category,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#warn(Object,Throwable,I18NMessage,Object...)}.
     */

    public void warn
        (Object category,
         Throwable throwable,
         Object... ps)
    {
        getLoggerProxy().warn(category,throwable,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#warn(Object,I18NMessage,Object...)}.
     */
    
    public void warn
        (Object category,
         Object... ps)
    {
        getLoggerProxy().warn(category,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#info(Object,Throwable,I18NMessage,Object...)}.
     */

    public void info
        (Object category,
         Throwable throwable,
         Object... ps)
    {
        getLoggerProxy().info(category,throwable,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#info(Object,I18NMessage,Object...)}.
     */
    
    public void info
        (Object category,
         Object... ps)
    {
        getLoggerProxy().info(category,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#debug(Object,Throwable,I18NMessage,Object...)}.
     */

    public void debug
        (Object category,
         Throwable throwable,
         Object... ps)
    {
        getLoggerProxy().debug(category,throwable,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#debug(Object,I18NMessage,Object...)}.
     */
    
    public void debug
        (Object category,
         Object... ps)
    {
        getLoggerProxy().debug(category,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#trace(Object,Throwable,I18NMessage,Object...)}.
     */

    public void trace
        (Object category,
         Throwable throwable,
         Object... ps)
    {
        getLoggerProxy().trace(category,throwable,this,ps);
    }

    /**
     * A convenience method for {@link
     * I18NLoggerProxy#trace(Object,I18NMessage,Object...)}.
     */
    
    public void trace
        (Object category,
         Object... ps)
    {
        getLoggerProxy().trace(category,this,ps);
    }
}

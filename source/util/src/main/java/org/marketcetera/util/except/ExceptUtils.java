package org.marketcetera.util.except;

import java.io.InterruptedIOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileLockInterruptionException;
import javax.naming.InterruptedNamingException;
import org.marketcetera.core.ClassVersion;
import org.marketcetera.util.log.I18NBoundMessage0P;
import org.marketcetera.util.log.I18NBoundMessage;
import org.marketcetera.util.log.I18NLoggerProxy;

/**
 * General-purpose utilities.
 * 
 * @author tlerios
 * @version $Id$
 */

/* $License$ */

@ClassVersion("$Id$")
public final class ExceptUtils
{

    // CLASS METHODS.

    /**
     * Checks whether the calling thread has been interrupted, and, if
     * so, throws an {@link InterruptedException} with the default
     * interruption message and no underlying cause. The interrupted
     * status of the thread is cleared.
     *
     * @throws InterruptedException Thrown if the calling thread
     * was interrupted.
     */

    public static void checkInterruption()
        throws InterruptedException
    {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException
                (Messages.THREAD_INTERRUPTED.getText());
        }
    }

    /**
     * Checks whether the calling thread has been interrupted, and, if
     * so, throws an {@link InterruptedException} with the given
     * interruption message, but without an underlying cause. The
     * interrupted status of the thread is cleared.
     *
     * @param message The message.
     *
     * @throws InterruptedException Thrown if the calling thread
     * was interrupted.
     */

    public static void checkInterruption
        (String message)
        throws InterruptedException
    {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException(message);
        }
    }

    /**
     * Checks whether the calling thread has been interrupted, and, if
     * so, throws an {@link InterruptedException} with the default
     * interruption message and the given underlying cause. The given
     * underlying cause is set on the thrown exception. The
     * interrupted status of the thread is cleared.
     *
     * @param cause The cause.
     *
     * @throws InterruptedException Thrown if the calling thread
     * was interrupted.
     */

    public static void checkInterruption
        (Throwable cause)
        throws InterruptedException
    {
        if (Thread.currentThread().isInterrupted()) {
            InterruptedException ex=new InterruptedException
                (Messages.THREAD_INTERRUPTED.getText());
            ex.initCause(cause);
            throw ex;
        }
    }

    /**
     * Checks whether the calling thread has been interrupted, and, if
     * so, throws an {@link InterruptedException} with the given
     * interruption message and the given underlying cause. The given
     * underlying cause is set on the thrown exception. The
     * interrupted status of the thread is cleared.
     *
     * @param cause The cause.
     * @param message The message.
     *
     * @throws InterruptedException Thrown if the calling thread
     * was interrupted.
     */

    public static void checkInterruption
        (Throwable cause,
         String message)
        throws InterruptedException
    {
        if (Thread.currentThread().isInterrupted()) {
            InterruptedException ex=new InterruptedException(message);
            ex.initCause(cause);
            throw ex;
        }
    }

    /**
     * Checks if the given throwable is an instance of {@link
     * InterruptedException}, {@link InterruptedIOException}, {@link
     * I18NInterruptedException}, or {@link
     * I18NInterruptedRuntimeException}.
     * 
     * @param throwable The throwable.
     *
     * @return True if so.
     */

    public static boolean isInterruptException
        (Throwable throwable)
    {
        return ((throwable instanceof InterruptedException) ||
                (throwable instanceof InterruptedIOException) ||
                (throwable instanceof ClosedByInterruptException) ||
                (throwable instanceof FileLockInterruptionException) ||
                (throwable instanceof InterruptedNamingException) ||
                (throwable instanceof I18NInterruptedException) ||
                (throwable instanceof I18NInterruptedRuntimeException));
    }

    /**
     * If the given throwable is an interruption exception per {@link
     * #isInterruptException(Throwable)}, then the calling thread is
     * interrupted. Otherwise, this is a no-op.
     * 
     * @param throwable The throwable.
     *
     * @return True if the calling thread was interrupted.
     */

    public static boolean interrupt
        (Throwable throwable)
    {
        if (isInterruptException(throwable)) {
            Thread.currentThread().interrupt();
            return true;
        }
        return false;
    }

    /**
     * Swallows the given throwable. It logs the given parameterized
     * message and throwable under the given logging category at the
     * warning level. Also, if the given throwable is an interruption
     * exception per {@link #isInterruptException(Throwable)}, then
     * the calling thread is interrupted.
     * 
     * @param throwable The throwable.
     * @param category The category.
     * @param message The message.
     *
     * @return True if the calling thread was interrupted.
     */

    public static boolean swallow
        (Throwable throwable,
         Object category,
         I18NBoundMessage message)
    {
        message.warn(category,throwable);
        return interrupt(throwable);
    }

    /**
     * Swallows the given throwable. It logs a default message
     * alongside the throwable at the warning level. Also, if the
     * given throwable is an interruption exception per {@link
     * #isInterruptException(Throwable)}, then the calling thread is
     * interrupted.
     * 
     * @param throwable The throwable.
     *
     * @return True if the calling thread was interrupted.
     */

    public static boolean swallow
        (Throwable throwable)
    {
        return swallow(throwable,ExceptUtils.class,Messages.THROWABLE_IGNORED);
    }

    /**
     * If the given throwable is an interruption exception per {@link
     * #isInterruptException(Throwable)}, then the throwable is
     * wrapped inside a {@link I18NInterruptedException}, and this
     * exception is returned; also, the calling thread is
     * interrupted. Otherwise, an {@link I18NException} is used to
     * wrap the throwable, and returned. All arguments are passed
     * as-is into the constructor of the wrapping exception.
     * 
     * @param throwable The throwable.
     * @param message The message.
     *
     * @return The wrapping exception.
     */

    public static I18NException wrap
        (Throwable throwable,
         I18NBoundMessage message)
    {
        if (isInterruptException(throwable)) {
            Thread.currentThread().interrupt();
            return new I18NInterruptedException(throwable,message);
        }
        return new I18NException(throwable,message);
    }

    /**
     * If the given throwable is an interruption exception per {@link
     * #isInterruptException(Throwable)}, then the throwable is
     * wrapped inside a {@link I18NInterruptedException}, and this
     * exception is returned; also, the calling thread is
     * interrupted. Otherwise, an {@link I18NException} is used to
     * wrap the throwable, and returned. All arguments are passed
     * as-is into the constructor of the wrapping exception.
     * 
     * @param throwable The throwable.
     *
     * @return The wrapping exception.
     */

    public static I18NException wrap
        (Throwable throwable)
    {
        if (isInterruptException(throwable)) {
            Thread.currentThread().interrupt();
            return new I18NInterruptedException(throwable);
        }
        return new I18NException(throwable);
    }

    /**
     * If the given throwable is an interruption exception per {@link
     * #isInterruptException(Throwable)}, then the throwable is
     * wrapped inside a {@link I18NInterruptedRuntimeException}, and
     * this exception is thrown; also, the calling thread is
     * interrupted. Otherwise, an {@link I18NRuntimeException} is used
     * to wrap the throwable. All arguments are passed as-is into the
     * constructor of the wrapping exception.
     * 
     * @param throwable The throwable.
     * @param message The message.
     *
     * @return The wrapping exception.
     */

    public static I18NRuntimeException wrapRuntime
        (Throwable throwable,
         I18NBoundMessage message)
    {
        if (isInterruptException(throwable)) {
            Thread.currentThread().interrupt();
            return new I18NInterruptedRuntimeException(throwable,message);
        }
        return new I18NRuntimeException(throwable,message);
    }

    /**
     * If the given throwable is an interruption exception per {@link
     * #isInterruptException(Throwable)}, then the throwable is
     * wrapped inside a {@link I18NInterruptedRuntimeException}, and
     * this exception is thrown; also, the calling thread is
     * interrupted. Otherwise, an {@link I18NRuntimeException} is used
     * to wrap the throwable. All arguments are passed as-is into the
     * constructor of the wrapping exception.
     * 
     * @param throwable The throwable.
     *
     * @return The wrapping exception.
     */

    public static I18NRuntimeException wrapRuntime
        (Throwable throwable)
    {
        if (isInterruptException(throwable)) {
            Thread.currentThread().interrupt();
            return new I18NInterruptedRuntimeException(throwable);
        }
        return new I18NRuntimeException(throwable);
    }


    // CONSTRUCTORS.

    /**
     * Constructor. It is private so that no instances can be created.
     */

    private ExceptUtils() {}
}
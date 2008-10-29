package org.marketcetera.module;

import org.marketcetera.util.misc.ClassVersion;
import org.marketcetera.util.log.I18NMessage;
import org.marketcetera.util.except.I18NException;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

/* $License$ */
/**
 * A utility class for testing failure conditions in unit tests.
 *
 * <p>
 * The code that is being tested is implemented within the
 * {@link #run()} method.
 *<p>
 * The type parameter <code>T</code> reflects the actual type
 * of exception that is expected to be thrown from the {@link #run()}
 * method.
 *<p> 
 * The class can be used to test failures as follows.
 *
 * <pre>
 * // Testing failures that throw I18NException subclass
 * new ExpectedFailure&lt;I18NExceptionSubClass&gt;(Messages.FAILURE_MESSAGE,parameter1) {
 *   protected void run() throws Exception {
 *     // The code that is expected to throw I18NExceptionSubClass
 *     // with <code>Messages.FAILURE_MESSAGE</code> message that has a
 *     // parameter <code>parameter1</code> 
 *   }
 * }
 *
 *
 * // Testing failures that throw any java Exception subclass
 * new ExpectedFailure&lt;ExceptionSubClass&gt;(Messages.FAILURE_MESSAGE.getText()) {
 *   protected void run() throws Exception {
 *     // The code that is expected to throw ExceptionSubClass
 *     // with <code>Messages.FAILURE_MESSAGE</code> message
 *   }
 * }
 * </pre>
 *
 * If the caller needs to do further verification of the exception
 * than is done by this class, they can get the caught exception by
 * calling {@link #getException()} 
 *
 * @author anshul@marketcetera.com
 */
@ClassVersion("$Id$")
public abstract class ExpectedFailure<T extends Exception> {

    /**
     * Returns the exception instance, for further verification, if needed.
     *
     * @return the exception instance.
     */
    public T getException() {
        return mException;
    }
    /**
     * Creates an instance that will test for failures with I18NExceptions
     *
     * @param inExpectedMessage the expected message in the exception, null
     * if the message need not be tested.
     * @param inExpectedParams the expected parameters to the message, null
     * if the parameters need not be tested.
     *
     * @throws Exception if there are unexpected failures.
     */
    protected ExpectedFailure(I18NMessage inExpectedMessage,
                              Object... inExpectedParams) throws Exception {
        mExpectedMessage = inExpectedMessage;
        mExpectedParams = inExpectedParams;
        doRun();
    }

    /**
     * Creates an instance that will test for failures with any java
     * Exception.
     *
     * @param inMessage the exception message.
     *
     * @throws Exception if there was an unexpected failure
     */
    protected ExpectedFailure(String inMessage) throws Exception {
        this(inMessage, true);
    }
    /**
     * Creates an instance that will test for failures with any java
     * Exception.
     *
     * @param inMessage the exception message.
     * @param inExactMatch true if the message should match exactly, false
     * if the generated message contains the supplied message.
     *
     * @throws Exception if there was an unexpected failure
     */
    protected ExpectedFailure(String inMessage, boolean inExactMatch)
            throws Exception {
        mMessage = inMessage;
        mExactMatch = inExactMatch;
        doRun();
    }

    /**
     * Subclasses should implement this method to execute
     * code that is expected to fail with the exception of type
     * <code>T<code>
     *
     * @throws Exception if there's a failure.
     */
    protected abstract void run() throws Exception;

    /**
     * Runs the test code and verifies the exception.
     *
     * @throws Exception runs the test code and verifies the exception.
     */
    @SuppressWarnings("unchecked")
    private void doRun() throws Exception {
        try {
            run();
            fail("Didn't fail!");
        } catch(Exception t) {
            Class expected = getExceptionClass();
            assertTrue("Expected<" + expected + ">Actual<"+t.getClass()+">" + t,
                    expected.isInstance(t));
            mException = (T) t;
            if(t instanceof I18NException) {
                I18NException e = (I18NException) t;
                if(mExpectedMessage != null) {
                    assertEquals(t.toString(), mExpectedMessage, e.getI18NBoundMessage().getMessage());
                }
                if(mExpectedParams != null && mExpectedParams.length > 0) {
                    assertArrayEquals(t.toString(),  mExpectedParams, e.getI18NBoundMessage().getParams());
                }
            } else {
                assertNull(t.getClass() + " is not an I18NException", mExpectedMessage);
            }
            if(mMessage != null) {
                if (mExactMatch) {
                    assertEquals(mMessage, t.getMessage());
                } else {
                    assertTrue(t.getMessage(), t.getMessage().contains(mMessage));
                }
            }
        }
    }

    /**
     * Extracts the exception type specified as the value of parameter
     * <code>T</code>from the class metadata.
     *
     * @return the expected exception type.
     */
    private Class getExceptionClass() {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] t = pt.getActualTypeArguments();
        assertEquals(1, t.length);
        return (Class) t[0];
    }
    private I18NMessage mExpectedMessage;
    private Object[] mExpectedParams;
    private T mException;
    private String mMessage;
    private boolean mExactMatch;
}
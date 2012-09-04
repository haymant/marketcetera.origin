package org.marketcetera.core.metrics;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.marketcetera.core.LoggerConfiguration;

import static org.junit.Assert.*;

/* $License$ */
/**
 * Tests {@link org.marketcetera.core.metrics.PrintStreamFactory} subclasses, {@link org.marketcetera.core.metrics.FileStreamFactory} &
 * {@link org.marketcetera.core.metrics.StdErrFactory}
 *
 * @version $Id: PrintStreamFactoryTest.java 16063 2012-01-31 18:21:55Z colin $
 * @since 2.0.0
 */
public class PrintStreamFactoryTest {
    @BeforeClass
    public static void setup() {
        LoggerConfiguration.logSetup();
    }

    /**
     * Tests the {@link org.marketcetera.core.metrics.StdErrFactory}
     *
     * @throws Exception if there were errors.
     */
    @Test
    public void stderrFactory() throws Exception {
        PrintStream stderr = System.err;
        MyStream stream = new MyStream();
        System.setErr(stream);
        try {
            PrintStream createdStream = StdErrFactory.INSTANCE.getStream("blue");
            assertSame(stream, createdStream);
            assertFalse(stream.isCloseCalled());
            assertFalse(stream.isFlushCalled());
            StdErrFactory.INSTANCE.done(createdStream);
            assertFalse(stream.isCloseCalled());
            assertTrue(stream.isFlushCalled());
        } finally {
            System.setErr(stderr);
        }

    }

    /**
     * Tests the {@link org.marketcetera.core.metrics.FileStreamFactory}.
     *
     * @throws Exception if there were errors.
     */
    @Test
    public void fileStreamFactory() throws Exception {
        final PrintStream stream = FileStreamFactory.INSTANCE.getStream("name");
        stream.print("test");
        FileStreamFactory.INSTANCE.done(stream);
        assertFalse(stream.checkError());
        //try writing
        stream.write(12);
        //the failure of write() above results in setting of the error flag.
        assertTrue(stream.checkError());
    }

    /**
     * A test stream that writes output into a byte array.
     * The class is used to verify whether {@link java.io.PrintStream#close()}
     * & {@link java.io.PrintStream#flush()} operations are being correctly
     * invoked.
     */
    private static class MyStream extends PrintStream {
        /**
         * Creates an instance.
         *
         */
        public MyStream() {
            super(new ByteArrayOutputStream());
        }

        @Override
        public void close() {
            super.close();
            mCloseCalled = true;
        }

        @Override
        public void flush() {
            super.flush();
            mFlushCalled = true;
        }

        /**
         *
         * @return if the {@link #close()} was invoked.
         */
        public boolean isCloseCalled() {
            return mCloseCalled;
        }

        /**
         *
         * @return if the {@link #flush()} was invoked.
         */
        public boolean isFlushCalled() {
            return mFlushCalled;
        }

        private boolean mCloseCalled;
        private boolean mFlushCalled;
    }
}

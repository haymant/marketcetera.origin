package org.marketcetera.core.util.misc;

import java.lang.annotation.Annotation;
import org.junit.Test;
import org.marketcetera.api.attributes.ClassVersion;
import org.marketcetera.util.test.TestCaseBase;

import static org.junit.Assert.*;

/**
 * @author tlerios@marketcetera.com
 * @since 0.5.0
 * @version $Id: ClassVersionTest.java 16063 2012-01-31 18:21:55Z colin $
 */

/* $License$ */

public class ClassVersionTest
    extends TestCaseBase
{
    private static final String TEST_VERSION="version";

    @ClassVersion(TEST_VERSION)
    private static class TestClass
    {
    }

    @Test
    public void annotationExists()
    {
        Annotation[] annonations=TestClass.class.getAnnotations();
        assertEquals(1,annonations.length);
        assertEquals(ClassVersion.class,annonations[0].annotationType());
        assertEquals(TEST_VERSION,((ClassVersion)annonations[0]).value());
    }
}

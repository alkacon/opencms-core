/*
 * Created on 28.02.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.opencms.file;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author thomas
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.opencms.file");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(CmsImportTest.class));
        //$JUnit-END$
        return suite;
    }
}

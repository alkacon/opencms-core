/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/module/AllTests.java,v $
 * Date   : $Date: 2004/11/25 13:04:33 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.module;

import org.opencms.test.OpenCmsTestProperties;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test cases for modules.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 5.0
 */
public final class AllTests {

    /**
     * Hide constructor to prevent generation of class instances.<p>
     */
    private AllTests() {
        // empty
    }
    
    /**
     * Returns the JUnit test suite for this package.<p>
     * 
     * @return the JUnit test suite for this package
     */        
    public static Test suite() {
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        TestSuite suite = new TestSuite("Tests for package org.opencms.module");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(TestCmsModuleNameGeneration.class));
        suite.addTest(new TestSuite(TestCmsModuleVersion.class));
        suite.addTest(TestModuleOperations.suite());
        suite.addTest(TestModuleIssues.suite());  
        suite.addTest(TestModuleLegacy.suite());
        //$JUnit-END$
        return suite;
    }
}

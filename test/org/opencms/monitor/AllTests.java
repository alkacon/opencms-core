/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/monitor/AllTests.java,v $
 * Date   : $Date: 2004/11/24 15:57:25 $
 * Version: $Revision: 1.2 $
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

package org.opencms.monitor; 

import org.opencms.test.OpenCmsTestPropertiesSingleton;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
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
        OpenCmsTestPropertiesSingleton.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        TestSuite suite = new TestSuite("Tests for package org.opencms.monitor");
        //$JUnit-BEGIN$
        suite.addTest(TestMemoryMonitor.suite());
        //$JUnit-END$
        return suite;
    }
}

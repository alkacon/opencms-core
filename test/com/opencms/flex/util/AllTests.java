/*
 * File   : $Source: /alkacon/cvs/opencms/test/com/opencms/flex/util/Attic/AllTests.java,v $
 * Date   : $Date: 2003/07/12 11:29:23 $
 * Version: $Revision: 1.7 $
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

package com.opencms.flex.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.7 $
 * 
 * @since 5.0
 */
public final class AllTests {

    /**
     * Hide constructor to prevent generation of class instances.<p>
     */
    private AllTests() {
    }
    
    /**
     * Returns the JUnit test suite for this package.<p>
     * 
     * @return the JUnit test suite for this package
     */        
    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for package com.opencms.flex.util");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(CmsResourceTranslatorTest.class));
        suite.addTest(new TestSuite(CmsStringSubstitutionTest.class));
        suite.addTest(new TestSuite(CmsUUIDTest.class));
        suite.addTest(new TestSuite(CmsBrowserMatcherTest.class));
        //$JUnit-END$
        return suite;
    }
}

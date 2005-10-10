/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/AllTests.java,v $
 * Date   : $Date: 2005/10/10 16:11:08 $
 * Version: $Revision: 1.40 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.test.OpenCmsTestProperties;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Main test suite for the package <code>{@link org.opencms.file}</code>.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.40 $
 * 
 * @since 6.0
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

        TestSuite suite = new TestSuite("Tests for package " + AllTests.class.getPackage().getName());
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        //$JUnit-BEGIN$
        suite.addTest(TestChacc.suite());
        suite.addTest(TestCopy.suite());
        suite.addTest(TestChtype.suite());
        suite.addTest(TestCreateWriteResource.suite());
        suite.addTest(TestDefaultResourceCollectors.suite());
        suite.addTest(TestDeletion.suite());
        suite.addTest(TestExists.suite());
        suite.addTest(TestLock.suite());
        suite.addTest(TestMoveRename.suite());
        suite.addTest(TestPermissions.suite());
        suite.addTest(TestPriorityResourceCollectors.suite());
        suite.addTest(TestProjects.suite());
        suite.addTest(TestProperty.suite());
        suite.addTest(TestPropertyDefinition.suite());
        suite.addTest(TestPublishing.suite());
        suite.addTest(TestPublishIssues.suite());
        suite.addTest(TestReadResource.suite());
        suite.addTest(TestReadResourceTree.suite());
        suite.addTest(TestReleaseExpire.suite());
        suite.addTest(TestReplace.suite());
        suite.addTest(TestResourceOperations.suite());
        suite.addTest(TestRestoreFromHistory.suite());
        suite.addTest(TestSiblings.suite());
        suite.addTest(TestTouch.suite());
        suite.addTest(TestUndoChanges.suite());
        suite.addTest(TestBackup.suite());
        suite.addTest(TestChangeProperties.suite());
        //$JUnit-END$
        return suite;
    }
}
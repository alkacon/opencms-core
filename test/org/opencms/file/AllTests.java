/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
        suite.addTest(TestAvailability.suite());
        suite.addTest(TestChacc.suite());
        suite.addTest(TestChangeProperties.suite());
        suite.addTest(TestChflags.suite());
        suite.addTest(TestChtype.suite());
        suite.addTest(TestConcurrentOperations.suite());
        suite.addTest(TestCopy.suite());
        suite.addTest(TestCreateWriteResource.suite());
        suite.addTest(TestDefaultResourceCollectors.suite());
        suite.addTest(TestDeletion.suite());
        suite.addTest(TestExists.suite());
        suite.addTest(TestGroupOperations.suite());
        suite.addTest(TestHistory.suite());
        suite.addTest(TestLinkValidation.suite());
        suite.addTest(TestLock.suite());
        suite.addTest(TestMoveRename.suite());
        suite.addTest(TestMoveRename2.suite());
        suite.addTest(TestMoveRename3.suite());
        suite.addTest(TestPermissions.suite());
        suite.addTest(TestProjectHistory.suite());
        suite.addTest(TestProjects.suite());
        suite.addTest(TestProperty.suite());
        suite.addTest(TestPropertyDefinition.suite());
        suite.addTest(TestPublishing.suite());
        suite.addTest(TestPublishIssues.suite());
        suite.addTest(TestReadResource.suite());
        suite.addTest(TestReadResourceTree.suite());
        suite.addTest(TestReplace.suite());
        suite.addTest(TestResourceFilter.suite());
        suite.addTest(TestResourceOperations.suite());
        suite.addTest(TestRestoreFromHistory.suite());
        suite.addTest(TestSiblings.suite());
        suite.addTest(TestTouch.suite());
        suite.addTest(TestUndelete.suite());
        suite.addTest(TestUndoChanges.suite());
        suite.addTest(TestUser.suite());
        suite.addTest(TestLinkRewriter.suite());
        //$JUnit-END$
        return suite;
    }
}
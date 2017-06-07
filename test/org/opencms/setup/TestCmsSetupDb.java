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

package org.opencms.setup;

import org.opencms.test.OpenCmsTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the database creation / removal used during setup.<p>
 *
 * @since 6.0.0
 */
public class TestCmsSetupDb extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsSetupDb(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class (becasue the order of test cases is important here).<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsSetupDb.class.getName());

        suite.addTest(new TestCmsSetupDb("testCreateDatabase"));
        suite.addTest(new TestCmsSetupDb("testCreateTables"));
        suite.addTest(new TestCmsSetupDb("testDropTables"));
        suite.addTest(new TestCmsSetupDb("testDropDatabase"));

        return suite;
    }

    /**
     * Tests database creation.<p>
     */
    public void testCreateDatabase() {

        if (DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testCreateDatabase not applicable for oracle.");
            return;
        }

        // use create method form superclass
        CmsSetupDb setupDb = getSetupDb(m_setupConnection);
        setupDb.createDatabase(getDbProduct(), getReplacer(m_defaultConnection), true);

        // check for errors
        checkErrors(setupDb);

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests table creation.<p>
     */
    public void testCreateTables() {

        if (DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testDropDatabase not applicable for oracle.");
            return;
        }

        // use create method form superclass
        CmsSetupDb setupDb = getSetupDb(m_defaultConnection);
        setupDb.createTables(getDbProduct(), getReplacer(m_defaultConnection), true);

        // check for errors
        checkErrors(setupDb);

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests database removal.<p>
     */
    public void testDropDatabase() {

        if (DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testDropDatabase not applicable for oracle.");
            return;
        }

        // use drop method form superclass
        CmsSetupDb setupDb = getSetupDb(m_setupConnection);
        setupDb.dropDatabase(getDbProduct(), getReplacer(m_defaultConnection), true);

        // check for errors
        checkErrors(setupDb);

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests table removal.<p>
     */
    public void testDropTables() {

        if (DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testDropDatabase not applicable for oracle.");
            return;
        }

        // use drop method form superclass
        CmsSetupDb setupDb = getSetupDb(m_defaultConnection);
        setupDb.dropTables(getDbProduct(), getReplacer(m_defaultConnection), true);

        // check for errors
        checkErrors(setupDb);

        // close connections
        setupDb.closeConnection();
    }
}
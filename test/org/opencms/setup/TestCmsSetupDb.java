/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup;

import org.opencms.test.OpenCmsTestRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests the database creation / removal used during setup.<p>
 *
 * @since 6.0.0
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsSetupDb extends OpenCmsTestRunner {

    @BeforeAll
    public void setUpConfiguration(TestInfo testInfo) {

        initConfiguration();
    }

    /**
     * Tests database creation.<p>
     */
    @Order(1)
    @Test
    public void testCreateDatabase() {

        if (DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testCreateDatabase not applicable for oracle.");
            return;
        }

        // use create method form superclass
        CmsSetupDb setupDb = getSetupDbForSetupConnection();
        setupDb.createDatabase(getDbProduct(), getDefaultConnectionReplacer(), true);

        // check for errors
        checkErrors(setupDb);

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests table creation.<p>
     */
    @Order(2)
    @Test
    public void testCreateTables() {

        if (DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testDropDatabase not applicable for oracle.");
            return;
        }

        // use create method form superclass
        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        setupDb.createTables(getDbProduct(), getDefaultConnectionReplacer(), true);

        // check for errors
        checkErrors(setupDb);

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests database removal.<p>
     */
    @Order(4)
    @Test
    public void testDropDatabase() {

        if (DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testDropDatabase not applicable for oracle.");
            return;
        }

        // use drop method form superclass
        CmsSetupDb setupDb = getSetupDbForSetupConnection();
        setupDb.dropDatabase(getDbProduct(), getDefaultConnectionReplacer(), true);

        // check for errors
        checkErrors(setupDb);

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests table removal.<p>
     */
    @Order(3)
    @Test
    public void testDropTables() {

        if (DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testDropDatabase not applicable for oracle.");
            return;
        }

        // use drop method form superclass
        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        setupDb.dropTables(getDbProduct(), getDefaultConnectionReplacer(), true);

        // check for errors
        checkErrors(setupDb);

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests that JDBC drivers referenced in database.properties actually match the existing Jar files.
     *
     * @throws Exception
     */
    @Order(5)
    @Test
    public void testJdbcDriverVersions() throws Exception {

        File baseFolder = new File("./webapp/WEB-INF/setupdata/database");
        for (File dbFolder : baseFolder.listFiles()) {
            if (!dbFolder.isDirectory()) {
                continue;
            }
            File propFile = new File(dbFolder, "database.properties");
            Properties props = new Properties();
            try (FileInputStream stream = new FileInputStream(propFile)) {
                props.load(stream);
                String name = dbFolder.getName();
                String lib = (String)props.get(name + ".libs");
                File driverFile = new File(dbFolder, lib);
                assertTrue(driverFile.exists(), "JDBC driver not found or wrong version: " + driverFile);
            }
        }
    }
}

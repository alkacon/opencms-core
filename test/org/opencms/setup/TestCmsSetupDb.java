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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.setup.db.CmsUpdateDBManager;
import org.opencms.test.OpenCmsTestRunner;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests the database creation / removal used during setup.<p>
 *
 * @since 6.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsSetupDb extends OpenCmsTestRunner {

    /**
     * Update bean test stub.<p>
     */
    private static class TestUpdateBean extends CmsUpdateBean {

        /** The test configuration. */
        private CmsParameterConfiguration m_properties;

        /**
         * Creates a new test update bean.<p>
         *
         * @param properties the test configuration
         */
        TestUpdateBean(CmsParameterConfiguration properties) {

            m_properties = properties;
        }

        /**
         * @see org.opencms.setup.CmsSetupBean#getProperties()
         */
        @Override
        public CmsParameterConfiguration getProperties() {

            return m_properties;
        }

        /**
         * @see org.opencms.setup.CmsSetupBean#isInitialized()
         */
        @Override
        public boolean isInitialized() {

            return true;
        }
    }

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
    @Order(7)
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
    @Order(6)
    @Test
    public void testDropTables() throws Exception {

        // use drop method form superclass
        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        if (setupDb.hasTableOrColumn("CMS_STORAGE", null)) {
            try (Statement stmt = setupDb.getConnection().createStatement()) {
                stmt.execute("DROP TABLE CMS_STORAGE");
            }
        }
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
    @Order(8)
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

    /**
     * Tests if the storage schema update requirement is detected.<p>
     */
    @Order(3)
    @Test
    public void testNeedsStorageSchemaUpdate() throws Exception {

        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        CmsUpdateDBManager manager = new CmsUpdateDBManager();

        assertFalse(manager.needsStorageSchemaUpdate(setupDb));
        try (Statement stmt = setupDb.getConnection().createStatement()) {
            stmt.execute("DROP TABLE CMS_STORAGE");
        }
        assertTrue(manager.needsStorageSchemaUpdate(setupDb));

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests the storage schema update plugin.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Order(4)
    @Test
    public void testStorageSchemaUpdate() throws Exception {

        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        CmsUpdateDBManager manager = new CmsUpdateDBManager();

        dropStorageIndexes(setupDb);
        if (setupDb.hasTableOrColumn("CMS_STORAGE", null)) {
            try (Statement stmt = setupDb.getConnection().createStatement()) {
                stmt.execute("DROP TABLE CMS_STORAGE");
            }
        }
        assertTrue(manager.needsStorageSchemaUpdate(setupDb));

        getStorageSchemaUpdate().execute(setupDb, getDefaultDbPoolData());
        assertFalse(manager.needsStorageSchemaUpdate(setupDb));

        getStorageSchemaUpdate().execute(setupDb, getDefaultDbPoolData());
        assertFalse(manager.needsStorageSchemaUpdate(setupDb));

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests if the storage schema update is controlled by the setup flag.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Order(5)
    @Test
    public void testStorageSchemaUpdateControlledBySetupFlag() throws Exception {

        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        if (setupDb.hasTableOrColumn("CMS_STORAGE", null)) {
            try (Statement stmt = setupDb.getConnection().createStatement()) {
                stmt.execute("DROP TABLE CMS_STORAGE");
            }
        }

        CmsParameterConfiguration properties = getTestConfiguration();
        properties.put("db.vfs.driver", "org.opencms.db.mysql.CmsVfsDriver");
        properties.put(CmsUpdateDBManager.PARAM_STORAGE_SCHEMA_UPDATE, CmsUpdateDBManager.STORAGE_SCHEMA_UPDATE_FALSE);
        CmsUpdateDBManager manager = new CmsUpdateDBManager();
        manager.initialize(new TestUpdateBean(properties));
        assertFalse(manager.needUpdate());

        properties.put(CmsUpdateDBManager.PARAM_STORAGE_SCHEMA_UPDATE, CmsUpdateDBManager.STORAGE_SCHEMA_UPDATE_AUTO);
        manager = new CmsUpdateDBManager();
        manager.initialize(new TestUpdateBean(properties));
        assertTrue(manager.needUpdate());

        properties.put(CmsUpdateDBManager.PARAM_STORAGE_SCHEMA_UPDATE, CmsUpdateDBManager.STORAGE_SCHEMA_UPDATE_TRUE);
        manager = new CmsUpdateDBManager();
        manager.initialize(new TestUpdateBean(properties));
        assertTrue(manager.needUpdate());

        getStorageSchemaUpdate().execute(setupDb, getDefaultDbPoolData());

        // close connections
        setupDb.closeConnection();
    }

    /**
     * Tests if the storage schema update flag is interpreted.<p>
     */
    @Order(10)
    @Test
    public void testStorageSchemaUpdateFlag() {

        CmsUpdateDBManager manager = new CmsUpdateDBManager();
        assertTrue(manager.isStorageSchemaUpdateEnabled(CmsUpdateDBManager.STORAGE_SCHEMA_UPDATE_AUTO));
        assertTrue(manager.isStorageSchemaUpdateEnabled(CmsUpdateDBManager.STORAGE_SCHEMA_UPDATE_TRUE));
        assertFalse(manager.isStorageSchemaUpdateEnabled(""));
        assertFalse(manager.isStorageSchemaUpdateEnabled(null));
        assertFalse(manager.isStorageSchemaUpdateEnabled(CmsUpdateDBManager.STORAGE_SCHEMA_UPDATE_FALSE));
    }

    /**
     * Tests if the storage schema update query properties can be loaded.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Order(9)
    @Test
    public void testStorageSchemaUpdateQueryProperties() throws Exception {

        new org.opencms.setup.db.update21to22.as400.CmsUpdateDBStorageSchema();
        new org.opencms.setup.db.update21to22.db2.CmsUpdateDBStorageSchema();
        new org.opencms.setup.db.update21to22.hsqldb.CmsUpdateDBStorageSchema();
        new org.opencms.setup.db.update21to22.mssql.CmsUpdateDBStorageSchema();
        new org.opencms.setup.db.update21to22.mysql.CmsUpdateDBStorageSchema();
        new org.opencms.setup.db.update21to22.oracle.CmsUpdateDBStorageSchema();
        new org.opencms.setup.db.update21to22.postgresql.CmsUpdateDBStorageSchema();
    }

    /**
     * Drops the storage indexes.<p>
     *
     * @param setupDb the setup database connection
     *
     * @throws Exception if something goes wrong
     */
    private void dropStorageIndexes(CmsSetupDb setupDb) throws Exception {

        try (Statement stmt = setupDb.getConnection().createStatement()) {
            if (DB_MYSQL.equals(getDbProduct())) {
                stmt.execute("DROP INDEX HASH_IDX ON CMS_CONTENTS");
                stmt.execute("DROP INDEX HASH_IDX ON CMS_OFFLINE_CONTENTS");
            } else if (DB_ORACLE.equals(getDbProduct())) {
                stmt.execute("DROP INDEX CMS_CONTENTS_06_IDX");
                stmt.execute("DROP INDEX CMS_OFFLINE_CONTENTS_01_IDX");
            } else if ("postgresql".equals(getDbProduct())) {
                stmt.execute("DROP INDEX CMS_CONTENTS_06_IDX");
                stmt.execute("DROP INDEX CMS_OFFLINE_CONTENTS_01_IDX");
            } else if ("mssql".equals(getDbProduct())) {
                stmt.execute("DROP INDEX CMS_CONTENTS_05_IDX ON CMS_CONTENTS");
                stmt.execute("DROP INDEX CMS_OFFLINE_CONTENTS_01_IDX ON CMS_OFFLINE_CONTENTS");
            } else if ("db2".equals(getDbProduct())) {
                stmt.execute("DROP INDEX CMS_CONTENTS_06");
                stmt.execute("DROP INDEX CMS_OFFLINE_CONTENTS_01");
            } else {
                stmt.execute("DROP INDEX CMS_CONTENTS_05_IDX");
                stmt.execute("DROP INDEX CMS_OFFLINE_CONTENTS_01_IDX");
            }
        }
    }

    /**
     * Returns the default database pool data.<p>
     *
     * @return the default database pool data
     */
    private Map<String, String> getDefaultDbPoolData() {

        Map<String, String> result = new HashMap<String, String>();
        result.put("dataTablespace", "users");
        result.put("indexTablespace", "users");
        result.put("engine", "MYISAM");
        return result;
    }

    /**
     * Returns the storage schema update plugin for the current database.<p>
     *
     * @return the storage schema update plugin
     *
     * @throws Exception if something goes wrong
     */
    private org.opencms.setup.db.update21to22.CmsUpdateDBStorageSchema getStorageSchemaUpdate() throws Exception {

        String className = "org.opencms.setup.db.update21to22." + getDbProduct() + ".CmsUpdateDBStorageSchema";
        return (org.opencms.setup.db.update21to22.CmsUpdateDBStorageSchema)Class.forName(className).newInstance();
    }

    /**
     * Returns the test configuration.<p>
     *
     * @return the test configuration
     *
     * @throws Exception if the configuration can not be read
     */
    private CmsParameterConfiguration getTestConfiguration() throws Exception {

        return new CmsParameterConfiguration(
            getTestDataPath("WEB-INF/config." + getDbProduct() + "/opencms.properties"));
    }

}

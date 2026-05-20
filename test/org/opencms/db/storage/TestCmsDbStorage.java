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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.storage;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbPoolV11;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;

import java.lang.reflect.Constructor;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests for the database storage backend.<p>
 */
public class TestCmsDbStorage extends OpenCmsTestRunner {

    /**
     * Calculates a SHA-512 hash.<p>
     *
     * @param content the content
     * @return the hash string
     *
     * @throws Exception if the digest can not be created
     */
    private static String calculateSha512(byte[] content) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        StringBuilder result = new StringBuilder(128);
        for (byte value : digest.digest(content)) {
            String hex = Integer.toHexString(0xff & value);
            if (hex.length() == 1) {
                result.append('0');
            }
            result.append(hex);
        }
        return result.toString();
    }

    /**
     * Creates test content.<p>
     *
     * @param size the content size
     * @param seed the byte value to fill the content with
     * @return the content
     */
    private static byte[] createContent(int size, byte seed) {

        byte[] content = new byte[size];
        Arrays.fill(content, seed);
        return content;
    }

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests null hash handling.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testNullHashHandling() throws Exception {

        I_CmsDbStorage storage = createDbStorage();
        CmsDbContext dbc = createDbContext();

        assertNull(storage.loadContent(dbc, null));
        storage.deleteContent(dbc, null);
    }

    /**
     * Tests that content is deduplicated by hash.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoreContentDeduplicatesByHash() throws Exception {

        I_CmsDbStorage storage = createDbStorage();
        CmsDbContext dbc = createDbContext();
        byte[] content = "original database content".getBytes("UTF-8");
        byte[] otherContent = "replacement database content".getBytes("UTF-8");
        String hash = calculateSha512(content);

        storage.deleteContent(dbc, hash);
        try {
            storage.storeContent(dbc, hash, content);
            storage.storeContent(dbc, hash, otherContent);

            assertEquals(1, countStorageRows(hash));
            assertArrayEquals(content, storage.loadContent(dbc, hash));
        } finally {
            storage.deleteContent(dbc, hash);
        }
    }

    /**
     * Tests storing, loading and deleting larger content through the database backend.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoreLoadAndDeleteLargeContent() throws Exception {

        byte[] content = createContent(4096, (byte)1);
        assertStoreLoadAndDelete(content);
    }

    /**
     * Tests storing, loading and deleting small content through the database backend.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoreLoadAndDeleteSmallContent() throws Exception {

        assertStoreLoadAndDelete("database storage content".getBytes("UTF-8"));
    }

    /**
     * Runs a store/load/delete roundtrip.<p>
     *
     * @param content the content
     *
     * @throws Exception if something goes wrong
     */
    private void assertStoreLoadAndDelete(byte[] content) throws Exception {

        I_CmsDbStorage storage = createDbStorage();
        CmsDbContext dbc = createDbContext();
        String hash = calculateSha512(content);

        storage.deleteContent(dbc, hash);
        try {
            storage.storeContent(dbc, hash, content);

            assertEquals(1, countStorageRows(hash));
            assertArrayEquals(content, storage.loadContent(dbc, hash));

            storage.deleteContent(dbc, hash);

            assertEquals(0, countStorageRows(hash));
            assertNull(storage.loadContent(dbc, hash));
        } finally {
            storage.deleteContent(dbc, hash);
        }
    }

    /**
     * Counts storage rows for a hash.<p>
     *
     * @param hash the hash
     * @return the row count
     *
     * @throws Exception if the query fails
     */
    private int countStorageRows(String hash) throws Exception {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = OpenCms.getSqlManager().getConnection(OpenCms.getSqlManager().getDefaultDbPoolName());
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM CMS_STORAGE WHERE HASH=?");
            stmt.setString(1, hash);
            res = stmt.executeQuery();
            res.next();
            return res.getInt(1);
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Creates a database context.<p>
     *
     * @return the database context
     */
    private CmsDbContext createDbContext() throws Exception {

        return new CmsDbContext(getCmsObject().getRequestContext());
    }

    /**
     * Creates the database-specific storage backend.<p>
     *
     * @return the database storage backend
     *
     * @throws Exception if the storage can not be created
     */
    private I_CmsDbStorage createDbStorage() throws Exception {

        String packageName = "org.opencms.db." + getDbProduct();
        CmsSqlManager sqlManager = CmsSqlManager.getInstance(
            packageName + ".CmsSqlManager",
            Arrays.asList("org/opencms/db/generic/storage.properties"));
        sqlManager.init(
            I_CmsVfsDriver.DRIVER_TYPE_ID,
            CmsDbPoolV11.OPENCMS_URL_PREFIX + OpenCms.getSqlManager().getDefaultDbPoolName());
        Class<?> storageClass = Class.forName(packageName + ".CmsDbStorage");
        Constructor<?> constructor = storageClass.getConstructor(CmsSqlManager.class);
        return (I_CmsDbStorage)constructor.newInstance(sqlManager);
    }
}

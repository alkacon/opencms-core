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

package org.opencms.tools.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.setup.CmsSetupDb;
import org.opencms.test.OpenCmsTestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Optional database integration tests for the standalone storage migration tool.<p>
 */
public class TestCmsStorageMigrationToolIntegration extends OpenCmsTestRunner {

    /**
     * Stored content row.<p>
     */
    private static class StoredRow {

        /** Content bytes. */
        private byte[] m_content;

        /** Hash column. */
        private String m_hash;

        /** Storage column. */
        private String m_storage;
    }

    /** Environment variable for selecting the enabled database products. */
    private static final String ENV_ENABLED_DBS = "OPENCMS_STORAGE_MIGRATION_TEST_DBS";

    /** Environment variable prefix for enabling one database product. */
    private static final String ENV_PRODUCT_PREFIX = "OPENCMS_STORAGE_MIGRATION_TEST_";

    /** System property for selecting the enabled database products. */
    private static final String PROP_ENABLED_DBS = "opencms.storage.migration.test.dbs";

    /** System property prefix for enabling one database product. */
    private static final String PROP_PRODUCT_PREFIX = "opencms.storage.migration.test.";

    /** Resource type id used by the default storage policy. */
    private static final int RESOURCE_TYPE_BINARY = 2;

    /**
     * Calculates a SHA-512 hash.<p>
     *
     * @param content the content
     *
     * @return the hash
     *
     * @throws Exception if hashing fails
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
     * Deletes a directory tree if it exists.<p>
     *
     * @param path the directory path
     *
     * @throws Exception if deletion fails
     */
    private static void deleteTree(Path path) throws Exception {

        if ((path == null) || !Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Returns true if the given value is empty.<p>
     *
     * @param value the value
     *
     * @return true if the value is empty
     */
    private static boolean isEmpty(String value) {

        return (value == null) || (value.trim().length() == 0);
    }

    /**
     * Returns true if a flag value is truthy.<p>
     *
     * @param value the value
     *
     * @return true if the flag is truthy
     */
    private static boolean isTruthy(String value) {

        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return "1".equals(normalized) || "true".equals(normalized) || "yes".equals(normalized);
    }

    /**
     * Tests that the maintenance tool deletes unreferenced file system storage blobs only in execute mode.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testMaintenanceDeletesFileSystemOrphansOnlyWithExecute() throws Exception {

        String configuredProduct = System.getProperty("db.product", "hsqldb");
        assumeEnabled(configuredProduct);
        initConfiguration();

        Path tempFolder = Files.createTempDirectory("opencms-storage-maintenance-");
        Path webInf = tempFolder.resolve("WEB-INF");
        Path storagePath = tempFolder.resolve("storage");
        Files.createDirectories(storagePath);

        String resourceId = UUID.randomUUID().toString();
        byte[] referencedContent = "referenced maintenance content".getBytes(StandardCharsets.UTF_8);
        byte[] orphanContent = "orphan maintenance content".getBytes(StandardCharsets.UTF_8);
        byte[] databaseOrphanContent = "orphan database maintenance content".getBytes(StandardCharsets.UTF_8);
        String referencedHash = calculateSha512(referencedContent);
        String orphanHash = calculateSha512(orphanContent);
        String databaseOrphanHash = calculateSha512(databaseOrphanContent);

        try {
            createFreshDatabase();
            createProperties(webInf, storagePath);
            appendProperties(webInf, "\nstorage.legacy=db\n");
            CmsSetupDb setupDb = getSetupDbForDefaultConnection();
            try (Connection connection = setupDb.getConnection()) {
                long now = System.currentTimeMillis();
                insertCurrentResource(connection, "CMS_OFFLINE_RESOURCES", resourceId, referencedContent.length, now);
                insertOfflineContent(connection, resourceId, new byte[0]);
                updateOfflineStorageReference(connection, resourceId, "fs", referencedHash);
                storeFile(storagePath, referencedHash, referencedContent);
                storeFile(storagePath, orphanHash, orphanContent);
                insertDatabaseStorageBlob(connection, databaseOrphanHash, databaseOrphanContent);
            } finally {
                setupDb.closeConnection();
            }

            runMaintenance(webInf, false);
            assertTrue(Files.exists(resolveStoredFile(storagePath, referencedHash)));
            assertTrue(Files.exists(resolveStoredFile(storagePath, orphanHash)));
            assertTrue(databaseStorageBlobExists(databaseOrphanHash));

            runMaintenance(webInf, true);
            assertTrue(Files.exists(resolveStoredFile(storagePath, referencedHash)));
            assertTrue(!Files.exists(resolveStoredFile(storagePath, orphanHash)));
            assertTrue(!databaseStorageBlobExists(databaseOrphanHash));
        } finally {
            try {
                if (getDbProduct() != null) {
                    if (DB_ORACLE.equals(getDbProduct())) {
                        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
                        try {
                            setupDb.dropTables(getDbProduct(), getDefaultConnectionReplacer(), false);
                        } finally {
                            setupDb.closeConnection();
                        }
                    } else {
                        removeDatabase();
                    }
                }
            } finally {
                deleteTree(tempFolder);
            }
        }
    }

    /**
     * Tests migration to file system storage and back to local VFS tables.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testMigratesLocalContentToExternalStorageAndBack() throws Exception {

        String configuredProduct = System.getProperty("db.product", "hsqldb");
        assumeEnabled(configuredProduct);
        initConfiguration();

        Path tempFolder = Files.createTempDirectory("opencms-storage-migration-");
        Path webInf = tempFolder.resolve("WEB-INF");
        Path storagePath = tempFolder.resolve("storage");
        Files.createDirectories(storagePath);

        String offlineResourceId = UUID.randomUUID().toString();
        String onlineResourceId = UUID.randomUUID().toString();
        String historyResourceId = UUID.randomUUID().toString();
        byte[] offlineContent = "offline migration content".getBytes(StandardCharsets.UTF_8);
        byte[] onlineContent = "online migration content".getBytes(StandardCharsets.UTF_8);
        byte[] historyContent = "history migration content".getBytes(StandardCharsets.UTF_8);

        try {
            createFreshDatabase();
            createProperties(webInf, storagePath);
            createVfsConfigForExternalStorage(webInf);
            CmsSetupDb setupDb = getSetupDbForDefaultConnection();
            try (Connection connection = setupDb.getConnection()) {
                seedContent(
                    connection,
                    offlineResourceId,
                    onlineResourceId,
                    historyResourceId,
                    offlineContent,
                    onlineContent,
                    historyContent);
            } finally {
                setupDb.closeConnection();
            }

            runMigration(webInf);
            setupDb = getSetupDbForDefaultConnection();
            try (Connection connection = setupDb.getConnection()) {
                assertExternallyStored(
                    connection,
                    storagePath,
                    "CMS_OFFLINE_CONTENTS",
                    offlineResourceId,
                    null,
                    offlineContent);
                assertExternallyStored(connection, storagePath, "CMS_CONTENTS", onlineResourceId, 1, onlineContent);
                assertExternallyStored(connection, storagePath, "CMS_CONTENTS", historyResourceId, 7, historyContent);
            } finally {
                setupDb.closeConnection();
            }

            createVfsConfigForLocalStorage(webInf);
            runMigration(webInf);
            setupDb = getSetupDbForDefaultConnection();
            try (Connection connection = setupDb.getConnection()) {
                assertLocallyStored(connection, "CMS_OFFLINE_CONTENTS", offlineResourceId, null, offlineContent);
                assertLocallyStored(connection, "CMS_CONTENTS", onlineResourceId, 1, onlineContent);
                assertLocallyStored(connection, "CMS_CONTENTS", historyResourceId, 7, historyContent);
            } finally {
                setupDb.closeConnection();
            }
        } finally {
            try {
                if (getDbProduct() != null) {
                    if (DB_ORACLE.equals(getDbProduct())) {
                        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
                        try {
                            setupDb.dropTables(getDbProduct(), getDefaultConnectionReplacer(), false);
                        } finally {
                            setupDb.closeConnection();
                        }
                    } else {
                        removeDatabase();
                    }
                }
            } finally {
                deleteTree(tempFolder);
            }
        }
    }

    /**
     * Appends properties to the temporary opencms.properties file.<p>
     *
     * @param webInf the temporary WEB-INF folder
     * @param properties the properties to append
     *
     * @throws Exception if writing fails
     */
    private void appendProperties(Path webInf, String properties) throws Exception {

        Files.write(
            webInf.resolve("config/opencms.properties"),
            properties.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
    }

    /**
     * Asserts that a row has been moved to the file system storage backend.<p>
     *
     * @param connection the database connection
     * @param storagePath the storage path
     * @param table the content table
     * @param resourceId the resource id
     * @param publishTagFrom the publish tag from value, or null for offline content
     * @param expectedContent the expected content
     *
     * @throws Exception if reading fails
     */
    private void assertExternallyStored(
        Connection connection,
        Path storagePath,
        String table,
        String resourceId,
        Integer publishTagFrom,
        byte[] expectedContent)
    throws Exception {

        StoredRow row = readStoredRow(connection, table, resourceId, publishTagFrom);
        assertEquals(0, row.m_content.length);
        assertEquals("fs", row.m_storage);
        assertNotNull(row.m_hash);
        assertEquals(calculateSha512(expectedContent), row.m_hash);
        assertArrayEquals(expectedContent, Files.readAllBytes(resolveStoredFile(storagePath, row.m_hash)));
    }

    /**
     * Asserts that a row has been migrated back to local table storage.<p>
     *
     * @param connection the database connection
     * @param table the content table
     * @param resourceId the resource id
     * @param publishTagFrom the publish tag from value, or null for offline content
     * @param expectedContent the expected content
     *
     * @throws Exception if reading fails
     */
    private void assertLocallyStored(
        Connection connection,
        String table,
        String resourceId,
        Integer publishTagFrom,
        byte[] expectedContent)
    throws Exception {

        StoredRow row = readStoredRow(connection, table, resourceId, publishTagFrom);
        assertArrayEquals(expectedContent, row.m_content);
        assertNull(row.m_storage);
        assertNull(row.m_hash);
    }

    /**
     * Assumes that the current DB product has explicitly been enabled.<p>
     *
     * @param dbProduct the current database product
     */
    private void assumeEnabled(String dbProduct) {

        String normalizedProduct = dbProduct.toLowerCase(Locale.ROOT);
        String enabledDbs = System.getProperty(PROP_ENABLED_DBS);
        if (isEmpty(enabledDbs)) {
            enabledDbs = System.getenv(ENV_ENABLED_DBS);
        }
        boolean enabled = containsProduct(enabledDbs, normalizedProduct);
        if (!enabled) {
            enabled = isTruthy(System.getProperty(PROP_PRODUCT_PREFIX + normalizedProduct))
                || isTruthy(System.getenv(ENV_PRODUCT_PREFIX + normalizedProduct.toUpperCase(Locale.ROOT)));
        }
        Assumptions.assumeTrue(
            enabled,
            "Skipping storage migration integration test for "
                + normalizedProduct
                + ". Enable it with "
                + PROP_ENABLED_DBS
                + " / "
                + ENV_ENABLED_DBS
                + " or the product-specific flag.");
    }

    /**
     * Binds common resource values.<p>
     *
     * @param statement the statement
     * @param resourceId the resource id
     * @param size the resource size
     * @param dateContent the content date
     *
     * @throws Exception if binding fails
     */
    private void bindResource(PreparedStatement statement, String resourceId, int size, long dateContent)
    throws Exception {

        String userId = UUID.randomUUID().toString();
        statement.setString(1, resourceId);
        statement.setInt(2, RESOURCE_TYPE_BINARY);
        statement.setInt(3, 0);
        statement.setInt(4, 0);
        statement.setInt(5, size);
        statement.setLong(6, dateContent);
        statement.setInt(7, 1);
        statement.setLong(8, dateContent);
        statement.setLong(9, dateContent);
        statement.setString(10, userId);
        statement.setString(11, userId);
        statement.setString(12, UUID.randomUUID().toString());
        statement.setInt(13, 1);
    }

    /**
     * Checks if a comma-separated product list contains a product or all.<p>
     *
     * @param value the list value
     * @param dbProduct the database product
     *
     * @return true if the product is enabled
     */
    private boolean containsProduct(String value, String dbProduct) {

        if (isEmpty(value)) {
            return false;
        }
        for (String token : value.split(",")) {
            String normalized = token.trim().toLowerCase(Locale.ROOT);
            if ("all".equals(normalized) || dbProduct.equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a clean OpenCms schema for the configured database product.<p>
     *
     * @throws Exception if schema creation fails
     */
    private void createFreshDatabase() throws Exception {

        if (DB_ORACLE.equals(getDbProduct())) {
            CmsSetupDb setupDb = getSetupDbForDefaultConnection();
            try {
                setupDb.dropTables(getDbProduct(), getDefaultConnectionReplacer(), false);
                setupDb.createTables(getDbProduct(), getDefaultConnectionReplacer(), true);
                checkErrors(setupDb);
            } finally {
                setupDb.closeConnection();
            }
        } else {
            removeDatabase();
            setupDatabase();
        }
    }

    /**
     * Creates an opencms.properties file for the migration tool.<p>
     *
     * @param webInf the temporary WEB-INF folder
     * @param storagePath the file system storage path
     *
     * @throws Exception if writing fails
     */
    private void createProperties(Path webInf, Path storagePath) throws Exception {

        Path config = webInf.resolve("config");
        Files.createDirectories(config);
        Path source = Path.of(getTestDataPath("WEB-INF/config." + getDbProduct() + "/opencms.properties"));
        Path target = config.resolve("opencms.properties");
        String properties = Files.readString(source, StandardCharsets.UTF_8).replaceFirst(
            "(?m)^db\\.vfs\\.pool=.*$",
            "db.vfs.pool=default");
        Files.write(target, properties.getBytes(StandardCharsets.UTF_8));
        String overrides = "\n"
            + "storage.active=fs\n"
            + "storage.backend.fs.type=fs\n"
            + "storage.backend.fs.path="
            + storagePath.toAbsolutePath()
            + "\n";
        Files.write(target, overrides.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }

    /**
     * Creates a minimal VFS configuration for the migration tool.<p>
     *
     * @param webInf the temporary WEB-INF folder
     * @param policyClass the storage policy class
     * @param policyParams the policy parameter XML
     *
     * @throws Exception if writing fails
     */
    private void createVfsConfig(Path webInf, String policyClass, String policyParams) throws Exception {

        Path config = webInf.resolve("config");
        Files.createDirectories(config);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<opencms>\n"
            + "  <vfs>\n"
            + "    <resources>\n"
            + "      <resourcetypes>\n"
            + "        <type class=\"org.opencms.file.types.CmsResourceTypeBinary\" name=\"binary\" id=\""
            + RESOURCE_TYPE_BINARY
            + "\" />\n"
            + "      </resourcetypes>\n"
            + "      <storage-policy class=\""
            + policyClass
            + "\">"
            + policyParams
            + "</storage-policy>\n"
            + "    </resources>\n"
            + "  </vfs>\n"
            + "</opencms>\n";
        Files.write(config.resolve("opencms-vfs.xml"), xml.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a VFS configuration using the default storage policy.<p>
     *
     * @param webInf the temporary WEB-INF folder
     *
     * @throws Exception if writing fails
     */
    private void createVfsConfigForExternalStorage(Path webInf) throws Exception {

        createVfsConfig(
            webInf,
            "org.opencms.db.storage.policy.CmsDefaultStoragePolicy",
            "<param name=\"threshold\">0</param>");
    }

    /**
     * Creates a VFS configuration using the no-external-storage policy.<p>
     *
     * @param webInf the temporary WEB-INF folder
     *
     * @throws Exception if writing fails
     */
    private void createVfsConfigForLocalStorage(Path webInf) throws Exception {

        createVfsConfig(webInf, "org.opencms.db.storage.policy.CmsNoExternalStoragePolicy", "");
    }

    /**
     * Checks whether a blob exists in CMS_STORAGE.<p>
     *
     * @param hash the hash
     *
     * @return true if the blob exists
     *
     * @throws Exception if reading fails
     */
    private boolean databaseStorageBlobExists(String hash) throws Exception {

        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        try (Connection connection = setupDb.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM CMS_STORAGE WHERE HASH=?")) {
            statement.setString(1, hash);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } finally {
            setupDb.closeConnection();
        }
    }

    /**
     * Inserts one content row.<p>
     *
     * @param connection the database connection
     * @param resourceId the resource id
     * @param content the content
     * @param publishTagFrom the publish tag from
     * @param publishTagTo the publish tag to
     * @param onlineFlag the online flag
     *
     * @throws Exception if insertion fails
     */
    private void insertContent(
        Connection connection,
        String resourceId,
        byte[] content,
        int publishTagFrom,
        int publishTagTo,
        int onlineFlag)
    throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO CMS_CONTENTS "
                + "(RESOURCE_ID, FILE_CONTENT, STORAGE, HASH, PUBLISH_TAG_FROM, PUBLISH_TAG_TO, ONLINE_FLAG) "
                + "VALUES (?, ?, NULL, NULL, ?, ?, ?)")) {
            statement.setString(1, resourceId);
            statement.setBytes(2, content);
            statement.setInt(3, publishTagFrom);
            statement.setInt(4, publishTagTo);
            statement.setInt(5, onlineFlag);
            statement.executeUpdate();
        }
    }

    /**
     * Inserts one current resource row.<p>
     *
     * @param connection the database connection
     * @param table the resource table
     * @param resourceId the resource id
     * @param size the resource size
     * @param dateContent the content date
     *
     * @throws Exception if insertion fails
     */
    private void insertCurrentResource(
        Connection connection,
        String table,
        String resourceId,
        int size,
        long dateContent)
    throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO "
                + table
                + " (RESOURCE_ID, RESOURCE_TYPE, RESOURCE_FLAGS, RESOURCE_STATE, RESOURCE_SIZE, DATE_CONTENT, "
                + "SIBLING_COUNT, DATE_CREATED, DATE_LASTMODIFIED, USER_CREATED, USER_LASTMODIFIED, "
                + "PROJECT_LASTMODIFIED, RESOURCE_VERSION) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            bindResource(statement, resourceId, size, dateContent);
            statement.executeUpdate();
        }
    }

    /**
     * Inserts one database storage blob.<p>
     *
     * @param connection the database connection
     * @param hash the hash
     * @param content the content
     *
     * @throws Exception if insertion fails
     */
    private void insertDatabaseStorageBlob(Connection connection, String hash, byte[] content) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO CMS_STORAGE (HASH, FILE_CONTENT) VALUES (?, ?)")) {
            statement.setString(1, hash);
            statement.setBytes(2, content);
            statement.executeUpdate();
        }
    }

    /**
     * Inserts one history resource row.<p>
     *
     * @param connection the database connection
     * @param resourceId the resource id
     * @param size the resource size
     * @param dateContent the content date
     * @param publishTag the publish tag
     *
     * @throws Exception if insertion fails
     */
    private void insertHistoryResource(
        Connection connection,
        String resourceId,
        int size,
        long dateContent,
        int publishTag)
    throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO CMS_HISTORY_RESOURCES "
                + "(RESOURCE_ID, RESOURCE_TYPE, RESOURCE_FLAGS, RESOURCE_STATE, RESOURCE_SIZE, DATE_CONTENT, "
                + "SIBLING_COUNT, DATE_CREATED, DATE_LASTMODIFIED, USER_CREATED, USER_LASTMODIFIED, "
                + "PROJECT_LASTMODIFIED, RESOURCE_VERSION, PUBLISH_TAG) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            bindResource(statement, resourceId, size, dateContent);
            statement.setInt(14, publishTag);
            statement.executeUpdate();
        }
    }

    /**
     * Inserts one offline content row.<p>
     *
     * @param connection the database connection
     * @param resourceId the resource id
     * @param content the content
     *
     * @throws Exception if insertion fails
     */
    private void insertOfflineContent(Connection connection, String resourceId, byte[] content) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO CMS_OFFLINE_CONTENTS (RESOURCE_ID, FILE_CONTENT, STORAGE, HASH) VALUES (?, ?, NULL, NULL)")) {
            statement.setString(1, resourceId);
            statement.setBytes(2, content);
            statement.executeUpdate();
        }
    }

    /**
     * Returns the stored content row.<p>
     *
     * @param connection the database connection
     * @param table the table
     * @param resourceId the resource id
     * @param publishTagFrom the publish tag from value, or null for offline content
     *
     * @return the stored row
     *
     * @throws Exception if reading fails
     */
    private StoredRow readStoredRow(Connection connection, String table, String resourceId, Integer publishTagFrom)
    throws Exception {

        String sql = "SELECT FILE_CONTENT, STORAGE, HASH FROM " + table + " WHERE RESOURCE_ID=?";
        if (publishTagFrom != null) {
            sql += " AND PUBLISH_TAG_FROM=?";
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resourceId);
            if (publishTagFrom != null) {
                statement.setInt(2, publishTagFrom.intValue());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "Missing content row for " + resourceId);
                StoredRow result = new StoredRow();
                result.m_content = resultSet.getBytes(1);
                result.m_storage = resultSet.getString(2);
                result.m_hash = resultSet.getString(3);
                return result;
            }
        }
    }

    /**
     * Resolves the file system backend path for a hash.<p>
     *
     * @param storagePath the storage root
     * @param hash the content hash
     *
     * @return the content path
     */
    private Path resolveStoredFile(Path storagePath, String hash) {

        return storagePath.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4)).resolve(
            hash.substring(4, 6)).resolve(hash);
    }

    /**
     * Runs the maintenance tool in delete-orphans mode.<p>
     *
     * @param webInf the temporary WEB-INF folder
     * @param execute true to actually delete orphans
     *
     * @throws Exception if the tool fails
     */
    private void runMaintenance(Path webInf, boolean execute) throws Exception {

        List<String> args = new java.util.ArrayList<String>();
        args.add("--webinf");
        args.add(webInf.toString());
        args.add("--mode");
        args.add("delete-orphans");
        args.add("--delete-limit");
        args.add("0");
        if (execute) {
            args.add("--execute");
        }
        assertEquals(0, CmsStorageMaintenanceTool.run(args.toArray(new String[args.size()])));
    }

    /**
     * Runs the migration tool.<p>
     *
     * @param webInf the temporary WEB-INF folder
     *
     * @throws Exception if the tool fails
     */
    private void runMigration(Path webInf) throws Exception {

        assertEquals(
            0,
            CmsStorageMigrationTool.run(
                new String[] {"--webinf", webInf.toString(), "--execute", "--verify", "--batch-size", "1"}));
    }

    /**
     * Seeds the content tables with local binary content.<p>
     *
     * @param connection the database connection
     * @param offlineResourceId the offline resource id
     * @param onlineResourceId the online resource id
     * @param historyResourceId the history resource id
     * @param offlineContent the offline content
     * @param onlineContent the online content
     * @param historyContent the history content
     *
     * @throws Exception if seeding fails
     */
    private void seedContent(
        Connection connection,
        String offlineResourceId,
        String onlineResourceId,
        String historyResourceId,
        byte[] offlineContent,
        byte[] onlineContent,
        byte[] historyContent)
    throws Exception {

        long now = System.currentTimeMillis();
        insertCurrentResource(connection, "CMS_OFFLINE_RESOURCES", offlineResourceId, offlineContent.length, now);
        insertCurrentResource(connection, "CMS_ONLINE_RESOURCES", onlineResourceId, onlineContent.length, now);
        insertHistoryResource(connection, historyResourceId, historyContent.length, now, 7);
        insertOfflineContent(connection, offlineResourceId, offlineContent);
        insertContent(connection, onlineResourceId, onlineContent, 1, 0, 1);
        insertContent(connection, historyResourceId, historyContent, 7, 8, 0);
    }

    /**
     * Stores one file system storage blob.<p>
     *
     * @param storagePath the storage root
     * @param hash the hash
     * @param content the content
     *
     * @throws Exception if writing fails
     */
    private void storeFile(Path storagePath, String hash, byte[] content) throws Exception {

        Path file = resolveStoredFile(storagePath, hash);
        Files.createDirectories(file.getParent());
        Files.write(file, content);
    }

    /**
     * Updates one offline content row to reference external storage.<p>
     *
     * @param connection the database connection
     * @param resourceId the resource id
     * @param storage the storage id
     * @param hash the hash
     *
     * @throws Exception if updating fails
     */
    private void updateOfflineStorageReference(Connection connection, String resourceId, String storage, String hash)
    throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
            "UPDATE CMS_OFFLINE_CONTENTS SET STORAGE=?, HASH=? WHERE RESOURCE_ID=?")) {
            statement.setString(1, storage);
            statement.setString(2, hash);
            statement.setString(3, resourceId);
            assertEquals(1, statement.executeUpdate());
        }
    }
}

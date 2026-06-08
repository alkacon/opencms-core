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

package org.opencms.db.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.CmsStoragePolicyConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.db.storage.CmsStorageManager.StorageResult;
import org.opencms.db.storage.policy.CmsStoragePolicyContext;
import org.opencms.db.storage.policy.I_CmsStoragePolicy;
import org.opencms.file.CmsDataAccessException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests for the file system storage backend.<p>
 */
public class TestCmsFsStorage {

    /**
     * Storage policy which always requires external storage.<p>
     */
    public static class AlwaysExternalStoragePolicy implements I_CmsStoragePolicy {

        /**
         * @see I_CmsStoragePolicy#isExternalStorageRequired(CmsStoragePolicyContext)
         */
        public boolean isExternalStorageRequired(CmsStoragePolicyContext context) {

            return true;
        }
    }

    /**
     * Storage policy which always stores content locally in the content table.<p>
     */
    public static class AlwaysLocalStoragePolicy implements I_CmsStoragePolicy {

        /**
         * @see I_CmsStoragePolicy#isExternalStorageRequired(CmsStoragePolicyContext)
         */
        public boolean isExternalStorageRequired(CmsStoragePolicyContext context) {

            return false;
        }
    }

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
     * Creates the always-external policy configuration.<p>
     *
     * @return the policy configuration
     */
    private static CmsStoragePolicyConfiguration createAlwaysExternalPolicyConfiguration() {

        CmsStoragePolicyConfiguration configuration = new CmsStoragePolicyConfiguration();
        configuration.setClassName(AlwaysExternalStoragePolicy.class.getName());
        return configuration;
    }

    /**
     * Creates the always-local policy configuration.<p>
     *
     * @return the policy configuration
     */
    private static CmsStoragePolicyConfiguration createAlwaysLocalPolicyConfiguration() {

        CmsStoragePolicyConfiguration configuration = new CmsStoragePolicyConfiguration();
        configuration.setClassName(AlwaysLocalStoragePolicy.class.getName());
        return configuration;
    }

    /**
     * Creates a test hash with a fixed prefix.<p>
     *
     * @param prefix the hash prefix
     * @param fill the fill character
     * @return the test hash
     */
    private static String createHash(String prefix, char fill) {

        StringBuilder result = new StringBuilder(prefix);
        while (result.length() < 128) {
            result.append(fill);
        }
        return result.toString();
    }

    /**
     * Creates a dynamic JDBC proxy.<p>
     *
     * @param type the proxied type
     * @param handler the proxy handler
     * @return the proxy instance
     */
    private static <T> T createJdbcProxy(Class<T> type, InvocationHandler handler) {

        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, handler));
    }

    /**
     * Creates a test SQL manager for the storage reference check.<p>
     *
     * @param contentReferenced true if the checked blob should be treated as still referenced
     * @return the test SQL manager
     */
    private static org.opencms.db.generic.CmsSqlManager createSqlManager(final boolean contentReferenced) {

        return new org.opencms.db.generic.CmsSqlManager() {

            @Override
            public void closeAll(CmsDbContext dbc, Connection con, Statement stmnt, ResultSet res) {

                // no pooled JDBC resources are used by this test double
            }

            @Override
            public Connection getConnection(CmsDbContext dbc) {

                return createJdbcProxy(Connection.class, new InvocationHandler() {

                    public Object invoke(Object proxy, Method method, Object[] args) {

                        if ("isClosed".equals(method.getName())) {
                            return Boolean.FALSE;
                        }
                        return null;
                    }
                });
            }

            @Override
            public PreparedStatement getPreparedStatement(Connection con, String queryKey) {

                return createJdbcProxy(PreparedStatement.class, new InvocationHandler() {

                    public Object invoke(Object proxy, Method method, Object[] args) {

                        if ("executeQuery".equals(method.getName())) {
                            return createJdbcProxy(ResultSet.class, new InvocationHandler() {

                                private boolean m_nextCalled;

                                public Object invoke(
                                    Object resultSetProxy,
                                    Method resultSetMethod,
                                    Object[] resultSetArgs) {

                                    if ("next".equals(resultSetMethod.getName())) {
                                        boolean result = !m_nextCalled && contentReferenced;
                                        m_nextCalled = true;
                                        return Boolean.valueOf(result);
                                    }
                                    return null;
                                }
                            });
                        }
                        return null;
                    }
                });
            }
        };
    }

    /**
     * Creates a storage manager configuration with one active and one legacy file system backend.<p>
     *
     * @param activeRepository the active storage repository
     * @param legacyRepository the legacy storage repository
     * @return the configuration
     */
    private static CmsParameterConfiguration createTwoBackendConfiguration(
        Path activeRepository,
        Path legacyRepository) {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add("storage.active", "fs1");
        configuration.add("storage.legacy", "fs2");
        configuration.add("storage.backend.fs1.type", "fs");
        configuration.add("storage.backend.fs1.path", activeRepository.toString());
        configuration.add("storage.backend.fs2.type", "fs");
        configuration.add("storage.backend.fs2.path", legacyRepository.toString());
        return configuration;
    }

    /**
     * Deletes a directory recursively.<p>
     *
     * @param directory the directory
     *
     * @throws Exception if deletion fails
     */
    private static void deleteDirectory(Path directory) throws Exception {

        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Returns the expected storage path for a hash.<p>
     *
     * @param repository the repository root
     * @param hash the content hash
     * @return the expected storage path
     */
    private static Path getStoredFile(Path repository, String hash) {

        return repository.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4)).resolve(
            hash.substring(4, 6)).resolve(hash.toLowerCase());
    }

    /**
     * Tests that deleting a file cleans up empty hash parent folders but keeps the repository root.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeleteContentCleansEmptyParentFoldersButKeepsRepositoryRoot() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsFsStorage storage = new CmsFsStorage("fs1", repository.toString());
            byte[] content = "cleanup content".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);
            Path level1 = repository.resolve(hash.substring(0, 2));
            Path level2 = level1.resolve(hash.substring(2, 4));
            Path level3 = level2.resolve(hash.substring(4, 6));

            storage.storeContent(null, hash, content);
            storage.deleteContent(null, hash);

            assertTrue(Files.isDirectory(repository));
            assertFalse(Files.exists(level3));
            assertFalse(Files.exists(level2));
            assertFalse(Files.exists(level1));
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that deleting a file stops parent cleanup at the first non-empty folder.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeleteContentKeepsNonEmptyParentFolders() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsFsStorage storage = new CmsFsStorage("fs1", repository.toString());
            String firstHash = createHash("abcdef", '1');
            String secondHash = createHash("abcdef", '2');
            Path sharedParent = repository.resolve("ab").resolve("cd").resolve("ef");

            storage.storeContent(null, firstHash, "first".getBytes(StandardCharsets.UTF_8));
            storage.storeContent(null, secondHash, "second".getBytes(StandardCharsets.UTF_8));
            storage.deleteContent(null, firstHash);

            assertFalse(Files.exists(getStoredFile(repository, firstHash)));
            assertTrue(Files.isRegularFile(getStoredFile(repository, secondHash)));
            assertTrue(Files.isDirectory(sharedParent));
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that invalid hashes are rejected explicitly.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testInvalidHashIsRejected() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsFsStorage storage = new CmsFsStorage("fs1", repository.toString());
            try {
                storage.loadContent(null, "../invalid");
                fail("Expected invalid hash to fail.");
            } catch (IllegalArgumentException e) {
                // expected
            }
            try {
                storage.storeContent(null, createHash("abcdef", 'g'), new byte[0]);
                fail("Expected non-hex hash to fail.");
            } catch (IllegalArgumentException e) {
                // expected
            }
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that database storage can be configured as legacy backend using the reserved id only.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerAcceptsDatabaseStorageAsReservedLegacyBackend() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsParameterConfiguration configuration = new CmsParameterConfiguration();
            configuration.add("storage.active", "fs1");
            configuration.add("storage.legacy", "db");
            configuration.add("storage.backend.fs1.type", "fs");
            configuration.add("storage.backend.fs1.path", repository.toString());

            new CmsStorageManager(createSqlManager(false), configuration, createAlwaysExternalPolicyConfiguration());
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that a missing legacy blob fails explicitly instead of returning null content.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerFailsForMissingLegacyBlob() throws Exception {

        Path activeRepository = Files.createTempDirectory("opencms-fs-storage-active");
        Path legacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy");
        try {
            CmsStorageManager storageManager = new CmsStorageManager(
                null,
                createTwoBackendConfiguration(activeRepository, legacyRepository),
                createAlwaysExternalPolicyConfiguration());
            String hash = createHash("abcdef", '1');

            try {
                storageManager.loadContent(null, null, "fs2", hash);
                fail("Expected missing legacy blob to fail.");
            } catch (CmsStorageBlobNotFoundException e) {
                assertTrue(e.getMessage().indexOf("fs2") >= 0);
                assertTrue(e.getMessage().indexOf(hash) >= 0);
            }
        } finally {
            deleteDirectory(activeRepository);
            deleteDirectory(legacyRepository);
        }
    }

    /**
     * Tests that an unknown storage identifier fails explicitly.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerFailsForUnknownStorageIdentifier() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsParameterConfiguration configuration = new CmsParameterConfiguration();
            configuration.add("storage.active", "fs1");
            configuration.add("storage.backend.fs1.type", "fs");
            configuration.add("storage.backend.fs1.path", repository.toString());
            CmsStorageManager storageManager = new CmsStorageManager(
                null,
                configuration,
                createAlwaysExternalPolicyConfiguration());

            try {
                storageManager.loadContent(null, null, "missing", createHash("abcdef", '1'));
                fail("Expected unknown storage identifier to fail.");
            } catch (CmsStorageException e) {
                assertFalse(e instanceof CmsStorageBlobNotFoundException);
                assertTrue(e.getMessage().indexOf("missing") >= 0);
            }
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that content can be read from a legacy backend without moving it to the active backend.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerReadsLegacyContentWithoutMigratingIt() throws Exception {

        Path activeRepository = Files.createTempDirectory("opencms-fs-storage-active");
        Path legacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy");
        try {
            CmsStorageManager storageManager = new CmsStorageManager(
                null,
                createTwoBackendConfiguration(activeRepository, legacyRepository),
                createAlwaysExternalPolicyConfiguration());
            byte[] content = "legacy content read only".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);
            new CmsFsStorage("fs2", legacyRepository.toString()).storeContent(null, hash, content);

            byte[] loadedContent = storageManager.loadContent(null, null, "fs2", hash);

            assertTrue(Arrays.equals(content, loadedContent));
            assertTrue(Files.isRegularFile(getStoredFile(legacyRepository, hash)));
            assertFalse(Files.exists(getStoredFile(activeRepository, hash)));
        } finally {
            deleteDirectory(activeRepository);
            deleteDirectory(legacyRepository);
        }
    }

    /**
     * Tests that the active backend can not also be configured as a legacy backend.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerRejectsActiveBackendInLegacyList() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsParameterConfiguration configuration = new CmsParameterConfiguration();
            configuration.add("storage.active", "fs1");
            configuration.add("storage.legacy", "fs1");
            configuration.add("storage.backend.fs1.type", "fs");
            configuration.add("storage.backend.fs1.path", repository.toString());

            try {
                new CmsStorageManager(null, configuration, createAlwaysExternalPolicyConfiguration());
                fail("Expected active backend in legacy list to fail.");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().indexOf("Legacy storage list") >= 0);
            }
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that the global storage.backend.path property is not accepted for an id-based backend.<p>
     */
    @Test
    public void testStorageManagerRejectsGlobalPathPropertyForNamedBackend() {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add("storage.active", "fs1");
        configuration.add("storage.backend.fs1.type", "fs");
        configuration.add("storage.backend.path", "/storage/fs1");

        try {
            new CmsStorageManager(null, configuration, createAlwaysExternalPolicyConfiguration());
            fail("Expected missing backend-specific path configuration to fail.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().indexOf("storage.backend.fs1.path") >= 0);
        }
    }

    /**
     * Tests that database storage can not be configured as a named backend.<p>
     */
    @Test
    public void testStorageManagerRejectsNamedDatabaseStorageBackend() {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add("storage.active", "legacydb");
        configuration.add("storage.backend.legacydb.type", "db");

        try {
            new CmsStorageManager(createSqlManager(false), configuration, createAlwaysExternalPolicyConfiguration());
            fail("Expected named database storage backend to fail.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().indexOf("reserved backend id \"db\"") >= 0);
            assertTrue(e.getMessage().indexOf("legacydb") >= 0);
        }
    }

    /**
     * Tests rewriting legacy external content back to local content-table storage.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerRewriteContentFromLegacyBackendToLocalStorage() throws Exception {

        Path activeRepository = Files.createTempDirectory("opencms-fs-storage-active");
        Path legacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy");
        try {
            CmsStorageManager storageManager = new CmsStorageManager(
                createSqlManager(false),
                createTwoBackendConfiguration(activeRepository, legacyRepository),
                createAlwaysLocalPolicyConfiguration());
            byte[] content = "legacy content to local storage".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);
            new CmsFsStorage("fs2", legacyRepository.toString()).storeContent(null, hash, content);

            byte[] loadedContent = storageManager.loadContent(null, null, "fs2", hash);
            StorageResult result = storageManager.prepareContent(null, new CmsStoragePolicyContext(loadedContent));
            storageManager.deleteContent(null, "fs2", hash);

            assertNull(result.getStorage());
            assertNull(result.getHash());
            assertTrue(Arrays.equals(content, result.getFileContent()));
            assertFalse(Files.exists(getStoredFile(activeRepository, hash)));
            assertFalse(Files.exists(getStoredFile(legacyRepository, hash)));
        } finally {
            deleteDirectory(activeRepository);
            deleteDirectory(legacyRepository);
        }
    }

    /**
     * Tests that rewrite-content does not delete a legacy blob while it is still referenced.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerRewriteContentKeepsReferencedLegacyBlob() throws Exception {

        Path activeRepository = Files.createTempDirectory("opencms-fs-storage-active");
        Path legacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy");
        try {
            CmsStorageManager storageManager = new CmsStorageManager(
                createSqlManager(true),
                createTwoBackendConfiguration(activeRepository, legacyRepository),
                createAlwaysExternalPolicyConfiguration());
            byte[] content = "shared legacy content".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);
            new CmsFsStorage("fs2", legacyRepository.toString()).storeContent(null, hash, content);

            StorageResult result = storageManager.prepareContent(
                null,
                new CmsStoragePolicyContext(storageManager.loadContent(null, null, "fs2", hash)));
            storageManager.deleteContent(null, "fs2", hash);

            assertEquals("fs1", result.getStorage());
            assertTrue(Files.isRegularFile(getStoredFile(activeRepository, hash)));
            assertTrue(Files.isRegularFile(getStoredFile(legacyRepository, hash)));
        } finally {
            deleteDirectory(activeRepository);
            deleteDirectory(legacyRepository);
        }
    }

    /**
     * Tests the rewrite-content migration from a legacy backend to the active backend.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerRewriteContentMigratesLegacyBlobToActiveBackend() throws Exception {

        Path activeRepository = Files.createTempDirectory("opencms-fs-storage-active");
        Path legacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy");
        try {
            CmsStorageManager storageManager = new CmsStorageManager(
                createSqlManager(false),
                createTwoBackendConfiguration(activeRepository, legacyRepository),
                createAlwaysExternalPolicyConfiguration());
            byte[] content = "legacy content to rewrite".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);
            new CmsFsStorage("fs2", legacyRepository.toString()).storeContent(null, hash, content);

            byte[] loadedContent = storageManager.loadContent(null, null, "fs2", hash);
            StorageResult result = storageManager.prepareContent(null, new CmsStoragePolicyContext(loadedContent));
            storageManager.deleteContent(null, "fs2", hash);

            assertEquals("fs1", result.getStorage());
            assertEquals(hash, result.getHash());
            assertTrue(Arrays.equals(content, Files.readAllBytes(getStoredFile(activeRepository, hash))));
            assertFalse(Files.exists(getStoredFile(legacyRepository, hash)));
        } finally {
            deleteDirectory(activeRepository);
            deleteDirectory(legacyRepository);
        }
    }

    /**
     * Tests that rewrite-content fails if the required active backend can not store the blob.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerRewriteContentPropagatesActiveBackendFailure() throws Exception {

        Path activeRepository = Files.createTempDirectory("opencms-fs-storage-active");
        Path legacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy");
        try {
            CmsStorageManager storageManager = new CmsStorageManager(
                createSqlManager(false),
                createTwoBackendConfiguration(activeRepository, legacyRepository),
                createAlwaysExternalPolicyConfiguration());
            byte[] content = "legacy content with failing active backend".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);
            new CmsFsStorage("fs2", legacyRepository.toString()).storeContent(null, hash, content);
            Files.createDirectories(getStoredFile(activeRepository, hash));

            byte[] loadedContent = storageManager.loadContent(null, null, "fs2", hash);
            try {
                storageManager.prepareContent(null, new CmsStoragePolicyContext(loadedContent));
                fail("Expected active backend storage failure to fail the rewrite.");
            } catch (CmsDataAccessException e) {
                assertTrue(e.getMessage().indexOf("Failed to store content") >= 0);
            }

            assertTrue(Files.isDirectory(getStoredFile(activeRepository, hash)));
            assertTrue(Files.isRegularFile(getStoredFile(legacyRepository, hash)));
            assertTrue(Arrays.equals(content, storageManager.loadContent(null, null, "fs2", hash)));
        } finally {
            deleteDirectory(activeRepository);
            deleteDirectory(legacyRepository);
        }
    }

    /**
     * Tests that required external storage failures are not converted to local content storage.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerStoreFailureDoesNotFallBackToLocalContent() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsParameterConfiguration configuration = new CmsParameterConfiguration();
            configuration.add("storage.active", "fs1");
            configuration.add("storage.backend.fs1.type", "fs");
            configuration.add("storage.backend.fs1.path", repository.toString());

            CmsStorageManager storageManager = new CmsStorageManager(
                null,
                configuration,
                createAlwaysExternalPolicyConfiguration());
            byte[] content = "content with failing active backend".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);
            Files.createDirectories(getStoredFile(repository, hash));

            try {
                storageManager.prepareContent(null, new CmsStoragePolicyContext(content));
                fail("Expected active backend storage failure to fail the write.");
            } catch (CmsDataAccessException e) {
                assertTrue(e.getMessage().indexOf("Failed to store content") >= 0);
            }
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that the storage manager binds the file system backend path by active backend id.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerUsesBackendSpecificPathProperty() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsParameterConfiguration configuration = new CmsParameterConfiguration();
            configuration.add("storage.active", "fs1");
            configuration.add("storage.backend.fs1.type", "fs");
            configuration.add("storage.backend.fs1.path", repository.toString());

            CmsStorageManager storageManager = new CmsStorageManager(
                null,
                configuration,
                createAlwaysExternalPolicyConfiguration());
            byte[] content = "managed file system content".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);

            StorageResult result = storageManager.prepareContent(null, new CmsStoragePolicyContext(content));

            assertEquals("fs1", result.getStorage());
            assertEquals(hash, result.getHash());
            assertEquals(0, result.getFileContent().length);
            assertTrue(
                Arrays.equals(content, storageManager.loadContent(null, null, result.getStorage(), result.getHash())));
            assertTrue(Files.isRegularFile(getStoredFile(repository, hash)));
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that multiple legacy backends are addressed by the stored storage identifier.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerUsesStoredIdentifierForMultipleLegacyBackends() throws Exception {

        Path activeRepository = Files.createTempDirectory("opencms-fs-storage-active");
        Path firstLegacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy-1");
        Path secondLegacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy-2");
        try {
            CmsParameterConfiguration configuration = new CmsParameterConfiguration();
            configuration.add("storage.active", "fs1");
            configuration.add("storage.legacy", "fs2, fs3");
            configuration.add("storage.backend.fs1.type", "fs");
            configuration.add("storage.backend.fs1.path", activeRepository.toString());
            configuration.add("storage.backend.fs2.type", "fs");
            configuration.add("storage.backend.fs2.path", firstLegacyRepository.toString());
            configuration.add("storage.backend.fs3.type", "fs");
            configuration.add("storage.backend.fs3.path", secondLegacyRepository.toString());
            CmsStorageManager storageManager = new CmsStorageManager(
                createSqlManager(false),
                configuration,
                createAlwaysExternalPolicyConfiguration());
            byte[] content = "second legacy content".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);
            new CmsFsStorage("fs2", firstLegacyRepository.toString()).storeContent(null, hash, content);
            new CmsFsStorage("fs3", secondLegacyRepository.toString()).storeContent(null, hash, content);

            byte[] loadedContent = storageManager.loadContent(null, null, "fs3", hash);
            storageManager.deleteContent(null, "fs3", hash);

            assertTrue(Arrays.equals(content, loadedContent));
            assertTrue(Files.isRegularFile(getStoredFile(firstLegacyRepository, hash)));
            assertFalse(Files.exists(getStoredFile(secondLegacyRepository, hash)));
        } finally {
            deleteDirectory(activeRepository);
            deleteDirectory(firstLegacyRepository);
            deleteDirectory(secondLegacyRepository);
        }
    }

    /**
     * Tests that all configured storage manager backends are validated.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerValidatesActiveAndLegacyBackends() throws Exception {

        Path activeRepository = Files.createTempDirectory("opencms-fs-storage-active");
        Path legacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy");
        try {
            CmsParameterConfiguration configuration = new CmsParameterConfiguration();
            configuration.add("storage.active", "fs1");
            configuration.add("storage.legacy", "fs2");
            configuration.add("storage.backend.fs1.type", "fs");
            configuration.add("storage.backend.fs1.path", activeRepository.toString());
            configuration.add("storage.backend.fs2.type", "fs");
            configuration.add("storage.backend.fs2.path", legacyRepository.toString());

            CmsStorageManager storageManager = new CmsStorageManager(
                null,
                configuration,
                createAlwaysExternalPolicyConfiguration());

            storageManager.validateStorages(null);
        } finally {
            deleteDirectory(activeRepository);
            deleteDirectory(legacyRepository);
        }
    }

    /**
     * Tests that validation fails if a configured legacy backend is unavailable.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerValidationFailsForUnavailableLegacyBackend() throws Exception {

        Path activeRepository = Files.createTempDirectory("opencms-fs-storage-active");
        Path legacyRepository = Files.createTempDirectory("opencms-fs-storage-legacy");
        deleteDirectory(legacyRepository);
        try {
            CmsParameterConfiguration configuration = new CmsParameterConfiguration();
            configuration.add("storage.active", "fs1");
            configuration.add("storage.legacy", "fs2");
            configuration.add("storage.backend.fs1.type", "fs");
            configuration.add("storage.backend.fs1.path", activeRepository.toString());
            configuration.add("storage.backend.fs2.type", "fs");
            configuration.add("storage.backend.fs2.path", legacyRepository.toString());

            CmsStorageManager storageManager = new CmsStorageManager(
                null,
                configuration,
                createAlwaysExternalPolicyConfiguration());

            try {
                storageManager.validateStorages(null);
                fail("Expected unavailable legacy storage to fail.");
            } catch (Exception e) {
                assertTrue(e.getMessage().indexOf("does not exist") >= 0);
            }
        } finally {
            deleteDirectory(activeRepository);
            deleteDirectory(legacyRepository);
        }
    }

    /**
     * Tests that storing the same hash twice keeps the original file untouched.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoreContentDeduplicatesByHash() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsFsStorage storage = new CmsFsStorage("fs1", repository.toString());
            byte[] content = "original content".getBytes(StandardCharsets.UTF_8);
            byte[] otherContent = "other content".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);

            storage.storeContent(null, hash, content);
            storage.storeContent(null, hash, otherContent);

            assertTrue(Arrays.equals(content, storage.loadContent(null, hash)));
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that an existing directory at the target path is not treated as stored content.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoreContentRejectsExistingDirectoryAtTargetPath() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsFsStorage storage = new CmsFsStorage("fs1", repository.toString());
            String hash = createHash("abcdef", '1');
            Files.createDirectories(getStoredFile(repository, hash));

            try {
                storage.storeContent(null, hash, "content".getBytes(StandardCharsets.UTF_8));
                fail("Expected directory at target path to fail.");
            } catch (Exception e) {
                assertTrue(e.getMessage().indexOf("regular file") >= 0);
            }
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests storing, loading and deleting content directly through the file system backend.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoreLoadAndDeleteContent() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsFsStorage storage = new CmsFsStorage("fs1", repository.toString());
            byte[] content = "external file system content".getBytes(StandardCharsets.UTF_8);
            String hash = calculateSha512(content);
            Path storedFile = getStoredFile(repository, hash);

            storage.storeContent(null, hash, content);

            assertTrue(Files.isRegularFile(storedFile));
            assertTrue(Arrays.equals(content, Files.readAllBytes(storedFile)));
            assertTrue(Arrays.equals(content, storage.loadContent(null, hash)));

            storage.deleteContent(null, hash);

            assertFalse(Files.exists(storedFile));
            try {
                storage.loadContent(null, hash);
                fail("Expected deleted content to be missing.");
            } catch (CmsStorageBlobNotFoundException e) {
                assertTrue(e.getMessage().indexOf(hash) >= 0);
            }
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that the availability check requires an existing repository root.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testValidateAvailableRequiresExistingRepositoryRoot() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        deleteDirectory(repository);
        CmsFsStorage storage = new CmsFsStorage("fs1", repository.toString());
        try {
            storage.validateAvailable(null);
            fail("Expected missing repository root to fail.");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("does not exist") >= 0);
        }
    }

    /**
     * Tests that the availability check performs a write/read roundtrip in the repository root.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testValidateAvailableUsesRepositoryRootRoundtrip() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-storage");
        try {
            CmsFsStorage storage = new CmsFsStorage("fs1", repository.toString());
            storage.validateAvailable(null);

            try (Stream<Path> paths = Files.list(repository)) {
                assertFalse(paths.findFirst().isPresent());
            }
        } finally {
            deleteDirectory(repository);
        }
    }
}

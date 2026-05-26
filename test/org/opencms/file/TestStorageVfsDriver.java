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

package org.opencms.file;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.CmsStoragePolicyConfiguration;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.storage.CmsFsStorage;
import org.opencms.db.storage.CmsStorageManager;
import org.opencms.db.storage.policy.CmsDefaultStoragePolicy;
import org.opencms.db.storage.policy.CmsNoExternalStoragePolicy;
import org.opencms.db.storage.policy.CmsStoragePolicyContext;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.setup.CmsSetupDb;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsUUID;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for the storage VFS driver.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestStorageVfsDriver extends OpenCmsTestRunner {

    /** Test content size. */
    private static final int LARGE_CONTENT_SIZE = 600 * 1024;

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/", "WEB-INF/config.storage-regression/");
    }

    /**
     * Tests the default storage policy threshold configuration.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(2)
    public void testDefaultStoragePolicyUsesConfiguredThreshold() throws Exception {

        CmsDefaultStoragePolicy policy = new CmsDefaultStoragePolicy();
        policy.addConfigurationParameter(CmsDefaultStoragePolicy.PARAM_THRESHOLD, "1024");
        policy.initConfiguration();

        assertFalse(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(
                    createSmallContent((byte)1),
                    createResource(CmsResourceTypeBinary.getStaticTypeId()))));
        assertTrue(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(
                    createContent((byte)1),
                    createResource(CmsResourceTypeBinary.getStaticTypeId()))));
    }

    /**
     * Tests the default storage policy resource type decisions.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(1)
    public void testDefaultStoragePolicyUsesResourceType() throws Exception {

        CmsDefaultStoragePolicy policy = new CmsDefaultStoragePolicy();
        byte[] largeContent = createContent((byte)0);
        byte[] smallContent = createSmallContent((byte)0);

        assertTrue(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(largeContent, createResource(CmsResourceTypeBinary.getStaticTypeId()))));
        assertTrue(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(largeContent, createResource(CmsResourceTypeImage.getStaticTypeId()))));
        assertTrue(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(smallContent, createResource(CmsResourceTypeBinary.getStaticTypeId()))));
        assertTrue(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(smallContent, createResource(CmsResourceTypeImage.getStaticTypeId()))));
        assertFalse(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(largeContent, createResource(CmsResourceTypePlain.getStaticTypeId()))));
        assertFalse(policy.isExternalStorageRequired(new CmsStoragePolicyContext(largeContent)));
        assertFalse(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(
                    createEmptyContent(),
                    createResource(CmsResourceTypeBinary.getStaticTypeId()))));
    }

    /**
     * Tests the default storage policy resource type id configuration.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(4)
    public void testDefaultStoragePolicyUsesResourceTypeConfiguration() throws Exception {

        CmsDefaultStoragePolicy policy = new CmsDefaultStoragePolicy();
        policy.addConfigurationParameter(
            CmsDefaultStoragePolicy.PARAM_RESOURCE_TYPE_IDS,
            CmsResourceTypeBinary.getStaticTypeId() + "," + CmsResourceTypePlain.getStaticTypeId());
        policy.initConfiguration();

        byte[] content = createContent((byte)4);

        assertTrue(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(content, createResource(CmsResourceTypeBinary.getStaticTypeId()))));
        assertTrue(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(content, createResource(CmsResourceTypePlain.getStaticTypeId()))));
        assertFalse(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(content, createResource(CmsResourceTypeImage.getStaticTypeId()))));
    }

    /**
     * Tests that deleting large unpublished content removes the storage blob.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(5)
    public void testDeletingContentRemovesUnusedStorageBlob() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/storage-delete.bin";
        CmsResource resource = cms.createResource(
            path,
            CmsResourceTypeBinary.getStaticTypeId(),
            createContent((byte)4),
            null);
        String hash = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        assertTrue(storageEntryExists(hash));

        cms.lockResource(path);
        cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);

        assertFalse(storageEntryExists(hash));
    }

    /**
     * Tests that deleting historical versions removes no longer referenced storage blobs.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(11)
    public void testDeletingHistoricalVersionsRemovesUnusedStorageBlob() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/storage-history-delete.bin";
        byte[] content1 = createContent((byte)15);
        CmsResource resource = cms.createResource(path, CmsResourceTypeBinary.getStaticTypeId(), content1, null);
        String hash1 = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        publishResource(cms, path);

        byte[] content2 = createContent((byte)16);
        writeFile(cms, path, content2);
        String hash2 = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        publishResource(cms, path);

        byte[] content3 = createContent((byte)17);
        writeFile(cms, path, content3);
        String hash3 = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        publishResource(cms, path);

        assertTrue(storageEntryExists(hash1));
        assertTrue(storageEntryExists(hash2));
        assertTrue(storageEntryExists(hash3));

        cms.deleteHistoricalVersions(2, -1, -1, new CmsShellReport(cms.getRequestContext().getLocale()));

        assertFalse(storageEntryExists(hash1));
        assertTrue(storageEntryExists(hash2));
        assertTrue(storageEntryExists(hash3));
    }

    /**
     * Tests that binary content is stored in CMS_STORAGE and can be read offline and online.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(3)
    public void testLargeContentUsesStorage() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/storage-large.bin";
        byte[] content = createContent((byte)1);
        CmsResource resource = cms.createResource(path, CmsResourceTypeBinary.getStaticTypeId(), content, null);
        String offlineHash = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());

        assertNotNull(offlineHash);
        assertEquals("db", readContentStorage("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertTrue(storageEntryExists(offlineHash));
        assertTrue(Arrays.equals(content, cms.readFile(path).getContents()));

        OpenCms.getPublishManager().publishResource(cms, path);
        OpenCms.getPublishManager().waitWhileRunning();

        assertEquals("db", readContentStorage("CMS_CONTENTS", resource.getResourceId().toString()));
        assertEquals(offlineHash, readContentHash("CMS_CONTENTS", resource.getResourceId().toString()));
        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        assertTrue(Arrays.equals(content, cms.readFile(path).getContents()));
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
    }

    /**
     * Tests that the no-external-storage policy keeps all content local.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(3)
    public void testNoExternalStoragePolicyKeepsContentLocal() throws Exception {

        CmsNoExternalStoragePolicy policy = new CmsNoExternalStoragePolicy();

        assertFalse(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(
                    createContent((byte)1),
                    createResource(CmsResourceTypeBinary.getStaticTypeId()))));
        assertFalse(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(
                    createContent((byte)2),
                    createResource(CmsResourceTypeImage.getStaticTypeId()))));
        assertFalse(
            policy.isExternalStorageRequired(
                new CmsStoragePolicyContext(
                    createContent((byte)3),
                    createResource(CmsResourceTypePlain.getStaticTypeId()))));
    }

    /**
     * Tests that replacing externally stored content removes the old unreferenced storage blob.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(4)
    public void testReplacingContentRemovesUnusedStorageBlob() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/storage-replace.bin";
        byte[] originalContent = createContent((byte)2);
        CmsResource resource = cms.createResource(path, CmsResourceTypeBinary.getStaticTypeId(), originalContent, null);
        String originalHash = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        assertTrue(storageEntryExists(originalHash));

        byte[] replacementContent = createContent((byte)3);
        CmsFile file = cms.readFile(path);
        file.setContents(replacementContent);
        cms.lockResource(path);
        cms.writeFile(file);
        cms.unlockResource(path);

        String replacementHash = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        assertFalse(originalHash.equals(replacementHash));
        assertFalse(storageEntryExists(originalHash));
        assertTrue(storageEntryExists(replacementHash));
        assertTrue(Arrays.equals(replacementContent, cms.readFile(path).getContents()));
    }

    /**
     * Tests replacing file content while alternating between local and external storage.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(10)
    public void testReplacingContentWithChangingStoragePolicy() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/storage-replace-policy-switch.bin";
        byte[] largeContent1 = createContent((byte)12);
        CmsResource resource = cms.createResource(path, CmsResourceTypeBinary.getStaticTypeId(), largeContent1, null);
        String hash1 = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        assertEquals("db", readContentStorage("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertTrue(storageEntryExists(hash1));

        byte[] emptyContent = createEmptyContent();
        writeFile(cms, path, emptyContent);
        assertTrue(Arrays.equals(emptyContent, cms.readFile(path).getContents()));
        assertNull(readContentStorage("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertNull(readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertFalse(storageEntryExists(hash1));

        byte[] largeContent2 = createContent((byte)14);
        writeFile(cms, path, largeContent2);
        String hash2 = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        assertTrue(Arrays.equals(largeContent2, cms.readFile(path).getContents()));
        assertEquals("db", readContentStorage("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertNotNull(hash2);
        assertTrue(storageEntryExists(hash2));
    }

    /**
     * Tests that restoring a deleted folder restores externally stored file contents below it.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(8)
    public void testRestoringDeletedFolderWithLargeContentUsesStorage() throws Exception {

        CmsObject cms = getCmsObject();
        String folder = "/storage-restore-folder/";
        String path = folder + "large.bin";
        byte[] content = createContent((byte)8);
        cms.createResource(folder, CmsResourceTypeFolder.getStaticTypeId());
        CmsResource resource = cms.createResource(path, CmsResourceTypeBinary.getStaticTypeId(), content, null);
        String hash = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        publishResource(cms, folder);

        cms.lockResource(folder);
        cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);
        publishResource(cms, folder);

        List<I_CmsHistoryResource> deletedResources = cms.readDeletedResources("/", true);
        I_CmsHistoryResource deletedFolder = null;
        for (I_CmsHistoryResource deletedResource : deletedResources) {
            if (folder.equals(cms.getSitePath((CmsResource)deletedResource))) {
                deletedFolder = deletedResource;
                break;
            }
        }
        assertNotNull(deletedFolder);
        cms.restoreDeletedResource(resource.getStructureId());

        assertTrue(Arrays.equals(content, cms.readFile(path).getContents()));
        assertEquals(hash, readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertTrue(storageEntryExists(hash));
    }

    /**
     * Tests that restoring a published deleted file restores its externally stored content.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(6)
    public void testRestoringDeletedLargeContentUsesStorage() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/storage-restore-deleted.bin";
        byte[] content = createContent((byte)5);
        CmsResource resource = cms.createResource(path, CmsResourceTypeBinary.getStaticTypeId(), content, null);
        String hash = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        publishResource(cms, path);

        cms.lockResource(path);
        cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
        publishResource(cms, path);

        cms.restoreDeletedResource(resource.getStructureId());

        assertTrue(Arrays.equals(content, cms.readFile(path).getContents()));
        assertEquals("db", readContentStorage("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertEquals(hash, readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertTrue(storageEntryExists(hash));
    }

    /**
     * Tests that restoring an old version restores the matching externally stored content.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(7)
    public void testRestoringLargeContentVersionUsesStorage() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/storage-restore-version.bin";
        byte[] content1 = createContent((byte)6);
        CmsResource resource = cms.createResource(path, CmsResourceTypeBinary.getStaticTypeId(), content1, null);
        String hash1 = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        publishResource(cms, path);

        byte[] content2 = createContent((byte)7);
        CmsFile file = cms.readFile(path);
        file.setContents(content2);
        cms.lockResource(path);
        cms.writeFile(file);
        cms.unlockResource(path);
        String hash2 = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        publishResource(cms, path);

        List<I_CmsHistoryResource> versions = cms.readAllAvailableVersions(path);
        assertEquals(2, versions.size());
        cms.lockResource(path);
        cms.restoreResourceVersion(versions.get(1).getStructureId(), versions.get(1).getVersion());

        assertFalse(hash1.equals(hash2));
        assertTrue(Arrays.equals(content1, cms.readFile(path).getContents()));
        assertEquals("db", readContentStorage("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertEquals(hash1, readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertTrue(storageEntryExists(hash1));
    }

    /**
     * Tests restoring file versions which alternate between local and external storage.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(9)
    public void testRestoringVersionsWithChangingStoragePolicy() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/storage-restore-policy-switch.bin";
        byte[] largeContent1 = createContent((byte)9);
        CmsResource resource = cms.createResource(path, CmsResourceTypeBinary.getStaticTypeId(), largeContent1, null);
        String hash1 = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        publishResource(cms, path);

        byte[] emptyContent = createEmptyContent();
        writeFile(cms, path, emptyContent);
        assertNull(readContentStorage("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertNull(readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        publishResource(cms, path);

        byte[] largeContent2 = createContent((byte)11);
        writeFile(cms, path, largeContent2);
        String hash2 = readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString());
        publishResource(cms, path);

        List<I_CmsHistoryResource> versions = cms.readAllAvailableVersions(path);
        assertEquals(3, versions.size());
        assertFalse(hash1.equals(hash2));
        assertTrue(storageEntryExists(hash1));
        assertTrue(storageEntryExists(hash2));

        cms.lockResource(path);
        cms.restoreResourceVersion(versions.get(1).getStructureId(), versions.get(1).getVersion());
        assertTrue(Arrays.equals(emptyContent, cms.readFile(path).getContents()));
        assertNull(readContentStorage("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertNull(readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));

        cms.restoreResourceVersion(versions.get(2).getStructureId(), versions.get(2).getVersion());
        assertTrue(Arrays.equals(largeContent1, cms.readFile(path).getContents()));
        assertEquals("db", readContentStorage("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertEquals(hash1, readContentHash("CMS_OFFLINE_CONTENTS", resource.getResourceId().toString()));
        assertTrue(storageEntryExists(hash1));
    }

    /**
     * Tests that externally stored legacy file system content can be rewritten back into the content tables.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(12)
    public void testRewriteLegacyFsStorageBackToLocalContentTables() throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/storage-fs-legacy-to-local.bin";
        byte[] externalContent = createContent((byte)18);
        byte[] localContent = createContent((byte)19);
        Path repository = Files.createTempDirectory("opencms-storage-legacy-fs");
        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        CmsStorageManager fsStorageManager = null;
        CmsStorageManager localStorageManager = null;

        try {
            org.opencms.db.generic.CmsSqlManager sqlManager = getStorageSqlManager(originalStorageManager);
            fsStorageManager = createStorageManager(
                sqlManager,
                "legacyfs",
                null,
                repository,
                CmsDefaultStoragePolicy.class.getName());
            storageManagerField.set(getVfsDriver(), fsStorageManager);

            CmsResource resource = cms.createResource(
                path,
                CmsResourceTypeBinary.getStaticTypeId(),
                externalContent,
                null);
            String resourceId = resource.getResourceId().toString();
            String legacyHash = readContentHash("CMS_OFFLINE_CONTENTS", resourceId);
            Path legacyBlob = getStoredFile(repository, legacyHash);
            assertEquals("legacyfs", readContentStorage("CMS_OFFLINE_CONTENTS", resourceId));
            assertTrue(Files.exists(legacyBlob));
            assertTrue(Arrays.equals(externalContent, cms.readFile(path).getContents()));

            localStorageManager = createStorageManager(
                sqlManager,
                "db",
                "legacyfs",
                repository,
                CmsNoExternalStoragePolicy.class.getName());
            storageManagerField.set(getVfsDriver(), localStorageManager);

            writeFile(cms, path, localContent);

            assertTrue(Arrays.equals(localContent, cms.readFile(path).getContents()));
            assertNull(readContentStorage("CMS_OFFLINE_CONTENTS", resourceId));
            assertNull(readContentHash("CMS_OFFLINE_CONTENTS", resourceId));
            assertTrue(Arrays.equals(localContent, readContentBytes("CMS_OFFLINE_CONTENTS", resourceId)));
            assertFalse(Files.exists(legacyBlob));

            publishResource(cms, path);

            assertNull(readContentStorage("CMS_CONTENTS", resourceId));
            assertNull(readContentHash("CMS_CONTENTS", resourceId));
            assertTrue(Arrays.equals(localContent, readContentBytes("CMS_CONTENTS", resourceId)));
            cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
            assertTrue(Arrays.equals(localContent, cms.readFile(path).getContents()));
            cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        } finally {
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (fsStorageManager != null) {
                fsStorageManager.close();
            }
            if (localStorageManager != null) {
                localStorageManager.close();
            }
            deleteDirectory(repository);
        }
    }

    /**
     * Creates test content.<p>
     *
     * @param seed the byte value to fill the content with
     *
     * @return the content bytes
     */
    private byte[] createContent(byte seed) {

        byte[] content = new byte[LARGE_CONTENT_SIZE];
        Arrays.fill(content, seed);
        return content;
    }

    /**
     * Creates empty test content.<p>
     *
     * @return the empty content bytes
     */
    private byte[] createEmptyContent() {

        return new byte[0];
    }

    /**
     * Creates a minimal test resource for policy tests.<p>
     *
     * @param typeId the resource type id
     *
     * @return the resource
     */
    private CmsResource createResource(int typeId) {

        return new CmsResource(
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            "/test",
            typeId,
            false,
            0,
            CmsUUID.getNullUUID(),
            CmsResource.STATE_UNCHANGED,
            0,
            CmsUUID.getNullUUID(),
            0,
            CmsUUID.getNullUUID(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            0,
            0,
            1);
    }

    /**
     * Creates small test content.<p>
     *
     * @param seed the byte value to fill the content with
     *
     * @return the content bytes
     */
    private byte[] createSmallContent(byte seed) {

        byte[] content = new byte[128];
        Arrays.fill(content, seed);
        return content;
    }

    /**
     * Creates a storage manager with a single optional file system backend.<p>
     *
     * @param sqlManager the SQL manager
     * @param activeStorage the active storage id
     * @param legacyStorage the legacy storage id, or <code>null</code>
     * @param repository the file system repository path
     * @param policyClassName the storage policy class name
     *
     * @return the storage manager
     */
    private CmsStorageManager createStorageManager(
        org.opencms.db.generic.CmsSqlManager sqlManager,
        String activeStorage,
        String legacyStorage,
        Path repository,
        String policyClassName) {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add("storage.active", activeStorage);
        if (legacyStorage != null) {
            configuration.add("storage.legacy", legacyStorage);
        }
        configuration.add("storage.backend.legacyfs.type", CmsFsStorage.STORAGE_TYPE);
        configuration.add("storage.backend.legacyfs.path", repository.toString());

        CmsStoragePolicyConfiguration policyConfiguration = new CmsStoragePolicyConfiguration();
        policyConfiguration.setClassName(policyClassName);
        return new CmsStorageManager(sqlManager, configuration, policyConfiguration);
    }

    /**
     * Deletes a directory tree if it exists.<p>
     *
     * @param directory the directory
     *
     * @throws Exception if deleting fails
     */
    private void deleteDirectory(Path directory) throws Exception {

        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
        }
    }

    /**
     * Finds a field in a class hierarchy.<p>
     *
     * @param type the type
     * @param fieldName the field name
     *
     * @return the field
     *
     * @throws NoSuchFieldException if the field can not be found
     */
    private Field getField(Class<?> type, String fieldName) throws NoSuchFieldException {

        Class<?> currentType = type;
        while (currentType != null) {
            try {
                Field result = currentType.getDeclaredField(fieldName);
                result.setAccessible(true);
                return result;
            } catch (NoSuchFieldException e) {
                currentType = currentType.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    /**
     * Gets the SQL manager from a storage manager.<p>
     *
     * @param storageManager the storage manager
     *
     * @return the SQL manager
     *
     * @throws Exception if the field can not be read
     */
    private org.opencms.db.generic.CmsSqlManager getStorageSqlManager(CmsStorageManager storageManager)
    throws Exception {

        return (org.opencms.db.generic.CmsSqlManager)getField(CmsStorageManager.class, "m_sqlManager").get(
            storageManager);
    }

    /**
     * Gets the file for a file system storage hash.<p>
     *
     * @param repository the repository path
     * @param hash the content hash
     *
     * @return the stored file path
     */
    private Path getStoredFile(Path repository, String hash) {

        return repository.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4)).resolve(
            hash.substring(4, 6)).resolve(hash);
    }

    /**
     * Gets the current VFS driver.<p>
     *
     * @return the VFS driver
     *
     * @throws Exception if the driver can not be accessed
     */
    private I_CmsVfsDriver getVfsDriver() throws Exception {

        CmsDriverManager driverManager = (CmsDriverManager)getField(
            OpenCms.getSqlManager().getClass(),
            "m_driverManager").get(OpenCms.getSqlManager());
        return driverManager.getVfsDriver();
    }

    /**
     * Publishes a resource and waits for the publish job to finish.<p>
     *
     * @param cms the CMS context
     * @param path the resource path
     *
     * @throws Exception if publishing fails
     */
    private void publishResource(CmsObject cms, String path) throws Exception {

        OpenCms.getPublishManager().publishResource(cms, path);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Reads the FILE_CONTENT value for a content row.<p>
     *
     * @param tableName the table name
     * @param resourceId the resource id
     *
     * @return the FILE_CONTENT value
     *
     * @throws SQLException if reading fails
     */
    private byte[] readContentBytes(String tableName, String resourceId) throws SQLException {

        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = setupDb.getConnection().prepareStatement(
                "SELECT FILE_CONTENT FROM " + tableName + " WHERE RESOURCE_ID=?");
            stmt.setString(1, resourceId);
            res = stmt.executeQuery();
            assertTrue(res.next(), "No content row found in " + tableName + " for " + resourceId);
            return res.getBytes(1);
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            setupDb.closeConnection();
        }
    }

    /**
     * Reads a value for a content row.<p>
     *
     * @param tableName the table name
     * @param columnName the column name
     * @param resourceId the resource id
     *
     * @return the column value
     *
     * @throws SQLException if reading fails
     */
    private String readContentColumn(String tableName, String columnName, String resourceId) throws SQLException {

        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = setupDb.getConnection().prepareStatement(
                "SELECT " + columnName + " FROM " + tableName + " WHERE RESOURCE_ID=?");
            stmt.setString(1, resourceId);
            res = stmt.executeQuery();
            assertTrue(res.next(), "No content row found in " + tableName + " for " + resourceId);
            return res.getString(1);
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            setupDb.closeConnection();
        }
    }

    /**
     * Reads the HASH value for a content row.<p>
     *
     * @param tableName the table name
     * @param resourceId the resource id
     *
     * @return the HASH value
     *
     * @throws SQLException if reading fails
     */
    private String readContentHash(String tableName, String resourceId) throws SQLException {

        return readContentColumn(tableName, "HASH", resourceId);
    }

    /**
     * Reads the STORAGE value for a content row.<p>
     *
     * @param tableName the table name
     * @param resourceId the resource id
     *
     * @return the STORAGE value
     *
     * @throws SQLException if reading fails
     */
    private String readContentStorage(String tableName, String resourceId) throws SQLException {

        return readContentColumn(tableName, "STORAGE", resourceId);
    }

    /**
     * Checks if a storage entry exists.<p>
     *
     * @param hash the content hash
     *
     * @return <code>true</code> if the storage row exists
     *
     * @throws SQLException if reading fails
     */
    private boolean storageEntryExists(String hash) throws SQLException {

        CmsSetupDb setupDb = getSetupDbForDefaultConnection();
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = setupDb.getConnection().prepareStatement("SELECT 1 FROM CMS_STORAGE WHERE HASH=?");
            stmt.setString(1, hash);
            res = stmt.executeQuery();
            return res.next();
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            setupDb.closeConnection();
        }
    }

    /**
     * Writes content to an existing file.<p>
     *
     * @param cms the CMS context
     * @param path the resource path
     * @param content the new content
     *
     * @throws Exception if writing fails
     */
    private void writeFile(CmsObject cms, String path, byte[] content) throws Exception {

        CmsFile file = cms.readFile(path);
        file.setContents(content);
        cms.lockResource(path);
        cms.writeFile(file);
        cms.unlockResource(path);
    }

}

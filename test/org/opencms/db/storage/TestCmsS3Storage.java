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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.storage.s3.CmsGenericS3Client;
import org.opencms.db.storage.s3.CmsS3ClientConfiguration;
import org.opencms.db.storage.s3.I_CmsS3Client;
import org.opencms.security.I_CmsCredentialsResolver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests for the S3 storage backend.<p>
 */
public class TestCmsS3Storage {

    /**
     * Credentials resolver test double.<p>
     */
    private static class TestCredentialsResolver implements I_CmsCredentialsResolver {

        /**
         * @see org.opencms.security.I_CmsCredentialsResolver#resolveCredential(java.lang.String, java.lang.String)
         */
        public String resolveCredential(String credentialType, String valueFromConfiguration) {

            if (I_CmsCredentialsResolver.S3_ACCESS_KEY.equals(credentialType)) {
                return "resolved-access";
            }
            if (I_CmsCredentialsResolver.S3_SECRET_KEY.equals(credentialType)) {
                return "resolved-secret";
            }
            return valueFromConfiguration;
        }
    }

    /**
     * S3 client test double.<p>
     */
    private static class TestS3Client implements I_CmsS3Client {

        /** The stored objects. */
        private final Map<String, byte[]> m_objects = new HashMap<String, byte[]>();

        /** Whether reads should return wrong content. */
        private boolean m_returnWrongContent;

        /** The last deleted key. */
        private String m_deletedKey;

        /** Whether the bucket access check was called. */
        private boolean m_bucketAccessValidated;

        /** Whether the client was closed. */
        private boolean m_closed;

        /** The last checked key. */
        private String m_existsKey;

        /** Exception thrown from exists(). */
        private Exception m_existsException;

        /** Exception thrown from getObject(). */
        private Exception m_getObjectException;

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#close()
         */
        public void close() throws Exception {

            m_closed = true;
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#deleteObject(java.lang.String)
         */
        public void deleteObject(String key) throws Exception {

            m_deletedKey = key;
            m_objects.remove(key);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#exists(java.lang.String)
         */
        public boolean exists(String key) throws Exception {

            m_existsKey = key;
            if (m_existsException != null) {
                throw m_existsException;
            }
            return m_objects.containsKey(key);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#getObject(java.lang.String)
         */
        public byte[] getObject(String key) throws Exception {

            if (m_getObjectException != null) {
                throw m_getObjectException;
            }
            if (m_returnWrongContent) {
                return new byte[] {1};
            }
            return m_objects.get(key);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#putObject(java.lang.String, byte[])
         */
        public void putObject(String key, byte[] content) throws Exception {

            m_objects.put(key, content);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#validateBucketAccess()
         */
        public void validateBucketAccess() throws Exception {

            m_bucketAccessValidated = true;
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#visitObjectKeys(org.opencms.db.storage.s3.I_CmsS3Client.I_CmsS3ObjectKeyVisitor)
         */
        public void visitObjectKeys(I_CmsS3ObjectKeyVisitor visitor) throws Exception {

            for (String key : m_objects.keySet()) {
                visitor.visit(key);
            }
        }
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
     * Returns the active storage from a storage manager.<p>
     *
     * @param storageManager the storage manager
     * @return the active storage
     * @throws Exception if reflection fails
     */
    private static I_CmsStorage getActiveStorage(CmsStorageManager storageManager) throws Exception {

        Field field = CmsStorageManager.class.getDeclaredField("m_activeStorage");
        field.setAccessible(true);
        return (I_CmsStorage)field.get(storageManager);
    }

    /**
     * Tests that closing the S3 storage closes the S3 client.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testCloseDelegatesToClient() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, client);

        storage.close();

        assertTrue(client.m_closed);
    }

    /**
     * Tests that delete is delegated without a preceding existence check.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeleteContentIsIdempotentAndDoesNotCheckExists() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, client);
        String hash = createHash("abcdef", '1');

        storage.deleteContent(null, hash);

        assertEquals("ab/cd/ef/" + hash, client.m_deletedKey);
        assertNull(client.m_existsKey);
    }

    /**
     * Tests that S3 storage rejects invalid hashes consistently.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testInvalidHashIsRejected() throws Exception {

        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, new TestS3Client());

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
        try {
            storage.deleteContent(null, null);
            fail("Expected null hash to fail.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Tests that missing S3 blobs are propagated as storage missing blob exceptions.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testLoadContentPropagatesMissingBlob() throws Exception {

        TestS3Client client = new TestS3Client();
        String hash = createHash("abcdef", '1');
        client.m_getObjectException = new CmsStorageBlobNotFoundException("missing");
        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, client);

        try {
            storage.loadContent(null, hash);
            fail("Expected missing blob to fail.");
        } catch (CmsStorageBlobNotFoundException e) {
            assertTrue(e.getMessage().indexOf("s3test") >= 0);
            assertTrue(e.getMessage().indexOf(hash) >= 0);
        }
    }

    /**
     * Tests that S3 credentials are resolved through the OpenCms credentials resolver.<p>
     */
    @Test
    public void testS3CredentialsAreResolved() {

        CmsS3ClientConfiguration configuration = CmsS3ClientConfiguration.createDefault(
            "http://localhost:9000",
            "bucket",
            "configured-access",
            "configured-secret",
            true);

        CmsS3ClientConfiguration resolved = CmsGenericS3Client.resolveCredentials(
            configuration,
            new TestCredentialsResolver());

        assertEquals("resolved-access", resolved.getAccessKey());
        assertEquals("resolved-secret", resolved.getSecretKey());
        assertEquals("http://localhost:9000", resolved.getEndpoint());
        assertEquals("bucket", resolved.getBucketName());
    }

    /**
     * Tests that the storage identifier is based on the configured backend id, not on the bucket name.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageIdentifierUsesBackendId() throws Exception {

        CmsS3Storage storage = new CmsS3Storage(
            "s3prod",
            "very-long-production-bucket-name-that-would-not-fit-the-storage-column",
            true,
            new TestS3Client());

        assertEquals("s3prod", storage.getStorageIdentifier());
    }

    /**
     * Tests that S3 timeout and retry settings are read from backend configuration.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageManagerReadsClientSettings() throws Exception {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add("storage.active", "s3test");
        configuration.add("storage.backend.s3test.type", "s3");
        configuration.add("storage.backend.s3test.endpoint", "http://localhost:9000");
        configuration.add("storage.backend.s3test.bucket", "bucket");
        configuration.add("storage.backend.s3test.accessKey", "access");
        configuration.add("storage.backend.s3test.secretKey", "secret");
        configuration.add("storage.backend.s3test.pathStyle", "false");
        configuration.add("storage.backend.s3test.region", "eu-central-1");
        configuration.add("storage.backend.s3test.connectionTimeout", "1234");
        configuration.add("storage.backend.s3test.socketTimeout", "2345");
        configuration.add("storage.backend.s3test.apiCallAttemptTimeout", "3456");
        configuration.add("storage.backend.s3test.apiCallTimeout", "4567");
        configuration.add("storage.backend.s3test.maxRetries", "4");

        CmsS3Storage storage = (CmsS3Storage)getActiveStorage(new CmsStorageManager(null, configuration));

        assertEquals("s3test", storage.getStorageIdentifier());
        assertEquals(false, storage.isPathStyle());
        assertEquals(1234, storage.getConnectionTimeout());
        assertEquals(2345, storage.getSocketTimeout());
        assertEquals(3456, storage.getApiCallAttemptTimeout());
        assertEquals(4567, storage.getApiCallTimeout());
        assertEquals(4, storage.getMaxRetries());
        assertEquals("eu-central-1", storage.getRegion());
    }

    /**
     * Tests that the S3 storage uses the production client defaults if no explicit values are supplied.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStorageUsesDefaultClientSettings() throws Exception {

        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, new TestS3Client());

        assertEquals(CmsGenericS3Client.DEFAULT_CONNECTION_TIMEOUT, storage.getConnectionTimeout());
        assertEquals(CmsGenericS3Client.DEFAULT_SOCKET_TIMEOUT, storage.getSocketTimeout());
        assertEquals(CmsGenericS3Client.DEFAULT_API_CALL_ATTEMPT_TIMEOUT, storage.getApiCallAttemptTimeout());
        assertEquals(CmsGenericS3Client.DEFAULT_API_CALL_TIMEOUT, storage.getApiCallTimeout());
        assertEquals(CmsGenericS3Client.DEFAULT_MAX_RETRIES, storage.getMaxRetries());
        assertEquals(CmsGenericS3Client.DEFAULT_REGION, storage.getRegion());
    }

    /**
     * Tests that existence check failures are propagated during store.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoreContentPropagatesExistsFailure() throws Exception {

        TestS3Client client = new TestS3Client();
        client.m_existsException = new IllegalStateException("backend unavailable");
        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, client);
        String hash = createHash("abcdef", '1');

        try {
            storage.storeContent(null, hash, "content".getBytes("UTF-8"));
            fail("Expected exists failure to be propagated.");
        } catch (IllegalStateException e) {
            assertEquals("backend unavailable", e.getMessage());
        }
    }

    /**
     * Tests that null content is rejected explicitly.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoreContentRejectsNullContent() throws Exception {

        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, new TestS3Client());

        try {
            storage.storeContent(null, createHash("abcdef", '1'), null);
            fail("Expected null content to fail.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().indexOf("must not be null") >= 0);
        }
    }

    /**
     * Tests that the S3 availability check fails if read content differs from written content.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testValidateAvailableFailsOnWrongReadContent() throws Exception {

        TestS3Client client = new TestS3Client();
        client.m_returnWrongContent = true;
        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, client);

        try {
            storage.validateAvailable(null);
            fail("Expected wrong read content to fail.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().indexOf("read/write check failed") >= 0);
            assertTrue(e.getMessage().indexOf("s3test") >= 0);
        }
        assertTrue(client.m_bucketAccessValidated);
        assertTrue(client.m_objects.isEmpty());
    }

    /**
     * Tests that the S3 availability check performs and cleans up a write/read roundtrip.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testValidateAvailableUsesRoundtripAndDeletesHealthcheckObject() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, client);

        storage.validateAvailable(null);

        assertTrue(client.m_bucketAccessValidated);
        assertTrue(client.m_objects.isEmpty());
        assertNotNull(client.m_deletedKey);
        assertTrue(client.m_deletedKey.startsWith(".opencms-healthcheck/"));
    }

    /**
     * Tests that content hash enumeration filters S3 housekeeping objects and invalid keys.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testVisitContentHashesFiltersObjectKeys() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3Storage storage = new CmsS3Storage("s3test", "bucket", true, client);
        String hash = createHash("abcdef", '1');
        String secondHash = createHash("123456", 'a');
        client.m_objects.put("ab/cd/ef/" + hash, new byte[0]);
        client.m_objects.put("12/34/56/" + secondHash, new byte[0]);
        client.m_objects.put(".opencms-healthcheck/test", new byte[0]);
        client.m_objects.put(hash, new byte[0]);

        List<String> hashes = new ArrayList<String>();
        storage.visitContentHashes(null, hashValue -> hashes.add(hashValue));

        assertEquals(2, hashes.size());
        assertTrue(hashes.contains(hash));
        assertTrue(hashes.contains(secondHash));
    }
}

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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Optional integration tests for the S3 storage backend.<p>
 *
 * These tests run against an externally provided S3-compatible endpoint, for
 * example a local RustFS or MinIO container. They are skipped unless the required
 * connection properties are supplied as system properties or environment variables.<p>
 */
public class TestCmsS3StorageIntegration {

    /** Environment variable for the S3 test access key. */
    private static final String ENV_ACCESS_KEY = "OPENCMS_S3_TEST_ACCESS_KEY";

    /** Environment variable for the S3 test bucket. */
    private static final String ENV_BUCKET = "OPENCMS_S3_TEST_BUCKET";

    /** Environment variable for the S3 test endpoint. */
    private static final String ENV_ENDPOINT = "OPENCMS_S3_TEST_ENDPOINT";

    /** Environment variable for path-style access. */
    private static final String ENV_PATH_STYLE = "OPENCMS_S3_TEST_PATH_STYLE";

    /** Environment variable for the S3 test secret key. */
    private static final String ENV_SECRET_KEY = "OPENCMS_S3_TEST_SECRET_KEY";

    /** System property for the S3 test access key. */
    private static final String PROP_ACCESS_KEY = "opencms.s3.test.accessKey";

    /** System property for the S3 test bucket. */
    private static final String PROP_BUCKET = "opencms.s3.test.bucket";

    /** System property for the S3 test endpoint. */
    private static final String PROP_ENDPOINT = "opencms.s3.test.endpoint";

    /** System property for path-style access. */
    private static final String PROP_PATH_STYLE = "opencms.s3.test.pathStyle";

    /** System property for the S3 test secret key. */
    private static final String PROP_SECRET_KEY = "opencms.s3.test.secretKey";

    /**
     * Creates a SHA-512 hash for the given content.<p>
     *
     * @param content the content
     * @return the hash
     * @throws Exception if hashing fails
     */
    private static String calculateSha512(byte[] content) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hash = digest.digest(content);
        StringBuilder result = new StringBuilder(hash.length * 2);
        for (int i = 0; i < hash.length; i++) {
            int value = hash[i] & 0xff;
            if (value < 16) {
                result.append('0');
            }
            result.append(Integer.toHexString(value));
        }
        return result.toString();
    }

    /**
     * Creates the S3 storage for the configured integration test endpoint.<p>
     *
     * @return the S3 storage
     */
    private static CmsS3Storage createStorage() {

        String endpoint = getRequiredConfig(PROP_ENDPOINT, ENV_ENDPOINT);
        String bucket = getRequiredConfig(PROP_BUCKET, ENV_BUCKET);
        String accessKey = getRequiredConfig(PROP_ACCESS_KEY, ENV_ACCESS_KEY);
        String secretKey = getRequiredConfig(PROP_SECRET_KEY, ENV_SECRET_KEY);
        boolean pathStyle = Boolean.parseBoolean(getConfig(PROP_PATH_STYLE, ENV_PATH_STYLE, "true"));
        return new CmsS3Storage("integration", endpoint, bucket, accessKey, secretKey, pathStyle);
    }

    /**
     * Gets a test configuration value.<p>
     *
     * @param propertyName the system property name
     * @param environmentName the environment variable name
     * @param defaultValue the default value
     * @return the configuration value
     */
    private static String getConfig(String propertyName, String environmentName, String defaultValue) {

        String result = System.getProperty(propertyName);
        if (isEmpty(result)) {
            result = System.getenv(environmentName);
        }
        if (isEmpty(result)) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Gets a required test configuration value or skips the test if it is missing.<p>
     *
     * @param propertyName the system property name
     * @param environmentName the environment variable name
     * @return the configuration value
     */
    private static String getRequiredConfig(String propertyName, String environmentName) {

        String result = getConfig(propertyName, environmentName, null);
        Assumptions.assumeTrue(
            !isEmpty(result),
            "Skipping S3 integration test because " + propertyName + " / " + environmentName + " is not configured.");
        return result;
    }

    /**
     * Returns true if the given value is empty.<p>
     *
     * @param value the value
     * @return true if the value is empty
     */
    private static boolean isEmpty(String value) {

        return (value == null) || (value.trim().length() == 0);
    }

    /**
     * Tests storing, loading and deleting content against a configured S3 backend.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testStoreLoadAndDeleteAgainstConfiguredS3Backend() throws Exception {

        CmsS3Storage storage = createStorage();
        byte[] content = ("OpenCms S3 integration test " + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8);
        String hash = calculateSha512(content);

        try {
            storage.storeContent(null, hash, content);
            assertArrayEquals(content, storage.loadContent(null, hash));

            storage.storeContent(null, hash, "different content".getBytes(StandardCharsets.UTF_8));
            assertArrayEquals(content, storage.loadContent(null, hash));

            assertEquals("integration", storage.getStorageIdentifier());
        } finally {
            storage.deleteContent(null, hash);
        }

        try {
            storage.loadContent(null, hash);
            fail("Expected deleted content to be missing.");
        } catch (CmsStorageBlobNotFoundException e) {
            // expected
        }
    }

    /**
     * Tests that a configured S3 backend accepts the availability roundtrip.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testValidateAvailableAgainstConfiguredS3Backend() throws Exception {

        createStorage().validateAvailable(null);
    }
}

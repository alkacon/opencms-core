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

import org.opencms.db.CmsDbContext;
import org.opencms.db.storage.s3.CmsGenericS3Client;
import org.opencms.db.storage.s3.CmsS3ClientConfiguration;
import org.opencms.db.storage.s3.I_CmsS3Client;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/**
 * S3 content storage implementation (e.g., RustFS).<p>
 *
 * This implementation stores binary content in an S3-compatible object storage.
 * To ensure high performance for read and write operations, it is highly recommended
 * to use a <b>local object storage</b> (e.g., RustFS or an on-premise S3 appliance)
 * within the same network as the OpenCms application server. Using remote cloud
 * storage may introduce latency that significantly impacts VFS performance.<p>
 *
 * Content is addressed by its unique hash to support deduplication across
 * different resources.<p>
 */
public class CmsS3Storage extends A_CmsStorage implements I_CmsEnumerableStorage {

    /** The type name of the storage implementation. */
    public static final String STORAGE_TYPE = "s3";

    /** The S3 client. */
    private final I_CmsS3Client m_s3Client;

    /** The client configuration. */
    private final CmsS3ClientConfiguration m_configuration;

    /** The storage identifier. */
    private final String m_identifier;

    /** Whether path-style access should be used. */
    private final boolean m_pathStyle;

    /**
     * Creates a new S3 storage.
     *
     * @param name the configured backend id used as storage identifier
     * @param configuration the S3 client configuration
     */
    public CmsS3Storage(String name, CmsS3ClientConfiguration configuration) {

        m_configuration = configuration;
        m_identifier = name;
        m_pathStyle = configuration.isPathStyle();
        m_s3Client = new CmsGenericS3Client(configuration);
    }

    /**
     * Creates a new S3 storage.
     *
     * @param name the configured backend id used as storage identifier
     * @param endpoint the S3 endpoint
     * @param bucketName the bucket name
     * @param accessKey the access key
     * @param secretKey the secret key
     * @param pathStyle whether path-style access should be used
     */
    public CmsS3Storage(
        String name,
        String endpoint,
        String bucketName,
        String accessKey,
        String secretKey,
        boolean pathStyle) {

        this(name, CmsS3ClientConfiguration.createDefault(endpoint, bucketName, accessKey, secretKey, pathStyle));
    }

    /**
     * Creates a new S3 storage.
     *
     * @param name the configured backend id used as storage identifier
     * @param endpoint the S3 endpoint
     * @param bucketName the bucket name
     * @param accessKey the access key
     * @param secretKey the secret key
     * @param pathStyle whether path-style access should be used
     * @param connectionTimeout the connection timeout in milliseconds
     * @param socketTimeout the socket timeout in milliseconds
     * @param apiCallAttemptTimeout the timeout for a single API call attempt in milliseconds
     * @param apiCallTimeout the timeout for the complete API call in milliseconds
     * @param maxRetries the maximum number of retries
     */
    public CmsS3Storage(
        String name,
        String endpoint,
        String bucketName,
        String accessKey,
        String secretKey,
        boolean pathStyle,
        int connectionTimeout,
        int socketTimeout,
        int apiCallAttemptTimeout,
        int apiCallTimeout,
        int maxRetries) {

        this(
            name,
            endpoint,
            bucketName,
            accessKey,
            secretKey,
            pathStyle,
            CmsS3ClientConfiguration.DEFAULT_REGION,
            connectionTimeout,
            socketTimeout,
            apiCallAttemptTimeout,
            apiCallTimeout,
            maxRetries);
    }

    /**
     * Creates a new S3 storage.
     *
     * @param name the configured backend id used as storage identifier
     * @param endpoint the S3 endpoint
     * @param bucketName the bucket name
     * @param accessKey the access key
     * @param secretKey the secret key
     * @param pathStyle whether path-style access should be used
     * @param region the AWS region
     * @param connectionTimeout the connection timeout in milliseconds
     * @param socketTimeout the socket timeout in milliseconds
     * @param apiCallAttemptTimeout the timeout for a single API call attempt in milliseconds
     * @param apiCallTimeout the timeout for the complete API call in milliseconds
     * @param maxRetries the maximum number of retries
     */
    public CmsS3Storage(
        String name,
        String endpoint,
        String bucketName,
        String accessKey,
        String secretKey,
        boolean pathStyle,
        String region,
        int connectionTimeout,
        int socketTimeout,
        int apiCallAttemptTimeout,
        int apiCallTimeout,
        int maxRetries) {

        this(
            name,
            new CmsS3ClientConfiguration(
                endpoint,
                bucketName,
                accessKey,
                secretKey,
                pathStyle,
                region,
                connectionTimeout,
                socketTimeout,
                apiCallAttemptTimeout,
                apiCallTimeout,
                maxRetries));
    }

    /**
     * Creates a new S3 storage with a custom client.<p>
     *
     * @param name the configured backend id used as storage identifier
     * @param bucketName the bucket name
     * @param pathStyle whether path-style access should be used
     * @param s3Client the S3 client
     */
    CmsS3Storage(String name, String bucketName, boolean pathStyle, I_CmsS3Client s3Client) {

        m_configuration = CmsS3ClientConfiguration.createDefault(
            "http://localhost",
            bucketName,
            "access",
            "secret",
            pathStyle);
        m_identifier = name;
        m_pathStyle = pathStyle;
        m_s3Client = s3Client;
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#close()
     */
    @Override
    public void close() throws Exception {

        m_s3Client.close();
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#deleteContent(org.opencms.db.CmsDbContext, java.lang.String)
     */
    @Override
    public void deleteContent(CmsDbContext dbc, String hash) throws Exception {

        String s3Key = getHashedPath(hash);
        m_s3Client.deleteObject(s3Key);
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#getStorageIdentifier()
     */
    @Override
    public String getStorageIdentifier() {

        return m_identifier;
    }

    /**
     * Returns whether path-style access is configured.
     *
     * @return true if path-style access is configured
     */
    public boolean isPathStyle() {

        return m_pathStyle;
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#loadContent(org.opencms.db.CmsDbContext, java.lang.String)
     */
    @Override
    public byte[] loadContent(CmsDbContext dbc, String hash) throws Exception {

        String s3Key = getHashedPath(hash);
        try {
            byte[] result = m_s3Client.getObject(s3Key);
            if (result == null) {
                throw new CmsStorageBlobNotFoundException(
                    Messages.get().getBundle().key(Messages.ERR_STORAGE_BLOB_MISSING_2, getStorageIdentifier(), hash));
            }
            return result;
        } catch (CmsStorageBlobNotFoundException e) {
            throw new CmsStorageBlobNotFoundException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_BLOB_MISSING_2, getStorageIdentifier(), hash),
                e);
        }
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#storeContent(org.opencms.db.CmsDbContext, java.lang.String, byte[])
     */
    @Override
    public void storeContent(CmsDbContext dbc, String hash, byte[] content) throws Exception {

        if (content == null) {
            throw new IllegalArgumentException(Messages.get().getBundle().key(Messages.ERR_STORAGE_CONTENT_NULL_0));
        }
        String s3Key = getHashedPath(hash);
        if (!m_s3Client.exists(s3Key)) {
            m_s3Client.putObject(s3Key, content);
        }
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#validateAvailable(org.opencms.db.CmsDbContext)
     */
    @Override
    public void validateAvailable(CmsDbContext dbc) throws Exception {

        String key = ".opencms-healthcheck/" + UUID.randomUUID().toString();
        byte[] content = "OpenCms storage health check".getBytes(StandardCharsets.UTF_8);
        Exception failure = null;
        boolean contentStored = false;
        try {
            m_s3Client.validateBucketAccess();
            m_s3Client.putObject(key, content);
            contentStored = true;
            byte[] readContent = m_s3Client.getObject(key);
            if (!Arrays.equals(content, readContent)) {
                throw new IllegalStateException(
                    Messages.get().getBundle().key(
                        Messages.ERR_STORAGE_S3_READ_WRITE_1,
                        m_configuration.getBucketName()) + " " + getDiagnosticContext(key));
            }
        } catch (Exception e) {
            failure = e;
            throw e;
        } finally {
            if (contentStored) {
                try {
                    m_s3Client.deleteObject(key);
                } catch (Exception e) {
                    if (failure != null) {
                        failure.addSuppressed(e);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * @see org.opencms.db.storage.I_CmsEnumerableStorage#visitContentHashes(org.opencms.db.CmsDbContext, org.opencms.db.storage.I_CmsEnumerableStorage.I_CmsContentHashVisitor)
     */
    @Override
    public void visitContentHashes(CmsDbContext dbc, I_CmsContentHashVisitor visitor) throws Exception {

        m_s3Client.visitObjectKeys(key -> {
            String hash = getHashFromObjectKey(key);
            if (hash != null) {
                visitor.visit(hash);
            }
        });
    }

    /**
     * Returns the configured single API call attempt timeout.
     *
     * @return the timeout in milliseconds
     */
    int getApiCallAttemptTimeout() {

        return m_configuration.getApiCallAttemptTimeout();
    }

    /**
     * Returns the configured complete API call timeout.
     *
     * @return the timeout in milliseconds
     */
    int getApiCallTimeout() {

        return m_configuration.getApiCallTimeout();
    }

    /**
     * Returns the configured connection timeout.
     *
     * @return the timeout in milliseconds
     */
    int getConnectionTimeout() {

        return m_configuration.getConnectionTimeout();
    }

    /**
     * Returns the configured maximum number of retries.
     *
     * @return the maximum number of retries
     */
    int getMaxRetries() {

        return m_configuration.getMaxRetries();
    }

    /**
     * Returns the configured AWS region.
     *
     * @return the AWS region
     */
    String getRegion() {

        return m_configuration.getRegion();
    }

    /**
     * Returns the configured socket timeout.
     *
     * @return the timeout in milliseconds
     */
    int getSocketTimeout() {

        return m_configuration.getSocketTimeout();
    }

    /**
     * Gets diagnostic context for health check failures.<p>
     *
     * @param key the S3 key
     * @return the diagnostic context
     */
    private String getDiagnosticContext(String key) {

        return "storage="
            + getStorageIdentifier()
            + ", endpoint="
            + m_configuration.getEndpoint()
            + ", bucket="
            + m_configuration.getBucketName()
            + ", key="
            + key
            + ", region="
            + m_configuration.getRegion()
            + ", pathStyle="
            + m_configuration.isPathStyle();
    }

    /**
     * Extracts a valid content hash from an object key.<p>
     *
     * @param key the object key
     * @return the content hash, or null if the key is not a content object
     */
    private String getHashFromObjectKey(String key) {

        if (key == null) {
            return null;
        }
        int slashPos = key.lastIndexOf('/');
        String candidate = slashPos >= 0 ? key.substring(slashPos + 1) : key;
        try {
            String hash = validateHash(candidate);
            String expectedKey = getHashedPath(hash);
            return expectedKey.equals(key) ? hash : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

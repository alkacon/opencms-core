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

package org.opencms.db.storage.s3;

import org.opencms.util.CmsStringUtil;

import software.amazon.awssdk.regions.Region;

/**
 * Configuration for the S3 client used by the storage backend.<p>
 */
public class CmsS3ClientConfiguration {

    /** Default complete API call timeout in milliseconds. */
    public static final int DEFAULT_API_CALL_TIMEOUT = 60000;

    /** Default single API call attempt timeout in milliseconds. */
    public static final int DEFAULT_API_CALL_ATTEMPT_TIMEOUT = 30000;

    /** Default connection timeout in milliseconds. */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

    /** Default maximum number of retries. */
    public static final int DEFAULT_MAX_RETRIES = 2;

    /** Default AWS region. */
    public static final String DEFAULT_REGION = Region.AWS_GLOBAL.id();

    /** Default socket timeout in milliseconds. */
    public static final int DEFAULT_SOCKET_TIMEOUT = 30000;

    /** The S3 access key. */
    private final String m_accessKey;

    /** The complete API call timeout in milliseconds. */
    private final int m_apiCallTimeout;

    /** The single API call attempt timeout in milliseconds. */
    private final int m_apiCallAttemptTimeout;

    /** The bucket name. */
    private final String m_bucketName;

    /** The connection timeout in milliseconds. */
    private final int m_connectionTimeout;

    /** The endpoint. */
    private final String m_endpoint;

    /** The maximum number of retries. */
    private final int m_maxRetries;

    /** Whether path-style access should be used. */
    private final boolean m_pathStyle;

    /** The AWS region. */
    private final String m_region;

    /** The S3 secret key. */
    private final String m_secretKey;

    /** The socket timeout in milliseconds. */
    private final int m_socketTimeout;

    /**
     * Creates a new S3 client configuration.<p>
     *
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
    public CmsS3ClientConfiguration(
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

        validateRequired("endpoint", endpoint);
        validateRequired("bucketName", bucketName);
        validateRequired("accessKey", accessKey);
        validateRequired("secretKey", secretKey);
        validateRequired("region", region);
        validatePositive("connectionTimeout", connectionTimeout);
        validatePositive("socketTimeout", socketTimeout);
        validatePositive("apiCallAttemptTimeout", apiCallAttemptTimeout);
        validatePositive("apiCallTimeout", apiCallTimeout);
        validateNonNegative("maxRetries", maxRetries);
        m_endpoint = endpoint.trim();
        m_bucketName = bucketName.trim();
        m_accessKey = accessKey;
        m_secretKey = secretKey;
        m_pathStyle = pathStyle;
        m_region = region.trim();
        m_connectionTimeout = connectionTimeout;
        m_socketTimeout = socketTimeout;
        m_apiCallAttemptTimeout = apiCallAttemptTimeout;
        m_apiCallTimeout = apiCallTimeout;
        m_maxRetries = maxRetries;
    }

    /**
     * Creates a new S3 client configuration using default client settings.<p>
     *
     * @param endpoint the S3 endpoint
     * @param bucketName the bucket name
     * @param accessKey the access key
     * @param secretKey the secret key
     * @param pathStyle whether path-style access should be used
     *
     * @return the configuration
     */
    public static CmsS3ClientConfiguration createDefault(
        String endpoint,
        String bucketName,
        String accessKey,
        String secretKey,
        boolean pathStyle) {

        return new CmsS3ClientConfiguration(
            endpoint,
            bucketName,
            accessKey,
            secretKey,
            pathStyle,
            DEFAULT_REGION,
            DEFAULT_CONNECTION_TIMEOUT,
            DEFAULT_SOCKET_TIMEOUT,
            DEFAULT_API_CALL_ATTEMPT_TIMEOUT,
            DEFAULT_API_CALL_TIMEOUT,
            DEFAULT_MAX_RETRIES);
    }

    /**
     * Validates that a configuration value is not negative.<p>
     *
     * @param name the configuration name
     * @param value the configuration value
     */
    private static void validateNonNegative(String name, int value) {

        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative.");
        }
    }

    /**
     * Validates that a configuration value is positive.<p>
     *
     * @param name the configuration name
     * @param value the configuration value
     */
    private static void validatePositive(String name, int value) {

        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive.");
        }
    }

    /**
     * Validates that a configuration value is not empty.<p>
     *
     * @param name the configuration name
     * @param value the configuration value
     */
    private static void validateRequired(String name, String value) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
    }

    /**
     * Returns the access key.<p>
     *
     * @return the access key
     */
    public String getAccessKey() {

        return m_accessKey;
    }

    /**
     * Returns the single API call attempt timeout.<p>
     *
     * @return the timeout in milliseconds
     */
    public int getApiCallAttemptTimeout() {

        return m_apiCallAttemptTimeout;
    }

    /**
     * Returns the complete API call timeout.<p>
     *
     * @return the timeout in milliseconds
     */
    public int getApiCallTimeout() {

        return m_apiCallTimeout;
    }

    /**
     * Returns the bucket name.<p>
     *
     * @return the bucket name
     */
    public String getBucketName() {

        return m_bucketName;
    }

    /**
     * Returns the connection timeout.<p>
     *
     * @return the timeout in milliseconds
     */
    public int getConnectionTimeout() {

        return m_connectionTimeout;
    }

    /**
     * Returns the endpoint.<p>
     *
     * @return the endpoint
     */
    public String getEndpoint() {

        return m_endpoint;
    }

    /**
     * Returns the maximum number of retries.<p>
     *
     * @return the maximum number of retries
     */
    public int getMaxRetries() {

        return m_maxRetries;
    }

    /**
     * Returns the configured AWS region.<p>
     *
     * @return the AWS region
     */
    public String getRegion() {

        return m_region;
    }

    /**
     * Returns the secret key.<p>
     *
     * @return the secret key
     */
    public String getSecretKey() {

        return m_secretKey;
    }

    /**
     * Returns the socket timeout.<p>
     *
     * @return the timeout in milliseconds
     */
    public int getSocketTimeout() {

        return m_socketTimeout;
    }

    /**
     * Returns whether path-style access is configured.<p>
     *
     * @return true if path-style access is configured
     */
    public boolean isPathStyle() {

        return m_pathStyle;
    }
}

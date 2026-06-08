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

import org.opencms.db.storage.CmsStorageBlobNotFoundException;
import org.opencms.db.storage.CmsStorageException;
import org.opencms.db.storage.Messages;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsDefaultCredentialsResolver;
import org.opencms.security.I_CmsCredentialsResolver;

import java.net.URI;
import java.time.Duration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * S3 client using AWS SDK v2 optimized for a local object storage.<p>
 *
 * Should work with most products such as SeaweedFS, MinIO, Garage, Ceph.<p>
 */
public class CmsGenericS3Client implements I_CmsS3Client {

    /** Default complete API call timeout in milliseconds. */
    public static final int DEFAULT_API_CALL_TIMEOUT = CmsS3ClientConfiguration.DEFAULT_API_CALL_TIMEOUT;

    /** Default single API call attempt timeout in milliseconds. */
    public static final int DEFAULT_API_CALL_ATTEMPT_TIMEOUT = CmsS3ClientConfiguration.DEFAULT_API_CALL_ATTEMPT_TIMEOUT;

    /** Default connection timeout in milliseconds. */
    public static final int DEFAULT_CONNECTION_TIMEOUT = CmsS3ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT;

    /** Default maximum number of retries. */
    public static final int DEFAULT_MAX_RETRIES = CmsS3ClientConfiguration.DEFAULT_MAX_RETRIES;

    /** Default AWS region. */
    public static final String DEFAULT_REGION = CmsS3ClientConfiguration.DEFAULT_REGION;

    /** Default socket timeout in milliseconds. */
    public static final int DEFAULT_SOCKET_TIMEOUT = CmsS3ClientConfiguration.DEFAULT_SOCKET_TIMEOUT;

    /** The S3 client. */
    private final S3Client m_s3Client;

    /** The client configuration. */
    private final CmsS3ClientConfiguration m_configuration;

    /**
     * Creates a new local S3 client.<p>
     *
     * @param configuration the S3 client configuration
     */
    public CmsGenericS3Client(CmsS3ClientConfiguration configuration) {

        m_configuration = resolveCredentials(configuration);
        ApacheHttpClient.Builder httpClientBuilder = ApacheHttpClient.builder().connectionTimeout(
            Duration.ofMillis(m_configuration.getConnectionTimeout())).socketTimeout(
                Duration.ofMillis(m_configuration.getSocketTimeout()));
        ClientOverrideConfiguration overrideConfiguration = ClientOverrideConfiguration.builder().apiCallAttemptTimeout(
            Duration.ofMillis(m_configuration.getApiCallAttemptTimeout())).apiCallTimeout(
                Duration.ofMillis(m_configuration.getApiCallTimeout())).retryPolicy(
                    RetryPolicy.builder().numRetries(m_configuration.getMaxRetries()).build()).build();
        m_s3Client = S3Client.builder().endpointOverride(URI.create(m_configuration.getEndpoint())).credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(m_configuration.getAccessKey(), m_configuration.getSecretKey()))).region(
                    Region.of(m_configuration.getRegion())).forcePathStyle(
                        m_configuration.isPathStyle()).httpClientBuilder(httpClientBuilder).overrideConfiguration(
                            overrideConfiguration).build();
    }

    /**
     * Creates a new local S3 client.<p>
     *
     * @param endpoint the URL of the S3 server (e.g., "http://localhost:8080")
     * @param bucketName the bucket name
     * @param accessKey the access key
     * @param secretKey the secret key
     * @param pathStyle whether path-style access should be used
     */
    public CmsGenericS3Client(
        String endpoint,
        String bucketName,
        String accessKey,
        String secretKey,
        boolean pathStyle) {

        this(CmsS3ClientConfiguration.createDefault(endpoint, bucketName, accessKey, secretKey, pathStyle));
    }

    /**
     * Creates a new local S3 client.<p>
     *
     * @param endpoint the URL of the S3 server (e.g., "http://localhost:8080")
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
    public CmsGenericS3Client(
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
            new CmsS3ClientConfiguration(
                endpoint,
                bucketName,
                accessKey,
                secretKey,
                pathStyle,
                DEFAULT_REGION,
                connectionTimeout,
                socketTimeout,
                apiCallAttemptTimeout,
                apiCallTimeout,
                maxRetries));
    }

    /**
     * Resolves credentials in the S3 client configuration with the OpenCms credentials resolver.<p>
     *
     * @param configuration the original S3 client configuration
     * @return the S3 client configuration with resolved credentials
     */
    public static CmsS3ClientConfiguration resolveCredentials(CmsS3ClientConfiguration configuration) {

        return resolveCredentials(configuration, getCredentialsResolver());
    }

    /**
     * Resolves credentials in the S3 client configuration.<p>
     *
     * @param configuration the original S3 client configuration
     * @param credentialsResolver the credentials resolver
     * @return the S3 client configuration with resolved credentials
     */
    public static CmsS3ClientConfiguration resolveCredentials(
        CmsS3ClientConfiguration configuration,
        I_CmsCredentialsResolver credentialsResolver) {

        String accessKey = credentialsResolver.resolveCredential(
            I_CmsCredentialsResolver.S3_ACCESS_KEY,
            configuration.getAccessKey());
        String secretKey = credentialsResolver.resolveCredential(
            I_CmsCredentialsResolver.S3_SECRET_KEY,
            configuration.getSecretKey());
        return new CmsS3ClientConfiguration(
            configuration.getEndpoint(),
            configuration.getBucketName(),
            accessKey,
            secretKey,
            configuration.isPathStyle(),
            configuration.getRegion(),
            configuration.getConnectionTimeout(),
            configuration.getSocketTimeout(),
            configuration.getApiCallAttemptTimeout(),
            configuration.getApiCallTimeout(),
            configuration.getMaxRetries());
    }

    /**
     * Returns the OpenCms credentials resolver, or the default resolver if OpenCms is not initialized far enough.<p>
     *
     * @return the credentials resolver
     */
    private static I_CmsCredentialsResolver getCredentialsResolver() {

        try {
            return OpenCms.getCredentialsResolver();
        } catch (RuntimeException e) {
            return new CmsDefaultCredentialsResolver();
        }
    }

    @Override
    public void close() throws Exception {

        m_s3Client.close();
    }

    @Override
    public void deleteObject(String key) throws Exception {

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(m_configuration.getBucketName()).key(
                key).build();
            m_s3Client.deleteObject(request);
        } catch (S3Exception e) {
            throw createStorageException("DELETE", key, e);
        } catch (RuntimeException e) {
            throw createStorageException("DELETE", key, e);
        }
    }

    @Override
    public boolean exists(String key) throws Exception {

        try {
            HeadObjectRequest request = HeadObjectRequest.builder().bucket(m_configuration.getBucketName()).key(
                key).build();
            m_s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw createStorageException("HEAD", key, e);
        } catch (RuntimeException e) {
            throw createStorageException("HEAD", key, e);
        }
    }

    @Override
    public byte[] getObject(String key) throws Exception {

        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(m_configuration.getBucketName()).key(
                key).build();
            ResponseBytes<GetObjectResponse> objectBytes = m_s3Client.getObjectAsBytes(request);
            return objectBytes.asByteArray();
        } catch (NoSuchKeyException e) {
            throw createBlobNotFoundException("GET", key, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw createBlobNotFoundException("GET", key, e);
            }
            throw createStorageException("GET", key, e);
        } catch (RuntimeException e) {
            throw createStorageException("GET", key, e);
        }
    }

    @Override
    public void putObject(String key, byte[] content) throws Exception {

        try {
            PutObjectRequest request = PutObjectRequest.builder().bucket(m_configuration.getBucketName()).key(
                key).build();
            m_s3Client.putObject(request, RequestBody.fromBytes(content));
        } catch (S3Exception e) {
            throw createStorageException("PUT", key, e);
        } catch (RuntimeException e) {
            throw createStorageException("PUT", key, e);
        }
    }

    @Override
    public void validateBucketAccess() throws Exception {

        try {
            HeadBucketRequest request = HeadBucketRequest.builder().bucket(m_configuration.getBucketName()).build();
            m_s3Client.headBucket(request);
        } catch (S3Exception e) {
            throw createStorageException("HEAD_BUCKET", null, e);
        } catch (RuntimeException e) {
            throw createStorageException("HEAD_BUCKET", null, e);
        }
    }

    @Override
    public void visitObjectKeys(I_CmsS3ObjectKeyVisitor visitor) throws Exception {

        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(
                m_configuration.getBucketName()).build();
            for (ListObjectsV2Response page : m_s3Client.listObjectsV2Paginator(request)) {
                for (S3Object object : page.contents()) {
                    visitor.visit(object.key());
                }
            }
        } catch (S3Exception e) {
            throw createStorageException("LIST", null, e);
        } catch (RuntimeException e) {
            throw createStorageException("LIST", null, e);
        }
    }

    /**
     * Creates a missing blob exception with S3 context.<p>
     *
     * @param operation the S3 operation
     * @param key the S3 key
     * @param cause the cause
     * @return the missing blob exception
     */
    private CmsStorageBlobNotFoundException createBlobNotFoundException(String operation, String key, Exception cause) {

        return new CmsStorageBlobNotFoundException(
            Messages.get().getBundle().key(Messages.ERR_STORAGE_BLOB_MISSING_2, m_configuration.getBucketName(), key)
                + " "
                + createContext(operation, key, cause),
            cause);
    }

    /**
     * Creates the S3 diagnostic context.<p>
     *
     * @param operation the S3 operation
     * @param key the S3 key
     * @param cause the cause
     * @return the context
     */
    private String createContext(String operation, String key, Exception cause) {

        StringBuilder result = new StringBuilder(192);
        result.append("operation=").append(operation);
        result.append(", endpoint=").append(m_configuration.getEndpoint());
        result.append(", bucket=").append(m_configuration.getBucketName());
        if (key != null) {
            result.append(", key=").append(key);
        }
        result.append(", region=").append(m_configuration.getRegion());
        result.append(", pathStyle=").append(m_configuration.isPathStyle());
        if (cause instanceof S3Exception) {
            S3Exception s3Exception = (S3Exception)cause;
            result.append(", statusCode=").append(s3Exception.statusCode());
            if (s3Exception.awsErrorDetails() != null) {
                result.append(", errorCode=").append(s3Exception.awsErrorDetails().errorCode());
            }
            result.append(", requestId=").append(s3Exception.requestId());
        }
        return result.toString();
    }

    /**
     * Creates an exception with S3 context.<p>
     *
     * @param operation the S3 operation
     * @param key the S3 key
     * @param cause the cause
     * @return the storage exception
     */
    private CmsStorageException createStorageException(String operation, String key, Exception cause) {

        return new CmsStorageException("S3 storage operation failed. " + createContext(operation, key, cause), cause);
    }
}

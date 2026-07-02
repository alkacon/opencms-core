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

package org.opencms.loader;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.db.storage.CmsStorageManager;
import org.opencms.db.storage.s3.CmsGenericS3Client;
import org.opencms.db.storage.s3.CmsS3ClientConfiguration;
import org.opencms.db.storage.s3.I_CmsS3Client;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.OutputStream;

/**
 * S3 based storage for generated image cache entries.<p>
 */
public class CmsS3ImageCache implements I_CmsImageCache, I_CmsConfigurationParameterHandler {

    /** Configuration parameter for the S3 bucket. */
    public static final String PARAM_BUCKET = "bucket";

    /** Configuration parameter for the S3 object key prefix. */
    public static final String PARAM_PREFIX = "prefix";

    /** The configuration parameters. */
    private CmsParameterConfiguration m_configuration = new CmsParameterConfiguration();

    /** The S3 client. */
    private I_CmsS3Client m_s3Client;

    /** The object key prefix. */
    private String m_prefix = "";

    /**
     * Creates a new uninitialized image cache.<p>
     */
    public CmsS3ImageCache() {

        // empty
    }

    /**
     * Creates a new image cache.<p>
     *
     * @param configuration the S3 configuration
     * @throws Exception if the configured bucket can not be accessed
     */
    public CmsS3ImageCache(CmsS3ClientConfiguration configuration)
    throws Exception {

        this(configuration, null);
    }

    /**
     * Creates a new image cache.<p>
     *
     * @param configuration the S3 configuration
     * @param prefix the object key prefix
     * @throws Exception if the configured bucket can not be accessed
     */
    public CmsS3ImageCache(CmsS3ClientConfiguration configuration, String prefix)
    throws Exception {

        initClient(new CmsGenericS3Client(configuration), prefix);
    }

    /**
     * Creates a new image cache with a custom client.<p>
     *
     * @param s3Client the S3 client
     * @throws Exception if the configured bucket can not be accessed
     */
    CmsS3ImageCache(I_CmsS3Client s3Client)
    throws Exception {

        this(s3Client, null);
    }

    /**
     * Creates a new image cache with a custom client.<p>
     *
     * @param s3Client the S3 client
     * @param prefix the object key prefix
     * @throws Exception if the configured bucket can not be accessed
     */
    CmsS3ImageCache(I_CmsS3Client s3Client, String prefix)
    throws Exception {

        initClient(s3Client, prefix);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_configuration.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#exists(java.lang.String)
     */
    public boolean exists(String key) throws Exception {

        ensureInitialized();
        return m_s3Client.exists(getObjectKey(key));
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#getLength(java.lang.String)
     */
    public long getLength(String key) throws Exception {

        ensureInitialized();
        return m_s3Client.getObjectLength(getObjectKey(key));
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        String bucket = m_configuration.get(PARAM_BUCKET);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(bucket)) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, PARAM_BUCKET));
        }
        String prefix = m_configuration.get(PARAM_PREFIX);
        try {
            CmsParameterConfiguration propertyConfiguration = new CmsParameterConfiguration(
                OpenCms.getSystemInfo().getConfigurationFileRfsPath());
            CmsS3ClientConfiguration activeConfiguration = CmsStorageManager.createS3ClientConfiguration(
                propertyConfiguration,
                null,
                null);
            if (activeConfiguration.getBucketName().equals(bucket) && CmsStringUtil.isEmptyOrWhitespaceOnly(prefix)) {
                throw new CmsConfigurationException(
                    Messages.get().container(Messages.ERR_IMAGE_CACHE_CONFIG_S3_PREFIX_2, PARAM_PREFIX, bucket));
            }
            initClient(
                new CmsGenericS3Client(
                    CmsStorageManager.createS3ClientConfiguration(propertyConfiguration, null, bucket)),
                prefix);
        } catch (CmsConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, getClass().getName()),
                e);
        }
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#supportsRangeDelivery()
     */
    public boolean supportsRangeDelivery() {

        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#write(java.lang.String, byte[])
     */
    public void write(String key, byte[] content) throws Exception {

        ensureInitialized();
        m_s3Client.putObject(getObjectKey(key), content);
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#writeRangeTo(java.lang.String, long, long, java.io.OutputStream)
     */
    public void writeRangeTo(String key, long start, long length, OutputStream out) throws Exception {

        ensureInitialized();
        m_s3Client.writeObjectRangeTo(getObjectKey(key), start, length, out);
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#writeTo(java.lang.String, java.io.OutputStream)
     */
    public void writeTo(String key, OutputStream out) throws Exception {

        ensureInitialized();
        m_s3Client.writeObjectTo(getObjectKey(key), out);
    }

    /**
     * Initializes the S3 client.<p>
     *
     * @param s3Client the S3 client
     * @param prefix the object key prefix
     * @throws Exception if the configured bucket can not be accessed
     */
    protected void initClient(I_CmsS3Client s3Client, String prefix) throws Exception {

        m_prefix = normalizePrefix(prefix);
        m_s3Client = s3Client;
        m_s3Client.validateBucketAccess();
    }

    /**
     * Ensures the cache has been initialized.<p>
     */
    private void ensureInitialized() {

        if (m_s3Client == null) {
            throw new IllegalStateException("Image cache has not been initialized.");
        }
    }

    /**
     * Returns the S3 object key.<p>
     *
     * @param key the image cache key
     * @return the object key
     */
    private String getObjectKey(String key) {

        String normalizedKey = key;
        while (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        return m_prefix + normalizedKey;
    }

    /**
     * Normalizes an S3 object key prefix.<p>
     *
     * @param prefix the raw prefix
     * @return the normalized prefix
     */
    private String normalizePrefix(String prefix) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(prefix)) {
            return "";
        }
        String result = prefix.trim();
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            return "";
        }
        return result + "/";
    }
}

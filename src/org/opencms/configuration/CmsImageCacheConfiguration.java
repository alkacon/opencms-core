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

package org.opencms.configuration;

import java.time.Duration;

import org.apache.commons.codec.digest.DigestUtils;

import org.dom4j.Element;

/**
 * Central configuration for image cache retention and maintenance.<p>
 *
 * The configuration is optional for the classic RFS cache. If it is not present in {@code opencms-system.xml},
 * {@link #isConfigured()} returns {@code false} and the legacy image cache behavior applies. External FS and S3
 * image caches require an explicit configuration.<p>
 */
public class CmsImageCacheConfiguration {

    /** Retention modes. */
    public enum RetentionMode {

        /** Cache retention is managed outside OpenCms. */
        external("external"),

        /** Entries expire based on their creation or replacement timestamp. */
        fixed("fixed"),

        /** Entries are renewed when successfully used within the renewal window. */
        renewOnUse("renew-on-use");

        /** The XML value. */
        private String m_xmlValue;

        /**
         * Creates a retention mode.<p>
         *
         * @param xmlValue the XML value
         */
        RetentionMode(String xmlValue) {

            m_xmlValue = xmlValue;
        }

        /**
         * Parses a retention mode.<p>
         *
         * @param value the XML value
         * @return the retention mode
         */
        public static RetentionMode fromXmlValue(String value) {

            for (RetentionMode mode : values()) {
                if (mode.getXmlValue().equals(value)) {
                    return mode;
                }
            }
            throw new IllegalArgumentException("Unsupported image cache retention mode: " + value);
        }

        /**
         * Returns the XML value.<p>
         *
         * @return the XML value
         */
        public String getXmlValue() {

            return m_xmlValue;
        }
    }

    /** Default maximum number of deletes per maintenance run. */
    public static final int DEFAULT_CLEANUP_MAX_DELETES_PER_RUN = 10000;

    /** Default maximum runtime per maintenance run. */
    public static final String DEFAULT_CLEANUP_MAX_RUNTIME = "PT5M";

    /** Default FS touch concurrency. */
    public static final int DEFAULT_FS_TOUCH_CONCURRENCY = 1;

    /** Default RFS minimum touch interval. */
    public static final String DEFAULT_RFS_TOUCH_MINIMUM_INTERVAL = "PT1H";

    /** Default S3 copy concurrency. */
    public static final int DEFAULT_S3_COPY_CONCURRENCY = 2;

    /** Default S3 delete batch size. */
    public static final int DEFAULT_S3_DELETE_BATCH_SIZE = 1000;

    /** Default S3 delete concurrency. */
    public static final int DEFAULT_S3_DELETE_CONCURRENCY = 1;

    /** Default maximum number of S3 copies per second. */
    public static final int DEFAULT_S3_MAX_COPIES_PER_SECOND = 20;

    /** Default S3 renewal queue capacity. */
    public static final int DEFAULT_S3_RENEWAL_QUEUE_CAPACITY = 10000;

    /** The maximum number of deletes per maintenance run. */
    private int m_cleanupMaxDeletesPerRun = DEFAULT_CLEANUP_MAX_DELETES_PER_RUN;

    /** Indicates whether cleanup settings were explicitly configured. */
    private boolean m_cleanupConfigured;

    /** The maximum runtime per maintenance run. */
    private Duration m_cleanupMaxRuntime = Duration.parse(DEFAULT_CLEANUP_MAX_RUNTIME);

    /** The configured maximum runtime per maintenance run. */
    private String m_cleanupMaxRuntimeValue = DEFAULT_CLEANUP_MAX_RUNTIME;

    /** Indicates whether the image cache configuration element was present. */
    private boolean m_configured;

    /** Indicates whether FS touch is enabled. */
    private boolean m_fsTouchEnabled;

    /** The FS touch concurrency. */
    private int m_fsTouchConcurrency = DEFAULT_FS_TOUCH_CONCURRENCY;

    /** Indicates whether FS settings were explicitly configured. */
    private boolean m_fsConfigured;

    /** The maximum cache entry age. */
    private Duration m_maxAge;

    /** The configured maximum cache entry age. */
    private String m_maxAgeValue;

    /** The renewal jitter. */
    private Duration m_renewalJitter;

    /** The configured renewal jitter value. */
    private String m_renewalJitterValue;

    /** The renewal window. */
    private Duration m_renewalWindow;

    /** The configured renewal window value. */
    private String m_renewalWindowValue;

    /** The retention mode. */
    private RetentionMode m_retentionMode;

    /** The RFS minimum touch interval. */
    private Duration m_rfsTouchMinimumInterval = Duration.parse(DEFAULT_RFS_TOUCH_MINIMUM_INTERVAL);

    /** Indicates whether RFS settings were explicitly configured. */
    private boolean m_rfsConfigured;

    /** The configured RFS minimum touch interval. */
    private String m_rfsTouchMinimumIntervalValue = DEFAULT_RFS_TOUCH_MINIMUM_INTERVAL;

    /** The S3 copy concurrency. */
    private int m_s3CopyConcurrency = DEFAULT_S3_COPY_CONCURRENCY;

    /** Indicates whether S3 settings were explicitly configured. */
    private boolean m_s3Configured;

    /** The S3 delete batch size. */
    private int m_s3DeleteBatchSize = DEFAULT_S3_DELETE_BATCH_SIZE;

    /** The S3 delete concurrency. */
    private int m_s3DeleteConcurrency = DEFAULT_S3_DELETE_CONCURRENCY;

    /** The maximum number of S3 copies per second. */
    private int m_s3MaxCopiesPerSecond = DEFAULT_S3_MAX_COPIES_PER_SECOND;

    /** The S3 renewal queue capacity. */
    private int m_s3RenewalQueueCapacity = DEFAULT_S3_RENEWAL_QUEUE_CAPACITY;

    /**
     * Creates a configuration populated from an explicit XML element.<p>
     */
    public CmsImageCacheConfiguration() {

        m_configured = true;
    }

    /**
     * Creates a configuration.<p>
     *
     * @param configured whether an explicit XML element is present
     */
    private CmsImageCacheConfiguration(boolean configured) {

        m_configured = configured;
    }

    /**
     * Creates the configuration representing the absence of an {@code imagecache} element.<p>
     *
     * @return the legacy configuration
     */
    public static CmsImageCacheConfiguration createLegacyConfiguration() {

        return new CmsImageCacheConfiguration(false);
    }

    /**
     * Parses a strict boolean value.<p>
     *
     * @param name the value name
     * @param value the value
     * @return the parsed value
     */
    private static boolean parseBoolean(String name, String value) {

        String normalizedValue = value.trim();
        if ("true".equalsIgnoreCase(normalizedValue)) {
            return true;
        }
        if ("false".equalsIgnoreCase(normalizedValue)) {
            return false;
        }
        throw new IllegalArgumentException(name + " must be either true or false: " + value);
    }

    /**
     * Parses a duration.<p>
     *
     * @param name the value name
     * @param value the value
     * @param zeroAllowed whether zero is allowed
     * @return the parsed duration
     */
    private static Duration parsePositiveDuration(String name, String value, boolean zeroAllowed) {

        if (value == null) {
            throw new IllegalArgumentException(name + " must be configured.");
        }
        Duration result;
        try {
            result = Duration.parse(value.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(name + " is not a valid ISO-8601 duration: " + value, e);
        }
        if (result.isNegative() || (!zeroAllowed && result.isZero())) {
            throw new IllegalArgumentException(name + " must be " + (zeroAllowed ? "non-negative" : "positive"));
        }
        return result;
    }

    /**
     * Parses a positive integer.<p>
     *
     * @param name the value name
     * @param value the value
     * @return the parsed integer
     */
    private static int parsePositiveInt(String name, String value) {

        int result;
        try {
            result = Integer.parseInt(value.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(name + " is not a valid integer: " + value, e);
        }
        if (result <= 0) {
            throw new IllegalArgumentException(name + " must be positive: " + value);
        }
        return result;
    }

    /**
     * Returns a stable string representation for a nullable value.<p>
     *
     * @param value the value
     * @return the string representation
     */
    private static String valueOf(Object value) {

        return value == null ? "" : value.toString();
    }

    /**
     * Appends this configuration to the given system configuration element.<p>
     *
     * Nothing is appended for the internal legacy configuration.<p>
     *
     * @param parent the system configuration element
     * @return the appended image cache element, or {@code null}
     */
    public Element appendToXml(Element parent) {

        if (!isConfigured()) {
            return null;
        }
        Element imageCacheElement = parent.addElement(CmsSystemConfiguration.N_IMAGECACHE);
        Element retentionElement = imageCacheElement.addElement(CmsSystemConfiguration.N_RETENTION);
        retentionElement.addAttribute(CmsSystemConfiguration.A_MODE, getRetentionMode().getXmlValue());
        if (getRetentionMode() != RetentionMode.external) {
            retentionElement.addAttribute(CmsSystemConfiguration.A_MAX_AGE, getMaxAgeValue());
        }
        if (getRetentionMode() == RetentionMode.renewOnUse) {
            retentionElement.addAttribute(CmsSystemConfiguration.A_RENEWAL_WINDOW, getRenewalWindowValue());
            retentionElement.addAttribute(CmsSystemConfiguration.A_RENEWAL_JITTER, getRenewalJitterValue());
        }
        if (m_cleanupConfigured) {
            imageCacheElement.addElement(CmsSystemConfiguration.N_CLEANUP).addAttribute(
                CmsSystemConfiguration.A_MAX_DELETES_PER_RUN,
                Integer.toString(getCleanupMaxDeletesPerRun())).addAttribute(
                    CmsSystemConfiguration.A_MAX_RUNTIME,
                    getCleanupMaxRuntimeValue());
        }
        if (m_rfsConfigured) {
            imageCacheElement.addElement(CmsSystemConfiguration.N_RFS).addAttribute(
                CmsSystemConfiguration.A_TOUCH_MINIMUM_INTERVAL,
                getRfsTouchMinimumIntervalValue());
        }
        if (m_fsConfigured) {
            imageCacheElement.addElement(CmsSystemConfiguration.N_FS).addAttribute(
                CmsSystemConfiguration.A_TOUCH_ENABLED,
                Boolean.toString(isFsTouchEnabled())).addAttribute(
                    CmsSystemConfiguration.A_TOUCH_CONCURRENCY,
                    Integer.toString(getFsTouchConcurrency()));
        }
        if (m_s3Configured) {
            imageCacheElement.addElement(CmsSystemConfiguration.N_S3).addAttribute(
                CmsSystemConfiguration.A_DELETE_BATCH_SIZE,
                Integer.toString(getS3DeleteBatchSize())).addAttribute(
                    CmsSystemConfiguration.A_DELETE_CONCURRENCY,
                    Integer.toString(getS3DeleteConcurrency())).addAttribute(
                        CmsSystemConfiguration.A_COPY_CONCURRENCY,
                        Integer.toString(getS3CopyConcurrency())).addAttribute(
                            CmsSystemConfiguration.A_MAX_COPIES_PER_SECOND,
                            Integer.toString(getS3MaxCopiesPerSecond())).addAttribute(
                                CmsSystemConfiguration.A_RENEWAL_QUEUE_CAPACITY,
                                Integer.toString(getS3RenewalQueueCapacity()));
        }
        return imageCacheElement;
    }

    /**
     * Returns the maximum number of deletes per maintenance run.<p>
     *
     * @return the maximum number of deletes per maintenance run
     */
    public int getCleanupMaxDeletesPerRun() {

        return m_cleanupMaxDeletesPerRun;
    }

    /**
     * Returns the maximum runtime per maintenance run.<p>
     *
     * @return the maximum runtime per maintenance run
     */
    public Duration getCleanupMaxRuntime() {

        return m_cleanupMaxRuntime;
    }

    /**
     * Returns the configured maximum runtime value.<p>
     *
     * @return the configured maximum runtime value
     */
    public String getCleanupMaxRuntimeValue() {

        return m_cleanupMaxRuntimeValue;
    }

    /**
     * Returns the FS touch concurrency.<p>
     *
     * @return the FS touch concurrency
     */
    public int getFsTouchConcurrency() {

        return m_fsTouchConcurrency;
    }

    /**
     * Returns the maximum cache entry age.<p>
     *
     * @return the maximum cache entry age, or {@code null} for externally managed retention
     */
    public Duration getMaxAge() {

        return m_maxAge;
    }

    /**
     * Returns the configured maximum cache entry age.<p>
     *
     * @return the configured maximum cache entry age, or {@code null} for externally managed retention
     */
    public String getMaxAgeValue() {

        return m_maxAgeValue;
    }

    /**
     * Returns a stable fingerprint of the configured policy and maintenance settings.<p>
     *
     * @return the configuration fingerprint
     */
    public String getPolicyFingerprint() {

        return DigestUtils.sha256Hex(
            isConfigured()
                + "|"
                + valueOf(m_retentionMode)
                + "|"
                + valueOf(m_maxAgeValue)
                + "|"
                + valueOf(m_retentionMode == RetentionMode.renewOnUse ? m_renewalWindowValue : null)
                + "|"
                + valueOf(m_retentionMode == RetentionMode.renewOnUse ? m_renewalJitterValue : null)
                + "|"
                + m_cleanupMaxDeletesPerRun
                + "|"
                + m_cleanupMaxRuntimeValue
                + "|"
                + m_rfsTouchMinimumIntervalValue
                + "|"
                + m_fsTouchEnabled
                + "|"
                + m_fsTouchConcurrency
                + "|"
                + m_s3DeleteBatchSize
                + "|"
                + m_s3DeleteConcurrency
                + "|"
                + m_s3CopyConcurrency
                + "|"
                + m_s3MaxCopiesPerSecond
                + "|"
                + m_s3RenewalQueueCapacity);
    }

    /**
     * Returns the renewal jitter.<p>
     *
     * @return the renewal jitter
     */
    public Duration getRenewalJitter() {

        return m_renewalJitter;
    }

    /**
     * Returns the effective renewal jitter value.<p>
     *
     * @return the effective renewal jitter value
     */
    public String getRenewalJitterValue() {

        return m_renewalJitterValue;
    }

    /**
     * Returns the renewal window.<p>
     *
     * @return the renewal window
     */
    public Duration getRenewalWindow() {

        return m_renewalWindow;
    }

    /**
     * Returns the effective renewal window value.<p>
     *
     * @return the effective renewal window value
     */
    public String getRenewalWindowValue() {

        return m_renewalWindowValue;
    }

    /**
     * Returns the retention mode.<p>
     *
     * @return the retention mode, or {@code null} for the legacy configuration
     */
    public RetentionMode getRetentionMode() {

        return m_retentionMode;
    }

    /**
     * Returns the RFS minimum touch interval.<p>
     *
     * @return the RFS minimum touch interval
     */
    public Duration getRfsTouchMinimumInterval() {

        return m_rfsTouchMinimumInterval;
    }

    /**
     * Returns the configured RFS minimum touch interval.<p>
     *
     * @return the configured RFS minimum touch interval
     */
    public String getRfsTouchMinimumIntervalValue() {

        return m_rfsTouchMinimumIntervalValue;
    }

    /**
     * Returns the S3 copy concurrency.<p>
     *
     * @return the S3 copy concurrency
     */
    public int getS3CopyConcurrency() {

        return m_s3CopyConcurrency;
    }

    /**
     * Returns the S3 delete batch size.<p>
     *
     * @return the S3 delete batch size
     */
    public int getS3DeleteBatchSize() {

        return m_s3DeleteBatchSize;
    }

    /**
     * Returns the S3 delete concurrency.<p>
     *
     * @return the S3 delete concurrency
     */
    public int getS3DeleteConcurrency() {

        return m_s3DeleteConcurrency;
    }

    /**
     * Returns the maximum number of S3 copies per second.<p>
     *
     * @return the maximum number of S3 copies per second
     */
    public int getS3MaxCopiesPerSecond() {

        return m_s3MaxCopiesPerSecond;
    }

    /**
     * Returns the S3 renewal queue capacity.<p>
     *
     * @return the S3 renewal queue capacity
     */
    public int getS3RenewalQueueCapacity() {

        return m_s3RenewalQueueCapacity;
    }

    /**
     * Returns whether the configuration was explicitly configured.<p>
     *
     * @return whether the configuration was explicitly configured
     */
    public boolean isConfigured() {

        return m_configured;
    }

    /**
     * Returns whether FS touch is enabled.<p>
     *
     * @return whether FS touch is enabled
     */
    public boolean isFsTouchEnabled() {

        return m_fsTouchEnabled;
    }

    /**
     * Sets the cleanup configuration.<p>
     *
     * @param maxDeletesPerRun the maximum number of deletes per maintenance run
     * @param maxRuntime the maximum runtime per maintenance run
     */
    public void setCleanup(String maxDeletesPerRun, String maxRuntime) {

        m_cleanupConfigured = true;
        if (maxDeletesPerRun != null) {
            m_cleanupMaxDeletesPerRun = parsePositiveInt("cleanup max-deletes-per-run", maxDeletesPerRun);
        }
        if (maxRuntime != null) {
            m_cleanupMaxRuntime = parsePositiveDuration("cleanup max-runtime", maxRuntime, false);
            m_cleanupMaxRuntimeValue = maxRuntime.trim();
        }
    }

    /**
     * Sets the FS configuration.<p>
     *
     * @param touchEnabled whether touch is enabled
     * @param touchConcurrency the touch concurrency
     */
    public void setFs(String touchEnabled, String touchConcurrency) {

        m_fsConfigured = true;
        if (touchEnabled != null) {
            m_fsTouchEnabled = parseBoolean("fs touch-enabled", touchEnabled);
        }
        if (touchConcurrency != null) {
            m_fsTouchConcurrency = parsePositiveInt("fs touch-concurrency", touchConcurrency);
        }
    }

    /**
     * Sets the retention policy.<p>
     *
     * @param mode the retention mode
     * @param maxAge the maximum cache entry age
     * @param renewalWindow the renewal window
     * @param renewalJitter the renewal jitter
     */
    public void setRetention(String mode, String maxAge, String renewalWindow, String renewalJitter) {

        if (mode == null) {
            throw new IllegalArgumentException("Image cache retention mode must be configured.");
        }
        m_retentionMode = RetentionMode.fromXmlValue(mode);
        m_maxAge = null;
        m_maxAgeValue = null;
        m_renewalWindow = null;
        m_renewalWindowValue = null;
        m_renewalJitter = null;
        m_renewalJitterValue = null;
        if (maxAge != null) {
            m_maxAge = parsePositiveDuration("retention max-age", maxAge, false);
            m_maxAgeValue = maxAge.trim();
        }
        if (renewalWindow != null) {
            m_renewalWindow = parsePositiveDuration("retention renewal-window", renewalWindow, false);
            m_renewalWindowValue = renewalWindow.trim();
        }
        if (renewalJitter != null) {
            m_renewalJitter = parsePositiveDuration("retention renewal-jitter", renewalJitter, true);
            m_renewalJitterValue = renewalJitter.trim();
        }
    }

    /**
     * Sets the RFS configuration.<p>
     *
     * @param touchMinimumInterval the minimum touch interval
     */
    public void setRfs(String touchMinimumInterval) {

        m_rfsConfigured = true;
        if (touchMinimumInterval != null) {
            m_rfsTouchMinimumInterval = parsePositiveDuration(
                "rfs touch-minimum-interval",
                touchMinimumInterval,
                false);
            m_rfsTouchMinimumIntervalValue = touchMinimumInterval.trim();
        }
    }

    /**
     * Sets the S3 configuration.<p>
     *
     * @param deleteBatchSize the delete batch size
     * @param deleteConcurrency the delete concurrency
     * @param copyConcurrency the copy concurrency
     * @param maxCopiesPerSecond the maximum number of copies per second
     * @param renewalQueueCapacity the renewal queue capacity
     */
    public void setS3(
        String deleteBatchSize,
        String deleteConcurrency,
        String copyConcurrency,
        String maxCopiesPerSecond,
        String renewalQueueCapacity) {

        m_s3Configured = true;
        if (deleteBatchSize != null) {
            m_s3DeleteBatchSize = parsePositiveInt("s3 delete-batch-size", deleteBatchSize);
            if (m_s3DeleteBatchSize > 1000) {
                throw new IllegalArgumentException("s3 delete-batch-size must not exceed 1000");
            }
        }
        if (deleteConcurrency != null) {
            m_s3DeleteConcurrency = parsePositiveInt("s3 delete-concurrency", deleteConcurrency);
        }
        if (copyConcurrency != null) {
            m_s3CopyConcurrency = parsePositiveInt("s3 copy-concurrency", copyConcurrency);
        }
        if (maxCopiesPerSecond != null) {
            m_s3MaxCopiesPerSecond = parsePositiveInt("s3 max-copies-per-second", maxCopiesPerSecond);
        }
        if (renewalQueueCapacity != null) {
            m_s3RenewalQueueCapacity = parsePositiveInt("s3 renewal-queue-capacity", renewalQueueCapacity);
        }
    }

    /**
     * Validates the complete configuration.<p>
     */
    public void validate() {

        if (!m_configured) {
            return;
        }
        if (m_retentionMode == null) {
            throw new IllegalArgumentException("The image cache retention mode must be configured.");
        }
        if (m_retentionMode == RetentionMode.external) {
            if ((m_maxAge != null) || (m_renewalWindow != null) || (m_renewalJitter != null)) {
                throw new IllegalArgumentException(
                    "Retention time settings must be absent when image cache retention is managed externally.");
            }
            return;
        }
        if (m_maxAge == null) {
            throw new IllegalArgumentException("The image cache retention max-age must be configured.");
        }
        if (m_retentionMode == RetentionMode.renewOnUse) {
            if (m_renewalWindow == null) {
                throw new IllegalArgumentException(
                    "The image cache retention renewal-window must be configured for renew-on-use.");
            }
            if (m_renewalJitter == null) {
                throw new IllegalArgumentException(
                    "The image cache retention renewal-jitter must be configured for renew-on-use.");
            }
            if (m_renewalWindow.compareTo(m_maxAge) >= 0) {
                throw new IllegalArgumentException("The image cache renewal-window must be smaller than max-age.");
            }
            if (m_renewalJitter.compareTo(m_renewalWindow) > 0) {
                throw new IllegalArgumentException(
                    "The image cache renewal-jitter must not exceed the renewal-window.");
            }
        } else if ((m_renewalWindow != null) || (m_renewalJitter != null)) {
            throw new IllegalArgumentException("Renewal settings require image cache mode renew-on-use.");
        }
    }
}

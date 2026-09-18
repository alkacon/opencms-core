/*
 * This library is part of OpenCms -
 * The Open Source Content Management System
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

package org.opencms.loader.imagecache;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Metrics and failures produced by a maintenance request.<p>
 */
public final class CmsImageCacheMaintenanceResult {

    /** Builder for maintenance results. */
    public static final class Builder {

        /** The recorded failures. */
        private Map<String, Exception> m_failures = new LinkedHashMap<String, Exception>();

        /** The operation. */
        private final CmsImageCacheMaintenanceRequest.Operation m_operation;

        /** The requested operation count. */
        private final int m_requested;

        /** The skipped operation count. */
        private int m_skipped;

        /** The successful operation count. */
        private int m_succeeded;

        /**
         * Creates a builder.<p>
         *
         * @param operation the operation
         * @param requested the requested operation count
         */
        public Builder(CmsImageCacheMaintenanceRequest.Operation operation, int requested) {

            if (requested < 0) {
                throw new IllegalArgumentException("The requested operation count must not be negative.");
            }
            m_operation = Objects.requireNonNull(operation, "operation");
            m_requested = requested;
        }

        /**
         * Records a failure.<p>
         *
         * @param key the entry key or operation identifier
         * @param exception the failure
         * @return this builder
         */
        public Builder addFailure(String key, Exception exception) {

            m_failures.put(key, exception);
            return this;
        }

        /**
         * Records a skipped operation.<p>
         *
         * @return this builder
         */
        public Builder addSkipped() {

            m_skipped += 1;
            return this;
        }

        /**
         * Records a successful operation.<p>
         *
         * @return this builder
         */
        public Builder addSuccess() {

            m_succeeded += 1;
            return this;
        }

        /**
         * Creates the result.<p>
         *
         * @param elapsed the elapsed time
         * @return the result
         */
        public CmsImageCacheMaintenanceResult build(Duration elapsed) {

            if ((m_succeeded + m_skipped + m_failures.size()) != m_requested) {
                throw new IllegalStateException("The maintenance result does not contain one outcome per request.");
            }
            return new CmsImageCacheMaintenanceResult(this, elapsed);
        }
    }

    /** The elapsed time. */
    private final Duration m_elapsed;

    /** The failures. */
    private final Map<String, Exception> m_failures;

    /** The operation. */
    private final CmsImageCacheMaintenanceRequest.Operation m_operation;

    /** The requested operation count. */
    private final int m_requested;

    /** The skipped operation count. */
    private final int m_skipped;

    /** The successful operation count. */
    private final int m_succeeded;

    /**
     * Creates a result.<p>
     *
     * @param builder the result builder
     * @param elapsed the elapsed time
     */
    private CmsImageCacheMaintenanceResult(Builder builder, Duration elapsed) {

        m_operation = builder.m_operation;
        m_requested = builder.m_requested;
        m_succeeded = builder.m_succeeded;
        m_skipped = builder.m_skipped;
        m_failures = Collections.unmodifiableMap(new LinkedHashMap<String, Exception>(builder.m_failures));
        m_elapsed = elapsed;
    }

    /** Returns the elapsed time. */
    public Duration getElapsed() {

        return m_elapsed;
    }

    /** Returns the number of failures. */
    public int getFailed() {

        return m_failures.size();
    }

    /** Returns the failures keyed by cache entry. */
    public Map<String, Exception> getFailures() {

        return m_failures;
    }

    /** Returns the operation. */
    public CmsImageCacheMaintenanceRequest.Operation getOperation() {

        return m_operation;
    }

    /** Returns the requested operation count. */
    public int getRequested() {

        return m_requested;
    }

    /** Returns the skipped operation count. */
    public int getSkipped() {

        return m_skipped;
    }

    /** Returns the successful operation count. */
    public int getSucceeded() {

        return m_succeeded;
    }

    /** Returns whether all requested operations succeeded or were skipped. */
    public boolean isSuccessful() {

        return m_failures.isEmpty();
    }
}

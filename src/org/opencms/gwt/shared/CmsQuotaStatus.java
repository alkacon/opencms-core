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

package org.opencms.gwt.shared;

import java.io.Serializable;

/**
 * Snapshot of a budget scope's usage and quota, suitable for both Vaadin and GWT clients.<p>
 *
 * Exclusive usage includes ordinary subsites but excludes subordinate budget scopes.
 * Total usage includes subordinate budget scopes as well.<p>
 */
public class CmsQuotaStatus implements Serializable {

    /**
     * Display states for stored usage and upload projections.<p>
     */
    public enum State {

        /** Below the warning threshold. */
        NORMAL,

        /** At least 80 percent used. */
        WARNING,

        /** Quota reached or exceeded. */
        EXCEEDED,

        /** Explicitly unlimited. */
        UNLIMITED,

        /** No applicable quota configured. */
        UNCONFIGURED,

        /** Usage or configuration could not be determined. */
        UNAVAILABLE
    }

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /** Quota scope root path. */
    private String m_rootPath;

    /** Bytes used by this budget scope, including ordinary subsites but excluding subordinate budget scopes. */
    private long m_usedBytes;

    /** Inclusive bytes used. */
    private long m_totalBytes;

    /** Limit in bytes, or a non-positive marker. */
    private long m_quotaBytes;

    /** Display state for the stored usage or the projected upload. */
    private State m_state;

    /** Whether this scope has an explicit individual quota rather than using a default. */
    private boolean m_individualQuota;

    /**
     * Creates a snapshot. Upload projections use the same shared usage thresholds as stored usage.<p>
     *
     * @param rootPath the quota scope's VFS root path
     * @param usedBytes exclusive usage, or -1 if unavailable
     * @param totalBytes inclusive usage, or -1 if unavailable
     * @param quotaBytes the limit, 0 for unlimited, or -1 if unknown/unconfigured
     * @param state the display state
     */
    public CmsQuotaStatus(String rootPath, long usedBytes, long totalBytes, long quotaBytes, State state) {

        this(rootPath, usedBytes, totalBytes, quotaBytes, state, false);
    }

    /**
     * Creates a snapshot including whether the budget overrides a default.<p>
     *
     * @param rootPath the quota scope's VFS root path
     * @param usedBytes exclusive usage, or -1 if unavailable
     * @param totalBytes inclusive usage, or -1 if unavailable
     * @param quotaBytes the limit, 0 for unlimited, or -1 if unknown/unconfigured
     * @param state the display state
     * @param individualQuota true if the scope has an explicit individual setting
     */
    public CmsQuotaStatus(
        String rootPath,
        long usedBytes,
        long totalBytes,
        long quotaBytes,
        State state,
        boolean individualQuota) {

        m_rootPath = rootPath;
        m_usedBytes = usedBytes;
        m_totalBytes = totalBytes;
        m_quotaBytes = quotaBytes;
        m_state = state;
        m_individualQuota = individualQuota;
    }

    /**
     * Constructor for serialization.<p>
     */
    protected CmsQuotaStatus() {}

    /**
     * Evaluates usage against a positive, finite budget without rounding at the warning boundary.<p>
     *
     * @param used the used bytes
     * @param quota the positive budget in bytes
     * @return normal, warning or exceeded
     */
    public static State getUsageState(long used, long quota) {

        if (used >= quota) {
            return State.EXCEEDED;
        }
        return used >= quota - (quota / 5) ? State.WARNING : State.NORMAL;
    }

    /**
     * Gets the percentage of the storage budget used.<p>
     *
     * @return the actual utilization, including values above 100, or -1 if unavailable
     */
    public double getPercentage() {

        return (m_quotaBytes > 0) && (m_usedBytes >= 0) ? (100.0 * m_usedBytes) / m_quotaBytes : -1;
    }

    /**
     * Gets the storage budget in bytes.<p>
     *
     * @return the limit, 0 for unlimited, or -1 if unknown/unconfigured
     */
    public long getQuotaBytes() {

        return m_quotaBytes;
    }

    /**
     * Gets the VFS root path of the quota scope.<p>
     *
     * @return the quota scope's VFS root path
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Gets the display state.<p>
     *
     * @return the display state
     */
    public State getState() {

        return m_state;
    }

    /**
     * Gets the storage usage including subordinate quota scopes.<p>
     *
     * @return inclusive usage, or -1 if unavailable
     */
    public long getTotalBytes() {

        return m_totalBytes;
    }

    /**
     * Gets the storage usage including ordinary subsites but excluding subordinate budget scopes.<p>
     *
     * @return exclusive usage, or -1 if unavailable
     */
    public long getUsedBytes() {

        return m_usedBytes;
    }

    /**
     * Checks whether this scope has an individual storage budget.<p>
     *
     * @return whether this scope has an explicit individual budget, including unlimited
     */
    public boolean isIndividualQuota() {

        return m_individualQuota;
    }

    /**
     * Checks whether a compact storage budget warning should be displayed.<p>
     *
     * @return whether a compact warning should be displayed
     */
    public boolean isWarningVisible() {

        return (m_state == State.WARNING) || (m_state == State.EXCEEDED) || (m_state == State.UNAVAILABLE);
    }
}

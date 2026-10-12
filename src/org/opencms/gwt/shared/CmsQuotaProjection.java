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

import org.opencms.gwt.shared.CmsQuotaStatus.State;

/**
 * Calculates the effect of a selected upload without changing the stored usage snapshot.<p>
 */
public class CmsQuotaProjection {

    /** Original snapshot. */
    private final CmsQuotaStatus m_current;

    /** Selected upload size in bytes. */
    private final long m_uploadBytes;

    /**
     * Constructor.<p>
     *
     * @param current the stored usage, or null if unavailable
     * @param uploadBytes the sum of the selected file sizes
     */
    public CmsQuotaProjection(CmsQuotaStatus current, long uploadBytes) {

        m_current = current;
        m_uploadBytes = Math.max(0, uploadBytes);
    }

    /**
     * Gets the expected storage quota status after the upload.<p>
     *
     * @return the expected status after uploading the selection
     */
    public CmsQuotaStatus getProjectedStatus() {

        if (!hasFiniteBudget()) {
            return m_current;
        }
        long used = m_current.getUsedBytes();
        long projected = m_uploadBytes > Long.MAX_VALUE - used ? Long.MAX_VALUE : used + m_uploadBytes;
        long total = m_current.getTotalBytes();
        if (total >= 0) {
            total = m_uploadBytes > Long.MAX_VALUE - total ? Long.MAX_VALUE : total + m_uploadBytes;
        }
        return new CmsQuotaStatus(
            m_current.getRootPath(),
            projected,
            total,
            m_current.getQuotaBytes(),
            CmsQuotaStatus.getUsageState(projected, m_current.getQuotaBytes()),
            m_current.isIndividualQuota());
    }

    /**
     * Checks whether the current usage has reached the storage budget.<p>
     *
     * @return whether the current usage already reaches the budget
     */
    public boolean isAlreadyFull() {

        return hasFiniteBudget() && (m_current.getUsedBytes() >= m_current.getQuotaBytes());
    }

    /**
     * Checks whether the upload would exceed the storage budget.<p>
     *
     * @return whether the upload would exceed the budget; filling it exactly remains allowed
     */
    public boolean isUploadBlocked() {

        return hasFiniteBudget()
            && (isAlreadyFull() || (m_uploadBytes > m_current.getQuotaBytes() - m_current.getUsedBytes()));
    }

    /**
     * Checks whether the usage and a finite storage budget are available.<p>
     *
     * @return whether usage and a finite budget are known
     */
    private boolean hasFiniteBudget() {

        return (m_current != null)
            && (m_current.getQuotaBytes() > 0)
            && (m_current.getUsedBytes() >= 0)
            && ((m_current.getState() == State.NORMAL)
                || (m_current.getState() == State.WARNING)
                || (m_current.getState() == State.EXCEEDED));
    }
}

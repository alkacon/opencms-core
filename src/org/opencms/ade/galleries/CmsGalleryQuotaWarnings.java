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

package org.opencms.ade.galleries;

import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.gwt.shared.CmsQuotaStatus.State;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage budget warnings offered and acknowledged in a single user's HTTP session.<p>
 */
public class CmsGalleryQuotaWarnings implements Serializable {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /** Highest warning offered for each budget scope root path. */
    private final Map<String, Integer> m_offered = new HashMap<>();

    /** Highest warning explicitly acknowledged for each budget scope root path. */
    private final Map<String, Integer> m_acknowledged = new HashMap<>();

    /**
     * Maps warning states to their explicit priority.<p>
     *
     * @param state the state
     * @return zero for non-warning states, one for orange, two for red
     */
    private static int getLevel(State state) {

        return state == State.EXCEEDED ? 2 : state == State.WARNING ? 1 : 0;
    }

    /**
     * Records only an explicitly acknowledged warning which has actually been offered.
     * An older orange acknowledgement must not suppress a new red warning or downgrade a red acknowledgement.<p>
     *
     * @param rootPath the budget scope root path from the displayed snapshot
     * @param state the displayed warning state
     */
    public synchronized void acknowledge(String rootPath, State state) {

        int level = getLevel(state);
        if ((level > 0) && (level <= m_offered.getOrDefault(rootPath, Integer.valueOf(0)).intValue())) {
            m_acknowledged.merge(rootPath, Integer.valueOf(level), Math::max);
        }
    }

    /**
     * Returns an unacknowledged red warning, without treating its display as acknowledgement.
     * Other budget states do not reset earlier acknowledgements.<p>
     *
     * @param status the current cached budget snapshot
     * @return the warning to display, or null
     */
    public synchronized CmsQuotaStatus getWarning(CmsQuotaStatus status) {

        if ((status == null)
            || (status.getState() != State.EXCEEDED)
            || (status.getQuotaBytes() <= 0)
            || (status.getUsedBytes() < 0)) {
            return null;
        }
        int level = getLevel(status.getState());
        String rootPath = status.getRootPath();
        if ((level == 0) || (level <= m_acknowledged.getOrDefault(rootPath, Integer.valueOf(0)).intValue())) {
            return null;
        }
        m_offered.merge(rootPath, Integer.valueOf(level), Math::max);
        return status;
    }
}

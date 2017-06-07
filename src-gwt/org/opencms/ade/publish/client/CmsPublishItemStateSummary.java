/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.publish.client;

/**
 * A bean containing statistics about the states of publish items in the publish dialog.<p>
 *
 * This is used for updating the check box states in the publish dialog.<p>
 *
 */
public class CmsPublishItemStateSummary {

    /** The counts for the normal state. */
    private int m_normalCount;
    /** The count for the publish state. */
    private int m_publishCount;

    /** The count for the remove state. */
    private int m_removeCount;

    /**
     * Adds a new state value to the statistics.<p>
     *
     * @param state the state  to add
     */
    public void addState(CmsPublishItemStatus.State state) {

        switch (state) {
            case normal:
                m_normalCount += 1;
                break;
            case publish:
                m_publishCount += 1;
                break;
            case remove:
            default:
                m_removeCount += 1;
                break;
        }
    }

    /**
     * Gets the count of 'normal' states.<p>
     *
     * @return the count
     */
    public int getNormalCount() {

        return m_normalCount;
    }

    /**
     * Gets the count of 'publish' states.<p>
     *
     * @return the count
     */
    public int getPublishCount() {

        return m_publishCount;
    }

    /**
     * Gets the count of 'remove' states.<p>
     *
     * @return the count
     */
    public int getRemoveCount() {

        return m_removeCount;
    }

}

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

package org.opencms.ui.shared.components;

import com.vaadin.shared.AbstractComponentState;

/**
 * The history state.<p>
 */
public class CmsHistoryState extends AbstractComponentState {

    /** The history back flag. */
    public static final int HISTORY_BACK = 1;

    /** The history forward flag. */
    public static final int HISTORY_FORWARD = 2;

    /** the serial version id. */
    private static final long serialVersionUID = -7299145857595566596L;

    /** The history direction. */
    private int m_historyDirection;

    /**
     * Returns the history direction.<p>
     *
     * @return the history direction
     */
    public int getHistoryDirection() {

        return m_historyDirection;
    }

    /**
     * Returns if the history direction is back.<p>
     *
     * @return <code>true</code> if the history direction is back
     */
    public boolean isHistoryBack() {

        return m_historyDirection == HISTORY_BACK;
    }

    /**
     * Returns if the history direction is forward.<p>
     *
     * @return <code>true</code> if the history direction is forward
     */
    public boolean isHistoryForward() {

        return m_historyDirection == HISTORY_FORWARD;
    }

    /**
     * Sets the history direction.<p>
     * Use -1 for history back, and +1 for history forward.<p>
     *
     * @param direction the history direction
     */
    public void setHistoryDirection(int direction) {

        m_historyDirection = direction;
    }
}

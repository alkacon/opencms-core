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
 * The scroll position CSS shared state.<p>
 */
public class CmsAutoGrowingTextAreaState extends AbstractComponentState {

    /** The serial version id. */
    private static final long serialVersionUID = -4224905041008648688L;

    /** The maximal number of rows. */
    private int m_maxRows;

    /** The minimal number of rows. */
    private int m_minRows;

    /**
     * Returns the maximal number of rows.<p>
     *
     * @return the maximal number of rows
     */
    public int getMaxRows() {

        return m_maxRows;
    }

    /**
     * Returns the minimal number of rows.<p>
     *
     * @return the minimal number of rows
     */
    public int getMinRows() {

        return m_minRows;
    }

    /**
     * Sets the maximal number of rows.<p>
     *
     * @param maxRows the maximal number of rows
     */
    public void setMaxRows(int maxRows) {

        m_maxRows = maxRows;
    }

    /**
     * Sets the minimal number of rows.<p>
     *
     * @param minRows the minimal number of rows
     */
    public void setMinRows(int minRows) {

        m_minRows = minRows;
    }

}

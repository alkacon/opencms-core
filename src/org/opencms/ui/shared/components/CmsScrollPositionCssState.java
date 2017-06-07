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
public class CmsScrollPositionCssState extends AbstractComponentState {

    /** The serial version id. */
    private static final long serialVersionUID = -4224905041008648688L;

    /** The scroll position barrier. */
    private int m_scrollBarrier;

    /** The barrier margin. */
    private int m_barrierMargin;

    /** The style name. */
    private String m_styleName;

    /**
     * Returns the barrier margin.<p>
     *
     * @return the barrier margin
     */
    public int getBarrierMargin() {

        return m_barrierMargin;
    }

    /**
     * Returns the scroll barrier.<p>
     *
     * @return the scroll barrier
     */
    public int getScrollBarrier() {

        return m_scrollBarrier;
    }

    /**
     * Sets the style name.<p>
     *
     * @return the style name
     */
    public String getStyleName() {

        return m_styleName;
    }

    /**
     * Sets the barrier margin.<p>
     *
     * @param barrierMargin the barrier margin
     */
    public void setBarrierMargin(int barrierMargin) {

        m_barrierMargin = barrierMargin;
    }

    /**
     * Sets the scroll barrier.<p>
     *
     * @param scrollBarrier the scroll barrier
     */
    public void setScrollBarrier(int scrollBarrier) {

        m_scrollBarrier = scrollBarrier;
    }

    /**
     * Sets the style name.<p>
     *
     * @param styleName the style name
     */
    public void setStyleName(String styleName) {

        m_styleName = styleName;
    }
}

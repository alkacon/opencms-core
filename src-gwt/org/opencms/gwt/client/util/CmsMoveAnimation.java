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

package org.opencms.gwt.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;

/**
 * Move animation. Moving the given element from it's start to it's end position
 * and executing the given call-back on complete.<p>
 *
 * @since 8.0.0
 */
public class CmsMoveAnimation extends A_CmsAnimation {

    /** The element. */
    private Element m_element;

    /** The end left. */
    private int m_endLeft;

    /** The end top. */
    private int m_endTop;

    /** The start left. */
    private int m_startLeft;

    /** The start top. */
    private int m_startTop;

    /**
     * Constructor. Setting the element to animate, it's start and end position.<p>
     *
     * @param element the element
     * @param startTop the start top
     * @param startLeft the start left
     * @param endTop the end top
     * @param endLeft the end left
     * @param callback the call-back to execute on complete
     */
    public CmsMoveAnimation(Element element, int startTop, int startLeft, int endTop, int endLeft, Command callback) {

        super(callback);
        m_element = element;
        m_startTop = startTop;
        m_startLeft = startLeft;
        m_endTop = endTop;
        m_endLeft = endLeft;
    }

    /**
     * @see com.google.gwt.animation.client.Animation#onUpdate(double)
     */
    @Override
    protected void onUpdate(double progress) {

        progress = progress * progress;
        double newTop = m_startTop + ((m_endTop - m_startTop) * progress);
        double newLeft = m_startLeft + ((m_endLeft - m_startLeft) * progress);
        m_element.getStyle().setTop(newTop, Unit.PX);
        m_element.getStyle().setLeft(newLeft, Unit.PX);
    }
}

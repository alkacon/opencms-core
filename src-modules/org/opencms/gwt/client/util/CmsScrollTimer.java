/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsScrollTimer.java,v $
 * Date   : $Date: 2010/04/16 13:54:15 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Timer to schedule automated scrolling.<p>
 */
public class CmsScrollTimer extends Timer {

    /** Scroll direction enumeration. */
    public enum Direction {
        /** Scroll direction. */
        down,

        /** Scroll direction. */
        left,

        /** Scroll direction. */
        right,

        /** Scroll direction. */
        up
    }

    /** The scroll speed. */
    private int m_scrollSpeed;

    /** The element that should scrolled. */
    private Element m_scrollParent;

    /** The current scroll direction. */
    private Direction m_direction;

    /** Flag indicating if the scroll parent is the body element. */
    private boolean m_isBody;

    /**
     * Constructor.<p>
     * 
     * @param scrollParent the element that should scrolled
     * @param scrollSpeed the scroll speed
     * @param direction the scroll direction
     */
    public CmsScrollTimer(Element scrollParent, int scrollSpeed, Direction direction) {

        m_scrollParent = scrollParent;
        m_scrollSpeed = scrollSpeed;
        m_isBody = m_scrollParent.getTagName().equalsIgnoreCase(CmsDomUtil.Tag.body.name());
        m_direction = direction;
    }

    /**
     * @see com.google.gwt.user.client.Timer#run()
     */
    @Override
    public void run() {

        int top, left;
        if (m_isBody) {
            top = Window.getScrollTop();
            left = Window.getScrollLeft();
        } else {
            top = m_scrollParent.getScrollTop();
            left = m_scrollParent.getScrollLeft();
        }
        switch (m_direction) {
            case down:
                top += m_scrollSpeed;
                break;
            case up:
                top -= m_scrollSpeed;
                break;
            case left:
                left += m_scrollSpeed;
                break;
            case right:
                left -= m_scrollSpeed;
                break;
            default:
                break;

        }

        if (m_isBody) {
            Window.scrollTo(left, top);
        } else {
            m_scrollParent.setScrollLeft(left);
            m_scrollParent.setScrollTop(top);
        }

    }

    /**
     * Convenience method to get the appropriate scroll direction.<p>
     * 
     * @param event the mouse event indicating the cursor position
     * @param offset the scroll parent border offset, if the cursor is within the border offset, scrolling should be triggered
     * 
     * @return the scroll direction
     */
    public static Direction getScrollDirection(MouseEvent<?> event, int offset) {

        Element body = RootPanel.getBodyElement();
        int windowHeight = Window.getClientHeight();
        int bodyHeight = body.getClientHeight();
        if (windowHeight < bodyHeight) {
            if ((windowHeight - event.getClientY() < offset) && (Window.getScrollTop() < bodyHeight - windowHeight)) {
                return Direction.down;
            }
            if ((event.getClientY() < offset) && (Window.getScrollTop() > 0)) {
                return Direction.up;
            }
        }

        int windowWidth = Window.getClientWidth();
        int bodyWidth = body.getClientWidth();
        if (windowWidth < bodyWidth) {
            if ((windowWidth - event.getClientX() < offset) && (Window.getScrollLeft() < bodyWidth - windowWidth)) {
                return Direction.right;
            }
            if ((event.getClientX() < offset) && (Window.getScrollLeft() > 0)) {
                return Direction.left;
            }
        }

        return null;
    }

}

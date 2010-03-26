/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsPositionBean.java,v $
 * Date   : $Date: 2010/03/26 09:42:20 $
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

import com.google.gwt.user.client.ui.UIObject;

/**
 * Bean holding the position data of a HTML DOM element.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsPositionBean {

    /** Element height. */
    private int m_height;

    /** Position left. */
    private int m_left;

    /** Position top. */
    private int m_top;

    /** Element width. */
    private int m_width;

    /**
     * Constructor.<p>
     */
    public CmsPositionBean() {

        // default constructor
    }

    /**
     * Returns the height.<p>
     *
     * @return the height
     */
    public int getHeight() {

        return m_height;
    }

    /**
     * Returns the left.<p>
     *
     * @return the left
     */
    public int getLeft() {

        return m_left;
    }

    /**
     * Returns the top.<p>
     *
     * @return the top
     */
    public int getTop() {

        return m_top;
    }

    /**
     * Returns the width.<p>
     *
     * @return the width
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * Sets the height.<p>
     *
     * @param height the height to set
     */
    public void setHeight(int height) {

        m_height = height;
    }

    /**
     * Sets the left.<p>
     *
     * @param left the left to set
     */
    public void setLeft(int left) {

        m_left = left;
    }

    /**
     * Sets the top.<p>
     *
     * @param top the top to set
     */
    public void setTop(int top) {

        m_top = top;
    }

    /**
     * Sets the width.<p>
     *
     * @param width the width to set
     */
    public void setWidth(int width) {

        m_width = width;
    }

    /**
     * Returns if given position is inside the position beans coordinates.<p>
     * 
     * @param absLeft the absolute left position
     * @param absTop the absolute top position
     * 
     * @return true if the given position if within the beans coordinates
     */
    public boolean isOverElement(int absLeft, int absTop) {

        if ((absTop > m_top) && (absTop < (m_top + m_height)) && (absLeft > m_left) && (absLeft < (m_left + m_width))) {
            return true;
        }
        /*     */
        return false;
    }

    /** 
     * Returns if given absolute top is above the vertical middle of the position beans coordinates.<p>
     * 
     * @param absTop the absolute top position
     * @return true if given absolute top is above the vertical middle
     */
    public boolean isOverTopHalf(int absTop) {

        if (absTop < (m_top + (m_height / 2))) {
            return true;
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "top: " + m_top + "   left: " + m_left + "   height: " + m_height + "   width: " + m_width;
    }

    /**
     * Collects the position information of the given UI object and returns a position info bean.<p> 
     * 
     * @param uiObject the object to read the position data from
     * 
     * @return the position data
     */
    public static CmsPositionBean generatePositionInfo(UIObject uiObject) {

        CmsPositionBean result = new CmsPositionBean();
        result.setHeight(uiObject.getOffsetHeight());
        result.setWidth(uiObject.getOffsetWidth());
        result.setTop(uiObject.getAbsoluteTop());
        result.setLeft(uiObject.getAbsoluteLeft());
        return result;
    }

}

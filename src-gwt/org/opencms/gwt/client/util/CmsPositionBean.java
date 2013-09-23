/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.client.util.CmsDomUtil.Style;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.UIObject;

/**
 * Bean holding the position data of a HTML DOM element.<p>
 * 
 * @since 8.0.0
 */
public class CmsPositionBean {

    /** Position area. */
    public static enum Area {

        /** Bottom border. */
        BORDER_BOTTOM,

        /** Left border. */
        BORDER_LEFT,

        /** Right border. */
        BORDER_RIGHT,

        /** Top border. */
        BORDER_TOP,

        /** The center. */
        CENTER,

        /** Bottom left corner. */
        CORNER_BOTTOM_LEFT,

        /** Bottom right corner. */
        CORNER_BOTTOM_RIGHT,

        /** Top left corner. */
        CORNER_TOP_LEFT,

        /** Top right corner. */
        CORNER_TOP_RIGHT
    }

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
     * Copy constructor. Generating a copy of the given model.<p>
     * 
     * @param model the model to copy
     */
    public CmsPositionBean(CmsPositionBean model) {

        m_height = model.getHeight();
        m_left = model.getLeft();
        m_top = model.getTop();
        m_width = model.getWidth();
    }

    /**
     * Collects the position information of the given UI object and returns a position info bean.<p> 
     * 
     * @param element the object to read the position data from
     * 
     * @return the position data
     */
    public static CmsPositionBean generatePositionInfo(Element element) {

        CmsPositionBean result = new CmsPositionBean();
        result.setHeight(element.getOffsetHeight());
        result.setWidth(element.getOffsetWidth());
        result.setTop(element.getAbsoluteTop());
        result.setLeft(element.getAbsoluteLeft());
        return result;
    }

    /**
     * Collects the position information of the given UI object and returns a position info bean.<p> 
     * 
     * @param uiObject the object to read the position data from
     * 
     * @return the position data
     */
    public static CmsPositionBean generatePositionInfo(UIObject uiObject) {

        return generatePositionInfo(uiObject.getElement());
    }

    /**
     * Returns a position info representing the dimensions of all visible child elements of the given panel (excluding elements with position:absolute).
     * If the panel has no visible child elements, it's outer dimensions are returned.<p>
     * 
     * @param panel the panel
     * 
     * @return the position info
     */
    public static CmsPositionBean getInnerDimensions(Element panel) {

        return getInnerDimensions(panel, 2, false);
    }

    /**
     * Returns a position info representing the dimensions of all visible child elements of the given panel (excluding elements with position:absolute).
     * If the panel has no visible child elements, it's outer dimensions are returned.<p>
     * 
     * @param panel the panel
     * @param levels the levels to traverse down the DOM tree
     * @param includeSelf <code>true</code> to include the outer dimensions of the given panel
     * 
     * @return the position info
     */
    private static CmsPositionBean getInnerDimensions(Element panel, int levels, boolean includeSelf) {

        boolean first = true;
        int top = 0;
        int left = 0;
        int bottom = 0;
        int right = 0;
        if (includeSelf) {
            top = panel.getAbsoluteTop();
            left = panel.getAbsoluteLeft();
            bottom = top + panel.getOffsetHeight();
            right = left + panel.getOffsetWidth();
        }
        Element child = panel.getFirstChildElement();
        while (child != null) {
            String positioning = CmsDomUtil.getCurrentStyle(child, Style.position);
            if (!Display.NONE.getCssName().equals(CmsDomUtil.getCurrentStyle(child, Style.display))
                && !(positioning.equalsIgnoreCase(Position.ABSOLUTE.getCssName()) || positioning.equalsIgnoreCase(Position.FIXED.getCssName()))) {
                CmsPositionBean childDimensions = levels > 0
                ? getInnerDimensions(child, levels - 1, true)
                : generatePositionInfo(panel);
                if (first) {
                    first = false;
                    top = childDimensions.getTop();
                    left = childDimensions.getLeft();
                    bottom = top + childDimensions.getHeight();
                    right = left + childDimensions.getWidth();
                } else {
                    int wTop = childDimensions.getTop();
                    top = top < wTop ? top : wTop;
                    int wLeft = childDimensions.getLeft();
                    left = left < wLeft ? left : wLeft;
                    int wBottom = wTop + childDimensions.getHeight();
                    bottom = bottom > wBottom ? bottom : wBottom;
                    int wRight = wLeft + childDimensions.getWidth();
                    right = right > wRight ? right : wRight;
                }
            }
            child = child.getNextSiblingElement();
        }
        if (!first) {
            CmsPositionBean result = new CmsPositionBean();
            result.setHeight(bottom - top);
            result.setWidth(right - left);
            result.setTop(top);
            result.setLeft(left);
            return result;
        } else {
            return generatePositionInfo(panel);
        }
    }

    /**
     * Returns over which area of this the given position is. Will return <code>null</code> if the provided position is not within this position.<p>
     *  
     * @param absLeft the left position
     * @param absTop the right position
     * @param offset the border offset
     * 
     * @return the area
     */
    public Area getArea(int absLeft, int absTop, int offset) {

        if (isOverElement(absLeft, absTop)) {
            if (absLeft < (m_left + 10)) {
                // left border
                if (absTop < (m_top + offset)) {
                    // top left corner
                    return Area.CORNER_TOP_LEFT;
                } else if (absTop > ((m_top + m_height) - offset)) {
                    // bottom left corner
                    return Area.CORNER_BOTTOM_LEFT;
                }
                return Area.BORDER_LEFT;
            }
            if (absLeft > ((m_left + m_width) - offset)) {
                // right border
                if (absTop < (m_top + offset)) {
                    // top right corner
                    return Area.CORNER_TOP_RIGHT;
                    // fixing opposite corner
                } else if (absTop > ((m_top + m_height) - offset)) {
                    // bottom right corner
                    return Area.CORNER_BOTTOM_RIGHT;
                    // fixing opposite corner
                }
                return Area.BORDER_RIGHT;
            }
            if (absTop < (m_top + offset)) {
                // border top
                return Area.BORDER_TOP;
            } else if (absTop > ((m_top + m_height) - offset)) {
                // border bottom
                return Area.BORDER_BOTTOM;
            }
            return Area.CENTER;
        }
        return null;
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "top: " + m_top + "   left: " + m_left + "   height: " + m_height + "   width: " + m_width;
    }

}

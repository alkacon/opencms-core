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

import org.opencms.gwt.client.util.CmsDomUtil.Style;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Window;
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

    /** The directions. */
    static enum Direction {
        /** Bottom. */
        bottom,

        /** Left. */
        left,

        /** Right. */

        right,

        /** Top. */
        top
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
     * Manipulates the position infos to ensure a minimum margin between the rectangles.<p>
     *
     * @param posA the first position to check
     * @param posB the second position to check
     * @param margin the required margin
     */
    public static void avoidCollision(CmsPositionBean posA, CmsPositionBean posB, int margin) {

        Direction dir = null;
        int diff = 0;
        int diffTemp = (posB.getLeft() + posB.getWidth()) - posA.getLeft();
        if (diffTemp > -margin) {
            dir = Direction.left;
            diff = diffTemp;
        }

        diffTemp = (posA.getLeft() + posA.getWidth()) - posB.getLeft();
        if ((diffTemp > -margin) && (diffTemp < diff)) {
            dir = Direction.right;
            diff = diffTemp;
        }
        diffTemp = (posB.getTop() + posB.getHeight()) - posA.getTop();
        if ((diffTemp > -margin) && (diffTemp < diff)) {
            dir = Direction.top;
            diff = diffTemp;
        }
        diffTemp = (posA.getTop() + posA.getHeight()) - posB.getTop();
        if ((diffTemp > -margin) && (diffTemp < diff)) {
            dir = Direction.bottom;
            diff = diffTemp;
        }

        diff = (int)Math.ceil((1.0 * (diff + margin)) / 2);
        if (dir != null) {
            switch (dir) {
                case left:
                    // move the left border of a
                    posA.setLeft(posA.getLeft() + diff);
                    posA.setWidth(posA.getWidth() - diff);

                    // move the right border of b
                    posB.setWidth(posB.getWidth() - diff);
                    break;

                case right:
                    // move the left border of b
                    posB.setLeft(posB.getLeft() + diff);
                    posB.setWidth(posB.getWidth() - diff);

                    // move the right border of a
                    posA.setWidth(posA.getWidth() - diff);
                    break;

                case top:
                    posA.setTop(posA.getTop() + diff);
                    posA.setHeight(posA.getHeight() - diff);

                    posB.setHeight(posB.getHeight() - diff);
                    break;
                case bottom:
                    posB.setTop(posB.getTop() + diff);
                    posB.setHeight(posB.getHeight() - diff);

                    posA.setHeight(posA.getHeight() - diff);
                    break;
                default:
                    // nothing to do
            }
        }
    }

    /**
     * Checks whether the two position rectangles collide.<p>
     *
     * @param posA the first position to check
     * @param posB the second position to check
     * @param margin the required margin
     *
     * @return <code>true</code> if the two position rectangles collide
     */
    public static boolean checkCollision(CmsPositionBean posA, CmsPositionBean posB, int margin) {

        // check for non collision is easier
        if ((posA.getLeft() - margin) >= (posB.getLeft() + posB.getWidth())) {
            // posA is right of posB
            return false;
        }
        if ((posA.getLeft() + posA.getWidth()) <= (posB.getLeft() - margin)) {
            // posA is left of posB
            return false;
        }
        if ((posA.getTop() - margin) >= (posB.getTop() + posB.getHeight())) {
            // posA is bellow posB
            return false;
        }
        if ((posA.getTop() + posA.getHeight()) <= (posB.getTop() - margin)) {
            // posA is above posB
            return false;
        }

        // in any other case the position rectangles collide
        return true;
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
     * Returns the bounding rectangle dimensions of the element including all floated elements.<p>
     *
     * @param panel the panel
     *
     * @return the position info
     */
    public static CmsPositionBean getBoundingClientRect(Element panel) {

        return getBoundingClientRect(panel, true);

    }

    /**
     * Returns the bounding rectangle dimensions of the element including all floated elements.<p>
     *
     * @param panel the panel
     * @param addScroll if true, the result will contain the coordinates in the document's coordinate system, not the viewport coordinate system
     *
     * @return the position info
     */
    public static CmsPositionBean getBoundingClientRect(Element panel, boolean addScroll) {

        CmsPositionBean result = new CmsPositionBean();
        getBoundingClientRect(
            panel,
            result,
            addScroll ? Window.getScrollLeft() : 0,
            addScroll ? Window.getScrollTop() : 0);
        return result;
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

        boolean first = true;
        int top = 0;
        int left = 0;
        int bottom = 0;
        int right = 0;
        Element child = panel.getFirstChildElement();
        while (child != null) {
            String tagName = child.getTagName();
            if (tagName.equalsIgnoreCase("br")
                || tagName.equalsIgnoreCase("tr")
                || tagName.equalsIgnoreCase("thead")
                || tagName.equalsIgnoreCase("tfoot")
                || tagName.equalsIgnoreCase("script")
                || tagName.equalsIgnoreCase("style")) {
                // ignore tags with no relevant position info
                child = child.getNextSiblingElement();
                continue;
            }
            String positioning = CmsDomUtil.getCurrentStyle(child, Style.position);
            if (!Display.NONE.getCssName().equals(CmsDomUtil.getCurrentStyle(child, Style.display))
                && !(positioning.equalsIgnoreCase(Position.ABSOLUTE.getCssName())
                    || positioning.equalsIgnoreCase(Position.FIXED.getCssName()))) {
                CmsPositionBean childDimensions = getBoundingClientRect(child);
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
            return getBoundingClientRect(panel);
        }
    }

    /**
     * Checks whether a value is in a given interval (including the end points).<p>
     *
     * @param min the minimum of the interval
     * @param max the maximum of the interval
     * @param value the value to check
     *
     * @return true if the value is in the given interval
     */
    public static boolean isInRangeInclusive(int min, int max, int value) {

        return (min <= value) && (value <= max);
    }

    /**
     * Uses the getBoundingClientRect method to evaluate the element dimensions.<p>
     *
     * @param element the element
     * @param pos the position bean
     * @param scrollLeft the window scroll position left
     * @param scrollTop the window scroll position top
     */
    private static native void getBoundingClientRect(
        Element element,
        CmsPositionBean pos,
        int scrollLeft,
        int scrollTop)/*-{

                      var rect = element.getBoundingClientRect();
                      pos.@org.opencms.gwt.client.util.CmsPositionBean::m_top=Math.round(rect.top+scrollTop);
                      pos.@org.opencms.gwt.client.util.CmsPositionBean::m_left=Math.round(rect.left+scrollLeft);
                      pos.@org.opencms.gwt.client.util.CmsPositionBean::m_height=Math.round(rect.height);
                      pos.@org.opencms.gwt.client.util.CmsPositionBean::m_width=Math.round(rect.width);
                      }-*/;

    /**
     * Checks if the rectangle defined by this bean contains the given point.<p>
     *
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     *
     * @return true if this object contains the given point
     */
    public boolean containsPoint(int x, int y) {

        return isInRangeInclusive(getLeft(), (getLeft() + getWidth()) - 1, x)
            && isInRangeInclusive(getTop(), (getTop() + getHeight()) - 1, y);
    }

    /**
     * Increases the dimensions to completely surround the child.<p>
     *
     * @param child the child position info
     * @param padding the padding to apply
     */
    public void ensureSurrounds(CmsPositionBean child, int padding) {

        // increase the size of the outer rectangle
        if ((getLeft() + padding) > child.getLeft()) {
            int diff = getLeft() - child.getLeft();
            // ensure padding
            diff += padding;
            setLeft(getLeft() - diff);
            setWidth(getWidth() + diff);
        }
        if ((getTop() + padding) > child.getTop()) {
            int diff = getTop() - child.getTop();
            diff += padding;
            setTop(getTop() - diff);
            setHeight(getHeight() + diff);
        }
        if ((getLeft() + getWidth()) < (child.getLeft() + child.getWidth() + padding)) {
            int diff = (child.getLeft() + child.getWidth()) - (getLeft() + getWidth());
            diff += padding;
            setWidth(getWidth() + diff);
        }
        if ((getTop() + getHeight()) < (child.getTop() + child.getHeight() + padding)) {
            int diff = (child.getTop() + child.getHeight()) - (getTop() + getHeight());
            diff += padding;
            setHeight(getHeight() + diff);
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
     * Checks whether the given position is completely surrounded by this position.<p>
     *
     * @param child the child position
     * @param padding the padding to use
     *
     * @return <code>true</code> if the child position is completely surrounded
     */
    public boolean isInside(CmsPositionBean child, int padding) {

        return ((getLeft() + padding) < child.getLeft()) // checking left border
            && ((getTop() + padding) < child.getTop()) // checking top border
            && (((getLeft() + getWidth()) - padding) > (child.getLeft() + child.getWidth())) // checking right border
            && (((getTop() + getHeight()) - padding) > (child.getTop() + child.getHeight())); // checking bottom border
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

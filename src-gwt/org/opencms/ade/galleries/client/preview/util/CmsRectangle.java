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

package org.opencms.ade.galleries.client.preview.util;

import org.opencms.ade.galleries.shared.CmsPoint;

/**
 * Axis-aligned rectangle in 2D space.<p>
 */
public class CmsRectangle {

    /** x coordinate of left side of the rectangle. */
    private double m_left;

    /** y coordinate of the top side of the rectangle. */
    private double m_top;

    /** The width. */
    private double m_width;

    /** The height. */
    private double m_height;

    /**
     * Hidden default constructor.<p>
     */
    protected CmsRectangle() {
        // do nothing
    }

    /**
     * Creates a new rectangle given its left, top coordinates and its width and height.<p>
     *
     * @param left the left side
     * @param top the top side
     * @param width the width
     * @param height the height
     *
     * @return the new rectangel
     */
    public static CmsRectangle fromLeftTopWidthHeight(double left, double top, double width, double height) {

        CmsRectangle result = new CmsRectangle();
        result.m_left = left;
        result.m_top = top;
        result.m_width = width;
        result.m_height = height;
        return result;
    }

    /**
     * Creates a new rectangle from its top left and bottom right corner points.<p>
     *
     * @param topLeft the top left corner
     * @param bottomRight the bottom right corner
     * @return the new rectangle
     */
    public static CmsRectangle fromPoints(CmsPoint topLeft, CmsPoint bottomRight) {

        CmsRectangle result = new CmsRectangle();
        result.m_left = topLeft.getX();
        result.m_top = topLeft.getY();
        result.m_width = bottomRight.getX() - topLeft.getX();
        result.m_height = bottomRight.getY() - topLeft.getY();
        return result;
    }

    /**
     * Helper method to constrain a number inside a given interval.<p>
     *
     * If the number is inside the given range, it will be returned unchanged, otherwise either the maximum
     * or minimum of the range is returned depending on on which side the value lies.
     *
     * @param min the minimum value
     * @param size the difference between maximum and minimum value
     * @param h the number to constrain
     *
     * @return the constrained value
     */
    private static double constrainNum(double min, double size, double h) {

        if (h < min) {
            return min;
        }
        if (h >= (min + size)) {
            return (min + size) - 1;
        }
        return h;
    }

    /**
     * Constrains a point to this rectangle.<p>
     *
     * If any of the coordinates of the point lie in the projection of this rectangle on the corresponding axis, that coordinate
     * in the result will be unchanged, otherwise it will be either the maximum or the minimum depending on on which side the original
     * coordinate is located.
     *
     * @param point the point to constrain
     *
     * @return the constrained point
     */
    public CmsPoint constrain(CmsPoint point) {

        return new CmsPoint(constrainNum(m_left, m_width, point.getX()), constrainNum(m_top, m_height, point.getY()));
    }

    /**
     * Checks if this rectangle contains a given point.<p>
     *
     * @param point the point to check
     *
     * @return true if the point is contained in the rectangle
     */
    public boolean contains(CmsPoint point) {

        return (point.getX() >= m_left)
            && (point.getX() < (m_left + m_width))
            && (point.getY() >= m_top)
            && (point.getY() < (m_top + m_height));
    }

    /**
     * Gets the bottom right corner.<p>
     *
     * @return the bottom right corner
     */
    public CmsPoint getBottomRight() {

        return new CmsPoint(m_left + m_width, m_top + m_height);
    }

    /**
     * Gets the top left corner.<p>
     *
     * @return the top left corner
     */
    public CmsPoint getTopLeft() {

        return new CmsPoint(m_left, m_top);
    }

}

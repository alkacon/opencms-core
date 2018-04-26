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
 * A coordinate system transform for translating between coordinates relative to a rectangle and the coordinates relative to a second
 * rectangle with the first rectangle fit into the second one, either by just centering if possible or by centering and scaling it.
 */
public class CmsBoxFit implements I_CmsTransform {

    /**
     * Scale mode.<p>
     */
    public enum Mode {
        /** Always scale. */
        scaleAlways,

        /** Only scale if the rectangle in the target coordinate system does not fit in the source coordinate system. */
        scaleOnlyIfNecessary;
    }

    /** Name for debugging. */
    private String m_name;

    /** The horizontal offset. */
    private double m_offsetLeft;

    /** The vertical offset. */
    private double m_offsetTop;

    /** The scaling factor. */
    private double m_scale;

    /**
     * Creates a new instance.<p>
     *
     * @param mode the scale mode
     * @param width the width of the first rectangle
     * @param height the height of the first rectangle
     * @param naturalWidth the width of the second rectangle
     * @param naturalHeight the height of the second rectangle
     */
    public CmsBoxFit(Mode mode, double width, double height, double naturalWidth, double naturalHeight) {

        m_name = "[CmsBoxFit " + mode + " " + width + " " + height + " " + naturalWidth + " " + naturalHeight + "]";
        if ((mode == Mode.scaleOnlyIfNecessary) && (naturalWidth <= width) && (naturalHeight <= height)) {
            // just fit the rectangle in the middle, no scaling
            m_offsetLeft = (width - naturalWidth) / 2.0;
            m_offsetTop = (height - naturalHeight) / 2.0;
            m_scale = 1;
        } else {
            // Use minimum so that the second rectangle, when scaled, completely fits in the first one
            m_scale = Math.min(width / naturalWidth, height / naturalHeight);
            m_offsetLeft = (width - (naturalWidth * m_scale)) / 2.0;
            m_offsetTop = (height - (naturalHeight * m_scale)) / 2.0;
        }

    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_name;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.util.I_CmsTransform#transformBack(org.opencms.ade.galleries.shared.CmsPoint)
     */
    public CmsPoint transformBack(CmsPoint point) {

        double rx = (m_scale * point.getX()) + m_offsetLeft;
        double ry = (m_scale * point.getY()) + m_offsetTop;
        return new CmsPoint(rx, ry);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.util.I_CmsTransform#transformForward(org.opencms.ade.galleries.shared.CmsPoint)
     */
    public CmsPoint transformForward(CmsPoint point) {

        double rx = (point.getX() - m_offsetLeft) / m_scale;
        double ry = (point.getY() - m_offsetTop) / m_scale;
        return new CmsPoint(rx, ry);

    }

}

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
 * Simple translation by x/y offset.<p>
 */
public class CmsTranslate implements I_CmsTransform {

    /** The horizontal offset. */
    private double m_cx;

    /** The vertical offset. */
    private double m_cy;

    /**
     * Creates a new instance.<p>
     *
     * @param cx the horizontal offset
     * @param cy the vertical offset
     */
    public CmsTranslate(double cx, double cy) {

        m_cx = cx;
        m_cy = cy;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "[CmsTranslate " + m_cx + " " + m_cy + "]";

    }

    /**
     * @see org.opencms.ade.galleries.client.preview.util.I_CmsTransform#transformBack(org.opencms.ade.galleries.shared.CmsPoint)
     */
    public CmsPoint transformBack(CmsPoint point) {

        return new CmsPoint(point.getX() - m_cx, point.getY() - m_cy);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.util.I_CmsTransform#transformForward(org.opencms.ade.galleries.shared.CmsPoint)
     */
    public CmsPoint transformForward(CmsPoint point) {

        return new CmsPoint(point.getX() + m_cx, point.getY() + m_cy);
    }

}

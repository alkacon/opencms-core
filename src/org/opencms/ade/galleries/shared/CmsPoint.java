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

package org.opencms.ade.galleries.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A point in 2D space.<p>
 */
public class CmsPoint implements IsSerializable {

    /** x coordinate. */
    private double m_x;

    /** y coordinate. */
    private double m_y;

    /**
     * Creates a new instance.<p>
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public CmsPoint(double x, double y) {

        m_x = x;
        m_y = y;
    }

    /**
     * Empty default constructor for serialization.<p>
     */
    protected CmsPoint() {
        // empty default constructor for serialization
    }

    /**
     * Gets the x coordinate.<p>
     *
     * @return the x coordinate
     */
    public double getX() {

        return m_x;
    }

    /**
     * Gets the y coordinate.<p>
     *
     * @return the y coordinate
     */
    public double getY() {

        return m_y;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "(" + m_x + "," + m_y + ")";
    }

}

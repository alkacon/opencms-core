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

package org.opencms.jsp.search.state;

import org.opencms.util.CmsStringUtil;

/**
 * Interface for the Geo filter state.
 */
public interface I_CmsSearchStateGeoFilter {

    /**
     * Returns the coordinates.
     * @return the coordinates
     */
    String getCoordinates();

    /**
     * Returns the radius.
     * @return the radius
     */
    String getRadius();

    /**
     * Returns the units.
     * @return the units
     */
    String getUnits();

    /**
     * Returns whether we are in a Geo filter state or not.
     * @return whether we are in a Geo filter state or not
     */
    default boolean hasGeoFilter() {

        return !CmsStringUtil.isEmptyOrWhitespaceOnly(getCoordinates())
            && !CmsStringUtil.isEmptyOrWhitespaceOnly(getRadius());
    }

    /**
     * Sets the coordinates.
     * @param coordinates the coordinates
     */
    void setCoordinates(String coordinates);

    /**
     * Sets the radius.
     * @param radius the radius
     */
    void setRadius(String radius);

    /**
     * Sets the units.
     * @param the units
     */
    void setUnits(String units);
}

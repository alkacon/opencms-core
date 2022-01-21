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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter;

/**
 * Class keeping the state of the Geo filter.
 */
public class CmsSearchStateGeoFilter implements I_CmsSearchStateGeoFilter {

    /** The coordinates. */
    private String m_coordinates;

    /** The search radius. */
    private String m_radius;

    /** The units. */
    private String m_units;

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateGeoFilter#getCoordinates()
     */
    @Override
    public String getCoordinates() {

        return m_coordinates;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateGeoFilter#getRadius()
     */
    @Override
    public String getRadius() {

        return m_radius;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateGeoFilter#getUnits()
     */
    @Override
    public String getUnits() {

        return m_units != null ? m_units : I_CmsSearchConfigurationGeoFilter.DEFAULT_UNITS;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateGeoFilter#setCoordinates(java.lang.String)
     */
    @Override
    public void setCoordinates(String coordinates) {

        m_coordinates = coordinates;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateGeoFilter#setRadius(java.lang.String)
     */
    @Override
    public void setRadius(String radius) {

        m_radius = radius;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateGeoFilter#setUnits(java.lang.String)
     */
    @Override
    public void setUnits(String units) {

        m_units = units;
    }
}

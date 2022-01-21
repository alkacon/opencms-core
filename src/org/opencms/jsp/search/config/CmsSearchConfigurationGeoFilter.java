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

package org.opencms.jsp.search.config;

import org.opencms.util.CmsGeoUtil;

/**
 * Search configuration for the Geo filter.
 */
public class CmsSearchConfigurationGeoFilter implements I_CmsSearchConfigurationGeoFilter {

    /** The coordinates. */
    private String m_coordinates;

    /** The name of the coordinates parameter. */
    private String m_coordinatesParam;

    /** The name of the field that stores the coordinates. */
    private String m_fieldName;

    /** The radius. */
    private String m_radius;

    /** The name of the radius parameter. */
    private String m_radiusParam;

    /** The units of the radius. */
    private String m_units;

    /** The name of the units parameter. */
    private String m_unitsParam;

    /**
     * Constructor for the Geo filter configuration.
     */
    public CmsSearchConfigurationGeoFilter() {}

    /**
     * Constructor for the Geo filter configuration.
     * @param coordinates the coordinates
     * @param coordinatesParam the name of the coordinates parameter used by the search form
     * @param fieldName the Solr field where coordinates are stored
     * @param radius the radius
     * @param radiusParam the name of the radius parameter used by the search form
     * @param units the units of the radius
     * @param unitsParam the name of the units parameter used by the search form
     */
    public CmsSearchConfigurationGeoFilter(
        String coordinates,
        String coordinatesParam,
        String fieldName,
        String radius,
        String radiusParam,
        String units,
        String unitsParam) {

        m_coordinates = CmsGeoUtil.validateCoordinates(coordinates) ? coordinates : null;
        m_coordinatesParam = coordinatesParam;
        m_fieldName = fieldName;
        m_radius = CmsGeoUtil.validateRadius(radius) ? radius : null;
        m_radiusParam = radiusParam;
        m_units = CmsGeoUtil.validateUnits(units) ? units : null;
        m_unitsParam = unitsParam;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter#getCoordinates()
     */
    @Override
    public String getCoordinates() {

        return m_coordinates;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter#getCoordinatesParam()
     */
    @Override
    public String getCoordinatesParam() {

        return m_coordinatesParam != null ? m_coordinatesParam : DEFAULT_COORDINATES_PARAM;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter#getFieldName()
     */
    @Override
    public String getFieldName() {

        return m_fieldName != null ? m_fieldName : DEFAULT_FIELD_NAME;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter#getRadius()
     */
    @Override
    public String getRadius() {

        return m_radius;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter#getRadiusParam()
     */
    @Override
    public String getRadiusParam() {

        return m_radiusParam != null ? m_radiusParam : DEFAULT_RADIUS_PARAM;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter#getUnits()
     */
    @Override
    public String getUnits() {

        return m_units != null ? m_units : DEFAULT_UNITS;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter#getUnitsParam()
     */
    @Override
    public String getUnitsParam() {

        return m_unitsParam != null ? m_unitsParam : DEFAULT_UNITS_PARAM;
    }

}

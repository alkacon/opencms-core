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

import org.opencms.util.CmsStringUtil;

/**
 * The interface a Geo filter configuration must implement.
 */
public interface I_CmsSearchConfigurationGeoFilter {

    /** The default name of the coordinates parameter. */
    String DEFAULT_COORDINATES_PARAM = "coordinates";

    /** The default name of the Solr field where coordinates are stored. */
    String DEFAULT_FIELD_NAME = "geocoords_loc";

    /** The default name of the radius parameter. */
    String DEFAULT_RADIUS_PARAM = "radius";

    /** The default units of the search radius. */
    String DEFAULT_UNITS = "km";

    /** The default name of the units parameter. */
    String DEFAULT_UNITS_PARAM = "units";

    /**
     * Returns the coordinates.
     * @return the coordinates
     */
    String getCoordinates();

    /**
     * Returns the name of the coordinates parameter.
     * @return the name of the coordinates parameter
     */
    String getCoordinatesParam();

    /**
     * Returns the Solr field name storing the coordinates.
     * @return the Solr field name storing the coordinates
     */
    String getFieldName();

    /**
     * Returns the radius.
     * @return the radius
     */
    String getRadius();

    /**
     * Returns the name of the radius parameter.
     * @return the name of the radius parameter
     */
    String getRadiusParam();

    /**
     * Returns the units the search radius uses, either kilometers (km) or miles (mi).
     * @return the search radius units
     */
    String getUnits();

    /**
     * Returns the name of the units parameter.
     * @return the name of the units parameter
     */
    String getUnitsParam();

    /**
     * Returns whether this Geo filter configuration has a valid Geo filter set.
     * @return whether this configuration has a valid Geo filter set or not
     */
    default boolean hasGeoFilter() {

        return !CmsStringUtil.isEmptyOrWhitespaceOnly(getCoordinates())
            && !CmsStringUtil.isEmptyOrWhitespaceOnly(getRadius());
    }
}

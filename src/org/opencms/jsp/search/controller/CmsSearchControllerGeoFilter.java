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

package org.opencms.jsp.search.controller;

import org.opencms.file.CmsObject;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter;
import org.opencms.jsp.search.state.CmsSearchStateGeoFilter;
import org.opencms.jsp.search.state.I_CmsSearchStateGeoFilter;
import org.opencms.main.CmsLog;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.util.CmsGeoUtil;

import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Search controller for the Geo filter.
 */
public class CmsSearchControllerGeoFilter implements I_CmsSearchControllerGeoFilter {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchControllerGeoFilter.class);

    /** Configuration of the Geo filter. */
    private final I_CmsSearchConfigurationGeoFilter m_config;

    /** State of the Geo filter. */
    private final I_CmsSearchStateGeoFilter m_state;

    /**
     * Constructor taking the managed configuration.
     * @param config the configuration managed by the controller
     */
    public CmsSearchControllerGeoFilter(final I_CmsSearchConfigurationGeoFilter config) {

        m_config = config;
        m_state = new CmsSearchStateGeoFilter();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    @Override
    public void addParametersForCurrentState(Map<String, String[]> parameters) {

        if (m_state.hasGeoFilter()) {
            parameters.put(m_config.getCoordinatesParam(), new String[] {m_state.getCoordinates()});
            parameters.put(m_config.getRadiusParam(), new String[] {m_state.getRadius()});
            parameters.put(m_config.getUnitsParam(), new String[] {m_state.getUnits()});
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(org.opencms.search.solr.CmsSolrQuery, org.opencms.file.CmsObject)
     */
    @Override
    public void addQueryParts(CmsSolrQuery query, CmsObject cms) {

        String fieldName = m_config.getFieldName();
        if (m_state.hasGeoFilter()) {
            String coordinates = m_state.getCoordinates();
            String radius = m_state.getRadius();
            String units = m_state.getUnits();
            query.setGeoFilterQuery(fieldName, coordinates, radius, units);
        }
        if (m_config.hasGeoFilter()) {
            String coordinates = m_config.getCoordinates();
            String radius = m_config.getRadius();
            String units = m_config.getUnits();
            query.setGeoFilterQuery(fieldName, coordinates, radius, units);
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerGeoFilter#getConfig()
     */
    @Override
    public I_CmsSearchConfigurationGeoFilter getConfig() {

        return m_config;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerGeoFilter#getState()
     */
    @Override
    public I_CmsSearchStateGeoFilter getState() {

        return m_state;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    @Override
    public void updateForQueryChange() {

        // Nothing to do
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map, boolean)
     */
    @Override
    public void updateFromRequestParameters(Map<String, String[]> parameters, boolean isRepeated) {

        if (parameters.containsKey(m_config.getCoordinatesParam())) {
            String[] coordinates = parameters.get(m_config.getCoordinatesParam());
            if (((coordinates != null) && (coordinates.length > 0)) && CmsGeoUtil.validateCoordinates(coordinates[0])) {
                m_state.setCoordinates(coordinates[0]);
            } else {
                m_state.setCoordinates(null);
            }
        }
        if (parameters.containsKey(m_config.getRadiusParam())) {
            String[] radius = parameters.get(m_config.getRadiusParam());
            if (((radius != null) && (radius.length > 0)) && CmsGeoUtil.validateRadius(radius[0])) {
                m_state.setRadius(radius[0]);
            } else {
                m_state.setRadius(null);
            }
        }
        if (parameters.containsKey(m_config.getUnitsParam())) {
            String[] units = parameters.get(m_config.getUnitsParam());
            if ((units != null) && (units.length > 0) && CmsGeoUtil.validateUnits(units[0])) {
                m_state.setUnits(units[0]);
            } else {
                m_state.setUnits(null);
            }
        }
    }
}

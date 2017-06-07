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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.search.solr.CmsSolrQuery;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Search controller as aggregation of all single field facet controllers. */
public class CmsSearchControllerFacetsRange implements I_CmsSearchControllerFacetsRange {

    /** Controllers of the single field facets with the facet's name as key. */
    Map<String, I_CmsSearchControllerFacetRange> m_rangeFacets;

    /** Constructor taking the list of field facet controllers that are aggregated.
     * @param configs The controllers for single field facets.
     */
    public CmsSearchControllerFacetsRange(final Map<String, I_CmsSearchConfigurationFacetRange> configs) {

        m_rangeFacets = new LinkedHashMap<String, I_CmsSearchControllerFacetRange>();
        for (final String name : configs.keySet()) {
            m_rangeFacets.put(name, new CmsSearchControllerFacetRange(configs.get(name)));
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    @Override
    public void addParametersForCurrentState(final Map<String, String[]> parameters) {

        for (final I_CmsSearchControllerFacetRange controller : m_rangeFacets.values()) {
            controller.addParametersForCurrentState(parameters);
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(CmsSolrQuery)
     */
    @Override
    public void addQueryParts(CmsSolrQuery query) {

        if (!m_rangeFacets.isEmpty()) {
            query.set("facet", "true");
            final Iterator<I_CmsSearchControllerFacetRange> it = m_rangeFacets.values().iterator();
            it.next().addQueryParts(query);
            while (it.hasNext()) {
                it.next().addQueryParts(query);
            }
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerFacetsRange#getRangeFacetController()
     */
    @Override
    public Map<String, I_CmsSearchControllerFacetRange> getRangeFacetController() {

        return m_rangeFacets;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerFacetsRange#getRangeFacetControllers()
     */
    @Override
    public Collection<I_CmsSearchControllerFacetRange> getRangeFacetControllers() {

        return m_rangeFacets.values();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    @Override
    public void updateForQueryChange() {

        for (final I_CmsSearchControllerFacetRange controller : m_rangeFacets.values()) {
            controller.updateForQueryChange();
        }

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map, boolean)
     */
    @Override
    public void updateFromRequestParameters(final Map<String, String[]> parameters, boolean isReloaded) {

        for (final I_CmsSearchControllerFacetRange controller : m_rangeFacets.values()) {
            controller.updateFromRequestParameters(parameters, isReloaded);
        }

    }

}

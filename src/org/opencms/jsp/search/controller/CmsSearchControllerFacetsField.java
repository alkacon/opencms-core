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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.search.solr.CmsSolrQuery;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Search controller as aggregation of all single field facet controllers. */
public class CmsSearchControllerFacetsField implements I_CmsSearchControllerFacetsField {

    /** Controllers of the single field facets with the facet's name as key. */
    Map<String, I_CmsSearchControllerFacetField> m_fieldFacets;

    /** Constructor taking the list of field facet controllers that are aggregated.
     * @param configs The controllers for single field facets.
     */
    public CmsSearchControllerFacetsField(final Map<String, I_CmsSearchConfigurationFacetField> configs) {

        m_fieldFacets = new LinkedHashMap<String, I_CmsSearchControllerFacetField>();
        for (final String name : configs.keySet()) {
            m_fieldFacets.put(name, new CmsSearchControllerFacetField(configs.get(name)));
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    @Override
    public void addParametersForCurrentState(final Map<String, String[]> parameters) {

        for (final I_CmsSearchControllerFacetField controller : m_fieldFacets.values()) {
            controller.addParametersForCurrentState(parameters);
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(CmsSolrQuery)
     */
    @Override
    public void addQueryParts(CmsSolrQuery query) {

        if (!m_fieldFacets.isEmpty()) {
            query.set("facet", "true");
            final Iterator<I_CmsSearchControllerFacetField> it = m_fieldFacets.values().iterator();
            it.next().addQueryParts(query);
            while (it.hasNext()) {
                it.next().addQueryParts(query);
            }
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerFacetsField#getFieldFacetController()
     */
    @Override
    public Map<String, I_CmsSearchControllerFacetField> getFieldFacetController() {

        return m_fieldFacets;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerFacetsField#getFieldFacetControllers()
     */
    @Override
    public Collection<I_CmsSearchControllerFacetField> getFieldFacetControllers() {

        return m_fieldFacets.values();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    @Override
    public void updateForQueryChange() {

        for (final I_CmsSearchControllerFacetField controller : m_fieldFacets.values()) {
            controller.updateForQueryChange();
        }

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map, boolean)
     */
    @Override
    public void updateFromRequestParameters(final Map<String, String[]> parameters, boolean isReloaded) {

        for (final I_CmsSearchControllerFacetField controller : m_fieldFacets.values()) {
            controller.updateFromRequestParameters(parameters, isReloaded);
        }

    }

}

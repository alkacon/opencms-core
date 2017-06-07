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

import org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * The main search configuration. It's a collection of all the different parts of a search configuration.
 */
public class CmsSearchConfiguration implements I_CmsSearchConfiguration {

    /** The general search configuration. */
    private final I_CmsSearchConfigurationCommon m_general;

    /** The configuration for pagination. */
    private final I_CmsSearchConfigurationPagination m_pagination;

    /** The configurations for field facets. */
    private final Map<String, I_CmsSearchConfigurationFacetField> m_fieldFacets;

    /** The configurations for range facets. */
    private final Map<String, I_CmsSearchConfigurationFacetRange> m_rangeFacets;

    /** The configuration for query facet. */
    private final I_CmsSearchConfigurationFacetQuery m_queryFacet;

    /** The sorting configuration. */
    private final I_CmsSearchConfigurationSorting m_sorting;

    /** The highlighting configuration. */
    private final I_CmsSearchConfigurationHighlighting m_highlighting;

    /** The "Did you mean ...?" configuration. */
    private final I_CmsSearchConfigurationDidYouMean m_didYouMean;

    /** Constructor to initialize the configuration object via a configuration parser.
     * @param parser The configuration parser that's used to read the configuration.
     */
    public CmsSearchConfiguration(final I_CmsSearchConfigurationParser parser) {

        m_general = parser.parseCommon();
        m_pagination = parser.parsePagination();
        m_sorting = parser.parseSorting();
        m_fieldFacets = parser.parseFieldFacets();
        m_rangeFacets = parser.parseRangeFacets();
        m_queryFacet = parser.parseQueryFacet();
        m_highlighting = parser.parseHighlighter();
        m_didYouMean = parser.parseDidYouMean();

        propagateFacetNames();

    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfiguration#getDidYouMeanConfig()
     */
    public I_CmsSearchConfigurationDidYouMean getDidYouMeanConfig() {

        return m_didYouMean;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfiguration#getFieldFacetConfigs()
     */
    @Override
    public Map<String, I_CmsSearchConfigurationFacetField> getFieldFacetConfigs() {

        return m_fieldFacets;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfiguration#getGeneralConfig()
     */
    @Override
    public I_CmsSearchConfigurationCommon getGeneralConfig() {

        return m_general;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfiguration#getHighlighterConfig()
     */
    @Override
    public I_CmsSearchConfigurationHighlighting getHighlighterConfig() {

        return m_highlighting;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfiguration#getPaginationConfig()
     */
    @Override
    public I_CmsSearchConfigurationPagination getPaginationConfig() {

        return m_pagination;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfiguration#getQueryFacetConfig()
     */
    @Override
    public I_CmsSearchConfigurationFacetQuery getQueryFacetConfig() {

        return m_queryFacet;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfiguration#getRangeFacetConfigs()
     */
    public Map<String, I_CmsSearchConfigurationFacetRange> getRangeFacetConfigs() {

        return m_rangeFacets;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfiguration#getSortConfig()
     */
    @Override
    public I_CmsSearchConfigurationSorting getSortConfig() {

        return m_sorting;
    }

    /**
     * Propagates the names of all facets to each single facet.
     */
    private void propagateFacetNames() {

        // collect all names and configurations
        Collection<String> facetNames = new ArrayList<String>();
        Collection<I_CmsSearchConfigurationFacet> facetConfigs = new ArrayList<I_CmsSearchConfigurationFacet>();
        facetNames.addAll(m_fieldFacets.keySet());
        facetConfigs.addAll(m_fieldFacets.values());
        facetNames.addAll(m_rangeFacets.keySet());
        facetConfigs.addAll(m_rangeFacets.values());
        if (null != m_queryFacet) {
            facetNames.add(m_queryFacet.getName());
            facetConfigs.add(m_queryFacet);
        }

        // propagate all names
        for (I_CmsSearchConfigurationFacet facetConfig : facetConfigs) {
            facetConfig.propagateAllFacetNames(facetNames);
        }
        for (I_CmsSearchConfigurationFacet facetConfig : m_rangeFacets.values()) {
            facetConfig.propagateAllFacetNames(facetNames);
        }

    }
}

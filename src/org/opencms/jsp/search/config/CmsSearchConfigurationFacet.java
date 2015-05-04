/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration that is common for all facets. Used as base class for special facet configurations, e.g. for the field facet configuration.
 */
public class CmsSearchConfigurationFacet implements I_CmsSearchConfigurationFacet {

    /** The minimal number of hits required to add an entry to a facet. */
    protected Integer m_minCount;
    /** The maximal number of entries shown in a facet. */
    protected Integer m_limit;
    /** A name used to identify the facet when showing it in the search form. */
    protected String m_name;
    /** A label that can be displayed in the form, e.g., at top of the facet. */
    protected String m_label;
    /** The sorting of facet entries. */
    protected SortOrder m_sort;
    /** The sorting of facet entries. */
    protected List<String> m_preselection;
    /** A flag, indicating if facet filter queries should be concatenated by AND. */
    protected boolean m_isAndFacet;

    /** The constructor setting all configuration options.
     * @param minCount The minimal number of hits required to add an entry to a facet.
     * @param limit The maximal number of entries shown in a facet.
     * @param label A label that can be displayed in the form, e.g., at top of the facet.
     * @param name An optional name for the facet
     * @param order The sorting of facet entries. (Either "count" or "index")
     * @param isAndFacet If set to true, the facets filters for results containing all checked entries. Otherwise it filters for results containing at least one checked entry.
     * @param preselection A list with entries that should be preselected in the facet, when the search page is called the first time.
     */
    public CmsSearchConfigurationFacet(
        final Integer minCount,
        final Integer limit,
        final String label,
        final String name,
        final SortOrder order,
        final Boolean isAndFacet,
        final List<String> preselection) {

        m_minCount = minCount;
        m_limit = limit;
        m_label = label == null ? name : label;
        m_sort = order;
        if (isAndFacet != null) {
            m_isAndFacet = isAndFacet.booleanValue();
        }
        m_name = name;
        m_preselection = preselection == null ? new ArrayList<String>() : preselection;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet#getIgnoreMaxParamKey()
     */
    public String getIgnoreMaxParamKey() {

        return getParamKey() + "_ignoremax";
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet#getIsAndFacet()
     */
    public boolean getIsAndFacet() {

        return m_isAndFacet;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet#getLabel()
     */
    @Override
    public String getLabel() {

        return m_label;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet#getLimit()
     */
    @Override
    public Integer getLimit() {

        return m_limit;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet#getMinCount()
     */
    @Override
    public Integer getMinCount() {

        return m_minCount;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet#getName()
     */
    @Override
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet#getParamKey()
     */
    @Override
    public String getParamKey() {

        return "facet_" + getName();
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet#getPreSelection()
     */
    public List<String> getPreSelection() {

        return m_preselection;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet#getSortOrder()
     */
    @Override
    public SortOrder getSortOrder() {

        return m_sort;
    }

}

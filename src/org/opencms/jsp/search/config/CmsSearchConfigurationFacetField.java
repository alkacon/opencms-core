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

import java.util.List;

/**
 * Search configuration special for field facets. Extends @see{org.opencms.jsp.search.config.CmsSearchConfigurationFacet}.
 */
public class CmsSearchConfigurationFacetField extends CmsSearchConfigurationFacet
implements I_CmsSearchConfigurationFacetField {

    /** A prefix, all entries of a facet must start with. */
    protected String m_prefix = "";

    /** The index field to use for the facet. */
    protected String m_field;

    /** The maximal number of entries shown in a facet. */
    protected Integer m_limit;

    /** The sorting of facet entries. */
    protected SortOrder m_sort;

    /** A modifier for filter queries. */
    protected String m_fiterQueryModifier;

    /** Constructor directly setting all configuration values.
     * @param field The index field to use for the facet.
     * @param name The name of the facet. If <code>null</code> it defaults to the name of the index field.
     * @param minCount The minimal number of hits that is necessary to add a term to the facet.
     * @param limit The maximal number of facet entries.
     * @param prefix A prefix all entries of a facet must have.
     * @param label The label that can be shown over the facet entries in your search form.
     * @param order The sorting of the facet entries (either "count", which is default, or "index", which causes alphabetical sorting).
     * @param filterQueryModifier Modifier for the filter queries when a facet entry is checked. Can contain "%(value)" - what is replaced by the facet entry's value.
     * @param isAndFacet If set to true, the facets filters for results containing all checked entries. Otherwise it filters for results containing at least one checked entry.
     * @param preselection The list of facet items that should be preselected for the first search.
     * @param ignoreFiltersFromAllFacets A flag, indicating if filters from all facets should be ignored or not.
     */
    public CmsSearchConfigurationFacetField(
        final String field,
        final String name,
        final Integer minCount,
        final Integer limit,
        final String prefix,
        final String label,
        final SortOrder order,
        final String filterQueryModifier,
        final Boolean isAndFacet,
        final List<String> preselection,
        final Boolean ignoreFiltersFromAllFacets) {

        super(minCount, label, null != name ? name : field, isAndFacet, preselection, ignoreFiltersFromAllFacets);

        if (prefix != null) {
            m_prefix = prefix;
        }

        m_limit = limit;
        m_sort = order;
        m_field = field;
        m_fiterQueryModifier = filterQueryModifier;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField#getField()
     */
    @Override
    public String getField() {

        return m_field;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField#getLimit()
     */
    @Override
    public Integer getLimit() {

        return m_limit;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField#getPrefix()
     */
    @Override
    public String getPrefix() {

        return m_prefix;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField#getSortOrder()
     */
    @Override
    public SortOrder getSortOrder() {

        return m_sort;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField#modifyFilterQuery(java.lang.String)
     */
    @Override
    public String modifyFilterQuery(final String facetValue) {

        if (m_fiterQueryModifier == null) {
            return "\"" + facetValue + "\"";
        }
        return m_fiterQueryModifier.replace("%(value)", facetValue);
    }

}

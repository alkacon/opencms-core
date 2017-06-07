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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Search configuration special for field facets. Extends @see{org.opencms.jsp.search.config.CmsSearchConfigurationFacet}.
 */
public class CmsSearchConfigurationFacetRange extends CmsSearchConfigurationFacet
implements I_CmsSearchConfigurationFacetRange {

    /** The range field to use for the facet. */
    protected String m_range;
    /** The start of the complete range. */
    private String m_start;
    /** The end of the complete range. */
    private String m_end;
    /** The range size for one facet entry. */
    private String m_gap;
    /** Additional information collected by the facet. */
    private Collection<Other> m_other;
    /** The value to use for facet.range.hardend. */
    private boolean m_hardEnd;

    /** Constructor directly setting all configuration values.
     * @param range The numeric index field to use for the facet.
     * @param start The begin of the range of the complete facet
     * @param end The end of the range of the complete facet
     * @param gap The range of one facet entry
     * @param other The way how to group other values
     * @param hardEnd Flag, indicating if the last facet item range should end at <code>end</code> (use <code>true</code>) or extend to the full size of <code>gap</code> (use <code>false</code>).
     * @param name The name of the facet. If <code>null</code> it defaults to the name of the index field.
     * @param minCount The minimal number of hits that is necessary to add a term to the facet.
     * @param label The label that can be shown over the facet entries in your search form.
     * @param isAndFacet If set to true, the facets filters for results containing all checked entries. Otherwise it filters for results containing at least one checked entry.
     * @param preselection The list of facet items that should be preselected for the first search.
     * @param ignoreFilterFromAllFacets A flag, indicating if filters from all facets should be ignored or not.
     */
    public CmsSearchConfigurationFacetRange(
        final String range,
        final String start,
        final String end,
        final String gap,
        final Collection<Other> other,
        final Boolean hardEnd,
        final String name,
        final Integer minCount,
        final String label,
        final Boolean isAndFacet,
        final List<String> preselection,
        final Boolean ignoreFilterFromAllFacets) {

        super(minCount, label, null != name ? name : range, isAndFacet, preselection, ignoreFilterFromAllFacets);

        m_range = range;
        m_start = start;
        m_end = end;
        m_gap = gap;
        m_other = null == other ? new ArrayList<Other>() : other;
        m_hardEnd = null == hardEnd ? false : hardEnd.booleanValue();
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange#getEnd()
     */
    public String getEnd() {

        return m_end;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange#getGap()
     */
    public String getGap() {

        return m_gap;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange#getHardEnd()
     */
    public boolean getHardEnd() {

        return m_hardEnd;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange#getOther()
     */
    public Collection<Other> getOther() {

        return m_other;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange#getRange()
     */
    @Override
    public String getRange() {

        return m_range;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange#getStart()
     */
    public String getStart() {

        return m_start;
    }

}

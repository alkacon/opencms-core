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

/**
 * Search configuration special for field facets. Extends @see{org.opencms.jsp.search.config.CmsSearchConfigurationFacet}.
 */
public class CmsSearchConfigurationFacetField extends CmsSearchConfigurationFacet
implements I_CmsSearchConfigurationFacetField {

    /** The index field to use for the facet. */
    protected String m_field;

    /** A modifier for filter queries. */
    protected String m_fiterQueryModifier;

    /** Constructor directly setting all configuration values.
     * @param field The index field to use for the facet.
     * @param minCount The minimal number of hits that is necessary to add a term to the facet.
     * @param limit The maximal number of facet entries.
     * @param prefix A prefix all entries of a facet must have.
     * @param name The name of the facet (used to identify it when
     * @param label The label that can be shown over the facet entries in your search form.
     * @param order The sorting of the facet entries (either "count", which is default, or "index", which causes alphabetical sorting).
     * @param filterQueryModifier Modifier for the filter queries when a facet entry is checked. Can contain "%(value)" - what is replaced by the facet entry's value.
     */
    public CmsSearchConfigurationFacetField(
        final String field,
        final Integer minCount,
        final Integer limit,
        final String prefix,
        final String name,
        final String label,
        final SortOrder order,
        final String filterQueryModifier) {

        super(minCount, limit, prefix, name, label, order);
        if (m_name == null) {
            m_name = field;
        }
        if (m_label == null) {
            m_label = m_name;
        }
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
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField#modifyFilterQuery(java.lang.String)
     */
    @Override
    public String modifyFilterQuery(final String facetValue) {

        if (m_fiterQueryModifier == null) {
            return facetValue;
        }
        return m_fiterQueryModifier.replace("%(value)", facetValue);
    }

}

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

/** Configuration for a single sort option. */
public class CmsSearchConfigurationSortOption implements I_CmsSearchConfigurationSortOption {

    /** The label shown when the sort option is displayed. */
    private final String m_label;
    /** The value send in the request that identifies the option. */
    private final String m_paramValue;
    /** The sort option as given to Solr. */
    private final String m_solrValue;

    /** Constructor setting all options.
     * @param label The label shown when the sort option is displayed.
     * @param paramValue The value send in the request that identifies the option.
     * @param solrValue The sort option as given to Solr.
     */
    public CmsSearchConfigurationSortOption(final String label, final String paramValue, final String solrValue) {

        m_solrValue = solrValue;
        m_paramValue = paramValue == null ? solrValue : paramValue;
        m_label = label == null ? m_paramValue : label;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption#getLabel()
     */
    @Override
    public String getLabel() {

        return m_label;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption#getParamValue()
     */
    @Override
    public String getParamValue() {

        return m_paramValue;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption#getSolrValue()
     */
    @Override
    public String getSolrValue() {

        return m_solrValue;
    }
}

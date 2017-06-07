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
import java.util.List;

/** Configuration for the query facet. */
public class CmsSearchConfigurationFacetQuery extends CmsSearchConfigurationFacet
implements I_CmsSearchConfigurationFacetQuery {

    /** Representation of one query facet item. */
    public static class CmsFacetQueryItem implements I_CmsFacetQueryItem {

        /** The query string for the item. */
        String m_query;
        /** The label for the query item. */
        String m_label;

        /** Constructor for a facet item.
         * @param query the query string for the item.
         * @param label the label for the item, defaults to the query string.
         */
        public CmsFacetQueryItem(String query, String label) {

            m_query = query;
            m_label = label == null ? query : label;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object queryItem) {

            if (this.hashCode() != queryItem.hashCode()) {
                return false;
            }

            if (queryItem instanceof CmsFacetQueryItem) {
                CmsFacetQueryItem item = (CmsFacetQueryItem)queryItem;
                boolean equalQueries = ((null == m_query) && (null == item.getQuery()))
                    || ((null != item.getQuery()) && m_query.equals(item.getQuery()));
                boolean equalLabels = false;
                if (equalQueries) {
                    equalLabels = ((null == m_label) && (null == item.getLabel()))
                        || ((null != item.getLabel()) && m_label.equals(item.getLabel()));
                }
                return equalQueries && equalLabels;
            }
            return false;
        }

        /**
         * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery.I_CmsFacetQueryItem#getLabel()
         */
        @Override
        public String getLabel() {

            return m_label;
        }

        /**
         * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery.I_CmsFacetQueryItem#getQuery()
         */
        @Override
        public String getQuery() {

            return m_query;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int hashCode = 0;
            if (null != m_label) {
                hashCode = m_label.hashCode();
            }
            if (null != m_query) {
                hashCode += m_query.hashCode() / 2;
            }
            return hashCode;
        }

    }

    /** List of queries for the facet. */
    List<I_CmsSearchConfigurationFacetQuery.I_CmsFacetQueryItem> m_queries;

    /** Constructor for the range facet configuration.
     * @param queries the queries that can be selected for the facet
     * @param label the label used to display the facet
     * @param isAndFacet true if checked facet entries should all be matched, otherwise only one checked entry must match
     * @param preselection list of entries that should be checked in advance
     * @param ignoreFiltersFromAllFacets A flag, indicating if filters from all facets should be ignored or not.
     */
    public CmsSearchConfigurationFacetQuery(
        final List<I_CmsFacetQueryItem> queries,
        final String label,
        final Boolean isAndFacet,
        final List<String> preselection,
        final Boolean ignoreFiltersFromAllFacets) {

        super(
            null,
            label,
            I_CmsSearchConfigurationFacetQuery.NAME,
            isAndFacet,
            preselection,
            ignoreFiltersFromAllFacets);
        m_queries = queries != null ? queries : new ArrayList<I_CmsFacetQueryItem>();
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery#getQueryList()
     */
    @Override
    public List<I_CmsFacetQueryItem> getQueryList() {

        return m_queries;
    }
}

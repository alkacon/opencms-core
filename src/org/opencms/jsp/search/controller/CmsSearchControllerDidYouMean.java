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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.state.CmsSearchStateDidYouMean;
import org.opencms.jsp.search.state.I_CmsSearchStateDidYouMean;
import org.opencms.search.solr.CmsSolrQuery;

import java.util.Map;

/** Controller for the "Did you mean ...?" feature. */
public class CmsSearchControllerDidYouMean implements I_CmsSearchControllerDidYouMean {

    /** Stores the configuration. */
    private final I_CmsSearchConfigurationDidYouMean m_config;
    /** Stores the configuration. */
    private final I_CmsSearchStateDidYouMean m_state;

    /** Constructor, taking the configuration.
     * @param config the configuration.
     */
    public CmsSearchControllerDidYouMean(I_CmsSearchConfigurationDidYouMean config) {

        m_config = config;
        m_state = new CmsSearchStateDidYouMean();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    public void addParametersForCurrentState(Map<String, String[]> parameters) {

        // nothing to do

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(CmsSolrQuery)
     */
    public void addQueryParts(CmsSolrQuery query) {

        query.set("spellcheck", "true");
        String queryString = m_state.getQuery();
        query.set("spellcheck.q", queryString);
        if (m_config.getCollate()) {
            query.set("spellcheck.collate", "true");
        } else {
            query.set("spellcheck.collate", "false");
        }
        query.set("spellcheck.extendedResults", "true");
        query.set("spellcheck.count", Integer.valueOf(m_config.getCount()).toString());
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerDidYouMean#getConfig()
     */
    public I_CmsSearchConfigurationDidYouMean getConfig() {

        return m_config;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerDidYouMean#getState()
     */
    public I_CmsSearchStateDidYouMean getState() {

        return m_state;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    public void updateForQueryChange() {

        // nothing to do

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map, boolean)
     */
    public void updateFromRequestParameters(Map<String, String[]> parameters, boolean isReloaded) {

        if (parameters.containsKey(m_config.getQueryParam())) {
            final String[] queryStrings = parameters.get(m_config.getQueryParam());
            if (queryStrings.length > 0) {
                m_state.setQuery(queryStrings[0]);
                return;
            }
        }
        m_state.setQuery("");
    }

}

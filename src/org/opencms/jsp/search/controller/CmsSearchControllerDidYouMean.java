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

package org.opencms.jsp.search.controller;

import org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean;

import java.util.Map;

/** Controller for the "Did you mean ...?" feature. */
public class CmsSearchControllerDidYouMean implements I_CmsSearchControllerDidYouMean {

    /** Stores the query param from which the query is read. */
    private final String m_queryparam;

    /** Stores the configuration. */
    private final I_CmsSearchConfigurationDidYouMean m_config;

    /** Stores the current query string. */
    private String m_querystring;

    /** Constructor, taking the configuration and the queryparam.
     * @param config The Configuration.
     * @param queryparam The configured query parameter.
     */
    public CmsSearchControllerDidYouMean(I_CmsSearchConfigurationDidYouMean config, String queryparam) {

        m_queryparam = queryparam;
        m_config = config;

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    public void addParametersForCurrentState(Map<String, String[]> parameters) {

        // nothing to do

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#generateQuery()
     */
    public String generateQuery() {

        StringBuffer q = new StringBuffer();
        if (m_config.getIsEnabled()) {
            q.append("spellcheck=true");
            q.append("&spellcheck.q=").append(m_querystring);
            q.append("&spellcheck.collate=true");
        }
        return q.toString();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerDidYouMean#getConfig()
     */
    public I_CmsSearchConfigurationDidYouMean getConfig() {

        return m_config;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    public void updateForQueryChange() {

        // nothing to do

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map)
     */
    public void updateFromRequestParameters(Map<String, String[]> parameters) {

        if (parameters.containsKey(m_queryparam)) {
            final String[] queryStrings = parameters.get(m_queryparam);
            if (queryStrings.length > 0) {
                m_querystring = queryStrings[0];
                return;
            }
        }
        m_querystring = "";

    }

}

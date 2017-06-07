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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.search.solr.CmsSolrQuery;

import java.util.Map;

/** Controller for highlighting options. */
public class CmsSearchControllerHighlighting implements I_CmsSearchControllerHighlighting {

    /** The highlighting configuration. */
    private final I_CmsSearchConfigurationHighlighting m_config;

    /** Constructor taking a highlighting configuration.
     * @param config The highlighting configuration.
     */
    public CmsSearchControllerHighlighting(final I_CmsSearchConfigurationHighlighting config) {

        m_config = config;

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    @Override
    public void addParametersForCurrentState(final Map<String, String[]> parameters) {

        // Here's nothing to do, since highlighting has no state.

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(CmsSolrQuery)
     */
    @Override
    public void addQueryParts(CmsSolrQuery query) {

        query.set("hl", "true");
        query.set("hl.fl", m_config.getHightlightField());
        if (m_config.getSnippetsCount() != null) {
            query.set("hl.snippets", m_config.getSnippetsCount().toString());
        }
        if (m_config.getFragSize() != null) {
            query.set("hl.fragsize", m_config.getFragSize().toString());
        }
        if (m_config.getAlternateHighlightField() != null) {
            query.set("hl.alternateField", m_config.getAlternateHighlightField());
        }
        if (m_config.getMaxAlternateHighlightFieldLength() != null) {
            query.set("hl.maxAlternateFieldLength", m_config.getMaxAlternateHighlightFieldLength().toString());
        }
        if (m_config.getSimplePre() != null) {
            query.set("hl.simple.pre", m_config.getSimplePre());
        }
        if (m_config.getSimplePost() != null) {
            query.set("hl.simple.post", m_config.getSimplePost());
        }
        if (m_config.getFormatter() != null) {
            query.set("hl.formatter", m_config.getFormatter());
        }
        if (m_config.getFragmenter() != null) {
            query.set("hl.fragmenter", m_config.getFragmenter());
        }
        if (m_config.getUseFastVectorHighlighting() != null) {
            query.set("hl.useFastVectorHighlighting", m_config.getUseFastVectorHighlighting().toString());
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerHighlighting#getConfig()
     */
    @Override
    public I_CmsSearchConfigurationHighlighting getConfig() {

        return m_config;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    @Override
    public void updateForQueryChange() {

        // Here's nothing to do, since highlighting has no state.

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map, boolean)
     */
    @Override
    public void updateFromRequestParameters(final Map<String, String[]> parameters, boolean isReloaded) {

        // Here's nothing to do, since highlighting has no state.

    }

}

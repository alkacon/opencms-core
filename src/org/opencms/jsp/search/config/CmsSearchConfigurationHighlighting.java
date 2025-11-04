/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.search.config;

import org.opencms.main.CmsLog;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Search configuration for highlighting options.
 */
public class CmsSearchConfigurationHighlighting implements I_CmsSearchConfigurationHighlighting {

    /** Logger for the class. */
    protected static final Log LOG = CmsLog.getLog(CmsSearchConfigurationHighlighting.class);

    /** Additional configuration parameters */
    private final Map<String, String> m_params;

    /** The constructor setting all configuration values.
     * @param params the highlighting parameters as given to solr, all without the "hl." prefix that is added automatically.
     */
    public CmsSearchConfigurationHighlighting(final Map<String, String> params) {

        m_params = params == null ? Collections.emptyMap() : Collections.unmodifiableMap(params);

    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getAlternateHighlightField()
     */
    @Deprecated
    @Override
    public String getAlternateHighlightField() {

        return m_params.get("alternateField");
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getFormatter()
     */
    @Deprecated
    @Override
    public String getFormatter() {

        return m_params.get("formatter");
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getFragmenter()
     */
    @Deprecated
    @Override
    public String getFragmenter() {

        return m_params.get("fragmenter");
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getFragSize()
     */
    @Deprecated
    @Override
    public Integer getFragSize() {

        String fragSize = m_params.get("fragsize");
        if ((fragSize != null) && !fragSize.isBlank()) {
            try {
                return Integer.valueOf(fragSize);
            } catch (NumberFormatException e) {
                LOG.error(
                    "Invalid fragsize value \""
                        + fragSize
                        + "\" will be used in the final query even if null is returned here.");
            }
        }
        return null;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getHightlightField()
     */
    @Deprecated
    @Override
    public String getHightlightField() {

        return m_params.get("fl");
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getMaxAlternateHighlightFieldLength()
     */
    @Deprecated
    @Override
    public Integer getMaxAlternateHighlightFieldLength() {

        String maxAlternateFieldLength = m_params.get("maxAlternateFieldLength");
        if ((maxAlternateFieldLength != null) && !maxAlternateFieldLength.isBlank()) {
            try {
                return Integer.valueOf(maxAlternateFieldLength);
            } catch (NumberFormatException e) {
                LOG.error(
                    "Invalid maxAlternateFieldLength value \""
                        + maxAlternateFieldLength
                        + "\" will be used in the final query even if null is returned here.");
            }
        }
        return null;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getParams()
     */
    @Override
    public Map<String, String> getParams() {

        return Collections.unmodifiableMap(m_params);
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getSimplePost()
     */
    @Deprecated
    @Override
    public String getSimplePost() {

        return m_params.get("simple.post");
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getSimplePre()
     */
    @Deprecated
    @Override
    public String getSimplePre() {

        return m_params.get("simple.pre");
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getSnippetsCount()
     */
    @Deprecated
    @Override
    public Integer getSnippetsCount() {

        String snippets = m_params.get("snippets");
        if ((snippets != null) && !snippets.isBlank()) {
            try {
                return Integer.valueOf(snippets);
            } catch (NumberFormatException e) {
                LOG.error(
                    "Invalid snippets value \""
                        + snippets
                        + "\" will be used in the final query even if null is returned here.");
            }
        }
        return null;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getUseFastVectorHighlighting()
     */
    @Deprecated
    @Override
    public Boolean getUseFastVectorHighlighting() {

        String method = m_params.get("method");
        if (null == method) {
            return null;
        }
        return Boolean.valueOf("fastVector".equals(method));
    }
}

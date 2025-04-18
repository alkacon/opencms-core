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

/** Class keeping the configuration of the "Did you mean ...?" feature of the search form. */
public class CmsSearchConfigurationDidYouMean implements I_CmsSearchConfigurationDidYouMean {

    /** The default escape setting. */
    private static final boolean DEFAULT_ESCAPE = true;
    /** The default collate setting. */
    private static final boolean DEFAULT_COLLATE = true;
    /** Default for maximal number of suggestions. */
    private static final int DEFAULT_COUNT = 5;

    /** A modifier for the search query. */
    private final boolean m_collate;
    /** Parameter used to transmit the query used for spellchecking. */
    private final String m_param;
    /** Flag, indicating if the query should be escaped. */
    private final boolean m_escapeQueryChars;
    /** Maximal number of suggestions. */
    private final int m_count;

    /** Constructor setting all the state.
     * @param param The request parameter used to send the spellcheck query.
     * @param escapeQueryChars Flag, indicating if query characters should be escaped.
     * @param collate Flag, indicating if the results should be collated.
     * @param count The maximal number of suggestions.
     */
    public CmsSearchConfigurationDidYouMean(
        final String param,
        final Boolean escapeQueryChars,
        final Boolean collate,
        final Integer count) {

        m_param = param;
        m_escapeQueryChars = null == escapeQueryChars ? DEFAULT_ESCAPE : escapeQueryChars.booleanValue();
        m_collate = null == collate ? DEFAULT_COLLATE : collate.booleanValue();
        m_count = null == count ? DEFAULT_COUNT : count.intValue();

    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean#getCollate()
     */
    public boolean getCollate() {

        return m_collate;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean#getCount()
     */
    public int getCount() {

        return m_count;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean#getEscapeQueryChars()
     */
    public boolean getEscapeQueryChars() {

        return m_escapeQueryChars;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean#getQueryParam()
     */
    public String getQueryParam() {

        return m_param;
    }

}

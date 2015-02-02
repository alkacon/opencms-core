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

package org.opencms.jsp.search.state;

/** Class for handling the state of the common search options. */
public class CmsSearchStateCommon implements I_CmsSearchStateCommon {

    /** The current query string (as given by the user). */
    private String m_query = "";
    /** The last query string (as given by the user). */
    private String m_lastquery = "";

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateCommon#getLastQuery()
     */
    @Override
    public String getLastQuery() {

        return m_lastquery;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateCommon#getQuery()
     */
    @Override
    public String getQuery() {

        return m_query;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateCommon#setLastQuery(java.lang.String)
     */
    @Override
    public void setLastQuery(final String lastquery) {

        m_lastquery = lastquery;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateCommon#setQuery(java.lang.String)
     */
    @Override
    public void setQuery(final String query) {

        m_query = query;

    }
}

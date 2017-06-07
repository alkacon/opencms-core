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

package org.opencms.jsp.search.state;

/** Class for keeping the state for the pagination. */
public class CmsSearchStatePagination implements I_CmsSearchStatePagination {

    /** The current page. */
    int m_current;
    /** Flag, indicating if the chosen page should be ignored. */
    // It's important that this is false by default.
    boolean m_ignorePage;

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStatePagination#getCurrentPage()
     */
    @Override
    public int getCurrentPage() {

        return m_current;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStatePagination#getIgnorePage()
     */
    @Override
    public boolean getIgnorePage() {

        return m_ignorePage;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStatePagination#setCurrentPage(int)
     */
    @Override
    public void setCurrentPage(final int page) {

        m_current = page;

    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStatePagination#setIgnorePage(boolean)
     */
    @Override
    public void setIgnorePage(final boolean ignore) {

        m_ignorePage = ignore;
    }

}

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

/** Interface for pagination states. */
public interface I_CmsSearchStatePagination {

    /** Returns the current page.
     * @return The current page.
     */
    int getCurrentPage();

    /** Returns a flag, indicating if the state of the current page should be ignored.
     * This is for example the case if the user's query has changed.
     * @return A flag, indicating if the state of the current page should be ignored.
     */
    boolean getIgnorePage();

    /** Setter for the current page.
     * @param page The current page to set.
     */
    void setCurrentPage(int page);

    /** Setter for a flag, indicating if the state of the current page should be ignored.
     * @param ignore A flag, indicating if the state of the current page should be ignored.
     */
    void setIgnorePage(boolean ignore);
}

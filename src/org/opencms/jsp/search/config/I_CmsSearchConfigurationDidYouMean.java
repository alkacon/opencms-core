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

/** Interface of the "Did you mean ...?" configuration for the JSP search form. */
public interface I_CmsSearchConfigurationDidYouMean {

    /** Returns a flag, indicating if collation is turned on or off.
     * @return A flag, indicating if collation is turned on or off.
     */
    boolean getCollate();

    /** Returns the maximal number of suggestions.
     * @return The maximal number of suggestions.
     */
    int getCount();

    /** Returns the parameter name of the request parameter used to send the current query string for spellchecking.
     * @return The request parameter name used to send the current query string for spellchecking.
     */
    String getQueryParam();
}

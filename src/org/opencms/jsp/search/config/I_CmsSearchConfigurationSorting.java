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

import java.util.Collection;

/** The interface a sort configuration must implement. */
public interface I_CmsSearchConfigurationSorting {

    /** Returns the sort configuration that is used as default.
     * @return The sort configuration that is used as default.
     */
    I_CmsSearchConfigurationSortOption getDefaultSortOption();

    /** Returns the configurations of all available sort options.
     * @return The configurations of all available sort options.
     */
    Collection<I_CmsSearchConfigurationSortOption> getSortOptions();

    /** Returns the request parameter that should be used to send the currently chosen sort option.
     * @return The request parameter that should be used to send the currently chosen sort option.
     */
    String getSortParam();
}

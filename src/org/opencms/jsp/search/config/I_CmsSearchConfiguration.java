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

import java.util.Map;

/** Interface the main search configuration must implement.
 * It basically allows to access the various configuration parts.
 */
public interface I_CmsSearchConfiguration {

    /** Returns the configuration for "Did you mean ...?".
     * @return The configuration for "Did you mean ...?".
     */
    I_CmsSearchConfigurationDidYouMean getDidYouMeanConfig();

    /** Returns the configuration for field facets.
     * @return The configuration for field facets.
     */
    Map<String, I_CmsSearchConfigurationFacetField> getFieldFacetConfigs();

    /** Returns the common search (form) configuration.
     * @return The common search (form) configuration.
     */
    I_CmsSearchConfigurationCommon getGeneralConfig();

    /** Returns the configuration for highlighting.
     * @return The configuration for highlighting.
     */
    I_CmsSearchConfigurationHighlighting getHighlighterConfig();

    /** Returns the configuration for pagination.
     * @return The configuration for pagination.
     */
    I_CmsSearchConfigurationPagination getPaginationConfig();

    /** Returns the configuration for field facets.
     * @return The configuration for field facets.
     */
    I_CmsSearchConfigurationFacetQuery getQueryFacetConfig();

    /** Returns the configuration for range facets.
     * @return The configuration for range facets.
     */
    Map<String, I_CmsSearchConfigurationFacetRange> getRangeFacetConfigs();

    /** Returns the configuration for sorting.
     * @return The configuration for sorting.
     */
    I_CmsSearchConfigurationSorting getSortConfig();
}

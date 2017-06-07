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

package org.opencms.jsp.search.result;

import java.util.Map;

/** Interface for the search state parameters. */
public interface I_CmsSearchStateParameters {

    /** Returns the search state parameters with the parameter for ignoring a facet's (specified by first key) limit added.
     * @return The search state parameters with the parameter for ignoring a facet's (specified by first key) limit added.
     */
    Map<String, I_CmsSearchStateParameters> getAddIgnoreFacetLimit();

    /** Returns the search state parameters with the filter query for facets (specified by first key) item (specified by the second key) added.
     * @return The search state parameters with the filter query for facets (specified by first key) item (specified by the second key) added.
     */
    Map<String, Map<String, I_CmsSearchStateParameters>> getCheckFacetItem();

    /** Returns the search state parameters with the Query parameter's value adjusted to the key of the map.
     * @return The search state parameters with the Query parameter's value adjusted to the key of the map.
     */
    Map<String, I_CmsSearchStateParameters> getNewQuery();

    /** Returns the search state parameters with the parameter for ignoring a facet's (specified by first key) limit removed.
     * @return The search state parameters with the parameter for ignoring a facet's (specified by first key) limit removed.
     */
    Map<String, I_CmsSearchStateParameters> getRemoveIgnoreFacetLimit();

    /** Returns the search state parameters with all filter queries for facets removed.
     * @return The search state parameters with all filter queries for facets removed.
     */
    I_CmsSearchStateParameters getResetAllFacetStates();

    /** Returns the search state parameters with all filter queries for the facet specified as key removed.
     * @return The search state parameters with all filter queries for the facet specified as key removed.
     */
    Map<String, I_CmsSearchStateParameters> getResetFacetState();

    /** Returns the search state parameters with the value for the current page's parameter replaced by the key.
     * @return The search state parameters with the value for the current page's parameter replaced by the key.
     */
    Map<String, I_CmsSearchStateParameters> getSetPage();

    /** Returns the search state parameters with the value for the sort parameter replaced by the key.
     * @return The search state parameters with the value for the sort parameter replaced by the key.
     */
    Map<String, I_CmsSearchStateParameters> getSetSortOption();

    /** Returns the search state parameters with the filter query for facets (specified by first key) item (specified by the second key) removed.
     * @return The search state parameters with the filter query for facets (specified by first key) item (specified by the second key) removed.
     */
    Map<String, Map<String, I_CmsSearchStateParameters>> getUncheckFacetItem();

}
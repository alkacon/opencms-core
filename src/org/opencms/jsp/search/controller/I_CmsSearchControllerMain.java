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

/** Interface for the main search controller. Allows access to the various sub-controllers. */
public interface I_CmsSearchControllerMain extends I_CmsSearchController {

    /** Returns the controller for common search (form) configurations.
     *
     * @return The controller for common search (form) configurations.
     */
    I_CmsSearchControllerCommon getCommon();

    /** Returns the controller for "Did you mean ...?".
     * @return The controller for "Did you mean ...?".
     */
    I_CmsSearchControllerDidYouMean getDidYouMean();

    /** Returns the controller for field facets.
     *
     * @return The controller for field facets.
     */
    I_CmsSearchControllerFacetsField getFieldFacets();

    /** Returns the controller for highlighting.
         * @return The controller for highlighting.
         */
    I_CmsSearchControllerHighlighting getHighlighting();

    /** Returns the controller for pagination.
    * @return The controller for highlighting.
    */
    I_CmsSearchControllerPagination getPagination();

    /** Returns the controller for the query facet.
    *
    * @return The controller for the query facet.
    */
    I_CmsSearchControllerFacetQuery getQueryFacet();

    /** Returns the controller for range facets.
    *
    * @return The controller for range facets.
    */
    I_CmsSearchControllerFacetsRange getRangeFacets();

    /** Returns the controller for sorting.
     * @return The controller for sorting.
     */
    I_CmsSearchControllerSorting getSorting();
}

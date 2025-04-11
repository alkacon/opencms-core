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

package org.opencms.jsp.search.controller;

import org.opencms.file.CmsObject;
import org.opencms.search.solr.CmsSolrQuery;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Interface all search controllers must implement. It consists of methods for query generation and state updates. */
public interface I_CmsSearchController {

    /** Solr query params that can have only one value. */
    List<String> SET_VARIABLES = Arrays.asList(new String[] {"q", "rows", "start", "sort", "fl"});

    /** Add the request parameters that reflect the controllers current state (useful for link generation outside of a form).
     * @param parameters The request parameters reflecting the controllers currents state.
     */
    void addParametersForCurrentState(Map<String, String[]> parameters);

    /** Generate the Solr query part specific for the controller, e.g., the part for a field facet.
     *
     * @param query A, possibly empty, query, where further query parts are added
     *
     * @deprecated use {@link #addQueryParts(CmsSolrQuery, CmsObject)} instead.
     */
    @Deprecated
    default void addQueryParts(CmsSolrQuery query) {

        addQueryParts(query, null);
    }

    /** Generate the Solr query part specific for the controller, e.g., the part for a field facet.
     * @param query A, possibly empty, query, where further query parts are added
     * @param cms the current context to resolve context-specific macros.
     */
    void addQueryParts(CmsSolrQuery query, CmsObject cms);

    /** Update the controllers state in case the term that is search for (the query as given by the user) has changed. */
    void updateForQueryChange();

    /** Update the controllers state from the given request parameters.
     * @param parameters The request parameters.
     * @param isRepeated a flag, indicating, if the search is performed repeatedly, opposed to entering the search page for the first time. */
    void updateFromRequestParameters(final Map<String, String[]> parameters, final boolean isRepeated);
}

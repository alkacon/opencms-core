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

/** Interface the common search configuration must implement. */
public interface I_CmsSearchConfigurationCommon {

    /** Returns a map from request parameter names to Solr query parts (where the parameter's values are typically inserted).
     * @return A map from request parameter names to Solr query parts (where the parameter's values are typically inserted).
     */
    Map<String, String> getAdditionalParameters();

    /** Returns a flag, indicating if special query characters (e.g., ":", "(", "[" ...) should be escaped in the query string.
     * @return A flag, indicating if special query characters (e.g., ":", "(", "[" ...) should be escaped in the query string.
     */
    boolean getEscapeQueryChars();

    /** Returns the extra params given to Solr.
     * @return The extra params given to Solr - in format "p1=v1&p2=v2".
     */
    String getExtraSolrParams();

    /**Flag, indicating if also resources that are expired.<p>
     * NOTE: if you are not in the edit mode, the flag is ignored and expired resources are never returned.
     *
     * @return Flag, indicating if also resources that are expired should be returned.
     */
    boolean getIgnoreExpirationDate();

    /** Returns a flag, indicating if the query and lastquery params should be ignored when
     *  generating the query. This is useful, if you have a fixed query in the extra Solr params
     *  configured.
     * @return A flag, indicating if the query and lastquery params should be ignored.
     */
    boolean getIgnoreQueryParam();

    /**Flag, indicating if also resources that are not yet released.<p>
     * NOTE: if you are not in the edit mode, the flag is ignored and unreleased resources are never returned.
     *
     * @return Flag, indicating if also resources that are not yet released should be returned.
     */
    boolean getIgnoreReleaseDate();

    /** Returns the parameter name of the request parameter used to send the last query string.
     * @return The request parameter name used to send the last query string.
     */
    String getLastQueryParam();

    /** Modifies the query string according to the specified query modifier.
     * @param queryString the query to modify.
     * @return the modified query.
     */
    String getModifiedQuery(String queryString);

    /** Returns the parameter name of the request parameter used to send the current query string.
     * @return The request parameter name used to send the current query string.
     */
    String getQueryParam();

    /** Returns the parameter name of the request parameter used to indicate if the search form is loaded the first time or repeatedly.
     * @return The request parameter name used to indicate if the search form is loaded the first time or repeatedly.
     */
    String getReloadedParam();

    /** Returns a flag, indicating if for an empty search query, search should be performed using a wildcard.
     * @return A flag, indicating if for an empty search query, search should be performed using a wildcard.
     */
    boolean getSearchForEmptyQueryParam();

    /** Returns the Solr core that should be used. Can also be <code>null</code>.
     * @return The Solr core to use, or <code>null</code> if none is configured.
     */
    String getSolrCore();

    /** Returns the Solr index that should be used. Can also be <code>null</code>.
     * @return The Solr index to use, or <code>null</code> if none is configured.
     */
    String getSolrIndex();

}

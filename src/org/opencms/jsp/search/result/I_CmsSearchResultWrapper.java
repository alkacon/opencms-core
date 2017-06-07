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

import org.opencms.jsp.search.controller.I_CmsSearchControllerMain;
import org.opencms.search.CmsSearchException;
import org.opencms.search.solr.CmsSolrQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

/** Interface of the JSP EL friendly wrapper for all Solr search results and the search form controller. */
public interface I_CmsSearchResultWrapper {

    /** Returns the main controller for the search form.
     * @return The main controller for the search form.
     */
    I_CmsSearchControllerMain getController();

    /** Returns the "Did you mean ...?" suggestion - if did you mean is enabled.
     * @return The "Did you mean ...?" suggestion - if did you mean is enabled.
     */
    String getDidYouMeanCollated();

    /** Returns the suggestion of "Did you mean ...?" for the complete query - if did you mean is enabled.
     * @return The suggestion of "Did you mean ...?" for the complete query - if did you mean is enabled.
     */
    Suggestion getDidYouMeanSuggestion();

    /** Returns the last index of the documents displayed.
     * @return The last index of the documents displayed.
     */
    int getEnd();

    /** Returns the search exception if search fails.
     * @return The exception thrown by Solr, or <code>null</code> if the search succeeds. */
    CmsSearchException getException();

    /** Returns the result of the query facet, i.e., the map from queries to the number of hits.
     * @return The result of the query facet, i.e., the map from queries to the number of hits.
     */
    Map<String, Integer> getFacetQuery();

    /** Returns the map for field facet names to the search result part for that field facet.
     * @return The map for field facet names to the search result part for that field facet.
     */
    Map<String, FacetField> getFieldFacet();

    /** Returns the collection of the search result parts for the field facets.
     * @return The collection of the search result parts for the field facets.
     */
    Collection<FacetField> getFieldFacets();

    /** Returns the query object as send to Solr.
     * @return The query object as send to Solr.
     */
    CmsSolrQuery getFinalQuery();

    /** Returns the map from the document ids to the corresponding highlighting results (as map from the highlighted field to the highlighted snippets).
     * @return The map from the document ids to the corresponding highlighting results (as map from the highlighted field to the highlighted snippets).
     */
    Map<String, Map<String, List<String>>> getHighlighting();

    /** Returns the maximal score of the found documents.
     * @return The maximal score of the found documents.
     */
    Float getMaxScore();

    /** Returns for the specified facet (key) the selected facet entries that are not part of the returned facet entries are provided (value).
     * @return For the specified facet (key) the selected facet entries that are not part of the returned facet entries are provided (value).
     */
    Map<String, List<String>> getMissingSelectedFieldFacetEntries();

    /** Returns the selected facet entries that are not part of the returned facet entries are provided (value).
     * @return The selected facet entries that are not part of the returned facet entries are provided (value).
     */
    List<String> getMissingSelectedQueryFacetEntries();

    /** Returns for the specified facet (key) the selected facet entries that are not part of the returned facet entries are provided (value).
     * @return For the specified facet (key) the selected facet entries that are not part of the returned facet entries are provided (value).
     */
    Map<String, List<String>> getMissingSelectedRangeFacetEntries();

    /** Returns the number of resources that where found.
     * @return The number of resources that where found.
     */
    long getNumFound();

    /** Returns the number of pages necessary to show all search results.
     * @return The number of pages necessary to show all search results.
     */
    int getNumPages();

    /** Returns the number of the fist page that should be shown in a "Google"-like page navigation.
     * @return The number of the fist page that should be shown in a "Google"-like page navigation.
     */
    int getPageNavFirst();

    /** Returns the number of the last page that should be shown in a "Google"-like page navigation.
     * @return The number of the last page that should be shown in a "Google"-like page navigation.
     */
    int getPageNavLast();

    /** Returns the map for range facet names to the search result part for that range facet.
     * @return The map for range facet names to the search result part for that range facet.
     */
    @SuppressWarnings("rawtypes")
    Map<String, RangeFacet> getRangeFacet();

    /** Returns the collection of the search result parts for the range facets.
     * @return The collection of the search result parts for the range facets.
     */
    @SuppressWarnings("rawtypes")
    Collection<RangeFacet> getRangeFacets();

    /** Returns the collection of the search results that are returned by Solr.
     * @return The collection of the search results that are returned by Solr.
     */
    Collection<I_CmsSearchResourceBean> getSearchResults();

    /** Returns the index (starting at 1) of the first result that is returned for displaying.
     * @return The index (starting at 1) of the first result that is returned for displaying.
     */
    Long getStart();

    /** Returns the map from a sort option name to the link parameters that should be appended when you want to display that search option.
     * @return The map from a sort option name to the link parameters that should be appended when you want to display that search option.
     */
    I_CmsSearchStateParameters getStateParameters();
}

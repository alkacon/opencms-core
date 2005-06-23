/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchParameters.java,v $
 * Date   : $Date: 2005/06/23 11:11:28 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search;

import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.lucene.search.Sort;

/**
 * Contains the search parameters for a call to <code>{@link org.opencms.search.CmsSearchIndex#search(org.opencms.file.CmsObject, CmsSearchParameters, int, int)}</code>.<p>
 *   
 * @version $Revision: 1.5 $
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchParameters {

    /** If <code>true</code>, the category count is calculated for all search results. */
    private boolean m_calculateCategories;

    /** The list of categories to limit the search to. */
    private List m_categories;

    /** The list of search index fields to search in. */
    private List m_fields;

    /** The search query to use. */
    private String m_query;

    /** Only resource that are sub-resource of one of the search roots are included in the search result. */
    private List m_roots;

    /** The sort order for the search. */
    private Sort m_sort;

    /**
     * Creates a new search parameter instance with the provided parameter values.<p>
     * 
     * @param query the search term to search the index
     * @param fields the list of fields to search
     * @param roots only resource that are sub-resource of one of the search roots are included in the search result
     * @param categories the list of categories to limit the search to
     * @param calculateCategories if <code>true</code>, the category count is calculated for all search results
     *      (use with caution, this option uses much performance)
     * @param sort the sort order for the search
     */
    public CmsSearchParameters(
        String query,
        List fields,
        List roots,
        List categories,
        boolean calculateCategories,
        Sort sort) {

        super();
        m_query = query;
        m_fields = fields;
        m_roots = roots;
        m_categories = categories;
        m_calculateCategories = calculateCategories;
        m_sort = sort;
    }

    /**
     * Returns the list of categories to limit the search to.<p>
     *
     * @return the list of categories to limit the search to
     */
    public List getCategories() {

        return m_categories;
    }

    /**
     * Returns the list of search index fields to search in.<p>
     *
     * @return the list of search index fields to search in
     */
    public List getFields() {

        return m_fields;
    }

    /**
     * Returns the search query to use.<p>
     *
     * @return the search query to use
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Returns the search roots to use.<p>
     * 
     * Only resource that are sub-resource of one of the search roots are included in the search result.<p>
     * 
     * @return the search roots to use
     */
    public List getRoots() {

        return m_roots;
    }

    /**
     * Returns the sort order for the search.<p>
     *
     * @return the sort order for the search
     */
    public Sort getSort() {

        return m_sort;
    }

    /**
     * Returns <code>true</code> if the category count is calculated for all search results.<p>
     *
     * @return <code>true</code> if the category count is calculated for all search results
     */
    public boolean isCalculateCategories() {

        return m_calculateCategories;
    }

    /**
     * Creates a merged parameter set from this parameters, restricted by the given other parameters.<p>
     * 
     * This is mainly intended for "search in search result" functions.<p>
     * 
     * The restricted query is build of the queries of both parameters, appended with AND.<p>
     * 
     * The lists in the restriction for <code>{@link #getFields()}</code>, <code>{@link #getRoots()}</code> and
     * <code>{@link #getCategories()}</code> are <b>intersected</b> with the lists of this search parameters. Only
     * elements containd in both lists are included for the created search parameters. 
     * If a list in either the restriction or in this search parameters is <code>null</code>, 
     * the list from the other search parameters is used direclty.<p> 
     * 
     * The values for
     * <code>{@link #isCalculateCategories()}</code>
     * and <code>{@link #getSort()}</code> of this parameters are used for the restricted parameters.<p>
     * 
     * @param restriction the parameters to restrict this parameters with
     * @return the restricted parameters
     */
    public CmsSearchParameters restrict(CmsSearchParameters restriction) {

        // append queries
        StringBuffer query = new StringBuffer(256);
        if (getQuery() != null) {
            query.append("+(");
            query.append(getQuery());
            query.append(")");
        }
        if (restriction.getQuery() != null) {
            query.append(" +(");
            query.append(restriction.getQuery());
            query.append(")");
        }

        // restrict fields
        List fields = null;
        if ((m_fields != null) && (m_fields.size() > 0)) {
            if ((restriction.getFields() != null) && (restriction.getFields().size() > 0)) {
                fields = ListUtils.intersection(m_fields, restriction.getFields());
            } else {
                fields = m_fields;
            }
        } else {
            fields = restriction.getFields();
        }

        // restrict roots
        List roots = null;
        if ((m_roots != null) && (m_roots.size() > 0)) {
            if ((restriction.getRoots() != null) && (restriction.getRoots().size() > 0)) {
                roots = ListUtils.intersection(m_roots, restriction.getRoots());
            } else {
                roots = m_roots;
            }
        } else {
            roots = restriction.getRoots();
        }

        // restrict categories
        List categories = null;
        if ((m_categories != null) && (m_categories.size() > 0)) {
            if ((restriction.getCategories() != null) && (restriction.getCategories().size() > 0)) {
                categories = ListUtils.intersection(m_categories, restriction.getCategories());
            } else {
                categories = m_categories;
            }
        } else {
            categories = restriction.getCategories();
        }

        // create the new search parameters 
        return new CmsSearchParameters(query.toString(), fields, roots, categories, m_calculateCategories, m_sort);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("query:[");
        result.append(m_query);
        result.append("] ");
        if ((m_fields != null) && (m_fields.size() > 0)) {
            result.append("fields:[");
            for (int i = 0; i < m_fields.size(); i++) {
                result.append(m_fields.get(i));
                if (i + 1 < m_fields.size()) {
                    result.append(", ");
                }
            }
            result.append("] ");
        }
        if ((m_roots != null) && (m_roots.size() > 0)) {
            result.append("roots:[");
            for (int i = 0; i < m_roots.size(); i++) {
                result.append(m_roots.get(i));
                if (i + 1 < m_roots.size()) {
                    result.append(", ");
                }
            }
            result.append("] ");
        }
        if ((m_categories != null) && (m_categories.size() > 0)) {
            result.append("categories:[");
            for (int i = 0; i < m_categories.size(); i++) {
                result.append(m_categories.get(i));
                if (i + 1 < m_categories.size()) {
                    result.append(", ");
                }
            }
            result.append("] ");
        }
        if (m_calculateCategories) {
            result.append("calculate-categories ");
        }
        result.append("sort:[");
        if (m_sort == CmsSearch.SORT_DEFAULT) {
            result.append("default");
        } else if (m_sort == CmsSearch.SORT_TITLE) {
            result.append("title");
        } else if (m_sort == CmsSearch.SORT_DATE_CREATED) {
            result.append("date-created");
        } else if (m_sort == CmsSearch.SORT_DATE_LASTMODIFIED) {
            result.append("date-lastmodified");
        } else {
            result.append("unknown");
        }
        result.append("]");
        return result.toString();
    }
}
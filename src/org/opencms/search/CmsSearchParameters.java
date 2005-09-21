/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchParameters.java,v $
 * Date   : $Date: 2005/09/21 09:21:08 $
 * Version: $Revision: 1.5.2.2 $
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * Contains the search parameters for a call to <code>{@link org.opencms.search.CmsSearchIndex#search(org.opencms.file.CmsObject, CmsSearchParameters, int)}</code>.<p>
 * 
 * Primary purpose is translation of search arguments to response parameters and from request parameters as 
 * well as support for creation of restrictions of several search query parameter sets. <p>
 * 
 *   
 * @version $Revision: 1.5.2.2 $
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.5.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchParameters {

    /** Sort result documents by date of last modification, then score. */
    public static final Sort SORT_DATE_CREATED = new Sort(new SortField[] {
        new SortField(I_CmsDocumentFactory.DOC_DATE_CREATED, true),
        SortField.FIELD_SCORE});

    /** Sort result documents by date of last modification, then score. */
    public static final Sort SORT_DATE_LASTMODIFIED = new Sort(new SortField[] {
        new SortField(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED, true),
        SortField.FIELD_SCORE});

    /** Default sort order (by document score - for this <code>null</code> gave best performance). */
    public static final Sort SORT_DEFAULT = null;

    /** Names of the default sort options. */
    public static final String[] SORT_NAMES = {
        "SORT_DEFAULT",
        "SORT_DATE_CREATED",
        "SORT_DATE_LASTMODIFIED",
        "SORT_TITLE"};

    /** Sort result documents by title, then score. */
    public static final Sort SORT_TITLE = new Sort(new SortField[] {
        new SortField(I_CmsDocumentFactory.DOC_TITLE_KEY),
        SortField.FIELD_SCORE});

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchParameters.class);

    /** The index to search. */
    protected CmsSearchIndex m_index;

    /** The current result page. */
    protected int m_page;

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
     * Creates a new search parameter instance with no search query and 
     * default values for the remaining parameters. <p>
     * 
     * Before using this search parameters for a search method 
     * <code>{@link #setQuery(String)}</code> has to be invoked. <p>
     * 
     */
    public CmsSearchParameters() {

        this("");
    }

    /**
     * Creates a new search parameter instance with the provided search query and 
     * default values for the remaining parameters. <p>
     * 
     * Only the "meta" field (combination of content and title) will be used for search. 
     * No search root restriction is chosen. 
     * No category restriction is used. 
     * No categorie counts are calculated for the result. 
     * Sorting is turned off. This is a simple but fast setup. <p>
     * 
     * @param query the query to search for 
     */
    public CmsSearchParameters(String query) {

        this(query, null, null, null, false, null);

    }

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
        m_query = (query == null) ? "" : query;
        if (fields == null) {
            fields = new ArrayList(2);
            fields.add(CmsSearchIndex.DOC_META_FIELDS[0]);
            fields.add(CmsSearchIndex.DOC_META_FIELDS[1]);
        }
        m_fields = fields;
        if (roots == null) {
            roots = new ArrayList();
        }
        m_roots = roots;
        m_categories = (categories == null) ? new LinkedList() : categories;
        m_calculateCategories = calculateCategories;
        // null sort is allowed default
        m_sort = sort;
        m_page = 1;
    }

    /**
     * Returns wether category counts are calculated for search results or not. <p>
     * 
     * @return a boolean that tells wether category counts are calculated for search results or not
     */
    public boolean getCalculateCategories() {

        return m_calculateCategories;
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
     * Get the name of the index for the search.<p>
     * 
     * @return the name of the index for the search
     */
    public String getIndex() {

        return m_index.getName();
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
     * Returns the list of strings of search roots to use.<p>
     * 
     * Only resource that are sub-resource of one of the search roots are included in the search result.<p>
     * 
     * @return the list of strings of search roots to use
     */
    public List getRoots() {

        return m_roots;
    }

    /**
     * Returns the list of categories to limit the search to.<p>
     *
     * @return the list of categories to limit the search to
     */
    public String getSearchCategories() {

        return toSeparatedString(getCategories(), ',');
    }

    /**
     * Returns wether the content field will be searched or not. 
     * 
     * @return wether the content field will be searched or not 
     * 
     * @see I_CmsDocumentFactory#DOC_CONTENT
     */
    public boolean getSearchFieldContent() {

        return m_fields.contains(I_CmsDocumentFactory.DOC_META);
    }

    /**
     * Returns wether the description field will be searched or not. 
     * 
     * @return wether the description field will be searched or not 
     * 
     * @see I_CmsDocumentFactory#DOC_DESCRIPTION
     */
    public boolean getSearchFieldDescription() {

        return m_fields.contains(I_CmsDocumentFactory.DOC_DESCRIPTION);
    }

    /**
     * Returns wether the keywords field will be searched or not. 
     * 
     * @return wether the keywords field will be searched or not 
     * 
     * @see I_CmsDocumentFactory#DOC_KEYWORDS
     */
    public boolean getSearchFieldKeywords() {

        return m_fields.contains(I_CmsDocumentFactory.DOC_KEYWORDS);
    }

    /**
     * Returns wether the meta field will be searched or not. 
     * 
     * @return wether the meta field will be searched or not 
     * 
     * @see I_CmsDocumentFactory#DOC_META
     */
    public boolean getSearchFieldMeta() {

        return m_fields.contains(I_CmsDocumentFactory.DOC_META);
    }

    /**
     * Returns wether the title field will be searched or not. 
     * 
     * @return wether the title field will be searched or not 
     * 
     * @see I_CmsDocumentFactory#DOC_TITLE_INDEXED
     */
    public boolean getSearchFieldTitle() {

        return m_fields.contains(I_CmsDocumentFactory.DOC_TITLE_INDEXED);
    }

    /**
     * Returns the search index to search in or null if not set before 
     * (<code>{@link #setSearchIndex(CmsSearchIndex)}</code>). <p>
     * 
     * @return the search index to search in or null if not set before (<code>{@link #setSearchIndex(CmsSearchIndex)}</code>)
     */
    public CmsSearchIndex getSearchIndex() {

        return m_index;
    }

    /**
     * Returns the search page to display.<p>
     *  
     * @return the search page to display
     */
    public int getSearchPage() {

        return m_page;
    }

    /**
     * Returns the comma separated lists of root folder names to restrict search to.<p>
     * 
     * This method is a "sibling" to method <code>{@link #getRoots()}</code> but with 
     * the support of being useable with widget technology. <p>
     * 
     * @return the comma separated lists of field names to search in
     * 
     * @see #setSortName(String)
     */

    public String getSearchRoots() {

        return toSeparatedString(m_roots, ',');
    }

    /**
     * Returns the instance that defines the sort order for the results. 
     * 
     * @return the instance that defines the sort order for the results
     */
    public Sort getSort() {

        return m_sort;
    }

    /**
     * Returns the name of the sort option being used.<p>
     * @return the name of the sort option being used
     * 
     * @see #SORT_NAMES
     * @see #setSortName(String)
     */
    public String getSortName() {

        if (m_sort == SORT_DATE_CREATED) {
            return SORT_NAMES[1];
        }
        if (m_sort == SORT_DATE_LASTMODIFIED) {
            return SORT_NAMES[2];
        }
        if (m_sort == SORT_TITLE) {
            return SORT_NAMES[3];
        }
        return SORT_NAMES[0];
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
            // don't blow up unneccessary closures (if CmsSearch is reused and restricted several times)
            boolean closure = !getQuery().startsWith("+(");
            if (closure) {
                query.append("+(");
            }
            query.append(getQuery());
            if (closure) {
                query.append(")");
            }
        }
        if (restriction.getQuery() != null) {
            // don't let lucene max terms be exceeded in case someone reuses a CmsSearch and continuously restricts 
            // query with the same restrictions...
            if (query.indexOf(restriction.getQuery()) < 0) {
                query.append(" +(");
                query.append(restriction.getQuery());
                query.append(")");
            }
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
                // TODO: This only works if there are equal paths in both parameter sets - for two distinct sets 
                //       all root restrictions are dropped with an empty list. 
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
        CmsSearchParameters result = new CmsSearchParameters(
            query.toString(),
            fields,
            roots,
            categories,
            m_calculateCategories,
            m_sort);
        result.setIndex(getIndex());
        return result;
    }

    /**
     * Set wether category counts shall be calculated for the corresponding search results or not.<p> 
     * 
     * @param flag true if category counts shall be calculated for the corresponding search results or false if not
     */
    public void setCalculateCategories(boolean flag) {

        m_calculateCategories = flag;
    }

    /**
     * Set the list of categories (strings) to this parameters. <p> 
     * 
     * @param categories the list of categories (strings) of this parameters
     */
    public void setCategories(List categories) {

        m_categories = categories;
    }

    /**
     * Sets the list of strings of names of fields to search in. <p>
     * 
     * @param fields the list of strings of names of fields to search in to set
     */
    public void setFields(List fields) {

        m_fields = fields;
    }

    /**
     * Set the name of the index to search.<p>
     * 
     * 
     * @param indexName the name of the index
     */
    public void setIndex(String indexName) {

        CmsSearchIndex index;
        if (CmsStringUtil.isNotEmpty(indexName)) {
            try {
                index = OpenCms.getSearchManager().getIndex(indexName);
                if (index == null) {
                    throw new CmsException(Messages.get().container(Messages.ERR_INDEX_NOT_FOUND_1, indexName));
                }
                setSearchIndex(index);
            } catch (Exception exc) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_INDEX_ACCESS_FAILED_1, indexName), exc);
                }
            }
        }
    }

    /**
     * Sets the query to search for. <p> 
     * 
     * The decoding here is tailored for query strings that are 
     * additionally manually utf-8 encoded at client side (javascript) to get around an 
     * issue with special chars in applications that use non- utf-8 encoding 
     * (e.g. ISO-8859-1) OpenCms applications. It is not recommended to use this with 
     * frontends that don't encode manually as characters like sole "%" (without number suffix) 
     * will cause an Exception.<p> 
     * 
     * @param query the querye to search for to set
     *
     */
    public void setQuery(String query) {

        query = CmsEncoder.decode(query);

        // for widget use the exception is thrown here to enforce the errmsg next to widget
        if (query.trim().length() < 4) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_QUERY_TOO_SHORT_1,
                new Integer(4)));
        }
        m_query = query;
    }

    /**
     * Sets the list of strings of roots to search under for the search.<p>
     * 
     * @param roots  the list of strings of roots to search under for the search to set
     */
    public void setRoots(List roots) {

        m_roots = roots;
    }

    /**
     * Set the comma separated search root names to  restrict search to.<p>
     * 
     * @param categories the comma separated category names to  restrict search to
     */
    public void setSearchCategories(String categories) {

        setCategories(CmsStringUtil.splitAsList(categories, ','));
    }

    /**
     * Set wether the content field should be searched.<p>
     * 
     * This method is a widget support for <code>{@link org.opencms.widgets.CmsCheckboxWidget}</code>.<p>
     * 
     * @param flag true if the field <code>{@link org.opencms.search.documents.I_CmsDocumentFactory#DOC_CONTENT}</code> 
     *        shall be searched - false else
     */
    public void setSearchFieldContent(boolean flag) {

        if (flag) {
            if (!m_fields.contains(I_CmsDocumentFactory.DOC_CONTENT)) {
                m_fields.add(I_CmsDocumentFactory.DOC_CONTENT);
            }
        } else {
            m_fields.remove(I_CmsDocumentFactory.DOC_CONTENT);
        }
    }

    /**
     * Set wether the description field should be searched.<p>
     * 
     * This method is a widget support for <code>{@link org.opencms.widgets.CmsCheckboxWidget}</code>.<p>
     * 
     * @param flag true if the field <code>{@link org.opencms.search.documents.I_CmsDocumentFactory#DOC_DESCRIPTION}</code> 
     *        shall be searched - false else
     */
    public void setSearchFieldDescription(boolean flag) {

        if (flag) {
            if (!m_fields.contains(I_CmsDocumentFactory.DOC_DESCRIPTION)) {
                m_fields.add(I_CmsDocumentFactory.DOC_DESCRIPTION);
            }
        } else {
            m_fields.remove(I_CmsDocumentFactory.DOC_DESCRIPTION);
        }
    }

    /**
     * Set wether the title field should be searched.<p>
     * 
     * This method is a widget support for <code>{@link org.opencms.widgets.CmsCheckboxWidget}</code>.<p>
     * 
     * @param flag true if the field <code>{@link org.opencms.search.documents.I_CmsDocumentFactory#DOC_KEYWORDS}</code> 
     *        shall be searched - false else
     */
    public void setSearchFieldKeywords(boolean flag) {

        if (flag) {
            if (!m_fields.contains(I_CmsDocumentFactory.DOC_KEYWORDS)) {
                m_fields.add(I_CmsDocumentFactory.DOC_KEYWORDS);
            }
        } else {
            m_fields.remove(I_CmsDocumentFactory.DOC_KEYWORDS);
        }
    }

    /**
     * Set wether the meta field should be searched.<p>
     * 
     * This method is a widget support for <code>{@link org.opencms.widgets.CmsCheckboxWidget}</code>.<p>
     * 
     * @param flag true if the field <code>{@link org.opencms.search.documents.I_CmsDocumentFactory#DOC_META}</code> 
     *        shall be searched - false else
     */
    public void setSearchFieldMeta(boolean flag) {

        if (flag) {
            if (!m_fields.contains(I_CmsDocumentFactory.DOC_META)) {
                m_fields.add(I_CmsDocumentFactory.DOC_META);
            }
        } else {
            m_fields.remove(I_CmsDocumentFactory.DOC_META);
        }
    }

    /**
     * Set wether the title field should be searched.<p>
     * 
     * This method is a widget support for <code>{@link org.opencms.widgets.CmsCheckboxWidget}</code>.<p>
     * 
     * @param flag true if the field <code>{@link org.opencms.search.documents.I_CmsDocumentFactory#DOC_TITLE_INDEXED}</code> 
     *        shall be searched - false else
     */
    public void setSearchFieldTitle(boolean flag) {

        if (flag) {
            if (!m_fields.contains(I_CmsDocumentFactory.DOC_TITLE_INDEXED)) {
                m_fields.add(I_CmsDocumentFactory.DOC_TITLE_INDEXED);
            }
        } else {
            m_fields.remove(I_CmsDocumentFactory.DOC_TITLE_INDEXED);
        }
    }

    /**
     * Sets the search index to use for the search. <p>
     * 
     * @param index the search index to use for the search to set.
     * 
     * @throws CmsIllegalArgumentException if null is given as argument 
     */
    public void setSearchIndex(CmsSearchIndex index) throws CmsIllegalArgumentException {

        if (index == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INDEX_NULL_0));
        }
        m_index = index;
    }

    /**
     * Set the search page to display. <p>
     * 
     * @param page the search page to display
     */
    public void setSearchPage(int page) {

        m_page = page;
    }

    /**
     * Set the comma separated search root names to  restrict search to.<p>
     * 
     * @param rootNameList the comma separated search root names to  restrict search to
     */
    public void setSearchRoots(String rootNameList) {

        m_roots = CmsStringUtil.splitAsList(rootNameList, ',');
    }

    /**
     * Set the instance that defines the sort order for search results. 
     * 
     * @param sortOrder the instance that defines the sort order for search results to set
     */
    public void setSort(Sort sortOrder) {

        m_sort = sortOrder;
    }

    /** 
     * Sets the internal member of type <code>{@link Sort}</code> according to 
     * the given sort name. <p>
     * 
     * For a list of valid sort names, please see <code>{@link #SORT_NAMES}</code>.<p>
     * 
     * @param sortName the name of the sort to use
     * 
     * @see #SORT_NAMES
     */
    public void setSortName(String sortName) {

        if (sortName.equals(SORT_NAMES[1])) {
            m_sort = SORT_DATE_CREATED;
        } else if (sortName.equals(SORT_NAMES[2])) {
            m_sort = SORT_DATE_LASTMODIFIED;
        } else if (sortName.equals(SORT_NAMES[3])) {
            m_sort = SORT_TITLE;
        } else {
            m_sort = SORT_DEFAULT;
        }
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
        if (m_sort == CmsSearchParameters.SORT_DEFAULT) {
            result.append("default");
        } else if (m_sort == CmsSearchParameters.SORT_TITLE) {
            result.append("title");
        } else if (m_sort == CmsSearchParameters.SORT_DATE_CREATED) {
            result.append("date-created");
        } else if (m_sort == CmsSearchParameters.SORT_DATE_LASTMODIFIED) {
            result.append("date-lastmodified");
        } else {
            result.append("unknown");
        }
        result.append("]");
        return result.toString();
    }

    private String toSeparatedString(List stringList, char c) {

        StringBuffer result = new StringBuffer();
        Iterator it = stringList.iterator();
        while (it.hasNext()) {
            result.append(it.next());
            if (it.hasNext()) {
                result.append(c);
            }
        }
        return result.toString();
    }
}
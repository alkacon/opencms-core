/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGallerySearchObject.java,v $
 * Date   : $Date: 2010/03/19 10:11:54 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.shared;

import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This bean represents the current search object.<p>
 * 
 * The search object collects the current parameters which are used for the search and
 * contains the search results for the current search parameters.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsGallerySearchObject implements IsSerializable {

    /** The list of selected categories ids (path). */
    private ArrayList<String> m_categories = new ArrayList<String>();

    /** The list of selected galleries ids (path). */
    private ArrayList<String> m_galleries = new ArrayList<String>();

    /** The selected locale for search. */
    private String m_locale;

    // TODO: define somewhere the default value
    /** The number of search results to be display pro page. */
    private int m_machesPerPage = 12;

    /** The current search result page. */
    private int m_page;

    /** The search query string. */
    private String m_query = "";

    /** The path to the selected resource. */
    private String m_resourcePath = "";

    /** The number of all search results. */
    private int m_resultCount;

    /** The results to display in the list of search results. */
    private List<CmsListInfoBean> m_results;

    /** The sort order of the search result. */
    private String m_sortOrder;

    /** The sort order of the search result. */
    private String m_tabId;

    /** The list of the resource types ids (resource type id). */
    private ArrayList<String> m_typeNames = new ArrayList<String>();

    /**
     * Empty default constructor. <p>
     */
    public CmsGallerySearchObject() {

        // empty constructor
    }

    /**
     * Constructor of the search object.<p>
     * 
     * The constructor copies the content of the provided parameter to the current bean.
     * 
     * @param searchObj a search object with content
     */
    public CmsGallerySearchObject(CmsGallerySearchObject searchObj) {

        setTypeNames(searchObj.getTypeNames());
        setGalleries(searchObj.getGalleries());
        setCategories(searchObj.getCategories());
        setQuery(searchObj.getQuery());
        setLocale(searchObj.getLocale());
        setMachesPerPage(searchObj.getMachesPerPage());
        setSortOrder(searchObj.getSortOrder());

    }

    /**
     * Returns the list of the available categories.<p>
     *
     * @return the categories
     */
    public ArrayList<String> getCategories() {

        return m_categories;
    }

    /**
     * Returns the list of the available galleries.<p>
     *
     * @return the galleries
     */
    public ArrayList<String> getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the search locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the number of maches per search page.<p>
     *
     * @return the machesPerPage
     */
    public int getMachesPerPage() {

        return m_machesPerPage;
    }

    /**
     * Returns the page.<p>
     *
     * @return the page
     */
    public int getPage() {

        return m_page;
    }

    /**
     * Returns the search query string.<p>
     *
     * @return the query
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Returns the path to the selected resource in the current search.<p>
     *
     * @return the path to the selected resource
     */
    public String getResourcePath() {

        return m_resourcePath;
    }

    /**
     * Returns the resultCount.<p>
     *
     * @return the resultCount
     */
    public int getResultCount() {

        return m_resultCount;
    }

    /**
     * Returns the results.<p>
     *
     * @return the results
     */
    public List<CmsListInfoBean> getResults() {

        return m_results;
    }

    /**
     * Returns the sort order of the search results.<p>
     *
     * @return the sortOrder
     */
    public String getSortOrder() {

        return m_sortOrder;
    }

    /**
     * Returns the tabId.<p>
     *
     * @return the tabId
     */
    public String getTabId() {

        return m_tabId;
    }

    /**
     * Returns the list of the available type.<p>
     *
     * @return the typeNames
     */
    public ArrayList<String> getTypeNames() {

        return m_typeNames;
    }

    /**
     * Sets the categories.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(ArrayList<String> categories) {

        m_categories = categories;
    }

    /**
     * Sets the galleries.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(ArrayList<String> galleries) {

        m_galleries = galleries;
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(String locale) {

        m_locale = locale;
    }

    /**
     * Sets the machesPerPage.<p>
     *
     * @param machesPerPage the machesPerPage to set
     */
    public void setMachesPerPage(int machesPerPage) {

        m_machesPerPage = machesPerPage;
    }

    /**
     * Sets the page.<p>
     *
     * @param page the page to set
     */
    public void setPage(int page) {

        m_page = page;
    }

    /**
     * Sets the query.<p>
     *
     * @param query the query to set
     */
    public void setQuery(String query) {

        m_query = query;
    }

    /**
     * Sets the resourcePath.<p>
     *
     * @param resourcePath the resourcePath to set
     */
    public void setResourcePath(String resourcePath) {

        m_resourcePath = resourcePath;
    }

    /**
     * Sets the resultCount.<p>
     *
     * @param resultCount the resultCount to set
     */
    public void setResultCount(int resultCount) {

        m_resultCount = resultCount;
    }

    /**
     * Sets the results.<p>
     *
     * @param results the results to set
     */
    public void setResults(List<CmsListInfoBean> results) {

        m_results = results;
    }

    /**
     * Sets the sortOrder.<p>
     *
     * @param sortOrder the sortOrder to set
     */
    public void setSortOrder(String sortOrder) {

        m_sortOrder = sortOrder;
    }

    /**
     * Sets the tabId.<p>
     *
     * @param tabId the tabId to set
     */
    public void setTabId(String tabId) {

        m_tabId = tabId;
    }

    /**
     * Sets the typeNames.<p>
     *
     * @param typeNames the typeNames to set
     */
    public void setTypeNames(ArrayList<String> typeNames) {

        m_typeNames = typeNames;
    }

}

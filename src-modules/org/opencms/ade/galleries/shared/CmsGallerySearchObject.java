/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGallerySearchObject.java,v $
 * Date   : $Date: 2010/04/28 10:25:47 $
 * Version: $Revision: 1.4 $
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

import org.opencms.ade.galleries.client.CmsGalleryProvider;
import org.opencms.util.CmsStringUtil;

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
 * @version $Revision: 1.4 $ 
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
    private int m_page = 1;

    /** The search query string. */
    private String m_query = "";

    /** The path to the selected resource. */
    private String m_resourcePath = "";

    /** The number of all search results. */
    private int m_resultCount;

    /** The results to display in the list of search results. */
    private ArrayList<CmsResultsListInfoBean> m_results;

    /** The sort order of the search result. */
    private String m_sortOrder;

    /** The tab id to be selected by opening the gallery dialog. */
    private String m_tabId = I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name();

    /** The list of the resource types ids (resource type name). */
    private List<String> m_types = new ArrayList<String>();

    /** The default tab id to use when the gallery is opened. */
    public static final int DEFAULT_TAB_ID = 0;

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

        setTypes(searchObj.getTypes());
        setGalleries(searchObj.getGalleries());
        setCategories(searchObj.getCategories());
        setQuery(searchObj.getQuery());
        setLocale(searchObj.getLocale());
        setMachesPerPage(searchObj.getMachesPerPage());
        setSortOrder(searchObj.getSortOrder());
        setTabId(searchObj.getTabId());
        setPage(searchObj.getPage());
    }

    /**
     * Initialize the search object using data from gallery core dict.<p>
     * 
     */
    public void init() {

        // if gallery is selected write the gallery path to the gallery list
        ArrayList<String> galleries = new ArrayList<String>();
        String galleryTabId = CmsGalleryProvider.get().getGalleryTabId();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(CmsGalleryProvider.get().getGalleryPath())) {
            galleries.add(CmsGalleryProvider.get().getGalleryPath());
            galleryTabId = I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_results.name();
        }

        setGalleries(galleries);
        setTabId(galleryTabId);

        // TODO: the types should be set
        setTypes(new ArrayList<String>());
        setCategories(new ArrayList<String>());
        setQuery("");
        // TODO: set locale
        setLocale("");
        // TODO: set the default values
        setMachesPerPage(getMachesPerPage());
        setSortOrder(getSortOrder());
        setPage(getPage());
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

        if (m_galleries == null) {
            return new ArrayList<String>();
        }
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

        if (m_page < 1) {
            return 1;
        }
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
    public ArrayList<CmsResultsListInfoBean> getResults() {

        if (m_results == null) {
            return new ArrayList<CmsResultsListInfoBean>();
        }
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
    public List<String> getTypes() {

        if (m_types == null) {
            return new ArrayList<String>();
        }
        return m_types;
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
    public void setResults(ArrayList<CmsResultsListInfoBean> results) {

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
     * Sets the type names.<p>
     *
     * @param types the type names to set
     */
    public void setTypes(List<String> types) {

        m_types = types;
    }

    /**
     * Handles the click on the select box of the gallery item.<p>
     * 
     * Adds or removes the gallery path from the list with selected galleries.
     * 
     * @param gallery the gallery path of the clicked gallery
     */
    public void handleClickedGallery(String gallery) {

        if (getGalleries().contains(gallery)) {
            getGalleries().remove(gallery);
        } else {
            getGalleries().add(gallery);
        }
    }

    /**
     * Handles the click on the select box of the category item.<p>
     * 
     * Adds or removes the category path from the list with selected categories.
     * 
     * @param category the category path of the clicked category
     */
    public void handleClickedCategory(String category) {

        if (getCategories().contains(category)) {
            getCategories().remove(category);
        } else {
            getCategories().add(category);
        }
    }

    /**
     * Handles the click on the select box of the type item.<p>
     * 
     * Adds or removes the resource type name from the list with selected types.
     * 
     * @param type the type name of the clicked type
     */
    public void handleClickedType(String type) {

        if (getTypes().contains(type)) {
            getTypes().remove(type);
        } else {
            getTypes().add(type);
        }
    }

    /**
     * Checks if any search parameter are selected.<p>
     * 
     * @return true if any search parameter is selected, false if there are no search parameter selected
     */
    public boolean isNotEmpty() {

        // TODO: add the param for query
        if (getTypes().isEmpty() && getGalleries().isEmpty() && getCategories().isEmpty()) {
            return false;
        }
        return true;

    }
}
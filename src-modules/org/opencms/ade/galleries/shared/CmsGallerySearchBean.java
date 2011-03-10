/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGallerySearchBean.java,v $
 * Date   : $Date: 2011/03/10 08:44:49 $
 * Version: $Revision: 1.10 $
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

import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
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
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0.0
 */
public class CmsGallerySearchBean implements IsSerializable {

    /** The default matches per page. */
    public static final int DEFAULT_MATCHES_PER_PAGE = 20;

    /** The default tab id to use when the gallery is opened. */
    public static final int DEFAULT_TAB_ID = 0;

    /** Name of the used JS variable. */
    public static final String DICT_NAME = "cms_gallery_search_bean";

    /** The list of selected categories ids (path). */
    private List<String> m_categories = new ArrayList<String>();

    /** The end creation date criteria as long. */
    private long m_dateCreatedEnd = -1L;

    /** The start creation date criteria as long. */
    private long m_dateCreatedStart = -1L;

    /** The end modification date criteria as long. */
    private long m_dateModifiedEnd = -1L;

    /** The start modification date criteria as long. */
    private long m_dateModifiedStart = -1L;

    /** The list of selected vfs folders. */
    private List<String> m_folders = new ArrayList<String>();

    /** The list of selected galleries ids (path). */
    private List<String> m_galleries = new ArrayList<String>();

    /** The selected locale for search. */
    private String m_locale;

    /** The number of search results to be display pro page. */
    private int m_matchesPerPage;

    /** The current search result page. */
    private int m_page;

    /** The search query string. */
    private String m_query;

    /** The path to the selected resource. */
    private String m_resourcePath;

    /** The type of the selected resource. */
    private String m_resourceType;

    /** The number of all search results. */
    private int m_resultCount;

    /** The results to display in the list of search results. */
    private List<CmsResultItemBean> m_results;

    /** The sort order of the search result. */
    private String m_sortOrder;

    /** The tab id to be selected by opening the gallery dialog. */
    private String m_tabId = I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name();

    /** The list of the resource types ids (resource type name). */
    private List<String> m_types = new ArrayList<String>();

    /**
     * Empty default constructor. <p>
     */
    public CmsGallerySearchBean() {

        m_matchesPerPage = DEFAULT_MATCHES_PER_PAGE;
        m_page = 1;
    }

    /**
     * Constructor of the search object.<p>
     * 
     * The constructor copies the content of the provided parameter to the current bean.
     * 
     * @param searchObj a search object with content
     */
    public CmsGallerySearchBean(CmsGallerySearchBean searchObj) {

        setTypes(searchObj.getTypes());
        setGalleries(searchObj.getGalleries());
        setFolders(searchObj.getFolders());
        setCategories(searchObj.getCategories());
        setQuery(searchObj.getQuery());
        setLocale(searchObj.getLocale());
        setMatchesPerPage(searchObj.getMatchesPerPage());
        setSortOrder(searchObj.getSortOrder());
        setTabId(searchObj.getTabId());
        setPage(searchObj.getPage());
        setDateCreatedEnd(searchObj.getDateCreatedEnd());
        setDateCreatedStart(searchObj.getDateCreatedStart());
        setDateModifiedEnd(searchObj.getDateModifiedEnd());
        setDateModifiedStart(searchObj.getDateModifiedStart());
    }

    /**
     * Adds a category to the categories list.<p>
     * 
     * @param category the category
     */
    public void addCategory(String category) {

        if (!m_categories.contains(category)) {
            m_categories.add(category);
        }
    }

    /**
     * Adds a new VFS folder to search in.<p>
     * 
     * @param folder the folder to add 
     */
    public void addFolder(String folder) {

        m_folders.add(folder);
    }

    /**
     * Adds a gallery folder to the galleries list.<p>
     * 
     * @param gallery the gallery
     */
    public void addGallery(String gallery) {

        if (!m_galleries.contains(gallery)) {
            m_galleries.add(gallery);
        }
    }

    /**
     * Adds a type to the types list.<p>
     * 
     * @param type the type
     */
    public void addType(String type) {

        if (!m_types.contains(type)) {
            m_types.add(type);
        }
    }

    /**
     * Clears the categories list.<p>
     */
    public void clearCategories() {

        m_categories.clear();
    }

    /**
     * Clears the list of VFS folders.<p>
     */
    public void clearFolders() {

        m_folders.clear();
    }

    /**
     * Clears the full text search.<p>
     */
    public void clearFullTextSearch() {

        m_query = null;
        m_dateCreatedEnd = -1L;
        m_dateCreatedStart = -1L;
        m_dateModifiedEnd = -1L;
        m_dateModifiedStart = -1L;
    }

    /**
     * Clears the galleries list.<p>
     */
    public void clearGalleries() {

        m_galleries.clear();
    }

    /**
     * Clears the types list.<p>
     */
    public void clearTypes() {

        m_types.clear();
    }

    /**
     * Returns the list of the available categories.<p>
     *
     * @return the categories
     */
    public List<String> getCategories() {

        return m_categories;
    }

    /**
     * Returns the dateCreatedEnd.<p>
     *
     * @return the dateCreatedEnd
     */
    public long getDateCreatedEnd() {

        return m_dateCreatedEnd;
    }

    /**
     * Returns the dateCreatedStart.<p>
     *
     * @return the dateCreatedStart
     */
    public long getDateCreatedStart() {

        return m_dateCreatedStart;
    }

    /**
     * Returns the dateModifiedEnd.<p>
     *
     * @return the dateModifiedEnd
     */
    public long getDateModifiedEnd() {

        return m_dateModifiedEnd;
    }

    /**
     * Returns the dateModifiedStart.<p>
     *
     * @return the dateModifiedStart
     */
    public long getDateModifiedStart() {

        return m_dateModifiedStart;
    }

    /**
     * Returns the list of selected VFS folders.<p>
     * 
     * @return the list of selected VFS folders 
     */
    public List<String> getFolders() {

        return m_folders;
    }

    /**
     * Returns the list of the available galleries.<p>
     *
     * @return the galleries
     */
    public List<String> getGalleries() {

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
     * Returns the number of matches per search page.<p>
     *
     * @return the matchesPerPage
     */
    public int getMatchesPerPage() {

        return m_matchesPerPage;
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
     * Returns the resource type of the selected resource.<p>
     *
     * @return the resource type
     */
    public String getResourceType() {

        return m_resourceType;
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
    public List<CmsResultItemBean> getResults() {

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

        return m_types;
    }

    /**
     * Checks if there are more search items available on the next page.<p>
     * 
     * @return <code>true</code> if there are more search results available <code>false</code> otherwise
     */
    public boolean hasMore() {

        return (m_resultCount > m_page * m_matchesPerPage);
    }

    /**
     * Checks if any search parameter are selected.<p>
     * 
     * @return false if any search parameter is selected, true if there are no search parameter selected
     */
    @SuppressWarnings("unchecked")
    public boolean isEmpty() {

        List<String>[] params = new List[] {m_types, m_galleries, m_categories, m_folders};
        for (List<String> paramList : params) {
            if (!CmsCollectionUtil.isEmptyOrNull(paramList)) {
                return false;
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_query)) {
            return false;
        }
        List<Long> dates = Arrays.asList(new Long[] {
            Long.valueOf(m_dateCreatedEnd),
            Long.valueOf(m_dateCreatedStart),
            Long.valueOf(m_dateModifiedEnd),
            Long.valueOf(m_dateModifiedStart)});
        for (Long date : dates) {
            if ((date != null) || (date == Long.valueOf(-1L))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes a category from the categories list.<p>
     * 
     * @param category the category
     */
    public void removeCategory(String category) {

        m_categories.remove(category);
    }

    /**
     * Removes a folder from the folder list.<p>
     * 
     * @param folder the folder to remove
     */
    public void removeFolder(String folder) {

        m_folders.remove(folder);
    }

    /**
     * Removes a gallery folder from the galleries list.<p>
     * 
     * @param gallery the gallery
     */
    public void removeGallery(String gallery) {

        m_galleries.remove(gallery);
    }

    /**
     * Removes a type from the types list.<p>
     * 
     * @param type the type
     */
    public void removeType(String type) {

        m_types.remove(type);
    }

    /**
     * Sets the categories.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(List<String> categories) {

        m_categories = categories;
    }

    /**
     * Sets the dateCreatedEnd.<p>
     *
     * @param dateCreatedEnd the dateCreatedEnd to set
     */
    public void setDateCreatedEnd(long dateCreatedEnd) {

        m_dateCreatedEnd = dateCreatedEnd;
    }

    /**
     * Sets the dateCreatedStart.<p>
     *
     * @param dateCreatedStart the dateCreatedStart to set
     */
    public void setDateCreatedStart(long dateCreatedStart) {

        m_dateCreatedStart = dateCreatedStart;
    }

    /**
     * Sets the dateModifiedEnd.<p>
     *
     * @param dateModifiedEnd the dateModifiedEnd to set
     */
    public void setDateModifiedEnd(long dateModifiedEnd) {

        m_dateModifiedEnd = dateModifiedEnd;
    }

    /**
     * Sets the dateModifiedStart.<p>
     *
     * @param dateModifiedStart the dateModifiedStart to set
     */
    public void setDateModifiedStart(long dateModifiedStart) {

        m_dateModifiedStart = dateModifiedStart;
    }

    /** 
     * Sets the folders to search in.<p>
     * 
     * @param folders the folders
     */
    public void setFolders(List<String> folders) {

        m_folders = folders;
    }

    /**
     * Sets the galleries.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(List<String> galleries) {

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
     * Sets the matchesPerPage.<p>
     *
     * @param matchesPerPage the matchesPerPage to set
     */
    public void setMatchesPerPage(int matchesPerPage) {

        m_matchesPerPage = matchesPerPage;
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
     * Sets the resource type of the selected resource.<p>
     *
     * @param resourceType the resource type to set
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
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
    public void setResults(List<CmsResultItemBean> results) {

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

        if (types == null) {
            m_types = new ArrayList<String>();
        } else {
            m_types = types;
        }
    }
}
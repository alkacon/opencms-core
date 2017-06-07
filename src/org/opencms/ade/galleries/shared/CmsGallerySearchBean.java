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

package org.opencms.ade.galleries.shared;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This bean represents the current search object.<p>
 *
 * The search object collects the current parameters which are used for the search and
 * contains the search results for the current search parameters.
 *
 * @since 8.0.0
 */
public class CmsGallerySearchBean implements IsSerializable {

    /** The default matches per page. */
    public static final int DEFAULT_MATCHES_PER_PAGE = 40;

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

    /** Flag to disable the preview. */
    private boolean m_disablePreview;

    /** The list of selected vfs folders. */
    private Set<String> m_folders = new HashSet<String>();

    /** The list of selected galleries ids (path). */
    private List<String> m_galleries = new ArrayList<String>();

    /** Flag to indicate whether the user changed the gallery selection. */
    private boolean m_galleriesChanged;

    /** The gallery mode. */
    private GalleryMode m_galleryMode;

    /** The prefix for the key used to store the last selected gallery. */
    private String m_galleryStoragePrefix;

    /** Indicates the search exclude property should be ignored. */
    private boolean m_ignoreSearchExclude;

    /** Flag indicating if the search should include expired or unreleased resources. */
    private boolean m_includeExpired;

    /** The id of a tab which will be set after an initial (CmsGalleryDataBean) search. */
    private GalleryTabId m_initialTabId;

    /** The index of the last search results page. */
    private int m_lastPage;

    /** The selected locale for search. */
    private String m_locale;

    /** The number of search results to be display pro page. */
    private int m_matchesPerPage;

    /** The original gallery data for which this search bean was created. */
    private CmsGalleryDataBean m_originalGalleryData;

    /** The current search result page. */
    private int m_page;

    /** The search query string. */
    private String m_query;

    /** The gallery reference path. */
    private String m_referencePath;

    /** The path to the selected resource. */
    private String m_resourcePath;

    /** The type of the selected resource. */
    private String m_resourceType;

    /** The number of all search results. */
    private int m_resultCount;

    /** The results to display in the list of search results. */
    private List<CmsResultItemBean> m_results;

    /** The search scope. */
    private CmsGallerySearchScope m_scope;

    /** The real list of types to be used for the search on the server. */
    private List<String> m_serverSearchTypes = new ArrayList<String>();

    /** The sitemap preload data. */
    private CmsSitemapEntryBean m_sitemapPreloadData;

    /** The sort order of the search result. */
    private String m_sortOrder;

    /** The tab id to be selected by opening the gallery dialog. */
    private String m_tabId = I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name();

    /** The list of the resource types ids (resource type name). */
    private List<String> m_types = new ArrayList<String>();

    /** The VFS tree preload data. */
    private CmsVfsEntryBean m_vfsPreloadData;

    /**
     * Empty default constructor. <p>
     */
    public CmsGallerySearchBean() {

        m_matchesPerPage = DEFAULT_MATCHES_PER_PAGE;
        m_page = 1;
        // default sorting by date last modified
        m_sortOrder = SortParams.dateLastModified_desc.name();
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
        setLastPage(searchObj.getLastPage());
        setDateCreatedEnd(searchObj.getDateCreatedEnd());
        setDateCreatedStart(searchObj.getDateCreatedStart());
        setDateModifiedEnd(searchObj.getDateModifiedEnd());
        setDateModifiedStart(searchObj.getDateModifiedStart());
        setScope(searchObj.getScope());
        setIncludeExpired(searchObj.isIncludeExpired());
        setIgnoreSearchExclude(searchObj.isIgnoreSearchExclude());
        setGalleryMode(searchObj.getGalleryMode());
        setGalleryStoragePrefix(searchObj.getGalleryStoragePrefix());
        setServerSearchTypes(searchObj.getServerSearchTypes());
        setOriginalGalleryData(searchObj.getOriginalGalleryData());
    }

    /**
     * Creates the key used to store the last selected gallery.<p>
     *
     * @param prefix the prefix for the key
     * @param referenceType the type name of the reference resource
     *
     * @return the key to store the last selected gallery
     */
    public static String getGalleryStorageKey(String prefix, String referenceType) {

        return prefix + "#" + referenceType;
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
    public Set<String> getFolders() {

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
     * Gets the gallery mode.<p>
     *
     * @return the gallery mode
     */
    public GalleryMode getGalleryMode() {

        return m_galleryMode;
    }

    /**
     * Gets the key used to store the last selected gallery.<p>
     *
     * @return the key used to store the last selected gallery
     */
    public String getGalleryStoragePrefix() {

        return m_galleryStoragePrefix;
    }

    /**
     * Gets the initial tab id.<p>
     *
     * @return the initial tab id
     */
    public GalleryTabId getInitialTabId() {

        return m_initialTabId;
    }

    /**
     * Gets the index of the last search results page.<p>
     *
     * @return the index of the last search results page
     */
    public int getLastPage() {

        return m_lastPage;
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
     * Returns the original gallery data.<p>
     *
     * @return the original gallery data
     */
    public CmsGalleryDataBean getOriginalGalleryData() {

        return m_originalGalleryData;
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
     * Gets the gallery reference path.<p>
     *
     * @return the gallery reference path
     */
    public String getReferencePath() {

        return m_referencePath;
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
     * Gets the search scope.<p>
     *
     * @return the search scope
     */
    public CmsGallerySearchScope getScope() {

        return m_scope;
    }

    /**
     * Gets the server search types.<p>
     *
     * These are the types which are actually used for the search on the server, rather than the types
     * which are checked in the types tab. The lists are different, for example, if the user hasn't selected any
     * types.
     *
     * @return the server search types
     */
    public List<String> getServerSearchTypes() {

        return m_serverSearchTypes;
    }

    /**
     * Gets the sitemap preload data.<p>
     *
     * @return the sitemap preload data
     */
    public CmsSitemapEntryBean getSitemapPreloadData() {

        return m_sitemapPreloadData;
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
     * Gets the VFS preload data.<p>
     *
     * @return the VFS preload data
     */
    public CmsVfsEntryBean getVfsPreloadData() {

        return m_vfsPreloadData;
    }

    /**
     * Checks if there are more search items available on the next page.<p>
     *
     * @return <code>true</code> if there are more search results available <code>false</code> otherwise
     */
    public boolean hasMore() {

        return (m_resultCount > (m_page * m_matchesPerPage));
    }

    /**
     * Checks if the gallery selection was changed by the user.<p>
     *
     * @return true if the gallery selection was changed
     */
    public boolean haveGalleriesChanged() {

        return m_galleriesChanged;
    }

    /**
     * Returns true if no preview should be shown for the search result.<p>
     *
     * @return true if no preview should be shown
     */
    public boolean isDisablePreview() {

        return m_disablePreview;
    }

    /**
     * Checks if any search parameter are selected.<p>
     *
     * @return false if any search parameter is selected, true if there are no search parameter selected
     */
    @SuppressWarnings("unchecked")
    public boolean isEmpty() {

        List<String>[] params = new List[] {m_types, m_galleries, m_categories, new ArrayList<String>(m_folders)};
        for (List<String> paramList : params) {
            if ((paramList != null) && !paramList.isEmpty()) {
                return false;
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_query)) {
            return false;
        }
        List<Long> dates = Arrays.asList(
            new Long[] {
                Long.valueOf(m_dateCreatedEnd),
                Long.valueOf(m_dateCreatedStart),
                Long.valueOf(m_dateModifiedEnd),
                Long.valueOf(m_dateModifiedStart)});
        for (Long date : dates) {
            if ((date != null) && (!date.equals(Long.valueOf(-1L)))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the search exclude property ignore flag.<p>
     *
     * @return the search exclude property ignore flag
     */
    public boolean isIgnoreSearchExclude() {

        return m_ignoreSearchExclude;
    }

    /**
     * Returns if the search should include expired or unreleased resources.<p>
     *
     * @return <code>true</code> if the search should include expired or unreleased resources
     */
    public boolean isIncludeExpired() {

        return m_includeExpired;
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
     * Sets the 'disable preview' flag.<p>
     *
     * @param disablePreview true if the preview for the search result should not be shown
     */
    public void setDisablePreview(boolean disablePreview) {

        m_disablePreview = disablePreview;
    }

    /**
     * Sets the folders to search in.<p>
     *
     * @param folders the folders
     */
    public void setFolders(Set<String> folders) {

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
     * Sets the "galleries changed" flag.<p>
     *
     * @param changed the new flag value
     */
    public void setGalleriesChanged(boolean changed) {

        m_galleriesChanged = changed;
    }

    /**
     * Sets the gallery mode.<p>
     *
     * @param galleryMode the gallery mode to set
     */
    public void setGalleryMode(GalleryMode galleryMode) {

        m_galleryMode = galleryMode;
    }

    /**
     * Sets the prefix of the key used to store the last selected gallery.<p>
     *
     * @param prefix the prefix of the key used to store the last selected gallery
     */
    public void setGalleryStoragePrefix(String prefix) {

        m_galleryStoragePrefix = prefix;
    }

    /**
     * Sets the search exclude property ignore flag.<p>
     *
     * @param excludeForPageEditor the search exclude property ignore flag
     */
    public void setIgnoreSearchExclude(boolean excludeForPageEditor) {

        m_ignoreSearchExclude = excludeForPageEditor;
    }

    /**
     * Sets if the search should include expired or unreleased resources.<p>
     *
     * @param includeExpired if the search should include expired or unreleased resources
     */
    public void setIncludeExpired(boolean includeExpired) {

        m_includeExpired = includeExpired;
    }

    /**
     * Sets the initial tab id.<p>
     *
     * @param initialTabId the initial tab id
     */
    public void setInitialTabId(GalleryTabId initialTabId) {

        m_initialTabId = initialTabId;
    }

    /**
     * Sets the index of the last search result page.<p>
     *
     * @param lastPage the index of the last search result page
     */
    public void setLastPage(int lastPage) {

        m_lastPage = lastPage;
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
     * Sets the original gallery data.<p>
     *
     * @param originalGalleryData the original gallery data to set
     */
    public void setOriginalGalleryData(CmsGalleryDataBean originalGalleryData) {

        m_originalGalleryData = originalGalleryData;
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
     * Sets the gallery reference path.<p>
     *
     * @param referencePath the gallery reference path
     */
    public void setReferencePath(String referencePath) {

        m_referencePath = referencePath;
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
     * Sets the search scope.<p>
     *
     * @param scope the search scope
     */
    public void setScope(CmsGallerySearchScope scope) {

        m_scope = scope;
    }

    /**
     * Sets the server search types.<p>
     *
     * @param types the server search types
     */
    public void setServerSearchTypes(List<String> types) {

        m_serverSearchTypes = types;
    }

    /**
     * Sets the sitemap preload data.<p>
     *
     * @param preloadData the sitemap preload data
     */
    public void setSitemapPreloadData(CmsSitemapEntryBean preloadData) {

        m_sitemapPreloadData = preloadData;
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

    /**
     * Sets the VFS tree preload data.<p>
     *
     * @param preloadData the VFS tree preload data
     */
    public void setVfsPreloadData(CmsVfsEntryBean preloadData) {

        m_vfsPreloadData = preloadData;
    }
}
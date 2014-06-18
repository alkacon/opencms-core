/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.search.galleries;

import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * Parameters used for the gallery search index.<p>
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchParameters {

    /**
     * Helper class to store a time range.<p>
     */
    public class CmsGallerySearchTimeRange {

        /** The end time of the time range. */
        private long m_endTime;

        /** The start time of the time range. */
        private long m_startTime;

        /**
         * Default constructor.<p>
         * 
         * This will create an object where the start date is equal to 
         * {@link Long#MIN_VALUE} and the end date is equal to {@link Long#MAX_VALUE}.<p>
         */
        public CmsGallerySearchTimeRange() {

            m_startTime = Long.MIN_VALUE;
            m_endTime = Long.MAX_VALUE;
        }

        /**
         * Constructor with start and end time.<p>
         * 
         * @param startTime the start time of the time range
         * @param endTime the end time of the time range
         */
        public CmsGallerySearchTimeRange(long startTime, long endTime) {

            m_startTime = startTime;
            m_endTime = endTime;
        }

        /**
         * Returns the end time of the time range.<p>
         * 
         * @return the end time of the time range
         */
        public long getEndTime() {

            return m_endTime;
        }

        /**
         * Returns the start time of the time range.<p>
         * 
         * @return the start time of the time range
         */
        public long getStartTime() {

            return m_startTime;
        }
    }

    /** Sort parameter constants. */
    public enum CmsGallerySortParam {

        /** Sort by date created ascending. */
        dateCreated_asc,

        /** Sort by date created descending. */
        dateCreated_desc,

        /** Sort date expired ascending. */
        dateExpired_asc,

        /** Sort date expired descending. */
        dateExpired_desc,

        /** Sort by date modified ascending. */
        dateLastModified_asc,

        /** Sort by date modified descending. */
        dateLastModified_desc,

        /** Sort date released ascending. */
        dateReleased_asc,

        /** Sort date released descending. */
        dateReleased_desc,

        /** Sort by length ascending. */
        length_asc,

        /** Sort by length descending. */
        length_desc,

        /** Sort by VFS root path ascending. */
        path_asc,

        /** Sort by VFS root path descending. */
        path_desc,

        /** Sort by score ascending. */
        score,

        /** Sort state ascending. */
        state_asc,

        /** Sort state descending. */
        state_desc,

        /** Sort by title ascending. */
        title_asc,

        /** Sort by title ascending. */
        title_desc,

        /** Sort by type ascending. */
        type_asc,

        /** Sort by type descending. */
        type_desc,

        /** Sort created by ascending. */
        userCreated_asc,

        /** Sort created by descending. */
        userCreated_desc,

        /** Sort modified by ascending. */
        userLastModified_asc,

        /** Sort modified by descending. */
        userLastModified_desc;

        /** The default sort parameter. */
        public static final CmsGallerySortParam DEFAULT = title_asc;
    }

    /** Sort result documents by date of creation ascending. */
    public static final Sort SORT_DATE_CREATED_ASC = new Sort(new SortField(
        CmsSearchField.FIELD_DATE_CREATED,
        SortField.Type.STRING,
        false));

    /** Sort result documents by date of creation descending. */
    public static final Sort SORT_DATE_CREATED_DESC = new Sort(new SortField(
        CmsSearchField.FIELD_DATE_CREATED,
        SortField.Type.STRING,
        true));

    /** Sort result documents by date of expiration ascending. */
    public static final Sort SORT_DATE_EXPIRED_ASC = new Sort(new SortField(
        CmsGallerySearchFieldMapping.FIELD_RESOURCE_DATE_EXPIRED,
        SortField.Type.STRING,
        false));

    /** Sort result documents by date of expiration descending. */
    public static final Sort SORT_DATE_EXPIRED_DESC = new Sort(new SortField(
        CmsGallerySearchFieldMapping.FIELD_RESOURCE_DATE_EXPIRED,
        SortField.Type.STRING,
        true));

    /** Sort result documents by date of last modification ascending. */
    public static final Sort SORT_DATE_LASTMODIFIED_ASC = new Sort(new SortField(
        CmsSearchField.FIELD_DATE_LASTMODIFIED,
        SortField.Type.STRING,
        false));

    /** Sort result documents by date of last modification descending. */
    public static final Sort SORT_DATE_LASTMODIFIED_DESC = new Sort(new SortField(
        CmsSearchField.FIELD_DATE_LASTMODIFIED,
        SortField.Type.STRING,
        true));

    /** Sort result documents by date of release ascending. */
    public static final Sort SORT_DATE_RELEASED_ASC = new Sort(new SortField(
        CmsGallerySearchFieldMapping.FIELD_RESOURCE_DATE_RELEASED,
        SortField.Type.STRING,
        false));

    /** Sort result documents by date of release descending. */
    public static final Sort SORT_DATE_RELEASED_DESC = new Sort(new SortField(
        CmsGallerySearchFieldMapping.FIELD_RESOURCE_DATE_RELEASED,
        SortField.Type.STRING,
        true));

    /** Sort result documents by resource length ascending, then score. */
    public static final Sort SORT_LENGTH_ASC = new Sort(new SortField[] {
        new SortField(CmsGallerySearchFieldMapping.FIELD_RESOURCE_LENGTH, SortField.Type.INT, false),
        SortField.FIELD_SCORE});

    /** Sort result documents by resource length descending, then score. */
    public static final Sort SORT_LENGTH_DESC = new Sort(new SortField[] {
        new SortField(CmsGallerySearchFieldMapping.FIELD_RESOURCE_LENGTH, SortField.Type.INT, true),
        SortField.FIELD_SCORE});

    /** Sort result documents by VFS path ascending. */
    public static final Sort SORT_PATH_ASC = new Sort(new SortField(
        CmsSearchField.FIELD_PATH,
        SortField.Type.STRING,
        false));

    /** Sort result documents by VFS path descending. */
    public static final Sort SORT_PATH_DESC = new Sort(new SortField(
        CmsSearchField.FIELD_PATH,
        SortField.Type.STRING,
        true));

    /** Sort result documents by score. */
    public static final Sort SORT_SCORE = Sort.RELEVANCE;

    /** Sort result documents by resource state ascending, then score. */
    public static final Sort SORT_STATE_ASC = new Sort(new SortField[] {
        new SortField(CmsGallerySearchFieldMapping.FIELD_RESOURCE_STATE, SortField.Type.INT, false),
        SortField.FIELD_SCORE});

    /** Sort result documents by resource state descending, then score. */
    public static final Sort SORT_STATE_DESC = new Sort(new SortField[] {
        new SortField(CmsGallerySearchFieldMapping.FIELD_RESOURCE_STATE, SortField.Type.INT, true),
        SortField.FIELD_SCORE});

    /** Sort result documents by title ascending, then score. */
    public static final Sort SORT_TITLE_ASC = new Sort(new SortField[] {
        new SortField(CmsSearchField.FIELD_TITLE, SortField.Type.STRING, false),
        SortField.FIELD_SCORE});

    /** Sort result documents by title descending, then score. */
    public static final Sort SORT_TITLE_DESC = new Sort(new SortField[] {
        new SortField(CmsSearchField.FIELD_TITLE, SortField.Type.STRING, true),
        SortField.FIELD_SCORE});

    /** Sort result documents by resource type ascending, then score. */
    public static final Sort SORT_TYPE_ASC = new Sort(new SortField[] {
        new SortField(CmsSearchField.FIELD_TYPE, SortField.Type.STRING, false),
        SortField.FIELD_SCORE});

    /** Sort result documents by resource type descending, then score. */
    public static final Sort SORT_TYPE_DESC = new Sort(new SortField[] {
        new SortField(CmsSearchField.FIELD_TYPE, SortField.Type.STRING, true),
        SortField.FIELD_SCORE});

    /** Sort result documents by date user who created ascending, then date of creation. */
    public static final Sort SORT_USER_CREATED_ASC = new Sort(new SortField[] {
        new SortField(CmsGallerySearchFieldMapping.FIELD_RESOURCE_USER_CREATED, SortField.Type.STRING, false),
        new SortField(CmsSearchField.FIELD_DATE_CREATED, SortField.Type.STRING, true)});

    /** Sort result documents by date user who created descending, then date of creation. */
    public static final Sort SORT_USER_CREATED_DESC = new Sort(new SortField[] {
        new SortField(CmsGallerySearchFieldMapping.FIELD_RESOURCE_USER_CREATED, SortField.Type.STRING, true),
        new SortField(CmsSearchField.FIELD_DATE_CREATED, SortField.Type.STRING, true)});

    /** Sort result documents by date user who did the last modification ascending, then date of last modification. */
    public static final Sort SORT_USER_LASTMODIFIED_ASC = new Sort(new SortField[] {
        new SortField(CmsGallerySearchFieldMapping.FIELD_RESOURCE_USER_LASTMODIFIED, SortField.Type.STRING, false),
        new SortField(CmsSearchField.FIELD_DATE_LASTMODIFIED, SortField.Type.STRING, true)});

    /** Sort result documents by date user who did the last modification descending, then date of last modification. */
    public static final Sort SORT_USER_LASTMODIFIED_DESC = new Sort(new SortField[] {
        new SortField(CmsGallerySearchFieldMapping.FIELD_RESOURCE_USER_LASTMODIFIED, SortField.Type.STRING, true),
        new SortField(CmsSearchField.FIELD_DATE_LASTMODIFIED, SortField.Type.STRING, true)});

    /** The categories to search in. */
    private List<String> m_categories;

    /** The container types to search in. */
    private List<String> m_containerTypes;

    /** The time range for the date of resource creation to consider in the search. */
    private CmsGallerySearchTimeRange m_dateCreatedTimeRange;

    /** The time range for the date of resource last modification to consider in the search. */
    private CmsGallerySearchTimeRange m_dateLastModifiedTimeRange;

    /** The list of search index fields to search in. */
    private List<String> m_fields;

    /** The list of folders to search in. */
    private List<String> m_folders;

    /** The galleries to search in. */
    private List<String> m_galleries;

    /** Indicates the search exclude property should be ignored. */
    private boolean m_ignoreSearchExclude;

    /** The locale for the search. */
    private String m_locale;

    /** The number of search results per page. */
    private int m_matchesPerPage;

    /** The sitemap reference path. */
    private String m_referencePath;

    /** The resource types to search for. */
    private List<String> m_resourceTypes;

    /** The requested page of the result. */
    private int m_resultPage;

    /** The gallery search scope. */
    private CmsGallerySearchScope m_scope;

    /** The sort order for the search result. */
    private CmsGallerySortParam m_sortOrder;

    /** Search words to search for. */
    private String m_words;

    /**
     * Default constructor.<p>
     */
    public CmsGallerySearchParameters() {

        m_resultPage = 1;
        m_matchesPerPage = 10;
    }

    /**
     * Returns the categories that have been included in the search.<p>
     *
     * If no categories have been set, then <code>null</code> is returned.<p>
     * 
     * @return the categories that have been included in the search
     */
    public List<String> getCategories() {

        return m_categories;
    }

    /**
     * Returns the container types that have been included in the search.<p>
     *
     * @return the container types that have been included in the search
     */
    public List<String> getContainerTypes() {

        return m_containerTypes;
    }

    /**
     * Returns the time range for the date of creation that has been used for the search result.<p>
     * 
     * In case this time range has not been set, this will return an object 
     * where the start date is equal to {@link Long#MIN_VALUE} and the end date is equal to {@link Long#MAX_VALUE}.<p>
     * 
     * @return the time range for the date of creation that has been used for the search result
     */
    public CmsGallerySearchTimeRange getDateCreatedRange() {

        if (m_dateCreatedTimeRange == null) {
            m_dateCreatedTimeRange = new CmsGallerySearchTimeRange();
        }
        return m_dateCreatedTimeRange;
    }

    /**
     * Returns the time range for the date of last modification that has been used for the search result.<p>
     * 
     * In case this time range has not been set, this will return an object 
     * where the start date is equal to {@link Long#MIN_VALUE} and the end date is equal to {@link Long#MAX_VALUE}.<p>
     * 
     * @return the time range for the date of last modification that has been used for the search result
     */
    public CmsGallerySearchTimeRange getDateLastModifiedRange() {

        if (m_dateLastModifiedTimeRange == null) {
            m_dateLastModifiedTimeRange = new CmsGallerySearchTimeRange();
        }
        return m_dateLastModifiedTimeRange;
    }

    /**
     * Returns the list of the names of the fields to search in.<p>
     *
     * If this has not been set, then the default fields defined in
     * {@link CmsSearchIndex#DOC_META_FIELDS} are used as default.<p>
     *
     * @return the list of the names of the fields to search in
     */
    public List<String> getFields() {

        if (m_fields == null) {
            setFields(Arrays.asList(CmsSearchIndex.DOC_META_FIELDS));
        }
        return m_fields;
    }

    /**
     * Returns the list of folders to search in.<p>
     * 
     * @return a list of paths of VFS folders
     */
    public List<String> getFolders() {

        return m_folders;
    }

    /**
     * Returns the galleries that have been included in the search.<p>
     *
     * If no galleries have been set, then <code>null</code> is returned.<p>
     *
     * @return the galleries that have been included in the search
     */
    public List<String> getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the locale that has been used for the search.<p>
     *     
     * If no locale has been set, then <code>null</code> is returned.<p>
     *
     * @return the locale that has been used for the search
     */
    public String getLocale() {

        if (m_locale == null) {
            m_locale = CmsLocaleManager.getDefaultLocale().toString();
        }
        return m_locale;
    }

    /**
     * Returns the maximum number of matches per result page.<p>
     *
     * @return the the maximum number of matches per result page
     * 
     * @see #getMatchesPerPage()
     * @see #setResultPage(int)
     */
    public int getMatchesPerPage() {

        return m_matchesPerPage;
    }

    /**
     * Gets the reference path.<p>
     * 
     * @return the gallery reference path 
     */
    public String getReferencePath() {

        return m_referencePath;
    }

    /**
     * Returns the names of the resource types that have been included in the search result.<p>
     *
     * If no resource types have been set, then <code>null</code> is returned.<p>
     *
     * @return the names of the resource types that have been included in the search result
     */
    public List<String> getResourceTypes() {

        return m_resourceTypes;
    }

    /**
     * Returns the index of the requested result page.<p>
     * 
     * @return the index of the requested result page
     * 
     * @see #setResultPage(int)
     * @see #getMatchesPerPage()
     * @see #setMatchesPerPage(int)
     */
    public int getResultPage() {

        return m_resultPage;
    }

    /**
     * The gallery search scope.<p>
     * 
     * @return the gallery search scope 
     */
    public CmsGallerySearchScope getScope() {

        if (m_scope == null) {
            return OpenCms.getWorkplaceManager().getGalleryDefaultScope();
        }
        return m_scope;
    }

    /**
     * Returns the words (terms) that have been used for the full text search.<p>
     * 
     * If no search words have been set, then <code>null</code> is returned.<p>
     *
     * @return the words (terms) that have been used for the full text search
     */
    public String getSearchWords() {

        return m_words;
    }

    /** 
     * Returns the Lucene sort indicated by the selected sort order.<p> 
     * 
     * @return the Lucene sort indicated by the selected sort order
     * 
     * @see #getSortOrder()
     */
    public Sort getSort() {

        switch (getSortOrder()) {
            case dateCreated_asc:
                return SORT_DATE_CREATED_ASC;
            case dateCreated_desc:
                return SORT_DATE_CREATED_DESC;
            case dateExpired_asc:
                return SORT_DATE_EXPIRED_ASC;
            case dateExpired_desc:
                return SORT_DATE_EXPIRED_DESC;
            case dateLastModified_asc:
                return SORT_DATE_LASTMODIFIED_ASC;
            case dateLastModified_desc:
                return SORT_DATE_LASTMODIFIED_DESC;
            case dateReleased_asc:
                return SORT_DATE_RELEASED_ASC;
            case dateReleased_desc:
                return SORT_DATE_RELEASED_DESC;
            case length_asc:
                return SORT_LENGTH_ASC;
            case length_desc:
                return SORT_LENGTH_DESC;
            case path_asc:
                return SORT_PATH_ASC;
            case path_desc:
                return SORT_PATH_DESC;
            case score:
                return SORT_SCORE;
            case state_asc:
                return SORT_STATE_ASC;
            case state_desc:
                return SORT_STATE_DESC;
            case title_asc:
                return getTitleSort(getLocale(), false);
            case title_desc:
                return getTitleSort(getLocale(), true);
            case type_asc:
                return SORT_TYPE_ASC;
            case type_desc:
                return SORT_TYPE_DESC;
            case userCreated_asc:
                return SORT_USER_CREATED_ASC;
            case userCreated_desc:
                return SORT_USER_CREATED_DESC;
            case userLastModified_asc:
                return SORT_USER_LASTMODIFIED_ASC;
            case userLastModified_desc:
                return SORT_USER_LASTMODIFIED_DESC;
            default:
                return SORT_TITLE_ASC;
        }
    }

    /**
     * Returns the sort order that has been used in the search.<p>
     * 
     * If the sort parameter has not been set the default sort order 
     * defined by {@link CmsGallerySortParam#DEFAULT} is used.<p>
     * 
     * @return the sort order that has been used in the search
     */
    public CmsGallerySortParam getSortOrder() {

        if (m_sortOrder == null) {

            m_sortOrder = CmsGallerySortParam.DEFAULT;
        }
        return m_sortOrder;
    }

    /**
     * Returns a sort for a localized title.<p>
     * 
     * @param locale the locale to sort with
     * @param desc indicates if the sort should be descending
     * 
     * @return a sort for a localized title
     */
    public Sort getTitleSort(String locale, boolean desc) {

        // for saving performance and sorting case-insensitive
        // the un-stored title field should be used
        String titleName = CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_SORT_TITLE, locale);
        return new Sort(new SortField[] {new SortField(titleName, SortField.Type.STRING, desc), SortField.FIELD_SCORE});
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
     * Sets the categories for the search.<p>
     *
     * Results are found only if they are contained in at least one of the given categories.
     *
     * @param categories the categories to set
     */
    public void setCategories(List<String> categories) {

        m_categories = categories;
    }

    /**
     * Sets the container types for the search.<p>
     *
     * Results are found only if they are compatible with one of the given container types.
     * If no container type is set, results compatible with any container will be returned in the search result.<p>
     *
     * @param containerTypes the container types to set
     */
    public void setContainerTypes(List<String> containerTypes) {

        m_containerTypes = containerTypes;
    }

    /** 
     * Sets the time range for the date of resource creation to consider in the search.<p>
     * 
     * @param startTime the start time of the time range
     * @param endTime the end time of the time range
     */
    public void setDateCreatedTimeRange(long startTime, long endTime) {

        if (m_dateCreatedTimeRange == null) {
            m_dateCreatedTimeRange = new CmsGallerySearchTimeRange(startTime, endTime);
        }
    }

    /** 
     * Sets the time range for the date of resource last modification to consider in the search.<p> 
     * 
     * @param startTime the start time of the time range
     * @param endTime the end time of the time range
     */
    public void setDateLastModifiedTimeRange(long startTime, long endTime) {

        if (m_dateLastModifiedTimeRange == null) {
            m_dateLastModifiedTimeRange = new CmsGallerySearchTimeRange(startTime, endTime);
        }
    }

    /**
     * Sets the list of the names of the fields to search in. <p>
     * 
     * @param fields the list of names of the fields to set
     */
    public void setFields(List<String> fields) {

        m_fields = fields;
    }

    /**
     * Sets the folders to search in.<p>
     * 
     * @param folders the list of VFS folders
     */
    public void setFolders(List<String> folders) {

        m_folders = folders;
    }

    /**
     * Sets the galleries for the search.<p>
     *
     * Results are found only if they are contained in one of the given galleries.
     * If no gallery is set, results from all galleries will be returned in the search result.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(List<String> galleries) {

        m_galleries = galleries;
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
     * Sets the maximum number of matches per result page.<p>
     *
     * Use this together with {@link #setResultPage(int)} in order to split the result 
     * in more than one page.<p> 
     *
     * @param matchesPerPage the the maximum number of matches per result page to set
     * 
     * @see #getMatchesPerPage()
     * @see #setResultPage(int)
     */
    public void setMatchesPerPage(int matchesPerPage) {

        m_matchesPerPage = matchesPerPage;
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
     * Sets the names of the resource types to include in the search result.<p>
     *
     * Results are found only if they resources match one of the given resource type names.
     * If no resource type name is set, all resource types will be returned in the search result.<p>
     *
     * @param resourceTypes the names of the resource types to include in the search result
     */
    public void setResourceTypes(List<String> resourceTypes) {

        m_resourceTypes = resourceTypes;
    }

    /**
     * Sets the index of the result page that should be returned.<p>
     *
     * Use this together with {@link #setMatchesPerPage(int)} in order to split the result 
     * in more than one page.<p> 
     *
     * @param resultPage the index of the result page to return
     *
     * @see #getResultPage()
     * @see #getMatchesPerPage()
     * @see #setMatchesPerPage(int)
     */
    public void setResultPage(int resultPage) {

        m_resultPage = resultPage;
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
     * Sets the locale for the search.<p>
     * 
     * Results are found only if they match the given locale.
     * If no locale is set, results for all locales will be returned in the search result.<p>
     * 
     * @param locale the locale to set
     */
    public void setSearchLocale(String locale) {

        m_locale = locale;
    }

    /**
     * Sets the words (terms) for the full text search.<p>
     *
     * Results are found only if they text extraction for the resource contains all given search words.
     * If no search word is set, all resources will be returned in the search result.<p>
     *
     * Please note that this should be a list of words separated by white spaces.
     * Simple Lucene modifiers such as (+), (-) and (*) are allowed, but anything more complex then this
     * will be removed.<p> 
     *
     * @param words the words (terms) for the full text search to set
     */
    public void setSearchWords(String words) {

        m_words = words;
    }

    /**
     * Sets the sort order for the search.<p>
     *
     * @param sortOrder the sort order to set
     */
    public void setSortOrder(CmsGallerySortParam sortOrder) {

        m_sortOrder = sortOrder;
    }

    /**
     * Wraps this parameters to the standard search parameters, so that inherited methods in the search index 
     * can be used.<p>
     * 
     * @return this parameters wrapped to the standard search parameters
     */
    protected CmsSearchParameters getCmsSearchParams() {

        CmsSearchParameters result = new CmsSearchParameters();
        result.setFields(getFields());
        result.setExcerptOnlySearchedFields(true);
        if (getSearchWords() != null) {
            result.setQuery(getSearchWords());
            result.setIgnoreQuery(false);
        } else {
            result.setIgnoreQuery(true);
        }

        return result;
    }
}
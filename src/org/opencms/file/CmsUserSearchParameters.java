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

package org.opencms.file;

import org.opencms.security.CmsOrganizationalUnit;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An object which represents search criteria for retrieving users.<p>
 *
 * @since 8.0.0
 */
public class CmsUserSearchParameters {

    /** An enum used for indicating searchable columns. */
    public enum SearchKey {
        /** Email address. */
        email, /** Full name. */
        fullName, /** Organizational unit. */
        orgUnit;
    }

    /** An enum used for indicating sort order. */
    public enum SortKey {
        /** User activation status. */
        activated, /** Email address. */
        email, /** Flags. */
        flagStatus, /** Full name: "firstname lastname (loginname)". */
        fullName, /** Last login date. */
        lastLogin, /** Login name. */
        loginName, /** Organizational unit. */
        orgUnit
    }

    /** The list of allowed OUs. */
    private List<CmsOrganizationalUnit> m_allowedOus;

    /** A collection of groups such that returned users must be in at least one of them. */
    private Collection<CmsGroup> m_anyGroups;

    /** Indicates whether the results should be retrieved in ascending/descending order. */
    private boolean m_ascending;

    /** Indicates whether search should be case sensitive. */
    private boolean m_caseSensitive = true;

    /** Indicates whether only users which match the given group's OU should be returned. */
    private boolean m_filterByGroupOu;

    /** True if non-core users should be filtered out. */
    private boolean m_filterCore;

    /** The flags to filter by. */
    private int m_flags;

    /** The group to which a resulting user must belong. */
    private CmsGroup m_group;

    /** If true, core users will not be filtered out if filtering by flags. */
    private boolean m_keepCoreUsers;

    /** A collection of groups such that returned users must be in none of them. */
    private Collection<CmsGroup> m_notAnyGroups;

    /** The group to which a resulting user may not belong. */
    private CmsGroup m_notGroup;

    /** The organizational unit to which a resulting user must belong. */
    private CmsOrganizationalUnit m_orgUnit;

    /** The results page index. */
    private int m_page;

    /** The maximum results page size. */
    private int m_pageSize = -1;

    /** If true, and an OU has been set, users of sub-OUs will also be retrieved. */
    private boolean m_recursiveOrgUnits;

    /** The search term entered by the user. */
    private String m_searchFilter;

    /** The set of search keys to use. */
    private Set<SearchKey> m_searchKeys = new HashSet<SearchKey>();

    /** The bit mask used for flag sorting. */
    private int m_sortFlags;

    /** The key which indicates by which column the table should be sorted. */
    private SortKey m_sortKey;

    /**
     * Adds a search key.<p>
     *
     * @param key the search key to add
     */
    public void addSearch(SearchKey key) {

        m_searchKeys.add(key);
    }

    /**
     * Returns the list of OUs from which users may be returned.<p>
     *
     * @return a list of OUs
     */
    public List<CmsOrganizationalUnit> getAllowedOus() {

        return m_allowedOus;
    }

    /**
     * Returns the collection of groups such that returned users must be in at least one of them.<p>
     *
     * @return a collection of groups
     */
    public Collection<CmsGroup> getAnyGroups() {

        return m_anyGroups;
    }

    /**
     * Returns the flags to filter by.<p>
     *
     * @return the flags
     */
    public int getFlags() {

        return m_flags;
    }

    /**
     * Returns the group such that users which are not in the group will be filtered out.<p>
     *
     * @return a group
     */
    public CmsGroup getGroup() {

        return m_group;
    }

    /**
     * Returns the groups whose users may not appear in the search results.<p>
     *
     * @return the groups whose users may not appear in the search results
     */
    public Collection<CmsGroup> getNotAnyGroups() {

        return m_notAnyGroups;
    }

    /**
     * Returns the group such that users not in that group will be filtered out.<p>
     *
     * @return a group
     */
    public CmsGroup getNotGroup() {

        return m_notGroup;
    }

    /**
     * Gets the organizational unit to which a user must belong.
     *
     * @return the organizational unit
     */
    public CmsOrganizationalUnit getOrganizationalUnit() {

        return m_orgUnit;
    }

    /**
     * Returns the results page index.<p>
     *
     * @return the results page index
     */
    public int getPage() {

        return m_page;
    }

    /**
     * Returns the maximum results page size.<p>
     *
     * @return the page size
     */
    public int getPageSize() {

        return m_pageSize;
    }

    /**
     * Returns the search term.
     *
     * @return the search term
     */
    public String getSearchFilter() {

        return m_searchFilter;
    }

    /**
     * Returns the set of search keys.<p>
     *
     * @return the set of search keys
     */
    public Set<SearchKey> getSearchKeys() {

        return m_searchKeys;
    }

    /**
     * Returns the bit mask to be used for ordering by flags.<p>
     *
     * @return the bit mask to be used for ordering by flags
     */
    public int getSortFlags() {

        return m_sortFlags;
    }

    /**
     * Returns the key indicating by which column the results should be sorted.<p>
     *
     * @return the sort key
     */
    public SortKey getSortKey() {

        return m_sortKey;
    }

    /**
     * If true, the results should be sorted in ascending order, else in descending order.<p>
     *
     * @return the flag indicating the sort order
     */
    public boolean isAscending() {

        return m_ascending;
    }

    /**
     * Returns true if the search filter should be case sensitive.<p>
     *
     * The default  value is <code>true</code>.
     *
     * @return true if the search filter should be case sensitive
     */
    public boolean isCaseSensitive() {

        return m_caseSensitive;
    }

    /**
     * Returns true if users of different OUs than the search group's OU will be filtered out.<p>
     *
     * @return the "filter by group OU" flag
     */
    public boolean isFilterByGroupOu() {

        return m_filterByGroupOu;
    }

    /**
     * Returns true if non-core users should be filtered out.<p>
     *
     * @return true if non-core users should be filtered out
     */
    public boolean isFilterCore() {

        return m_filterCore;
    }

    /**
     * Return true if core users should not be filtered out if filtering by flag.<p>
     *
     * @return true if core users should not be filtered out if filtering by flag.<p>
     */
    public boolean keepCoreUsers() {

        return m_keepCoreUsers;
    }

    /**
     * Returns true if sub-OU users will be returned in the result.<p>
     *
     * @return true if sub-OU users will be returned in the result
     */
    public boolean recursiveOrgUnits() {

        return m_recursiveOrgUnits;
    }

    /**
     * Sets the OUs from which users should be returned.<p>
     *
     * @param ous a list of OUs
     */
    public void setAllowedOus(List<CmsOrganizationalUnit> ous) {

        m_allowedOus = ous;
    }

    /**
     * Sets the groups such that returned users must be in at least one of them.<p>
     *
     * @param anyGroups the groups
     */
    public void setAnyGroups(Collection<CmsGroup> anyGroups) {

        m_anyGroups = anyGroups;
    }

    /**
     * Sets the case sensitivity for the search filter.<p>
     *
     * @param caseSensitive if true, the search filter will be case sensitive.
     */
    public void setCaseSensitive(boolean caseSensitive) {

        m_caseSensitive = caseSensitive;
    }

    /**
     * Sets the "filter by group OU" flag.<p>
     *
     * If the flag is true, users of a different OU than the search group's OU will be filtered out.<p>
     *
     * @param filterByGroupOu the "filter by group OU" flag
     */
    public void setFilterByGroupOu(boolean filterByGroupOu) {

        m_filterByGroupOu = filterByGroupOu;
    }

    /**
     * Enables or disables the filtering of non-core users.<p>
     *
     * @param filterCore if true, non-core users will be filtered out
     */
    public void setFilterCore(boolean filterCore) {

        m_filterCore = filterCore;
    }

    /**
     * Sets the flags to filter by.<p>
     *
     * @param flags the flags
     */
    public void setFlags(int flags) {

        m_flags = flags;
    }

    /**
     * Sets the group such that users which are not in the group will be filtered out.<p>
     *
     * @param group a group
     */
    public void setGroup(CmsGroup group) {

        m_group = group;
    }

    /**
     * If this is set to true, core users will not be filtered out if filtering by flag.<p>
     *
     * @param keepCoreUsers true if core users should not be filtered out when filtering by flag
     */
    public void setKeepCoreUsers(boolean keepCoreUsers) {

        m_keepCoreUsers = keepCoreUsers;
    }

    /**
     * Sets the groups whose users may not appear in the search results.<p>
     *
     * @param groups the groups whose users may not appear in the search results
     */
    public void setNotAnyGroups(Collection<CmsGroup> groups) {

        m_notAnyGroups = groups;
    }

    /**
     * Sets the group such that users not in that group will be filtered out.<p>
     *
     * @param group a group
     */
    public void setNotGroup(CmsGroup group) {

        m_notGroup = group;
    }

    /**
     * Sets the organizational unit to which a user must belong.<p>
     *
     *  @param ou the organizational unit
     */
    public void setOrganizationalUnit(CmsOrganizationalUnit ou) {

        m_orgUnit = ou;
    }

    /**
     * Sets the paging parameters.<p>
     *
     * @param pageSize the maximum page size
     * @param page the page index
     */
    public void setPaging(int pageSize, int page) {

        m_pageSize = pageSize;
        m_page = page;
    }

    /**
     * Enables fetching of users of sub-OUs (if an OU has been set).<p>
     *
     * @param recursive if true, enable sub-OU users in the result
     */
    public void setRecursiveOrgUnits(boolean recursive) {

        m_recursiveOrgUnits = recursive;
    }

    /**
     * Sets the search term.<p>
     *
     * @param searchFilter the search term
     */
    public void setSearchFilter(String searchFilter) {

        m_searchFilter = searchFilter;
    }

    /**
     * Sets the bit mask used when the results should be ordered by flags.<p>
     *
     * @param sortFlags the bit mask for ordering by flags
     */
    public void setSortFlags(int sortFlags) {

        m_sortFlags = sortFlags;
    }

    /**
     * Sets the sort key and order.<p>
     *
     * @param key the sort key
     * @param ascending the sort order (ascending if true, descending if false)
     */
    public void setSorting(SortKey key, boolean ascending) {

        m_sortKey = key;
        m_ascending = ascending;
    }

}

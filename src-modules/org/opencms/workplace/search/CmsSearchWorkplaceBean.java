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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.search;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.search.CmsSearchParameters;
import org.opencms.util.CmsStringUtil;

/**
 * Bean to handle search parameters in the workplace.<p>
 *
 * @since 6.3.0
 */
public class CmsSearchWorkplaceBean {

    /** The current folder. */
    private String m_currentFolder;

    /** The comma separated list of fields to search parameter value. */
    private String m_fields;

    /** The index name. */
    private String m_indexName;

    /** The creation date the resources have to have as maximum. */
    private String m_maxDateCreated;

    /** The last modification date the resources have to have as maximum. */
    private String m_maxDateLastModified;

    /** The creation date the resources have to have as minimum. */
    private String m_minDateCreated;

    /** The last modification date the resources have to have as minimum. */
    private String m_minDateLastModified;

    /** The query. */
    private String m_query;

    /** If set, will restrict the search to the current folder. */
    private boolean m_restrictSearch;

    /** The sort Order. */
    private String m_sortOrder;

    /**
     * Default constructor.<p>
     *
     * @param currentFolder the current folder
     */
    public CmsSearchWorkplaceBean(String currentFolder) {

        m_fields = CmsStringUtil.collectionAsString(new CmsSearchParameters().getFields(), ",");
        m_sortOrder = CmsSearchParameters.SORT_NAMES[0];
        m_currentFolder = currentFolder;
    }

    /**
     * Returns the fields parameter value.<p>
     *
     * @return the fields parameter value
     */
    public String getFields() {

        return m_fields;
    }

    /**
     * Returns the index name.<p>
     *
     * @return the index name
     */
    public String getIndexName() {

        return m_indexName;
    }

    /**
     * Returns the creation date the resources have to have as maximum.<p>
     *
     * @return the creation date the resources have to have as maximum
     */
    public String getMaxDateCreated() {

        return m_maxDateCreated;
    }

    /**
     * Returns the last modification date the resources have to have as maximum.<p>
     *
     * @return the last modification date the resources have to have as maximum
     */
    public String getMaxDateLastModified() {

        return m_maxDateLastModified;
    }

    /**
     * Returns the creation date the resources have to have as minimum.<p>
     *
     * @return the creation date the resources have to have as minimum
     */
    public String getMinDateCreated() {

        return m_minDateCreated;
    }

    /**
     * Returns the last modification date the resources have to have as minimum.<p>
     *
     * @return the last modification date the resources have to have as minimum
     */
    public String getMinDateLastModified() {

        return m_minDateLastModified;
    }

    /**
     * Returns the query.<p>
     *
     * @return the query
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Returns the search path.<p>
     *
     * @return the search path
     */
    public String getSearchPath() {

        if (isRestrictSearch()) {
            return m_currentFolder;
        } else {
            return "/";
        }
    }

    /**
     * Returns the sort Order.<p>
     *
     * @return the sort Order
     */
    public String getSortOrder() {

        return m_sortOrder;
    }

    /**
     * Returns the restrict Search flag.<p>
     *
     * @return the restrict Search flag
     */
    public boolean isRestrictSearch() {

        return m_restrictSearch;
    }

    /**
     * Sets the fields parameter value.<p>
     *
     * @param fields the fields parameter value to set
     */
    public void setFields(String fields) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(fields)) {
            throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_VALIDATE_SEARCH_PARAMS_0));
        }
        m_fields = fields;
    }

    /**
     * Sets the index name.<p>
     *
     * @param indexName the index name
     */
    public void setIndexName(String indexName) {

        m_indexName = indexName;
    }

    /**
     * Sets the creation date the resources have to have as maximum.<p>
     *
     * @param maxDateCreated the creation date the resources have to have as maximum to set
     */
    public void setMaxDateCreated(String maxDateCreated) {

        m_maxDateCreated = maxDateCreated;
    }

    /**
     * Sets the last modification date the resources have to have as maximum.<p>
     *
     * @param maxDateLastModified the last modification date the resources have to have as maximum to set
     */
    public void setMaxDateLastModified(String maxDateLastModified) {

        m_maxDateLastModified = maxDateLastModified;
    }

    /**
     * Sets the creation date the resources have to have as minimum.<p>
     *
     * @param dateCreatedFrom the creation date the resources have to have as minimum to set
     */
    public void setMinDateCreated(String dateCreatedFrom) {

        m_minDateCreated = dateCreatedFrom;
    }

    /**
     * Sets the last modification date the resources have to have as minimum.<p>
     *
     * @param minDateLastModified the last modification date the resources have to have as minimum to set
     */
    public void setMinDateLastModified(String minDateLastModified) {

        m_minDateLastModified = minDateLastModified;
    }

    /**
     * Sets the query.<p>
     *
     * @param query the query to set
     */
    public void setQuery(String query) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(query)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_VALIDATE_SEARCH_QUERY_0));
        }
        m_query = query;
    }

    /**
     * Sets the restrict Search flag.<p>
     *
     * @param restrictSearch the restrict Search flag to set
     */
    public void setRestrictSearch(boolean restrictSearch) {

        m_restrictSearch = restrictSearch;
    }

    /**
     * Sets the sort Order.<p>
     *
     * @param sortOrder the sort Order to set
     */
    public void setSortOrder(String sortOrder) {

        m_sortOrder = sortOrder;
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/galleries/CmsGallerySearch.java,v $
 * Date   : $Date: 2010/01/14 15:30:14 $
 * Version: $Revision: 1.2 $
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

package org.opencms.search.galleries;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.Messages;
import org.opencms.util.CmsStringUtil;

import org.apache.commons.logging.Log;

/**
 * Contains the functions for the ADE gallery search.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearch {

    /** Sort parameter constants. */
    public enum CmsGallerySortParam {

        /** Sort by date created ascending. */
        DATE_CREATED_ASC("dateCreated.asc"),

        /** Sort by date created descending. */
        DATE_CREATED_DESC("dateCreated.desc"),

        /** Sort by date modified ascending. */
        DATE_LASTMODIFIED_ASC("dateLastModified.asc"),

        /** Sort by date modified descending. */
        DATE_LASTMODIFIED_DESC("dateLastModified.desc"),

        /** Sort by VFS root path ascending. */
        PATH_ASC("path.asc"),

        /** Sort by VFS root path descending. */
        PATH_DESC("path.desc"),

        /** Sort by score ascending. */
        SCORE_ASC("score.asc"),

        /** Sort by score descending. */
        SCORE_DESC("score.desc"),

        /** Sort size ascending. */
        SIZE_ASC("size.asc"),

        /** Sort size descending. */
        SIZE_DESC("size.desc"),

        /** Sort by title ascending. */
        TITLE_ASC("title.asc"),

        /** Sort by title ascending. */
        TITLE_DESC("title.desc"),

        /** Sort by type ascending. */
        TYPE_ASC("type.asc"),

        /** Sort by type descending. */
        TYPE_DESC("type.desc"),

        /** Sort created by ascending. */
        X_CREATEDBY_ASC("createdby.asc"),

        /** Sort created by descending. */
        X_CREATEDBY_DESC("createdby.desc"),

        /** Sort date expired ascending. */
        X_DATE_EXPIRED_ASC("dateExpired.asc"),

        /** Sort date expired descending. */
        X_DATE_EXPIRED_DESC("dateExpired.desc"),

        /** Sort date released ascending. */
        X_DATE_RELEASED_ASC("dateReleased.asc"),

        /** Sort date released descending. */
        X_DATE_RELEASED_DESC("dateReleased.desc"),

        /** Sort modified by ascending. */
        X_MODIFIEDBY_ASC("modifiedby.asc"),

        /** Sort modified by descending. */
        X_MODIFIEDBY_DESC("modifiedby.desc"),

        /** Sort state ascending. */
        X_STATE_ASC("state.asc"),

        /** Sort state descending. */
        X_STATE_DESC("state.desc");

        /** The default sort parameter. */
        public static final CmsGallerySortParam DEFAULT = TITLE_DESC;

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private CmsGallerySortParam(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGallerySearch.class);

    /** The OpenCms object used for the search. */
    protected transient CmsObject m_cms;

    /** The latest exception. */
    protected Exception m_lastException;

    /** The gallery search index used for the gallery search. */
    CmsGallerySearchIndex m_index;

    /**
     * Returns the name of the current search index.<p>
     * 
     * @return the name of the current search index
     */
    public String getIndex() {

        return getSearchIndex().getName();
    }

    /**
     * Returns the last exception that occurred during the last search operation.<p>
     * 
     * @return the last exception that occurred during the last search operation
     */
    public Exception getLastException() {

        return m_lastException;
    }

    /**
     * Returns the gallery search result list.<p>
     *
     * @param params the gallery search parameters
     *
     * @return the gallery search result list
     */
    public CmsGallerySearchResultList getResult(CmsGallerySearchParameters params) {

        CmsGallerySearchResultList result = null;
        if ((m_cms != null) && (m_index != null)) {

            try {

                result = m_index.searchGallery(m_cms, params);

                if (result.size() > 0) {

                    result.calculatePages(params.getResultPage(), params.getMatchesPerPage());

                } else {
                    result = new CmsGallerySearchResultList();
                }
            } catch (Exception exc) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_SEARCHING_FAILED_0), exc);
                }

                m_lastException = exc;
            }
        }

        return result;
    }

    /**
     * Returns the current gallery search index.<p>
     * 
     * @return the current gallery search index
     */
    public CmsGallerySearchIndex getSearchIndex() {

        return m_index;
    }

    /**
     * Initializes the bean with the provided OpenCms context object.<p>
     * 
     * @param cms the OpenCms context to use for the search
     */
    public void init(CmsObject cms) {

        m_cms = cms;
        m_lastException = null;
    }

    /**
     * Set the name of the index to search.<p>
     * 
     * A former search result will be deleted.<p>
     * 
     * @param indexName the name of the index
     */
    public void setIndex(String indexName) {

        if (CmsStringUtil.isNotEmpty(indexName)) {
            try {
                CmsSearchIndex index = OpenCms.getSearchManager().getIndex(indexName);
                if (index == null) {
                    throw new CmsException(Messages.get().container(Messages.ERR_INDEX_NOT_FOUND_1, indexName));
                }
                if (!(index instanceof CmsGallerySearchIndex)) {
                    throw new CmsException(Messages.get().container(
                        Messages.ERR_INDEX_WRONG_CLASS_2,
                        indexName,
                        CmsGallerySearchIndex.class.getName()));
                }
                m_index = (CmsGallerySearchIndex)index;
            } catch (Exception exc) {
                m_lastException = exc;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_INDEX_ACCESS_FAILED_1, indexName), exc);
                }
            }
        }
    }
}
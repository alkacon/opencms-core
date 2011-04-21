/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/galleries/CmsGallerySearch.java,v $
 * Date   : $Date: 2011/04/21 14:20:12 $
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
 * Contains the functions for the gallery search.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearch {

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
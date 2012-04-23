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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.Messages;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Locale;

import org.apache.lucene.document.Document;

/**
 * Contains the functions for the gallery search.<p>
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearch {

    /** The OpenCms object used for the search. */
    protected transient CmsObject m_cms;

    /** The gallery search index used for the gallery search. */
    CmsGallerySearchIndex m_index;

    /**
     * Searches by structure id.<p>
     * 
     * @param cms the OpenCms context to use for the search
     * @param structureId the structure id of the document to search for 
     * @param locale the locale for which the search result should be returned
     *  
     * @return the search result 
     * 
     * @throws CmsException if something goes wrong 
     */
    public static CmsGallerySearchResult searchById(CmsObject cms, CmsUUID structureId, Locale locale)
    throws CmsException {

        CmsGallerySearch gallerySearch = new CmsGallerySearch();
        gallerySearch.init(cms);
        gallerySearch.setIndex(CmsGallerySearchIndex.GALLERY_INDEX_NAME);
        return gallerySearch.searchById(structureId, locale);
    }

    /**
     * Returns the name of the current search index.<p>
     * 
     * @return the name of the current search index
     */
    public String getIndex() {

        return getSearchIndex().getName();
    }

    /**
     * Returns the gallery search result list.<p>
     *
     * @param params the gallery search parameters
     *
     * @return the gallery search result list
     * 
     * @throws CmsException if the search failed
     */
    public CmsGallerySearchResultList getResult(CmsGallerySearchParameters params) throws CmsException {

        CmsGallerySearchResultList result = null;
        if ((m_cms == null) && (m_index == null)) {
            throw new CmsException(Messages.get().container(Messages.ERR_SEARCH_NOT_INITIALIZED_0));
        }

        result = m_index.searchGallery(m_cms, params);

        if (result.size() > 0) {

            result.calculatePages(params.getResultPage(), params.getMatchesPerPage());

        } else {
            result = new CmsGallerySearchResultList();
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
    }

    /**
     * Searches by structure id.<p>
     * 
     * @param id the structure id of the document to search for 
     * @param locale the locale for which the search result should be returned
     *  
     * @return the search result 
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsGallerySearchResult searchById(CmsUUID id, Locale locale) throws CmsException {

        Document doc = m_index.getDocument(CmsGallerySearchFieldMapping.FIELD_RESOURCE_STRUCTURE_ID, id.toString());
        CmsGallerySearchResult result = null;
        if (doc != null) {
            result = new CmsGallerySearchResult(m_cms, 100, doc, null, locale);
        } else {
            CmsResource res = m_cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
            result = new CmsGallerySearchResult(m_cms, res);
        }
        return result;
    }

    /**
     * Set the name of the index to search.<p>
     * 
     * A former search result will be deleted.<p>
     * 
     * @param indexName the name of the index
     * 
     * @throws CmsException if the index was not found or was not an instance of @link {@link org.opencms.search.galleries.CmsGallerySearchIndex}
     */
    public void setIndex(String indexName) throws CmsException {

        if (CmsStringUtil.isEmpty(indexName)) {
            throw new CmsException(Messages.get().container(Messages.ERR_INDEXSOURCE_CREATE_MISSING_NAME_0));
        }
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

    }
}

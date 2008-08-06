/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/A_CmsVfsDocument.java,v $
 * Date   : $Date: 2008/08/06 10:47:20 $
 * Version: $Revision: 1.19 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.documents;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.extractors.I_CmsExtractionResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;

/**
 * Base document factory class for a VFS <code>{@link org.opencms.file.CmsResource}</code>, 
 * just requires a specialized implementation of 
 * <code>{@link I_CmsDocumentFactory#extractContent(CmsObject, CmsResource, CmsSearchIndex)}</code>
 * for text extraction from the binary document content.<p>
 * 
 * @author Carsten Weinholz 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.19 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsVfsDocument implements I_CmsDocumentFactory {

    /** 
     * Value for "high" search priority.
     * 
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#SEARCH_PRIORITY_HIGH_VALUE} instead
     */
    public static final String SEARCH_PRIORITY_HIGH_VALUE = "high";

    /** 
     * Value for "low" search priority. 
     * 
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#SEARCH_PRIORITY_LOW_VALUE} instead
     */
    public static final String SEARCH_PRIORITY_LOW_VALUE = "low";

    /**
     * Value for "maximum" search priority. 
     * 
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#SEARCH_PRIORITY_MAX_VALUE} instead
     */
    public static final String SEARCH_PRIORITY_MAX_VALUE = "max";

    /** 
     * Value for "normal" search priority.
     *  
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#SEARCH_PRIORITY_NORMAL_VALUE} instead
     */
    public static final String SEARCH_PRIORITY_NORMAL_VALUE = "normal";

    /** 
     * The VFS prefix for document keys.
     *  
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#VFS_DOCUMENT_KEY_PREFIX} instead
     */
    public static final String VFS_DOCUMENT_KEY_PREFIX = "VFS";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsVfsDocument.class);

    /**
     * Name of the documenttype.
     */
    protected String m_name;

    /** The cache used for storing extracted documents. */
    private CmsExtractionResultCache m_cache;

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public A_CmsVfsDocument(String name) {

        m_name = name;
    }

    /**
     * Creates a document factory lookup key for the given resource type name / MIME type configuration.<p>
     * 
     * If the given <code>mimeType</code> is <code>null</code>, this indicates that the key should 
     * match all VFS resource of the given resource type regardless of the MIME type.<p>
     * 
     * @param type the resource type name to use
     * @param mimeType the MIME type to use
     * 
     * @return a document factory lookup key for the given resource id / MIME type configuration
     */
    public static String getDocumentKey(String type, String mimeType) {

        StringBuffer result = new StringBuffer(16);
        result.append(A_CmsVfsDocument.VFS_DOCUMENT_KEY_PREFIX);
        result.append('_');
        result.append(type);
        if (mimeType != null) {
            result.append(':');
            result.append(mimeType);
        }
        return result.toString();
    }

    /**
     * Generates a new lucene document instance from contents of the given resource for the provided index.<p>
     * 
     * @see org.opencms.search.documents.I_CmsDocumentFactory#createDocument(CmsObject, CmsResource, CmsSearchIndex)
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#createDocument(CmsObject, CmsResource, CmsSearchIndex, I_CmsExtractionResult)
     */
    public Document createDocument(CmsObject cms, CmsResource resource, CmsSearchIndex index) throws CmsException {

        // extract the content from the resource
        I_CmsExtractionResult content = null;

        // check if caching is enabled for this document type
        String cacheName = null;
        CmsExtractionResultCache cache = getCache();
        if (cache != null) {
            cacheName = cache.getCacheName(resource, isLocaleDependend() ? index.getLocale() : null);
            content = (I_CmsExtractionResult)cache.getCacheObject(cacheName);
        }

        if (content == null) {
            // extraction result has not been found in the cache
            try {
                content = extractContent(cms, resource, index);
                if (cache != null) {
                    // save extracted content to the cache
                    cache.saveCacheObject(cacheName, content);
                }
            } catch (Exception e) {
                // text extraction failed for document - continue indexing meta information only
                LOG.error(Messages.get().getBundle().key(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()), e);
            }
        }

        // create the Lucene document according to the index field configuration
        return index.getFieldConfiguration().createDocument(cms, resource, index, content);
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getCache()
     */
    public CmsExtractionResultCache getCache() {

        return m_cache;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKeys(java.util.List, java.util.List)
     */
    public List getDocumentKeys(List resourceTypes, List mimeTypes) throws CmsException {

        ArrayList keys = new ArrayList();

        if (resourceTypes.contains("*")) {
            ArrayList allTypes = new ArrayList();
            for (Iterator i = OpenCms.getResourceManager().getResourceTypes().iterator(); i.hasNext();) {
                I_CmsResourceType resourceType = (I_CmsResourceType)i.next();
                allTypes.add(resourceType.getTypeName());
            }
            resourceTypes = allTypes;
        }

        try {
            for (Iterator i = resourceTypes.iterator(); i.hasNext();) {

                String typeName = OpenCms.getResourceManager().getResourceType((String)i.next()).getTypeName();
                for (Iterator j = mimeTypes.iterator(); j.hasNext();) {
                    keys.add(getDocumentKey(typeName, (String)j.next()));
                }
                if (mimeTypes.isEmpty()) {
                    keys.add(getDocumentKey(typeName, null));
                }
            }
        } catch (Exception exc) {
            throw new CmsException(Messages.get().container(Messages.ERR_CREATE_DOC_KEY_0), exc);
        }

        return keys;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#setCache(org.opencms.search.documents.CmsExtractionResultCache)
     */
    public void setCache(CmsExtractionResultCache cache) {

        m_cache = cache;
    }

    /**
     * Upgrades the given resource to a {@link CmsFile} with content.<p>
     * 
     * @param cms the current users OpenCms context
     * @param resource the resource to upgrade
     * 
     * @return the given resource upgraded to a {@link CmsFile} with content
     * 
     * @throws CmsException if the resource could not be read 
     * @throws CmsIndexException if the resource has no content
     */
    protected CmsFile readFile(CmsObject cms, CmsResource resource) throws CmsException, CmsIndexException {

        CmsFile file = cms.readFile(resource);
        if (file.getLength() <= 0) {
            throw new CmsIndexException(Messages.get().container(Messages.ERR_NO_CONTENT_1, resource.getRootPath()));
        }
        return file;
    }
}
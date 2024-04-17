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

package org.opencms.search.documents;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.extractors.I_CmsExtractionResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Base document factory class for a VFS <code>{@link org.opencms.file.CmsResource}</code>,
 * just requires a specialized implementation of
 * <code>{@link I_CmsDocumentFactory#extractContent(CmsObject, CmsResource, I_CmsSearchIndex)}</code>
 * for text extraction from the binary document content.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsVfsDocument implements I_CmsDocumentFactory {

    /**
     * Generic type name used as default for all types that are globally unconfigured.
     * Note that any special xml content is already configured if xmlcontent is configured.
     */
    public static final String DEFAULT_ALL_UNCONFIGURED_TYPES = "__unconfigured__";
    /** Generic type name used as default for all types. */
    public static final String DEFAULT_ALL_TYPES = "__all__";
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsVfsDocument.class);

    /** Name of the document type. */
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
        result.append(I_CmsSearchDocument.VFS_DOCUMENT_KEY_PREFIX);
        result.append('_');
        result.append(type);
        if (mimeType != null) {
            result.append(':');
            result.append(mimeType);
        }
        return result.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.opencms.search.documents.I_CmsDocumentFactory#createDocument(CmsObject, CmsResource, I_CmsSearchIndex)
     */
    public I_CmsSearchDocument createDocument(CmsObject cms, CmsResource resource, I_CmsSearchIndex index)
    throws CmsException {

        // extract the content from the resource
        I_CmsExtractionResult content = null;

        if (index.isExtractingContent()) {
            // do full text content extraction only if required

            // check if caching is enabled for this document type
            CmsExtractionResultCache cache = getCache();
            String cacheName = null;
            if ((cache != null) && (resource.getSiblingCount() > 1)) {
                // hard drive based caching only makes sense for resources that have siblings,
                // because the index will also store the content as a blob
                cacheName = cache.getCacheName(
                    resource,
                    isLocaleDependend() ? index.getLocaleForResource(cms, resource, null) : null,
                    getName());
                content = cache.getCacheObject(cacheName);
                if ((content != null) && LOG.isDebugEnabled()) {
                    LOG.debug("Not re-extracting. Using cached content for '" + resource.getRootPath() + "'.");
                }
            }

            if ((content == null) && isOnlyDependentOnContent()) {
                // extraction result has not been found in the cache
                // use the currently indexed content, if it is still up to date.
                content = index.getContentIfUnchanged(resource);
                if ((content != null) && LOG.isDebugEnabled()) {
                    LOG.debug(
                        "Not re-extracting. Using previously indexed content for '" + resource.getRootPath() + "'.");
                }
            }

            if (content == null) {
                // extraction result has not been attached to the resource
                try {
                    content = extractContent(cms, resource, index);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Extracting content for '" + resource.getRootPath() + "' successful.");
                    }
                    if ((cache != null) && (resource.getSiblingCount() > 1)) {
                        // save extracted content to the cache
                        cache.saveCacheObject(cacheName, content);
                    }
                } catch (CmsIndexNoContentException e) {
                    // there was no content found for the resource
                    LOG.info(
                        Messages.get().getBundle().key(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath())
                            + " "
                            + e.getMessage());
                } catch (Throwable e) {
                    // text extraction failed for document - continue indexing meta information only
                    LOG.error(
                        Messages.get().getBundle().key(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                        e);
                }
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
    public List<String> getDocumentKeys(List<String> resourceTypes, List<String> mimeTypes) throws CmsException {

        List<String> keys = new ArrayList<String>();

        try {
            for (Iterator<String> i = resourceTypes.iterator(); i.hasNext();) {

                String typeName = i.next();
                if (typeName.equals("*")) {
                    typeName = DEFAULT_ALL_UNCONFIGURED_TYPES;
                }
                if (typeName.equals("**")) {
                    typeName = DEFAULT_ALL_TYPES;
                }
                for (Iterator<String> j = mimeTypes.iterator(); j.hasNext();) {
                    keys.add(getDocumentKey(typeName, j.next()));
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
     * Logs content extraction for the specified resource and index.<p>
     *
     * @param resource the resource to log content extraction for
     * @param index the search index to log content extraction for
     */
    protected void logContentExtraction(CmsResource resource, I_CmsSearchIndex index) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_EXTRACT_CONTENT_2,
                    resource.getRootPath(),
                    index.getName()));
        }
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
     * @throws CmsIndexNoContentException if the resource has no content
     */
    protected CmsFile readFile(CmsObject cms, CmsResource resource) throws CmsException, CmsIndexNoContentException {

        CmsFile file = cms.readFile(resource);
        if (file.getLength() <= 0) {
            throw new CmsIndexNoContentException(
                Messages.get().container(Messages.ERR_NO_CONTENT_1, resource.getRootPath()));
        }
        return file;
    }
}

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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.I_CmsSearchIndex;

import java.util.List;

/**
 * Used to create index Lucene Documents for OpenCms resources,
 * controls the text extraction algorithm used for a specific OpenCms resource type / MIME type combination.<p>
 *
 * The configuration of the search index is defined in <code>opencms-search.xml</code>.
 * There you can associate a combintion of OpenCms resource types and MIME types to an instance
 * of this factory. This rather complex configuration is required because only the combination of
 * OpenCms resource type and MIME type can decide what to use for search indexing.
 * For example, if the OpenCms resource type is <code>plain</code>,
 * the extraction algorithm for MIME types <code>.html</code> and <code>.txt</code> must be different.
 * On the other hand, the MIME type <code>.html</code> in OpenCms can be almost any resource type,
 * like <code>xmlpage</code>, <code>xmlcontent</code> or even <code>jsp</code>.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsDocumentFactory extends I_CmsSearchExtractor {

    /**
     * Creates the Lucene Document for the given VFS resource and the given search index.<p>
     *
     * This triggers the indexing process for the given VFS resource according to the configuration
     * of the provided index.<p>
     *
     * The provided index resource contains the basic contents to index.
     * The provided search index contains the configuration what to index, such as the locale and
     * possible special field mappings.<p>
     *
     * @param cms the OpenCms user context used to access the OpenCms VFS
     * @param resource the search index resource to create the Lucene document from
     * @param index the search index to create the Document for
     *
     * @return the Search Document for the given index resource and the given search index
     *
     * @throws CmsException if something goes wrong
     *
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#createDocument(CmsObject, CmsResource, I_CmsSearchIndex, org.opencms.search.extractors.I_CmsExtractionResult)
     */
    I_CmsSearchDocument createDocument(CmsObject cms, CmsResource resource, I_CmsSearchIndex index)
    throws CmsException;

    /**
     * Returns the disk based cache used to store the raw extraction results.<p>
     *
     * In case <code>null</code> is returned, then result caching is not supported for this factory.<p>
     *
     * @return the disk based cache used to store the raw extraction results
     */
    CmsExtractionResultCache getCache();

    /**
     * Returns the list of accepted keys for the resource types that can be indexed using this document factory.<p>
     *
     * The result List contains String objects.
     * This String is later matched against {@link A_CmsVfsDocument#getDocumentKey(String, String)} to find
     * the corrospondig {@link I_CmsDocumentFactory} for a resource to index.<p>
     *
     * The list of accepted resource types may contain a catch-all entry "*";
     * in this case, a list for all possible resource types is returned,
     * calculated by a logic depending on the document handler class.<p>
     *
     * @param resourceTypes list of accepted resource types
     * @param mimeTypes list of accepted mime types
     *
     * @return the list of accepted keys for the resource types that can be indexed using this document factory (String objects)
     *
     * @throws CmsException if something goes wrong
     */
    List<String> getDocumentKeys(List<String> resourceTypes, List<String> mimeTypes) throws CmsException;

    /**
     * Returns the name of this document type factory.<p>
     *
     * @return the name of this document type factory
     */
    String getName();

    /**
     * Returns <code>true</code> if this document factory is locale depended.<p>
     *
     * @return <code>true</code> if this document factory is locale depended
     */
    boolean isLocaleDependend();

    /**
     * Returns <code>true</code> if the extraction result dependent on the resources content itself, i.e., has not to be re-extracted if the content date is unchanged.<p>
     *
     * @return  <code>true</code> if the extraction result dependent on the resources content itself, i.e., has not to be re-extracted if the content date is unchanged.
     */
    default boolean isOnlyDependentOnContent() {

        return true;
    }

    /**
     * Returns <code>true</code> if result caching is supported for this factory.<p>
     *
     * @return <code>true</code> if result caching is supported for this factory
     */
    boolean isUsingCache();

    /**
     * Sets the disk based cache used to store the raw extraction results.<p>
     *
     * This should only be used for factories where {@link #isUsingCache()} returns <code>true</code>.<p>
     *
     * @param cache the disk based cache used to store the raw extraction results
     */
    void setCache(CmsExtractionResultCache cache);
}
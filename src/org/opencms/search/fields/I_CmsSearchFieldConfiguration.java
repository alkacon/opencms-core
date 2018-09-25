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

package org.opencms.search.fields;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.extractors.I_CmsExtractionResult;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.solr.uninverting.UninvertingReader.Type;

/**
 * Interface for search field configurations used by {@link org.opencms.search.I_CmsSearchIndex} and configured in the
 * opencms-search.xml (see {@link org.opencms.configuration.CmsSearchConfiguration}).
 */
public interface I_CmsSearchFieldConfiguration extends Comparable<I_CmsSearchFieldConfiguration>, Serializable {

    /**
     * Adds a field to this search field configuration.<p>
     *
     * @param field the field to add
     */
    void addField(CmsSearchField field);

    /** To allow sorting on a field (without docvalues) the field must be added to the map
     *  given to {@link org.apache.solr.uninverting.UninvertingReader#wrap(org.apache.lucene.index.DirectoryReader, Map)}.
     *  The method adds the configured fields.
     * @param uninvertingMap the map to which the fields are added.
     */
    void addUninvertingMappings(Map<String, Type> uninvertingMap);

    /**
     * Creates the document to index.
     *
     * The structure of the document depends on the concrete field configuration, the provided VFS resource, search index and extracted content.<p>
     *
     * The method is typically triggered by {@link I_CmsDocumentFactory#createDocument(CmsObject, CmsResource, I_CmsSearchIndex)} to create the document.<p>
     *
     * @param cms the OpenCms user context used to access the OpenCms VFS
     * @param resource the resource to create the document from
     * @param index the search index to create the document for
     * @param extractionResult the plain text content and additional information extracted from the document
     *
     * @return the document to index for the given VFS resource and the given search index
     *
     * @throws CmsException if something goes wrong
     */
    I_CmsSearchDocument createDocument(
        CmsObject cms,
        CmsResource resource,
        I_CmsSearchIndex index,
        I_CmsExtractionResult extractionResult)
    throws CmsException;

    /**
     * Returns the description of this field configuration.<p>
     *
     * @return the description of this field configuration
     */
    String getDescription();

    /**
     * Returns the configured {@link CmsSearchField} instance with the given name.<p>
     *
     * @return the configured {@link CmsSearchField} instance with the given name
     */
    List<CmsSearchField> getFields();

    /**
     * Returns the name of this field configuration.<p>
     *
     * @return the name of this field configuration
     */
    String getName();

    /**
     * Initializes this field configuration.<p>
     */
    void init();

    /**
     * Sets the description of this field configuration.<p>
     *
     * @param description the description to set
     */
    void setDescription(String description);

    /**
     * Sets the name of this field configuration.<p>
     *
     * @param name the name to set
     */
    void setName(String name);

}

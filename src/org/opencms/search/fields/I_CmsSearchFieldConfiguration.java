/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.search.fields;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.A_CmsSearchIndex;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.extractors.I_CmsExtractionResult;

import java.util.List;

/**
 * The interface for OpenCms search field configurations.<p>
 * 
 * @since 8.5.0
 */
public interface I_CmsSearchFieldConfiguration extends Comparable<I_CmsSearchFieldConfiguration> {

    /**
     * Adds a field to this search field configuration.<p>
     * 
     * @param field the field to add
     */
    void addField(I_CmsSearchField field);

    /**
     * Creates the Lucene Document with this field configuration for the provided VFS resource, search index and content.<p>
     * 
     * This triggers the indexing process for the given VFS resource according to the configuration 
     * of the provided index.<p>
     * 
     * The provided index resource contains the basic contents to index.
     * The provided search index contains the configuration what to index, such as the locale and 
     * possible special field mappings.<p>
     * 
     * @param cms the OpenCms user context used to access the OpenCms VFS
     * @param resource the resource to create the Lucene document from
     * @param index the search index to create the Document for
     * @param content the plain text content extracted from the document
     * 
     * @return the Search Document for the given VFS resource and the given search index
     * 
     * @throws CmsException if something goes wrong
     */
    I_CmsSearchDocument createDocument(
        CmsObject cms,
        CmsResource resource,
        A_CmsSearchIndex index,
        I_CmsExtractionResult content) throws CmsException;

    /**
     * Creates an empty document that can be used by this search field configuration.<p>
     * 
     * @param resource the resource to create the document for
     * 
     * @return a new and empty document
     */
    I_CmsSearchDocument createEmptyDocument(CmsResource resource);

    /**
     * Returns the description of this field configuration.<p>
     * 
     * @return the description of this field configuration
     */
    String getDescription();

    /**
     * Returns the configured {@link I_CmsSearchField} instance with the given name.<p>
     * 
     * @param name the search field name to look up
     * 
     * @return the configured {@link I_CmsSearchField} instance with the given name
     */
    I_CmsSearchField getField(String name);

    /**
     * Returns the list of configured field names (Strings).<p>
     * 
     * @return the list of configured field names (Strings)
     */
    List<String> getFieldNames();

    /**
     * Returns the list of configured {@link I_CmsSearchField} instances.<p>
     * 
     * @return the list of configured {@link I_CmsSearchField} instances
     */
    List<I_CmsSearchField> getFields();

    /**
     * Returns the name of this field configuration.<p>
     *
     * @return the name of this field configuration
     */
    String getName();

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
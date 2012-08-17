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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.search.extractors.I_CmsExtractionResult;

import java.io.Serializable;
import java.util.List;

/**
 * Describes a mapping of a piece of content from an OpenCms VFS 
 * resource to a field of a search index.<p>
 * 
 * @since 8.5.0
 */
public interface I_CmsSearchFieldMapping extends Serializable {

    /**
     * Returns the default value used for this field mapping in case no content is available.<p>
     * 
     * @return the default value used for this field mapping in case no content is available
     */
    String getDefaultValue();

    /**
     * Returns the mapping parameter.<p>
     *
     * The parameter is used depending on the implementation of the rules of 
     * the selected {@link CmsSearchFieldMappingType}.<p>
     *
     * @return the mapping parameter
     */
    String getParam();

    /**
     * Returns the String value extracted form the provided data according to the rules of this mapping type.<p> 
     * 
     * @param cms the OpenCms context used for building the search index
     * @param res the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource  
     * 
     * @return the String value extracted form the provided data according to the rules of this mapping type
     */
    String getStringValue(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched);

    /**
     * Returns the mapping type.<p>
     *
     * @return the mapping type
     */
    CmsSearchFieldMappingType getType();

    /**
     * Sets the default value for this field mapping in case no content is available.<p> 
     * 
     * @param defaultValue the default value to set
     */
    void setDefaultValue(String defaultValue);

    /**
     * Sets the mapping parameter.<p>
     *
     * The parameter is used depending on the implementation of the rules of 
     * the selected {@link CmsSearchFieldMappingType}.<p>
     *
     * @param param the parameter to set
     */
    void setParam(String param);

    /**
     * Sets the mapping type.<p>
     *
     * @param type the type to set
     */
    void setType(CmsSearchFieldMappingType type);

    /**
     * Sets the mapping type as a String.<p>
     *
     * @param type the name of the type to set
     */
    void setType(String type);
}

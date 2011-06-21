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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.search.extractors.I_CmsExtractionResult;

import java.util.List;

/**
 * Provides additional information to be stored in the gallery search index.<p>
 * 
 * @since 8.0.0 
 */
public final class CmsGallerySearchInfoProvider {

    /**
     * Hides the public constructor.<p>
     */
    private CmsGallerySearchInfoProvider() {

        // NOOP
    }

    /**
     * Provides additional information to be stored in the gallery search index.<p>
     * 
     * @param cms the OpenCms context used for building the search index
     * @param res the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource  
     * 
     * @return the String value extracted form the provided data according to the rules of this mapping type
     */
    public static String getAdditionalInfo(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        return "Not yet implemented!";
    }

    /**
     * Provides the container types to be stored in the gallery search index.<p>
     * 
     * The return value is one string that contains all supported container names separated by a white space.<p>
     * 
     * @param cms the OpenCms context used for building the search index
     * @param res the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource  
     * 
     * @return the String value extracted form the provided data according to the rules of this mapping type
     */
    public static String getContainerTypes(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        // multiple container names must be separated by white space
        return "not yet implemented";
    }
}
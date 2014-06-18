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
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.extractors.I_CmsExtractionResult;

import java.util.List;

/**
 * Adds fields to the configuration and appends fields to a document during index process.<p>
 * 
 * @since 9.0.0
 */
public interface I_CmsSearchFieldAppdender {

    /**
     * Add the additional fields to the configuration.<p>
     * 
     * {@link #appendFields(I_CmsSearchDocument, CmsObject, CmsResource, I_CmsExtractionResult, List, List)}
     */
    void addAdditionalFields();

    /**
     * Can append one or more fields to an document.<p>
     * 
     * @param document the document to append field(s)
     * @param cms the CMS object used during indexing process
     * @param resource the resource that is currently indexed
     * @param extractionResult the extraction result of this resource
     * @param properties {@link org.opencms.file.CmsObject#readPropertyObjects(CmsResource, boolean)} with false
     * @param propertiesSearched {@link org.opencms.file.CmsObject#readPropertyObjects(CmsResource, boolean)} with true
     * 
     * @see  org.opencms.file.CmsObject#readPropertyObjects(CmsResource, boolean)
     * 
     * @return the document
     */
    I_CmsSearchDocument appendFields(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched);
}

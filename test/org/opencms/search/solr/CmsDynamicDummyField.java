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

package org.opencms.search.solr;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.CmsSearchFieldMappingType;

import java.util.List;

/**
 * Dummy Field Mapping implementation.<p>
 */
public class CmsDynamicDummyField extends CmsSearchFieldMapping {

    /** Serial version UID. */
    private static final long serialVersionUID = -3451280904747959635L;

    /**
     * Public constructor.<p>
     */
    public CmsDynamicDummyField() {

        super();
    }

    /**
     * @see org.opencms.search.fields.CmsSearchFieldMapping#getStringValue(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    public String getStringValue(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        String result = null;
        if (getType().getMode() == CmsSearchFieldMappingType.DYNAMIC.getMode()) {
            // dynamic mapping
            if ("special".equals(getParam())) {
                result = "This is an amazing and very 'dynamic' content";
            }
        } else {
            // default mapping
            result = super.getStringValue(cms, res, extractionResult, properties, propertiesSearched);
        }
        return result;
    }
}

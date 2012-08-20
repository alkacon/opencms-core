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

package org.opencms.search.fields;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;

import java.util.List;

/**
 * Describes a mapping of a piece of content from an OpenCms VFS resource to a field of a search index.<p>
 * 
 * @since 7.0.0 
 */
public class CmsSearchFieldMapping extends A_CmsSearchFieldMapping {

    /** Serial version UID. */
    private static final long serialVersionUID = -4118281721737064202L;

    /**
     * Public constructor for a new search field mapping.<p>
     */
    public CmsSearchFieldMapping() {

    }

    /**
     * Public constructor for a new search field mapping.<p>
     * 
     * @param type the type to use, see {@link #setType(CmsSearchFieldMappingType)}
     * @param param the mapping parameter, see {@link #setParam(String)}
     */
    public CmsSearchFieldMapping(CmsSearchFieldMappingType type, String param) {

        super(type, param);
    }

    /**
     * @see org.opencms.search.fields.A_CmsSearchFieldMapping#getStringValue(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    public String getStringValue(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        String content = super.getStringValue(cms, res, extractionResult, properties, propertiesSearched);
        if (getType().equals(CmsSearchFieldMappingType.CONTENT) && CmsStringUtil.isEmptyOrWhitespaceOnly(content)) {
            // try the localized content field
            String key = A_CmsSearchFieldConfiguration.getLocaleExtendedName(
                I_CmsSearchField.FIELD_CONTENT,
                cms.getRequestContext().getLocale());
            content = extractionResult.getContentItems().get(key);
        }
        return content;
    }
}
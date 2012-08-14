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
import org.opencms.search.fields.A_CmsSearchFieldConfiguration;
import org.opencms.search.fields.A_CmsSearchFieldMapping;
import org.opencms.search.fields.CmsSearchFieldMappingType;
import org.opencms.util.CmsStringUtil;

import java.util.List;
import java.util.Locale;

/**
 * The Solr field mapping implementation.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrFieldMapping extends A_CmsSearchFieldMapping {

    /**
     * Public constructor.<p>
     * 
     * @param type the mapping type
     * @param param the parameter
     */
    public CmsSolrFieldMapping(CmsSearchFieldMappingType type, String param) {

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

        if (getType().equals(CmsSearchFieldMappingType.CONTENT) || getType().equals(CmsSearchFieldMappingType.ITEM)) {
            Locale locale = cms.getRequestContext().getLocale();
            String key = A_CmsSearchFieldConfiguration.getLocaleExtendedName(getParam(), locale);
            String content = extractionResult.getContentItems().get(key);
            if (getType().equals(CmsSearchFieldMappingType.CONTENT) && CmsStringUtil.isEmptyOrWhitespaceOnly(content)) {
                content = extractionResult.getContent();
            }
            return content;
        }
        return super.getStringValue(cms, res, extractionResult, properties, propertiesSearched);
    }
}

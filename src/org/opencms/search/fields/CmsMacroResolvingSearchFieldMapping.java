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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.galleries.CmsGalleryNameMacroResolver;
import org.opencms.util.CmsMacroResolver;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Adopted version of the default {@link org.opencms.search.fields.CmsSearchFieldMapping}
 * that resolves macros via the {@link org.opencms.search.galleries.CmsGalleryNameMacroResolver} in the mapped value before returning it.
 */
public class CmsMacroResolvingSearchFieldMapping extends CmsSearchFieldMapping {

    /** Serial version UID. */
    private static final long serialVersionUID = 7690286960084342440L;

    /** Logger for the class */
    protected static final Log LOG = CmsLog.getLog(CmsMacroResolvingSearchFieldMapping.class);

    /**
     * Default constructor.
     */
    public CmsMacroResolvingSearchFieldMapping() {

        super();
    }

    /**
     * Calls the super method and resolves macros in the returned value.
     *
     * @see org.opencms.search.fields.CmsSearchFieldMapping#getStringValue(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    public String getStringValue(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        String result = super.getStringValue(cms, res, extractionResult, properties, propertiesSearched);
        if ((result != null) && !result.isEmpty()) {
            try {
                CmsObject cmsClone = OpenCms.initCmsObject(cms);
                if (null != m_locale) {
                    cmsClone.getRequestContext().setLocale(m_locale);
                }
                CmsFile file = cmsClone.readFile(res);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsClone, file);
                CmsMacroResolver resolver = new CmsGalleryNameMacroResolver(cms, content, m_locale);
                return resolver.resolveMacros(result);
            } catch (CmsException e) {
                LOG.error(
                    "Failed to resolve macros in a search field mapping for content "
                        + (res != null ? res.getRootPath() : "null")
                        + ". Returning unresolved value.",
                    e);
            }
        }
        return result;
    }

}

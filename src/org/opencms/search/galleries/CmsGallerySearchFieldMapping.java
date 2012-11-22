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
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.main.OpenCms;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.CmsSearchFieldMappingType;

import java.util.List;
import java.util.Locale;

/**
 * Special search field mapping class for the gallery search.<p>
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchFieldMapping extends CmsSearchFieldMapping {

    /** Name of the field that contains the gallery index additional information. */
    public static final String FIELD_ADDITIONAL_INFO = "additional_info";

    /** Name of the field that contains the gallery index container information. */
    public static final String FIELD_CONTAINER_TYPES = "container_types";

    /** Name of the field that contains the date the resource has expired. */
    public static final String FIELD_RESOURCE_DATE_EXPIRED = "res_dateExpired";

    /** Name of the field that contains the date the resource was released. */
    public static final String FIELD_RESOURCE_DATE_RELEASED = "res_dateReleased";

    /** Name of the field that contains the resource length. */
    public static final String FIELD_RESOURCE_LENGTH = "res_length";

    /** Name of the field that contains the resource state. */
    public static final String FIELD_RESOURCE_STATE = "res_state";

    /** Name of the field that contains the structure id the resource. */
    public static final String FIELD_RESOURCE_STRUCTURE_ID = "res_structureId";

    /** Name of the field that contains the name of the user who created the resource. */
    public static final String FIELD_RESOURCE_USER_CREATED = "res_userCreated";

    /** Name of the field that contains the name of the user who last modified the resource. */
    public static final String FIELD_RESOURCE_USER_LASTMODIFIED = "res_userLastModified";

    /** Serial version UID. */
    private static final long serialVersionUID = -9040025344745155491L;

    /**
     * Public constructor for a new search field mapping.<p>
     */
    public CmsGallerySearchFieldMapping() {

        super();
    }

    /**
     * Public constructor for a new search field mapping.<p>
     * 
     * @param type the type to use, see {@link #setType(CmsSearchFieldMappingType)}
     * @param param the mapping parameter, see {@link #setParam(String)}
     */
    public CmsGallerySearchFieldMapping(CmsSearchFieldMappingType type, String param) {

        super(type, param);
    }

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
            if (CmsGallerySearchFieldMapping.FIELD_ADDITIONAL_INFO.equals(getParam())) {
                result = CmsGallerySearchInfoProvider.getAdditionalInfo(
                    cms,
                    res,
                    extractionResult,
                    properties,
                    propertiesSearched);
            } else if (CmsGallerySearchFieldMapping.FIELD_CONTAINER_TYPES.equals(getParam())) {
                result = CmsGallerySearchInfoProvider.getContainerTypes(
                    cms,
                    res,
                    extractionResult,
                    properties,
                    propertiesSearched);
            } else if (CmsSearchField.FIELD_RESOURCE_LOCALES.equals(getParam())) {
                List<Locale> locales = null;
                if (CmsResourceTypeXmlContent.isXmlContent(res)) {
                    // resource type is XML content - just return the locales actually available
                    result = extractionResult.getContentItems().get(CmsSearchField.FIELD_RESOURCE_LOCALES);
                } else {
                    // for all other resource types we return the locales available
                    locales = OpenCms.getLocaleManager().getAvailableLocales(cms, res);
                    StringBuffer buf = new StringBuffer();
                    for (Locale locale : locales) {
                        buf.append(locale.toString());
                        buf.append(' ');
                    }
                    result = buf.toString();
                }
            }
        } else {
            // default mapping
            result = super.getStringValue(cms, res, extractionResult, properties, propertiesSearched);
        }
        return result;
    }
}
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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Class extracting the Geo coordinates from a content field.
 */
public class CmsSearchFieldMappingGeoCoords implements I_CmsSearchFieldMapping {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchFieldMappingGeoCoords.class);

    /** Serial version UID. */
    private static final long serialVersionUID = 3016384419639743033L;

    /** Default latitude longitude value. */
    private String m_defaultValue;

    /** The search setting parameters. */
    private String m_param;

    /** The search setting type. */
    private CmsSearchFieldMappingType m_type;

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getDefaultValue()
     */
    public String getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getParam()
     */
    public String getParam() {

        return m_param;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getStringValue(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    public String getStringValue(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        String coordinates = m_defaultValue;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParam())) {
            String value = findFirstCoordinatesValue(extractionResult);
            String locationPickerCoordinates = parseLocationPickerCoordinates(value);
            if (locationPickerCoordinates != null) {
                coordinates = locationPickerCoordinates;
            } else if (CmsGeoCoordinateUtil.validateCoordinates(value)) {
                coordinates = value;
            }
        }
        return coordinates;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getType()
     */
    public CmsSearchFieldMappingType getType() {

        return m_type;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String defaultValue) {

        m_defaultValue = defaultValue;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setParam(java.lang.String)
     */
    public void setParam(String param) {

        m_param = param;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setType(org.opencms.search.fields.CmsSearchFieldMappingType)
     */
    public void setType(CmsSearchFieldMappingType type) {

        m_type = type;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setType(java.lang.String)
     */
    public void setType(String type) {

        m_type = CmsSearchFieldMappingType.valueOf(type);
    }

    /**
     * At first, we search for a coordinates value in the best matching locale of the extraction
     * result. If not available, search for a coordinates value in other locales. In the case of
     * a multi-valued field, only the first coordinates value is returned. Further values are ignored.
     * @param extractionResult the extraction result
     * @return the coordinates value
     */
    private String findFirstCoordinatesValue(I_CmsExtractionResult extractionResult) {

        String value = extractionResult.getContentItems().get(getParam() + "[1]");
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            for (Locale locale : extractionResult.getLocales()) {
                String val = extractionResult.getContentItems(locale).get(getParam() + "[1]");
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(val)) {
                    return val;
                }
            }
        }
        return value;
    }

    /**
     * Parses a location picker JSON value and returns the coordinates contained therein.
     * @param jsonValue the JSON value as string
     * @return the coordinates string or null if there are no valid location picker coordinates
     */
    private String parseLocationPickerCoordinates(String jsonValue) {

        try {
            JSONObject json = new JSONObject(jsonValue);
            if (!CmsGeoCoordinateUtil.validateLocationPickerCoordinates(json)) {
                return null;
            }
            return json.getString("lat") + "," + json.getString("lng");
        } catch (JSONException e) {
            return null;
        }
    }
}

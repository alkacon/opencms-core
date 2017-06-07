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
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.I_CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.search.Messages;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.DateTools;

/**
 * Describes a mapping of a piece of content from an OpenCms VFS resource to a field of a search index.<p>
 *
 * @since 7.0.0
 */
public class CmsSearchFieldMapping implements I_CmsSearchFieldMapping {

    /** Default for expiration date since Long.MAX_VALUE is to big. */
    private static final String DATE_EXPIRED_DEFAULT_STR = "21000101";

    /** The default expiration date. */
    private static Date m_defaultDateExpired;

    /** Serial version UID. */
    private static final long serialVersionUID = 3016384419639743033L;

    /** The configured default value. */
    private String m_defaultValue;

    /** Pre-calculated hash value. */
    private int m_hashCode;

    /** The parameter for the mapping type. */
    private String m_param;

    /** The mapping type. */
    private CmsSearchFieldMappingType m_type;

    /** Flag, indicating if the mapping applies to a lucene index. */
    private boolean m_isLucene;

    /**
     * Public constructor for a new search field mapping.<p>
     */
    public CmsSearchFieldMapping() {

        // no initialization required
    }

    /**
     * Public constructor for a new search field mapping.<p>
     *
     * @param isLucene flag, indicating if the mapping is done for a lucene index
     */
    public CmsSearchFieldMapping(boolean isLucene) {

        this();
        m_isLucene = isLucene;
    }

    /**
     * Public constructor for a new search field mapping.<p>
     *
     * @param type the type to use, see {@link #setType(CmsSearchFieldMappingType)}
     * @param param the mapping parameter, see {@link #setParam(String)}
     */
    public CmsSearchFieldMapping(CmsSearchFieldMappingType type, String param) {

        this();
        setType(type);
        setParam(param);
    }

    /**
     * Public constructor for a new search field mapping.<p>
     *
     * @param type the type to use, see {@link #setType(CmsSearchFieldMappingType)}
     * @param param the mapping parameter, see {@link #setParam(String)}
     * @param isLucene flag, indicating if the mapping is done for a lucene index
     */
    public CmsSearchFieldMapping(CmsSearchFieldMappingType type, String param, boolean isLucene) {

        this(type, param);
        m_isLucene = isLucene;
    }

    /**
     * Returns the default expiration date, meaning the resource never expires.<p>
     *
     * @return the default expiration date
     *
     * @throws ParseException if something goes wrong parsing the default date string
     */
    public static Date getDefaultDateExpired() throws ParseException {

        if (m_defaultDateExpired == null) {
            m_defaultDateExpired = DateTools.stringToDate("21000101");
        }
        return m_defaultDateExpired;
    }

    /**
     * Two mappings are equal if the type and the parameter is equal.<p>
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if ((obj instanceof I_CmsSearchFieldMapping)) {
            I_CmsSearchFieldMapping other = (I_CmsSearchFieldMapping)obj;
            return (CmsStringUtil.isEqual(m_type, other.getType()))
                && (CmsStringUtil.isEqual(m_param, other.getParam()));
        }
        return false;
    }

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

        String content = null;
        switch (getType().getMode()) {
            case 0: // content
                if (extractionResult != null) {
                    content = extractionResult.getContent();
                }
                break;
            case 1: // property
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParam())) {
                    content = CmsProperty.get(getParam(), properties).getValue();
                }
                break;
            case 2: // property-search
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParam())) {
                    content = CmsProperty.get(getParam(), propertiesSearched).getValue();
                }
                break;
            case 3: // item (retrieve value for the given XPath from the content items)
                if ((extractionResult != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParam())) {
                    String[] paramParts = getParam().split("\\|");
                    Map<String, String> localizedContentItems = null;
                    String xpath = null;
                    if (paramParts.length > 1) {
                        OpenCms.getLocaleManager();
                        localizedContentItems = extractionResult.getContentItems(
                            CmsLocaleManager.getLocale(paramParts[0].trim()));
                        xpath = paramParts[1].trim();
                    } else {
                        localizedContentItems = extractionResult.getContentItems();
                        xpath = paramParts[0].trim();
                    }
                    content = getContentItemForXPath(localizedContentItems, xpath);
                }
                break;
            case 5: // attribute
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParam())) {
                    I_CmsResource.CmsResourceAttribute attribute = null;
                    try {
                        attribute = I_CmsResource.CmsResourceAttribute.valueOf(getParam());
                    } catch (Exception e) {
                        // invalid attribute name specified, attribute will be null
                    }
                    if (attribute != null) {
                        // map all attributes for a resource
                        switch (attribute) {
                            case dateContent:
                                content = m_isLucene
                                ? DateTools.timeToString(res.getDateContent(), DateTools.Resolution.MILLISECOND)
                                : Long.toString(res.getDateContent());
                                break;
                            case dateCreated:
                                content = m_isLucene
                                ? DateTools.timeToString(res.getDateCreated(), DateTools.Resolution.MILLISECOND)
                                : Long.toString(res.getDateCreated());
                                break;
                            case dateExpired:
                                if (m_isLucene) {
                                    long expirationDate = res.getDateExpired();
                                    if (expirationDate == CmsResource.DATE_EXPIRED_DEFAULT) {
                                        // default of Long.MAX_VALUE is to big, use January 1, 2100 instead
                                        content = DATE_EXPIRED_DEFAULT_STR;
                                    } else {
                                        content = DateTools.timeToString(
                                            expirationDate,
                                            DateTools.Resolution.MILLISECOND);
                                    }
                                } else {
                                    content = Long.toString(res.getDateExpired());
                                }
                                break;
                            case dateLastModified:
                                content = m_isLucene
                                ? DateTools.timeToString(res.getDateLastModified(), DateTools.Resolution.MILLISECOND)
                                : Long.toString(res.getDateLastModified());
                                break;
                            case dateReleased:
                                content = m_isLucene
                                ? DateTools.timeToString(res.getDateReleased(), DateTools.Resolution.MILLISECOND)
                                : Long.toString(res.getDateReleased());
                                break;
                            case flags:
                                content = String.valueOf(res.getFlags());
                                break;
                            case length:
                                content = String.valueOf(res.getLength());
                                break;
                            case name:
                                content = res.getName();
                                break;
                            case projectLastModified:
                                try {
                                    CmsProject project = cms.readProject(res.getProjectLastModified());
                                    content = project.getName();
                                } catch (Exception e) {
                                    // NOOP, content is already null
                                }
                                break;
                            case resourceId:
                                content = res.getResourceId().toString();
                                break;
                            case rootPath:
                                content = res.getRootPath();
                                break;
                            case siblingCount:
                                content = String.valueOf(res.getSiblingCount());
                                break;
                            case state:
                                content = res.getState().toString();
                                break;
                            case structureId:
                                content = res.getStructureId().toString();
                                break;
                            case typeId:
                                content = String.valueOf(res.getTypeId());
                                break;
                            case userCreated:
                                try {
                                    CmsUser user = cms.readUser(res.getUserCreated());
                                    content = user.getName();
                                } catch (Exception e) {
                                    // NOOP, content is already null
                                }
                                break;
                            case userLastModified:
                                try {
                                    CmsUser user = cms.readUser(res.getUserLastModified());
                                    content = user.getName();
                                } catch (Exception e) {
                                    // NOOP, content is already null
                                }
                                break;
                            case version:
                                content = String.valueOf(res.getVersion());
                                break;
                            default:
                                // NOOP, content is already null
                        }
                    }
                }
                break;
            default:
                // NOOP, content is already null
        }
        if (content == null) {
            // in case the content is not available, use the default value for this mapping
            content = getDefaultValue();
        }
        return content;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getType()
     */
    public CmsSearchFieldMappingType getType() {

        return m_type;
    }

    /**
     * The hash code depends on the type and the parameter.<p>
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if (m_hashCode == 0) {
            int hashCode = 73 * (m_type == null ? 29 : m_type.hashCode());
            if (m_param != null) {
                hashCode += m_param.hashCode();
            }
            m_hashCode = hashCode;
        }
        return m_hashCode;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String defaultValue) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(defaultValue)) {
            m_defaultValue = defaultValue.trim();
        } else {
            m_defaultValue = null;
        }
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setParam(java.lang.String)
     */
    public void setParam(String param) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(param)) {
            m_param = param.trim();
        } else {
            m_param = null;
        }
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

        CmsSearchFieldMappingType mappingType = CmsSearchFieldMappingType.valueOf(type);
        if (mappingType == null) {
            // invalid mapping type has been used, throw an exception
            throw new CmsRuntimeException(
                new CmsMessageContainer(Messages.get(), Messages.ERR_FIELD_TYPE_UNKNOWN_1, new Object[] {type}));
        }
        setType(mappingType);
    }

    /**
     * Returns a "\n" separated String of values for the given XPath if according content items can be found.<p>
     *
     * @param contentItems the content items to get the value from
     * @param xpath the short XPath parameter to get the value for
     *
     * @return a "\n" separated String of element values found in the content items for the given XPath
     */
    private String getContentItemForXPath(Map<String, String> contentItems, String xpath) {

        if (contentItems.get(xpath) != null) { // content item found for XPath
            return contentItems.get(xpath);
        } else { // try a multiple value mapping
            StringBuffer result = new StringBuffer();
            for (Map.Entry<String, String> entry : contentItems.entrySet()) {
                if (CmsXmlUtils.removeXpath(entry.getKey()).equals(xpath)) { // the removed path refers an item
                    result.append(entry.getValue());
                    result.append("\n");
                }
            }
            return result.length() > 1 ? result.toString().substring(0, result.length() - 1) : null;
        }
    }
}
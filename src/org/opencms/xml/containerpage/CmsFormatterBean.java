/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/CmsFormatterBean.java,v $
 * Date   : $Date: 2011/05/03 10:48:48 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContent;

/**
 * A bean containing formatter configuration data as strings.<p>
 * 
 * @author Georg Westenberger
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsFormatterBean {

    /** Default formatter type constant. */
    public static final String DEFAULT_LOCATION = "_location not available_";

    /** Default formatter path. */
    public static final String DEFAULT_PREVIEW_JSPURI = "/system/workplace/editors/ade/default-list-formatter.jsp";

    /** Default formatter type constant. */
    public static final String DEFAULT_PREVIEW_TYPE = "_DEFAULT_PREVIEW_";

    /** Default formatter bean. */
    public static final CmsFormatterBean PREVIEW_FORMATTER = new CmsFormatterBean(
        DEFAULT_PREVIEW_JSPURI,
        DEFAULT_PREVIEW_TYPE);

    /** Wildcard formatter type for width based formatters. */
    public static final String WILDCARD_TYPE = "*";

    /** Indicates if this formatter was configured in a XML schema XSD or sitemap configuration. */
    private boolean m_isFromSchema;

    /** Indicates if this is a type based or width based formatter. */
    private boolean m_isTypeFormatter;

    /** The formatter JSP. */
    private String m_jspRootPath;

    /** The location this formatter was configured in. */
    private String m_location;

    /** The formatter max width. */
    private int m_maxWidth;

    /** The formatter min width. */
    private int m_minWidth;

    /** Indicates if the content should be searchable in the online index when this formatter is used. */
    private boolean m_search;

    /** The formatter container type. */
    private String m_type;

    /**
     * Constructor for creating a new formatter.<p>
     * 
     * This constructor should be used to create default type based formatters.<p>
     * 
     * @param jspUri the formatter JSP URI
     * @param type the formatter container type 
     */
    public CmsFormatterBean(String jspUri, String type) {

        this(jspUri, type, "", "", String.valueOf(false), DEFAULT_LOCATION, true);
    }

    /**
     * Constructor for creating a new formatter.<p>
     * 
     * This constructor should be used to create default type based formatters.<p>
     * 
     * @param jspUri the formatter JSP URI
     * @param type the formatter container type 
     * @param location the location URI of the configuration
     */
    public CmsFormatterBean(String jspUri, String type, String location) {

        this(jspUri, type, "", "", String.valueOf(false), location, true);
    }

    /**
     * Constructor for creating a new formatter configuration from a sitemap configuration.<p>
     * 
     * @param jspUri the formatter JSP URI
     * @param type the formatter container type 
     * @param minWidthStr the formatter min width
     * @param maxWidthStr the formatter max width 
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param configurationDocument the sitemap configuration used to configure this formatter
     */
    public CmsFormatterBean(
        String jspUri,
        String type,
        String minWidthStr,
        String maxWidthStr,
        String searchContent,
        CmsXmlContent configurationDocument) {

        this(
            jspUri,
            type,
            minWidthStr,
            maxWidthStr,
            searchContent,
            configurationDocument.getFile().getRootPath(),
            false);
    }

    /**
     * Constructor for creating a new formatter configuration from an XML schema annotation.<p>
     * 
     * @param jspUri the formatter JSP URI
     * @param type the formatter container type 
     * @param minWidthStr the formatter min width
     * @param maxWidthStr the formatter max width 
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param contentDefinition the content definition the XML schema annotation used to configure this formatter belongs to
     */
    public CmsFormatterBean(
        String jspUri,
        String type,
        String minWidthStr,
        String maxWidthStr,
        String searchContent,
        CmsXmlContentDefinition contentDefinition) {

        this(jspUri, type, minWidthStr, maxWidthStr, searchContent, contentDefinition.getSchemaLocation(), true);
    }

    /**
     * Constructor for creating a new formatter.<p>
     * 
     * @param jspUri the formatter JSP URI
     * @param type the formatter container type 
     * @param minWidthStr the formatter min width
     * @param maxWidthStr the formatter max width 
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param location the location URI of the configuration
     * @param isFromSchema indicates if this formatter was configured in a XML schema XSD or sitemap configuration
     */
    public CmsFormatterBean(
        String jspUri,
        String type,
        String minWidthStr,
        String maxWidthStr,
        String searchContent,
        String location,
        boolean isFromSchema) {

        m_jspRootPath = jspUri;

        m_type = type;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_type)) {
            m_type = WILDCARD_TYPE;
        }

        m_minWidth = -1;
        m_maxWidth = Integer.MAX_VALUE;
        m_isTypeFormatter = true;

        if (WILDCARD_TYPE.equals(m_type)) {
            // wildcard formatter; index by width
            m_isTypeFormatter = false;
            // if no width available, use -1

            try {
                m_minWidth = Integer.parseInt(minWidthStr);
            } catch (NumberFormatException e) {
                //ignore; width will be -1 
            }
            try {
                m_maxWidth = Integer.parseInt(maxWidthStr);
            } catch (NumberFormatException e) {
                //ignore; maxWidth will be max. integer 
            }
        }

        m_search = CmsStringUtil.isEmptyOrWhitespaceOnly(searchContent)
        ? true
        : Boolean.valueOf(searchContent).booleanValue();

        m_location = location;
        m_isFromSchema = isFromSchema;
    }

    /**
     * Checks if the given type is the default preview type.<p>
     * 
     * This check is required for resources types like images which do not really have different formatters,
     * in order to render the preview in the ADE gallery GUI.<p>
     * 
     * @param type the formatter type to check
     * 
     * @return <code>true</code> if the given type is the default preview type
     */
    public static boolean isDefaultPreviewType(String type) {

        return DEFAULT_PREVIEW_TYPE.equals(type);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsFormatterBean) {
            CmsFormatterBean other = (CmsFormatterBean)obj;

            if (other.m_isTypeFormatter == m_isTypeFormatter) {
                // not same formatter type means not equal
                if (m_isTypeFormatter) {
                    // this is a type formatter, we use just the type name
                    return CmsStringUtil.isEqual(m_type, other.m_type);
                } else {
                    // this is a width formatter, we use both min and max width
                    return (m_minWidth == other.m_minWidth) && (m_maxWidth == other.m_maxWidth);
                }
            }
        }
        return false;
    }

    /**
     * Returns the root path of the formatter JSP in the OpenCms VFS.<p>
     * 
     * @return the root path of the formatter JSP in the OpenCms VFS.<p>
     */
    public String getJspRootPath() {

        return m_jspRootPath;
    }

    /**
     * Returns the location this formatter was defined in.<p>
     * 
     * This will be an OpenCms VFS URI, either to the XML schema XSD, or the
     * configuration file this formatter was defined in.<p>
     * 
     * @return the location this formatter was defined in
     */
    public String getLocation() {

        return m_location;
    }

    /**
     * Returns the maximum formatter width.<p>
     * 
     * If this is not set, then {@link Integer#MAX_VALUE} is returned.<p>
     *  
     * @return the maximum formatter width 
     */
    public int getMaxWidth() {

        return m_maxWidth;
    }

    /**
     * Returns the minimum formatter width.<p>
     * 
     * If this is not set, then <code>-1</code> is returned.<p>
     * 
     * @return the minimum formatter width
     */
    public int getMinWidth() {

        return m_minWidth;
    }

    /**
     * Returns the formatter container type.<p>
     * 
     * If this is "*", then the formatter is a width based formatter.<p>
     * 
     * @return the formatter container type 
     */
    public String getType() {

        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_type.hashCode() ^ ((m_minWidth * 33) ^ m_maxWidth);
    }

    /**
     * Returns <code>true</code> if this formatter was configured in an XML schema annotation.<p>
     * 
     * @return <code>true</code> if this formatter was configured in an XML schema annotation
     */
    public boolean isConfiguredInSchema() {

        return m_isFromSchema;
    }

    /**
     * Returns <code>true</code> in case an XML content formatted with this formatter should be included in the 
     * online full text search.<p>
     * 
     * @return <code>true</code> in case an XML content formatted with this formatter should be included in the 
     * online full text search
     */
    public boolean isSearchContent() {

        return m_search;
    }

    /**
     * Returns <code>true</code> in case this formatter is based on type information.<p>
     * 
     * @return <code>true</code> in case this formatter is based on type information
     */
    public boolean isTypeFormatter() {

        return m_isTypeFormatter;
    }
}
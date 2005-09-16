/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsPropertyDefinition.java,v $
 * Date   : $Date: 2005/09/16 08:47:37 $
 * Version: $Revision: 1.13.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.util.CmsUUID;

/**
 * Defines a property name, so that <code>{@link CmsProperty}</code> instances can be created with that name.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.13.2.1 $
 * 
 * @since 6.0.0 
 */
public class CmsPropertyDefinition implements Cloneable, Comparable {

    /** Property for the active method in the administration view. */
    public static final String PROPERTY_ACTIV = "activemethod";

    /** Property for the allowed set of locales. */
    public static final String PROPERTY_AVAILABLE_LOCALES = "locale-available";

    /** Property to control the Java class for body. */
    public static final String PROPERTY_BODY_CLASS = "templateclass";

    /** The name of the VFS property that controls the caching. */
    public static final String PROPERTY_CACHE = "cache";

    /** Property for the channel id. */
    public static final String PROPERTY_CHANNELID = "ChannelId";

    /** Property for the content conversion. */
    public static final String PROPERTY_CONTENT_CONVERSION = "content-conversion";

    /** Property for the content encoding. */
    public static final String PROPERTY_CONTENT_ENCODING = "content-encoding";

    /** Property for the default file in folders. */
    public static final String PROPERTY_DEFAULT_FILE = "default-file";

    /** Property for the description. */
    public static final String PROPERTY_DESCRIPTION = "Description";

    /** Property for the resource title. */
    public static final String PROPERTY_ENABLE_NOTIFICATION = "enable-notification";

    /** Property for the static export. */
    public static final String PROPERTY_EXPORT = "export";

    /** Property for the resource export name, during export this name is used instead of the resource name. */
    public static final String PROPERTY_EXPORTNAME = "exportname";

    /** Property for JSP additional suffix during static export, default is "html". */
    public static final String PROPERTY_EXPORTSUFFIX = "exportsuffix";

    /** Property for internal use (e.g. delete). */
    public static final String PROPERTY_INTERNAL = "internal";

    /** Property for the keywords. */
    public static final String PROPERTY_KEYWORDS = "Keywords";

    /** Property for the current locale. */
    public static final String PROPERTY_LOCALE = "locale";

    /** Property for the current locale. */
    public static final String PROPERTY_LOCALE_DEFAULT = "locale-default";
    
    /** Property for the login form. */
    public static final String PROPERTY_LOGIN_FORM = "login-form";

    /** Property constant for <code>"NavImage"</code>. */
    public static final String PROPERTY_NAVIMAGE = "NavImage";

    /** Property constant for <code>"NavInfo"</code>. */
    public static final String PROPERTY_NAVINFO = "NavInfo";

    /** Property for the navigation position. */
    public static final String PROPERTY_NAVPOS = "NavPos";

    /** Property for the navigation text. */
    public static final String PROPERTY_NAVTEXT = "NavText";

    /** Property for the resource title. */
    public static final String PROPERTY_NOTIFICATION_INTERVAL = "notification-interval";
    
    /** Property for the relative root link substitution. */
    public static final String PROPERTY_RELATIVEROOT = "relativeroot";

    /** Property to sort search results in categories. */
    public static final String PROPERTY_SEARCH_CATEGORY = "category";

    /** Property to define a bean for search content extraction. */
    public static final String PROPERTY_SEARCH_EXTRACTIONCLASS = "search.extractionclass";

    /** Property to boost certain search results. */
    public static final String PROPERTY_SEARCH_PRIORITY = "search.priority";

    /** Property for the secure transmission of resources. */
    public static final String PROPERTY_SECURE = "secure";

    /** Property for the stylesheet of files. */
    public static final String PROPERTY_STYLESHEET = "stylesheet";

    /** Property to control the template. */
    public static final String PROPERTY_TEMPLATE = "template";

    /** Property to control the template. */
    public static final String PROPERTY_TEMPLATE_ELEMENTS = "template-elements";

    /** Property for the resource title. */
    public static final String PROPERTY_TITLE = "Title";
    
    /** Property for the visible method in the administration view. */
    public static final String PROPERTY_VISIBLE = "visiblemethod";

    /** The propertydefinitiontype for resources. */
    public static final int PROPERYDEFINITION_RESOURCE = 1;

    /** The null property definition object. */
    private static final CmsPropertyDefinition NULL_PROPERTY_DEFINITION = new CmsPropertyDefinition(
        CmsUUID.getNullUUID(),
        "");

    /** The id of this property definition. */
    private CmsUUID m_id;

    /** The name of this property definition. */
    private String m_name;

    /**
     * Creates a new CmsPropertydefinition.<p>
     * @param id the id of the property definition
     * @param name the name of the property definition
     */
    public CmsPropertyDefinition(CmsUUID id, String name) {

        m_id = id;
        m_name = name;
    }

    /**
     * Returns the null property definition.<p>
     * 
     * @return the null property definition
     */
    public static CmsPropertyDefinition getNullPropertyDefinition() {

        return CmsPropertyDefinition.NULL_PROPERTY_DEFINITION;
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        return new CmsPropertyDefinition(m_id, m_name);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsPropertyDefinition) {
            return m_name.compareTo(((CmsPropertyDefinition)obj).m_name);
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsPropertyDefinition) {
            return ((CmsPropertyDefinition)obj).m_id.equals(m_id);
        }
        return false;
    }

    /**
     * Returns the id of this property definition.<p>
     *
     * @return id the id of this Propertydefinition
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the name of this property definition.<p>
     *
     * @return name The name of this property definition
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        if (m_name != null) {
            return m_name.hashCode();
        }
        return 0;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[Propertydefinition]");
        result.append(" name:");
        result.append(m_name);
        result.append(" id:");
        result.append(m_id);
        return result.toString();
    }
}
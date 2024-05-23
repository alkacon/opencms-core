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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.A_CmsModeIntEnumeration;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

/**
 * Defines a property name, so that <code>{@link CmsProperty}</code> instances can be created with that name.<p>
 *
 * @since 6.0.0
 */
public class CmsPropertyDefinition implements Cloneable, Comparable<CmsPropertyDefinition> {

    /**
     *  Enumeration class for property types.<p>
     */
    public static final class CmsPropertyType extends A_CmsModeIntEnumeration {

        /** Property value is treated as a link or list of links. */
        protected static final CmsPropertyType LINK = new CmsPropertyType(1);

        /** Property value is not a link. */
        protected static final CmsPropertyType NORMAL = new CmsPropertyType(0);

        /** serializable version id. */
        private static final long serialVersionUID = 74746076708908673L;

        /**
         * Creates a new property type with the given identifier.<p>
         *
         * @param type the mode id to use
         */
        private CmsPropertyType(int type) {

            super(type);
        }

        /**
         * Returns the property definition type for the given type id. <p>
         *
         * If the given String matches no known type <code>{@link #NORMAL}</code>
         * will be returned as the default.<p>
         *
         * @param type the type value to get the property type for
         *
         * @return the property type for the given type value
         */
        public static CmsPropertyType valueOf(int type) {

            switch (type) {
                case 1:
                    return LINK;
                case 0:
                default:
                    return NORMAL;
            }

        }
    }

    /** The name constraints when generating new properties. */
    public static final String NAME_CONSTRAINTS = "-._~$";

    /** Property for the active method in the administration view. */
    public static final String PROPERTY_ACTIV = "activemethod";

    /** Property for the allowed set of locales. */
    public static final String PROPERTY_AVAILABLE_LOCALES = "locale-available";

    /** Property to control the Java class for body. */
    public static final String PROPERTY_BODY_CLASS = "templateclass";

    /** The name of the VFS property that controls the caching. */
    public static final String PROPERTY_CACHE = "cache";

    /** Property used to hide categories from the category widgets in the page/content editor.
     *
     * <p>If this property is set to 'true' on a category, it should never be set to 'false' on one of its subcategory.
     * */
    public static final String PROPERTY_CATEGORY_HIDDEN = "category.hidden";

    /** The property to read an additional category folder from.  */
    public static final String PROPERTY_CATEGORY_REPOSITORY = "category.repository";

    /** Property to define the function detail container for a template. */
    public static final String PROPERTY_CONTAINER_INFO = "container.info";

    /** Property for the content conversion. */
    public static final String PROPERTY_CONTENT_CONVERSION = "content-conversion";

    /** Property for the content encoding. */
    public static final String PROPERTY_CONTENT_ENCODING = "content-encoding";

    /** Property for the content encoding. */
    public static final String PROPERTY_COPYRIGHT = "Copyright";

    /** Property for the default file in folders. */
    public static final String PROPERTY_DEFAULT_FILE = "default-file";

    /** Property for the days a resource has to be expired to be deleted by the <code>{@link  org.opencms.scheduler.jobs.CmsDeleteExpiredResourcesJob}</code>. */
    public static final String PROPERTY_DELETE_EXPIRED = "delete.expired";

    /** Property for the description. */
    public static final String PROPERTY_DESCRIPTION = "Description";

    /** Property for the description in HTML format. */
    public static final String PROPERTY_DESCRIPTION_HTML = "Description.html";

    /** Property to set the display order of a content in lists. */
    public static final String PROPERTY_DISPLAY_ORDER = "display-order";

    /** The name of the property which controls whether an element will be used as a copy model by the container page editor. */
    public static final String PROPERTY_ELEMENT_MODEL = "element.model";

    /** May contain a path to an element replacement configuration, for use in the 'copy page' dialog. */
    public static final String PROPERTY_ELEMENT_REPLACEMENTS = "element.replacements";

    /** Property for the resource title. */
    public static final String PROPERTY_ENABLE_NOTIFICATION = "enable-notification";

    /** Property for the static export. */
    public static final String PROPERTY_EXPORT = "export";

    /** Property used to record the resource type for resources whose type is unknown at import time, so they can be exported with that type later. */
    public static final String PROPERTY_EXPORT_TYPE = "export.type";

    /** Property for the resource export name, during export this name is used instead of the resource name. */
    public static final String PROPERTY_EXPORTNAME = "exportname";

    /** Property for JSP additional suffix during static export, default is "html". */
    public static final String PROPERTY_EXPORTSUFFIX = "exportsuffix";

    /** Property to control the folders where template or default bodies should be available. */
    public static final String PROPERTY_FOLDERS_AVAILABLE = "folders.available";

    /** Property stating where to create new gallery folders. */
    public static final String PROPERTY_GALLERIES_FOLDER = "galleries.folder";

    /** Property containing the maps API key. */
    public static final String PROPERTY_GOOGLE_API_KEY = "google.apikey";

    /** Property containing the maps API key. */
    public static final String PROPERTY_GOOGLE_API_KEY_WORKPLACE = "google.apikey.workplace";

    /** Property to control whether historic versions should be removed when deleted resources are published. */
    public static final String PROPERTY_HISTORY_REMOVE_DELETED = "history.removedeleted";

    /** Name of the property in which the focal point is stored. */
    public static final String PROPERTY_IMAGE_FOCAL_POINT = CmsGwtConstants.PROPERTY_IMAGE_FOCALPOINT;

    /** Property constant for <code>"image.size"</code>. */
    public static final String PROPERTY_IMAGE_SIZE = "image.size";

    /** The property for defining the date (as Solr field) that should be used for sorting in lists. */
    public static final String PROPERTY_INSTANCEDATE_COPYFIELD = "instancedate.copyfield";

    /** Property for the keywords. */
    public static final String PROPERTY_KEYWORDS = "Keywords";

    /** Property to enable __forceAbsoluteLinks request parameter for a resource. */
    public static final String PROPERTY_LINKS_FORCEABSOLUTE_ENABLED = "links.forceabsolute.enabled";

    /** Property for the current locale. */
    public static final String PROPERTY_LOCALE = "locale";

    /** Property to mark detail pages to have locale independent detail only containers. */
    public static final String PROPERTY_LOCALE_INDEPENDENT_DETAILS = "locale.independent-details";

    /** Property for the 'do not translate' marking in the sitemap editor. */
    public static final String PROPERTY_LOCALE_NOTRANSLATION = "locale.notranslation";

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

    /** Property name that defines the available resource types for the "new" dialog. */
    public static final String PROPERTY_RESTYPES_AVAILABLE = "restypes.available";

    /** Property to sort search results in categories. */
    public static final String PROPERTY_SEARCH_CATEGORY = "category";

    /** Property to exclude individual resources from search index generation. */
    public static final String PROPERTY_SEARCH_EXCLUDE = "search.exclude";

    /** Property to exclude individual resources from search results online (Solr only). */
    public static final String PROPERTY_SEARCH_EXCLUDE_ONLINE = "search.exclude.online";

    /** Property to boost certain search results. */
    public static final String PROPERTY_SEARCH_PRIORITY = "search.priority";

    /** Property for secondary locales for use in the locale compare view in the sitemap editor. */
    public static final String PROPERTY_SECONDARY_LOCALES = "locale.secondary";

    /** Property for the secure transmission of resources. */
    public static final String PROPERTY_SECURE = "secure";

    /** Property for the stylesheet of files. */
    public static final String PROPERTY_STYLESHEET = "stylesheet";

    /** Property to control the template. */
    public static final String PROPERTY_TEMPLATE = "template";

    /** Property to customize for which templates elements should be displayed in the gallery when using the CmsTransformerTemplateProvider. */
    public static final String PROPERTY_TEMPLATE_COMPATILIBITY = "template.compatibility";

    /** Property for specifying a list of container types used to match formatters in the display formatter selection widget. */
    public static final String PROPERTY_TEMPLATE_DISPLAY_TYPES = "template.display.types";

    /** Property to control the template elements. */
    public static final String PROPERTY_TEMPLATE_ELEMENTS = "template-elements";

    /** Property for the template image. */
    public static final String PROPERTY_TEMPLATE_IMAGE = "template.image";

    /** Property to configure the value which should be used instead of the template path when selecting the template in the GUI. Please note that this does not have to actually be a template provider configuration string, this is just the most common use case.  */
    public static final String PROPERTY_TEMPLATE_PROVIDER = "template.provider";

    /** Property for the resource title. */
    public static final String PROPERTY_TITLE = "Title";

    /** Property for user data request configuration. */
    public static final String PROPERTY_UDR_CONFIG = "udr.config";

    /** Property used to configure default organizational unit. */
    public static final String PROPERTY_UDR_DEFAULTOU = "udr.defaultou";

    /** Name of the property used to control whether mapped URL names should replace previous URL names. */
    public static final String PROPERTY_URLNAME_REPLACE = "urlname.replace";

    /** Property for the visible method in the administration view. */
    public static final String PROPERTY_VISIBLE = "visiblemethod";

    /** Property for the XML sitemap change frequency. */
    public static final String PROPERTY_XMLSITEMAP_CHANGEFREQ = "xmlsitemap.changefreq";

    /** Property for the XML sitemap priority. */
    public static final String PROPERTY_XMLSITEMAP_PRIORITY = "xmlsitemap.priority";

    /** The property definition type for resources. */
    public static final int PROPERYDEFINITION_RESOURCE = 1;

    /** Property value is treated as a link or list of links. */
    public static final CmsPropertyType TYPE_LINK = CmsPropertyType.LINK;

    /** Property value is not a link. */
    public static final CmsPropertyType TYPE_NORMAL = CmsPropertyType.NORMAL;

    /** The null property definition object. */
    private static final CmsPropertyDefinition NULL_PROPERTY_DEFINITION = new CmsPropertyDefinition(
        CmsUUID.getNullUUID(),
        "",
        TYPE_NORMAL);

    /** The id of this property definition. */
    private CmsUUID m_id;

    /** The name of this property definition. */
    private String m_name;

    /** The type of this property definition.*/
    private CmsPropertyType m_type;

    /**
     * Creates a new property definition object with the type
     * <code>{@link #TYPE_NORMAL}</code>.<p>
     *
     * @param id the id of the property definition
     * @param name the name of the property definition
     */
    public CmsPropertyDefinition(CmsUUID id, String name) {

        this(id, name, TYPE_NORMAL);
    }

    /**
     * Creates a new property definition object.<p>
     *
     * @param id the id of the property definition
     * @param name the name of the property definition
     * @param propertyType the type of the property
     */
    public CmsPropertyDefinition(CmsUUID id, String name, CmsPropertyType propertyType) {

        m_id = id;
        m_name = name;
        m_type = propertyType;
    }

    /**
     * Checks if the provided property name is a valid property name,
     * that is contains only valid characters.<p>
     *
     * A property name can only be composed of digits,
     * standard ASCII letters and the symbols defined in {@link #NAME_CONSTRAINTS}.<p>
     *
     * @param name the property name to check
     *
     * @throws CmsIllegalArgumentException if the given property name is not valid
     */
    public static void checkPropertyName(String name) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_BAD_PROPERTYNAME_EMPTY_0, name));
        }

        CmsStringUtil.checkName(name, NAME_CONSTRAINTS, Messages.ERR_BAD_PROPERTYNAME_4, Messages.get());
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
    @Override
    public Object clone() {

        return new CmsPropertyDefinition(m_id, m_name, m_type);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsPropertyDefinition obj) {

        if (obj == this) {
            return 0;
        }
        return m_name.compareTo(obj.m_name);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
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
     * Returns the the type of this property definition.<p>
     *
     * @return the type of this property definition
     */
    public CmsPropertyType getType() {

        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if (m_name != null) {
            return m_name.hashCode();
        }
        return 0;
    }

    /**
     * Sets the type for this property definition.<p>
     *
     * @param type the type to set
     */
    public void setType(CmsPropertyType type) {

        m_type = type;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[Propertydefinition]");
        result.append(" name:");
        result.append(m_name);
        result.append(" id:");
        result.append(m_id);
        result.append(" type:");
        result.append(m_type);
        return result.toString();
    }
}

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

package org.opencms.jsp;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.Locale;
import java.util.Map;

/**
 * Bean to collect navigation information from a resource in the OpenCms VFS.<p>
 *
 * Each navigation element contains a number of information about a VFS resource,
 * obtained either from the resources properties or attributes.
 * You can use this information to generate a HTML navigation for
 * files in the VFS in your template.<p>
 *
 * Note: this class has a natural ordering that is inconsistent with equals.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.jsp.CmsJspNavBuilder
 */
public class CmsJspNavElement implements Comparable<CmsJspNavElement> {

    /** The locale for which the property should be read. */
    protected Locale m_locale;

    /** The navigation position has changed flag. */
    private boolean m_changedNavPos;

    /** The file name. */
    private String m_fileName;

    /** The has navigation flag. */
    private Boolean m_hasNav;

    /** Flag indicating whether this is a hidden navigation entry. */
    private Boolean m_isHiddenNavigationEntry;

    /** The properties accessed according to the chosen locale. */
    private Map<String, String> m_localeProperties;

    /** The navigation tree level. */
    private int m_navTreeLevel = Integer.MIN_VALUE;

    /** The navigation position. */
    private float m_position;

    /** The properties. */
    private Map<String, String> m_properties;

    /** The resource. */
    private CmsResource m_resource;

    /** The site path. */
    private String m_sitePath;

    /** The navigation text. */
    private String m_text;

    /**
     * Empty constructor required for every JavaBean, does nothing.<p>
     *
     * Call one of the init methods after you have created an instance
     * of the bean. Instead of using the constructor you should use
     * the static factory methods provided by this class to create
     * navigation beans that are properly initialized with current
     * OpenCms context.<p>
     *
     * @see CmsJspNavBuilder#getNavigationForResource()
     * @see CmsJspNavBuilder#getNavigationForFolder()
     * @see CmsJspNavBuilder#getNavigationTreeForFolder(int, int)
     */
    public CmsJspNavElement() {

        // empty
    }

    /**
     * Create a new instance of the bean and calls the init method
     * with the provided parameters.<p>
     *
     * @param sitePath will be passed to <code>init</code>
     * @param resource the resource
     * @param properties will be passed to <code>init</code>
     */
    public CmsJspNavElement(String sitePath, CmsResource resource, Map<String, String> properties) {

        setResource(resource);
        init(sitePath, properties);
    }

    /**
     * Create a new instance of the bean and calls the init method
     * with the provided parameters.<p>
     *
     * @param sitePath will be passed to <code>init</code>
     * @param resource the resource
     * @param properties will be passed to <code>init</code>
     * @param navTreeLevel will be passed to <code>init</code>
     *
     * @see #init(String, Map, int, Locale)
     */
    public CmsJspNavElement(String sitePath, CmsResource resource, Map<String, String> properties, int navTreeLevel) {
        this(sitePath, resource, properties, navTreeLevel, null);

    }

    /**
     * Create a new instance of the bean and calls the init method
     * with the provided parameters.<p>
     *
     * @param sitePath will be passed to <code>init</code>
     * @param resource the resource
     * @param properties will be passed to <code>init</code>
     * @param navTreeLevel will be passed to <code>init</code>
     * @param locale the locale for which properties should be accessed.
     *
     * @see #init(String, Map, int, Locale)
     */
    public CmsJspNavElement(
        String sitePath,
        CmsResource resource,
        Map<String, String> properties,
        int navTreeLevel,
        Locale locale) {

        setResource(resource);
        init(sitePath, properties, navTreeLevel, locale);
    }

    /**
     * Create a new instance of the bean and calls the init method
     * with the provided parameters.<p>
     *
     * @param sitePath will be passed to <code>init</code>
     * @param properties will be passed to <code>init</code>
     *
     * @see #init(String, Map)
     *
     * @deprecated use {@link #CmsJspNavElement(String, CmsResource, Map)}
     */
    @Deprecated
    public CmsJspNavElement(String sitePath, Map<String, String> properties) {

        init(sitePath, properties, -1, null);
    }

    /**
     * Create a new instance of the bean and calls the init method
     * with the provided parameters.<p>
     *
     * @param sitePath will be passed to <code>init</code>
     * @param properties will be passed to <code>init</code>
     * @param navTreeLevel will be passed to <code>init</code>
     *
     * @see #init(String, Map, int, Locale)
     *
     * @deprecated use {@link #CmsJspNavElement(String, CmsResource, Map, int)}
     */
    @Deprecated
    public CmsJspNavElement(String sitePath, Map<String, String> properties, int navTreeLevel) {

        init(sitePath, properties, navTreeLevel, null);
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.<p>
     *
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(CmsJspNavElement obj) {

        if (obj == this) {
            return 0;
        }
        float pos = obj.getNavPosition();
        // please note: can't just subtract and cast to int here because of float precision loss
        if (m_position == pos) {
            return 0;
        }
        return (m_position < pos) ? -1 : 1;
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.<p>
     *
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsJspNavElement) {
            return ((CmsJspNavElement)obj).m_sitePath.equals(m_sitePath);
        }
        return false;
    }

    /**
     * Returns the value of the property PROPERTY_DESCRIPTION of this navigation element,
     * or <code>null</code> if this property is not set.<p>
     *
     * @return the value of the property PROPERTY_DESCRIPTION of this navigation element
     *          or <code>null</code> if this property is not set
     */
    public String getDescription() {

        return getProperties().get(CmsPropertyDefinition.PROPERTY_DESCRIPTION);
    }

    /**
     * Returns the filename of the navigation element, i.e.
     * the name of the navigation resource without any path information.<p>
     *
     * @return the filename of the navigation element, i.e.
     *          the name of the navigation resource without any path information
     */
    public String getFileName() {

        if (m_fileName == null) {
            // use "lazy initializing"
            if (!m_sitePath.endsWith("/")) {
                m_fileName = m_sitePath.substring(m_sitePath.lastIndexOf("/") + 1, m_sitePath.length());
            } else {
                m_fileName = m_sitePath.substring(
                    m_sitePath.substring(0, m_sitePath.length() - 1).lastIndexOf("/") + 1,
                    m_sitePath.length());
            }
        }
        return m_fileName;
    }

    /**
     * Returns the value of the property <code>{@link CmsPropertyDefinition#PROPERTY_NAVINFO}</code> of this
     * navigation element, or <code>null</code> if this property is not set.<p>
     *
     * @return the value of the property or <code>null</code> if this property is not set
     */
    public String getInfo() {

        return getProperties().get(CmsPropertyDefinition.PROPERTY_NAVINFO);
    }

    /**
     * Returns the value of the property <code>{@link CmsPropertyDefinition#PROPERTY_LOCALE}</code> of this
     * navigation element, or <code>null</code> if this property is not set.<p>
     *
     * @return the value of the property or <code>null</code> if this property is not set
     */
    public String getLocale() {

        return getProperties().get(CmsPropertyDefinition.PROPERTY_LOCALE);
    }

    /**
     * Returns the value of the property <code>{@link CmsPropertyDefinition#PROPERTY_NAVIMAGE}</code> of this
     * navigation element, or <code>null</code> if this property is not set.<p>
     *
     * @return the value of the property or <code>null</code> if this property is not set
     */
    public String getNavImage() {

        return getProperties().get(CmsPropertyDefinition.PROPERTY_NAVIMAGE);
    }

    /**
     * Returns the value of the property C_PROPERTY_NAVPOS converted to a <code>float</code>,
     * or a value of <code>Float.MAX_VALUE</code> if the navigation position property is not
     * set (or not a valid number) for this resource.<p>
     *
     * @return float the value of the property C_PROPERTY_NAVPOS converted to a <code>float</code>,
     *          or a value of <code>Float.MAX_VALUE</code> if the navigation position property is not
     *          set (or not a valid number) for this resource
     */
    public float getNavPosition() {

        return m_position;
    }

    /**
     * Returns the value of the property PROPERTY_NAVTEXT of this navigation element,
     * or a warning message if this property is not set
     * (this method will never return <code>null</code>).<p>
     *
     * @return the value of the property PROPERTY_NAVTEXT of this navigation element,
     *          or a warning message if this property is not set
     *          (this method will never return <code>null</code>)
     */
    public String getNavText() {

        if (m_text == null) {
            // use "lazy initializing"
            m_text = getProperties().get(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            if (m_text == null) {
                m_text = CmsMessages.formatUnknownKey(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            }
        }
        return m_text;
    }

    /**
     * Returns the navigation tree level of this resource.<p>
     *
     * @return the navigation tree level of this resource
     */
    public int getNavTreeLevel() {

        if (m_navTreeLevel < 0) {
            // use "lazy initializing"
            m_navTreeLevel = CmsResource.getPathLevel(m_sitePath);
        }
        return m_navTreeLevel;
    }

    /**
     * Returns the name of the parent folder of the resource of this navigation element.<p>
     *
     * @return the name of the parent folder of the resource of this navigation element
     */
    public String getParentFolderName() {

        return CmsResource.getParentFolder(m_sitePath);
    }

    /**
     * Returns the original map of all file properties of the resource that
     * the navigation element belongs to.<p>
     *
     * Please note that the original reference is returned, so be careful when making
     * changes to the map.<p>
     *
     * @return the original map of all file properties of the resource that
     *          the navigation element belongs to
     */
    public Map<String, String> getProperties() {

        if (null == m_locale) {
            return m_properties;
        } else {
            return getLocaleProperties();
        }
    }

    /**
     * Returns the value of the selected property from this navigation element.<p>
     *
     * The navigation element contains a hash of all file properties of the resource that
     * the navigation element belongs to.<p>
     *
     * @param key the property name to look up
     *
     * @return the value of the selected property
     */
    public String getProperty(String key) {

        return getProperties().get(key);
    }

    /**
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the resource name this navigation element was initialized with.<p>
     *
     * @return the resource name this navigation element was initialized with
     */
    public String getResourceName() {

        return m_sitePath;
    }

    /**
     * Returns the value of the property PROPERTY_TITLE of this navigation element,
     * or <code>null</code> if this property is not set.<p>
     *
     * @return the value of the property PROPERTY_TITLE of this navigation element
     *          or <code>null</code> if this property is not set
     */
    public String getTitle() {

        return getProperties().get(CmsPropertyDefinition.PROPERTY_TITLE);
    }

    /**
     * Returns if the navigation position has been changed since initialization.<p>
     *
     * @return <code>true</code> if the navigation position has been changed since initialization
     */
    public boolean hasChangedNavPosition() {

        return m_changedNavPos;
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.<p>
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_sitePath.hashCode();
    }

    /**
     * Same as calling {@link #init(String, Map, int, Locale)
     * init(String, Hashtable, -1, null)}.<p>
     *
     * @param resource the name of the resource to extract the navigation
     *     information from
     * @param properties the properties of the resource read from the vfs
     */
    public void init(String resource, Map<String, String> properties) {

        init(resource, properties, -1, null);
    }

    /**
     * Initialized the member variables of this bean with the values
     * provided.<p>
     *
     * A resource will be in the navigation if at least one of the two properties
     * <code>I_CmsConstants.PROPERTY_NAVTEXT</code> or
     * <code>I_CmsConstants.PROPERTY_NAVPOS</code> is set. Otherwise
     * it will be ignored.<p>
     *
     * This bean does provides static methods to create a new instance
     * from the context of a current CmsObject. Call these static methods
     * in order to get a properly initialized bean.<p>
     *
     * @param resource the name of the resource to extract the navigation
     *     information from
     * @param properties the properties of the resource read from the vfs
     * @param navTreeLevel tree level of this resource, for building
     *     navigation trees
     *
     * @see CmsJspNavBuilder#getNavigationForResource()
     */
    public void init(String resource, Map<String, String> properties, int navTreeLevel) {

        init(resource, properties, navTreeLevel, null);
    }

    /**
     * Initialized the member variables of this bean with the values
     * provided.<p>
     *
     * A resource will be in the navigation if at least one of the two properties
     * <code>I_CmsConstants.PROPERTY_NAVTEXT</code> or
     * <code>I_CmsConstants.PROPERTY_NAVPOS</code> is set. Otherwise
     * it will be ignored.<p>
     *
     * This bean does provides static methods to create a new instance
     * from the context of a current CmsObject. Call these static methods
     * in order to get a properly initialized bean.<p>
     *
     * @param resource the name of the resource to extract the navigation
     *     information from
     * @param properties the properties of the resource read from the vfs
     * @param navTreeLevel tree level of this resource, for building
     *     navigation trees
     * @param locale The locale for which properties should be accessed.
     *
     * @see CmsJspNavBuilder#getNavigationForResource()
     */
    public void init(String resource, Map<String, String> properties, int navTreeLevel, Locale locale) {

        m_sitePath = resource;
        m_properties = properties;
        m_navTreeLevel = navTreeLevel;
        m_locale = locale;
        // init the position value
        m_position = Float.MAX_VALUE;
        try {
            m_position = Float.parseFloat(getProperties().get(CmsPropertyDefinition.PROPERTY_NAVPOS));
        } catch (@SuppressWarnings("unused") Exception e) {
            // m_position will have Float.MAX_VALUE, so navigation element will
            // appear last in navigation
        }
    }

    /**
     * Returns <code>true</code> if this navigation element describes a folder,
     * <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if this navigation element describes a folder,
     *          <code>false</code> otherwise.<p>
     */
    public boolean isFolderLink() {

        return m_sitePath.endsWith("/");
    }

    /**
     * Returns if this is a hidden navigation entry.<p>
     *
     * @return <code>true</code> if this is a hidden navigation entry
     */
    public boolean isHiddenNavigationEntry() {

        if (m_isHiddenNavigationEntry == null) {
            // use "lazy initializing"
            String navInfo = getProperties().get(CmsPropertyDefinition.PROPERTY_NAVINFO);
            m_isHiddenNavigationEntry = Boolean.valueOf(CmsClientSitemapEntry.HIDDEN_NAVIGATION_ENTRY.equals(navInfo));
        }
        return m_isHiddenNavigationEntry.booleanValue();
    }

    /**
     * Returns <code>true</code> if this navigation element is in the navigation,
     * <code>false</code> otherwise.<p>
     *
     * A resource is considered to be in the navigation, if <ol>
     * <li>it has the property PROPERTY_NAVTEXT set
     * <li><em>or</em> it has the property PROPERTY_NAVPOS set
     * <li><em>and</em> it is not a temporary file as defined by {@link CmsResource#isTemporaryFileName(String)}.</ol>
     *
     * @return <code>true</code> if this navigation element is in the navigation, <code>false</code> otherwise
     */
    public boolean isInNavigation() {

        if (m_hasNav == null) {
            // use "lazy initializing"
            Object o1 = getProperties().get(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            Object o2 = getProperties().get(CmsPropertyDefinition.PROPERTY_NAVPOS);
            m_hasNav = Boolean.valueOf(((o1 != null) || (o2 != null)) && !CmsResource.isTemporaryFileName(m_sitePath));
        }
        return m_hasNav.booleanValue();
    }

    /**
     * Returns if the navigation element represents a navigation level, linking to it's first sub-element.<p>
     *
     * @return <code>true</code> if the navigation element represents a navigation level
     */
    public boolean isNavigationLevel() {

        return CmsJspNavBuilder.NAVIGATION_LEVEL_FOLDER.equals(
            getProperties().get(CmsPropertyDefinition.PROPERTY_DEFAULT_FILE));
    }

    /**
     * Sets the value that will be returned by the {@link #getNavPosition()}
     * method of this class.<p>
     *
     * @param value the value to set
     */
    public void setNavPosition(float value) {

        m_position = value;
        m_changedNavPos = true;
    }

    /**
     * Returns the site path of the target resource. This may not be the same as the navigation resource.<p>
     *
     * @return the target resource site path
     */
    protected String getSitePath() {

        return m_sitePath;
    }

    /**
     * Sets the resource.<p>
     *
     * @param resource the resource to set
     */
    protected void setResource(CmsResource resource) {

        m_resource = resource;
    }

    /**
     * Helper to get locale specific properties.
     * @return the locale specific properties map.
     */
    private Map<String, String> getLocaleProperties() {

        if (m_localeProperties == null) {
            m_localeProperties = CmsCollectionsGenericWrapper.createLazyMap(
                new CmsProperty.CmsPropertyLocaleTransformer(m_properties, m_locale));
        }
        return m_localeProperties;
    }

}
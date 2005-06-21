/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspNavElement.java,v $
 * Date   : $Date: 2005/06/21 15:49:58 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;

import java.util.Map;

/**
 * Bean to collect navigation information from a resource in the OpenCms VFS.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.8 $
 */
public class CmsJspNavElement implements Comparable {

    /** Property constant for <code>"locale"</code>. */
    public static final String C_PROPERTY_LOCALE = "locale";
    
    /** Property constant for <code>"NavImage"</code>. */
    public static final String C_PROPERTY_NAVIMAGE = "NavImage";
    
    /** Property constant for <code>"NavInfo"</code>. */
    public static final String C_PROPERTY_NAVINFO = "NavInfo";

    private String m_fileName;
    private Boolean m_hasNav;
    private int m_navTreeLevel = Integer.MIN_VALUE;
    private float m_position;
    private Map m_properties;
    private String m_resource;
    private String m_text;

    /**
     * Empty constructor required for every JavaBean, does nothing.<p>
     * 
     * Call one of the init methods afer you have created an instance 
     * of the bean. Instead of using the constructor you should use 
     * the static factory methods provided by this class to create
     * navigation beans that are properly initialized with current 
     * OpenCms context.
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
     * with the provided parametes.<p>
     * 
     * @param resource will be passed to <code>init</code>
     * @param properties will be passed to <code>init</code>
     * 
     * @see #init(String, Map)
     */
    public CmsJspNavElement(String resource, Map properties) {

        init(resource, properties, -1);
    }

    /**
     * Create a new instance of the bean and calls the init method 
     * with the provided parametes.<p>
     * 
     * @param resource will be passed to <code>init</code>
     * @param properties will be passed to <code>init</code>
     * @param navTreeLevel will be passed to <code>init</code>
     * 
     * @see #init(String, Map, int)
     */
    public CmsJspNavElement(String resource, Map properties, int navTreeLevel) {

        init(resource, properties, navTreeLevel);
    }

    /**
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsJspNavElement) {
            float pos = ((CmsJspNavElement)obj).getNavPosition();
            // please note: can't just substract and cast to int here because of float precision loss
            if (m_position == pos) {
                return 0;
            }
            return (m_position < pos) ? -1 : 1;
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsJspNavElement) {
            return ((CmsJspNavElement)obj).m_resource.equals(m_resource);
        }
        return false;
    }

    /**
     * Returns the value of the property C_PROPERTY_DESCRIPTION of this nav element,
     * or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property C_PROPERTY_DESCRIPTION of this nav element
     * or <code>null</code> if this property is not set
     */
    public String getDescription() {

        return (String)m_properties.get(CmsPropertyDefinition.PROPERTY_DESCRIPTION);
    }

    /**
     * Returns the filename of the nav element, i.e.
     * the name of the nav resource without any path information.<p>
     * 
     * @return the filename of the nav element, i.e.
     * the name of the nav resource without any path information
     */
    public String getFileName() {

        if (m_fileName == null) {
            // use "lazy initialiting"
            if (!m_resource.endsWith("/")) {
                m_fileName = m_resource.substring(m_resource.lastIndexOf("/") + 1, m_resource.length());
            } else {
                m_fileName = m_resource.substring(
                    m_resource.substring(0, m_resource.length() - 1).lastIndexOf("/") + 1,
                    m_resource.length());
            }
        }
        return m_fileName;
    }

    /**
     * Returns the value of the property C_PROPERTY_NAVINFO of this nav element,
     * or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property C_PROPERTY_NAVINFO of this nav element
     * or <code>null</code> if this property is not set
     */
    public String getInfo() {

        return (String)m_properties.get(C_PROPERTY_NAVINFO);
    }

    /**
     * Returns the value of the property C_PROPERTY_LOCALE of this nav element,
     * or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property C_PROPERTY_LOCALE of this nav element
     * or <code>null</code> if this property is not set
     */
    public String getLocale() {

        return (String)m_properties.get(C_PROPERTY_LOCALE);
    }

    /**
     * Returns the value of the property C_PROPERTY_NAVIMAGE of this nav element,
     * or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property C_PROPERTY_NAVIMAGE of this nav element
     * or <code>null</code> if this property is not set
     */
    public String getNavImage() {

        return (String)m_properties.get(C_PROPERTY_NAVIMAGE);
    }

    /**
     * Returns the value of the property C_PROPERTY_NAVPOS converted to a <code>float</code>,
     * or a value of <code>Float.MAX_VALUE</code> if the nav position property is not 
     * set (or not a valid number) for this resource.<p>
     * 
     * @return float the value of the property C_PROPERTY_NAVPOS converted to a <code>float</code>,
     * or a value of <code>Float.MAX_VALUE</code> if the nav position property is not 
     * set (or not a valid number) for this resource
     */
    public float getNavPosition() {

        return m_position;
    }

    /**
     * Returns the value of the property C_PROPERTY_NAVTEXT of this nav element,
     * or a warning message if this property is not set 
     * (this method will never return <code>null</code>).<p> 
     * 
     * @return the value of the property C_PROPERTY_NAVTEXT of this nav element,
     * or a warning message if this property is not set 
     * (this method will never return <code>null</code>)
     */
    public String getNavText() {

        if (m_text == null) {
            // use "lazy initialiting"
            m_text = (String)m_properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            if (m_text == null) {
                m_text = CmsMessages.formatUnknownKey(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            }
        }
        return m_text;
    }

    /**
     * Returns the nav tree level of this resource.<p>
     * 
     * @return int the nav tree level of this resource
     */
    public int getNavTreeLevel() {

        if (m_navTreeLevel < 0) {
            // use "lazy initialiting"
            m_navTreeLevel = CmsResource.getPathLevel(m_resource);
        }
        return m_navTreeLevel;
    }

    /**
     * Returns the name of the parent folder of the resource of this nav element.<p>
     * 
     * @return the name of the parent folder of the resource of this nav element
     */
    public String getParentFolderName() {

        return CmsResource.getParentFolder(m_resource);
    }

    /**
     * Returns the original Hashtable of all file properties of the resource that
     * the nav element belongs to.<p>
     * 
     * Please note that the original reference is returned, so be careful when making 
     * changes to the Hashtable.<p>
     * 
     * @return the original Hashtable of all file properties of the resource that
     * the nav element belongs to
     */
    public Map getProperties() {

        return m_properties;
    }

    /**
     * Returns the value of the selected property from this nav element.<p> 
     * 
     * The nav element contains a hash of all file properties of the resource that
     * the nav element belongs to.<p>
     * 
     * @param key the property name to look up
     * @return the value of the selected property
     */
    public String getProperty(String key) {

        return (String)m_properties.get(key);
    }

    /**
     * Returns the resource name this nav element was initalized with.<p>
     * 
     * @return the resource name this nav element was initalized with
     */
    public String getResourceName() {

        return m_resource;
    }

    /**
     * Returns the value of the property C_PROPERTY_TITLE of this nav element,
     * or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property C_PROPERTY_TITLE of this nav element
     * or <code>null</code> if this property is not set
     */
    public String getTitle() {

        return (String)m_properties.get(CmsPropertyDefinition.PROPERTY_TITLE);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return super.hashCode();
    }

    /**
     * Same as calling {@link #init(String, Map, int) 
     * init(String, Hashtable, -1)}.<p>
     * 
     * @param resource the name of the resource to extract the navigation 
     *     information from
     * @param properties the properties of the resource read from the vfs
     */
    public void init(String resource, Map properties) {

        init(resource, properties, -1);
    }

    /**
     * Initialized the member variables of this bean with the values 
     * provided.<p>
     * 
     * A resource will be in the nav if at least one of the two properties 
     * <code>I_CmsConstants.C_PROPERTY_NAVTEXT</code> or 
     * <code>I_CmsConstants.C_PROPERTY_NAVPOS</code> is set. Otherwise
     * it will be ignored.
     * 
     * This bean does provides static methods to create a new instance 
     * from the context of a current CmsObject. Call these static methods
     * in order to get a properly initialized bean.
     * 
     * @param resource the name of the resource to extract the navigation 
     *     information from
     * @param properties the properties of the resource read from the vfs
     * @param navTreeLevel tree level of this resource, for building 
     *     navigation trees
     * 
     * @see CmsJspNavBuilder#getNavigationForResource()
     */
    public void init(String resource, Map properties, int navTreeLevel) {

        m_resource = resource;
        m_properties = properties;
        m_navTreeLevel = navTreeLevel;
        // init the position value
        m_position = Float.MAX_VALUE;
        try {
            m_position = Float.parseFloat((String)m_properties.get(CmsPropertyDefinition.PROPERTY_NAVPOS));
        } catch (Exception e) {
            // m_position will have Float.MAX_VALUE, so nevigation element will 
            // appear last in navigation
        }
    }

    /**
     * Returns <code>true</code> if this nav element describes a folder, <code>false</code>
     * otherwise.<p>
     * 
     * @return <code>true</code> if this nav element describes a folder, <code>false</code>
     * otherwise.<p>
     */
    public boolean isFolderLink() {

        return m_resource.endsWith("/");
    }

    /**
     * Returns <code>true</code> if this nav element is in the navigation, <code>false</code>
     * otherwise.<p>
     * 
     * A resource is considered to be in the navigation, if <ol>
     * <li>it has the property C_PROPERTY_NAVTEXT set
     * <li><em>or</em> it has the property C_PROPERTY_NAVPOS set 
     * <li><em>and</em> it is not a temporary file that contains a '~' in it's filename.</ol> 
     * 
     * @return <code>true</code> if this nav element is in the navigation, <code>false</code>
     * otherwise
     */
    public boolean isInNavigation() {

        if (m_hasNav == null) {
            // use "lazy initialiting"
            Object o1 = m_properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            Object o2 = m_properties.get(CmsPropertyDefinition.PROPERTY_NAVPOS);
            m_hasNav = new Boolean(((o1 != null) || (o2 != null)) && (m_resource.indexOf('~') < 0));
        }
        return m_hasNav.booleanValue();
    }

    /**
     * Sets the value that will be returned by the {@link #getNavPosition()}
     * method of this class.<p>
     * 
     * @param value the value to set
     */
    public void setNavPosition(float value) {

        m_position = value;
    }
}
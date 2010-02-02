/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSiteEntryBean.java,v $
 * Date   : $Date: 2010/02/02 10:06:18 $
 * Version: $Revision: 1.11 $
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One entry in a sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 7.6 
 */
public class CmsSiteEntryBean {

    /** The content id, for detail pages. */
    private CmsUUID m_contentId;

    /** The entry id. */
    private final CmsUUID m_id;

    /** The inherited properties. */
    private Map<String, String> m_inheritedProperties;

    /** The entry name. */
    private final String m_name;

    /** The original uri, without content ID. */
    private final String m_originalUri;

    /** The configured properties. */
    private final Map<String, String> m_properties;

    /** The file's structure id. */
    private final CmsUUID m_resourceId;

    /** Flag to indicate if this is a sitemap or a VFS entry. */
    private final boolean m_sitemap;

    /** The list of sub-entries. */
    private final List<CmsSiteEntryBean> m_subEntries;

    /** The entry title. */
    private final String m_title;

    /**
     * Creates a new VFS entry bean.<p>
     * 
     * @param cms the current CMS context
     * @param uri the current URI
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsSiteEntryBean(CmsObject cms, String uri)
    throws CmsException {

        CmsResource res = cms.readResource(uri);
        m_id = res.getStructureId();
        m_resourceId = res.getStructureId();
        m_name = res.getName();
        m_title = null;
        m_subEntries = Collections.<CmsSiteEntryBean> emptyList();
        m_properties = null;
        m_originalUri = uri;
        m_sitemap = false;
    }

    /**
     * Creates a new sitemap entry bean.<p> 
     * 
     * @param id the entry's id
     * @param originalUri the original, sitemap file dependent, uri
     * @param resourceId the file's structure id
     * @param name the entry's name
     * @param title the entry's title
     * @param properties the properties as a map of name/value pairs
     * @param subEntries the list of sub-entries
     **/
    public CmsSiteEntryBean(
        CmsUUID id,
        String originalUri,
        CmsUUID resourceId,
        String name,
        String title,
        Map<String, String> properties,
        List<CmsSiteEntryBean> subEntries) {

        m_id = id;
        m_resourceId = resourceId;
        m_name = name;
        m_title = title;
        m_subEntries = (subEntries == null
        ? Collections.<CmsSiteEntryBean> emptyList()
        : Collections.unmodifiableList(subEntries));
        // do not freeze the properties
        m_properties = new HashMap<String, String>();
        if (properties != null) {
            m_properties.putAll(properties);
        }
        m_originalUri = originalUri;
        m_sitemap = true;
    }

    /**
     * Returns the content id, for detail pages.<p>
     *
     * @return the content id
     */
    public CmsUUID getContentId() {

        return m_contentId;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the inherited properties.<p>
     * 
     * @return the inherited properties
     */
    public Map<String, String> getInheritedProperties() {

        return m_inheritedProperties;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the original, sitemap file dependent, uri.<p>
     *
     * @return the original, sitemap file dependent, uri
     */
    public String getOriginalUri() {

        return m_originalUri;
    }

    /**
     * Returns the configured properties.<p>
     * 
     * @return the configured properties
     */
    public Map<String, String> getProperties() {

        return Collections.unmodifiableMap(m_properties);
    }

    /**
     * Returns the properties.<p>
     * 
     * @param search if taking into account inherited properties or not
     * 
     * @return the properties
     */
    public Map<String, String> getProperties(boolean search) {

        if (search) {
            return getInheritedProperties();
        } else {
            return getProperties();
        }
    }

    /**
     * Returns the file's structure id.<p>
     *
     * @return the file's structure id
     */
    public CmsUUID getResourceId() {

        return m_resourceId;
    }

    /**
     * Returns the current root uri.<p>
     * 
     * @return the current root uri
     */
    public String getRootPath() {

        StringBuffer sb = new StringBuffer();
        sb.append(getOriginalUri());
        if (getContentId() != null) {
            sb.append(getContentId()).append('/');
        }
        return sb.toString();
    }

    /**
     * Returns the current site uri.<p>
     * 
     * @param cms the current CMS context
     * 
     * @return the current site uri
     */
    public String getSitePath(CmsObject cms) {

        return cms.getRequestContext().removeSiteRoot(getRootPath());
    }

    /**
     * Returns the sub-entries.<p>
     *
     * @return the sub-entries
     */
    public List<CmsSiteEntryBean> getSubEntries() {

        return m_subEntries;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Checks if this is a sitemap entry.<p>
     * 
     * @return <code>true</code> if this is a sitemap entry
     */
    public boolean isSitemap() {

        return m_sitemap;
    }

    /**
     * Checks if this is a VFS entry.<p>
     * 
     * @return <code>true</code> if this is a VFS entry
     */
    public boolean isVfs() {

        return !m_sitemap;
    }

    /**
     * Sets the runtime information.<p>
     * 
     * @param inheritedProperties the inherited properties to set
     * @param position the position to set
     * @param contentId the contentId to set
     */
    public void setRuntimeInfo(Map<String, String> inheritedProperties, int position, CmsUUID contentId) {

        // set the inhereted properties
        m_inheritedProperties = new HashMap<String, String>();
        if (inheritedProperties != null) {
            m_inheritedProperties.putAll(inheritedProperties);
            m_inheritedProperties.putAll(m_properties);
        }
        // set the position
        m_properties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, String.valueOf(position));
        m_inheritedProperties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, String.valueOf(position));
        // set the content id
        m_contentId = contentId;
    }

    /**
     * Flexcache will use this as variation key.<p>
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getRootPath();
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapEntry.java,v $
 * Date   : $Date: 2010/07/23 11:38:25 $
 * Version: $Revision: 1.14 $
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
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * One entry in a sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 7.6 
 */
public class CmsSitemapEntry {

    /** The inherited properties. */
    protected Map<String, String> m_inheritedProperties;

    /** The entry name. */
    protected String m_name;

    /** The original uri, without entry point info nor content ID. */
    protected String m_originalUri;

    /** The position. */
    protected int m_position;

    /** The configured properties. */
    protected final Map<String, String> m_properties;

    /** The content id, for detail pages. */
    private CmsUUID m_contentId;

    /** The entry id. */
    private final CmsUUID m_id;

    /** True if this is a root entry of a root sitemap. */
    private boolean m_isRootEntry;

    /** Flag to indicate if this is a sitemap or a VFS entry. */
    private final boolean m_sitemap;

    /** The file's structure id. */
    private final CmsUUID m_structureId;

    /** The entry title. */
    private final String m_title;

    /**
     * Clone constructor.<p>
     * 
     * @param entry the entry to clone
     */
    public CmsSitemapEntry(CmsSitemapEntry entry) {

        this(
            entry.getId(),
            entry.getOriginalUri(),
            entry.getStructureId(),
            entry.getName(),
            entry.getTitle(),
            entry.isRootEntry(),
            entry.getProperties(),
            entry.getInheritedProperties(),
            entry.getContentId());
    }

    /**
     * Creates a new sitemap entry bean.<p> 
     * 
     * @param id the entry's id
     * @param originalUri the original, sitemap file dependent, uri
     * @param structureId the file's structure id
     * @param name the entry's name
     * @param title the entry's title
     * @param isRoot true if this is the root entry of a root sitemap 
     * @param properties the properties as a map of name/value pairs
     * @param inheritedProperties the properties as a map of name/value pairs
     * @param contentId optional content id
     **/
    public CmsSitemapEntry(
        CmsUUID id,
        String originalUri,
        CmsUUID structureId,
        String name,
        String title,
        boolean isRoot,
        Map<String, String> properties,
        Map<String, String> inheritedProperties,
        CmsUUID contentId) {

        m_id = id;
        m_structureId = structureId;
        m_name = name;
        m_title = title;
        m_isRootEntry = isRoot;
        // do not freeze the properties
        m_properties = new HashMap<String, String>();
        if (properties != null) {
            m_properties.putAll(properties);
        }
        m_inheritedProperties = new HashMap<String, String>();
        if (inheritedProperties != null) {
            m_inheritedProperties.putAll(inheritedProperties);
        }
        m_originalUri = originalUri;
        if (m_originalUri.equals("/")) {
            m_originalUri = "";
        }
        m_sitemap = ((id == null) || !id.equals(structureId));
        m_contentId = contentId;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        if ((o == null) || !(o instanceof CmsSitemapEntry)) {
            return false;
        }
        return m_id.equals(((CmsSitemapEntry)o).getId());
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
     * Returns the position.<p>
     *
     * @return the position
     */
    public int getPosition() {

        return m_position;
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
     * Returns the current root uri.<p>
     * 
     * @return the current root uri
     */
    public String getRootPath() {

        StringBuffer sb = new StringBuffer();
        sb.append("/");
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
     * Returns the file's structure id.<p>
     *
     * @return the file's structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the template that should be used for this sitemap entry.<p>
     * 
     * The template is normally determined by the template-inherited property (which may have
     * been inherited from another sitemap entry), but can be overridden by setting an entry's 
     * template property. Inherited values of the template property will be ignored. If a template
     * can't be found by inspecting those properties, the default value passed as a parameter is returned. 
     *
     * @param defaultValue the value to be returned if no template can be found 
     * @return the template to be used, or the default value 
     */
    public String getTemplate(String defaultValue) {

        Map<String, String> ownProperties = getProperties(false);
        Map<String, String> allProperties = getProperties(true);

        if (ownProperties.containsKey(CmsSitemapManager.Property.template.getName())) {
            return ownProperties.get(CmsSitemapManager.Property.template.getName());
        } else if (allProperties.containsKey(CmsSitemapManager.Property.templateInherited.getName())) {
            return allProperties.get(CmsSitemapManager.Property.templateInherited.getName());
        } else {
            return defaultValue;
        }
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_id.hashCode();
    }

    /**
     * Returns true if this is the root entry of a root sitemap.<p> 
     * 
     * @return true if this is the root entry of a root sitemap 
     */
    public boolean isRootEntry() {

        return m_isRootEntry;
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
     * Sets the 'is root entry of a root sitemap' status to true.<p>
     * 
     * @param isRoot true if this is the root entry of a root sitemap 
     */
    public void setRootEntry(boolean isRoot) {

        m_isRootEntry = isRoot;
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

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsInternalSitemapEntry.java,v $
 * Date   : $Date: 2010/06/08 07:12:45 $
 * Version: $Revision: 1.2 $
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
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One entry in a sitemap with hierarchical structure as read from the XML content.<p>
 * 
 * Mainly for package internal use.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0 
 */
public class CmsInternalSitemapEntry extends CmsSitemapEntry {

    /** The entry point. */
    private String m_entryPoint = "";

    /** The list of sub-entries. */
    private List<CmsInternalSitemapEntry> m_subEntries;

    /**
     * Clone constructor.<p>
     * 
     * @param entry the entry to clone
     */
    public CmsInternalSitemapEntry(CmsInternalSitemapEntry entry) {

        this(
            entry.getId(),
            entry.getOriginalUri(),
            entry.getResourceId(),
            entry.getName(),
            entry.getTitle(),
            entry.getProperties(),
            entry.getSubEntries(),
            entry.getContentId());
        setRuntimeInfo("", entry.getPosition(), entry.getInheritedProperties());
    }

    /**
     * Creates a new VFS entry bean.<p>
     * 
     * @param cms the current CMS context
     * @param uri the current URI
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsInternalSitemapEntry(CmsObject cms, String uri)
    throws CmsException {

        super(create(cms, uri));
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
     * @param contentId optional content id
     **/
    public CmsInternalSitemapEntry(
        CmsUUID id,
        String originalUri,
        CmsUUID resourceId,
        String name,
        String title,
        Map<String, String> properties,
        List<CmsInternalSitemapEntry> subEntries,
        CmsUUID contentId) {

        super(id, originalUri, resourceId, name, title, properties, null, contentId);
        m_subEntries = (subEntries == null
        ? Collections.<CmsInternalSitemapEntry> emptyList()
        : Collections.unmodifiableList(subEntries));
    }

    /**
     * Creates a new sitemap entry from the given resource.<p>
     * 
     * @param cms the current CMS context
     * @param uri the site relative VFS URI of the resource to use
     * 
     * @return a new sitemap entry
     * 
     * @throws CmsException if the resource can not be read
     */
    private static CmsSitemapEntry create(CmsObject cms, String uri) throws CmsException {

        CmsResource res = cms.readResource(uri);
        return new CmsSitemapEntry(
            res.getStructureId(),
            res.getRootPath(),
            res.getStructureId(),
            res.getName(),
            null,
            null,
            null,
            null);
    }

    /**
     * Creates a dummy root entry for a sub-sitemap from this entry.<p>
     * 
     * @param cms the CmsObject to use for VFS operations 

     * @return a dummy sub-sitemap root
     */
    public CmsInternalSitemapEntry copyAsSubSitemapRoot(CmsObject cms) {

        CmsInternalSitemapEntry clone = new CmsInternalSitemapEntry(
            getId(),
            "",
            getResourceId(),
            "",
            getTitle(),
            getProperties(),
            new ArrayList<CmsInternalSitemapEntry>(),
            getContentId());

        clone.setRuntimeInfo(getSitePath(cms), 0, new HashMap<String, String>());
        return clone;
    }

    /**
     * Returns the entry point.<p>
     *
     * @return the entry point, as root path
     */
    public String getEntryPoint() {

        return m_entryPoint;
    }

    /**
     * Returns the current root uri.<p>
     * 
     * @return the current root uri
     */
    @Override
    public String getRootPath() {

        StringBuffer sb = new StringBuffer();
        sb.append(getEntryPoint());
        sb.append(getOriginalUri());
        if (getContentId() != null) {
            sb.append(getContentId()).append('/');
        }
        return sb.toString();
    }

    /**
     * Returns the sub-entries.<p>
     *
     * @return the sub-entries
     */
    public List<CmsInternalSitemapEntry> getSubEntries() {

        return m_subEntries;
    }

    /**
     * Root entries of root sitemaps HAVE to have an empty name,
     * but we can not enforce that while editing the xml, so 
     * we have to enforce it here.<p>
     */
    protected void removeName() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_name)) {
            // nothing to do
            return;
        }
        fixPath(m_name);
        m_name = "";
    }

    /**
     * Sets the runtime information.<p>
     * 
     * @param entryPoint the entry point
     * @param position the position to set
     * @param inheritedProperties the inherited properties to set
     */
    protected void setRuntimeInfo(String entryPoint, int position, Map<String, String> inheritedProperties) {

        // set the inherited properties
        m_inheritedProperties = new HashMap<String, String>();
        if (inheritedProperties != null) {
            // it is important that they are cloned, see CmsSitemapManager#getEntry(...)
            m_inheritedProperties.putAll(inheritedProperties);
            m_inheritedProperties.putAll(m_properties);
        }
        // set the position
        m_properties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, String.valueOf(position));
        m_inheritedProperties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, String.valueOf(position));
        m_position = position;
        m_entryPoint = entryPoint;
    }

    /**
     * Sets the sub-entries.<p>
     * 
     * @param subEntries the sub-entries to set
     */
    protected void setSubEntries(List<CmsInternalSitemapEntry> subEntries) {

        m_subEntries = subEntries;
    }

    /**
     * Fixes the path.<p>
     * 
     * @param name the name to remove from the path
     */
    private void fixPath(String name) {

        int pos = m_originalUri.indexOf(name + "/");
        if (pos < 0) {
            // nothing to do
            return;
        }
        m_originalUri = m_originalUri.substring(0, pos) + m_originalUri.substring(pos + 1 + name.length());
        for (CmsInternalSitemapEntry entry : m_subEntries) {
            entry.fixPath(name);
        }
    }
}

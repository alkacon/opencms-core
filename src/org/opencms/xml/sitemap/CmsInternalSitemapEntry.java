/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsInternalSitemapEntry.java,v $
 * Date   : $Date: 2010/12/17 08:45:29 $
 * Version: $Revision: 1.10 $
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
import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

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
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0 
 */
public class CmsInternalSitemapEntry extends CmsSitemapEntry {

    /** The entry point. */
    private String m_entryPoint = "";

    /** The sitemap info bean associated with this sitemap entry. */
    private CmsSitemapRuntimeInfo m_sitemapInfo;

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
            entry.getStructureId(),
            entry.getName(),
            entry.getTitle(),
            entry.isRootEntry(),
            entry.getNewProperties(),
            entry.getSubEntries(),
            entry.getContentId(),
            entry.getContentName());

        setRuntimeInfo("", entry.getPosition(), entry.getComputedProperties());

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
     * @param isRootEntry true if this entry is a root entry of a root sitemap 
     * @param properties the properties as a map of name/value pairs
     * @param subEntries the list of sub-entries
     * @param contentId optional content id
     * @param contentName optional content name 
     **/
    public CmsInternalSitemapEntry(
        CmsUUID id,
        String originalUri,
        CmsUUID resourceId,
        String name,
        String title,
        boolean isRootEntry,
        Map<String, CmsSimplePropertyValue> properties,
        List<CmsInternalSitemapEntry> subEntries,
        CmsUUID contentId,
        String contentName) {

        super(id, originalUri, resourceId, name, title, isRootEntry, properties, null, contentId, contentName);
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
            false,
            null,
            null,
            null,
            null);
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
            sb.append(getContentName()).append('/');
        }
        return sb.toString();
    }

    /**
     * @see org.opencms.xml.sitemap.CmsSitemapEntry#getSitemapInfo()
     */
    @Override
    public CmsSitemapRuntimeInfo getSitemapInfo() {

        return m_sitemapInfo;
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
     * Sets the runtime entry point of this sitemap entry.<p>
     * 
     * @param entryPoint the new entry point 
     */
    protected void setEntryPoint(String entryPoint) {

        m_entryPoint = entryPoint;
    }

    /**
     * Sets the parent computed properties for this entry.<p>
     * 
     * @param computedProperties the computed properties of this entry's parent 
     */
    protected void setParentComputedProperties(Map<String, CmsComputedPropertyValue> computedProperties) {

        m_parentComputedProperties = computedProperties;
    }

    /**
     * Sets the runtime information.<p>
     * 
     * @param position the position to set
     * @param inheritedProperties the inherited properties to set
     */
    protected void setRuntimeInfo(

    int position, Map<String, CmsComputedPropertyValue> inheritedProperties) {

        m_computedProperties = new HashMap<String, CmsComputedPropertyValue>(inheritedProperties);
        CmsComputedPropertyValue navpos = CmsComputedPropertyValue.create("" + position, null, getRootPath());
        m_computedProperties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, navpos);
        String navposValue = "" + position;
        CmsSimplePropertyValue simpleNavpos = new CmsSimplePropertyValue(navposValue, navposValue);
        m_newProperties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, simpleNavpos);
        m_position = position;
    }

    /**
     * Sets the runtime information including the entry point.<p>
     * 
     * @param entryPoint the new entry point 
     * @param position the position to set 
     * @param inheritedProperties the inherited properties to set 
     */
    protected void setRuntimeInfo(
        String entryPoint,
        int position,
        Map<String, CmsComputedPropertyValue> inheritedProperties) {

        setEntryPoint(entryPoint);
        setRuntimeInfo(position, inheritedProperties);
    }

    /**
     * Sets the sitemap info bean.<p>
     * 
     * @param info the sitemap info bean 
     */
    protected void setSitemapInfo(CmsSitemapRuntimeInfo info) {

        m_sitemapInfo = info;
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

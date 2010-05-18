/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsClientSitemapEntry.java,v $
 * Date   : $Date: 2010/05/18 14:08:53 $
 * Version: $Revision: 1.8 $
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

package org.opencms.ade.sitemap.shared;

import org.opencms.gwt.client.util.CmsMapUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * GWT implementation of {@link org.opencms.xml.sitemap.CmsSitemapEntry}.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapEntry implements IsSerializable {

    /** The entry id. */
    private CmsUUID m_id;

    /** The entry name. */
    private String m_name;

    /** The relative position between siblings. */
    private int m_position;

    /** The property map. */
    private Map<String, String> m_properties;

    /** The sitemap path. */
    private String m_sitePath;

    /** The children. */
    private List<CmsClientSitemapEntry> m_subEntries;

    /** The title. */
    private String m_title;

    /** The VFS path. */
    private String m_vfsPath;

    /**
     * Constructor.<p>
     */
    public CmsClientSitemapEntry() {

        m_subEntries = new ArrayList<CmsClientSitemapEntry>();
    }

    /**
     * Creates a copy without children of the given entry.<p>
     * 
     * @param clone the entry to clone 
     */
    public CmsClientSitemapEntry(CmsClientSitemapEntry clone) {

        this();
        setId(clone.getId());
        setName(clone.getName());
        setProperties(new HashMap<String, String>(clone.getProperties()));
        setSitePath(clone.getSitePath());
        setTitle(clone.getTitle());
        setVfsPath(clone.getVfsPath());
        setPosition(clone.getPosition());
    }

    /**
     * Adds the given entry to the children.<p>
     * 
     * @param entry the entry to add
     */
    public void addSubEntry(CmsClientSitemapEntry entry) {

        entry.setPosition(m_subEntries.size());
        m_subEntries.add(entry);
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
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
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

        return m_properties;
    }

    /**
     * Returns the sitemap path.<p>
     *
     * @return the sitemap path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the children.<p>
     *
     * @return the children
     */
    public List<CmsClientSitemapEntry> getSubEntries() {

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
     * Returns the vfs path.<p>
     *
     * @return the vfs path
     */
    public String getVfsPath() {

        return m_vfsPath;
    }

    /**
     * Inserts the given entry at the given position.<p>
     * 
     * @param entry the entry to insert
     * @param position the position
     */
    public void insertSubEntry(CmsClientSitemapEntry entry, int position) {

        m_subEntries.add(position, entry);
        updatePositions(position);
    }

    /**
     * Removes the child at the given position.<p>
     * 
     * @param position the index of the child to remove
     * 
     * @return the removed child
     */
    public CmsClientSitemapEntry removeSubEntry(int position) {

        CmsClientSitemapEntry removed = m_subEntries.remove(position);
        updatePositions(position);
        return removed;
    }

    /**
     * Sets the id.<p>
     *
     * @param id the id to set
     */
    public void setId(CmsUUID id) {

        m_id = id;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the position.<p>
     *
     * @param position the position to set
     */
    public void setPosition(int position) {

        m_position = position;
    }

    /**
     * Sets the properties.<p>
     *
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String> properties) {

        m_properties = properties;
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the children.<p>
     *
     * @param children the children to set
     */
    public void setSubEntries(List<CmsClientSitemapEntry> children) {

        m_subEntries.clear();
        m_subEntries.addAll(children);
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the VFS path.<p>
     *
     * @param path the path to set
     */
    public void setVfsPath(String path) {

        m_vfsPath = path;
    }

    /**
     * Updates the properties of the sitemap entry.<p>
     * 
     * Entries of the map of properties passed as an argument which have a null value
     * will cause the corresponding property to be deleted.
     * 
     * @param newProperties the properties which should be updated
     */
    public void updateProperties(Map<String, String> newProperties) {

        CmsMapUtil.updateMapAndRemoveNulls(newProperties, m_properties);
    }

    /**
     * Updates all the children positions starting from the given position.<p>
     * 
     * @param position the position to start with
     */
    private void updatePositions(int position) {

        for (int i = position; i < m_subEntries.size(); i++) {
            m_subEntries.get(i).setPosition(i);
        }
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapTreeEntry.java,v $
 * Date   : $Date: 2010/04/20 08:27:48 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Sitemap tree entry.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.shared.CmsClientSitemapEntry
 */
public class CmsSitemapTreeEntry {

    /** The entry's children. */
    private List<CmsSitemapTreeEntry> m_children;

    /** The entry. */
    private CmsClientSitemapEntry m_entry;

    /** The entry's children lookup table. */
    private Map<String, CmsSitemapTreeEntry> m_lookup;

    /**
     * Constructor.<p>
     * 
     * @param entry the entry
     */
    public CmsSitemapTreeEntry(CmsClientSitemapEntry entry) {

        m_entry = entry;
        m_children = new ArrayList<CmsSitemapTreeEntry>();
        m_lookup = new HashMap<String, CmsSitemapTreeEntry>();
    }

    /**
     * Adds a new child to this entry.<p>
     * 
     * @param child the child to add
     */
    public void addChild(CmsClientSitemapEntry child) {

        CmsSitemapTreeEntry childElem = new CmsSitemapTreeEntry(child);
        m_children.add(childElem);
        m_lookup.put(child.getName(), childElem);
    }

    /**
     * Adds the children to this entry.<p>
     * 
     * @param children the children to add
     */
    public void addChildren(List<CmsClientSitemapEntry> children) {

        for (CmsClientSitemapEntry entry : children) {
            addChild(entry);
        }
    }

    /**
     * Returns the child with the given name.<p>
     * 
     * @param name the child's name
     * 
     * @return the child with the given name
     */
    public CmsSitemapTreeEntry getChild(String name) {

        return m_lookup.get(name);
    }

    /**
     * Returns the children.<p>
     *
     * @return the children
     */
    public List<CmsSitemapTreeEntry> getChildren() {

        return m_children;
    }

    /**
     * Returns the entry.<p>
     *
     * @return the entry
     */
    public CmsClientSitemapEntry getEntry() {

        return m_entry;
    }

    /**
     * Inserts a new child at the given position.<p>
     * 
     * @param child the entry to insert
     * @param position the position to be inserted
     */
    public void insertChild(CmsClientSitemapEntry child, int position) {

        CmsSitemapTreeEntry childEntry = new CmsSitemapTreeEntry(child);
        m_children.add(position, childEntry);
        m_lookup.put(child.getName(), childEntry);
    }

    /**
     * Inserts a new child at the given position.<p>
     * 
     * @param child the tree entry to insert
     * @param position the position to be inserted
     */
    public void insertChild(CmsSitemapTreeEntry child, int position) {

        m_children.add(position, child);
        m_lookup.put(child.getEntry().getName(), child);
        child.updatePath(m_entry.getSitePath());
    }

    /**
     * Removes the child with the given name.<p>
     * 
     * @param name the name of the child to remove 
     * 
     * @return the removed child
     */
    public CmsSitemapTreeEntry removeChild(String name) {

        CmsSitemapTreeEntry childToRemove = m_lookup.remove(name);
        if (childToRemove == null) {
            return childToRemove;
        }
        Iterator<CmsSitemapTreeEntry> it = m_children.iterator();
        while (it.hasNext()) {
            CmsSitemapTreeEntry child = it.next();
            if (child == childToRemove) {
                it.remove();
                break;
            }
        }
        return childToRemove;
    }

    /**
     * Sets the underlying sitemap entry.<p>
     * 
     * @param entry the entry to set
     */
    public void setEntry(CmsClientSitemapEntry entry) {

        m_entry = entry;
    }

    /**
     * Updates the path for this entry and all its descendants.<p>
     * 
     * @param parentPath the new parent path
     */
    private void updatePath(String parentPath) {

        String newPath = parentPath + "/" + m_entry.getName();
        if (newPath.equals(m_entry.getSitePath())) {
            return;
        }
        m_entry.setSitePath(newPath);
        for (CmsSitemapTreeEntry child : m_children) {
            child.updatePath(newPath);
        }
    }
}

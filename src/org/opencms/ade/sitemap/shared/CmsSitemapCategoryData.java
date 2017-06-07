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

import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.util.CmsUUID;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The category data for the current sitemap.<p>
 */
public class CmsSitemapCategoryData implements IsSerializable {

    /** The base path for the categories. */
    private String m_basePath;

    /** A forest of categories. */
    private List<CmsCategoryTreeEntry> m_categoryEntries = Lists.newArrayList();

    /**
     * Default constructor.<p>
     */
    public CmsSitemapCategoryData() {

    }

    /**
     * Adds a new category entry.<p>
     *
     * @param item the category entry
     */
    public void add(CmsCategoryTreeEntry item) {

        m_categoryEntries.add(item);
    }

    /**
     * Gets a multimap of the top-level entries, indexed by whether they are local categories or not.<p>
     *
     * A category counts as local if all of its parent categories are defined in the current sitemap.
     *
     * @return the multimap of entries
     */
    public Multimap<Boolean, CmsCategoryTreeEntry> getEntriesIndexedByLocality() {

        return ArrayListMultimap.create(
            Multimaps.index(m_categoryEntries, new Function<CmsCategoryTreeEntry, Boolean>() {

                @SuppressWarnings("synthetic-access")
                public Boolean apply(CmsCategoryTreeEntry entry) {

                    return Boolean.valueOf(entry.getBasePath().equals(m_basePath));
                }
            }));
    }

    /**
     * Gets the category bean by id.<p>
     *
     * @param id a structure id
     * @return the entry with the given id, or null if no such entry was found
     */
    public CmsCategoryTreeEntry getEntryById(CmsUUID id) {

        for (CmsCategoryTreeEntry entry : getFlatList()) {
            if (id.equals(entry.getId())) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Sets the base path.<p>
     *
     * @param basePath the base path
     */
    public void setBasePath(String basePath) {

        m_basePath = basePath;
    }

    /**
     * Returns the category entries in a flat list instead of a tree structure.<p>
     *
     * @return the list of all tree entries
     */
    List<CmsCategoryTreeEntry> getFlatList() {

        List<CmsCategoryTreeEntry> toProcess = Lists.newArrayList(m_categoryEntries);
        List<CmsCategoryTreeEntry> result = Lists.newArrayList();
        Set<CmsUUID> visited = Sets.newHashSet();
        while (!toProcess.isEmpty()) {
            Iterator<CmsCategoryTreeEntry> iter = toProcess.iterator();
            CmsCategoryTreeEntry entry = iter.next();
            iter.remove();
            if (!visited.contains(entry.getId())) {
                result.add(entry);
                visited.add(entry.getId());
                toProcess.addAll(entry.getChildren());
            }
        }
        return result;
    }

    /**
     * Gets the top-level entries.<p>
     *
     * @return the top-level entries
     */
    List<CmsCategoryTreeEntry> getRootEntries() {

        return m_categoryEntries;
    }

}

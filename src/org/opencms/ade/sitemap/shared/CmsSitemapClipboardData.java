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

import org.opencms.util.CmsUUID;

import java.util.LinkedHashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap clipboard data bean.<p>
 *
 * @since 8.0
 */
public class CmsSitemapClipboardData implements IsSerializable {

    /** The session stored list of deleted sitemap entries. */
    private LinkedHashMap<CmsUUID, CmsClientSitemapEntry> m_deletions;

    /** The session stored list of modified sitemap entry paths. */
    private LinkedHashMap<CmsUUID, CmsClientSitemapEntry> m_modifications;

    /**
     * Constructor.<p>
     */
    public CmsSitemapClipboardData() {

        m_deletions = new LinkedHashMap<CmsUUID, CmsClientSitemapEntry>();
        m_modifications = new LinkedHashMap<CmsUUID, CmsClientSitemapEntry>();
    }

    /**
     * Constructor.<p>
     *
     * @param deletions the session stored list of deleted sitemap entries
     * @param modifications the session stored list of modified sitemap entry paths
     */
    public CmsSitemapClipboardData(
        LinkedHashMap<CmsUUID, CmsClientSitemapEntry> deletions,
        LinkedHashMap<CmsUUID, CmsClientSitemapEntry> modifications) {

        m_deletions = deletions;
        m_modifications = modifications;
    }

    /**
     * Adds an entry to the deleted list.<p>
     *
     * @param entry the entry to add
     */
    public void addDeleted(CmsClientSitemapEntry entry) {

        if (m_deletions.containsKey(entry.getId())) {
            m_deletions.remove(entry.getId());
        }
        m_deletions.put(entry.getId(), entry);
    }

    /**
     * Adds an entry to the modified list.<p>
     *
     * @param entry the entry to add
     */
    public void addModified(CmsClientSitemapEntry entry) {

        if (m_modifications.containsKey(entry.getId())) {
            m_modifications.remove(entry.getId());
        }
        m_modifications.put(entry.getId(), entry);
    }

    /**
     * Provides a copy of the clip-board data.<p>
     *
     * @return the copied data
     */
    public CmsSitemapClipboardData copy() {

        LinkedHashMap<CmsUUID, CmsClientSitemapEntry> deletions = new LinkedHashMap<CmsUUID, CmsClientSitemapEntry>();
        deletions.putAll(m_deletions);
        LinkedHashMap<CmsUUID, CmsClientSitemapEntry> modifications = new LinkedHashMap<CmsUUID, CmsClientSitemapEntry>();
        modifications.putAll(m_modifications);
        return new CmsSitemapClipboardData(deletions, modifications);
    }

    /**
     * Returns the session stored list of deleted sitemap entries.<p>
     *
     * @return the session stored list of deleted sitemap entries
     */
    public LinkedHashMap<CmsUUID, CmsClientSitemapEntry> getDeletions() {

        return m_deletions;
    }

    /**
     * Returns the session stored list of modified sitemap entry paths.<p>
     *
     * @return the session stored list of modified sitemap entry paths
     */
    public LinkedHashMap<CmsUUID, CmsClientSitemapEntry> getModifications() {

        return m_modifications;
    }

    /**
     * Removes an entry from the deleted list.<p>
     *
     * @param entry the entry to remove
     */
    public void removeDeleted(CmsClientSitemapEntry entry) {

        m_deletions.remove(entry.getId());
    }

    /**
     * Removes an entry from the modified list.<p>
     *
     * @param entry the entry to remove
     */
    public void removeModified(CmsClientSitemapEntry entry) {

        m_modifications.remove(entry.getId());
    }

    /**
     * Sets list of deleted sitemap entries.<p>
     *
     * @param deletions the deleted sitemap entries
     */
    public void setDeletions(LinkedHashMap<CmsUUID, CmsClientSitemapEntry> deletions) {

        m_deletions = deletions;
    }

    /**
     * Sets the list of modified sitemap entry paths.<p>
     *
     * @param modifications the list of modified sitemap entry paths
     */
    public void setModifications(LinkedHashMap<CmsUUID, CmsClientSitemapEntry> modifications) {

        m_modifications = modifications;
    }
}

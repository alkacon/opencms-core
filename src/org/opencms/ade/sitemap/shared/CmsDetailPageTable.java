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

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.util.CmsUUID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A data structure for managing the detail page ordering for different types in a given sitemap.<p>
 *
 * @since 8.0.0
 */
public class CmsDetailPageTable implements Cloneable, Serializable {

    /** A type indicating the status of a page. */
    public static enum Status {
        /** default detail page. */
        firstDetailPage,
        /** no detail page. */
        noDetailPage,
        /** non-default detail page. */
        otherDetailPage
    }

    /** ID for serialization. */
    private static final long serialVersionUID = -4561142050519767250L;

    /** The detail page info beans indexed by id. */
    private Map<CmsUUID, CmsDetailPageInfo> m_infoById = new HashMap<CmsUUID, CmsDetailPageInfo>();

    /** The detail page info beans, indexed by type. */
    private Map<String, List<CmsDetailPageInfo>> m_map = new HashMap<>();

    /**
     * Creates a detail page table from a list of detail page info bean.<p>
     *
     * @param infos the detail page info beans
     */
    public CmsDetailPageTable(List<CmsDetailPageInfo> infos) {

        for (CmsDetailPageInfo info : infos) {
            m_map.compute(info.getType(), (k, vs) -> vs == null ? new ArrayList<>() : vs).add(info);
            m_infoById.put(info.getId(), info);
        }
    }

    /**
     * Empty default constructor for serialization.<p>
     */
    protected CmsDetailPageTable() {

        // for serialization
    }

    /**
     * Adds a new detail page information bean to the detail page table.<p>
     *
     * @param info the detail page info to add
     */
    public void add(CmsDetailPageInfo info) {

        m_map.computeIfAbsent(info.getType(), type -> new ArrayList<>()).add(info);
        m_infoById.put(info.getId(), info);
    }

    /**
     * Checks if the entry for the given id can be made the default detail page entry for its type.
     *
     * @param id the id to check
     * @return true if the entry can be made the default detail page entry
     */
    public boolean canMakeDefault(CmsUUID id) {

        if (isDefaultDetailPage(id)) {
            return false;
        }
        CmsDetailPageInfo info = m_infoById.get(id);
        if ((info == null) || info.isInherited()) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the detail page table contains a page with a given id.<p>
     *
     * @param id the page id
     * @return true if the detail page table contains the page with the given id
     */
    public boolean contains(CmsUUID id) {

        return m_infoById.containsKey(id);
    }

    /**
     * Copies the detail page table.<p>
     *
     * @return the copy of the detail page table
     */
    public CmsDetailPageTable copy() {

        List<CmsDetailPageInfo> infos = toList();
        CmsDetailPageTable result = new CmsDetailPageTable();
        for (CmsDetailPageInfo info : infos) {
            result.add(info);
        }
        return result;
    }

    /**
     * Returns the detail page info for a given page id.<p>
     *
     * @param id a page id
     * @return the detail page info for the given page id
     */
    public CmsDetailPageInfo get(CmsUUID id) {

        return m_infoById.get(id);
    }

    /**
     * Returns the page ids of all detail pages. <p>
     *
     * @return the page ids of all detail pages
     */
    public Collection<CmsUUID> getAllIds() {

        return m_infoById.keySet();
    }

    /**
     * Returns the list of detail page info beans for a given type.<p>
     *
     * @param type the type for which the detail page beans should be retrieved
     *
     * @return the detail page beans for that type
     */
    public List<CmsDetailPageInfo> getInfosForType(String type) {

        return new ArrayList<CmsDetailPageInfo>(m_map.computeIfAbsent(type, k -> new ArrayList<>()));
    }

    /**
     * Returns the page status for the page with the given id.<p>
     *
     * @param id the id for which the page status should be checked
     *
     * @return the status of the page with the given id
     */
    public Status getStatus(CmsUUID id) {

        CmsDetailPageInfo info = m_infoById.get(id);
        if (info == null) {
            return Status.noDetailPage;
        }
        String type = info.getType();
        List<CmsDetailPageInfo> pagesWithNoQualifier = m_map.computeIfAbsent(
            type,
            k -> new ArrayList<>()).stream().filter(detailPage -> detailPage.getQualifier() == null).collect(
                Collectors.toList());
        int index = pagesWithNoQualifier.indexOf(info);
        // if info has a qualifier, index will be -1, but it's still in the list of other detail pages
        if (index == 0) {
            return Status.firstDetailPage;
        }
        return Status.otherDetailPage;
    }

    /**
     * Returns true if the page with the given id is the default detail page for its type.<p>
     *
     * @param id a page id
     *
     * @return true if the detail page for the page id is the default detail page
     */
    public boolean isDefaultDetailPage(CmsUUID id) {

        CmsDetailPageInfo info = m_infoById.get(id);
        if (info == null) {
            return false;
        }
        CmsDetailPageInfo firstUnqualifiedEntry = m_map.get(info.getType()).stream().filter(
            page -> page.getQualifier() == null).findFirst().orElse(null);
        return (firstUnqualifiedEntry != null) && firstUnqualifiedEntry.getId().equals(id);
    }

    /**
     * Moves the detail page information for a given page to the front of the detail pages for the same type.<p>
     *
     * @param id a page id
     *
     * @return the original position of the detail page entry in the list for the same type
     */
    public int makeDefault(CmsUUID id) {

        CmsDetailPageInfo info = m_infoById.get(id);
        if (info == null) {
            throw new IllegalArgumentException();
        }

        CmsDetailPageInfo infoToSave = info;
        // Making a detail page the default detail page should discard the qualifier

        if (info.getQualifier() != null) {
            CmsDetailPageInfo info2 = new CmsDetailPageInfo(
                info.getId(),
                info.getUri(),
                info.getType(),
                null,
                info.getIconClasses());
            m_infoById.put(id, info2);
            if (info.isInherited()) {
                // this case should be prevented by the GUI, we handle it just to make sure the entry is not saved
                infoToSave = info2.copyAsInherited();
            } else {
                infoToSave = info2;
            }
        }
        String type = info.getType();
        List<CmsDetailPageInfo> infos = m_map.computeIfAbsent(type, k -> new ArrayList<>());
        int oldPos = infos.indexOf(info);
        infos.remove(oldPos);
        infos.add(0, infoToSave);
        return oldPos;
    }

    /**
     * Removes the detail page with the given id.<p>
     *
     * @param id the id of the detail page to remove
     *
     * @return the original position of the detail page in the list for its type
     */
    public int remove(CmsUUID id) {

        CmsDetailPageInfo info = m_infoById.get(id);
        if (info == null) {
            throw new IllegalArgumentException();
        }
        String type = info.getType();
        List<CmsDetailPageInfo> infos = m_map.get(type);
        int pos = infos.indexOf(info);
        infos.remove(pos);
        m_infoById.remove(id);
        return pos;

    }

    /**
     * The number of configured detail pages.<p>
     *
     * @return the number of detail pages
     */
    public int size() {

        return m_infoById.size();
    }

    /**
     * Returns a flat list containing all detail pages for all types which preserves the order of detail pages from each type list.<p>
     *
     * @return a list of all detail page info beans
     */
    public List<CmsDetailPageInfo> toList() {

        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        for (String key : m_map.keySet()) {
            for (CmsDetailPageInfo info : m_map.get(key)) {
                result.add(info);
            }
        }
        return result;
    }

}

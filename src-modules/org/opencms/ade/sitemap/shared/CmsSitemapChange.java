/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsSitemapChange.java,v $
 * Date   : $Date: 2011/01/21 11:09:42 $
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

package org.opencms.ade.sitemap.shared;

import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;
import org.opencms.xml.sitemap.CmsDetailPageInfo;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean containing sitemap entry change information.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapChange implements IsSerializable, Comparable<CmsSitemapChange> {

    /** The changed clip-board data. */
    private CmsSitemapClipboardData m_clipBoardData;

    /** Detail page info's to change. */
    private List<CmsDetailPageInfo> m_detailPageInfos;

    /** The entry id. */
    private CmsUUID m_entryId;

    /** Flag to indicate this is a deleting change. */
    private boolean m_isDelete;

    /** Flag to indicate this is a creating new change. */
    private boolean m_isNew;

    /** The entry name. */
    private String m_name;

    /** The entry parent id. */
    private CmsUUID m_parentId;

    /** The entry position.*/
    private int m_position = -1;

    /** The edited entry properties. */
    private Map<String, CmsSimplePropertyValue> m_properties;

    /** The entry site path. */
    private String m_sitePath;

    /** The entry's title. */
    private String m_title;

    /**
     * Constructor needed for serialization.<p>
     */
    public CmsSitemapChange() {

        // nothing to do
    }

    /**
     * Constructor.<p>
     * 
     * @param entryId entry id
     * @param sitePath
     */
    public CmsSitemapChange(CmsUUID entryId, String sitePath) {

        m_entryId = entryId;
        m_sitePath = sitePath;
    }

    /**
     * Adds the given change data to this change object.<p>
     * 
     * @param data the change data to add
     */
    public void addChangeData(CmsSitemapChange data) {

        if (data == null) {
            return;
        }
        if (!m_entryId.equals(data.m_entryId)) {
            throw new UnsupportedOperationException("Can't add data for a different entry id.");
        }
        m_sitePath = data.m_sitePath;
        if (data.hasChangedName()) {
            m_name = data.m_name;
        }
        if (data.hasChangedPosition()) {
            m_position = data.m_position;
        }
        if (data.hasChangedProperties()) {
            m_properties.putAll(data.m_properties);
        }
        if (data.hasNewParent()) {
            m_parentId = data.m_parentId;
        }
        if (data.isDelete()) {
            m_isDelete = true;
        }
        if (data.getClipBoardData() != null) {
            m_clipBoardData = data.getClipBoardData();
        }
    }

    /**
     * Will compare the parent site path and the position of the entry to change.<p>
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsSitemapChange arg0) {

        if (m_entryId.equals(arg0.m_entryId)) {
            return 0;
        }
        int result = CmsResource.getParentFolder(m_sitePath).compareTo(CmsResource.getParentFolder(arg0.m_sitePath));
        if (result == 0) {
            result = m_position - arg0.m_position;
            if (result == 0) {
                // two different entries should never have the same parent and the same name, so this compare should be sufficient
                result = m_name.compareTo(arg0.m_name);
            }
        }
        return result;
    }

    /**
     * Two sitemap change objects are considered equal, if the entry id's are equal.<p>
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof CmsSitemapChange) {
            return m_entryId.equals(((CmsSitemapChange)obj).m_entryId);
        }
        return false;
    }

    /**
     * Returns the clip-board data.<p>
     *
     * @return the clip-board data
     */
    public CmsSitemapClipboardData getClipBoardData() {

        return m_clipBoardData;
    }

    /**
     * Returns the detail page info's.<p>
     *
     * @return the detail page info's
     */
    public List<CmsDetailPageInfo> getDetailPageInfos() {

        return m_detailPageInfos;
    }

    /**
     * Returns the entry id.<p>
     *
     * @return the entry id
     */
    public CmsUUID getEntryId() {

        return m_entryId;
    }

    /**
     * Returns the entry name.<p>
     *
     * @return the entry name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the entry parent id.<p>
     *
     * @return the entry parent id
     */
    public CmsUUID getParentId() {

        return m_parentId;
    }

    /**
     * Returns the entry position.<p>
     *
     * @return the entry position
     */
    public int getPosition() {

        return m_position;
    }

    /**
     * Returns the entry properties.<p>
     *
     * @return the entry properties
     */
    public Map<String, CmsSimplePropertyValue> getProperties() {

        return m_properties;
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
     * Returns if there are changed inherit properties.<p>
     * 
     * @return <code>true</code> if there are changed inherit properties
     */
    public boolean hasChangedInheritProperties() {

        if (hasChangedProperties()) {
            for (CmsSimplePropertyValue prop : m_properties.values()) {
                if (prop.getInheritValue() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns if the entry name has changed.<p>
     * 
     * @return <code>true</code> if the entry name has changed
     */
    public boolean hasChangedName() {

        return m_name != null;
    }

    /**
     * Returns if the position has changed.<p>
     * 
     * @return <code>true</code> if the position has changed
     */
    public boolean hasChangedPosition() {

        return m_position >= 0;
    }

    /**
     * Returns if there are changed properties.<p>
     * 
     * @return <code>true</code> if there are changed properties
     */
    public boolean hasChangedProperties() {

        return m_properties != null;
    }

    /**
     * Returns if the entry title has changed.<p>
     * 
     * @return <code>true</code> if the entry title has changed
     */
    public boolean hasChangedTitle() {

        return m_title != null;
    }

    /**
     * Returns if detail page info's have changed.<p>
     * 
     * @return <code>true</code> if detail page info's have changed
     */
    public boolean hasDetailPageInfos() {

        return m_detailPageInfos != null;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_entryId.hashCode();
    }

    /**
     * Returns if this change sets a new parent.<p>
     * 
     * @return <code>true</code> if the entry gets a new parent
     */
    public boolean hasNewParent() {

        return m_parentId != null;
    }

    /**
     * Returns if this is a deleting change.<p>
     * 
     * @return the is delete flag
     */
    public boolean isDelete() {

        return m_isDelete;
    }

    /**
     * Returns if this is a creating new change.<p>
     * 
     * @return the is new flag
     */
    public boolean isNew() {

        return m_isNew;
    }

    /**
     * Sets the clip-board data.<p>
     *
     * @param clipBoardData the clip-board data to set
     */
    public void setClipBoardData(CmsSitemapClipboardData clipBoardData) {

        m_clipBoardData = clipBoardData;
    }

    /**
     * Sets the is delete flag.<p>
     *
     * @param isDelete the is delete flag to set
     */
    public void setDelete(boolean isDelete) {

        m_isDelete = isDelete;
    }

    /**
     * Sets the detail page info's.<p>
     *
     * @param detailPageInfos the detail page info's to set
     */
    public void setDetailPageInfos(List<CmsDetailPageInfo> detailPageInfos) {

        m_detailPageInfos = detailPageInfos;
    }

    /**
     * Sets the entry name.<p>
     *
     * @param name the entry name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the isNew.<p>
     *
     * @param isNew the isNew to set
     */
    public void setNew(boolean isNew) {

        m_isNew = isNew;
    }

    /**
     * Sets the entry parent id.<p>
     *
     * @param parentId the entry parent id to set
     */
    public void setParentId(CmsUUID parentId) {

        m_parentId = parentId;
    }

    /**
     * Sets the entry position.<p>
     *
     * @param position the entry position to set
     */
    public void setPosition(int position) {

        m_position = position;
    }

    /**
     * Sets the entry properties.<p>
     *
     * @param properties the entry properties to set
     */
    public void setProperties(Map<String, CmsSimplePropertyValue> properties) {

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
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }
}

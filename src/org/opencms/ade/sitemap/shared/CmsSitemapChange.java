/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean containing sitemap entry change information.<p>
 * 
 * @since 8.0.0
 */
public class CmsSitemapChange implements IsSerializable, Comparable<CmsSitemapChange> {

    /** The change types. */
    public enum ChangeType {
        /** Making a detail page the default. */
        bumpDetailPage,
        /** The clip-board only change. */
        clipboardOnly,
        /** The create/new change. */
        create,
        /** The delete resource change. */
        delete,
        /** The modify change. */
        modify,
        /** The remove from navigation change. */
        remove,
        /** The undelete resource change. */
        undelete
    }

    /** The change type. */
    private ChangeType m_changeType;

    /** The changed clip-board data. */
    private CmsSitemapClipboardData m_clipBoardData;

    /** The default file id. */
    private CmsUUID m_defaultFileId;

    /** The default file's properties. */
    private Map<String, CmsClientProperty> m_defaultFileInternalProperties = new HashMap<String, CmsClientProperty>();

    /** Detail page info's to change. */
    private List<CmsDetailPageInfo> m_detailPageInfos;

    /** The entry id. */
    private CmsUUID m_entryId;

    /** Indicates if the entry to change is a leaf type entry. */
    private boolean m_isLeafType;

    /** The entry name. */
    private String m_name;

    /** The new entry copy resource structure id. */
    private CmsUUID m_newCopyResourceId;

    /** The new entry resource type id. */
    private int m_newResourceTypeId;

    /** The changed entry's own properties. */
    private Map<String, CmsClientProperty> m_ownInternalProperties = new HashMap<String, CmsClientProperty>();

    /** An additional parameter which may contain additional information for creating a new resource. */
    private String m_parameter;

    /** The entry parent id. */
    private CmsUUID m_parentId;

    /** The entry position.*/
    private int m_position = -1;

    /** The list of property modifications. */
    private List<CmsPropertyModification> m_propertyModifications = new ArrayList<CmsPropertyModification>();

    /** The entry site path. */
    private String m_sitePath;

    /** The updated entry. */
    private CmsClientSitemapEntry m_updatedEntry;

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
     * @param sitePath the entry site-path
     * @param changeType the change type
     */
    public CmsSitemapChange(CmsUUID entryId, String sitePath, ChangeType changeType) {

        m_entryId = entryId;
        m_sitePath = sitePath;
        m_changeType = changeType;
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
            m_propertyModifications.addAll(data.m_propertyModifications);
        }
        if (data.hasNewParent()) {
            m_parentId = data.m_parentId;
        }

        if (data.getClipBoardData() != null) {
            m_clipBoardData = data.getClipBoardData();
        }
        if ((data.m_changeType == ChangeType.delete)
            || ((data.m_changeType == ChangeType.remove) && (m_changeType != ChangeType.delete))
            || (m_changeType == ChangeType.clipboardOnly)) {
            m_changeType = data.m_changeType;
        }
    }

    /**
     * Adds a property change for a changed title.<p>
     * 
     * @param title the changed title 
     */
    public void addChangeTitle(String title) {

        CmsPropertyModification propChange = new CmsPropertyModification(
            m_entryId,
            CmsClientProperty.PROPERTY_NAVTEXT,
            title,
            true);
        m_propertyModifications.add(propChange);
        m_ownInternalProperties.put("NavText", new CmsClientProperty("NavText", title, null));
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
     * Returns the change type.<p>
     *
     * @return the change type
     */
    public ChangeType getChangeType() {

        return m_changeType;
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
     * Returns an additional parameter for creating new resources.<p>
     *  
     * @return an additional parameter which may contain information needed to create new resources 
     */
    public String getCreateParameter() {

        return m_parameter;
    }

    /**
     * Gets the default file id.<p>
     * 
     * @return the default file id 
     */
    public CmsUUID getDefaultFileId() {

        return m_defaultFileId;
    }

    /**
     * Returns the change'S properties for the default file.<p>
     * 
     * @return the properties for the default file 
     */
    public Map<String, CmsClientProperty> getDefaultFileProperties() {

        return m_defaultFileInternalProperties;
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
     * Returns the new entry copy resource structure id.<p>
     *
     * @return the new entry copy resource structure id
     */
    public CmsUUID getNewCopyResourceId() {

        return m_newCopyResourceId;
    }

    /**
     * Returns the new entry resource type id.<p>
     *
     * @return the new entry resource type id
     */
    public int getNewResourceTypeId() {

        return m_newResourceTypeId;
    }

    /**
     * Returns the properties for the entry itself.<p> 
     * 
     * @return the properties for the entry itself 
     */
    public Map<String, CmsClientProperty> getOwnInternalProperties() {

        return m_ownInternalProperties;
    }

    /**
     * Returns the change's properties for the entry itself.<p>
     * 
     * @return the change's properties for the entry itself 
     */
    public Map<String, CmsClientProperty> getOwnProperties() {

        return m_ownInternalProperties;
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
     * Gets the list of property changes.<p>
     * 
     * @return the list of property changes 
     */
    public List<CmsPropertyModification> getPropertyChanges() {

        return m_propertyModifications;
    }

    /**
     * Returns the site-path.<p>
     *
     * @return the site-path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the updated entry.<p>
     *
     * @return the updated entry
     */
    public CmsClientSitemapEntry getUpdatedEntry() {

        return m_updatedEntry;
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

        return (m_propertyModifications != null) && (m_propertyModifications.size() > 0);
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

        return ChangeType.delete == m_changeType;
    }

    /**
     * Returns if the entry to change is a leaf type entry.<p>
     *
     * @return <code>true</code> if the entry to change is a leaf type entry
     */
    public boolean isLeafType() {

        return m_isLeafType;
    }

    /**
     * Returns if this is a creating new change.<p>
     * 
     * @return the is new flag
     */
    public boolean isNew() {

        return ChangeType.create == m_changeType;
    }

    /**
     * Returns if this is a remove from navigation change.<p>
     * 
     * @return the is new flag
     */
    public boolean isRemove() {

        return ChangeType.remove == m_changeType;
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
     * Sets additional info needed for creating new resources.<p>
     * 
     * @param parameter the additional resource creation information
     */
    public void setCreateParameter(String parameter) {

        m_parameter = parameter;

    }

    /** 
     * Sets the default file id. <p>
     * 
     * @param id the default file id 
     */
    public void setDefaultFileId(CmsUUID id) {

        m_defaultFileId = id;
    }

    /**
     * Sets the properties for the default file.<p>
     * 
     * @param props the properties for the default file 
     */
    public void setDefaultFileInternalProperties(Map<String, CmsClientProperty> props) {

        m_defaultFileInternalProperties = props;
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
     * Sets the entry id.<p>
     * 
     * @param entryId the entry id to set
     */
    public void setEntryId(CmsUUID entryId) {

        m_entryId = entryId;
    }

    /**
     * Sets if the entry to change is a leaf type entry.<p>
     *
     * @param isLeafEntry <code>true</code> if the entry to change is a leaf type entry
     */
    public void setLeafType(boolean isLeafEntry) {

        m_isLeafType = isLeafEntry;
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
     * Sets the new entry copy resource structure id.<p>
     *
     * @param newCopyResourceId the new entry copy resource structure id to set
     */
    public void setNewCopyResourceId(CmsUUID newCopyResourceId) {

        m_newCopyResourceId = newCopyResourceId;
    }

    /**
     * Sets the new entry resource type id.<p>
     *
     * @param newResourceTypeId the new entry resource type id to set
     */
    public void setNewResourceTypeId(int newResourceTypeId) {

        m_newResourceTypeId = newResourceTypeId;
    }

    /** 
     * Sets the changed properties of the entry itself.<p>
     * 
     * @param props the entry's changed properties 
     */
    public void setOwnInternalProperties(Map<String, CmsClientProperty> props) {

        m_ownInternalProperties = props;
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
     * Sets the list of property changes.<p>
     * 
     * @param propertyChanges the property changes 
     */
    public void setPropertyChanges(List<CmsPropertyModification> propertyChanges) {

        m_propertyModifications = propertyChanges;
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
     * @param title the title 
     */
    public void setTitle(String title) {

        addChangeTitle(title);
    }

    /**
     * Sets the updated entry.<p>
     *
     * @param updatedEntry the updated entry to set
     */
    public void setUpdatedEntry(CmsClientSitemapEntry updatedEntry) {

        m_updatedEntry = updatedEntry;
    }
}

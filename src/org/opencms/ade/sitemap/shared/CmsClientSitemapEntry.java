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

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.CmsClientLock;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap entry data.<p>
 * 
 * @since 8.0.0 
 */
public class CmsClientSitemapEntry implements IsSerializable {

    /**
     * An enum for the edit status of the entry.<p>
     */
    public enum EditStatus {
        /** edit status constant. */
        created,
        /** edit status constant. */
        edited,
        /** edit status constant. */
        normal
    }

    /** An enum for the entry type. */
    public enum EntryType {
        /** The default type. An entry of type folder may have children. */
        folder,

        /** An entry of type leaf doesn't have any children. */
        leaf,

        /** A redirect entry. */
        redirect,

        /** An entry of type sub-sitemap is a reference to a sub-sitemap. */
        subSitemap
    }

    /** The cached export name. */
    private String m_cachedExportName;

    /** True if the children of this entry have initially been loaded. */
    private boolean m_childrenLoadedInitially;

    /** The expiration date. */
    private String m_dateExpired;

    /** The release date. */
    private String m_dateReleased;

    /** The default file id. */
    private CmsUUID m_defaultFileId;

    /** The default file properties. */
    private Map<String, CmsClientProperty> m_defaultFileProperties = new HashMap<String, CmsClientProperty>();

    /** The default file resource type name. */
    private String m_defaultFileType;

    /** The detail page type name. */
    private String m_detailpageTypeName;

    /** The entry type. */
    private EntryType m_entryType;

    /** Locked child resources. */
    private boolean m_hasBlockingLockedChildren;

    /** Indicates if the entry folder is locked by another user. */
    private boolean m_hasForeignFolderLock;

    /** The entry id. */
    private CmsUUID m_id;

    /** Flag to indicate if the entry is visible in navigation. */
    private boolean m_inNavigation;

    /** Indicates if this entry represents the default page of the parent folder. */
    private boolean m_isFolderDefaultPage;

    /** Flag indicating the resource is released and not expired. */
    private boolean m_isResleasedAndNotExpired;

    /** The lock of the entry resource. */
    private CmsClientLock m_lock;

    /** The entry name. */
    private String m_name;

    /** True if this entry has been just created, and its name hasn't been directly changed. */
    private boolean m_new;

    /** The properties for the entry itself. */
    private Map<String, CmsClientProperty> m_ownProperties = new HashMap<String, CmsClientProperty>();

    /** The relative position between siblings. */
    private int m_position = -1;

    /** The target in case of a redirect type. */
    private String m_redirectTarget;

    /** The resource state. */
    private CmsResourceState m_resourceState;

    /** The resource type name. */
    private String m_resourceTypeName;

    /** The sitemap path. */
    private String m_sitePath;

    /** The children. */
    private List<CmsClientSitemapEntry> m_subEntries;

    /** The VFS path. */
    private String m_vfsPath;

    /**
     * Constructor.<p>
     */
    public CmsClientSitemapEntry() {

        m_subEntries = new ArrayList<CmsClientSitemapEntry>();
        m_entryType = EntryType.folder;
    }

    /**
     * Creates a copy without children of the given entry.<p>
     * 
     * @param clone the entry to clone 
     */
    public CmsClientSitemapEntry(CmsClientSitemapEntry clone) {

        this();
        copyMembers(clone);
        setPosition(clone.getPosition());
    }

    /**
    * Adds the given entry to the children.<p>
    * 
    * @param entry the entry to add
    * @param controller a sitemap controller instance 
    */
    public void addSubEntry(CmsClientSitemapEntry entry, I_CmsSitemapController controller) {

        entry.setPosition(m_subEntries.size());
        entry.updateSitePath(CmsStringUtil.joinPaths(m_sitePath, entry.getName()), controller);
        entry.setFolderDefaultPage(entry.isLeafType() && getVfsPath().equals(entry.getVfsPath()));
        m_subEntries.add(entry);
    }

    /**
     * Returns true if this item's children have been loaded initially.<p>
     * 
     * @return true if this item's children have been loaded initially
     */
    public boolean getChildrenLoadedInitially() {

        return m_childrenLoadedInitially;
    }

    /**
     * Returns the expiration date.<p>
     *
     * @return the expiration date
     */
    public String getDateExpired() {

        return m_dateExpired;
    }

    /**
     * Returns the release date.<p>
     *
     * @return the release date
     */
    public String getDateReleased() {

        return m_dateReleased;
    }

    /**
     * Gets the default file id.<p> 
     *  
     * @return the default file id, or null if there is no detail page 
     */
    public CmsUUID getDefaultFileId() {

        return m_defaultFileId;
    }

    /**
     * Returns the properties for the default file.<p>
     * 
     * @return the properties for the default file 
     */
    public Map<String, CmsClientProperty> getDefaultFileProperties() {

        return m_defaultFileProperties;
    }

    /**
     * Returns the default file resource type name.<p>
     *
     * @return the default file resource type name
     */
    public String getDefaultFileType() {

        return m_defaultFileType;
    }

    /**
     * Returns the detail resource type name.<p>
     * 
     * @return the detail resource type name
     */
    public String getDetailpageTypeName() {

        return m_detailpageTypeName;
    }

    /**
     * Returns the entry type.<p>
     *
     * @return the entry type
     */
    public EntryType getEntryType() {

        return m_entryType;
    }

    /**
     * Returns the cached export name for this entry.<p>
     * 
     * @return the cached export name for this entry 
     */
    public String getExportName() {

        return m_cachedExportName;
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
     * Returns the lock of the entry resource.<p>
     *
     * @return the lock of the entry resource
     */
    public CmsClientLock getLock() {

        return m_lock;
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
     * Returns the properties for the entry itself.<p>
     * 
     * @return the properties for the entry itself 
     */
    public Map<String, CmsClientProperty> getOwnProperties() {

        return m_ownProperties;
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
     * Returns the property value or null if not set.<p>
     * 
     * @param propertyName the property name
     * 
     * @return the property value
     */
    public String getPropertyValue(String propertyName) {

        if (m_ownProperties.containsKey(propertyName)) {
            return m_ownProperties.get(propertyName).getEffectiveValue();
        }
        return null;
    }

    /**
     * Returns the redirect target.<p>
     *
     * @return the redirect target
     */
    public String getRedirectTarget() {

        return m_redirectTarget;
    }

    /**
     * Returns the resource state.<p>
     *
     * @return the resource state
     */
    public CmsResourceState getResourceState() {

        return m_resourceState;
    }

    /**
     * Returns the resource type name.<p>
     * 
     * @return the resource type name 
     */
    public String getResourceTypeName() {

        return m_resourceTypeName;
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

        String title = getPropertyValue(CmsClientProperty.PROPERTY_NAVTEXT);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
            title = getPropertyValue(CmsClientProperty.PROPERTY_TITLE);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
            title = m_name;
        }
        return title;
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
     * Returns if this entry has blocking locked children.<p>
     * 
     * @return <code>true</code> if this entry has blocking locked children
     */
    public boolean hasBlockingLockedChildren() {

        return m_hasBlockingLockedChildren;
    }

    /**
     * Returns if the entry folder is locked by another user.<p>
     *
     * @return <code>true</code> if the entry folder is locked by another user
     */
    public boolean hasForeignFolderLock() {

        return m_hasForeignFolderLock;
    }

    /**
     * Initializes this sitemap entry.<p>
     * 
     * @param controller a sitemap controller instance 
     */
    public void initialize(I_CmsSitemapController controller) {

        m_ownProperties = controller.replaceProperties(m_id, m_ownProperties);
        m_defaultFileProperties = controller.replaceProperties(m_defaultFileId, m_defaultFileProperties);
        controller.registerEntry(this);
    }

    /**
     * Initializes this sitemap entry and its descendants.<p>
     * 
     * @param controller the controller instance with which to initialize the entries 
     */
    public void initializeAll(I_CmsSitemapController controller) {

        initialize(controller);
        for (CmsClientSitemapEntry child : m_subEntries) {
            child.initializeAll(controller);
        }
    }

    /**
     * Inserts the given entry at the given position.<p>
     * 
     * @param entry the entry to insert
     * @param position the position
     * @param controller a sitemap controller instance 
     */
    public void insertSubEntry(CmsClientSitemapEntry entry, int position, I_CmsSitemapController controller) {

        entry.updateSitePath(CmsStringUtil.joinPaths(m_sitePath, entry.getName()), controller);
        m_subEntries.add(position, entry);
        updatePositions(position);
    }

    /**
     * Returns if the current lock state allows editing.<p>
     * 
     * @return <code>true</code> if the resource is editable
     */
    public boolean isEditable() {

        return !hasForeignFolderLock()
            && !hasBlockingLockedChildren()
            && (((getLock() == null) || (getLock().getLockOwner() == null)) || getLock().isOwnedByUser());
    }

    /**
     * Returns if the entry is the folder default page.<p>
     *
     * @return if the entry is the folder default page
     */
    public boolean isFolderDefaultPage() {

        return m_isFolderDefaultPage;
    }

    /**
     * Returns if this entry is of type folder.<p>
     * 
     * @return <code>true</code> if this entry is of type folder
     */
    public boolean isFolderType() {

        return EntryType.folder == m_entryType;
    }

    /**
     * Returns if the entry is visible in navigation.<p>
     *
     * @return <code>true</code> if the entry is visible in navigation
     */
    public boolean isInNavigation() {

        return m_inNavigation;
    }

    /**
     * Returns if this entry is of type leaf.<p>
     * 
     * @return <code>true</code> if this entry is of type leaf
     */
    public boolean isLeafType() {

        return (EntryType.leaf == m_entryType) || (EntryType.redirect == m_entryType);
    }

    /**
     * Returns the "new" flag of the sitemap entry.<p>
     * 
     * @return the "new" flag
     */
    public boolean isNew() {

        return m_new;
    }

    /**
     * Returns if the resource is released and not expired.<p>
     *
     * @return <code>true</code> if the resource is released and not expired
     */
    public boolean isResleasedAndNotExpired() {

        return m_isResleasedAndNotExpired;
    }

    /**
     * Returns true if this entry is the root entry of the sitemap.<p>
     * 
     * @return true if this entry is the root entry of the sitemap 
     */
    public boolean isRoot() {

        return m_name.equals("");
    }

    /**
     * Returns if this entry is of type sub-sitemap.<p>
     * 
     * @return <code>true</code> if this entry is of type sub-sitemap
     */
    public boolean isSubSitemapType() {

        return EntryType.subSitemap == m_entryType;
    }

    /**
     * Removes empty properties.<p>
     */
    public void normalizeProperties() {

        CmsClientProperty.removeEmptyProperties(m_ownProperties);
        if (m_defaultFileProperties != null) {
            CmsClientProperty.removeEmptyProperties(m_defaultFileProperties);
        }
    }

    /**
     * Removes the child at the given position.<p>
     * 
     * @param entryId the id of the child to remove
     * 
     * @return the removed child
     */
    public CmsClientSitemapEntry removeSubEntry(CmsUUID entryId) {

        CmsClientSitemapEntry removed = null;
        int position = -1;
        if (!m_subEntries.isEmpty()) {
            for (int i = 0; i < m_subEntries.size(); i++) {
                if (m_subEntries.get(i).getId().equals(entryId)) {
                    position = i;
                }
            }
            if (position != -1) {
                removed = m_subEntries.remove(position);
                updatePositions(position);
            }

        }
        return removed;
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
     * Sets if the entry resource has blocking locked children that can not be locked by the current user.<p>
     * 
     * @param hasBlockingLockedChildren <code>true</code> if the entry resource has blocking locked children
     */
    public void setBlockingLockedChildren(boolean hasBlockingLockedChildren) {

        m_hasBlockingLockedChildren = hasBlockingLockedChildren;
    }

    /**
     * Sets the 'children loaded initially' flag.<p>
     * 
     * @param childrenLoaded <code>true</code> if children are loaded initially
     */
    public void setChildrenLoadedInitially(boolean childrenLoaded) {

        m_childrenLoadedInitially = childrenLoaded;
    }

    /** 
     * Sets the expiration date.<p>
     *
     * @param dateExpired the expiration date to set
     */
    public void setDateExpired(String dateExpired) {

        m_dateExpired = dateExpired;
    }

    /**
     * Sets the release date.<p>
     *
     * @param dateReleased the release date to set
     */
    public void setDateReleased(String dateReleased) {

        m_dateReleased = dateReleased;
    }

    /** 
     * Sets the default file id.
     * 
     * @param defaultFileId the new default file id 
     **/
    public void setDefaultFileId(CmsUUID defaultFileId) {

        m_defaultFileId = defaultFileId;
    }

    /**
     * Sets the properties for the default file.<p>
     * 
     * @param properties the properties for the default file 
     */
    public void setDefaultFileProperties(Map<String, CmsClientProperty> properties) {

        m_defaultFileProperties = properties;
    }

    /**
     * Sets the default file resource type name.<p>
     *
     * @param defaultFileType the default file resource type name to set
     */
    public void setDefaultFileType(String defaultFileType) {

        m_defaultFileType = defaultFileType;
    }

    /**
     * Sets the detail resource type name.<p>
     * 
     * @param detailpageTypeName the detail resource type name
     */
    public void setDetailpageTypeName(String detailpageTypeName) {

        m_detailpageTypeName = detailpageTypeName;
    }

    /**
     * Sets the entry type.<p>
     *
     * @param entryType the entry type to set
     */
    public void setEntryType(EntryType entryType) {

        m_entryType = entryType;
    }

    /**
     * Sets the export name for this entry.<p>
     * 
     * @param exportName the export name for this entry 
     */
    public void setExportName(String exportName) {

        m_cachedExportName = exportName;
    }

    /**
     * Sets if the entry is the folder default page.<p>
     *
     * @param isFolderDefaultPage the isFolderDefaultPage to set
     */
    public void setFolderDefaultPage(boolean isFolderDefaultPage) {

        m_isFolderDefaultPage = isFolderDefaultPage;
    }

    /**
     * Sets if the entry folder is locked by another user.<p>
     *
     * @param hasForeignFolderLock set <code>true</code> if the entry folder is locked by another user
     */
    public void setHasForeignFolderLock(boolean hasForeignFolderLock) {

        m_hasForeignFolderLock = hasForeignFolderLock;
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
     * Sets the entry visibility in navigation.<p>
     *
     * @param inNavigation set <code>true</code> for entries visible in navigation
     */
    public void setInNavigation(boolean inNavigation) {

        m_inNavigation = inNavigation;
    }

    /**
     * Sets the lock of the entry resource.<p>
     *
     * @param lock the lock of the entry resource to set
     */
    public void setLock(CmsClientLock lock) {

        m_lock = lock;
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
     * Sets the "new" flag of the client sitemap entry.<p>
     *
     * @param isNew the new new
     */
    public void setNew(boolean isNew) {

        m_new = isNew;
    }

    /**
     * Sets the properties for the entry itself.<p>
     * 
     * @param properties the properties for the entry itself 
     */
    public void setOwnProperties(Map<String, CmsClientProperty> properties) {

        m_ownProperties = properties;
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
     * Sets the redirect target.<p>
     *
     * @param redirectTarget the redirect target to set
     */
    public void setRedirectTarget(String redirectTarget) {

        m_redirectTarget = redirectTarget;
    }

    /**
     * Sets the resource is released and not expired flag.<p>
     *
     * @param isResleasedAndNotExpired the resource is released and not expired flag
     */
    public void setResleasedAndNotExpired(boolean isResleasedAndNotExpired) {

        m_isResleasedAndNotExpired = isResleasedAndNotExpired;
    }

    /**
     * Sets the resource state.<p>
     *
     * @param resourceState the resource state to set
     */
    public void setResourceState(CmsResourceState resourceState) {

        m_resourceState = resourceState;
    }

    /**
     * Sets the resource type name.<p>
     * 
     * @param typeName the resource type name 
     */
    public void setResourceTypeName(String typeName) {

        m_resourceTypeName = typeName;

    }

    /**
     * Sets the site path.<p>
     *
     * @param sitepath the site path to set
     */
    public void setSitePath(String sitepath) {

        if (!isLeafType() && !sitepath.endsWith("/")) {
            sitepath = sitepath + "/";
        }
        m_sitePath = sitepath;
    }

    /**
     * Sets the children.<p>
     *
     * @param children the children to set
     * @param controller a sitemap controller instance 
     */
    public void setSubEntries(List<CmsClientSitemapEntry> children, I_CmsSitemapController controller) {

        m_childrenLoadedInitially = true;

        m_subEntries.clear();
        if (children != null) {
            m_subEntries.addAll(children);
            for (CmsClientSitemapEntry child : children) {
                child.updateSitePath(CmsStringUtil.joinPaths(m_sitePath, child.getName()), controller);
            }
        }
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
     * To string.
     *
     * @return the string
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(m_sitePath).append("\n");
        for (CmsClientSitemapEntry child : m_subEntries) {
            sb.append(child.toString());
        }
        return sb.toString();
    }

    /**
     * Updates all entry properties apart from it's position-info and sub-entries.<p>
     * 
     * @param source the source entry to update from
     */
    public void update(CmsClientSitemapEntry source) {

        copyMembers(source);
        // position values < 0 are considered as not set
        if (source.getPosition() >= 0) {
            setPosition(source.getPosition());
        }
    }

    /**
     * Updates the entry's site path and the name accordingly.<p>
     * 
     * @param sitepath the new site path to set
     * @param controller a sitemap controller instance 
     */
    public void updateSitePath(String sitepath, I_CmsSitemapController controller) {

        if (!isLeafType() && !sitepath.endsWith("/")) {
            sitepath = sitepath + "/";
        }
        if (!m_sitePath.equals(sitepath)) {
            // update the vfs path as well
            int start = m_vfsPath.lastIndexOf(m_sitePath);
            int stop = start + m_sitePath.length();
            m_vfsPath = CmsStringUtil.joinPaths(m_vfsPath.substring(0, start), sitepath, m_vfsPath.substring(stop));
            if (isLeafType() && m_vfsPath.endsWith("/")) {
                m_vfsPath = m_vfsPath.substring(0, m_vfsPath.length() - 1);
            }
            String oldPath = m_sitePath;
            m_sitePath = sitepath;
            String name = CmsResource.getName(sitepath);
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
            m_name = name;
            for (CmsClientSitemapEntry child : m_subEntries) {
                child.updateSitePath(CmsStringUtil.joinPaths(sitepath, child.getName()), controller);
            }
            if (controller != null) {
                controller.registerPathChange(this, oldPath);
            }
        }
    }

    /**
     * Copies all member variables apart from sub-entries and position.<p>
     * 
     * @param source the source to copy from
     */
    private void copyMembers(CmsClientSitemapEntry source) {

        setId(source.getId());
        setName(source.getName());
        setOwnProperties(new HashMap<String, CmsClientProperty>(source.getOwnProperties()));
        setDefaultFileId(source.getDefaultFileId());
        setDefaultFileType(source.getDefaultFileType());
        Map<String, CmsClientProperty> defaultFileProperties = source.getDefaultFileProperties();
        if (defaultFileProperties == null) {
            defaultFileProperties = new HashMap<String, CmsClientProperty>();
        }
        setDefaultFileProperties(new HashMap<String, CmsClientProperty>(defaultFileProperties));
        if (source.getDetailpageTypeName() != null) {
            // do not copy the detail page type name unless it is not null, otherwise newly created detail pages
            // are not displayed correctly in the sitemap editor.
            setDetailpageTypeName(source.getDetailpageTypeName());
        }
        setSitePath(source.getSitePath());
        setVfsPath(source.getVfsPath());
        setLock(source.getLock());
        setEntryType(source.getEntryType());
        setInNavigation(source.isInNavigation());
        setHasForeignFolderLock(source.hasForeignFolderLock());
        setBlockingLockedChildren(source.hasBlockingLockedChildren());
        setFolderDefaultPage(source.isFolderDefaultPage());
        setResourceTypeName(source.getResourceTypeName());
        setChildrenLoadedInitially(source.getChildrenLoadedInitially());
        setFolderDefaultPage(source.isFolderDefaultPage());
        setDateExpired(source.getDateExpired());
        setDateReleased(source.getDateReleased());
        setResleasedAndNotExpired(source.isResleasedAndNotExpired());
        setRedirectTarget(source.getRedirectTarget());
        setResourceState(source.getResourceState());
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

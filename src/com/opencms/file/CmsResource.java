/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResource.java,v $
 * Date   : $Date: 2003/07/28 16:29:42 $
 * Version: $Revision: 1.73 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package com.opencms.file;

import com.opencms.core.I_CmsConstants;
import com.opencms.flex.util.CmsUUID;

import java.io.Serializable;

/**
 * This is the base class for all resources in OpenCms.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @version $Revision: 1.73 $ 
 */
public class CmsResource extends Object implements Cloneable, Serializable, Comparable {

    /** The access flags of this resource */
    private int m_accessFlags;

    /** The ID of the content database record */
    private CmsUUID m_contentId;

    /** The id of the user who created this resource */
    private CmsUUID m_createdByUser;

    /** The creation date of this resource */
    private long m_dateCreated;

    /** The date of the last modification of this resource */
    private long m_dateLastModified;

    /** The full name of a resource include it's path from the site root */
    private String m_fullResourceName;

    /** Boolean flag whether the timestamp of this resource was modified by a touch command */
    private boolean m_isTouched;

    /** The id of the user who modified this resource last */
    private CmsUUID m_lastModifiedByUser;

    /** The id of the loader which is used to process this resource */
    private int m_loaderId;

    /** The id of the user who locked this resource */
    private CmsUUID m_lockedByUserId;

    /** The projectId of the project where the resource was last modified in */
    private int m_lockedInProject;
    
    /** The ID of the parent's strcuture database record */
    private CmsUUID m_parentId;

    /** The project id this recouce belongs to */
    private int m_projectId;

    /** The flags of this resource ( not used yet; the Accessflags are stored in m_accessFlags) */
    private int m_resourceFlags;

    /** The ID of the resource database record */
    private CmsUUID m_resourceId;

    /** The name of this resource */
    private String m_resourceName;

    /** The type of this resource */
    private int m_resourceType;
    
    /** The size of the content */
    protected int m_size;

    /**
     * The state of this resource.<p>
     * A resource can have the following states:
     * <ul>
     * <li>unchanged </li>
     * <li>changed </li>
     * <li>new </li>
     * <li>deleted </li>
     * </ul>
     */
    private int m_state;

    /** The ID of the structure database record */
    private CmsUUID m_structureId;

    /**
     * Constructor, creates a new CmsRecource object.<p>
     *
     * @param structureId the structure id of the resource
     * @param resourceId the resource id of the resource
     * @param parentId the id of the parent resource in the structure table
     * @param fileId the file content id of the resource 
     * @param resourceName the name (including complete path) of the resouce
     * @param resourceType the type of the resource
     * @param resourceFlags the flags of the resource
     * @param projectId the project id this resource belongs to
     * @param accessFlags the access flags of this resource
     * @param state the state of this resource
     * @param lockedByUser the user id of the user who has locked this resource
     * @param launcherType the launcher that is require to process this recource     
     * @param dateCreated the creation date of this resource
     * @param createdByUser the user who created this resource
     * @param dateLastModified the date of the last modification of the resource
     * @param lastModifiedByUser the user who changed the file
     * @param size the file content size of the resource
     * @param lockedInProject id of the project the resource was last modified in
     */
    public CmsResource(
        CmsUUID structureId, 
        CmsUUID resourceId, 
        CmsUUID parentId, 
        CmsUUID fileId, 
        String resourceName, 
        int resourceType, 
        int resourceFlags, 
        int projectId, 
        int accessFlags, 
        int state, 
        CmsUUID lockedByUser, 
        int launcherType, 
        long dateCreated, 
        CmsUUID createdByUser, 
        long dateLastModified, 
        CmsUUID lastModifiedByUser, 
        int size, 
        int lockedInProject
    ) {
        m_structureId = structureId;
        m_resourceId = resourceId;
        m_parentId = parentId;
        m_contentId = fileId;
        m_resourceName = resourceName;
        m_resourceType = resourceType;
        m_resourceFlags = resourceFlags;
        m_projectId = projectId;
        m_accessFlags = accessFlags;
        m_loaderId = launcherType;
        m_state = state;
        m_lockedByUserId = lockedByUser;
        m_dateCreated = dateCreated;
        m_createdByUser = createdByUser;
        m_dateLastModified = dateLastModified;
        m_lastModifiedByUser = lastModifiedByUser;
        m_size = size;
        m_lockedInProject = lockedInProject;
        m_isTouched = false;

        m_fullResourceName = null;
    }

    /**
     * Returns the name of a resource without the path information.<p>
     * 
     * The resource name of a file is the name of the file.
     * The resource name of a folder is the folder name with trailing "/".
     * The resource name of the root folder is <code>/</code>.<p>
     * 
     * Example: <code>/system/workplace/</code> has the resource name <code>workplace/</code>.
     * 
     * @param resource the resource to get the name for
     * @return the name of a resource without the path information
     */
    public static String getName(String resource) {
        if (I_CmsConstants.C_ROOT.equals(resource)) {
            return I_CmsConstants.C_ROOT;
        }
        // remove the last char, for a folder this will be "/", for a file it does not matter
        String parent = (resource.substring(0, resource.length() - 1));
        // now as the name does not end with "/", check for the last "/" which is the parent folder name
        return resource.substring(parent.lastIndexOf("/") + 1);
    }

    /**
     * Returns the absolute parent folder name of a resource.<p>
     * 
     * The parent resource of a file is the folder of the file.
     * The parent resource of a folder is the parent folder.
     * The parent resource of the root folder is <code>null</code>.<p>
     * 
     * Example: <code>/system/workplace/</code> has the parent <code>/system/</code>.
     * 
     * @param resource the resource to find the parent folder for
     * @return the calculated parent absolute folder path, or <code>null</code> for the root folder 
     */
    public static String getParent(String resource) {
        if (I_CmsConstants.C_ROOT.equals(resource)) {
            return null;
        }
        // remove the last char, for a folder this will be "/", for a file it does not matter
        String parent = (resource.substring(0, resource.length() - 1));
        // now as the name does not end with "/", check for the last "/" which is the parent folder name
        return parent.substring(0, parent.lastIndexOf("/") + 1);
    }

    /**
     * Returns the folder path of the resource with the given name,
     * if the resource is a folder (i.e. ends with a "/"), the complete path of the folder 
     * is returned (not the parent folder path).<p>
     * 
     * This is achived by just cutting of everthing behind the last occurence of a "/" character
     * in the String, no check if performed if the resource exists or not in the VFS, 
     * only resources that end with a "/" are considered to be folders.
     * 
     * Example: Returns <code>/system/def/</code> for the
     * resource <code>/system/def/file.html</code> and 
     * <code>/system/def/</code> for the (folder) resource <code>/system/def/</code>..
     *
     * @param resource the name of a resource
     * @return the folder of the given resource
     */
    public static String getPath(String resource) {
        return resource.substring(0, resource.lastIndexOf("/") + 1);
    }

    /**
     * Returns the directory level of a resource.<p>
     * 
     * The root folder "/" has level 0,
     * a folder "/foo/" would have level 1,
     * a folfer "/foo/bar/" level 2 etc.<p> 
     * 
     * @param resource the resource to determin the directory level for
     * @return the directory level of a resource
     */
    public static int getPathLevel(String resource) {
        int level = -1;
        int pos = 0;
        while (resource.indexOf('/', pos) >= 0) {
            pos = resource.indexOf('/', pos) + 1;
            level++;
        }
        return level;
    }

    /**
     * Returns the name of a parent folder of the given resource, 
     * that is either minus levels up 
     * from the current folder, or that is plus levels down from the 
     * root folder.<p>
     * 
     * @param resource the name of a resource
     * @param level of levels to walk up or down
     * @return the name of a parent folder of the given resource 
     */
    public static String getPathPart(String resource, int level) {
        resource = getPath(resource);
        String result = null;
        int pos = 0, count = 0;
        if (level >= 0) {
            // Walk down from the root folder /
            while ((count < level) && (pos > -1)) {
                count++;
                pos = resource.indexOf('/', pos + 1);
            }
        } else {
            // Walk up from the current folder
            pos = resource.length();
            while ((count > level) && (pos > -1)) {
                count--;
                pos = resource.lastIndexOf('/', pos - 1);
            }
        }
        if (pos > -1) {
            // To many levels walked
            result = resource.substring(0, pos + 1);
        } else {
            // Add trailing slash
            result = (level < 0) ? "/" : resource;
        }
        return result;
    }

    /**
     * Returns true if the resource name is a folder name, i.e. ends with a "/".<p>
     * 
     * @param resource the resource to check
     * @return true if the resource name is a folder name, i.e. ends with a "/"
     */
    public static boolean isFolder(String resource) {
        return ((resource != null) && resource.endsWith("/"));
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new CmsResource(m_structureId, m_resourceId, m_parentId, m_contentId, m_resourceName, m_resourceType, m_resourceFlags, m_projectId, m_accessFlags, m_state, m_lockedByUserId, m_loaderId, m_dateCreated, m_createdByUser, m_dateLastModified, m_lastModifiedByUser, m_size, m_lockedInProject);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if ((o == null) || (!(o instanceof CmsResource))) {
            return 0;
        }

        String ownResourceName = getResourceName();
        String otherResourceName = ((CmsResource)o).getResourceName();

        return ownResourceName.compareTo(otherResourceName);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof CmsResource) {
            if (((CmsResource)obj).getId().equals(this.getId())) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Returns the absolute path of this resource without the current site root,
     * e.g. <code>/folder/index.html</code><p>
     *
     * @param cms the current cms context (to obtain the site root from)
     * @return the absolute path of the this resource
     */    
    public String getAbsolutePath(CmsObject cms) {               
        return cms.getRequestContext().removeSiteRoot(m_fullResourceName);
    }

    /**
     * Returns the accessflags of this resource.<p>
     *
     * @return the accessflags of this resource
     * @deprecated no access flags are maintained on the resource in the new permission model
     */
    public int getAccessFlags() {
        return m_accessFlags;
    }
    
    /**
     * Returns the date of the creation of this resource.<p>
     *
     * @return the date of the creation of this resource
     */
    public long getDateCreated() {
        return m_dateCreated;
    }
    
    /**
     * Returns the date of the last modification of this resource.<p>
     *
     * @return the date of the last modification of this resource
     */
    public long getDateLastModified() {
        return m_dateLastModified;
    }

    /**
     * Gets the id of the file content database entry.<p>
     *
     * @return the ID of the file content database entry
     */
    public CmsUUID getFileId() {
        return m_contentId;
    }

    /**
     * Returns the flags of this resource.<p>
     *
     * @return the flags of this resource
     */
    public int getFlags() {
        return m_resourceFlags;
    }

    /**
     * Returns the full resource name of this resource including the path,
     * also including the current site context 
     * e.g. <code>/default/vfs/system/workplace/action/index.html</code>.<p>
     *
     * @return the resource name including it's path
     */
    public String getFullResourceName() {
        return m_fullResourceName;
    }

    /**
     * Returns the id of the file structure database entry of this resource.<p>
     * 
     * @return the id of the file structure database
     */
    public CmsUUID getId() {
        return m_structureId;
    }
    
    /**
     * Gets the length of the content (i.e. the file size).<p>
     *
     * @return the length of the content
     */
    public int getLength() {
        return m_size;
    }

    /**
     * Gets the loader id of this resource.<p>
     *
     * @return the loader type id of this resource
     */
    public int getLoaderId() {
        return m_loaderId;
    }

    /**
     * Returns the ID of the project if the resource is directly locked in the database<p>
     * 
     * Don't use this method to detect the lock state of a resource. 
     * Use {@link com.opencms.file.CmsObject#getLock(CmsResource)} 
     * instead to detect the lock state.
     *
     * @return the project id
     * @see org.opencms.lock.CmsLock#getProjectId()
     */
    public int getLockedInProject() {
        return m_lockedInProject;
    }
    
    /**
     * Returns the name of this resource, e.g. <code>index.html</code>.<p>
     *
     * @return the name of this resource
     * @deprecated use {@link #getResourceName()} instead
     */
    public String getName() {
        return getResourceName();
    }
    
    /**
     * Returns the parent folder name of this resource with absolute path from the site root,
     * same as callig <code>getParent(getAbsolutePath(cms))</code>.<p>
     * 
     * Example: <code>/system/workplace/</code> has the parent <code>/system/</code>.
     * 
     * @param cms the current cms context
     * @see #getParent(String)
     * @return the parent folder name of this resource with absolute path from the site root, 
     * or <code>null</code> for the site root folder 
     */
     public String getParent(CmsObject cms) {
        return getParent(getAbsolutePath(cms));
     }    

    /**
     * Gets the ID of the parent's file tree/hierarchy database entry.
     *
     * @return the ID of the parent's file tree/hierarchy database entry.
     */
    public CmsUUID getParentId() {
        return m_parentId;
    }
     
    /**
     * Returns the folder path of this resource with absolute path from the site root,
     * same as calling <code>getPath(getAbsolutePath(cms))</code>.<p>
     *
     * @param cms the current cms context
     * @see #getPath(String)
     * @return the folder path of this resource with absolute path from the site root
     */
    public String getPath(CmsObject cms) {
        return getPath(getAbsolutePath(cms));
    }     

    /**
     * Returns the project id of this resource.<p>
     *
     * @return the project id of this resource.
     */
    public int getProjectId() {
        return m_projectId;
    }

    /**
     * Encapsulates which id of this resource is used to handle ACE's for resources/files/folders.<p>
     * 
     * @return the resource id of this resource
     */
    public CmsUUID getResourceAceId() {
        return getResourceId();
    }

    /**
     * Returns the id of the resource database entry of this resource.<p>
     *
     * @return the id of the resource database entry
     */
    public CmsUUID getResourceId() {
        return m_resourceId;
    }

    /**
     * Returns the name of this resource, e.g. <code>index.html</code>.<p>
     *
     * @return the name of this resource
     */
    public String getResourceName() {
        return m_resourceName;
    }
    
    /**
     * Returns the state of this resource.<p>
     *
     * This may be C_STATE_UNCHANGED, C_STATE_CHANGED, C_STATE_NEW or C_STATE_DELETED.<p>
     *
     * @return the state of this resource
     */
    public int getState() {
        return m_state;
    }
    
    /**
     * Returns the type id for this resource.<p>
     *
     * @return the type id of this resource.
     */
    public int getType() {
        return m_resourceType;
    }

    /**
     * Returns the user id of the user who created this resource.<p>
     * 
     * @return the user id
     */
    public CmsUUID getUserCreated() {
        return m_createdByUser;
    }
    
    /**
     * Returns the user id of the user who made the last change on this resource.<p>
     *
     * @return the user id of the user who made the last change<p>
     */
    public CmsUUID getUserLastModified() {
        return m_lastModifiedByUser;
    }

    /**
     * Checks whether the current state of this resource contains the full resource
     * path including the site root or not.<p>
     * 
     * @return true if the current state of this resource contains the full resource path
     * @deprectated in the revised resouce model
     */
    public boolean hasFullResourceName() {
        return (getFullResourceName() != null);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (m_structureId != null)
            return m_structureId.hashCode();
        return CmsUUID.getNullUUID().hashCode();
    }

    /**
    * Checks if this resource belongs to a project.<p>
    * 
    * @param project the project which this resources is checked against
    * @return true if this resource is in the project, false otherwise
     */
    public boolean inProject(CmsProject project) {
        return project.getId() == getProjectId();
    }
    
    /**
     * Determines if this resource is a file.<p>
     *
     * @return true if this resource is a file, false otherwise
     */
    public boolean isFile() {
        return getType() != CmsResourceTypeFolder.C_RESOURCE_TYPE_ID;
    }

    /**
     * Checks if this resource is a folder.<p>
     * 
     * @return true if this is is a folder
     */
    public boolean isFolder() {
        return this.getType() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID;
    }

    /**
     * Checks if this resource is currently directly locked in the database.<p>
     * 
     * Don't use this method to detect the lock state of a resource. 
     * Use {@link com.opencms.file.CmsObject#getLock(CmsResource)} 
     * instead to detect the lock state.
     *
     * @return true, if and only if this resource is directly locked by a user
     * @deprecated use {@link com.opencms.file.CmsObject#getLock(CmsResource)} instead to detect the lock state
     * @see org.opencms.lock.CmsLock#isNullLock()
     */
    public boolean isLocked() {
        return !isLockedBy().equals(CmsUUID.getNullUUID());
    }

    /**
     * Returns the ID of the user if the resource is directly locked in the database.<p>
     * 
     * Don't use this method to detect the lock state of a resource. 
     * Use {@link com.opencms.file.CmsObject#getLock(CmsResource)} 
     * instead to detect the lock state.
     *
     * @return the CmsUUID of the user
     * @see org.opencms.lock.CmsLock#getUserId()
     */
    public CmsUUID isLockedBy() {
        return m_lockedByUserId;
    }

    /**
     * Returns true if this resource was touched.<p>
     * 
     * @return boolean true if this resource was touched
     */
    public boolean isTouched() {
        return m_isTouched;
    }

    /**
    * Sets the access flags of this resource.<p>
    *
    * @param flags the access flags to set
    */
    public void setAccessFlags(int flags) {
        m_accessFlags = flags;
    }

    /**
     * Sets the date of the last modification of this resource.<p>
     * 
     * @param time the date to set
     */
    public void setDateLastModified(long time) {
        m_isTouched = true;
        m_dateLastModified = time;
    }
    
    /**
     * Sets the file id of this resource.<p>
     *
     * @param file the file id to set
     */
    public void setFileId(CmsUUID file) {
        m_contentId = file;
    }
    
    /**
    * Sets the flags of this resource.<p>
    *
    * @param flags the flags to set
    */
    void setFlags(int flags) {
        m_resourceFlags = flags;
    }

    /**
     * Sets the resource name including the path.<p>
     * 
     * @param fullResourceName the resource name including the path
     */
    public void setFullResourceName(String fullResourceName) {
        m_fullResourceName = fullResourceName;
    }

    /**
    * Sets the loader id of this resource.<p>
    *
    * @param the loader id of this resource
    */
    public void setLoaderId(int loaderId) {
        m_loaderId = loaderId;
    }
    
    /**
     * Sets the the user id that locked this resource.<p>
     *
     * @param user the user id to set
     */
    public void setLocked(CmsUUID user) {
        m_lockedByUserId = user;
    }

    /**
     * Sets the project id in which this resource is locked.<p>
     *
     * @param project a project id
     */
    public void setLockedInProject(int project) {
        m_lockedInProject = project;
    }

    /**
     * Sets the parent of this resource.<p>
     *
     * @param parent the id of the parent resource
     */
    public void setParentId(CmsUUID parent) {
        m_parentId = parent;
    }

    /**
     * Sets the project id of this resource.<p>
     *
     * @param project a project id
     */
    public void setProjectId(int project) {
        m_projectId = project;
    }
    
    /**
     * Sets the state of this resource.<p>
     *
     * @param state the state to set
     */
    public void setState(int state) {
        m_state = state;
    }

    /**
     * Sets the type of this resource.<p>
     *
     * @param type the type to set
     */
    public void setType(int type) {
        m_resourceType = type;
    }

    /**
     * Sets the user id of the user who created this resource.
     * 
     * @param resourceCreatedByUserId user id
     */
    public void setUserCreated(CmsUUID resourceCreatedByUserId) {
        m_createdByUser = resourceCreatedByUserId;
    }
    
    /**
     * Sets the user id of the user who changed this resource.<p>
     *
     * @param resourceLastModifiedByUserId the user id of the user who changed the resource
     */
    public void setUserLastModified(CmsUUID resourceLastModifiedByUserId) {
        m_lastModifiedByUser = resourceLastModifiedByUserId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("[Resource]:");
        output.append(m_resourceName);
        output.append(" ID: ");
        output.append(m_resourceId);
        output.append(" ParentID: ");
        output.append(m_parentId);
        output.append(" , Project=");
        output.append(m_projectId);
        output.append(" : Resource-type=");
        output.append(getType());
        output.append(" : Locked=");
        output.append(isLockedBy());
        output.append(" : length=");
        output.append(getLength());
        output.append(" : state=");
        output.append(getState());
        output.append(" : Date created=");
        output.append(new java.util.Date(getDateCreated()));
        output.append(" : User created=");
        output.append(getUserCreated());
        output.append(" : Date lastmodified=");
        output.append(new java.util.Date(getDateLastModified()));
        output.append(" : User lastmodified=");
        output.append(getUserLastModified());
        return output.toString();
    }
}

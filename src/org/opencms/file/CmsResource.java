/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsResource.java,v $
 * Date   : $Date: 2004/02/13 13:41:44 $
 * Version: $Revision: 1.1 $
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

package org.opencms.file;

import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;


import java.io.Serializable;

/**
 * Base class for all OpenCms resources.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @version $Revision: 1.1 $ 
 */
public class CmsResource extends Object implements Cloneable, Serializable, Comparable {

    /** The ID of the content database record */
    private CmsUUID m_contentId;

    /** The creation date of this resource */
    private long m_dateCreated;

    /** The date of the last modification of this resource */
    private long m_dateLastModified;

    /** The flags of this resource ( not used yet; the Accessflags are stored in m_accessFlags) */
    private int m_flags;

    /** Boolean flag whether the timestamp of this resource was modified by a touch command */
    private boolean m_isTouched;

    /** The size of the content */
    protected int m_length;

    /** The number of links that point to this resource */
    private int m_linkCount;

    /** The id of the loader which is used to process this resource */
    private int m_loaderId;
    
    /** The name of this resource */
    private String m_name;
    
    /** The ID of the parent's strcuture database record */
    private CmsUUID m_parentId;

    /** The project id where this resource has been last modified in */
    private int m_projectLastModified;

    /** The ID of the resource database record */
    private CmsUUID m_resourceId;

    /** The name of a resource with it's full path from the root folder including the current site root */
    private String m_rootPath;

    /** The state of this resource */
    private int m_state;

    /** The ID of the structure database record */
    private CmsUUID m_structureId;

    /** The type of this resource */
    private int m_type;
    
    /** The id of the user who created this resource */
    private CmsUUID m_userCreated;
    
    /** The id of the user who modified this resource last */
    private CmsUUID m_userLastModified;

    /**
     * Constructor, creates a new CmsRecource object.<p>
     *
     * @param structureId the id of this resources structure record
     * @param resourceId the id of this resources resource record
     * @param parentId the id of this resources parent folder
     * @param fileId the id of this resources content record
     * @param name the filename of this resouce
     * @param type the type of this resource
     * @param flags the flags of this resource
     * @param projectId the project id this resource was last modified in
     * @param state the state of this resource
     * @param loaderId the id for the that is used to load this recource
     * @param dateCreated the creation date of this resource
     * @param userCreated the id of the user who created this resource
     * @param dateLastModified the date of the last modification of this resource
     * @param userLastModified the id of the user who did the last modification of this resource
     * @param size the size of the file content of this resource
     * @param linkCount the count of all siblings of this resource 
     */
    public CmsResource(
        CmsUUID structureId, 
        CmsUUID resourceId, 
        CmsUUID parentId, 
        CmsUUID fileId, 
        String name, 
        int type, 
        int flags, 
        int projectId, 
        int state, 
        int loaderId, 
        long dateCreated, 
        CmsUUID userCreated, 
        long dateLastModified, 
        CmsUUID userLastModified, 
        int size, 
        int linkCount
    ) {
        m_structureId = structureId;
        m_resourceId = resourceId;
        m_parentId = parentId;
        m_contentId = fileId;
        m_name = name;
        m_type = type;
        m_flags = flags;
        m_projectLastModified = projectId;
        m_loaderId = loaderId;
        m_state = state;
        m_dateCreated = dateCreated;
        m_userCreated = userCreated;
        m_dateLastModified = dateLastModified;
        m_userLastModified = userLastModified;
        m_length = size;
        m_linkCount = linkCount;
        
        m_isTouched = false;
        m_rootPath = null;
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
    public static String getFolderPath(String resource) {
        return resource.substring(0, resource.lastIndexOf('/') + 1);
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
        return resource.substring(parent.lastIndexOf('/') + 1);
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
    public static String getParentFolder(String resource) {
        if (I_CmsConstants.C_ROOT.equals(resource)) {
            return null;
        }
        // remove the last char, for a folder this will be "/", for a file it does not matter
        String parent = (resource.substring(0, resource.length() - 1));
        // now as the name does not end with "/", check for the last "/" which is the parent folder name
        return parent.substring(0, parent.lastIndexOf('/') + 1);
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
        resource = getFolderPath(resource);
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
        return ((resource != null) && (resource.charAt(resource.length()-1) == '/'));
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {
        return new CmsResource(
            m_structureId, 
            m_resourceId, 
            m_parentId, 
            m_contentId, 
            m_name, 
            m_type,
            m_flags, 
            m_projectLastModified, 
            m_state, 
            m_loaderId, 
            m_dateCreated, 
            m_userCreated, 
            m_dateLastModified, 
            m_userLastModified, 
            m_length, 
            m_linkCount
        );
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if ((o == null) || (!(o instanceof CmsResource))) {
            return 0;
        }

        String ownResourceName = getName();
        String otherResourceName = ((CmsResource)o).getName();

        return ownResourceName.compareTo(otherResourceName);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof CmsResource) {
            return ((CmsResource)obj).getStructureId().equals(getStructureId());
            }
        return false;
    }

    /**
     * Same as calling {@link #getPath()}, only for backward compatibility,
     * deprecated, will be removed in the next version.<p>
     * 
     * @return same result as calling getPath()
     * @see #getPath()
     * @deprecated use getPath(), this method will be removed in the next version
     */
    public String getAbsolutePath() {
        return getPath();
    }

    /**
     * Used to return the access flags of this resource,
     * deprecated, will now always throw a <code>RuntimeException</code><p>
     *
     * @return will throw a RuntimeException
     * @deprecated the access flags are not part of the resource in the revised resource model
     */
    public int getAccessFlags() {
        throw new RuntimeException("getAccessFlags() not longer supported on CmsResource");
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
        return m_flags;
    }
    
    /**
     * Gets the length of the content (i.e. the file size).<p>
     *
     * @return the length of the content
     */
    public int getLength() {
        return m_length;
    }

    /**
     * Gets the number of references to the resource.<p>
     * 
     * @return the number of links
     */
    public int getLinkCount() {
        return m_linkCount;
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
     * Used to returns the ID of the project if the resource is locked in the database,
     * deprecated, will now always throw a <code>RuntimeException</code><p>
     * 
     * Don't use this method to detect the lock state of a resource. 
     * Use {@link org.opencms.file.CmsObject#getLock(CmsResource)} instead.
     *
     * @return will throw a RuntimeException
     * @deprecated the lock state is not part of the resource in the revised resource model
     * @see org.opencms.file.CmsObject#getLock(CmsResource)
     * @see org.opencms.lock.CmsLock#getProjectId()
     */
    public int getLockedInProject() {
        throw new RuntimeException("getLockedInProject() not longer supported on CmsResource");
    }

    /**
     * Returns the name of this resource, e.g. <code>index.html</code>.<p>
     *
     * @return the name of this resource
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the structure record id of the parent of this resource.<p>
     *
     * @return the structure record id of the parent of this resource
     */
    public CmsUUID getParentStructureId() {
        return m_parentId;
    }
    
    /**
     * Returns the name of this resource including the full path in the current site,
     * but without the current site root.<p>
     *
     * @return the name of this resource including the full path in the current site
     */
    public String getPath() {
        // TODO: Must be implemented
        return null;
    }    

    /**
     * Returns the id of the project where the resource has been last modified.<p>
     *
     * @return the id of the project where the resource has been last modified
     */
    public int getProjectLastModified() {
        return m_projectLastModified;
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
     * Returns the name of a resource with it's full path from the root folder including the current site root, 
     * e.g. <code>/default/vfs/system/workplace/action/index.html</code>.<p>
     *
     * @return the name of a resource with it's full path from the root folder including the current site root
     */
    public String getRootPath() {
        if (m_rootPath == null) {
            throw new RuntimeException("Full resource name not set for CmsResource " + getName());
        }
        return m_rootPath;
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
     * Returns the id of the structure record of this resource.<p>
     * 
     * @return the id of the structure record of this resource
     */
    public CmsUUID getStructureId() {
        return m_structureId;
    }
    
    /**
     * Returns the type id for this resource.<p>
     *
     * @return the type id of this resource.
     */
    public int getType() {
        return m_type;
    }

    /**
     * Returns the user id of the user who created this resource.<p>
     * 
     * @return the user id
     */
    public CmsUUID getUserCreated() {
        return m_userCreated;
    }
    
    /**
     * Returns the user id of the user who made the last change on this resource.<p>
     *
     * @return the user id of the user who made the last change<p>
     */
    public CmsUUID getUserLastModified() {
        return m_userLastModified;
    }

    /**
     * Checks whether the current state of this resource contains the full resource
     * path including the site root or not.<p>
     * 
     * @return true if the current state of this resource contains the full resource path
     */
    public boolean hasFullResourceName() {
        return m_rootPath != null;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (m_structureId != null) {
            return m_structureId.hashCode();
        }

        return CmsUUID.getNullUUID().hashCode();
    }

    /**
     * Used to check if this resource is inside the specified project,
     * deprecated, will now always throw a <code>RuntimeException</code><p>
     * 
     * @param project the specified project
     * @return false
     * @deprecated the project state is not part of the resource in the revised resource model
     * @see org.opencms.file.CmsObject#isInsideCurrentProject(CmsResource)
     */
    public boolean inProject(CmsProject project) {
        OpenCms.getLog(this).warn("Deprecated method called: inProject(" + project + ")");
        throw new RuntimeException("inProject() not longer supported on CmsResource");
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
        return getType() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID;
    }
    
    /**
     * Checks if the resource is internal.<p>
     * 
     * This state is stored as bit 1 in the resource flags.<p>
     * 
     * @return true if the resource is internal, otherwise false
     */
    public boolean isInternal() {
        return ((m_flags & I_CmsConstants.C_RESOURCEFLAG_INTERNAL) > 0);
    }
    
    /**
     * Checks if the link has to be labeled with a special icon in the explorer view.<p>
     *
     * This state is stored as bit 2 in the resource flags.<p>
     * 
     * @return true if a link to the resource has to be labeled, otherwise false
     */
    public boolean isLabeled() {
        return ((m_flags & I_CmsConstants.C_RESOURCEFLAG_LABELLINK) > 0);
    }

    /**
     * Used to check if this resource is currently directly locked in the database,
     * deprecated, will now always throw a <code>RuntimeException</code><p>
     * 
     * Don't use this method to detect the lock state of a resource. 
     * Use {@link org.opencms.file.CmsObject#getLock(CmsResource)} instead.
     *
     * @return will throw a RuntimeException
     * @deprecated the lock state is not part of the resource in the revised resource model
     * @see org.opencms.file.CmsObject#getLock(CmsResource)
     * @see org.opencms.lock.CmsLock#isNullLock()
     */
    public boolean isLocked() {
        throw new RuntimeException("isLocked() not longer supported on CmsResource");
    }

    /**
     * Used to return the ID of the user if the resource is directly locked in the database,
     * deprecated, will now always throw a <code>RuntimeException</code><p>
     * 
     * Don't use this method to detect the lock state of a resource. 
     * Use {@link org.opencms.file.CmsObject#getLock(CmsResource)} instead.
     *
     * @return will throw a RuntimeException
     * @deprecated the lock state is not part of the resource in the revised resource model
     * @see org.opencms.file.CmsObject#getLock(CmsResource)
     * @see org.opencms.lock.CmsLock#getUserId()
     */
    public CmsUUID isLockedBy() {
        throw new RuntimeException("isLockedBy() not longer supported on CmsResource");
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
     * @deprecated the access flags are not longer maintained on the resource in the revised OpenCms ACL model
     * @param flags the access flags to set
     */
    public void setAccessFlags(int flags) {
        OpenCms.getLog(this).warn("Deprecated method called: setAccessFlags(" + flags + ")");
        throw new RuntimeException("setAccessFlags() not longer supported on CmsResource");
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
    * Sets the flags of this resource.<p>
    *
    * @param flags int value with flag values to set
    */
    public void setFlags(int flags) {
        m_flags = flags;
    }

    /**
     * Sets the resource name including the path.<p>
     * 
     * @param fullResourceName the resource name including the path
     */
    public void setFullResourceName(String fullResourceName) {
        m_rootPath = fullResourceName;
    }

    /**
    * Sets the loader id of this resource.<p>
    *
    * @param loaderId the loader id of this resource
    */
    public void setLoaderId(int loaderId) {
        m_loaderId = loaderId;
    }
    
    /**
     * Used to set the the user id that locked this resource,
     * deprecated, will now always throw a <code>RuntimeException</code><p>
     * 
     * Don't use this method to detect the lock state of a resource. 
     * Use {@link org.opencms.file.CmsObject#getLock(CmsResource)} instead.
     *
     * @param user the user id to set
     * @deprecated the lock state is not part of the resource in the revised resource model
     * @see org.opencms.file.CmsObject#getLock(CmsResource)
     * @see org.opencms.lock.CmsLock#getUserId()
     */
    public void setLocked(CmsUUID user) {
        OpenCms.getLog(this).warn("Deprecated method called: setLocked(" + user + ")");        
        throw new RuntimeException("setLocked() not longer supported on CmsResource");
    }

    /**
     * Used to set the project id in which this resource is locked,
     * deprecated, will now always throw a <code>RuntimeException</code><p>
     * 
     * Don't use this method to detect the lock state of a resource. 
     * Use {@link org.opencms.file.CmsObject#getLock(CmsResource)} instead.
     *
     * @param projectId a project id
     * @deprecated the lock state is not part of the resource in the revised resource model
     * @see org.opencms.file.CmsObject#getLock(CmsResource)
     * @see org.opencms.lock.CmsLock#getProjectId()
     */
    public void setLockedInProject(int projectId) {
        OpenCms.getLog(this).warn("Deprecated method called: setLockedInProject(" + projectId + ")");                
        throw new RuntimeException("setLockedInProject() not longer supported on CmsResource");
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
     * Used to sets the ID of the project where the resource has been last modified,
     * deprecated, will now always throw a <code>RuntimeException</code><p>
     * 
     * It is unsafe to set the project state explicit, the project state is saved 
     * implicit when the resource get modified. Thus, this value is never written 
     * to the database.
     *
     * @param projectId the ID of the project where the resource has been last modified
     * @deprecated the project state is not part of the resource in the revised resource model
     */
    public void setProjectId(int projectId) {
        OpenCms.getLog(this).warn("Deprecated method called: setProjectId(" + projectId + ")");                        
        throw new RuntimeException("setProjectId() not longer supported on CmsResource");
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
        m_type = type;
    }
    
    /**
     * Sets the user id of the user who changed this resource.<p>
     *
     * @param resourceLastModifiedByUserId the user id of the user who changed the resource
     */
    public void setUserLastModified(CmsUUID resourceLastModifiedByUserId) {
        m_userLastModified = resourceLastModifiedByUserId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        
        result.append("[");
        result.append(this.getClass().getName());
        result.append(", name: ");
        result.append(m_name);
        result.append(", structure-ID: ");
        result.append(m_structureId);
        result.append(", resource-ID: ");
        result.append(m_resourceId);
        result.append(", parent-ID: ");
        result.append(m_parentId);
        result.append(", content-ID: ");
        result.append(m_contentId);               
        result.append(", type: ");
        result.append(m_type);
        result.append(", flags: ");
        result.append(m_flags);  
        result.append(", project: ");
        result.append(m_projectLastModified);
        result.append(", state: ");
        result.append(m_state);        
        result.append(", loader-ID: ");
        result.append(m_loaderId);
        result.append(", date created: ");
        result.append(new java.util.Date(m_dateCreated));
        result.append(", user created: ");
        result.append(m_userCreated);
        result.append(", date lastmodified: ");
        result.append(new java.util.Date(m_dateLastModified));
        result.append(", user lastmodified: ");
        result.append(m_userLastModified);
        result.append(", size: ");
        result.append(m_length);        
        result.append(" link count: ");
        result.append(m_linkCount);
        result.append("]");
        
        return result.toString();
    }
    
}

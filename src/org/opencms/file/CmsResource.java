/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsResource.java,v $
 * Date   : $Date: 2004/06/28 07:47:33 $
 * Version: $Revision: 1.10 $
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

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * Base class for all OpenCms resources.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @version $Revision: 1.10 $ 
 */
public class CmsResource extends Object implements Cloneable, Serializable, Comparable {

    /** The default expiration date of a resource (which is: never expires). */
    public static long DATE_EXPIRED_DEFAULT = Long.MAX_VALUE;

    /** The default release date of a resource (which is: always released). */
    public static long DATE_RELEASED_DEFAULT;

    /** The size of the content. */
    protected int m_length;

    /** The id of the content database record. */
    private CmsUUID m_contentId;

    /** The creation date of this resource. */
    private long m_dateCreated;

    /** The expiration date of this resource. */
    private long m_dateExpired;

    /** The date of the last modification of this resource. */
    private long m_dateLastModified;

    /** The release date of this resource. */
    private long m_dateReleased;

    /** The flags of this resource ( not used yet; the access flags are stored in m_accessFlags). */
    private int m_flags;

    /** Boolean flag whether the timestamp of this resource was modified by a touch command. */
    private boolean m_isTouched;

    /** The id of the loader which is used to process this resource. */
    private int m_loaderId;

    /** The name of this resource. */
    private String m_name;

    /** The id of the parent's strcuture database record. */
    private CmsUUID m_parentId;

    /** The project id where this resource has been last modified in. */
    private int m_projectLastModified;

    /** The id of the resource database record. */
    private CmsUUID m_resourceId;

    /** The name of a resource with it's full path from the root folder including the current site root. */
    private String m_rootPath;

    /** The number of links that point to this resource. */
    private int m_siblingCount;

    /** The state of this resource. */
    private int m_state;

    /** The id of the structure database record. */
    private CmsUUID m_structureId;

    /** The resource type id of this resource. */
    private int m_typeId;

    /** The id of the user who created this resource. */
    private CmsUUID m_userCreated;

    /** The id of the user who modified this resource last. */
    private CmsUUID m_userLastModified;

    /**
     * Constructor, creates a new CmsRecource object.<p>
     *
     * @param structureId the id of this resources structure record
     * @param resourceId the id of this resources resource record
     * @param parentId the id of this resources parent folder
     * @param contentId the id of this resources content record
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
     * @param dateReleased the release date of this resource
     * @param dateExpired the expiration date of this resource
     * @param linkCount the count of all siblings of this resource 
     * @param size the size of the file content of this resource
     */
    public CmsResource(
        CmsUUID structureId,
        CmsUUID resourceId,
        CmsUUID parentId,
        CmsUUID contentId,
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
        long dateReleased,
        long dateExpired,
        int linkCount,
        int size) {

        m_structureId = structureId;
        m_resourceId = resourceId;
        m_parentId = parentId;
        m_contentId = contentId;
        m_name = name;
        m_typeId = type;
        m_flags = flags;
        m_projectLastModified = projectId;
        m_loaderId = loaderId;
        m_state = state;
        m_dateCreated = dateCreated;
        m_userCreated = userCreated;
        m_dateLastModified = dateLastModified;
        m_userLastModified = userLastModified;
        m_length = size;
        m_siblingCount = linkCount;
        m_dateReleased = dateReleased;
        m_dateExpired = dateExpired;
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

        return ((resource != null) && (resource.charAt(resource.length() - 1) == '/'));
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
            m_typeId,
            m_flags,
            m_projectLastModified,
            m_state,
            m_loaderId,
            m_dateCreated,
            m_userCreated,
            m_dateLastModified,
            m_userLastModified,
            m_dateReleased,
            m_dateExpired,
            m_siblingCount,
            m_length);
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
     * Gets the id of the content database entry.<p>
     *
     * @return the id of the content database entry
     */
    public CmsUUID getContentId() {

        return m_contentId;
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
     * Returns the expiration date this resource.<p>
     *
     * @return the expiration date of this resource
     */
    public long getDateExpired() {

        return m_dateExpired;
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
     * Returns the release date this resource.<p>
     *
     * @return the release date of this resource
     */
    public long getDateReleased() {

        return m_dateReleased;
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
     * Gets the loader id of this resource.<p>
     *
     * @return the loader type id of this resource
     */
    public int getLoaderId() {

        return m_loaderId;
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
     * Returns the name of a resource with it's full path from the root folder 
     * including the current site root, 
     * for example <code>/sites/default/myfolder/index.html</code>.<p>
     *
     * In a presentation level application usually the current site root must be
     * cut of from the root path. Use {@link CmsObject#getSitePath(CmsResource)} 
     * to get the "absolute" path of a resource in the current site.<p>
     *
     * @return the name of a resource with it's full path from the root folder 
     *      including the current site root
     * 
     * @see CmsObject#getSitePath(CmsResource)
     * @see CmsRequestContext#getSitePath(CmsResource)
     * @see CmsRequestContext#removeSiteRoot(String) 
     */
    public String getRootPath() {

        if (m_rootPath == null) {
            throw new RuntimeException("Full resource name not set for CmsResource " + getName());
        }
        return m_rootPath;
    }

    /**
     * Returns the number of siblings of the resource.<p>
     * 
     * @return the number of siblings
     */
    public int getSiblingCount() {

        return m_siblingCount;
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
     * Returns the resource type id for this resource.<p>
     *
     * @return the resource type id of this resource.
     */
    public int getTypeId() {

        return m_typeId;
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
     * Determines if this resource is a file.<p>
     *
     * @return true if this resource is a file, false otherwise
     */
    public boolean isFile() {

        return getTypeId() != CmsResourceTypeFolder.C_RESOURCE_TYPE_ID;
    }

    /**
     * Checks if this resource is a folder.<p>
     * 
     * @return true if this is is a folder
     */
    public boolean isFolder() {

        return getTypeId() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID;
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
     * Returns true if this resource was touched.<p>
     * 
     * @return boolean true if this resource was touched
     */
    public boolean isTouched() {

        return m_isTouched;
    }

    /**
     * Sets the expiration date this resource.<p>
     * 
     * @param time the date to set
     */
    public void setDateExpired(long time) {

        m_isTouched = true;
        m_dateExpired = time;
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
     * Sets the release date this resource.<p>
     * 
     * @param time the date to set
     */
    public void setDateReleased(long time) {

        m_isTouched = true;
        m_dateReleased = time;
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
     * Sets the resource name including the full root path.<p>
     * 
     * @param rootPath the resource name including the full root path to set
     * 
     * @see #getRootPath()
     * @see CmsObject#getSitePath(CmsResource)
     * @see CmsRequestContext#getSitePath(CmsResource)
     * @see CmsRequestContext#removeSiteRoot(String)
     */
    public void setRootPath(String rootPath) {

        if (isFolder() && !CmsResource.isFolder(rootPath)) {
            // ensure a folder always ends with a /
            m_rootPath = rootPath.concat("/");
        } else {
            m_rootPath = rootPath;
        }
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
     * Sets the parent of this resource.<p>
     *
     * @param parent the id of the parent resource
     */
    public void setParentId(CmsUUID parent) {

        m_parentId = parent;
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

        m_typeId = type;
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
        result.append(", structure id ");
        result.append(m_structureId);
        result.append(", resource id: ");
        result.append(m_resourceId);
        result.append(", parent id: ");
        result.append(m_parentId);
        result.append(", content id: ");
        result.append(m_contentId);
        result.append(", type id: ");
        result.append(m_typeId);
        result.append(", flags: ");
        result.append(m_flags);
        result.append(", project: ");
        result.append(m_projectLastModified);
        result.append(", state: ");
        result.append(m_state);
        result.append(", loader id: ");
        result.append(m_loaderId);
        result.append(", date created: ");
        result.append(new java.util.Date(m_dateCreated));
        result.append(", user created: ");
        result.append(m_userCreated);
        result.append(", date lastmodified: ");
        result.append(new java.util.Date(m_dateLastModified));
        result.append(", user lastmodified: ");        
        result.append(m_userLastModified);
        result.append(", date released: ");
        result.append(new java.util.Date(m_dateReleased));        
        result.append(", date expired: ");
        result.append(new java.util.Date(m_dateExpired));        
        result.append(", size: ");
        result.append(m_length);
        result.append(" sibling count: ");
        result.append(m_siblingCount);
        result.append("]");

        return result.toString();
    }

}
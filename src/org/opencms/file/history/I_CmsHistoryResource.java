/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/history/I_CmsHistoryResource.java,v $
 * Date   : $Date: 2011/03/23 14:53:01 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.history;

import org.opencms.db.CmsResourceState;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * A historical version of a resource in the OpenCms VFS resource history.<p>
 *
 * History resources are resources that contain additional information 
 * used to describe the historical state.<p>
 * 
 * The historical resource object extends the resource object since it be 
 * an history for a file as well as for a folder.<p>
 * 
 * History resources contain the names of the users that created or last 
 * modified the resource as string obejcts because a user id might have been 
 * deleted.<p>
 *
 * @author Michael Moossen
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 6.9.1
 */
public interface I_CmsHistoryResource extends Cloneable, Serializable, Comparable {

    /**
     * Returns the history id of this historical resource.<p>
     *
     * @return the history id of this historical resource
     * 
     * @deprecated this field has been removed
     */
    CmsUUID getBackupId();

    /**
     * Returns the user name of the creator of this historical resource.<p>
     *
     * @return the user name of the creator of this historical resource
     * 
     * @deprecated use {#getUserCreated()} instead
     *             now the {@link org.opencms.file.CmsObject#readUser(CmsUUID)} 
     *             also returns historical users
     */
    String getCreatedByName();

    /**
     * Returns the date of the last modification of the content of this resource.<p>
     *
     * @return the date of the last modification of the content of this resource
     */
    long getDateContent();

    /**
     * Returns the date of the creation of this resource.<p>
     *
     * @return the date of the creation of this resource
     */
    long getDateCreated();

    /**
     * Returns the expiration date this resource.<p>
     *
     * @return the expiration date of this resource
     */
    long getDateExpired();

    /**
     * Returns the date of the last modification of this resource.<p>
     *
     * @return the date of the last modification of this resource
     */
    long getDateLastModified();

    /**
     * Returns the release date this resource.<p>
     *
     * @return the release date of this resource
     */
    long getDateReleased();

    /**
     * Returns the flags of this resource.<p>
     *
     * @return the flags of this resource
     */
    int getFlags();

    /**
     * Returns the name of the user who last changed this historical resource.<p>
     *
     * @return the name of the user who last changed this historical resource
     * 
     * @deprecated use {#getUserLastModified()} instead
     *             with {@link org.opencms.security.CmsPrincipal#readPrincipalIncludingHistory(org.opencms.file.CmsObject, CmsUUID)} 
     */
    String getLastModifiedByName();

    /**
     * Returns the length of the resource.<p>
     *
     * If the resource is a file, then this is the byte size of the file content.
     * If the resource is a folder, then the size is always -1.<p>
     *
     * @return the length of the content
     */
    int getLength();

    /**
     * Returns the name of this resource, e.g. <code>index.html</code>.<p>
     *
     * @return the name of this resource
     */
    String getName();

    /**
     * Returns the structure id of the parent resource.<p>
     *
     * @return the structure id of the parent resource
     */
    CmsUUID getParentId();

    /**
     * Returns the id of the project where the resource has been last modified.<p>
     *
     * @return the id of the project where the resource has been last modified, or <code>null</code>
     */
    CmsUUID getProjectLastModified();

    /**
     * Returns the publish tag of this historical resource.<p>
     *
     * @return the publish tag of this historical resource
     */
    int getPublishTag();

    /**
     * Returns the publish tag of this historical resource.<p>
     *
     * @return the publish tag of this historical resource
     * 
     * @deprecated use {@link #getPublishTag()} instead
     */
    int getPublishTagId();

    /**
     * Returns the id of the resource database entry of this resource.<p>
     *
     * @return the id of the resource database entry
     */
    CmsUUID getResourceId();

    /**
     * Returns the version number of the resource part for this historical resource.<p>
     *
     * @return the version number of the resource part for this historical resource
     */
    int getResourceVersion();

    /**
     * Returns the name of a resource with it's full path from the root folder 
     * including the current site root, 
     * for example <code>/sites/default/myfolder/index.html</code>.<p>
     *
     * @return the name of a resource with it's full path from the root folder 
     *      including the current site root
     */
    String getRootPath();

    /**
     * Returns the number of siblings of the resource, also counting this resource.<p>
     * 
     * If a resource has no sibling, the total sibling count for this resource is <code>1</code>, 
     * if a resource has <code>n</code> siblings, the sibling count is <code>n + 1</code>.<p> 
     * 
     * @return the number of siblings
     */
    int getSiblingCount();

    /**
     * Returns the state of this resource.<p>
     *
     * @return the state of this resource
     */
    CmsResourceState getState();

    /**
     * Returns the id of the structure record of this resource.<p>
     * 
     * @return the id of the structure record of this resource
     */
    CmsUUID getStructureId();

    /**
     * Returns the version number of the structure part for this historical resource.<p>
     *
     * @return the version number of the structure part for this historical resource
     */
    int getStructureVersion();

    /**
     * Returns the resource type id for this resource.<p>
     *
     * @return the resource type id of this resource
     */
    int getTypeId();

    /**
     * Returns the user id of the user who created this resource.<p>
     * 
     * @return the user id
     */
    CmsUUID getUserCreated();

    /**
     * Returns the user id of the user who made the last change on this resource.<p>
     *
     * @return the user id of the user who made the last change<p>
     */
    CmsUUID getUserLastModified();

    /**
     * Returns the version number of this historical resource.<p>
     *
     * @return the version number of this historical resource
     */
    int getVersion();

    /** 
     * Returns <code>true</code> if this resource is expired at the given time according to the 
     * information stored in {@link #getDateExpired()}.<p>
     * 
     * @param time the time to check the expiration date against
     * 
     * @return <code>true</code> if this resource is expired at the given time
     *      
     * @see #isReleased(long)
     * @see #isReleasedAndNotExpired(long)
     */
    boolean isExpired(long time);

    /**
     * Returns <code>true</code> if the resource is a file, i.e. can have no sub-resources.<p>
     *
     * @return true if this resource is a file, false otherwise
     */
    boolean isFile();

    /**
     * Returns <code>true</code> if the resource is a folder, i.e. can have sub-resources.<p>
     *
     * @return true if this resource is a folder, false otherwise
     */
    boolean isFolder();

    /**
     * Checks if the resource is internal.<p>
     * 
     * This state is stored as bit 1 in the resource flags.<p>
     * 
     * @return true if the resource is internal, otherwise false
     */
    boolean isInternal();

    /**
     * Checks if the link has to be labeled with a special icon in the explorer view.<p>
     *
     * This state is stored as bit 2 in the resource flags.<p>
     * 
     * @return true if a link to the resource has to be labeled, otherwise false
     */
    boolean isLabeled();

    /** 
     * Returns <code>true</code> if this resource is released at the given time according to the 
     * information stored in {@link #getDateReleased()}.<p>
     * 
     * @param time the time to check the release date against
     * 
     * @return <code>true</code> if this resource is released at the given time
     *      
     * @see #isExpired(long)
     * @see #isReleasedAndNotExpired(long)
     */
    boolean isReleased(long time);

    /** 
     * Returns <code>true</code> if this resource is valid at the given time according to the 
     * information stored in {@link #getDateReleased()} and {@link #getDateExpired()}.<p>
     * 
     * A resource is valid if it is released and not yet expired.<p>
     * 
     * @param time the time to check the release and expiration date against
     * 
     * @return <code>true</code> if this resource is valid at the given time
     *      
     * @see #isExpired(long)
     * @see #isReleased(long)
     */
    boolean isReleasedAndNotExpired(long time);

    /**
     * Returns true if this resource was touched.<p>
     * 
     * @return boolean true if this resource was touched
     */
    boolean isTouched();
}

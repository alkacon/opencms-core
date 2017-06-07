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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.db.CmsResourceState;
import org.opencms.util.CmsStringUtil;

/**
 * Client side implementation of {@link org.opencms.file.CmsResource}.<p>
 * 
 * @since 8.0.0 
 */
public class CmsResource {

    /** Indicates if a resource has been changed in the offline version when compared to the online version. */
    public static final CmsResourceState STATE_CHANGED = CmsResourceState.STATE_CHANGED;

    /** Indicates if a resource has been deleted in the offline version when compared to the online version. */
    public static final CmsResourceState STATE_DELETED = CmsResourceState.STATE_DELETED;

    /**
     * Special state value that indicates the current state must be kept on a resource,
     * this value must never be written to the database.
     */
    public static final CmsResourceState STATE_KEEP = CmsResourceState.STATE_KEEP;

    /** Indicates if a resource is new in the offline version when compared to the online version. */
    public static final CmsResourceState STATE_NEW = CmsResourceState.STATE_NEW;

    /** Indicates if a resource is unchanged in the offline version when compared to the online version. */
    public static final CmsResourceState STATE_UNCHANGED = CmsResourceState.STATE_UNCHANGED;

    /** 
     * Prefix for temporary files in the VFS. 
     * 
     * @see #isTemporaryFileName(String)  
     */
    public static final String TEMP_FILE_PREFIX = "~";

    /**
     * Returns the folder path of the resource with the given name.<p>
     * 
     * If the resource name denotes a folder (that is ends with a "/"), the complete path of the folder 
     * is returned (not the parent folder path).<p>
     * 
     * This is achieved by just cutting of everything behind the last occurrence of a "/" character
     * in the String, no check if performed if the resource exists or not in the VFS, 
     * only resources that end with a "/" are considered to be folders.
     * 
     * Example: Returns <code>/system/def/</code> for the
     * resource <code>/system/def/file.html</code> and 
     * <code>/system/def/</code> for the (folder) resource <code>/system/def/</code>.
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

        if ("/".equals(resource)) {
            return "/";
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

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(resource) || "/".equals(resource)) {
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
     * @param resource the resource to determine the directory level for
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
     * Returns true if the resource name certainly denotes a folder, that is ends with a "/".<p>
     * 
     * @param resource the resource to check
     * @return true if the resource name certainly denotes a folder, that is ends with a "/"
     */
    public static boolean isFolder(String resource) {

        return CmsStringUtil.isNotEmpty(resource) && (resource.charAt(resource.length() - 1) == '/');
    }

    /**
     * Returns <code>true</code> if the given resource path points to a temporary file name.<p>
     * 
     * A resource name is considered a temporary file name if the name of the file 
     * (without parent folders) starts with the prefix char <code>'~'</code> (tilde).
     * Existing parent folder elements are removed from the path before the file name is checked.<p>
     * 
     * @param path the resource path to check
     * 
     * @return <code>true</code> if the given resource name is a temporary file name
     */
    public static boolean isTemporaryFileName(String path) {

        return (path != null) && getName(path).startsWith(TEMP_FILE_PREFIX);
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/I_CmsConstants.java,v $
 * Date   : $Date: 2005/06/22 13:35:39 $
 * Version: $Revision: 1.49 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.main;

/**
 * This interface is a pool for constants in OpenCms.<p>
 * 
 * @author Michael Emmerich 
 * @author Thomas Weckert  
 *
 * @version $Revision: 1.49 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsConstants {

    /**
     * This is an internal resource, it can't be accessed directly.
     */
    int C_ACCESS_INTERNAL_READ = 512;

    /**
     * Flag to indicate that an access control entry is currently deleted.
     */
    int C_ACCESSFLAGS_DELETED = 1;

    /**
     * Flag to signal the pricipal type group.
     */
    int C_ACCESSFLAGS_GROUP = 32;

    /**
     * Flag to indicate that an access control entry should be inherited.
     */
    int C_ACCESSFLAGS_INHERIT = 2;

    /**
     * Flag to indicate that an access control entry was inherited (read only).
     */
    int C_ACCESSFLAGS_INHERITED = 8;

    /**
     * Flag to indicate that an access control entry overwrites inherited entries. 
     */
    int C_ACCESSFLAGS_OVERWRITE = 4;

    /**
     * Flag to signal the principal type user.
     */
    int C_ACCESSFLAGS_USER = 16;

    /** Copy mode for copy resources as new resource. */

    int C_COPY_AS_NEW = 1;

    /** Copy mode for copy resources as sibling. */
    int C_COPY_AS_SIBLING = 2;

    /** Copy mode to preserve siblings during copy. */
    int C_COPY_PRESERVE_SIBLING = 3;

    /** Flag for leaving a date unchanged during a touch operation. */

    long C_DATE_UNCHANGED = -1;

    /** Signals that siblings of this resource should be deleted. */
    int C_DELETE_OPTION_DELETE_SIBLINGS = 1;

    /** Signals that siblings of this resource should not be deleted. */
    int C_DELETE_OPTION_PRESERVE_SIBLINGS = 0;

    /**
     * This flag is set for disabled entrys in the database.
     * (GROUP_FLAGS for example)
     */
    int C_FLAG_DISABLED = 1;

    /**
     * This flag is set for enabled entries in the database.
     * (GROUP_FLAGS for example).
     */
    int C_FLAG_ENABLED = 0;

    /**
     * Flag constant: ProjectCoWorker
     * flag for groups.
     */
    int C_FLAG_GROUP_PROJECTCOWORKER = 4;

    /**
     * Flag constant: Projectmanager
     * flag for groups.
     */
    int C_FLAG_GROUP_PROJECTMANAGER = 2;

    /**
     * Flag constant: Role (for coworkers)
     * flag for groups.
     */
    int C_FLAG_GROUP_ROLE = 8;

    /** HTTP Accept-Charset Header for internal requests used during static export. */
    String C_HEADER_ACCEPT_CHARSET = "Accept-Charset";

    /** HTTP Accept-Language Header for internal requests used during static export. */
    String C_HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    /** HTTP Header "Cache-Control". */
    String C_HEADER_CACHE_CONTROL = "Cache-Control";

    /** HTTP Header "Expires". */
    String C_HEADER_EXPIRES = "Expires";

    /** HTTP Header "If-Modified-Since". */
    String C_HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

    /** HTTP Header "Last-Modified". */
    String C_HEADER_LAST_MODIFIED = "Last-Modified";

    /** HTTP Header for internal requests used during static export. */
    String C_HEADER_OPENCMS_EXPORT = "OpenCms-Export";

    /** HTTP Header "Pragma". */
    String C_HEADER_PRAGMA = "Pragma";

    /** HTTP Header "Server". */
    String C_HEADER_SERVER = "Server";

    /** HTTP Header value "max-age=" (for "Cache-Control"). */
    String C_HEADER_VALUE_MAX_AGE = "max-age=";

    /** HTTP Header value "must-revalidate" (for "Cache-Control"). */
    String C_HEADER_VALUE_MUST_REVALIDATE = "must-revalidate";

    /** HTTP Header value "no-cache" (for "Cache-Control"). */
    String C_HEADER_VALUE_NO_CACHE = "no-cache";

    /** HTTP Header "WWW-Authenticate". */
    String C_HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

    /** Identifier for x-forwarded-for (i.e. proxied) request headers. */
    String C_HEADER_X_FORWARDED_FOR = "x-forwarded-for";

    /** Localhost ip used in fallback cases. */
    String C_IP_LOCALHOST = "127.0.0.1";

    /**
     * The key for the date of the last linkcheck in the linkchecktable.
     */
    String C_LINKCHECKTABLE_DATE = "linkcheckdate";

    /**
     * The maximum length of a resource name (incl. path).
     */
    int C_MAX_LENGTH_RESOURCE_NAME = 240;

    /** The name of the module folder in the package path. */

    String C_MODULE_PATH = "modules/";

    /** Request parameter to force element selection. */
    String C_PARAMETER_ELEMENT = "__element";

    /** Request parameter to force encoding selection. */
    String C_PARAMETER_ENCODING = "__encoding";

    /** Request parameter to force locale selection. */
    String C_PARAMETER_LOCALE = "__locale";

    /**
     * This constant defines the onlineproject. This is the project which
     * is used to show the resources for guestusers
     */
    String C_PROJECT_ONLINE = "Online";

    /**
     * This constant defines the onlineproject. This is the project which
     * is used to show the resources for guestusers
     */
    int C_PROJECT_ONLINE_ID = 1;
    /**
     * This constant defines a project that is invisible.
     * The project is invisible for users. It is needed
     * for creating and editing temporary files
     */
    int C_PROJECT_STATE_INVISIBLE = 3;

    /** 
     * This constant defines a unlocked project.
     * Resources may be changed in this project.
     */
    int C_PROJECT_STATE_UNLOCKED = 0;

    /**
     * This constant defines a normal project-type.
     */
    int C_PROJECT_TYPE_NORMAL = 0;

    /**
     * This constant defines a temporary project-type.
     * The project will be deleted after publishing.
     */
    int C_PROJECT_TYPE_TEMPORARY = 1;

    /** Indicates to ignore the resource path when matching resources. */
    String C_READ_IGNORE_PARENT = null;

    /** Indicates to ignore the resource state when matching resources. */
    int C_READ_IGNORE_STATE = -1;

    /** Indicates to ignore the time value. */
    long C_READ_IGNORE_TIME = 0L;

    /** Indicates to ignore the resource type when matching resources. */
    int C_READ_IGNORE_TYPE = -1;

    /** The resource is marked as internal. */
    int C_RESOURCEFLAG_INTERNAL = 512;

    /** The resource is linked inside a site folder specified in the OpenCms configuration. */
    int C_RESOURCEFLAG_LABELLINK = 2;

    /**
     * The name of the root folder.
     */
    String C_ROOT = "/";

    /**
     * Indicates if a resource has been changed in the offline version when compared to the online version.
     */
    int C_STATE_CHANGED = 1;

    /**
     * Indicates if a resource has been deleted in the offline version when compared to the online version.
     */
    int C_STATE_DELETED = 3;

    /**
     * Special state value that indicates the current state must be kept on a resource.
     * This value must not be written to the database!
     */
    int C_STATE_KEEP = 99;

    /**
     * Indicates if a resource in new in the offline version when compared to the online version.
     */
    int C_STATE_NEW = 2;

    /**
     * Indicates if a resource is unchanged in the offline version when compared to the online version.
     */
    int C_STATE_UNCHANGED = 0;

    /** prefix for temporary files. */
    String C_TEMP_PREFIX = "~";

    /**
     * This id will be returned for resources with no id.
     * (filesystem resources).
     */
    int C_UNKNOWN_ID = -1;

    /**
     * This value will be returned for int's withaout a value.
     */
    int C_UNKNOWN_INT = -1;

    /**
     * This value will be returned for long's withaout a value.
     */
    int C_UNKNOWN_LONG = -1;

    /** The vfs path of the loast and found folder. */
    String C_VFS_LOST_AND_FOUND = "/system/lost-found";

    /**
     * The vfs path of the channel folders.
     */
    // src-modules (com), core, alderaan
    String VFS_FOLDER_CHANNELS = "/channels";

    /**
     * The vfs path of the sites master folder.
     */
    String VFS_FOLDER_SITES = "/sites";

    /**
     * The vfs path of the system folder.
     */
    String VFS_FOLDER_SYSTEM = "/system";

}
/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/core/Attic/I_CmsConstants.java,v $
 * Date   : $Date: 2005/06/22 10:38:32 $
 * Version: $Revision: 1.2 $
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

package com.opencms.core;

import org.opencms.security.CmsPermissionSet;

/**
 * This interface is a pool for constants in OpenCms.<p>
 * 
 * @author Michael Emmerich 
 * @author Thomas Weckert  
 *
 * @version $Revision: 1.2 $
 */
public interface I_CmsConstants {

    /**
     * Group may read this resource.
     */
    int C_ACCESS_GROUP_READ = 8;

    /**
     * Group may view this resource.
     */
    int C_ACCESS_GROUP_VISIBLE = 32;

    /**
     * Group may write this resource.
     */
    int C_ACCESS_GROUP_WRITE = 16;

    /**
     *  Public may read this resource.
     */
    int C_ACCESS_PUBLIC_READ = 64;

    /**
     *  Public may view this resource.
     */
    int C_ACCESS_PUBLIC_VISIBLE = 256;

    /**
     *  Public may write this resource.
     */
    int C_ACCESS_PUBLIC_WRITE = 128;
    /**
     * All may write this resource.
     */
    int C_ACCESS_WRITE = CmsPermissionSet.PERMISSION_WRITE + C_ACCESS_GROUP_WRITE + C_ACCESS_PUBLIC_WRITE;

    /**
     * The default-flags for a new resource.
     */
    int C_ACCESS_DEFAULT_FLAGS = CmsPermissionSet.PERMISSION_READ
        + CmsPermissionSet.PERMISSION_WRITE
        + CmsPermissionSet.PERMISSION_VIEW
        + C_ACCESS_GROUP_WRITE
        + C_ACCESS_GROUP_VISIBLE
        + C_ACCESS_PUBLIC_READ
        + C_ACCESS_PUBLIC_VISIBLE;

    /**
     * Group has full access to this resource.
     */
    int C_ACCESS_GROUP = C_ACCESS_GROUP_WRITE + C_ACCESS_GROUP_VISIBLE;

    /**
     * The name of the error tag prefix in backoffice templates.
     */
    String C_ERRPREFIX = "err";

    /**
     * The name of the error tag separator in backoffice templates.
     */
    String C_ERRSPERATOR = "_";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_MODULEXPORT = "modulexport";
    /**
     * This constant defines a project in a archive.
     * Resources can't be changed in this project. Its state will never
     * go back to the previos one.
     */
    int C_PROJECT_STATE_ARCHIVE = 2;

    /** Identifier for request type http. */
    int C_REQUEST_HTTP = 0;

    /** Identifier for response type http. */
    int C_RESPONSE_HTTP = 0;

    /** Template element name used for the canonical root template. */
    String C_ROOT_TEMPLATE_NAME = "root";

    /**
     * Session key for storing the position in the administration navigation.
     */
    String C_SESSION_ADMIN_POS = "adminposition";

    /**
     * Session key for storing the current charcter encoding to be used in HTTP
     * requests and responses.
     */
    // Encoding project:
    String C_SESSION_CONTENT_ENCODING = "content-encoding";

    /**
     * Session key for storing the files Vector for moduleimport.
     */
    String C_SESSION_MODULE_VECTOR = "modulevector";

    /**
     * Session key for storing a possible error while executing a thread.
     */
    String C_SESSION_THREAD_ERROR = "threaderror";

    /** Start preferences language. */
    String C_START_LOCALE = "StartLanguage";

    /** Start preferences view. */
    String C_START_VIEW = "StartView";

    /**
     * The name of the entry for the id generator to create new channelid's.
     */
    String C_TABLE_CHANNELID = "CORE_CHANNEL_ID";

    /** Suffix for caching of simple pages. */
    String C_XML_CONTROL_FILE_SUFFIX = ".xmlcontrol";

    /** Start preferences lock dialog. */
    String C_START_LOCKDIALOG = "StartLockDialog";

}
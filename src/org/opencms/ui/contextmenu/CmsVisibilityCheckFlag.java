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

package org.opencms.ui.contextmenu;

/**
 * Flag to control which visibility checks should be performed by CmsStandardVisibilityCheck.<p>
 */
public enum CmsVisibilityCheckFlag {
    /** Checks if resource is deleted. */
    deleted,

    /** Check that the resource is in the current project. */
    inproject,

    /** Checks if resource is not deleted. */
    notdeleted,

    /** Check that we are in an Offline project. */
    notonline,

    /** Check that the resource is a file. */
    file,

    /** Checks that the resource is a file which is not unchanged. */
    notunchangedfile,

    /** Checks that the resource is not new. */
    notnew,

    /** Check editor rule. */
    roleeditor,

    /** Check workplace user role. */
    rolewpuser,

    /** Checks if user has publish permissions. */
    publishpermission,

    /** Check that we have write permission. */
    writepermisssion;

}

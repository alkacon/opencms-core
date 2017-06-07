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
    /** Check that we have control permission. */
    controlpermission,

    /** Check that resource is a defaultfile. */
    defaultfile,

    /** Checks if resource is deleted. */
    deleted,

    /** Check that the resource is a file. */
    file,

    /** Check that the resource is a folder. */
    folder,

    /** Checks if an editor is available. */
    haseditor,

    /** Checks if a sourcecode editor is available. */
    hassourcecodeeditor,

    /** Check that the resource is in the current project. */
    inproject,

    /** Checks if resource is locked by current user. */
    mylock,

    /** Checks that lock is not inherited. */
    noinheritedlock,

    /** Checks if resource is not locked by different user. */
    nootherlock,

    /** Checks if resource is not deleted. */
    notdeleted,

    /** Checks that the resource is not in the current project. */
    notinproject,

    /** Checks that the resource is not new. */
    notnew,

    /** Check that we are in an Offline project. */
    notonline,

    /** Checks that the resource is a file which is not unchanged. */
    notunchangedfile,

    /** Checks if resource is locked by different user. */
    otherlock,

    /** Folder with container page default file. */
    pagefolder,

    /** Checks if resource is a pointer. */
    pointer,

    /** Checks if user has publish permissions. */
    publishpermission,

    /** Checks that replace operations are valid for the type. */
    replacable,

    /** Check editor rule. */
    roleeditor,

    /** Check workplace user role. */
    rolewpuser,

    /** Checks if resource is unlocked. */
    unlocked,

    /** Check that we have write permission. */
    writepermisssion,

    /** Checks if resource is xml content. */
    xml;
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/Messages.java,v $
 * Date   : $Date: 2010/05/04 09:40:41 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client;

import org.opencms.gwt.client.i18n.CmsMessages;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0.0
 */
public final class Messages {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_DELETE_TEXT_0 = "GUI_DIALOG_DELETE_TEXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_DELETE_TITLE_0 = "GUI_DIALOG_DELETE_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DISABLED_PARENT_SITEMAP_0 = "GUI_DISABLED_PARENT_SITEMAP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DISABLED_REDO_0 = "GUI_DISABLED_REDO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DISABLED_RESET_0 = "GUI_DISABLED_RESET_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DISABLED_ROOT_ITEM_0 = "GUI_DISABLED_ROOT_ITEM_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DISABLED_SAVE_0 = "GUI_DISABLED_SAVE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DISABLED_UNDO_0 = "GUI_DISABLED_UNDO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_TITLE_0 = "GUI_EDITOR_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HOVERBAR_DELETE_0 = "GUI_HOVERBAR_DELETE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HOVERBAR_EDIT_0 = "GUI_HOVERBAR_EDIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HOVERBAR_GOTO_0 = "GUI_HOVERBAR_GOTO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HOVERBAR_MOVE_0 = "GUI_HOVERBAR_MOVE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HOVERBAR_NEW_0 = "GUI_HOVERBAR_NEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HOVERBAR_PARENT_0 = "GUI_HOVERBAR_PARENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HOVERBAR_SUBSITEMAP_0 = "GUI_HOVERBAR_SUBSITEMAP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOADING_0 = "GUI_LOADING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NAME_0 = "GUI_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NO_EDIT_NOTIFICATION_1 = "GUI_NO_EDIT_NOTIFICATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SITEMAP_0 = "GUI_SITEMAP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TOOLBAR_REDO_0 = "GUI_TOOLBAR_REDO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TOOLBAR_UNDO_0 = "GUI_TOOLBAR_UNDO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_VFS_PATH_0 = "GUI_VFS_PATH_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.sitemap.clientmessages";

    /** Static instance member. */
    private static CmsMessages INSTANCE;

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     * 
     * @return an instance of this localized message accessor
     */
    public static CmsMessages get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsMessages(BUNDLE_NAME);
        }
        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     * 
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}

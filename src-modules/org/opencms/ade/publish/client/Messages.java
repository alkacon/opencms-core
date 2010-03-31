/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/client/Attic/Messages.java,v $
 * Date   : $Date: 2010/03/31 12:15:10 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.publish.client;

import org.opencms.gwt.client.i18n.CmsMessages;

/**
 * This class contains the message keys for the client side of the publish module.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public final class Messages {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NO_TITLE_0 = "GUI_NO_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_CHECKBOXES_REL_RES_0 = "GUI_PUBLISH_CHECKBOXES_REL_RES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_CHECKBOXES_SIBLINGS_0 = "GUI_PUBLISH_CHECKBOXES_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_DIALOG_BACK_0 = "GUI_PUBLISH_DIALOG_BACK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_DIALOG_BROKEN_LINKS_0 = "GUI_PUBLISH_DIALOG_BROKEN_LINKS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_DIALOG_CANCEL_BUTTON_0 = "GUI_PUBLISH_DIALOG_CANCEL_BUTTON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_DIALOG_MY_CHANGES_0 = "GUI_PUBLISH_DIALOG_MY_CHANGES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_DIALOG_NO_RES_0 = "GUI_PUBLISH_DIALOG_NO_RES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_DIALOG_OK_BUTTON_0 = "GUI_PUBLISH_DIALOG_OK_BUTTON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_DIALOG_PROBLEM_1 = "GUI_PUBLISH_DIALOG_PROBLEM_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_DIALOG_PUBLISH_0 = "GUI_PUBLISH_DIALOG_PUBLISH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_DIALOG_TITLE_0 = "GUI_PUBLISH_DIALOG_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_REMOVE_BUTTON_0 = "GUI_PUBLISH_REMOVE_BUTTON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_RESOURCE_STATE_0 = "GUI_PUBLISH_RESOURCE_STATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_RESOURCE_STATE_CHANGED_0 = "GUI_PUBLISH_RESOURCE_STATE_CHANGED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_RESOURCE_STATE_DELETED_0 = "GUI_PUBLISH_RESOURCE_STATE_DELETED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_RESOURCE_STATE_NEW_0 = "GUI_PUBLISH_RESOURCE_STATE_NEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_TOP_PANEL_ALL_BUTTON_0 = "GUI_PUBLISH_TOP_PANEL_ALL_BUTTON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_TOP_PANEL_LEFT_LABEL_0 = "GUI_PUBLISH_TOP_PANEL_LEFT_LABEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_TOP_PANEL_NONE_BUTTON_0 = "GUI_PUBLISH_TOP_PANEL_NONE_BUTTON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_TOP_PANEL_RIGHT_LABEL_0 = "GUI_PUBLISH_TOP_PANEL_RIGHT_LABEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_UNREMOVE_BUTTON_0 = "GUI_PUBLISH_UNREMOVE_BUTTON_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.publish.clientmessages";

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

/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui.rename;

import org.opencms.gwt.client.Messages;

/**
 * Message accessor class for the Rename dialog.<p>
 */
public final class CmsRenameMessages {

    /** Hidden constructor to prevent instantiation. */
    protected CmsRenameMessages() {

        // empty
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageCancel() {

        return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageDialogTitle() {

        return Messages.get().key(Messages.GUI_RENAME_DIALOG_TITLE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageNewNameLabel() {

        return Messages.get().key(Messages.GUI_RENAME_DIALOG_NEW_NAME_LABEL_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageOk() {

        return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageOldNameLabel() {

        return Messages.get().key(Messages.GUI_RENAME_DIALOG_OLD_NAME_LABEL_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageRenameMenuText() {

        return Messages.get().key(Messages.GUI_RENAME_DIALOG_MENU_TEXT_0);
    }

}

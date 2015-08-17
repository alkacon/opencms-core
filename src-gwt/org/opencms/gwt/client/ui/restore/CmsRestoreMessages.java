/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.restore;

import org.opencms.gwt.client.Messages;

/**
 * Message accessor class for the 'Undo changes' dialog.<p>
 */
public final class CmsRestoreMessages {

    /**
     * Hide default constructor.<p>
     */
    private CmsRestoreMessages() {

        // do nothing
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageConfirmRestore() {

        return Messages.get().key(Messages.GUI_RESTORE_CONFIRM_MESSAGE_0);
        //return "Do you really want to undo all changes that were not published?";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageDateModified() {

        return Messages.get().key(Messages.GUI_RESTORE_INFO_OFFLINE_DATE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageDateModifiedOnline() {

        return Messages.get().key(Messages.GUI_RESTORE_INFO_ONLINE_DATE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @param onlinePath the path of the resource in the online project
     * @param offlinePath the path of the resource in the offline path
     *
     * @return the message text
     */
    public static String messageMoved(String onlinePath, String offlinePath) {

        return Messages.get().key(Messages.GUI_RESTORE_RESOURCE_MOVED_2, onlinePath, offlinePath);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageRestoreCancel() {

        return Messages.get().key(Messages.GUI_CANCEL_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageRestoreDialogTitle() {

        return Messages.get().key(Messages.GUI_RESTORE_DIALOG_TITLE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageRestoreOk() {

        return Messages.get().key(Messages.GUI_OK_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageUndoMove() {

        return Messages.get().key(Messages.GUI_RESTORE_CHECKBOX_UNDO_MOVE_0);
    }
}

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

package org.opencms.gwt.client.ui.history;

import org.opencms.gwt.client.Messages;

/**
 * Message provider for the history dialog.<p>
 */
public class CmsHistoryMessages {

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String captionConfirm() {

        return Messages.get().key(Messages.GUI_CONFIRM_REVERT_TITLE_0); // Revert
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String columnModificationDate() {

        return Messages.get().key(Messages.GUI_HISTORY_MODIFICATION_DATE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String columnPath() {

        return Messages.get().key(Messages.GUI_HISTORY_COLUMN_PATH_0); // Path
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String columnPreview() {

        return "";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String columnPublishDate() {

        return Messages.get().key(Messages.GUI_HISTORY_PUBLISH_DATE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String columnReplace() {

        return "";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String columnSize() {

        return Messages.get().key(Messages.GUI_HISTORY_COLUMN_SIZE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String columnUserLastModified() {

        return Messages.get().key(Messages.GUI_HISTORY_MODIFICATION_USER_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String columnVersion() {

        return "V";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String dialogTitle() {

        return Messages.get().key(Messages.GUI_HISTORY_DIALOG_TITLE_0); // History
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String historyDialogText() {

        return Messages.get().key(Messages.GUI_HISTORY_DIALOG_TEXT_0);

    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String noHistoryVersions() {

        return Messages.get().key(Messages.GUI_HISTORY_NO_VERSIONS_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String offline() {

        return Messages.get().key(Messages.GUI_HISTORY_OFFLINE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String online() {

        return Messages.get().key(Messages.GUI_HISTORY_OFFLINE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String textConfirm() {

        return Messages.get().key(Messages.GUI_HISTORY_REVERT_CONFIRMATION_0);

        // return "Do you really want to revert this content?";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String titlePreview() {

        return Messages.get().key(Messages.GUI_HISTORY_PREVIEW_HELP_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String titleRevert() {

        return Messages.get().key(Messages.GUI_HISTORY_REVERT_HELP_0);
    }

}

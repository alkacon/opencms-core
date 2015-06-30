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

package org.opencms.ade.sitemap.client.alias;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.gwt.shared.alias.CmsAliasImportResult;

/**
 * Message accessor collection for the alias editor.<p>
 */
public final class CmsAliasMessages {

    /**
     * Hide constructor.<p>
     */
    private CmsAliasMessages() {

        // do nothing
    }

    /**
     * Message accessor.<p>
     *
     * @param result an alias import result
     *
     * @return the message text
     */
    public static String messageAliasImportLine(CmsAliasImportResult result) {

        if ((result.getAliasPath() == null) || (result.getTargetPath() == null)) {
            return result.getLine();
        } else {
            return Messages.get().key(
                Messages.GUI_ALIASES_IMPORT_LINE_2,
                result.getAliasPath(),
                result.getTargetPath());
        }
    }

    /**
     * Message accessor.<p>
     *
     * @param lockOwner the lock owner
     *
     * @return the message text
     */
    public static String messageAliasTableLocked(String lockOwner) {

        return Messages.get().key(Messages.GUI_ALIASES_TABLE_LOCKED_1, lockOwner);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageAliasTableLockedTitle() {

        return Messages.get().key(Messages.GUI_ALIASES_TABLE_LOCKED_TITLE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageButtonCancel() {

        return Messages.get().key(Messages.GUI_ALIASES_BUTTON_CANCEL_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageButtonDelete() {

        return Messages.get().key(Messages.GUI_ALIASES_BUTTON_DELETE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageButtonDownload() {

        return Messages.get().key(Messages.GUI_ALIASES_BUTTON_DOWNLOAD_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageButtonSave() {

        return Messages.get().key(Messages.GUI_ALIASES_BUTTON_SAVE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageButtonSelectFile() {

        return Messages.get().key(Messages.GUI_ALIASES_BUTTON_SELECT_FILE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageButtonSubmit() {

        return Messages.get().key(Messages.GUI_ALIASES_BUTTON_IMPORT_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageButtonUpload() {

        return Messages.get().key(Messages.GUI_ALIASES_BUTTON_UPLOAD_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageColumnAlias() {

        return Messages.get().key(Messages.GUI_ALIASES_COLUMN_ALIAS_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageColumnError() {

        return Messages.get().key(Messages.GUI_ALIASES_COLUMN_ERROR_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageColumnMode() {

        return Messages.get().key(Messages.GUI_ALIASES_COLUMN_MODE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageColumnPath() {

        return Messages.get().key(Messages.GUI_ALIASES_COLUMN_PATH_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageColumnPattern() {

        return Messages.get().key(Messages.GUI_ALIASES_COLUMN_PATTERN_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageColumnReplacement() {

        return Messages.get().key(Messages.GUI_ALIASES_COLUMN_REPLACEMENT_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageColumnSelect() {

        return Messages.get().key(Messages.GUI_ALIASES_COLUMN_SELECT_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageContextMenuEditAliases() {

        return Messages.get().key(Messages.GUI_ALIASES_CONTEXT_MENU_EDIT_0);

        //return "Edit aliases";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageEditAliasesNotPermitted() {

        return Messages.get().key(Messages.GUI_ALIAS_EDITING_NOT_PERMITTED_0);

    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageImportSeparator() {

        return Messages.get().key(Messages.GUI_ALIAS_FIELD_SEPARATOR_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageLabelRegex() {

        return messageColumnPattern() + ":";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageLabelReplacement() {

        return messageColumnReplacement() + ":";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageLegendNewRewrite() {

        return Messages.get().key(Messages.GUI_ALIASES_LEGEND_NEW_REWRITE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageLegendRewriteTable() {

        return Messages.get().key(Messages.GUI_ALIASES_LEGEND_REWRITE_TABLE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageNewAliasActionLabel() {

        return messageColumnMode() + ":";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageNewAliasLabel() {

        return messageColumnAlias() + ":";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageNewAliasTargetLabel() {

        return messageColumnPath() + ":";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageNewFieldsetLegend() {

        return Messages.get().key(Messages.GUI_ALIASES_NEW_BOX_LEGEND_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messagePage() {

        return Messages.get().key(Messages.GUI_ALIASES_MODE_PAGE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messagePassthrough() {

        return Messages.get().key(Messages.GUI_ALIASES_PASSTHROUGH_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messagePermanentRedirect() {

        return Messages.get().key(Messages.GUI_ALIASES_MODE_PERMANENT_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageRedirect() {

        return Messages.get().key(Messages.GUI_ALIASES_MODE_REDIRECT_0);
    }

    /**
     * Message accessor.<p>
     *
     * @param newRowCount the alias row count
     *
     * @return the message text
     */
    public static String messageRowCount(int newRowCount) {

        return Messages.get().key(Messages.GUI_ALIASES_COUNT_1, "" + newRowCount);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messagesEmptyImportResult() {

        return Messages.get().key(Messages.GUI_ALIASES_IMPORT_EMPTY_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageStatusError() {

        return Messages.get().key(Messages.GUI_ALIASES_STATUS_ERROR_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageStatusOk() {

        return Messages.get().key(Messages.GUI_ALIASES_STATUS_OK_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageTableFieldsetLegend() {

        return Messages.get().key(Messages.GUI_ALIASES_TABLE_BOX_LEGEND_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageTitleAliasEditor() {

        return Messages.get().key(Messages.GUI_ALIASES_TITLE_EDITOR_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageTitleImport() {

        return Messages.get().key(Messages.GUI_ALIASES_TITLE_IMPORT_0);
    }

}

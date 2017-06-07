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

package org.opencms.ui.dialogs.history;

import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.gwt.shared.CmsHistoryVersion;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.util.table.CmsTableUtil;
import org.opencms.ui.util.table.Column;

import java.util.Date;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;

/**
 * Represents a row of the file history table.<p>
 */
public class CmsHistoryRow {

    /** The history resource bean. */
    private CmsHistoryResourceBean m_bean;

    /** The V1 check box. */
    private CheckBox m_checkbox1 = new CheckBox();

    /** The V2 check box. */
    private CheckBox m_checkbox2 = new CheckBox();

    /** The Preview button. */
    private Button m_previewButton = CmsTableUtil.createIconButton(
        FontAwesome.SEARCH,
        CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_BUTTON_PREVIEW_0));

    /** The Restore button. */
    private Button m_restoreButton = CmsTableUtil.createIconButton(
        FontAwesome.CLOCK_O,
        CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_BUTTON_RESTORE_0));

    /**
     * Creates a new instance.<p>
     *
     * @param bean the history resource bean
     */
    public CmsHistoryRow(CmsHistoryResourceBean bean) {
        m_bean = bean;
    }

    /**
     * Formats the file version for display.<p>
     *
     * @param bean the history resource bean
     *
     * @return the formatted version
     */
    public static String formatVersion(CmsHistoryResourceBean bean) {

        CmsHistoryVersion hVersion = bean.getVersion();
        Integer v = hVersion.getVersionNumber();
        String result = "" + (v != null ? v.toString() : "-");
        String suffix = "";
        if (hVersion.isOnline()) {
            suffix = " (Online)";
        } else if (hVersion.isOffline()) {
            suffix = " (Offline)";
        }
        result += suffix;
        return result;

    }

    /**
     * Gets the V1 check box.<p>
     *
     * @return the V1 check box
     */
    @Column(header = "V1", order = 90)
    public CheckBox getCheckBoxV1() {

        return m_checkbox1;
    }

    /**
     * Gets the V2 check box.<p>
     *
     * @return the V2 check box
     */
    @Column(header = "V2", order = 100)
    public CheckBox getCheckBoxV2() {

        return m_checkbox2;
    }

    /**
     * Gets the modification date.<p>
     *
     * @return the modification date
     */
    @Column(header = Messages.GUI_HISTORY_DIALOG_COL_DATE_LASTMODIFIED_0, order = 60)
    public Date getModificationDate() {

        return new Date(m_bean.getModificationDate().getDate());
    }

    /**
     * Gets the path.<p>
     *
     * @return the path
     */
    @Column(header = Messages.GUI_HISTORY_DIALOG_COL_PATH_0, order = 40, expandRatio = 1.0f, view = "wide")
    public String getPath() {

        String rootPath = m_bean.getRootPath();
        CmsObject cms = A_CmsUI.getCmsObject();
        String result = cms.getRequestContext().removeSiteRoot(rootPath);
        return result;
    }

    /**
     * Gets the preview button.<p>
     *
     * @return the preview button
     */
    @Column(header = Messages.GUI_HISTORY_DIALOG_BUTTON_PREVIEW_0, order = 7)
    public Button getPreviewButton() {

        return m_previewButton;
    }

    /**
     * Gets the publish date.<p>
     *
     * @return the publish date
     */
    @Column(header = Messages.GUI_HISTORY_DIALOG_COL_DATE_PUBLISHED_0, order = 50)
    public Date getPublishDate() {

        if (m_bean.getPublishDate() == null) {
            return null;
        }
        return new Date(m_bean.getPublishDate().getDate());
    }

    /**
     * Gets the restore button.<p>
     *
     * @return the restore button
     */
    @Column(header = Messages.GUI_HISTORY_DIALOG_BUTTON_RESTORE_0, order = 6)
    public Button getRestoreButton() {

        if (m_bean.getVersion().getVersionNumber() == null) {
            return null;
        }
        return m_restoreButton;

    }

    /**
     * Gets the last modification user.<p>
     *
     * @return the last modification user
     */
    @Column(header = org.opencms.workplace.commons.Messages.GUI_LABEL_USER_LAST_MODIFIED_0, order = 70)
    public String getUserLastModified() {

        return m_bean.getUserLastModified();
    }

    /**
     * Gets the file version.<p>
     *
     * @return the file version
     */
    @Column(header = org.opencms.workplace.commons.Messages.GUI_LABEL_VERSION_0, order = 30)
    public String getVersion() {

        return formatVersion(m_bean);
    }

    /**
     * Gets the file size.<p>
     *
     * @return the file size
     */
    @Column(header = org.opencms.workplace.commons.Messages.GUI_LABEL_SIZE_0, order = 80)
    Integer getSize() {

        return Integer.valueOf(m_bean.getSize());
    }

}

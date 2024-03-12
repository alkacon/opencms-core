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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.gwt.CmsVfsService;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Dialog used to change resource modification times.<p>
 */
public class CmsUndoDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUndoDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The dialog context. */
    protected I_CmsDialogContext m_context;

    /** The Cancel button. */
    private Button m_cancelButton;

    /** Check box to enable/disable modification of children. */
    private OptionGroup m_modifySubresourcesField;

    /** The OK  button. */
    private Button m_okButton;

    /** Label with info text. */
    private Label m_infoText;

    /** Label for displaying last modification date / user. */
    private Label m_modifiedText;

    /** The date selection field. */
    private CheckBox m_undoMoveField;

    /** The lock warning to display if sub-resources are locked. */
    private VerticalLayout m_lockWarning;

    /** The warning icon */
    private Label m_icon;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     * @param hasBlockingLocksOnSubResources flag, indicating if there are blocking locks on sub-resources
     */
    public CmsUndoDialog(I_CmsDialogContext context, boolean hasBlockingLocksOnSubResources) {

        m_context = context;
        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);
        m_infoText.setValue(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_UNDO_CONFIRMATION_0));
        boolean hasFolders = false;
        boolean hasMoved = false;
        if (context.getResources().size() == 1) {
            CmsResource singleRes = context.getResources().get(0);
            CmsResourceUtil resUtil = new CmsResourceUtil(context.getCms(), singleRes);
            String fileName = CmsResource.getName(singleRes.getRootPath());
            String date = CmsVfsService.formatDateTime(context.getCms(), singleRes.getDateLastModified());
            String user = resUtil.getUserLastModified();
            String key = org.opencms.workplace.commons.Messages.GUI_UNDO_LASTMODIFIED_INFO_3;
            String message = CmsVaadinUtils.getMessageText(key, fileName, date, user);
            m_modifiedText.setVisible(true);
            m_modifiedText.setValue(message);
        }
        for (CmsResource resource : context.getResources()) {
            if (resource.isFolder()) {
                hasFolders = true;
                break;
            } else {
                try {
                    CmsObject cms = OpenCms.initCmsObject(context.getCms());
                    cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                    CmsResource onlineres = cms.readResource(resource.getStructureId());
                    hasMoved |= !onlineres.getRootPath().equals(resource.getRootPath());
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }

        boolean multi = context.getResources().size() > 1;
        String undoMessage = getUndoMessage(multi, hasFolders);
        m_infoText.setValue(undoMessage);
        m_modifySubresourcesField.setVisible(hasFolders);
        m_modifySubresourcesField.addItem("false");
        m_modifySubresourcesField.setItemCaption("false", getNonRecursiveMessage(multi, hasFolders));
        m_modifySubresourcesField.addItem("true");
        m_modifySubresourcesField.setItemCaption("true", getRecursiveMessage(multi, hasFolders));
        m_modifySubresourcesField.setValue("false");

        m_undoMoveField.setVisible(hasFolders || hasMoved);
        if (hasBlockingLocksOnSubResources) {
            m_modifySubresourcesField.setItemEnabled("true", false);
            m_undoMoveField.setEnabled(false);
            m_icon.setContentMode(ContentMode.HTML);
            m_icon.setValue(FontOpenCms.WARNING.getHtml());
            m_lockWarning.setVisible(true);
        }

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }

        });

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                undo();

            }
        });
        displayResourceInfo(m_context.getResources());

        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsUndoDialog.this.cancel();
            }

            @Override
            protected void ok() {

                undo();
            }
        });
    }

    /**
     * Undoes the changes.<p>
     */
    protected void undo() {

        try {
            boolean recursive = Boolean.parseBoolean(m_modifySubresourcesField.getValue().toString());
            boolean undoMove = m_undoMoveField.getValue().booleanValue();
            CmsObject cms = m_context.getCms();
            Set<CmsUUID> updateResources = new HashSet<CmsUUID>();
            for (CmsResource resource : m_context.getResources()) {
                updateResources.add(resource.getStructureId());
                if (undoMove) {
                    // in case a move is undone, add the former parent folder
                    updateResources.add(cms.readParentFolder(resource.getStructureId()).getStructureId());
                }
                CmsLockActionRecord actionRecord = null;
                try {
                    actionRecord = CmsLockUtil.ensureLock(
                        m_context.getCms(),
                        resource,
                        !(resource.isFile() || recursive || undoMove));
                    CmsResourceUndoMode mode = CmsResourceUndoMode.getUndoMode(undoMove, recursive);
                    cms.undoChanges(cms.getSitePath(resource), mode);
                    if (undoMove) {
                        // in case a move is undone, also add the new parent folder
                        updateResources.add(cms.readParentFolder(resource.getStructureId()).getStructureId());
                    }
                } finally {
                    if ((actionRecord != null) && (actionRecord.getChange() == LockChange.locked)) {
                        try {
                            m_context.getCms().unlockResource(cms.readResource(resource.getStructureId()));
                        } catch (CmsLockException e) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                    }

                }

            }
            m_context.finish(updateResources);
        } catch (Exception e) {
            m_context.error(e);
        }

    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_context.finish(new ArrayList<CmsUUID>());
    }

    /**
     * Gets the message for the non-recursive modification option.<p>
     *
     * @param multi true if selection contains multiple resources
     * @param hasFolder true if selection contains a folder
     *
     * @return the message text
     */
    String getNonRecursiveMessage(boolean multi, boolean hasFolder) {

        return CmsVaadinUtils.getMessageText(key("GUI_UNDO_NONRECURSIVE_", multi, hasFolder));

    }

    /**
     * Gets the message for the recursive modification option.<p>
     *
     * @param multi true if selection contains multiple resources
     * @param hasFolder true if selection contains a folder
     *
     * @return the message text
     */
    String getRecursiveMessage(boolean multi, boolean hasFolder) {

        return CmsVaadinUtils.getMessageText(key("GUI_UNDO_RECURSIVE_", multi, hasFolder));

    }

    /**
     * Gets the undo message.<p>
     *
     * @param multi true if selection contains multiple resources
     * @param hasFolder true if selection contains a folder
     *
     * @return the message text
     */
    String getUndoMessage(boolean multi, boolean hasFolder) {

        return CmsVaadinUtils.getMessageText(key("GUI_UNDO_", multi, hasFolder));
    }

    /**
     * Generates message key for a given combination of prefix, multi/single file , and folder/ no folder.<p>
     *
     * @param prefix the key prefix
     * @param multi true if we have a multi-resource selection case
     * @param hasFolder true if the selection contains a foldr
     *
     * @return the message key for the given input parameters
     */
    private String key(String prefix, boolean multi, boolean hasFolder) {

        return prefix + (multi ? "MULTI" : "SINGLE") + "_" + (hasFolder ? "FOLDER" : "FILE") + "_0";
    }

}

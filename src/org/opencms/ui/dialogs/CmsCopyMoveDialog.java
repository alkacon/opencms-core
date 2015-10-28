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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsVfsException;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.fileselect.CmsResourceSelectField;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.commons.Messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.OptionGroup;

/**
 * The copy move dialog.<p>
 */
public class CmsCopyMoveDialog extends CmsBasicDialog {

    /** The copy/move actions. */
    public static enum Action {

        /** Copy resources as new. */
        copy_all,
        /** Create siblings. */
        copy_sibling_all,
        /** Copy and preserve siblings. */
        copy_sibling_mixed,
        /** Move resources. */
        move;
    }

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsCopyMoveDialog.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The action radio buttons. */
    private OptionGroup m_actionRadio;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The cms context. */
    private CmsObject m_cms;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The OK button. */
    private Button m_okButton;

    /** The overwrite existing resources checkbox. */
    private CheckBox m_overwriteExisting;

    /** The root cms context. */
    private CmsObject m_rootCms;

    /** The target select field. */
    private CmsResourceSelectField m_targetFolder;

    /** The resources to update after dialog close. */
    private Set<CmsUUID> m_updateResources;

    /**
     * Constructor.<p>
     *
     * @param context the dialog context
     */
    public CmsCopyMoveDialog(final I_CmsDialogContext context) {
        m_updateResources = new HashSet<CmsUUID>();
        m_context = context;
        displayResourceInfo(context.getResources());
        FormLayout form = initForm();
        setContent(form);
        m_okButton = new Button(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });
        addButton(m_okButton);
        m_cancelButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }
        });
        addButton(m_cancelButton);

    }

    /**
     * Preselects the target folder.<p>
     *
     * @param structureId the target structure id
     *
     * @throws CmsException in case the target can not be read or is not a folder
     */
    public void setTargetFolder(CmsUUID structureId) throws CmsException {

        CmsObject cms = A_CmsUI.getCmsObject();
        CmsResource res = cms.readResource(structureId);
        if (res.isFolder()) {
            m_targetFolder.setValue(cms.getSitePath(res));
        } else {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_COPY_MULTI_TARGET_NOFOLDER_1, cms.getSitePath(res)));
        }
    }

    /**
     * Preselects the target folder.<p>
     *
     * @param sitePath the folder site path
     */
    public void setTargetFolder(String sitePath) {

        m_targetFolder.setValue(sitePath);
    }

    /**
     * Performs the single resource operation.<p>
     *
     * @param source the source
     * @param target the target
     * @param action the action
     * @param overwrite if existing resources should be overwritten
     *
     * @throws CmsException in case the operation fails
     */
    protected void performSingleOperation(CmsResource source, CmsResource target, Action action, boolean overwrite)
    throws CmsException {

        // add new parent and source to the update resources
        m_updateResources.add(target.getStructureId());
        m_updateResources.add(source.getStructureId());
        // calculate the target name
        String finalTarget = target.getRootPath();
        if (finalTarget.equals(source.getRootPath()) || finalTarget.startsWith(source.getRootPath())) {
            throw new CmsVfsException(Messages.get().container(Messages.ERR_COPY_ONTO_ITSELF_1, finalTarget));
        }

        if (!finalTarget.endsWith("/")) {
            finalTarget += "/";
        }
        if (finalTarget.equals(CmsResource.getParentFolder(source.getRootPath()))) {
            finalTarget += OpenCms.getResourceManager().getNameGenerator().getCopyFileName(
                getRootCms(),
                finalTarget,
                source.getName());
        } else {
            finalTarget += source.getName();
        }

        // delete existing target resource if selected or confirmed by the user
        if (overwrite && getRootCms().existsResource(finalTarget)) {
            CmsLockUtil.ensureLock(getRootCms(), getRootCms().readResource(finalTarget));
            getRootCms().deleteResource(finalTarget, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
        // copy the resource
        if (action == Action.move) {
            // add former parent to the update resources
            CmsResource parent = getRootCms().readParentFolder(source.getStructureId());
            m_updateResources.add(parent.getStructureId());
            CmsLockActionRecord lockRecord = CmsLockUtil.ensureLock(getRootCms(), source);
            getRootCms().moveResource(source.getRootPath(), finalTarget);
            if (lockRecord.getChange() == CmsLockActionRecord.LockChange.locked) {
                getRootCms().unlockResource(finalTarget);
            }
        } else {
            CmsResourceCopyMode copyMode = null;
            switch ((Action)m_actionRadio.getValue()) {
                case copy_all:
                    copyMode = CmsResource.COPY_AS_NEW;
                    break;
                case copy_sibling_all:
                    copyMode = CmsResource.COPY_AS_SIBLING;
                    break;
                case copy_sibling_mixed:
                default:
                    copyMode = CmsResource.COPY_PRESERVE_SIBLING;
            }

            getRootCms().copyResource(source.getRootPath(), finalTarget, copyMode);
            getRootCms().unlockResource(finalTarget);
            CmsResource copyResource = getRootCms().readResource(finalTarget);
            m_updateResources.add(copyResource.getStructureId());
        }
    }

    /**
     * Cancels the dialog action.<p>
     */
    void cancel() {

        m_context.finish(Collections.<CmsUUID> emptyList());
    }

    /**
     * Submits the dialog action.<p>
     */
    void submit() {

        try {
            CmsResource targetFolder = getTargetFolder();
            if (targetFolder.isFile()) {
                throw new CmsVfsException(
                    Messages.get().container(Messages.ERR_COPY_MULTI_TARGET_NOFOLDER_1, m_targetFolder.getValue()));
            }
            boolean overwrite = isOverwriteExisting();
            Map<CmsResource, CmsException> errors = new HashMap<CmsResource, CmsException>();
            for (CmsResource source : m_context.getResources()) {
                try {
                    performSingleOperation(source, targetFolder, (Action)m_actionRadio.getValue(), overwrite);
                } catch (CmsException e) {
                    errors.put(source, e);
                    LOG.error(
                        "Error while executing "
                            + m_actionRadio.getValue().toString()
                            + " on resource "
                            + source.getRootPath(),
                        e);
                }
            }

            if (!errors.isEmpty()) {
                //TODO: handle errors
                m_context.finish(m_updateResources);
            } else {
                m_context.finish(m_updateResources);
            }
        } catch (CmsException e) {
            m_context.error(e);
        }
    }

    /**
     * Returns the cms context.<p>
     *
     * @return the cms context
     */
    private CmsObject getCms() {

        if (m_cms == null) {
            m_cms = A_CmsUI.getCmsObject();
        }
        return m_cms;
    }

    /**
     * Returns the root cms context.<p>
     *
     * @return the root cms context
     *
     * @throws CmsException in case initializing the context fails
     */
    private CmsObject getRootCms() throws CmsException {

        if (m_rootCms == null) {
            m_rootCms = OpenCms.initCmsObject(getCms());
            m_rootCms.getRequestContext().setSiteRoot("/");
        }
        return m_rootCms;
    }

    /**
     * Returns the target folder resource.<p>
     *
     * @return the target folder resource
     *
     * @throws CmsException in case reading the folder fails
     */
    private CmsResource getTargetFolder() throws CmsException {

        String target = m_targetFolder.getValue();
        CmsResource targetFolder = null;
        // check if a site root was added to the target name
        if (OpenCms.getSiteManager().getSiteRoot(target) != null) {
            targetFolder = getRootCms().readResource(target);
        } else {
            targetFolder = getCms().readResource(target);
        }

        return targetFolder;
    }

    /**
     * Initializes the form fields.<p>
     *
     * @return the form component
     */
    private FormLayout initForm() {

        FormLayout form = new FormLayout();
        form.setWidth("100%");
        m_targetFolder = new CmsResourceSelectField();
        m_targetFolder.setCaption(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_MOVE_TARGET_FOLDER_0));
        m_targetFolder.setWidth("100%");
        form.addComponent(m_targetFolder);
        m_actionRadio = new OptionGroup();
        if (m_context.getResources().size() == 1) {
            if (m_context.getResources().get(0).isFile()) {
                m_actionRadio.addItem(Action.copy_all);
                m_actionRadio.setItemCaption(
                    Action.copy_all,
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_AS_NEW_0));
                m_actionRadio.addItem(Action.copy_sibling_all);
                m_actionRadio.setItemCaption(
                    Action.copy_sibling_all,
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_CREATE_SIBLING_0));
                m_actionRadio.addItem(Action.move);
                m_actionRadio.setItemCaption(
                    Action.move,
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_MOVE_MOVE_FILE_0));

                m_actionRadio.setValue(Action.copy_all);
            } else {
                m_actionRadio.addItem(Action.copy_all);
                m_actionRadio.setItemCaption(
                    Action.copy_all,
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_ALL_0));
                m_actionRadio.addItem(Action.copy_sibling_mixed);
                m_actionRadio.setItemCaption(
                    Action.copy_sibling_mixed,
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_ALL_NO_SIBLINGS_0));
                m_actionRadio.addItem(Action.copy_sibling_all);
                m_actionRadio.setItemCaption(
                    Action.copy_sibling_all,
                    CmsVaadinUtils.getMessageText(
                        org.opencms.workplace.commons.Messages.GUI_COPY_MULTI_CREATE_SIBLINGS_0));
                m_actionRadio.addItem(Action.move);
                m_actionRadio.setItemCaption(
                    Action.move,
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_MOVE_MOVE_FOLDER_0));

                m_actionRadio.setValue(Action.copy_sibling_mixed);
            }
        } else {
            m_actionRadio.addItem(Action.copy_all);
            m_actionRadio.setItemCaption(
                Action.copy_all,
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_ALL_0));
            m_actionRadio.addItem(Action.copy_sibling_mixed);
            m_actionRadio.setItemCaption(
                Action.copy_sibling_mixed,
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_ALL_NO_SIBLINGS_0));
            m_actionRadio.addItem(Action.copy_sibling_all);
            m_actionRadio.setItemCaption(
                Action.copy_sibling_all,
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_MULTI_CREATE_SIBLINGS_0));
            m_actionRadio.addItem(Action.move);
            m_actionRadio.setItemCaption(
                Action.move,
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_MOVE_MOVE_RESOURCES_0));

            m_actionRadio.setValue(Action.copy_sibling_mixed);

            m_overwriteExisting = new CheckBox(
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_MULTI_OVERWRITE_0));
            m_overwriteExisting.setValue(Boolean.FALSE);
        }

        form.addComponent(m_actionRadio);
        if (m_overwriteExisting != null) {
            form.addComponent(m_overwriteExisting);
        }

        return form;
    }

    /**
     * Checks the overwrite existing setting.<p>
     *
     * @return <code>true</code> if overwrite existing is set
     */
    private boolean isOverwriteExisting() {

        return (m_overwriteExisting != null) && m_overwriteExisting.getValue().booleanValue();
    }

}

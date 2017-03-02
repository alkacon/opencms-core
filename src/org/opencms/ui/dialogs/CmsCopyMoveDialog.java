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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.types.CmsResourceTypeFolderSubSitemap;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.tools.CmsContainerPageCopier;
import org.opencms.i18n.tools.CmsContainerPageCopier.NoCustomReplacementException;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComboBox.ItemStyleGenerator;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * The copy move dialog.<p>
 */
public class CmsCopyMoveDialog extends CmsBasicDialog {

    /** The copy/move actions. */
    public static enum Action {

        /** Copy container page automatic mode. */
        container_page_automatic,
        /** Copy container page including referenced elements. */
        container_page_copy,
        /** Copy container page reuse referenced elements. */
        container_page_reuse,
        /** Copy resources as new. */
        copy_all,
        /** Create siblings. */
        copy_sibling_all,
        /** Copy and preserve siblings. */
        copy_sibling_mixed,
        /** Move resources. */
        move,
        /** Copy sub sitemap, adjust internal links. */
        sub_sitemap;
    }

    /** The dialog mode. */
    public static enum DialogMode {
        /** Allow copy only. */
        copy,
        /** Allow copy and move. */
        copy_and_move,
        /** Allow move only. */
        move
    }

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsCopyMoveDialog.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The default actions. */
    List<Action> m_defaultActions;

    /** The action radio buttons. */
    private ComboBox m_actionCombo;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The cms context. */
    private CmsObject m_cms;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** Flag indicating the move option is allowed. */
    private DialogMode m_dialogMode;

    /** Indicates the copy folder has a default file of the type container page. */
    private boolean m_hasContainerPageDefaultFile;

    /** The OK button. */
    private Button m_okButton;

    /** The overwrite existing resources checkbox. */
    private CheckBox m_overwriteExisting;

    /** The root cms context. */
    private CmsObject m_rootCms;

    /** The target path select field. */
    private CmsPathSelectField m_targetPath;

    /** The resources to update after dialog close. */
    private Set<CmsUUID> m_updateResources;

    /**
     * Constructor.<p>
     *
     * @param context the dialog context
     * @param mode the dialog mode
     */
    public CmsCopyMoveDialog(final I_CmsDialogContext context, DialogMode mode) {
        m_dialogMode = mode;
        m_updateResources = new HashSet<CmsUUID>();
        m_context = context;
        m_defaultActions = new ArrayList<Action>();
        displayResourceInfo(context.getResources());
        FormLayout form = initForm();
        setContent(form);
        updateDefaultActions(null);
        m_okButton = new Button(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit(false);
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
        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsCopyMoveDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit(false);
            }
        });
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
        setTargetForlder(res);
    }

    /**
     * Preselects the target folder.<p>
     *
     * @param resource the target resource
     */
    public void setTargetForlder(CmsResource resource) {

        if (resource.isFolder()) {
            m_targetPath.setValue(getCms().getSitePath(resource));
            updateDefaultActions(resource.getRootPath());
        } else {
            throw new CmsIllegalArgumentException(
                Messages.get().container(
                    org.opencms.workplace.commons.Messages.ERR_COPY_MULTI_TARGET_NOFOLDER_1,
                    A_CmsUI.getCmsObject().getSitePath(resource)));
        }
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

        String name;
        String folderRootPath = target.getRootPath();
        if (!folderRootPath.endsWith("/")) {
            folderRootPath += "/";
        }
        if (folderRootPath.equals(CmsResource.getParentFolder(source.getRootPath()))) {
            name = OpenCms.getResourceManager().getNameGenerator().getCopyFileName(
                getRootCms(),
                folderRootPath,
                source.getName());
        } else {
            name = source.getName();
        }
        performSingleOperation(source, target, name, action, overwrite);
    }

    /**
     * Performs the single resource operation.<p>
     *
     * @param source the source
     * @param target the target folder
     * @param name the target resource name
     * @param action the action
     * @param overwrite if existing resources should be overwritten
     *
     * @throws CmsException in case the operation fails
     */
    protected void performSingleOperation(
        CmsResource source,
        CmsResource target,
        String name,
        Action action,
        boolean overwrite)
    throws CmsException {

        // add new parent and source to the update resources
        m_updateResources.add(target.getStructureId());
        m_updateResources.add(source.getStructureId());

        String finalTarget = target.getRootPath();
        if (finalTarget.equals(source.getRootPath()) || finalTarget.startsWith(source.getRootPath())) {
            throw new CmsVfsException(
                Messages.get().container(org.opencms.workplace.commons.Messages.ERR_COPY_ONTO_ITSELF_1, finalTarget));
        }
        finalTarget = CmsStringUtil.joinPaths(finalTarget, name);
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
        } else if ((action == Action.container_page_automatic)
            || (action == Action.container_page_copy)
            || (action == Action.container_page_reuse)) {
            CmsContainerPageCopier copier = new CmsContainerPageCopier(m_context.getCms());
            try {

                CmsContainerPageCopier.CopyMode mode = action == Action.container_page_automatic
                ? CmsContainerPageCopier.CopyMode.automatic
                : (action == Action.container_page_copy
                ? CmsContainerPageCopier.CopyMode.smartCopyAndChangeLocale
                : CmsContainerPageCopier.CopyMode.reuse);
                copier.setCopyMode(mode);
                copier.run(m_context.getResources().get(0), target, name);
                m_context.finish(
                    Arrays.asList(
                        copier.getTargetFolder().getStructureId(),
                        copier.getCopiedFolderOrPage().getStructureId()));
            } catch (CmsException e) {
                m_context.error(e);
            } catch (NoCustomReplacementException e) {
                String errorMessage = CmsVaadinUtils.getMessageText(
                    org.opencms.ui.Messages.GUI_COPYPAGE_NO_REPLACEMENT_FOUND_1,
                    e.getResource().getRootPath());
                CmsErrorDialog.showErrorDialog(errorMessage, e);
            }
        } else {

            CmsResourceCopyMode copyMode = null;
            switch ((Action)m_actionCombo.getValue()) {
                case copy_all:
                    copyMode = CmsResource.COPY_AS_NEW;
                    break;
                case copy_sibling_all:
                    copyMode = CmsResource.COPY_AS_SIBLING;
                    break;
                case copy_sibling_mixed:
                case sub_sitemap:
                default:
                    copyMode = CmsResource.COPY_PRESERVE_SIBLING;
            }

            getRootCms().copyResource(source.getRootPath(), finalTarget, copyMode);
            if (action == Action.sub_sitemap) {
                getRootCms().adjustLinks(source.getRootPath(), finalTarget);
            }
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
     *
     * @param overwrite to forcefully overwrite existing files
     */
    void submit(boolean overwrite) {

        try {
            CmsResource targetFolder;
            String targetName = null;
            String target = m_targetPath.getValue();
            // resolve relative paths
            target = CmsLinkManager.getAbsoluteUri(
                target,
                CmsResource.getParentFolder(getCms().getSitePath(m_context.getResources().get(0))));

            // check if the given path is a root path
            CmsObject cms = OpenCms.getSiteManager().getSiteForRootPath(target) != null ? getRootCms() : getCms();

            if (cms.existsResource(target)) {
                targetFolder = cms.readResource(target);

            } else {
                targetName = CmsResource.getName(target);
                targetFolder = cms.readResource(CmsResource.getParentFolder(target));
            }

            if (targetFolder.isFile()) {
                throw new CmsVfsException(
                    Messages.get().container(
                        org.opencms.workplace.commons.Messages.ERR_COPY_MULTI_TARGET_NOFOLDER_1,
                        targetFolder));
            }
            overwrite = overwrite || isOverwriteExisting();
            if (!overwrite) {
                List<CmsResource> collidingResources = getExistingFileCollisions(targetFolder);
                if (collidingResources != null) {
                    showConfirmOverwrite(collidingResources);
                    return;
                }
            }
            Action action = m_actionCombo != null ? (Action)m_actionCombo.getValue() : Action.move;
            Map<CmsResource, CmsException> errors = new HashMap<CmsResource, CmsException>();
            if (targetName == null) {
                for (CmsResource source : m_context.getResources()) {
                    try {
                        performSingleOperation(source, targetFolder, action, overwrite);
                    } catch (CmsException e) {
                        errors.put(source, e);
                        LOG.error(
                            "Error while executing "
                                + m_actionCombo.getValue().toString()
                                + " on resource "
                                + source.getRootPath(),
                            e);
                    }
                }
            } else {
                // this will only be the case in a single resource scenario
                CmsResource source = m_context.getResources().get(0);
                try {
                    performSingleOperation(source, targetFolder, targetName, action, overwrite);
                } catch (CmsException e) {
                    errors.put(source, e);
                    LOG.error(
                        "Error while executing "
                            + m_actionCombo.getValue().toString()
                            + " on resource "
                            + source.getRootPath(),
                        e);
                }
            }

            if (!errors.isEmpty()) {
                m_context.finish(m_updateResources);
                m_context.error(errors.values().iterator().next());
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
     * Returns the resources that collide with already existing resources.<p>
     *
     * @param targetFolder the target folder
     *
     * @return the colliding resources or <code>null</code> if no collisions found
     *
     * @throws CmsException in case the checking the resources fails
     */
    private List<CmsResource> getExistingFileCollisions(CmsResource targetFolder) throws CmsException {

        List<CmsResource> collidingResources = new ArrayList<CmsResource>();

        String finalTarget = targetFolder.getRootPath();
        if (!finalTarget.endsWith("/")) {
            finalTarget += "/";
        }
        for (CmsResource source : m_context.getResources()) {
            if (finalTarget.equals(CmsResource.getParentFolder(source.getRootPath()))) {
                // copying to the same folder, a new name will be generated
                return null;
            }
            String fileName = finalTarget + source.getName();
            if (getRootCms().existsResource(fileName, CmsResourceFilter.ALL)) {
                collidingResources.add(source);
            }
        }
        return collidingResources.isEmpty() ? null : collidingResources;
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
     * Checks whether the folder has a default file of the type container page.<p>
     *
     * @param folder the folder to check
     *
     * @return <code>true</code> if the folder has a default file of the type container page
     */
    private boolean hasContainerPageDefaultFile(CmsResource folder) {

        try {
            CmsResource defaultFile = A_CmsUI.getCmsObject().readDefaultFile(
                folder,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            return (defaultFile != null) && CmsResourceTypeXmlContainerPage.isContainerPage(defaultFile);
        } catch (CmsSecurityException e) {
            return false;
        }
    }

    /**
     * Initializes the form fields.<p>
     *
     * @return the form component
     */
    private FormLayout initForm() {

        FormLayout form = new FormLayout();
        form.setWidth("100%");
        m_targetPath = new CmsPathSelectField();
        m_targetPath.setCaption(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_MOVE_TARGET_0));
        m_targetPath.setFileSelectCaption(
            CmsVaadinUtils.getMessageText(Messages.GUI_COPY_MOVE_SELECT_TARGET_CAPTION_0));
        m_targetPath.setResourceFilter(CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder());
        m_targetPath.setWidth("100%");
        form.addComponent(m_targetPath);

        if (m_dialogMode != DialogMode.move) {
            m_actionCombo = new ComboBox();
            m_actionCombo.setCaption(CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_COPYPAGE_COPY_MODE_0));
            m_actionCombo.setNullSelectionAllowed(false);
            m_actionCombo.setNewItemsAllowed(false);
            m_actionCombo.setWidth("100%");
            //            m_actionCombo.addItem(Action.automatic);
            //            m_actionCombo.setItemCaption(
            //                Action.automatic,
            //                CmsVaadinUtils.getMessageText(Messages.GUI_COPY_MOVE_AUTOMATIC_0));
            //            m_actionCombo.setValue(Action.automatic);
            if (m_context.getResources().size() == 1) {
                if (m_context.getResources().get(0).isFile()) {
                    m_actionCombo.addItem(Action.copy_all);
                    m_actionCombo.setItemCaption(
                        Action.copy_all,
                        CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_AS_NEW_0));
                    m_actionCombo.addItem(Action.copy_sibling_all);
                    m_actionCombo.setItemCaption(
                        Action.copy_sibling_all,
                        CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_CREATE_SIBLING_0));
                    if (m_dialogMode == DialogMode.copy_and_move) {
                        m_actionCombo.addItem(Action.move);
                        m_actionCombo.setItemCaption(
                            Action.move,
                            CmsVaadinUtils.getMessageText(
                                org.opencms.workplace.commons.Messages.GUI_COPY_MOVE_MOVE_FILE_0));
                    }
                } else {
                    CmsResource folder = m_context.getResources().get(0);
                    m_hasContainerPageDefaultFile = hasContainerPageDefaultFile(folder);
                    if (m_hasContainerPageDefaultFile) {
                        m_actionCombo.addItem(Action.container_page_automatic);
                        m_actionCombo.setItemCaption(
                            Action.container_page_automatic,
                            CmsVaadinUtils.getMessageText(Messages.GUI_COPY_MOVE_AUTOMATIC_0));
                        m_actionCombo.addItem(Action.container_page_copy);
                        m_actionCombo.setItemCaption(
                            Action.container_page_copy,
                            CmsVaadinUtils.getMessageText(Messages.GUI_COPY_MOVE_CONTAINERPAGE_COPY_0));
                        m_actionCombo.addItem(Action.container_page_reuse);
                        m_actionCombo.setItemCaption(
                            Action.container_page_reuse,
                            CmsVaadinUtils.getMessageText(Messages.GUI_COPY_MOVE_CONTAINERPAGE_REUSE_0));
                    }
                    if (CmsResourceTypeFolderSubSitemap.isSubSitemap(folder)) {
                        m_actionCombo.addItem(Action.sub_sitemap);
                        m_actionCombo.setItemCaption(
                            Action.sub_sitemap,
                            CmsVaadinUtils.getMessageText(Messages.GUI_COPY_MOVE_SUBSITEMAP_0));
                    }
                    m_actionCombo.addItem(Action.copy_sibling_mixed);
                    m_actionCombo.setItemCaption(
                        Action.copy_sibling_mixed,
                        CmsVaadinUtils.getMessageText(
                            org.opencms.workplace.commons.Messages.GUI_COPY_ALL_NO_SIBLINGS_0));
                    m_actionCombo.addItem(Action.copy_all);
                    m_actionCombo.setItemCaption(
                        Action.copy_all,
                        CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_ALL_0));
                    m_actionCombo.addItem(Action.copy_sibling_all);
                    m_actionCombo.setItemCaption(
                        Action.copy_sibling_all,
                        CmsVaadinUtils.getMessageText(
                            org.opencms.workplace.commons.Messages.GUI_COPY_MULTI_CREATE_SIBLINGS_0));
                    if (m_dialogMode == DialogMode.copy_and_move) {
                        m_actionCombo.addItem(Action.move);
                        m_actionCombo.setItemCaption(
                            Action.move,
                            CmsVaadinUtils.getMessageText(
                                org.opencms.workplace.commons.Messages.GUI_COPY_MOVE_MOVE_FOLDER_0));
                    }
                }
            } else {
                m_actionCombo.addItem(Action.copy_sibling_mixed);
                m_actionCombo.setItemCaption(
                    Action.copy_sibling_mixed,
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_ALL_NO_SIBLINGS_0));
                m_actionCombo.addItem(Action.copy_all);
                m_actionCombo.setItemCaption(
                    Action.copy_all,
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_ALL_0));
                m_actionCombo.addItem(Action.copy_sibling_all);
                m_actionCombo.setItemCaption(
                    Action.copy_sibling_all,
                    CmsVaadinUtils.getMessageText(
                        org.opencms.workplace.commons.Messages.GUI_COPY_MULTI_CREATE_SIBLINGS_0));
                if (m_dialogMode == DialogMode.copy_and_move) {
                    m_actionCombo.addItem(Action.move);
                    m_actionCombo.setItemCaption(
                        Action.move,
                        CmsVaadinUtils.getMessageText(
                            org.opencms.workplace.commons.Messages.GUI_COPY_MOVE_MOVE_RESOURCES_0));
                }
            }
            m_actionCombo.setItemStyleGenerator(new ItemStyleGenerator() {

                private static final long serialVersionUID = 1L;

                public String getStyle(ComboBox source, Object itemId) {

                    String style = null;
                    if (m_defaultActions.contains(itemId)) {
                        style = "bold";
                    }
                    return style;
                }
            });
            form.addComponent(m_actionCombo);
        }
        if (m_context.getResources().size() > 1) {
            m_overwriteExisting = new CheckBox(
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_MULTI_OVERWRITE_0));
            m_overwriteExisting.setValue(Boolean.FALSE);
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

    /**
     * Displays the confirm overwrite dialog.<p>
     *
     * @param collidingResources the colliding resources
     */
    private void showConfirmOverwrite(List<CmsResource> collidingResources) {

        final Window window = CmsBasicDialog.prepareWindow();
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_COPY_MOVE_CONFIRM_OVERWRITE_TITLE_0));
        CmsConfirmationDialog dialog = new CmsConfirmationDialog(
            CmsVaadinUtils.getMessageText(Messages.GUI_COPY_MOVE_CONFIRM_OVERWRITE_MESSAGE_0),
            new Runnable() {

                public void run() {

                    window.close();
                    submit(true);
                }
            },
            new Runnable() {

                public void run() {

                    window.close();
                    cancel();
                }
            });
        dialog.displayResourceInfo(collidingResources);
        window.setContent(dialog);
        UI.getCurrent().addWindow(window);
    }

    /**
     * Updates the default dialog actions.<p>
     *
     * @param targetRootPath the target root path
     */
    private void updateDefaultActions(String targetRootPath) {

        if (m_actionCombo != null) {
            m_defaultActions.clear();
            String resPath = m_context.getResources().get(0).getRootPath();
            String parentFolder = CmsResource.getParentFolder(resPath);
            if ((DialogMode.copy_and_move == m_dialogMode) && !parentFolder.equals(targetRootPath)) {
                m_defaultActions.clear();
                m_defaultActions.add(Action.move);
            } else if (m_context.getResources().size() == 1) {
                if (m_context.getResources().get(0).isFile()) {
                    m_defaultActions.add(Action.copy_all);
                } else {
                    m_defaultActions.add(Action.copy_sibling_mixed);
                    if (m_hasContainerPageDefaultFile) {
                        m_defaultActions.clear();
                        m_defaultActions.add(Action.container_page_automatic);
                        m_defaultActions.add(Action.container_page_copy);
                        m_defaultActions.add(Action.container_page_reuse);
                    }
                    CmsResource folder = m_context.getResources().get(0);
                    if (CmsResourceTypeFolderSubSitemap.isSubSitemap(folder)) {
                        m_defaultActions.clear();
                        m_defaultActions.add(Action.sub_sitemap);
                    }
                }
            } else {
                m_defaultActions.add(Action.copy_sibling_mixed);
            }
            if (!m_defaultActions.isEmpty()) {
                m_actionCombo.setValue(m_defaultActions.get(0));
            }
            m_actionCombo.markAsDirty();
        }
    }
}

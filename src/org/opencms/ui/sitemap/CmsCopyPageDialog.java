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

package org.opencms.ui.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.tools.CmsContainerPageCopier;
import org.opencms.i18n.tools.CmsContainerPageCopier.NoCustomReplacementException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

/**
 * Dialog used to copy container pages including their elements.<p>
 */
public class CmsCopyPageDialog extends CmsBasicDialog {

    /**
     * Helper class used to interpret the target path in the 'copy page' dioalog.<p>
     */
    public class TargetInfo {

        /** True if the target path or its parent is a file, not a folder. */
        private boolean m_isFile;

        /** The target folder. */
        private CmsResource m_targetFolder;

        /** The target name. */
        private String m_targetName;

        /**
         * Creates a new instance.<p>
         *
         * @param cms the CMS context to use
         * @param path the target path to analyze
         */
        @SuppressWarnings("synthetic-access")
        public TargetInfo(CmsObject cms, String path) {
            try {
                if (CmsStringUtil.isPrefixPath(CmsResource.VFS_FOLDER_SITES, path)
                    || (OpenCms.getSiteManager().getSiteForRootPath(path) != null)) {
                    cms = OpenCms.initCmsObject(cms);
                    cms.getRequestContext().setSiteRoot("");
                }
                CmsResource resource = readIfExists(cms, path);
                if (resource != null) {
                    if (resource.isFile()) {
                        m_isFile = true;
                    } else {
                        m_targetFolder = resource;
                    }
                    return;
                }
                resource = readIfExists(cms, CmsResource.getParentFolder(path));
                if (resource != null) {
                    if (resource.isFile()) {
                        m_isFile = true;
                    } else {
                        m_targetFolder = resource;
                        m_targetName = CmsResource.getName(path);
                    }
                    return;
                }
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }

        /**
         * Gets the target folder resource, or null if no target folder could be determined.<p>
         *
         * @return the target folder resource
         */
        public CmsResource getTargetFolder() {

            return m_targetFolder;
        }

        /**
         * Gets the target name, or null if only the target folder was given in the target select widget.<p>
         *
         * @return the target name
         */
        public String getTargetName() {

            return m_targetName;
        }

        /**
         * Returns true if the target path or its parent is actually a file.<p>
         *
         * @return true if the target path or its parent is a file
         */
        public boolean isFile() {

            return m_isFile;
        }

        /**
         * Returns true if the target folder could be determined from the target path.<p>
         *
         * @return true if the target folder could be determined
         */
        public boolean isValid() {

            return m_targetFolder != null;
        }

        /**
         * Helper method for reading a resource 'safely', i.e. without logging errors when it fails.<p>
         *
         * @param cms the CMS context
         * @param path a resource path
         *
         * @return the resource, or null if the resource doesn't exist or couldn't be read
         */
        @SuppressWarnings("synthetic-access")
        private CmsResource readIfExists(CmsObject cms, String path) {

            if (cms.existsResource(path, CmsResourceFilter.IGNORE_EXPIRATION)) {
                try {
                    return cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
                } catch (Exception e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCopyPageDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The copy mode selection field. */
    private ComboBox m_copyMode = new ComboBox();

    /** The OK button. */
    private Button m_okButton;

    /** The field for selecting the target folder. */
    private CmsPathSelectField m_targetSelect;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsCopyPageDialog(I_CmsDialogContext context) {
        m_context = context;
        displayResourceInfo(context.getResources());
        initButtons();
        m_copyMode.setNullSelectionAllowed(false);
        setContent(initContent());
    }

    /**
     * Initializes the button bar.<p>
     */
    void initButtons() {

        m_okButton = new Button(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClickOk();
            }
        });
        addButton(m_okButton);
        m_cancelButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                m_context.finish(new ArrayList<CmsUUID>());
            }
        });

        addButton(m_cancelButton);
    }

    /**
     * Method that is called when the OK button is clicked.<p>
     */
    void onClickOk() {

        CmsContainerPageCopier copier = new CmsContainerPageCopier(m_context.getCms());
        try {
            CmsContainerPageCopier.CopyMode mode = (CmsContainerPageCopier.CopyMode)(m_copyMode.getValue());
            copier.setCopyMode(mode);
            TargetInfo info = new TargetInfo(m_context.getCms(), m_targetSelect.getValue());
            if (!info.isValid()) {
                Type type = Type.ERROR_MESSAGE;
                String error = CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_INVALID_TARGET_0);
                Notification.show(error, type);
                return;
            }
            CmsResource targetFolder = info.getTargetFolder();
            copier.run(m_context.getResources().get(0), targetFolder, info.getTargetName());
            m_context.finish(
                Arrays.asList(
                    copier.getTargetFolder().getStructureId(),
                    copier.getCopiedFolderOrPage().getStructureId()));
        } catch (CmsException e) {
            m_context.error(e);
        } catch (NoCustomReplacementException e) {
            String errorMessage = CmsVaadinUtils.getMessageText(
                Messages.GUI_COPYPAGE_NO_REPLACEMENT_FOUND_1,
                e.getResource().getRootPath());
            CmsErrorDialog.showErrorDialog(errorMessage, e);
        }
    }

    /**
     * Gets the initial target path to display, based on the selected resource.<p>
     *
     * @param cms the cms context
     * @param resource the selected resource
     *
     * @return the initial target path
     */
    private String getInitialTarget(CmsObject cms, CmsResource resource) {

        String sitePath = cms.getSitePath(resource);
        String parent = CmsResource.getParentFolder(sitePath);
        if (parent != null) {
            return parent;
        } else {
            String rootParent = CmsResource.getParentFolder(resource.getRootPath());
            if (rootParent != null) {
                return rootParent;
            } else {
                return sitePath;
            }
        }
    }

    /**
     * Initializes the content panel.<p>
     *
     * @return the content panel
     */
    private FormLayout initContent() {

        FormLayout form = new FormLayout();
        CmsPathSelectField field = new CmsPathSelectField();
        field.setValue(getInitialTarget(m_context.getCms(), m_context.getResources().get(0)));
        field.setStartWithSitempaView(true);
        field.setResourceFilter(CmsResourceFilter.IGNORE_EXPIRATION.addRequireFolder());
        field.setCaption(CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_COPY_MOVE_TARGET_0));
        form.addComponent(field);
        m_targetSelect = field;
        m_copyMode.addItem(CmsContainerPageCopier.CopyMode.automatic);
        m_copyMode.setItemCaption(
            CmsContainerPageCopier.CopyMode.automatic,
            CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_MODE_AUTO_0));

        m_copyMode.addItem(CmsContainerPageCopier.CopyMode.smartCopyAndChangeLocale);
        m_copyMode.setItemCaption(
            CmsContainerPageCopier.CopyMode.smartCopyAndChangeLocale,
            CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_MODE_SMART_0));
        m_copyMode.addItem(CmsContainerPageCopier.CopyMode.reuse);
        m_copyMode.setItemCaption(
            CmsContainerPageCopier.CopyMode.reuse,
            CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_MODE_REUSE_0));
        m_copyMode.setValue(CmsContainerPageCopier.CopyMode.automatic);
        form.addComponent(m_copyMode);
        m_copyMode.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_COPY_MODE_0));
        return form;
    }

}

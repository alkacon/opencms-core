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

import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsCoreService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.CmsPageEditorConfiguration;
import org.opencms.ui.apps.I_CmsHasAppLaunchCommand;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsExtendedSiteSelector;
import org.opencms.ui.components.CmsExtendedSiteSelector.SiteSelectorOption;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.util.CmsUUID;

import java.util.Collections;

import org.apache.commons.logging.Log;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.UI;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.ComboBox;

/**
 * The project select dialog.<p>
 */
public class CmsProjectSelectDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsProjectSelectDialog.class);

    /** The project name property. */
    private static final String CAPTION_PROPERTY = "caption";

    /** The serial version id. */
    private static final long serialVersionUID = 4455901453008760434L;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The project select. */
    private ComboBox m_projectComboBox;

    /** The site select. */
    private CmsExtendedSiteSelector m_siteComboBox;

    /**
     * Constructor.<p>
     *
     * @param context the dialog context
     */
    public CmsProjectSelectDialog(I_CmsDialogContext context) {

        m_context = context;
        setContent(initForm());
        m_cancelButton = createButtonCancel();
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

                CmsProjectSelectDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Static method for actually changing the site/project.
     *
     * @param context the dialog context
     * @param projectId the project id (possibly null)
     * @param siteOption the option from the site selector
     */
    public static void changeSiteOrProject(
        I_CmsDialogContext context,
        CmsUUID projectId,
        SiteSelectorOption siteOption) {

        String siteRoot = null;
        try {
            CmsProject project = null;
            if (projectId != null) {
                project = context.getCms().readProject(projectId);
                if (!context.getCms().getRequestContext().getCurrentProject().equals(project)) {
                    A_CmsUI.get().changeProject(project);
                } else {
                    project = null;
                }
            }
            if (siteOption != null) {

                siteRoot = siteOption.getSite();
                if (!context.getCms().getRequestContext().getSiteRoot().equals(siteRoot)) {
                    A_CmsUI.get().changeSite(siteRoot);
                } else if (siteOption.getPath() == null) {
                    siteRoot = null;
                }
            }
            if ((siteRoot != null) && CmsFileExplorerConfiguration.APP_ID.equals(context.getAppId())) {
                if (siteOption.getPath() != null) {
                    CmsResource defaultFile = null;
                    try {
                        defaultFile = A_CmsUI.getCmsObject().readDefaultFile(siteOption.getPath());
                    } catch (Exception e) {
                        // ignore
                    }
                    if (defaultFile != null) {
                        Page.getCurrent().setLocation(
                            OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                                A_CmsUI.getCmsObject(),
                                siteOption.getPath()));
                        return;
                    } else {
                        Page.getCurrent().open(
                            CmsCoreService.getFileExplorerLink(A_CmsUI.getCmsObject(), siteOption.getSite())
                                + siteOption.getPath(),
                            "_top");
                    }
                } else {
                    I_CmsWorkplaceAppConfiguration editorConf = OpenCms.getWorkplaceAppManager().getAppConfiguration(
                        CmsPageEditorConfiguration.APP_ID);
                    if (editorConf.getVisibility(context.getCms()).isActive()) {
                        ((I_CmsHasAppLaunchCommand)editorConf).getAppLaunchCommand().run();
                        return;
                    }
                }
            }
            context.finish(project, siteRoot);
        } catch (CmsException e) {
            context.error(e);
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

        I_CmsDialogContext context = m_context;
        CmsUUID projectId = (CmsUUID)m_projectComboBox.getValue();
        SiteSelectorOption option = m_siteComboBox.getValue();
        changeSiteOrProject(context, projectId, option);
    }

    /**
     * Initializes the form component.<p>
     *
     * @return the form component
     */
    private FormLayout initForm() {

        FormLayout form = new FormLayout();
        form.setWidth("100%");

        m_siteComboBox = prepareSiteSelector(org.opencms.workplace.Messages.GUI_LABEL_SITE_0);

        m_siteComboBox.selectSite(m_context.getCms().getRequestContext().getSiteRoot());
        form.addComponent(m_siteComboBox);

        m_siteComboBox.addValueChangeListener(evt -> submit());
        IndexedContainer projects = CmsVaadinUtils.getProjectsContainer(m_context.getCms(), CAPTION_PROPERTY);
        m_projectComboBox = prepareComboBox(projects, org.opencms.workplace.Messages.GUI_LABEL_PROJECT_0);
        CmsUUID currentProjectId = m_context.getCms().getRequestContext().getCurrentProject().getUuid();
        if (projects.containsId(currentProjectId)) {
            m_projectComboBox.select(currentProjectId);
        } else {
            try {
                CmsUUID ouProject = OpenCms.getOrgUnitManager().readOrganizationalUnit(
                    m_context.getCms(),
                    m_context.getCms().getRequestContext().getOuFqn()).getProjectId();
                if (projects.containsId(ouProject)) {
                    m_projectComboBox.select(ouProject);
                }
            } catch (CmsException e) {
                LOG.error("Error while reading current OU.", e);
            }
        }

        form.addComponent(m_projectComboBox);
        m_projectComboBox.addValueChangeListener(evt -> submit());
        return form;
    }

    /**
     * Prepares a combo box.<p>
     *
     * @param container the indexed item container
     * @param captionKey the caption message key
     *
     * @return the combo box
     */
    private ComboBox prepareComboBox(IndexedContainer container, String captionKey) {

        ComboBox result = new ComboBox(CmsVaadinUtils.getWpMessagesForCurrentLocale().key(captionKey), container);
        result.setTextInputAllowed(true);
        result.setNullSelectionAllowed(false);
        result.setWidth("100%");
        result.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_CLICK_TO_EDIT_0));
        result.setItemCaptionPropertyId(CAPTION_PROPERTY);
        result.setFilteringMode(FilteringMode.CONTAINS);
        return result;
    }

    /**
     * Prepares a combo box.<p>
     *
     * @param captionKey the caption message key
     *
     * @return the combo box
     */
    private CmsExtendedSiteSelector prepareSiteSelector(String captionKey) {

        CmsExtendedSiteSelector result = new CmsExtendedSiteSelector();
        boolean isExplorer = CmsFileExplorerConfiguration.APP_ID.equals(m_context.getAppId());
        result.initOptions(m_context.getCms(), isExplorer);
        result.setPageLength(CmsExtendedSiteSelector.LONG_PAGE_LENGTH);
        result.setCaption(CmsVaadinUtils.getWpMessagesForCurrentLocale().key(captionKey));
        result.setWidth("100%");
        result.setPlaceholder(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_CLICK_TO_EDIT_0));
        return result;
    }
}

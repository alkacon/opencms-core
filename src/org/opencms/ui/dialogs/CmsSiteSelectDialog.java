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
import org.opencms.gwt.CmsCoreService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsExtendedSiteSelector;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.UI;

/**
 * The site select dialog.<p>
 */
public class CmsSiteSelectDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsSiteSelectDialog.class);

    /** The serial version id. */
    private static final long serialVersionUID = 4455901453008760434L;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The site select. */
    private CmsExtendedSiteSelector m_siteComboBox;

    /** Select box for changing the current project. */
    private ComboBox<CmsUUID> m_projectSelector;

    /**
     * Constructor.<p>
     *
     * @param context the dialog context
     */
    public CmsSiteSelectDialog(CmsEmbeddedDialogContext context) {

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

                CmsSiteSelectDialog.this.cancel();
            }

            @Override
            protected void ok() {

                changeSite();
            }
        });
    }

    /**
     * Actually changes the site.
     *
     * @param context the dialog context
     * @param option the site selector option
     */
    public static void changeSite(CmsEmbeddedDialogContext context, CmsExtendedSiteSelector.SiteSelectorOption option) {

        if ((option.getPath() != null)
            || !CmsStringUtil.comparePaths(context.getCms().getRequestContext().getSiteRoot(), option.getSite())) {
            String siteRoot = option.getSite();
            A_CmsUI.get().changeSite(option.getSite());
            if (CmsStringUtil.comparePaths(siteRoot, "/") || OpenCms.getSiteManager().isSharedFolder(siteRoot)) {
                // switch to explorer view when selecting shared or root site

                Page.getCurrent().open(CmsCoreService.getFileExplorerLink(A_CmsUI.getCmsObject(), siteRoot), "_top");
                return;
            }
            context.finish(option);
        } else {
            context.finish(null, null);
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
    void changeSite() {

        CmsExtendedSiteSelector.SiteSelectorOption option = m_siteComboBox.getValue();
        I_CmsDialogContext context = m_context;
        changeSite((CmsEmbeddedDialogContext)context, option);
    }

    /**
     * Initializes the form component.<p>
     *
     * @return the form component
     */
    private FormLayout initForm() {

        FormLayout form = new FormLayout();
        form.setWidth("100%");
        CmsObject cms = A_CmsUI.getCmsObject();
        m_siteComboBox = prepareSiteSelector(org.opencms.workplace.Messages.GUI_LABEL_SITE_0);
        CmsExtendedSiteSelector.SiteSelectorOption optionForCurrentSite = m_siteComboBox.getOptionForSiteRoot(
            cms.getRequestContext().getSiteRoot());
        if (optionForCurrentSite != null) {
            m_siteComboBox.setValue(optionForCurrentSite);
        }
        form.addComponent(m_siteComboBox);
        m_siteComboBox.addValueChangeListener(evt -> changeSite());

        Map<CmsUUID, String> projects = CmsVaadinUtils.getProjectsMap(A_CmsUI.getCmsObject());

        // switching to the Online project gives you no way to switch back unless you have access to the workplace
        projects.remove(CmsProject.ONLINE_PROJECT_ID);

        ListDataProvider<CmsUUID> projectsProvider = new ListDataProvider<>(projects.keySet());
        m_projectSelector = new ComboBox<CmsUUID>();
        m_projectSelector.setDataProvider(projectsProvider);
        m_projectSelector.setItemCaptionGenerator(item -> projects.get(item));
        m_projectSelector.setWidth("100%");
        m_projectSelector.setEmptySelectionAllowed(false);
        m_projectSelector.setCaption(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_LABEL_PROJECT_0));
        CmsUUID currentProjectId = m_context.getCms().getRequestContext().getCurrentProject().getUuid();
        if (projects.containsKey(currentProjectId)) {
            m_projectSelector.setValue(currentProjectId);
        } else {
            try {
                CmsUUID ouProject = OpenCms.getOrgUnitManager().readOrganizationalUnit(
                    m_context.getCms(),
                    m_context.getCms().getRequestContext().getOuFqn()).getProjectId();
                if (projects.containsKey(ouProject)) {
                    m_projectSelector.setValue(ouProject);
                }
            } catch (CmsException e) {
                LOG.error("Error while reading current OU.", e);
            }
        }
        form.addComponent(m_projectSelector);
        m_projectSelector.addValueChangeListener(event -> {
            try {
                A_CmsUI.get().changeProject(cms.readProject(event.getValue()));

                // Reload, to make sure that we don't have any project ids stored in client side code that differ
                // from the new project
                m_context.finish(Arrays.asList(CmsUUID.getNullUUID()));
            } catch (Exception e) {
                CmsErrorDialog.showErrorDialog(e, () -> m_context.finish(null));
            }
        });
        return form;
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
        String caption = CmsVaadinUtils.getWpMessagesForCurrentLocale().key(captionKey);
        result.setCaption(caption);
        result.initOptions(m_context.getCms(), true);
        result.setPageLength(CmsExtendedSiteSelector.LONG_PAGE_LENGTH);
        result.setWidth("100%");
        result.setPlaceholder(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_CLICK_TO_EDIT_0));
        return result;
    }
}

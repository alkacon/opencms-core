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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.UI;

/**
 * The project select dialog.<p>
 */
public class CmsProjectSelectDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsProjectSelectDialog.class);

    /** The project name property. */
    private static final String PROJECT_NAME = "project-name";

    /** The serial version id. */
    private static final long serialVersionUID = 4455901453008760434L;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The OK button. */
    private Button m_okButton;

    /** The project select. */
    private ComboBox m_projectComboBox;

    /**
     * Constructor.<p>
     *
     * @param context the dialog context
     */
    public CmsProjectSelectDialog(I_CmsDialogContext context) {
        m_context = context;
        setContent(initForm());
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
            CmsProject project = m_context.getCms().readProject((CmsUUID)m_projectComboBox.getValue());

            m_context.getCms().getRequestContext().setCurrentProject(project);
            CmsAppWorkplaceUi.get().getWorkplaceSettings().setProject(project.getUuid());
            m_context.reload();
        } catch (CmsException e) {
            m_context.error(e);
        }
    }

    /**
     * Returns the available projects.<p>
     *
     * @return the available projects
     */
    private List<CmsProject> getAvailableProjects() {

        // get all project information
        List<CmsProject> allProjects;
        try {
            String ouFqn = "";
            CmsUserSettings settings = new CmsUserSettings(m_context.getCms());
            if (!settings.getListAllProjects()) {
                ouFqn = m_context.getCms().getRequestContext().getCurrentUser().getOuFqn();
            }
            allProjects = new ArrayList<CmsProject>(
                OpenCms.getOrgUnitManager().getAllAccessibleProjects(
                    m_context.getCms(),
                    ouFqn,
                    settings.getListAllProjects()));
            Iterator<CmsProject> itProjects = allProjects.iterator();
            while (itProjects.hasNext()) {
                CmsProject prj = itProjects.next();
                if (prj.isHiddenFromSelector()) {
                    itProjects.remove();
                }
            }
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            allProjects = Collections.emptyList();
        }
        return allProjects;
    }

    /**
     * Returns the selectable projects container.<p>
     *
     * @return the projects container
     */
    private IndexedContainer getProjectsContainer() {

        IndexedContainer result = new IndexedContainer();
        result.addContainerProperty(PROJECT_NAME, String.class, null);
        List<CmsProject> projects = getAvailableProjects();
        boolean isSingleOu = isSingleOu(projects);
        for (CmsProject project : projects) {
            String projectName = project.getSimpleName();
            if (!isSingleOu && !project.isOnlineProject()) {
                try {
                    projectName = projectName
                        + " - "
                        + OpenCms.getOrgUnitManager().readOrganizationalUnit(
                            m_context.getCms(),
                            project.getOuFqn()).getDisplayName(getLocale());
                } catch (CmsException e) {
                    LOG.debug("Error reading project OU.", e);
                    projectName = projectName + " - " + project.getOuFqn();
                }
            }
            Item projectItem = result.addItem(project.getUuid());
            projectItem.getItemProperty(PROJECT_NAME).setValue(projectName);
        }
        return result;
    }

    /**
     * Initializes the form component.<p>
     *
     * @return the form component
     */
    private FormLayout initForm() {

        FormLayout form = new FormLayout();
        form.setWidth("100%");

        final IndexedContainer projects = getProjectsContainer();
        m_projectComboBox = new ComboBox(
            CmsVaadinUtils.getWpMessagesForCurrentLocale().key(org.opencms.workplace.Messages.GUI_LABEL_PROJECT_0),
            projects);
        m_projectComboBox.setTextInputAllowed(true);
        m_projectComboBox.setNullSelectionAllowed(false);
        m_projectComboBox.setWidth("100%");
        m_projectComboBox.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_CLICK_TO_EDIT_0));
        m_projectComboBox.setItemCaptionPropertyId(PROJECT_NAME);
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

        m_projectComboBox.setFilteringMode(FilteringMode.CONTAINS);
        form.addComponent(m_projectComboBox);
        return form;
    }

    /**
     * Returns whether only a single OU is visible to the current user.<p>
     *
     * @param projects the selectable projects
     *
     * @return <code>true</code> if only a single OU is visible to the current user
     */
    private boolean isSingleOu(List<CmsProject> projects) {

        String ouFqn = null;
        for (CmsProject project : projects) {
            if (project.isOnlineProject()) {
                // skip the online project
                continue;
            }
            if (ouFqn == null) {
                // set the first ou
                ouFqn = project.getOuFqn();
            } else if (!ouFqn.equals(project.getOuFqn())) {
                // break if one different ou is found
                return false;
            }
        }
        return true;
    }
}

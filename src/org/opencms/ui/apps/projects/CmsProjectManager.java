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

package org.opencms.ui.apps.projects;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.components.CmsInfoButton;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The project manager app.<p>
 */
public class CmsProjectManager extends A_CmsWorkplaceApp {

    /** The project files path name. */
    public static final String PATH_NAME_FILES = "files";

    /** The project history path name. */
    public static final String PATH_NAME_HISTORY = "history";

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsProjectManager.class.getName());

    /** The projects table. */
    CmsProjectsTable m_projectsTable;

    /** The file table filter input. */
    private TextField m_fileTableFilter;

    /** The sub nav buttons of this app. */
    private List<Component> m_navButtons = new ArrayList<Component>();

    /** The project table filter input. */
    private TextField m_projectTableFilter;

    /**
     * Returns the projects table component.<p>
     *
     * @return the projects table
     */
    protected CmsProjectsTable createProjectsTable() {

        CmsProjectsTable table = new CmsProjectsTable(this);
        table.loadProjects();
        return table;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        if (!OpenCms.getRoleManager().hasRole(A_CmsUI.getCmsObject(), CmsRole.PROJECT_MANAGER)) {
            crumbs.put(
                "",
                CmsVaadinUtils.getMessageText(
                    Messages.GUI_PROJECTS_FILES_1,
                    A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().getName()));
        } else if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_0));
        } else if (state.equals(PATH_NAME_HISTORY)) {
            crumbs.put(CmsProjectManagerConfiguration.APP_ID, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_0));
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_HISTORY_0));
        } else if (state.startsWith(PATH_NAME_FILES)) {
            CmsUUID projectId = getIdFromState(state);
            if (projectId != null) {
                crumbs.put(
                    CmsProjectManagerConfiguration.APP_ID,
                    CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_0));
                try {
                    crumbs.put(
                        "",
                        CmsVaadinUtils.getMessageText(
                            Messages.GUI_PROJECTS_FILES_1,
                            A_CmsUI.getCmsObject().readProject(projectId).getName()));
                } catch (CmsException e) {
                    LOG.error("Error reading project for bread crumb.", e);
                }
            }
        }
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (!OpenCms.getRoleManager().hasRole(A_CmsUI.getCmsObject(), CmsRole.PROJECT_MANAGER)) {
            return prepareProjectFilesTable(A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().getUuid());
        } else {

            if (m_fileTableFilter != null) {
                m_infoLayout.removeComponent(m_fileTableFilter);
                m_fileTableFilter = null;
            }
            if (m_projectTableFilter != null) {
                m_infoLayout.removeComponent(m_projectTableFilter);
                m_projectTableFilter = null;
            }

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
                m_rootLayout.setMainHeightFull(true);
                if (m_projectsTable == null) {
                    m_projectsTable = createProjectsTable();
                } else {
                    m_projectsTable.loadProjects();
                }
                m_projectTableFilter = new TextField();
                m_projectTableFilter.setIcon(FontOpenCms.FILTER);
                m_projectTableFilter.setInputPrompt(
                    Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
                m_projectTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
                m_projectTableFilter.setWidth("200px");
                m_projectTableFilter.addTextChangeListener(new TextChangeListener() {

                    private static final long serialVersionUID = 1L;

                    public void textChange(TextChangeEvent event) {

                        m_projectsTable.filterTable(event.getText());

                    }
                });
                m_infoLayout.addComponent(m_projectTableFilter);
                showNavButtons();
                return m_projectsTable;
                //            } else if (state.equals(PATH_NAME_ADD)) {
                //                m_rootLayout.setMainHeightFull(false);
                //                return getNewProjectForm();
            } else if (state.equals(PATH_NAME_HISTORY)) {
                m_rootLayout.setMainHeightFull(true);
                hideNavButtons();
                return new CmsProjectHistoryTable();
                //            } else if (state.startsWith(PATH_NAME_EDIT)) {
                //                CmsUUID projectId = getIdFromState(state);
                //                if (projectId != null) {
                //                    m_rootLayout.setMainHeightFull(false);
                //                    return new CmsEditProjectForm(this, projectId);
                //                }
            } else if (state.startsWith(PATH_NAME_FILES)) {
                hideNavButtons();
                CmsUUID projectId = getIdFromState(state);
                if (projectId != null) {
                    return prepareProjectFilesTable(projectId);
                }
            }
        }
        return null;
    }

    /**
     * Returns the project files table.<p>
     *
     * @param projectId the selected project id
     *
     * @return the file table
     */
    protected CmsFileTable getProjectFiles(CmsUUID projectId) {

        final CmsFileTable fileTable = new CmsFileTable(null);
        fileTable.applyWorkplaceAppSettings();
        fileTable.setContextProvider(new I_CmsContextProvider() {

            /**
             * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
             */
            public I_CmsDialogContext getDialogContext() {

                CmsFileTableDialogContext context = new CmsFileTableDialogContext(
                    CmsProjectManagerConfiguration.APP_ID,
                    ContextType.fileTable,
                    fileTable,
                    fileTable.getSelectedResources());
                context.setEditableProperties(CmsFileExplorer.INLINE_EDIT_PROPERTIES);
                return context;
            }
        });
        CmsObject cms = A_CmsUI.getCmsObject();
        List<CmsResource> childResources;
        try {
            childResources = cms.readProjectView(projectId, CmsResource.STATE_KEEP);
            fileTable.fillTable(cms, childResources);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(
                CmsVaadinUtils.getMessageText(Messages.ERR_PROJECTS_CAN_NOT_DISPLAY_FILES_0),
                e);
        }
        return fileTable;
    }

    //    /**
    //     * Returns the new project form component.<p>
    //     *
    //     * @return the form component
    //     */
    //    protected Component getNewProjectForm() {
    //
    //        return new CmsEditProjectForm(this);
    //    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Returns the project id parameter from the given state.<p>
     *
     * @param state the state
     *
     * @return the project id
     */
    private CmsUUID getIdFromState(String state) {

        CmsUUID result = null;
        String temp = A_CmsWorkplaceApp.getParamFromState(state, "projectId");
        if (CmsUUID.isValidUUID(temp)) {
            result = new CmsUUID(temp);
        }
        return result;
    }

    /**
     * Hides the sub navigation buttons.<p>
     */
    private void hideNavButtons() {

        for (Component button : m_navButtons) {
            button.setVisible(false);
        }
    }

    /**
     * Prepares the view to show the project files table.<p>
     *
     * @param projectId the project id
     *
     * @return the project file table
     */
    private CmsFileTable prepareProjectFilesTable(CmsUUID projectId) {

        m_rootLayout.setMainHeightFull(true);
        final CmsFileTable fileTable = getProjectFiles(projectId);
        m_fileTableFilter = new TextField();
        m_fileTableFilter.setIcon(FontOpenCms.FILTER);
        m_fileTableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_fileTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_fileTableFilter.setWidth("200px");
        m_fileTableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                fileTable.filterTable(event.getText());

            }
        });
        m_infoLayout.addComponent(m_fileTableFilter);
        return fileTable;
    }

    /**
     * Shows the sub navigation buttons.<p>
     */
    private void showNavButtons() {

        if (m_navButtons.isEmpty()) {
            Button addProject = CmsToolBar.createButton(
                FontOpenCms.WAND,
                CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_ADD_0));
            addProject.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
                    CmsEditProjectForm form = new CmsEditProjectForm(m_projectsTable, window);
                    window.setContent(form);
                    window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_ADD_0));
                    A_CmsUI.get().addWindow(window);
                    window.center();
                }
            });
            m_uiContext.addToolbarButton(addProject);
            m_navButtons.add(addProject);
            Button history = CmsToolBar.createButton(
                FontOpenCms.CLIPBOARD,
                CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_HISTORY_0));
            history.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    openSubView(CmsProjectManager.PATH_NAME_HISTORY, true);
                }
            });
            m_uiContext.addToolbarButton(history);
            m_navButtons.add(history);
            try {
                Map<String, String> infos = new LinkedHashMap<String, String>();
                infos.put(
                    CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_STATISTICS_COUNT_0),
                    String.valueOf(
                        OpenCms.getOrgUnitManager().getAllManageableProjects(A_CmsUI.getCmsObject(), "", true).size()));
                CmsInfoButton infoButton = new CmsInfoButton(infos);
                infoButton.setWindowCaption(CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_STATISTICS_0));
                infoButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_STATISTICS_0));
                m_uiContext.addToolbarButton(infoButton);
            } catch (CmsException e) {
                LOG.error("unable to get orgunit manager", e);
            }
        } else {
            for (Component button : m_navButtons) {
                button.setVisible(true);
            }
        }
    }
}

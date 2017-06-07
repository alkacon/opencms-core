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
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The project manager app.<p>
 */
public class CmsProjectManager extends A_CmsWorkplaceApp {

    /** The small project icon path. */
    public static final String ICON_PROJECT_SMALL = "apps/project_fileicon.png";

    /** The add project path name. */
    public static final String PATH_NAME_ADD = "add";

    /** The edit project path name. */
    public static final String PATH_NAME_EDIT = "edit";

    /** The project files path name. */
    public static final String PATH_NAME_FILES = "files";

    /** The project history path name. */
    public static final String PATH_NAME_HISTORY = "history";

    /** The add project icon path. */
    private static final String ICON_ADD = "apps/project_add.png";

    /** The project history icon path. */
    private static final String ICON_HISTORY = "apps/project_history.png";

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsProjectManager.class.getName());

    /** The file table filter input. */
    private TextField m_fileTableFilter;

    /** The project table filter input. */
    private TextField m_projectTableFilter;

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_MANAGER_TITLE_0));
        } else if (state.equals(PATH_NAME_ADD)) {
            crumbs.put(
                CmsProjectManagerConfiguration.APP_ID,
                CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_MANAGER_TITLE_0));
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_ADD_0));
        } else if (state.equals(PATH_NAME_HISTORY)) {
            crumbs.put(
                CmsProjectManagerConfiguration.APP_ID,
                CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_MANAGER_TITLE_0));
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_HISTORY_0));
        } else if (state.startsWith(PATH_NAME_EDIT)) {
            CmsUUID projectId = getIdFromState(state);
            if (projectId != null) {
                crumbs.put(
                    CmsProjectManagerConfiguration.APP_ID,
                    CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_MANAGER_TITLE_0));
                try {
                    crumbs.put(
                        "",
                        CmsVaadinUtils.getMessageText(
                            Messages.GUI_PROJECTS_EDIT_1,
                            A_CmsUI.getCmsObject().readProject(projectId).getName()));
                } catch (CmsException e) {
                    LOG.error("Error reading project for bread crumb.", e);
                }
            }
        } else if (state.startsWith(PATH_NAME_FILES)) {
            CmsUUID projectId = getIdFromState(state);
            if (projectId != null) {
                crumbs.put(
                    CmsProjectManagerConfiguration.APP_ID,
                    CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_MANAGER_TITLE_0));
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
            final CmsProjectsTable table = getProjectsTable();
            m_projectTableFilter = new TextField();
            m_projectTableFilter.setIcon(FontOpenCms.FILTER);
            m_projectTableFilter.setInputPrompt(
                Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
            m_projectTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
            m_projectTableFilter.setWidth("200px");
            m_projectTableFilter.addTextChangeListener(new TextChangeListener() {

                private static final long serialVersionUID = 1L;

                public void textChange(TextChangeEvent event) {

                    table.filterTable(event.getText());

                }
            });
            m_infoLayout.addComponent(m_projectTableFilter);
            return table;
        } else if (state.equals(PATH_NAME_ADD)) {
            m_rootLayout.setMainHeightFull(false);
            return getNewProjectForm();
        } else if (state.equals(PATH_NAME_HISTORY)) {
            m_rootLayout.setMainHeightFull(true);
            return new CmsProjectHistoryTable();
        } else if (state.startsWith(PATH_NAME_EDIT)) {
            CmsUUID projectId = getIdFromState(state);
            if (projectId != null) {
                m_rootLayout.setMainHeightFull(false);
                return new CmsEditProjectForm(this, projectId);
            }
        } else if (state.startsWith(PATH_NAME_FILES)) {
            CmsUUID projectId = getIdFromState(state);
            if (projectId != null) {
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
        }

        return null;
    }

    /**
     * Returns the new project form component.<p>
     *
     * @return the form component
     */
    protected Component getNewProjectForm() {

        return new CmsEditProjectForm(this);
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

    /**
     * Returns the projects table component.<p>
     *
     * @return the projects table
     */
    protected CmsProjectsTable getProjectsTable() {

        CmsProjectsTable table = new CmsProjectsTable(this);
        table.loadProjects();
        return table;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            List<NavEntry> subNav = new ArrayList<NavEntry>();
            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_ADD_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_ADD_DESCRIPTION_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_ADD)),
                    PATH_NAME_ADD));
            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_HISTORY_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_HISTORY_DESCRIPTION_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_HISTORY)),
                    PATH_NAME_HISTORY));
            return subNav;
        }
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
}

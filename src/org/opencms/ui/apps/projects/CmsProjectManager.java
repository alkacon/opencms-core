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

package org.opencms.ui.apps.projects;

import org.opencms.main.CmsException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsProjectManagerConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;

/**
 * The project manager app.<p>
 */
public class CmsProjectManager extends A_CmsWorkplaceApp {

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

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected Map<String, String> getBreadCrumbForState(String state) {

        Map<String, String> crumbs = new LinkedHashMap<String, String>();
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            return getProjectsTable();
        } else if (state.equals(PATH_NAME_ADD)) {
            return getNewProjectForm();
        } else if (state.equals(PATH_NAME_HISTORY)) {
            return new CmsProjectHistoryTable();
        } else if (state.startsWith(PATH_NAME_EDIT)) {
            CmsUUID projectId = getIdFromState(state);
            if (projectId != null) {
                return new CmsEditProjectForm(this, projectId);
            }
        } else if (state.startsWith(PATH_NAME_FILES)) {
            CmsUUID projectId = getIdFromState(state);
            if (projectId != null) {
                return new CmsProjectFiles(projectId);
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
     * Returns the projects table component.<p>
     *
     * @return the projects table
     */
    protected Component getProjectsTable() {

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

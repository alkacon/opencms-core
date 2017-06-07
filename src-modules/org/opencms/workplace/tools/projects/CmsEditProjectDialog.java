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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.projects;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsOrgUnitWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new and existing project in the administration view.<p>
 *
 * @since 6.0.0
 */
public class CmsEditProjectDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "project";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the project id. */
    public static final String PARAM_PROJECTID = "projectid";

    /** Request parameter name for the project name. */
    public static final String PARAM_PROJECTNAME = "projectname";

    /** The project object that is edited on this dialog. */
    protected CmsProject m_project;

    /** Stores the value of the request parameter for the project id. */
    private String m_paramProjectid;

    /** Auxiliary Property for better representation of the bean VFS resources. */
    private List m_resources;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsEditProjectDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditProjectDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited project to the database.<p>
     */
    @Override
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // if new create it first
            if (m_project.getUuid() == null) {
                CmsProject newProject = getCms().createProject(
                    m_project.getName(),
                    m_project.getDescription(),
                    getUserGroup(),
                    getManagerGroup(),
                    m_project.getType());
                m_project = newProject;
            } else {
                getCms().writeProject(m_project);
            }
            // write the edited project resources
            CmsProject currentProject = getCms().getRequestContext().getCurrentProject();
            // change the current project
            getCms().getRequestContext().setCurrentProject(m_project);
            // store the current site root
            String currentSite = getCms().getRequestContext().getSiteRoot();
            // copy the resources to the current project
            try {
                // switch to the root site
                getCms().getRequestContext().setSiteRoot("");

                // remove deleted resources
                Iterator itDel = getCms().readProjectResources(m_project).iterator();
                while (itDel.hasNext()) {
                    String resName = itDel.next().toString();
                    if (!getResources().contains(resName)) {
                        getCms().removeResourceFromProject(resName);
                    }
                }
                // read project resources again!
                List currentResNames = getCms().readProjectResources(m_project);
                // copy missing resources
                Iterator itAdd = getResources().iterator();
                while (itAdd.hasNext()) {
                    String resName = itAdd.next().toString();
                    if (!currentResNames.contains(resName)) {
                        getCms().copyResourceToProject(resName);
                    }
                }
            } finally {
                // switch back to current site and project
                getCms().getRequestContext().setSiteRoot(currentSite);
                getCms().getRequestContext().setCurrentProject(currentProject);
            }
            // refresh the list
            Map objects = (Map)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsProjectsList.class.getName());
            }
        } catch (Throwable t) {
            errors.add(t);
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the description of the parent ou.<p>
     *
     * @return the description of the parent ou
     */
    public String getAssignedOu() {

        try {
            CmsOrganizationalUnit ou;
            if (!isNewProject()) {
                ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), m_project.getOuFqn());
            } else {
                ou = OpenCms.getRoleManager().getOrgUnitsForRole(getCms(), CmsRole.PROJECT_MANAGER, true).get(0);
            }
            return ou.getDisplayName(getLocale());
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * Returns the manager Group name.<p>
     *
     * @return the manager Group name
     */
    public String getManagerGroup() {

        try {
            return getCms().readGroup(m_project.getManagerGroupId()).getName();
        } catch (Throwable e) {
            return "";
        }
    }

    /**
     * Returns the simple name of the project.<p>
     *
     * @return the simple name of the project
     */
    public String getName() {

        String name = m_project.getSimpleName();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name) || name.equals("/")) {
            name = "";
        }
        return name;
    }

    /**
     * Returns the fully qualified name of the organizational unit.<p>
     *
     * @return the fully qualified name of the organizational unit
     */
    public String getOufqn() {

        return m_project.getOuFqn();
    }

    /**
     * Returns the project id parameter value.<p>
     *
     * @return the project id parameter value
     */
    public String getParamProjectid() {

        return m_paramProjectid;
    }

    /**
     * Returns the list of VFS resources that belong to this project.<p>
     *
     * @return the list of VFS resources that belong to this project
     */
    public List getResources() {

        if (m_resources == null) {
            return new ArrayList();
        }
        return m_resources;
    }

    /**
     * Returns the user Group name.<p>
     *
     * @return the user Group name
     */
    public String getUserGroup() {

        try {
            return getCms().readGroup(m_project.getGroupId()).getName();
        } catch (Throwable e) {
            return "";
        }
    }

    /**
     * Just a setter method needed for the widget dialog.<p>
     *
     * @param ou ignored
     */
    public void setAssignedOu(String ou) {

        ou.length(); // prevent warning
    }

    /**
     * Sets the manager Group name.<p>
     *
     * @param managerGroup the manager Group name to set
     */
    public void setManagerGroup(String managerGroup) {

        CmsGroup group = checkGroup(managerGroup);
        if (group != null) {
            m_project.setManagerGroupId(group.getId());
        }
    }

    /**
     * Sets the name of the project.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        String oufqn = getOufqn();
        if (oufqn != null) {
            if (!oufqn.endsWith("/")) {
                oufqn += "/";
            }
        } else {
            oufqn = "/";
        }
        m_project.setName(oufqn + name);

    }

    /**
     * Sets the fully qualified name of the organizational unit.<p>
     *
     * @param oufqn the fully qualified name of the organizational unit to set
     */
    public void setOufqn(String oufqn) {

        if (oufqn != null) {
            if (!oufqn.endsWith("/")) {
                oufqn += '/';
            }
        } else {
            oufqn = "/";
        }
        String name = m_project.getSimpleName();
        if (name == null) {
            name = "";
        }
        name = oufqn + name;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            name = "/";
        }
        m_project.setName(name);
    }

    /**
     * Sets the project id parameter value.<p>
     *
     * @param projectId the project id parameter value
     */
    public void setParamProjectid(String projectId) {

        m_paramProjectid = projectId;
    }

    /**
     * Sets the resources of this project.<p>
     *
     * @param value the project resources to set
     */
    public void setResources(List value) {

        if (value == null) {
            m_resources = new ArrayList();
            return;
        }
        m_resources = CmsFileUtil.removeRedundancies(value);
    }

    /**
     * Sets the user Group name.<p>
     *
     * @param userGroup the user Group name to set
     */
    public void setUserGroup(String userGroup) {

        CmsGroup group = checkGroup(userGroup);
        if (group != null) {
            m_project.setGroupId(group.getId());
        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_PROJECT_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 5));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_PROJECT_EDITOR_LABEL_CONTENT_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(6, 6));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        // initialize the project object to use for the dialog
        initProjectObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        if (isNewProject()) {
            addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_project, "description", "", PAGES[0], new CmsTextareaWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(this, "managerGroup", PAGES[0], new CmsGroupWidget(null, null)));
        addWidget(new CmsWidgetDialogParameter(this, "userGroup", PAGES[0], new CmsGroupWidget(null, null)));
        if (isNewProject()) {
            int ous = 1;
            try {
                ous = OpenCms.getRoleManager().getOrgUnitsForRole(getCms(), CmsRole.PROJECT_MANAGER, true).size();
            } catch (CmsException e) {
                // should never happen
            }
            if (ous < 2) {
                addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
            } else {
                addWidget(
                    new CmsWidgetDialogParameter(
                        this,
                        "oufqn",
                        PAGES[0],
                        new CmsOrgUnitWidget(CmsRole.PROJECT_MANAGER)));
            }
        } else {
            addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_project, "deleteAfterPublishing", PAGES[0], new CmsCheckboxWidget()));
        addWidget(
            new CmsWidgetDialogParameter(this, "resources", PAGES[0], new CmsVfsFileWidget(false, "", false, false)));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the project object to work with depending on the dialog state and request parameters.<p>
     *
     * Two initializations of the project object on first dialog call are possible:
     * <ul>
     * <li>edit an existing project</li>
     * <li>create a new project</li>
     * </ul>
     */
    protected void initProjectObject() {

        Object o = null;

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // edit an existing project, get the project object from database
                m_project = getCms().readProject(new CmsUUID(getParamProjectid()));
                m_resources = getCms().readProjectResources(m_project);
            } else {
                // this is not the initial call, get the project object from session
                o = getDialogObject();
                Map map = (Map)o;
                m_project = (CmsProject)map.get("prj");
                // test
                m_project.getUuid();
                m_resources = (List)map.get("res");
                m_resources.isEmpty();
            }
        } catch (Exception e) {
            // create a new project object
            m_project = new CmsProject();
            m_resources = new ArrayList();
            setOufqn(getCms().getRequestContext().getOuFqn());
            setUserGroup(getCms().getRequestContext().getOuFqn() + OpenCms.getDefaultUsers().getGroupUsers());
        }
    }

    /**
     * Overridden to set a custom online help mapping.<p>
     *
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceMembers(org.opencms.jsp.CmsJspActionElement)
     */
    @Override
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        super.initWorkplaceMembers(jsp);
        setOnlineHelpUriCustom("/projects/project_edit.jsp");
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        Map map = new HashMap();
        map.put("prj", m_project);
        map.put("res", m_resources);
        // save the current state of the project (may be changed because of the widget values)
        setDialogObject(map);
    }

    /**
     * Checks if the Project overview has to be displayed.<p>
     *
     * @return <code>true</code> if the project overview has to be displayed
     */
    protected boolean isNewProject() {

        return getCurrentToolPath().equals("/projects/new");
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if (!isNewProject()) {
            // test the needed parameters
            getCms().readProject(new CmsUUID(getParamProjectid())).getName();
        }

    }

    /**
     * Checks if the given group name is a valid opencms user group.<p>
     *
     * @param groupName the group name to check
     *
     * @return the read group
     */
    private CmsGroup checkGroup(String groupName) {

        try {
            return getCms().readGroup(groupName);
        } catch (CmsException e) {
            throw new CmsIllegalArgumentException(e.getMessageContainer());
        }
    }
}
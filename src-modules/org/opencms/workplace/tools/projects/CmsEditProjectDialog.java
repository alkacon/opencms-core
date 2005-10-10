/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsEditProjectDialog.java,v $
 * Date   : $Date: 2005/10/10 10:53:19 $
 * Version: $Revision: 1.13.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new and existing project in the administration view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.13.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsEditProjectDialog extends A_CmsProjectDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "project";

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
     * Overridden to set a custom online help mapping.<p>
     * 
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceMembers(org.opencms.jsp.CmsJspActionElement)
     */
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        super.initWorkplaceMembers(jsp);
        setOnlineHelpUriCustom("/projects/project_edit.html");
    }

    /**
     * Commits the edited project to the db.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // if new create it first
            if (m_project.getId() == 0) {
                CmsProject newProject = getCms().createProject(
                    m_project.getName(),
                    m_project.getDescription(),
                    this.getUserGroup(),
                    this.getManagerGroup(),
                    m_project.getType());
                m_project = newProject;
                // write the edited project
                CmsProject currentProject = getCms().getRequestContext().currentProject();
                // change the current project
                getCms().getRequestContext().setCurrentProject(m_project);
                // store the current site root
                String currentSite = getCms().getRequestContext().getSiteRoot();
                // copy the resources to the current project               
                try {
                    // switch to the root site
                    getCms().getRequestContext().setSiteRoot("");
                    // start copying
                    Iterator it = this.getResources().iterator();
                    while (it.hasNext()) {
                        getCms().copyResourceToProject(it.next().toString());
                    }
                } catch (CmsException e) {
                    // if there are no projectresources in the project delete the project
                    List projectResources = getCms().readProjectResources(m_project);
                    if (projectResources == null || projectResources.isEmpty()) {
                        getCms().deleteProject(m_project.getId());
                    }
                    throw e;
                } finally {
                    // switch back to current site and project
                    getCms().getRequestContext().setSiteRoot(currentSite);
                    getCms().getRequestContext().setCurrentProject(currentProject);
                }
            } else {
                m_project.setGroupId(getCms().readGroup(getUserGroup()).getId());
                m_project.setManagerGroupId(getCms().readGroup(getManagerGroup()).getId());
                getCms().writeProject(m_project);
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

        return m_resources;
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
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_PROJECT_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 4));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            if (isNewProject()) {
                result.append(dialogBlockStart(key(Messages.GUI_PROJECT_EDITOR_LABEL_CONTENT_BLOCK_0)));
                result.append(createWidgetTableStart());
                result.append(createDialogRowsHtml(5, 5));
                result.append(createWidgetTableEnd());
                result.append(dialogBlockEnd());
            }
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        // initialize the project object to use for the dialog
        initProjectObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        if (isNewProject()) {
            addWidget(new CmsWidgetDialogParameter(m_project, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_project, "name", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_project, "description", "", PAGES[0], new CmsTextareaWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(this, "managerGroup", PAGES[0], new CmsSelectWidget(
            getSelectGroups(true))));
        addWidget(new CmsWidgetDialogParameter(this, "userGroup", PAGES[0], new CmsSelectWidget(getSelectGroups(false))));
        addWidget(new CmsWidgetDialogParameter(m_project, "deleteAfterPublishing", PAGES[0], new CmsCheckboxWidget()));
        if (isNewProject()) {
            addWidget(new CmsWidgetDialogParameter(this, "resources", PAGES[0], new CmsVfsFileWidget(false, "")));
        }
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
                // edit an existing project, get the project object from db
                m_project = getCms().readProject(new Integer(getParamProjectid()).intValue());
            } else {
                // this is not the initial call, get the project object from session            
                o = getDialogObject();
                m_project = (CmsProject)o;
                // test
                m_project.getId();
            }
        } catch (Exception e) {
            // create a new project object
            m_project = new CmsProject();
        }
        try {
            CmsUserSettings settings = new CmsUserSettings(getCms());
            m_project.setDeleteAfterPublishing(settings.getProjectSettings().isDeleteAfterPublishing());
        } catch (Exception e) {
            // ignore
        }
        try {
            setManagerGroup(getCms().readManagerGroup(m_project).getName());
        } catch (Exception e) {
            // ignore
        }
        try {
            setUserGroup(getCms().readGroup(m_project).getName());
        } catch (Exception e) {
            // ignore
        }
        try {
            setResources(getCms().readProjectResources(m_project));
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the project (may be changed because of the widget values)
        setDialogObject(m_project);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        if (!isNewProject()) {
            // test the needed parameters
            getCms().readProject(new Integer(getParamProjectid()).intValue()).getName();
        }

    }

    /**
     * Checks if the Project overview has to be displayed.<p>
     * 
     * @return <code>true</code> if the project overview has to be displayed
     */
    private boolean isNewProject() {

        return getCurrentToolPath().equals("/projects/new");
    }
}
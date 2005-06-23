/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsEditProjectDialog.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.projects;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
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
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new and existing project in the administration view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsEditProjectDialog extends A_CmsProjectDialog {

    /** localized messages Keys prefix. */
    public static final String C_KEY_PREFIX = "project";

    /** Request parameter name for the project id. */
    public static final String PARAM_PROJECTID = "projectid";

    /** Request parameter name for the project name. */
    public static final String PARAM_PROJECTNAME = "projectname";

    /** The project object that is edited on this dialog. */
    protected CmsProject m_project;

    /** Stores the value of the request parameter for the project id. */
    private String m_paramProjectid;

    /** Stores the value of the request parameter for the project name. */
    private String m_paramProjectname;

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
     * Returns the project name parameter value.<p>
     * 
     * @return the project name parameter value
     */
    public String getParamProjectname() {

        return m_paramProjectname;
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
     * Sets the project name parameter value.<p>
     * 
     * @param projectName the project name parameter value
     */
    public void setParamProjectname(String projectName) {

        m_paramProjectname = projectName;
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
        checkRedundancies(value);
        m_resources = value;
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

        setKeyPrefix(C_KEY_PREFIX);

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
            CmsUserSettings settings = new CmsUserSettings(getCms().getRequestContext().currentUser());
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

        if (!isNewProject()) {
            // test the needed parameters
            try {
                getCms().readProject(getParamProjectname());
                getCms().readProject(new Integer(getParamProjectid()).intValue());
            } catch (Exception e) {
                // redirect to parent if parameters not available
                setAction(ACTION_CANCEL);
                try {
                    actionCloseDialog();
                } catch (JspException e1) {
                    // noop
                }
                return;
            }
        }

        // save the current state of the project (may be changed because of the widget values)
        setDialogObject(m_project);
    }

    /** 
     *  Check whether some of the resources are redundant because a superfolder has also
     *  been selected.<p>
     * 
     *  The resources list will be modified so that the paths are disjoint.<p>
     *
     *  @param resources a list of full pathnames for all the resources
     */
    private void checkRedundancies(List resources) {

        if (resources == null) {
            return;
        }
        List redundant = new ArrayList();
        int n = resources.size();
        if (n < 2) {
            return;
        }
        for (int i = 0; i < n; i++) {
            redundant.add(new Boolean(false));
        }
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (((String)resources.get(i)).length() < ((String)resources.get(j)).length()) {
                    if (((String)resources.get(j)).startsWith((String)resources.get(i))) {
                        redundant.set(j, new Boolean(true));
                    }
                } else {
                    if (((String)resources.get(i)).startsWith((String)resources.get(j))) {
                        redundant.set(i, new Boolean(true));
                    }
                }
            }
        }
        for (int i = n - 1; i >= 0; i--) {
            if (((Boolean)redundant.get(i)).booleanValue()) {
                resources.remove(i);
            }
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
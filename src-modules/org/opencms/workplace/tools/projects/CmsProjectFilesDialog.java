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

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListDropdownAction;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListResourceCollector;
import org.opencms.workplace.tools.CmsToolDialog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Explorer dialog for the project files view.<p>
 *
 * @since 6.0.0
 */
public class CmsProjectFilesDialog extends A_CmsListExplorerDialog {

    /** list independent action constant. */
    public static final String LIST_IACTION_FILTER = "iaf";

    /** list id constant. */
    public static final String LIST_ID = "lpr";

    /** Session attribute key for the stored project. */
    public static final String SESSION_STORED_PROJECT = "CmsProjectFilesDialog_storedProject";

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /** Stores the value of the request parameter for the resource filter. */
    private String m_filter;

    /** Stores the value of the request parameter for the project id. */
    private String m_paramProjectid;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsProjectFilesDialog(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_PROJECT_FILES_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsProjectFilesDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListIndepActions()
     */
    @Override
    public void executeListIndepActions() {

        if (getParamListAction().equals(LIST_IACTION_FILTER)) {
            // forward to the editor
            getList().setCurrentPage(1);
            m_collector = null;
            refreshList();
        } else {
            super.executeListIndepActions();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    @Override
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            CmsUUID projectId = getProject().getUuid();
            CmsResourceState state = CmsResource.STATE_KEEP;
            CmsHtmlList list = getList();
            if (list != null) {
                if (getSettings().getCollector() != null) {
                    getSettings().setCollector(null);
                }
            }
            if (m_filter.equals("new")) {
                state = CmsResource.STATE_NEW;
            } else if (m_filter.equals("changed")) {
                state = CmsResource.STATE_CHANGED;
            } else if (m_filter.equals("deleted")) {
                state = CmsResource.STATE_DELETED;
            }
            m_collector = new CmsProjectFilesCollector(this, projectId, state);
        }
        return m_collector;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getList()
     */
    @Override
    public CmsHtmlList getList() {

        CmsHtmlList list = super.getList();
        // get parameter
        m_filter = getJsp().getRequest().getParameter(LIST_IACTION_FILTER + CmsListDropdownAction.SUFFIX_PARAM);
        CmsListDropdownAction listAction = null;
        if (list != null) {
            listAction = ((CmsListDropdownAction)list.getMetadata().getIndependentAction(LIST_IACTION_FILTER));
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_filter)) {
                // if no param, get old value
                m_filter = listAction.getSelection();
            }
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_filter)) {
            m_filter = CmsProjectResourcesDisplayMode.ALL_CHANGES.getMode();
        }
        if (listAction != null) {
            listAction.setSelection(m_filter);
        }
        return list;
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
     * @see org.opencms.workplace.list.A_CmsListDialog#refreshList()
     */
    @Override
    public synchronized void refreshList() {

        if (LIST_IACTION_FILTER.equals(getParamListAction())) {
            if (m_collector != null) {
                // refresh only if really necessary
                return;
            }
        }
        super.refreshList();
    }

    /**
     * Sets the project id parameter value.<p>
     *
     * @param projectId the project id parameter value
     */
    public void setParamProjectid(String projectId) {

        m_paramProjectid = projectId;
        getJsp().getRequest().getSession().setAttribute("LASTPRJ", projectId);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // no-details
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getAdditionalParametersForExplorerForward()
     */
    @Override
    protected java.util.Map<String, String[]> getAdditionalParametersForExplorerForward() {

        Map<String, String[]> result = new HashMap<String, String[]>();
        result.put(
            CmsExplorer.PARAMETER_CONTEXTMENUPARAMS,
            new String[] {CmsToolDialog.PARAM_ADMIN_PROJECT + "=" + m_paramProjectid});
        return result;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getProject()
     */
    @Override
    protected CmsProject getProject() {

        CmsUUID projectId = new CmsUUID(getParamProjectid());
        try {
            return getCms().readProject(projectId);
        } catch (CmsException e) {
            return super.getProject();
        }
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
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        CmsListDropdownAction filterAction = new CmsListDropdownAction(LIST_IACTION_FILTER);
        filterAction.setName(Messages.get().container(Messages.GUI_PROJECT_FILES_FILTER_ACTION_NAME_0));
        filterAction.setHelpText(Messages.get().container(Messages.GUI_PROJECT_FILES_FILTER_ACTION_HELP_0));
        Iterator<?> it = CmsProjectResourcesDisplayMode.VALUES.iterator();
        while (it.hasNext()) {
            CmsProjectResourcesDisplayMode mode = (CmsProjectResourcesDisplayMode)it.next();
            filterAction.addItem(mode.getMode(), Messages.get().container(A_CmsWidget.LABEL_PREFIX + mode.getMode()));
        }
        metadata.addIndependentAction(filterAction);
        super.setIndependentActions(metadata);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no LMAs
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        try {
            getCms().readProject(new CmsUUID(getParamProjectid()));
            setStoredProject(getParamProjectid()); // doing this after the readProject call because now we know the id is valid
        } catch (Exception e) {
            Exception exceptionToRethrow = e;
            String storedProject = getStoredProject();
            boolean usingStoredProject = false;
            if (storedProject != null) {
                try {
                    getCms().readProject(new CmsUUID(storedProject));
                    m_paramProjectid = storedProject;
                    usingStoredProject = true;
                } catch (Exception e2) {
                    exceptionToRethrow = e2;
                }
            }
            if (!usingStoredProject) {
                if (!getCms().getRequestContext().getCurrentProject().isOnlineProject()) {
                    m_paramProjectid = getCms().getRequestContext().getCurrentProject().getUuid().toString();
                } else {
                    throw exceptionToRethrow;
                }
            }
        }
    }

    /**
     * Gets the stored project id from the session.<p>
     *
     * @return the stored project id
     */
    private String getStoredProject() {

        return (String)getSession().getAttribute(SESSION_STORED_PROJECT);
    }

    /**
     * Sets the stored project id.<p>
     *
     * @param project the project id to be stored
     */
    private void setStoredProject(String project) {

        getSession().setAttribute(SESSION_STORED_PROJECT, project);
    }
}

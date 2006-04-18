/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsProjectFilesDialog.java,v $
 * Date   : $Date: 2006/04/18 16:14:03 $
 * Version: $Revision: 1.17.4.1 $
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

import org.opencms.db.CmsProjectResourcesDisplayMode;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Explorer dialog for the project files view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.17.4.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsProjectFilesDialog extends A_CmsListExplorerDialog {

    /** list independent action constant. */
    public static final String LIST_IACTION_FILTER = "if";

    /** list id constant. */
    public static final String LIST_ID = "lpr";

    /** Request parameter name for the show explorer flag. */
    public static final String PARAM_SHOW_EXPLORER = "showexplorer";

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /** Stores the value of the request parameter for the project id. */
    private String m_paramProjectid;

    /** Stores the value of the request parameter for the show explorer flag. */
    private String m_paramShowexplorer;

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
    public void executeListIndepActions() {

        if (getParamListAction().equals(LIST_IACTION_FILTER)) {
            // forward to the editor
            CmsProjectFilterIAction filterAction = (CmsProjectFilterIAction)getList().getMetadata().getIndependentAction(
                LIST_IACTION_FILTER);
            filterAction.toggle();
            refreshList();
            m_collector = null;
        } else {
            super.executeListIndepActions();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            int projectId = new Integer(getProject().getId()).intValue();
            int state = CmsResource.STATE_KEEP;
            CmsHtmlList list = getList();
            if (list != null && list == null) { // TODO: check this
                CmsProjectFilterIAction filterAction = (CmsProjectFilterIAction)list.getMetadata().getIndependentAction(
                    LIST_IACTION_FILTER);
                if (getSettings().getCollector() != null) {
                    getSettings().setCollector(null);
                    filterAction.setFilter(CmsProjectResourcesDisplayMode.ALL_CHANGES);
                }
                if (filterAction.getFilter().getMode().equals("new")) {
                    state = CmsResource.STATE_NEW;
                } else if (filterAction.getFilter().getMode().equals("changed")) {
                    state = CmsResource.STATE_CHANGED;
                } else if (filterAction.getFilter().getMode().equals("deleted")) {
                    state = CmsResource.STATE_DELETED;
                }
            }
            m_collector = new CmsProjectFilesCollector(this, projectId, state);
        }
        return m_collector;
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
     * Returns the Show explorer parameter value.<p>
     *
     * @return the Show explorer parameter value
     */
    public String getParamShowexplorer() {

        return m_paramShowexplorer;
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
     * Sets the Show explorer parameter value.<p>
     *
     * @param showExplorer the Show explorer parameter value to set
     */
    public void setParamShowexplorer(String showExplorer) {

        m_paramShowexplorer = showExplorer;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // no-details
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getProject()
     */
    protected CmsProject getProject() {

        int projectId = new Integer(getParamProjectid()).intValue();
        try {
            return getCms().readProject(projectId);
        } catch (CmsException e) {
            return super.getProject();
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);

        // this to show first the exlorer view
        if (Boolean.valueOf(getParamShowexplorer()).booleanValue()) {
            int projectId = getProject().getId();
            Map params = new HashMap();
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
            params.putAll(getToolManager().getCurrentTool(this).getHandler().getParameters(this));

            getSettings().setExplorerProjectId(projectId);
            getSettings().setExplorerPage(1);
            getSettings().setCollector(getCollector());
            getSettings().setExplorerMode(CmsExplorer.VIEW_LIST);
            try {
                getToolManager().jspForwardPage(this, PATH_DIALOGS + "list-explorer.jsp", params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // TODO: check this
        // metadata.addIndependentAction(new CmsProjectFilterIAction(LIST_IACTION_FILTER));
        super.setIndependentActions(metadata);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // no LMAs
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        getCms().readProject(Integer.parseInt(getParamProjectid()));
    }
}

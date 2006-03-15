/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsProjectFilesDialog.java,v $
 * Date   : $Date: 2006/03/15 10:19:56 $
 * Version: $Revision: 1.15.2.3 $
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

import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListResourcesCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Explorer dialog for the project files view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.15.2.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsProjectFilesDialog extends A_CmsListExplorerDialog {

    /** list id constant. */
    public static final String LIST_ID = "lpr";

    /** Request parameter name for the show explorer flag. */
    public static final String PARAM_SHOW_EXPLORER = "showexplorer";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProjectFilesDialog.class);

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
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        int projectId = new Integer(getProject().getId()).intValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_READ_PROJECT_VIEW_START_0));
        }
        List resources = getCms().readProjectView(projectId, CmsResource.STATE_KEEP);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_READ_PROJECT_VIEW_END_1, new Integer(resources.size())));
        }
        return getListItemsFromResources(resources);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_READ_PROJECT_VIEW_START_0));
            }
            List resources;
            try {
                resources = getCms().readProjectView(projectId, CmsResource.STATE_KEEP);
                Iterator itRes = resources.iterator();
                while (itRes.hasNext()) {
                    CmsResource resource = (CmsResource)itRes.next();
                    if (!resource.getRootPath().startsWith(getJsp().getRequestContext().getSiteRoot())
                        && !resource.getRootPath().startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
                        itRes.remove();
                    }
                }
            } catch (CmsException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e);
                }
                resources = new ArrayList();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_READ_PROJECT_VIEW_END_1, new Integer(resources.size())));
            }
            Map params = new HashMap();
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
            params.putAll(getToolManager().getCurrentTool(this).getHandler().getParameters(this));

            getSettings().setExplorerProjectId(projectId);
            getSettings().setExplorerPage(1);
            getSettings().setCollector(new CmsListResourcesCollector(resources));
            getSettings().setExplorerMode(CmsExplorer.VIEW_LIST);
            try {
                getToolManager().jspForwardPage(this, PATH_DIALOGS + "list-explorer.html", params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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

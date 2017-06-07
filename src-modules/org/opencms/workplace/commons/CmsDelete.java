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

package org.opencms.workplace.commons;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialogSelector;
import org.opencms.workplace.CmsMultiDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.I_CmsDialogHandler;
import org.opencms.workplace.list.CmsListExplorerColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the delete resources dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/delete_standard.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsDelete extends CmsMultiDialog implements I_CmsDialogHandler {

    /** Value for the action: delete the resource. */
    public static final int ACTION_DELETE = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "delete";

    /** The log object for this class. */
    public static final Log LOG = CmsLog.getLog(CmsDelete.class);

    /** Request parameter name for the deletesiblings parameter. */
    public static final String PARAM_DELETE_SIBLINGS = "deletesiblings";

    /** The delete dialog URI. */
    public static final String URI_DELETE_DIALOG = PATH_DIALOGS + "delete_standard.jsp";

    /** The delete siblings parameter value. */
    private String m_deleteSiblings;

    /**
     * Default constructor needed for dialog handler implementation.<p>
     */
    public CmsDelete() {

        super(null);
    }

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsDelete(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDelete(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the delete action, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void actionDelete() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            boolean isFolder = false;
            if (!isMultiOperation()) {
                isFolder = getCms().readResource(getParamResource(), CmsResourceFilter.ALL).isFolder();
            }
            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned delete operation was successful
                if (isMultiOperation() || isFolder) {
                    // set request attribute to reload the explorer tree view
                    List<String> folderList = new ArrayList<String>(1);
                    folderList.add(CmsResource.getParentFolder(getResourceList().get(0)));
                    getJsp().getRequest().setAttribute(REQUEST_ATTRIBUTE_RELOADTREE, folderList);
                }
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // prepare common message part
            includeErrorpage(this, e);
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        // not implemented yet

    }

    /**
     * Returns the html for the confirmation message.<p>
     *
     * @return the html for the confirmation message
     */
    public String buildConfirmation() {

        StringBuffer result = new StringBuffer(512);
        boolean isFolder = false;
        if (!isMultiOperation()) {
            try {
                isFolder = getCms().readResource(getParamResource(), CmsResourceFilter.ALL).isFolder();
            } catch (CmsException e) {
                // ignore
            }
        }
        result.append("<div id='conf-msg'>\n");
        if (isMultiOperation() || isFolder || (hasSiblings() && hasCorrectLockstate())) {
            result.append(key(Messages.GUI_DELETE_MULTI_CONFIRMATION_0));
        } else {
            result.append(key(Messages.GUI_DELETE_CONFIRMATION_0));
        }
        result.append("\n</div>\n");
        return result.toString();
    }

    /**
     * Returns the html for the "delete siblings" options when deleting a a resource with siblings.<p>
     *
     * @return the html for the "delete siblings" options
     */
    public String buildDeleteSiblings() {

        StringBuffer result = new StringBuffer(512);
        if (isMultiOperation() || (hasSiblings() && hasCorrectLockstate())) {
            // show only for multi resource operation or if resource has siblings and correct lock state
            CmsResourceDeleteMode defaultMode = Boolean.valueOf(getParamDeleteSiblings()).booleanValue()
            ? CmsResource.DELETE_REMOVE_SIBLINGS
            : CmsResource.DELETE_PRESERVE_SIBLINGS;
            if (!isMultiOperation()) {
                result.append(key(Messages.GUI_DELETE_WARNING_SIBLINGS_0));
                result.append("<p>");
            }
            result.append("<input type=\"radio\" name=\"");
            result.append(PARAM_DELETE_SIBLINGS);
            result.append("radio\" value=\"false\"");
            if (defaultMode == CmsResource.DELETE_PRESERVE_SIBLINGS) {
                result.append(" checked=\"checked\"");
            }
            result.append(" onclick='reloadDialog(false);'>&nbsp;");
            result.append(key(Messages.GUI_DELETE_PRESERVE_SIBLINGS_0));
            result.append("<br>");
            result.append("<input type=\"radio\" name=\"");
            result.append(PARAM_DELETE_SIBLINGS);
            result.append("radio\" value=\"true\"");
            if (defaultMode == CmsResource.DELETE_REMOVE_SIBLINGS) {
                result.append(" checked=\"checked\"");
            }
            result.append(" onclick='reloadDialog(true);'>&nbsp;");
            result.append(key(Messages.GUI_DELETE_ALL_SIBLINGS_0));
            result.append("<p>");
        }
        return result.toString();
    }

    /**
     * Returns html code for the possible broken relations.<p>
     *
     * @return html code for the possible broken relations
     *
     * @throws JspException if dialog actions fail
     * @throws IOException in case of errros forwarding to the required result page
     * @throws ServletException in case of errros forwarding to the required result page
     */
    public String buildReport() throws JspException, ServletException, IOException {

        CmsDeleteBrokenRelationsList list = new CmsDeleteBrokenRelationsList(
            getJsp(),
            getResourceList(),
            Boolean.valueOf(getParamDeleteSiblings()).booleanValue());
        list.actionDialog();

        StringBuffer result = new StringBuffer(512);
        list.getList().setBoxed(false);
        result.append("<input type='hidden' name='result' value='");
        result.append(list.getList().getTotalSize()).append("'>\n");
        result.append(CmsListExplorerColumn.getExplorerStyleDef());
        result.append("<div style='height:150px; overflow: auto;'>\n");
        result.append(list.getList().listHtml());
        result.append("</div>\n");
        return result.toString();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        return CmsParameterConfiguration.EMPTY_PARAMETERS;
    }

    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogHandler()
     */
    @Override
    public String getDialogHandler() {

        return CmsDialogSelector.DIALOG_DELETE;
    }

    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogUri(java.lang.String, CmsJspActionElement)
     */
    @Override
    public String getDialogUri(String resource, CmsJspActionElement jsp) {

        return URI_DELETE_DIALOG;
    }

    /**
     * Returns the value of the boolean option to delete siblings.<p>
     *
     * @return the value of the boolean option to delete siblings as a lower case string
     */
    public String getParamDeleteSiblings() {

        return m_deleteSiblings;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    @Override
    public void initConfiguration() {

        // not implemented yet

    }

    /**
     * Returns <code>true</code> if the current user is allowed
     * to delete the selected resources.<p>
     *
     * @return <code>true</code> if the current user is allowed
     *          to delete the selected resources
     */
    public boolean isCanDelete() {

        return OpenCms.getWorkplaceManager().getDefaultUserSettings().isAllowBrokenRelations()
            || OpenCms.getRoleManager().hasRole(getCms(), CmsRole.VFS_MANAGER);
    }

    /**
     * Sets the value of the boolean option to delete siblings.<p>
     *
     * @param value the value of the boolean option to delete siblings
     */
    public void setParamDeleteSiblings(String value) {

        m_deleteSiblings = value;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamDeleteSiblings())) {
            setParamDeleteSiblings(Boolean.toString(
                getSettings().getUserSettings().getDialogDeleteFileMode() == CmsResource.DELETE_REMOVE_SIBLINGS));
        }
        // check the required permissions to delete the resource
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_DELETE);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for delete dialog
            setDialogTitle(Messages.GUI_DELETE_RESOURCE_1, Messages.GUI_DELETE_MULTI_2);
        }
    }

    /**
     * Performs the resource deletion.<p>
     *
     * @return true, if the resource(s) was/were deleted, otherwise false
     * @throws CmsException if deletion is not successful
     */
    @Override
    protected boolean performDialogOperation() throws CmsException {

        // check if the current resource is a folder for single operation
        boolean isFolder = isOperationOnFolder();
        // on folder deletion or multi operation display "please wait" screen, not for simple file deletion
        if ((isMultiOperation() || isFolder) && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // determine the correct delete option
        CmsResourceDeleteMode deleteOption = Boolean.valueOf(getParamDeleteSiblings()).booleanValue()
        ? CmsResource.DELETE_REMOVE_SIBLINGS
        : CmsResource.DELETE_PRESERVE_SIBLINGS;

        Iterator<String> i = getResourceList().iterator();
        // iterate the resources to delete
        while (i.hasNext()) {
            String resName = i.next();
            try {
                performSingleDeleteOperation(resName, deleteOption);
            } catch (CmsException e) {
                if (isMultiOperation()) {
                    // collect exceptions to create a detailed output
                    addMultiOperationException(e);
                } else {
                    // for single operation, throw the exception immediately
                    throw e;
                }
            }
        }
        // check if exceptions occurred
        checkMultiOperationException(Messages.get(), Messages.ERR_DELETE_MULTI_0);

        return true;
    }

    /**
     * Performs the delete operation for a single VFS resource.<p>
     *
     * @param resource the resource VFS path
     * @param deleteOption the delete option for sibling deletion
     * @throws CmsException if deleting the resource fails
     */
    protected void performSingleDeleteOperation(String resource, CmsResourceDeleteMode deleteOption)
    throws CmsException {

        // lock resource if autolock is enabled
        checkLock(resource);
        // delete the resource
        getCms().deleteResource(resource, deleteOption);
    }

}
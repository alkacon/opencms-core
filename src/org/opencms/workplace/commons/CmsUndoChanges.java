/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsUndoChanges.java,v $
 * Date   : $Date: 2005/10/19 09:55:34 $
 * Version: $Revision: 1.15.2.1 $
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

package org.opencms.workplace.commons;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the undo changes on a resource dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/undochanges.jsp
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.15.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsUndoChanges extends CmsDialog {

    /** Value for the action: undo changes. */
    public static final int ACTION_UNDOCHANGES = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "undochanges";

    /** Request parameter name for the recursive flag.<p> */
    public static final String PARAM_RECURSIVE = "recursive";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUndoChanges.class);

    private CmsResource m_currentResource;
    private String m_paramRecursive;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUndoChanges(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUndoChanges(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the undo changes action, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionUndoChanges() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            if (performUndoChangesOperation()) {
                // if no exception is caused undo changes operation was successful
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // error during deletion, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Creates the "recursive" checkbox for undoing changes to subresources of folders.<p>
     *  
     * @return the String with the checkbox input field or an empty String for folders.
     */
    public String buildCheckRecursive() {

        StringBuffer retValue = new StringBuffer(256);

        CmsResource res = null;
        try {
            res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
        } catch (CmsException e) {
            return "";
        }

        // show the checkbox only for folders
        if (res.isFolder()) {
            retValue.append("<tr>\n\t<td colspan=\"3\" style=\"white-space: nowrap;\" unselectable=\"on\">");
            retValue.append("<input type=\"checkbox\" name=\""
                + PARAM_RECURSIVE
                + "\" value=\"true\">&nbsp;"
                + key(Messages.GUI_UNDO_CHANGES_SUBRESOURCES_0));
            retValue.append("</td>\n</tr>\n");
        }
        return retValue.toString();
    }

    /**
     * Returns the current CmsResource.<p>
     * 
     * @return the CmsResource
     */
    public CmsResource getCurrentResource() {

        return m_currentResource;
    }

    /**
     * Returns the file name without path information of the current resource.<p>
     * 
     * @return the name of the current resource
     */
    public String getFileName() {

        return CmsResource.getName(getParamResource());
    }

    /**
     * Returns the last modified date of the current resource as localized String.<p>
     * 
     * @return the date of last modification
     */
    public String getLastModifiedDate() {

        long dateLong = getCurrentResource().getDateLastModified();
        return getMessages().getDateTime(dateLong);
    }

    /**
     * Returns the user who made the last changes to the current resource.<p>
     * 
     * @return the user who changed the resource
     */
    public String getLastModifiedUser() {

        CmsUUID userId = getCurrentResource().getUserLastModified();
        try {
            return getCms().readUser(userId).getName();
        } catch (CmsException e) {
            return "";
        }
    }

    /**
     * Returns the value of the recursive parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The recursive parameter on folders decides if all subresources
     * of the folder should be unchanged, too.<p>
     * 
     * @return the value of the recursive parameter
     */
    public String getParamRecursive() {

        return m_paramRecursive;
    }

    /**
     * Sets the current CmsResource.<p>
     * 
     * @param res the CmsResource
     */
    public void setCurrentResource(CmsResource res) {

        m_currentResource = res;
    }

    /**
     * Sets the value of the recursive parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamRecursive(String value) {

        m_paramRecursive = value;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        
        // check the required permissions to undo changes of the resource       
        if (! checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }
        
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_UNDOCHANGES);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for undo changes dialog     
            setParamTitle(key(Messages.GUI_UNDO_CHANGES_1, new Object[] {CmsResource.getName(getParamResource())}));
        }

        try {
            setCurrentResource(getCms().readResource(getParamResource(), CmsResourceFilter.ALL));
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

    }

    /**
     * Performs the undo changes operation on a resource.<p>
     * 
     * @return true, if the changes on a resource were undone, otherwise false
     * @throws CmsException if undo changes is not successful
     */
    private boolean performUndoChangesOperation() throws CmsException {

        // on undo changes display "please wait" screen, not for simple file copy
        CmsResource sourceRes = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
        if (sourceRes.isFolder() && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // get the flag if the touch is recursive from request parameter
        boolean touchRecursive = Boolean.valueOf(getParamRecursive()).booleanValue();
        // lock resource if autolock is enabled
        checkLock(getParamResource());
        // undo changes on the resource
        getCms().undoChanges(getParamResource(), touchRecursive);

        return true;
    }

}

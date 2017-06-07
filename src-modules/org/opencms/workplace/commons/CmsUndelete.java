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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.security.CmsPermissionSet;
import org.opencms.workplace.CmsMultiDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the undelete resources dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/undelete.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsUndelete extends CmsMultiDialog {

    /** Value for the action: undelete resource. */
    public static final int ACTION_UNDELETE = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "undelete";

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUndelete(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUndelete(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the undelete action, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void actionUndelete() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned delete operation was successful
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
     * Returns the HTML for the localized undelete confirmation message depending on single or multi operation.<p>
     *
     * @return the HTML for the localized undelete confirmation message
     */
    public String buildConfirmationMessage() {

        if (isMultiOperation()) {
            return key(Messages.GUI_UNDELETE_MULTI_CONFIRMATION_0);
        } else {
            return key(Messages.GUI_UNDELETE_CONFIRMATION_0);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // check the required permissions to undelete the resource
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_UNDELETE);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for delete dialog
            setDialogTitle(Messages.GUI_UNDELETE_RESOURCE_1, Messages.GUI_UNDELETE_MULTI_2);
        }
    }

    /**
     * Performs the resource undeletion.<p>
     *
     * @return true, if the undelete operation is successful, otherwise false
     * @throws CmsException if undeletion is not successful
     */
    @Override
    protected boolean performDialogOperation() throws CmsException {

        // check if the current resource is a folder for single operation
        boolean isFolder = isOperationOnFolder();
        // on folder undelete or multi operation display "please wait" screen, not for simple file undeletion
        if ((isMultiOperation() || isFolder) && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        Iterator<String> i = getResourceList().iterator();
        // iterate the resources to undelete
        while (i.hasNext()) {
            String resName = i.next();
            try {
                // lock resource if autolock is enabled
                checkLock(resName);
                // undelete the resource
                getCms().undeleteResource(resName, true);
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
        checkMultiOperationException(Messages.get(), Messages.ERR_UNDELETE_MULTI_0);
        return true;
    }
}

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

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.workplace.CmsMultiDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the move resources dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/move.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsMove extends CmsMultiDialog {

    /** Value for the action: move resource. */
    public static final int ACTION_MOVE = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "move";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMove.class);

    /** The value for the 'target' parameter. */
    private String m_paramTarget;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsMove(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsMove(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the move action, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void actionMove() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            boolean isFolder = false;
            String source = getResourceList().get(0);
            String target = CmsLinkManager.getAbsoluteUri(getParamTarget(), CmsResource.getParentFolder(source));
            if (!isMultiOperation()) {
                CmsResource resource = getCms().readResource(source, CmsResourceFilter.ALL);
                isFolder = resource.isFolder();
            } else {
                String siteRootFolder = null;
                CmsResource resource;
                try {
                    // check if a site root was added to the target name
                    if (OpenCms.getSiteManager().getSiteRoot(target) != null) {
                        siteRootFolder = getCms().getRequestContext().getSiteRoot();
                        if (siteRootFolder.endsWith("/")) {
                            siteRootFolder = siteRootFolder.substring(0, siteRootFolder.length() - 1);
                        }
                        getCms().getRequestContext().setSiteRoot("/");
                    }
                    resource = getCms().readResource(target, CmsResourceFilter.ALL);
                } finally {
                    if (siteRootFolder != null) {
                        getCms().getRequestContext().setSiteRoot(siteRootFolder);
                    }
                }
                if (!resource.isFolder()) {
                    // no folder selected for multi operation, throw exception
                    throw new CmsVfsException(
                        Messages.get().container(Messages.ERR_MOVE_MULTI_TARGET_NOFOLDER_1, target));
                }
            }
            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned move operation was successful
                if (isMultiOperation() || isFolder) {
                    // set request attribute to reload the explorer tree view
                    List<String> folderList = new ArrayList<String>(2);
                    String sourceParent = CmsResource.getParentFolder(source);
                    folderList.add(sourceParent);
                    try {
                        String targetParent = CmsResource.getParentFolder(target);
                        if (!targetParent.equals(sourceParent)) {
                            // update target folder if its not the same as the source folder
                            folderList.add(targetParent);
                        }
                    } catch (Exception e) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info(e);
                        }
                    }
                    getJsp().getRequest().setAttribute(REQUEST_ATTRIBUTE_RELOADTREE, folderList);
                }
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // error during move, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Returns the current name of the resource without path information.<p>
     *
     * This is used to preset the input text field with the current resource name for single resource operations.<p>
     *
     * @return the current name of the resource without path information
     */
    public String getCurrentResourceName() {

        if (isMultiOperation()) {
            return "";
        }
        String resourceName = CmsResource.getName(getParamResource());
        if (resourceName.endsWith("/")) {
            resourceName = resourceName.substring(0, resourceName.length() - 1);
        }
        return resourceName;
    }

    /**
     * Returns the value of the target parameter,
     * or null if this parameter was not provided.<p>
     *
     * The target parameter selects the target name
     * of the operation.<p>
     *
     * @return the value of the target parameter
     */
    public String getParamTarget() {

        return m_paramTarget;
    }

    /**
     * Sets the value of the target parameter.<p>
     *
     * @param value the value to set
     */
    public void setParamTarget(String value) {

        m_paramTarget = value;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // check the required permissions to rename/move the resource
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_MOVE);
        } else if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for move dialog
            setDialogTitle(Messages.GUI_MOVE_RESOURCE_1, Messages.GUI_MOVE_MULTI_2);
        }
    }

    /**
     * Performs the resource moving.<p>
     *
     * @return true, if the resource was successfully moved, otherwise false
     * @throws CmsException if moving is not successful
     */
    @Override
    protected boolean performDialogOperation() throws CmsException {

        // check if the current resource is a folder for single operation
        boolean isFolder = isOperationOnFolder();
        // on folder move operation display "please wait" screen, not for simple file move operation
        if ((isMultiOperation() || isFolder) && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // get the target name
        String target = getParamTarget();
        if (target == null) {
            target = "";
        }

        String siteRootFolder = null;
        try {
            // check if a site root was added to the target name
            String sitePrefix = "";
            if (OpenCms.getSiteManager().getSiteRoot(target) != null) {
                siteRootFolder = getCms().getRequestContext().getSiteRoot();
                if (siteRootFolder.endsWith("/")) {
                    siteRootFolder = siteRootFolder.substring(0, siteRootFolder.length() - 1);
                }
                sitePrefix = siteRootFolder;
                getCms().getRequestContext().setSiteRoot("/");
            }

            Iterator<String> i = getResourceList().iterator();
            // iterate the resources to move
            while (i.hasNext()) {
                String resName = i.next();
                try {
                    performSingleMoveOperation(resName, target, sitePrefix);
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
            checkMultiOperationException(Messages.get(), Messages.ERR_MOVE_MULTI_0);
        } finally {
            if (siteRootFolder != null) {
                getCms().getRequestContext().setSiteRoot(siteRootFolder);
            }
        }
        return true;
    }

    /**
     * Performs the move operation for a single VFS resource.<p>
     *
     * @param source the source VFS path
     * @param target the target VFS path
     * @param sitePrefix the site prefix
     *
     * @throws CmsException if moving the resource fails
     */
    protected void performSingleMoveOperation(String source, String target, String sitePrefix) throws CmsException {

        // calculate the target name
        String finalTarget = getCms().getRequestContext().getFileTranslator().translateResource(target);
        finalTarget = CmsLinkManager.getAbsoluteUri(finalTarget, CmsResource.getParentFolder(source));

        if (finalTarget.equals(source) || (isMultiOperation() && finalTarget.startsWith(source))) {
            throw new CmsVfsException(Messages.get().container(Messages.ERR_MOVE_ONTO_ITSELF_1, finalTarget));
        }

        try {
            CmsResource res = getCms().readResource(finalTarget, CmsResourceFilter.ALL);
            if (res.isFolder()) {
                // target folder already exists, so we add the current folder name
                if (!finalTarget.endsWith("/")) {
                    finalTarget += "/";
                }
                finalTarget = finalTarget + CmsResource.getName(source);
                if (finalTarget.endsWith("/")) {
                    finalTarget = finalTarget.substring(0, finalTarget.length() - 1);
                }
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // target folder does not already exist, so target name is o.k.
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        // set the target parameter value
        setParamTarget(finalTarget);

        // could not overwrite a resource in a move operation
        if (getCms().existsResource(finalTarget, CmsResourceFilter.ALL)) {
            // throw exception to indicate that the target exists
            throw new CmsVfsResourceAlreadyExistsException(
                Messages.get().container(
                    Messages.ERR_MOVE_FAILED_TARGET_EXISTS_2,
                    source,
                    getJsp().getRequestContext().removeSiteRoot(finalTarget)));
        }

        // lock resource if autolock is enabled
        checkLock(sitePrefix + source);
        // move the resource
        getCms().moveResource(sitePrefix + source, finalTarget);
    }
}

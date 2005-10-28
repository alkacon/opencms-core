/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsMove.java,v $
 * Date   : $Date: 2005/10/28 12:07:36 $
 * Version: $Revision: 1.19.2.1 $
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
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPermissionSet;
import org.opencms.site.CmsSiteManager;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
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
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.19.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsMove extends CmsMultiDialog {

    /** Value for the action: move resource. */
    public static final int ACTION_MOVE = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "move";
    
    /** Request parameter name for the overwrite flag. */
    public static final String PARAM_OVERWRITE = "overwrite";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMove.class);

    private String m_paramOverwrite;
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
        CmsResource resource = null;
        try {
            boolean isFolder = false;
            if (! isMultiOperation()) {
                resource = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
                isFolder = resource.isFolder();
            } else {
                resource = getCms().readResource(getParamTarget(), CmsResourceFilter.ALL);
                if (! resource.isFolder()) {
                    // no folder selected for multi operation, throw exception
                    throw new CmsVfsException(Messages.get().container(Messages.ERR_MOVE_MULTI_TARGET_NOFOLDER_1, getParamTarget()));
                }
            }
            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned move operation was successful
                if (isMultiOperation() || isFolder) {
                    // set request attribute to reload the explorer tree view
                    List folderList = new ArrayList(2);
                    String sourceParent = CmsResource.getParentFolder((String)getResourceList().get(0));
                    String targetParent = CmsResource.getParentFolder(getParamTarget());
                    if (!targetParent.equals(sourceParent)) {
                        // update target folder if its not the same as the source folder
                        folderList.add(targetParent);
                    }
                    folderList.add(sourceParent);
                    getJsp().getRequest().setAttribute(REQUEST_ATTRIBUTE_RELOADTREE, folderList);
                }
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // check if this exception requires a confirmation or error screen
            if (! isMultiOperation() && (e instanceof CmsVfsResourceAlreadyExistsException) && !(resource.isFolder())) {
                // file move but file already exists, now check target file type
                int targetType = -1;
                boolean restoreSiteRoot = false;
                try {
                    if (CmsSiteManager.getSiteRoot(getParamTarget()) != null) {
                        getCms().getRequestContext().saveSiteRoot();
                        getCms().getRequestContext().setSiteRoot("/");
                        restoreSiteRoot = true;
                    }
                    CmsResource targetRes = getCms().readResource(getParamTarget());
                    targetType = targetRes.getTypeId();
                } catch (CmsException e2) {
                    // can usually be ignored
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e2.getLocalizedMessage());
                    }
                } finally {
                    if (restoreSiteRoot) {
                        getCms().getRequestContext().restoreSiteRoot();
                    }
                }
                if (resource.getTypeId() == targetType) {
                    // file type of target is the same as source, show confirmation dialog
                    setParamMessage(CmsStringUtil.escapeHtml(key(Messages.GUI_COPY_CONFIRM_OVERWRITE_2, 
                        new Object[] {getParamResource(), getParamTarget()})));
                    getJsp().include(FILE_DIALOG_SCREEN_CONFIRM);
                } else {
                    // file type is different, create error message
                    includeErrorpage(this, e);
                }
            } else {
                // error during move, show error dialog
                includeErrorpage(this, e);
            }
        }
    }
    
    /**
     * Builds the available options when moving.<p>
     * 
     * @return the HTML code for the options
     */
    public String buildMoveOptions() {

        StringBuffer retValue = new StringBuffer(256);
        if (isMultiOperation()) {
            // show overwrite option for multi resource moving
            retValue.append("<input type=\"checkbox\" name=\"");
            retValue.append(PARAM_OVERWRITE);
            retValue.append("\" value=\"true\"> ");
            retValue.append(key(Messages.GUI_COPY_MULTI_OVERWRITE_0));
            retValue.append("<br>\n");
            retValue.append(dialogSpacer());
        }
        return retValue.toString();
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
     * Returns the value of the overwrite parameter.<p>
     * 
     * @return the value of the overwrite parameter
     */
    public String getParamOverwrite() {

        return m_paramOverwrite;
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
     * Sets the value of the overwrite parameter.<p>
     * 
     * @param paramOverwrite the value of the overwrite parameter
     */
    public void setParamOverwrite(String paramOverwrite) {

        m_paramOverwrite = paramOverwrite;
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
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        
        // check the required permissions to rename/move the resource       
        if (! checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
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
    protected boolean performDialogOperation() throws CmsException {

        // check if the current resource is a folder for single operation
        boolean isFolder = isOperationOnFolder();
        // on folder move operation display "please wait" screen, not for simple file move operation
        if ((isMultiOperation() || isFolder) && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }
        
        // check the overwrite options
        boolean overwrite = Boolean.valueOf(getParamOverwrite()).booleanValue();
        overwrite = ((isMultiOperation() && overwrite) || DIALOG_CONFIRMED.equals(getParamAction()));

        // get the target name
        String target = getParamTarget();
        if (target == null) {
            target = "";
        }

        boolean restoreSiteRoot = false;
        try {
            // check if a site root was added to the target name
            String sitePrefix = "";
            if (CmsSiteManager.getSiteRoot(target) != null) {
                String siteRootFolder = getCms().getRequestContext().getSiteRoot();
                if (siteRootFolder.endsWith("/")) {
                    siteRootFolder = siteRootFolder.substring(0, siteRootFolder.length() - 1);
                }
                sitePrefix = siteRootFolder;
                getCms().getRequestContext().saveSiteRoot();
                getCms().getRequestContext().setSiteRoot("/");
                restoreSiteRoot = true;
            }

            Iterator i = getResourceList().iterator();
            // iterate the resources to move
            while (i.hasNext()) {
                String resName = (String)i.next();
                try {
                    performSingleMoveOperation(resName, target, sitePrefix, overwrite);
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
            // check if exceptions occured
            checkMultiOperationException(Messages.get(), Messages.ERR_MOVE_MULTI_0);
        } finally {
            if (restoreSiteRoot) {
                getCms().getRequestContext().restoreSiteRoot();
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
     * @param overwrite the overwrite flag
     * @throws CmsException if moving the resource fails
     */
    protected void performSingleMoveOperation(String source, String target, String sitePrefix, boolean overwrite)
    throws CmsException {

        // calculate the target name
        target = CmsLinkManager.getAbsoluteUri(target, CmsResource.getParentFolder(source));

        if (target.equals(source) || (isMultiOperation() && target.startsWith(source))) {
            throw new CmsVfsException(Messages.get().container(Messages.ERR_MOVE_ONTO_ITSELF_1, target));
        }

        try {
            CmsResource res = getCms().readResource(target, CmsResourceFilter.ALL);
            if (res.isFolder()) {
                // target folder already exists, so we add the current folder name
                if (! target.endsWith("/")) {
                    target += "/";
                }
                target = target + CmsResource.getName(source);
                if (target.endsWith("/")) {
                    target = target.substring(0, target.length() - 1);
                }
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // target folder does not already exist, so target name is o.k.
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        // set the target parameter value
        setParamTarget(target);
        
        // set the target parameter value
        setParamTarget(target);

        // delete existing target resource if selected or confirmed by the user
        if (getCms().existsResource(target, CmsResourceFilter.ALL)) {
            if (overwrite) {
                checkLock(target);
                getCms().deleteResource(target, CmsResource.DELETE_PRESERVE_SIBLINGS);
            } else {
                // throw exception to indicate that the target exists
                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                    Messages.ERR_MOVE_FAILED_TARGET_EXISTS_2,
                    source,
                    getJsp().getRequestContext().removeSiteRoot(target)));
            }
        }

        // lock resource if autolock is enabled
        checkLock(sitePrefix + source);
        // move the resource
        getCms().moveResource(sitePrefix + source, target);
    }
}

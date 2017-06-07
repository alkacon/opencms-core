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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsUUID;
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
 * Provides methods for the undo changes on a resource dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/undochanges.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsUndoChanges extends CmsMultiDialog {

    /** Value for the action: undo changes. */
    public static final int ACTION_UNDOCHANGES = 100;

    /** Value for the action: check for siblings and warn in case they exist. */
    public static final int ACTION_CHECKSIBLINGS = 101;

    /** Action string constant for the check siblings dialog. */
    public static final String DIALOG_CHECKSIBLINGS = "checksiblings";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "undochanges";

    /** Request parameter name for the recursive flag.<p> */
    public static final String PARAM_RECURSIVE = "recursive";

    /** Request parameter name for the move flag.<p> */
    public static final String PARAM_MOVE = "move";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUndoChanges.class);

    /** The single current resource. */
    private CmsResource m_currentResource;

    /** The undo move operation flag parameter value. */
    private String m_paramMove;

    /** The undo recursively flag parameter value. */
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
     * Returns the original path of given resource, that is the online path for the resource.
     * If it differs from the offline path, the resource has been moved.<p>
     *
     * @param cms the cms context
     * @param resourceName a site relative resource name
     *
     * @return the online path, or <code>null</code> if resource has not been published
     */
    public static String resourceOriginalPath(CmsObject cms, String resourceName) {

        CmsProject proj = cms.getRequestContext().getCurrentProject();
        try {
            CmsResource resource = cms.readResource(resourceName, CmsResourceFilter.ALL);
            String result = cms.getSitePath(resource);
            cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
            result = cms.getSitePath(cms.readResource(resource.getStructureId()));
            // remove '/' if needed
            if (result.charAt(result.length() - 1) == '/') {
                if (resourceName.charAt(resourceName.length() - 1) != '/') {
                    result = result.substring(0, result.length() - 1);
                }
            }
            return result;
        } catch (CmsException e) {
            return null;
        } finally {
            cms.getRequestContext().setCurrentProject(proj);
        }
    }

    /**
     * Performs the check for siblings action and returns false in case of existence.<p>
     *
     * @return true if siblings are found.
     */
    public boolean actionCheckSiblings() {

        List<String> resourceList = getResourceList();
        String resourcePath;
        Iterator<String> itResourcePaths = resourceList.iterator();
        boolean foundSibling = false;
        while (itResourcePaths.hasNext()) {
            resourcePath = itResourcePaths.next();
            try {
                foundSibling = recursiveCheckSiblings(resourcePath);
                if (foundSibling) {
                    break; // shortcut
                }

            } catch (CmsException e) {
                LOG.error(Messages.get().getBundle(getLocale()).key(
                    Messages.ERR_UNDO_CHANGES_1,
                    new String[] {resourcePath}));
            }

        }
        return foundSibling;
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
            boolean isFolder = false;
            String source = getResourceList().get(0);
            if (!isMultiOperation()) {
                CmsResource resource = getCms().readResource(source, CmsResourceFilter.ALL);
                isFolder = resource.isFolder();
            }
            // get the folders to refresh
            List<String> folderList = new ArrayList<String>(1 + getResourceList().size());
            folderList.add(CmsResource.getParentFolder(source));
            Iterator<String> it = getResourceList().iterator();
            while (it.hasNext()) {
                String res = it.next();
                String target = resourceOriginalPath(getCms(), res);
                if ((target != null) && !target.equals(res)) {
                    CmsResource resource = getCms().readResource(res, CmsResourceFilter.ALL);
                    if (resource.isFolder()) {
                        folderList.add(CmsResource.getParentFolder(target));
                    }
                }
            }
            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned move operation was successful
                if (isMultiOperation() || isFolder) {
                    // set request attribute to reload the explorer tree view
                    getJsp().getRequest().setAttribute(REQUEST_ATTRIBUTE_RELOADTREE, folderList);
                }
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
     * Returns the HTML for the undo changes options and detailed output for single resource operations.<p>
     *
     * @return the HTML for the undo changes options
     */
    public String buildDialogOptions() {

        StringBuffer result = new StringBuffer(256);

        boolean isMoved = isOperationOnMovedResource();
        if (!isMultiOperation()) {
            result.append(dialogSpacer());
            result.append(key(
                Messages.GUI_UNDO_LASTMODIFIED_INFO_3,
                new Object[] {getFileName(), getLastModifiedDate(), getLastModifiedUser()}));
            if (isMoved) {
                result.append(dialogSpacer());
                result.append(key(
                    Messages.GUI_UNDO_MOVE_OPERATION_INFO_2,
                    new Object[] {getFileName(), resourceOriginalPath(getCms(), getParamResource())}));
            }
        }
        result.append(dialogSpacer());
        result.append(key(Messages.GUI_UNDO_CONFIRMATION_0));
        if (isMoved || isOperationOnFolder()) {
            // show undo move option if both options are available
            result.append(dialogSpacer());
            result.append("<input type=\"checkbox\" name=\"");
            result.append(PARAM_MOVE);
            result.append("\" value=\"true\" checked='checked'>&nbsp;");
            if (isMultiOperation()) {
                result.append(key(Messages.GUI_UNDO_CHANGES_MOVE_MULTI_SUBRESOURCES_0));
            } else {
                result.append(key(Messages.GUI_UNDO_CHANGES_MOVE_SUBRESOURCES_0));
            }
        } else {
            if (isMoved) {
                result.append(dialogSpacer());
                result.append("<input type=\"hidden\" name=\"");
                result.append(PARAM_MOVE);
                result.append("\" value=\"true\">&nbsp;");
            }
        }
        if (isOperationOnFolder()) {
            // show recursive option if folder(s) is/are selected
            result.append(dialogSpacer());
            result.append(dialogBlockStart(key(Messages.GUI_UNDO_CHANGES_RECURSIVE_TITLE_0)));
            result.append("<input type=\"checkbox\" name=\"");
            result.append(PARAM_RECURSIVE);
            result.append("\" value=\"true\">&nbsp;");
            if (isMultiOperation()) {
                result.append(key(Messages.GUI_UNDO_CHANGES_RECURSIVE_MULTI_SUBRESOURCES_0));
            } else {
                result.append(key(Messages.GUI_UNDO_CHANGES_RECURSIVE_SUBRESOURCES_0));
            }
            result.append(dialogBlockEnd());
        }
        return result.toString();
    }

    /**
     * Returns the undo move operation flag parameter value.<p>
     *
     * @return the undo move operation flag parameter value
     */
    public String getParamMove() {

        return m_paramMove;
    }

    /**
     * Returns the value of the recursive parameter,
     * or <code>null</code> if this parameter was not provided.<p>
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
     * Sets the undo move operation flag parameter value.<p>
     *
     * @param paramMove the undo move operation flag to set
     */
    public void setParamMove(String paramMove) {

        m_paramMove = paramMove;
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
     * Returns the current CmsResource.<p>
     *
     * @return the CmsResource
     */
    protected CmsResource getCurrentResource() {

        return m_currentResource;
    }

    /**
     * Returns the file name without path information of the current resource.<p>
     *
     * @return the name of the current resource
     */
    protected String getFileName() {

        return CmsResource.getName(getParamResource());
    }

    /**
     * Returns the last modified date of the current resource as localized String.<p>
     *
     * @return the date of last modification
     */
    protected String getLastModifiedDate() {

        long dateLong = getCurrentResource().getDateLastModified();
        return getMessages().getDateTime(dateLong);
    }

    /**
     * Returns the user who made the last changes to the current resource.<p>
     *
     * @return the user who changed the resource
     */
    protected String getLastModifiedUser() {

        CmsUUID userId = getCurrentResource().getUserLastModified();
        try {
            return getCms().readUser(userId).getName();
        } catch (CmsException e) {
            return "";
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // check the required permissions to undo changes of the resource
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_UNDOCHANGES);
        } else if (DIALOG_CHECKSIBLINGS.equals(getParamAction())) {
            setAction(ACTION_CHECKSIBLINGS);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for undo changes dialog
            setDialogTitle(Messages.GUI_UNDO_CHANGES_1, Messages.GUI_UNDO_CHANGES_MULTI_2);
        }

        if (!isMultiOperation()) {
            // collect resource to display information on single operation dialog
            try {
                setCurrentResource(getCms().readResource(getParamResource(), CmsResourceFilter.ALL));
            } catch (CmsException e) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
        }

    }

    /**
     * Checks if the resource operation is an operation on at least one moved resource.<p>
     *
     * @return true if the operation an operation on at least one moved resource, otherwise false
     */
    protected boolean isOperationOnMovedResource() {

        Iterator<String> i = getResourceList().iterator();
        while (i.hasNext()) {
            String resName = i.next();
            String target = resourceOriginalPath(getCms(), resName);
            if ((target != null) && !target.equals(resName)) {
                // found a moved resource
                return true;
            }
        }
        return false;
    }

    /**
     * Performs the undo changes operation on a resource.<p>
     *
     * @return true, if the changes on a resource were undone, otherwise false
     * @throws CmsException if undo changes is not successful
     */
    @Override
    protected boolean performDialogOperation() throws CmsException {

        // check if the current resource is a folder for single operation
        boolean isFolder = isOperationOnFolder();
        // on folder undo changes or multi operation display "please wait" screen, not for simple file undo changes
        if ((isMultiOperation() || isFolder) && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // get the flag if the undo changes is recursive from request parameter
        boolean undoRecursive = Boolean.valueOf(getParamRecursive()).booleanValue();
        boolean undoMove = Boolean.valueOf(getParamMove()).booleanValue();

        CmsResourceUndoMode mode = CmsResource.UNDO_CONTENT;
        if (undoRecursive) {
            mode = CmsResource.UNDO_CONTENT_RECURSIVE;
        }
        if (undoMove) {
            mode = mode.includeMove();
        }

        Iterator<String> i = getResourceList().iterator();
        // iterate the resources to delete
        while (i.hasNext()) {
            String resName = i.next();
            try {
                // lock resource if autolock is enabled
                checkLock(resName);
                // undo changes on the resource
                getCms().undoChanges(resName, mode);
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
        checkMultiOperationException(Messages.get(), Messages.ERR_UNDO_CHANGES_MULTI_0);

        return true;
    }

    /**
     * Sets the current CmsResource.<p>
     *
     * @param res the CmsResource
     */
    protected void setCurrentResource(CmsResource res) {

        m_currentResource = res;
    }

    /**
     * Depth first recursion for searching if a file has siblings with early termination.<p>
     *
     * This avoids to read a whole resource tree (which will be hard for e.g.  /sites/).<p>
     *
     * @param path the path to check the siblings in
     *
     * @return true if a resource which is a sibling was found.
     *
     * @throws CmsException if something goes wrong
     */
    private boolean recursiveCheckSiblings(String path) throws CmsException {

        boolean result = false;
        CmsObject cms = getCms();
        /*
         * No trailing slash for the multi dialogs... therefore a read resource first necessary:
         */
        CmsResource resource = cms.readResource(path);
        path = cms.getSitePath(resource);
        if (CmsResource.isFolder(path)) {
            // only check subresources in case of recursive option checked.
            boolean undoRecursive = Boolean.valueOf(getParamRecursive()).booleanValue();
            if (undoRecursive) {
                // don't read the whole tree, this is  expensive - most likely we will find a sibling faster step by step:
                List<CmsResource> subResources = cms.readResources(path, CmsResourceFilter.ALL, false);
                Iterator<CmsResource> itSubResources = subResources.iterator();
                while (itSubResources.hasNext()) {
                    resource = itSubResources.next();
                    result = recursiveCheckSiblings(cms.getSitePath(resource));
                    if (result) {
                        break; // shortcut
                    }
                }
            }

        } else {
            List<CmsResource> siblings = cms.readSiblings(path, CmsResourceFilter.ALL);
            result = siblings.size() > 1;
        }
        return result;
    }

}

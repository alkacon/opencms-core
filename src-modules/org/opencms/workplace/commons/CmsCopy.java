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
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
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
 * Provides methods for the copy resources dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/copy.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsCopy extends CmsMultiDialog {

    /** Value for the action: copy the resource. */
    public static final int ACTION_COPY = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "copy";

    /** Request parameter name for the keep rights flag. */
    public static final String PARAM_KEEPRIGHTS = "keeprights";

    /** Request parameter name for the overwrite flag. */
    public static final String PARAM_OVERWRITE = "overwrite";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCopy.class);

    /** A parameter of this dialog. */
    private String m_paramCopymode;

    /** A parameter of this dialog. */
    private String m_paramKeeprights;

    /** A parameter of this dialog. */
    private String m_paramOverwrite;

    /** A parameter of this dialog. */
    private String m_paramTarget;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsCopy(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsCopy(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the copy action, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void actionCopy() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        CmsResource resource = null;
        try {
            boolean isFolder = false;
            String source = getResourceList().get(0);
            String target = CmsLinkManager.getAbsoluteUri(getParamTarget(), CmsResource.getParentFolder(source));
            if (!isMultiOperation()) {
                resource = getCms().readResource(source, CmsResourceFilter.ALL);
                isFolder = resource.isFolder();
            } else {
                String siteRootFolder = null;
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
                        Messages.get().container(Messages.ERR_COPY_MULTI_TARGET_NOFOLDER_1, target));
                }
            }
            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned copy operation was successful
                if (isMultiOperation() || isFolder) {
                    // set request attribute to reload the explorer tree view
                    List<String> folderList = new ArrayList<String>(1);
                    String targetParent = CmsResource.getParentFolder(target);
                    folderList.add(targetParent);
                    getJsp().getRequest().setAttribute(REQUEST_ATTRIBUTE_RELOADTREE, folderList);
                }
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // check if this exception requires a confirmation or error screen for single resource operations
            if (!isMultiOperation()
                && ((e instanceof CmsVfsResourceAlreadyExistsException) || (e instanceof CmsLockException))
                && (resource != null)
                && !(resource.isFolder())) {
                // file copy but file already exists, now check target file type
                int targetType = -1;
                String storedSiteRoot = null;
                try {
                    if (OpenCms.getSiteManager().getSiteRoot(getParamTarget()) != null) {
                        storedSiteRoot = getCms().getRequestContext().getSiteRoot();
                        getCms().getRequestContext().setSiteRoot("/");
                    }
                    CmsResource targetRes = getCms().readResource(getParamTarget());
                    targetType = targetRes.getTypeId();
                } catch (CmsException e2) {
                    // can usually be ignored
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e2.getLocalizedMessage());
                    }
                } finally {
                    if (storedSiteRoot != null) {
                        getCms().getRequestContext().setSiteRoot(storedSiteRoot);
                    }
                }
                if (resource.getTypeId() == targetType) {
                    // file type of target is the same as source, show confirmation dialog
                    setParamMessage(
                        CmsStringUtil.escapeHtml(key(
                            Messages.GUI_COPY_CONFIRM_OVERWRITE_2,
                            new Object[] {getParamResource(), getParamTarget()})));
                    getJsp().include(FILE_DIALOG_SCREEN_CONFIRM);
                } else {
                    // file type is different, create error message
                    includeErrorpage(this, e);
                }
            } else {
                // error during copy, show error dialog
                includeErrorpage(this, e);
            }
        }
    }

    /**
     * Builds the input radio buttons to select between preserving links or creating new resources when copying.<p>
     *
     * @return the HTML code for the radio buttons
     */
    public String buildRadioCopyMode() {

        StringBuffer retValue = new StringBuffer(256);

        // check if the current resource is a folder for single operation
        boolean isFolder = isOperationOnFolder();
        String checkedAttr = " checked=\"checked\"";

        if (isMultiOperation() || isFolder) {
            // for multi resource operations or folders, show an additional option "preserve links"
            CmsResourceCopyMode defaultMode = getSettings().getUserSettings().getDialogCopyFolderMode();
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"");
            retValue.append(CmsResource.COPY_AS_SIBLING.getMode());
            retValue.append("\"");
            if (defaultMode == CmsResource.COPY_AS_SIBLING) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            String msgKey;
            if (isMultiOperation()) {
                msgKey = Messages.GUI_COPY_MULTI_CREATE_SIBLINGS_0;
            } else {
                msgKey = Messages.GUI_COPY_CREATE_SIBLINGS_0;
            }
            retValue.append(key(msgKey));
            retValue.append("<br>\n");
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"");
            retValue.append(CmsResource.COPY_PRESERVE_SIBLING.getMode());
            retValue.append("\"");
            if (defaultMode == CmsResource.COPY_PRESERVE_SIBLING) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            retValue.append(key(Messages.GUI_COPY_ALL_NO_SIBLINGS_0));
            retValue.append("<br>\n");
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"");
            retValue.append(CmsResource.COPY_AS_NEW.getMode());
            retValue.append("\"");
            if (defaultMode == CmsResource.COPY_AS_NEW) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            retValue.append(key(Messages.GUI_COPY_ALL_0));
            retValue.append("<br>\n");

            if (isMultiOperation()) {
                // show overwrite option for multi resource copy
                retValue.append(dialogSpacer());
                retValue.append("<input type=\"checkbox\" name=\"");
                retValue.append(PARAM_OVERWRITE);
                retValue.append("\" value=\"true\"> ");
                retValue.append(key(Messages.GUI_COPY_MULTI_OVERWRITE_0));
                retValue.append("<br>\n");
            }
        } else {
            // for files, show copy option "copy as sibling" and "copy as new resource"
            CmsResourceCopyMode defaultMode = getSettings().getUserSettings().getDialogCopyFileMode();
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"");
            retValue.append(CmsResource.COPY_AS_SIBLING.getMode());
            retValue.append("\"");
            if (defaultMode == CmsResource.COPY_AS_SIBLING) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            retValue.append(key(Messages.GUI_CREATE_SIBLING_0));
            retValue.append("<br>\n");
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"");
            retValue.append(CmsResource.COPY_AS_NEW.getMode());
            retValue.append("\"");
            if (defaultMode == CmsResource.COPY_AS_NEW) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            retValue.append(key(Messages.GUI_COPY_AS_NEW_0));
            retValue.append("<br>\n");
        }

        return retValue.toString();
    }

    /**
     * Returns the value of the copymode parameter.<p>
     *
     * @return the value of the copymode parameter
     */
    public String getParamCopymode() {

        return m_paramCopymode;
    }

    /**
     * Returns the value of the keeprights parameter.<p>
     *
     * @return the value of the keeprights parameter
     */
    public String getParamKeeprights() {

        return m_paramKeeprights;
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
     * Sets the value of the copymode parameter.<p>
     *
     * @param value the value of the copymode parameter
     */
    public void setParamCopymode(String value) {

        m_paramCopymode = value;
    }

    /**
     * Sets the value of the "keeprights" parameter.<p>
     *
     * @param value the value of the "keeprights" parameter
     */
    public void setParamKeeprights(String value) {

        m_paramKeeprights = value;
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
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // check the required permissions to copy the resource
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_COPY);
        } else if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for copy dialog
            setDialogTitle(Messages.GUI_COPY_RESOURCE_1, Messages.GUI_COPY_MULTI_2);
        }
    }

    /**
     * Performs the resource copying.<p>
     *
     * @return true, if the resource was copied, otherwise false
     * @throws CmsException if copying is not successful
     */
    @Override
    protected boolean performDialogOperation() throws CmsException {

        // check if the current resource is a folder for single operation
        boolean isFolder = isOperationOnFolder();

        // on folder copy display "please wait" screen, not for simple file copy
        if ((isMultiOperation() || isFolder) && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // get the copy mode from request parameter value
        CmsResourceCopyMode copyMode = CmsResource.COPY_PRESERVE_SIBLING;
        try {
            copyMode = CmsResourceCopyMode.valueOf(Integer.parseInt(getParamCopymode()));
        } catch (Exception e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        // check the overwrite options
        boolean overwrite = Boolean.valueOf(getParamOverwrite()).booleanValue();
        overwrite = ((isMultiOperation() && overwrite) || DIALOG_CONFIRMED.equals(getParamAction()));

        // calculate the target name
        String target = getParamTarget();
        if (target == null) {
            target = "";
        }

        String storedSiteRoot = null;
        try {
            // check if a site root was added to the target name
            String sitePrefix = "";
            if (OpenCms.getSiteManager().getSiteRoot(target) != null) {
                String siteRootFolder = getCms().getRequestContext().getSiteRoot();
                if (siteRootFolder.endsWith("/")) {
                    siteRootFolder = siteRootFolder.substring(0, siteRootFolder.length() - 1);
                }
                sitePrefix = siteRootFolder;
                storedSiteRoot = getCms().getRequestContext().getSiteRoot();
                getCms().getRequestContext().setSiteRoot("/");
            }

            Iterator<String> i = getResourceList().iterator();
            // iterate the resources to copy
            while (i.hasNext()) {
                String resName = i.next();
                try {
                    performSingleCopyOperation(resName, target, sitePrefix, copyMode, overwrite);
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
            checkMultiOperationException(Messages.get(), Messages.ERR_COPY_MULTI_0);
        } finally {
            // restore the site root
            if (storedSiteRoot != null) {
                getCms().getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
        return true;
    }

    /**
     * Performs the copy operation for a single VFS resource.<p>
     *
     * @param source the source VFS path
     * @param target the target VFS path
     * @param sitePrefix the site prefix
     * @param copyMode the copy mode for siblings
     * @param overwrite the overwrite flag
     *
     * @throws CmsException if copying the resource fails
     */
    protected void performSingleCopyOperation(
        String source,
        String target,
        String sitePrefix,
        CmsResourceCopyMode copyMode,
        boolean overwrite) throws CmsException {

        // calculate the target name
        String finalTarget = CmsLinkManager.getAbsoluteUri(target, CmsResource.getParentFolder(source));

        if (finalTarget.equals(source) || (isMultiOperation() && finalTarget.startsWith(source))) {
            throw new CmsVfsException(Messages.get().container(Messages.ERR_COPY_ONTO_ITSELF_1, finalTarget));
        }

        try {
            CmsResource res = getCms().readResource(finalTarget, CmsResourceFilter.ALL);
            if (res.isFolder()) {
                // target folder already exists, so we add the current folder name
                if (!finalTarget.endsWith("/")) {
                    finalTarget += "/";
                }
                finalTarget = finalTarget + CmsResource.getName(source);
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // target folder does not already exist, so target name is o.k.
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        // set the target parameter value
        setParamTarget(finalTarget);

        // delete existing target resource if selected or confirmed by the user
        if (overwrite && getCms().existsResource(finalTarget)) {
            checkLock(finalTarget);
            getCms().deleteResource(finalTarget, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
        // copy the resource
        getCms().copyResource(sitePrefix + source, finalTarget, copyMode);
    }
}

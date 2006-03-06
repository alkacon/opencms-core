/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsLock.java,v $
 * Date   : $Date: 2006/03/06 13:20:07 $
 * Version: $Revision: 1.15.2.2 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.security.CmsPermissionSet;
import org.opencms.workplace.CmsDialogSelector;
import org.opencms.workplace.CmsMultiDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.I_CmsDialogHandler;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Creates the dialogs for locking, unlocking or steal lock operations on a resource.<p> 
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/lock_standard.jsp
 * <li>/commons/lockchange_standard.jsp
 * <li>/commons/unlock_standard.jsp
 * </ul>
 * <p>
 * 
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.15.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLock extends CmsMultiDialog implements I_CmsDialogHandler {

    /** Value for the action: confirmed. */
    public static final int ACTION_SUBMIT_NOCONFIRMATION = 200;

    /** Request parameter value for the action: submit form without user interaction. */
    public static final String DIALOG_SUBMIT_NOCONFIRMATION = "submitnoconfirmation";

    /** The dialog type: lock a resource. */
    public static final String DIALOG_TYPE_LOCK = "lock";
    /** The dialog type: Steal a lock. */
    public static final String DIALOG_TYPE_LOCKCHANGE = "lockchange";
    /** The dialog type: unlock a resource. */
    public static final String DIALOG_TYPE_UNLOCK = "unlock";

    /** Type of the operation which is performed: lock resource. */
    public static final int TYPE_LOCK = 1;
    /** Type of the operation which is performed: steal a lock. */
    public static final int TYPE_LOCKCHANGE = 2;
    /** Type of the operation which is performed: unlock resource. */
    public static final int TYPE_UNLOCK = 3;

    /** The lock dialog URI. */
    public static final String URI_LOCK_DIALOG = PATH_DIALOGS + "lock_standard.jsp";
    /** The steal lock dialog URI. */
    public static final String URI_LOCKCHANGE_DIALOG = PATH_DIALOGS + "lockchange_standard.jsp";
    /** The unlock dialog URI. */
    public static final String URI_UNLOCK_DIALOG = PATH_DIALOGS + "unlock_standard.jsp";

    /** Flag indicating if the selected resources to lockes contain locked subresources.*/
    private boolean m_hasLockedSubResources;

    /**
     * Default constructor needed for dialog handler implementation.<p>
     */
    public CmsLock() {

        super(null);
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsLock(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsLock(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Determines if the resource should be locked, unlocked or if the lock should be stolen.<p>
     * 
     * @param cms the CmsObject
     * @return the dialog action: lock, change lock (steal) or unlock
     */
    public static int getDialogAction(CmsObject cms) {

        String fileName = CmsResource.getName(cms.getRequestContext().getUri());
        if (fileName == null) {
            // file name could not be determined, return "unlock" action
            return TYPE_UNLOCK;
        } else if (fileName.equalsIgnoreCase("lock.jsp")) {
            // a "lock" action is requested
            return TYPE_LOCK;
        } else if (fileName.indexOf("change") != -1) {
            // a "steal lock" action is requested
            return TYPE_LOCKCHANGE;
        } else {
            // an "unlock" action is requested
            return TYPE_UNLOCK;
        }
    }

    /**
     * Performs the lock/unlock operation, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionToggleLock() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);

        try {

            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned the lock/unlock operation was successful          
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // exception occured, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Builds the HTML for the text that is shown on the confirmation dialog depending on the selected resource(s).<p>
     * 
     * @return the HTML for the text that is shown on the confirmation dialog depending on the selected resource(s)
     */
    public String buildDialogText() {

        switch (getDialogAction(getCms())) {
            case TYPE_LOCK:
                if (hasLockedSubResources()) {
                    if (isMultiOperation()) {
                        return key(Messages.GUI_LOCK_MULTI_INFO_LOCKEDSUBRESOURCES_0);
                    } else {
                        return key(Messages.GUI_LOCK_INFO_LOCKEDSUBRESOURCES_0);
                    }
                } else {
                    if (isMultiOperation()) {
                        return key(Messages.GUI_LOCK_MULTI_LOCK_CONFIRMATION_0);
                    } else {
                        return key(Messages.GUI_LOCK_CONFIRMATION_0);
                    }
                }
            case TYPE_LOCKCHANGE:
                return key(Messages.GUI_LOCK_CHANGE_CONFIRMATION_0);
            case TYPE_UNLOCK:
            default:
                if (isMultiOperation()) {
                    return key(Messages.GUI_LOCK_MULTI_UNLOCK_CONFIRMATION_0);
                } else {
                    return key(Messages.GUI_LOCK_UNLOCK_CONFIRMATION_0);
                }
        }
    }

    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogHandler()
     */
    public String getDialogHandler() {

        return CmsDialogSelector.DIALOG_LOCK;
    }

    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogUri(java.lang.String, CmsJspActionElement)
     */
    public String getDialogUri(String resource, CmsJspActionElement jsp) {

        switch (getDialogAction(jsp.getCmsObject())) {
            case TYPE_LOCK:
                return URI_LOCK_DIALOG;
            case TYPE_LOCKCHANGE:
                return URI_LOCKCHANGE_DIALOG;
            case TYPE_UNLOCK:
            default:
                return URI_UNLOCK_DIALOG;
        }
    }

    /**
     * Returns true if the resources to lock have locked subresources.<p>
     * 
     * @return true if the resources to lock have locked subresources
     */
    public boolean hasLockedSubResources() {

        return m_hasLockedSubResources;
    }

    /**
     * Sets if the resources to lock have locked subresources.<p>
     * 
     * @param hasLockedSubResources if the resources to lock have locked subresources
     */
    public void setHasLockedSubResources(boolean hasLockedSubResources) {

        m_hasLockedSubResources = hasLockedSubResources;
    }

    /**
     * Determines whether to show the lock dialog depending on the users settings and the dilaog type.<p>
     * 
     * In case of locking a folder, a confirmation dialog is needed if any sub resources are already locked.<p>
     * 
     * @return true if dialogs should be shown, otherwise false
     */
    public boolean showConfirmation() {

        boolean showConfirmation = getSettings().getUserSettings().getDialogShowLock();
        if (DIALOG_TYPE_LOCK.equals(getParamDialogtype())) {
            // in case of locking resources, check if there are locked sub resources in the selected folder(s)
            Iterator i = getResourceList().iterator();
            while (i.hasNext()) {
                String resName = (String)i.next();
                try {
                    CmsResource res = getCms().readResource(resName);
                    if (res.isFolder() && getCms().countLockedResources(resName) > 0) {
                        // found folder with locked subresources, set flag to show confirmation dialog
                        setHasLockedSubResources(true);
                        return true;
                    }
                } catch (CmsException e) {
                    // error reading a resource, should usually never happen
                }
            }
        }
        return showConfirmation;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // set the action for the JSP switch 
        if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else {
            switch (getDialogAction(getCms())) {
                case TYPE_LOCK:
                    setDialogTitle(Messages.GUI_LOCK_RESOURCE_1, Messages.GUI_LOCK_MULTI_LOCK_2);
                    setParamDialogtype(DIALOG_TYPE_LOCK);
                    // check the required permissions to lock/unlock a single resource       
                    if (!isMultiOperation() && !checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
                        // no write permissions for the resource, set cancel action to close dialog
                        setAction(ACTION_CANCEL);
                        return;
                    }
                    break;
                case TYPE_LOCKCHANGE:
                    setDialogTitle(Messages.GUI_LOCK_STEAL_1, Messages.GUI_LOCK_MULTI_STEAL_2);
                    setParamDialogtype(DIALOG_TYPE_UNLOCK);
                    break;
                case TYPE_UNLOCK:
                default:
                    setDialogTitle(Messages.GUI_LOCK_UNLOCK_1, Messages.GUI_LOCK_MULTI_UNLOCK_2);
                    setParamDialogtype(DIALOG_TYPE_UNLOCK);
            }
            // set action depending on user settings
            if (showConfirmation()) {
                // show confirmation dialog
                setAction(ACTION_DEFAULT);
            } else {
                // lock/unlock resource without confirmation
                setAction(ACTION_SUBMIT_NOCONFIRMATION);
            }
        }
    }

    /**
     * Performs the lock/unlock/steal lock operation.<p>
     * 
     * @return true, if the operation was performed, otherwise false
     * @throws CmsException if operation is not successful
     */
    protected boolean performDialogOperation() throws CmsException {

        //on multi resource operation display "please wait" screen
        if (isMultiOperation() && !DIALOG_WAIT.equals(getParamAction())) {
            return false;
        }
        // determine action to perform (lock, unlock, change lock)
        int dialogAction = getDialogAction(getCms());

        // now perform the operation on the resource(s)
        Iterator i = getResourceList().iterator();
        while (i.hasNext()) {
            String resName = (String)i.next();
            try {
                performSingleResourceOperation(resName, dialogAction);
            } catch (CmsException e) {
                // collect exceptions to create a detailed output
                addMultiOperationException(e);
            }
        }
        // generate the error message for exception
        String message;
        if (dialogAction == TYPE_LOCK) {
            message = Messages.ERR_LOCK_MULTI_0;
        } else {
            message = Messages.ERR_UNLOCK_MULTI_0;
        }
        checkMultiOperationException(Messages.get(), message);

        return true;
    }

    /**
     * Performs the lock state operation on a single resource.<p>
     * 
     * @param resourceName the resource name to perform the operation on
     * @param dialogAction the lock action: lock, unlock or change lock
     * @throws CmsException if the operation fails
     */
    protected void performSingleResourceOperation(String resourceName, int dialogAction) throws CmsException {

        // store original name to use for lock action 
        String originalResourceName = resourceName;
        CmsResource res = getCms().readResource(resourceName, CmsResourceFilter.ALL);
        if (res.isFolder() && !resourceName.endsWith("/")) {
            resourceName += "/";
        }
        // perform action depending on dialog uri
        switch (dialogAction) {
            case TYPE_LOCK:
                getCms().lockResource(originalResourceName);
                break;
            case TYPE_LOCKCHANGE:
                getCms().changeLock(resourceName);
                break;
            case TYPE_UNLOCK:
            default:
                getCms().unlockResource(resourceName);
        }
    }

}
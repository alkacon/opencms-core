/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsLock.java,v $
 * Date   : $Date: 2003/11/07 13:17:33 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.opencms.workplace;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.security.CmsSecurityException;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Creates the dialogs for locking, unlocking or steal lock operations on a resource.<p> 
 *
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/lock_standard_html
 * <li>/jsp/dialogs/lockchange_standard_html
 * <li>/jsp/dialogs/unlock_standard_html
 * </ul>
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.1.12
 */
public class CmsLock extends CmsDialog implements I_CmsDialogHandler {
        
    public static final String DIALOG_TYPE_LOCK = "lock";
    public static final String DIALOG_TYPE_LOCKCHANGE = "lockchange";
    public static final String DIALOG_TYPE_UNLOCK = "unlock";
    // always start individual action id's with 100 to leave enough room for more default actions
    
    public static final String URI_LOCK_DIALOG = C_PATH_DIALOGS + "lock_standard.html";
    public static final String URI_LOCKCHANGE_DIALOG = C_PATH_DIALOGS + "lockchange_standard.html";
    public static final String URI_UNLOCK_DIALOG = C_PATH_DIALOGS + "unlock_standard.html";
    
    // the three possible action types performed by this class
    public static final int TYPE_LOCK = 1;
    public static final int TYPE_LOCKCHANGE = 2;
    public static final int TYPE_UNLOCK = 3;
    
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
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogUri(java.lang.String, com.opencms.flex.jsp.CmsJspActionElement)
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
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogHandler()
     */
    public String getDialogHandler() {
        return CmsDialogSelector.DIALOG_LOCK;
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
        } else {
            switch (getDialogAction(getCms())) {
            case TYPE_LOCK:
                setParamTitle(key("messagebox.title.lock"));
                setParamDialogtype(DIALOG_TYPE_LOCK);
                break;
            case TYPE_LOCKCHANGE:
                setParamTitle(key("messagebox.title.lockchange"));
                setParamDialogtype(DIALOG_TYPE_UNLOCK);
                break;
            case TYPE_UNLOCK:
            default:
                setParamTitle(key("messagebox.title.unlock"));
                setParamDialogtype(DIALOG_TYPE_UNLOCK);
            }
            // set action depending on user settings
            if (showConfirmation()) {
                // show confirmation dialog
                setAction(ACTION_DEFAULT);
            } else {
                // lock/unlock resource without confirmation
                setAction(ACTION_CONFIRMED);
            }
        }                 
    }
    
    /**
     * Performs the lock/unlock operation, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionToggleLock() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
    
        try {
            String resName = getParamResource();
            CmsResource res = getCms().readFileHeader(resName);
            if (res.isFolder() && !resName.endsWith("/")) {
                resName += "/";
            }
            // perform action depending on dialog uri
            switch (getDialogAction(getCms())) {
            case TYPE_LOCK:
                getCms().lockResource(getParamResource());
                break;
            case TYPE_LOCKCHANGE:
                getCms().changeLock(resName);
                break;
            case TYPE_UNLOCK:
            default:               
                getCms().unlockResource(resName, false);
            }
            // create a map with an empty "resource" parameter to avoid changing the folder when returning to explorer file list
            Map params = new HashMap();
            params.put(I_CmsWpConstants.C_PARA_RESOURCE, "");
            // if no exception is caused operation was successful
            getJsp().include(C_FILE_EXPLORER_FILELIST, null, params);
        } catch (CmsException e) {
            // exception occured, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            // check if this exception is a security or other exception
            if (e instanceof CmsSecurityException) {
                // security exception, prepare error message
                setParamMessage(key("error.message.accessdenied")); 
                setParamReasonSuggestion(key("error.reason.accessdenied") + "<br>\n" + key("error.suggestion.accessdenied") + "\n");
            } else {                
                // error during lock/unlock, prepare common error messages
                setParamMessage(key("error.message." + getParamDialogtype()));
                setParamReasonSuggestion(getErrorSuggestionDefault());
            }
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }
    
    /**
     * Determines whether to show the lock dialog depending on the users settings.<p>
     * 
     * @return true if dialogs should be shown, otherwise false
     */
    public boolean showConfirmation() {
        Hashtable startSettings = (Hashtable)getSettings().getUser().getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS);
        String showLockDialog = "off";
        if (startSettings != null) {
            showLockDialog = (String)startSettings.get(I_CmsConstants.C_START_LOCKDIALOG);
        }
        return ("on".equals(showLockDialog));
    }
    
    /**
     * Determines if the resource should be locked, unlocked or if the lock should be stolen.<p>
     * 
     * @param cms the CmsObject
     * @return the dialog action: lock, change lock (steal) or unlock
     */
    public static int getDialogAction(CmsObject cms) {
        if ("lock.html".equals(cms.getRequestContext().getFileUri())) {
            // a "lock" action is requested
            return TYPE_LOCK;
        } else if ("lockchange.html".equals(cms.getRequestContext().getFileUri())) {
            // a "steal lock" action is requested
            return TYPE_LOCKCHANGE;
        } else {
            // an "unlock" action is requested
            return TYPE_UNLOCK;
        }
    }
    
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsDelete.java,v $
 * Date   : $Date: 2004/08/19 11:26:34 $
 * Version: $Revision: 1.1 $
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
package org.opencms.workplace.commons;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsDialogSelector;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.I_CmsDialogHandler;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the delete resources dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/delete_standard.jsp
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsDelete extends CmsDialog implements I_CmsDialogHandler {
    
    /** The dialog type. */
    public static final String DIALOG_TYPE = "delete";
    
    /** Value for the action: delete the resource. */
    public static final int ACTION_DELETE = 100;
    
    private String m_deleteVfsLinks;
    
    /** The delete dialog URI. */
    public static final String URI_DELETE_DIALOG = C_PATH_DIALOGS + "delete_standard.jsp";
    
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
     * Returns the html for the "delete siblings" options when deleting a a resource with siblings.<p>
     * 
     * @return the html for the "delete siblings" options
     */
    public String buildDeleteSiblings() {
        if (hasVfsLinks() && hasCorrectLockstate()) {         
            // show only if resource has siblings and correct lock state
            StringBuffer result = new StringBuffer(512);
            int defaultMode = getSettings().getUserSettings().getDialogDeleteFileMode();
            result.append(key("messagebox.siblings"));
            result.append("<p>");
            result.append("<input type=\"radio\" name=\"deletevfslinks\" value=\"false\"");
            if (defaultMode == I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS) {
                result.append(" checked=\"checked\"");
            }
            result.append(">&nbsp;");
            result.append(key("messagebox.option.siblingsPreserve"));
            result.append("<br>");
            result.append("<input type=\"radio\" name=\"deletevfslinks\" value=\"true\"");
            if (defaultMode == I_CmsConstants.C_DELETE_OPTION_DELETE_SIBLINGS) {
                result.append(" checked=\"checked\"");
            }
            result.append(">&nbsp;");
            result.append(key("messagebox.option.siblingsDelete"));
            result.append("<p>");
            return result.toString();
        }
        return "";
    }
    
    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogUri(java.lang.String, CmsJspActionElement)
     */
    public String getDialogUri(String resource, CmsJspActionElement jsp) {
        return URI_DELETE_DIALOG;
    }
    
    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogHandler()
     */
    public String getDialogHandler() {
        return CmsDialogSelector.DIALOG_DELETE;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_DELETE);                            
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {          
            setAction(ACTION_CANCEL);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for delete dialog     
            setParamTitle(key("title.delete") + ": " + CmsResource.getName(getParamResource()));
        }      
    } 

    /**
     * Performs the delete action, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionDelete() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            CmsResource resource = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            boolean isFolder = resource.isFolder();
            if (performDeleteOperation(isFolder))  {
                // if no exception is caused and "true" is returned delete operation was successful
                if (isFolder) {
                    // set request attribute to reload the explorer tree view
                    List folderList = new ArrayList(1);
                    folderList.add(CmsResource.getParentFolder(getParamResource()));
                    getJsp().getRequest().setAttribute(C_REQUEST_ATTRIBUTE_RELOADTREE, folderList);
                }
                actionCloseDialog();
            } else  {
                // "false" returned, display "please wait" screen
                getJsp().include(C_FILE_DIALOG_SCREEN_WAIT);
            }    
        } catch (CmsException e) {
            // prepare common message part

            String message = "<p>\n" + key("title.delete") + ": " + getParamResource() + "\n</p>\n";                 
            
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(message + key("error.message." + getParamDialogtype()));
            
            if (e instanceof CmsLockException) {
                setParamReasonSuggestion(e.getMessage());
            } else {
                setParamReasonSuggestion(getErrorSuggestionDefault());
            }

            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR); 
        }
    }
    
    /**
     * Performs the resource deletion.<p>
     * 
     * @param isFolder true if the resource to delete is a folder, otherwise false
     * @return true, if the resource was deleted, otherwise false
     * @throws CmsException if deletion is not successful
     */
    public boolean performDeleteOperation(boolean isFolder) throws CmsException {
        int deleteOption = -1;     
        
        // on folder deletion display "please wait" screen, not for simple file deletion
        if (isFolder && ! DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }
        
        // determine the correct delete option
        deleteOption = Boolean.valueOf(getParamDeleteVfsLinks()).booleanValue()
            ? I_CmsConstants.C_DELETE_OPTION_DELETE_SIBLINGS 
            : I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS;
         
        // lock resource if autolock is enabled
        checkLock(getParamResource());        
        // delete the resource
        getCms().deleteResource(getParamResource(), deleteOption);
        
        return true;
    }
    
    /**
     * Checks if VFS links are pointing to this resource.
     * 
     * @return true if one or more VFS links are pointing to this resource
     */
    public boolean hasVfsLinks() {
        try {
            return getCms().readSiblings(getParamResource(), CmsResourceFilter.ALL).size() > 1;
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error getting siblings of resource " + getParamResource(), e);
            }
            return false;
        }
        
    }
    
    /**
     * Checks if the current resource has lock state exclusive or inherited.<p>
     * 
     * This is used to determine whether the dialog shows the option to delete all
     * siblings of the resource or not.
     * 
     * @return true if lock state is exclusive or inherited, otherwise false
     */
    public boolean hasCorrectLockstate() {
        CmsLock lock = null;
        try {
            // get the lock state for the current resource
            lock = getCms().getLock(getParamResource());
        } catch (CmsException e) {
            // error getting lock state, log the error and return false
            if (OpenCms.getLog(this).isErrorEnabled()) { 
                OpenCms.getLog(this).error("Error getting lock state for resource " + getParamResource(), e);
            }  
            return false;           
        }
        int type = lock.getType();
        // check if autolock feature is enabled
        boolean autoLockFeature = lock.isNullLock() && OpenCms.getWorkplaceManager().autoLockResources();
        return (autoLockFeature || type == CmsLock.C_TYPE_EXCLUSIVE || type == CmsLock.C_TYPE_INHERITED);
    }
    
    /**
     * Sets the value of the boolean option to delete VFS links.<p>
     * 
     * @param value the value of the boolean option to delete VFS links
     */
    public void setParamDeleteVfsLinks(String value) {
        m_deleteVfsLinks = value;
    }
    
    /**
     * Returns the value of the boolean option to delete VFS links.<p>
     * 
     * @return the value of the boolean option to delete VFS links as a lower case string
     */
    public String getParamDeleteVfsLinks() {
        return m_deleteVfsLinks;
    }
    
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsMove.java,v $
 * Date   : $Date: 2003/11/07 13:17:33 $
 * Version: $Revision: 1.13 $
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
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.opencms.site.CmsSiteManager;

/**
 * Provides methods for the move resources dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/move_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.13 $
 * 
 * @since 5.1
 */
public class CmsMove extends CmsDialog {

    // always start individual action id's with 100 to leave enough room for more default actions
    public static final int ACTION_MOVE = 100;
    
    public static final String DIALOG_TYPE = "move";

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
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_MOVE);                            
        } else if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for copy dialog     
            setParamTitle(key("title.move") + ": " + CmsResource.getName(getParamResource()));
        }      
    } 

    /**
     * Performs the move action, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionMove() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            if (performMoveOperation())  {
                // if no exception is caused and "true" is returned copy operation was successful
                getJsp().include(C_FILE_EXPLORER_FILELIST);
            } else  {
                // "false" returned, display "please wait" screen
                getJsp().include(C_FILE_DIALOG_SCREEN_WAIT);
            }    
        } catch (CmsException e) {
            // prepare common message part
            String message = "<p>\n" 
                + key("source") + ": " + getParamResource() + "<br>\n" 
                + key("target") + ": " + getParamTarget() + "\n</p>\n";
            // check if this exception requires a confirmation or error screen
            if ((e.getType() == CmsException.C_FILE_EXISTS)) {
                // file copy but file already exists, show confirmation dialog
                setParamMessage(message + key("confirm.message.copy"));
                getJsp().include(C_FILE_DIALOG_SCREEN_CONFIRM);        
            } else {                
                // error during copy, show error dialog
                setParamErrorstack(e.getStackTraceAsString());
                setParamMessage(message + key("error.message." + getParamDialogtype()));
                setParamReasonSuggestion(getErrorSuggestionDefault());
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
            }
        }
    }
    
    /**
     * Performs the resource moving.<p>
     * 
     * @return true, if the resource was copied, otherwise false
     * @throws CmsException if copying is not successful
     */
    private boolean performMoveOperation() throws CmsException {

        // on folder copy display "please wait" screen, not for simple file copy
        CmsResource sourceRes = getCms().readFileHeader(getParamResource());
        if (sourceRes.isFolder() && ! DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // get the target name
        String target = getParamTarget();
        if (target == null) target = "";
        
        boolean restoreSiteRoot = false;
        try {
            // check if a site root was added to the target name
            String sitePrefix = "";
            if (CmsSiteManager.getSiteRoot(target) != null) { 
                String siteRootFolder = getCms().getRequestContext().getSiteRoot();
                if (siteRootFolder.endsWith("/")) {
                    siteRootFolder = siteRootFolder.substring(0, siteRootFolder.length()-1);
                }  
                sitePrefix = siteRootFolder;
                getCms().getRequestContext().saveSiteRoot();
                getCms().getRequestContext().setSiteRoot("/");
                restoreSiteRoot = true;
            }
        
        if (target.equals(getParamResource())) {
            throw new CmsException("Can't move resource onto itself.", CmsException.C_FILESYSTEM_ERROR);
        }
        
        // calculate the target name
        if (! target.startsWith("/")) {
            // target is not an absolute path, add the current parent folder
            target = CmsResource.getParentFolder(getParamResource()) + target; 
        }
        try {
            CmsResource res = getCms().readFileHeader(target);
            if (res.isFolder()) {
                // target folder already exists, so we add the current folder name
                if (! target.endsWith("/")) target += "/";
                target = target + CmsResource.getName(getParamResource());
                if (target.endsWith("/")) target = target.substring(0, target.length()-1);
            }
        } catch (CmsException e) {
            // target folder does not already exist, so target name is o.k.
        }
        
        // set the target parameter value
        setParamTarget(target);        
        
        // check if target already exists, if so, throw exception to show confirmation dialog
        CmsResource targetRes = null;
        try {
            targetRes = getCms().readFileHeader(target);
        } catch (CmsException e) { 
            // ignore
        }

        if (targetRes != null) {
            if (DIALOG_CONFIRMED.equals(getParamAction())) {
                // delete existing target resource if confirmed by the user
                getCms().deleteResource(target, I_CmsConstants.C_DELETE_OPTION_IGNORE_VFS_LINKS);
            } else {
                // throw exception to indicate that the target exists
                throw new CmsException("The target already exists", CmsException.C_FILE_EXISTS);
            }
        } 
                
        // move the resource
        getCms().moveResource(sitePrefix + getParamResource(), target);
        } finally {
            if (restoreSiteRoot) {
                getCms().getRequestContext().restoreSiteRoot();
            }
        }
        return true;
    }
}

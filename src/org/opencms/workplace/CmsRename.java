/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsRename.java,v $
 * Date   : $Date: 2003/08/06 15:58:39 $
 * Version: $Revision: 1.10 $
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
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the rename resources dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/rename_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.10 $
 * 
 * @since 5.1
 */
public class CmsRename extends CmsDialog {

    // always start individual action id's with 100 to leave enough room for more default actions
    public static final int ACTION_RENAME = 100;
    
    public static final String DIALOG_TYPE = "rename";

    private String m_paramTarget;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsRename(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRename(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
            setAction(ACTION_RENAME);                            
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for delete dialog     
            setParamTitle(key("title.rename") + ": " + CmsResource.getName(getParamResource()));
        }      
    } 
    
    /**
     * Returns the old name of the resource which should be renamed.<p>
     * 
     * This is used to predefine the input text field with the old resource name.
     * 
     * @return the old name of the resource which should be renamed
     */
    public String getOldResourceName() {
        String resourceName = CmsResource.getName(getParamResource());
        if (resourceName.endsWith("/")) {
            resourceName = resourceName.substring(0, resourceName.length() - 1);
        }
        return resourceName;
    }

    /**
     * Performs the rename action, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionRename() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            performRenameOperation();
            // if no exception is caused rename operation was successful
            getJsp().include(C_FILE_EXPLORER_FILELIST);          
        } catch (CmsException e) {
            // prepare common message part
            String message = "<p>\n" 
                + key("source") + ": " + getParamResource() + "<br>\n" 
                + key("target") + ": " + getParamTarget() + "\n</p>\n";
           
                  
            // error during copy, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(message + key("error.message." + getParamDialogtype()));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }
    
    /**
     * Performs the resource renaming.<p>
     * 
     * @return true, if the resource was renamed, otherwise false
     * @throws CmsException if renaming is not successful
     */
    private boolean performRenameOperation() throws CmsException {
   
        String target = getParamTarget();
        if (target == null) target = "";     
        
        // check if target name contains a "/"
        if (target.indexOf("/") != -1) {
            throw new CmsException("The new resource name must not contain a \"/\" character", CmsException.C_BAD_NAME);
        }
        
        String parentFolder = CmsResource.getParent(getParamResource());
        
        // check if resource is a folder, if so, add absolute path to parent folder to target
        CmsResource res = getCms().readFileHeader(getParamResource());
        if (res.isFolder() && !getParamResource().endsWith("/")) {
            target = parentFolder + target;
        }
        
        // check if target already exists, if so, throw exception and terminate
        boolean targetExists = false;
        try {
            getCms().readFileHeader(parentFolder + target);
            targetExists = true;
        } catch (CmsException e) { } 
        if (targetExists) {
            throw new CmsException("The resource already exists", CmsException.C_FILE_EXISTS);       
        }
        
        // rename the resource
        getCms().renameResource(getParamResource(), target);
        return true;
    }
}

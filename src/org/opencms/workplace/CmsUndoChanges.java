/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsUndoChanges.java,v $
 * Date   : $Date: 2003/07/08 14:08:48 $
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
package org.opencms.workplace;

import com.opencms.core.CmsException;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.flex.util.CmsUUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the undo changes on a resource dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/undochanges_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsUndoChanges extends CmsDialog {

    // always start individual action id's with 100 to leave enough room for more default actions
    public static final int ACTION_UNDOCHANGES = 100;
    
    public static final String DIALOG_TYPE = "undochanges";
    
    private CmsResource m_currentResource;

    
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_UNDOCHANGES);                            
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for delete dialog     
            setParamTitle(key("title.undochanges") + ": " + CmsResource.getName(getParamFile()));
        }
        
        try {
            setCurrentResource(getCms().readFileHeader(getParamFile()));
        } catch (CmsException e) {}
              
    } 

    /**
     * Performs the undo changes action, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionUndoChanges() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
           performUndoChangesOperation();
           // if no exception is caused undo changes operation was successful
           getJsp().include(CmsWorkplaceAction.C_JSP_WORKPLACE_FILELIST);
        } catch (CmsException e) {          
            // error during deletion, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message." + getParamDialogtype()));
            getJsp().include(CmsWorkplaceAction.C_PATH_JSP_WORKPLACE_COMMONS + "error.html");
        }
    }
    
    /**
     * Performs the undo changes operation on a resource.<p>
     * 
     * @return true, if the changes on a resource were undone, otherwise false
     * @throws CmsException if undo changes is not successful
     */
    private boolean performUndoChangesOperation() throws CmsException {     
         
        // undo changes on the resource
        getCms().undoChanges(getParamFile());
        
        return true;
    }
    
    /**
     * Returns the last modified date of the current resource as localized String.<p>
     * 
     * @return the date of last modification
     */
    public String getLastModifiedDate() {
        long dateLong = getCurrentResource().getDateLastModified();
        return getSettings().getMessages().getDateTime(dateLong);
    }
    
    /**
     * Returns the user who made the last changes to the current resource.<p>
     * 
     * @return the user who changed the resource
     */
    public String getLastModifiedUser() {
        CmsUUID userId = getCurrentResource().getResourceLastModifiedBy();
        try {
            return getCms().readUser(userId).getName();
        } catch (CmsException e) {
            return "";
        }
    }
    
    /**
     * Returns the file name without path information of the current resource.<p>
     * 
     * @return the name of the current resource
     */
    public String getFileName() {
        return CmsResource.getName(getParamFile());
    }
    
    /**
     * Returns the current CmsResource.<p>
     * 
     * @return the CmsResource
     */
    public CmsResource getCurrentResource() {
        return m_currentResource;
    }
    
    /**
     * Sets the current CmsResource.<p>
     * 
     * @param res the CmsResource
     */
    public void setCurrentResource(CmsResource res) {
        m_currentResource = res;
    }
    
}

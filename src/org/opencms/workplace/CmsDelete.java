/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsDelete.java,v $
 * Date   : $Date: 2003/07/21 12:45:17 $
 * Version: $Revision: 1.5 $
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
 * Provides methods for the delete resources dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/delete_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.5 $
 * 
 * @since 5.1
 */
public class CmsDelete extends CmsDialog {

    // always start individual action id's with 100 to leave enough room for more default actions
    public static final int ACTION_DELETE = 100;
    
    public static final String DIALOG_TYPE = "delete";

    
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
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for delete dialog     
            setParamTitle(key("title.delete") + ": " + CmsResource.getName(getParamFile()));
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
            if (performDeleteOperation())  {
                // if no exception is caused and "true" is returned delete operation was successful
                getJsp().include(C_FILE_EXPLORER_FILELIST);
            } else  {
                // "false" returned, display "please wait" screen
                getJsp().include(C_FILE_DIALOG_SCREEN_WAIT);
            }    
        } catch (CmsException e) {
            // prepare common message part
            String message = "<p>\n" 
                + key("title.delete") + ": " + getParamFile() + "\n</p>\n";                 
            // error during deletion, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(message + key("error.message." + getParamDialogtype()));
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }
    
    /**
     * Performs the resource deletion.<p>
     * 
     * @return true, if the resource was deleted, otherwise false
     * @throws CmsException if deletion is not successful
     */
    private boolean performDeleteOperation() throws CmsException {     
        
        // on folder copy display "please wait" screen, not for simple file copy
        CmsResource sourceRes = getCms().readFileHeader(getParamFile());
        if (sourceRes.isFolder() && ! DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }
         
        // delete the resource
        getCms().deleteResource(getParamFile());
        return true;
    }
    
    /**
     * Checks if VFS links are pointing to this resource.
     * 
     * @return true if a one or more VFS links are pointing to this resource
     * @throws CmsException if something goes wrong
     */
    public boolean hasVfsLinks() throws CmsException {
        return getCms().fetchVfsLinksForResource(getParamFile()).size() > 0;
    }
    
}

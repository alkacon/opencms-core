/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsEditPointer.java,v $
 * Date   : $Date: 2004/08/27 15:51:49 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The edit pointer dialog changes the link target of a pointer resource.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/editpointer.jsp
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.5.0
 */
public class CmsEditPointer extends CmsDialog {
    
    /** The dialog type.<p> */
    public static final String DIALOG_TYPE = "newlink";
    
    /** Request parameter name for the link target.<p> */
    public static final String PARAM_LINKTARGET = "linktarget";
    
    /** Stores the value of the link target.<p> */
    private String m_paramLinkTarget;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditPointer(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditPointer(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }    
    
    /**
     * Changes the link target of the pointer.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionChangeLinkTarget() throws JspException {
        try {
            // check the resource lock state
            checkLock(getParamResource());
            // change the link target
            CmsFile editFile = getCms().readFile(getParamResource());
            editFile.setContents(getParamLinkTarget().getBytes());
            getCms().writeFile(editFile);
            // close the dialog window
            actionCloseDialog();
        } catch (CmsException e) {
            // error changing link target, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.newlink"));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }
    
    /**
     * Returns the old link target value of the pointer resource to edit.<p>
     * 
     * @return the old link target value
     */
    public String getOldTargetValue() {
        String linkTarget = "";
        if (getParamLinkTarget() == null || "".equals(getParamLinkTarget())) {
            // this is the initial dialog call, get link target value
            try {
                // get pointer contents
                CmsFile file = getCms().readFile(getParamResource());
                linkTarget = new String(file.getContents());
            } catch (CmsException e) {
                // error reading file, show error dialog
                getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
                setParamErrorstack(e.getStackTraceAsString());
                setParamMessage(key("error.message.newlink"));
                setParamReasonSuggestion(getErrorSuggestionDefault());
                try {
                    getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
                } catch (JspException je) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error including error dialog JSP "+je);
                    }    
                }
            }
        }
        return linkTarget;
    }
    
    /**
     * Returns the link target request parameter value.<p>
     * 
     * @return the link target request parameter value
     */
    public String getParamLinkTarget() {
        return m_paramLinkTarget;
    }

    /**
     * Sets the link target request parameter value.<p>
     * 
     * @param linkTarget the link target request parameter value
     */
    public void setParamLinkTarget(String linkTarget) {
        m_paramLinkTarget = linkTarget;
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
        if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed, change link target
            setAction(ACTION_OK);                            
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
        } else {
            // first call of dialog
            setAction(ACTION_DEFAULT);
            // build title for change link target dialog     
            setParamTitle(key("title.changelink") + ": " + CmsResource.getName(getParamResource()));
        }   
    }
   
}

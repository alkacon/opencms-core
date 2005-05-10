/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsErrorpage.java,v $
 * Date   : $Date: 2005/05/10 07:50:57 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsThrowable;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the error dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/includes/error.jsp
 * </ul>
 *
 * @author  Jan Baudisch (j.baudisch@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.9.1
 */
public class CmsErrorpage extends CmsDialog {
    
    /** The dialog type. */
    public static final String DIALOG_TYPE = "error";

    /** The dialog type. */
    public static final String DIALOG_DETAILS = "details";
    
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsErrorpage(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsErrorpage(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
        if (DIALOG_DETAILS.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {          
            setAction(ACTION_CANCEL);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for copy dialog     
            
        }      
    }
    
    /**
     * Returns the error message to be displayed.<p>
     * 
     * @return the error message to be displayed
     */
    public String getErrorMessage() {
        StringBuffer result = new StringBuffer(512);
        Throwable t = (Throwable)getJsp().getRequest().getAttribute("throwable");
        result.append(getMessage(t));
        for (Throwable cause = t.getCause(); cause != null; cause = cause.getCause()) {
            result.append("<br><br>").append(key("label.reason")).append(": ");
            result.append(getMessage(cause));
        }    
        return result.toString().replaceAll("\n", "<br>");
    }
    
    
    /** 
     * returns the localized Message, if the argument is a CmsException, or
     * the message otherwise.<p>
     * 
     * @param t the Throwable to get the message from
     * @return returns the localized Message, if the argument is a CmsException, or
     * the message otherwise
     */
    String getMessage(Throwable t) {
        if (t instanceof I_CmsThrowable && ((I_CmsThrowable)t).getMessageContainer()!=null) {
            I_CmsThrowable cmsThrowable = (I_CmsThrowable)t;
            return cmsThrowable.getLocalizedMessage(getLocale());
        } else {
            return t.getMessage();
        }
    }
    
    /**
     * returns the StackTrace of the Exception that was thrown as a String.<p>
     * 
     * @return the StackTrace of the Exception that was thrown as a String
     */
    public String getStackTraceAsString() {
        return CmsException.getStackTraceAsString(((Throwable)getJsp().getRequest().getAttribute("throwable")));
    }    
    
    /**
     * Returns the formatted value of the exception.<p>
     * 
     * The error stack is used by the common error screen 
     * that is displayed if an error occurs.<p>
     * 
     * @return the formatted value of the errorstack parameter
     */
    public String getFormattedErrorstack() {

        String exception = CmsException.getStackTraceAsString(((Throwable)getJsp().getRequest().getAttribute("throwable")));
        if (CmsStringUtil.isEmpty(exception)) {
            return "";
        } else {
            exception = CmsStringUtil.escapeJavaScript(exception);
            exception = CmsStringUtil.substitute(exception, ">", "&gt;");
            exception = CmsStringUtil.substitute(exception, "<", "&lt;");
            return "<html><body style='background-color: Window; overflow: scroll;'><pre>"
                + exception
                + "</pre></body></html>";
        }
    }    
}

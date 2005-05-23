/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsErrorpage.java,v $
 * Date   : $Date: 2005/05/23 12:38:35 $
 * Version: $Revision: 1.4 $
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

import org.opencms.main.CmsException;
import org.opencms.main.I_CmsThrowable;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;

/**
 * Provides methods for the error dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/includes/errorpage.jsp
 * </ul>
 *
 * @author  Jan Baudisch (j.baudisch@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 * @since 5.9.1
 */
public final class CmsErrorpage {

    /** 
     * Default constructor (empty), private because this class has only 
     * static methods.<p>
     */
    private CmsErrorpage() {

        // empty
    }

    /**
     * Returns the error message to be displayed.<p>
     * 
     * @param wp the workplace object
     * 
     * @return the error message to be displayed
     */
    public static String getErrorMessage(CmsDialog wp) {
   
        StringBuffer result = new StringBuffer(512);
        Throwable t = (Throwable)wp.getJsp().getRequest().getAttribute("throwable");
        // if a localized message is already set as a parameter, append it.
        if (CmsStringUtil.isNotEmpty(wp.getParamMessage())) {
            result.append(wp.getParamMessage());
            result.append("<br><br>").append(wp.key("label.reason")).append(": ");
        } 
        result.append(getMessage(wp, t));
        // recursively append all error reasons to the message
        for (Throwable cause = t.getCause(); cause != null; cause = cause.getCause()) {
            result.append("<br><br>").append(wp.key("label.reason")).append(": ");
            result.append(getMessage(wp, cause));
        }
        return result.toString().replaceAll("\n", "<br>");
    }

    /**
     * Returns the formatted value of the exception.<p>
     * 
     * The error stack is used by the common error screen 
     * that is displayed if an error occurs.<p>
     * 
     * @param wp the workplace object
     * 
     * @return the formatted value of the errorstack parameter
     */
    public static String getFormattedErrorstack(CmsDialog wp) {

        String exception = CmsException.getStackTraceAsString(((Throwable)wp.getJsp().getRequest().getAttribute(
            "throwable")));
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

    /** 
     * returns the localized Message, if the argument is a CmsException, or
     * the message otherwise.<p>
     * 
     * @param t the Throwable to get the message from
     * @param wp the workplace object
     * 
     * @return returns the localized Message, if the argument is a CmsException, or
     * the message otherwise
     */
    public static String getMessage(CmsDialog wp, Throwable t) {

        if (t instanceof I_CmsThrowable && ((I_CmsThrowable)t).getMessageContainer() != null) {
            I_CmsThrowable cmsThrowable = (I_CmsThrowable)t;
            return cmsThrowable.getLocalizedMessage(wp.getLocale());
        } else {
            return t.getMessage();
        }
    }

    /**
     * returns the StackTrace of the Exception that was thrown as a String.<p>
     * 
     * @param wp the workplace object
     * 
     * @return the StackTrace of the Exception that was thrown as a String
     */
    public static String getStackTraceAsString(CmsDialog wp) {

        return CmsException.getStackTraceAsString(((Throwable)wp.getJsp().getRequest().getAttribute("throwable")));
    }
}

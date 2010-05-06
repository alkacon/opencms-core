/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsClientStringUtil.java,v $
 * Date   : $Date: 2010/05/06 09:35:11 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.util;

/**
 * Additional string related helper methods.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.util.CmsStringUtil
 */
public final class CmsClientStringUtil {

    /**
     * Prevent instantiation.<p> 
     */
    private CmsClientStringUtil() {

        // empty
    }

    /**
     * Returns the exception message.<p>
     * 
     * @param t the exception to get the message for
     * 
     * @return the exception message
     */
    public static String getMessage(Throwable t) {

        String message = t.getLocalizedMessage();
        if (message == null) {
            message = t.getMessage();
        }
        if (message == null) {
            message = t.getClass().getName();
        }
        return message;
    }

    /**
     * Returns the stack trace of the Throwable as a string.<p>
     * 
     * @param t the Throwable for which the stack trace should be returned
     * @param separator the separator between the lines of the stack trace 
     * 
     * @return a string representing a stack trace 
     */
    public static String getStackTrace(Throwable t, String separator) {

        String result = "";
        for (StackTraceElement elem : t.getStackTrace()) {
            result += elem.toString();
            result += separator;
        }
        return result;
    }

    /**
     * The parseFloat() function parses a string and returns a float.<p>
     * 
     * Only the first number in the string is returned. Leading and trailing spaces are allowed.
     * 
     * @param str the string to be parsed
     * 
     * @return the parsed number
     */
    public static native double parseFloat(String str) /*-{
        var ret = parseFloat(str, 10);
        return ret;
    }-*/;

    /**
     * The parseInt() function parses a string and returns an integer.<p>
     * 
     * Only the first number in the string is returned. Leading and trailing spaces are allowed.
     * If the first character cannot be converted to a number, parseInt() returns zero.<p>
     * 
     * @param str the string to be parsed
     * 
     * @return the parsed number
     */
    public static native int parseInt(String str) /*-{
        var ret = parseInt(str, 10);
        if (isNaN(ret)) {
        return 0;
        } 
        return ret;
    }-*/;
}
/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 
package org.opencms.gwt.shared;

/** 
 * Client-side log implementation.
 */
public class CmsGwtLog {

    /**
     * Logs a message to the browser console if possible.<p>
     *
     * @param message the message to log
     */
    public static native void log(String message) /*-{
        var cns = $wnd.top.console;
        if (cns && cns.log) {
            cns.log(message);
        }
    }-*/;

    /**
     * Logs a message with the current stack trace to the browser console if possible.
     * 
     * @param message the message to log
     */
    public static native void trace(String message) /*-{
        var cns = $wnd.top.console;
        if (cns && cns.trace) {
            cns.trace(message);
        }
    }-*/;

}

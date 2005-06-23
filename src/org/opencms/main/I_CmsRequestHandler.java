/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/I_CmsRequestHandler.java,v $
 * Date   : $Date: 2005/06/23 10:47:19 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.main;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Describes an OpenCms request handler.<p>
 * 
 * Request handlers are used for special requests to OpenCms 
 * that should NOT be mapped to a VFS resource.
 * A request handler URI always start with <code>/handle</code> and then 
 * one or more possible handler names as defined with the {@link #getHandlerNames()} 
 * method.<p>
 * 
 * For example, if a registerd request handler has the name <code>"MyName"</code>,
 * any request (in a simple setup) to <code>/opencms/opencms/handlerMyName...</code> will directly be transfered 
 * to the {@link #handle(HttpServletRequest, HttpServletResponse, String)} method of this 
 * handler.<p>
 * 
 * In essence, the request handlers are like simplified mini-servlets that run inside OpenCms. 
 * Of course they are not intended as replacements for real servlets.
 * In case you require sophisticated lifecycle support use a genuine servlet instead.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsRequestHandler {

    /**
     * Returns the handler name.<p>
     * 
     * @return the handler name
     */
    String[] getHandlerNames();

    /**
     * Handles an OpenCms request.<p>
     * 
     * @param req the current request
     * @param res the current response 
     * @param name the handler name to invoke
     * @throws ServletException in case an error occurs
     * @throws IOException in case an error occurs
     */
    void handle(HttpServletRequest req, HttpServletResponse res, String name) throws IOException, ServletException;
}

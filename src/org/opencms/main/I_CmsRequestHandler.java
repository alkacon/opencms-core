/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/I_CmsRequestHandler.java,v $
 * Date   : $Date: 2004/01/06 09:45:59 $
 * Version: $Revision: 1.2 $
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

package org.opencms.main;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Describes an OpenCms request handler
 */
public interface I_CmsRequestHandler {
    
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
    
    /**
     * Returns the handler name.<p>
     * 
     * @return the handler name
     */
    String[] getHandlerNames();
}


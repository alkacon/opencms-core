/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/OpenCmsListener.java,v $
 * Date   : $Date: 2005/02/17 12:44:35 $
 * Version: $Revision: 1.5 $
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

package org.opencms.main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.5 $
 * @since 5.1
 */
public class OpenCmsListener implements ServletContextListener {

    private static final boolean DEBUG = false;

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event) {
        if (DEBUG) {
            System.err.println(this.getClass().getName() + ".contextInitialized() called!");
            System.err.println("Context is : " + event.getServletContext().getServletContextName());
            System.err.println("Server info: " + event.getServletContext().getServerInfo());
            System.err.println("Real path  : " + event.getServletContext().getRealPath("/"));
        }  
        
        // upgrade the runlevel
        OpenCmsCore.getInstance().upgradeRunlevel(event.getServletContext());
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event) {
        if (DEBUG) {
            System.err.println(this.getClass().getName() + ".contextDestroyed() called!");        
        }
        
        // destroy the OpenCms instance
        OpenCmsCore.getInstance().destroy();
    }   


}

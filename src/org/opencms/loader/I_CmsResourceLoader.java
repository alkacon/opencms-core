/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/I_CmsResourceLoader.java,v $
 * Date   : $Date: 2003/07/21 14:22:47 $
 * Version: $Revision: 1.4 $
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

package org.opencms.loader;

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import source.org.apache.java.util.Configurations;

/**
 * This interface describes a resource loader for OpenCms, 
 * a class that can load a resource from the VFS, 
 * process it's contents and deliver the result to the user.<p>
 *
 * The I_CmsResourceLoader operates with Request and Response in 
 * much the same way as a standard Java web application.<p>
 * 
 * The I_CmsResourceLoader is closely related to the {@link com.opencms.launcher.I_CmsLauncher}
 * interface. In essence, both interfaces serve the same purpose. 
 * However, the I_ResourceLoader is much closer related to the standard 
 * Java Servlet API then the I_CmsLauncher, which makes it easier to 
 * understand for the novice OpenCms programmer. That way, a programmer
 * will hopefully need less time to get productive with OpenCms.<p>
 * 
 * This interface uses a standard servlet
 * {@link javax.servlet.http.HttpServletRequestWrapper} / {@link javax.servlet.http.HttpServletResponseWrapper}
 * that provide access to a special implementation of the {@link javax.servlet.RequestDispatcher}.
 * The handling of the output written to the response is done by this 
 * dispatcher. The results are then passed back to OpenCms which 
 * will deliver them to the requesting user.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.4 $
 * @since FLEX alpha 1
 * 
 * @see com.opencms.flex.cache.CmsFlexRequest
 * @see com.opencms.flex.cache.CmsFlexResponse
 * @see com.opencms.flex.cache.CmsFlexRequestDispatcher
 */
public interface I_CmsResourceLoader {
    
    /** The name of the VFS property that steers the caching */
    String C_LOADER_CACHEPROPERTY = "cache";
    
    /** The name of the VFS property that steers the streaming */
    String C_LOADER_STREAMPROPERTY = "stream";
    
    /** Name of FlexCache runtime property */
    String C_LOADER_CACHENAME = "flex.cache";       
               
    /** 
     * Initialize the ResourceLoader.<p>
     *
     * @param openCms the initialized OpenCms object
     * @param conf the OpenCms configuration 
     */
    void init(A_OpenCms openCms, Configurations conf);
    
    /** Destroy this ResourceLoder */
    void destroy();
        
    /** 
     * Returns a String describing the ResourceLoader.<p>
     * 
     * @return a String describing the ResourceLoader
     */
    String getResourceLoaderInfo();
    
    /**
     * Returns the id of the ResourceLoader.<p>
     * 
     * @return the id of the ResourceLoader
     */
    int getLoaderId();
      
    /**
     * Basic top-page processing method for a I_CmsResourceLoader,
     * this method is called if the page is called as a sub-element 
     * on a page not already loded with a I_CmsResourceLoader,
     * which most often would be an I_CmsLauncher then.<p>
     *
     * @param cms the initialized CmsObject which provides user permissions
     * @param file the requested OpenCms VFS resource
     * @param req the original servlet request
     * @param res the original servlet response
     * 
     * @throws ServletException might be thrown in the process of including the JSP 
     * @throws IOException might be thrown in the process of including the JSP 
     * 
     * @see #service(CmsObject, CmsResource, ServletRequest, ServletResponse)
     */
    void load(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException;
    
    /**
     * Exports the contents of the requested file and it's subfiles.<p>
     * 
     * @param cms the initialized CmsObject which provides user permissions
     * @param file the requested OpenCms VFS resource
     * @throws CmsException in case of errors during export
     */
    void export(CmsObject cms, CmsFile file) 
    throws CmsException;
    
    /**
     * Does the job of including the requested resource, 
     * this method is called directly if the element is 
     * called as a sub-element from another I_CmsResourceLoader.<p>
     * 
     * @param cms used to access the OpenCms VFS
     * @param file the reqested JSP file resource in the VFS
     * @param req the current request
     * @param res the current response
     * 
     * @throws ServletException might be thrown in the process of including the JSP 
     * @throws IOException might be thrown in the process of including the JSP 
     * 
     * @see com.opencms.flex.cache.CmsFlexRequestDispatcher
     */   
    void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res) 
    throws ServletException, IOException;
}
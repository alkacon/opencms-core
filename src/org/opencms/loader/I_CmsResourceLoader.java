/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/I_CmsResourceLoader.java,v $
 * Date   : $Date: 2004/01/06 09:46:26 $
 * Version: $Revision: 1.10 $
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

import com.opencms.core.CmsException;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This interface describes a resource loader for OpenCms, 
 * a class that can load a resource from the VFS, 
 * process it's contents and deliver the result to the user.<p>
 *
 * The I_CmsResourceLoader operates with Request and Response in 
 * much the same way as a standard Java web application.<p>
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
 * @version $Revision: 1.10 $
 * @since FLEX alpha 1
 * 
 * @see org.opencms.flex.CmsFlexRequest
 * @see org.opencms.flex.CmsFlexResponse
 * @see org.opencms.flex.CmsFlexRequestDispatcher
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
     * @param configuration the OpenCms configuration 
     */
    void init(ExtendedProperties configuration);
    
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
     * on a page not already loded with a I_CmsResourceLoader.<p>
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
     * Exports the contents of the requested file and it's sub-elements.<p>
     *
     * @param cms the initialized CmsObject which provides user permissions
     * @param file the requested OpenCms VFS resource
     * @param exportStream the stream to write the exported content to
     * @param req the original servlet request
     * @param res the original servlet response
     * 
     * @throws ServletException might be thrown in the process of including the JSP 
     * @throws IOException might be thrown in the process of including the JSP 
     * @throws CmsException might be thrown if errors during the static export occur 
     */    
    void export(CmsObject cms, CmsFile file, OutputStream exportStream, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException, CmsException;
        
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
     * @see org.opencms.flex.CmsFlexRequestDispatcher
     */   
    void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res) 
    throws ServletException, IOException;
}
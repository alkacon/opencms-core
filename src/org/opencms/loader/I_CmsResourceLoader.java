/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/I_CmsResourceLoader.java,v $
 * Date   : $Date: 2004/06/21 09:56:59 $
 * Version: $Revision: 1.27 $
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

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 * @version $Revision: 1.27 $
 * @since FLEX alpha 1
 * 
 * @see org.opencms.flex.CmsFlexRequest
 * @see org.opencms.flex.CmsFlexResponse
 * @see org.opencms.flex.CmsFlexRequestDispatcher
 */
public interface I_CmsResourceLoader extends I_CmsConfigurationParameterHandler {
    
    /** The name of the VFS property that controls the caching. */
    String C_LOADER_CACHEPROPERTY = "cache";
    
    /** The name of the VFS property that controls the streaming. */
    String C_LOADER_STREAMPROPERTY = "stream";
    
    /** 
     * Destroys this ResourceLoder.<p>
     */
    void destroy();
     
    /**
     * Dumps the processed content of the the requested file (and it's sub-elements) to a String.<p>
     * 
     * Dumping the content is like calling "load" where the result is 
     * not written to the response stream, but to the returned byte array.
     * Dumping is different from an export because the export might actually require 
     * that the content is handled or modified in a special way, or set special http headers.<p>
     * 
     * Moreover, if the page type is template based, calling "dump" will not trigger the 
     * template but directly deliver the contents from the selected resource.<p>
     * 
     * @param cms used to access the OpenCms VFS
     * @param resource the reqested resource in the VFS
     * @param element the element in the file to display
     * @param locale the locale to display
     * @param req the servlet request
     * @param res the servlet response
     * 
     * @return the content of the processed file
     * 
     * @throws ServletException might be thrown by the servlet environment
     * @throws IOException might be thrown by the servlet environment
     * @throws CmsException in case of errors acessing OpenCms functions
     */
    byte[] dump(CmsObject cms, CmsResource resource, String element, Locale locale, HttpServletRequest req, HttpServletResponse res) 
    throws  ServletException, IOException, CmsException;
    
    /**
     * Static exports the contents of the requested file and it's sub-elements.<p>
     *
     * During static export, the resource content may be written to 2 streams: 
     * The export stream, and the http response output stream.
     * Which stream is actually used depends wether the export is in "on demand" 
     * or "after publish" mode. In "on demand" mode, the resource needs to 
     * be written both to the response stream and to the file stream. In 
     * "after publish" mode, it's usually only written to the file stream, 
     * but sometimes it's required to write to the response stream as well.<p> 
     *
     * @param cms the initialized CmsObject which provides user permissions
     * @param resource the requested OpenCms VFS resource
     * @param req the servlet request
     * @param res the servlet response
     * 
     * @throws ServletException might be thrown in the process of including the sub element
     * @throws IOException might be thrown in the process of including the sub element
     * @throws CmsException in case something goes wrong
     * @return the contents to export, or <code>null</code> if no export is required
     */    
    byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException, CmsException;
    
    /**
     * Returns the id of the ResourceLoader.<p>
     * 
     * @return the id of the ResourceLoader
     */
    int getLoaderId();
        
    /** 
     * Returns a String describing the ResourceLoader.<p>
     * 
     * @return a String describing the ResourceLoader
     */
    String getResourceLoaderInfo();    
    
    /**
     * Signals if the loader implementation supports static export of resources.<p>
     * 
     * @return true if static export is supported, false otherwise
     */
    boolean isStaticExportEnabled();

    /**
     * Signals if the loader implementation requires processing during static export of resources.<p>
     * 
     * @return true if static export processing is required, false otherwise
     */
    boolean isStaticExportProcessable();
    
    /**
     * Signals if the loader implementation is usable for creating templates.<p>
     * 
     * @return true  if the loader implementation is usable for creating templates, false otherwise
     */
    boolean isUsableForTemplates();
    
    /**
     * Signals if a loader that supports templates must be invoked on the 
     * template URI or the resource URI.<p>
     * 
     * @return true if the resource URI is to be used, false if the template URI is to be used
     */
    boolean isUsingUriWhenLoadingTemplate();
      
    /**
     * Basic top-page processing method for a I_CmsResourceLoader,
     * this method is called if the page is called as a sub-element 
     * on a page not already loded with a I_CmsResourceLoader.<p>
     *
     * @param cms the initialized CmsObject which provides user permissions
     * @param resource the requested OpenCms VFS resource
     * @param req the servlet request
     * @param res the servlet response
     * 
     * @throws ServletException might be thrown by the servlet environment
     * @throws IOException might be thrown by the servlet environment
     * @throws CmsException in case of errors acessing OpenCms functions
     * 
     * @see #service(CmsObject, CmsResource, ServletRequest, ServletResponse)
     */
    void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException, CmsException;
        
    /**
     * Does the job of including the requested resource, 
     * this method is called directly if the element is 
     * called as a sub-element from another I_CmsResourceLoader.<p>
     * 
     * @param cms used to access the OpenCms VFS
     * @param resource the reqested resource in the VFS
     * @param req the servlet request
     * @param res the servlet response
     * 
     * @throws ServletException might be thrown by the servlet environment
     * @throws IOException might be thrown by the servlet environment
     * @throws CmsException in case of errors acessing OpenCms functions
     * 
     * @see org.opencms.flex.CmsFlexRequestDispatcher
     */   
    void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res) 
    throws ServletException, IOException, CmsException;
}
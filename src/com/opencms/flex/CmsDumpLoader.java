/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/CmsDumpLoader.java,v $
 * Date   : $Date: 2002/12/15 14:23:44 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * First created on 14. April 2002, 19:10
 */


package com.opencms.flex;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.flex.cache.CmsFlexCache;
import com.opencms.flex.cache.CmsFlexRequest;
import com.opencms.flex.cache.CmsFlexResponse;

import java.io.IOException;

import javax.servlet.ServletException;


/**
 * Dump loader for binary or other unprocessed resource types.<p>
 * 
 * This loader is used to deliver static sub-elements of pages processed 
 * by other loaders. 
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.9 $
 */
public class CmsDumpLoader extends com.opencms.launcher.CmsDumpLauncher implements I_CmsResourceLoader {
    
    /** The CmsFlexCache used to store generated cache entries in */
    private static CmsFlexCache m_cache;
    
    /** Flag for debugging output. Set to 9 for maximum verbosity. */ 
    private static final int DEBUG = 0;
    
    /**
     * The constructor of the class is empty and does nothing.
     */
    public CmsDumpLoader() {
        // NOOP
    }
        
    /** Destroy this ResourceLoder, this is a NOOP so far.  */
    public void destroy() {
        // NOOP
    }
    
    /**
     * Return a String describing the ResourceLoader,
     * which is <code>"A simple dump loader that extends from com.opencms.launcher.CmsDumpLauncher"</code>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        return "A simple dump loader that extends from com.opencms.launcher.CmsDumpLauncher";
    }
    
    /** 
     * Initialize the ResourceLoader,
     * not much done here, only the FlexCache is initialized for dump elements.
     *
     * @param openCms An OpenCms object to use for initalizing.
     */
    public void init(A_OpenCms openCms) {
        m_cache = (CmsFlexCache)openCms.getRuntimeProperty(C_LOADER_CACHENAME);  
              
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_FLEX_LOADER)) 
            A_OpenCms.log(I_CmsLogChannels.C_FLEX_LOADER, this.getClass().getName() + " initialized!");        
    }
    
    
    /**
     * Basic top-page processing method for this I_CmsResourceLoader,
     * this method is called if the page is called as a sub-element 
     * on a page not already loded with a I_CmsResourceLoader,
     * which most often would be a I_CmsLauncher then.
     *
     * @param cms The initialized CmsObject which provides user permissions
     * @param file The requested OpenCms VFS resource
     * @param req The original servlet request
     * @param res The original servlet response
     * 
     * @throws ServletException might be thrown in the process of including the JSP 
     * @throws IOException might be thrown in the process of including the JSP 
     * 
     * @see I_CmsResourceLoader
     * @see com.opencms.launcher.I_CmsLauncher
     * @see #service(CmsObject, CmsResource, CmsFlexRequest, CmsFlexResponse)
     */
    public void load(com.opencms.file.CmsObject cms, com.opencms.file.CmsFile file, javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) 
    throws ServletException, IOException {           
        CmsFlexRequest w_req = new CmsFlexRequest(req, file, m_cache, cms); 
        CmsFlexResponse w_res = new CmsFlexResponse(res, false, cms.getRequestContext().getEncoding());
        service(cms, file, w_req, w_res);
    }   
    
    /**
     * Does the job of dumping the contents of the requested file to the
     * output stream, this method is called directly if the element is 
     * called as a sub-element from another I_CmsResourceLoader.
     * 
     * @param cms Used to access the OpenCms VFS
     * @param file The reqested JSP file resource in the VFS
     * @param req The current request
     * @param res The current response
     * 
     * @throws ServletException might be thrown in the process of including the JSP 
     * @throws IOException might be thrown in the process of including the JSP 
     * 
     * @see com.opencms.flex.cache.CmsFlexRequestDispatcher
     */     
    public void service(CmsObject cms, CmsResource file, CmsFlexRequest req, CmsFlexResponse res)
    throws ServletException, IOException {
        long timer1 = 0;
        if (DEBUG > 0) {
            timer1 = System.currentTimeMillis();        
            System.err.println("========== DumpLoader loading: " + file.getAbsolutePath());            
        }
        try {
            res.getOutputStream().write(req.getCmsObject().readFile(file.getAbsolutePath()).getContents());
        }  catch (CmsException e) {
            System.err.println("Error in CmsDumpLoader: " + e.toString());
            if (DEBUG > 0) System.err.println(com.opencms.util.Utils.getStackTrace(e));
            throw new ServletException("Error in CmsDumpLoader processing", e);    
        }
        if (DEBUG > 0) {
            long timer2 = System.currentTimeMillis() - timer1;        
            System.err.println("========== Time delivering dump for " + file.getAbsolutePath() + ": " + timer2 + "ms");            
        }
    }
    
    // --------------------------- Overloaded methods from launcher interface
    /*
    public void setOpenCms(A_OpenCms openCms) {
        init(openCms);
    } 
    */   
 }

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/CmsXmlTemplateLoader.java,v $
 * Date   : $Date: 2002/12/13 17:38:12 $
 * Version: $Revision: 1.11 $
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

import com.opencms.core.A_OpenCms;
import com.opencms.core.I_CmsRequest;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.flex.cache.CmsFlexCache;
import com.opencms.flex.cache.CmsFlexRequest;
import com.opencms.flex.cache.CmsFlexResponse;
import com.opencms.launcher.CmsXmlLauncher;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


/**
 * Implementation of the {@link I_CmsResourceLoader} and 
 * the {@link com.opencms.launcher.I_CmsLauncher} interface for 
 * XMLTemplates.<p>
 * 
 * This implementation can deliver XMLTemplates directly since it extends
 * the {@link com.opencms.launcher.CmsXmlLauncher}. It is also usable to include 
 * XMLTemplates as sub-elements on a JSP page since it implements the
 * {@link I_CmsResourceLoader} interface. 
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.11 $
 * @since FLEX alpha 1
 */
public class CmsXmlTemplateLoader extends CmsXmlLauncher implements I_CmsResourceLoader {
    
    /** The CmsFlexCache used to store generated cache entries in */
    private static CmsFlexCache m_cache;

    /** Required to access the XMLTemplate load mechanism */    
    private A_OpenCms m_openCms = null;    

    /** Flag for debugging output. Set to 9 for maximum verbosity. */ 
    private static final int DEBUG = 0;
        
    /**
     * The constructor of the class is empty and does nothing.
     */
    public CmsXmlTemplateLoader() {
        // NOOP
    }
        
    /** Destroy this ResourceLoder, this is a NOOP so far.  */
    public void destroy() {
        // NOOP
    }
    
    /**
     * Return a String describing the ResourceLoader,
     * which is <code>"A XMLTemplate loader that extends from com.opencms.launcher.CmsXmlLauncher"</code>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        return "A XMLTemplate loader that extends from com.opencms.launcher.CmsXmlLauncher";
    }
    
    /** 
     * Initialize the ResourceLoader, the OpenCms parameter is
     * saved here for later access to <code>generateOutput()</code>.
     * 
     * @param openCms used to access <code>generateOutput()</code> later
     * 
     * @see com.opencms.launcher.CmsXmlLauncher#generateOutput(CmsObject, CmsFile, String, I_CmsRequest, A_OpenCms)
     */
    public void init(A_OpenCms openCms) {
        // This must be saved for call to this.generateOutput();
        m_openCms = openCms;
        m_cache = (CmsFlexCache)openCms.getRuntimeProperty(C_LOADER_CACHENAME);        
        if (C_LOGGING && A_OpenCms.isLogging(C_FLEX_LOADER)) 
            A_OpenCms.log(C_FLEX_LOADER, this.getClass().getName() + " initialized!");     
    }
    
    /**
     * Basic top-page processing method for this I_CmsResourceLoader,
     * this method is called if the page is called as a sub-element 
     * on a page not already loded with a I_CmsResourceLoader,
     * which most often would be an I_CmsLauncher then.
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
     * @see #service(CmsObject, CmsResource, CmsFlexRequest, CmsFlexResponse)
     */
    public void load(com.opencms.file.CmsObject cms, com.opencms.file.CmsFile file, javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) 
    throws ServletException, IOException {    
        CmsFlexRequest w_req; 
        CmsFlexResponse w_res;
        if (req instanceof CmsFlexRequest) {
            w_req = (CmsFlexRequest)req; 
        } else {
            w_req = new CmsFlexRequest(req, file, m_cache, cms); 
        }        
        if (res instanceof CmsFlexResponse) {
            w_res = (CmsFlexResponse)res;              
        } else {
            w_res = new CmsFlexResponse(res, false);
        }                
        service(cms, file, w_req, w_res);
    }
    
    /**
     * Does the job of including the XMLTemplate, 
     * this method is called directly if the element is 
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
            System.err.println("========== XmlTemplateLoader loading: " + file.getAbsolutePath());            
        }
        try {                        
            // get the CmsRequest
            I_CmsRequest cms_req = cms.getRequestContext().getRequest();
            byte[] result = null;
            
            HttpServletRequest originalreq = (HttpServletRequest)cms_req.getOriginalRequest();            
            com.opencms.file.CmsFile fx = req.getCmsObject().readFile(file.getAbsolutePath());            
            
            // fake the called URI (otherwise XMLTemplate / ElementCache would not work)
            cms.getRequestContext().setUri(fx.getAbsolutePath());            
            cms_req.setOriginalRequest(req);
            // process the included XMLTemplate
            result = generateOutput(cms, fx, fx.getLauncherClassname(), cms_req, m_openCms);            
            // reset the called URI to the originally requested resource
            cms_req.setOriginalRequest(originalreq);
            cms.getRequestContext().setUri(null);

            if(result != null) {
                res.getOutputStream().write(result);
            }        
        }  catch (Exception e) {
            System.err.println("Error in CmsXmlTemplateLoader: " + e.toString());
            if (DEBUG > 0) e.printStackTrace(System.err);
            throw new ServletException("Error in CmsXmlTemplateLoader processing", e);       
        }
        if (DEBUG > 0) {
            long timer2 = System.currentTimeMillis() - timer1;        
            System.err.println("========== Time delivering XmlTemplate for " + file.getAbsolutePath() + ": " + timer2 + "ms");            
        }
    }   
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/CmsDumpLoader.java,v $
 * Date   : $Date: 2002/09/12 08:58:15 $
 * Version: $Revision: 1.5 $
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

import com.opencms.core.*;
import com.opencms.file.*;

import com.opencms.flex.cache.*;

import java.io.IOException;

import javax.servlet.ServletException;


/**
 * Description of the class CmsDumpLoader here.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.5 $
 */
public class CmsDumpLoader extends com.opencms.launcher.CmsDumpLauncher implements I_CmsResourceLoader {
    
    private static CmsFlexCache m_cache;    
    
    private static int DEBUG = 0;
    
    // ---------------------------- Implementation of interface com.opencms.launcher.I_CmsLauncher          
    
    /** Destroy this ResourceLoder  */
    public void destroy() {
        // NOOP
    }
    
    /** Return a String describing the ResourceLoader  */
    public String getResourceLoaderInfo() {
        return "A dump loader that extends from com.opencms.launcher.CmsDumpLauncher";
    }
    
    /** Initialize the ResourceLoader  */
    public void init(A_OpenCms openCms) {
        m_cache = (CmsFlexCache)openCms.getRuntimeProperty(this.C_LOADER_CACHENAME);  
              
        log(this.getClass().getName() + " initialized!");        
    }
    
    /** Basic top-call processing method for Cms */
    public void load(com.opencms.file.CmsObject cms, com.opencms.file.CmsFile file, javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) 
    throws ServletException, IOException {           
        CmsFlexRequest w_req = new CmsFlexRequest(req, file, m_cache, cms); 
        CmsFlexResponse w_res = new CmsFlexResponse(res, false);
        service(cms, file, w_req, w_res);
    }   
    
    // --------------------------- Overloaded methods from launcher interface

    public void setOpenCms(A_OpenCms openCms) {
        init(openCms);
    }
    
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
    
    private void log(String message) {
        if (com.opencms.boot.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
            com.opencms.boot.CmsBase.log(com.opencms.boot.CmsBase.C_FLEX_LOADER, "[CmsDumpLoader] " + message);
        }
    }
 }

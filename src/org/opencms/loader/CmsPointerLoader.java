/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsPointerLoader.java,v $
 * Date   : $Date: 2003/09/12 17:38:06 $
 * Version: $Revision: 1.8 $
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

import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;
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

import source.org.apache.java.util.Configurations;


/**
 * Loader for "pointers" to resources in the VFS or to external resources.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.8 $
 */
public class CmsPointerLoader implements I_CmsResourceLoader {
    
    /**
     * The html-code for returning the export file for external links
     */
    private static String C_EXPORT_PREFIX = "<html>\n<head>\n<meta http-equiv="+'"'+"refresh"+'"'+" content="+'"'+"0; url=";
    private static String C_EXPORT_SUFFIX = '"'+">\n</head>\n<body></body>\n</html>";
    
    /** The id of this loader */
    public static final int C_RESOURCE_LOADER_ID = 4;    
    
    /**
     * The constructor of the class is empty and does nothing.<p>
     */
    public CmsPointerLoader() {
        // NOOP
    }
        
    /** 
     * Destroy this ResourceLoder, this is a NOOP so far.<p>
     */
    public void destroy() {
        // NOOP
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(com.opencms.file.CmsObject, com.opencms.file.CmsFile)
     */
    public void export(CmsObject cms, CmsFile file) throws CmsException {
        try {
            String pointer = new String(file.getContents());        
            OutputStream responsestream = cms.getRequestContext().getResponse().getOutputStream();
            responsestream.write(C_EXPORT_PREFIX.getBytes());
            responsestream.write(pointer.getBytes());
            responsestream.write(C_EXPORT_SUFFIX.getBytes());
            responsestream.close();
        } catch (Throwable t) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) { 
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, this.getClass().getName() + " Error during static export of " + cms.readAbsolutePath(file) + ": " + t.getMessage());
            }        
        }        
    }    

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, CmsException {
        // TODO: Auto-generated method stub
        return null;
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {
        return C_RESOURCE_LOADER_ID;
    }            
    
    /**
     * Return a String describing the ResourceLoader,
     * which is <code>"The OpenCms default resource loader for pointers"</code><p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        return "The OpenCms default resource loader for pointers";
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#init(source.org.apache.java.util.Configurations)
     */
    public void init(Configurations conf) {
        if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) { 
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Loader init          : " + this.getClass().getName() + " initialized!");
        }        
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException {        
        String pointer = new String(file.getContents());
        if (pointer == null || "".equals(pointer.trim())) {
            throw new ServletException("Invalid pointer file " + file.getName());
        }
        if (pointer.startsWith("/")) {
            try {
                CmsFile target = cms.readFile(pointer);
                OpenCms.getLoaderManager().getLoader(target.getLoaderId()).load(cms, target, req, res);
            } catch (CmsException e) {
                throw new ServletException("Could not load pointed file from " + file.getName());
            }
        } else {
            res.sendRedirect(pointer);
        }
    }   
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(com.opencms.file.CmsObject, com.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */ 
    public void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res)
    throws ServletException, IOException {
        throw new RuntimeException("service() not a supported operation for resources of type " + this.getClass().getName());  
    }
 }

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsDumpLoader.java,v $
 * Date   : $Date: 2003/11/10 08:12:58 $
 * Version: $Revision: 1.20 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
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
 * Dump loader for binary or other unprocessed resource types.<p>
 * 
 * This loader is also used to deliver static sub-elements of pages processed 
 * by other loaders.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.20 $
 */
public class CmsDumpLoader implements I_CmsResourceLoader {
    
    /** The id of this loader */
    public static final int C_RESOURCE_LOADER_ID = 1;    
    
    /** Flag for debugging output. Set to 9 for maximum verbosity. */ 
    private static final int DEBUG = 0;
    
    /**
     * The constructor of the class is empty and does nothing.<p>
     */
    public CmsDumpLoader() {
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
    public void export(CmsObject cms, CmsFile file) {
        try {    
            OutputStream exportStream = cms.getRequestContext().getResponse().getOutputStream();
            exportStream.write(file.getContents());
            exportStream.close();
        } catch (Throwable t) {
            if (OpenCms.getLog(this).isErrorEnabled()) { 
                OpenCms.getLog(this).error("Error during static export of " + cms.readAbsolutePath(file), t);
            }         
        }        
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void export(CmsObject cms, CmsFile file, OutputStream exportStream, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (exportStream != null) {
            exportStream.write(file.getContents());
        }
        load(cms, file, req, res);  
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {
        return C_RESOURCE_LOADER_ID;
    }            
    
    /**
     * Return a String describing the ResourceLoader,
     * which is <code>"The OpenCms default resource loader for unprocessed files"</code><p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        return "The OpenCms default resource loader for unprocessed files";
    }
    
    /** 
     * Initialize the ResourceLoader,
     * not much done here, only the FlexCache is initialized for dump elements.<p>
     *
     * @param configuration the OpenCms configuration 
     */
    public void init(ExtendedProperties configuration) {        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) { 
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Loader init          : " + this.getClass().getName() + " initialized!");
        }        
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException {

        // check if we can send a 304 "Not Modified" header
        if (file.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            // never use 304 when the file has somehow changed (can only be true in an offline project)      
            if (req.getDateHeader("If-Modified-Since") == file.getDateLastModified()) {
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }
           
        // set content length header
        res.setContentLength(file.getContents().length);
        // set date last modified header
        res.setDateHeader("Last-Modified", file.getDateLastModified());
        // set expire and chache headers        
        int expireTime;
        if (cms.getRequestContext().currentProject().isOnlineProject()) {
            // allow proxy caching of 1 day
            // TODO: Allow this value to be configured somehow  
            expireTime = 86400;        
        } else {
            // allow proxy caching of 10 seconds only (required for PDF preview in offline project)
            expireTime = 10;
        }
        res.setHeader("Cache-Control", "max-age=" + expireTime); // HTTP 1.1
        res.setDateHeader("Expires", System.currentTimeMillis() + (expireTime * 1000)); // HTTP 1.0
        
        // now send the file to the client          
        service(cms, file, req, res);        
    }
        
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(com.opencms.file.CmsObject, com.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res)
    throws ServletException, IOException {
        byte[] content = null;
        if (file instanceof CmsFile) {
            content = ((CmsFile)file).getContents();
        } else {
            String filename = cms.readAbsolutePath(file);
            try {                
                content = cms.readFile(filename).getContents();
            }  catch (CmsException e) {
                if (DEBUG > 0) {
                    System.err.println(com.opencms.util.Utils.getStackTrace(e));
                }
                throw new ServletException("Error in CmsDumpLoader while processing " + filename, e);    
            }
        }
        res.getOutputStream().write(content);
    }
 }

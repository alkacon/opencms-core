/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsDumpLoader.java,v $
 * Date   : $Date: 2004/02/26 08:42:25 $
 * Version: $Revision: 1.32 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;

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
 * @version $Revision: 1.32 $
 */
public class CmsDumpLoader implements I_CmsResourceLoader {
    
    /** The id of this loader */
    public static final int C_RESOURCE_LOADER_ID = 1;
    
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
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.io.OutputStream, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void export(CmsObject cms, CmsResource resource, OutputStream exportStream, HttpServletRequest req, HttpServletResponse res) 
    throws IOException, CmsException {
        CmsFile file = CmsFile.upgrade(resource, cms);
        
        if (exportStream != null) {
            exportStream.write(file.getContents());
        }
        
        // overwrite headers if set as default
        for (Iterator i = OpenCms.getStaticExportManager().getExportHeaders().listIterator(); i.hasNext();) {
            String header = (String)i.next();
            
            // set header only if format is "key: value"
            String parts[] = CmsStringSubstitution.split(header, ":"); 
            if (parts.length == 2) {
                res.setHeader(parts[0], parts[1]);
            }
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
     * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res) 
    throws IOException, CmsException {

        CmsFile file = CmsFile.upgrade(resource, cms);
        
        // check if we can send a 304 "Not Modified" header
        if (file.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            // never use 304 when the file has somehow changed (can only be true in an offline project)      
            if (req.getDateHeader("If-Modified-Since") == file.getDateLastModified()) {
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }
        
        // set response status to "200 - OK" (required for export since a 404 status might have been set before)
        res.setStatus(HttpServletResponse.SC_OK);           
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
        
        // set default headers for cache control only if not already set
        if (!res.containsHeader("Cache-Control")) {
            res.setHeader("Cache-Control", "max-age=" + expireTime); // HTTP 1.1
            res.setDateHeader("Expires", System.currentTimeMillis() + (expireTime * 1000)); // HTTP 1.0
        }
                         
        service(cms, file, req, res);        
    }
        
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res)
    throws CmsException, IOException {
        
        res.getOutputStream().write(CmsFile.upgrade(resource, cms).getContents());
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#dump(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, java.util.Locale, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] dump(CmsObject cms, CmsResource resource, String element, Locale locale, HttpServletRequest req, HttpServletResponse res) 
    throws CmsException {
        
        return CmsFile.upgrade(resource, cms).getContents();
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportEnabled()
     */
    public boolean isStaticExportEnabled() {
        
        return true;
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsableForTemplates()
     */
    public boolean isUsableForTemplates() {
        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsingUriWhenLoadingTemplate()
     */
    public boolean isUsingUriWhenLoadingTemplate() {
        return false;
    }      
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsXmlPageLoader.java,v $
 * Date   : $Date: 2003/12/17 17:46:37 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.page.CmsXmlPage;

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
 * OpenCms loader class for xml pages.<p>
 *
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.8 $
 * @since 5.3
 */
public class CmsXmlPageLoader implements I_CmsResourceLoader {   
    
    /** The id of this loader */
    public static final int C_RESOURCE_LOADER_ID = 9;

    /** Template part identifier (request parameter) */
    public static final String C_TEMPLATE_ELEMENT = "__element";
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#destroy()
     */
    public void destroy() {
        // NOOP
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void export(CmsObject cms, CmsFile file, OutputStream exportStream, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, CmsException {        
        CmsResourceLoaderFacade loaderFacade = OpenCms.getLoaderManager().getLoaderFacade(cms, file);        
        loaderFacade.getLoader().export(cms, loaderFacade.getFile(), exportStream, req, res);
    }    
               
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {
        return C_RESOURCE_LOADER_ID;
    }

    /**
     * Returns a String describing the ResourceLoader,
     * which is <code>"The OpenCms default resource loader for xml pages"</code><p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        return "The OpenCms default resource loader for xml pages";
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#init(source.org.apache.java.util.Configurations)
     */
    public void init(ExtendedProperties configuration) {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) { 
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Loader init          : " + this.getClass().getName() + " initialized");
        }  
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {        
        try {
            CmsResourceLoaderFacade loaderFacade = OpenCms.getLoaderManager().getLoaderFacade(cms, file);        
            loaderFacade.getLoader().load(cms, loaderFacade.getFile(), req, res);
        } catch (CmsException e) {
            throw new ServletException(e.getMessage(), e);            
        }
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(com.opencms.file.CmsObject, com.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res) throws ServletException, IOException {
        
        String absolutePath = cms.readAbsolutePath(file);
        try {
            // get the requested page
            CmsXmlPage page = CmsXmlPage.read(cms, cms.readFile(absolutePath));
            
            // get the element selector
            // TODO: Check this, maybe use a request attribute instead of a parameter
            String elementName = req.getParameter(C_TEMPLATE_ELEMENT);
            
            // check the current locales
            String localeProp = OpenCms.getUserDefaultLanguage();
            
            // get the appropriate content and convert it to bytes
            byte[] result = page.getContent(cms, elementName, localeProp).getBytes(); 
            
            // append the result to the output stream
            if (result != null) {
                res.getOutputStream().write(result);
            }        
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error in CmsXmlPageLoader while processing " + absolutePath, e);
            }
            throw new ServletException("Error in CmsXmlPageLoader while processing " + absolutePath, e);       
        }
    }    
}

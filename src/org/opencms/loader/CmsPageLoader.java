/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/Attic/CmsPageLoader.java,v $
 * Date   : $Date: 2003/08/10 11:49:48 $
 * Version: $Revision: 1.11 $
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
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
 * OpenCms loader class for "simple" page templates.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.11 $
 * @since 5.1
 */
public class CmsPageLoader implements I_CmsResourceLoader {   
    
    /** The id of this loader */
    public static final int C_RESOURCE_LOADER_ID = 8;

    /**
     * @see com.opencms.flex.I_CmsResourceLoader#destroy()
     */
    public void destroy() {
        // NOOP
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(com.opencms.file.CmsObject, com.opencms.file.CmsFile)
     */
    public void export(CmsObject cms, CmsFile file) throws CmsException {  
        CmsFile templateFile = getTemplateFile(cms, file);  
        if (templateFile.getLoaderId() == CmsJspLoader.C_RESOURCE_LOADER_ID) {
            A_OpenCms.getLoaderManager().getLoader(CmsJspLoader.C_RESOURCE_LOADER_ID).export(cms, templateFile);           
        } else {
            A_OpenCms.getLoaderManager().getLoader(CmsXmlTemplateLoader.C_RESOURCE_LOADER_ID).export(cms, file);
        }     
    }    

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, CmsException {
        CmsFile templateFile = getTemplateFile(cms, file);  
        if (templateFile.getLoaderId() == CmsJspLoader.C_RESOURCE_LOADER_ID) {
            return A_OpenCms.getLoaderManager().getLoader(CmsJspLoader.C_RESOURCE_LOADER_ID).export(cms, templateFile, req, res);           
        } else {
            return A_OpenCms.getLoaderManager().getLoader(CmsXmlTemplateLoader.C_RESOURCE_LOADER_ID).export(cms, file, req, res);
        }  
    }    
               
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {
        return C_RESOURCE_LOADER_ID;
    }

    /**
     * Return a String describing the ResourceLoader,
     * which is <code>"The OpenCms default resource loader for pages"</code><p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        return "The OpenCms default resource loader for pages";
    }
    
    /**
     * Reads the template file for the selected page.<p>
     * 
     * @param cms the current cms context
     * @param file the requested file
     * @return the template file for the selected page
     * @throws CmsException if something goes wrong
     */
    private CmsFile getTemplateFile(CmsObject cms, CmsFile file) throws CmsException {        
        String absolutePath = cms.readAbsolutePath(file);        
        String templateProp = cms.readProperty(absolutePath, I_CmsConstants.C_PROPERTY_TEMPLATE);       

        if (templateProp == null) {
            // no template property defined, throw exception
            throw new CmsException("Property '" + I_CmsConstants.C_PROPERTY_TEMPLATE + "' undefined for page file " + absolutePath);
        }        

        return cms.readFile(templateProp);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#init(com.opencms.core.A_OpenCms, source.org.apache.java.util.Configurations)
     */
    public void init(A_OpenCms openCms, Configurations conf) {
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) { 
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Loader init          : " + this.getClass().getName() + " initialized!");
        }  
    }

    /**
     * @see com.opencms.flex.I_CmsResourceLoader#load(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {        
        CmsFile templateFile = null;
        try {
            templateFile = getTemplateFile(cms, file);
        } catch (CmsException e) {
            throw new ServletException(e.getMessage(), e);            
        }        
        if (templateFile.getLoaderId() == CmsJspLoader.C_RESOURCE_LOADER_ID) {
            A_OpenCms.getLoaderManager().getLoader(CmsJspLoader.C_RESOURCE_LOADER_ID).load(cms, templateFile, req, res);
        } else {
            A_OpenCms.getLoaderManager().getLoader(CmsXmlTemplateLoader.C_RESOURCE_LOADER_ID).load(cms, file, req, res);
        }
    }

    /**
     * @see com.opencms.flex.I_CmsResourceLoader#service(com.opencms.file.CmsObject, com.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res) throws ServletException, IOException {
        throw new RuntimeException("service() not a supported operation for resources of type " + this.getClass().getName());  
    }    
}

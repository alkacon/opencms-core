/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsXmlPageLoader.java,v $
 * Date   : $Date: 2003/11/28 17:00:05 $
 * Version: $Revision: 1.3 $
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
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsRequest;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Encoder;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;

/**
 * OpenCms loader class for "simple" page templates.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.3 $
 * @since 5.1
 */
public class CmsXmlPageLoader implements I_CmsResourceLoader {   
    
    /** The id of this loader */
    public static final int C_RESOURCE_LOADER_ID = 9;

    /** Template part identifier */
    public static final String C_TEMPLATE_ELEMENT = "__element";
        
    /** Flag for debugging output. Set to 9 for maximum verbosity. */ 
    private static final int DEBUG = 9;
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#destroy()
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
            OpenCms.getLoaderManager().getLoader(CmsJspLoader.C_RESOURCE_LOADER_ID).export(cms, templateFile);           
        } else {
            OpenCms.getLoaderManager().getLoader(CmsXmlTemplateLoader.C_RESOURCE_LOADER_ID).export(cms, file);
        }     
    }    

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void export(CmsObject cms, CmsFile file, OutputStream exportStream, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, CmsException {
        CmsFile templateFile = getTemplateFile(cms, file);  
        if (templateFile.getLoaderId() == CmsJspLoader.C_RESOURCE_LOADER_ID) {
            OpenCms.getLoaderManager().getLoader(CmsJspLoader.C_RESOURCE_LOADER_ID).export(cms, templateFile, exportStream, req, res);           
        } else {
            OpenCms.getLoaderManager().getLoader(CmsXmlTemplateLoader.C_RESOURCE_LOADER_ID).export(cms, file, exportStream, req, res);
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
        return "The OpenCms default resource loader for xml pages";
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
        CmsFile templateFile = null;
        try {
            templateFile = getTemplateFile(cms, file);
        } catch (CmsException e) {
            throw new ServletException(e.getMessage(), e);            
        }        
        if (templateFile.getLoaderId() == CmsJspLoader.C_RESOURCE_LOADER_ID) {
            OpenCms.getLoaderManager().getLoader(CmsJspLoader.C_RESOURCE_LOADER_ID).load(cms, templateFile, req, res);
        } else {
            OpenCms.getLoaderManager().getLoader(CmsXmlTemplateLoader.C_RESOURCE_LOADER_ID).load(cms, file, req, res);
        }
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(com.opencms.file.CmsObject, com.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res)
        throws ServletException {
          
        I_CmsRequest cms_req = cms.getRequestContext().getRequest(); 
        String rnc = cms.getRequestContext().getEncoding().trim();
        HttpServletRequest originalreq = (HttpServletRequest)cms_req.getOriginalRequest();
        
        try {
            byte[] result = null;
            String absolutePath = cms.readAbsolutePath(file);
            CmsXmlPage page = CmsXmlPage.newInstance(cms, cms.readFile(absolutePath));
            
            // care about encoding issues
            String dnc = OpenCms.getDefaultEncoding().trim();
            String enc = cms.readProperty(cms.readAbsolutePath(page), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true, dnc).trim();
            
            cms.getRequestContext().setEncoding(enc);      
            if (DEBUG > 1) {
                System.err.println("CmsXmlPageLoader.service(): Encodig set to " + cms.getRequestContext().getEncoding());
                System.err.println("CmsXmlPageLoader.service(): Uri set to " + cms.getRequestContext().getUri());
            }
            
            // get the element selector
            String elementName = req.getParameter(C_TEMPLATE_ELEMENT);
            
            // check the current locales
            String localeProp = OpenCms.getUserDefaultLanguage();
            
            // get the appropriate content
            result = page.getContent(elementName, localeProp); 
            
            // append the result to the output stream
            if (result != null) {
                // Encoding project:
                // The byte array must internally be encoded in the OpenCms
                // default encoding. It will be converted to the requested encoding 
                // on the most top-level JSP element
                result = Encoder.changeEncoding(result, enc, dnc);
                if (DEBUG > 1) {
                    System.err.println("CmsXmlPageLoader.service(): encoding=" + enc + " requestEncoding=" + rnc + " defaultEncoding=" + dnc);
                }
                res.getOutputStream().write(result);
            }        
        } catch (Throwable t) {
            if (DEBUG > 0) {
                t.printStackTrace(System.err);
            }
            throw new ServletException("Error in CmsXmlPageLoader while processing " + cms.readAbsolutePath(file), t);       
        } finally {
            // restore the context settings
            cms_req.setOriginalRequest(originalreq);
            cms.getRequestContext().setEncoding(rnc);
        
            if (DEBUG > 1) {
                System.err.println("CmsXmlPageLoader.service(): Encodig reset to " + cms.getRequestContext().getEncoding());
                System.err.println("CmsXmlPageLoader.service(): Uri reset to " + cms.getRequestContext().getUri());
            }
        }
    }    
}

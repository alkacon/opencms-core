/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/Attic/CmsPageLoader.java,v $
 * Date   : $Date: 2003/07/14 20:12:41 $
 * Version: $Revision: 1.1 $
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

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.launcher.I_CmsLauncher;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OpenCms launcher class for "simple" page templates.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.1 $
 * @since 5.1
 */
public class CmsPageLoader implements I_CmsLauncher, I_CmsResourceLoader {   
    
    /**
     * @see com.opencms.launcher.I_CmsLauncher#getLauncherId()
     */
    public int getLauncherId() {
        return C_TYPE_PAGE;
    }

    /**
     * @see com.opencms.launcher.I_CmsLauncher#clearCache()
     */
    public void clearCache() {
        // NOOP      
    }

    /**
     * @see com.opencms.launcher.I_CmsLauncher#initlaunch(com.opencms.file.CmsObject, com.opencms.file.CmsFile, java.lang.String, com.opencms.core.A_OpenCms)
     */
    public void initlaunch(CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms) throws CmsException {
        String absolutePath = cms.readAbsolutePath(file);
        String templateProp = cms.readProperty(absolutePath, I_CmsConstants.C_XML_CONTROL_TEMPLATE_PROPERTY);
        
        if (templateProp == null) {
            // no template property defined, throw exception
            throw new CmsException("Property '" + I_CmsConstants.C_XML_CONTROL_TEMPLATE_PROPERTY + "' undefined for page file " + absolutePath, CmsException.C_LAUNCH_ERROR);
        }
        
        CmsFile templateFile = null;
        try {
            templateFile = (CmsFile)cms.readFile(templateProp);
        } catch (CmsException e) {
            throw new CmsException("Template '" + templateProp + "' unreadable for page file " + absolutePath, CmsException.C_LAUNCH_ERROR);            
        }
        
        if (templateFile.getLauncherType() == C_TYPE_JSP) {
            cms.getLauncherManager().getLauncher(templateFile.getLauncherType()).initlaunch(cms, templateFile, (String)null, openCms);
        } else {
            cms.getLauncherManager().getLauncher(C_TYPE_XML).initlaunch(cms, templateFile, (String)null, openCms);
        }
    }

    /**
     * @see com.opencms.launcher.I_CmsLauncher#setOpenCms(com.opencms.core.A_OpenCms)
     */
    public void setOpenCms(A_OpenCms openCms) {
        // NOOP   
    }

    /**
     * @see com.opencms.flex.I_CmsResourceLoader#init(com.opencms.core.A_OpenCms)
     */
    public void init(A_OpenCms openCms) {
        // NOOP
    }

    /**
     * @see com.opencms.flex.I_CmsResourceLoader#destroy()
     */
    public void destroy() {
        // NOOP
    }

    /**
     * @see com.opencms.flex.I_CmsResourceLoader#getResourceLoaderInfo()
     */
    public String getResourceLoaderInfo() {
        return "The OpenCms default resource loader for pages";
    }

    /**
     * @see com.opencms.flex.I_CmsResourceLoader#load(com.opencms.file.CmsObject, com.opencms.file.CmsFile, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        throw new RuntimeException("load() not a supported operation for resources of this type");  
    }

    /**
     * @see com.opencms.flex.I_CmsResourceLoader#service(com.opencms.file.CmsObject, com.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res) throws ServletException, IOException {
        throw new RuntimeException("service() not a supported operation for resources of this type");  
    }    
}

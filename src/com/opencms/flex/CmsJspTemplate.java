/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/CmsJspTemplate.java,v $
 * Date   : $Date: 2002/12/16 13:21:01 $
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
 * First created on 24. Mai 2002, 09:53
 */


package com.opencms.flex;

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsLogChannels;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.template.CmsCacheDirectives;

import java.util.Hashtable;

/**
 * A simple dump class for JSPs which enables
 * the use of JSP as sub-elements in the XMLTemplate 
 * mechanism.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * @since 5.0 beta 1
 * 
 */
public class CmsJspTemplate extends com.opencms.template.CmsDumpTemplate {
    
    /** 
     * Constructor for class CmsJspTemplate,
     * not used.
     */
    public CmsJspTemplate() {       
    }

    /**
     * Gets the content of the given JSP file to include them
     * in the XMLTemplate.
     *
     * @param cms for accessing system resources
     * @param jspFile filename of the JSP in the VFS
     * @param elementName <em>not used</em>
     * @param parameters <em>not used</em>
     * 
     * @return Content of the requested JSP page.
     * 
     * @throws CmsException
     */
    public byte[] getContent(CmsObject cms, String jspFile, String elementName, Hashtable parameters) throws CmsException {
        if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(C_FLEX_LOADER)) {
            A_OpenCms.log(C_FLEX_LOADER, "[CmsJspTemplate] Now loading contents of file " + jspFile);
        }

        byte[] s = null;
        try {
            CmsFile file = cms.readFile(jspFile);
            int type = file.getLauncherType();
            com.opencms.flex.CmsJspLoader loader = (com.opencms.flex.CmsJspLoader)cms.getLauncherManager().getLauncher(type);
            s = loader.loadTemplate(cms, file);
        } catch (java.lang.ClassCastException e) {
            throw new CmsException("[CmsJspTemplate] " + jspFile + " is not a JSP");
        } catch (com.opencms.core.CmsException e) {
            // File might not exist or no read permissions
            throw new CmsException("[CmsJspTemplate] Error while reading JSP " + jspFile + "\n" + e, e);
        }        

        catch(Exception e) {
            String errorMessage = "[CmsJspTemplate] Error while loading jsp file " + jspFile + ": " + e;
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_CRITICAL) ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsJspTemplate] " + errorMessage);
            }
            if(e instanceof CmsException) {
                throw (CmsException)e;
            }
            else {
                throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
            }
        }
        return s;
    }
    
    /**
     * Cache method required by the ElementCache to indicate if the
     * results of the page should be cached in the ElementCache.<p>
     * 
     * JSPs will be cached in the FlexCache and so 
     * we always return <code>false</code> here.
     * 
     * @return <code>false</code>
     */
    public boolean isTemplateCacheSet() {
        return false;
    }    
    
    /**
     * Method used by the ElementCache to check if the page 
     * should reload or not.<p>
     * 
     * JSPs will be cached in the FlexCache and so 
     * we always return <code>true</code> here.
     * 
     * @return <code>true</code>
     */
    public boolean shouldReload(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    } 
    
    /**
     * Returns the caching information from the current template class for the
     * ElementCache.<p>
     * 
     * JSPs will be cached in the FlexCache and so 
     * we always return directives that prevent caching here,
     * i.e. <code>new CmsCacheDirectives(false)</code>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        // Just set caching to false
        return new CmsCacheDirectives(false);
    }    
}

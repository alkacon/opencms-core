/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsJavascriptTemplate.java,v $
 * Date   : $Date: 2000/06/05 13:37:57 $
 * Version: $Revision: 1.9 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*; 
import com.opencms.launcher.*;

import java.util.*;


/** Document the purpose of this class.
 *
 * @version 1.0
 * @author 
 **/
public class CmsJavascriptTemplate implements I_CmsJavascriptTemplate {
    /**
     * Gets the content of a given template file with the given parameters.
     * <P>
     * Parameters are stored in a hashtable and can derive from
     * <UL>
     * <LI>Template file of the parent template</LI>
     * <LI>Body file clicked by the user</LI>
     * <LI>URL parameters</LI>
     * </UL>
     * Paramter names must be in "elementName.parameterName" format.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template
     * @param parameters Hashtable with all template class parameters.
     * @return Content of the template and all subtemplates.
     * @exception CmsException 
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
        throws CmsException {
        return "".getBytes();
    }
    
    /**
     * Gets the content of a defined section in a given template file 
     * with the given parameters.
     * 
     * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector section that should be processed.
     * @return Content of the template and all subtemplates.
     * @exception CmsException 
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector)
        throws CmsException {
        return "".getBytes();
    }
    
    /**
     * Set the instance of template cache that should be used to store 
     * cacheable results of the subtemplates.
     * If the template cache is not set, caching will be disabled.
     * @param c Template cache to be used.
     */
    public void setTemplateCache(I_CmsTemplateCache c) {
    }
    
    /**
     * Tests, if the template cache is setted.
     * @return <code>true</code> if setted, <code>false</code> otherwise.
     */
    public boolean isTemplateCacheSet() {
        return true;
    }
    
    /**
     * Gets the key that should be used to cache the results of
     * <EM>this</EM> template class. For simple template classes, e.g.
     * classes only dumping file contents and not using parameters,
     * the name of the template file may be adequate.
     * Other classes have to return a more complex key.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return key that can be used for caching
     */
    public Object getKey(CmsObject cms, String templateFile, Hashtable parameters, String templateSelector) {
        //Vector v = new Vector();
        CmsRequestContext reqContext = cms.getRequestContext();
        
        //v.addElement(reqContext.currentProject().getName());
        //v.addElement(templateFile);
        //return v;
        return "" + reqContext.currentProject().getId() + ":" + templateFile;
    }
    
    /**
     * Indicates if the results of this class are cacheable.
     * <P>
     * Complex classes that are able top include other subtemplates
     * have to check the cacheability of their subclasses here!
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Indicates if a previous cached result should be reloaded.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if reload is neccesary, <EM>false</EM> otherwise.
     */    
    public boolean shouldReload(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    }
}
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminElementCache.java,v $
* Date   : $Date: 2005/02/18 15:18:51 $
* Version: $Revision: 1.20 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
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
*/

package com.opencms.workplace;

import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import org.opencms.file.CmsObject;

import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.cache.CmsElementCache;

import java.util.Hashtable;
import java.util.Vector;
/**
 * Template class for displaying OpenCms workplace elementCache administration.
 * <P>
 *
 * @author Hanjo Riege
 * @version $Revision: 1.20 $ $Date: 2005/02/18 15:18:51 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsAdminElementCache extends CmsWorkplaceDefault {

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Getting content of element "
                            + ((elementName == null) ? "<root>" : elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: "
                            + ((templateSelector == null) ? "<default>" : templateSelector));
        }
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // need the element cache
        CmsElementCache cache = CmsXmlTemplateLoader.getOnlineElementCache();
        // any debug action?
        String info = (String)parameters.get("info");
        if(info != null && "dep_out".equals(info)){
            // print out the pependencies cache
            cache.printCacheInfo(1);
        }
        // get the parameter
        String action = (String)parameters.get("action");
        if((action == null) || ("".equals(action))){
            // first call, fill the process tags
            Vector cacheInfo = cache.getCacheInfo();
            xmlTemplateDocument.setData("urisize",  ((Integer)cacheInfo.elementAt(1)).toString() + " | " +
                                                    ((Integer)cacheInfo.elementAt(0)).toString());
            xmlTemplateDocument.setData("elementsize", ((Integer)cacheInfo.elementAt(3)).toString() + " | " +
                                                       ((Integer)cacheInfo.elementAt(2)).toString());
        }else{
            // action! clear the cache
            cache.clearCache();
            templateSelector = "done";
        }

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
    }

    /**
     * Checks if the current user is <strong>administrator</strong> and the element cache is activ.
     * <P>
     * This method is used by workplace icons to decide whether the icon should
     * be activated or not. Icons will use this method if the attribute <code>method="isElementcacheAdmin"</code>
     * is defined in the <code>&lt;ICON&gt;</code> tag.
     *
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>true</code> if the current project is the online project, <code>false</code> otherwise.
     * @throws CmsException if there were errors while accessing project data.
     */
    public Boolean isElementcacheAdmin(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        return new Boolean(cms.isAdmin() && (CmsXmlTemplateLoader.getElementCache() != null));
    }

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }

}
/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/template/Attic/CmsJavascriptTemplate.java,v $
* Date   : $Date: 2005/05/17 13:47:32 $
* Version: $Revision: 1.1 $
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


package com.opencms.template;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;

import com.opencms.template.cache.A_CmsElement;

import java.util.Hashtable;

/**
 * Represents a JavaScript template.
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2005/05/17 13:47:32 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release. 
 */
public class CmsJavascriptTemplate extends A_CmsTemplate implements I_CmsJavascriptTemplate {

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
     * @throws CmsException if something goes wrong
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        return "".getBytes();
    }

    /**
     * Gets the content of a defined section in a given template file
     * with the given parameters.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector section that should be processed.
     * @return Content of the template and all subtemplates.
     * @throws CmsException if something goes wrong
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        return "".getBytes();
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
        CmsRequestContext reqContext = cms.getRequestContext();
        return "" + reqContext.currentProject().getId() + ":" + reqContext.addSiteRoot(templateFile);
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
    /*public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    }*/

    /**
     * Indicates if the current template class is able to stream it's results
     * directly to the response oputput stream.
     * <P>
     * Classes must not set this feature, if they might throw special
     * exception that cause HTTP errors (e.g. 404/Not Found), or if they
     * might send HTTP redirects.
     * <p>
     * If a class sets this feature, it has to check the
     * isStreaming() property of the RequestContext. If this is set
     * to <code>true</code> the results must be streamed directly
     * to the output stream. If it is <code>false</code> the results
     * must not be streamed.
     * <P>
     * Complex classes that are able top include other subtemplates
     * have to check the streaming ability of their subclasses here!
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public boolean isStreamable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    /**
     * Tests, if the template cache is setted.
     * @return <code>true</code> if setted, <code>false</code> otherwise.
     */
    public boolean isTemplateCacheSet() {
        return true;
    }

    /**
     * Set the instance of template cache that should be used to store
     * cacheable results of the subtemplates.
     * If the template cache is not set, caching will be disabled.
     * @param c Template cache to be used.
     */
    public void setTemplateCache(I_CmsTemplateCache c) {
        // noop
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

    /**
     * Not yet implemented.<p>
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param parameters Hashtable with all template class parameters
     * @return the element
     */
    public A_CmsElement createElement(CmsObject cms, String templateFile, Hashtable parameters) {
        // to be implemented
        return null;
    }
}

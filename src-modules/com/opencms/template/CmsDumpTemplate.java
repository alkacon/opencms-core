/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/template/Attic/CmsDumpTemplate.java,v $
* Date   : $Date: 2005/05/31 15:51:19 $
* Version: $Revision: 1.3 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import com.opencms.legacy.CmsLegacyException;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.cache.A_CmsElement;
import com.opencms.template.cache.CmsElementDump;

import java.util.Hashtable;

/**
 * Template class for dumping files to the output without further
 * interpreting or processing.
 * This can be used for plain text files or files containing graphics.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2005/05/31 15:51:19 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsDumpTemplate extends A_CmsTemplate implements I_CmsDumpTemplate {

    /** Boolean for additional debug output control. */
    private static final boolean C_DEBUG = false;

    /**
     * Creates a new CmsDumpTemplate.<p>
     */
    public CmsDumpTemplate() {
        // noop    
    }

    /**
     * gets the caching information from the current template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        // First build our own cache directives.
        CmsCacheDirectives result = new CmsCacheDirectives(true);
        result.setCacheUri(true);
        return result;

    }

    /**
     * Gets the content of a given template file.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @throws CmsException if something goes wrong
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        if (C_DEBUG && CmsLog.getLog(this).isDebugEnabled()) {
            CmsLog.getLog(this).debug("Dumping contents of file " + templateFile);
        }
        byte[] s = null;
        try {
            // Encoding project:
            CmsFile file = cms.readFile(templateFile); 
            s = file.getContents();
        } catch (Exception e) {
            s = null;
            String errorMessage = "Error while reading file " + templateFile + ": " + e;
            if (CmsLog.getLog(this).isErrorEnabled()) {
                CmsLog.getLog(this).error(errorMessage, e);
            }
            if (e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                throw new CmsLegacyException(errorMessage, CmsLegacyException.C_UNKNOWN_EXCEPTION);
            }
        }
        return s;
    }

    /**
     * Gets the content of a given template file.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @param templateSelector <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @throws CmsException if something goes wrong
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

        // ignore the templateSelector since we only dump the template
        return getContent(cms, templateFile, elementName, parameters);
    }

    /**
     * Gets the key that should be used to cache the results of
     * this template class.
     * <P>
     * Since this class is quite simple it's okay to return
     * just the name of the template file here.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param parameter Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return key that can be used for caching
     */
    public Object getKey(CmsObject cms, String templateFile, Hashtable parameter, String templateSelector) {
        CmsRequestContext reqContext = cms.getRequestContext();
        return "" + reqContext.currentProject().getId() + ":" + reqContext.addSiteRoot(templateFile);
    }

    /**
     * Any results of this class are cacheable since we don't include
     * any subtemplates. So we can always return <code>true</code> here.
     * @return <code>true</code>
     */
    public boolean isTemplateCacheSet() {
        return true;
    }

    /**
     * Template cache is not used here since we don't include
     * any subtemplates <em>(not implemented)</em>.
     * 
     * @param c the cms template cache
     */
    public void setTemplateCache(I_CmsTemplateCache c) {
        // do nothing.
    }

    /**
     * Template cache is not used here since we don't include
     * any subtemplates. So we can always return <code>false</code> here.
     * @param cms the cms object
     * @param templateFile the name of the template file
     * @param elementName the name of the element
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <code>false</code>
     */
    public boolean shouldReload(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Create a new element for the element cache consisting of the current template
     * class and the given template file.
     * <P>
     * Complex template classes that are able to include other (sub-)templates
     * must generate a collection of element definitions for their possible
     * subtemplates. This collection is part of the new element.
     * @param cms CmsObject for accessing system resources.
     * @param templateFile Name of the template file for the new element
     * @param parameters All parameters of the current request
     * @return New element for the element cache
     */
    public A_CmsElement createElement (CmsObject cms, String templateFile, Hashtable parameters) {
        return new CmsElementDump(getClass().getName(), templateFile, getCacheDirectives(cms, templateFile, null, parameters, null),
                    CmsXmlTemplateLoader.getElementCache().getVariantCachesize());
    }
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsRootTemplate.java,v $
* Date   : $Date: 2004/06/28 07:44:02 $
* Version: $Revision: 1.52 $
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
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import com.opencms.core.I_CmsResponse;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Hashtable;

/**
 * Represents an "empty" page or screen that should be filled with
 * the content of a master template.<p>
 *
 * @author Alexander Lucas
 * @version $Revision: 1.52 $ $Date: 2004/06/28 07:44:02 $
 */
public class CmsRootTemplate {

    /**
     * Gets the processed content of the requested master template by calling
     * the given template class.
     * <P>
     * If the result is cacheable, the complete output will be stored
     * in the template cache for later re-use.
     *
     * @param cms CmsObject object for accessing system resources.
     * @param templateClass Instance of the template class to be called.
     * @param masterTemplate CmsFile object of the master template file.
     * @param cache templateCache to be used.
     * @param parameters Hashtable with all template class parameters.
     *
     * @return Byte array containing the results of the master template.
     * 
     * @throws CmsException if something goes wrong
     */
    public byte[] getMasterTemplate(CmsObject cms, I_CmsTemplate templateClass, CmsFile masterTemplate, com.opencms.template.I_CmsTemplateCache cache, Hashtable parameters) throws CmsException {
        byte[] result;

        String masterTemplateUri = cms.getSitePath(masterTemplate);
        // Collect cache directives from subtemplates
        CmsCacheDirectives cd = templateClass.collectCacheDirectives(cms, masterTemplateUri, I_CmsConstants.C_ROOT_TEMPLATE_NAME, parameters, null);

        /*System.err.println("******************************************************************");
        System.err.println("* Cache directives Summary");
        System.err.println("* File                    : " + cms.readPath(masterTemplate));
        System.err.println("* Class                   : " + templateClass.getClass());
        System.err.println("* internal cacheable      : " + (cd.isInternalCacheable()?"true":"false"));
        System.err.println("* proxy public cacheable  : " + (cd.isProxyPublicCacheable()?"true":"false"));
        System.err.println("* proxy private cacheable : " + (cd.isProxyPrivateCacheable()?"true":"false"));
        System.err.println("* exportable cacheable    : " + (cd.isExportable()?"true":"false"));
        System.err.println("* streamable              : " + (cd.isStreamable()?"true":"false"));
        System.err.println("******************************************************************");
        */

        Object cacheKey = templateClass.getKey(cms, masterTemplateUri, parameters, null);
        boolean cacheable = cd.isInternalCacheable();

        I_CmsResponse resp = CmsXmlTemplateLoader.getResponse(cms.getRequestContext());

        // was there already a cache-control header set?
        if (!resp.containsHeader("Cache-Control")) {

            // only if the resource is cacheable and if the current project is online,
            // then the browser may cache the resource
            if (cd.isProxyPublicCacheable() || cd.isProxyPrivateCacheable()) {

                // set max-age to 5 minutes. In this time a proxy may cache this content.
                resp.setHeader("Cache-Control", "max-age=300");
                if (cd.isProxyPrivateCacheable()) {
                    resp.addHeader("Cache-Control", "private");
                }


           } else {
                // set the http-header to pragma no-cache.
                //HTTP 1.1
                resp.setHeader("Cache-Control", "no-cache");
                //HTTP 1.0
                resp.setHeader("Pragma", "no-cache");
            }
        }

        if (cacheable && cache.has(cacheKey) && !templateClass.shouldReload(cms, masterTemplateUri, I_CmsConstants.C_ROOT_TEMPLATE_NAME, parameters, null)) {
            result = cache.get(cacheKey);
        } else {
            try {
                result = templateClass.getContent(cms, masterTemplateUri, I_CmsConstants.C_ROOT_TEMPLATE_NAME, parameters);
            } catch (CmsException e) {
                cache.clearCache(cacheKey);
                if (OpenCms.getLog(this).isWarnEnabled() && (e.getType() != CmsException.C_NO_USER)) {
                    OpenCms.getLog(this).warn("Could not get contents of master template " + masterTemplate.getName(), e);
                }
                throw e;
            }
            if (cacheable) {
                cache.put(cacheKey, result);
            }
        }
        return result;
    }
}

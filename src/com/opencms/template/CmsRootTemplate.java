/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsRootTemplate.java,v $
* Date   : $Date: 2003/07/12 12:49:03 $
* Version: $Revision: 1.33 $
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsResponse;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;

import java.util.Hashtable;

/**
 * Represents an "empty" page or screen that should be filled with
 * the content of a master template.
 * <P>
 * Every launcher uses this canonical root the invoke the output
 * generation of the master template class to be used.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.33 $ $Date: 2003/07/12 12:49:03 $
 */
public class CmsRootTemplate implements I_CmsLogChannels,I_CmsConstants {

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
     */
    public byte[] getMasterTemplate(CmsObject cms, I_CmsTemplate templateClass, CmsFile masterTemplate, com.opencms.template.I_CmsTemplateCache cache, Hashtable parameters) throws CmsException {
        byte[] result;

        // Collect cache directives from subtemplates
        CmsCacheDirectives cd = templateClass.collectCacheDirectives(cms, cms.readAbsolutePath(masterTemplate), C_ROOT_TEMPLATE_NAME, parameters, null);
        boolean streamable = cms.getRequestContext().isStreaming() && cd.isStreamable();
        cms.getRequestContext().setStreaming(streamable);

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

        //String cacheKey = cms.getUrl();
        Object cacheKey = templateClass.getKey(cms, cms.readAbsolutePath(masterTemplate), parameters, null);
        //boolean cacheable = templateClass.isCacheable(cms, cms.readPath(masterTemplate), C_ROOT_TEMPLATE_NAME, parameters, null);
        boolean cacheable = cd.isInternalCacheable();

        I_CmsResponse resp = cms.getRequestContext().getResponse();

        // was there already a cache-control header set?
        if(!resp.containsHeader("Cache-Control")) {

            // only if the resource is cacheable and if the current project is online,
            // then the browser may cache the resource
            if(cd.isProxyPublicCacheable() || cd.isProxyPrivateCacheable()) {

                // set max-age to 5 minutes. In this time a proxy may cache this content.
                resp.setHeader("Cache-Control", "max-age=300");
                if(cd.isProxyPrivateCacheable()) {
                    resp.addHeader("Cache-Control", "private");
                }


           }
            else {
                // set the http-header to pragma no-cache.
                //HTTP 1.1
                resp.setHeader("Cache-Control", "no-cache");
                //HTTP 1.0
                resp.setHeader("Pragma", "no-cache");
            }
        }

        if(cacheable && cache.has(cacheKey) && !templateClass.shouldReload(cms, cms.readAbsolutePath(masterTemplate), C_ROOT_TEMPLATE_NAME, parameters, null)) {
            result = cache.get(cacheKey);
            // We don't want to stream this...
            cms.getRequestContext().setStreaming(false);
        }
        else {
            try {
                result = templateClass.getContent(cms, cms.readAbsolutePath(masterTemplate), C_ROOT_TEMPLATE_NAME, parameters);
            }
            catch(CmsException e) {
                cache.clearCache(cacheKey);
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsRootTemplate] Could not get contents of master template " + masterTemplate.getResourceName());
                }
                throw e;
            }
            if(cacheable) {
                cache.put(cacheKey, result);
            }
            if(streamable) {
                result = null;
            }
        }
        return result;
    }
}

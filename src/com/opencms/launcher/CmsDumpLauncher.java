/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsDumpLauncher.java,v $
* Date   : $Date: 2003/07/02 11:03:13 $
* Version: $Revision: 1.40 $
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

package com.opencms.launcher;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsRequest;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.template.cache.CmsElementCache;
import com.opencms.template.cache.CmsElementDefinitionCollection;
import com.opencms.template.cache.CmsElementDescriptor;
import com.opencms.template.cache.CmsUri;
import com.opencms.template.cache.CmsUriDescriptor;
import com.opencms.template.cache.CmsUriLocator;
import com.opencms.util.Utils;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

/**
 * OpenCms launcher class for starting template classes implementing
 * the I_CmsDumpTemplate interface.
 * This can be used for plain text files or files containing graphics.
 * <P>
 * If no other start template class is given, CmsDumpTemplate will
 * be used to create output.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.40 $ $Date: 2003/07/02 11:03:13 $
 */
public class CmsDumpLauncher extends A_CmsLauncher implements I_CmsConstants {

    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId() {
        return C_TYPE_DUMP;
    }

    /**
     * Unitary method to start generating the output.
     * Every launcher has to implement this method.
     * In it possibly the selected file will be analyzed, and the
     * Canonical Root will be called with the appropriate
     * template class, template file and parameters. At least the
     * canonical root's output must be written to the HttpServletResponse.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param file CmsFile Object with the selected resource to be shown
     * @param startTemplateClass Name of the template class to start with.
     * @param openCms a instance of A_OpenCms for redirect-needs
     * @throws CmsException
     */
    protected void launch(CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms) throws CmsException {
        byte[] result = null;

        CmsElementCache elementCache = null;
        boolean elementCacheEnabled = cms.getRequestContext().isElementCacheEnabled();

        // Get the currently requested URI
        String uri = cms.getRequestContext().getUri();
        CmsUri cmsUri = null;

        String templateClass = startTemplateClass;
        if(templateClass == null || "".equals(templateClass) || (startTemplateClass.equals(C_UNKNOWN_LAUNCHER))) {
            templateClass = "com.opencms.template.CmsDumpTemplate";
        }

        if(elementCacheEnabled) {
            // -------- element cache -----
            elementCache = cms.getRequestContext().getElementCache();

            CmsUriDescriptor uriDesc = new CmsUriDescriptor(uri);
            CmsUriLocator uriLoc = elementCache.getUriLocator();
            cmsUri = uriLoc.get(uriDesc);

            if(cmsUri == null) {
                // hammer nich
                CmsElementDescriptor elemDesc = new CmsElementDescriptor(templateClass, cms.readAbsolutePath(file));
//				TODO: fix this later - check how to do this without getReadingpermittedGroup
//				String readAccessGroup = CmsObject.C_GROUP_ADMIN;
				String readAccessGroup = cms.getReadingpermittedGroup(cms.getRequestContext().currentProject().getId(), cms.readAbsolutePath(file));
                cmsUri = new CmsUri(elemDesc, readAccessGroup, (CmsElementDefinitionCollection)null,
                        Utils.isHttpsResource(cms, file));
                elementCache.getUriLocator().put(uriDesc, cmsUri);
            }
        }

        Hashtable newParameters = new Hashtable();
        I_CmsRequest req = cms.getRequestContext().getRequest();

        // Now check URL parameters
        String datafor = req.getParameter("datafor");
        if(datafor == null) {
            datafor = "";
        }
        else {
            if(!"".equals(datafor)) {
                datafor = datafor + ".";
            }
        }
        Enumeration urlParameterNames = req.getParameterNames();
        while(urlParameterNames.hasMoreElements()) {
            String pname = (String)urlParameterNames.nextElement();
            String paramValue = req.getParameter(pname);
            if(paramValue != null) {
                if((!"datafor".equals(pname)) && (!"_clearcache".equals(pname))) {
                    newParameters.put(datafor + pname, paramValue);
                }
            }
            else {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(C_OPENCMS_INFO, getClassName() + "Empty URL parameter \"" + pname + "\" found.");
                }
            }
        }

        // Make sure binary documents (*.pdf, *.doc etc.) are displayed in offline project
        String mimetype = cms.getRequestContext().getResponse().getContentType();
        // Text based non-binary mime types need no special treatment
        if ((null == mimetype) || (! mimetype.startsWith("text"))) {
            // Setting this header here will cause root template and element cache 
            // not to set caching header, othwerwise a "no-cache" header will be set
            cms.getRequestContext().getResponse().setHeader("Cache-Control", "max-age=60");
            cms.getRequestContext().getResponse().setHeader("Pragma", "no-pragma");
        } 
        if(elementCacheEnabled) {
            // lets check if ssl is active
            if(cms.getMode() == C_MODUS_ONLINE){
                String scheme = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getScheme();
                boolean httpsReq = "https".equalsIgnoreCase(scheme);
                if(cmsUri.isHttpsResource() != httpsReq){
                    if(httpsReq){
                        // throw new CmsException(" "+cms.readPath(file)+" needs a http request", CmsException.C_HTTPS_PAGE_ERROR);
                        // since the netscape 4.7 dont shows http pics on https sides we cant throw this error.
                    }else if(CmsObject.getStaticExportProperties().isStaticExportEnabled()
                                || "false_ssl".equals(CmsObject.getStaticExportProperties().getStaticExportEnabledValue())){
                        // check if static export is enabled and value is not false_ssl
                        throw new CmsException(" "+cms.readAbsolutePath(file)+" needs a https request", CmsException.C_HTTPS_REQUEST_ERROR);
                    }
                }
            }
            result = elementCache.callCanonicalRoot(cms, newParameters);
        } else {
            Object tmpl = getTemplateClass(cms, templateClass);
            if(!(tmpl instanceof com.opencms.template.I_CmsDumpTemplate)) {
                String errorMessage = "Error in " + cms.readAbsolutePath(file) + ": " + templateClass + " is not a Cms dump template class.";
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
                }
                throw new CmsException(errorMessage, CmsException.C_XML_WRONG_TEMPLATE_CLASS);
            }

            try {
                result = this.callCanonicalRoot(cms, (com.opencms.template.I_CmsTemplate)tmpl, file, newParameters);
            }
            catch(CmsException e) {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "There were errors while building output for template file \"" + cms.readAbsolutePath(file) + "\" and template class \"" + templateClass + "\". See above for details.");
                }
                throw e;
            }
        }
        if(result != null) {
            writeBytesToResponse(cms, result);
        }
    }
}

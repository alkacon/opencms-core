/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsDumpLauncher.java,v $
* Date   : $Date: 2002/10/30 10:28:47 $
* Version: $Revision: 1.34 $
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

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.*;
import javax.servlet.http.*;
import com.opencms.template.cache.*;

/**
 * OpenCms launcher class for starting template classes implementing
 * the I_CmsDumpTemplate interface.
 * This can be used for plain text files or files containing graphics.
 * <P>
 * If no other start template class is given, CmsDumpTemplate will
 * be used to create output.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.34 $ $Date: 2002/10/30 10:28:47 $
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
     * @exception CmsException
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
                CmsElementDescriptor elemDesc = new CmsElementDescriptor(templateClass, file.getAbsolutePath());
                cmsUri = new CmsUri(elemDesc, cms.getReadingpermittedGroup(
                        cms.getRequestContext().currentProject().getId(),
                        file.getAbsolutePath()), (CmsElementDefinitionCollection)null,
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

        // Important because PDFs will not be displayed if caching is disabled
        // Setting this header here will cause root template and element cache 
        // not to set caching header again
        String mimetype = cms.getRequestContext().getResponse().getContentType();   
        if ((null != mimetype) && (mimetype.endsWith("pdf"))) {
            cms.getRequestContext().getResponse().setHeader("Cache-Control","max-age=300");
        }         

        if(elementCacheEnabled) {
            // lets check if ssl is active
            if(cms.getMode() == C_MODUS_ONLINE){
                String scheme = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getScheme();
                boolean httpsReq = "https".equalsIgnoreCase(scheme);
                if(cmsUri.isHttpsResource() != httpsReq){
                    if(httpsReq){
                        //throw new CmsException(" "+file.getAbsolutePath()+" needs a http request", CmsException.C_HTTPS_PAGE_ERROR);
                        // since the netscape 4.7 dont shows http pics on https sides we cant throw this error.
                    }else if(cms.getStaticExportProperties().isStaticExportEnabled()
                                || "false_ssl".equals(cms.getStaticExportProperties().getStaticExportEnabledValue())){
                        // check if static export is enabled and value is not false_ssl
                        throw new CmsException(" "+file.getAbsolutePath()+" needs a https request", CmsException.C_HTTPS_REQUEST_ERROR);
                    }
                }
            }
            result = elementCache.callCanonicalRoot(cms, newParameters);
        } else {
            Object tmpl = getTemplateClass(cms, templateClass);
            if(!(tmpl instanceof com.opencms.template.I_CmsDumpTemplate)) {
                String errorMessage = "Error in " + file.getAbsolutePath() + ": " + templateClass + " is not a Cms dump template class.";
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
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "There were errors while building output for template file \"" + file.getAbsolutePath() + "\" and template class \"" + templateClass + "\". See above for details.");
                }
                throw e;
            }
        }
        if(result != null) {

            // cms.getRequestContext().getResponse().setLastModified(file.getDateLastModified());
            writeBytesToResponse(cms, result);
        }
    }
}

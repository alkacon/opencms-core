
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsDumpLauncher.java,v $
* Date   : $Date: 2001/05/10 12:31:14 $
* Version: $Revision: 1.22 $
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

package com.opencms.launcher;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
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
 * @version $Revision: 1.22 $ $Date: 2001/05/10 12:31:14 $
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

        String templateClass = startTemplateClass;
        if(templateClass == null || "".equals(templateClass) || (startTemplateClass.equals(C_UNKNOWN_LAUNCHER))) {
            templateClass = "com.opencms.template.CmsDumpTemplate";
        }

        if(elementCacheEnabled) {
            // -------- element cache -----
            elementCache = cms.getRequestContext().getElementCache();

            CmsUriDescriptor uriDesc = new CmsUriDescriptor(uri);
            CmsUriLocator uriLoc = elementCache.getUriLocator();
            CmsUri cmsUri = uriLoc.get(uriDesc);

            if(cmsUri == null) {
                // hammer nich
                CmsElementDescriptor elemDesc = new CmsElementDescriptor(templateClass, file.getAbsolutePath());
                cmsUri = new CmsUri(elemDesc, null, (CmsElementDefinitionCollection)null);
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
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, getClassName() + "Empty URL parameter \"" + pname + "\" found.");
                }
            }
        }

        if(elementCacheEnabled) {
            result = elementCache.callCanonicalRoot(cms, newParameters);
        } else {
            Object tmpl = getTemplateClass(cms, templateClass);
            if(!(tmpl instanceof com.opencms.template.I_CmsDumpTemplate)) {
                String errorMessage = "Error in " + file.getAbsolutePath() + ": " + templateClass + " is not a Cms dump template class.";
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
                }
                throw new CmsException(errorMessage, CmsException.C_XML_WRONG_TEMPLATE_CLASS);
            }

            try {
                result = this.callCanonicalRoot(cms, (com.opencms.template.I_CmsTemplate)tmpl, file, newParameters);
            }
            catch(CmsException e) {
                if(A_OpenCms.isLogging()) {
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

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsXmlLauncher.java,v $
* Date   : $Date: 2001/10/12 07:46:09 $
* Version: $Revision: 1.31 $
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
import com.opencms.template.cache.*;
import com.opencms.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * OpenCms launcher class for XML templates.
 * This can be used generating output for XML body files using XML template and
 * subtemplate technology.
 * <P>
 * The selected body should define a start template class using <BR> <CODE>
 * &lt;PAGE&gt;<BR>
 * &nbsp;&nbsp;&lt;CLASS&gt;...&lt;/CLASS&gt;<BR>
 * &lt;/PAGE&gt;</CODE><P>
 *
 * If no start template is defined, the class given by the parameters
 * will be used.
 * <P>
 * If even this is not defined, CmsXmlTemplate will
 * be used to create output.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.31 $ $Date: 2001/10/12 07:46:09 $
 */
public class CmsXmlLauncher extends A_CmsLauncher implements I_CmsLogChannels,I_CmsConstants {

    /**
     * Starts generating the output.
     * Calls the canonical root with the appropriate template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param file CmsFile Object with the selected resource to be shown
     * @param startTemplateClass Name of the template class to start with.
     * @exception CmsException
     */
    protected byte[] generateOutput(CmsObject cms, CmsFile file, String startTemplateClass, I_CmsRequest req, A_OpenCms openCms) throws CmsException {
        byte[] output = null;

        // Hashtable for collecting all parameters.
        Hashtable newParameters = new Hashtable();

        // Parameters used for element cache
        boolean elementCacheEnabled = cms.getRequestContext().isElementCacheEnabled();
        CmsElementCache elementCache = null;
        String uri = cms.getRequestContext().getUri();
        CmsUriDescriptor uriDesc = null;
        CmsUriLocator uriLoc = null;
        CmsUri cmsUri = null;

        String templateClass = null;
        String templateName = null;
        CmsXmlControlFile doc = null;

        if(elementCacheEnabled) {
            // Get the global element cache object
            elementCache = cms.getRequestContext().getElementCache();

            // Prepare URI Locator
            uriDesc = new CmsUriDescriptor(uri);
            uriLoc = elementCache.getUriLocator();
            cmsUri = uriLoc.get(uriDesc);
        }

        if(cmsUri == null || !elementCacheEnabled) {
            // Entry point to page file analysis.
            // For performance reasons this should only be done if the element
            // cache is not activated or if it's activated but no URI object could be found.

            // Parse the page file
            try {
                doc = new CmsXmlControlFile(cms, file);
            }
            catch(Exception e) {
                // there was an error while parsing the document.
                // No chance to go on here.
                handleException(cms, e, "There was an error while parsing XML page file " + file.getAbsolutePath());
                return "".getBytes();
            }

            // Get the names of the master template and the template class from
            // the parsed page file. Fall back to default value, if template class
            // is not defined
            templateClass = doc.getTemplateClass();
            if(templateClass == null || "".equals(templateClass)) {
                templateClass = startTemplateClass;
            }
            if(templateClass == null || "".equals(templateClass)) {
                templateClass = "com.opencms.template.CmsXmlTemplate";
            }
            templateName = doc.getMasterTemplate();
            if(templateName != null && !"".equals(templateName)){
                templateName = Utils.mergeAbsolutePath(file.getAbsolutePath(), templateName);
            }

            // Previously, the template class was loaded here.
            // We avoid doing this so early, since in element cache mode the template
            // class is not needed here.

            // Now look for parameters in the page file...
            // ... first the params of the master template...
            Enumeration masterTemplateParams = doc.getParameterNames();
            while(masterTemplateParams.hasMoreElements()) {
                String paramName = (String)masterTemplateParams.nextElement();
                String paramValue = doc.getParameter(paramName);
                newParameters.put(C_ROOT_TEMPLATE_NAME + "." + paramName, paramValue);
            }

            // ... and now the params of all subtemplates
            Enumeration elementDefinitions = doc.getElementDefinitions();
            while(elementDefinitions.hasMoreElements()) {
                String elementName = (String)elementDefinitions.nextElement();
                if(doc.isElementClassDefined(elementName)) {
                    newParameters.put(elementName + "._CLASS_", doc.getElementClass(elementName));
                }
                if(doc.isElementTemplateDefined(elementName)) {
                    newParameters.put(elementName + "._TEMPLATE_", doc.getElementTemplate(elementName));
                }
                if(doc.isElementTemplSelectorDefined(elementName)) {
                    newParameters.put(elementName + "._TEMPLATESELECTOR_", doc.getElementTemplSelector(elementName));
                }
                Enumeration parameters = doc.getElementParameterNames(elementName);
                while(parameters.hasMoreElements()) {
                    String paramName = (String)parameters.nextElement();
                    String paramValue = doc.getElementParameter(elementName, paramName);
                    if(paramValue != null) {
                        newParameters.put(elementName + "." + paramName, paramValue);
                    }
                    else {
                        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                            A_OpenCms.log(C_OPENCMS_INFO, getClassName() + "Empty parameter \"" + paramName + "\" found.");
                        }
                    }
                }
            }
        }

        // URL parameters ary really dynamic.
        // We cannot store them in an element cache.
        // Therefore these parameters must be collected in ANY case!

        String datafor = req.getParameter("datafor");
        if(datafor == null) {
            datafor = "";
        } else {
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

        if(elementCacheEnabled && cmsUri == null) {
            // ---- element cache stuff --------
            // No URI could be found in cache.
            // So create a new URI object with a start element and store it using the UriLocator
            CmsElementDescriptor elemDesc = new CmsElementDescriptor(templateClass, templateName);
            CmsElementDefinitionCollection eldefs = doc.getElementDefinitionCollection();
            cmsUri = new CmsUri(elemDesc, cms.getReadingpermittedGroup(
                        cms.getRequestContext().currentProject().getId(),
                        templateName), eldefs);
            elementCache.getUriLocator().put(uriDesc, cmsUri);
        }

        if(elementCacheEnabled) {
                output = elementCache.callCanonicalRoot(cms, newParameters);
        } else {
            // ----- traditional stuff ------
            // Element cache is deactivated. So let's go on as usual.
            try {
                CmsFile masterTemplate = loadMasterTemplateFile(cms, templateName, doc);
                I_CmsTemplate tmpl = getTemplateClass(cms, templateClass);
                if(!(tmpl instanceof I_CmsXmlTemplate)) {
                    String errorMessage = "Error in " + file.getAbsolutePath() + ": " + templateClass + " is not a XML template class.";
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                        A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
                    }
                    throw new CmsException(errorMessage, CmsException.C_XML_WRONG_TEMPLATE_CLASS);
                }
                output = callCanonicalRoot(cms, (I_CmsTemplate)tmpl, masterTemplate, newParameters);
            }
            catch(CmsException e) {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlLauncher] There were exceptions while generating output for " + file.getAbsolutePath());
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlLauncher] Clearing template file cache for this file.");
                }
                doc.removeFromFileCache();
                throw e;
            }
        }
        return output;
    }

    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId() {
        return C_TYPE_XML;
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

        // get the CmsRequest
        I_CmsRequest req = cms.getRequestContext().getRequest();
        byte[] result = null;
        result = generateOutput(cms, file, startTemplateClass, req, openCms);
        if(result != null) {
            writeBytesToResponse(cms, result);
        }
    }

    /**
     * Internal utility method for checking and loading a given template file.
     * @param cms CmsObject for accessing system resources.
     * @param templateName Name of the requestet template file.
     * @param doc CmsXmlControlFile object containig the parsed body file.
     * @return CmsFile object of the requested template file.
     * @exception CmsException
     */
    private CmsFile loadMasterTemplateFile(CmsObject cms, String templateName, com.opencms.template.CmsXmlControlFile doc) throws CmsException {
        CmsFile masterTemplate = null;
        try {
            masterTemplate = cms.readFile(templateName);
        }
        catch(Exception e) {
            handleException(cms, e, "Cannot load master template " + templateName + ". ");
            doc.removeFromFileCache();
        }
        return masterTemplate;
    }
}

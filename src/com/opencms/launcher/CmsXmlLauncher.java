/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsXmlLauncher.java,v $
* Date   : $Date: 2003/07/03 13:29:45 $
* Version: $Revision: 1.47 $
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
import com.opencms.template.CmsXmlControlFile;
import com.opencms.template.I_CmsTemplate;
import com.opencms.template.I_CmsXmlTemplate;
import com.opencms.template.cache.CmsElementCache;
import com.opencms.template.cache.CmsElementDefinition;
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
 * OpenCms launcher class for XML templates.<p>
 * 
 * This can be used generating output for XML body files using XML template and
 * subtemplate technology.<p>
 * 
 * The selected body should define a start template class using <BR> <CODE>
 * &lt;PAGE&gt;<BR>
 * &nbsp;&nbsp;&lt;CLASS&gt;...&lt;/CLASS&gt;<BR>
 * &lt;/PAGE&gt;</CODE><p>
 *
 * If no start template is defined, the class given by the parameters
 * will be used.
 * If even this is not defined, CmsXmlTemplate will
 * be used to create output.<p>
 *
 * @author Alexander Lucas
 * @version $Revision: 1.47 $ $Date: 2003/07/03 13:29:45 $
 */
public class CmsXmlLauncher extends A_CmsLauncher implements I_CmsLogChannels, I_CmsConstants {
    
    /** Magic elemet replace name */
    public static final String C_ELEMENT_REPLACE = "_CMS_ELEMENTREPLACE";

    /**
     * Starts generating the output.
     * Calls the canonical root with the appropriate template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param file CmsFile Object with the selected resource to be shown
     * @param startTemplateClass Name of the template class to start with.
     * @throws CmsException
     */
    protected byte[] generateOutput(CmsObject cms, CmsFile file, String startTemplateClass, I_CmsRequest req) throws CmsException {
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

        // check if printversion is requested
        String replace = req.getParameter(C_ELEMENT_REPLACE);
        boolean elementreplace = false;
        CmsElementDefinition replaceDef = null;
        if(replace != null){
            int index = replace.indexOf(":");
            if(index != -1){
                elementreplace = true;
                cmsUri = null;
                replaceDef = new CmsElementDefinition(replace.substring(0,index),
                                        "com.opencms.template.CmsXmlTemplate",
                                        replace.substring(index+1), null, new Hashtable());
            }
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
                handleException(cms, e, "There was an error while parsing XML page file " + cms.readAbsolutePath(file));
                return "".getBytes();
            }

            if (! elementCacheEnabled && (replaceDef != null)) {
                // Required to enable element replacement if element cache is disabled
                doc.setElementClass(replaceDef.getName(), replaceDef.getClassName());
                doc.setElementTemplate(replaceDef.getName(), replaceDef.getTemplateName());
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
                templateName = Utils.mergeAbsolutePath(cms.readAbsolutePath(file), templateName);
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
                    // need to check for the body template here so that non-XMLTemplate templates 
                    // like JSPs know where to find the body defined in the XMLTemplate
                    String template = doc.getElementTemplate(elementName);
                    template = doc.validateBodyPath(cms, template, file);
                    if (I_CmsConstants.C_XML_BODY_ELEMENT.equalsIgnoreCase(elementName)) {
                        // found body element
                        if (template != null) {
                            cms.getRequestContext().setAttribute(I_CmsConstants.C_XML_BODY_ELEMENT, template);
                        }
                    } 
                    newParameters.put(elementName + "._TEMPLATE_", template);
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
            }else {
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
            if(elementreplace){
                // we cant cach this
                eldefs.add(replaceDef);
//				TODO: fix this later - check how to do this without getReadingpermittedGroup
//				String readAccessGroup = CmsObject.C_GROUP_ADMIN;
				String readAccessGroup = cms.getReadingpermittedGroup(cms.getRequestContext().currentProject().getId(),templateName);
                cmsUri = new CmsUri(elemDesc, readAccessGroup, eldefs, Utils.isHttpsResource(cms, file));
            }else{
                cmsUri = new CmsUri(elemDesc, cms.getReadingpermittedGroup(
                            cms.getRequestContext().currentProject().getId(),
                            templateName), eldefs, Utils.isHttpsResource(cms, file));
                elementCache.getUriLocator().put(uriDesc, cmsUri);
            }
        }

        if(elementCacheEnabled) {
                // lets check if ssl is active
                if(cms.getMode() == C_MODUS_ONLINE){
                    String scheme = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getScheme();
                    boolean httpsReq = "https".equalsIgnoreCase(scheme);
                    if(cmsUri.isHttpsResource() != httpsReq){
                        if(httpsReq){
                            //throw new CmsException(" "+cms.readPath(file)+" needs a http request", CmsException.C_HTTPS_PAGE_ERROR);
                        }else if(CmsObject.getStaticExportProperties().isStaticExportEnabled()
                                || "false_ssl".equals(CmsObject.getStaticExportProperties().getStaticExportEnabledValue())){
                            // check if static export is enabled and value is not false_ssl
                            throw new CmsException(" "+cms.readAbsolutePath(file)+" needs a https request", CmsException.C_HTTPS_REQUEST_ERROR);
                        }
                    }
                }
                // now lets get the output
                if(elementreplace){
                    output = cmsUri.callCanonicalRoot(elementCache, cms, newParameters);
                }else{
                    output = elementCache.callCanonicalRoot(cms, newParameters);
                }
        } else {
            // ----- traditional stuff ------
            // Element cache is deactivated. So let's go on as usual.
            try {
                CmsFile masterTemplate = loadMasterTemplateFile(cms, templateName, doc);
                I_CmsTemplate tmpl = getTemplateClass(cms, templateClass);               
                if(!(tmpl instanceof I_CmsXmlTemplate)) {
                    String errorMessage = "Error in " + cms.readAbsolutePath(file) + ": " + templateClass + " is not a XML template class.";
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                        A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
                    }
                    throw new CmsException(errorMessage, CmsException.C_XML_WRONG_TEMPLATE_CLASS);
                }
                output = callCanonicalRoot(cms, (I_CmsTemplate)tmpl, masterTemplate, newParameters);
            }
            catch(CmsException e) {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlLauncher] There were exceptions while generating output for " + cms.readAbsolutePath(file));
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
     * @throws CmsException
     */
    protected void launch(CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms) throws CmsException {

        // get the CmsRequest
        I_CmsRequest req = cms.getRequestContext().getRequest();
        byte[] result = null;
        result = generateOutput(cms, file, startTemplateClass, req);
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
     * @throws CmsException
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

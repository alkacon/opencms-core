/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsXmlTemplateLoader.java,v $
 * Date   : $Date: 2004/03/29 10:39:54 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.legacy;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspTagInclude;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.I_CmsLoaderIncludeExtension;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.core.CmsRequestHttpServlet;
import com.opencms.core.CmsResponseHttpServlet;
import com.opencms.core.CmsSession;
import com.opencms.core.I_CmsRequest;
import com.opencms.core.I_CmsResponse;
import com.opencms.core.I_CmsSession;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsRootTemplate;
import com.opencms.template.CmsTemplateCache;
import com.opencms.template.CmsTemplateClassManager;
import com.opencms.template.CmsXmlControlFile;
import com.opencms.template.CmsXmlTemplate;
import com.opencms.template.I_CmsTemplate;
import com.opencms.template.I_CmsTemplateCache;
import com.opencms.template.I_CmsXmlTemplate;
import com.opencms.template.cache.CmsElementCache;
import com.opencms.template.cache.CmsElementDefinition;
import com.opencms.template.cache.CmsElementDefinitionCollection;
import com.opencms.template.cache.CmsElementDescriptor;
import com.opencms.template.cache.CmsUri;
import com.opencms.template.cache.CmsUriDescriptor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Implementation of the {@link I_CmsResourceLoader} for XMLTemplates.<p>
 * 
 * Parameters supported by this loader:<dl>
 * 
 * <dt>elementcache.enabled</dt>
 * <dd>(Optional) Controls if the legacy XMLTemplate element cache is disabled 
 * (the default) or enabled. Enable the element cache only to support old
 * XMLTemplate based code that depend on specific element cache behaviour.</dd>
 * 
 * <dt>elementcache.uri</dt>
 * <dd>(Optional) Element cache URI size. The default is 10000.</dd>
 * 
 * <dt>elementcache.elements</dt>
 * <dd>(Optional) Element cache element size. The default is 50000.</dd>
 * 
 * <dt>elementcache.variants</dt>
 * <dd>(Optional) Element cache variant size. The default is 100.</dd></dl>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.16 $
 */
public class CmsXmlTemplateLoader implements I_CmsResourceLoader, I_CmsLoaderIncludeExtension {
    
    /** Magic elemet replace name */
    public static final String C_ELEMENT_REPLACE = "_CMS_ELEMENTREPLACE";
    
    /** URI of the bodyloader XML file in the OpenCms VFS*/    
    public static final String C_BODYLOADER_URI = I_CmsWpConstants.C_VFS_PATH_SYSTEM + "shared/bodyloader.html";
            
    /** The id of this loader */
    public static final int C_RESOURCE_LOADER_ID = 3;

    /** Flag for debugging output. Set to 9 for maximum verbosity. */ 
    private static final int DEBUG = 0;

    /** The element cache used for the online project */
    private static CmsElementCache m_elementCache;

    /** The template cache that holds all cached templates */
    private static I_CmsTemplateCache m_templateCache;
    
    /** The variant dependencies for the element cache */
    private static Hashtable m_variantDeps;
    
    /** The resource loader configuration */
    private ExtendedProperties m_configuration;
        
    /**
     * The constructor of the class is empty and does nothing.
     */
    public CmsXmlTemplateLoader() {
        m_templateCache = new CmsTemplateCache();
        m_configuration = new ExtendedProperties();
    }
    
    /**
     * Compatibility method to ensure the legacy cache command line parameters
     * are still supported.<p>
     * 
     * @param clearFiles if true, A_CmsXmlContent cache is cleared
     * @param clearTemplates if true, internal template cache is cleared
     */
    private static void clearLoaderCache(boolean clearFiles, boolean clearTemplates) {
        if (clearFiles) {
            A_CmsXmlContent.clearFileCache();
        }
        if (clearTemplates) {
            m_templateCache.clearCache();
        }        
    }

    /**
     * Returns the element cache,
     * or null if the element cache is not initialized.<p>
     * 
     * @return the element cache that belongs to the given cms context
     */        
    public static final CmsElementCache getElementCache() {
        return m_elementCache;
    }

    /**
     * Returns the variant dependencies of the online element cache.<p>
     * 
     * @return the variant dependencies of the online element cache
     */
    public static final CmsElementCache getOnlineElementCache() {
        return m_elementCache;
    }
    
    /**
     * Returns the hashtable with the variant dependencies used for the elementcache.<p>
     * 
     * @return the hashtable with the variant dependencies used for the elementcache
     */
    public static final Hashtable getVariantDependencies() {
        return m_variantDeps;
    }    
    
    /**
     * Returns true if the element cache is enabled.<p>
     * 
     * @return true if the element cache is enabled
     */
    public static final boolean isElementCacheEnabled() {
        return m_elementCache != null;
    }

    /**
     * Utility method used by the loader implementation to give control
     * to the CanonicalRoot.<p>
     * 
     * The CanonicalRoot will call the master template and return a byte array of the
     * generated output.<p>
     *
     * @param cms the cms context object
     * @param templateClass to generate the output of the master template
     * @param masterTemplate masterTemplate for the output
     * @param parameters contains all parameters for the template class
     * @return the generated output or null if there were errors
     * @throws CmsException if something goes wrong
     */
    private byte[] callCanonicalRoot(CmsObject cms, I_CmsTemplate templateClass, CmsFile masterTemplate, Hashtable parameters) throws CmsException {
        try {
            CmsRootTemplate root = new CmsRootTemplate();
            return root.getMasterTemplate(cms, templateClass, masterTemplate, m_templateCache, parameters);
        } catch (Exception e) {
            // no document we could show...
            handleException(cms, e, "Received error while calling canonical root for requested file " + masterTemplate.getName() + ". ");
        }
        return null;
    }
        
    /** 
     * Destroy this ResourceLoder, this is a NOOP so far.  
     */
    public void destroy() {
        // NOOP
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res) 
    throws IOException, CmsException {
        
        CmsFile file = CmsFile.upgrade(resource, cms);
        initLegacyRequest(cms, req, res);
        CmsRequestHttpServlet cmsReq = new CmsRequestHttpServlet(req, cms.getRequestContext().getFileTranslator());
        return generateOutput(cms, file, cmsReq);
    }        
    
    /**
     * Starts generating the output.
     * Calls the canonical root with the appropriate template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param file CmsFile Object with the selected resource to be shown
     * @param req the CmsRequest
     * @return the generated output for the file
     * @throws CmsException if something goes wrong
     */
    protected byte[] generateOutput(CmsObject cms, CmsFile file, I_CmsRequest req) throws CmsException {
        byte[] output = null;

        // hashtable for collecting all parameters.
        Hashtable newParameters = new Hashtable();
        String uri = cms.getRequestContext().getUri();

        // collect xml template information
        String absolutePath = cms.readAbsolutePath(file);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("absolutePath=" + absolutePath);
        }
        String templateProp = cms.readProperty(absolutePath, I_CmsConstants.C_PROPERTY_TEMPLATE);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("templateProp=" + templateProp);
        }
        String templateClassProp = cms.readProperty(absolutePath, I_CmsConstants.C_PROPERTY_BODY_CLASS, false, I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("templateClassProp=" + templateClassProp);
        }
        
        // ladies and gentelman: and now for something completly different 
        String xmlTemplateContent = null;
        if (templateProp != null) {
            // i got a black magic template,
            StringBuffer buf = new StringBuffer(256);
            buf.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
            buf.append("<PAGE>\n<class>");
            buf.append(I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS);
            buf.append("</class>\n<masterTemplate>");
            // i got a black magic template,
            buf.append(templateProp);
            buf.append("</masterTemplate>\n<ELEMENTDEF name=\"body\">\n<CLASS>");
            buf.append(templateClassProp);
            buf.append("</CLASS>\n<TEMPLATE>");
            // i got a black magic template got me so blind I can't see,
            buf.append(uri);
            buf.append("</TEMPLATE>\n</ELEMENTDEF>\n</PAGE>\n");              
            // i got a black magic template it's try'in to make a devil out of me...
            xmlTemplateContent = buf.toString();
            uri += I_CmsConstants.C_XML_CONTROL_FILE_SUFFIX; 
        }
        
        // Parameters used for element cache
        boolean elementCacheEnabled = CmsXmlTemplateLoader.isElementCacheEnabled();
        CmsElementCache elementCache = null;
        CmsUriDescriptor uriDesc = null;
        CmsUri cmsUri = null;

        String templateClass = null;
        String templateName = null;
        CmsXmlControlFile doc = null;

        if (elementCacheEnabled) {
            // Get the global element cache object
            elementCache = CmsXmlTemplateLoader.getElementCache();

            // Prepare URI Locator
            uriDesc = new CmsUriDescriptor(uri);
            cmsUri = elementCache.getUriLocator().get(uriDesc);
            // check if cached
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("found cmsUri=" + cmsUri);
        }
            if ((cmsUri != null) && !cmsUri.getElementDescriptor().getTemplateName().equalsIgnoreCase(templateProp)) {
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("cmsUri has different template: " + cmsUri.getElementDescriptor().getTemplateName()
                            + " than current template: " + templateProp + ", not using cmsUri from cache");
                }
                cmsUri = null;
            }            
        }

        // check if printversion is requested
        String replace = req.getParameter(C_ELEMENT_REPLACE);
        boolean elementreplace = false;
        CmsElementDefinition replaceDef = null;
        if (replace != null) {
            int index = replace.indexOf(":");
            if (index != -1) {
                elementreplace = true;
                cmsUri = null;
                String elementName = replace.substring(0, index);
                String replaceUri = replace.substring(index+1);               
                replaceDef = new CmsElementDefinition(elementName,
                                        I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS,
                                        replaceUri, null, new Hashtable());
                newParameters.put(C_ELEMENT_REPLACE + "_VFS_" + elementName, cms.getRequestContext().addSiteRoot(replaceUri));
            }
        }

        if ((cmsUri == null) || !elementCacheEnabled) {
            // Entry point to page file analysis.
            // For performance reasons this should only be done if the element
            // cache is not activated or if it's activated but no URI object could be found.

            // Parse the page file
            try {
                if (xmlTemplateContent == null) {
                    doc = new CmsXmlControlFile(cms, file);
                } else {
                    doc = new CmsXmlControlFile(cms, uri, xmlTemplateContent);
                }
            } catch (Exception e) {
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
            if (templateClass == null || "".equals(templateClass)) {
                templateClass = this.getClass().getName();
            }
            if (templateClass == null || "".equals(templateClass)) {
                templateClass = I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS;
            }
            templateName = doc.getMasterTemplate();
            if (templateName != null && !"".equals(templateName)) {
                templateName = CmsLinkManager.getAbsoluteUri(templateName, cms.readAbsolutePath(file));
            }

            // Previously, the template class was loaded here.
            // We avoid doing this so early, since in element cache mode the template
            // class is not needed here.

            // Now look for parameters in the page file...
            // ... first the params of the master template...
            Enumeration masterTemplateParams = doc.getParameterNames();
            while (masterTemplateParams.hasMoreElements()) {
                String paramName = (String)masterTemplateParams.nextElement();
                String paramValue = doc.getParameter(paramName);
                newParameters.put(I_CmsConstants.C_ROOT_TEMPLATE_NAME + "." + paramName, paramValue);
            }

            // ... and now the params of all subtemplates
            Enumeration elementDefinitions = doc.getElementDefinitions();
            while (elementDefinitions.hasMoreElements()) {
                String elementName = (String)elementDefinitions.nextElement();
                if (doc.isElementClassDefined(elementName)) {
                    newParameters.put(elementName + "._CLASS_", doc.getElementClass(elementName));
                }
                if (doc.isElementTemplateDefined(elementName)) {
                    // need to check for the body template here so that non-XMLTemplate templates 
                    // like JSPs know where to find the body defined in the XMLTemplate
                    String template = doc.getElementTemplate(elementName);
                    if (xmlTemplateContent == null) {
                        template = doc.validateBodyPath(cms, template, file);
                    }
                    if (I_CmsConstants.C_XML_BODY_ELEMENT.equalsIgnoreCase(elementName)) {
                        // found body element
                        if (template != null) {
                            cms.getRequestContext().setAttribute(I_CmsConstants.C_XML_BODY_ELEMENT, template);
                        }
                    } 
                    newParameters.put(elementName + "._TEMPLATE_", template);
                }
                if (doc.isElementTemplSelectorDefined(elementName)) {
                    newParameters.put(elementName + "._TEMPLATESELECTOR_", doc.getElementTemplSelector(elementName));
                }
                Enumeration parameters = doc.getElementParameterNames(elementName);
                while (parameters.hasMoreElements()) {
                    String paramName = (String)parameters.nextElement();
                    String paramValue = doc.getElementParameter(elementName, paramName);
                    if (paramValue != null) {
                        newParameters.put(elementName + "." + paramName, paramValue);
                    } else {
                        if (OpenCms.getLog(this).isInfoEnabled()) {
                            OpenCms.getLog(this).info("Empty parameter \"" + paramName + "\" found.");
                        }
                    }
                }
            }
        }

        // URL parameters ary really dynamic.
        // We cannot store them in an element cache.
        // Therefore these parameters must be collected in ANY case!

        String datafor = req.getParameter("datafor");
        if (datafor == null) {
            datafor = "";
        } else {
            if (!"".equals(datafor)) {
                datafor = datafor + ".";
            }
        }
        Enumeration urlParameterNames = req.getParameterNames();
        while (urlParameterNames.hasMoreElements()) {
            String pname = (String)urlParameterNames.nextElement();
            String paramValue = req.getParameter(pname);
            if (paramValue != null) {
                if ((!"datafor".equals(pname)) && (!"_clearcache".equals(pname))) {
                    newParameters.put(datafor + pname, paramValue);
                }
            } else {
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info("Empty URL parameter \"" + pname + "\" found.");
                }
            }
        }

        if (elementCacheEnabled && (cmsUri == null)) {
            // ---- element cache stuff --------
            // No URI could be found in cache.
            // So create a new URI object with a start element and store it using the UriLocator
            CmsElementDescriptor elemDesc = new CmsElementDescriptor(templateClass, templateName);
            CmsElementDefinitionCollection eldefs = doc.getElementDefinitionCollection();
            if (elementreplace) {
                // we cant cach this
                eldefs.add(replaceDef);
                cmsUri = new CmsUri(elemDesc, eldefs);
            } else {
                cmsUri = new CmsUri(elemDesc, eldefs);
                elementCache.getUriLocator().put(uriDesc, cmsUri);
            }
        }

        if (elementCacheEnabled) {
//        patched 23.1.2004 by ph@ethikom.de: otherwise problems with cached and parsed XML content appear
            // TODO: Make cache more efficient
            clearLoaderCache(true, true);

            // now lets get the output
            if (elementreplace) {
                output = cmsUri.callCanonicalRoot(elementCache, cms, newParameters);
            } else {
                output = elementCache.callCanonicalRoot(cms, newParameters, uri);
            }
        } else {
            // ----- traditional stuff ------
            // Element cache is deactivated. So let's go on as usual.
            try {
                CmsFile masterTemplate = loadMasterTemplateFile(cms, templateName, doc);
                I_CmsTemplate tmpl = getTemplateClass(templateClass);               
                if (!(tmpl instanceof I_CmsXmlTemplate)) {
                    String errorMessage = "Error in " + cms.readAbsolutePath(file) + ": " + templateClass + " is not a XML template class.";
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error(errorMessage);
                    } 
                    throw new CmsException(errorMessage, CmsException.C_XML_WRONG_TEMPLATE_CLASS);
                }
                // TODO: Make cache more efficient
                clearLoaderCache(true, true);
                output = callCanonicalRoot(cms, tmpl, masterTemplate, newParameters);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this);
                }
                doc.removeFromFileCache();
                throw e;
            }
        }
        return output;
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {
        return C_RESOURCE_LOADER_ID;
    }      
    
    /**
     * Return a String describing the ResourceLoader,
     * which is <code>"The OpenCms default resource loader for XMLTemplates"</code>.<p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        return "The OpenCms default resource loader for XMLTemplates";
    }   

    /**
     * Calls the CmsClassManager to get an instance of the given template class.<p>
     * 
     * The returned object is checked to be an implementing class of the interface
     * I_CmsTemplate.
     * If the template cache of the template class is not yet set up, 
     * this will be done, too.<p>
     * 
     * @param classname name of the requested template class
     * @return instance of the template class
     * @throws CmsException if something goes wrong
     */
    private I_CmsTemplate getTemplateClass(String classname) throws CmsException {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Getting start template class: " + classname);
        }
        Object o = CmsTemplateClassManager.getClassInstance(classname);

        // Check, if the loaded class really is a OpenCms template class.

        // This is done be checking the implemented interface.
        if (!(o instanceof I_CmsTemplate)) {
            String errorMessage = "Class " + classname + " is not an OpenCms template class.";
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_XML_NO_TEMPLATE_CLASS);
        }
        I_CmsTemplate cmsTemplate = (I_CmsTemplate)o;
        if (!cmsTemplate.isTemplateCacheSet()) {
            cmsTemplate.setTemplateCache(m_templateCache);
        }
        return cmsTemplate;
    }

    /**
     * Utility method to handle any occurence of an execption.<p>
     * 
     * If the Exception is NO CmsException (i.e. it was not detected previously)
     * it will be written to the logfile.<p>
     * 
     * If the current user is the anonymous user, no further exception will
     * be thrown, but a server error will be sent
     * (we want to prevent the user from seeing any exeptions).
     * Otherwise a new Exception will be thrown.
     * This will trigger the OpenCms error message box.<p>
     *
     * @param cms the cms context object
     * @param e Exception that should be handled
     * @param errorText error message that should be shown
     * @throws CmsException if 
     */
    private void handleException(CmsObject cms, Exception e, String errorText) throws CmsException {

        // log the error if it is no CmsException
        if (! (e instanceof CmsException)) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(errorText, e);
            }
        }

        // if the user is a guest (and it's not a login exception) we send an servlet error,
        // otherwise we try to throw an exception.
        CmsRequestContext reqContext = cms.getRequestContext();
        if ((DEBUG == 0) && reqContext.currentUser().isGuestUser()
            && (!(e instanceof CmsException && ((CmsException)e).getType() == CmsException.C_NO_USER))) {
            throw new CmsException(errorText, CmsException.C_SERVICE_UNAVAILABLE, e);
        } else {
            if (e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                throw new CmsException(errorText, CmsLoaderException.C_LOADER_GENERIC_ERROR, e);
            }
        }
    }
    
    /** 
     * Initialize this resource loader.<p> 
     */
    public void initialize() {
        // check if the element cache is enabled
        boolean elementCacheEnabled = m_configuration.getBoolean("elementcache.enabled", false);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Loader init          : XMLTemplate element cache " + (elementCacheEnabled ? "enabled" : "disabled"));
        }
        if (elementCacheEnabled) {
            try {
                m_elementCache = new CmsElementCache(
                    m_configuration.getInteger("elementcache.uri", 10000), 
                    m_configuration.getInteger("elementcache.elements", 50000), 
                    m_configuration.getInteger("elementcache.variants", 100));
            } catch (Exception e) {
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(". Loader init          : XMLTemplate element cache non-critical error " + e.toString());
                }
            }
            m_variantDeps = new Hashtable();
            m_elementCache.getElementLocator().setExternDependencies(m_variantDeps);
        } else {
            m_elementCache = null;
        }
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) { 
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Loader init          : " + this.getClass().getName() + " initialized");
        }             
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res) 
    throws CmsException {
        initLegacyRequest(cms, req, res);        
        processXmlTemplate(cms, CmsFile.upgrade(resource, cms));
    }
    
    /**
     * Initializes the current request with the legacy Cms request and response wrappers.<p>
     * 
     * @param cms the current cms context
     * @param req the request to wrap
     * @param res the response to wrap
     * @throws CmsException if something goes wrong
     */
    public static void initLegacyRequest(CmsObject cms, HttpServletRequest req, HttpServletResponse res) throws CmsException {
        if (cms.getRequestContext().getAttribute(I_CmsRequest.C_CMS_REQUEST) != null) {
            return;
        }
        try {  
            CmsRequestHttpServlet cmsReq = new CmsRequestHttpServlet(req, cms.getRequestContext().getFileTranslator());
            CmsResponseHttpServlet cmsRes = new CmsResponseHttpServlet(req, res);
            cms.getRequestContext().setAttribute(I_CmsRequest.C_CMS_REQUEST, cmsReq);
            cms.getRequestContext().setAttribute(I_CmsResponse.C_CMS_RESPONSE, cmsRes);
        } catch (IOException e) {
            throw new CmsLoaderException("Trouble setting up legacy request / response", e);
        }    
    }
    
    /**
     * Internal utility method for checking and loading a given template file.
     * @param cms CmsObject for accessing system resources.
     * @param templateName Name of the requestet template file.
     * @param doc CmsXmlControlFile object containig the parsed body file.
     * @return CmsFile object of the requested template file.
     * @throws CmsException if something goes wrong
     */
    private CmsFile loadMasterTemplateFile(CmsObject cms, String templateName, com.opencms.template.CmsXmlControlFile doc) throws CmsException {
        CmsFile masterTemplate = null;
        try {
            masterTemplate = cms.readFile(templateName);
        } catch (Exception e) {
            handleException(cms, e, "Cannot load master template " + templateName + ". ");
            doc.removeFromFileCache();
        }
        return masterTemplate;
    }
    
    /**
     * Processes the XmlTemplates and writes the result to 
     * the apropriate output stream, which is obtained from the request 
     * context of the cms object.<p>
     *
     * @param cms the cms context object
     * @param file the selected resource to be shown
     * @throws CmsException if something goes wrong
     */
    private void processXmlTemplate(CmsObject cms, CmsFile file) throws CmsException {

        // first some debugging output.
        if ((DEBUG > 0) && OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Loader started for " + file.getName());
        }

        // check all values to be valid
        String errorMessage = null;
        if (file == null) {
            errorMessage = "CmsFile missing";
        }
        if (cms == null) {
            errorMessage = "CmsObject missing";
        }
        if (errorMessage != null) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(errorMessage);
            } 
            throw new CmsException(errorMessage, CmsLoaderException.C_LOADER_GENERIC_ERROR);
        }

        // Check the clearcache parameter
        String clearcache = getRequest(cms.getRequestContext()).getParameter("_clearcache");
        
        // Clear loader caches if this is required
        clearLoaderCache(((clearcache != null) && ("all".equals(clearcache) || "file".equals(clearcache))), 
            ((clearcache != null) && ("all".equals(clearcache) || "template".equals(clearcache))));
        
        // get the CmsRequest
        I_CmsRequest req = getRequest(cms.getRequestContext());
        byte[] result = generateOutput(cms, file, req);
        if (result != null) {
            writeBytesToResponse(cms, result);
        }
    }    
        
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res)
    throws CmsException, IOException {
        
        long timer1;
        if (DEBUG > 0) {
            timer1 = System.currentTimeMillis();        
            System.err.println("============ CmsXmlTemplateLoader loading: " + cms.readAbsolutePath(file));            
            System.err.println("CmsXmlTemplateLoader.service() cms uri is: " + cms.getRequestContext().getUri());            
        }
        // save the original context settings
        String rnc = cms.getRequestContext().getEncoding().trim();
        // String oldUri = cms.getRequestContext().getUri();
        
        initLegacyRequest(cms, (HttpServletRequest)req, (HttpServletResponse)res);        
        I_CmsRequest cms_req = CmsXmlTemplateLoader.getRequest(cms.getRequestContext());        
        HttpServletRequest originalreq = cms_req.getOriginalRequest();
        
        try {                        
            // get the CmsRequest
            byte[] result = null;
            org.opencms.file.CmsFile fx = cms.readFile(cms.readAbsolutePath(file));            
            // care about encoding issues
            String dnc = OpenCms.getSystemInfo().getDefaultEncoding().trim();
            String enc = cms.readProperty(cms.readAbsolutePath(fx), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true, dnc).trim();
            // fake the called URI (otherwise XMLTemplate / ElementCache would not work)
            // cms.getRequestContext().setUri(cms.readAbsolutePath(fx));            
            cms_req.setOriginalRequest((HttpServletRequest)req);
            cms.getRequestContext().setEncoding(enc);      
            if (DEBUG > 1) {
                System.err.println("CmsXmlTemplateLoader.service(): Encodig set to " + cms.getRequestContext().getEncoding());
                System.err.println("CmsXmlTemplateLoader.service(): Uri set to " + cms.getRequestContext().getUri());
            }
            // process the included XMLTemplate
            result = generateOutput(cms, fx, cms_req);                                    
            // append the result to the output stream
            if (result != null) {
                if (DEBUG > 1) {
                    System.err.println("CmsXmlTemplateLoader.service(): encoding=" + enc + " requestEncoding=" + rnc + " defaultEncoding=" + dnc);
                }
                res.getOutputStream().write(result);
            }        
        } finally {
            // restore the context settings
            cms_req.setOriginalRequest(originalreq);
            cms.getRequestContext().setEncoding(rnc);
            // cms.getRequestContext().setUri(oldUri);
            if (DEBUG > 1) {
                System.err.println("CmsXmlTemplateLoader.service(): Encodig reset to " + cms.getRequestContext().getEncoding());
                System.err.println("CmsXmlTemplateLoader.service(): Uri reset to " + cms.getRequestContext().getUri());
            }
        }
        
        if (DEBUG > 0) {
            long timer2 = System.currentTimeMillis() - timer1;        
            System.err.println("============ CmsXmlTemplateLoader time delivering XmlTemplate for " + cms.readAbsolutePath(file) + ": " + timer2 + "ms");            
        }
    } 

    /**
     * Writes a given byte array to the HttpServletRespose output stream.<p>
     * 
     * @param cms an initialized CmsObject
     * @param result byte array that should be written.
     * @throws CmsException if something goes wrong
     */
    private void writeBytesToResponse(CmsObject cms, byte[] result) throws CmsException {
        try {
            I_CmsResponse resp = getResponse(cms.getRequestContext());
            if ((result != null) && !resp.isRedirected()) {
                // Only write any output to the response output stream if
                // the current request is neither redirected nor streamed.
                OutputStream out = resp.getOutputStream();

                resp.setContentLength(result.length);
                resp.setHeader("Connection", "keep-alive");
                out.write(result);
                out.close();
            }
        } catch (IOException ioe) {
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("IO error while writing to response stream for " + cms.getRequestContext().getUri(), ioe);
            }
        } catch (Exception e) {
            String errorMessage = "Cannot write output to HTTP response stream";
            handleException(cms, e, errorMessage);
        }
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#dump(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, java.util.Locale, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] dump(CmsObject cms, CmsResource file, String element, Locale locale, HttpServletRequest req, HttpServletResponse res) 
    throws CmsException {
        initLegacyRequest(cms, req, res);        
        String absolutePath = cms.readAbsolutePath(file);
        // this will work for the "default" template class com.opencms.template.CmsXmlTemplate only
        CmsXmlTemplate template = new CmsXmlTemplate();
        // get the appropriate content and convert it to bytes
        return template.getContent(cms, absolutePath, element, null);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportEnabled()
     */
    public boolean isStaticExportEnabled() {
        return true;
    }
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportProcessable()
     */
    public boolean isStaticExportProcessable() {
        return true;
    }
    
    
    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsableForTemplates()
     */
    public boolean isUsableForTemplates() {
        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsingUriWhenLoadingTemplate()
     */
    public boolean isUsingUriWhenLoadingTemplate() {
        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsLoaderIncludeExtension#includeExtension(java.lang.String, java.lang.String, boolean, java.util.Map, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public String includeExtension(String target, String element, boolean editable, Map parameterMap, ServletRequest req, ServletResponse res) throws CmsException {
        // the Flex controller provides access to the interal OpenCms structures
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        // simple sanity check, controller should never be null here
        if (controller == null) {
            return target;
        }
        // special code to handle XmlTemplate based file includes        
        if (element != null) {
            if (!("body".equals(element) || "(default)".equals(element))) {
                // add template selector for multiple body XML files
                CmsJspTagInclude.addParameter(parameterMap, CmsXmlTemplate.C_FRAME_SELECTOR, element, true);
            }
        }        
        boolean isPageTarget;
        try {            
            // check if the target does exist in the OpenCms VFS
            CmsResource targetResource = controller.getCmsObject().readFileHeader(target);
            isPageTarget = ((CmsResourceTypePage.C_RESOURCE_TYPE_ID == targetResource.getType()));
        } catch (CmsException e) {
            controller.setThrowable(e, target);
            throw new CmsException("File not found: " + target, e);
        }
        String bodyAttribute = (String) controller.getCmsObject().getRequestContext().getAttribute(I_CmsConstants.C_XML_BODY_ELEMENT);               
        if (bodyAttribute == null) {
            // no body attribute is set: this is NOT a sub-element in a XML mastertemplate
            if (isPageTarget) {
                // add body file path to target 
                if (! target.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES)) {
                    target = I_CmsWpConstants.C_VFS_PATH_BODIES + target.substring(1);
                }
                // save target as "element replace" parameter for body loader
                CmsJspTagInclude.addParameter(parameterMap, CmsXmlTemplateLoader.C_ELEMENT_REPLACE, "body:" + target, true);  
                target = C_BODYLOADER_URI;                   
            }
        } else {
            // body attribute is set: this is a sub-element in a XML mastertemplate
            if (target.equals(controller.getCmsObject().getRequestContext().getUri())) {
                // target can be ignored, set body attribute as "element replace" parameter  
                CmsJspTagInclude.addParameter(parameterMap, CmsXmlTemplateLoader.C_ELEMENT_REPLACE, "body:" + bodyAttribute, true);
                // redirect target to body loader
                target = C_BODYLOADER_URI;                
            } else {
                if (isPageTarget) {
                    // add body file path to target 
                    if (isPageTarget && ! target.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES)) {
                        target = I_CmsWpConstants.C_VFS_PATH_BODIES + target.substring(1);
                    }           
                    // save target as "element replace" parameter  
                    CmsJspTagInclude.addParameter(parameterMap, CmsXmlTemplateLoader.C_ELEMENT_REPLACE, "body:" + target, true);  
                    target = C_BODYLOADER_URI;                     
                }
            }          
        }
        
        return target;
    }
    
    /**
     * Provides access to the current request through a CmsRequestContext, 
     * required for legacy backward compatibility.<p>
     * 
     * @param context the current request context
     * @return the request, of null if no request is available
     */
    public static I_CmsRequest getRequest(CmsRequestContext context) {
        return (I_CmsRequest)context.getAttribute(I_CmsRequest.C_CMS_REQUEST);
    }

    /**
     * Provides access to the current response through a CmsRequestContext, 
     * required for legacy backward compatibility.<p>
     * 
     * @param context the current request context
     * @return the response, of null if no request is available
     */
    public static I_CmsResponse getResponse(CmsRequestContext context) {
        return (I_CmsResponse)context.getAttribute(I_CmsResponse.C_CMS_RESPONSE);
    }
       
    /**
     * Provides access to the current session through a CmsRequestContext, 
     * required for legacy backward compatibility.<p>
     * 
     * @param context the current request context
     * @param value if true, try to create a session if none exist, if false, do not create a session
     * @return the response, of null if no request is available
     */
    public static I_CmsSession getSession(CmsRequestContext context, boolean value) {
        I_CmsRequest req = (I_CmsRequest)context.getAttribute(I_CmsRequest.C_CMS_REQUEST);
        HttpSession session = req.getOriginalRequest().getSession(value);
        if (session != null) {
            return (I_CmsSession)new CmsSession(session);
        } else {
            return null;
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {
        m_configuration.addProperty(paramName, paramValue);        
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {
        // return only a copy of the configuration
        ExtendedProperties copy = new ExtendedProperties();
        copy.combine(m_configuration);
        return copy; 
    }
}

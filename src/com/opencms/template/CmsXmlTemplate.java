
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlTemplate.java,v $
* Date   : $Date: 2001/01/24 09:42:40 $
* Version: $Revision: 1.46 $
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

package com.opencms.template;

import java.util.*;
import java.io.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.util.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.servlet.http.*;

/**
 * Template class for displaying the processed contents of hierachical XML template files
 * that can include other subtemplates.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.46 $ $Date: 2001/01/24 09:42:40 $
 */
public class CmsXmlTemplate implements I_CmsConstants,I_CmsXmlTemplate,I_CmsLogChannels {
    public static final String C_FRAME_SELECTOR = "cmsframe";
    
    /** name of the special body element */
    public final static String C_BODY_ELEMENT = "body";
    
    /** Boolean for additional debug output control */
    public final static boolean C_DEBUG = false;
    
    /** Error string to be inserted for corrupt subtemplates for guest user requests. */
    private final static String C_ERRORTEXT = "ERROR!";
    
    /**
     * Template cache for storing cacheable results of the subtemplates.
     */
    protected static com.opencms.launcher.I_CmsTemplateCache m_cache = null;
    
    /**
     * For debugging purposes only.
     * Counts the number of re-uses od the instance of this class.
     */
    private int counter = 0;
    
    /**
     * For debugging purposes only.
     * Increments the class variable <code>counter</code> and
     * prints out its new value..
     * <P>
     * May be called from the template file using
     * <code>&lt;METHOD name="counter"&gt;</code>.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return Actual value of <code>counter</code>.
     */
    public Integer counter(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        counter++;
        return new Integer(counter);
    }
    
    /**
     * Help method to print nice classnames in error messages
     * @return class name in [ClassName] format
     */
    protected String getClassName() {
        String name = getClass().getName();
        return "[" + name.substring(name.lastIndexOf(".") + 1) + "] ";
    }
    
    /**
     * Gets the content of a given template file and its subtemplates
     * with the given parameters. The default section in the template file
     * will be used.
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
     * @exception CmsException 
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        return getContent(cms, templateFile, elementName, parameters, null);
    }
    
    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] getting content of element " + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] selected template section is: " + ((templateSelector == null) ? "<default>" : templateSelector));
        }
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        if(templateSelector == null || "".equals(templateSelector)) {
            templateSelector = (String)parameters.get(C_FRAME_SELECTOR);
        }
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
    
    /** 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getFileUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        return cms.getRequestContext().getFileUri().getBytes();
    }
    
    /**
     * Gets the QueryString for CmsFrameTemplates.
     * <P>
     * This method can be called using <code>&lt;METHOD name="getCmsQueryString"&gt;</code>
     * in the template file.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getFrameQueryString(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        String query = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getQueryString();
        String frame = "", param = "";
        if(!tagcontent.equals("")) {
            if(!tagcontent.startsWith("&")) {
                if(tagcontent.indexOf(",") != -1) {
                    frame = tagcontent.substring(0, tagcontent.indexOf(","));
                    param = tagcontent.substring(tagcontent.indexOf(",") + 1);
                }
                else {
                    frame = tagcontent;
                }
            }
            else {
                param = tagcontent;
            }
        }
        StringBuffer encQuery = new StringBuffer();
        boolean notfirst = false;
        if(query != null) {
            
            // Fine. A lasturl parameter was found in session or parameter hashtable.
            
            // Check, if the URL parameters of the last url have to be encoded.
            int asteriskIdx = query.indexOf("?");
            if(asteriskIdx > -1 && (asteriskIdx < (query.length() - 1))) {
                
                // In fact, there are URL parameters
                encQuery.append(query.substring(0, asteriskIdx + 1));
                String queryString = query.substring(asteriskIdx + 1);
                StringTokenizer st = new StringTokenizer(queryString, "&");
                while(st.hasMoreTokens()) {
                    
                    // Loop through all URL parameters
                    String currToken = st.nextToken();
                    if(currToken != null && !"".equals(currToken)) {
                        
                        // Look for the "=" character to divide parameter name and value
                        int idx = currToken.indexOf("=");
                        if(notfirst) {
                            encQuery.append("&");
                        }
                        else {
                            notfirst = true;
                        }
                        if(idx > -1) {
                            
                            // A parameter name/value pair was found.
                            
                            // Encode the parameter value and write back!
                            String key = currToken.substring(0, idx);
                            String value = (idx < (currToken.length() - 1)) ? currToken.substring(idx + 1) : "";
                            encQuery.append(key);
                            encQuery.append("=");
                            encQuery.append(Encoder.escape(value));
                        }
                        else {
                            
                            // Something strange happened.
                            
                            // Maybe a parameter without "=" ?
                            
                            // Write back without encoding!
                            encQuery.append(currToken);
                        }
                    }
                }
                query = encQuery.toString();
            }
        }
        query = (query == null ? "" : query);
        if(!query.equals("")) {
            if(query.indexOf("cmsframe=") != -1) {
                int start = query.indexOf("cmsframe=");
                int end = query.indexOf("&", start);
                String cmsframe = "";
                if(end != -1) {
                    cmsframe = query.substring(start + 9, end);
                }
                else {
                    cmsframe = query.substring(start + 9);
                }
                if(!cmsframe.equals("plain")) {
                    if(!frame.equals("")) {
                        if(end != -1) {
                            query = query.substring(0, start + 9) + frame + query.substring(end);
                        }
                        else {
                            query = query.substring(0, start + 9) + frame;
                        }
                    }
                    else {
                        if(end != -1) {
                            query = query.substring(0, start) + query.substring(end + 1);
                        }
                        else {
                            query = query.substring(0, start);
                        }
                    }
                }
            }
            else {
                if(!tagcontent.equals("")) {
                    query = query + "&cmsframe=" + frame;
                }
            }
            if(!query.equals("")) {
                query = "?" + query;
            }
        }
        else {
            if(!frame.equals("")) {
                query = "?cmsframe=" + frame;
            }
        }
        if(!query.equals("")) {
            query = query + param;
        }
        else {
            query = "?" + param.substring(param.indexOf("&") + 1);
        }
        return query;
    }
    
    /**
     * Gets the target for a link.
     * <P>
     * This method can be called using <code>&lt;METHOD name="getCmsFrame"&gt;</code>
     * in the template file.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getFrameTarget(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        String target = "";
        String cmsframe = (String)((Hashtable)userObject).get("cmsframe");
        cmsframe = (cmsframe == null ? "" : cmsframe);
        if(cmsframe.equals("plain")) {
            target = "";
        }
        else {
            if(tagcontent.equals("")) {
                target = "target=_top";
            }
            else {
                target = "target=" + tagcontent;
            }
        }
        return target;
    }
    
    /**
     * Gets the key that should be used to cache the results of
     * <EM>this</EM> template class. 
     * <P>
     * Since our results may depend on the used template file, 
     * the parameters and the requested body document, we must
     * build a complex key using this three arguments.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return key that can be used for caching
     */
    public Object getKey(CmsObject cms, String templateFile, Hashtable parameters, String templateSelector) {
        
        //Vector v = new Vector();
        CmsRequestContext reqContext = cms.getRequestContext();
        
        //v.addElement(reqContext.currentProject().getName());
        
        //v.addElement(reqContext.getUri());
        
        //v.addElement(templateFile);
        
        //v.addElement(parameters);
        
        //v.addElement(templateSelector);
        
        //return v;
        String result = "" + reqContext.currentProject().getId() + ":" + reqContext.currentUser().getName() + reqContext.getUri() + templateFile;
        Enumeration keys = parameters.keys();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            result = result + key + parameters.get(key);
        }
        result = result + templateSelector;
        return result;
    }
    
    /**
     * Reads in the template file and starts the XML parser for the expected
     * content type.
     * <P>
     * Every extending class using not CmsXmlTemplateFile as content type,
     * but any derived type should override this method.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public CmsXmlTemplateFile getOwnTemplateFile(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        CmsXmlTemplateFile xmlTemplateDocument = new CmsXmlTemplateFile(cms, templateFile);
        return xmlTemplateDocument;
    }
    
    /** 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPathUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        String path = cms.getRequestContext().getUri();
        path = path.substring(0, path.lastIndexOf("/") + 1);
        path = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath() + path;
        return path.getBytes();
    }
    
    /**
     * Inserts the correct servlet path title into the template.
     * <P>
     * This method can be called using <code>&lt;METHOD name="getTitle"&gt;</code>
     * in the template file.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getQueryString(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        String query = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getQueryString();
        if(query != null && !"".equals(query)) {
            query = "?" + query;
        }
        return query;
    }
    
    /**
     * Get the IP address of the current request.
     * <P>
     * This method can be called using <code>&lt;METHOD name="getRequestIp"&gt;</code>
     * in the template file.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public String getRequestIp(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        return ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getRemoteAddr();
    }
    
    /**
     * Inserts the correct servlet path title into the template.
     * <P>
     * This method can be called using <code>&lt;METHOD name="getTitle"&gt;</code>
     * in the template file.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getServletPath(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        return ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath() + "/";
    }
    
    /**
     * Get the session id. If no session exists, a new one will be created.
     * <P>
     * This method can be called using <code>&lt;METHOD name="getSessionId"&gt;</code>
     * in the template file.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public String getSessionId(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        return ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true).getId();
    }
    
    /**
     * Inserts the correct stylesheet into the layout template.
     * <P>
     * This method can be called using <code>&lt;METHOD name="getStylesheet"&gt;</code>
     * in the template file.
     * <P>
     * When using this method follwing parameters should be defined
     * either in the page file or in the template file:
     * <ul>
     * <li><code>root.stylesheet-ie</code></li>
     * <li><code>root.stylesheet-ns</code></li>
     * </ul>
     * These parameters should contain the correct OpenCms path
     * for the Internet Explorer and Netscape Navigate 
     * specific stylesheets.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public String getStylesheet(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile)doc;
        
        // Get the styles from the parameter hashtable        
        String styleIE = null;
        String styleNS = null;
        if(templateFile.hasData("stylesheet-ie")) {
            styleIE = templateFile.getDataValue("stylesheet-ie");
        }
        else {
            if(templateFile.hasData("stylesheet")) {
                styleIE = templateFile.getDataValue("stylesheet");
            }
            else {
                styleIE = "";
            }
        }
        if(templateFile.hasData("stylesheet-ns")) {
            styleNS = templateFile.getDataValue("stylesheet-ns");
        }
        else {
            if(templateFile.hasData("stylesheet")) {
                styleNS = templateFile.getDataValue("stylesheet");
            }
            else {
                styleNS = "";
            }
        }
        HttpServletRequest orgReq = (HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();
        String servletPath = orgReq.getServletPath() + "/";
        
        // Get the user's browser
        String browser = orgReq.getHeader("user-agent");
        if(browser == null) {
            
            // the browser is unknown - return the ns-style
            return styleNS;
        }
        if(browser.indexOf("MSIE") > -1) {
            return servletPath + styleIE;
        }
        else {
            return servletPath + styleNS;
        }
    }
    
    /**
     * Find the corresponding template class to be loaded.
     * this should be defined in the template file of the parent
     * template and can be overwritten in the body file.
     * 
     * @param elementName Element name of this template in our parent template.
     * @param doc CmsXmlTemplateFile object of our template file including a subtemplate.
     * @param parameters Hashtable with all template class parameters.
     * @return Name of the class that should generate the output for the included template file.
     */
    protected String getTemplateClassName(String elementName, CmsXmlTemplateFile doc, Hashtable parameters) throws CmsException {
        String result = null;
        if(parameters.containsKey(elementName + "._CLASS_")) {
            result = (String)parameters.get(elementName + "._CLASS_");
        }
        else {
            if(doc.hasSubtemplateClass(elementName)) {
                result = doc.getSubtemplateClass(elementName);
            }
            else {
                
                // Fallback to "body" element
                if(parameters.containsKey("body._CLASS_")) {
                    result = (String)parameters.get("body._CLASS_");
                }
            }
        }
        return result;
    }
    
    /**
     * Find the corresponding template file to be loaded by the template class.
     * this should be defined in the template file of the parent
     * template and can be overwritten in the body file.
     * 
     * @param elementName Element name of this template in our parent template.
     * @param doc CmsXmlTemplateFile object of our template file including a subtemplate.
     * @param parameters Hashtable with all template class parameters.
     * @return Name of the template file that should be included.
     */
    protected String getTemplateFileName(String elementName, CmsXmlTemplateFile doc, Hashtable parameters) throws CmsException {
        String result = null;
        if(parameters.containsKey(elementName + "._TEMPLATE_")) {
            result = (String)parameters.get(elementName + "._TEMPLATE_");
        }
        else {
            if(doc.hasSubtemplateFilename(elementName)) {
                result = doc.getSubtemplateFilename(elementName);
            }
            else {
                
                // Fallback to "body" element
                if(parameters.containsKey("body._TEMPLATE_")) {
                    result = (String)parameters.get("body._TEMPLATE_");
                }
            }
        }
        
        //System.err.println("returning template file name for Element " + elementName + " in File " + doc + ": " + result);
        return result;
    }
    
    /**
     * Find the corresponding template selector to be activated.
     * This may be defined in the template file of the parent
     * template and can be overwritten in the body file.
     * 
     * @param elementName Element name of this template in our parent template.
     * @param doc CmsXmlTemplateFile object of our template file including a subtemplate.
     * @param parameters Hashtable with all template class parameters.
     * @return Name of the class that should generate the output for the included template file.
     */
    protected String getTemplateSelector(String elementName, CmsXmlTemplateFile doc, Hashtable parameters) throws CmsException {
        if(parameters.containsKey(elementName + "._TEMPLATESELECTOR_")) {
            return (String)parameters.get(elementName + "._TEMPLATESELECTOR_");
        }
        else {
            if(doc.hasSubtemplateSelector(elementName)) {
                return doc.getSubtemplateSelector(elementName);
            }
            else {
                return null;
            }
        }
    }
    
    /**
     * Inserts the correct document title into the template.
     * <P>
     * This method can be called using <code>&lt;METHOD name="getTitle"&gt;</code>
     * in the template file.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getTitle(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        String requestedUri = cms.getRequestContext().getUri();
        String title = cms.readProperty(requestedUri, C_PROPERTY_TITLE);
        if(title == null) {
            title = "";
        }
        return title;
    }
    
    /** 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        return (((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath() + cms.getRequestContext().getUri()).getBytes();
    }
    
    /**
     * Indicates if the results of this class are cacheable.
     * <P>
     * Checks if the templateCache is set and if all subtemplates
     * are cacheable.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        boolean cacheable;
        try {
            if(!cms.getRequestContext().currentProject().equals(cms.onlineProject())) {
                
                // never cache offline-resources
                return false;
            }
            else {
                if(templateSelector == null || "".equals(templateSelector)) {
                    templateSelector = (String)parameters.get(C_FRAME_SELECTOR);
                }
                cacheable = ((m_cache != null) && subtemplatesCacheable(cms, templateFile, elementName, parameters, templateSelector));
                if(C_DEBUG && A_OpenCms.isLogging()) {
                    String errorMessage = getClassName() + "Template class " + getClass().getName() + " with file " + templateFile + " is ";
                    if(cacheable) {
                        errorMessage = errorMessage + "cacheable.";
                    }
                    else {
                        errorMessage = errorMessage + "not cacheable.";
                    }
                    A_OpenCms.log(C_OPENCMS_DEBUG, errorMessage);
                }
            }
        }
        catch(CmsException exc) {
            
            // there was an exception => don't cache this res.
            cacheable = false;
        }
        return cacheable;
    }
    
    /**
     * Tests, if the template cache is setted.
     * @return <code>true</code> if setted, <code>false</code> otherwise.
     */
    public final boolean isTemplateCacheSet() {
        return m_cache != null;
    }
    
    /**
     * For debugging purposes only.
     * Prints out all parameters.
     * <P>
     * May be called from the template file using
     * <code>&lt;METHOD name="parameters"&gt;</code>.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return Debugging information about all parameters.
     */
    public String parameters(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) {
        Hashtable param = (Hashtable)userObject;
        Enumeration keys = param.keys();
        String s = "";
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            s = s + "<B>" + key + "</B>: " + param.get(key) + "<BR>";
        }
        s = s + "<B>" + tagcontent + "</B><BR>";
        return s;
    }
    
    /**
     * Set the instance of template cache that should be used to store 
     * cacheable results of the subtemplates.
     * If the template cache is not set, caching will be disabled.
     * @param c Template cache to be used.
     */
    public final void setTemplateCache(I_CmsTemplateCache c) {
        m_cache = c;
    }
    
    /**
     * Indicates if a previous cached result should be reloaded.
     * <P>
     * <em>not implemented.</em> Returns always <code>false</code>.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <code>false</code> 
     */
    public boolean shouldReload(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    
    /**
     * Starts the processing of the given template file by calling the
     * <code>getProcessedTemplateContent()</code> method of the content defintition
     * of the corresponding content type.
     * <P>
     * Any exceptions thrown while processing the template will be caught,
     * printed and and thrown again.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param xmlTemplateDocument XML parsed document of the content type "XML template file" or
     * any derived content type.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return Content of the template and all subtemplates.
     * @exception CmsException 
     */
    protected byte[] startProcessing(CmsObject cms, CmsXmlTemplateFile xmlTemplateDocument, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        String result = null;
        
        // Try to process the template file
        try {
            result = xmlTemplateDocument.getProcessedTemplateContent(this, parameters, templateSelector);
        }
        catch(Throwable e) {
            
            // There were errors while generating output for this template.
            
            // Clear HTML cache and then throw exception again
            xmlTemplateDocument.removeFromFileCache();
            if(isCacheable(cms, xmlTemplateDocument.getAbsoluteFilename(), elementName, parameters, templateSelector)) {
                m_cache.clearCache(getKey(cms, xmlTemplateDocument.getAbsoluteFilename(), parameters, templateSelector));
            }
            if(e instanceof CmsException) {
                throw (CmsException)e;
            }
            else {
                
                // under normal cirumstances, this should not happen.
                
                // any exception should be caught earlier and replaced by 
                
                // corresponding CmsExceptions.
                String errorMessage = "Exception while getting content for (sub)template " + elementName + ". " + e;
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
                }
                throw new CmsException(errorMessage);
            }
        }
        return result.getBytes();
    }
    
    /**
     * Checks if all subtemplates are cacheable.
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <code>true</code> if all subtemplates are cacheable, <code>false</code> otherwise.
     */
    public boolean subtemplatesCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        boolean cacheable = true;
        CmsXmlTemplateFile doc = null;
        Vector subtemplates = null;
        try {
            doc = this.getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
            doc.init(cms, templateFile);
            subtemplates = doc.getAllSubElements(templateSelector);
        }
        catch(Exception e) {
            System.err.println(e);
            return false;
        }
        int numSubtemplates = subtemplates.size();
        for(int i = 0;i < numSubtemplates;i++) {
            String elName = (String)subtemplates.elementAt(i);
            String className = null;
            String templateName = null;
            try {
                className = getTemplateClassName(elName, doc, parameters);
                templateName = getTemplateFileName(elName, doc, parameters);
            }
            catch(CmsException e) {
                
                // There was an error while reading the class name or template name 
                
                // from the subtemplate.
                
                // So we cannot determine the cacheability.
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, getClassName() + "Could not determine cacheability of subelement " + elName + " in template file " + doc.getFilename() + ". There were missing datablocks.");
                }
                return false;
            }
            try {
                I_CmsTemplate templClass = (I_CmsTemplate)CmsTemplateClassManager.getClassInstance(cms, className);
                cacheable = cacheable && templClass.isCacheable(cms, templateName, elName, parameters, null);
            }
            catch(Exception e) {
                System.err.println("E: " + e);
            }
        }
        return cacheable;
    }
    
    /**
     * Handles any occurence of an <code>&lt;ELEMENT&gt;</code> tag.
     * <P>
     * Every XML template class should use CmsXmlTemplateFile as
     * the interface to the XML file. Since CmsXmlTemplateFile is
     * an extension of A_CmsXmlContent by the additional tag
     * <code>&lt;ELEMENT&gt;</code> this user method ist mandatory.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object templateElement(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        
        // Our own template file that wants to include a subelement
        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile)doc;
        
        // Indicates, if this is a request of a guest user. Needed for error outputs.
        boolean isAnonymousUser = cms.anonymousUser().equals(cms.getRequestContext().currentUser());
        
        // First create a copy of the parameter hashtable
        Hashtable parameterHashtable = (Hashtable)((Hashtable)userObject).clone();
        
        // Name of the template class that should be used to handle the subtemplate
        String templateClass = getTemplateClassName(tagcontent, templateFile, parameterHashtable);
        
        // Name of the subtemplate file.
        String templateFilename = getTemplateFileName(tagcontent, templateFile, parameterHashtable);
        
        // Name of the subtemplate template selector
        String templateSelector = getTemplateSelector(tagcontent, templateFile, parameterHashtable);
        
        // Results returned by the subtemplate class
        byte[] result = null;
        
        // Temporary object for loading the subtemplate class
        Object loadedObject = null;
        
        // subtemplate class to be used for the include
        I_CmsTemplate subTemplate = null;
        
        // Key for the cache
        Object subTemplateKey = null;
        
        // try to load the subtemplate class
        try {
            loadedObject = CmsTemplateClassManager.getClassInstance(cms, templateClass);
        }
        catch(CmsException e) {
            
            // There was an error. First remove the template file from the file cache
            templateFile.removeFromFileCache();
            if(isAnonymousUser) {
                
                // The current user is the anonymous user
                return C_ERRORTEXT;
            }
            else {
                
                // The current user is a system user, so we throw the exception again.
                throw e;
            }
        }
        
        // Check if the loaded object is really an instance of an OpenCms template class
        if(!(loadedObject instanceof I_CmsTemplate)) {
            String errorMessage = "Class " + templateClass + " is no OpenCms template class.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlTemplate] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_XML_NO_TEMPLATE_CLASS);
        }
        subTemplate = (I_CmsTemplate)loadedObject;
        
        // Template class is now loaded. Next try to read the parameters        
        Vector parameterTags = null;
        parameterTags = templateFile.getParameterNames(tagcontent);
        if(parameterTags != null) {
            int numParameterTags = parameterTags.size();
            for(int i = 0;i < numParameterTags;i++) {
                String paramName = (String)parameterTags.elementAt(i);
                String paramValue = templateFile.getParameter(tagcontent, paramName);
                if(!parameterHashtable.containsKey(paramName)) {
                    parameterHashtable.put(tagcontent + "." + paramName, paramValue);
                }
            }
        }
        
        // all parameters are now parsed. Finally give the own subelement name        
        // as parameter        
        // TODO: replace _ELEMENT_ by a constant
        parameterHashtable.put("_ELEMENT_", tagcontent);
        
        // Try to get the result from the cache
        if(subTemplate.isCacheable(cms, templateFilename, tagcontent, parameterHashtable, null)) {
            subTemplateKey = subTemplate.getKey(cms, templateFilename, parameterHashtable, null);
            if(m_cache.has(subTemplateKey) && (!subTemplate.shouldReload(cms, templateFilename, tagcontent, parameterHashtable, null))) {
                result = m_cache.get(subTemplateKey);
            }
        }
        
        // OK. let's call the subtemplate
        if(result == null) {
            try {
                result = subTemplate.getContent(cms, templateFilename, tagcontent, parameterHashtable, templateSelector);
            }
            catch(Exception e) {
                
                // Oh, oh..
                
                // There were errors while getting the content of the subtemplate
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "Could not generate output for template file \"" + templateFilename + "\" included as element \"" + tagcontent + "\".");
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + e);
                }
                
                // The anonymous user gets an error String instead of an exception
                if(isAnonymousUser) {
                    return C_ERRORTEXT;
                }
                else {
                    if(e instanceof CmsException) {
                        throw (CmsException)e;
                    }
                    else {
                        e.printStackTrace();
                        throw new CmsException("Error while executing getContent for subtemplate \"" + tagcontent + "\". " + e);
                    }
                }
            }
            
            // Store the results in the template cache, if cacheable
            if(subTemplate.isCacheable(cms, templateFilename, tagcontent, parameterHashtable, null)) {
                
                // we don't need to re-get the caching-key here since it already exists
                m_cache.put(subTemplateKey, result);
            }
        }
        return result;
    }
    
    /**
     * Help method that handles any occuring error by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @exception CmsException
     */
    protected void throwException(String errorMessage) throws CmsException {
        throwException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
    }
    
    /**
     * Help method that handles any occuring error by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the given type.
     * @param errorMessage String with the error message to be printed.
     * @param type Type of the exception to be thrown.
     * @exception CmsException
     */
    protected void throwException(String errorMessage, int type) throws CmsException {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
        }
        throw new CmsException(errorMessage, type);
    }
    
    /**
     * Help method that handles any occuring error by writing
     * an error message to the OpenCms logfile and re-throwing a 
     * caught exception.
     * @param errorMessage String with the error message to be printed.
     * @param e Exception to be re-thrown.
     * @exception CmsException
     */
    protected void throwException(String errorMessage, Exception e) throws CmsException {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "Exception: " + e);
        }
        if(e instanceof CmsException) {
            throw (CmsException)e;
        }
        else {
            throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION, e);
        }
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsXmlLauncher.java,v $
 * Date   : $Date: 2000/04/28 13:47:07 $
 * Version: $Revision: 1.13 $
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
 * @version $Revision: 1.13 $ $Date: 2000/04/28 13:47:07 $
 */
public class CmsXmlLauncher extends A_CmsLauncher implements I_CmsLogChannels, I_CmsConstants { 	
        
    /**
 	 * Starts generating the output.
 	 * Calls the canonical root with the appropriate template class.
 	 * 
	 * @param cms A_CmsObject Object for accessing system resources
	 * @param file CmsFile Object with the selected resource to be shown
     * @param startTemplateClass Name of the template class to start with.
     * @exception CmsException
	 */	
    protected void launch(A_CmsObject cms, CmsFile file, String startTemplateClass) throws CmsException {
  
         
        // get the CmsRequest 
        I_CmsRequest req = cms.getRequestContext().getRequest();
        byte[] result = null;
        
        CmsXmlControlFile doc = null;
        try {
            //doc.init(cms, file);
            doc = new CmsXmlControlFile(cms, file);
        } catch(Exception e) {
            // there was an error while parsing the document
            handleException(cms, e, "There was an error while parsing XML file " + file.getAbsolutePath());
            return;
        }
        
        String templateClass = doc.getTemplateClass();
        if(templateClass == null || "".equals(templateClass)) {
            templateClass = startTemplateClass;
        }
        if(templateClass == null || "".equals(templateClass)) {
            templateClass = "com.opencms.template.CmsXmlTemplate";
        }
        String templateName = doc.getMasterTemplate();
        
        CmsFile masterTemplate = loadMasterTemplateFile(cms, templateName, doc);
                
        I_CmsTemplate tmpl = getTemplateClass(cms, templateClass);
        if(!(tmpl instanceof I_CmsXmlTemplate)) {
            String errorMessage = "Error in " + file.getAbsolutePath() + ": " + templateClass + " is not a XML template class.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_XML_WRONG_TEMPLATE_CLASS);
        }
        
        // Now look for parameters in the page file...                
        Hashtable newParameters = new Hashtable();
        
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
                if(paramValue!=null) {
                    newParameters.put(elementName + "." + paramName, paramValue);
                } else {
                    if(A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_INFO, getClassName() + "Empty parameter \"" + paramName + "\" found.");
                    }
                }
            }     
        }           
                        
        // Now check URL parameters      
        String datafor = req.getParameter("datafor");
        if(datafor == null) {
            datafor = "";
        } else if(! "".equals(datafor)) {
            datafor = datafor + ".";
        }
        
        Enumeration urlParameterNames = req.getParameterNames();
        while(urlParameterNames.hasMoreElements()) {
            
            String pname = (String)urlParameterNames.nextElement();
            String paramValue = req.getParameter(pname);

            if(paramValue!=null) {
                if((! "datafor".equals(pname)) && (! "_clearcache".equals(pname))) {
                    newParameters.put(datafor + pname, paramValue);
                }
            } else {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, getClassName() + "Empty URL parameter \"" + pname + "\" found.");
                }
            }
        }
       try {
            result = callCanonicalRoot(cms, (I_CmsTemplate)tmpl, masterTemplate, newParameters);
        } catch (CmsException e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlLauncher] There were exceptions while generating output for " + file.getAbsolutePath());
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlLauncher] Clearing template file cache for this file.");
            }
            doc.removeFromFileCache();
            throw e;
        }
           
        if(result != null) {
            writeBytesToResponse(cms, result);
        }
       
    }

    /**
     * Internal utility method for checking and loading a given template file.
     * @param cms A_CmsObject for accessing system resources.
     * @param templateName Name of the requestet template file.
     * @param doc CmsXmlControlFile object containig the parsed body file.
     * @return CmsFile object of the requested template file.
     * @exception CmsException
     */
    private CmsFile loadMasterTemplateFile(A_CmsObject cms, String templateName, com.opencms.template.CmsXmlControlFile doc) 
        throws CmsException {
        	
        CmsFile masterTemplate = null;
        try {
            masterTemplate = cms.readFile(templateName);
        } catch(Exception e) {
            handleException(cms, e, "Cannot load master template " + templateName + ". ");
            doc.removeFromFileCache();
        }
        return masterTemplate;
    }
                    
    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */    
    public int getLauncherId() {
	    return C_TYPE_XML;
    }    
}

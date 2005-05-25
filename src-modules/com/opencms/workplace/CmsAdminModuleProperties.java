/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsAdminModuleProperties.java,v $
* Date   : $Date: 2005/05/25 09:01:57 $
* Version: $Revision: 1.2 $
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

package com.opencms.workplace;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsDateUtil;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Template class for displaying the properties of a module.
 * Creation date: (07.09.00 11:30:26)
 * @author Hanjo Riege
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsAdminModuleProperties extends CmsWorkplaceDefault {
    
    /**
     * the different template views.
     */
    private final String C_DESCRIPTION = "description";
    private final String C_VIEW = "view";
    private final String C_MODULENAME = "module";
    private final String C_PARAMETER = "parameter";
    private final String C_CHANGE_PARAMETER = "changeparameter";
    private final String C_NEW_VALUE = "newvalue";
    private final String C_FROMERRORPAGE = "fromerrorpage";
    
    // sessionentry
    private final String C_SESSION_MODULENAME = "modulename_error";
    private final String C_SESSION_PARAMETER = "moduleparameter_error";
    
    /**
     *  Checks if the type of the value is correct and returns the converted value or null.
     *  @param type the type that the value should have..
     *  @param value the value to check.
     */
    private String checkType(String type, String value) {
        type = type.toLowerCase();
        try {
            if("string".equals(type)) {
                if((value != null) && (value.indexOf("\"") < 0)) {
                    return value;
                }
                else {
                    return null;
                }
            }
            else {
                if("int".equals(type) || "integer".equals(type)) {
                    value = "" + Integer.parseInt(value);
                    return value;
                }
                else {
                    if("float".equals(type)) {
                        value = "" + Float.valueOf(value);
                        return value;
                    }
                    else {
                        if("boolean".equals(type)) {
                            value = "" + Boolean.valueOf(value);
                            return value;
                        }
                        else {
                            if("long".equals(type)) {
                                value = "" + Long.valueOf(value);
                                return value;
                            }
                            else {
                                if("double".equals(type)) {
                                    value = "" + Double.valueOf(value);
                                    return value;
                                }
                                else {
                                    if("byte".equals(type)) {
                                        value = "" + Byte.valueOf(value);
                                        return value;
                                    }
                                    else {
                                        
                                        // the type dosen't exist
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception exc) {
            
            // the type of the value was wrong
            return null;
        }
    }
    
    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String view = (String)parameters.get(C_VIEW);
        String module = (String)parameters.get(C_MODULENAME);
        if((view != null) && (C_DESCRIPTION.equals(view))) {
            
            // set the values in the template "description"
            xmlTemplateDocument.setData("name", module);
            xmlTemplateDocument.setData("version", "" + OpenCms.getModuleManager().getModule(module).getVersion());
            xmlTemplateDocument.setData("descriptiontext", OpenCms.getModuleManager().getModule(module).getDescription());
            xmlTemplateDocument.setData("author", OpenCms.getModuleManager().getModule(module).getAuthorName());
            xmlTemplateDocument.setData("email", OpenCms.getModuleManager().getModule(module).getAuthorEmail());
            xmlTemplateDocument.setData("createdate", CmsDateUtil.getDateTimeShort(OpenCms.getModuleManager().getModule(module).getDateCreated()));
            xmlTemplateDocument.setData("uploadfrom", OpenCms.getModuleManager().getModule(module).getUserInstalled());
            xmlTemplateDocument.setData("uploaddate", CmsDateUtil.getDateTimeShort(OpenCms.getModuleManager().getModule(module).getDateInstalled()));
            xmlTemplateDocument.setData("view", "");
            xmlTemplateDocument.setData("documentation", "");
            String dependences = "";
            List moduleDependencies = OpenCms.getModuleManager().getModule(module).getDependencies();
            for (int i=0; i<moduleDependencies.size(); i++) {
                CmsModuleDependency dep = (CmsModuleDependency)moduleDependencies.get(i);
                dependences += dep.getName() + "  " + dep.getVersion() + " - " + "*" + "\n";                
            }
            xmlTemplateDocument.setData("dependences", dependences);
            String outputRep = "";
            xmlTemplateDocument.setData("repository", outputRep);
            
            // set the correct templateselector
            templateSelector = C_DESCRIPTION;
        }
        else {
            if((view != null) && (C_PARAMETER.equals(view))) {
                
                // set the values in the template "parameter"
                xmlTemplateDocument.setData("name", module);
                xmlTemplateDocument.setData("version", "" + OpenCms.getModuleManager().getModule(module).getVersion());
                Map moduleParameters = OpenCms.getModuleManager().getModule(module).getParameters();
                String allParameter = "";
                Iterator it = moduleParameters.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String)it.next();
                    xmlTemplateDocument.setData("paraname", key);
                    xmlTemplateDocument.setData("paravalue", (String)moduleParameters.get(key));
                    allParameter += xmlTemplateDocument.getProcessedDataValue("parameterentry");
                }
                xmlTemplateDocument.setData("allparameter", allParameter);
                
                // set the correct templateselector
                templateSelector = C_PARAMETER;
            }
            else {
                if((view != null) && (C_CHANGE_PARAMETER.equals(view))) {
                    
                    // set the values in the template "changeparameter"
                    String parameter = (String)parameters.get("selectpara");
                    String fromError = (String)parameters.get(C_FROMERRORPAGE);
                    if(fromError != null) {
                        module = (String)session.getValue(C_SESSION_MODULENAME);
                        parameter = (String)session.getValue(C_SESSION_PARAMETER);
                        session.removeValue(C_SESSION_MODULENAME);
                        session.removeValue(C_SESSION_PARAMETER);
                    }
                    Map moduleParameters = OpenCms.getModuleManager().getModule(module).getParameters();
                    xmlTemplateDocument.setData("name", module);
                    xmlTemplateDocument.setData("version", "" + OpenCms.getModuleManager().getModule(module).getVersion());
                    xmlTemplateDocument.setData("paraname", parameter);
                    xmlTemplateDocument.setData("paratext", "");
                    xmlTemplateDocument.setData("paratype", "String");
                    xmlTemplateDocument.setData("paravalue", (String)moduleParameters.get(parameter));
                    
                    // set the correct templateselector
                    templateSelector = C_CHANGE_PARAMETER;
                }
                else {
                    if((view != null) && (C_NEW_VALUE.equals(view))) {
                        
                        // Now we can finaly change the value.
                        String parameter = (String)parameters.get("parameter");
                        String value = (String)parameters.get("parawert");
                        xmlTemplateDocument.setData("name", module);
                        templateSelector = "done";
                        if(value != null) {
                            CmsModule oldModule = OpenCms.getModuleManager().getModule(module);
                            Map moduleParameters = new HashMap(oldModule.getParameters()); 
                            moduleParameters.put(parameter, value);
                            
                            CmsModule updatedModule = 
                                new CmsModule(
                                    oldModule.getName(),
                                    oldModule.getNiceName(),
                                    oldModule.getGroup(),
                                    oldModule.getActionClass(),
                                    oldModule.getDescription(),
                                    oldModule.getVersion(),
                                    oldModule.getAuthorName(),
                                    oldModule.getAuthorEmail(),
                                    oldModule.getDateCreated(),
                                    oldModule.getUserInstalled(),
                                    oldModule.getDateInstalled(),
                                    oldModule.getDependencies(),
                                    oldModule.getExportPoints(),
                                    oldModule.getResources(),
                                    moduleParameters);
                            
                            updatedModule.setResourceTypes(oldModule.getResourceTypes());
                            updatedModule.setExplorerTypes(oldModule.getExplorerTypes());
                            
                            OpenCms.getModuleManager().updateModule(cms, updatedModule);
                            
                        }
                        else {
                            
                            // wrong value
                            session.putValue(C_SESSION_MODULENAME, module);
                            session.putValue(C_SESSION_PARAMETER, parameter);
                            templateSelector = "error";
                            xmlTemplateDocument.setData("paraname", parameter);
                            xmlTemplateDocument.setData("DETAILS", "");
                        }
                    }
                }
            }
        }
        
        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
    
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
}

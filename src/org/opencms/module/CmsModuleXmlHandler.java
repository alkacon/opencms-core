/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/module/CmsModuleXmlHandler.java,v $
 * Date   : $Date: 2004/09/21 08:09:55 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.module;

import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.db.CmsExportPoint;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.I_CmsWpConstants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Adds the XML handler rules for import and export of a single module.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3.6
 */
public class CmsModuleXmlHandler {
    
    /** The "version" attribute. */
    protected static final String A_VERSION = "version";
    
    /** The node name for the authoremail node. */
    protected static final String N_AUTHOREMAIL = "authoremail";

    /** The node name for the authorname node. */
    protected static final String N_AUTHORNAME = "authorname";
    
    /** The node name for the class node. */
    protected static final String N_CLASS = "class";
    
    /** The node name for the datecreated node. */
    protected static final String N_DATECREATED = "datecreated";
    
    /** The node name for the date installed node. */
    protected static final String N_DATEINSTALLED = "dateinstalled";
    
    /** The node name for the dependencies node. */
    protected static final String N_DEPENDENCIES = "dependencies";    

    /** The node name for the dependency node. */
    protected static final String N_DEPENDENCY = "dependency"; 
    
    /** The node name for the description node. */
    protected static final String N_DESCRIPTION = "description";
    
    /** The node name for a module. */
    protected static final String N_MODULE = "module";
    
    /** The node name for the name node. */
    protected static final String N_NAME = "name";

    /** The node name for the nicename node. */
    protected static final String N_NICENAME = "nicename";
    
    /** The node name for the parameters node. */
    protected static final String N_PARAMETERS = "parameters";        
    
    /** The node name for the resources node. */
    protected static final String N_RESOURCES = "resources"; 
    
    /** The node name for the user installed node. */
    protected static final String N_USERINSTALLED = "userinstalled";
    
    /** The node name for the version node. */
    protected static final String N_VERSION = "version";
    
    /** The list of dependencies for a module. */
    private List m_dependencies;

    /** The list of export points for a module. */
    private List m_exportPoints;

    /** The generated module. */
    private CmsModule m_module;
    
    /** Indicates if the module was an old (5.0.x) style module. */
    private boolean m_oldModule;
    
    /** The module parameters. */
    private Map m_parameters;
    
    /** The list of resources for a module. */
    private List m_resources;
    
    /**
     * Public constructor, will be called by digester during import.<p> 
     */
    public CmsModuleXmlHandler() {
        
        m_exportPoints = new ArrayList();
        m_dependencies = new ArrayList();
        m_resources = new ArrayList();
        m_parameters = new HashMap();                             
    }

    /**
     * Adds the XML digester rules for a single module.<p>
     * 
     * @param digester the digester to add the rules to
     */
    public static void addXmlDigesterRules(Digester digester) {        
        
        // add class generation rule
        digester.addObjectCreate("*/" + N_MODULE, CmsModuleXmlHandler.class);
        digester.addSetNext("*/" + N_MODULE, "setModule");
        
        // add rules for base module information
        digester.addCallMethod("*/" + N_MODULE, "createdModule", 10);
        digester.addCallParam("*/" + N_MODULE + "/" + N_NAME, 0);
        digester.addCallParam("*/" + N_MODULE + "/" + N_NICENAME, 1);
        digester.addCallParam("*/" + N_MODULE + "/" + N_CLASS, 2);
        digester.addCallParam("*/" + N_MODULE + "/" + N_DESCRIPTION, 3);
        digester.addCallParam("*/" + N_MODULE + "/" + N_VERSION, 4);     
        digester.addCallParam("*/" + N_MODULE + "/" + N_AUTHORNAME, 5);   
        digester.addCallParam("*/" + N_MODULE + "/" + N_AUTHOREMAIL, 6);        
        digester.addCallParam("*/" + N_MODULE + "/" + N_DATECREATED, 7);        
        digester.addCallParam("*/" + N_MODULE + "/" + N_USERINSTALLED, 8);        
        digester.addCallParam("*/" + N_MODULE + "/" + N_DATEINSTALLED, 9);             
        
        // add rules for module dependencies
        digester.addCallMethod("*/" + N_MODULE + "/" + N_DEPENDENCIES + "/" + N_DEPENDENCY, "addDependency", 2);
        digester.addCallParam("*/" + N_MODULE + "/" + N_DEPENDENCIES + "/" + N_DEPENDENCY, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + N_MODULE + "/" + N_DEPENDENCIES + "/" + N_DEPENDENCY, 1, A_VERSION);

        // add rules for the module export points 
        digester.addCallMethod("*/" + N_MODULE + "/" + I_CmsXmlConfiguration.N_EXPORTPOINTS + "/" + I_CmsXmlConfiguration.N_EXPORTPOINT, "addExportPoint", 2);        
        digester.addCallParam("*/" + N_MODULE + "/" + I_CmsXmlConfiguration.N_EXPORTPOINTS + "/" + I_CmsXmlConfiguration.N_EXPORTPOINT, 0, I_CmsXmlConfiguration.A_URI);
        digester.addCallParam("*/" + N_MODULE + "/" + I_CmsXmlConfiguration.N_EXPORTPOINTS + "/" + I_CmsXmlConfiguration.N_EXPORTPOINT, 1, I_CmsXmlConfiguration.A_DESTINATION);      
        
        // add rules for the module resources 
        digester.addCallMethod("*/" + N_MODULE + "/" + N_RESOURCES + "/" + I_CmsXmlConfiguration.N_RESOURCE, "addResource", 1);        
        digester.addCallParam("*/" + N_MODULE +  "/" + N_RESOURCES + "/" + I_CmsXmlConfiguration.N_RESOURCE, 0, I_CmsXmlConfiguration.A_URI);
        
        // add rules for the module parameters
        digester.addCallMethod("*/" + N_MODULE + "/" + N_PARAMETERS + "/" + I_CmsXmlConfiguration.N_PARAM , "addParameter", 2);        
        digester.addCallParam("*/" + N_MODULE + "/" + N_PARAMETERS + "/" + I_CmsXmlConfiguration.N_PARAM , 0, I_CmsXmlConfiguration.A_NAME);        
        digester.addCallParam("*/" + N_MODULE + "/" + N_PARAMETERS + "/" + I_CmsXmlConfiguration.N_PARAM , 1);

        // finally add all rules for backward compatibility with OpenCms 5.0
        addXmlDigesterRulesForVersion5Modules(digester);
    }
    
    /**
     * Generates a detached XML element for a module.<p>
     * 
     * @param module the module to genterate the XML element for
     * @return the detached XML element for the module
     */
    public static Element generateXml(CmsModule module) {
        
        Document doc = DocumentHelper.createDocument();
        
        Element moduleElement = doc.addElement(N_MODULE);
        
        moduleElement.addElement(N_NAME).setText(module.getName());
        if (! module.getName().equals(module.getNiceName())) {
            moduleElement.addElement(N_NICENAME).addCDATA(module.getNiceName());
        } else {
            moduleElement.addElement(N_NICENAME);
        }
        if (CmsStringUtil.isNotEmpty(module.getActionClass())) {
            moduleElement.addElement(N_CLASS).setText(module.getActionClass());
        } else {
            moduleElement.addElement(N_CLASS);
        }
        if (CmsStringUtil.isNotEmpty(module.getDescription())) {
            moduleElement.addElement(N_DESCRIPTION).addCDATA(module.getDescription());
        } else {
            moduleElement.addElement(N_DESCRIPTION);
        }
        moduleElement.addElement(N_VERSION).setText(module.getVersion().toString());
        if (CmsStringUtil.isNotEmpty(module.getAuthorName())) {
            moduleElement.addElement(N_AUTHORNAME).addCDATA(module.getAuthorName());
        } else {
            moduleElement.addElement(N_AUTHORNAME);
        }
        if (CmsStringUtil.isNotEmpty(module.getAuthorEmail())) {
            moduleElement.addElement(N_AUTHOREMAIL).addCDATA(module.getAuthorEmail());
        } else {
            moduleElement.addElement(N_AUTHOREMAIL);
        }
        if (module.getDateCreated() != CmsModule.C_DEFAULT_DATE) {
            moduleElement.addElement(N_DATECREATED).setText(CmsDateUtil.getHeaderDate(module.getDateCreated()));
        } else {
            moduleElement.addElement(N_DATECREATED);
        }
        if (CmsStringUtil.isNotEmpty(module.getUserInstalled())) {
            moduleElement.addElement(N_USERINSTALLED).setText(module.getUserInstalled());
        } else {
            moduleElement.addElement(N_USERINSTALLED);
        }
        if (module.getDateInstalled() != CmsModule.C_DEFAULT_DATE) {
            moduleElement.addElement(N_DATEINSTALLED).setText(CmsDateUtil.getHeaderDate(module.getDateInstalled()));
        } else {
            moduleElement.addElement(N_DATEINSTALLED);
        }
        Element dependenciesElement = moduleElement.addElement(N_DEPENDENCIES);
        for (int i=0; i< module.getDependencies().size(); i++) {
            CmsModuleDependency dependency = (CmsModuleDependency)module.getDependencies().get(i);
            dependenciesElement
                .addElement(N_DEPENDENCY)
                .addAttribute(I_CmsXmlConfiguration.A_NAME, dependency.getName())
                .addAttribute(A_VERSION, dependency.getVersion().toString());
        }            
        Element exportpointsElement = moduleElement.addElement(I_CmsXmlConfiguration.N_EXPORTPOINTS);
        for (int i=0; i< module.getExportPoints().size(); i++) {
            CmsExportPoint point = (CmsExportPoint)module.getExportPoints().get(i);
            exportpointsElement
                .addElement(I_CmsXmlConfiguration.N_EXPORTPOINT)
                .addAttribute(I_CmsXmlConfiguration.A_URI, point.getUri())
                .addAttribute(I_CmsXmlConfiguration.A_DESTINATION, point.getConfiguredDestination());
        }                  
        Element resourcesElement = moduleElement.addElement(N_RESOURCES);
        for (int i=0; i< module.getResources().size(); i++) {
            String resource = (String)module.getResources().get(i);
            resourcesElement
                .addElement(I_CmsXmlConfiguration.N_RESOURCE)
                .addAttribute(I_CmsXmlConfiguration.A_URI, resource);
        }                       
        Element parametersElement = moduleElement.addElement(N_PARAMETERS);
        Map parameters = module.getParameters();
        if (parameters != null) {
            Iterator it = parameters.keySet().iterator();
            while (it.hasNext()) {
                String name = (String)it.next();
                String value = parameters.get(name).toString();
                Element paramNode = parametersElement.addElement(I_CmsXmlConfiguration.N_PARAM);
                paramNode.addAttribute(I_CmsXmlConfiguration.A_NAME, name);
                paramNode.addText(value);
            }
        }
        
        // return the modules node
        moduleElement.detach();
        return moduleElement;
    }
    
    /**
     * Generates a (hopefully) valid Java class name from an invalid class name.<p>
     * 
     * All invalid characters are replaced by an underscore "_".
     * This is for example used to make sure old (5.0) modules can still be imported,
     * by converting the name to a valid class name.<p>
     * 
     * @param className the class name to make valid
     * @return a valid Java class name from an invalid class name
     */
    public static String makeValidJavaClassName(String className) {

        StringBuffer result = new StringBuffer(className.length());
        int length = className.length();
        boolean nodot = true;
        for (int i = 0; i < length; i++) {
            char ch = className.charAt(i);
            if (nodot) {
                if (ch == '.') {
                    // ignore, remove
                } else if (Character.isJavaIdentifierStart(ch)) {
                    nodot = false;
                    result.append(ch);
                } else {
                    result.append('_');
                }
            } else {
                if (ch == '.') {
                    nodot = true;
                    result.append(ch);
                } else if (Character.isJavaIdentifierPart(ch)) {
                    nodot = false;
                    result.append(ch);
                } else {
                    result.append('_');
                }
            }
        }
        return result.toString();
    }
    
    /**
     * Adds the digester rules for OpenCms version 5 modules.<p>
     * 
     * @param digester the digester to add the rules to
     */
    private static void addXmlDigesterRulesForVersion5Modules(Digester digester) {
        
        // mark method
        digester.addCallMethod("*/" + N_MODULE + "/author", "setOldModule");
        
        // base module information
        digester.addCallParam("*/" + N_MODULE + "/author", 5);   
        digester.addCallParam("*/" + N_MODULE + "/email", 6);    
        digester.addCallParam("*/" + N_MODULE + "/creationdate", 7);   
        
        // dependencies
        digester.addCallParam("*/" + N_MODULE + "/dependencies/dependency/name", 0);
        digester.addCallParam("*/" + N_MODULE + "/dependencies/dependency/minversion", 1);
                
        // export points
        digester.addCallMethod("*/" + N_MODULE + "/exportpoint", "addExportPoint", 2);        
        digester.addCallParam("*/" + N_MODULE + "/exportpoint/source", 0);        
        digester.addCallParam("*/" + N_MODULE + "/exportpoint/destination", 1);  
        
        // parameters        
        digester.addCallMethod("*/" + N_MODULE + "/parameters/para" , "addParameter", 2);        
        digester.addCallParam("*/" + N_MODULE + "/parameters/para/name", 0);        
        digester.addCallParam("*/" + N_MODULE + "/parameters/para/value", 1);           
    }
    
    /**
     * Adds a module dependency to the current module.<p>
     * 
     * @param name the module name of the dependency
     * @param version the module version of the dependency
     */
    public void addDependency(String name, String version) {
        
        CmsModuleVersion moduleVersion = new CmsModuleVersion(version);
        
        CmsModuleDependency dependency = new CmsModuleDependency(name, moduleVersion);
        m_dependencies.add(dependency);
    
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Added module dependency to name: " + name + " version: " + version);
        }        
    }
    
    /**
     * Adds an export point to the module configuration.<p>
     * 
     * @param uri the export point uri
     * @param destination the export point destination
     */
    public void addExportPoint(String uri, String destination) {
        CmsExportPoint point = new CmsExportPoint(uri, destination);
        m_exportPoints.add(point);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Adding export point  : " + point.getUri() + " --> " + point.getDestinationPath());
        }            
    }
    
    /**
     * Adds a module parameter to the module configuration.<p>
     * 
     * @param key the parameter key
     * @param value the parameter value
     */
    public void addParameter(String key, String value) {
        m_parameters.put(key, value);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Added module parameter key: " + key + " value: " + value);
        }        
    }
    
    /**
     * Adds a resource to the list module resources.<p>
     * 
     * @param resource a resources uri in the OpenCms VFS
     */
    public void addResource(String resource) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Added module resource: " + resource);
        }           
        m_resources.add(resource);
    }
    
    /**
     * Created a new module from the provided parameters.<p>
     * 
     * @param name the name of this module, usually looks like a java package name
     * @param niceName the "nice" display name of this module
     * @param actionClass the (optional) module action class name
     * @param description the description of this module
     * @param version the version of this module
     * @param authorName the name of the author of this module
     * @param authorEmail the email of the module author
     * @param dateCreated the date this module was created by the author
     * @param userInstalled the name of the user who uploaded this module
     * @param dateInstalled the date this module was uploaded
     */
    public void createdModule(
        String name,
        String niceName,
        String actionClass,
        String description,
        String version,
        String authorName, 
        String authorEmail,
        String dateCreated,
        String userInstalled,
        String dateInstalled) {
        
        String moduleName;
        
        if (! CmsStringUtil.isValidJavaClassName(name)) {
            // ensure backward compatibility with old (5.0) module names
            OpenCms.getLog(this).error("Invalid module name imported: '" + name + "'");
            moduleName = makeValidJavaClassName(name);
            OpenCms.getLog(this).error("Corrected module name is: '" + moduleName + "'");                        
        } else {
            moduleName = name;
        }
        
        // parse the module version
        CmsModuleVersion moduleVersion = new CmsModuleVersion(version);
        
        // parse date created
        long moduleDateCreated = CmsModule.C_DEFAULT_DATE;
        if (dateCreated != null) {
            try {
                moduleDateCreated = CmsDateUtil.parseHeaderDate(dateCreated);
            } catch (ParseException e) {
                // noop
            }
        }
        
        // parse date installed
        long moduleDateInstalled = CmsModule.C_DEFAULT_DATE;
        if (dateInstalled != null) {
            try {
                moduleDateInstalled = CmsDateUtil.parseHeaderDate(dateInstalled);
            } catch (ParseException e1) {
                // noop
            }
        }
        
        if (m_oldModule) {
            // make sure module path is added to resources for "old" (5.0.x) modules
            String modulePath = I_CmsWpConstants.C_VFS_PATH_MODULES + name + "/";
            m_resources.add(modulePath);
        }
        
        // now create the module
        CmsModule module = 
            new CmsModule(
                moduleName, 
                niceName, 
                actionClass, 
                description, 
                moduleVersion, 
                authorName, 
                authorEmail, 
                moduleDateCreated,
                userInstalled,
                moduleDateInstalled, 
                m_dependencies,
                m_exportPoints, 
                m_resources,
                m_parameters);       
        
        // add the module to the list of configured modules
        m_module = module;
    }    
    
    /**
     * Returns the generated module.<p>
     * 
     * @return the generated module
     */
    public CmsModule getModule() {
        
        return m_module;
    }
    
    /** 
     * Sets the current imported module to an old (5.0.x) style module. 
     */
    public void setOldModule() {
        
        m_oldModule = true;
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Imported module is an old (5.0.x) style module.");
        }          
    }
}

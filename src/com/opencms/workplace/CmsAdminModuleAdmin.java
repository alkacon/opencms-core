/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleAdmin.java,v $
* Date   : $Date: 2005/02/18 14:23:15 $
* Version: $Revision: 1.50 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001 - 2005 The OpenCms Group
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
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleVersion;
import org.opencms.util.CmsDateUtil;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.CmsXmlTemplateFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace administration module administration.
 *
 * Creation date: (29.10.00 10:28:08)
 * @author Hanjo Riege
 * @author Thomas Weckert
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsAdminModuleAdmin extends CmsWorkplaceDefault {
    private final String C_FROM = "from";
    private final String C_VERSION = "version";
    private final String C_MODULENAME = "modulename";
    private final String C_DESCRIPTION = "description";
    private final String C_MAINTENANCE = "maintenance";
    private final String C_PUBLISHCLASS = "publishclass";
    private final String C_AUTHOR = "author";
    private final String C_EMAIL = "email";
    private final String C_DATE = "date";
    private final String C_DEPENDENCY = "dependencies";
    private final String C_ALLDEPS = "alldeps";
    private final String C_ONEDEP = "dependentry";
    private final String C_OPTIONENTRY = "optionentry";
    private final String C_NAME_PARAMETER = "module";

    /**
     * fills the data from the module in the hashtable.
     * Creation date: (30.10.00 14:22:22)
     * @param param java.lang.String
     * @return java.util.Hashtable
     */
    private void fillHashtable(CmsObject cms, Hashtable table, String module) {
        table.put(C_MODULE_PACKETNAME, module);
        table.put(C_VERSION, getStringValue(OpenCms.getModuleManager().getModule(module).getVersion().toString()));
        table.put(C_MODULENAME, getStringValue(OpenCms.getModuleManager().getModule(module).getNiceName()));
        table.put(C_DESCRIPTION, getStringValue(OpenCms.getModuleManager().getModule(module).getDescription()));
        table.put(C_MAINTENANCE, getStringValue(OpenCms.getModuleManager().getModule(module).getActionClass()));
        table.put(C_PUBLISHCLASS, getStringValue(OpenCms.getModuleManager().getModule(module).getActionClass()));
        table.put(C_AUTHOR, getStringValue(OpenCms.getModuleManager().getModule(module).getAuthorName()));
        table.put(C_EMAIL, getStringValue(OpenCms.getModuleManager().getModule(module).getAuthorEmail()));
        table.put(C_DATE, getStringValue(CmsDateUtil.getDateShort(OpenCms.getModuleManager().getModule(module).getDateCreated())));

        // get the dependencies
        Vector stringDeps = new Vector();
        List moduleDependencies = OpenCms.getModuleManager().getModule(module).getDependencies();
        for (int i=0; i<moduleDependencies.size(); i++) {
            CmsModuleDependency dep = (CmsModuleDependency)moduleDependencies.get(i);
            stringDeps.addElement(dep.getName() + "  Version:" + dep.getVersion() + " - *");
        }
        table.put(C_DEPENDENCY, stringDeps);

        // handle the properties
        Vector paraNames = new Vector();
        Vector paraDescr = new Vector();
        Vector paraTyp = new Vector();
        Vector paraVal = new Vector();
        Map params = OpenCms.getModuleManager().getModule(module).getParameters();
        Iterator it = params.keySet().iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            String value = (String)params.get(key);
            paraNames.addElement(key);
            paraDescr.addElement("");
            paraVal.addElement(getStringValue(value));
            paraTyp.addElement("");
        }
        table.put(C_SESSION_MODULE_ADMIN_PROP_NAMES, paraNames);
        table.put(C_SESSION_MODULE_ADMIN_PROP_DESCR, paraDescr);
        table.put(C_SESSION_MODULE_ADMIN_PROP_TYP, paraTyp);
        table.put(C_SESSION_MODULE_ADMIN_PROP_VAL, paraVal);        
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
        CmsXmlTemplateFile templateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String stepTo = "";
        String from = (String)parameters.get(C_FROM);
        String packetName = (String)parameters.get(C_NAME_PARAMETER);
        Hashtable sessionData = new Hashtable();
        if((from == null) || "".equals(from)) {

            // first call; clear session
            session.removeValue(C_SESSION_MODULE_ADMIN_DATA);
            fillHashtable(cms, sessionData, packetName);
            session.putValue(C_SESSION_MODULE_ADMIN_DATA, sessionData);
            stepTo = "firstpage";
        }else {
            sessionData = (Hashtable)session.getValue(C_SESSION_MODULE_ADMIN_DATA);
            if("first".equals(from)) {

                //  put the data in the session
                sessionData.put(C_VERSION, getStringValue((String)parameters.get(C_VERSION)));
                sessionData.put(C_MODULENAME, getStringValue((String)parameters.get(C_MODULENAME)));
                sessionData.put(C_DESCRIPTION, getStringValue((String)parameters.get(C_DESCRIPTION)));
                sessionData.put(C_MAINTENANCE, getStringValue((String)parameters.get(C_MAINTENANCE)));
                sessionData.put(C_PUBLISHCLASS, getStringValue((String)parameters.get(C_PUBLISHCLASS)));
                sessionData.put(C_AUTHOR, getStringValue((String)parameters.get(C_AUTHOR)));
                sessionData.put(C_EMAIL, getStringValue((String)parameters.get(C_EMAIL)));
                sessionData.put(C_DATE, getStringValue((String)parameters.get(C_DATE)));
    
                session.putValue(C_SESSION_MODULE_ADMIN_DATA, sessionData);
                stepTo = "deps";
            }
            if("deps".equals(from)) {

                // put the data in the session
                String allData = (String)parameters.get(C_ALLDEPS);
                Vector allDeps = new Vector();
                allDeps = parseAllDeps(getStringValue(allData));
                sessionData.put(C_DEPENDENCY, allDeps);
                session.putValue(C_SESSION_MODULE_ADMIN_DATA, sessionData);

                // decide if we are going back or forward => set stepTo
                String back = (String)parameters.get("back");
                if((back == null) || ("".equals(back))) {
                    stepTo = "props";
                }
                else {
                    stepTo = "firstpage";
                }
            }
            if("props".equals(from)) {
                stepTo = "deps";
            }
            if("edit".equals(from)) {
                stepTo = "props";
            }
            if("propsready".equals(from)) {

                // ready; save changes in registry
                updateTheModule(cms, sessionData, packetName);
                session.removeValue(C_SESSION_MODULE_ADMIN_DATA);
                templateSelector = "done";
            }
        }
        if("firstpage".equals(stepTo)) {

            // show the first page
            templateDocument.setData(C_MODULE_PACKETNAME, (String)sessionData.get(C_MODULE_PACKETNAME));
            templateDocument.setData(C_VERSION, (String)sessionData.get(C_VERSION));
            templateDocument.setData(C_MODULENAME, (String)sessionData.get(C_MODULENAME));
            templateDocument.setData(C_DESCRIPTION, (String)sessionData.get(C_DESCRIPTION));
            templateDocument.setData(C_MAINTENANCE, (String)sessionData.get(C_MAINTENANCE));
            templateDocument.setData(C_PUBLISHCLASS, (String)sessionData.get(C_PUBLISHCLASS));
            templateDocument.setData(C_AUTHOR, (String)sessionData.get(C_AUTHOR));
            templateDocument.setData(C_EMAIL, (String)sessionData.get(C_EMAIL));
            templateDocument.setData(C_DATE, (String)sessionData.get(C_DATE));
            
            templateDocument.setData("moduletype", "checked");
            templateSelector = "";
        }
        if("deps".equals(stepTo)) {

            // show the dependencies
            templateDocument.setData(C_MODULE_PACKETNAME, (String)sessionData.get(C_MODULE_PACKETNAME));
            Vector deps = (Vector)sessionData.get(C_DEPENDENCY);
            String entrys = "";
            for(int i = 0;i < deps.size();i++) {
                templateDocument.setData(C_ONEDEP, (String)deps.elementAt(i));
                entrys += templateDocument.getProcessedDataValue(C_OPTIONENTRY);
            }
            templateDocument.setData(C_ALLDEPS, entrys);
            templateSelector = "dependencies";
        }
        if("props".equals(stepTo)) {

            // prepare the properties page
            templateSelector = "properties";
        }

        // Now load the template file and start the processing
        return startProcessing(cms, templateDocument, elementName, parameters, templateSelector);
    }

    /**
     * returns the String or "" if it is null.
     * Creation date: (29.10.00 16:05:38)
     * @return java.lang.String
     * @param param java.lang.String
     */
    private String getStringValue(String param) {
        if(param == null) {
            return "";
        }
        return param;
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

    /** Parse the string which holds all dependencies
     *
     * @param resources containts the full pathnames of all the resources, separated by semicolons
     * @return A vector with the same resources
     */
    private Vector parseAllDeps(String resources) {
        Vector ret = new Vector();
        if(resources != null) {
            StringTokenizer resTokenizer = new StringTokenizer(resources, ";");
            while(resTokenizer.hasMoreElements()) {
                String path = (String)resTokenizer.nextElement();
                ret.addElement(path);
            }
        }
        return ret;
    }

    /**
     * Insert the method's description here.
     * Creation date: (03.11.00 08:23:13)
     * @param param org.opencms.file.CmsObject
     * @param folder java.lang.String
     * @param newFolder java.lang.String
     */
    private void tryToCreateFolder(CmsObject cms, String folder, String newFolder) {
        try {
            cms.createResource(folder + newFolder, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
        }catch(Exception e) {
        }
    }

    /**
     * fills the data from the hashtable in the module.
     * Creation date: (30.10.00 14:22:22)
     * @param param java.lang.String
     * @return java.util.Hashtable
     */
    private void updateTheModule(CmsObject cms, Hashtable table, String module) {
        SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy");
        String name = (String)table.get(C_MODULE_PACKETNAME);

        try {
        
            // set the module version
            CmsModuleVersion version = new CmsModuleVersion((String)table.get(C_VERSION));
        
            // the easy values
            String niceName = (String)table.get(C_MODULENAME);
            String description = (String)table.get(C_DESCRIPTION);
            String moduleClass = (String)table.get(C_PUBLISHCLASS);
            String moduleAuthor = (String)table.get(C_AUTHOR);
            String moduleEmail = (String)table.get(C_EMAIL);

            // set the date, if the value is not correct set the current date
            String date = (String)table.get(C_DATE);
            long moduleDateCreated = 0;
            try {
                moduleDateCreated = dateFormat.parse(date).getTime();
            }catch(Exception exc) {
                moduleDateCreated = (new Date()).getTime();
            }

            // now the dependnecies
            Vector stringDeps = (Vector)table.get(C_DEPENDENCY);
            List dependencies = new ArrayList();
            
            for(int i = 0;i < stringDeps.size();i++) {
                String complString = (String)stringDeps.elementAt(i);
                complString = complString.substring(0, complString.lastIndexOf("-") - 1);
                String min = complString.substring(complString.lastIndexOf(":") + 1);
                String depName = (complString.substring(0, complString.lastIndexOf("Version:") - 1)).trim();
                
                dependencies.add(new CmsModuleDependency(depName, new CmsModuleVersion(String.valueOf(min))));
            }

            // last not least: the properties
            Vector paraNames = (Vector)table.get(C_SESSION_MODULE_ADMIN_PROP_NAMES);
            Vector paraVal = (Vector)table.get(C_SESSION_MODULE_ADMIN_PROP_VAL);
            
            Map parameters = new HashMap();
            for (int i=0; i<paraNames.size(); i++) {
                String key = (String)paraNames.get(i);
                parameters.put(key, paraVal.get(i));
            }
            
            CmsModule currentModule = OpenCms.getModuleManager().getModule(name);

            CmsModule updatedModule = 
                new CmsModule(
                    name,
                    niceName,
                    moduleClass,
                    description,
                    version,
                    moduleAuthor,
                    moduleEmail,
                    moduleDateCreated,
                    currentModule.getUserInstalled(),
                    currentModule.getDateInstalled(),
                    dependencies,
                    currentModule.getExportPoints(),
                    currentModule.getResources(),
                    parameters);
            
                updatedModule.setResourceTypes(currentModule.getResourceTypes());
                updatedModule.setExplorerTypes(currentModule.getExplorerTypes());
            
            OpenCms.getModuleManager().updateModule(cms, updatedModule);

        } catch (CmsException e) {
             if(OpenCms.getLog(this).isErrorEnabled()) {
                 OpenCms.getLog(this).error("Error in module administration", e);
             }
        }
    }
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleCreate.java,v $
* Date   : $Date: 2004/07/18 16:27:12 $
* Version: $Revision: 1.50 $
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

import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleVersion;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.CmsXmlTemplateFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace administration module create.
 *
 * Creation date: (27.10.00 10:28:08)
 * @author Hanjo Riege
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsAdminModuleCreate extends CmsWorkplaceDefault {
    private final static String C_PACKETNAME = "packetname";
    private final static String C_STEP = "step";
    private final static String C_VERSION = "version";
    private final static String C_MODULENAME = "modulename";
    private final static String C_DESCRIPTION = "description";
    private final static String C_VIEW = "view";
    private final static String C_ADMINPOINT = "adminpoint";
    private final static String C_MAINTENANCE = "maintenance";
    private final static String C_PUBLISHCLASS = "publishclass";    
    private final static String C_AUTHOR = "author";
    private final static String C_EMAIL = "email";
    private final static String C_DATE = "date";
    private final static String C_MODULE_SESSION_DATA = "module_create_data";
    private final static String C_MODULE_TYPE = "moduletype";
    private final static String C_EXPORTCLASSES = "exportclasses";
    private final static String C_EXPORTLIB = "exportlib";
        
    /**
     *  Checks if the name is correct.
     *  @param name the name to check..
     */
    private boolean checkName(String name) {
        if(name == null || name.length() == 0 || name.trim().length() == 0) {
            return false;
        }
        for(int i = 0;i < name.length();i++) {
            char c = name.charAt(i);
            if(((c < 'a') || (c > 'z')) && ((c < '0') || (c > '9')) && ((c < 'A') || (c > 'Z')) && (c != '-') && (c != '.') && (c != '_') && (c != '~')) {
                return false;
            }
        }
        return true;
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
        String step = (String)parameters.get(C_STEP);
        SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy");
        if((step == null) || "".equals(step)) {
            templateDocument.setData(C_PACKETNAME, "");
            templateDocument.setData(C_VERSION, "1");
            templateDocument.setData(C_MODULENAME, "");
            templateDocument.setData(C_DESCRIPTION, "");
            templateDocument.setData(C_VIEW, "");
            templateDocument.setData(C_ADMINPOINT, "");
            templateDocument.setData(C_MAINTENANCE, "");
            templateDocument.setData(C_PUBLISHCLASS, "");            
            templateDocument.setData(C_AUTHOR, "");
            templateDocument.setData(C_EMAIL, "");
            templateDocument.setData(C_MODULE_TYPE, "checked");
            templateDocument.setData(C_EXPORTCLASSES, "checked");
            templateDocument.setData(C_EXPORTLIB, "checked");                        
            //  set the current date:
            templateDocument.setData(C_DATE, dateFormat.format(new Date()));
        }else {
            if("OK".equals(step) || "Ok".equals(step)) {
                String modulename = (String)parameters.get(C_PACKETNAME);
                String nicename = (String)parameters.get(C_MODULENAME);
                String version = (String)parameters.get(C_VERSION);
                String description = (String)parameters.get(C_DESCRIPTION);
                String maintenance = (String)parameters.get(C_MAINTENANCE);
                String publishclass = (String)parameters.get(C_PUBLISHCLASS);
                String author = (String)parameters.get(C_AUTHOR);
                String email = (String)parameters.get(C_EMAIL);
                String createDate = (String)parameters.get(C_DATE);
                String moduleType = (String)parameters.get(C_MODULE_TYPE);
                String exportClasses = (String)parameters.get(C_EXPORTCLASSES);
                String exportLib = (String)parameters.get(C_EXPORTLIB);

                boolean mustExportClasses = ((exportClasses != null) && exportClasses.equals("checked"));
                boolean mustExportLib = ((exportLib != null) && exportLib.equals("checked"));
                
                boolean moduleExists = OpenCms.getModuleManager().hasModule(modulename); 

                CmsModuleVersion moduleVersion = new CmsModuleVersion(version);
                
                if((!checkName(modulename)) || (version == null) || ("".equals(version)) || moduleExists) {
                    Hashtable sessionData = new Hashtable();
                    sessionData.put(C_MODULENAME, getStringValue(nicename));
                    sessionData.put(C_VERSION, getStringValue(version));
                    sessionData.put(C_DESCRIPTION, getStringValue(description));
                    sessionData.put(C_VIEW, "");
                    sessionData.put(C_ADMINPOINT, "");
                    sessionData.put(C_MAINTENANCE, getStringValue(maintenance));
                    sessionData.put(C_PUBLISHCLASS, getStringValue(publishclass));                    
                    sessionData.put(C_AUTHOR, getStringValue(author));
                    sessionData.put(C_EMAIL, getStringValue(email));
                    sessionData.put(C_DATE, getStringValue(createDate));
                    sessionData.put(C_MODULE_TYPE, getStringValue(moduleType));
                    session.putValue(C_MODULE_SESSION_DATA, sessionData);
                    if(moduleExists) {
                        templateSelector = "errorexists";
                    }else {
                        templateSelector = "errornoname";
                    }
                }else {
                    tryToCreateFolder(cms, C_VFS_PATH_SYSTEM, "modules");
                    // create the module (first test if we are in a project including /system/
                    try {
                        cms.createResource(C_VFS_PATH_MODULES + modulename, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
                    }catch(CmsException e) {
                        if(e.getType() != CmsException.C_FILE_EXISTS) {
                            // couldn't create Module
                            templateDocument.setData("details", CmsException.getStackTraceAsString(e));
                            return startProcessing(cms, templateDocument, elementName, parameters, "errorProject");
                        }else {
                            try {
                                cms.readFolder(C_VFS_PATH_MODULES + modulename + "/");
                            }catch(CmsException ex) {
                                // folder exist but is deleted
                                templateDocument.setData("details", "Sorry, you have to publish this Project and create a new one.\n" + CmsException.getStackTraceAsString(e));
                                return startProcessing(cms, templateDocument, elementName, parameters, "errorProject");
                            }
                        }
                    }
                    long createDateLong = 0;
                    try {
                        createDateLong = dateFormat.parse(createDate).getTime();
                    }catch(Exception exc) {
                        createDateLong = (new Date()).getTime();
                    }

                    String modulePath = C_VFS_PATH_MODULES + modulename + "/";
                    List moduleResources = new ArrayList();
                    moduleResources.add(modulePath);                    
                    
                    List moduleExportPoints = new ArrayList();
                    if (mustExportClasses) {
                        
                        CmsExportPoint exportPoint =
                            new CmsExportPoint(
                                I_CmsWpConstants.C_VFS_PATH_MODULES + modulename + "/classes/",
                                "WEB-INF/classes/");

                        moduleExportPoints.add(exportPoint);
                        
                        // create the class folder, will get exportet to the "real" file system
                        tryToCreateFolder(cms, modulePath, "classes");
                        // create all package folders beneth class folder
                        Vector cFolders = new Vector();
                        String workString = modulename;
                        while(workString.lastIndexOf('.') > -1) {
                            cFolders.addElement(workString.substring(workString.lastIndexOf('.') + 1));
                            workString = workString.substring(0, workString.lastIndexOf('.'));
                        }
                        tryToCreateFolder(cms, modulePath+"classes/", workString);
                        workString = modulePath + "classes/" + workString + "/";
                        for(int i = cFolders.size() - 1;i >= 0;i--) {
                            tryToCreateFolder(cms, workString, (String)cFolders.elementAt(i));
                            workString = workString + (String)cFolders.elementAt(i) + "/";
                        }                    
                    }
                    
                    if (mustExportLib) {

                        CmsExportPoint exportPoint =
                            new CmsExportPoint(
                                I_CmsWpConstants.C_VFS_PATH_MODULES + modulename + "/lib/",
                                "WEB-INF/lib/");

                        moduleExportPoints.add(exportPoint);

                        // create the lib folder, will get exportet to the "real" file system
                        tryToCreateFolder(cms, modulePath, "lib");
                    }                    
                    
                    List moduleDepedencies = new ArrayList();
                    Map moduleParameters = new HashMap();

                    // as default don't export any module data                
                    cms.writeProperty(modulePath, C_PROPERTY_EXPORT, "false");                    
                    
                    // create the templates folder
                    tryToCreateFolder(cms, modulePath, I_CmsWpConstants.C_VFS_DIR_TEMPLATES);
                    // create the "default_bodies" folder 
                    tryToCreateFolder(cms, modulePath, I_CmsWpConstants.C_VFS_DIR_DEFAULTBODIES);
                        
                    CmsModule updatedModule = 
                        new CmsModule(
                            modulename,
                            nicename, 
                            publishclass,
                            description,
                            moduleVersion,
                            author,
                            email,
                            createDateLong,
                            null,
                            0L,
                            moduleDepedencies,
                            moduleExportPoints,
                            moduleResources,
                            moduleParameters);

                    OpenCms.getModuleManager().addModule(cms, updatedModule);
                    
                    try {
                        CmsXmlTemplateLoader.getResponse(cms.getRequestContext()).sendCmsRedirect(getConfigFile(cms).getWorkplaceAdministrationPath() + "module/index.html");
                    }catch(Exception e) {
                        throw new CmsException("Redirect fails :system/workplace/administration/module/index.html", CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                    return null;
                }
            }else {
                if("fromerrorpage".equals(step)) {
                    Hashtable sessionData = (Hashtable)session.getValue(C_MODULE_SESSION_DATA);
                    session.removeValue(C_MODULE_SESSION_DATA);
                    templateDocument.setData(C_PACKETNAME, "");
                    templateDocument.setData(C_VERSION, (String)sessionData.get(C_VERSION));
                    templateDocument.setData(C_MODULENAME, (String)sessionData.get(C_MODULENAME));
                    templateDocument.setData(C_DESCRIPTION, (String)sessionData.get(C_DESCRIPTION));
                    templateDocument.setData(C_VIEW, "");
                    templateDocument.setData(C_ADMINPOINT, "");
                    templateDocument.setData(C_MAINTENANCE, (String)sessionData.get(C_MAINTENANCE));
                    templateDocument.setData(C_PUBLISHCLASS, (String)sessionData.get(C_PUBLISHCLASS));                    
                    templateDocument.setData(C_AUTHOR, (String)sessionData.get(C_AUTHOR));
                    templateDocument.setData(C_EMAIL, (String)sessionData.get(C_EMAIL));
                    templateDocument.setData(C_DATE, (String)sessionData.get(C_DATE));
                    templateDocument.setData(C_MODULE_TYPE, (String)sessionData.get(C_MODULE_TYPE));
                    templateSelector = "";
                }
            }
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
}


/*
* File   : $File$
* Date   : $Date: 2001/02/01 09:51:54 $
* Version: $Revision: 1.11 $
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

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.text.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace administration module create.
 *
 * Creation date: (27.10.00 10:28:08)
 * @author: Hanjo Riege
 */
public class CmsAdminModuleCreate extends CmsWorkplaceDefault implements I_CmsConstants {
    private final String C_PACKETNAME = "packetname";
    private final String C_STEP = "step";
    private final String C_VERSION = "version";
    private final String C_MODULENAME = "modulename";
    private final String C_DESCRIPTION = "description";
    private final String C_VIEW = "view";
    private final String C_ADMINPOINT = "adminpoint";
    private final String C_MAINTENANCE = "maintenance";
    private final String C_AUTHOR = "author";
    private final String C_EMAIL = "email";
    private final String C_DATE = "date";
    private final String C_SESSION_DATA = "module_create_data";
    
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
     * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector == null) ? "<default>" : templateSelector));
        }
        CmsXmlTemplateFile templateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        //CmsRequestContext reqCont = cms.getRequestContext();
        I_CmsRegistry reg = cms.getRegistry();
        I_CmsSession session = cms.getRequestContext().getSession(true);
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
            templateDocument.setData(C_AUTHOR, "");
            templateDocument.setData(C_EMAIL, "");
            
            //  set the current date:
            templateDocument.setData(C_DATE, dateFormat.format(new Date()));
        }
        else {
            if("OK".equals(step) || "Ok".equals(step)) {
                String packetname = (String)parameters.get(C_PACKETNAME);
                String modulename = (String)parameters.get(C_MODULENAME);
                String version = (String)parameters.get(C_VERSION);
                String description = (String)parameters.get(C_DESCRIPTION);
                String view = (String)parameters.get(C_VIEW);
                String adminpoint = (String)parameters.get(C_ADMINPOINT);
                String maintenance = (String)parameters.get(C_MAINTENANCE);
                String author = (String)parameters.get(C_AUTHOR);
                String email = (String)parameters.get(C_EMAIL);
                String createDate = (String)parameters.get(C_DATE);
                boolean moduleExists = reg.moduleExists(packetname);
                int v = -1;
                try {
                    v = Integer.parseInt(version);
                }
                catch(Exception e) {
                    
                }
                if((!checkName(packetname)) || (version == null) || ("".equals(version)) || moduleExists || (v < 0)) {
                    Hashtable sessionData = new Hashtable();
                    sessionData.put(C_MODULENAME, getStringValue(modulename));
                    sessionData.put(C_VERSION, getStringValue(version));
                    sessionData.put(C_DESCRIPTION, getStringValue(description));
                    sessionData.put(C_VIEW, getStringValue(view));
                    sessionData.put(C_ADMINPOINT, getStringValue(adminpoint));
                    sessionData.put(C_MAINTENANCE, getStringValue(maintenance));
                    sessionData.put(C_AUTHOR, getStringValue(author));
                    sessionData.put(C_EMAIL, getStringValue(email));
                    sessionData.put(C_DATE, getStringValue(createDate));
                    session.putValue(C_SESSION_DATA, sessionData);
                    if(moduleExists) {
                        templateSelector = "errorexists";
                    }
                    else {
                        templateSelector = "errornoname";
                    }
                }
                else {
                    tryToCreateFolder(cms, "/system/", "modules");
                    
                    // create the module (first test if we are in a project including /system/
                    try {
                        cms.createFolder("/system/modules/", packetname);
                    }
                    catch(CmsException e) {
                        if(e.getType() != e.C_FILE_EXISTS) {
                            
                            //couldn't create Module
                            templateDocument.setData("details", Utils.getStackTrace(e));
                            return startProcessing(cms, templateDocument, elementName, parameters, "errorProject");
                        }
                        else {
                            try {
                                cms.readFolder("/system/modules/" + packetname + "/");
                            }
                            catch(CmsException ex) {
                                
                                // Folder exist but is deleted
                                templateDocument.setData("details", "Sorry, you have to publish this Project and create a new one.\n" + Utils.getStackTrace(e));
                                return startProcessing(cms, templateDocument, elementName, parameters, "errorProject");
                            }
                        }
                    }
                    long createDateLong = 0;
                    try {
                        createDateLong = dateFormat.parse(createDate).getTime();
                    }
                    catch(Exception exc) {
                        createDateLong = (new Date()).getTime();
                    }
                    reg.createModule(packetname, getStringValue(modulename), getStringValue(description), getStringValue(author), createDateLong, v);
                    reg.setModuleAuthorEmail(packetname, getStringValue(email));
                    reg.setModuleMaintenanceEventClass(packetname, getStringValue(maintenance));
                    tryToCreateFolder(cms, "/system/", "classes");
                    tryToCreateFolder(cms, "/", "moduledemos");
                    tryToCreateFolder(cms, "/moduledemos/", packetname);
                    
                    // create the class folder:
                    Vector cFolders = new Vector();
                    String workString = packetname;
                    while(workString.lastIndexOf('.') > -1) {
                        cFolders.addElement(workString.substring(workString.lastIndexOf('.') + 1));
                        workString = workString.substring(0, workString.lastIndexOf('.'));
                    }
                    tryToCreateFolder(cms, "/system/classes/", workString);
                    workString = "/system/classes/" + workString + "/";
                    for(int i = cFolders.size() - 1;i >= 0;i--) {
                        tryToCreateFolder(cms, workString, (String)cFolders.elementAt(i));
                        workString = workString + (String)cFolders.elementAt(i) + "/";
                    }
                    String modulePath = "/system/modules/" + packetname + "/";
                    tryToCreateFolder(cms, modulePath, "templates");
                    tryToCreateFolder(cms, modulePath, "language");
                    tryToCreateFolder(cms, modulePath + "language/", "de");
                    tryToCreateFolder(cms, modulePath + "language/", "uk");
                    tryToCreateFolder(cms, modulePath, "doc");
                    reg.setModuleDocumentPath(packetname, modulePath + "doc/index.html");
                    if("checked".equals(view)) {
                        reg.setModuleView(packetname, packetname.replace('.', '_') + ".view", modulePath + "view/index.html");
                        tryToCreateFolder(cms, modulePath, "view");
                    }
                    if("checked".equals(adminpoint)) {
                        tryToCreateFolder(cms, modulePath, "administration");
                        tryToCreateFolder(cms, modulePath, "pics");
                    }
                    try {
                        cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceAdministrationPath() + "module/index.html");
                    }
                    catch(Exception e) {
                        throw new CmsException("Redirect fails :system/workplace/administration/module/index.html", CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                    return null;
                }
            }
            else {
                if("fromerrorpage".equals(step)) {
                    Hashtable sessionData = (Hashtable)session.getValue(C_SESSION_DATA);
                    session.removeValue(C_SESSION_DATA);
                    templateDocument.setData(C_PACKETNAME, "");
                    templateDocument.setData(C_VERSION, (String)sessionData.get(C_VERSION));
                    templateDocument.setData(C_MODULENAME, (String)sessionData.get(C_MODULENAME));
                    templateDocument.setData(C_DESCRIPTION, (String)sessionData.get(C_DESCRIPTION));
                    templateDocument.setData(C_VIEW, (String)sessionData.get(C_VIEW));
                    templateDocument.setData(C_ADMINPOINT, (String)sessionData.get(C_ADMINPOINT));
                    templateDocument.setData(C_MAINTENANCE, (String)sessionData.get(C_MAINTENANCE));
                    templateDocument.setData(C_AUTHOR, (String)sessionData.get(C_AUTHOR));
                    templateDocument.setData(C_EMAIL, (String)sessionData.get(C_EMAIL));
                    templateDocument.setData(C_DATE, (String)sessionData.get(C_DATE));
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
     * @param param com.opencms.file.CmsObject
     * @param folder java.lang.String
     * @param newFolder java.lang.String
     */
    private void tryToCreateFolder(CmsObject cms, String folder, String newFolder) {
        try {
            cms.createFolder(folder, newFolder);
        }
        catch(Exception e) {
            
        }
    }
}

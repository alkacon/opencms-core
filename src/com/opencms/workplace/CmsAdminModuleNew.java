
/*
* File   : $File$
* Date   : $Date: 2001/07/18 08:14:29 $
* Version: $Revision: 1.8 $
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

import java.util.*;
import java.io.*;
import javax.servlet.http.*;

/**
 *  Template class for displaying OpenCms workplace admin module screens.
 *
 * Creation date: (01.09.00 12:55:58)
 * @author: Hanjo Riege
 */
public class CmsAdminModuleNew extends CmsWorkplaceDefault implements I_CmsConstants {

    /**
     * the different templateselectors.
     */
    private final String C_DONE = "done";
    private final String C_LOCAL = "local";
    private final String C_SERVER = "server";
    private final String C_FILES = "files";
    private final String C_FROMERRORPAGE = "fromerrorpage";
    private final String C_ERRORREPLACE = "errorreplace";
    private final String C_ERRORDEP = "errordep";
    private final String C_WAIT = "wait";

    /**
     * const for session entrys.
     */
    private final String C_MODULE_NAV = "modulenav";
    private final String C_MODULE_FILENAME = "modulefilename";
    private final String C_MODULE_THREAD = "modulethread";

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
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && C_DEBUG ) {
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector == null) ? "<default>" : templateSelector));
        }

        //CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        //Get the registry
        I_CmsRegistry reg = cms.getRegistry();
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String errorNavigation = (String)parameters.get(C_FROMERRORPAGE);
        if(errorNavigation != null) {
            templateSelector = importModule(cms, reg, xmlTemplateDocument, session, null);
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
        }
        String step = (String)parameters.get("step");

        // first look if there is already a thread running.
        if((step != null) && ("working".equals(step))) {

            // Thread is already running
            Thread doImport = (Thread)session.getValue(C_MODULE_THREAD);
            if(doImport.isAlive()) {
                String time = (String)parameters.get("time");
                int wert = Integer.parseInt(time);
                wert += 20;
                xmlTemplateDocument.setData("time", "" + wert);
                return startProcessing(cms, xmlTemplateDocument, elementName, parameters, C_WAIT);
            }
            else {
                xmlTemplateDocument.clearcache();
                return startProcessing(cms, xmlTemplateDocument, elementName, parameters, C_DONE);
            }
        }
        if(step != null) {
            if("server".equals(step)) {
                File modulefolder = new File(com.opencms.boot.CmsBase.getAbsolutePath(cms.readExportPath()) + "/" + reg.C_MODULE_PATH);
                if(!modulefolder.exists()) {
                    boolean success = modulefolder.mkdir();
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && (!success)) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsAccessFilesystem] Couldn't create folder " + com.opencms.boot.CmsBase.getAbsolutePath(cms.readExportPath()) + "/" + reg.C_MODULE_PATH + ".");
                    }
                }
                String listentrys = "";
                if(modulefolder.exists()) {
                    String[] modules = modulefolder.list();
                    for(int i = 0;i < modules.length;i++) {
                        xmlTemplateDocument.setData("modulname", modules[i]);
                        listentrys += xmlTemplateDocument.getProcessedDataValue("optionentry");
                    }
                }
                xmlTemplateDocument.setData("entries", listentrys);
                templateSelector = "server";
            }
            else {
                if("local".equals(step)) {
                    templateSelector = "local";
                }
                else {
                    if("localupload".equals(step)) {

                        // get the filename
                        String filename = null;
                        Enumeration files = cms.getRequestContext().getRequest().getFileNames();
                        while(files.hasMoreElements()) {
                            filename = (String)files.nextElement();
                        }
                        if(filename != null) {
                            session.putValue(C_PARA_FILE, filename);
                        }
                        filename = (String)session.getValue(C_PARA_FILE);

                        // get the filecontent
                        byte[] filecontent = new byte[0];
                        if(filename != null) {
                            filecontent = cms.getRequestContext().getRequest().getFile(filename);
                        }
                        if(filecontent != null) {
                            session.putValue(C_PARA_FILECONTENT, filecontent);
                        }
                        filecontent = (byte[])session.getValue(C_PARA_FILECONTENT);

                        // first create the folder if it doesnt exists
                        File discFolder = new File(com.opencms.boot.CmsBase.getAbsolutePath(cms.readExportPath()) + "/" + reg.C_MODULE_PATH);
                        if(!discFolder.exists()) {
                            boolean success = discFolder.mkdir();
                            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && (!success)) {
                                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsAccessFilesystem] Couldn't create folder " + com.opencms.boot.CmsBase.getAbsolutePath(cms.readExportPath()) + "/" + reg.C_MODULE_PATH + ".");
                            }
                        }

                        // now write the file into the modules dierectory in the exportpaht
                        File discFile = new File(com.opencms.boot.CmsBase.getAbsolutePath(cms.readExportPath()) + "/" + reg.C_MODULE_PATH + filename);
                        try {

                            // write the new file to disk
                            OutputStream s = new FileOutputStream(discFile);
                            s.write(filecontent);
                            s.close();
                        }
                        catch(Exception e) {
                            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage());
                        }
                        session.removeValue(C_MODULE_NAV);
                        templateSelector = importModule(cms, reg, xmlTemplateDocument, session, com.opencms.boot.CmsBase.getAbsolutePath(cms.readExportPath()) + "/" + reg.C_MODULE_PATH + filename);
                    }
                    else {
                        if("serverupload".equals(step)) {
                            String filename = (String)parameters.get("moduleselect");
                            session.removeValue(C_MODULE_NAV);
                            if((filename == null) || ("".equals(filename))) {
                                templateSelector = C_DONE;
                            }
                            else {
                                templateSelector = importModule(cms, reg, xmlTemplateDocument, session, com.opencms.boot.CmsBase.getAbsolutePath(cms.readExportPath()) + "/" + reg.C_MODULE_PATH + filename);
                            }
                        }
                    }
                }
            }
        }

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * Imports the file zipName.
     * Creation date: (05.09.00 10:17:56)
     * @param reg com.opencms.file.I_CmsRegistry
     * @param zipName the complete path and name of the zip-file to import.
     * @return the new templateSelector.
     */
    private String importModule(CmsObject cms, I_CmsRegistry reg, CmsXmlTemplateFile xmlDocument, I_CmsSession session, String zipName) throws CmsException {
        String nav = (String)session.getValue(C_MODULE_NAV);
        Vector conflictFiles = null;
        if(nav == null) {

            // this is the first go. Try to import the module and if it dont't work return the corresponding errorpage
            String moduleName = reg.importGetModuleName(zipName);
            if(reg.moduleExists(moduleName)) {
                xmlDocument.setData("name", moduleName);
                xmlDocument.setData("version", "" + reg.getModuleVersion(moduleName));
                session.removeValue(C_MODULE_NAV);
                return C_ERRORREPLACE;
            }
            Vector needs = reg.importCheckDependencies(zipName);
            if(!needs.isEmpty()) {

                // there are dependences not fulfilled
                xmlDocument.setData("name", moduleName);
                xmlDocument.setData("version", "" + reg.getModuleVersion(moduleName));
                String preconditions = "";
                for(int i = 0;i < needs.size();i++) {
                    preconditions += "<br><br>" + needs.elementAt(i);
                }
                xmlDocument.setData("precondition", preconditions);
                session.removeValue(C_MODULE_NAV);
                return C_ERRORDEP;
            }
            conflictFiles = reg.importGetConflictingFileNames(zipName);
            if(!conflictFiles.isEmpty()) {

                //
                session.putValue(C_SESSION_MODULE_VECTOR, conflictFiles);
                session.putValue(C_MODULE_NAV, C_FILES);
                session.putValue(C_MODULE_FILENAME, zipName);
                return C_FILES;
            }
        }
        else {
            if(C_FILES.equals(nav)) {

                //
                zipName = (String)session.getValue(C_MODULE_FILENAME);
                conflictFiles = (Vector)session.getValue(C_SESSION_MODULE_VECTOR);
                session.removeValue(C_MODULE_NAV);
            }
        }
        // just use the rootfolder instead of: Vector projectFiles = reg.importGetResourcesForProject(zipName);
        Vector projectFiles = new Vector();
        projectFiles.add("/");
        Thread doTheImport = new CmsAdminModuleImport(cms, reg, zipName, conflictFiles, projectFiles);
        doTheImport.start();
        session.putValue(C_MODULE_THREAD, doTheImport);
        xmlDocument.setData("time", "10");
        return C_WAIT;
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

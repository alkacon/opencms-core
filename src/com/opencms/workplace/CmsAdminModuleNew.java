/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleNew.java,v $
* Date   : $Date: 2004/02/13 13:41:44 $
* Version: $Revision: 1.37 $
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

import org.opencms.main.CmsException;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.threads.CmsModuleImportThread;
import org.opencms.threads.CmsModuleReplaceThread;

import com.opencms.core.I_CmsSession;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRegistry;
import com.opencms.template.CmsXmlTemplateFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 *  Template class for displaying OpenCms workplace admin module screens.
 *
 * Creation date: (01.09.00 12:55:58)
 * @author Hanjo Riege
 */
public class CmsAdminModuleNew extends CmsWorkplaceDefault {

    /**
     * the different templateselectors.
     */
    private final String C_DONE = "done";
    private final String C_FILES = "files";
    private final String C_FROMERRORPAGE = "fromerrorpage";
    private final String C_ERRORREPLACE = "errorreplace";
    private final String C_ERRORDEP = "errordep";

    /**
     * const for session entrys.
     */
    private final String C_MODULE_NAV = "modulenav";
    private final String C_MODULE_FILENAME = "modulefilename";
    private final String C_MODULE_NAME = "modulename";
    private final String C_MODULE_THREAD = "modulethread";

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

        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // get the registry
        CmsRegistry reg = cms.getRegistry();
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String errorNavigation = (String)parameters.get(C_FROMERRORPAGE);
        if(errorNavigation != null) {
            templateSelector = importModule(cms, reg, xmlTemplateDocument, session, null);
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
        }

        String step = (String)parameters.get("step");               

        // first look if there is already a thread running.
        if("showResult".equals(step)){
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_MODULE_THREAD);
            if(doTheWork.isAlive()){
                // thread is still running
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", "");
            } else {
                // thread is finished, activate the buttons 
                xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod"));
                xmlTemplateDocument.setData("autoUpdate","");
                xmlTemplateDocument.setData("text", xmlTemplateDocument.getLanguageFile().getLanguageValue("module.lable.importend"));
                session.removeValue(C_MODULE_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");         
        } else if ("server".equals(step)) {
            File modulefolder = new File(OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath() + File.separator + CmsRegistry.C_MODULE_PATH));
            if (!modulefolder.exists()) {
                boolean success = modulefolder.mkdir();
                if (OpenCms.getLog(this).isWarnEnabled()
                    && (!success)) {
                    OpenCms.getLog(this).warn("Could not create folder " + modulefolder.getAbsolutePath());
                }
            }
            String listentrys = "";
            if (modulefolder.exists()) {
                String[] modules = modulefolder.list();
                for (int i = 0; i < modules.length; i++) {
                    xmlTemplateDocument.setData("modulname", modules[i]);
                    listentrys += xmlTemplateDocument.getProcessedDataValue("optionentry");
                }
            }
            xmlTemplateDocument.setData("entries", listentrys);
            templateSelector = "server";

        } else if ("local".equals(step)) {
            templateSelector = "local";

        } else if ("localupload".equals(step)) {
            // get the filename
            String filename = null;
            Enumeration files = cms.getRequestContext().getRequest().getFileNames();
            while (files.hasMoreElements()) {
                filename = (String) files.nextElement();
            }
            if (filename != null) {
                session.putValue(C_PARA_RESOURCE, filename);
            }
            filename = (String) session.getValue(C_PARA_RESOURCE);

            // get the filecontent
            byte[] filecontent = new byte[0];
            if (filename != null) {
                filecontent = cms.getRequestContext().getRequest().getFile(filename);
            }
            if (filecontent != null) {
                session.putValue(C_PARA_FILECONTENT, filecontent);
            }
            filecontent = (byte[]) session.getValue(C_PARA_FILECONTENT);

            // first create the folder if it doesnt exists
            File discFolder = new File(OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath() + File.separator + CmsRegistry.C_MODULE_PATH));
            if (!discFolder.exists()) {
                boolean success = discFolder.mkdir();
                if (OpenCms.getLog(this).isWarnEnabled()
                    && (!success)) {
                    OpenCms.getLog(this).warn("Could not create folder " + discFolder.getAbsolutePath());
                }
            }

            // now write the file into the modules dierectory in the exportpaht
            File discFile =
                new File(OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath()+ File.separator + CmsRegistry.C_MODULE_PATH + filename));
            try {

                // write the new file to disk
                OutputStream s = new FileOutputStream(discFile);
                s.write(filecontent);
                s.close();
            } catch (Exception e) {
                throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage());
            }
            session.removeValue(C_MODULE_NAV);
            templateSelector = importModule(cms, reg, xmlTemplateDocument, session, OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath() + File.separator + CmsRegistry.C_MODULE_PATH + filename));
                        
        } else if ("serverupload".equals(step)) {
            String filename = (String) parameters.get("moduleselect");
            session.removeValue(C_MODULE_NAV);
            if ((filename == null) || ("".equals(filename))) {
                templateSelector = C_DONE;
            } else {
                templateSelector = importModule(cms, reg, xmlTemplateDocument, session, OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath() + File.separator + CmsRegistry.C_MODULE_PATH + filename));
            }
        }

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * Imports the file zipName.
     * Creation date: (05.09.00 10:17:56)
     * @param reg org.opencms.file.I_CmsRegistry
     * @param zipName the complete path and name of the zip-file to import.
     * @return the new templateSelector.
     */
    private String importModule(CmsObject cms, CmsRegistry reg, CmsXmlTemplateFile xmlDocument, I_CmsSession session, String zipName) throws CmsException {
        String nav = (String)session.getValue(C_MODULE_NAV);
        Vector conflictFiles = null;
        String moduleName = null;
        boolean importNewModule = true;
        String moduleType = CmsRegistry.C_MODULE_TYPE_TRADITIONAL;
        
        if(nav == null) {
            // this is the first go. Try to import the module and if it dont't work return the corresponding errorpage
            Map moduleInfo = reg.importGetModuleInfo(zipName);
            
            moduleName = (String)moduleInfo.get("name");
            moduleType = (String)moduleInfo.get("type");
            
            if(reg.moduleExists(moduleName)) {
                if (! CmsRegistry.C_MODULE_TYPE_SIMPLE.equals(moduleType)) {
                    // not a simple module, can not be replaced
                    xmlDocument.setData("name", moduleName);
                    xmlDocument.setData("version", "" + reg.getModuleVersion(moduleName));
                    session.removeValue(C_MODULE_NAV);
                    return C_ERRORREPLACE;
                } else {
                    // simple module, start module replacement
                    session.putValue(C_MODULE_NAME, moduleName);                    
                    importNewModule = false;
                }
            }
            Vector needs = reg.importCheckDependencies(zipName, true);
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
            if (! CmsRegistry.C_MODULE_TYPE_SIMPLE.equals(moduleType)) {
                conflictFiles = reg.importGetConflictingFileNames(zipName);
                if (!conflictFiles.isEmpty()) {
                    session.putValue(C_SESSION_MODULE_VECTOR, conflictFiles);
                    session.putValue(C_MODULE_NAV, C_FILES);
                    session.putValue(C_MODULE_FILENAME, zipName);
                    return C_FILES;
                }
            } else {
                // simple module, no "confict file" check performed
                conflictFiles = new Vector();
            }
            
        }
        else {
            if(C_FILES.equals(nav)) {
                zipName = (String)session.getValue(C_MODULE_FILENAME);
                moduleName = (String)session.getValue(C_MODULE_NAME);
                if (moduleName != null) importNewModule = false; 
                conflictFiles = (Vector)session.getValue(C_SESSION_MODULE_VECTOR);
                session.removeValue(C_MODULE_NAV);
            }
        }
        	
        // add root folder as file list for the project
        if (importNewModule) {
            A_CmsReportThread doTheImport = new CmsModuleImportThread(cms, reg, moduleName, zipName, conflictFiles);
            doTheImport.start();
            session.putValue(C_MODULE_THREAD, doTheImport);
        } else {
            A_CmsReportThread doTheReplace = new CmsModuleReplaceThread(cms, reg, moduleName, zipName, conflictFiles);
            doTheReplace.start();
            session.putValue(C_MODULE_THREAD, doTheReplace);
        }
        xmlDocument.setData("time", "5");      
        return "showresult";                
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

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleNew.java,v $
* Date   : $Date: 2005/02/18 14:23:15 $
* Version: $Revision: 1.46 $
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
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.module.CmsModuleManager;
import org.opencms.report.A_CmsReportThread;
import org.opencms.threads.CmsDatabaseImportThread;
import org.opencms.threads.CmsModuleReplaceThread;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.CmsXmlTemplateFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *  Template class for displaying OpenCms workplace admin module screens.
 *
 * Creation date: (01.09.00 12:55:58)
 * @author Hanjo Riege
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsAdminModuleNew extends CmsWorkplaceDefault {

    /**
     * the different templateselectors.
     */
    private final String C_DONE = "done";
    private final String C_ERRORDEP = "errordep";
    private final String C_ERRORREPLACE = "errorreplace";
    private final String C_FILES = "files";
    private final String C_FROMERRORPAGE = "fromerrorpage";
    private final String C_MODULE = "moduleinstance";
    private final String C_MODULE_FILENAME = "modulefilename";

    /**
     * const for session entrys.
     */
    private final String C_MODULE_NAV = "modulenav";
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

        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String errorNavigation = (String)parameters.get(C_FROMERRORPAGE);
        if(errorNavigation != null) {
            templateSelector = importModule(cms, xmlTemplateDocument, session, null);
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
            File modulefolder = new File(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator + I_CmsConstants.C_MODULE_PATH));
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
            Enumeration files = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getFileNames();
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
                filecontent = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getFile(filename);
            }
            if (filecontent != null) {
                session.putValue(C_PARA_FILECONTENT, filecontent);
            }
            filecontent = (byte[]) session.getValue(C_PARA_FILECONTENT);

            // first create the folder if it doesnt exists
            File discFolder = new File(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator + I_CmsConstants.C_MODULE_PATH));
            if (!discFolder.exists()) {
                boolean success = discFolder.mkdir();
                if (OpenCms.getLog(this).isWarnEnabled()
                    && (!success)) {
                    OpenCms.getLog(this).warn("Could not create folder " + discFolder.getAbsolutePath());
                }
            }

            // now write the file into the modules dierectory in the exportpaht
            File discFile =
                new File(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSystemInfo().getPackagesRfsPath()+ File.separator + I_CmsConstants.C_MODULE_PATH + filename));
            try {

                // write the new file to disk
                OutputStream s = new FileOutputStream(discFile);
                s.write(filecontent);
                s.close();
            } catch (Exception e) {
                throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage());
            }
            session.removeValue(C_MODULE_NAV);
            templateSelector = importModule(cms, xmlTemplateDocument, session, OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSystemInfo().getPackagesRfsPath() + I_CmsConstants.C_MODULE_PATH + filename));
                        
        } else if ("serverupload".equals(step)) {
            String filename = (String) parameters.get("moduleselect");
            session.removeValue(C_MODULE_NAV);
            if ((filename == null) || ("".equals(filename))) {
                templateSelector = C_DONE;
            } else {
                templateSelector = importModule(cms, xmlTemplateDocument, session, OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSystemInfo().getPackagesRfsPath() + I_CmsConstants.C_MODULE_PATH + filename));
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

    /**
     * Imports the file zipName.
     * Creation date: (05.09.00 10:17:56)
     * @param zipName the complete path and name of the zip-file to import.
     * @return the new templateSelector.
     */
    private String importModule(CmsObject cms, CmsXmlTemplateFile xmlDocument, I_CmsSession session, String zipName) throws CmsException {
        String nav = (String)session.getValue(C_MODULE_NAV);
        Vector conflictFiles = new Vector();
        CmsModule module = null;
        boolean importNewModule = true;

        if(nav == null) {
            // this is the first go. Try to import the module and if it dont't work return the corresponding errorpage
            module = CmsModuleImportExportHandler.readModuleFromImport(zipName);
            
            if(OpenCms.getModuleManager().hasModule(module.getName())) {
                // simple module, start module replacement
                session.putValue(C_MODULE, module);                    
                importNewModule = false;
            }
            List dependencies = OpenCms.getModuleManager().checkDependencies(module, CmsModuleManager.C_DEPENDENCY_MODE_IMPORT);
            if(!dependencies.isEmpty()) {
                // there are dependences not fulfilled
                xmlDocument.setData("name", module.getName());
                xmlDocument.setData("version", module.getVersion().toString());
                String preconditions = "";
                for(int i = 0;i < dependencies.size(); i++) {
                    preconditions += "<br><br>" + dependencies.get(i);
                }
                xmlDocument.setData("precondition", preconditions);
                session.removeValue(C_MODULE_NAV);
                return C_ERRORDEP;
            }
        
            // simple module, no "confict file" check performed
            conflictFiles = new Vector();
        }
        else {
            if(C_FILES.equals(nav)) {
                zipName = (String)session.getValue(C_MODULE_FILENAME);
                module = (CmsModule)session.getValue(C_MODULE);
                if (module != null) importNewModule = false; 
                conflictFiles = (Vector)session.getValue(C_SESSION_MODULE_VECTOR);
                session.removeValue(C_MODULE);
                session.removeValue(C_MODULE_NAV);
            }
        }
        	
        if (importNewModule) {
            A_CmsReportThread doTheImport = new CmsDatabaseImportThread(cms, zipName);
            doTheImport.start();
            session.putValue(C_MODULE_THREAD, doTheImport);
        } else {
            A_CmsReportThread doTheReplace = new CmsModuleReplaceThread(cms, module.getName(), zipName);
            doTheReplace.start();
            session.putValue(C_MODULE_THREAD, doTheReplace);
        }
        xmlDocument.setData("time", "5");      
        return "showresult";                
    }
}

/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsAdminModuleDelete.java,v $
* Date   : $Date: 2005/05/31 15:51:19 $
* Version: $Revision: 1.3 $
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
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleManager;
import org.opencms.report.A_CmsReportThread;
import org.opencms.workplace.threads.CmsModuleDeleteThread;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace administration module delete.
 *
 * Creation date: (12.09.00 10:28:08)
 * @author Hanjo Riege
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsAdminModuleDelete extends CmsWorkplaceDefault {
    
    private static final  String C_MODULE = "module";
    private static final String C_STEP = "step";
    private static final String C_DELETE = "delete";
    private static final String C_WARNING = "warning";
    private static final String C_ERROR = "error";
    private static final String C_SESSION_MODULENAME = "deletemodulename";
    private static final String C_MODULE_THREAD = "moduledeletethread";

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
        if(CmsLog.getLog(this).isDebugEnabled() && C_DEBUG) {
            CmsLog.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            CmsLog.getLog(this).debug("Template file is: " + templateFile);
            CmsLog.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        CmsRequestContext reqCont = cms.getRequestContext();
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String step = (String)parameters.get(C_STEP);
        String moduleName = (String)parameters.get(C_MODULE);
        
        if(step == null) {
            String moduleVersion = OpenCms.getModuleManager().getModule(moduleName).getVersion().toString();
            xmlTemplateDocument.setData("name", moduleName);
            xmlTemplateDocument.setData("version", moduleVersion);
            
        } else if("showResult".equals(step)){
            // first look if there is already a thread running.
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_MODULE_THREAD);
            if(doTheWork.isAlive()){
                // thread is still running
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", "");
            }else{
                // thread is finished, activate the buttons
                xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod"));
                xmlTemplateDocument.setData("autoUpdate","");
                xmlTemplateDocument.setData("text", xmlTemplateDocument.getLanguageFile().getLanguageValue("module.lable.deleteend"));
                session.removeValue(C_MODULE_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");
            
        } else if(C_DELETE.equals(step)) {  
            CmsModule module = OpenCms.getModuleManager().getModule(moduleName);            
            List dependencies = OpenCms.getModuleManager().checkDependencies(module, CmsModuleManager.C_DEPENDENCY_MODE_DELETE);
            if(!dependencies.isEmpty()) {
                // don't delete; send message error
                xmlTemplateDocument.setData("name", module.getName());
                xmlTemplateDocument.setData("version", module.getVersion().toString());
                String depModules = "";
                for(int i = 0;i < dependencies.size();i++) {
                    depModules += ((CmsModuleDependency)dependencies.get(i)).toString()+"\n";
                }
                xmlTemplateDocument.setData("precondition", depModules);
                templateSelector = C_ERROR;
            }
            else {
                // now we will look if there are any conflicting files
                Vector filesWithProperty = new Vector();
                Vector missingFiles = new Vector();
                Vector wrongChecksum = new Vector();
                Vector filesInUse = new Vector();
                Vector resourcesForProject = new Vector();
                reqCont.setCurrentProject(cms.readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
                session.putValue(C_SESSION_MODULENAME, moduleName);
                session.putValue(C_SESSION_MODULE_PROJECTFILES, resourcesForProject);
                if(filesWithProperty.isEmpty() && missingFiles.isEmpty() && wrongChecksum.isEmpty() && filesInUse.isEmpty()) {
                    step = "fromerrorpage";
                } else {
                    session.putValue(C_SESSION_MODULE_DELETE_STEP, "0");
                    session.putValue(C_SESSION_MODULE_CHECKSUM, wrongChecksum);
                    session.putValue(C_SESSION_MODULE_PROPFILES, filesWithProperty);
                    session.putValue(C_SESSION_MODULE_INUSE, filesInUse);
                    session.putValue(C_SESSION_MODULE_MISSFILES, missingFiles);
                    templateSelector = C_WARNING;
                }
            }
            
        }
        // no else here because the value of "step" might have been changed above 
        if ("fromerrorpage".equals(step)) {
            moduleName = (String)session.getValue(C_SESSION_MODULENAME);    
            List modules = new ArrayList();
            modules.add(moduleName);
            A_CmsReportThread doDelete = new CmsModuleDeleteThread(cms, modules, false, true);
            doDelete.start();
            session.putValue(C_MODULE_THREAD, doDelete);
            xmlTemplateDocument.setData("time", "5");
            templateSelector = "showresult";            
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

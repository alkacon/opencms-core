/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleDelete.java,v $
* Date   : $Date: 2001/07/31 15:50:17 $
* Version: $Revision: 1.7 $
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

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace administration module delete.
 *
 * Creation date: (12.09.00 10:28:08)
 * @author: Hanjo Riege
 */
public class CmsAdminModuleDelete extends CmsWorkplaceDefault implements I_CmsConstants {
    private final String C_MODULE = "module";
    private final String C_STEP = "step";
    private final String C_DELETE = "delete";
    private final String C_WARNING = "warning";
    private final String C_DONE = "done";
    private final String C_WAIT = "wait";
    private final String C_ERROR = "error";
    private final String C_SESSION_MODULENAME = "deletemodulename";
    private final String C_MODULE_THREAD = "moduledeletethread";

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
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && C_DEBUG) {
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector == null) ? "<default>" : templateSelector));
        }
        CmsXmlTemplateFile xmlDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        CmsRequestContext reqCont = cms.getRequestContext();
        I_CmsRegistry reg = cms.getRegistry();
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String step = (String)parameters.get(C_STEP);
        String moduleName = (String)parameters.get(C_MODULE);
        if((step != null) && ("working".equals(step))) {

            // Thread is already running
            Thread doDelete = (Thread)session.getValue(C_MODULE_THREAD);
            if(doDelete.isAlive()) {
                String time = (String)parameters.get("time");
                int wert = Integer.parseInt(time);
                wert += 20;
                xmlDocument.setData("time", "" + wert);
                return startProcessing(cms, xmlDocument, elementName, parameters, C_WAIT);
            }
            else {
                return startProcessing(cms, xmlDocument, elementName, parameters, C_DONE);
            }
        }
        if(step == null) {
            xmlDocument.setData("name", moduleName);
            xmlDocument.setData("version", "" + reg.getModuleVersion(moduleName));
        }
        else {
            if(C_DELETE.equals(step)) {
                Vector otherModules = reg.deleteCheckDependencies(moduleName);
                if(!otherModules.isEmpty()) {

                    // Don't delete; send message error
                    xmlDocument.setData("name", moduleName);
                    xmlDocument.setData("version", "" + reg.getModuleVersion(moduleName));
                    String depModules = "";
                    for(int i = 0;i < otherModules.size();i++) {
                        depModules += (String)otherModules.elementAt(i) + "\n";
                    }
                    xmlDocument.setData("precondition", depModules);
                    templateSelector = C_ERROR;
                }
                else {

                    // now we will look if ther are any conflicting files
                    Vector filesWithProperty = new Vector();
                    Vector missingFiles = new Vector();
                    Vector wrongChecksum = new Vector();
                    Vector filesInUse = new Vector();
                    Vector resourcesForProject = new Vector();
                    reqCont.setCurrentProject(cms.onlineProject().getId());
                    reg.deleteGetConflictingFileNames(moduleName, filesWithProperty, missingFiles, wrongChecksum, filesInUse, resourcesForProject);
                    session.putValue(C_SESSION_MODULENAME, moduleName);
                    session.putValue(C_SESSION_MODULE_PROJECTFILES, resourcesForProject);
                    if(filesWithProperty.isEmpty() && missingFiles.isEmpty() && wrongChecksum.isEmpty() && filesInUse.isEmpty()) {
                        step = "fromerrorpage";
                    }
                    else {
                        session.putValue(C_SESSION_MODULE_DELETE_STEP, "0");
                        session.putValue(C_SESSION_MODULE_CHECKSUM, wrongChecksum);
                        session.putValue(C_SESSION_MODULE_PROPFILES, filesWithProperty);
                        session.putValue(C_SESSION_MODULE_INUSE, filesInUse);
                        session.putValue(C_SESSION_MODULE_MISSFILES, missingFiles);
                        templateSelector = C_WARNING;
                    }
                }
            }
        }
        if((step != null) && ("fromerrorpage".equals(step))) {
            Vector exclusion = (Vector)session.getValue(C_SESSION_MODULE_EXCLUSION);
            // use the root folder instead of: Vector resourcesForProject = (Vector)session.getValue(C_SESSION_MODULE_PROJECTFILES);
            Vector resourcesForProject = new Vector();
            resourcesForProject.add("/");
            if(exclusion == null) {
                exclusion = new Vector();
            }
            moduleName = (String)session.getValue(C_SESSION_MODULENAME);
            Thread doDelete = new CmsAdminModuleDeleteThread(cms, reg, moduleName, exclusion, resourcesForProject);
            doDelete.start();
            session.putValue(C_MODULE_THREAD, doDelete);
            xmlDocument.setData("time", "10");
            templateSelector = C_WAIT;
        }

        // Now load the template file and start the processing
        return startProcessing(cms, xmlDocument, elementName, parameters, templateSelector);
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

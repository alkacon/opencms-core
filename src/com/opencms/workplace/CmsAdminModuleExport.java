/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleExport.java,v $
* Date   : $Date: 2004/07/09 16:01:31 $
* Version: $Revision: 1.53 $
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
import org.opencms.file.CmsRegistry;
import org.opencms.importexport.CmsModuleImportExportHandler;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.threads.CmsExportThread;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Template class for displaying OpenCms workplace administration module create.
 *
 * Creation date: (27.10.00 10:28:08)
 * @author Hanjo Riege
 * @author Thomas Weckert
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsAdminModuleExport extends CmsWorkplaceDefault {

	private final String C_MODULE = "module";
    private final String C_MODULENAME = "modulename";
	private final String C_ACTION = "action";
    private final String C_MODULE_THREAD = "modulethread";    

	private static final int C_MINIMUM_MODULE_RESOURCE_COUNT = 1;

	private static final int DEBUG = 0;

	/**
	 * Collects all resources of a module to be exported in a string array. By setting the module property
	 * "additional_folders" as a folder list separated by ";", you can specify folders outside the 
	 * "system/modules" directory to be exported with the module!
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
        
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        CmsRegistry reg = OpenCms.getRegistry();
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);

		String step = (String) parameters.get(C_ACTION);
        String moduleName = (String) parameters.get(C_MODULENAME);
               
		if (step == null) {
            // first call
            xmlTemplateDocument.setData("modulename", (String)parameters.get(C_MODULE));    
            
        } else if("showResult".equals(step)){
            if (DEBUG > 1) System.out.println("showResult for export");
                     
            // first look if there is already a thread running.
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_MODULE_THREAD);
            if(doTheWork.isAlive()){
                if (DEBUG > 1) System.out.println("showResult: thread is still running");
                // thread is still running
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", "");
            }else{
                if (DEBUG > 1) System.out.println("showResult: thread is finished");
                // thread is finished, activate the buttons
                xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod"));
                xmlTemplateDocument.setData("autoUpdate","");
                xmlTemplateDocument.setData("text", xmlTemplateDocument.getLanguageFile().getLanguageValue("module.lable.exportend"));
                session.removeValue(C_MODULE_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");            
                  
        } else if ("ok".equals(step)) {
            // export is confirmed			
			String[] resourcen = null;
			int resourceCount = 0;
			int i = 0;

			if (reg.getModuleType(moduleName).equals(CmsRegistry.C_MODULE_TYPE_SIMPLE)) {
				// SIMPLE MODULE
				if (DEBUG > 0) {
					System.out.println(moduleName + " is a simple module");
				}

				// check if additional resources outside the system/modules/{exportName} folder were 
				// specified as module resources by reading the property {C_MODULE_PROPERTY_ADDITIONAL_RESOURCES}
				// to the module (in the module administration)
				String additionalResources = OpenCms.getRegistry().getModuleParameterString(moduleName, I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES);
				StringTokenizer additionalResourceTokens = null;

				if (additionalResources != null && !additionalResources.equals("")) {
					// add each additonal folder plus its content folder under "content/bodys"
					additionalResourceTokens = new StringTokenizer(additionalResources, I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR);

					resourceCount = (additionalResourceTokens.countTokens()) + CmsAdminModuleExport.C_MINIMUM_MODULE_RESOURCE_COUNT;
					resourcen = new String[resourceCount];

					// add each resource plus its equivalent at content/bodys to 
					// the string array of all resources for the export
					while (additionalResourceTokens.hasMoreTokens()) {
						String currentResource = additionalResourceTokens.nextToken().trim();

                        if (! "-".equals(currentResource)) {
                            if (DEBUG > 0) {
                                System.err.println("Adding resource: " + currentResource);
                            }                            
						    resourcen[i++] = currentResource;
                        }
					}
				} else {
					// no additional resources were specified...
				    resourceCount = CmsAdminModuleExport.C_MINIMUM_MODULE_RESOURCE_COUNT;
					resourcen = new String[resourceCount];
					i = 0;
				}
			} else {
				// TRADITIONAL MODULE
				if (DEBUG > 0) {
					System.out.println(moduleName + " is a traditional module");
				}

				resourceCount = CmsAdminModuleExport.C_MINIMUM_MODULE_RESOURCE_COUNT;
				resourcen = new String[resourceCount];
				i = 0;
			}

			// finally, add the "standard" module resources to the string of all resources for the export
			// if you add or remove paths here, ensure to adjust CmsAdminModuleExport.C_MINIMUM_MODULE_RESOURCE_COUNT to the proper length!
			resourcen[i++] = C_VFS_PATH_MODULES + moduleName + "/";

			// check if all resources exists and can be read
            ArrayList resList = new ArrayList(Arrays.asList(resourcen));    
            ArrayList resListCopy = new ArrayList();  
			for (Iterator it = resList.iterator(); it.hasNext(); ) {
                String res = (String)it.next();
				try {
					if (res != null) {
						if (DEBUG > 0) {
							System.err.println("reading file header of: " + res);
						}
						cms.readResource(res);       
                        resListCopy.add(res);                 
					}
				}
				catch (CmsException e) {
                    // resource did not exist / could not be read
					if (OpenCms.getLog(this).isErrorEnabled()) {
						OpenCms.getLog(this).error("Error exporting module: couldn't add " + res + " to Module", e);
					}
                    if (DEBUG > 0) {
                        System.err.println("couldn't add " + res);
                    } 
				}
			}            
            resourcen = new String[resListCopy.size()];         
            for (int count=0; count < resListCopy.size(); count++ ) {               
                resourcen[count] = (String)resListCopy.get(count);        
                if (DEBUG > 0) {
                    System.err.println("exporting " + resourcen[count]);
                } 
            }                            
            
            String filename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSystemInfo().getPackagesRfsPath() + CmsRegistry.C_MODULE_PATH + moduleName + "_" + reg.getModuleVersion(moduleName));
            
            CmsModuleImportExportHandler moduleExportHandler = new CmsModuleImportExportHandler();
            moduleExportHandler.setFileName(filename);
            moduleExportHandler.setModuleName(moduleName.replace('\\', '/'));
            moduleExportHandler.setAdditionalResources(resourcen);
            moduleExportHandler.setDescription("Module export of " + moduleExportHandler.getModuleName());
            
            A_CmsReportThread doExport = new CmsExportThread(cms, moduleExportHandler);
            doExport.start();
            session.putValue(C_MODULE_THREAD, doExport);
            xmlTemplateDocument.setData("time", "5");
            templateSelector = "showresult";
		}

		// now load the template file and start the processing
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

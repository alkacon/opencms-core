/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminStaticExport.java,v $
* Date   : $Date: 2001/05/15 19:29:06 $
* Version: $Revision: 1.3 $
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

import com.opencms.boot.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace admin static export.
 * <P>
 *
 * @author Hanjo Riege
 * @version $Revision: 1.3 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminStaticExport extends CmsWorkplaceDefault implements I_CmsConstants {

    private static String C_STATICEXPORT_THREAD = "static_export_thread";

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

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && (A_OpenCms.isLogging() && com.opencms.boot.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING)) {
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element "
                            + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: "
                            + ((templateSelector == null) ? "<default>" : templateSelector));
        }

        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // get the parameters
        String exportTo = (String)parameters.get("filename");
        String action = (String)parameters.get("action");
        String allResources = (String)parameters.get("ALLRES");
        if(action == null) {
            // This is an initial request of the static export page
            xmlTemplateDocument.setData("path", com.opencms.boot.CmsBase.getAbsolutePath(cms.readExportPath()));
        }

        // first we look if the thread is allready running
        if((action != null) && ("working".equals(action))) {
            // still working?
            Thread doTheWork = (Thread)session.getValue(C_STATICEXPORT_THREAD);
            if(doTheWork.isAlive()) {
                String time = (String)parameters.get("time");
                int wert = Integer.parseInt(time);
                wert += 20;
                xmlTemplateDocument.setData("time", "" + wert);
                return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "wait");
            }else {
                // thread has come to an end, was there an error?
                String errordetails = (String)session.getValue(C_SESSION_THREAD_ERROR);
                session.removeValue(C_SESSION_THREAD_ERROR);
                if(errordetails == null) {
                    // export ready
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "done");
                }else {
                    // get errorpage:
                    xmlTemplateDocument.setData("details", errordetails);
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "error");
                }
            }
        }
        try {
            if("export".equals(action)) {

                // export the files
                Vector resourceNames = parseResources(allResources);
                String[] exportPaths = new String[resourceNames.size()];
                CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
                for(int i = 0;i < resourceNames.size();i++) {
                    // modify the foldername if nescessary (the root folder is always given
                    // as a nice name)
                    if(lang.getLanguageValue("title.rootfolder").equals(resourceNames.elementAt(i))) {
                        resourceNames.setElementAt("/", i);
                    }
                    exportPaths[i] = (String)resourceNames.elementAt(i);
                }

                // start the thread for export
                // first clear the session entry if necessary
                if(session.getValue(C_SESSION_THREAD_ERROR) != null) {
                    session.removeValue(C_SESSION_THREAD_ERROR);
                }
                Thread doExport = new CmsAdminStaticExportThread(cms, exportTo, exportPaths);
                doExport.start();
                session.putValue(C_STATICEXPORT_THREAD , doExport);
                xmlTemplateDocument.setData("time", "10");
                templateSelector = "wait";
            }
        }catch(CmsException exc) {
            xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
            templateSelector = "error";
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
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }

    /** Parse the string which holds all resources
     *
     * @param resources containts the full pathnames of all the resources, separated by semicolons
     * @return A vector with the same resources
     */
    private Vector parseResources(String resources) {
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
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDatabase.java,v $
* Date   : $Date: 2001/05/15 19:29:05 $
* Version: $Revision: 1.18 $
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
 * Template class for displaying OpenCms workplace task head screens.
 * <P>
 *
 * @author Andreas Schouten
 * @version $Revision: 1.18 $ $Date: 2001/05/15 19:29:05 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminDatabase extends CmsWorkplaceDefault implements I_CmsConstants {

    private static String C_DATABASE_THREAD = "databse_im_export_thread";

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
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName()
                    + "getting content of element "
                            + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName()
                    + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName()
                    + "selected template section is: "
                            + ((templateSelector == null) ? "<default>" : templateSelector));
        }

        //CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // get the parameters
        // String folder = (String)parameters.get("selectallfolders");
        String fileName = (String)parameters.get("filename");
        String existingFile = (String)parameters.get("existingfile");
        String action = (String)parameters.get("action");
        String allResources = (String)parameters.get("ALLRES");
        if(action == null) {

            // This is an initial request of the database administration page
            // Generate datablocks for checkboxes in the HTML form
            if(!cms.getRequestContext().currentProject().equals(cms.onlineProject())) {
                xmlTemplateDocument.setData("nounchanged",
                        xmlTemplateDocument.getProcessedDataValue("nounchangedbox", this, parameters));
            }
            if(cms.isAdmin()) {
                xmlTemplateDocument.setData("userdata",
                        xmlTemplateDocument.getProcessedDataValue("userdatabox", this, parameters));
            }
        }

        // first we look if the thread is allready running
        if((action != null) && ("working".equals(action))) {

            // still working?
            Thread doTheWork = (Thread)session.getValue(C_DATABASE_THREAD);
            if(doTheWork.isAlive()) {
                String time = (String)parameters.get("time");
                int wert = Integer.parseInt(time);
                wert += 20;
                xmlTemplateDocument.setData("time", "" + wert);
                return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "wait");
            }
            else {

                // thread has come to an end, was there an error?
                String errordetails = (String)session.getValue(C_SESSION_THREAD_ERROR);
                if(errordetails == null) {

                    // im/export ready
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "done");
                }
                else {

                    // get errorpage:
                    xmlTemplateDocument.setData("details", errordetails);
                    session.removeValue(C_SESSION_THREAD_ERROR);
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "error");
                }
            }
        }
        try {
            if("export".equals(action)) {

                // export the database
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
                boolean excludeSystem = false;
                if(parameters.get("nosystem") != null) {
                    excludeSystem = true;
                }
                boolean excludeUnchanged = false;
                if(parameters.get("nounchanged") != null) {
                    excludeUnchanged = true;
                }
                boolean exportUserdata = false;
                if(parameters.get("userdata") != null) {
                    exportUserdata = true;
                }

                // start the thread for: export
                // first clear the session entry if necessary
                if(session.getValue(C_SESSION_THREAD_ERROR) != null) {
                    session.removeValue(C_SESSION_THREAD_ERROR);
                }
                Thread doExport = new CmsAdminDatabaseExportThread(cms, CmsBase.getAbsolutePath(cms.readExportPath()) + File.separator
                        + fileName, exportPaths, excludeSystem, excludeUnchanged, exportUserdata, session);
                doExport.start();
                session.putValue(C_DATABASE_THREAD, doExport);
                xmlTemplateDocument.setData("time", "10");
                templateSelector = "wait";
            }
            else {
                if("import".equals(action)) {

                    // start the thread for: import
                    // first clear the session entry if necessary
                    if(session.getValue(C_SESSION_THREAD_ERROR) != null) {
                        session.removeValue(C_SESSION_THREAD_ERROR);
                    }
                    Thread doImport = new CmsAdminDatabaseImportThread(cms, CmsBase.getAbsolutePath(cms.readExportPath()) + File.separator
                            + existingFile, session);
                    doImport.start();
                    session.putValue(C_DATABASE_THREAD, doImport);
                    xmlTemplateDocument.setData("time", "10");
                    templateSelector = "wait";
                }
            }
        }
        catch(CmsException exc) {
            xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
            templateSelector = "error";
        }

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
    }

    /**
     * Gets all export-files from the export-path.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * <code>names</code> will contain language specific view descriptions
     * and <code>values</code> will contain the correspondig URL for each
     * of these views after returning from this method.
     * <P>
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current filter view in the vectors.
     * @exception CmsException
     */

    public Integer getExportFiles(CmsObject cms, CmsXmlLanguageFile lang, Vector values,
            Vector names, Hashtable parameters) throws CmsException {

        // get the systems-exportpath
        String exportpath = cms.readExportPath();
        exportpath = CmsBase.getAbsolutePath(exportpath);
        File folder = new File(exportpath);
        if (!folder.exists()){
            folder.mkdirs();
        }
        // get a list of all files
        String[] list = folder.list(new FilenameFilter() {
                public boolean accept(File dir, String fileName) {
                        return(fileName.endsWith(".zip"));
                }});
        for(int i = 0;i < list.length;i++) {
            File diskFile = new File(exportpath, list[i]);

            // check if it is a file
            if(diskFile.isFile()) {
                values.addElement(list[i]);
                names.addElement(list[i]);
            }
        }
        return new Integer(0);
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

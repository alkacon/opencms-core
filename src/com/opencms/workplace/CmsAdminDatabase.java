/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDatabase.java,v $
* Date   : $Date: 2004/02/13 13:41:44 $
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

import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.threads.CmsDatabaseExportThread;
import org.opencms.threads.CmsDatabaseImportThread;

import com.opencms.core.I_CmsSession;
import org.opencms.file.CmsObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class manages the templates for import and export of the database.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Andreas Schouten
 * @version $Revision: 1.53 $ 
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsAdminDatabase extends CmsWorkplaceDefault {

    private static String C_DATABASE_THREAD = "databse_im_export_thread";

    /**
     * Gets a database importfile from the client and copys it to the server.<p>
     *
     * @param cms The CmsObject.
     * @param session The session.
     *
     * @return the name of the file.
     */
    private String copyFileToServer(
        CmsObject cms, 
        I_CmsSession session ) 
    throws CmsException{
        // get the filename
        String filename = null;
        Enumeration files = cms.getRequestContext().getRequest().getFileNames();
        while(files.hasMoreElements()) {
            filename = (String)files.nextElement();
        }
        if(filename != null) {
            session.putValue(C_PARA_RESOURCE, filename);
        }
        filename = (String)session.getValue(C_PARA_RESOURCE);

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
        File discFolder = new File(OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath() + File.separator));
        if(!discFolder.exists()) {
            boolean success = discFolder.mkdir();
            if(OpenCms.getLog(this).isWarnEnabled() && (!success)) {
                OpenCms.getLog(this).warn("Couldn't create folder " + discFolder.getAbsolutePath());
            }
        }

        // now write the file into the modules dierectory in the exportpaht
        File discFile = new File(OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath() + File.separator + filename));
        try {

            // write the new file to disk
            OutputStream s = new FileOutputStream(discFile);
            s.write(filecontent);
            s.close();
        }
        catch(Exception e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage());
        }
        return filename;
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
    public byte[] getContent(
        CmsObject cms, 
        String templateFile, 
        String elementName,
        Hashtable parameters, 
        String templateSelector
    ) throws CmsException {               
        if(C_DEBUG && OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Getting content of element "
                            + ((elementName == null) ? "<root>" : elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Delected template section is: "
                            + ((templateSelector == null) ? "<default>" : templateSelector));
        }

        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();

        // get the parameters
        // String folder = (String)parameters.get("selectallfolders");
        String fileName = (String)parameters.get("filename");
        String existingFile = (String)parameters.get("existingfile");
        String action = (String)parameters.get("action");
        String allResources = (String)parameters.get("ALLRES");
        String allModules = (String)parameters.get("ALLMOD");
        String step = (String)parameters.get("step");
 
        // here we show the report updates when the threads are allready running.
        // This is used for import (action=showResult) and for export (action=showExportResult).
        if("showResult".equals(action)){
            // thread for import is started and we need to update the report information
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_DATABASE_THREAD);
            //still working?
            if(doTheWork.isAlive()){
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", "");
            }else{
                xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod"));
                xmlTemplateDocument.setData("autoUpdate","");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("database.label.importend"));
                session.removeValue(C_DATABASE_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");
        }else if("showExportResult".equals(action)){
            // thread for emport is started and we need to update the report information
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_DATABASE_THREAD);
            //still working?
            if(doTheWork.isAlive()){
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", "");
            }else{
                xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod"));
                xmlTemplateDocument.setData("autoUpdate","");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("database.label.exportend"));
                session.removeValue(C_DATABASE_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");
        }

        if(action == null) {
            // this is an initial request of the database administration page
            // generate datablocks for checkboxes in the HTML form
            if(!cms.getRequestContext().currentProject().isOnlineProject()) {
                xmlTemplateDocument.setData("nounchanged",
                        xmlTemplateDocument.getProcessedDataValue("nounchangedbox", this, parameters));
            }
            if(cms.isAdmin()) {
                xmlTemplateDocument.setData("userdata",
                        xmlTemplateDocument.getProcessedDataValue("userdatabox", this, parameters));
            }
            xmlTemplateDocument.setData("moduleselect", this.getModuleSelectbox(cms, xmlTemplateDocument));
        }

        try {
            if("export".equals(action)) {
                // export the VFS 
                Vector resourceNames = parseResources(allResources);
                String[] exportPaths = new String[resourceNames.size()];
                for(int i = 0;i < resourceNames.size();i++) {
                    // modify the foldername if nescessary (the root folder is always given
                    // as a nice name)
                    if(lang.getLanguageValue("title.rootfolder").equals(resourceNames.elementAt(i))) {
                        resourceNames.setElementAt("/", i);
                    }
                    exportPaths[i] = (String)resourceNames.elementAt(i);
                }
                
                // default content age is 0 (Unix 1970 time, this should export all resources)
                long contentAge = 0;                
                String contentAgePara = (String)parameters.get("contentage");
                if (contentAgePara != null && (! "".equals(contentAgePara)) && (! "(none)".equals(contentAgePara))) {
                    try {
                        contentAge = Long.parseLong(contentAgePara);
                    } catch (NumberFormatException numEx) {
                        // in case a invalid number was returned, use 0 (ie. export all files)
                        contentAge = 0;
                    }
                }
                
                boolean excludeSystem = false;
                if (parameters.get("nosystem") != null) {
                    excludeSystem = true;
                }
                boolean excludeUnchanged = false;
                if (parameters.get("nounchanged") != null) {
                    excludeUnchanged = true;
                }
                boolean exportUserdata = false;
                if (parameters.get("userdata") != null) {
                    exportUserdata = true;
                }
                // start the thread for: export
                A_CmsReportThread doExport = new CmsDatabaseExportThread(cms, 
                    OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath() + File.separator + fileName),                        
                    exportPaths, excludeSystem, excludeUnchanged, exportUserdata, contentAge);
                    
                doExport.start();
                session.putValue(C_DATABASE_THREAD, doExport);
                xmlTemplateDocument.setData("time", "10");
                xmlTemplateDocument.setData("contentage", "" + contentAge);
                xmlTemplateDocument.setData("currenttime", "" + System.currentTimeMillis());
                templateSelector = "showresult";

            } else if("exportmoduledata".equals(action)) {
                // export the COS and the COS data
                Vector channelNames = parseResources(allResources);
                String[] exportChannels = new String[channelNames.size()];
                for (int i = 0;i < channelNames.size();i++) {
                    // modify the foldername if nescessary (the root folder is always given
                    // as a nice name)
                    if(lang.getLanguageValue("title.rootfolder").equals(channelNames.elementAt(i))) {
                        channelNames.setElementAt("/", i);
                    }
                    exportChannels[i] = (String)channelNames.elementAt(i);
                }
                
                // get the selected modulenames
                Vector moduleNames = parseResources(allModules);
                String[] exportModules = new String[moduleNames.size()];
                for (int i = 0;i < moduleNames.size();i++) {
                    exportModules[i] = (String)moduleNames.elementAt(i);
                }
                
                // start the thread for: exportmodules
                A_CmsReportThread doExport = new CmsDatabaseExportThread(cms, 
                    OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath() + File.separator + fileName),
                    exportChannels, exportModules);
                            
                doExport.start();
                session.putValue(C_DATABASE_THREAD, doExport);
                xmlTemplateDocument.setData("time", "10");
                xmlTemplateDocument.setData("currenttime", "" + System.currentTimeMillis());                
                templateSelector = "showresult";

            } else if("import".equals(action)) {
                // look for the step
                if ("local".equals(step) || "server".equals(step)){
                    templateSelector = step;
                } else if("localupload".equals(step)){
                    // get the filename and set step to 'go'
                    existingFile = copyFileToServer(cms, session);
                    step = "go";
                }
                if ("go".equals(step) ){
                    // start the thread for: import
                    A_CmsReportThread doImport = new CmsDatabaseImportThread(cms,
                        OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(cms.readPackagePath() + File.separator + existingFile));                   
                    doImport.start();
                    session.putValue(C_DATABASE_THREAD, doImport);
                    xmlTemplateDocument.setData("time", "10");
                    templateSelector = "showresult";
                }
            }
        } catch (CmsException exc) {
            xmlTemplateDocument.setData("details", CmsException.getStackTraceAsString(exc));
            templateSelector = "error";
        }

        // now load the template file and start the processing
        return startProcessing (cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * Gets all export-files from the export-path.<p>
     * 
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.<p>
     * 
     * <code>names</code> will contain language specific view descriptions
     * and <code>values</code> will contain the correspondig URL for each
     * of these views after returning from this method.<p>
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current filter view in the vectors.
     * @throws CmsException
     */
    public Integer getExportFiles(
        CmsObject cms, 
        CmsXmlLanguageFile lang, 
        Vector values, 
        Vector names, 
        Hashtable parameters 
    ) throws CmsException {
        // get the systems-exportpath
        String exportpath = cms.readPackagePath();
        exportpath = OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(exportpath);
        File folder = new File(exportpath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // get a list of all files
        String[] list = folder.list();        
        for (int i = 0; i < list.length; i++) {
            try {
                File diskFile = new File(exportpath, list[i]);
                // check if it is a file
                if (diskFile.isFile() && diskFile.getName().endsWith(".zip")) {
                    values.addElement(list[i]);
                    names.addElement(list[i]);
                } else if (diskFile.isDirectory() 
                    && !diskFile.getName().equalsIgnoreCase("modules") 
                    && ((new File(diskFile + File.separator + "manifest.xml")).exists())) {
                        values.addElement(list[i] + "/");
                        names.addElement(list[i]);
                }
            } catch (Throwable t) {
                // ignore and continue
            }         
        }
        return new Integer(0);
    }

    /**
     * Indicates that the results of this class are not cacheable.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <code>false</code>
     */
    public boolean isCacheable(
        CmsObject cms, 
        String templateFile, 
        String elementName, 
        Hashtable parameters, 
        String templateSelector
    ) {
        return false;
    }

    /**
     * Gets all exportable modules for a select box.<p>
     * 
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.<p>
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @throws CmsException
     */
    public int getModules(
        CmsObject cms, 
        CmsXmlLanguageFile lang, 
        Vector names, 
        Vector values, 
        Hashtable parameters
    ) throws CmsException {        
        // get all exportable modules
        Hashtable modules = new Hashtable();
        cms.getRegistry().getModuleExportables(modules);
        Enumeration keys = modules.keys();
        // fill the names and values
        while (keys.hasMoreElements()) {
            String name = (String)keys.nextElement();
            String value = (String)modules.get(name);
            names.addElement(name);
            values.addElement(value);
        }
        return 0;
    }

    /**
     * Gets all exportable modules for a select box.<p>
     * 
     * The method returns a string with option tags that contains the module information
     * to be used for building a select box.<p>
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param template The current template
     * @return String with the modules optiontags.
     * @throws CmsException
     */
    public String getModuleSelectbox(
        CmsObject cms, 
        CmsXmlWpTemplateFile template
    ) throws CmsException {        
        StringBuffer selectBox = new StringBuffer();
        if (template.hasData("selectoption")) {
            // get all exportable modules
            Hashtable modules = new Hashtable();
            cms.getRegistry().getModuleExportables(modules);
            Enumeration keys = modules.keys();
            // fill the names and values
            while (keys.hasMoreElements()) {
                String name = (String)keys.nextElement();
                String value = (String)modules.get(name);
                template.setData("name", name);
                template.setData("value", value);
                try {
                    selectBox.append(template.getProcessedDataValue("selectoption", this));
                } catch (Exception e) {
                    // do not throw exception because selectbox might not exist
                }
            }
        }
        return selectBox.toString();
    }

    /** 
     * Parse the string which holds all resources.<p>
     *
     * @param resources containts the full pathnames of all the resources, separated by semicolons
     * @return A vector with the same resources
     */
    private Vector parseResources(
        String resources
    ) {                
        Vector ret = new Vector();
        if (resources != null) {
            StringTokenizer resTokenizer = new StringTokenizer(resources, ";");
            while (resTokenizer.hasMoreElements()) {
                String path = (String)resTokenizer.nextElement();
                ret.addElement(path);
            }
        }
        return ret;
    }
}

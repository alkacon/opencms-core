/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourceFolder.java,v $
* Date   : $Date: 2004/02/09 14:16:35 $
* Version: $Revision: 1.55 $
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRegistry;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypeFolder;
import com.opencms.file.CmsResourceTypeImage;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying the new resource screen for a new folder
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.55 $ $Date: 2004/02/09 14:16:35 $
 */

public class CmsNewResourceFolder extends CmsWorkplaceDefault {

    final static String C_SESSIONHEADER = "CmsNewResourceFolder_";

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the new resource page template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The new folder template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearry containing the processed data of the template.
     * @throws Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

        // the template to be displayed
        String template = null;
        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsRegistry registry = cms.getRegistry();
        boolean extendedNavigation = "on".equals(registry.getSystemValue("extendedNavigation"));

        // clear the session on first call
        String initial = (String) parameters.get("initial");
        if (initial != null) {
            clearSession(session);
            xmlTemplateDocument.setData("name", "");
            xmlTemplateDocument.setData("title", "");
        }
        xmlTemplateDocument.setData("navtext", "");
        xmlTemplateDocument.setData("doOnload", "");

        //get the current filelist
        // String currentFilelist = (String)session.getValue(C_PARA_FILELIST);
        String currentFilelist = CmsWorkplaceAction.getCurrentFolder(cms);
        if (currentFilelist == null) {
            currentFilelist = cms.readAbsolutePath(cms.rootFolder());
        }

        // set the title
        if ((extendedNavigation) && (cms.readAbsolutePath(cms.rootFolder()).equals(currentFilelist))) {
            xmlTemplateDocument.setData("frameTitle", lang.getLanguageValue("label.ebtitle"));
        } else {
            xmlTemplateDocument.setData("frameTitle", lang.getLanguageValue("title.newfolder"));
        }

        // set the right endbutton lable
        if (extendedNavigation) {
            xmlTemplateDocument.setData("endbutton", lang.getLanguageValue("button.nextscreen"));
        } else {
            xmlTemplateDocument.setData("endbutton", lang.getLanguageValue("button.endwizard"));
        }

        // get request parameters
        String newFolder = (String) parameters.get(C_PARA_NEWFOLDER);
        String title = CmsEncoder.redecodeUriComponent((String) parameters.get(C_PARA_TITLE));
        String navtitle = CmsEncoder.redecodeUriComponent((String) parameters.get(C_PARA_NAVTEXT));
        String navpos = (String) parameters.get(C_PARA_NAVPOS);

        // get the current phase of this wizard
        String step = (String) parameters.get("step");
        if (step != null) {
            // test if we come from the extended Nav errorpages
            if (step.equals("extNavError")) {
                step = "1";
                newFolder = (String) session.getValue(C_SESSIONHEADER + C_PARA_NEWFOLDER);
                title = (String) session.getValue(C_SESSIONHEADER + C_PARA_TITLE);
                navtitle = (String) session.getValue(C_SESSIONHEADER + C_PARA_NAVTEXT);
                navpos = (String) session.getValue(C_SESSIONHEADER + C_PARA_NAVPOS);
            }
            if (step.equals("1")) {
                // store all data in session for ext nav or in case of an error
                session.putValue(C_SESSIONHEADER + C_PARA_NEWFOLDER, newFolder);
                session.putValue(C_SESSIONHEADER + C_PARA_TITLE, title);
                if (navtitle != null) {
                    session.putValue(C_SESSIONHEADER + C_PARA_NAVTEXT, navtitle);
                    session.putValue(C_SESSIONHEADER + C_PARA_NAVPOS, navpos);
                } else {
                    session.removeValue(C_SESSIONHEADER + C_PARA_NAVTEXT);
                    session.removeValue(C_SESSIONHEADER + C_PARA_NAVPOS);
                }
                if (extendedNavigation) {
                    // display the extended navigation
                    // look if there are allready values to display (if user used the backbutton)
                    Hashtable preprops = (Hashtable) session.getValue(C_SESSIONHEADER + "extendedProperties");
                    //set the default values or the before selected values in the template
                    setNavDefault(xmlTemplateDocument, preprops, newFolder);
                    // now we decide if we should show the sectionLogo entry (only in root)
                    if (cms.readAbsolutePath(cms.rootFolder()).equals(currentFilelist)) {
                        //display it
                        xmlTemplateDocument.setData("displaySektLogo", xmlTemplateDocument.getProcessedDataValue("SektLogo", this));
                        xmlTemplateDocument.setData("displayFolderLogo", xmlTemplateDocument.getProcessedDataValue("folderLogo", this));
                    } else {
                        xmlTemplateDocument.setData("displayFolderLogo", xmlTemplateDocument.getProcessedDataValue("folderLogo", this));
                        xmlTemplateDocument.setData("displaySektLogo", "");
                    }
                    template = "extendedNav";
                } else {
                    try {
                        // create the folder
                        CmsFolder folder = (CmsFolder) cms.createResource(currentFilelist, newFolder, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
                        //cms.lockResource(cms.readPath(folder));
                        cms.writeProperty(cms.readAbsolutePath(folder), C_PROPERTY_TITLE, title);
                        // create the folder in content bodys
                        // TODO: We don't need this anymore

                        if (CmsResourceTypeFolder.C_BODY_MIRROR) {
                            try {
                                CmsFolder bodyFolder = (CmsFolder) cms.createResource(C_VFS_PATH_BODIES.substring(0, C_VFS_PATH_BODIES.length() - 1) + currentFilelist, newFolder, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
                                //cms.lockResource(cms.readPath(bodyFolder));
                                cms.writeProperty(cms.readAbsolutePath(bodyFolder), C_PROPERTY_TITLE, title);
                            } catch (CmsException ce) {
                                //throw ce;
                            }
                        }
                        // now check if navigation informations have to be added to the new page.
                        if (navtitle != null) {
                            cms.writeProperty(cms.readAbsolutePath(folder), C_PROPERTY_NAVTEXT, navtitle);
                            // update the navposition.
                            if (navpos != null) {
                                updateNavPos(cms, folder, navpos);
                                // ednfal(20.06.01) new projectmechanism: do not unlock the new folder
                                // try to unlock the folder, ignore the Exception (the parent folder is looked)
                                //try{
                                //    cms.unlockResource(cms.readPath(folder));
                                //}catch(CmsException e){
                                //}
                                // prepare to call the dialog for creating the index.html page
                                // session.putValue(C_PARA_FILELIST, cms.readAbsolutePath(folder));
                                CmsWorkplaceAction.setCurrentFolder(cms, cms.readAbsolutePath(folder));
                                xmlTemplateDocument.setData("indexlocation", cms.readAbsolutePath(folder));
                            }
                            template = "update2";
                        } else {
                            template = "update";
                        }
                        // all done, now we have to clean up our mess
                        clearSession(session);
                    } catch (CmsException ex) {
                        xmlTemplateDocument.setData("details", CmsException.getStackTraceAsString(ex));
                        return startProcessing(cms, xmlTemplateDocument, "", parameters, "error_system");
                    }
                }
            } else if ("2".equals(step)) {
                // get all the usefull parameters and the session stuff
                String back = (String) parameters.get("back");
                newFolder = (String) session.getValue(C_SESSIONHEADER + C_PARA_NEWFOLDER);
                title = (String) session.getValue(C_SESSIONHEADER + C_PARA_TITLE);
                navtitle = (String) session.getValue(C_SESSIONHEADER + C_PARA_NAVTEXT);
                navpos = (String) session.getValue(C_SESSIONHEADER + C_PARA_NAVPOS);
                // generate a Hashtable for all properties for createFolder and for saving all data in the session
                Hashtable allProperties = new Hashtable();
                insertProperty(allProperties, C_PROPERTY_TITLE, title);
                insertProperty(allProperties, C_PROPERTY_NAVTEXT, navtitle);
                fillProperties(allProperties, parameters);
                session.putValue(C_SESSIONHEADER + "extendedProperties", allProperties);
                // check the backbutton
                if (back == null) {
                    // fast forward
                    try {
                        // first check if everything is ok
                        boolean checkFileLogo = !cms.readAbsolutePath(cms.rootFolder()).equals(currentFilelist);
                        int propError = checkProperties(cms, allProperties, checkFileLogo);
                        if (propError > 0) {
                            // error by user get the correct error template
                            template = "error_" + propError;
                            return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
                        }
                        // create the folder
                        CmsFolder folder = (CmsFolder) cms.createResource(currentFilelist, newFolder, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
                        try {
                            cms.lockResource(cms.readAbsolutePath(folder));
                        } catch (CmsException e) {
                            //folder is already locked, do nothing
                        }
                        cms.writeProperties(cms.readAbsolutePath(folder), allProperties);
                        // create the folder in content bodys
                        try {
                            CmsFolder bodyFolder = (CmsFolder) cms.createResource(C_VFS_PATH_BODIES.substring(0, C_VFS_PATH_BODIES.length() - 1) + currentFilelist, newFolder, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
                            cms.lockResource(cms.readAbsolutePath(bodyFolder));
                            cms.writeProperty(cms.readAbsolutePath(bodyFolder), C_PROPERTY_TITLE, title);
                        } catch (CmsException ce) {
                            //throw ce;
                        }
                        // update the navposition.
                        if ((navtitle != null) && (navpos != null)) {
                            updateNavPos(cms, folder, navpos);
                            // ednfal(20.06.01) new projectmechanism: do not unlock the new folder
                            // try to unlock the folder, ignore the Exception (the parent folder is looked)
                            //try{
                            //    cms.unlockResource(cms.readPath(folder));
                            //}catch(CmsException e){
                            //}
                            // prepare to call the new Page dialog
                            xmlTemplateDocument.setData("indexlocation", cms.readAbsolutePath(folder));
                            // session.putValue(C_PARA_FILELIST, cms.readAbsolutePath(folder));
                            CmsWorkplaceAction.setCurrentFolder(cms, cms.readAbsolutePath(folder));
                            template = "update2";
                        } else {
                            template = "update";
                        }
                        // we dont need our session entrys anymore
                        clearSession(session);
                    } catch (CmsException ex) {
                        xmlTemplateDocument.setData("details", CmsException.getStackTraceAsString(ex));
                        return startProcessing(cms, xmlTemplateDocument, "", parameters, "error_system");
                    }
                } else {
                    // user pressed backbutton. show the first template again
                    xmlTemplateDocument.setData("name", newFolder);
                    xmlTemplateDocument.setData("title", CmsEncoder.escapeXml(title));
                    if (navtitle != null) {
                        xmlTemplateDocument.setData("navtext", navtitle);
                    } else {
                        // deaktivate the linklayer
                        xmlTemplateDocument.setData("doOnload", "checkInTheBox();");
                    }
                    template = null;
                }
            } else if ("fromerror".equalsIgnoreCase(step)) {
                // error while creating the folder go to firs page and set the stored parameter
                template = null;
                xmlTemplateDocument.setData("name", (String) session.getValue(C_SESSIONHEADER + C_PARA_NEWFOLDER));
                xmlTemplateDocument.setData("title", CmsEncoder.escapeXml((String) session.getValue(C_SESSIONHEADER + C_PARA_TITLE)));
                navtitle = (String) session.getValue(C_SESSIONHEADER + C_PARA_NAVTEXT);
                if (navtitle != null) {
                    xmlTemplateDocument.setData("navtext", navtitle);
                } else {
                    // deaktivate the linklayer
                    xmlTemplateDocument.setData("doOnload", "checkInTheBox();");
                }
            }
        }
        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }

    /**
     * Gets all required navigation information from the files and subfolders of a folder.
     * A file list of all files and folder is created, for all those resources, the navigation
     * property is read. The list is sorted by their navigation position.
     * @param cms The CmsObject.
     * @return Hashtable including three arrays of strings containing the filenames,
     * nicenames and navigation positions.
     * @throws Throws CmsException if something goes wrong.
     */

    private Hashtable getNavData(CmsObject cms) throws CmsException {
        // I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        String[] filenames;
        String[] nicenames;
        String[] positions;
        Hashtable storage = new Hashtable();
        CmsFolder folder = null;
        CmsFile file = null;
        String nicename = null;
        String currentFilelist = null;
        int count = 1;
        float max = 0;

        // get the current folder
        // currentFilelist = (String)session.getValue(C_PARA_FILELIST);
        currentFilelist = CmsWorkplaceAction.getCurrentFolder(cms);
        if (currentFilelist == null) {
            currentFilelist = cms.readAbsolutePath(cms.rootFolder());
        }

        // get all files and folders in the current filelist.
        List files = cms.getFilesInFolder(currentFilelist);
        List folders = cms.getSubFolders(currentFilelist);

        // combine folder and file vector
        Vector filefolders = new Vector();
        Iterator enum = folders.iterator();
        while (enum.hasNext()) {
            folder = (CmsFolder) enum.next();
            filefolders.addElement(folder);
        }
        enum = files.iterator();
        while (enum.hasNext()) {
            file = (CmsFile) enum.next();
            filefolders.addElement(file);
        }
        if (filefolders.size() > 0) {

            // Create some arrays to store filename, nicename and position for the
            // nav in there. The dimension of this arrays is set to the number of
            // found files and folders plus two more entrys for the first and last
            // element.
            filenames = new String[filefolders.size() + 2];
            nicenames = new String[filefolders.size() + 2];
            positions = new String[filefolders.size() + 2];

            //now check files and folders that are not deleted and include navigation
            // information
            enum = filefolders.iterator();
            while (enum.hasNext()) {
                CmsResource res = (CmsResource) enum.next();

                // check if the resource is not marked as deleted
                if (res.getState() != C_STATE_DELETED) {
                    String navpos = cms.readProperty(cms.readAbsolutePath(res), C_PROPERTY_NAVPOS);

                    // check if there is a navpos for this file/folder
                    if (navpos != null) {
                        nicename = cms.readProperty(cms.readAbsolutePath(res), C_PROPERTY_NAVTEXT);
                        if (nicename == null) {
                            nicename = res.getName();
                        }

                        // add this file/folder to the storage.
                        filenames[count] = cms.readAbsolutePath(res);
                        nicenames[count] = nicename;
                        positions[count] = navpos;
                        if (new Float(navpos).floatValue() > max) {
                            max = new Float(navpos).floatValue();
                        }
                        count++;
                    }
                }
            }
        } else {
            filenames = new String[2];
            nicenames = new String[2];
            positions = new String[2];
        }

        // now add the first and last value
        filenames[0] = "FIRSTENTRY";
        nicenames[0] = lang.getLanguageValue("input.firstelement");
        positions[0] = "0";
        filenames[count] = "LASTENTRY";
        nicenames[count] = lang.getLanguageValue("input.lastelement");
        positions[count] = new Float(max + 1).toString();

        // finally sort the nav information.
        sort(cms, filenames, nicenames, positions, count);

        // put all arrays into a hashtable to return them to the calling method.
        storage.put("FILENAMES", filenames);
        storage.put("NICENAMES", nicenames);
        storage.put("POSITIONS", positions);
        storage.put("COUNT", new Integer(count));
        return storage;
    }

    /**
     * Gets the files displayed in the navigation select box.
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources.
     * @param values The links that are connected with each resource.
     * @param parameters Hashtable of parameters (not used yet).
     * @return The vectors names and values are filled with data for building the navigation.
     * @throws Throws CmsException if something goes wrong.
     */

    public Integer getNavPos(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException {

        I_CmsSession session = cms.getRequestContext().getSession(true);
        String preselect = (String) session.getValue(C_SESSIONHEADER + C_PARA_NAVPOS);
        int retValue = -1;
        // get the nav information
        Hashtable storage = getNavData(cms);
        if (storage.size() > 0) {
            String[] nicenames = (String[]) storage.get("NICENAMES");
            int count = ((Integer) storage.get("COUNT")).intValue();

            // finally fill the result vectors
            for (int i = 0; i <= count; i++) {
                names.addElement(CmsEncoder.escapeHtml(nicenames[i]));
                values.addElement(CmsEncoder.escapeHtml(nicenames[i]));
                if ((preselect != null) && (preselect.equals(nicenames[i]))) {
                    retValue = values.size() - 1;
                }
            }
        } else {
            values = new Vector();
        }
        if (retValue == -1) {
            return new Integer(values.size() - 1);
        } else {
            return new Integer(retValue);
        }
    }

    /**
     * Gets the templates displayed in the template select box.
     * @param cms The CmsObject.
     * @param names Will be filled with the display names of the found templates.
     * @param values Will be filled with the file names of the found templates.
     * @param parameters Hashtable of parameters (not used yet).
     * @return The return value is always 0
     * @throws Throws CmsException if something goes wrong.
     */
    public Integer getTemplates(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException {

        // Gather templates from the VFS
        CmsHelperMastertemplates.getTemplateElements(cms, I_CmsWpConstants.C_VFS_DIR_TEMPLATES, names, values);

        // Always return 0
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

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Sorts a set of three String arrays containing navigation information depending on
     * their navigation positions.
     * @param cms Cms Object for accessign files.
     * @param filenames Array of filenames
     * @param nicenames Array of well formed navigation names
     * @param positions Array of navpostions
     */

    private void sort(CmsObject cms, String[] filenames, String[] nicenames, String[] positions, int max) {

        // Sorting algorithm
        // This method uses an bubble sort, so replace this with something more
        // efficient
        for (int i = max - 1; i > 1; i--) {
            for (int j = 1; j < i; j++) {
                float a = new Float(positions[j]).floatValue();
                float b = new Float(positions[j + 1]).floatValue();
                if (a > b) {
                    String tempfilename = filenames[j];
                    String tempnicename = nicenames[j];
                    String tempposition = positions[j];
                    filenames[j] = filenames[j + 1];
                    nicenames[j] = nicenames[j + 1];
                    positions[j] = positions[j + 1];
                    filenames[j + 1] = tempfilename;
                    nicenames[j + 1] = tempnicename;
                    positions[j + 1] = tempposition;
                }
            }
        }
    }

    /**
     * Updates the navigation position of all resources in the actual folder.
     * @param cms The CmsObject.
     * @param newfile The new file added to the nav.
     * @param navpos The file after which the new entry is sorted.
     */

    private void updateNavPos(CmsObject cms, CmsFolder newfolder, String newpos) throws CmsException {
        float newPos = 0;

        // get the nav information
        Hashtable storage = getNavData(cms);
        if (storage.size() > 0) {
            String[] nicenames = (String[]) storage.get("NICENAMES");
            String[] positions = (String[]) storage.get("POSITIONS");
            int count = ((Integer) storage.get("COUNT")).intValue();

            // now find the file after which the new file is sorted
            int pos = 0;
            for (int i = 0; i < nicenames.length; i++) {
                if (newpos.equals(nicenames[i])) {
                    pos = i;
                }
            }
            if (pos < count) {
                float low = new Float(positions[pos]).floatValue();
                float high = new Float(positions[pos + 1]).floatValue();
                newPos = (high + low) / 2;
            } else {
                newPos = new Float(positions[pos]).floatValue() + 1;
            }
        } else {
            newPos = 1;
        }
        cms.writeProperty(cms.readAbsolutePath(newfolder), C_PROPERTY_NAVPOS, new Float(newPos).toString());
    }

    /**
     * removes all session parameters used by this class.
     * @param session The session.
     */
    private void clearSession(I_CmsSession session) {
        session.removeValue(C_SESSIONHEADER + C_PARA_NEWFOLDER);
        session.removeValue(C_SESSIONHEADER + C_PARA_TITLE);
        session.removeValue(C_SESSIONHEADER + C_PARA_NAVTEXT);
        session.removeValue(C_SESSIONHEADER + C_PARA_NAVPOS);
        session.removeValue(C_SESSIONHEADER + "extendedProperties");
    }
    /**
     * Enters a value in a Hashtable if it is not null.
     * @param properties The Hashtable to be filled.
     * @param key The key for the entry.
     * @param value The entryvalue.
     */
    private void insertProperty(Hashtable properties, String key, String value) {
        if (value != null) {
            properties.put(key, value);
        }
    }

    /**
     * Fills the needed parameters in the properties hashtable.
     * @param properties The Hashtable to be filled.
     * @param parameters From here we get the values.
     */
    private void fillProperties(Hashtable properties, Hashtable parameters) {
        insertProperty(properties, "Verzeichnislogo_img", (String) parameters.get("flogopic"));
        insertProperty(properties, "Verzeichnislogo_target", (String) parameters.get("flogolink"));
        insertProperty(properties, "Hauptlogo_img", (String) parameters.get("mlogopic"));
        insertProperty(properties, "Hauptlogo_target", (String) parameters.get("mlogolink"));
        insertProperty(properties, "Sektionslogo_img", (String) parameters.get("slogopic"));
        insertProperty(properties, "Sektionslogo_target", (String) parameters.get("slogolink"));
        //set properties for FreierLink only when the entry is not empty
        if (!((String) parameters.get("statnav")).equals("") && !((String) parameters.get("statlink")).equals("")) {
            insertProperty(properties, "FreierLink_navtext", (String) parameters.get("statnav"));
            insertProperty(properties, "FreierLink_target", (String) parameters.get("statlink"));
        }
        insertProperty(properties, "Templ_bgcolor", (String) parameters.get("bcoldyn"));
        insertProperty(properties, "Templ_fontcolor", (String) parameters.get("tcoldyn"));
        //insertProperty(properties, "FreierLink_bgcolor", (String)parameters.get("bcollink"));
        insertProperty(properties, "Templ_fontcolor_hover", (String) parameters.get("tcollink"));
        insertProperty(properties, "Templ_bordercolor", (String) parameters.get("bcolemail"));
        //insertProperty(properties, "Email_fontcolor", (String)parameters.get("tcolemail"));
        insertProperty(properties, "Templ_leadcolor", (String) parameters.get("bcolsearch"));
        //insertProperty(properties, "Suche_fontcolor", (String)parameters.get("tcolsearch"));
    }

    /**
     * Checks if all properties are correct. if not it returns the nummber so the
     * rigth errortemplate can be displayed.
     *
     * @param cms The cmsObject
     * @param properties The Hashtable with the properties to be checked.
     * @param checkFolderLogo Indicates if the FolderLogo should be checked too.
     * @return int the error nummber.
     */
    private int checkProperties(CmsObject cms, Hashtable properties, boolean checkFolderLogo) {

        // serch for error 1 (the Hauptlogo must be a OpenCms resource from type image)
        try {
            String logo = (String) properties.get("Hauptlogo_img");
            if (logo == null || "".equals(logo)) {
                properties.remove("Hauptlogo_img");
            } else {
                CmsResource pic = cms.readFileHeader(logo);
                if (!(pic.getType() == CmsResourceTypeImage.C_RESOURCE_TYPE_ID)) {
                    return 1;
                }
            }
        } catch (CmsException e) {
            return 1;
        }
        if (checkFolderLogo) {
            // serch for error 1 (the Folderlogo must be a OpenCms resource from type image)
            try {
                String logo = (String) properties.get("Verzeichnislogo_img");
                if (logo == null || "".equals(logo)) {
                    properties.remove("Verzeichnislogo_img");
                } else {
                    CmsResource pic = cms.readFileHeader(logo);
                    if (!(pic.getType() == CmsResourceTypeImage.C_RESOURCE_TYPE_ID)) {
                        return 1;
                    }
                }
            } catch (CmsException e) {
                return 1;
            }
        }

        // serch for error 2 (FreierLink_navtext and FreierLink_target: both or none)
        String text = (String) properties.get("FreierLink_navtext");
        String target = (String) properties.get("FreierLink_target");
        if ((text == null || "".equals(text)) != (target == null || "".equals(target))) {
            return 2;
        }

        // serch for error3 ( the colors must have the form #xxxxxx with 0<= x <= F )
        String[] propsToCheck = { "Templ_bgcolor", "Templ_fontcolor", "Templ_fontcolor_hover", "Templ_bordercolor", "Templ_leadcolor" };
        for (int i = 0; i < propsToCheck.length; i++) {
            String test = (String) properties.get(propsToCheck[i]);
            if ((test.length() != 7) || test.charAt(0) != '#') {
                return 3;
            }
            String valid = "0123456789ABCDEF";
            for (int j = 1; j < 7; j++) {
                if (valid.indexOf(test.charAt(j)) < 0) {
                    return 3;
                }
            }
        }
        // no errors found
        return 0;
    }

    /**
     * sets the given or the default (when parameters is null) values in the
     * template for the extended navigation
     *
     * @param xmlTemplateDoc The xmlTemplateDoc to be filled.
     * @param parameters From here we get the values.
     * @param folder The name of the folder (needed for the Sectionlogo)
     */
    private void setNavDefault(CmsXmlWpTemplateFile xmlTemplateDoc, Hashtable parameters, String name) {

        if (parameters == null) {
            // set the default values for the first time it is shown
            xmlTemplateDoc.setData("flogopic", "");
            xmlTemplateDoc.setData("flogolink", "");
            xmlTemplateDoc.setData("mlogopic", "");
            xmlTemplateDoc.setData("mlogolink", "/index.html");
            xmlTemplateDoc.setData("slogopic", "");
            xmlTemplateDoc.setData("slogolink", "/" + name + "/index.html");
            xmlTemplateDoc.setData("statnav", "AKTUELL");
            xmlTemplateDoc.setData("statlink", "");
            xmlTemplateDoc.setData("bcoldyn", "#DDDDDD");
            xmlTemplateDoc.setData("tcoldyn", "#333333");
            //xmlTemplateDoc.setData("bcollink", "#CCCCCC");
            xmlTemplateDoc.setData("tcollink", "#FF3300");
            xmlTemplateDoc.setData("bcolemail", "#000099");
            //xmlTemplateDoc.setData("tcolemail", "#000099");
            xmlTemplateDoc.setData("bcolsearch", "#999999");
            //xmlTemplateDoc.setData("tcolsearch", "#000099");
        } else {
            // set the values which the user entered before he pressed the backbutton
            xmlTemplateDoc.setData("flogopic", getStringValue((String) parameters.get("Verzeichnislogo_img")));
            xmlTemplateDoc.setData("flogolink", getStringValue((String) parameters.get("Verzeichnislogo_target")));
            xmlTemplateDoc.setData("mlogopic", getStringValue((String) parameters.get("Hauptlogo_img")));
            xmlTemplateDoc.setData("mlogolink", getStringValue((String) parameters.get("Hauptlogo_target")));
            xmlTemplateDoc.setData("slogopic", getStringValue((String) parameters.get("Sektionslogo_img")));
            xmlTemplateDoc.setData("slogolink", getStringValue((String) parameters.get("Sektionslogo_target")));
            xmlTemplateDoc.setData("statnav", getStringValue((String) parameters.get("FreierLink_navtext")));
            xmlTemplateDoc.setData("statlink", getStringValue((String) parameters.get("FreierLink_target")));
            xmlTemplateDoc.setData("bcoldyn", getStringValue((String) parameters.get("Templ_bgcolor")));
            xmlTemplateDoc.setData("tcoldyn", getStringValue((String) parameters.get("Templ_fontcolor")));
            //xmlTemplateDoc.setData("bcollink", getStringValue((String)parameters.get("FreierLink_bgcolor")));
            xmlTemplateDoc.setData("tcollink", getStringValue((String) parameters.get("Templ_fontcolor_hover")));
            xmlTemplateDoc.setData("bcolemail", getStringValue((String) parameters.get("Templ_bordercolor")));
            //xmlTemplateDoc.setData("tcolemail", getStringValue((String)parameters.get("Email_fontcolor")));
            xmlTemplateDoc.setData("bcolsearch", getStringValue((String) parameters.get("Templ_leadcolor")));
            //xmlTemplateDoc.setData("tcolsearch", getStringValue((String)parameters.get("Suche_fontcolor")));
        }
    }

    /**
     * returns the String or "" if it is null.
     * @return java.lang.String
     * @param param java.lang.String
     */
    private String getStringValue(String param) {
        if (param == null) {
            return "";
        }
        return param;
    }
}

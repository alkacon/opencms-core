
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourceFolder.java,v $
* Date   : $Date: 2001/03/16 15:01:55 $
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

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;

/**
 * Template class for displaying the new resource screen for a new folder
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.18 $ $Date: 2001/03/16 15:01:55 $
 */

public class CmsNewResourceFolder extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

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
     * @exception Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        // the template to be displayed
        String template = null;
        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // clear the session on first call
        String initial = (String)parameters.get("initial");
        if (initial != null){
            clearSession(session);
            xmlTemplateDocument.setData("name", "");
            xmlTemplateDocument.setData("title", "");
        }
        xmlTemplateDocument.setData("navtext", "");
        xmlTemplateDocument.setData("doOnload", "");

        //get the current filelist
        String currentFilelist = (String)session.getValue(C_PARA_FILELIST);
        if(currentFilelist == null) {
            currentFilelist = cms.rootFolder().getAbsolutePath();
        }

        // get request parameters
        String newFolder = (String)parameters.get(C_PARA_NEWFOLDER);
        String title = (String)parameters.get(C_PARA_TITLE);
        String navtitle = (String)parameters.get(C_PARA_NAVTEXT);
        String navpos = (String)parameters.get(C_PARA_NAVPOS);

        // get the current phase of this wizard
        String step = (String)parameters.get("step");
        if(step != null) {
            if(step.equals("1")) {
// TODO: (true) here we must read in the resistry if ebk is activ
                if(true){
                    // display the extended navigation
                    session.putValue(C_SESSIONHEADER + C_PARA_NEWFOLDER, newFolder);
                    session.putValue(C_SESSIONHEADER + C_PARA_TITLE, title);
                    if(navtitle != null) {
                        session.putValue(C_SESSIONHEADER + C_PARA_NAVTEXT, navtitle);
                        session.putValue(C_SESSIONHEADER + C_PARA_NAVPOS, navpos);
                    }
                    // look if there are allready values to display (if user used the backbutton)
                    Hashtable preprops = (Hashtable)session.getValue(C_SESSIONHEADER + "extendedProperties");
                    //set the default values or the before selected values in the template
                    setNavDefault(xmlTemplateDocument, preprops, newFolder);
                    // now we decide if we should show the sectionLogo entry (only in root)
                    if (cms.rootFolder().getAbsolutePath().equals(currentFilelist)){
                        //display it
                        xmlTemplateDocument.setData("displaySektLogo", xmlTemplateDocument.getProcessedDataValue("SektLogo", this));
                    }else{
                        xmlTemplateDocument.setData("displaySektLogo", "");
                    }
                    template = "extendedNav";
                }else{
                    try {
                        // create the folder
                        CmsFolder folder = cms.createFolder(currentFilelist, newFolder);
                        cms.lockResource(folder.getAbsolutePath());
                        cms.writeProperty(folder.getAbsolutePath(), C_PROPERTY_TITLE, title);
                        // now check if navigation informations have to be added to the new page.
                        if(navtitle != null) {
                            cms.writeProperty(folder.getAbsolutePath(), C_PROPERTY_NAVTEXT, navtitle);
                            // update the navposition.
                            if(navpos != null) {
                                updateNavPos(cms, folder, navpos);
                            }
                        }
                        // all done, now we have to clean up our mess
                        clearSession(session);
                    }catch(CmsException ex) {
                        throw new CmsException("Error while creating new Folder" + ex.getMessage(), ex.getType(), ex);
                    }
                    // TODO: ErrorHandling
                    // now return to filelist
                    template = "update";
                }
            }else if("2".equals(step)){
                // get all the usefull parameters and the session stuff
                String back = (String)parameters.get("back");
                newFolder = (String)session.getValue(C_SESSIONHEADER + C_PARA_NEWFOLDER);
                title = (String)session.getValue(C_SESSIONHEADER + C_PARA_TITLE);
                navtitle = (String)session.getValue(C_SESSIONHEADER + C_PARA_NAVTEXT);
                navpos = (String)session.getValue(C_SESSIONHEADER + C_PARA_NAVPOS);
                // generate a Hashtable for all properties for createFolder and for saving all data in the session
                Hashtable allProperties = new Hashtable();
                insertProperty(allProperties, C_PROPERTY_TITLE, title);
                insertProperty(allProperties, C_PROPERTY_NAVTEXT, navtitle);
                fillProperties(allProperties, parameters);
                // check the backbutton
                if (back == null){
                    // fast forward
                    try {
                        // create the folder
                        CmsFolder folder = cms.createFolder(currentFilelist, newFolder);
                        cms.lockResource(folder.getAbsolutePath());
                        cms.writeProperties(folder.getAbsolutePath(), allProperties);
                        // update the navposition.
                        if ((navtitle != null) && (navpos != null)){
                            updateNavPos(cms, folder, navpos);
                        }
                        // we dont need our session entrys anymore
                        clearSession(session);
                    }catch(CmsException ex) {
                        throw new CmsException("Error while creating new Folder" + ex.getMessage(), ex.getType(), ex);
                    }
                    template = "update";
                }else{
                    // user pressed backbutton. show the first template again
                    xmlTemplateDocument.setData("name", newFolder);
                    xmlTemplateDocument.setData("title", title);
                    if (navtitle != null){
                        xmlTemplateDocument.setData("navtext", navtitle);
                    }else{
                        // deaktivate the linklayer
                        xmlTemplateDocument.setData("doOnload", "checkInTheBox();");
                    }
                    session.putValue(C_SESSIONHEADER + "extendedProperties", allProperties);
                    template = null;
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
     * @exception Throws CmsException if something goes wrong.
     */

    private Hashtable getNavData(CmsObject cms) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
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
        currentFilelist = (String)session.getValue(C_PARA_FILELIST);
        if(currentFilelist == null) {
            currentFilelist = cms.rootFolder().getAbsolutePath();
        }

        // get all files and folders in the current filelist.
        Vector files = cms.getFilesInFolder(currentFilelist);
        Vector folders = cms.getSubFolders(currentFilelist);

        // combine folder and file vector
        Vector filefolders = new Vector();
        Enumeration enum = folders.elements();
        while(enum.hasMoreElements()) {
            folder = (CmsFolder)enum.nextElement();
            filefolders.addElement(folder);
        }
        enum = files.elements();
        while(enum.hasMoreElements()) {
            file = (CmsFile)enum.nextElement();
            filefolders.addElement(file);
        }
        if(filefolders.size() > 0) {

            // Create some arrays to store filename, nicename and position for the
            // nav in there. The dimension of this arrays is set to the number of
            // found files and folders plus two more entrys for the first and last
            // element.
            filenames = new String[filefolders.size() + 2];
            nicenames = new String[filefolders.size() + 2];
            positions = new String[filefolders.size() + 2];

            //now check files and folders that are not deleted and include navigation
            // information
            enum = filefolders.elements();
            while(enum.hasMoreElements()) {
                CmsResource res = (CmsResource)enum.nextElement();

                // check if the resource is not marked as deleted
                if(res.getState() != C_STATE_DELETED) {
                    String navpos = cms.readProperty(res.getAbsolutePath(), C_PROPERTY_NAVPOS);

                    // check if there is a navpos for this file/folder
                    if(navpos != null) {
                        nicename = cms.readProperty(res.getAbsolutePath(), C_PROPERTY_NAVTEXT);
                        if(nicename == null) {
                            nicename = res.getName();
                        }

                        // add this file/folder to the storage.
                        filenames[count] = res.getAbsolutePath();
                        nicenames[count] = nicename;
                        positions[count] = navpos;
                        if(new Float(navpos).floatValue() > max) {
                            max = new Float(navpos).floatValue();
                        }
                        count++;
                    }
                }
            }
        }
        else {
            filenames = new String[2];
            nicenames = new String[2];
            positions = new String[2];
        }

        // now add the first and last value
        filenames[0] = "FIRSTENTRY";
        nicenames[0] = lang.getDataValue("input.firstelement");
        positions[0] = "0";
        filenames[count] = "LASTENTRY";
        nicenames[count] = lang.getDataValue("input.lastelement");
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
     * @returns The vectors names and values are filled with data for building the navigation.
     * @exception Throws CmsException if something goes wrong.
     */

    public Integer getNavPos(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        I_CmsSession session = cms.getRequestContext().getSession(true);
        String preselect = (String)session.getValue(C_SESSIONHEADER + C_PARA_NAVPOS);
        int retValue = -1;
       // get the nav information
        Hashtable storage = getNavData(cms);
        if(storage.size() > 0) {
            String[] nicenames = (String[])storage.get("NICENAMES");
            int count = ((Integer)storage.get("COUNT")).intValue();

            // finally fill the result vectors
            for(int i = 0;i <= count;i++) {
                names.addElement(nicenames[i]);
                values.addElement(nicenames[i]);
                if ((preselect != null) && (preselect.equals(nicenames[i]))){
                    retValue = values.size() -1;
                }
            }
        }
        else {
            values = new Vector();
        }
        if (retValue == -1){
            return new Integer(values.size() - 1);
        }else{
            return new Integer(retValue);
        }
    }

    /**
     * Gets the templates displayed in the template select box.
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources.
     * @param values The links that are connected with each resource.
     * @param parameters Hashtable of parameters (not used yet).
     * @returns The vectors names and values are filled with the information found in the
     * workplace.ini.
     * @exception Throws CmsException if something goes wrong.
     */

    public Integer getTemplates(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        //Vector files=cms.getFilesInFolder(C_CONTENTTEMPLATEPATH);
        Vector files = cms.getFilesInFolder(C_CONTENTTEMPLATEPATH);

        // get all module Templates
        Vector modules = new Vector();
        modules = cms.getSubFolders(C_MODULES_PATH);
        for(int i = 0;i < modules.size();i++) {
            Vector moduleTemplateFiles = new Vector();
            moduleTemplateFiles = cms.getFilesInFolder(((CmsFolder)modules.elementAt(i)).getAbsolutePath() + "templates/");
            for(int j = 0;j < moduleTemplateFiles.size();j++) {
                files.addElement(moduleTemplateFiles.elementAt(j));
            }
        }
        Enumeration enum = files.elements();
        while(enum.hasMoreElements()) {
            CmsFile file = (CmsFile)enum.nextElement();
            if(file.getState() != C_STATE_DELETED) {
                String nicename = cms.readProperty(file.getAbsolutePath(), C_PROPERTY_TITLE);
                if(nicename == null) {
                    nicename = file.getName();
                }
                names.addElement(nicename);
                values.addElement(file.getAbsolutePath());
            }
        }
        bubblesort(names, values);
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
        for(int i = max - 1;i > 1;i--) {
            for(int j = 1;j < i;j++) {
                float a = new Float(positions[j]).floatValue();
                float b = new Float(positions[j + 1]).floatValue();
                if(a > b) {
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
        if(storage.size() > 0) {
            String[] nicenames = (String[])storage.get("NICENAMES");
            String[] positions = (String[])storage.get("POSITIONS");
            int count = ((Integer)storage.get("COUNT")).intValue();

            // now find the file after which the new file is sorted
            int pos = 0;
            for(int i = 0;i < nicenames.length;i++) {
                if(newpos.equals((String)nicenames[i])) {
                    pos = i;
                }
            }
            if(pos < count) {
                float low = new Float(positions[pos]).floatValue();
                float high = new Float(positions[pos + 1]).floatValue();
                newPos = (high + low) / 2;
            }
            else {
                newPos = new Float(positions[pos]).floatValue() + 1;
            }
        }
        else {
            newPos = 1;
        }
        cms.writeProperty(newfolder.getAbsolutePath(), C_PROPERTY_NAVPOS, new Float(newPos).toString());
    }

    /**
     * removes all session parameters used by this class.
     * @param session The session.
     */
    private void clearSession(I_CmsSession session){
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
    private void insertProperty(Hashtable properties, String key, String value){
        if(value != null){
            properties.put(key, value);
        }
    }

    /**
     * Fills the needed parameters in the properties hashtable.
     * @param properties The Hashtable to be filled.
     * @param parameters From here we get the values.
     */
    private void fillProperties(Hashtable properties, Hashtable parameters){
        insertProperty(properties, "Verzeichnislogo_img", (String)parameters.get("flogopic"));
        insertProperty(properties, "Verzeichnislogo_target", (String)parameters.get("flogolink"));
        insertProperty(properties, "Hauptlogo_img", (String)parameters.get("mlogopic"));
        insertProperty(properties, "Hauptlogo_target", (String)parameters.get("mlogolink"));
        insertProperty(properties, "Sektionslogo_img", (String)parameters.get("slogopic"));
        insertProperty(properties, "Sektionslogo_target", (String)parameters.get("slogolink"));
        insertProperty(properties, "FreierLink_navtext", (String)parameters.get("statnav"));
        insertProperty(properties, "FreierLink_target", (String)parameters.get("statlink"));
        insertProperty(properties, "Nav_bgcolor", (String)parameters.get("bcoldyn"));
        insertProperty(properties, "Nav_fontcolor", (String)parameters.get("tcoldyn"));
        insertProperty(properties, "FreierLink_bgcolor", (String)parameters.get("bcollink"));
        insertProperty(properties, "FreierLink_fontcolor", (String)parameters.get("tcollink"));
        insertProperty(properties, "Email_bgcolor", (String)parameters.get("bcolemail"));
        insertProperty(properties, "Email_fontcolor", (String)parameters.get("tcolemail"));
        insertProperty(properties, "Suche_bgcolor", (String)parameters.get("bcolsearch"));
        insertProperty(properties, "Suche_fontcolor", (String)parameters.get("tcolsearch"));
    }

    /**
     * sets the given or the default (when parameters is null) values in the
     * template for the extended navigation
     *
     * @param xmlTemplateDoc The xmlTemplateDoc to be filled.
     * @param parameters From here we get the values.
     * @param folder The name of the folder (needed for the Sectionlogo)
     */
    private void setNavDefault(CmsXmlWpTemplateFile xmlTemplateDoc, Hashtable parameters, String name){

        if(parameters == null){
            // set the default values for the first time it is shown
            xmlTemplateDoc.setData("flogopic", "");
            xmlTemplateDoc.setData("flogolink", "");
            xmlTemplateDoc.setData("mlogopic", "");
            xmlTemplateDoc.setData("mlogolink", "/index.html");
            xmlTemplateDoc.setData("slogopic", "");
            xmlTemplateDoc.setData("slogolink", "/"+ name +"/index.html");
            xmlTemplateDoc.setData("statnav", "AKTUELL");
            xmlTemplateDoc.setData("statlink", "");
            xmlTemplateDoc.setData("bcoldyn", "#CCCCCC");
            xmlTemplateDoc.setData("tcoldyn", "#000099");
            xmlTemplateDoc.setData("bcollink", "#CCCCCC");
            xmlTemplateDoc.setData("tcollink", "#000099");
            xmlTemplateDoc.setData("bcolemail", "#CCCCCC");
            xmlTemplateDoc.setData("tcolemail", "#000099");
            xmlTemplateDoc.setData("bcolsearch", "#CCCCCC");
            xmlTemplateDoc.setData("tcolsearch", "#000099");
        }else{
            // set the values which the user entered before he pressed the backbutton
            xmlTemplateDoc.setData("flogopic", getStringValue((String)parameters.get("Verzeichnislogo_img")));
            xmlTemplateDoc.setData("flogolink", getStringValue((String)parameters.get("Verzeichnislogo_target")));
            xmlTemplateDoc.setData("mlogopic", getStringValue((String)parameters.get("Hauptlogo_img")));
            xmlTemplateDoc.setData("mlogolink", getStringValue((String)parameters.get("Hauptlogo_target")));
            xmlTemplateDoc.setData("slogopic", getStringValue((String)parameters.get("Sektionslogo_img")));
            xmlTemplateDoc.setData("slogolink", getStringValue((String)parameters.get("Sektionslogo_target")));
            xmlTemplateDoc.setData("statnav", getStringValue((String)parameters.get("FreierLink_navtext")));
            xmlTemplateDoc.setData("statlink", getStringValue((String)parameters.get("FreierLink_target")));
            xmlTemplateDoc.setData("bcoldyn", getStringValue((String)parameters.get("Nav_bgcolor")));
            xmlTemplateDoc.setData("tcoldyn", getStringValue((String)parameters.get("Nav_fontcolor")));
            xmlTemplateDoc.setData("bcollink", getStringValue((String)parameters.get("FreierLink_bgcolor")));
            xmlTemplateDoc.setData("tcollink", getStringValue((String)parameters.get("FreierLink_fontcolor")));
            xmlTemplateDoc.setData("bcolemail", getStringValue((String)parameters.get("Email_bgcolor")));
            xmlTemplateDoc.setData("tcolemail", getStringValue((String)parameters.get("Email_fontcolor")));
            xmlTemplateDoc.setData("bcolsearch", getStringValue((String)parameters.get("Suche_bgcolor")));
            xmlTemplateDoc.setData("tcolsearch", getStringValue((String)parameters.get("Suche_fontcolor")));
       }
    }

    /**
     * returns the String or "" if it is null.
     * @return java.lang.String
     * @param param java.lang.String
     */
    private String getStringValue(String param) {
        if(param == null) {
            return "";
        }
        return param;
    }
}


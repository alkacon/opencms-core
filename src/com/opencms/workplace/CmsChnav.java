/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChnav.java,v $
* Date   : $Date: 2003/07/12 12:49:03 $
* Version: $Revision: 1.16 $
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

import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Encoder;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying the screen for changing the navigation position
 * for folders or pages.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.16 $ $Date: 2003/07/12 12:49:03 $
 */

public class CmsChnav extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    final static String C_SESSIONHEADER = "CmsChnav_";

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

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        // the template to be displayed
        String template = null;
        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String navtext = "";

        // clear the session on first call
        String initial = (String)parameters.get("initial");
        if (initial != null){
            clearSession(session);
        }

        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename == null || "".equals(filename)){
            filename = (String)session.getValue(C_SESSIONHEADER + C_PARA_FILE);
        }
        if(filename != null){
            session.putValue(C_SESSIONHEADER + C_PARA_FILE, filename);
        }
        CmsResource resource = (CmsResource)cms.readFileHeader(filename);

        // get request parameters
        String navpos = (String)parameters.get(C_PARA_NAVPOS);

        navtext = (String)parameters.get(C_PARA_NAVTEXT);
        if((navtext == null) || ("".equals(navtext))){
            navtext = cms.readProperty(cms.readAbsolutePath(resource), I_CmsConstants.C_PROPERTY_NAVTEXT);
        }

        // get the current phase of this wizard
        String action = (String)parameters.get("action");
        if(action != null) {
            if("save".equals(action)) {
                try {
                    // update the position
                    updateNavPos(cms, resource, navpos);

                    // update the navigation text
                    if(navtext != null){
                        cms.writeProperty(cms.readAbsolutePath(resource), I_CmsConstants.C_PROPERTY_NAVTEXT, navtext);
                    }
                    // all done, now we have to clean up our mess
                    clearSession(session);
                } catch(CmsException ex) {
                    session.putValue(C_SESSIONHEADER + C_PARA_NAVPOS, navpos);
                    session.putValue(C_SESSIONHEADER + C_PARA_NAVTEXT, navtext);
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(ex));
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    return startProcessing(cms, xmlTemplateDocument, "", parameters, "error_system");
                }
                // return to filelist
                try {
                    if(lasturl == null || "".equals(lasturl)) {
                        cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                            + CmsWorkplaceAction.getExplorerFileUri(cms));
                    }else {
                        cms.getRequestContext().getResponse().sendRedirect(lasturl);
                    }
                }catch(Exception e) {
                    throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                        + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, e);
                }
                return null;
            } else if("fromerror".equalsIgnoreCase(action)){
                // error while creating the folder go to firs page and set the stored parameter
                template = null;
                xmlTemplateDocument.setData(C_PARA_NAVPOS, (String)session.getValue(C_SESSIONHEADER +  C_PARA_NAVPOS));
                navtext = (String)session.getValue(C_SESSIONHEADER +  C_PARA_NAVTEXT);
                session.removeValue(C_SESSIONHEADER + C_PARA_NAVPOS);
                session.removeValue(C_SESSIONHEADER + C_PARA_NAVTEXT);
            }
        }
        xmlTemplateDocument.setData("frametitle", resource.getResourceName());
        xmlTemplateDocument.setData(C_PARA_NAVTEXT, Encoder.escapeXml(navtext));

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }

    /**
     * Gets all required navigation information from the files and subfolders of a folder.
     * A file list of all files and folder is created, for all those resources, the navigation
     * property is read. The list is sorted by their navigation position.
     * @param cms The CmsObject.
     * @param filename The Name of the current file.
     * @return Hashtable including three arrays of strings containing the filenames,
     * nicenames and navigation positions.
     * @throws Throws CmsException if something goes wrong.
     */
    private Hashtable getNavData(CmsObject cms, String filename) throws CmsException {
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
        if(currentFilelist == null) {
            currentFilelist = cms.readAbsolutePath(cms.rootFolder());
        }

        // get all files and folders in the current filelist.
        List files = cms.getFilesInFolder(currentFilelist);
        List folders = cms.getSubFolders(currentFilelist);

        // combine folder and file vector
        Vector filefolders = new Vector();
        Iterator enum = folders.iterator();
        while(enum.hasNext()) {
            folder = (CmsFolder)enum.next();
            filefolders.addElement(folder);
        }
        enum = files.iterator();
        while(enum.hasNext()) {
            file = (CmsFile)enum.next();
            filefolders.addElement(file);
        }
        if(filefolders.size() > 0) {
            // Create some arrays to store filename, nicename and position for the
            // nav in there. The dimension of this arrays is set to the number of
            // found files and folders plus two more entrys for the first and last
            // element.
            filenames = new String[filefolders.size() + 3];
            nicenames = new String[filefolders.size() + 3];
            positions = new String[filefolders.size() + 3];

            //now check files and folders that are not deleted and include navigation
            // information
            enum = filefolders.iterator();
            while(enum.hasNext()) {
                CmsResource res = (CmsResource)enum.next();
                // do not include the current file
                if(!cms.readAbsolutePath(res).equals(filename)){
                    // check if the resource is not marked as deleted
                    if(res.getState() != C_STATE_DELETED) {
                        String navpos = cms.readProperty(cms.readAbsolutePath(res), C_PROPERTY_NAVPOS);
                        // check if there is a navpos for this file/folder
                        if(navpos != null) {
                            nicename = Encoder.escapeHtml(cms.readProperty(cms.readAbsolutePath(res), C_PROPERTY_NAVTEXT));
                            if(nicename == null) {
                                nicename = res.getResourceName();
                            }

                            // add this file/folder to the storage.
                            filenames[count] = cms.readAbsolutePath(res);
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
        } else {
            filenames = new String[3];
            nicenames = new String[3];
            positions = new String[3];
        }

        // now add the first and last value
        filenames[0] = "FIRSTENTRY";
        nicenames[0] = lang.getLanguageValue("input.firstelement");
        positions[0] = "0";
        filenames[count] = "LASTENTRY";
        nicenames[count] = lang.getLanguageValue("input.lastelement");
        positions[count] = new Float(max + 1).toString();

        // add the default value for no change of navigation position
        filenames[count+1] = "NOCHANGE";
        nicenames[count+1] = lang.getLanguageValue("input.nochange");
        positions[count+1] = "-1";

        // finally sort the nav information.
        sort(cms, filenames, nicenames, positions, count);

        // put all arrays into a hashtable to return them to the calling method.
        storage.put("FILENAMES", filenames);
        storage.put("NICENAMES", nicenames);
        storage.put("POSITIONS", positions);
        // the value for count includes the entry for no change
        storage.put("COUNT", new Integer(count+1));
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

    public Integer getNavPos(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        I_CmsSession session = cms.getRequestContext().getSession(true);
        String preselect = (String)session.getValue(C_SESSIONHEADER + C_PARA_NAVPOS);
        int retValue = -1;
        // get the name of the current file
        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename == null || "".equals(filename)){
            filename = (String)session.getValue(C_SESSIONHEADER + C_PARA_FILE);
        }

       // get the nav information
        Hashtable storage = getNavData(cms, filename);
        if(storage.size() > 0) {
            String[] nicenames = (String[])storage.get("NICENAMES");
            int count = ((Integer)storage.get("COUNT")).intValue();

            // finally fill the result vectors
            for(int i = 0;i <= count;i++) {
                names.addElement(nicenames[i]);
                values.addElement(nicenames[i]);
                if ((preselect != null) && (Encoder.escapeHtml(preselect).equals(nicenames[i]))){
                    retValue = values.size() -1;
                }
            }
        }
        else {
            values = new Vector();
        }
        if (retValue == -1){
            // set the default value to no change
            return new Integer(values.size() - 1);
        }else{
            return new Integer(retValue);
        }
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

    private void updateNavPos(CmsObject cms, CmsResource curResource, String navpos) throws CmsException {
        float newPos = 0;
        boolean changePos = true;

        // get the nav information
        Hashtable storage = getNavData(cms, cms.readAbsolutePath(curResource));
        if(storage.size() > 0) {
            String[] nicenames = (String[])storage.get("NICENAMES");
            String[] positions = (String[])storage.get("POSITIONS");
            // the value for count must not include the entry for no change
            int count = ((Integer)storage.get("COUNT")).intValue()-1;

            // now find the file after which the new file is sorted
            int pos = 0;
            for(int i = 0;i < nicenames.length;i++) {
                if(Encoder.escapeHtml(navpos).equals((String)nicenames[i])) {
                    pos = i;
                }
            }
            // check if the value for no change is selected
            if("-1".equals(positions[pos])){
                changePos = false;
            }
            // only get new position if a new position was selected
            if(changePos){
                if(pos < count) {
                    float low = new Float(positions[pos]).floatValue();
                    float high = new Float(positions[pos + 1]).floatValue();
                    newPos = (high + low) / 2;
                } else {
                    newPos = new Float(positions[pos]).floatValue() + 1;
                }
            }
        } else {
            newPos = 1;
        }
        // only change position if new position was selected
        if(changePos){
            cms.writeProperty(cms.readAbsolutePath(curResource), C_PROPERTY_NAVPOS, new Float(newPos).toString());
        }
    }

    /**
     * removes all session parameters used by this class.
     * @param session The session.
     */
    private void clearSession(I_CmsSession session){
        session.removeValue(C_SESSIONHEADER + C_PARA_NAVPOS);
        session.removeValue(C_SESSIONHEADER + C_PARA_FILE);
    }
}


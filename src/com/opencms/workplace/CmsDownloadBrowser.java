/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsDownloadBrowser.java,v $
* Date   : $Date: 2003/07/09 10:58:09 $
* Version: $Revision: 1.25 $
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Encoder;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying OpenCms download browser.
 * <P>
 *
 * @author Mario Stanke
 * @version $Revision: 1.25 $ $Date: 2003/07/09 10:58:09 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsDownloadBrowser extends CmsWorkplaceDefault implements I_CmsFileListUsers {

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
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && C_DEBUG) {
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "getting content of element "
                    + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "template file is: "
                    + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "selected template section is: "
                    + ((templateSelector == null) ? "<default>" : templateSelector));
        }
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);

        // test whether the download folder exists at all
        try {
            cms.readFileHeader(getConfigFile(cms).getDownGalleryPath());
        }
        catch(CmsException e) {
            xmlTemplateDocument.setData("ERRORDETAILS", Utils.getStackTrace(e));
            templateSelector = "error";
        }
        if(!"error".equals(templateSelector)) {

            // clear session parameters of first load
            if(parameters.get(C_PARA_INITIAL) != null) {
                session.removeValue(C_PARA_FOLDER);
                session.removeValue(C_PARA_PAGE);
                session.removeValue("_DOWNLIST_");
                session.removeValue(C_PARA_FILTER);
                session.removeValue("numfiles");
                session.removeValue("lasturl");
            }

            // most parameters have to be stored in the session because 'getFiles' needs them
            String folder = (String)parameters.get(C_PARA_FOLDER);
            if(folder != null) {
                session.putValue(C_PARA_FOLDER, folder);
            }
            folder = (String)session.getValue(C_PARA_FOLDER);
            if(folder == null || "".equals(folder)) {
                folder = getConfigFile(cms).getDownGalleryPath();
                List galleries = cms.getSubFolders(folder);
                if(galleries.size() > 0) {

                    // take the first gallery
                    folder = cms.readAbsolutePath((CmsResource)galleries.get(0));
                    session.putValue(C_PARA_FOLDER, folder);
                }
                else {

                    // there was a C_VFS_GALLERY_DOWNLOAD - folder but no gallery in it
                    templateSelector = "error_no_gallery";
                }
            }
            if(!"error_no_gallery".equals(templateSelector)) {
                String pageText = (String)parameters.get(C_PARA_PAGE);
                String filter = (String)parameters.get(C_PARA_FILTER);

                // Check if the user requested a certain page number
                if(pageText == null || "".equals(pageText)) {
                    pageText = "1";
                    parameters.put(C_PARA_PAGE, pageText);
                }
                session.putValue(C_PARA_PAGE, pageText);

                // Check if the user requested a filter
                if(filter == null) {
                    filter = "";
                    session.putValue(C_PARA_FILTER, filter);
                    parameters.put(C_PARA_FILTER, filter);
                }

                // Compute the maximum page number
                Vector filteredFiles = getFilteredDownList(cms, folder, filter);
                int maxpage = ((filteredFiles.size() - 1) / C_DOWNBROWSER_MAXENTRIES) + 1;

                // Now set the appropriate datablocks
                xmlTemplateDocument.setData(C_PARA_FOLDER, Encoder.escape(folder,
                    cms.getRequestContext().getEncoding()));
                xmlTemplateDocument.setData(C_PARA_PAGE, pageText);
                xmlTemplateDocument.setData(C_PARA_FILTER, filter);
                xmlTemplateDocument.setData(C_PARA_MAXPAGE, "" + maxpage);
                session.putValue("_DOWNLIST_", filteredFiles);
                session.putValue("numfiles", new Integer(filteredFiles.size())); // for 'showNextButton'
            }
        }

        // Start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
    }

    /**
     * From interface <code>I_CmsFileListUsers</code>.
     * <P>
     * Fills all customized columns with the appropriate settings for the given file
     * list entry. Any column filled by this method may be used in the customized template
     * for the file list.
     * @param cms Cms object for accessing system resources.
     * @param filelist Template file containing the definitions for the file list together with
     * the included customized defintions.
     * @param res CmsResource Object of the current file list entry.
     * @param lang Current language file.
     * @throws CmsException if access to system resources failed.
     * @see I_CmsFileListUsers
     */

    public void getCustomizedColumnValues(CmsObject cms, CmsXmlWpTemplateFile filelistTemplate,
            CmsResource res, CmsXmlLanguageFile lang) throws CmsException {
        String servletPath = cms.getRequestContext().getRequest().getServletUrl();
        String downloadPath = servletPath + cms.readAbsolutePath(res);
        filelistTemplate.setData("fullpath", downloadPath);
        filelistTemplate.setData("name_value", res.getName());
        String title = "";
        try {
            title = cms.readProperty(cms.readAbsolutePath(res), C_PROPERTY_TITLE);
        }
        catch(CmsException e) {

        }
        if(title == null) {
            title = "";
        }
        filelistTemplate.setData("title_value", title);
    }

    public Integer getDownGalleryNames(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {
        int ret = -1;
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // which folder is the gallery?
        String chosenFolder = (String)parameters.get(C_PARA_FOLDER);
        if(chosenFolder == null) {
            chosenFolder = (String)session.getValue(C_PARA_FOLDER);
        }
        if(chosenFolder == null) {
            chosenFolder = "";
        }
        List folders = cms.getSubFolders(getConfigFile(cms).getDownGalleryPath());
        int numFolders = folders.size();
        for(int i = 0;i < numFolders;i++) {
            CmsResource currFolder = (CmsResource)folders.get(i);
            String name = currFolder.getName();
            if(chosenFolder.equals(cms.readAbsolutePath(currFolder))) {
                ret = i;
            }
            values.addElement(cms.readAbsolutePath(currFolder));
            names.addElement(name);
        }
        return new Integer(ret);
    }

    /**
     * From interface <code>I_CmsFileListUsers</code>.
     * <P>
     * Collects all files in the chosen download gallery list.
     * @param cms The CmsObject.
     * @return A vector of folder and file objects.
     * @throws Throws CmsException if something goes wrong.
     */

    public List getFiles(CmsObject cms) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        Vector files = new Vector();

        // get the list of filtered files from session
        Vector filteredFiles = (Vector)session.getValue("_DOWNLIST_");
        int numFiles = filteredFiles.size();

        // Get limits for the requested page
        String pageText = (String)session.getValue(C_PARA_PAGE);
        if(pageText == null || "".equals(pageText)) {
            pageText = "1";
        }
        int page = new Integer(pageText).intValue();
        int from = (page - 1) * C_DOWNBROWSER_MAXENTRIES;
        int to = ((from + C_DOWNBROWSER_MAXENTRIES) > numFiles) ? numFiles : (from + C_DOWNBROWSER_MAXENTRIES);
        String folder = (String)session.getValue(C_PARA_FOLDER);
        if(folder == null || "".equals(folder)) {
            folder = getConfigFile(cms).getDownGalleryPath();
            List galleries = cms.getSubFolders(folder);
            if(galleries.size() > 0) {

                // take the first gallery if none was chosen
                folder = cms.readAbsolutePath((CmsResource)galleries.get(0));
            }
            session.putValue(C_PARA_FOLDER, folder);
        }

        // Generate the download list for all files on the selected page
        for(int i = from;i < to;i++) {
            CmsResource currFile = (CmsResource)filteredFiles.elementAt(i);
            files.addElement(currFile);
        }
        return files;
    }

    /**
     * Internal method for getting a vector of all files that match a given filter
     * i.e., <code>filter</code> is a substring of the name or the title
     *
     * @param cms Cms object for accessing system resources.
     * @param folder Folder to look for files.
     * @param filter Search pattern that should be used.
     * @return Vector of CmsFile objects.
     */

    private Vector getFilteredDownList(CmsObject cms, String folder, String filter)
            throws CmsException {

        // Get all pictures in the given folder using the cms object
        List allFiles = cms.getFilesInFolder(folder);

        // Filter the files
        Vector filteredFiles = new Vector();
        for(int i = 0;i < allFiles.size();i++) {
            CmsFile file = (CmsFile)allFiles.get(i);
            String filename = file.getName();
            String title = cms.readProperty(cms.readAbsolutePath(file), C_PROPERTY_TITLE);
            boolean filenameFilter = inFilter(filename, filter);
            boolean titleFilter = ((title == null) || ("".equals(title))) ? false : inFilter(title, filter);
            if(filenameFilter || titleFilter) {
                filteredFiles.addElement(file);
            }
        }
        return filteredFiles;
    }

    /**
     * Checks, if the given filename matches the filter.
     * @param filename filename to be checked.
     * @param filter filter to be checked.
     * @return <code>true</code> if the filename matches the filter, <code>false</code> otherwise.
     */

    private boolean inFilter(String name, String filter) {
        String compareName = name.toLowerCase();
        String compareFilter = filter.toLowerCase();
        if("".equals(compareFilter) || (compareName.indexOf(compareFilter) != -1)) {
            return true;
        }
        else {
            return false;
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
     * From interface <code>I_CmsFileListUsers</code>.
     * <P>
     * Used to modify the bit pattern for hiding and showing columns in
     * the file list.
     * @param cms Cms object for accessing system resources.
     * @param prefs Old bit pattern.
     * @return New modified bit pattern.
     * @see I_CmsFileListUsers
     */

    public int modifyDisplayedColumns(CmsObject cms, int prefs) {

        // not display the following columns
        prefs = ((prefs & C_FILELIST_NAME) == 0) ? prefs : (prefs - C_FILELIST_NAME);
        prefs = ((prefs & C_FILELIST_TITLE) == 0) ? prefs : (prefs - C_FILELIST_TITLE);
        prefs = ((prefs & C_FILELIST_USER_CREATED) == 0) ? prefs : (prefs - C_FILELIST_USER_CREATED);
        prefs = ((prefs & C_FILELIST_GROUP) == 0) ? prefs : (prefs - C_FILELIST_GROUP);
        prefs = ((prefs & C_FILELIST_PERMISSIONS) == 0) ? prefs : (prefs - C_FILELIST_PERMISSIONS);
        prefs = ((prefs & C_FILELIST_STATE) == 0) ? prefs : (prefs - C_FILELIST_STATE);
        prefs = ((prefs & C_FILELIST_LOCKEDBY) == 0) ? prefs : (prefs - C_FILELIST_LOCKEDBY);
        return prefs;
    }

    /**
     * Used by the workplace "back" button to decide whether the icon should
     * be activated or not. A button will use this method if the attribute <code>method="showBackButton"</code>
     * is defined in the <code>&lt;BUTTON&gt;</code> tag.
     * <P>
     * This method returns <code>false</code> if the currently displayed page is
     * the first page.
     *
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>true</code> if the button should be enabled, <code>false</code> otherwise.
     */

    public Boolean showBackButton(CmsObject cms, CmsXmlLanguageFile lang,
            Hashtable parameters) {

        // Get the current page number
        String pageText = (String)parameters.get(C_PARA_PAGE);
        int page = new Integer(pageText).intValue();
        return new Boolean(page > 1);
    }

    /**
     * Used by the workplace "next" button to decide whether the icon should
     * be activated or not. A button will use this method if the attribute <code>method="showNextButton"</code>
     * is defined in the <code>&lt;BUTTON&gt;</code> tag.
     * <P>
     * This method returns <code>false</code> if the currently displayed page is
     * the last page.
     *
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>true</code> if the button should be enabled, <code>false</code> otherwise.
     */

    public Boolean showNextButton(CmsObject cms, CmsXmlLanguageFile lang,
            Hashtable parameters) {
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // Get the current page number
        String pageText = (String)parameters.get(C_PARA_PAGE);
        int page = new Integer(pageText).intValue();

        // get the number of pics
        int numFiles = ((Integer)session.getValue("numfiles")).intValue();

        // Get the maximum page number
        int maxpage = ((numFiles - 1) / C_DOWNBROWSER_MAXENTRIES) + 1;
        return new Boolean(page < maxpage);
    }
}

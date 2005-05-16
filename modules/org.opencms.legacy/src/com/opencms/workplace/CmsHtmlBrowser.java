/*
* File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.legacy/src/com/opencms/workplace/Attic/CmsHtmlBrowser.java,v $
* Date   : $Date: 2005/05/16 17:44:59 $
* Version: $Revision: 1.1 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.A_CmsXmlContent;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying OpenCms html snippet browser.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author magnus meurer
 * @version $Revision: 1.1 $ $Date: 2005/05/16 17:44:59 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsHtmlBrowser extends A_CmsGalleryBrowser {

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
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);

        // test whether the links folder exists at all
        try {
            cms.readResource(C_VFS_GALLERY_EXTERNALLINKS);
        } catch(CmsException e) {
            xmlTemplateDocument.setData("ERRORDETAILS", CmsException.getStackTraceAsString(e));
            templateSelector = "error";
        }
        if(!"error".equals(templateSelector)) {
            if(parameters.get(C_PARA_INITIAL) != null) {
                session.removeValue(C_PARA_FOLDER);
                session.removeValue("htmlBrowser_for_ext_nav");
            }
            String setOnClick = (String)parameters.get("setonclick");
            if(setOnClick != null){
                session.putValue("htmlBrowser_for_ext_nav", setOnClick);
            }
            setOnClick = (String)session.getValue("htmlBrowser_for_ext_nav");
            String folder = (String)parameters.get(C_PARA_FOLDER);
            if(folder != null) {
                session.putValue(C_PARA_FOLDER, folder);
            }
            folder = (String)session.getValue(C_PARA_FOLDER);
            if(folder == null || "".equals(folder)) {
                List galleries = cms.getSubFolders(getConfigFile(cms).getHtmlGalleryPath());
                if(galleries.size() > 0) {

                    // take the first gallery
                    folder = cms.getSitePath((CmsResource)galleries.get(0));
                    session.putValue(C_PARA_FOLDER, folder);
                } else {

                    // there was a C_VFS_GALLERY_EXTERNALLINKS - folder but no galery in it
                    templateSelector = "error_no_gallery";
                }
            }
            if(!"error_no_gallery".equals(templateSelector)) {
                String pageText = (String)parameters.get(C_PARA_PAGE);
                String filter = (String)parameters.get(C_PARA_FILTER);

                // Check if the user requested a special page
                if(pageText == null || "".equals(pageText)) {
                    pageText = "1";
                    parameters.put(C_PARA_PAGE, pageText);
                }

                // Check if the user requested a filter
                if(filter == null) {
                    filter = "";
                    parameters.put(C_PARA_FILTER, filter);
                }

                // Compute the maximum page number
                Vector filteredLinks = getFilteredHtmlList(cms, folder, filter);
                int maxpage = ((filteredLinks.size() - 1) / C_PICBROWSER_MAXIMAGES) + 1;

                // Now set the appropriate datablocks
                xmlTemplateDocument.setData(C_PARA_FOLDER, CmsEncoder.escape(folder,
                    cms.getRequestContext().getEncoding()));
                xmlTemplateDocument.setData(C_PARA_PAGE, pageText);
                xmlTemplateDocument.setData(C_PARA_FILTER, filter);
                xmlTemplateDocument.setData(C_PARA_MAXPAGE, "" + maxpage);
                if(setOnClick == null || !"true".equals(setOnClick)){
                    xmlTemplateDocument.setData("setonclick", "");
                }else{
                    xmlTemplateDocument.setData("setonclick", "true");
                }
                parameters.put("_HTMLLIST_", filteredLinks);
            }
        }

        // Start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * Internal method for getting a vector of all links using
     * a given filter.
     * @param cms Cms object for accessing system resources.
     * @param folder Folder to look for links.
     * @param filter Search pattern that should be used.
     * @return Vector of CmsFile objects.
     */

    private Vector getFilteredHtmlList(CmsObject cms, String folder, String filter) throws CmsException {

        // Get all links in the given folder using the cms object
        List allLinks = cms.getFilesInFolder(folder);

        // Filter the links
        Vector filteredLinks = new Vector();
        for(int i = 0;i < allLinks.size();i++) {
            CmsFile file = (CmsFile)allLinks.get(i);
            String filename = file.getName();
            String title = cms.readProperty(cms.getSitePath(file), C_PROPERTY_TITLE);
            boolean filenameFilter = inFilter(filename, filter);
            boolean titleFilter = ((title == null) || ("".equals(title))) ? false : inFilter(title, filter);
            if((filenameFilter || titleFilter)) {
                filteredLinks.addElement(file);
            }
        }
        return filteredLinks;
    }

    /**
     * Gets the filenames of all html galleries
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box. The values will be the paths to the galleries.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index of the selected Gallery
     * @throws CmsException
     */
    public Integer getHtmlGalleryNames(CmsObject cms, CmsXmlLanguageFile lang,
            Vector names, Vector values, Hashtable parameters) throws CmsException {
        return getGalleryNames(cms, getConfigFile(cms).getHtmlGalleryPath(), lang, names, values, parameters);
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
     * User method to generate an URL for the pics folder.
     * <P>
     * All pictures should reside in the docroot of the webserver for
     * performance reasons. This folder can be mounted into the OpenCms system to
     * make it accessible for the OpenCms explorer.
     * <P>
     * The path to the docroot can be set in the workplace ini.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the pics URL.
     * @throws CmsException
     */

    public Object htmlList(CmsObject cms, String tagcontent, A_CmsXmlContent doc,
            Object userObj) throws CmsException {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        Hashtable parameters = (Hashtable)userObj;
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)doc;
        StringBuffer result = new StringBuffer();
        String pageText = (String)parameters.get(C_PARA_PAGE);

        // Filter the links
        Vector filteredLinks = (Vector)parameters.get("_HTMLLIST_");
        int numLinks = filteredLinks.size();

        // Get limits for the requested page
        int page = new Integer(pageText).intValue();
        int from = (page - 1) * C_PICBROWSER_MAXIMAGES;
        int to = ((from + C_PICBROWSER_MAXIMAGES) > numLinks) ? numLinks : (from + C_PICBROWSER_MAXIMAGES);
        String folder = (String)parameters.get(C_PARA_FOLDER);
        if(folder == null) {
            folder = (String)session.getValue(C_PARA_FOLDER);
        }
        if(folder == null || "".equals(folder)) {
            folder = getConfigFile(cms).getHtmlGalleryPath();
            parameters.put(C_PARA_FOLDER, folder);
        }

        String linkUrl = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl() + folder;

        // Generate the link list for all links on the selected page
        for(int i = from;i < to;i++) {
            CmsFile file = cms.readFile(cms.getSitePath((CmsFile)filteredLinks.elementAt(i)));
            String filename = file.getName();
            String title = cms.readProperty(cms.getSitePath(file), C_PROPERTY_TITLE);

            // If no "Title" property is given, the title will be set to the filename
            // without its postfix
            int dotIndex = filename.lastIndexOf(".");
            if(title == null) {
                if(dotIndex > 0) {
                    title = filename.substring(0, dotIndex);
                }
                else {
                    title = filename;
                }
            }

            // Set all datablocks for the current picture list entry
            xmlTemplateDocument.setData("linksource", linkUrl + file.getName());
            xmlTemplateDocument.setData("filename", cms.getSitePath(file));
            xmlTemplateDocument.setData("title", filename);
            xmlTemplateDocument.setData("linktext", filename);
            xmlTemplateDocument.setData("snippetid", "" + i);
            xmlTemplateDocument.setData("filecontent", new String(file.getContents()));
            try {
                xmlTemplateDocument.setData("filecontent_escaped", CmsEncoder.escape(
                    new String(file.getContents(), cms.getRequestContext().getEncoding()),
                    cms.getRequestContext().getEncoding()));
            } catch (UnsupportedEncodingException e) {
                xmlTemplateDocument.setData("filecontent_escaped", CmsEncoder.escape(
                    new String(file.getContents()),
                    cms.getRequestContext().getEncoding()));
            }

            // look if the onclick event must be set
            String paraSetOnClick = (String)session.getValue("htmlBrowser_for_ext_nav");
            String setOnClick = "";
            if ("true".equals(paraSetOnClick)){
                setOnClick = xmlTemplateDocument.getProcessedDataValue("clickentry");
            }
            xmlTemplateDocument.setData("toclickornot", setOnClick);
            result.append(xmlTemplateDocument.getProcessedDataValue("htmllistentry", this, userObj));
        }
        return result.toString();
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

    public Boolean showBackButton(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {

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

    public Boolean showNextButton(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {

        // Get the current page number
        String pageText = (String)parameters.get(C_PARA_PAGE);
        int page = new Integer(pageText).intValue();

        // get the number of links
        Vector filteredLinks = (Vector)parameters.get("_HTMLLIST_");
        int numLinks = filteredLinks.size();

        // Get the maximum page number
        int maxpage = ((numLinks - 1) / C_PICBROWSER_MAXIMAGES) + 1;
        return new Boolean(page < maxpage);
    }
}

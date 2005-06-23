/*
 * File   :
 * Date   : 
 * Version: 
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.database;

import org.opencms.db.CmsDbIoException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.page.CmsXmlPage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;

/**
 * This class implements the HTML->OpenCms Template converter for OpenCms 6.x.<p>
 * 
 * @author Michael Emmerich 
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsHtmlImport {

    /** filename of the meta.properties file. */
    public static final String C_META_PROPERTIES = "meta.properties";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlImport.class);

    /** the base URL for link modification. */
    private URL m_baseUrl;

    private CmsJspActionElement m_cms;

    /** the CmsObject to use. */
    private CmsObject m_cmsObject;

    /** the destination directory in the OpenCms VFS. */
    private String m_destinationDir;

    /** he download gallery name. */
    private String m_downloadGallery;

    private String m_element;

    /** the end pattern for extracting content. */
    private String m_endPattern;

    /** HashMap of all known extensions in OpenCms. */
    private Map m_extensions;

    /**
     * Storage for external links.<p>
     * 
     * It is filled by the HtmlConverter each time a new external link is found.<p>
     */
    private HashSet m_externalLinks;

    /** 
     * The file index contains all resourcenames in the real file system and their renamed ones in the OpenCms VFS.<p>
     */
    private HashMap m_fileIndex;

    /** the HTML converter to parse and modifiy the content. */
    private CmsHtmlImportConverter m_htmlConverter;

    /** reference to the import thread. */
    private CmsHtmlImportThread m_htmlImportThread;

    /** the image gallery name. */
    private String m_imageGallery;

    /**
     * Storage for image alt tags, it is filled by the HtmlConverter each time a new image is found.
     */
    private HashMap m_imageInfo;

    /** the input directory in the "real" file system. */
    private String m_inputDir;

    /** the encoding used for all imported input files. */
    private String m_inputEncoding;

    /** the external link gallery name. */
    private String m_linkGallery;

    /** the locale use for content definition. */
    private Locale m_locale;

    /** the overwrite value new resources. */
    private String m_overwrite;

    /** the overwrite mode flag. */
    private boolean m_overwriteMode;

    /** the report for the output. */
    private I_CmsReport m_report;

    /** the start pattern for extracting content. */
    private String m_startPattern;

    /** the template use for all pages. */
    private String m_template;

    /**
     * Creates new HtmlImport Object with http request parameters.<p>
     * 
     * @param cms the current CmsJspActionElement  
     * @param request the http servlet request
     */
    public CmsHtmlImport(CmsJspActionElement cms, HttpServletRequest request) {

        this(
            cms,
            request.getParameter("inputDir"),
            request.getParameter("destinationDir"),
            request.getParameter("imageGallery"),
            request.getParameter("linkGallery"),
            request.getParameter("downloadGallery"),
            request.getParameter("template"),
            request.getParameter("element"),
            request.getParameter("locale"),
            request.getParameter("encoding"),
            request.getParameter("startPattern"),
            request.getParameter("endPattern"),
            request.getParameter("overwrite"));
    }

    /**
     * Constructor, creates a new HtmlImport.<p>
     * 
     * @param cms the current CmsJspActionElement 
     * @param inputDir the input directory in the "real" file system 
     * @param destinationDir the destination directory in the OpenCms VFS
     * @param imageGallery the image gallery name
     * @param linkGallery the external link gallery name
     * @param downloadGallery the download gallery name
     * @param template the template use for all pages
     * @param element the element property use for all pages
     * @param locale the full locale name 
     * @param encoding encoding used for importing all pages
     * @param startPattern the start pattern definition for content extracting
     * @param endPattern the end pattern definition for content extracting 
     * @param overwrite the overwrite mode
     */
    public CmsHtmlImport(
        CmsJspActionElement cms,
        String inputDir,
        String destinationDir,
        String imageGallery,
        String linkGallery,
        String downloadGallery,
        String template,
        String element,
        String locale,
        String encoding,
        String startPattern,
        String endPattern,
        String overwrite) {

        if (inputDir == null) {
            inputDir = "";
        }
        if (destinationDir == null) {
            destinationDir = "/";
        }
        if (imageGallery == null) {
            imageGallery = "";
        }
        if (linkGallery == null) {
            linkGallery = "";
        }
        if (downloadGallery == null) {
            downloadGallery = "";
        }
        if (template == null) {
            template = "";
        }
        if (element == null) {
            element = "body";
        }
        if (encoding == null) {
            encoding = "";
        }
        if (startPattern == null) {
            startPattern = "";
        }
        if (endPattern == null) {
            endPattern = "";
        }
        if (overwrite == null) {
            overwrite = "checked";
        }

        // store all member variables
        m_cms = cms;
        m_cmsObject = m_cms.getCmsObject();
        m_locale = CmsLocaleManager.getLocale(locale);
        if (m_locale == null) {
            m_locale = m_cms.getRequestContext().getLocale();
        }
        // body element should be set by html-form
        m_inputDir = inputDir.trim();

        // cut of a trailing '/' or '\'
        if (m_inputDir.endsWith("/") || m_inputDir.endsWith("\\")) {
            m_inputDir = m_inputDir.substring(0, m_inputDir.length() - 1);
        }

        m_destinationDir = destinationDir.trim();
        if (!m_destinationDir.endsWith("/")) {
            m_destinationDir += "/";
        }

        m_imageGallery = imageGallery.trim();
        if (!m_imageGallery.endsWith("/")) {
            m_imageGallery += "/";
        }
        m_linkGallery = linkGallery.trim();
        if (!m_linkGallery.endsWith("/")) {
            m_linkGallery += "/";
        }
        m_downloadGallery = downloadGallery.trim();
        if (!m_downloadGallery.endsWith("/")) {
            m_downloadGallery += "/";
        }

        m_template = template;
        m_element = element;
        m_inputEncoding = encoding;

        if (CmsStringUtil.isEmpty(m_inputEncoding)) {
            m_inputEncoding = CmsEncoder.ENCODING_ISO_8859_1;
        }
        m_startPattern = startPattern;
        m_endPattern = endPattern;

        m_overwrite = overwrite.trim();
        if (m_overwrite.equals("checked")) {
            m_overwriteMode = true;
        } else {
            m_overwriteMode = false;
        }

        // create all other required member objects
        m_fileIndex = new HashMap();
        m_externalLinks = new HashSet();
        m_imageInfo = new HashMap();
        m_extensions = OpenCms.getResourceManager().getExtensionMapping();
        m_htmlConverter = new CmsHtmlImportConverter(this, false);
        m_baseUrl = null;
        try {
            m_baseUrl = new URL("file://");
        } catch (MalformedURLException e) {
            // this won't happen
        }
    }

    /**
     * Substitutes searchString in content with replaceItem.<p>
     * 
     * @param content the content which is scanned
     * @param searchString the String which is searched in content
     * @param replaceItem the new String which replaces searchString
     * @return String the substituted String
     */
    public static String substitute(String content, String searchString, String replaceItem) {

        // high performance implementation to avoid regular expression overhead
        int findLength;
        if (content == null) {
            return null;
        }
        int stringLength = content.length();
        if (searchString == null || (findLength = searchString.length()) == 0) {
            return content;
        }
        if (replaceItem == null) {
            replaceItem = "";
        }
        int replaceLength = replaceItem.length();
        int length;
        if (findLength == replaceLength) {
            length = stringLength;
        } else {
            int count;
            int start;
            int end;
            count = 0;
            start = 0;
            while ((end = content.indexOf(searchString, start)) != -1) {
                count++;
                start = end + findLength;
            }
            if (count == 0) {
                return content;
            }
            length = stringLength - (count * (findLength - replaceLength));
        }
        int start = 0;
        int end = content.indexOf(searchString, start);
        if (end == -1) {
            return content;
        }
        StringBuffer sb = new StringBuffer(length);
        while (end != -1) {
            sb.append(content.substring(start, end));
            sb.append(replaceItem);
            start = end + findLength;
            end = content.indexOf(searchString, start);
        }
        end = stringLength;
        sb.append(content.substring(start, end));
        return sb.toString();
    }

    /**
     * Tests if all given input parameters for the HTML Import are valid, i.e. that all the 
     * given folders do exist. <p>
     * 
     * @throws CmsIllegalArgumentException if some parameters are not valid
     */
    public void checkParameters() throws CmsIllegalArgumentException {

        // check the input directory
        File inputDir = new File(m_inputDir);
        if (!inputDir.exists() || inputDir.isFile()) {
            // the input directory is not valid
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.GUI_HTMLIMPORT_INPUTDIR_1,
                m_inputDir));
        }

        // check the destination directory        
        try {
            m_cmsObject.readFolder(m_destinationDir);
        } catch (CmsException e) {
            // an excpetion is thrown if the folder does not exist
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.GUI_HTMLIMPORT_DESTDIR_1,
                m_destinationDir), e);
        }

        // check the image gallery
        try {
            m_cmsObject.readFolder(m_imageGallery);
        } catch (CmsException e) {
            // an excpetion is thrown if the folder does not exist
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.GUI_HTMLIMPORT_DESTDIR_1,
                m_imageGallery), e);
        }

        // check the link gallery
        try {
            m_cmsObject.readFolder(m_linkGallery);
        } catch (CmsException e) {
            // an excpetion is thrown if the folder does not exist
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.GUI_HTMLIMPORT_LINKGALLERY_1,
                m_linkGallery), e);

        }

        // check the download gallery
        if (!isExternal(m_downloadGallery)) {
            try {
                m_cmsObject.readFolder(m_downloadGallery);
            } catch (CmsException e) {
                // an excpetion is thrown if the folder does not exist
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.GUI_HTMLIMPORT_DOWNGALLERY_1,
                    m_downloadGallery), e);
            }
        }

        // check the template
        try {
            m_cmsObject.readResource(m_template, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            // an excpetion is thrown if the template does not exist
            if (!isValidElement()) {
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.GUI_HTMLIMPORT_TEMPLATE_1,
                    m_template), e);
            }
        }

        // check the element
        if (!isValidElement()) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.GUI_HTMLIMPORT_INVALID_ELEM_2,
                m_element,
                m_template));
        }

        // check if we are in an offline project
        if (m_cmsObject.getRequestContext().currentProject().isOnlineProject()) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.GUI_HTMLIMPORT_CONSTRAINT_OFFLINE_0));
        }
    }

    /**
     * Calculates an absolute uri from a relative "uri" and the given absolute "baseUri".<p> 
     * 
     * If "uri" is already absolute, it is returned unchanged.
     * This method also returns "uri" unchanged if it is not well-formed.<p>
     *    
     * @param relativeUri the relative uri to calculate an absolute uri for
     * @param baseUri the base uri, this must be an absolute uri
     * @return an absolute uri calculated from "uri" and "baseUri"
     */
    public String getAbsoluteUri(String relativeUri, String baseUri) {

        if ((relativeUri == null) || (relativeUri.charAt(0) == '/') || (relativeUri.startsWith("#"))) {

            return relativeUri;
        }

        // if we are on a windows system, we must add a ":" in the uri later               
        String windowsAddition = "";
        if (File.separator.equals("\\")) {
            windowsAddition = ":";
        }

        try {
            URL url = new URL(new URL(m_baseUrl, "file://" + baseUri), relativeUri);
            if (url.getQuery() == null) {
                return url.getHost() + windowsAddition + url.getPath();
            } else {
                return url.getHost() + windowsAddition + url.getPath() + "?" + url.getQuery();
            }
        } catch (MalformedURLException e) {
            return relativeUri;
        }
    }

    /**
     * Returns the destinationDir.<p>
     *
     * @return the destinationDir
     */
    public String getDestinationDir() {

        return m_destinationDir.substring(0, m_destinationDir.length() - 1);
    }

    /**
     * Returns the downloadGallery.<p>
     *
     * @return the downloadGallery
     */
    public String getDownloadGallery() {

        return m_downloadGallery;
    }

    /**
     * Returns the element.<p>
     *
     * @return the element
     */
    public String getElement() {

        return m_element;
    }

    /**
     * Returns the endPattern.<p>
     *
     * @return the endPattern
     */
    public String getEndPattern() {

        return m_endPattern;
    }

    /**
     * Returns the imageGallery.<p>
     *
     * @return the imageGallery
     */
    public String getImageGallery() {

        return m_imageGallery;
    }

    /**
     * Returns the inputDir.<p>
     *
     * @return the inputDir
     */
    public String getInputDir() {

        return m_inputDir;
    }

    /**
     * Returns the inputEncoding.<p>
     *
     * @return the inputEncoding
     */
    public String getInputEncoding() {

        return m_inputEncoding;
    }

    /**
     * Returns the linkGallery.<p>
     *
     * @return the linkGallery
     */
    public String getLinkGallery() {

        return m_linkGallery;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the overwrite flag.<p>
     *
     * @return the overwrite flag
     */
    public String getOverwrite() {

        return m_overwrite;
    }

    /**
     * Returns the startPattern.<p>
     *
     * @return the startPattern
     */
    public String getStartPattern() {

        return m_startPattern;
    }

    /**
     * Returns the template.<p>
     *
     * @return the template
     */
    public String getTemplate() {

        return m_template;
    }

    /**
     * Returns the output of the HtmlImportThread.<p>
     * 
     * @return log output of the import threat
     */
    public String getThreadOutput() {

        String output = "";
        // check if we have a thread
        if (m_htmlImportThread != null) {
            // is it still alive?
            if (m_htmlImportThread.isAlive()) {
                output = m_htmlImportThread.getReportUpdate();
            }
        }

        return output;
    }

    /**
     * Imports all resources from the real filesystem, stores them into the correct locations
     * in the OpenCms VFS and modifies all links. This method is called form the JSP to start the
     * import process.<p>
     * @param report StringBuffer for reporting
     * @throws Exception if something goes wrong
     */
    public void startImport(I_CmsReport report) throws Exception {

        try {
            m_report = report;
            m_report.println(Messages.get().container(Messages.RPT_HTML_IMPORT_BEGIN_0), I_CmsReport.C_FORMAT_HEADLINE);

            // first build the index of all resources
            buildIndex(m_inputDir);
            // copy and parse all html files first. during the copy process we will collect all 
            // required data for downloads and images
            copyHtmlFiles(m_inputDir);
            // now copy the other files
            copyOtherFiles(m_inputDir);
            // finally create all the external links    
            createExternalLinks();
            m_report.println(Messages.get().container(Messages.RPT_HTML_IMPORT_END_0), I_CmsReport.C_FORMAT_HEADLINE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // clear memory
            clear();
        }

    }

    /**
     * Add a new external link to the storage of external links.<p>
     * 
     * All links in this storage are later used to create entries in the external link gallery.
     * @param externalLink link to an external resource
     */
    public void storeExternalLink(String externalLink) {

        m_externalLinks.add(externalLink);
    }

    /**
     * Add a new image info to the storage of image infos.<p>
     * 
     * The image infoes are later used to set the description properties of the images.
     * @param image the name of the image
     * @param altText the alt-text of the image
     */
    public void storeImageInfo(String image, String altText) {

        m_imageInfo.put(image, altText);
    }

    /**
     * Checks if the HtmlImportThread is still alive.<p>
     * 
     * @return true or false
     */
    public boolean threadAlive() {

        boolean alive = false;
        if (m_htmlImportThread != null) {
            alive = m_htmlImportThread.isAlive();
        }

        return alive;
    }

    /**
     * Translated a link into the real filesystem to its new location in the OpenCms VFS.<p>
     * 
     * This is needed by the HtmlConverter to get the correct links for link translation.
     * @param link link to the reafl filesystem
     * @return string containing absulute link into the OpenCms VFS
     */
    public String translateLink(String link) {

        String translatedLink = null;
        translatedLink = (String)m_fileIndex.get(link.replace('\\', '/'));

        if (translatedLink == null) {
            // its an anchor link, so copy use it
            if (link.startsWith("#")) {
                translatedLink = link;

            } else if (link.length() >= m_inputDir.length() + 1) {
                // create a 'faked' link into the VFS. Original link was
                // directing to a missing page, so let the link so to the
                // same page inside of OpenCms.
                String relativeFSName = link.substring(m_inputDir.length() + 1);
                translatedLink = m_destinationDir + relativeFSName;
            }
        }
        // if the link goes to a directory, lets link to the index page within
        if (translatedLink.endsWith("/")) {
            translatedLink += "index.html";
        }

        return translatedLink;
    }

    /**
     * Builds an index of all files to be imported and determines their new names in the OpenCms.<p>
     * @param startfolder the folder to start with
     */
    private void buildIndex(String startfolder) throws Exception {

        File folder = new File(startfolder);
        // get all subresources

        File[] subresources = folder.listFiles();
        // now loop through all subresources and add them to the index list
        for (int i = 0; i < subresources.length; i++) {
            try {

                String relativeFSName = subresources[i].getAbsolutePath().substring(m_inputDir.length() + 1);
                String absoluteVFSName = getVfsName(relativeFSName, subresources[i].getName(), subresources[i].isFile());
                m_report.print(Messages.get().container(Messages.RPT_CREATE_INDEX_0), I_CmsReport.C_FORMAT_NOTE);
                m_report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    relativeFSName.replace('\\', '/')));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                m_report.print(Messages.get().container(Messages.RPT_ARROW_RIGHT_0), I_CmsReport.C_FORMAT_NOTE);
                m_report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    absoluteVFSName));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                m_fileIndex.put(subresources[i].getAbsolutePath().replace('\\', '/'), absoluteVFSName);
                // if the subresource is a folder, get all subresources of it as well
                if (subresources[i].isDirectory()) {
                    buildIndex(subresources[i].getAbsolutePath());
                }
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.C_FORMAT_OK);
            } catch (Exception e) {
                LOG.error(e);
                m_report.println(e);
            }
        }
    }

    /**
     * Clear all used indices and lists.<p>
     * 
     * This should only be done when the import has been done. 
     */
    private void clear() {

        m_fileIndex = null;
        m_externalLinks = null;
        m_imageInfo = null;
    }

    /**
     * Copies all  HTML files to the VFS.<p>
     * 
     * @param startfolder startfolder the folder to start with
     * @throws CmsException if something goes wrong
     */
    private void copyHtmlFiles(String startfolder) throws Exception {

        try {
            File folder = new File(startfolder);
            // get all subresources
            File[] subresources = folder.listFiles();
            // now loop through all subresources 
            for (int i = 0; i < subresources.length; i++) {
                // if the subresource is a folder, get all subresources of it as well          
                if (subresources[i].isDirectory()) {
                    // first, create the folder in the VFS    
                    Hashtable properties = new Hashtable();
                    createFolder(subresources[i].getAbsolutePath(), i, properties);
                    // now process all rescources inside of the folder
                    copyHtmlFiles(subresources[i].getAbsolutePath());
                } else {
                    // create a new file in the VFS      
                    String vfsFileName = (String)m_fileIndex.get(subresources[i].getAbsolutePath().replace('\\', '/'));
                    // check if this is an Html file, do only import and parse those
                    int type = getFileType(vfsFileName);
                    if (CmsResourceTypePlain.getStaticTypeId() == type) {
                        Hashtable properties = new Hashtable();
                        // the subresource is a file, so start the parsing process
                        String content = new String();
                        try {
                            content = parseHtmlFile(subresources[i], properties);
                        } catch (CmsException e) {
                            m_report.println(e);
                        }
                        properties.put("template", m_template);

                        // create the file in the VFS
                        createFile(subresources[i].getAbsolutePath(), i, content, properties);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * Copies all files except HTML files to the VFS.<p>
     * 
     * @param startfolder startfolder the folder to start with
     * @throws CmsException if something goes wrong
     */
    private void copyOtherFiles(String startfolder) {

        try {
            File folder = new File(startfolder);
            // get all subresources
            File[] subresources = folder.listFiles();
            // now loop through all subresources 
            for (int i = 0; i < subresources.length; i++) {
                // if the subresource is a folder, get all subresources of it as well
                if (subresources[i].isDirectory()) {
                    copyOtherFiles(subresources[i].getAbsolutePath());
                } else {
                    // do not import the "meta.properties" file
                    if (!subresources[i].getName().equals(C_META_PROPERTIES)) {
                        // create a new file in the VFS      
                        String vfsFileName = (String)m_fileIndex.get(subresources[i].getAbsolutePath().replace(
                            '\\',
                            '/'));
                        // get the file type of the FS file
                        int type = getFileType(vfsFileName);
                        if (CmsResourceTypePlain.getStaticTypeId() != type) {

                            if (isExternal(vfsFileName)) {

                                m_report.print(
                                    Messages.get().container(Messages.RPT_SKIP_EXTERNAL_0),
                                    I_CmsReport.C_FORMAT_NOTE);
                                m_report.print(org.opencms.report.Messages.get().container(
                                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                                    subresources[i]));
                                m_report.print(org.opencms.report.Messages.get().container(
                                    org.opencms.report.Messages.RPT_DOTS_0));
                                m_report.print(
                                    Messages.get().container(Messages.RPT_ARROW_RIGHT_0),
                                    I_CmsReport.C_FORMAT_NOTE);
                                m_report.println(org.opencms.report.Messages.get().container(
                                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                                    vfsFileName));
                            } else {

                                m_report.print(
                                    Messages.get().container(Messages.RPT_IMPORT_0),
                                    I_CmsReport.C_FORMAT_NOTE);
                                m_report.print(org.opencms.report.Messages.get().container(
                                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                                    vfsFileName));
                                m_report.print(org.opencms.report.Messages.get().container(
                                    org.opencms.report.Messages.RPT_DOTS_0));

                                // get the content of the FS file
                                byte[] content = getFileBytes(subresources[i]);
                                // get the filename from the fileIndex list

                                // check if there are some image infos stored for this resource
                                List properties = new ArrayList();
                                String altText = (String)m_imageInfo.get(subresources[i].getAbsolutePath().replace(
                                    '\\',
                                    '/'));
                                CmsProperty property1 = new CmsProperty(
                                    CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                                    altText,
                                    altText);
                                CmsProperty property2 = new CmsProperty(
                                    CmsPropertyDefinition.PROPERTY_TITLE,
                                    altText,
                                    altText);
                                // add them to the title and description property
                                if (altText != null) {
                                    properties.add(property1);
                                    properties.add(property2);
                                }
                                // create the file
                                if (!m_overwriteMode) {
                                    m_cmsObject.createResource(vfsFileName, type, content, properties);
                                } else {
                                    try {
                                        CmsLock lock = m_cmsObject.getLock(vfsFileName);
                                        if (lock.getType() != CmsLock.C_TYPE_EXCLUSIVE) {
                                            m_cmsObject.lockResource(vfsFileName);
                                        }
                                        m_cmsObject.deleteResource(
                                            vfsFileName,
                                            I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
                                    } catch (CmsException e) {
                                        // the file did not exist, so create it                                     
                                    } finally {
                                        m_cmsObject.createResource(vfsFileName, type, content, properties);
                                    }

                                    m_report.print(
                                        Messages.get().container(Messages.RPT_OVERWRITE_0),
                                        I_CmsReport.C_FORMAT_NOTE);
                                    m_report.print(org.opencms.report.Messages.get().container(
                                        org.opencms.report.Messages.RPT_DOTS_0));
                                }
                                m_report.println(org.opencms.report.Messages.get().container(
                                    org.opencms.report.Messages.RPT_OK_0), I_CmsReport.C_FORMAT_OK);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            m_report.println(e);
        }
    }

    /**
     * Creates all external links, which were found during the HTML-page processing.<p>
     * 
     */
    private void createExternalLinks() {

        // loop through all links
        Iterator i = m_externalLinks.iterator();
        while (i.hasNext()) {
            String linkUrl = (String)i.next();
            String filename = linkUrl.substring(linkUrl.indexOf("://") + 3, linkUrl.length());
            filename = m_cmsObject.getRequestContext().getFileTranslator().translateResource(filename.replace('/', '-'));

            m_report.print(Messages.get().container(Messages.RPT_CREATE_EXTERNAL_LINK_0), I_CmsReport.C_FORMAT_NOTE);
            m_report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                filename));
            m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            List properties = new ArrayList();
            CmsProperty property1 = new CmsProperty(
                CmsPropertyDefinition.PROPERTY_TITLE,
                "Link to " + linkUrl,
                "Link to " + linkUrl);
            properties.add(property1);
            try {
                m_cmsObject.createResource(
                    m_linkGallery + filename,
                    CmsResourceTypePointer.getStaticTypeId(),
                    linkUrl.getBytes(),
                    properties);
            } catch (CmsException e) {
                // do nothing here, an exception will be thrown if this link already exisits                
            }
            m_report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.C_FORMAT_OK);
        }
    }

    /**
     * Creates a file in the VFS.<p>
     * 
     * @param filename the complete filename in the real file system
     * @param position the default nav pos of this folder
     * @param content the html content of the file
     * @param properties the file properties
     */
    private void createFile(String filename, int position, String content, Hashtable properties) {

        String vfsFileName = (String)m_fileIndex.get(filename.replace('\\', '/'));

        if (vfsFileName != null) {
            try {

                m_report.print(Messages.get().container(Messages.RPT_CREATE_FILE_0), I_CmsReport.C_FORMAT_NOTE);
                m_report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    vfsFileName));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                // check if we have to set the navpos property.
                if ((properties.get(CmsPropertyDefinition.PROPERTY_NAVPOS) == null)
                    && (properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT) != null)) {
                    // set the position in the folder as navpos
                    // we have to add one to the postion, since it is counted from 0
                    properties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, (position + 1) + "");
                }

                // create new xml page
                CmsXmlPage page = new CmsXmlPage(m_locale, OpenCms.getSystemInfo().getDefaultEncoding());
                page.addValue(m_element, m_locale);
                page.setStringValue(m_cmsObject, m_element, m_locale, content);

                // check links
                CmsLinkTable linkTable = page.getLinkTable(m_element, m_locale);
                Iterator i = linkTable.iterator();
                while (i.hasNext()) {
                    CmsLink link = (CmsLink)i.next();
                    String target = link.getTarget();
                    // do only update internal links 
                    if (target.indexOf("://") == 0) {
                        //if (!target.startsWith("http") && !target.startsWith("mailto")) {
                        target = m_cmsObject.getRequestContext().getFileTranslator().translateResource(target);
                        // update link
                        link.updateLink(target, link.getAnchor(), link.getQuery());
                    }
                }
                // marshal xml page and get the content
                byte[] contentByteArray = page.marshal();
                List oldProperties = new ArrayList();

                if (!m_overwriteMode) {
                    m_cmsObject.createResource(
                        vfsFileName,
                        CmsResourceTypeXmlPage.getStaticTypeId(),
                        contentByteArray,
                        new ArrayList());
                } else {
                    try {
                        // try if the file is there
                        oldProperties = m_cmsObject.readPropertyObjects(vfsFileName, false);
                        CmsLock lock = m_cmsObject.getLock(vfsFileName);
                        if (lock.getType() != CmsLock.C_TYPE_EXCLUSIVE) {
                            m_cmsObject.lockResource(vfsFileName);
                        }
                        m_cmsObject.deleteResource(vfsFileName, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
                    } catch (CmsException e) {
                        // the file did not exist, so we do not have to delete it                      
                    } finally {
                        // create the new resource
                        m_report.print(Messages.get().container(Messages.RPT_OVERWRITE_0), I_CmsReport.C_FORMAT_NOTE);
                        m_report.print(org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_DOTS_0));
                        m_cmsObject.createResource(
                            vfsFileName,
                            CmsResourceTypeXmlPage.getStaticTypeId(),
                            contentByteArray,
                            new ArrayList());
                    }
                }
                // create all properties and put them in an ArrayList
                Enumeration en = properties.keys();
                List propertyList = new ArrayList();
                while (en.hasMoreElements()) {
                    // get property and value
                    String propertyKey = (String)en.nextElement();
                    String propertyVal = (String)properties.get(propertyKey);
                    // create new Property Object
                    CmsProperty property = new CmsProperty(propertyKey, propertyVal, propertyVal);
                    // create implicitly if Property doesn't exist already
                    property.setAutoCreatePropertyDefinition(true);
                    // add new property to the list
                    propertyList.add(property);
                }
                // try to write the property
                try {
                    m_cmsObject.writePropertyObjects(vfsFileName, propertyList);
                    // write the old properties if available
                    m_cmsObject.writePropertyObjects(vfsFileName, oldProperties);
                } catch (CmsException e1) {
                    e1.printStackTrace();
                }
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.C_FORMAT_OK);
            } catch (CmsException e) {
                m_report.println(e);
                LOG.error(e);
            }
        }
    }

    /**
     * Creates a folder in the VFS.<p>
     * 
     * @param foldername the complete foldername in the real file system
     * @param position the default nav pos of this folder
     * @param properties the file properties
     */
    private void createFolder(String foldername, int position, Hashtable properties) {

        String vfsFolderName = (String)m_fileIndex.get(foldername.replace('\\', '/'));

        m_report.print(Messages.get().container(Messages.RPT_CREATE_FOLDER_0), I_CmsReport.C_FORMAT_NOTE);
        m_report.print(org.opencms.report.Messages.get().container(
            org.opencms.report.Messages.RPT_ARGUMENT_1,
            vfsFolderName));
        m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

        if (vfsFolderName != null) {
            String path = vfsFolderName.substring(
                0,
                vfsFolderName.substring(0, vfsFolderName.length() - 1).lastIndexOf("/"));
            String folder = vfsFolderName.substring(path.length(), vfsFolderName.length());
            try {
                // try to find a meta.properties file in the folder
                String propertyFileName = foldername + File.separator + C_META_PROPERTIES;

                boolean metaPropertiesFound = false;
                ExtendedProperties propertyFile = new ExtendedProperties();
                try {
                    propertyFile.load(new FileInputStream(new File(propertyFileName)));
                    metaPropertiesFound = true;
                } catch (Exception e1) {
                    // do nothing if the propertyfile could not be loaded since it is not required
                    // that such s file does exist
                }
                // now copy all values from the propertyfile to the already found properties of the
                // new folder in OpenCms
                // only do this if we have found a meta.properties file          
                if (metaPropertiesFound) {
                    Enumeration enu = propertyFile.keys();
                    String property = "";
                    while (enu.hasMoreElements()) {
                        // get property and value
                        try {
                            property = (String)enu.nextElement();
                            String propertyvalue = (String)propertyFile.get(property);
                            // copy to the properties of the OpenCms folder
                            properties.put(property, propertyvalue);
                        } catch (Exception e2) {
                            // just skip this property if it could ne be read.
                            e2.printStackTrace();
                        }
                    }

                    // check if we have to set the navpos property.
                    if (properties.get(CmsPropertyDefinition.PROPERTY_NAVPOS) == null) {
                        // set the position in the folder as navpos
                        // we have to add one to the postion, since it is counted from 0
                        properties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, (position + 1) + "");
                    }
                    // check if we have to set the navpos property.
                    if (properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT) == null) {
                        // set the foldername in the folder as navtext
                        String navtext = folder.substring(1, 2).toUpperCase()
                            + folder.substring(2, folder.length() - 1);
                        properties.put(CmsPropertyDefinition.PROPERTY_NAVTEXT, navtext);
                    }
                } else {
                    // if there was no meta.properties file, no properties should be added to the
                    // folder
                    properties = new Hashtable();
                }
                // try to read the folder, it its there we must not create it again
                try {
                    m_cmsObject.readFolder(path + folder);
                    m_cmsObject.lockResource(path + folder);
                } catch (CmsException e1) {
                    // the folder was not there, so create it
                    m_cmsObject.createResource(path + folder, CmsResourceTypeFolder.getStaticTypeId());
                }
                // create all properties and put them in an ArrayList
                Enumeration enu = properties.keys();
                List propertyList = new ArrayList();
                while (enu.hasMoreElements()) {
                    // get property and value
                    String propertyKey = (String)enu.nextElement();
                    String propertyVal = (String)properties.get(propertyKey);
                    CmsProperty property = new CmsProperty(propertyKey, propertyVal, propertyVal);
                    // create implicitly if Property doesn't exist already
                    property.setAutoCreatePropertyDefinition(true);
                    // add new property to the list
                    propertyList.add(property);
                }
                // try to write the property Objects
                try {
                    m_cmsObject.writePropertyObjects(path + folder, propertyList);
                } catch (CmsException e1) {
                    e1.printStackTrace();
                }
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.C_FORMAT_OK);
            } catch (CmsException e) {
                m_report.println(e);
                LOG.error(e);
            }
        }
    }

    /**
     * Returns a byte array containing the content of server FS file.<p>
     *
     * @param file the name of the file to read
     * @return bytes[] the content of the file
     * @throws CmsException if something goes wrong
     */
    private byte[] getFileBytes(File file) throws CmsException {

        byte[] buffer = null;

        FileInputStream fileStream = null;
        int charsRead;
        int size;
        try {
            fileStream = new FileInputStream(file);
            charsRead = 0;
            size = new Long(file.length()).intValue();
            buffer = new byte[size];
            while (charsRead < size) {
                charsRead += fileStream.read(buffer, charsRead, size - charsRead);
            }
            return buffer;
        } catch (IOException e) {
            throw new CmsDbIoException(
                Messages.get().container(Messages.ERR_GET_FILE_BYTES_1, file.getAbsolutePath()),
                e);
        } finally {
            try {
                if (fileStream != null) {
                    fileStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the OpenCms file type of a real filesystem file. <p>
     * 
     * This is made by checking the extension.
     * 
     * @param filename the name of the file in the real filesystem  
     * @return the id of the OpenCms file type
     */
    private int getFileType(String filename) throws Exception {

        String extension = "";
        if (filename.indexOf(".") > -1) {
            extension = filename.substring((filename.lastIndexOf(".")));
        }

        String typename = (String)m_extensions.get(extension.toLowerCase());
        if (typename == null) {
            typename = "binary";
        }
        CmsResourceManager resourceManager = OpenCms.getResourceManager();

        return resourceManager.getResourceType(typename).getTypeId();
    }

    /**
     * Gets a valid VfsName form a given name in the real filesystem.<p>
     * 
     * This name will ater be used for all link translations during the HTML-parsing process.
     * @param relativeName the name in the real fielsystem, relative to the start folder
     * @param name the name of the file
     * @param isFile flag to indicate that the resource is a file
     * @return a valid name in the VFS
     */
    private String getVfsName(String relativeName, String name, boolean isFile) throws Exception {

        // first translate all fileseperators to the valid "/" in OpenCms
        String vfsName = relativeName.replace('\\', '/');
        // the resource is a file
        if (isFile) {
            // we must check if it might be copied into a gallery. this can be done by checking the
            // file extension
            int filetype = getFileType(name);

            // there is no name before the ".extension"
            if (name.indexOf(".") == 0) {
                name = "unknown" + name;
                int dot = relativeName.lastIndexOf(".");

                relativeName = relativeName.substring(0, dot) + name;
            }

            // depending on the filetype, the resource must be moved into a speical folder in 
            // OpenCms:
            // images -> move into image gallery
            // binary -> move into download gallery
            // plain -> move into destination folder
            // other -> move into download gallery
            if (CmsResourceTypeImage.getStaticTypeId() == filetype) {
                // move to image gallery
                // as the image gallery is "flat", we must use the file name and not the complete
                // relative name
                vfsName = m_imageGallery + name;
            } else if (CmsResourceTypePlain.getStaticTypeId() == filetype) {
                // move to destination folder
                //vfsName=m_destinationDir+relativeName;

                // we have to check if there is a folder with the same name but without extension
                // if so, we will move the file into the folder and name it "index.html"
                String folderName = relativeName;
                if (folderName.indexOf(".") > 0) {
                    folderName = folderName.substring(0, folderName.indexOf("."));
                }
                folderName = m_inputDir + "\\" + folderName;
                File folder = new File(folderName);

                if ((folder != null) && (folder.isDirectory())) {
                    vfsName = m_destinationDir + relativeName.substring(0, relativeName.indexOf(".")) + "/index.html";
                    // System.err.println("MOVING "+ relativeName + " -> " + name.substring(0,name.indexOf("."))+"/index.html");
                } else {
                    // move to destination folder
                    vfsName = m_destinationDir + relativeName;
                }

            } else {
                // everything else will be moved to the download gallery.
                // as the download gallery is "flat", we must use the file name and not the complete
                // relative name
                vfsName = m_downloadGallery + name;
            }
            // now we have the filename in the VFS. its possible that a file with the same name
            // is already existing, in this case, we have to adjust the filename.        
            return validateFilename(vfsName);
        } else {
            // folders are always moved to the destination folder
            vfsName = m_destinationDir + vfsName + "/";
            return vfsName;
        }
    }

    /**
     * Tests if a filename is an external name, i.e. this name does not point into the OpenCms Vfs.<p>
     * 
     * A filename is an external name if it contains the string "://", e.g. "http://" or "ftp://"
     * @param filename the filename to test 
     * @return true or false
     */
    private boolean isExternal(String filename) {

        boolean external = false;
        if (filename.indexOf("://") > 0) {
            external = true;
        }
        return external;
    }

    /** 
     * Checks if m_element is valid element.<p>
     * 
     * @return true if element is valid, otherwise false
     */
    private boolean isValidElement() {

        boolean validElement = false;
        List elementList = new ArrayList();
        try {
            // get Elements of template stored in Property "template-elements"
            String elements = m_cms.property(CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS, m_template);
            // template may contain more than one Element
            // Elements are seperated by the delimiter ","
            if (elements != null) {
                StringTokenizer T = new StringTokenizer(elements, ",");
                while (T.hasMoreTokens()) {
                    // current element probably looks like "body*|Body" <name><mandatory>|<nicename>
                    String currentElement = T.nextToken();
                    int sepIndex = currentElement.indexOf("|");
                    if (sepIndex != -1) {
                        // current element == "body*"
                        currentElement = currentElement.substring(0, sepIndex);
                    }
                    if (currentElement.endsWith("*")) {
                        // current element == "body"
                        currentElement = currentElement.substring(0, currentElement.length() - 1);
                    }
                    elementList.add(currentElement);
                }
            }
            if (elementList.contains(m_element)) {
                validElement = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return validElement;
    }

    /**
     * Reads the content of an Html file from the real file system and parses it for link
     * transformation.<p>
     * 
     * @param  file the filein the real file system
     * @param properties the file properties
     * @return the modified Html code of the file
     * @throws CmsException if something goes wrong
     */
    private String parseHtmlFile(File file, Hashtable properties) throws CmsException {

        String parsedHtml = "";
        try {

            byte[] content = getFileBytes(file);

            // use the correct encoding to get the string from the file bytes
            String contentString = new String(content, m_inputEncoding);
            // escape the string to remove all special chars
            contentString = CmsEncoder.escapeNonAscii(contentString);
            // we must substitute all occurences of "&#", otherwiese tidy would remove them
            contentString = substitute(contentString, "&#", "{subst}");
            // parse the content                  
            parsedHtml = m_htmlConverter.convertHTML(
                file.getAbsolutePath(),
                contentString,
                m_startPattern,
                m_endPattern,
                properties);
            // resubstidute the converted HTML code
            parsedHtml = substitute(parsedHtml, "{subst}", "&#");
        } catch (Exception e) {
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_HTMLIMPORT_PARSE_1,
                file.getAbsolutePath());
            LOG.error(message.key(), e);
            throw new CmsImportExportException(message, e);
        }
        return parsedHtml;
    }

    /**
     * Validates a fielname for OpenCms.<p>
     * 
     * This method checks if there are any illegal characters in the fielname and modifies them
     * if nescessary. In addition it ensures that no dublicate filenames are created.
     * 
     * @param filename the filename to validate
     * @return a validated and unique filename in OpenCms
     */
    private String validateFilename(String filename) {

        // if its an external filename, use it directley        
        if (isExternal(filename)) {
            return filename;
        }

        // check if this resource name does already exist
        // if so add a postfix to the name

        int postfix = 1;
        boolean found = true;
        String validFilename = filename.toLowerCase();

        // if we are not in overwrite mode, we must find a valid, non-existing filename
        // otherwise we will use the current translated name
        if (!m_overwriteMode) {

            while (found) {
                try {
                    // get the translated name, this one only contains valid chars in OpenCms       
                    validFilename = m_cmsObject.getRequestContext().getFileTranslator().translateResource(validFilename);

                    // try to read the file.....
                    found = true;
                    // first try to read it form the fileIndex of already processed files
                    if (!m_fileIndex.containsValue(validFilename.replace('\\', '/'))) {
                        found = false;
                    }
                    if (!found) {
                        found = true;
                        // there was no entry in the fileIndex, so try to read from the VFS         
                        m_cmsObject.readResource(validFilename, CmsResourceFilter.ALL);
                    }
                    // ....it's there, so add a postfix and try again
                    String path = filename.substring(0, filename.lastIndexOf("/") + 1);
                    String name = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
                    validFilename = path;
                    if (name.lastIndexOf(".") > 0) {
                        validFilename += name.substring(0, name.lastIndexOf("."));
                    } else {
                        validFilename += name;
                    }
                    validFilename += "_" + postfix;
                    if (name.lastIndexOf(".") > 0) {
                        validFilename += name.substring(name.lastIndexOf("."), name.length());
                    }
                    postfix++;
                } catch (CmsException e) {
                    // the file does not exist, so we can use this filename                               
                    found = false;
                }
            }

        } else {
            validFilename = validFilename.replace('\\', '/');
        }

        return OpenCms.getResourceManager().getFileTranslator().translateResource(validFilename);
    }

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportManager.java,v $
 * Date   : $Date: 2005/05/02 13:33:48 $
 * Version: $Revision: 1.99 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsI18nInfo;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

/**
 * Provides the functionaility to export resources from the OpenCms VFS
 * to the file system.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Moossen (a.moossen@alkacon.com)
 * @version $Revision: 1.99 $
 */
public class CmsStaticExportManager implements I_CmsEventListener {

    /** Marker for error message attribute. */
    public static final String EXPORT_ATTRIBUTE_ERROR_MESSAGE = "javax.servlet.error.message";

    /** Marker for error request uri attribute. */
    public static final String EXPORT_ATTRIBUTE_ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

    /** Marker for error servlet name attribute. */
    public static final String EXPORT_ATTRIBUTE_ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";

    /** Marker for error status code attribute. */
    public static final String EXPORT_ATTRIBUTE_ERROR_STATUS_CODE = "javax.servlet.error.status_code";

    /** Name for the folder default index file. */
    public static final String EXPORT_DEFAULT_FILE = "index_export.html";

    /** Flag value for links without paramerters. */
    public static final int EXPORT_LINK_WITH_PARAMETER = 2;

    /** Flag value for links without paramerters. */
    public static final int EXPORT_LINK_WITHOUT_PARAMETER = 1;

    /** Marker for externally redirected 404 uri's. */
    public static final String EXPORT_MARKER = "exporturi";

    /** Time given (in seconds) to the static export handler to finish a publish task. */
    public static final int HANDLER_FINISH_TIME = 60;

    /** Cache value to indicate a true 404 error. */
    private static final String CACHEVALUE_404 = "?404";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsStaticExportManager.class);

    /** Matcher for  selecting those resources which should be part of the staic export. */
    private static CmsExportFolderMatcher m_exportFolderMatcher;

    /** HTTP header Accept-Charset. */
    private String m_acceptCharsetHeader;

    /** HTTP header Accept-Language. */
    private String m_acceptLanguageHeader;

    /** Cache for the export links. */
    private Map m_cacheExportLinks;

    /** Cache for the export uris. */
    private Map m_cacheExportUris;

    /** Cache for the online links. */
    private Map m_cacheOnlineLinks;

    /** Cache for the secure links. */
    private Map m_cacheSecureLinks;

    /** OpenCms default charset header. */
    private String m_defaultAcceptCharsetHeader;

    /** OpenCms default locale header. */
    private String m_defaultAcceptLanguageHeader;

    /** List of export resources which should be part of the static export. */
    private List m_exportFolders;

    /** The additional http headers for the static export. */
    private List m_exportHeaders;

    /** List of all resources that have the "exportname" property set. */
    private Map m_exportnameResources;

    /** Indicates if <code>true</code> is the default value for the property "export". */
    private boolean m_exportPropertyDefault;

    /** Indicates if links in the static export should be relative. */
    private boolean m_exportRelativeLinks;

    /** List of export suffixes where the "export" property default is always "true". */
    private List m_exportSuffixes;

    /** Export url to send internal requests to. */
    private String m_exportUrl;

    /** Export url with unstubstituted context values. */
    private String m_exportUrlConfigured;

    /** Handler class for static export. */
    private I_CmsStaticExportHandler m_handler;

    /** Indicates if the quick static export for plain resources is enabled. */
    private boolean m_quickPlainExport;

    /** Remote addr. */
    private String m_remoteAddr;

    /** Prefix to use for exported files. */
    private String m_rfsPrefix;

    /** Prefix to use for exported files with unstubstituted context values. */
    private String m_rfsPrefixConfigured;

    /** Indicates if the static export is enabled or diabled. */
    private boolean m_staticExportEnabled;

    /** The path to where the static export will be written. */
    private String m_staticExportPath;

    /** The path to where the static export will be written without the complete rfs path. */
    private String m_staticExportPathConfigured;

    /** Vfs Name of a resource used to do a "static export required" test. */
    private String m_testResource;

    /** Prefix to use for internal OpenCms files. */
    private String m_vfsPrefix;

    /** Prefix to use for internal OpenCms files with unstubstituted context values. */
    private String m_vfsPrefixConfigured;

    /**
     * Creates a new static export property object.<p>
     * 
     */
    public CmsStaticExportManager() {

        m_exportSuffixes = new ArrayList();
        m_exportFolders = new ArrayList();
        m_exportHeaders = new ArrayList();
    }

    /**
     * Caches a calculated export uri.<p>
     * 
     * @param rfsName the name of the resource in the "real" file system
     * @param vfsName the name of the resource in the VFS
     */
    public void cacheExportUri(Object rfsName, Object vfsName) {

        m_cacheExportUris.put(rfsName, vfsName);
    }

    /**
     * Caches a calculated online link.<p>
     * 
     * @param linkName the link
     * @param vfsName the name of the VFS resource 
     */
    public void cacheOnlineLink(Object linkName, Object vfsName) {

        m_cacheOnlineLinks.put(linkName, vfsName);
    }

    /**
     * Implements the CmsEvent interface,
     * the static export properties uses the events to clear 
     * the list of cached keys in case a project is published.<p>
     *
     * @param event CmsEvent that has occurred
     */
    public synchronized void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_UPDATE_EXPORTS:
                scrubExportFolder();
                clearCaches(event);
                break;
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                // event data contains a list of the published resources
                CmsUUID publishHistoryId = new CmsUUID((String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_EVENT_PUBLISH_PROJECT_1, publishHistoryId));
                }
                I_CmsReport report = (I_CmsReport)event.getData().get(I_CmsEventListener.KEY_REPORT);
                getHandler().performEventPublishProject(publishHistoryId, report);

                clearCaches(event);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_EVENT_PUBLISH_PROJECT_FINISHED_1, publishHistoryId));
                }

                break;
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                clearCaches(event);
                break;
            default:
        // no operation
        }
    }

    /**
     * Exports the requested uri and at the same time writes the uri to the response output stream
     * if required.<p>
     * 
     * @param req the current request
     * @param res the current response
     * @param cms an initialized cms context (should be initialized with the "Guest" user only)
     * @param data the static export data set
     * @return status code of the export operation, status codes are the same as http status codes (200,303,304)
     * @throws CmsException in case of errors accessing the VFS
     * @throws ServletException in case of errors accessing the servlet 
     * @throws IOException in case of erros writing to the export output stream
     * @throws CmsStaticExportException if static export is disabled
     */
    public int export(HttpServletRequest req, HttpServletResponse res, CmsObject cms, CmsStaticExportData data)
    throws CmsException, IOException, ServletException, CmsStaticExportException {

        int status = -1;

        CmsFile file;

        String vfsName = data.getVfsName();
        String rfsName = data.getRfsName();
        CmsResource resource = data.getResource();

        // cut the site root from the vfsName and switch to the correct site
        String siteRoot = CmsSiteManager.getSiteRoot(vfsName);

        CmsI18nInfo i18nInfo = OpenCms.getLocaleManager().getI18nInfo(
            req,
            cms.getRequestContext().currentUser(),
            cms.getRequestContext().currentProject(),
            vfsName);

        String remoteAddr = m_remoteAddr;
        if (remoteAddr == null) {
            remoteAddr = I_CmsConstants.C_IP_LOCALHOST;
        }
        CmsContextInfo contextInfo = new CmsContextInfo(
            cms.getRequestContext().currentUser(),
            cms.getRequestContext().currentProject(),
            vfsName,
            "/",
            i18nInfo.getLocale(),
            i18nInfo.getEncoding(),
            remoteAddr);

        cms = OpenCms.initCmsObject(null, contextInfo);

        if (siteRoot != null) {
            vfsName = vfsName.substring(siteRoot.length());
        } else {
            siteRoot = "/";
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_STATIC_EXPORT_SITE_ROOT_2, siteRoot, vfsName));
        }
        cms.getRequestContext().setSiteRoot(siteRoot);

        String oldUri = null;

        // this flag signals if the export method is used for "on demand" or "after publish". 
        // if no request and result stream are available, it was called during "export on publish"
        boolean exportOnDemand = ((req != null) && (res != null));

        CmsStaticExportResponseWrapper wrapRes = null;
        if (res != null) {
            wrapRes = new CmsStaticExportResponseWrapper(res);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SE_RESOURCE_START_1, data));
        }

        // read vfs resource
        if (resource.isFile()) {
            file = cms.readFile(vfsName);
        } else {
            file = CmsFile.upgrade(OpenCms.initResource(cms, vfsName, req, wrapRes), cms);
            if (cms.existsResource(vfsName + file.getName())) {
                vfsName = vfsName + file.getName();
            }
            rfsName += EXPORT_DEFAULT_FILE;
        }

        // check loader id for resource
        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(file);
        if ((loader == null) || (!loader.isStaticExportEnabled())) {
            Object[] arguments = new Object[] {vfsName, new Integer(file.getTypeId())};
            throw new CmsStaticExportException(Messages.get().container(Messages.ERR_EXPORT_NOT_SUPPORTED_2, arguments));
        }

        FileOutputStream exportStream = null;
        File exportFile = null;
        String exportFileName = CmsFileUtil.normalizePath(getExportPath() + rfsName);

        // only export those resource where the export property is set
        if (isExportLink(cms, vfsName)) {
            // make sure all required parent folder exist
            createExportFolder(rfsName);
            status = HttpServletResponse.SC_OK;
            // generate export file instance and output stream
            exportFile = new File(exportFileName);
        } else {
            // the resource was not used for export, so return HttpServletResponse.SC_SEE_OTHER
            // as a signal for not exported resource
            status = HttpServletResponse.SC_SEE_OTHER;
        }

        // ensure we have exactly the same setup as if called "the usual way"
        // we only have to do this in case of the static export on demand
        if (exportOnDemand) {
            String mimetype = OpenCms.getResourceManager().getMimeType(
                file.getName(),
                cms.getRequestContext().getEncoding());
            if (wrapRes != null) {
                wrapRes.setContentType(mimetype);
            }
            oldUri = cms.getRequestContext().getUri();
            cms.getRequestContext().setUri(vfsName);
        }

        // do the export
        byte[] result = loader.export(cms, file, req, wrapRes);

        // release unused resources
        file = null;

        if (result != null) {
            if (exportFile != null) {
                // write new exported file content
                try {
                    exportStream = new FileOutputStream(exportFile);
                    exportStream.write(result);
                    exportStream.close();

                } catch (Throwable t) {
                    throw new CmsStaticExportException(Messages.get().container(
                        Messages.ERR_OUTPUT_STREAM_1,
                        exportFileName), t);
                }
                // update the file with the modification date from the server
                if (req != null) {
                    Long dateLastModified = (Long)req.getAttribute(I_CmsConstants.C_HEADER_OPENCMS_EXPORT);
                    if ((dateLastModified != null) && (dateLastModified.longValue() != -1)) {
                        exportFile.setLastModified((dateLastModified.longValue() / 1000) * 1000);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().key(
                                Messages.LOG_SET_LAST_MODIFIED_2,
                                exportFile.getName(),
                                new Long((dateLastModified.longValue() / 1000) * 1000)));
                        }
                    }
                } else {
                    // otherweise take the last modification date form the OpenCms resource
                    exportFile.setLastModified((resource.getDateLastModified() / 1000) * 1000);
                }
            }
            // get the status that was set 
            status = (wrapRes != null) ? wrapRes.getStatus() : -1;
            if (status < 0) {
                // the status was not set, assume everything is o.k.
                status = HttpServletResponse.SC_OK;
            }
        } else {
            // the resource was not written because it was not modified. 
            // set the status to not modified
            status = HttpServletResponse.SC_NOT_MODIFIED;
        }

        // restore context
        // we only have to do this in case of the static export on demand
        if (exportOnDemand) {
            cms.getRequestContext().setUri(oldUri);
        }

        // log export success 
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.LOG_STATIC_EXPORTED_2, vfsName, exportFileName));
        }

        return status;
    }

    /**
     * Starts a complete static export of all resources.<p>
     * 
     * @param purgeFirst flag to delete all resources in the export folder of the rfs
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file   
     * 
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    public synchronized void exportFullStaticRender(boolean purgeFirst, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        // delete all old exports if the purgeFirst flag is set
        if (purgeFirst) {

            Map eventData = new HashMap();
            eventData.put(I_CmsEventListener.KEY_REPORT, report);
            CmsEvent clearCacheEvent = new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, eventData);
            OpenCms.fireCmsEvent(clearCacheEvent);

            scrubExportFolder();
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            cms.deleteAllStaticExportPublishedResources(EXPORT_LINK_WITHOUT_PARAMETER);
            cms.deleteAllStaticExportPublishedResources(EXPORT_LINK_WITH_PARAMETER);
        }

        //export must be done in the context of the export user    
        CmsObject cmsExportObject = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

        List publishedResources = getAllResources(cmsExportObject);

        // do the export
        CmsAfterPublishStaticExportHandler handler = new CmsAfterPublishStaticExportHandler();
        handler.doExportAfterPublish(publishedResources, report);
    }

    /**
     * Exports all non template resources found in a list of published resources.<p>
     * 
     * @param cms the current cms object
     * @param publishedResources the list of published resources
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     * 
     * @return true if some template resources were found whil looping the list of published resources
     * 
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    public boolean exportNonTemplateResources(CmsObject cms, List publishedResources, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        String vfsName = null;
        List resourcesToExport = new ArrayList();
        boolean templatesFound = false;

        int count = 1;

        report.println(report.key("report.staticexport.nontemplateresources_begin"), I_CmsReport.C_FORMAT_HEADLINE);

        // loop through all resources
        Iterator i = publishedResources.iterator();

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_EXPORTING_NON_TEMPLATE_1, new Integer(publishedResources.size())));
        }

        while (i.hasNext()) {
            CmsPublishedResource pupRes = (CmsPublishedResource)i.next();

            vfsName = pupRes.getRootPath();

            // only process this resource, if it is within the tree of allowed folders for static export
            if (m_exportFolderMatcher.match(vfsName)) {

                // only export VFS files, other data is handled elsewhere 
                if (pupRes.isVfsResource() && (pupRes.isFile())) {
                    // get the export data object, if null is returned, this resource cannot be exported
                    CmsStaticExportData exportData = getExportData(vfsName, cms);

                    if (exportData != null) {
                        // check loader for current resource if it must be processed before exported
                        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(exportData.getResource());
                        if (!loader.isStaticExportProcessable()) {
                            // this resource must not be process, so export it if its not marked as deleted
                            if (pupRes.getState() != I_CmsConstants.C_STATE_DELETED) {
                                // mark the resource for export to the real file system                  
                                resourcesToExport.add(exportData);
                            }
                        } else {
                            // the resource is a template resource, so store the name of it in the DB for further use                  
                            templatesFound = true;
                            cms.writeStaticExportPublishedResource(
                                exportData.getRfsName(),
                                EXPORT_LINK_WITHOUT_PARAMETER,
                                "",
                                System.currentTimeMillis());
                        }
                    }
                }
            }
        }

        // now do the export
        i = resourcesToExport.iterator();
        int size = resourcesToExport.size();

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_NUM_EXPORT_1, new Integer(size)));
        }
        while (i.hasNext()) {
            CmsStaticExportData exportData = (CmsStaticExportData)i.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(
                    Messages.LOG_EXPORT_FILE_2,
                    exportData.getVfsName(),
                    exportData.getRfsName()));
            }

            report.print("(" + count++ + " / " + size + ") ", I_CmsReport.C_FORMAT_NOTE);
            report.print(report.key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
            report.print(exportData.getVfsName());
            report.print(report.key("report.dots"));
            int status = export(null, null, cms, exportData);
            if (status == HttpServletResponse.SC_OK) {
                report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            } else {
                report.println(report.key("report.ignored"), I_CmsReport.C_FORMAT_NOTE);
            }

            if (LOG.isInfoEnabled()) {
                Object[] arguments = new Object[] {
                    exportData.getVfsName(),
                    exportData.getRfsName(),
                    new Integer(status)};
                LOG.info(Messages.get().key(Messages.LOG_EXPORT_FILE_STATUS_3, arguments));
            }
        }

        resourcesToExport = null;

        report.println(report.key("report.staticexport.nontemplateresources_end"), I_CmsReport.C_FORMAT_HEADLINE);

        return templatesFound;

    }

    /**
     * Exports all template resources found in a list of published resources.<p>
     * 
     * @param publishedTemplateResources list of potential candidates to export
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file    
     */
    public void exportTemplateResources(List publishedTemplateResources, I_CmsReport report) {

        int size = publishedTemplateResources.size();
        int count = 1;

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_EXPORT_TEMPLATES_1, new Integer(size)));
        }
        report.println(report.key("report.staticexport.templateresources_begin"), I_CmsReport.C_FORMAT_HEADLINE);

        // now loop through all of them and request them from the server
        Iterator i = publishedTemplateResources.iterator();

        while (i.hasNext()) {
            String rfsName = (String)i.next();

            report.print("(" + count++ + " / " + size + ") ", I_CmsReport.C_FORMAT_NOTE);
            report.print(report.key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
            report.print(rfsName);
            report.print(report.key("report.dots"));

            String exportUrlStr = getExportUrl() + getRfsPrefix() + rfsName;

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_SENDING_REQUEST_2, rfsName, exportUrlStr));
            }

            try {
                // setup the connection and request the resource
                URL exportUrl = new URL(exportUrlStr);
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection urlcon = (HttpURLConnection)exportUrl.openConnection();
                // set request type to GET
                urlcon.setRequestMethod("GET");
                // add special export header
                urlcon.setRequestProperty(I_CmsConstants.C_HEADER_OPENCMS_EXPORT, "true");
                // add additional headers if available
                if (getAcceptLanguageHeader() != null) {
                    urlcon.setRequestProperty(I_CmsConstants.C_HEADER_ACCEPT_LANGUAGE, getAcceptLanguageHeader());
                } else {
                    urlcon.setRequestProperty(I_CmsConstants.C_HEADER_ACCEPT_LANGUAGE, m_defaultAcceptLanguageHeader);
                }
                if (getAcceptCharsetHeader() != null) {
                    urlcon.setRequestProperty(I_CmsConstants.C_HEADER_ACCEPT_CHARSET, getAcceptCharsetHeader());
                } else {
                    urlcon.setRequestProperty(I_CmsConstants.C_HEADER_ACCEPT_CHARSET, m_defaultAcceptCharsetHeader);
                }

                // get the last modified date and add it to the request
                String exportFileName = CmsFileUtil.normalizePath(getExportPath() + rfsName);
                File exportFile = new File(exportFileName);
                if (exportFile != null) {
                    long dateLastModified = exportFile.lastModified();
                    urlcon.setIfModifiedSince(dateLastModified);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().key(
                            Messages.LOG_IF_MODIFIED_SINCE_SET_2,
                            exportFile.getName(),
                            new Long((dateLastModified / 1000) * 1000)));
                    }
                }

                // now perform the request
                urlcon.connect();
                int status = urlcon.getResponseCode();
                urlcon.disconnect();
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().key(
                        Messages.LOG_REQUEST_RESULT_3,
                        rfsName,
                        exportUrlStr,
                        new Integer(status)));
                }

                // write the report
                if (status == HttpServletResponse.SC_OK) {
                    report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                } else if (status == HttpServletResponse.SC_NOT_MODIFIED) {
                    report.println(report.key("report.skipped"), I_CmsReport.C_FORMAT_NOTE);
                } else if (status == HttpServletResponse.SC_SEE_OTHER) {
                    report.println(report.key("report.ignored"), I_CmsReport.C_FORMAT_NOTE);
                } else {
                    report.println(String.valueOf(status), I_CmsReport.C_FORMAT_OK);
                }
            } catch (IOException e) {
                report.println(e);
            }
        }
        report.println(report.key("report.staticexport.templateresources_end"), I_CmsReport.C_FORMAT_HEADLINE);

    }

    /**
     * Returns the accept-charset header used for internal requests.<p>
     * 
     * @return the accept-charset header
     */
    public String getAcceptCharsetHeader() {

        return m_acceptCharsetHeader;
    }

    /**
     * Returns the accept-language header used for internal requests.<p>
     * 
     * @return the accept-language header
     */
    public String getAcceptLanguageHeader() {

        return m_acceptLanguageHeader;
    }

    /**
     * Returns a cached vfs resource name for the given rfs name.<p>
     * 
     * @param rfsName the name of the ref resource to get the cached vfs resource name for
     * @return a cached vfs resource name for the given rfs name, or null 
     */
    public String getCachedExportUri(Object rfsName) {

        return (String)m_cacheExportUris.get(rfsName);
    }

    /**
     * Returns a cached link for the given vfs name.<p>
     * 
     * @param vfsName the name of the vfs resource to get the cached link for
     * @return a cached link for the given vfs name, or null 
     */
    public String getCachedOnlineLink(Object vfsName) {

        return (String)m_cacheOnlineLinks.get(vfsName);
    }

    /**
     * Returns the key for the online, export and secure cache.<p>
     * 
     * @param siteRoot the site root of the resource
     * @param uri the URI of the resource
     * @return a key for the cache 
     */
    public String getCacheKey(String siteRoot, String uri) {

        return new StringBuffer(siteRoot).append(uri).toString();
    }

    /**
     * Gets the default property value as a string representation.<p>
     * 
     * @return "true" or "false"
     */
    public String getDefault() {

        if (m_exportPropertyDefault) {
            return "true";
        } else {
            return "false";
        }
    }

    /**
     * Returns the current default charset header.<p>
     * 
     * @return the current default charset header
     */
    public String getDefaultAcceptCharsetHeader() {

        return m_defaultAcceptCharsetHeader;
    }

    /**
     * Returns the current default locale header.<p>
     * 
     * @return the current default locale header
     */
    public String getDefaultAcceptLanguageHeader() {

        return m_defaultAcceptLanguageHeader;
    }

    /**
     * Returns the export data for the request, if null is returned no export is required.<p>
     * 
     * @param request the request to check for export data
     * @param cms an initialized cms context (should be initialized with the "Guest" user only
     * @return the export data for the request, if null is returned no export is required
     */
    public CmsStaticExportData getExportData(HttpServletRequest request, CmsObject cms) {

        if (!isStaticExportEnabled()) {
            // export is disabled
            return null;
        }

        // build the rfs name for the export "on demand"
        String rfsName = request.getParameter(EXPORT_MARKER);
        if ((rfsName == null)) {
            rfsName = (String)request.getAttribute(EXPORT_ATTRIBUTE_ERROR_REQUEST_URI);
        }

        if (request.getHeader(I_CmsConstants.C_HEADER_OPENCMS_EXPORT) != null) {
            // this is a request created by the static export and directly send to 404 handler
            // so remove the leading handler identification
            int prefix = rfsName.indexOf(getRfsPrefix());
            if (prefix > 0) {
                rfsName = rfsName.substring(prefix);
            } else {
                return null;
            }
        }

        if ((rfsName == null) || !rfsName.startsWith(getRfsPrefix())) {
            // this is not an export request, no further processing is required
            return null;
        }

        return getExportData(rfsName, null, cms);
    }

    /**
     * Returns the export data for a requested resource, if null is returned no export is required.<p>
     * 
     * @param vfsName the VFS name of the resource requested
     * @param cms an initialized cms context (should be initialized with the "Guest" user only
     * @return the export data for the request, if null is returned no export is required
     */
    public CmsStaticExportData getExportData(String vfsName, CmsObject cms) {

        return getExportData(getRfsName(cms, vfsName), vfsName, cms);
    }

    /**
     * Gets the export enbled value as a string representation.<p>
     * 
     * @return "true" or "false"
     */
    public String getExportEnabled() {

        return String.valueOf(m_staticExportEnabled);
    }

    /**
     * Returns the current folder matcher.<p>
     * 
     * @return the current folder matcher
     */
    public CmsExportFolderMatcher getExportFolderMatcher() {

        return m_exportFolderMatcher;
    }

    /**
     * Returns list of resouces patterns which are part of the export.<p>
     * 
     * @return the of resouces patterns which are part of the export.
     */
    public List getExportFolderPatterns() {

        return Collections.unmodifiableList(m_exportFolders);
    }

    /**
     * Returns specific http headers for the static export.<p>
     * 
     * If the header <code>Cache-Control</code> is set, OpenCms will not use its default headers.<p>
     * 
     * @return the list of http export headers
     */
    public List getExportHeaders() {

        return Collections.unmodifiableList(m_exportHeaders);
    }

    /**
     * Returns the list of all resources that have the "exportname" property set.<p>
     * 
     * @return the list of all resources that have the "exportname" property set
     */
    public Map getExportnames() {

        return m_exportnameResources;
    }

    /**
     * Returns the export path for the static export, that is the folder where the 
     * static exported resources will be written to.<p>
     * 
     * The returned value will be a direcory like prefix. The value is configured
     * in the <code>opencms-importexport.xml</code> configuration file. An optimization
     * of the configured value will be performed, where all relative path infprmation is resolved
     * (for example <code>/export/../static</code> will be resolved to <code>/export</code>. 
     * Moreover, if the configured path ends with a <code>/</code>, this will be cut off 
     * (for example <code>/export/</code> becomes <code>/export</code>.<p>
     * 
     * @return the export path for the static export, that is the folder where the
     * 
     * @see #getRfsPrefix()
     * @see #getVfsPrefix()
     */
    public String getExportPath() {

        return m_staticExportPath;
    }

    /**
     * Returns the original configured export path for the static export without the complete rfs path, to be used 
     * when re-writing the configuration.<p>
     * 
     * This is required <b>only</b> to serialize the configuration again exactly as it was configured.
     * This method should <b>not</b> be used otherwise. Use <code>{@link #getExportPath()}</code>
     * to obtain the export path to use when exporting.<p> 
     * 
     * @return the original configured export path for the static export without the complete rfs path
     */
    public String getExportPathForConfiguration() {

        return m_staticExportPathConfigured;
    }

    /**
     * Returns true if the default value for the resource property "export" is true.<p>
     * 
     * @return true if the default value for the resource property "export" is true
     */
    public boolean getExportPropertyDefault() {

        return m_exportPropertyDefault;
    }

    /**
     * Gets the list of resouce suffixes which will be exported by default.<p>
     * 
     * @return list of resouce suffixe
     */
    public List getExportSuffixes() {

        return m_exportSuffixes;
    }

    /**
     * Returns the export URL used for internal requests for exporting resources that require a 
     * request / response (like JSP).<p>
     * 
     * @return the export URL used for internal requests for exporting resources like JSP
     */
    public String getExportUrl() {

        return m_exportUrl;
    }

    /**
     * Returns the export URL used for internal requests with unsubstituted context values, to be used 
     * when re-writing the configuration.<p>
     * 
     * This is required <b>only</b> to serialize the configuration again exactly as it was configured.
     * This method should <b>not</b> be used otherwise. Use <code>{@link #getExportUrl()}</code>
     * to obtain the export path to use when exporting.<p> 
     * 
     * @return the export URL used for internal requests with unsubstituted context values
     */
    public String getExportUrlForConfiguration() {

        return m_exportUrlConfigured;
    }

    /**
     * Returns the static export handler class.<p>
     * 
     * If not set, a new <code>{@link CmsAfterPublishStaticExportHandler}</code> is created and returned.<p>
     * 
     * @return the static export handler class.
     */
    public I_CmsStaticExportHandler getHandler() {

        if (m_handler == null) {
            setHandler(CmsOnDemandStaticExportHandler.class.getName());
        }
        return m_handler;
    }

    /**
     * Gets the plain export optimization value as a string representation.<p>
     * 
     * @return "true" or "false"
     */
    public String getPlainExportOptimization() {

        if (m_quickPlainExport) {
            return "true";
        } else {
            return "false";
        }
    }

    /**
     * Returns true if the quick plain export is enabled.<p>
     * 
     * @return true if the quick plain export is enabled
     */
    public boolean getQuickPlainExport() {

        return m_quickPlainExport;
    }

    /**
     * Gets the relative links value as a string representation.<p>
     * 
     * @return "true" or "false"
     */
    public String getRelativeLinks() {

        if (m_exportRelativeLinks) {
            return "true";
        } else {
            return "false";
        }
    }

    /**
     * Returns the remote addr used for internal requests.<p>
     * 
     * @return the remote addr
     */
    public String getRemoteAddr() {

        return m_remoteAddr;
    }

    /**
     * Returns the remote address.<p>
     * 
     * @return the remote address
     */
    public String getRemoteAddress() {

        return m_remoteAddr;
    }

    /**
     * Returns the static export rfs name for a given vfs resoure.<p>
     * 
     * @param cms an initialized cms context
     * @param vfsName the name of the vfs resource
     * 
     * @return the static export rfs name for a give vfs resoure
     * 
     * @see #getVfsName(CmsObject, String)
     * @see #getRfsName(CmsObject, String, String)
     */
    public String getRfsName(CmsObject cms, String vfsName) {

        return getRfsName(cms, vfsName, null);
    }

    /**
     * Returns the static export rfs name for a given vfs resoure where the link to the 
     * resource includes request parameters.<p>
     * 
     * @param cms an initialized cms context
     * @param vfsName the name of the vfs resource
     * @param parameters the parameters of the link pointing to the resource
     * @return the static export rfs name for a give vfs resoure
     */
    public String getRfsName(CmsObject cms, String vfsName, String parameters) {

        String rfsName = vfsName;

        try {
            // check if the resource folder (or a parent folder) has the "exportname" property set
            CmsProperty exportNameProperty = cms.readPropertyObject(
                CmsResource.getFolderPath(rfsName),
                I_CmsConstants.C_PROPERTY_EXPORTNAME,
                true);

            if (exportNameProperty.isNullProperty()) {
                // if "exportname" is not set we must add the site root 
                rfsName = cms.getRequestContext().addSiteRoot(rfsName);
            } else {
                // "exportname" property is set
                String exportname = exportNameProperty.getValue();
                if (exportname.charAt(0) != '/') {
                    exportname = '/' + exportname;
                }
                if (exportname.charAt(exportname.length() - 1) != '/') {
                    exportname = exportname + '/';
                }
                String value;
                boolean cont;
                String resourceName = rfsName;
                do {
                    // find out where the export name was set, to replace these parent folders in the RFS name
                    try {
                        value = cms.readPropertyObject(resourceName, I_CmsConstants.C_PROPERTY_EXPORTNAME, false).getValue();
                        cont = (value == null) && (resourceName.length() > 1);
                    } catch (CmsVfsResourceNotFoundException e) {
                        // this is for publishing deleted resources 
                        cont = (resourceName.length() > 1);
                    } catch (CmsSecurityException se) {
                        // a security exception (probably no read permission) we return the current result                      
                        cont = false;
                    }
                    if (cont) {
                        resourceName = CmsResource.getParentFolder(resourceName);
                    }
                } while (cont);
                rfsName = exportname + rfsName.substring(resourceName.length());
            }

            String extension;
            int pos = rfsName.lastIndexOf('.');
            if (pos >= 0) {
                extension = rfsName.substring(pos).toLowerCase();
            } else {
                extension = "";
            }
            // check if the VFS resouce is a JSP page with a ".jsp" ending 
            // in this case the rfs name suffix must be build with special care,
            // usually it must be set to ".html"             
            boolean isJsp = extension.equals(".jsp");
            if (isJsp) {
                CmsResource res = cms.readResource(vfsName);
                isJsp = res.getTypeId() == CmsResourceTypeJsp.getStaticTypeId();
                // if the resource is a plain resouce then no change in suffix is required
                if (isJsp) {
                    String suffix = cms.readPropertyObject(vfsName, I_CmsConstants.C_PROPERTY_EXPORTSUFFIX, true).getValue(
                        ".html");
                    if (!extension.equals(suffix.toLowerCase())) {
                        rfsName += suffix;
                        extension = suffix;
                    }
                }
            }

            if (parameters != null) {
                // build the RFS name for the link with parameters
                StringBuffer buf = new StringBuffer(128);
                buf.append(rfsName);
                buf.append('_');
                buf.append(getExportParamId(parameters));
                buf.append(extension);
                rfsName = buf.toString();

                // we have found a rfs name for a vfs resource with parameters, save it to the database
                try {
                    cms.writeStaticExportPublishedResource(
                        rfsName,
                        EXPORT_LINK_WITH_PARAMETER,
                        parameters,
                        System.currentTimeMillis());
                } catch (CmsException e) {
                    LOG.error(Messages.get().key(Messages.LOG_WRITE_FAILED_1, rfsName), e);
                }
            }

        } catch (CmsException e) {
            // ignore exception, return vfsName as rfsName
            rfsName = vfsName;
        }

        // add export rfs prefix and return result         
        return getRfsPrefix().concat(rfsName);
    }

    /**
     * Returns the prefix for exported links in the "real" file system.<p>
     * 
     * The returned value will be a direcory like prefix. The value is configured
     * in the <code>opencms-importexport.xml</code> configuration file. An optimization
     * of the configured value will be performed, where all relative path infprmation is resolved
     * (for example <code>/export/../static</code> will be resolved to <code>/export</code>. 
     * Moreover, if the configured path ends with a <code>/</code>, this will be cut off 
     * (for example <code>/export/</code> becomes <code>/export</code>.<p>
     * 
     * @return the prefix for exported links in the "real" file system
     * 
     * @see #getExportPath()
     * @see #getVfsPrefix()
     */
    public String getRfsPrefix() {

        return m_rfsPrefix;
    }

    /**
     * Returns the original configured prefix for exported links in the "real" file, to be used 
     * when re-writing the configuration.<p>
     * 
     * This is required <b>only</b> to serialize the configuration again exactly as it was configured.
     * This method should <b>not</b> be used otherwise. Use <code>{@link #getRfsPrefix()}</code>
     * to obtain the rfs prefix to use for the exported links.<p> 
     * 
     * @return the original configured prefix for exported links in the "real" file
     */
    public String getRfsPrefixForConfiguration() {

        return m_rfsPrefixConfigured;
    }

    /**
     * Returns the vfs name of the test resource.<p>
     * 
     * @return the vfs name of the test resource.
     */
    public String getTestResource() {

        return m_testResource;
    }

    /**
     * Returns the VFS name for the given RFS name, being the exact reverse of <code>{@link #getRfsName(CmsObject, String)}</code>.<p>
     * 
     * Returns <code>null</code> if no matching VFS resource can be found for the given RFS name.<p>
     * 
     * @param cms the current users OpenCms context
     * @param rfsName the RFS name to get the VFS name for
     * 
     * @return the VFS name for the given RFS name, or <code>null</code> if the RFS name does not match to the VFS
     * 
     * @see #getRfsName(CmsObject, String)
     */
    public String getVfsName(CmsObject cms, String rfsName) {

        CmsStaticExportData data = getExportData(rfsName, null, cms);
        if (data != null) {
            String result = data.getVfsName();
            if ((result != null) && result.startsWith(cms.getRequestContext().getSiteRoot())) {
                result = result.substring(cms.getRequestContext().getSiteRoot().length());
            }
            return result;
        }
        return null;
    }

    /**
     * Returns the prefix for the internal in the VFS.<p>
     * 
     * The returned value will be a direcory like prefix. The value is configured
     * in the <code>opencms-importexport.xml</code> configuration file. An optimization
     * of the configured value will be performed, where all relative path infprmation is resolved
     * (for example <code>/opencms/../mycms</code> will be resolved to <code>/mycms</code>. 
     * Moreover, if the configured path ends with a <code>/</code>, this will be cut off 
     * (for example <code>/opencms/</code> becomes <code>/opencms</code>.<p>
     * 
     * @return the prefix for the internal in the VFS
     * 
     * @see #getExportPath()
     * @see #getRfsPrefix()
     */
    public String getVfsPrefix() {

        return m_vfsPrefix;
    }

    /**
     * Returns the original configured prefix for internal links in the VFS, to be used 
     * when re-writing the configuration.<p>
     * 
     * This is required <b>only</b> to serialize the configuration again exactly as it was configured.
     * This method should <b>not</b> be used otherwise. Use <code>{@link #getVfsPrefix()}</code>
     * to obtain the VFS prefix to use for the internal links.<p> 
     * 
     * @return the original configured prefix for internal links in the VFS
     */
    public String getVfsPrefixForConfiguration() {

        return m_vfsPrefixConfigured;
    }

    /**
     * Initializes the static export manager with the OpenCms system configuration.<p>
     * 
     * @param cms an OpenCms context object
     */
    public void initialize(CmsObject cms) {

        // initialize static export RFS path (relative to web application)
        m_staticExportPath = insertContextStrings(m_staticExportPathConfigured);
        m_staticExportPath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(m_staticExportPath);
        if (m_staticExportPath.endsWith(File.separator)) {
            // ensure export path does NOT end with a File.separator
            m_staticExportPath = m_staticExportPath.substring(0, m_staticExportPath.length() - 1);
        }

        // initialize prefix variables
        m_rfsPrefix = insertContextStrings(m_rfsPrefixConfigured);
        m_rfsPrefix = CmsFileUtil.normalizePath(m_rfsPrefix, '/');
        if (CmsResource.isFolder(m_rfsPrefix)) {
            // ensure prefix does NOT end with a folder '/'
            m_rfsPrefix = m_rfsPrefix.substring(0, m_rfsPrefix.length() - 1);
        }
        m_vfsPrefix = insertContextStrings(m_vfsPrefixConfigured);
        m_vfsPrefix = CmsFileUtil.normalizePath(m_vfsPrefix, '/');
        if (CmsResource.isFolder(m_vfsPrefix)) {
            // ensure prefix does NOT end with a folder '/'
            m_vfsPrefix = m_vfsPrefix.substring(0, m_vfsPrefix.length() - 1);
        }

        if (CmsLog.LOG.isDebugEnabled()) {
            if (cms != null) {
                CmsLog.LOG.debug(Messages.get().key(Messages.INIT_SE_MANAGER_CREATED_1, cms));
            } else {
                CmsLog.LOG.debug(Messages.get().key(Messages.INIT_SE_MANAGER_CREATED_0));
            }
        }

        LRUMap lruMap1 = new LRUMap(2048);
        m_cacheOnlineLinks = Collections.synchronizedMap(lruMap1);
        if (OpenCms.getMemoryMonitor().enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_cacheOnlineLinks", lruMap1);
        }

        LRUMap lruMap2 = new LRUMap(2048);
        m_cacheExportUris = Collections.synchronizedMap(lruMap2);
        if (OpenCms.getMemoryMonitor().enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_cacheExportUris", lruMap2);
        }

        LRUMap lruMap3 = new LRUMap(2048);
        m_cacheSecureLinks = Collections.synchronizedMap(lruMap3);
        if (OpenCms.getMemoryMonitor().enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_cacheSecureLinks", lruMap3);
        }

        LRUMap lruMap4 = new LRUMap(2048);
        m_cacheExportLinks = Collections.synchronizedMap(lruMap4);
        if (OpenCms.getMemoryMonitor().enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_cacheExportLinks", lruMap4);
        }

        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {
            I_CmsEventListener.EVENT_PUBLISH_PROJECT,
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_UPDATE_EXPORTS});

        m_exportFolderMatcher = new CmsExportFolderMatcher(m_exportFolders, m_testResource);

        // initialize "exportname" folders
        setExportnames();

        // get the default accept-language header value
        m_defaultAcceptLanguageHeader = CmsAcceptLanguageHeaderParser.createLanguageHeader();

        // get the default accept-charset header value
        m_defaultAcceptCharsetHeader = OpenCms.getSystemInfo().getDefaultEncoding();

        if (CmsLog.LOG.isInfoEnabled()) {
            if (isStaticExportEnabled()) {
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_STATIC_EXPORT_ENABLED_0 ));                
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_EXPORT_DEFAULT_1, new Boolean(getExportPropertyDefault())));
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_EXPORT_PATH_1, getExportPath()));
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_EXPORT_RFS_PREFIX_1, getRfsPrefix()));
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_EXPORT_VFS_PREFIX_1, getVfsPrefix()));
                CmsLog.LOG.info(Messages.get().key(relativLinksInExport() ? Messages.INIT_EXPORT_LINK_STYLE_RELATIVE_0 : Messages.INIT_EXPORT_LINK_STYLE_ABSOLUTE_0));
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_EXPORT_EXPORT_HANDLER_1, getHandler().getClass().getName()));
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_EXPORT_URL_1, getExportUrl()));
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_EXPORT_OPTIMIZATION_1, getPlainExportOptimization()));
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_EXPORT_TESTRESOURCE_1, getTestResource()));
            } else {
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_STATIC_EXPORT_DISABLED_0));
            }    
        }
    }

    /**
     * Checks if the static export is required for the given VFS resource.<p>
     * 
     * Please note that the given OpenCms user context is NOT used to read the resource.
     * The check for export is always done with the permissions of the "Export" user.
     * The provided user context is just used to get the current site root.<p>
     * 
     * Since the "Export" user always operates in the "Online" project, the resource
     * is also read from the "Online" project, not from the current project of the given 
     * OpenCms context.<p>
     * 
     * @param cms the current users OpenCms context
     * @param vfsName the VFS resource name to check
     * 
     * @return <code>true</code> if static export is required for the given VFS resource
     */
    public boolean isExportLink(CmsObject cms, String vfsName) {

        boolean result = false;
        if (isStaticExportEnabled()) {
            Boolean exportResource = (Boolean)m_cacheExportLinks.get(getCacheKey(
                cms.getRequestContext().getSiteRoot(),
                vfsName));
            if (exportResource == null) {
                try {
                    // static export must always be checked with the export users permissions,
                    // not the current users permissions
                    CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
                    exportCms.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
                    // let's look up export property in VFS
                    String exportValue = exportCms.readPropertyObject(vfsName, I_CmsConstants.C_PROPERTY_EXPORT, true).getValue();
                    if (exportValue == null) {
                        // no setting found for "export" property
                        if (getExportPropertyDefault()) {
                            // if the default is "true" we always export
                            result = true;
                        } else {
                            // check if the resource is exportable by suffix
                            result = isSuffixExportable(vfsName);
                        }
                    } else {
                        // "export" value found, if it was "true" we export
                        result = Boolean.valueOf(exportValue).booleanValue();
                    }
                } catch (Exception e) {
                    // no export required (probably security issues, e.g. no access for export user)
                }
                m_cacheExportLinks.put(getCacheKey(cms.getRequestContext().getSiteRoot(), vfsName), new Boolean(result));
            } else {
                result = exportResource.booleanValue();
            }
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the given VFS resource should be transported through a secure channel.<p>
     * 
     * The secure mode is only checked in the "Online" project. 
     * If the given OpenCms context is currently not in the "Online" project,
     * <code>false</code> is returned.<p>
     * 
     * The given resource is read from the site root of the provided OpenCms context.<p>
     * 
     * @param cms the current users OpenCms context
     * @param vfsName the VFS resource name to check
     * 
     * @return <code>true</code> if the given VFS resource should be transported through a secure channel
     * 
     * @see #isSecureLink(CmsObject, String, String)
     */
    public boolean isSecureLink(CmsObject cms, String vfsName) {

        if (! cms.getRequestContext().currentProject().isOnlineProject()) {
            return false; 
        }
        Boolean secureResource = (Boolean)m_cacheSecureLinks.get(getCacheKey(
            cms.getRequestContext().getSiteRoot(),
            vfsName));
        if (secureResource == null) {
            try {
                String secureProp = cms.readPropertyObject(vfsName, I_CmsConstants.C_PROPERTY_SECURE, true).getValue();
                secureResource = Boolean.valueOf(secureProp);
                // only cache result if read was successfull
                m_cacheSecureLinks.put(getCacheKey(cms.getRequestContext().getSiteRoot(), vfsName), secureResource);
            } catch (CmsVfsResourceNotFoundException e) {
                secureResource = Boolean.FALSE;
                // resource does not exist, no secure link will be required for any user
                m_cacheSecureLinks.put(getCacheKey(cms.getRequestContext().getSiteRoot(), vfsName), secureResource);
            } catch (Exception e) {
                // no secure link required (probably security issues, e.g. no access for current user)
                // however other users may be allowed to read the resource, so the result can't be cached
                secureResource = Boolean.FALSE;
            }
        }
        return secureResource.booleanValue();
    }

    /**
     * Returns <code>true</code> if the given VFS resource that is located under the 
     * given site root should be transported through a secure channel.<p>
     * 
     * @param cms the current users OpenCms context
     * @param vfsName the VFS resource name to check
     * @param siteRoot the site root where the the VFS resource should be read
     * 
     * @return <code>true</code> if the given VFS resource should be transported through a secure channel
     * 
     * @see #isSecureLink(CmsObject, String)
     */
    public boolean isSecureLink(CmsObject cms, String vfsName, String siteRoot) {

        if (siteRoot == null) {
            return isSecureLink(cms, vfsName);
        }

        // the site root of the cms object has to be changed so that the property can be read    
        try {
            cms.getRequestContext().saveSiteRoot();
            cms.getRequestContext().setSiteRoot(siteRoot);
            return isSecureLink(cms, vfsName);
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Returns true if the static export is enabled.<p>
     * 
     * @return true if the static export is enabled
     */
    public boolean isStaticExportEnabled() {

        return m_staticExportEnabled;
    }

    /**
     * Returns true if the given resource name is exportable because of it's suffix.<p>
     * 
     * @param resourceName the name to check 
     * @return true if the given resource name is exportable because of it's suffix
     */
    public boolean isSuffixExportable(String resourceName) {

        if (resourceName == null) {
            return false;
        }
        int pos = resourceName.lastIndexOf('.');
        if (pos >= 0) {
            String suffix = resourceName.substring(pos).toLowerCase();
            return m_exportSuffixes.contains(suffix);
        }
        return false;
    }

    /**
     * Returns true if the links in the static export should be relative.<p>
     * 
     * @return true if the links in the static export should be relative
     */
    public boolean relativLinksInExport() {

        return m_exportRelativeLinks;
    }

    /**
     * Sets the accept-charset header value.<p>
     * 
     * @param value accept-language header value
     */
    public void setAcceptCharsetHeader(String value) {

        m_acceptCharsetHeader = value;
    }

    /**
     * Sets the accept-language header value.<p>
     * 
     * @param value accept-language header value
     */
    public void setAcceptLanguageHeader(String value) {

        m_acceptLanguageHeader = value;
    }

    /**
     * Sets the default property value.<p>
     * 
     * @param value must be "true" or "false"
     */
    public void setDefault(String value) {

        m_exportPropertyDefault = Boolean.valueOf(value).booleanValue();
    }

    /**
     * Sets the export enbled value.<p>
     * 
     * @param value must be "true" or "false"
     */
    public void setExportEnabled(String value) {

        m_staticExportEnabled = Boolean.valueOf(value).booleanValue();
    }

    /**
     * Adds a resource pattern to the list of resouces which are part of the export.<p>
     * 
     * @param folder the folder pattern to add to the list.
     */
    public void setExportFolderPattern(String folder) {

        m_exportFolders.add(folder);
    }

    /**
     * Sets specific http header for the static export.<p>
     * 
     * The format of the headers must be "header:value".<p> 
     *  
     * @param exportHeader a specific http header
     */
    public void setExportHeader(String exportHeader) {

        if (CmsStringUtil.splitAsArray(exportHeader, ':').length == 2) {
            if (CmsLog.LOG.isInfoEnabled()) {
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_EXPORT_HEADERS_1, exportHeader));
            }
            m_exportHeaders.add(exportHeader);
        } else {
            if (CmsLog.LOG.isWarnEnabled()) {
                CmsLog.LOG.warn(Messages.get().key(Messages.INIT_INVALID_HEADER_1, exportHeader));
            }
        }
    }

    /**
     * Sets the path where the static export is written.<p>
     * 
     * @param path the path where the static export is written
     */
    public void setExportPath(String path) {

        m_staticExportPathConfigured = path;
    }

    /**
     * Adds a suffix to the list of resouce suffixes which will be exported by default.<p>
     * 
     * @param suffix the suffix to add to the list.
     */
    public void setExportSuffix(String suffix) {

        m_exportSuffixes.add(suffix.toLowerCase());
    }

    /**
     * Sets the export url.<p>
     * 
     * @param url the export url
     */
    public void setExportUrl(String url) {

        m_exportUrl = insertContextStrings(url);
        m_exportUrlConfigured = url;
    }

    /**
     * Sets the static export handler class.<p>
     * 
     * @param handlerClassName the static export handler class name.
     */
    public void setHandler(String handlerClassName) {

        try {
            m_handler = (I_CmsStaticExportHandler)Class.forName(handlerClassName).newInstance();
        } catch (Exception e) {
            // should never happen
            OpenCms.getLog(this).error(e);
        }
    }

    /**
     * Sets the plain export optimization value.<p>
     * 
     * @param value must be "true" or "false"
     */
    public void setPlainExportOptimization(String value) {

        m_quickPlainExport = Boolean.valueOf(value).booleanValue();
    }

    /**
     * Sets the relative links value.<p>
     * 
     * @param value must be "true" or "false"
     */
    public void setRelativeLinks(String value) {

        m_exportRelativeLinks = Boolean.valueOf(value).booleanValue();
    }

    /**
     * Sets the remote addr. wirch will be used for internal requests during the static export.<p>
     * 
     * @param addr the remote addr. to be used
     */
    public void setRemoteAddr(String addr) {

        m_remoteAddr = addr;
    }

    /**
     * Sets the prefix for exported links in the "real" file system.<p>
     * 
     * @param rfsPrefix the prefix for exported links in the "real" file system
     */
    public void setRfsPrefix(String rfsPrefix) {

        m_rfsPrefixConfigured = rfsPrefix;
    }

    /**
     * Sets the test resource.<p>
     *  
     * @param testResource the vfs name of the test resource
     */
    public void setTestResource(String testResource) {

        m_testResource = testResource;
    }

    /**
     * Sets the prefix for internal links in the vfs.<p>
     * 
     * @param vfsPrefix the prefix for internal links in the vfs
     */
    public void setVfsPrefix(String vfsPrefix) {

        m_vfsPrefixConfigured = vfsPrefix;
    }

    /**
     * Shuts down all this static export manager.<p>
     * 
     * This is required since there may still be a thread running when the system is being shut down.<p>
     */
    public synchronized void shutDown() {

        int count = 0;
        // if the handler is still running, we must wait up to 30 secounds until it is finished
        while ((count < HANDLER_FINISH_TIME) && m_handler.isBusy()) {
            count++;
            try {
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                        ". Shutting down        : Waiting for static export handler "
                            + m_handler.getClass().getName()
                            + " to finish ("
                            + count
                            + "/"
                            + HANDLER_FINISH_TIME
                            + ")");
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // if interrupted we ignore the handler, this will produce some log messages but should be ok 
                count = HANDLER_FINISH_TIME;
            }
        }

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_SHUTDOWN_1, this.getClass().getName()));
        }

    }

    /**
     * Clears the caches in the export manager.<p>
     * 
     * @param event the event that requested to clear the caches
     */
    private void clearCaches(CmsEvent event) {

        // flush all caches   
        m_cacheOnlineLinks.clear();
        m_cacheExportUris.clear();
        m_cacheSecureLinks.clear();
        m_cacheExportLinks.clear();
        setExportnames();
        if (LOG.isDebugEnabled()) {
            String eventType = "EVENT_CLEAR_CACHES";
            if (event.getType() != I_CmsEventListener.EVENT_CLEAR_CACHES) {
                eventType = "EVENT_PUBLISH_PROJECT";
            }
            LOG.debug(Messages.get().key(Messages.LOG_FLUSHED_CACHES_1, eventType));
        }
    }

    /**
     * Creates the parent folder for a exported resource in the RFS.<p>
     * 
     * @param rfsName the name of the resource
     * @throws CmsException if the folder could not be created
     */
    private void createExportFolder(String rfsName) throws CmsException {

        String exportFolderName = CmsFileUtil.normalizePath(getExportPath() + CmsResource.getFolderPath(rfsName));
        File exportFolder = new File(exportFolderName);
        if (!exportFolder.exists()) {
            if (!exportFolder.mkdirs()) {
                throw new CmsStaticExportException(Messages.get().container(Messages.ERR_CREATE_FOLDER_1, rfsName));

            }
        }
    }

    /**
     * Creates a list of CmsPulishedResource objects containing all resources of the VFS tree.<p>
     * 
     * This list is used as input for the static export triggered by the OpenCms workplace
     *
     * @param cms the current cms object
     * @return list of CmsPulishedResource objects containing all resources of the VFS tree
     * @throws CmsException in case of errors accessing the VFS
     */
    private List getAllResources(CmsObject cms) throws CmsException {

        List resources = new ArrayList();
        CmsResource vfsResource;
        CmsPublishedResource resource;
        long starttime;
        long endtime;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_GET_ALL_RESOURCES_0));
        }

        try {
            // switch to root site
            cms.getRequestContext().saveSiteRoot();
            cms.getRequestContext().setSiteRoot("/");

            // now get all resources within the folder tree. since the long min and max value
            // do not work with the sql timestamp function in the driver, we must calculate 
            // some different, but usable start and endtime values first

            //starttime to 01.01.1970
            starttime = 0;
            // endtime to now plus one week
            endtime = System.currentTimeMillis() + 604800000;
            List vfsResources = cms.getResourcesInTimeRange("/", starttime, endtime);

            // loop through the list and create the list of CmsPublishedResources

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_NUM_RESOURCES_1, new Integer(vfsResources.size())));
            }

            Iterator i = vfsResources.iterator();
            while (i.hasNext()) {
                vfsResource = (CmsResource)i.next();
                resource = new CmsPublishedResource(vfsResource);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_PROCESSING_1, resource.getRootPath()));
                }

                resources.add(resource);

            }

        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }

        return resources;
    }

    /**
     * Returns the export data for a requested resource, if null is returned no export is required.<p>
     * 
     * @param rfsName the RFS name of the resource requested
     * @param vfsName the VFS name of the resource requested
     * @param cms an initialized cms context (should be initialized with the "Guest" user only
     * @return the export data for the request, if null is returned no export is required
     */
    private CmsStaticExportData getExportData(String rfsName, String vfsName, CmsObject cms) {

        CmsResource resource = null;
        cms.getRequestContext().saveSiteRoot();

        try {
            cms.getRequestContext().setSiteRoot("/");

            // cut export prefix from name
            rfsName = rfsName.substring(getRfsPrefix().length());

            if (vfsName == null) {
                // check if we have the result already in the cache        
                vfsName = getCachedExportUri(rfsName);
            }

            if (vfsName != null) {
                // this export uri is already cached            
                if (CACHEVALUE_404 != vfsName) {
                    // this uri can be exported
                    try {
                        resource = cms.readResource(vfsName);
                    } catch (CmsException e) {
                        // no export if error occured here                       
                        return null;
                    }
                    // valid cache entry, return export data object
                    return new CmsStaticExportData(vfsName, rfsName, resource);
                } else {
                    // this uri can not be exported
                    return null;
                }
            } else {
                // export uri not in cache, must look up the file in the VFS
                boolean match = false;

                vfsName = getVfsNameInternal(cms, rfsName);
                if (vfsName != null) {
                    match = true;
                    try {
                        resource = cms.readResource(vfsName);
                    } catch (CmsException e) {
                        rfsName = null;
                    }
                }

                if (!match) {
                    // it could be a translated resourcename with parameters, so make a lookup
                    // in the published resources table
                    try {
                        String parameters = cms.readStaticExportPublishedResourceParameters(rfsName);
                        // there was a match in the db table, so get the StaticExportData 
                        if (CmsStringUtil.isNotEmpty(parameters)) {

                            // get the rfs base string without the parameter hashcode
                            String rfsBaseName = rfsName.substring(0, rfsName.lastIndexOf('_'));

                            // get the vfs base name, which is later used to read the resource in the vfs
                            String vfsBaseName = getVfsNameInternal(cms, rfsBaseName);

                            // everything is there, so read the resource in the vfs and build the static export data object
                            if (vfsBaseName != null) {
                                resource = cms.readResource(vfsBaseName);
                                CmsStaticExportData data = new CmsStaticExportData(
                                    vfsBaseName,
                                    rfsName,
                                    resource,
                                    parameters);
                                cacheExportUri(rfsName, data.getVfsName());
                                return data;
                            }
                        }
                    } catch (CmsException e) {
                        // ignore, resource does not exist
                    }
                    // no match found, nothing to export
                    cacheExportUri(rfsName, CACHEVALUE_404);
                    return null;
                } else {
                    // found a resource to export
                    cacheExportUri(rfsName, vfsName);
                    return new CmsStaticExportData(vfsName, rfsName, resource);
                }
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Returns the (positive) id to replace parameters in the static export name.<p>
     * 
     * @param parameters the parameters to calculate the export parameter id for
     * @return a (positive) id to replace parameters in the static export name
     */
    private int getExportParamId(String parameters) {

        int h = parameters.hashCode();
        if (h < 0) {
            h = -h;
        }
        return h;
    }

    /**
     * Returns the VFS name from a given RFS name.<p>
     * 
     * The RFS name must not contain the RFS prefix.<p>
     * 
     * @param cms an initialized OpenCms user context
     * @param rfsName the name of the RFS resource
     * @return the name of the VFS resource
     */
    private String getVfsNameInternal(CmsObject cms, String rfsName) {

        String vfsName = null;
        CmsResource resource;

        boolean match = false;

        try {
            resource = cms.readResource(cms.getRequestContext().removeSiteRoot(rfsName));
            if (resource.isFolder() && !CmsResource.isFolder(rfsName)) {
                rfsName += '/';
            }
            vfsName = rfsName;
            match = true;
        } catch (Throwable t) {
            // resource not found                   
        }

        if (!match) {
            // name of export resource could not be resolved by reading the resource directly,
            // now try to find a match with the "exportname" folders            
            Map exportnameFolders = getExportnames();
            Iterator i = exportnameFolders.keySet().iterator();
            while (i.hasNext()) {
                String exportName = (String)i.next();
                if (rfsName.startsWith(exportName)) {
                    // prefix match
                    match = true;
                    vfsName = exportnameFolders.get(exportName) + rfsName.substring(exportName.length());
                    try {
                        resource = cms.readResource(vfsName);
                        if (resource.isFolder()) {
                            if (!CmsResource.isFolder(rfsName)) {
                                rfsName += '/';
                            }
                            if (!CmsResource.isFolder(vfsName)) {
                                vfsName += '/';
                            }
                        }
                        break;
                    } catch (CmsVfsResourceNotFoundException e) {
                        // continue with trying out the other exportname to find a match (may be a multiple prefix)
                        match = false;
                        continue;
                    } catch (CmsException e) {
                        rfsName = null;
                        break;
                    }
                }
            }
        }

        // finally check if its a modified jsp resource        
        if (!match) {
            // first cur of the last extension
            String cutName = rfsName.substring(0, rfsName.lastIndexOf('.'));
            int pos = cutName.lastIndexOf('.');
            if (pos >= 0) {
                // now check if remaining String ends with ".jsp"
                String extension = cutName.substring(pos).toLowerCase();
                if (".jsp".equals(extension)) {
                    return getVfsNameInternal(cms, cutName);
                }
            }
        }
        if (match) {
            return vfsName;
        } else {
            return null;
        }
    }

    /**
     * Substitutes the ${CONTEXT_NAME} and ${SERVLET_NAME} in a path with the real values.<p>
     * 
     * @param path the path to substitute
     * @return path with real context values
     */
    private String insertContextStrings(String path) {

        // create a new macro resolver
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();

        // add special mappings for macros 
        resolver.addMacro("CONTEXT_NAME", OpenCms.getSystemInfo().getContextPath());
        resolver.addMacro("SERVLET_NAME", OpenCms.getSystemInfo().getServletPath());

        // resolve the macros
        return resolver.resolveMacros(path);
    }

    /**
     * Scrubs the "export" folder.<p>
     */
    private void scrubExportFolder() {

        String exportFolderName = CmsFileUtil.normalizePath(getExportPath() + '/');
        try {
            File exportFolder = new File(exportFolderName);
            // check if export file exists, if so delete it
            if (exportFolder.exists() && exportFolder.canWrite()) {
                CmsFileUtil.purgeDirectory(exportFolder);
                // write log message
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().key(Messages.LOG_DEL_MAIN_SE_FOLDER_1, exportFolderName));
                }

            }
        } catch (Throwable t) {
            // ignore, nothing to do about the
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_FOLDER_DELETION_FAILED_1, exportFolderName), t);
            }

        }
    }

    /**
     * Set the list of all resources that have the "exportname" property set.<p>
     */
    private synchronized void setExportnames() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_UPDATE_EXPORTNAME_PROP_START_0));
        }

        List resources;
        CmsObject cms = null;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            resources = cms.readResourcesWithProperty(I_CmsConstants.C_PROPERTY_EXPORTNAME);
        } catch (CmsException e) {
            resources = Collections.EMPTY_LIST;
        }

        m_exportnameResources = new HashMap(resources.size());
        for (int i = 0, n = resources.size(); i < n; i++) {
            CmsResource res = (CmsResource)resources.get(i);
            try {
                String foldername = cms.getSitePath(res);
                String exportname = cms.readPropertyObject(foldername, I_CmsConstants.C_PROPERTY_EXPORTNAME, false).getValue();
                if (exportname != null) {
                    if (!exportname.endsWith("/")) {
                        exportname = exportname + "/";
                    }
                    if (!exportname.startsWith("/")) {
                        exportname = "/" + exportname;
                    }
                    m_exportnameResources.put(exportname, foldername);
                }
            } catch (CmsException e) {
                // ignore exception, folder will no be added
            }
        }
        m_exportnameResources = Collections.unmodifiableMap(m_exportnameResources);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_UPDATE_EXPORTNAME_PROP_FINISHED_0));
        }
    }
}
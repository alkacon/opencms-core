/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportManager.java,v $
 * Date   : $Date: 2005/01/04 17:34:51 $
 * Version: $Revision: 1.83 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypePlain;
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
import org.opencms.main.OpenCmsCore;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsFileUtil;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LRUMap;

/**
 * Provides the functionaility to export resources from the OpenCms VFS
 * to the file system.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.83 $
 */
public class CmsStaticExportManager implements I_CmsEventListener {

    /** Marker for error message attribute. */
    public static final String C_EXPORT_ATTRIBUTE_ERROR_MESSAGE = "javax.servlet.error.message";

    /** Marker for error request uri attribute. */
    public static final String C_EXPORT_ATTRIBUTE_ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

    /** Marker for error servlet name attribute. */
    public static final String C_EXPORT_ATTRIBUTE_ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";

    /** Marker for error status code attribute. */
    public static final String C_EXPORT_ATTRIBUTE_ERROR_STATUS_CODE = "javax.servlet.error.status_code";

    /** Name for the folder default index file. */
    public static final String C_EXPORT_DEFAULT_FILE = "index_export.html";

    /** Flag value for links without paramerters. */
    public static final int C_EXPORT_LINK_WITH_PARAMETER = 2;

    /** Flag value for links without paramerters. */
    public static final int C_EXPORT_LINK_WITHOUT_PARAMETER = 1;

    /** Marker for externally redirected 404 uri's. */
    public static final String C_EXPORT_MARKER = "exporturi";
    
    /** Cache value to indicate a true 404 error. */
    private static final String C_CACHEVALUE_404 = "?404";
    
    /** value for "after-publish" mode. */
    private static String C_MODE_AFTERPUBLISH = "after-publish";

    /** value for "on-demand" mode. */
    private static String C_MODE_ONDEMAND = "on-demand";

    /** Matcher for  selecting those resources which should be part of the staic export. */
    private static CmsExportFolderMatcher m_exportFolderMatcher;

    /** HTTP header Accept-Charset. */
    private String m_acceptCharsetHeader;
    
    /** HTTP header Accept-Language. */
    private String m_acceptLanguageHeader; 
    
    /** Cache for the export uris. */
    private Map m_cacheExportUris;

    /** Cache for the online links. */
    private Map m_cacheOnlineLinks;

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
    private String m_exportUrlUnsubstituted;
    
    /** Indicates if the quick static export for plain resources is enabled. */
    private boolean m_quickPlainExport;

    /** Remote addr. */
    private String m_remoteAddr; 

    /** Prefix to use for exported files. */
    private String m_rfsPrefix;

    /** Prefix to use for exported files with unstubstituted context values. */
    private String m_rfsPrefixUnsubstituted;
    
    /** Indicates if the static export is enabled or diabled. */
    private boolean m_staticExportEnabled;

    /** Indicates if the static export is switched to export "on demand" or "after publish" mode.  */
    private boolean m_staticExportOnDemand;

    /** The path to where the static export will be written. */
    private String m_staticExportPath;

    /** The path to where the static export will be written without the complete rfs path. */
    private String m_staticExportPathUnmodified;
    
    /** Vfs Name of a resource used to do a "static export required" test. */
    private String m_testResource;

    /** Prefix to use for internal OpenCms files. */
    private String m_vfsPrefix;

    /** Prefix to use for internal OpenCms files with unstubstituted context values. */
    private String m_vfsPrefixUnsubstituted;
    
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
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Static export manager catched event EVENT_PUBLISH_PROJECT for project ID " + publishHistoryId);
                }

                // check for the static export mode, this is either static export "on demand" or "after publish"                
                if (isStaticExportOnDemand()) {
                    // static export "on demand" enabled, so scrub the export folders only                   
                    scrubExportFolders(publishHistoryId, false);
                } else {
                    I_CmsReport report = (I_CmsReport)event.getData().get(I_CmsEventListener.KEY_REPORT);
                    // static export "after publish" is enabled, so start to write the exported resources
                    try {
                        exportAfterPublish(publishHistoryId, report);
                    } catch (Throwable t) {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error during static export:", t);
                        }
                    }
                }

                clearCaches(event);
                
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Static export manager finished publish event for project ID " + publishHistoryId);
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
     */
    public int export(HttpServletRequest req, HttpServletResponse res, CmsObject cms, CmsStaticExportData data)
    throws CmsException, IOException, ServletException {

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
        CmsContextInfo contextInfo = 
            new CmsContextInfo(
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

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Static export site root " + siteRoot + " / vfsName " + vfsName);
        }

        cms.getRequestContext().setSiteRoot(siteRoot);

        String oldUri = null;

        // this flag signals if the export method is used for "on demand" or "after publish". 
        // if no request and result stream are available, it was called during "export on publish"
        boolean exportOnDemand = ((req != null) && (res != null));

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Static export starting for resource " + data);
        }

        // read vfs resource
        if (resource.isFile()) {
            file = cms.readFile(vfsName);
        } else {
            file = CmsFile.upgrade(OpenCmsCore.getInstance().initResource(cms, vfsName, req, res), cms);
            try {
                cms.readResource(vfsName + file.getName());
            vfsName = vfsName + file.getName();
            } catch (CmsException e) {
                // 
            }
            
            rfsName += C_EXPORT_DEFAULT_FILE;
        }

        // check loader id for resource
        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(file);
        if ((loader == null) || (!loader.isStaticExportEnabled())) {
            throw new CmsException("Unable to export VFS file "
                + vfsName
                + ", loader for type "
                + file.getTypeId()
                + " does not support static export");
        }

        FileOutputStream exportStream = null;
        File exportFile = null;
        String exportFileName = CmsFileUtil.normalizePath(getExportPath() + rfsName.substring(1));

        // only export those resource where the export property is set
        if (OpenCms.getLinkManager().exportRequired(cms, vfsName)) {
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
            res.setContentType(mimetype);
            oldUri = cms.getRequestContext().getUri();
            cms.getRequestContext().setUri(vfsName);
        }

        // do the export
        byte[] result = loader.export(cms, file, req, res);

        // release unused resources
        file = null;

        if (result != null) {
            if (exportFile != null) {
                // write new exported file content
                try {
                    exportStream = new FileOutputStream(exportFile);
                    exportStream.write(result);
                    exportStream.close();
                    // the resource was exported, so return status ok
                    status = HttpServletResponse.SC_OK;

                } catch (Throwable t) {
                    throw new CmsException("Creation of static export output stream failed for RFS file " + exportFileName);
                }
                // update the file with the modification date from the server
                if (req != null) {
                    Long dateLastModified = (Long)req.getAttribute(I_CmsConstants.C_HEADER_OPENCMS_EXPORT);
                    if ((dateLastModified != null) && (dateLastModified.longValue() != -1)) {
                        exportFile.setLastModified((dateLastModified.longValue() / 1000) * 1000);
                        if (OpenCms.getLog(this).isDebugEnabled()) {
                            OpenCms.getLog(this).debug("Setting RFS file " + exportFile.getName() + " 'date last modified' to " + (dateLastModified.longValue() / 1000) * 1000);
                        }                      
                    }            
                } else {
                    // otherweise take the last modification date form the OpenCms resource
                    exportFile.setLastModified((resource.getDateLastModified() / 1000) * 1000);
                }
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
        if (OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info("Static exported vfs file '" + vfsName + "' to rfs file '" + exportFileName + "'");
        }

        return status;
    }

    /**
     * Starts the static export on publish.<p>
     * 
     * Exports all modified resources after a publish process into the real FS.<p>
     * 
     * @param publishHistoryId the publichHistoryId of the published project
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file   
     *  
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    public synchronized void exportAfterPublish(CmsUUID publishHistoryId, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        // first check if the test resource was published already
        // if not, we must do a complete export of all static resources
        String rfsName = CmsFileUtil.normalizePath(getExportPath() + getTestResource());
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Static export, checking test resource " + rfsName);
        }

        File file = new File(rfsName);
        if (!file.exists()) {
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Test resource does not exist -> do export 'full static render'");
            }
            // the file is not there, so export everything
            exportFullStaticRender(true, report);
        } else {
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Test resource exists -> do static export 'after publish'");
            }

            // delete all resources deleted during the publish process
            scrubExportFolders(publishHistoryId, true);

            // get the list of published resources from the publish history table
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            List publishedResources = cms.readPublishedResources(publishHistoryId);

            // do the export
            doExportAfterPublish(publishedResources, report);
        }
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
            cms.deleteAllStaticExportPublishedResources(C_EXPORT_LINK_WITHOUT_PARAMETER);
            cms.deleteAllStaticExportPublishedResources(C_EXPORT_LINK_WITH_PARAMETER);
        }

        //export must be done in the context of the export user    
        CmsObject cmsExportObject = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

        List publishedResources = getAllResources(cmsExportObject);

        // do the export
        doExportAfterPublish(publishedResources, report);
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
        String rfsName = request.getParameter(C_EXPORT_MARKER);
        if ((rfsName == null)) {
            rfsName = (String)request.getAttribute(C_EXPORT_ATTRIBUTE_ERROR_REQUEST_URI);
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

        String rfsName = null;

        rfsName = getRfsName(cms, vfsName);

        return getExportData(rfsName, vfsName, cms);
    }
    
    /**
     * Gets the export enbled value as a string representation.<p>
     * 
     * @return "true" or "false"
     */
    public String getExportEnabled() {
        if (m_staticExportEnabled) {
            return "true";
        } else {
            return "false";
        }
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
     * Returns the export path for the static export.<p>
     * 
     * @return the export path for the static export
     */
    public String getExportPath() {

        return m_staticExportPath;
    }

    /**
     * Returns the export path for the static export without the complete rfs path.<p>
     * 
     * @return the export path for the static export without the complete rfs path
     */
    public String getExportPathUnmodified() {

        return m_staticExportPathUnmodified;
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
     * Returns the export url used for internal requests.<p>
     * 
     * @return the export url
     */
    public String getExportUrl() {

        return m_exportUrl;
    }
    
    /**
     * Returns the export url used for internal requests with unsubstituted context values.<p>
     * 
     * @return the export url with unsubstituted context values
     */
    public String getExportUrlUnsubstituted() {

        return m_exportUrlUnsubstituted;
    }
    
    /**
     * Gets the mode in which the static export is working.<p>
     * 
     * @return either "on-demand" or "after-publish";
     */
    public String getMode() {
        if (m_staticExportOnDemand) {
            return C_MODE_ONDEMAND;
        } else {
            return C_MODE_AFTERPUBLISH;
        }
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
     * Returns the static export rfs name for a give vfs resoure.<p>
     * 
     * @param cms an initialized cms context
     * @param vfsName the name of the vfs resource
     * @return the static export rfs name for a give vfs resoure
     */
    public String getRfsName(CmsObject cms, String vfsName) {

        String originalVfsName = vfsName;

        try {
            // check if the resource folder (or a parent folder) has the "exportname" property set
            CmsProperty exportProperty = cms.readPropertyObject(
                CmsResource.getFolderPath(vfsName),
                I_CmsConstants.C_PROPERTY_EXPORTNAME,
                true);
            
            if (exportProperty != null && !exportProperty.isNullProperty()) {
                String exportname = exportProperty.getValue();
                // "exportname" property set
                if (!exportname.endsWith("/")) {
                    exportname = exportname + "/";
                }
                if (!exportname.startsWith("/")) {
                    exportname = "/" + exportname;
                }
                String value;
                boolean cont;
                String resourceName = vfsName;
                do {
                    try {
                        value = cms.readPropertyObject(resourceName, I_CmsConstants.C_PROPERTY_EXPORTNAME, false).getValue();
                        cont = ((value == null) && (!"/".equals(resourceName)));
                    } catch (CmsSecurityException se) {
                        // a security exception (probably no read permission) we return the current result                      
                        cont = false;
                    }
                    if (cont) {
                        resourceName = CmsResource.getParentFolder(resourceName);
                    }
                } while (cont);
                vfsName = exportname + vfsName.substring(resourceName.length());
            } else {
                // if "exportname" is not set we must add the site root 
                vfsName = cms.getRequestContext().addSiteRoot(vfsName);
            }
            // check if the vfsname ends with ".jsp", 
            // in this case the rfs name suffix must be build with special care,
            // usually it must be set to ".html" 
            if (vfsName.toLowerCase().endsWith(".jsp")) {
                CmsResource res = cms.readResource(originalVfsName);
                if (res.getTypeId() != CmsResourceTypePlain.C_RESOURCE_TYPE_ID) {
                    // if the resource is a plain resouce then no change in suffix is required
                    String suffix = cms.readPropertyObject(originalVfsName, I_CmsConstants.C_PROPERTY_EXPORTSUFFIX, true).getValue(".html");
                    if (! vfsName.toLowerCase().endsWith(suffix.toLowerCase())) {
                        // add special suffix, e.g. ".html"
                        vfsName += suffix;
                    }
                }
            }
        } catch (CmsException e) {
            // ignore exception, leave vfsName unmodified
        }
        // add export rfs prefix and return result         
        return OpenCms.getStaticExportManager().getRfsPrefix() + vfsName;
    }

    /**
     * Returns the prefix for exported links in the "real" file system.<p>
     * 
     * @return the prefix for exported links in the "real" file system
     */
    public String getRfsPrefix() {

        return m_rfsPrefix;
    }
    
    /**
     * Returns the prefix for exported links in the "real" file system with unsubstituted context values.<p>
     * 
     * @return the prefix for exported links in the "real" file system with unsubstituted context values.
     */
    public String getRfsPrefixUnsubstituted() {

        return m_rfsPrefixUnsubstituted;
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
     * Returns the static export rfs name for a give vfs resoure which includes parameters.<p>
     * 
     * @param cms an initialized cms context
     * @param vfsName the name of the vfs resource
     * @param parameters the parameters of this vfs resource
     * @return the static export rfs name for a give vfs resoure
     */
    public String getTranslatedRfsName(CmsObject cms, String vfsName, String parameters) {

        // build the RFS name for the link with parameters
        StringBuffer buf = new StringBuffer(128);
        buf.append(getRfsName(cms, vfsName));
        buf.append('_');
        buf.append(OpenCms.getLinkManager().hashCode(parameters));
        buf.append(".html");
        String translatedRfsName = buf.toString();

        // we have found a new rfs name for a vfs resource with parameters, so save it to the database
        try {
            cms.writeStaticExportPublishedResource(
                translatedRfsName.substring(getRfsPrefix().length()),
                C_EXPORT_LINK_WITH_PARAMETER,
                parameters,
                System.currentTimeMillis());
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Failed to write RFS resource '" + translatedRfsName + "' to database ", e);
            }
        }
        return translatedRfsName;
    }

    /**
     * Returns the prefix for internal links in the vfs.<p>
     * 
     * @return the prefix for internal links in the vfs
     */
    public String getVfsPrefix() {

        return m_vfsPrefix;
    }

    /**
     * Returns the prefix for internal links in the vfs with unsubstituted context values.<p>
     * 
     * @return the prefix for internal links in the vfs with unsubstituted context values
     */
    public String getVfsPrefixUnsubstituted() {

        return m_vfsPrefixUnsubstituted;
    }

    /**
     * Initializes the static export manager with the OpenCms system configuration.<p>
     * 
     * @param cms an OpenCms context object
     */
    public void initialize(CmsObject cms) {

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isDebugEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).debug("Created static export manager" + ((cms != null) ? (" with CmsObject " + cms) : ""));
        }

        LRUMap lruMap1 = new LRUMap(2048);
        m_cacheOnlineLinks = Collections.synchronizedMap(lruMap1);
        if (OpenCms.getMemoryMonitor().enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_cacheOnlineLinks", lruMap1);
        }

        LRUMap lruMap2 = new LRUMap(2048);
        m_cacheExportUris = Collections.synchronizedMap(lruMap2);
        if (OpenCms.getMemoryMonitor().enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_cacheExportUris", lruMap2);
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
                
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Static export        : " + (isStaticExportEnabled() ? "enabled" : "disabled"));
            if (isStaticExportEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export default       : " + getExportPropertyDefault());
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export path          : " + getExportPath());
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export rfs prefix    : " + getRfsPrefix());
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export vfs prefix    : " + getVfsPrefix());
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export link style    : " + (relativLinksInExport() ? "relative" : "absolute"));
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export mode          : " + (getMode()));
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export URL           : " + (getExportUrl()));
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export optimization  : " + (getPlainExportOptimization()));
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export testresource  : " + (getTestResource()));
            }
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
     * Returns true if the static export is set to export on publish.<p>
     * 
     * @return true if the static export is set to export on publish
     */
    public boolean isStaticExportOnDemand() {

        return m_staticExportOnDemand;
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
        if (resourceName.indexOf(".") > 0) {
            String suffix = resourceName.substring(resourceName.lastIndexOf(".")).toLowerCase();
            if (m_exportSuffixes.contains(suffix)) {
                return true;
            }
        } else {
            return false;
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
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export headers       : " + exportHeader);
            }
            m_exportHeaders.add(exportHeader);
        } else {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(". Export headers       : " + "invalid header: " + exportHeader + ", using default headers");
            }
        }
    }

    /**
     * Sets the path where the static export is written.<p>
     * 
     * @param path the path where the static export is written
     */
    public void setExportPath(String path) {

       m_staticExportPath = path;
       if (!m_staticExportPath.endsWith(File.separator)) {      
            m_staticExportPath += File.separator;
        }
        m_staticExportPathUnmodified = m_staticExportPath;
        m_staticExportPath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(m_staticExportPath);
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
        m_exportUrlUnsubstituted = url;
    }
    
    /**
     * Sets the mode in which the static export is working.<p>
     * 
     * Possible values are "on-demand" and "after-publish".
     * 
     * @param mode the mode of the static export
     */
    public void setMode(String mode) {
        if (mode.equals(C_MODE_ONDEMAND)) {
            m_staticExportOnDemand = true;
        } else {
            m_staticExportOnDemand = false;
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

        m_rfsPrefix = insertContextStrings(rfsPrefix);
        m_rfsPrefixUnsubstituted = rfsPrefix;
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

        m_vfsPrefix = insertContextStrings(vfsPrefix);
        m_vfsPrefixUnsubstituted = vfsPrefix;
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
        setExportnames();
        if (OpenCms.getLog(this).isDebugEnabled()) {
            String eventType = "EVENT_CLEAR_CACHES";
            if (event.getType() != I_CmsEventListener.EVENT_CLEAR_CACHES) {
                eventType = "EVENT_PUBLISH_PROJECT";
            }
            OpenCms.getLog(this).debug("Static export manager flushed caches after recieving event " + eventType);
        }
    }

    /**
     * Creates the parent folder for a exported resource in the RFS.<p>
     * 
     * @param rfsName the name of the resource
     * @throws CmsException if the folder could not be created
     */
    private void createExportFolder(String rfsName) throws CmsException {

        String exportFolderName = CmsFileUtil.normalizePath(getExportPath()
            + CmsResource.getFolderPath(rfsName).substring(1));
        File exportFolder = new File(exportFolderName);
        if (!exportFolder.exists()) {
            if (!exportFolder.mkdirs()) {
                throw new CmsException("Creation of static export folder failed for RFS file " + rfsName);
            }
        }
    }

    /**
     * Does the actual static export.<p>
     *  
     * @param resources a list of CmsPublishedREsources to start the static export with
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file      
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    private void doExportAfterPublish(List resources, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        boolean templatesFound;

        // export must be done in the context of the export user 
        CmsObject cmsExportObject = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

        // first export all non-template resources,
        templatesFound = exportNonTemplateResources(cmsExportObject, resources, report);

        // export template resourses (check "plainoptimization" setting)
        if ((templatesFound) || (!getQuickPlainExport())) {

            long timestamp = 0;
            List publishedTemplateResources;
            boolean newTemplateLinksFound;
            int linkMode = C_EXPORT_LINK_WITHOUT_PARAMETER;

            do {
                // get all template resources which are potential candidates for a static export
                publishedTemplateResources = cmsExportObject.readStaticExportResources(linkMode, timestamp);
                newTemplateLinksFound = publishedTemplateResources.size() > 0;
                if (newTemplateLinksFound) {
                    if (linkMode == C_EXPORT_LINK_WITHOUT_PARAMETER) {
                        // first loop, switch mode to parameter links, leave the timestamp unchanged
                        linkMode = C_EXPORT_LINK_WITH_PARAMETER;
                    } else {
                        // second and subsequent loops, only look for links not already exported
                        // this can only be the case for a link with parameters 
                        // that was present on a page also generated with parameters
                        timestamp = System.currentTimeMillis();
                    }
                    exportTemplateResources(publishedTemplateResources, report);
                }
                // if no new template links where found we are finished
            } while (newTemplateLinksFound);
        }
    }

    /**
     * Exports all non template resources found in a list of published resources.<p>
     * 
     * @param cms the current cms object
     * @param publishedResources the list of published resources
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     * @return true if some template resources were found whil looping the list of published resources
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    private boolean exportNonTemplateResources(CmsObject cms, List publishedResources, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        String vfsName = null;
        List resourcesToExport = new ArrayList();
        boolean templatesFound = false;

        int count = 1;

        report.println(report.key("report.staticexport.nontemplateresources_begin"), I_CmsReport.C_FORMAT_HEADLINE);

        // loop through all resources
        Iterator i = publishedResources.iterator();

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Starting export of non-template resources with " + publishedResources.size() + " possible candidates in list");
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
                        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(
                            exportData.getResource());
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
                                C_EXPORT_LINK_WITHOUT_PARAMETER,
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

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Found " + size + " resources to export");
        }

        while (i.hasNext()) {
            CmsStaticExportData exportData = (CmsStaticExportData)i.next();

            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Exporting "+exportData.getVfsName()+" -> "+exportData.getRfsName()+"...");
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
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info("Export "+exportData.getVfsName()+" -> "+exportData.getRfsName()+" [STATUS "+status+"]");
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
    private void exportTemplateResources(List publishedTemplateResources, I_CmsReport report) {

        int size = publishedTemplateResources.size();
        int count = 1;

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Starting export of template resources with " + size + " possible canditates in list");
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

            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Sending request for RFS file " + rfsName + " with url (" + exportUrlStr + ")");
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
                String exportFileName = CmsFileUtil.normalizePath(getExportPath() + rfsName.substring(1));
                File exportFile = new File(exportFileName);
                if (exportFile != null) {
                    long dateLastModified = exportFile.lastModified();
                    urlcon.setIfModifiedSince(dateLastModified);

                    if (OpenCms.getLog(this).isDebugEnabled()) {
                        OpenCms.getLog(this).debug("Request for RFS file " + exportFile.getName() + "' If-Modified-Since' header set to " + (dateLastModified / 1000) * 1000);
                    }
                }

                // now perform the request
                urlcon.connect();
                int status = urlcon.getResponseCode();
                urlcon.disconnect();

                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info("Request result for RFS file " + rfsName + " with url (" + exportUrlStr + ") was STATUS=" + status);
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

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Get all resources from vfs");
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

            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Got " + vfsResources.size() + " resources, building list now");
            }

            Iterator i = vfsResources.iterator();
            while (i.hasNext()) {
                vfsResource = (CmsResource)i.next();
                resource = new CmsPublishedResource(vfsResource);

                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Processing " + resource.getRootPath());
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
                if (!C_CACHEVALUE_404.equals(vfsName)) {
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

                vfsName = getVfsName(cms, rfsName);
                if (vfsName != null) {
                    match = true;
                    try {
                        resource = cms.readResource(vfsName);
                    } catch (CmsException e) {
                        rfsName = null;
                    }
                }

                if (!match) {
                    // no match found, nothing to export
                    cacheExportUri(rfsName, C_CACHEVALUE_404);
                    // it could be a translated resourcename with parameters, so make a lookup
                    // in the published resources table

                    String parameters = null;
                    try {
                        parameters = cms.readStaticExportPublishedResourceParameters(rfsName);
                        // there was a match in the db table, so get the StaticExportData 
                        if (parameters != null && parameters.length() > 0) {
                            return getStaticExportDataWithParameter(cms, rfsName, parameters);
                        } else {
                            return null;
                        }
                    } catch (CmsException e) {
                        return null;
                    }

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
     * Returns the export data for a requested resource, if null is returned no export is required.<p>
     *
     * @param cms an initialized cms context
     * @param rfsName the name of the rfs resource
     * @param parameters a query string of url parameters
     * @return the export data for the request, if null is returned no export is required
     * @throws CmsException in case of errors accessing the VFS
     */
    private CmsStaticExportData getStaticExportDataWithParameter(CmsObject cms, String rfsName, String parameters)
    throws CmsException {

        String vfsName = null;
        CmsStaticExportData data = null;

        // get the rfs base string without the parameter hashcode
        String rfsBaseName = rfsName.substring(0, rfsName.lastIndexOf('_'));

        // get the vfs base name, which is later used to read the resoruce in the vfs
        String vfsBaseName = getVfsName(cms, rfsBaseName);

        // everything is there, so read the resource in the vfs and build the static export data object
        if (vfsBaseName != null) {

            CmsResource resource = cms.readResource(vfsBaseName);

            data = new CmsStaticExportData(vfsBaseName, rfsName, resource, parameters);
            cacheExportUri(rfsName, vfsName);

        } else {
            throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + "] " + vfsBaseName);
        }

        return data;
    }

    /**
     * Gets the vfsName from a given rfsName.<p>
     * 
     * @param cms an initialized cms context
     * @param rfsName the name of the rfs resource
     * @return the name of the vfs resource
     */
    private String getVfsName(CmsObject cms, String rfsName) {

        String vfsName = null;
        CmsResource resource;

        boolean match = false;

        try {
            resource = cms.readResource(cms.getRequestContext().removeSiteRoot(rfsName));
            if (resource.isFolder() && !rfsName.endsWith("/")) {
                rfsName += "/";
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
                            if (!rfsName.endsWith("/")) {
                                rfsName += "/";
                            }
                            if (!vfsName.endsWith("/")) {
                                vfsName += "/";
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
            if (rfsName.endsWith(".jsp.html")) {
                rfsName = rfsName.substring(0, rfsName.lastIndexOf("."));
                return getVfsName(cms, rfsName);
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
        String value = path;

        // replace the "magic" names                 
        String servletName = OpenCms.getSystemInfo().getServletPath();
        String contextName = OpenCms.getSystemInfo().getContextPath();
        
        value = CmsStringUtil.substitute(value, "${CONTEXT_NAME}", contextName);
        value = CmsStringUtil.substitute(value, "${SERVLET_NAME}", servletName);  
        
        return value;
    }

    /**
     * Scrubs the "export" folder.<p>
     */
    private void scrubExportFolder() {

        String exportFolderName = CmsFileUtil.normalizePath(getExportPath());
        try {
            File exportFolder = new File(exportFolderName);
            // check if export file exists, if so delete it
            if (exportFolder.exists() && exportFolder.canWrite()) {
                CmsFileUtil.purgeDirectory(exportFolder);
                // write log message
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info("Static export deleted main export folder '" + exportFolderName + "'");
                }
            }
        } catch (Throwable t) {
            // ignore, nothing to do about this
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Error deleting static export folder rfsName='" + exportFolderName + "'", t);
            }
        }
    }

    /**
     * Scrubs all files from the export folder that might have been changed,
     * so that the export is newly created after the next request to the resource.<p>
     * 
     * @param publishHistoryId id of the last published project
     * @param onlyDeleted flag to remove only those files which were deleted in the publish proces
     */
    private void scrubExportFolders(CmsUUID publishHistoryId, boolean onlyDeleted) {
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Static export manager scrubbing export folders for project ID " + publishHistoryId);
        }      
        
        Set scrubedFolders = new HashSet();
        Set scrubedFiles = new HashSet();
        // get a export user cms context        
        CmsObject cms;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        } catch (CmsException e) {
            // this should never happen
            OpenCms.getLog(this).error("Could not init CmsObject with default export user");
            return;
        }
        List publishedResources;
        try {
            publishedResources = cms.readPublishedResources(publishHistoryId);
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Static export manager could not read list of changes resources for project ID " + publishHistoryId);
            }                    
            return;
        }
        Iterator it = publishedResources.iterator();
        while (it.hasNext()) {
            CmsPublishedResource res = (CmsPublishedResource)it.next();
            if (res.isUnChanged() || !res.isVfsResource()) {
                // unchanged resources and non vfs resources don't need to be deleted
                continue;
            }
            if ((onlyDeleted) && !res.isDeleted()) {
                // if the onlyDeelted switch is turned on, do not delete resources which are not 
                // marked as deleted
                continue;
            }

            List siblings = Collections.singletonList(res.getRootPath());
            if (res.getSiblingCount() > 1) {
                // ensure all siblings are scrubbed if the resource has one 
                try {
                    List li = cms.readSiblings(res.getRootPath(), CmsResourceFilter.ALL);
                    siblings = new ArrayList();
                    for (int i = 0, l = li.size(); i < l; i++) {
                        siblings.add(cms.getSitePath((CmsResource)li.get(i)));
                    }
                } catch (CmsException e) {
                    siblings = Collections.singletonList(res.getRootPath());
                }
            }

            for (int i = 0, l = siblings.size(); i < l; i++) {
                String vfsName = (String)siblings.get(i);
                // get the link name for the published file 
                String rfsName = getRfsName(cms, vfsName);
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Static export checking for deletion vfsName='" + vfsName + "' rfsName='" + rfsName + "'");
                }
                if (rfsName.startsWith(getRfsPrefix())
                    && (!scrubedFiles.contains(vfsName))
                    && (!scrubedFolders.contains(CmsResource.getFolderPath(vfsName)))) {
                    scrubedFiles.add(vfsName);
                    // this file could have been exported
                    String exportFileName;
                    if (res.isFolder()) {
                        if (res.isDeleted()) {
                            String exportFolderName = CmsFileUtil.normalizePath(getExportPath()
                                + rfsName.substring(getRfsPrefix().length() + 1));
                            try {
                                File exportFolder = new File(exportFolderName);
                                // check if export file exists, if so delete it
                                if (exportFolder.exists() && exportFolder.canWrite()) {
                                    CmsFileUtil.purgeDirectory(exportFolder);
                                    // write log message
                                    if (OpenCms.getLog(this).isInfoEnabled()) {
                                        OpenCms.getLog(this).info("Static export deleted export folder '" + exportFolderName + "'");
                                    }
                                    scrubedFolders.add(vfsName);
                                    continue;
                                }
                            } catch (Throwable t) {
                                // ignore, nothing to do about this
                                if (OpenCms.getLog(this).isWarnEnabled()) {
                                    OpenCms.getLog(this).warn("Error deleting static export folder vfsName='" + vfsName + "' rfsName='" + exportFolderName + "'", t);
                                }
                            }
                        }
                        // add index.html to folder name
                        rfsName += C_EXPORT_DEFAULT_FILE;
                        if (OpenCms.getLog(this).isDebugEnabled()) {
                            OpenCms.getLog(this).debug("Static export folder index file rfsName='" + rfsName + "'");
                        }
                    }
                    exportFileName = CmsFileUtil.normalizePath(getExportPath()
                        + rfsName.substring(getRfsPrefix().length() + 1));
                    try {
                        File exportFile = new File(exportFileName);
                        // check if export file exists, if so delete it
                        if (exportFile.exists() && exportFile.canWrite()) {
                            exportFile.delete();
                            // write log message
                            if (OpenCms.getLog(this).isInfoEnabled()) {
                                OpenCms.getLog(this).info("Static export deleted exported rfs file '" + rfsName + "'");
                            }
                        }
                    } catch (Throwable t) {
                        // ignore, nothing to do about this
                        if (OpenCms.getLog(this).isWarnEnabled()) {
                            OpenCms.getLog(this).warn("Error deleting static export file vfsName='" + vfsName + "' rfsName='" + exportFileName + "'", t);
                        }
                    }
                }
            }
        }
    }

    /**
     * Set the list of all resources that have the "exportname" property set.<p>
     */
    private synchronized void setExportnames() {

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Static export manager starting update of list of resources with 'exportname' property");
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
                String exportname = cms.readPropertyObject(foldername, I_CmsConstants.C_PROPERTY_EXPORTNAME, false)
                    .getValue();
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
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Static export manager finished update of list of resources with 'exportname' property");
        }
    }
}
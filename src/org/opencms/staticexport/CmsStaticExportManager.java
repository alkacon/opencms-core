/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportManager.java,v $
 * Date   : $Date: 2004/03/25 17:16:58 $
 * Version: $Revision: 1.46 $
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
import org.opencms.file.CmsResource;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsStringSubstitution;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Provides the functionaility to export resources from the OpenCms VFS
 * to the file system.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.46 $
 */
public class CmsStaticExportManager implements I_CmsEventListener {
    
    /** Cache value to indicate a true 404 error */
    private static final String C_CACHEVALUE_404 = "?404";
    
    /** Marker for error message attribute */
    public static final String C_EXPORT_ATTRIBUTE_ERROR_MESSAGE = "javax.servlet.error.message";
    
    /** Marker for error request uri attribute */
    public static final String C_EXPORT_ATTRIBUTE_ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    
    /** Marker for error servlet name attribute */
    public static final String C_EXPORT_ATTRIBUTE_ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";
    
    /** Marker for error status code attribute */
    public static final String C_EXPORT_ATTRIBUTE_ERROR_STATUS_CODE = "javax.servlet.error.status_code";
    
    /** Name for the folder default index file */
    public static final String C_EXPORT_DEFAULT_FILE = "index_export.html";
    
    /** Marker for externally redirected 404 uri's */
    public static final String C_EXPORT_MARKER = "exporturi";
    
    /** Cache for the export uris */
    private Map m_cacheExportUris;
    
    /** Cache for the online links */
    private Map m_cacheOnlineLinks;
    
    /** The additional http headers for the static export */
    private String[] m_exportHeaders;    
    
    /** List of all resources that have the "exportname" property set */
    private Map m_exportnameResources;

    /** Indicates if <code>true</code> is the default value for the property "export" */
    private boolean m_exportPropertyDefault;

    /** Indicates if links in the static export should be relative */
    private boolean m_exportRelativeLinks;
    
    /** List of export suffixes where the "export" property default is always "true" */
    private String[] m_exportSuffixes;

    /** Prefix to use for exported files */
    private String m_rfsPrefix;

    /** Indicates if the static export is enabled or diabled */
    private boolean m_staticExportEnabled;

    /** Indicates if the static export is switched to export on publish or export on demand */
    private boolean m_staticExportOnPublish;

    /** The path to where the static export will be written */
    private String m_staticExportPath;
    
    /** Prefix to use for internal OpenCms files */
    private String m_vfsPrefix;
    
    /** Export url to send internal requests to */
    private String m_exportUrl;
    
    /**
     * Creates a new static export property object.<p>
     */
    public CmsStaticExportManager() {
        m_exportRelativeLinks = false;
        m_staticExportEnabled = false;
        m_exportPropertyDefault = true;
                
        LRUMap lruMap1 = new LRUMap(1024);
        m_cacheOnlineLinks = Collections.synchronizedMap(lruMap1);
        if (OpenCms.getMemoryMonitor().enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_cacheOnlineLinks", lruMap1);
        }              
        
        LRUMap lruMap2 = new LRUMap(1024);
        m_cacheExportUris = Collections.synchronizedMap(lruMap2);
        if (OpenCms.getMemoryMonitor().enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_cacheExportUris", lruMap2);
        }
        
        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {
            I_CmsEventListener.EVENT_PUBLISH_PROJECT, 
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_UPDATE_EXPORTS
        });          
    }
    
    /**
     * Initializes the static export manager with the OpenCms system configuration.<p>
     * 
     * @param configuration the OpenCms configuration
     * @param cms an OpenCms context object (not used in static export manager)
     * @return the initialized site manager
     */
    public static CmsStaticExportManager initialize(ExtendedProperties configuration, CmsObject cms) {
        CmsStaticExportManager exportManager = new CmsStaticExportManager();
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isDebugEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).debug("Created static export manager" + ((cms != null)?(" with CmsObject " + cms):""));
        }
        
        // set if the static export is enabled or not
        exportManager.setStaticExportEnabled("true".equalsIgnoreCase(configuration.getString("staticexport.enabled", "false")));

         // set if the static export is set to export on publish or export on demand
        exportManager.setStaticExportOnPublish("true".equalsIgnoreCase(configuration.getString("staticexport.onpublish", "false")));

        // set the default value for the "export" property
        exportManager.setExportPropertyDefault("true".equalsIgnoreCase(configuration.getString("staticexport.export_default", "false")));
        
        // set the export URL
        exportManager.setExportUrl(configuration.getString("staticexport.url", "http://127.0.0.1:8080/opencms/handle404"));
        
        // set the export suffixes
        String[] exportSuffixes = configuration.getStringArray("staticexport.export_suffixes");
        if (exportSuffixes == null) {
            exportSuffixes = new String[0];
        }
        exportManager.setExportSuffixes(exportSuffixes);
        
        // set the path for the export
        exportManager.setExportPath(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(configuration.getString("staticexport.export_path", "export")));

        // replace the "magic" names                 
        String servletName = OpenCms.getSystemInfo().getServletPath(); 
        String contextName = OpenCms.getSystemInfo().getContextPath();
        
        // set the "magic" names in the extended properties
        configuration.setProperty("CONTEXT_NAME", contextName);
        configuration.setProperty("SERVLET_NAME", servletName);
        
        // set the export URL
        exportManager.setExportUrl(configuration.getString("staticexport.url", "http://127.0.0.1:8080/opencms/handle404"));
        
        
        // get the export prefix variables for rfs and vfs
        String rfsPrefix = configuration.getString("staticexport.prefix_rfs", contextName + "/export");
        String vfsPrefix = configuration.getString("staticexport.prefix_vfs", contextName + servletName);
        
        // set the export prefix variables for rfs and vfs
        exportManager.setRfsPrefix(rfsPrefix);
        exportManager.setVfsPrefix(vfsPrefix);    
        
        // set if links in the export should be relative or not
        exportManager.setExportRelativeLinks(configuration.getBoolean("staticexport.relative_links", false)); 

        // initialize "exportname" folders
        exportManager.setExportnames();
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Static export        : " + (exportManager.isStaticExportEnabled()?"enabled":"disabled"));
            if (exportManager.isStaticExportEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export default       : " + exportManager.getExportPropertyDefault());
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export path          : " + exportManager.getExportPath());
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export rfs prefix    : " + exportManager.getRfsPrefix());
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export vfs prefix    : " + exportManager.getVfsPrefix());
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export link style    : " + (exportManager.relativLinksInExport()?"relative":"absolute"));                
            }
        }               
        
        // initialize specific static export headers
        String[] exportHeaders = null;
        try {
            exportHeaders = configuration.getStringArray("staticexport.headers");
            for (int i = 0; i < exportHeaders.length; i++) {
                if (CmsStringSubstitution.split(exportHeaders[i], ":").length == 2) {
                    if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                        OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export headers       : " + exportHeaders[i]);
                    }
                } else {
                    if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                        OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(". Export headers       : " + "invalid header: " + exportHeaders[i] + ", using default headers");
                    }
                    exportHeaders = null;
                    break;
                }
            }
        } catch (Exception e) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(". Export headers       : non-critical error " + e.toString());
            }
        }
        exportManager.setExportHeaders(exportHeaders);
        
        return exportManager;
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
                CmsUUID publishHistoryId = new CmsUUID((String) event.getData().get("publishHistoryId"));
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Static export manager catched event EVENT_PUBLISH_PROJECT for project ID " + publishHistoryId);
                }
                
                // decide what to do, static export on publish or staitc export on demand
                
                if (isStaticExportOnPublish()) {
                    I_CmsReport report = (I_CmsReport) event.getData().get("report");                 
                    // static export on publish is enabled, so start the complete static export
                    try {                
                        exportOnPublish(event.getCmsObject(), publishHistoryId, report);                
                    } catch (Exception e) {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error during static export:", e);
                        }
                    }
                } else {
                    // static export on demand enabled, so scrub the export folders only                   
                    scrubExportFolders(publishHistoryId, false);
                }

                
                clearCaches(event);
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
     * @throws CmsException in case of errors accessing the VFS
     * @throws ServletException in case of errors accessing the servlet 
     * @throws IOException in case of erros writing to the export output stream
     */
    public void export(HttpServletRequest req, HttpServletResponse res, CmsObject cms, CmsStaticExportData data) 
    throws CmsException, IOException, ServletException {

        CmsFile file;
        // TODO: Check if setting site root to "/" still works with HTML pages that contain links
        cms.getRequestContext().setSiteRoot("/");
        String vfsName = data.getVfsName();
        String rfsName = data.getRfsName();
        CmsResource resource = data.getResource();
        String oldUri = null;
        
        // this flag signals if the export method is used for "export on demand" or 
        // "export on publish". 
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
            vfsName = vfsName + file.getName();
            rfsName += C_EXPORT_DEFAULT_FILE;
        }

        // check loader id for resource
        int loaderId = file.getLoaderId();
        I_CmsResourceLoader loader = OpenCms.getLoaderManager().getLoader(loaderId);
        if ((loader == null) || (! loader.isStaticExportEnabled())) {
            throw new CmsException("Unable to export VFS file " + vfsName + ", loader with id " + loaderId + " does not support static export");
        }

        FileOutputStream exportStream = null;
        File exportFile = null;
        String exportFileName = CmsLinkManager.normalizeRfsPath(getExportPath() + rfsName.substring(1));
        
        // only export those resource where the export property is set
        if (OpenCms.getLinkManager().exportRequired(cms, vfsName)) {
            // make sure all required parent folder exist
            createExportFolder(rfsName);
            
            // generate export file instance and output stream
            exportFile = new File(exportFileName);           

            try {
                exportStream = new FileOutputStream(exportFile);
            } catch (Throwable t) {
                throw new CmsException("Creation of static export output stream failed for RFS file " + exportFileName);
            }            
        }

        // ensure we have exactly the same setup as if called "the usual way"
        // we only have to do this in case of the static export on demand
        if (exportOnDemand) {        
            String mimetype = OpenCms.getLoaderManager().getMimeType(file.getName(), cms.getRequestContext().getEncoding());
            res.setContentType(mimetype);
            oldUri = cms.getRequestContext().getUri();
            cms.getRequestContext().setUri(vfsName);
        }
                                        
        // do the export
        loader.export(cms, file, exportStream, req, res);

        // update the file with the modification date from the server
        if (req != null) {
            Long timestamp = (Long)req.getAttribute(I_CmsConstants.C_HEADER_OPENCMS_EXPORT);
            if (timestamp != null) {
                exportFile.setLastModified(timestamp.longValue());
            }                                      
        }
                
        // set unused resources
        file = null;
        
        // close the export stream 
        if (exportStream != null) {
            exportStream.close();
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
        
    }    
    
    
    /**
     * Starts the static export on publish.<p>
     * 
     * Exports all modified resources after a publish process into the real FS.<p>
     *  
     * @param cms the current cms object
     * @param publishHistoryId the publichHistoryId of the published project
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file   
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    public synchronized void exportOnPublish(CmsObject cms, CmsUUID publishHistoryId, I_CmsReport report)
        throws CmsException, IOException, ServletException {
            
        // first export all non-template resources, this can be taken from the publish history
        List publishedResources = cms.readPublishedResources(publishHistoryId);
        exportNonTemplateResources(cms, publishedResources, report);
        
        exportTemplateResources(cms, report);
        // exportNonVfsResources(cms, publishedResources);
             
        // delete all resources deleted during the publish process
        scrubExportFolders(publishHistoryId, true);       
    }
    
    
    /**
     * Exports all non template resources found in a list of published resources.<p>
     * 
     * @param cms the current cms object
     * @param publishedResources the list of published resources
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    private void exportNonTemplateResources(CmsObject cms, List publishedResources, I_CmsReport report) 
        throws CmsException, IOException, ServletException {
       
        String vfsName=null;
        List resourcesToExport = new ArrayList();

        int count = 1;

        report.println(report.key("report.staticexport.nontemplateresources_begin"), I_CmsReport.C_FORMAT_HEADLINE);                        
        
        // loop through all resources
        Iterator i = publishedResources.iterator();
      
        while (i.hasNext()) {   
            CmsPublishedResource pupRes = (CmsPublishedResource)i.next();          
            
            vfsName = pupRes.getRootPath();
            
            // only export VFS files. COS data and foldersis handled elsewhere 
            if (pupRes.isVfsResource() && (pupRes.isFile())) {
                // get the export data object. if null is returned, this resource cannot be
                // exported.
                CmsStaticExportData exportData = getExportData(vfsName, cms);
              
                //
                if (exportData != null) {
                    // check loader for current resource if it must be processed before exported
                    I_CmsResourceLoader loader = OpenCms.getLoaderManager().getLoader(exportData.getResource().getLoaderId());
                    if (! loader.isStaticExportProcessable()) {
                        // this resource must not be process, so export it if its not marked as deleted
                        if (pupRes.getState() != I_CmsConstants.C_STATE_DELETED) {
                            // mark the resource for export to the real file system     
                            resourcesToExport.add(exportData);
                        } 
                    } else {
                        // the resource is a template resource. so store the name of it in the DB
                        // for further use.                       
                        cms.writeStaticExportPublishedResource(exportData.getRfsName(), 0, "");                            
                    }              
                }                    
            }
        }
        
        // now do the export
        i = resourcesToExport.iterator();
        int size = resourcesToExport.size();
        while (i.hasNext()) {
            CmsStaticExportData exportData = (CmsStaticExportData)i.next();
            report.print("("+ count++ +" / " + size + ") ", I_CmsReport.C_FORMAT_NOTE);
            report.print(report.key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
            report.print(exportData.getVfsName());
            report.print(report.key("report.dots"));
            export(null, null, cms, exportData);
            report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
        }
        
        resourcesToExport = null;
        
        report.println(report.key("report.staticexport.nontemplateresources_end"), I_CmsReport.C_FORMAT_HEADLINE);       
       
    }
    
    
    /**
     * Exports all template resources found in a list of published resources.<p>
     * 
     * @param cms the current cms object
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file    
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros sending a request
     */
    private void exportTemplateResources(CmsObject cms, I_CmsReport report) 
        throws CmsException, IOException {
                
        String rfsName;
        String url;
                                 
        // get all template resources which are potential candidates for an static export
        List publishedTemplateResources = cms.readStaticExportResources(false);
        int size = publishedTemplateResources.size(); 
        int count = 1;
        
        report.println(report.key("report.staticexport.templateresources_begin"), I_CmsReport.C_FORMAT_HEADLINE);                   
         
        // now loop through all of them and request them from the server
        Iterator i = publishedTemplateResources.iterator();

        // define everything we need to send the requests
        HttpClient client = new HttpClient();       
        Header exportHeader = new Header(I_CmsConstants.C_HEADER_OPENCMS_EXPORT, "true");   
        Header lastModified;
        
        try {
            cms.getRequestContext().saveSiteRoot();
            cms.getRequestContext().setSiteRoot("/");
                        
            while (i.hasNext()) {
                rfsName = (String) i.next();
                
                report.print("("+ count++ +" / "+size+") ", I_CmsReport.C_FORMAT_NOTE);
                report.print(report.key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
                report.print(rfsName);
                report.print(report.key("report.dots"));

                url = getExportUrl() + getRfsPrefix() + rfsName;              
                
                
                // now check the export property for this resource.
                // depending on its value, the export header will be set, which will later
                // trigger the export to the rfs. The content of the resource must
                // be scanned for links even if it is marked as not exportable.
                
                // we have created an url, so request the resource
                if (url != null) {
                    HttpMethod method = new GetMethod(url);
                    method.addRequestHeader(exportHeader);

                    // get the lastmodification date and add it to the request
                    String exportFileName = CmsLinkManager.normalizeRfsPath(getExportPath() + rfsName.substring(1));
                    File exportFile = new File(exportFileName);
                    if (exportFile != null) {
                        long timestamp = exportFile.lastModified();
                        lastModified = new Header(I_CmsConstants.C_HEADER_IF_MODIFIED_SINCE , new Long(timestamp).toString());
                        method.addRequestHeader(lastModified);
                    }
                    
                    // execute the method.                                      
                    client.executeMethod(method);
                    
                    //TODO: handle the response status
                    //statusCode = client.executeMethod(method);
                                        
                    report.println(report.key("report.ok")+" ", I_CmsReport.C_FORMAT_OK);
                                  
                    // release the connection.
                    method.releaseConnection();
                }     
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();           
        }
        report.println(report.key("report.staticexport.templateresources_end"), I_CmsReport.C_FORMAT_HEADLINE);       
       
    }
           
    /**
     * Returns a cached vfs resource name for the given rfs name
     * 
     * @param rfsName the name of the ref resource to get the cached vfs resource name for
     * @return a cached vfs resource name for the given rfs name, or null 
     */    
    public String getCachedExportUri(Object rfsName) {
        return (String)m_cacheExportUris.get(rfsName);
    }
    
    /**
     * Returns a cached link for the given vfs name
     * 
     * @param vfsName the name of the vfs resource to get the cached link for
     * @return a cached link for the given vfs name, or null 
     */
    public String getCachedOnlineLink(Object vfsName) {
        return (String)m_cacheOnlineLinks.get(vfsName);
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
    
            // check if we have the result already in the cache        
            vfsName = getCachedExportUri(rfsName);
            
            if (vfsName != null) {
                // this export uri is already cached            
                if (! C_CACHEVALUE_404.equals(vfsName)) {
                    // this uri can be exported
                    try {
                        resource = cms.readFileHeader(vfsName);
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
    
                try {
                    resource = cms.readFileHeader(cms.getRequestContext().removeSiteRoot(rfsName));
                    if (resource.isFolder() && !rfsName.endsWith("/")) {
                        rfsName += "/";
                    }
                    vfsName = rfsName;
                    match = true;
                } catch (Throwable t) {
                    // resource not found
                }
    
                if (! match) {
                    // name of export resource could not be resolved by reading the resource directly,
                    // now try to find a match with the "exportname" folders            
                    Map exportnameFolders = getExportnames();
                    Iterator i = exportnameFolders.keySet().iterator();
                    while (i.hasNext()) {
                        String exportName = (String)i.next();
                        if (rfsName.startsWith(exportName)) {
                            // prefix match
                            match = true;
                            // TODO: handle multiple matches         
                            vfsName = exportnameFolders.get(exportName) + rfsName.substring(exportName.length());
                            try {
                                resource = cms.readFileHeader(vfsName);
                                if (resource.isFolder()) {
                                    if (!rfsName.endsWith("/")) {
                                        rfsName += "/";
                                    }
                                    if (!vfsName.endsWith("/")) {
                                        vfsName += "/";
                                    }
                                }
                            } catch (CmsException e) {
                                rfsName = null;
                            }
                            break;
                        }
                    }
                }
                 
                if (!match) {
                    // no match found, nothing to export
                    cacheExportUri(rfsName, C_CACHEVALUE_404);
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
     * Returns the export data for the request, if null is returned no export is required.<p>
     * 
     * @param request the request to check for export data
     * @param cms an initialized cms context (should be initialized with the "Guest" user only
     * @return the export data for the request, if null is returned no export is required
     */
    public CmsStaticExportData getExportData(HttpServletRequest request, CmsObject cms) {                
        if (! isStaticExportEnabled()) {
            // export is diabled
            return null;
        }

        String rfsName = null;
        String vfsName = null;
               
        // build the rfs name during for the export on demand
        rfsName = request.getParameter(C_EXPORT_MARKER);
        if ((rfsName == null)) {
            rfsName = (String)request.getAttribute(C_EXPORT_ATTRIBUTE_ERROR_REQUEST_URI);
        }

        // this is a request created by the static export and directly send to 404 handler
        // so remove the leading handler identification
        if (request.getHeader(I_CmsConstants.C_HEADER_OPENCMS_EXPORT) != null) {

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
        
        return getExportData(rfsName, vfsName, cms);
    }

    /**
     * Returns specific http headers for the static export.<p>
     * 
     * If the header <code>Cache-Control</code> is set, OpenCms will not use its default headers.<p>
     * 
     * @return the list of http export headers from opencms.properties
     */
    public List getExportHeaders() {
        return Collections.unmodifiableList(Arrays.asList(m_exportHeaders));
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
     * Returns true if the default value for the resource property "export" is true.<p>
     * 
     * @return true if the default value for the resource property "export" is true
     */
    public boolean getExportPropertyDefault() {
        return m_exportPropertyDefault;
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
     * Returns the static export rfs name for a give vfs resoure.<p>
     * 
     * @param cms an initialized cms context
     * @param vfsName the name of the vfs resource
     * @return the static export rfs name for a give vfs resoure
     */
    public String getRfsName(CmsObject cms, String vfsName) {
        try {
            // check if the resource folder (or a parent folder) has the "exportname" property set
            String exportname = cms.readProperty(CmsResource.getFolderPath(vfsName), I_CmsConstants.C_PROPERTY_EXPORTNAME, true);
            if (exportname != null) {
                // "exportname" property set
                if (!exportname.endsWith("/")) {
                    exportname = exportname + "/";
                }
                if (!exportname.startsWith("/")) {
                    exportname = "/" + exportname;
                }
                String value;
                boolean cont;
                String resource = vfsName;
                do {
                    try {
                        value = cms.readProperty(resource, I_CmsConstants.C_PROPERTY_EXPORTNAME, false);
                        cont = ((value == null) && (!"/".equals(resource)));
                    } catch (CmsSecurityException se) {
                        // a security exception (probably no read permission) we return the current result                      
                        cont = false;
                    }
                    if (cont) {
                        resource = CmsResource.getParentFolder(resource);
                    }
                } while (cont);
                vfsName = exportname + vfsName.substring(resource.length());
            } else {
                // if "exportname" is not set we must add the site root 
                vfsName = cms.getRequestContext().addSiteRoot(vfsName);
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
     * Returns the prefix for internal links in the vfs.<p>
     * 
     * @return the prefix for internal links in the vfs
     */
    public String getVfsPrefix() {
        return m_vfsPrefix;
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
    public boolean isStaticExportOnPublish() {
        return m_staticExportOnPublish;
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
        for (int i=0; i<m_exportSuffixes.length; i++) {
            if (resourceName.endsWith(m_exportSuffixes[i])) {
                return true;
            }
        }
        return false;
    } 
    
    
    
    
    /**
     * Creates the parent folder for a exported resource in the RFS.<p>
     * 
     * @param rfsName the name of the resource
     * @throws CmsException if the folder could not be created
     */
    private void createExportFolder(String rfsName) throws CmsException {     
        String exportFolderName = CmsLinkManager.normalizeRfsPath(getExportPath() + CmsResource.getFolderPath(rfsName).substring(1));
        File exportFolder = new File(exportFolderName);
        if (!exportFolder.exists()) {
            if (!exportFolder.mkdirs()) {
                throw new CmsException("Creation of static export folder failed for RFS file " + rfsName);
            }
       }
    }
     
    
    /**
     * Deletes a directory in the file system and all subfolders of the directory.<p>
     * 
     * @param d the directory to delete
     */
    private void purgeDirectory(File d) {
        if (d.canRead() && d.isDirectory()) {
            java.io.File[] files = d.listFiles();             
            for (int i = 0; i<files.length; i++) {
                File f = files[i];
                if (f.isDirectory()) {
                    purgeDirectory(f);
                }
                if (f.canWrite()) {
                    f.delete();
                }
            }
        }      
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
     * Scrubs the "export" folder.<p>
     */
    private void scrubExportFolder() {
        String exportFolderName = CmsLinkManager.normalizeRfsPath(getExportPath());
        try {
            File exportFolder = new File(exportFolderName);
            // check if export file exists, if so delete it
            if (exportFolder.exists() && exportFolder.canWrite()) {
                purgeDirectory(exportFolder);
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
        Set scrubedFolders = new HashSet();
        Set scrubedFiles = new HashSet();
        // get a export user cms context        
        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
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
            if (res.getLinkCount() > 1) {
                // ensure all siblings are scrubbed if the resource has one 
                try {
                    List li = cms.getAllVfsLinks(res.getRootPath());
                    siblings = new ArrayList();
                    for (int i=0, l=li.size(); i < l; i++) {
                        siblings.add(cms.readAbsolutePath((CmsResource)li.get(i)));
                    }
                } catch (CmsException e) {
                    siblings = Collections.singletonList(res.getRootPath());
                }
            }
            
            for (int i=0, l=siblings.size(); i < l; i++) {      
                String vfsName = (String)siblings.get(i);    
                // get the link name for the published file 
                String rfsName = getRfsName(cms, vfsName);
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Static export checking for deletion vfsName='" + vfsName + "' rfsName='" + rfsName + "'");
                }
                if (rfsName.startsWith(getRfsPrefix()) && (!scrubedFiles.contains(vfsName)) && (!scrubedFolders.contains(CmsResource.getFolderPath(vfsName)))) {
                    scrubedFiles.add(vfsName);
                    // this file could have been exported
                    String exportFileName;
                    if (res.isFolder()) {
                        if (res.isDeleted()) {
                            String exportFolderName = CmsLinkManager.normalizeRfsPath(getExportPath() + rfsName.substring(getRfsPrefix().length()+1));
                            try {
                                File exportFolder = new File(exportFolderName);
                                // check if export file exists, if so delete it
                                if (exportFolder.exists() && exportFolder.canWrite()) {
                                    purgeDirectory(exportFolder);
                                    exportFolder.delete();
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
                    exportFileName = CmsLinkManager.normalizeRfsPath(getExportPath() + rfsName.substring(getRfsPrefix().length()+1));
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
     * Sets specific http headers for the static export.<p>
     * 
     * The format of the headers must be "header:value".<p> 
     *  
     * @param exportHeaders the list of http export headers to set
     */    
    private void setExportHeaders(String[] exportHeaders) {
        m_exportHeaders = exportHeaders;
        if (m_exportHeaders == null) {
            m_exportHeaders = new String[0];
        }
    }
    
    /**
     * Set the list of all resources that have the "exportname" property set.<p>
     */
    private synchronized void setExportnames() {        
        Vector resources;
        CmsObject cms = null;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            resources = cms.getResourcesWithPropertyDefinition(I_CmsConstants.C_PROPERTY_EXPORTNAME);
        } catch (CmsException e) {
            resources = new Vector(0);
        }
        
        m_exportnameResources = new HashMap(resources.size());
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            try {
                String foldername = cms.readAbsolutePath(res);
                String exportname = cms.readProperty(foldername, I_CmsConstants.C_PROPERTY_EXPORTNAME);
                if (exportname != null) {
                    if (! exportname.endsWith("/")) {
                        exportname = exportname + "/";
                    }
                    if (! exportname.startsWith("/")) {
                        exportname = "/" + exportname;
                    }
                    m_exportnameResources.put(exportname, foldername);
                }
            } catch (CmsException e) {
                // ignore exception, folder will no be added
            }
        }
        m_exportnameResources = Collections.unmodifiableMap(m_exportnameResources);
    }
    
    /**
     * Sets the path where the static export is written.<p>
     * 
     * @param path the path where the static export is written
     */
    private void setExportPath(String path) {
        m_staticExportPath = path;
        if (! m_staticExportPath.endsWith(File.separator)) {
            m_staticExportPath += File.separator;
        }
    }
    
    /**
     * Sets the default for the "export" resource property, 
     * possible values are "true", "false" or "dynamic".<p>
     *  
     * @param value the default for the "export" resource property
     */
    private void setExportPropertyDefault(boolean value) {
        m_exportPropertyDefault = value;
    }
    
    /**
     * Controls if links in exported files are relative or absolute.<p>
     * 
     * @param value if true, links in exported files are relative
     */
    private void setExportRelativeLinks(boolean value) {
        m_exportRelativeLinks = value;
    }

    /**
     * Sets the list of export suffices.<p>
     * 
     * @param exportSuffixes the list of export suffixes
     */
    private void setExportSuffixes(String[] exportSuffixes) {
        m_exportSuffixes = exportSuffixes;
    }

    /**
     * Sets the export url.<p>
     * 
     * @param url the export url
     */
    private void setExportUrl(String url) {
        m_exportUrl = url;
    }
    
    /**
     * Sets the prefix for exported links in the "real" file system.<p>
     * 
     * @param rfsPrefix the prefix for exported links in the "real" file system
     */
    private void setRfsPrefix(String rfsPrefix) {
        m_rfsPrefix = rfsPrefix;
    }
    
    /**
     * Controls if the static export is enabled or not.<p>
     * 
     * @param value if true, the static export is enabled
     */
    private void setStaticExportEnabled(boolean value) {
        m_staticExportEnabled = value;
    }


    /**
     * Controls if the static export is set to export on publish or export on demand.<p>
     * 
     * @param value if true, the static export is set to export on publish
     */
    private void setStaticExportOnPublish(boolean value) {   
        m_staticExportOnPublish = value;
    }

    /**
     * Sets the prefix for internal links in the vfs.<p>
     * 
     * @param vfsPrefix the prefix for internal links in the vfs
     */
    private void setVfsPrefix(String vfsPrefix) {
        m_vfsPrefix = vfsPrefix;
    }
}
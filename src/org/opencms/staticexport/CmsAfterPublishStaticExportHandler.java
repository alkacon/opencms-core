/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.ade.detailpage.CmsDetailPageUtil;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Implementation for the <code>{@link I_CmsStaticExportHandler}</code> interface.<p>
 * 
 * This handler exports all changes immediately after something is published.<p>
 * 
 * @since 6.0.0 
 * 
 * @see I_CmsStaticExportHandler
 */
public class CmsAfterPublishStaticExportHandler extends A_CmsStaticExportHandler {

    /** Header field set-cookie constant. */
    private static final String HEADER_FIELD_SET_COOKIE = "Set-Cookie";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAfterPublishStaticExportHandler.class);

    /** Request method get constant. */
    private static final String REQUEST_METHOD_GET = "GET";

    /** Request property cookie constant. */
    private static final String REQUEST_PROPERTY_COOKIE = "Cookie";

    /**
     * Does the actual static export.<p>
     *  
     * @param resources a list of CmsPublishedREsources to start the static export with
     * @param report an <code>{@link I_CmsReport}</code> instance to print output message, or <code>null</code> to write messages to the log file
     *       
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of errors writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    public void doExportAfterPublish(List<CmsPublishedResource> resources, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        boolean templatesFound;

        // export must be done in the context of the export user 
        // this will always use the root site
        CmsObject cmsExportObject = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

        List<CmsPublishedResource> resourcesToExport = getRelatedResources(cmsExportObject, resources);
        // first export all non-template resources
        templatesFound = exportNonTemplateResources(cmsExportObject, resourcesToExport, report);
        LOG.warn("finished exporting non-template resources. ");

        // export template resources (check "plainoptimization" setting)
        if ((templatesFound) || (!OpenCms.getStaticExportManager().getQuickPlainExport())) {
            CmsStaticExportManager manager = OpenCms.getStaticExportManager();

            // build resource filter set
            Set<String> resourceFilter = new HashSet<String>();
            for (CmsPublishedResource pubResource : resourcesToExport) {
                String rfsName = manager.getRfsName(cmsExportObject, pubResource.getRootPath());
                resourceFilter.add(rfsName.substring(manager.getRfsPrefixForRfsName(rfsName).length()));
            }

            long timestamp = 0;
            List<String> publishedTemplateResources;
            boolean newTemplateLinksFound;
            int linkMode = CmsStaticExportManager.EXPORT_LINK_WITHOUT_PARAMETER;
            do {
                // get all template resources which are potential candidates for a static export
                publishedTemplateResources = cmsExportObject.readStaticExportResources(linkMode, timestamp);
                if (publishedTemplateResources == null) {
                    break;
                }
                newTemplateLinksFound = publishedTemplateResources.size() > 0;
                if (newTemplateLinksFound) {
                    if (linkMode == CmsStaticExportManager.EXPORT_LINK_WITHOUT_PARAMETER) {
                        // first loop, switch mode to parameter links, leave the timestamp unchanged
                        linkMode = CmsStaticExportManager.EXPORT_LINK_WITH_PARAMETER;
                        // filter without parameter
                        publishedTemplateResources.retainAll(resourceFilter);
                    } else {
                        // second and subsequent loops, only look for links not already exported
                        // this can only be the case for a link with parameters 
                        // that was present on a page also generated with parameters
                        timestamp = System.currentTimeMillis();
                        // filter with parameter
                        Iterator<String> itPubTemplates = publishedTemplateResources.iterator();
                        while (itPubTemplates.hasNext()) {
                            String rfsName = itPubTemplates.next();
                            if (!resourceFilter.contains(rfsName.substring(0, rfsName.lastIndexOf('_')))) {
                                itPubTemplates.remove();
                            }
                        }
                    }
                    // leave if no template left
                    if (publishedTemplateResources.isEmpty()) {
                        break;
                    }
                    // export
                    LOG.warn("exporting template resources. ");
                    exportTemplateResources(cmsExportObject, publishedTemplateResources, report);
                }
                // if no new template links where found we are finished
            } while (newTemplateLinksFound);
        }
    }

    /**
     * Returns all resources within the current OpenCms site that are not marked as internal.<p>
     * 
     * The result list contains objects of type {@link CmsPublishedResource}.<p>
     * 
     * @param cms the cms context
     * 
     * @return all resources within the current OpenCms site that are not marked as internal
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsPublishedResource> getAllResources(CmsObject cms) throws CmsException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_GET_ALL_RESOURCES_0));
        }
        // TODO: to improve performance, get here only the resources to render from the configuration

        // read all from the root path, exclude resources flagged as internal        
        List<CmsResource> vfsResources = cms.readResources(
            "/",
            CmsResourceFilter.ALL.addExcludeFlags(CmsResource.FLAG_INTERNAL));

        CmsExportFolderMatcher matcher = OpenCms.getStaticExportManager().getExportFolderMatcher();
        // loop through the list and create the list of CmsPublishedResources
        List<CmsPublishedResource> resources = new ArrayList<CmsPublishedResource>(vfsResources.size());
        Iterator<CmsResource> i = vfsResources.iterator();
        while (i.hasNext()) {
            CmsResource resource = i.next();
            if (!matcher.match(resource.getRootPath())) {
                // filter files that do not match the resources to render 
                continue;
            }
            CmsPublishedResource pubRes = new CmsPublishedResource(resource);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_PROCESSING_1, resource.getRootPath()));
            }
            resources.add(pubRes);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_NUM_RESOURCES_1, new Integer(resources.size())));
        }
        return resources;
    }

    /**
     * @see org.opencms.staticexport.I_CmsStaticExportHandler#performEventPublishProject(org.opencms.util.CmsUUID, org.opencms.report.I_CmsReport)
     */
    @Override
    public void performEventPublishProject(CmsUUID publishHistoryId, I_CmsReport report) {

        try {
            m_busy = true;
            exportAfterPublish(publishHistoryId, report);
        } catch (Throwable t) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_STATIC_EXPORT_ERROR_0), t);
            }
            if (report != null) {
                report.addError(t);
            }
        } finally {
            m_busy = false;
        }
    }

    /**
     * Starts the static export on publish.<p>
     * 
     * Exports all modified resources after a publish process into the real FS.<p>
     * 
     * @param publishHistoryId the publichHistoryId of the published project
     * @param report an <code>{@link I_CmsReport}</code> instance to print output message, or <code>null</code> to write messages to the log file   
     *  
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of errors writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    protected void exportAfterPublish(CmsUUID publishHistoryId, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        // first check if the test resource was published already
        // if not, we must do a complete export of all static resources
        String rfsName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath(
            OpenCms.getStaticExportManager().getTestResource())
            + OpenCms.getStaticExportManager().getTestResource());

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_CHECKING_TEST_RESOURCE_1, rfsName));
        }
        File file = new File(rfsName);
        if (!file.exists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_TEST_RESOURCE_NOT_EXISTANT_0));
            }
            // the file is not there, so export everything
            OpenCms.getStaticExportManager().exportFullStaticRender(true, report);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_TEST_RESOURCE_EXISTS_0));
            }

            // delete all resources deleted during the publish process, and retrieve the list of resources to actually export
            List<CmsPublishedResource> publishedResources = scrubExportFolders(publishHistoryId);

            // do the export
            doExportAfterPublish(publishedResources, report);
        }

    }

    /**
     * Exports all non template resources found in a list of published resources.<p>
     * 
     * @param cms the current cms object
     * @param publishedResources the list of published resources
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     * 
     * @return true if some template resources were found while looping the list of published resources
     * 
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of errors writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    protected boolean exportNonTemplateResources(
        CmsObject cms,
        List<CmsPublishedResource> publishedResources,
        I_CmsReport report) throws CmsException, IOException, ServletException {

        report.println(
            Messages.get().container(Messages.RPT_STATICEXPORT_NONTEMPLATE_RESOURCES_BEGIN_0),
            I_CmsReport.FORMAT_HEADLINE);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_EXPORTING_NON_TEMPLATE_1,
                new Integer(publishedResources.size())));
        }

        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
        List<CmsStaticExportData> resourcesToExport = new ArrayList<CmsStaticExportData>();
        boolean templatesFound = readNonTemplateResourcesToExport(cms, publishedResources, resourcesToExport);

        int count = 1;
        int size = resourcesToExport.size();
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_NUM_EXPORT_1, new Integer(size)));
        }
        // now do the export
        Iterator<CmsStaticExportData> i = resourcesToExport.iterator();
        while (i.hasNext()) {
            CmsStaticExportData exportData = i.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_EXPORT_FILE_2,
                    exportData.getVfsName(),
                    exportData.getRfsName()));
            }

            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    new Integer(count++),
                    new Integer(size)),
                I_CmsReport.FORMAT_NOTE);
            report.print(Messages.get().container(Messages.RPT_EXPORTING_0), I_CmsReport.FORMAT_NOTE);
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                exportData.getVfsName()));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
            int status = manager.export(null, null, cms, exportData);
            if (status == HttpServletResponse.SC_OK) {
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            } else {
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_IGNORED_0),
                    I_CmsReport.FORMAT_NOTE);
            }

            if (LOG.isInfoEnabled()) {
                Object[] arguments = new Object[] {
                    exportData.getVfsName(),
                    exportData.getRfsName(),
                    new Integer(status)};
                LOG.info(Messages.get().getBundle().key(Messages.LOG_EXPORT_FILE_STATUS_3, arguments));
            }
            //don't lock up the CPU exclusively - allow other Threads to run as well 
            Thread.yield();
        }

        resourcesToExport = null;

        report.println(
            Messages.get().container(Messages.RPT_STATICEXPORT_NONTEMPLATE_RESOURCES_END_0),
            I_CmsReport.FORMAT_HEADLINE);

        return templatesFound;
    }

    /**
     * Exports all sitemap resources found in a list of published resources.<p>
     * 
     * @param entry the sitemap entry 
     * @param data the export data
     * @param cookies cookies to keep the session
     * 
     * @return the status of the http request used to perform the export
     * 
     * @throws IOException if something goes wrong 
     */
    //    protected int exportSitemapResource(CmsSitemapEntry entry, CmsStaticExportData data, StringBuffer cookies)
    //    throws IOException {
    //
    //        String vfsName = data.getVfsName();
    //        String rfsName = data.getRfsName();
    //        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
    //
    //        // check if the resource to export is a detail page
    //        // TODO: later when the property concept is finished this info is unnecessary
    //        String path = vfsName;
    //        boolean exportPropertyAlreadyReadFromVFS = false;
    //        if (path.endsWith("/")) {
    //            path = path.substring(0, path.length() - 1);
    //            String detailId = CmsResource.getName(path);
    //            if (CmsUUID.isValidUUID(detailId)) {
    //                exportPropertyAlreadyReadFromVFS = true;
    //            }
    //        }
    //
    //        try {
    //            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
    //            Map<String, String> props = entry.getProperties(true);
    //            // if a container page was found in the vfs and 
    //            if (((props.get("export") != null) && props.get("export").equals("true"))
    //                || exportPropertyAlreadyReadFromVFS) {
    //                // calculate rfs name
    //                rfsName = manager.getRfsName(exportCms, entry.getRootPath());
    //
    //                String exportUrlStr;
    //                if (rfsName.contains(manager.getRfsPrefix(vfsName))) {
    //                    exportUrlStr = manager.getExportUrl() + rfsName;
    //                } else {
    //                    exportUrlStr = manager.getExportUrl() + manager.getRfsPrefix(vfsName) + rfsName;
    //                }
    //                if (LOG.isDebugEnabled()) {
    //                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_SENDING_REQUEST_2, rfsName, exportUrlStr));
    //                }
    //                // setup the connection and request the resource
    //                URL exportUrl = new URL(exportUrlStr);
    //                HttpURLConnection.setFollowRedirects(false);
    //                HttpURLConnection urlcon = (HttpURLConnection)exportUrl.openConnection();
    //                // set request type to GET
    //                urlcon.setRequestMethod(REQUEST_METHOD_GET);
    //                // add special export header
    //                urlcon.setRequestProperty(CmsRequestUtil.HEADER_OPENCMS_EXPORT, CmsStringUtil.TRUE);
    //                // add additional headers if available
    //                if (manager.getAcceptLanguageHeader() != null) {
    //                    urlcon.setRequestProperty(CmsRequestUtil.HEADER_ACCEPT_LANGUAGE, manager.getAcceptLanguageHeader());
    //                } else {
    //                    urlcon.setRequestProperty(
    //                        CmsRequestUtil.HEADER_ACCEPT_LANGUAGE,
    //                        manager.getDefaultAcceptLanguageHeader());
    //                }
    //                if (manager.getAcceptCharsetHeader() != null) {
    //                    urlcon.setRequestProperty(CmsRequestUtil.HEADER_ACCEPT_CHARSET, manager.getAcceptCharsetHeader());
    //                } else {
    //                    urlcon.setRequestProperty(
    //                        CmsRequestUtil.HEADER_ACCEPT_CHARSET,
    //                        manager.getDefaultAcceptCharsetHeader());
    //                }
    //
    //                // get the last modified date and add it to the request
    //                String exportFileName = CmsFileUtil.normalizePath(manager.getExportPath(vfsName) + rfsName);
    //                File exportFile = new File(exportFileName);
    //                long dateLastModified = exportFile.lastModified();
    //                // system folder case
    //                if (vfsName.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
    //                    // iterate over all rules
    //                    for (CmsStaticExportRfsRule rule : manager.getRfsRules()) {
    //                        if (rule.match(vfsName)) {
    //                            exportFileName = CmsFileUtil.normalizePath(rule.getExportPath() + rfsName);
    //                            exportFile = new File(exportFileName);
    //                            if (dateLastModified > exportFile.lastModified()) {
    //                                dateLastModified = exportFile.lastModified();
    //                            }
    //                        }
    //                    }
    //                }
    //                urlcon.setIfModifiedSince(dateLastModified);
    //                if (LOG.isDebugEnabled()) {
    //                    LOG.debug(Messages.get().getBundle().key(
    //                        Messages.LOG_IF_MODIFIED_SINCE_SET_2,
    //                        exportFile.getName(),
    //                        new Long((dateLastModified / 1000) * 1000)));
    //                }
    //                if (cookies.length() > 0) {
    //                    // set the cookies, included the session id to keep the same session
    //                    urlcon.setRequestProperty(REQUEST_PROPERTY_COOKIE, cookies.toString());
    //                }
    //
    //                // now perform the request
    //                urlcon.connect();
    //                int status = urlcon.getResponseCode();
    //
    //                if (cookies.length() == 0) {
    //                    //Now retrieve the cookies. The jsessionid is here
    //                    cookies.append(urlcon.getHeaderField(HEADER_FIELD_SET_COOKIE));
    //                    if (LOG.isDebugEnabled()) {
    //                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_STATICEXPORT_COOKIES_1, cookies));
    //                    }
    //                }
    //                urlcon.disconnect();
    //                if (LOG.isInfoEnabled()) {
    //                    LOG.info(Messages.get().getBundle().key(
    //                        Messages.LOG_REQUEST_RESULT_3,
    //                        rfsName,
    //                        exportUrlStr,
    //                        new Integer(status)));
    //                }
    //                return status;
    //            }
    //        } catch (CmsException e) {
    //            LOG.error(e.getLocalizedMessage(), e);
    //        }
    //        return HttpServletResponse.SC_SEE_OTHER;
    //    }

    /**
     * Exports a single (template) resource specified by its export data.<p>
     * 
     * @param data the export data
     * @param cookies cookies to keep the session
     * 
     * @return the status of the http request used to perform the export
     * 
     * @throws IOException if the http request fails
     */
    protected int exportTemplateResource(CmsStaticExportData data, StringBuffer cookies) throws IOException {

        String vfsName = data.getVfsName();
        String rfsName = data.getRfsName();
        CmsStaticExportManager manager = OpenCms.getStaticExportManager();

        String exportUrlStr;
        if (rfsName.contains(manager.getRfsPrefix(vfsName))) {
            LOG.info("rfsName " + rfsName + " contains rfsPrefix " + manager.getRfsPrefix(vfsName));
            exportUrlStr = manager.getExportUrl() + rfsName;
        } else {
            exportUrlStr = manager.getExportUrl() + manager.getRfsPrefix(vfsName) + rfsName;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SENDING_REQUEST_2, rfsName, exportUrlStr));
        }
        // setup the connection and request the resource
        URL exportUrl = new URL(exportUrlStr);
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection urlcon = (HttpURLConnection)exportUrl.openConnection();
        // set request type to GET
        urlcon.setRequestMethod(REQUEST_METHOD_GET);
        // add special export header
        urlcon.setRequestProperty(CmsRequestUtil.HEADER_OPENCMS_EXPORT, CmsStringUtil.TRUE);
        // add additional headers if available
        if (manager.getAcceptLanguageHeader() != null) {
            urlcon.setRequestProperty(CmsRequestUtil.HEADER_ACCEPT_LANGUAGE, manager.getAcceptLanguageHeader());
        } else {
            urlcon.setRequestProperty(CmsRequestUtil.HEADER_ACCEPT_LANGUAGE, manager.getDefaultAcceptLanguageHeader());
        }
        if (manager.getAcceptCharsetHeader() != null) {
            urlcon.setRequestProperty(CmsRequestUtil.HEADER_ACCEPT_CHARSET, manager.getAcceptCharsetHeader());
        } else {
            urlcon.setRequestProperty(CmsRequestUtil.HEADER_ACCEPT_CHARSET, manager.getDefaultAcceptCharsetHeader());
        }

        // get the last modified date and add it to the request
        String exportFileName = CmsFileUtil.normalizePath(manager.getExportPath(vfsName) + rfsName);
        File exportFile = new File(exportFileName);
        long dateLastModified = exportFile.lastModified();
        // system folder case
        if (vfsName.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
            // iterate over all rules
            Iterator<CmsStaticExportRfsRule> it = manager.getRfsRules().iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = it.next();
                if (rule.match(vfsName)) {
                    exportFileName = CmsFileUtil.normalizePath(rule.getExportPath() + rfsName);
                    exportFile = new File(exportFileName);
                    if (dateLastModified > exportFile.lastModified()) {
                        dateLastModified = exportFile.lastModified();
                    }
                }
            }
        }
        urlcon.setIfModifiedSince(dateLastModified);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_IF_MODIFIED_SINCE_SET_2,
                exportFile.getName(),
                new Long((dateLastModified / 1000) * 1000)));
        }
        if (cookies.length() > 0) {
            // set the cookies, included the session id to keep the same session
            urlcon.setRequestProperty(REQUEST_PROPERTY_COOKIE, cookies.toString());
        }

        // now perform the request
        urlcon.connect();
        int status = urlcon.getResponseCode();

        if (cookies.length() == 0) {
            //Now retrieve the cookies. The jsessionid is here
            cookies.append(urlcon.getHeaderField(HEADER_FIELD_SET_COOKIE));
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_STATICEXPORT_COOKIES_1, cookies));
            }
        }
        urlcon.disconnect();
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_REQUEST_RESULT_3,
                rfsName,
                exportUrlStr,
                new Integer(status)));
        }
        return status;
    }

    /**
     * Exports all template resources found in a list of published resources.<p>
     * 
     * @param cms the cms context, in the root site as Export user
     * @param publishedTemplateResources list of potential candidates to export
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file    
     */
    protected void exportTemplateResources(CmsObject cms, List<String> publishedTemplateResources, I_CmsReport report) {

        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
        int size = publishedTemplateResources.size();
        int count = 1;

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_EXPORT_TEMPLATES_1, new Integer(size)));
        }
        report.println(
            Messages.get().container(Messages.RPT_STATICEXPORT_TEMPLATE_RESOURCES_BEGIN_0),
            I_CmsReport.FORMAT_HEADLINE);

        StringBuffer cookies = new StringBuffer();
        // now loop through all of them and request them from the server
        Iterator<String> i = publishedTemplateResources.iterator();
        while (i.hasNext()) {
            String rfsName = i.next();
            CmsStaticExportData data = null;
            try {
                data = manager.getVfsNameInternal(cms, rfsName);
            } catch (CmsVfsResourceNotFoundException e) {
                String rfsBaseName = rfsName;
                int pos = rfsName.lastIndexOf('_');
                if (pos >= 0) {
                    rfsBaseName = rfsName.substring(0, pos);
                }
                try {
                    data = manager.getVfsNameInternal(cms, rfsBaseName);
                } catch (CmsVfsResourceNotFoundException e2) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(Messages.get().getBundle().key(
                            Messages.LOG_NO_INTERNAL_VFS_RESOURCE_FOUND_1,
                            new String[] {rfsName}));
                    }
                }
            }
            if (data != null) {
                data.setRfsName(rfsName);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        new Integer(count++),
                        new Integer(size)),
                    I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORTING_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    rfsName));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
            } else {
                // no valid resource found for rfs name (already deleted), skip it
                continue;
            }

            try {
                CmsResource resource = data.getResource();
                try {
                    Collection<String> detailPages = CmsDetailPageUtil.getAllDetailPagesWithUrlName(cms, resource);
                    for (String detailPageUri : detailPages) {
                        String altRfsName = manager.getRfsName(cms, detailPageUri);
                        CmsStaticExportData detailData = new CmsStaticExportData(
                            data.getVfsName(),
                            altRfsName,
                            data.getResource(),
                            data.getParameters());
                        exportTemplateResource(detailData, cookies);
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }

                int status = exportTemplateResource(data, cookies);

                // write the report
                if (status == HttpServletResponse.SC_OK) {
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                } else if (status == HttpServletResponse.SC_NOT_MODIFIED) {
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                        I_CmsReport.FORMAT_NOTE);
                } else if (status == HttpServletResponse.SC_SEE_OTHER) {
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_IGNORED_0),
                        I_CmsReport.FORMAT_NOTE);
                } else {
                    report.println(
                        org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_ARGUMENT_1,
                            new Integer(status)),
                        I_CmsReport.FORMAT_OK);
                }
            } catch (IOException e) {
                report.println(e);
            }
            //don't lock up the CPU exclusively - allow other Threads to run as well 
            Thread.yield();
        }
        report.println(
            Messages.get().container(Messages.RPT_STATICEXPORT_TEMPLATE_RESOURCES_END_0),
            I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * @see org.opencms.staticexport.A_CmsStaticExportHandler#getRelatedFilesToPurge(java.lang.String, java.lang.String)
     */
    @Override
    protected List<File> getRelatedFilesToPurge(String exportFileName, String vfsName) {

        return Collections.emptyList();
    }

    /**
     * Creates a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> objects containing all related resources of the VFS tree.<p>
     * 
     * If the static export has been triggered by the OpenCms workplace, publishedResources is null and all resources in the VFS tree are returned.<p>
     * If really an after publish static export is triggered, then only the related resources are returned.<p>
     *
     * @param cms the current cms object
     * @param publishedResources the list of published resources
     * 
     * @return list of CmsPulishedResource objects containing all resources of the VFS tree
     * 
     * @throws CmsException in case of errors accessing the VFS
     */
    protected List<CmsPublishedResource> getRelatedResources(
        CmsObject cms,
        List<CmsPublishedResource> publishedResources) throws CmsException {

        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            // switch to root site
            cms.getRequestContext().setSiteRoot("/");
            if (publishedResources == null) {
                // full static export
                return getAllResources(cms);
            } else {
                // after publish export
                Map<String, CmsPublishedResource> resourceMap = new HashMap<String, CmsPublishedResource>();
                Iterator<CmsPublishedResource> itPubRes = publishedResources.iterator();
                while (itPubRes.hasNext()) {
                    CmsPublishedResource pubResource = itPubRes.next();
                    // check the internal flag if the resource does still exist
                    // we cannot export with an internal flag
                    if (cms.existsResource(pubResource.getRootPath())) {
                        CmsResource vfsResource = cms.readResource(pubResource.getRootPath());
                        if (!vfsResource.isInternal()) {
                            // add only if not internal
                            // additionally, add all siblings of the resource
                            Iterator<CmsPublishedResource> itSiblings = getSiblings(cms, pubResource).iterator();
                            while (itSiblings.hasNext()) {
                                CmsPublishedResource sibling = itSiblings.next();
                                resourceMap.put(sibling.getRootPath(), sibling);
                            }
                        }
                    } else {
                        // the resource does not exist, so add them for deletion in the static export
                        resourceMap.put(pubResource.getRootPath(), pubResource);
                    }

                    boolean match = false;
                    Iterator<CmsStaticExportExportRule> itExportRules = OpenCms.getStaticExportManager().getExportRules().iterator();
                    while (itExportRules.hasNext()) {
                        CmsStaticExportExportRule rule = itExportRules.next();
                        Set<CmsPublishedResource> relatedResources = rule.getRelatedResources(cms, pubResource);
                        if (relatedResources != null) {
                            Iterator<CmsPublishedResource> itRelatedRes = relatedResources.iterator();
                            while (itRelatedRes.hasNext()) {
                                CmsPublishedResource relatedRes = itRelatedRes.next();
                                resourceMap.put(relatedRes.getRootPath(), relatedRes);
                            }
                            match = true;
                        }
                    }
                    // if one res does not match any rule, then export all files
                    if (!match) {
                        return getAllResources(cms);
                    }
                }
                return new ArrayList<CmsPublishedResource>(resourceMap.values());
            }
        } finally {
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
    }

    /**
     * Returns all siblings of the published resource as list of <code>CmsPublishedResource</code>.<p>
     * 
     * @param cms the cms object
     * @param pubResource the published resource
     * 
     * @return all siblings of the published resource
     * 
     * @throws CmsException if something goes wrong
     */
    protected Set<CmsPublishedResource> getSiblings(CmsObject cms, CmsPublishedResource pubResource)
    throws CmsException {

        Set<CmsPublishedResource> siblings = new HashSet<CmsPublishedResource>();
        for (Iterator<String> i = getSiblingsList(cms, pubResource.getRootPath()).iterator(); i.hasNext();) {
            String sibling = i.next();
            siblings.add(new CmsPublishedResource(cms.readResource(sibling)));
        }
        return siblings;
    }

    /**
     * Returns all non template resources found in a list of published resources.<p>
     * 
     * @param cms the current cms object
     * @param publishedResources the list of published resources
     * @param resourcesToExport the list of non-template resources
     * 
     * @return <code>true</code> if some template resources were found while looping the list of published resources
     * 
     * @throws CmsException in case of errors accessing the VFS
     */
    protected boolean readNonTemplateResourcesToExport(
        CmsObject cms,
        List<CmsPublishedResource> publishedResources,
        List<CmsStaticExportData> resourcesToExport) throws CmsException {

        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
        boolean templatesFound = false;
        // loop through all resources
        Iterator<CmsPublishedResource> i = publishedResources.iterator();
        while (i.hasNext()) {
            CmsPublishedResource pubRes = i.next();
            String vfsName = pubRes.getRootPath();
            // only process this resource, if it is within the tree of allowed folders for static export
            if (manager.getExportFolderMatcher().match(vfsName)) {
                // get the export data object, if null is returned, this resource cannot be exported
                CmsStaticExportData exportData = manager.getVfsExportData(cms, vfsName);
                if (exportData != null) {
                    CmsResource resource = null;
                    if (pubRes.isFile()) {
                        resource = exportData.getResource();
                    } else {
                        // the resource is a folder, check if PROPERTY_DEFAULT_FILE is set on folder
                        try {
                            resource = cms.readDefaultFile(vfsName);
                            //                            String defaultFileName = cms.readPropertyObject(
                            //                                vfsName,
                            //                                CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                            //                                false).getValue();
                            //                            if (defaultFileName != null) {
                            //                                resource = cms.readResource(vfsName + defaultFileName);
                            //                            }
                        } catch (CmsException e) {
                            // resource is (still) a folder, check default files specified in configuration
                            for (int j = 0; j < OpenCms.getDefaultFiles().size(); j++) {
                                String tmpResourceName = vfsName + OpenCms.getDefaultFiles().get(j);
                                try {
                                    resource = cms.readResource(tmpResourceName);
                                    break;
                                } catch (CmsException e1) {
                                    // ignore all other exceptions and continue the lookup process
                                }
                            }
                        }
                    }
                    if ((resource != null) && resource.isFile()) {
                        // check loader for current resource if it must be processed before exported
                        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(resource);
                        if (!loader.isStaticExportProcessable()) {
                            // this resource must not be processed, so export it (if it's not marked as deleted)
                            if (!pubRes.getState().isDeleted()) {
                                // mark the resource for export to the real file system                  
                                resourcesToExport.add(exportData);
                            }
                        } else {
                            // the resource is a template resource or a folder, so store the name of it in the DB for further use                  
                            templatesFound = true;
                            cms.writeStaticExportPublishedResource(
                                exportData.getRfsName(),
                                CmsStaticExportManager.EXPORT_LINK_WITHOUT_PARAMETER,
                                "",
                                System.currentTimeMillis());
                        }
                    }
                }
            }
        }

        return templatesFound;
    }
}
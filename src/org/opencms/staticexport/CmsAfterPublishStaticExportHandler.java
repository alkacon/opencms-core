/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsAfterPublishStaticExportHandler.java,v $
 * Date   : $Date: 2005/07/18 12:27:48 $
 * Version: $Revision: 1.15 $
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

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Implementation for the <code>{@link I_CmsStaticExportHandler}</code> interface.<p>
 * 
 * This handler exports all changes immediately after something is published.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 6.0.0 
 * 
 * @see I_CmsStaticExportHandler
 */
public class CmsAfterPublishStaticExportHandler implements I_CmsStaticExportHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAfterPublishStaticExportHandler.class);

    /** Indicates if this content handler is busy. */
    protected boolean m_busy;

    /**
     * Does the actual static export.<p>
     *  
     * @param resources a list of CmsPublishedREsources to start the static export with
     * @param report an <code>{@link I_CmsReport}</code> instance to print output message, or <code>null</code> to write messages to the log file
     *       
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    public void doExportAfterPublish(List resources, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        boolean templatesFound;

        // export must be done in the context of the export user 
        CmsObject cmsExportObject = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

        List resourcesToExport = getRelatedResources(cmsExportObject, resources);
        // first export all non-template resources
        templatesFound = exportNonTemplateResources(cmsExportObject, resourcesToExport, report);

        // export template resourses (check "plainoptimization" setting)
        if ((templatesFound) || (!OpenCms.getStaticExportManager().getQuickPlainExport())) {
            CmsStaticExportManager manager = OpenCms.getStaticExportManager();

            // build resource filter set
            Set resourceFilter = new HashSet();
            Iterator itExpRes = resourcesToExport.iterator();
            while (itExpRes.hasNext()) {
                CmsPublishedResource pubResource = (CmsPublishedResource)itExpRes.next();
                String rfsName = manager.getRfsName(cmsExportObject, pubResource.getRootPath());
                resourceFilter.add(rfsName.substring(manager.getRfsPrefixForRfsName(rfsName).length()));
            }

            long timestamp = 0;
            List publishedTemplateResources;
            boolean newTemplateLinksFound;
            int linkMode = CmsStaticExportManager.EXPORT_LINK_WITHOUT_PARAMETER;
            do {
                // get all template resources which are potential candidates for a static export
                publishedTemplateResources = cmsExportObject.readStaticExportResources(linkMode, timestamp);
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
                        Iterator itPubTemplates = publishedTemplateResources.iterator();
                        while (itPubTemplates.hasNext()) {
                            String rfsName = (String)itPubTemplates.next();
                            if (!resourceFilter.contains(rfsName.substring(0, rfsName.lastIndexOf('_')))) {
                                itPubTemplates.remove();
                            }
                        }
                    }
                    // leave if no template left
                    if (publishedTemplateResources == null || publishedTemplateResources.isEmpty()) {
                        break;
                    }
                    // export
                    exportTemplateResources(cmsExportObject, publishedTemplateResources, report);
                }
                // if no new template links where found we are finished
            } while (newTemplateLinksFound);
        }
    }

    /**
     * Gets all resources within the folder tree.<p>
     * Since the long min and max value do not work with the sql timestamp function in the driver, we must calculate 
     * some different, but usable start and endtime values first.<p>
     * 
     * @param cms the cms context
     * 
     * @return all resources within the folder tree
     * 
     * @throws CmsException if something goes wrong
     */
    public List getAllResources(CmsObject cms) throws CmsException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_GET_ALL_RESOURCES_0));
        }

        List resources = new ArrayList();
        //starttime to 01.01.1970
        long starttime = 0;
        // endtime to now plus one week
        long endtime = System.currentTimeMillis() + 604800000;
        List vfsResources = cms.getResourcesInTimeRange("/", starttime, endtime);

        // loop through the list and create the list of CmsPublishedResources
        Iterator i = vfsResources.iterator();
        while (i.hasNext()) {
            CmsResource vfsResource = (CmsResource)i.next();
            if ((vfsResource.getFlags() & CmsResource.FLAG_INTERNAL) == CmsResource.FLAG_INTERNAL) {
                // skip internal files
                continue;
            }
            CmsPublishedResource resource = new CmsPublishedResource(vfsResource);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_PROCESSING_1, resource.getRootPath()));
            }
            resources.add(resource);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_NUM_RESOURCES_1, new Integer(resources.size())));
        }
        return resources;
    }

    /**
     * @see org.opencms.staticexport.I_CmsStaticExportHandler#isBusy()
     */
    public boolean isBusy() {

        return m_busy;
    }

    /**
     * @see org.opencms.staticexport.I_CmsStaticExportHandler#performEventPublishProject(org.opencms.util.CmsUUID, org.opencms.report.I_CmsReport)
     */
    public void performEventPublishProject(CmsUUID publishHistoryId, I_CmsReport report) {

        try {
            m_busy = true;
            exportAfterPublish(publishHistoryId, report);
        } catch (Throwable t) {
            LOG.error(Messages.get().key(Messages.LOG_STATIC_EXPORT_ERROR_0), t);
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
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    private void exportAfterPublish(CmsUUID publishHistoryId, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        // first check if the test resource was published already
        // if not, we must do a complete export of all static resources
        String rfsName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath(
            OpenCms.getStaticExportManager().getTestResource())
            + OpenCms.getStaticExportManager().getTestResource());

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_CHECKING_TEST_RESOURCE_1, rfsName));
        }
        File file = new File(rfsName);
        if (!file.exists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_TEST_RESOURCE_NOT_EXISTANT_0));
            }
            // the file is not there, so export everything
            OpenCms.getStaticExportManager().exportFullStaticRender(true, report);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_TEST_RESOURCE_EXISTS_0));
            }

            // delete all resources deleted during the publish process
            scrubExportFolders(publishHistoryId);

            // get the list of published resources from the publish history table
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            List publishedResources = cms.readPublishedResources(publishHistoryId);

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
     * @return true if some template resources were found whil looping the list of published resources
     * 
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    private boolean exportNonTemplateResources(CmsObject cms, List publishedResources, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
        String vfsName = null;
        List resourcesToExport = new ArrayList();
        boolean templatesFound = false;

        int count = 1;

        report.println(
            Messages.get().container(Messages.RPT_STATICEXPORT_NONTEMPLATE_RESOURCES_BEGIN_0),
            I_CmsReport.FORMAT_HEADLINE);

        // loop through all resources
        Iterator i = publishedResources.iterator();

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_EXPORTING_NON_TEMPLATE_1, new Integer(publishedResources.size())));
        }

        while (i.hasNext()) {
            CmsPublishedResource pupRes = (CmsPublishedResource)i.next();

            vfsName = pupRes.getRootPath();

            // only process this resource, if it is within the tree of allowed folders for static export
            if (manager.getExportFolderMatcher().match(vfsName)) {

                // only export VFS files, other data is handled elsewhere 
                if (pupRes.isVfsResource()) {
                    // get the export data object, if null is returned, this resource cannot be exported
                    CmsStaticExportData exportData = manager.getExportData(vfsName, cms);
                    if (exportData != null) {
                        CmsResource resource = null;
                        if (pupRes.isFile()) {
                            resource = exportData.getResource();
                        } else {
                            // the resource is a folder, check if PROPERTY_DEFAULT_FILE is set on folder
                            try {
                                String defaultFileName = cms.readPropertyObject(
                                    vfsName,
                                    CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                                    false).getValue();
                                if (defaultFileName != null) {
                                    resource = cms.readResource(vfsName + defaultFileName);
                                }
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
                        if (resource != null) {
                            // check loader for current resource if it must be processed before exported
                            I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(resource);
                            if (!loader.isStaticExportProcessable()) {
                                // this resource must not be process, so export it if its not marked as deleted
                                if (pupRes.getState() != CmsResource.STATE_DELETED) {
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

            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_2,
                new Integer(count++),
                new Integer(size)), I_CmsReport.FORMAT_NOTE);
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
                LOG.info(Messages.get().key(Messages.LOG_EXPORT_FILE_STATUS_3, arguments));
            }
        }

        resourcesToExport = null;

        report.println(
            Messages.get().container(Messages.RPT_STATICEXPORT_NONTEMPLATE_RESOURCES_END_0),
            I_CmsReport.FORMAT_HEADLINE);

        return templatesFound;
    }

    /**
     * Exports all template resources found in a list of published resources.<p>
     * 
     * @param cms the cms context
     * @param publishedTemplateResources list of potential candidates to export
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file    
     */
    private void exportTemplateResources(CmsObject cms, List publishedTemplateResources, I_CmsReport report) {

        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
        int size = publishedTemplateResources.size();
        int count = 1;

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_EXPORT_TEMPLATES_1, new Integer(size)));
        }
        report.println(
            Messages.get().container(Messages.RPT_STATICEXPORT_TEMPLATE_RESOURCES_BEGIN_0),
            I_CmsReport.FORMAT_HEADLINE);

        // now loop through all of them and request them from the server
        Iterator i = publishedTemplateResources.iterator();

        while (i.hasNext()) {
            String rfsName = (String)i.next();
            // String rfsNameWithoutPrefix = rfsName.substring(manager.getRfsPrefixForRfsName(rfsName).length());
            String vfsName = manager.getVfsNameInternal(cms, rfsName);
            if (vfsName == null) {
                String rfsBaseName = rfsName.substring(0, rfsName.lastIndexOf('_'));
                vfsName = manager.getVfsNameInternal(cms, rfsBaseName);
            }

            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_2,
                new Integer(count++),
                new Integer(size)), I_CmsReport.FORMAT_NOTE);
            report.print(Messages.get().container(Messages.RPT_EXPORTING_0), I_CmsReport.FORMAT_NOTE);
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                rfsName));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            String exportUrlStr = manager.getExportUrl() + manager.getRfsPrefix(vfsName) + rfsName;

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
                urlcon.setRequestProperty(CmsRequestUtil.HEADER_OPENCMS_EXPORT, "true");
                // add additional headers if available
                if (manager.getAcceptLanguageHeader() != null) {
                    urlcon.setRequestProperty(CmsRequestUtil.HEADER_ACCEPT_LANGUAGE, manager.getAcceptLanguageHeader());
                } else {
                    urlcon.setRequestProperty(
                        CmsRequestUtil.HEADER_ACCEPT_LANGUAGE,
                        manager.getDefaultAcceptLanguageHeader());
                }
                if (manager.getAcceptCharsetHeader() != null) {
                    urlcon.setRequestProperty(CmsRequestUtil.HEADER_ACCEPT_CHARSET, manager.getAcceptCharsetHeader());
                } else {
                    urlcon.setRequestProperty(
                        CmsRequestUtil.HEADER_ACCEPT_CHARSET,
                        manager.getDefaultAcceptCharsetHeader());
                }

                // get the last modified date and add it to the request
                String exportFileName = CmsFileUtil.normalizePath(manager.getExportPath(vfsName) + rfsName);
                File exportFile = new File(exportFileName);
                long dateLastModified = exportFile.lastModified();
                // system folder case
                if (vfsName.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
                    // iterate over all rules
                    Iterator it = manager.getRfsRules().iterator();
                    while (it.hasNext()) {
                        CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
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
                    LOG.debug(Messages.get().key(
                        Messages.LOG_IF_MODIFIED_SINCE_SET_2,
                        exportFile.getName(),
                        new Long((dateLastModified / 1000) * 1000)));
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
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                } else if (status == HttpServletResponse.SC_NOT_MODIFIED) {
                    report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SKIPPED_0), I_CmsReport.FORMAT_NOTE);
                } else if (status == HttpServletResponse.SC_SEE_OTHER) {
                    report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_IGNORED_0), I_CmsReport.FORMAT_NOTE);
                } else {
                    report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        new Integer(status)), I_CmsReport.FORMAT_OK);
                }
            } catch (IOException e) {
                report.println(e);
            }
        }
        report.println(
            Messages.get().container(Messages.RPT_STATICEXPORT_TEMPLATE_RESOURCES_END_0),
            I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Creates a list of <code>{@link CmsPulishedResource}</code> objects containing all related resources of the VFS tree.<p>
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
    private List getRelatedResources(CmsObject cms, List publishedResources) throws CmsException {

        try {
            // switch to root site
            cms.getRequestContext().saveSiteRoot();
            cms.getRequestContext().setSiteRoot("/");
            if (publishedResources == null) {
                // full static export
                return getAllResources(cms);
            } else {
                // after publish export
                Set resourceSet = new HashSet();
                Iterator itPubRes = publishedResources.iterator();
                while (itPubRes.hasNext()) {
                    CmsPublishedResource pubResource = (CmsPublishedResource)itPubRes.next();
                    CmsResource vfsResource = cms.readResource(pubResource.getRootPath());
                    if ((vfsResource.getFlags() & CmsResource.FLAG_INTERNAL) != CmsResource.FLAG_INTERNAL) {
                        // add only if not internal
                        resourceSet.add(pubResource);
                    }
                    boolean match = false;
                    Iterator itExportRules = OpenCms.getStaticExportManager().getExportRules().iterator();
                    while (itExportRules.hasNext()) {
                        CmsStaticExportExportRule rule = (CmsStaticExportExportRule)itExportRules.next();
                        Set relatedResources = rule.getRelatedResources(cms, pubResource);
                        if (relatedResources != null) {
                            resourceSet.addAll(relatedResources);
                            match = true;
                        }
                    }
                    // if one res does not match any rule, then export all files
                    if (!match) {
                        return getAllResources(cms);
                    }
                }
                return new ArrayList(resourceSet);
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Scrubs all files from the export folder that might have been changed,
     * so that the export is newly created after the next request to the resource.<p>
     * 
     * @param publishHistoryId id of the last published project
     */
    private void scrubExportFolders(CmsUUID publishHistoryId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SCRUBBING_EXPORT_FOLDERS_1, publishHistoryId));
        }

        Set scrubedFolders = new HashSet();
        Set scrubedFiles = new HashSet();
        // get a export user cms context        
        CmsObject cms;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        } catch (CmsException e) {
            // this should never happen
            LOG.error(Messages.get().key(Messages.LOG_INIT_FAILED_0), e);
            return;
        }
        List publishedResources;
        try {
            publishedResources = cms.readPublishedResources(publishHistoryId);
        } catch (CmsException e) {
            LOG.error(Messages.get().key(Messages.LOG_READING_CHANGED_RESOURCES_FAILED_1, publishHistoryId), e);
            return;
        }
        Iterator it = publishedResources.iterator();
        while (it.hasNext()) {
            CmsPublishedResource res = (CmsPublishedResource)it.next();
            if (res.isUnChanged() || !res.isVfsResource()) {
                // unchanged resources and non vfs resources don't need to be deleted
                continue;
            }
            if (!res.isDeleted()) {
                // do not delete resources which are not 
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
                        siblings.add(((CmsResource)li.get(i)).getRootPath());
                    }
                } catch (CmsException e) {
                    siblings = Collections.singletonList(res.getRootPath());
                }
            }

            for (int i = 0, l = siblings.size(); i < l; i++) {
                String vfsName = (String)siblings.get(i);
                // get the link name for the published file, vfsName is root path
                String rfsName = OpenCms.getStaticExportManager().getRfsName(cms, vfsName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_CHECKING_STATIC_EXPORT_2, vfsName, rfsName));
                }
                if (rfsName.startsWith(OpenCms.getStaticExportManager().getRfsPrefix(vfsName))
                    && (!scrubedFiles.contains(vfsName))
                    && (!scrubedFolders.contains(CmsResource.getFolderPath(vfsName)))) {
                    scrubedFiles.add(vfsName);
                    // this file could have been exported
                    String exportFileName;
                    if (res.isFolder()) {
                        if (res.isDeleted()) {
                            String exportFolderName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath(
                                vfsName)
                                + rfsName.substring(OpenCms.getStaticExportManager().getRfsPrefix(vfsName).length()));
                            try {
                                File exportFolder = new File(exportFolderName);
                                // check if export file exists, if so delete it
                                if (exportFolder.exists() && exportFolder.canWrite()) {
                                    CmsFileUtil.purgeDirectory(exportFolder);
                                    // write log message
                                    if (LOG.isDebugEnabled()) {
                                        LOG.info(Messages.get().key(Messages.LOG_FOLDER_DELETED_1, exportFolderName));
                                    }
                                    scrubedFolders.add(vfsName);
                                    continue;
                                }
                            } catch (Throwable t) {
                                // ignore, nothing to do about this
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn(Messages.get().key(
                                        Messages.LOG_FOLDER_DELETION_FAILED_2,
                                        vfsName,
                                        exportFolderName));
                                }
                            }
                        }
                        // add index.html to folder name
                        rfsName += CmsStaticExportManager.EXPORT_DEFAULT_FILE;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().key(Messages.LOG_FOLDER_1, rfsName));
                        }
                    }
                    exportFileName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath(vfsName)
                        + rfsName.substring(OpenCms.getStaticExportManager().getRfsPrefix(vfsName).length() + 1));
                    try {
                        File exportFile = new File(exportFileName);
                        // check if export file exists, if so delete it
                        if (exportFile.exists() && exportFile.canWrite()) {
                            exportFile.delete();
                            // write log message
                            if (LOG.isInfoEnabled()) {
                                LOG.info(Messages.get().key(Messages.LOG_FILE_DELETED_1, rfsName));
                            }
                        }
                    } catch (Throwable t) {
                        // ignore, nothing to do about this
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(Messages.get().key(Messages.LOG_FILE_DELETION_FAILED_2, vfsName, exportFileName));
                        }
                    }
                }
            }
        }
    }

}
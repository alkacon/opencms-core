/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.relations;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.threads.A_CmsProgressThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Validates relations of resources in the OpenCms VFS.<p>
 *
 * Relations are, for instance, href attribs in anchor tags and src attribs in
 * image tags, as well as OpenCmsVfsFile values in Xml Content.<p>
 *
 * External links to targets outside the OpenCms VFS don't get validated.<p>
 *
 * Objects using this class are responsible to handle detected broken links.<p>
 *
 * @since 6.3.0
 */
public class CmsRelationSystemValidator {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRelationSystemValidator.class);

    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /**
     * Default constructor.<p>
     *
     * @param driverManager The Cms driver manager
     */
    public CmsRelationSystemValidator(CmsDriverManager driverManager) {

        m_driverManager = driverManager;
    }

    /**
     * Validates the relations against the online project.<p>
     *
     * The result is printed to the given report.<p>
     *
     * Validating references means to answer the question, whether
     * we would have broken links in the online project if the given
     * publish list would get published.<p>
     *
     * @param dbc the database context
     * @param publishList the publish list to validate
     * @param report a report to print messages
     *
     * @return a map with lists of invalid links
     *          (<code>{@link org.opencms.relations.CmsRelation}}</code> objects)
     *          keyed by root paths
     *
     * @throws Exception if something goes wrong
     */
    public Map<String, List<CmsRelation>> validateResources(
        CmsDbContext dbc,
        CmsPublishList publishList,
        I_CmsReport report)
    throws Exception {

        // check if progress should be set in the thread
        A_CmsProgressThread thread = null;
        if (Thread.currentThread() instanceof A_CmsProgressThread) {
            thread = (A_CmsProgressThread)Thread.currentThread();
        }

        Map<String, List<CmsRelation>> invalidResources = new HashMap<String, List<CmsRelation>>();
        boolean interProject = (publishList != null);
        if (report != null) {
            report.println(
                Messages.get().container(Messages.RPT_HTMLLINK_VALIDATOR_BEGIN_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
        List<CmsResource> resources = new ArrayList<CmsResource>();
        if (publishList == null) {
            CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION;
            List<I_CmsResourceType> resTypes = OpenCms.getResourceManager().getResourceTypes();
            Iterator<I_CmsResourceType> itTypes = resTypes.iterator();
            int count = 0;
            while (itTypes.hasNext()) {

                // set progress in thread (first 10 percent)
                count++;
                if (thread != null) {

                    if (thread.isInterrupted()) {
                        throw new CmsIllegalStateException(
                            org.opencms.workplace.commons.Messages.get().container(
                                org.opencms.workplace.commons.Messages.ERR_PROGRESS_INTERRUPTED_0));
                    }
                    thread.setProgress((count * 10) / resTypes.size());
                }

                I_CmsResourceType type = itTypes.next();
                if (type instanceof I_CmsLinkParseable) {
                    filter = filter.addRequireType(type.getTypeId());
                    try {
                        resources.addAll(
                            m_driverManager.readResources(
                                dbc,
                                m_driverManager.readResource(dbc, "/", filter),
                                filter,
                                true));
                    } catch (CmsException e) {
                        LOG.error(
                            Messages.get().getBundle().key(Messages.LOG_RETRIEVAL_RESOURCES_1, type.getTypeName()),
                            e);
                    }
                }
            }
        } else {
            resources.addAll(publishList.getAllResources());
        }

        // populate a lookup map with the project resources that
        // actually get published keyed by their resource names.
        // second, resources that don't get validated are ignored.
        Map<String, CmsResource> offlineFilesLookup = new HashMap<String, CmsResource>();
        Iterator<CmsResource> itResources = resources.iterator();
        int count = 0;
        while (itResources.hasNext()) {

            // set progress in thread (next 10 percent)
            count++;
            if (thread != null) {

                if (thread.isInterrupted()) {
                    throw new CmsIllegalStateException(
                        org.opencms.workplace.commons.Messages.get().container(
                            org.opencms.workplace.commons.Messages.ERR_PROGRESS_INTERRUPTED_0));
                }
                thread.setProgress(((count * 10) / resources.size()) + 10);
            }

            CmsResource resource = itResources.next();
            offlineFilesLookup.put(resource.getRootPath(), resource);
            offlineFilesLookup.put(resource.getStructureId().toString(), resource);
        }
        CmsProject project = dbc.currentProject();
        if (interProject) {
            try {
                project = m_driverManager.readProject(dbc, CmsProject.ONLINE_PROJECT_ID);
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        boolean foundBrokenLinks = false;
        for (int index = 0, size = resources.size(); index < size; index++) {

            // set progress in thread (next 20 percent; leave rest for creating the list and the html)
            if (thread != null) {

                if (thread.isInterrupted()) {
                    throw new CmsIllegalStateException(
                        org.opencms.workplace.commons.Messages.get().container(
                            org.opencms.workplace.commons.Messages.ERR_PROGRESS_INTERRUPTED_0));
                }
                thread.setProgress(((index * 20) / resources.size()) + 20);
            }

            CmsResource resource = resources.get(index);
            String resourceName = resource.getRootPath();

            if (report != null) {
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        Integer.valueOf(index + 1),
                        Integer.valueOf(size)),
                    I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_HTMLLINK_VALIDATING_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        dbc.removeSiteRoot(resourceName)));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
            }
            List<CmsRelation> brokenLinks = validateLinks(dbc, resource, offlineFilesLookup, project, report);
            if (brokenLinks.size() > 0) {
                // the resource contains broken links
                invalidResources.put(resourceName, brokenLinks);
                foundBrokenLinks = true;
            } else {
                // the resource contains *NO* broken links
                if (report != null) {
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                }
            }
        }

        if (foundBrokenLinks) {
            // print a summary if we found broken links in the validated resources
            if (report != null) {
                report.println(
                    Messages.get().container(Messages.RPT_HTMLLINK_VALIDATOR_ERROR_0),
                    I_CmsReport.FORMAT_ERROR);
            }
        }
        if (report != null) {
            report.println(
                Messages.get().container(Messages.RPT_HTMLLINK_VALIDATOR_END_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
        return invalidResources;
    }

    /**
     * Checks a link to a resource which has been deleted.<p>
     * @param relation
     *
     * @param link the URI of the resource which has a link to the deleted resource
     * @param fileLookup a lookup table of files to be published
     * @param relationTargets
     *
     * @return true if the resource which has a link to the deleted resource is also going to be deleted
     */
    protected boolean checkLinkForDeletedLinkTarget(
        CmsRelation relation,
        String link,
        Map<String, CmsResource> fileLookup) {

        boolean isValidLink = false;
        // since we are going to delete the resource
        // check if the linked resource is also to be deleted
        if (fileLookup.containsKey(link) || fileLookup.containsKey(relation.getSourceId().toString())) {
            // Technically, if the relation source is going to be published too and is not deleted, the link is not valid. But in that case, validateLinks will also be called for that resource and detect broken the broken link there.
            isValidLink = true;
        }
        return isValidLink;
    }

    /**
     * Checks a link from a resource which has changed.<p>
     *
     * @param dbc the current dbc
     * @param resource the link source
     * @param relation the relation
     * @param link the link target
     * @param project the current project
     * @param fileLookup a lookup table which contains the files which are going to be published
     *
     * @return true if the link will be valid after publishing
     */
    protected boolean checkLinkForNewOrChangedLinkSource(
        CmsDbContext dbc,
        CmsResource resource,
        CmsRelation relation,
        String link,
        CmsProject project,
        Map<String, CmsResource> fileLookup) {

        boolean isValidLink = true;
        // the link is valid...
        try {
            // ... if the linked resource exists in the online project
            // search the target of link in the online project
            try {
                link = m_driverManager.getVfsDriver(
                    dbc).readResource(dbc, project.getUuid(), relation.getTargetId(), true).getRootPath();
            } catch (CmsVfsResourceNotFoundException e) {
                // reading by id failed, this means that the link variable still equals relation.getTargetPath()
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_LINK_VALIDATION_READBYID_FAILED_2,
                            relation.getTargetId().toString(),
                            project.getName()),
                        e);
                }
                m_driverManager.getVfsDriver(dbc).readResource(dbc, project.getUuid(), relation.getTargetPath(), true);
            }
        } catch (CmsException e) {
            // ... or if the linked resource is a resource that gets actually published
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_LINK_VALIDATION_READBYPATH_FAILED_2,
                        relation.getTargetPath(),
                        project.getName()),
                    e);
            }
            if (!fileLookup.containsKey(link)) {
                isValidLink = false;
            }
        } finally {
            // ... and if the linked resource to be published get deleted
            if (fileLookup.containsKey(link)) {
                CmsResource offlineResource = fileLookup.get(link);
                if (offlineResource.getState().isDeleted()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_LINK_VALIDATION_RESOURCEDELETED_1, link));
                    }
                    isValidLink = false;
                }
            }
        }
        return isValidLink;
    }

    /**
     * Validates the links for the specified resource.<p>
     *
     * @param dbc the database context
     * @param resource the resource that will be validated
     * @param fileLookup a map for faster lookup with all resources keyed by their rootpath
     * @param project the project to validate
     * @param report the report to write to
     *
     * @return a list with the broken links as {@link CmsRelation} objects for the specified resource,
     *          or an empty list if no broken links were found
     */
    protected List<CmsRelation> validateLinks(
        CmsDbContext dbc,
        CmsResource resource,
        Map<String, CmsResource> fileLookup,
        CmsProject project,
        I_CmsReport report) {

        List<CmsRelation> brokenRelations = new ArrayList<CmsRelation>();
        Map<String, Boolean> validatedLinks = new HashMap<String, Boolean>();

        // get the relations
        List<CmsRelation> incomingRelationsOnline = new ArrayList<CmsRelation>();
        List<CmsRelation> outgoingRelationsOffline = new ArrayList<CmsRelation>();
        try {
            if (!resource.getState().isDeleted()) {
                // search the target of links in the current (offline) project
                outgoingRelationsOffline = m_driverManager.getRelationsForResource(
                    dbc,
                    resource,
                    CmsRelationFilter.TARGETS);
            } else {
                // search the source of links in the online project
                CmsProject currentProject = dbc.currentProject();
                dbc.getRequestContext().setCurrentProject(project);
                try {
                    incomingRelationsOnline = m_driverManager.getRelationsForResource(
                        dbc,
                        resource,
                        CmsRelationFilter.SOURCES);
                } finally {
                    dbc.getRequestContext().setCurrentProject(currentProject);
                }
            }
        } catch (CmsException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_LINK_SEARCH_1, resource), e);
            if (report != null) {
                report.println(
                    Messages.get().container(Messages.LOG_LINK_SEARCH_1, dbc.removeSiteRoot(resource.getRootPath())),
                    I_CmsReport.FORMAT_ERROR);
            }
            return brokenRelations;
        }

        List<CmsRelation> relations = new ArrayList<CmsRelation>();
        relations.addAll(incomingRelationsOnline);
        relations.addAll(outgoingRelationsOffline);
        // check the relations
        boolean first = true;
        Iterator<CmsRelation> itRelations = relations.iterator();
        while (itRelations.hasNext()) {
            CmsRelation relation = itRelations.next();
            String link;
            if (!resource.getState().isDeleted()) {
                link = relation.getTargetPath();
            } else {
                link = relation.getSourcePath();
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(link)) {
                // skip empty links
                continue;
            }
            if (validatedLinks.keySet().contains(link)) {
                // skip already validated links
                if (validatedLinks.get(link).booleanValue()) {
                    // add broken relation of different type
                    brokenRelations.add(relation);
                }
                continue;
            }
            boolean result;
            if (resource.getState().isDeleted()) {
                result = checkLinkForDeletedLinkTarget(relation, link, fileLookup);
            } else {
                result = checkLinkForNewOrChangedLinkSource(dbc, resource, relation, link, project, fileLookup);

            }
            boolean isValidLink = result;
            if (!isValidLink) {
                if (first) {
                    if (report != null) {
                        report.println(
                            Messages.get().container(Messages.RPT_HTMLLINK_FOUND_BROKEN_LINKS_0),
                            I_CmsReport.FORMAT_WARNING);
                    }
                    first = false;
                }
                brokenRelations.add(relation);
                if (report != null) {
                    if (!resource.getState().isDeleted()) {
                        report.println(
                            Messages.get().container(
                                Messages.RPT_HTMLLINK_BROKEN_TARGET_2,
                                relation.getSourcePath(),
                                dbc.removeSiteRoot(link)),
                            I_CmsReport.FORMAT_WARNING);
                    } else {
                        report.println(
                            Messages.get().container(
                                Messages.RPT_HTMLLINK_BROKEN_SOURCE_2,
                                dbc.removeSiteRoot(link),
                                relation.getTargetPath()),
                            I_CmsReport.FORMAT_WARNING);
                    }
                }
            }
            validatedLinks.put(link, Boolean.valueOf(!isValidLink));
        }
        return brokenRelations;
    }

}
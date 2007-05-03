/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsRelationSystemValidator.java,v $
 * Date   : $Date: 2007/05/03 14:09:46 $
 * Version: $Revision: 1.1.2.4 $
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

package org.opencms.relations;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;

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
 * @author Thomas Weckert
 * @author Michael Moossen
 *   
 * @version $Revision: 1.1.2.4 $ 
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
     * Validates the relations.<p>
     * 
     * The result is printed to the given report.<p>
     * 
     * If the resources list is <code>null</code> or empty, the whole current project will be validated.<p>
     * 
     * If the resources list is not empty, Validating references means to answer the question, whether 
     * we would have broken links in the online project if a file or a list of files would get published.<p>
     * 
     * @param dbc the database context
     * @param resources a list of offline resources, or <code>null</code>
     * @param report a report to print messages
     * 
     * @return a map with lists of invalid links 
     *          (<code>{@link org.opencms.relations.CmsRelation}}</code> objects) 
     *          keyed by resource names
     */
    public Map validateResources(CmsDbContext dbc, List resources, I_CmsReport report) {

        Map invalidResources = new HashMap();
        boolean interProject = (resources != null);
        if (report != null) {
            report.println(
                Messages.get().container(Messages.RPT_HTMLLINK_VALIDATOR_BEGIN_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
        if (resources == null) {
            resources = new ArrayList();
            CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION;
            Iterator itTypes = OpenCms.getResourceManager().getResourceTypes().iterator();
            while (itTypes.hasNext()) {
                I_CmsResourceType type = (I_CmsResourceType)itTypes.next();
                if (type instanceof I_CmsLinkParseable) {
                    filter = filter.addRequireType(type.getTypeId());
                    try {
                        resources.addAll(m_driverManager.readResources(dbc, m_driverManager.readResource(
                            dbc,
                            "/",
                            filter), filter, true));
                    } catch (CmsException e) {
                        LOG.error(
                            Messages.get().getBundle().key(Messages.LOG_RETRIEVAL_RESOURCES_1, type.getTypeName()),
                            e);
                    }
                }
            }
        }

        // populate a lookup map with the project resources that 
        // actually get published keyed by their resource names.
        // second, resources that don't get validated are ignored.
        Map offlineFilesLookup = new HashMap();
        List validatableResources = new ArrayList();
        Iterator itResources = resources.iterator();
        while (itResources.hasNext()) {
            CmsResource resource = (CmsResource)itResources.next();
            offlineFilesLookup.put(resource.getRootPath(), resource);
            try {
                I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
                if (resourceType instanceof I_CmsLinkParseable) {
                    // don't validate links on deleted resources
                    validatableResources.add(resource);
                }
            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_RETRIEVAL_RESOURCETYPE_1, resource.getRootPath()),
                    e);
            }
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
        for (int index = 0, size = validatableResources.size(); index < size; index++) {
            CmsResource resource = (CmsResource)validatableResources.get(index);
            String resourceName = resource.getRootPath();

            if (report != null) {
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    new Integer(index + 1),
                    new Integer(size)), I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_HTMLLINK_VALIDATING_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(resourceName)));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
            }
            List brokenLinks = validateLinks(dbc, resource, offlineFilesLookup, project, report);
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
            report.println(Messages.get().container(Messages.RPT_HTMLLINK_VALIDATOR_END_0), I_CmsReport.FORMAT_HEADLINE);
        }
        return invalidResources;
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
    protected List validateLinks(
        CmsDbContext dbc,
        CmsResource resource,
        Map fileLookup,
        CmsProject project,
        I_CmsReport report) {

        List brokenRelations = new ArrayList();
        Map validatedLinks = new HashMap();

        // get the relations
        List relations = null;
        try {
            if (!resource.getState().isDeleted()) {
                // search the target of links in the current (offline) project
                relations = m_driverManager.getRelationsForResource(dbc, resource, CmsRelationFilter.TARGETS);
            } else {
                // search the source of links in the online project
                CmsProject currentProject = dbc.currentProject();
                dbc.getRequestContext().setCurrentProject(project);
                try {
                    relations = m_driverManager.getRelationsForResource(dbc, resource, CmsRelationFilter.SOURCES);
                } finally {
                    dbc.getRequestContext().setCurrentProject(currentProject);
                }
            }
        } catch (CmsException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_LINK_SEARCH_1, resource), e);
            if (report != null) {
                report.println(Messages.get().container(
                    Messages.LOG_LINK_SEARCH_1,
                    dbc.removeSiteRoot(resource.getRootPath())), I_CmsReport.FORMAT_ERROR);
            }
            return brokenRelations;
        }

        // check the relations
        boolean first = true;
        Iterator itRelations = relations.iterator();
        while (itRelations.hasNext()) {
            CmsRelation relation = (CmsRelation)itRelations.next();
            String link;
            if (!resource.getState().isDeleted()) {
                link = relation.getTargetPath();
            } else {
                link = relation.getSourcePath();
            }
            boolean isValidLink = true;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(link)) {
                // skip empty links
                continue;
            }
            if (validatedLinks.keySet().contains(link)) {
                // skip already validated links
                if (((Boolean)validatedLinks.get(link)).booleanValue()) {
                    // add broken relation of different type
                    brokenRelations.add(relation);
                }
                continue;
            }
            // the link is valid...
            try {
                // ... if the linked resource exists in the online project
                if (!resource.getState().isDeleted()) {
                    // search the target of link in the online project
                    try {
                        link = m_driverManager.getVfsDriver().readResource(
                            new CmsDbContext(),
                            project.getUuid(),
                            relation.getTargetId(),
                            true).getRootPath();
                    } catch (CmsVfsResourceNotFoundException e) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().getBundle().key(
                                Messages.LOG_LINK_VALIDATION_READBYID_FAILED_2,
                                relation.getTargetId().toString(),
                                project.getName()), e);
                        }
                        m_driverManager.getVfsDriver().readResource(
                            new CmsDbContext(),
                            project.getUuid(),
                            relation.getTargetPath(),
                            true);
                    }
                } else {
                    // since we are going to delete the resource the link is always not valid
                    isValidLink = false;
                }
            } catch (CmsException e) {
                // ... or if the linked resource is a resource that gets actually published
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_LINK_VALIDATION_READBYPATH_FAILED_2,
                        relation.getTargetPath(),
                        project.getName()), e);
                }
                if (!fileLookup.containsKey(link)) {
                    isValidLink = false;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(
                            Messages.LOG_LINK_VALIDATION_RESOURCENOTINLOOKUP_1,
                            link));
                    }
                }
            } finally {
                // ... and if the linked resource to be published get deleted
                if (fileLookup.containsKey(link)) {
                    CmsResource offlineResource = (CmsResource)fileLookup.get(link);
                    if (offlineResource.getState().isDeleted()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().getBundle().key(
                                Messages.LOG_LINK_VALIDATION_RESOURCEDELETED_1,
                                link));
                        }
                        isValidLink = false;
                    }
                }
            }
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
                        report.println(Messages.get().container(
                            Messages.RPT_HTMLLINK_BROKEN_TARGET_1,
                            dbc.removeSiteRoot(link)), I_CmsReport.FORMAT_WARNING);
                    } else {
                        report.println(Messages.get().container(
                            Messages.RPT_HTMLLINK_BROKEN_SOURCE_1,
                            dbc.removeSiteRoot(link)), I_CmsReport.FORMAT_WARNING);
                    }
                }
            }
            validatedLinks.put(link, Boolean.valueOf(!isValidLink));
        }
        return brokenRelations;
    }
}
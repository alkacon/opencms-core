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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Util class to find broken links in a bundle of resources to be deleted.<p>
 *
 * @since 6.5.3
 */
public class CmsRelationDeleteValidator {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRelationDeleteValidator.class);

    /** The internal computed broken relations map. */
    protected Map<String, List<CmsRelation>> m_brokenRelations;

    /** the cms context object. */
    private CmsObject m_cms;

    /**
     * Creates a new helper object.<p>
     *
     * @param cms the cms object
     * @param resourceNames a list of resource names to be deleted
     * @param includeSiblings if the siblings should also be deleted
     */
    public CmsRelationDeleteValidator(CmsObject cms, List<String> resourceNames, boolean includeSiblings) {

        m_cms = cms;
        m_brokenRelations = getBrokenRelations(resourceNames, includeSiblings);
    }

    /**
     * Returns the information bean for the given entry.<p>
     *
     * @param resourceName the entry name
     *
     * @return the information bean for the given entry
     */
    public CmsRelationValidatorInfoEntry getInfoEntry(String resourceName) {

        String resName = resourceName;
        String siteRoot = m_cms.getRequestContext().getSiteRoot();
        String siteName = null;
        if (resName.startsWith(m_cms.getRequestContext().getSiteRoot())) {
            resName = m_cms.getRequestContext().removeSiteRoot(resName);
        } else {
            siteRoot = OpenCms.getSiteManager().getSiteRoot(resName);
            siteName = siteRoot;
            if (siteRoot != null) {
                String oldSite = m_cms.getRequestContext().getSiteRoot();
                try {
                    m_cms.getRequestContext().setSiteRoot("/");
                    siteName = m_cms.readPropertyObject(siteRoot, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                        siteRoot);
                } catch (CmsException e) {
                    siteName = siteRoot;
                } finally {
                    m_cms.getRequestContext().setSiteRoot(oldSite);
                }
                resName = resName.substring(siteRoot.length());
            } else {
                siteName = "/";
            }
        }
        return new CmsRelationValidatorInfoEntry(
            resourceName,
            resName,
            siteName,
            siteRoot,
            Collections.unmodifiableList(m_brokenRelations.get(resourceName)));
    }

    /**
     * If no relation would be broken deleting the given resources.<p>
     *
     * @return <code>true</code> if no relation would be broken deleting the given resources
     */
    public boolean isEmpty() {

        return m_brokenRelations.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     *
     * @return the broken relations key set
     */
    public Set<String> keySet() {

        return m_brokenRelations.keySet();
    }

    /**
     * @see java.util.Map#values()
     *
     * @return the broken relations value set
     */
    public Collection<List<CmsRelation>> values() {

        return m_brokenRelations.values();
    }

    /**
     * Returns a map of where each entry has as key a name of a resource to be deleted,
     * and value a list of relations that would be broken.<p>
     *
     * The keys for non-siblings have following format:
     * <code>file root path</code>.<p>
     *
     * The keys for siblings have following format:
     * <code>original file root path + PREFIX_SIBLING + sibling root path</code>.<p>
     *
     * The values are {@link CmsRelation} objects.<p>
     *
     * @param resourceNames a list of resource names to be deleted
     * @param includeSiblings if the siblings should also be deleted
     *
     * @return a map of broken relations
     */
    private Map<String, List<CmsRelation>> getBrokenRelations(List<String> resourceNames, boolean includeSiblings) {

        Map<String, List<CmsRelation>> brokenRelations = new HashMap<String, List<CmsRelation>>();
        Set<String> resources = new HashSet<String>();
        // expand the folders to single resources
        String site = m_cms.getRequestContext().getSiteRoot();
        String oldSite = site;
        try {
            m_cms.getRequestContext().setSiteRoot("/");
            List<CmsResource> resourceList = new ArrayList<CmsResource>();
            Iterator<String> itResourceNames = resourceNames.iterator();
            while (itResourceNames.hasNext()) {
                // get the root path
                String resName = m_cms.getRequestContext().addSiteRoot(site, itResourceNames.next());
                try {
                    CmsResource resource = m_cms.readResource(resName);
                    resourceList.add(resource);
                    if (resource.isFolder()) {
                        resourceList.addAll(m_cms.readResources(resName, CmsResourceFilter.IGNORE_EXPIRATION, true));
                    }
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            }

            // collect the root paths
            Iterator<CmsResource> itResources = resourceList.iterator();
            while (itResources.hasNext()) {
                CmsResource resource = itResources.next();
                resources.add(resource.getRootPath());
            }

            if (Boolean.valueOf(includeSiblings).booleanValue()) {
                // expand the siblings
                itResources = new ArrayList<CmsResource>(resourceList).iterator();
                while (itResources.hasNext()) {
                    CmsResource resource = itResources.next();
                    try {
                        if (!resource.isFolder() && (resource.getSiblingCount() > 1)) {
                            Iterator<CmsResource> itSiblings = m_cms.readSiblings(
                                resource.getRootPath(),
                                CmsResourceFilter.IGNORE_EXPIRATION).iterator();
                            while (itSiblings.hasNext()) {
                                CmsResource sibling = itSiblings.next();
                                if (!resources.contains(sibling.getRootPath())) {
                                    resources.add(sibling.getRootPath());
                                    resourceList.add(sibling);
                                }
                            }
                        }
                    } catch (CmsException e) {
                        // should never happen
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
            }

            // check every resource
            itResources = resourceList.iterator();
            while (itResources.hasNext()) {
                CmsResource resource = itResources.next();
                String resourceName = resource.getRootPath();
                try {
                    Iterator<CmsRelation> it = m_cms.getRelationsForResource(
                        resource,
                        CmsRelationFilter.SOURCES).iterator();
                    while (it.hasNext()) {
                        CmsRelation relation = it.next();
                        String relationName = relation.getSourcePath();
                        // add only if the source is not to be deleted too
                        if (!resources.contains(relationName)) {
                            List<CmsRelation> broken = brokenRelations.get(resourceName);
                            if (broken == null) {
                                broken = new ArrayList<CmsRelation>();
                                brokenRelations.put(resourceName, broken);
                            }
                            broken.add(relation);
                        }
                    }
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        } finally {
            m_cms.getRequestContext().setSiteRoot(oldSite);
        }
        return brokenRelations;
    }
}

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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Util class to find broken links in a bundle of resources.<p>
 *
 * @since 6.5.3
 */
public class CmsInternalLinksValidator {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsInternalLinksValidator.class);

    /** The internal computed broken relations map. */
    protected Map<String, List<CmsRelation>> m_brokenRelations;

    /** The cms context object. */
    private CmsObject m_cms;

    /** The number of not visible resources with broken links. */
    private int m_notVisibleResourcesCount;

    /** All resources with broken links. */
    private List<CmsResource> m_resourcesWithBrokenLinks;

    /**
     * Creates a new helper object.<p>
     *
     * @param cms the cms object
     * @param resourceNames a list of resource names to be deleted
     */
    public CmsInternalLinksValidator(CmsObject cms, List<String> resourceNames) {

        m_cms = cms;
        m_brokenRelations = getBrokenRelations(resourceNames);
    }

    /**
     * Returns all broken links for the given resource.<p>
     *
     * @param resourceName the resource to get the broken link
     *
     * @return a list of {@link CmsRelation} objects
     */
    public List<CmsRelation> getBrokenLinksForResource(String resourceName) {

        return m_brokenRelations.get(resourceName);
    }

    /**
     * Returns the number of not visible resources with broken links.<p>
     *
     * @return the number of not visible resources with broken links
     */
    public int getNotVisibleResourcesCount() {

        if (m_resourcesWithBrokenLinks == null) {
            // compute it if needed
            getResourcesWithBrokenLinks();
        }
        return m_notVisibleResourcesCount;
    }

    /**
     * Returns all resources with broken links.<p>
     *
     * @return a list of {@link org.opencms.file.CmsResource} objects
     */
    public List<CmsResource> getResourcesWithBrokenLinks() {

        if (m_resourcesWithBrokenLinks == null) {
            // sort the resulting hash map
            List<String> resources = new ArrayList<String>(m_brokenRelations.keySet());
            Collections.sort(resources);

            m_resourcesWithBrokenLinks = new ArrayList<CmsResource>(resources.size());
            m_notVisibleResourcesCount = 0;
            // remove not visible resources
            CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireVisible();
            String storedSiteRoot = m_cms.getRequestContext().getSiteRoot();
            try {
                m_cms.getRequestContext().setSiteRoot("/");
                Iterator<String> itResources = resources.iterator();
                while (itResources.hasNext()) {
                    String resourceName = itResources.next();
                    try {
                        m_resourcesWithBrokenLinks.add(m_cms.readResource(resourceName, filter));
                    } catch (Exception e) {
                        // resource is not visible, increase count
                        m_notVisibleResourcesCount++;
                    }
                }
            } finally {
                m_cms.getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
        return m_resourcesWithBrokenLinks;
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
     * Returns a map of where each entry has as key a name of a resource to be validated,
     * and value a list of relations that are broken.<p>
     *
     * @param resourceNames a list of resource names to be validated
     *
     * @return a map of broken relations
     */
    private Map<String, List<CmsRelation>> getBrokenRelations(List<String> resourceNames) {

        Map<String, List<CmsRelation>> brokenRelations = new HashMap<String, List<CmsRelation>>();

        CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterIncludeChildren().filterStructureId(
            CmsUUID.getNullUUID());

        Iterator<String> itFolders = resourceNames.iterator();
        while (itFolders.hasNext()) {
            String folderName = itFolders.next();
            List<CmsRelation> relations;
            try {
                relations = m_cms.getRelationsForResource(folderName, filter);
            } catch (CmsException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_LINK_SEARCH_1, folderName), e);
                continue;
            }
            Iterator<CmsRelation> itRelations = relations.iterator();
            while (itRelations.hasNext()) {
                CmsRelation relation = itRelations.next();
                // target is broken
                String resourceName = relation.getSourcePath();
                List<CmsRelation> broken = brokenRelations.get(resourceName);
                if (broken == null) {
                    broken = new ArrayList<CmsRelation>();
                    brokenRelations.put(resourceName, broken);
                }
                broken.add(relation);
            }
        }
        return brokenRelations;
    }
}
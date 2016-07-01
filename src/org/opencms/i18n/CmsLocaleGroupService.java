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

package org.opencms.i18n;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Helper class for manipulating locale groups.<p>
 */
public class CmsLocaleGroupService {

    /** CMS context to use for VFS operations. */
    private CmsObject m_cms;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsLocaleGroupService(CmsObject cms) {
        m_cms = cms;
    }

    /**
     * Adds a resource to a locale group.<p>
     *
     * @param secondaryPage the page to add
     * @param primaryPage the primary resource of the locale group which the resource should be added to
     * @throws CmsException if something goes wrong
     */
    public void attachLocaleGroup(CmsResource secondaryPage, CmsResource primaryPage) throws CmsException {

        if (secondaryPage.getStructureId().equals(primaryPage.getStructureId())) {
            throw new IllegalArgumentException(
                "A page can not be linked with itself as a locale variant: " + secondaryPage.getRootPath());
        }
        CmsLocaleGroup group = readLocaleGroup(secondaryPage);
        if (group.isRealGroup()) {
            throw new IllegalArgumentException(
                "The page " + secondaryPage.getRootPath() + " is already part of a group. ");
        }

        // TODO: Check for redundant locales

        CmsLocaleGroup targetGroup = readLocaleGroup(primaryPage);
        CmsLockActionRecord record = CmsLockUtil.ensureLock(m_cms, secondaryPage);
        try {
            m_cms.deleteRelationsFromResource(
                secondaryPage,
                CmsRelationFilter.ALL.filterType(CmsRelationType.LOCALE_VARIANT));
            m_cms.addRelationToResource(
                secondaryPage,
                targetGroup.getPrimaryResource(),
                CmsRelationType.LOCALE_VARIANT.getName());
        } finally {
            if (record.getChange() == LockChange.locked) {
                m_cms.unlockResource(secondaryPage);
            }
        }
    }

    /**
     * Removes a locale group relation between two resources.<p>
     *
     * @param firstPage the first resource
     * @param secondPage the second resource
     * @throws CmsException if something goes wrong
     */
    public void detachLocaleGroup(CmsResource firstPage, CmsResource secondPage) throws CmsException {

        CmsRelationFilter typeFilter = CmsRelationFilter.ALL.filterType(CmsRelationType.LOCALE_VARIANT);
        List<CmsRelation> relations = m_cms.readRelations(typeFilter.filterStructureId(secondPage.getStructureId()));
        CmsUUID firstId = firstPage.getStructureId();
        CmsUUID secondId = secondPage.getStructureId();
        for (CmsRelation relation : relations) {
            CmsUUID sourceId = relation.getSourceId();
            CmsUUID targetId = relation.getTargetId();
            CmsResource resourceToModify = null;
            if (sourceId.equals(firstId) && targetId.equals(secondId)) {
                resourceToModify = firstPage;
            } else if (sourceId.equals(secondId) && targetId.equals(firstId)) {
                resourceToModify = secondPage;
            }
            if (resourceToModify != null) {
                CmsLockActionRecord record = CmsLockUtil.ensureLock(m_cms, resourceToModify);
                try {
                    m_cms.deleteRelationsFromResource(resourceToModify, typeFilter);
                } finally {
                    if (record.getChange() == LockChange.locked) {
                        m_cms.unlockResource(resourceToModify);
                    }
                }
                break;
            }
        }
    }

    /**
     * Reads a locale group from the VFS.<p>
     *
     * @param resource the resource for which to read the locale group
     *
     * @return the locale group for the resource
     * @throws CmsException if something goes wrong
     */
    public CmsLocaleGroup readLocaleGroup(CmsResource resource) throws CmsException {

        List<CmsRelation> relations = m_cms.readRelations(
            CmsRelationFilter.ALL.filterType(CmsRelationType.LOCALE_VARIANT).filterStructureId(
                resource.getStructureId()));
        List<CmsRelation> out = Lists.newArrayList();
        List<CmsRelation> in = Lists.newArrayList();
        for (CmsRelation rel : relations) {
            if (rel.getSourceId().equals(resource.getStructureId())) {
                out.add(rel);
            } else {
                in.add(rel);
            }
        }
        CmsResource primaryResource = null;
        List<CmsResource> secondaryResources = Lists.newArrayList();
        if ((out.size() == 0) && (in.size() == 0)) {
            primaryResource = resource;
        } else if ((out.size() == 0) && (in.size() > 0)) {
            primaryResource = resource;
            // resource is the primary variant
            for (CmsRelation relation : in) {
                CmsResource source = relation.getSource(m_cms, CmsResourceFilter.ALL);
                secondaryResources.add(source);
            }
        } else if ((out.size() == 1) && (in.size() == 0)) {

            CmsResource target = out.get(0).getTarget(m_cms, CmsResourceFilter.ALL);
            primaryResource = target;
            CmsRelationFilter filter = CmsRelationFilter.relationsToStructureId(target.getStructureId());
            List<CmsRelation> relationsToTarget = m_cms.readRelations(filter);
            for (CmsRelation targetRelation : relationsToTarget) {
                CmsResource secondaryResource = targetRelation.getSource(m_cms, CmsResourceFilter.ALL);
                secondaryResources.add(secondaryResource);
            }
        } else {
            throw new IllegalStateException(
                "illegal locale variant relations for resource with id="
                    + resource.getStructureId()
                    + ", path="
                    + resource.getRootPath());
        }
        return new CmsLocaleGroup(m_cms, primaryResource, secondaryResources);
    }

}

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

package org.opencms.workplace.commons;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Collector for resources with relations to a given resource.<p>
 *
 * @since 6.9.1
 */
public class CmsListResourceLinkRelationCollector extends A_CmsListResourceCollector {

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsListResourceLinkRelationCollector.class);

    /** Parameter of the default collector name. */
    private static final String COLLECTOR_NAME = "linkRelations";

    /** Indicates if the current request shows the source resources for the relations are shown. */
    private boolean m_isSource;

    /** The current resource to get link relations for. */
    private String m_resource;

    /**
     * Public constructor.<p>
     *
     * @param wp the current list explorer dialog
     * @param resource the current resource to get link relations for
     * @param isSource indicates if the current request shows the source resources for the relations are shown
     */
    public CmsListResourceLinkRelationCollector(A_CmsListExplorerDialog wp, String resource, boolean isSource) {

        super(wp);
        m_isSource = isSource;
        m_resource = resource;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        List<String> names = new ArrayList<String>();
        names.add(COLLECTOR_NAME);
        return names;
    }

    /**
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public String getResource() {

        return m_resource;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResource(org.opencms.file.CmsObject, org.opencms.workplace.list.CmsListItem)
     */
    @Override
    public CmsResource getResource(CmsObject cms, CmsListItem item) {

        String itemId;
        if (item.getId().startsWith(item.get(CmsResourceLinkRelationList.LIST_COLUMN_RELATION_TYPE) + "_")) {
            itemId = item.getId().substring(item.getId().lastIndexOf("_") + 1);
        } else {
            itemId = item.getId();
        }
        CmsResource res = m_resCache.get(itemId);
        if (res == null) {
            CmsUUID id = new CmsUUID(item.getId());
            if (!id.isNullUUID()) {
                try {
                    res = cms.readResource(id, CmsResourceFilter.ALL);
                    m_resCache.put(itemId, res);
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        return res;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResources(org.opencms.file.CmsObject, java.util.Map)
     */
    @Override
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params) {

        List<CmsResource> allResources = new ArrayList<CmsResource>();
        CmsRelationFilter filter = CmsRelationFilter.TARGETS;
        if (isSource()) {
            filter = CmsRelationFilter.SOURCES;
        }
        List<CmsRelation> relations = new ArrayList<CmsRelation>();
        try {
            relations = cms.getRelationsForResource(getResource(), filter);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(getWp().getLocale()), e);
            }
        }
        Map<CmsResource, List<CmsRelationType>> relationTypes = new HashMap<CmsResource, List<CmsRelationType>>();
        List<String> brokenLinks = new ArrayList<String>();
        Iterator<CmsRelation> itRelations = relations.iterator();
        while (itRelations.hasNext()) {
            CmsRelation relation = itRelations.next();
            CmsResource resource = null;
            try {
                if (isSource()) {
                    resource = relation.getSource(cms, CmsResourceFilter.ALL);
                } else {
                    resource = relation.getTarget(cms, CmsResourceFilter.ALL);
                }
            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getLocalizedMessage(getWp().getLocale()), e);
                }
                resource = new CmsResource(
                    new CmsUUID(),
                    new CmsUUID(),
                    relation.getTargetPath(),
                    CmsResourceTypePlain.getStaticTypeId(),
                    false,
                    0,
                    getWp().getJsp().getRequestContext().getCurrentProject().getUuid(),
                    CmsResourceState.STATE_DELETED,
                    0,
                    getWp().getJsp().getRequestContext().getCurrentUser().getId(),
                    0,
                    getWp().getJsp().getRequestContext().getCurrentUser().getId(),
                    0,
                    0,
                    0,
                    0,
                    0,
                    0);
                if (!brokenLinks.contains(resource)) {
                    brokenLinks.add(
                        relation.getType().getLocalizedName(getWp().getJsp().getRequestContext().getLocale())
                            + "_"
                            + resource.getStructureId());
                }
            }
            allResources.add(resource);
            if (relationTypes.containsKey(resource)) {
                relationTypes.get(resource).add(relation.getType());
            } else {
                List<CmsRelationType> types = new ArrayList<CmsRelationType>();
                types.add(relation.getType());
                relationTypes.put(resource, types);
            }
        }
        if (getWp() instanceof CmsResourceLinkRelationList) {
            CmsResourceLinkRelationList wp = (CmsResourceLinkRelationList)getWp();
            wp.setRelationTypes(relationTypes);
            wp.setBrokenLinks(brokenLinks);
        }
        return allResources;
    }

    /**
     * Returns the isSource.<p>
     *
     * @return the isSource
     */
    public boolean isSource() {

        return m_isSource;
    }

    /**
     * Sets the resource.<p>
     *
     * @param resource the resource to set
     */
    public void setResource(String resource) {

        m_resource = resource;
    }

    /**
     * Sets the isSource.<p>
     *
     * @param isSource the isSource to set
     */
    public void setSource(boolean isSource) {

        m_isSource = isSource;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
     */
    @Override
    protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

        // noop
    }
}

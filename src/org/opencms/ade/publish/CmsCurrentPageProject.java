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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsCollectorPublishListProvider;
import org.opencms.gwt.shared.I_CmsCollectorInfoFactory;
import org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Virtual project which includes the currently edited resource and all its related resources.
 */
public class CmsCurrentPageProject implements I_CmsVirtualProject {

    /** The uuid of this virtual project. */
    public static final CmsUUID ID = CmsUUID.getConstantUUID("currentpage");

    /** The logger for this class. */
    static final Log LOG = CmsLog.getLog(CmsCurrentPageProject.class);

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectBean(org.opencms.file.CmsObject, java.util.Map)
     */
    public CmsProjectBean getProjectBean(CmsObject cms, Map<String, String> params) {

        String pageId = params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
        String elementId = params.get(CmsPublishOptions.PARAM_CONTENT);
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        String title = Messages.get().getBundle(locale).key(Messages.GUI_CURRENTPAGE_PROJECT_0);
        CmsUUID structureIdForTitle;
        if ((pageId == null) && (elementId == null)) {
            return null;
        } else {
            structureIdForTitle = pageId != null ? new CmsUUID(pageId) : new CmsUUID(elementId);
        }

        CmsProjectBean bean = new CmsProjectBean(ID, 0, title, title);
        bean.setRank(100);
        bean.setDefaultGroupName("");
        try {
            CmsResource titleResource = cms.readResource(structureIdForTitle, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsProperty titleProp = cms.readPropertyObject(titleResource, CmsPropertyDefinition.PROPERTY_TITLE, true);
            String rawName;
            if (titleProp.isNullProperty()) {
                rawName = cms.getSitePath(titleResource);
            } else {
                rawName = titleProp.getValue();
            }
            bean.setDefaultGroupName(Messages.get().getBundle(locale).key(Messages.GUI_PAGE_1, rawName));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return bean;
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectId()
     */
    public CmsUUID getProjectId() {

        return ID;
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getRelatedResourceProvider(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public I_CmsPublishRelatedResourceProvider getRelatedResourceProvider(
        final CmsObject cmsObject,
        final CmsPublishOptions options) {

        return new I_CmsPublishRelatedResourceProvider() {

            public Set<CmsResource> getAdditionalRelatedResources(CmsObject cms, CmsResource res) {

                Map<String, String> params = options.getParameters();

                String pageId = options.getParameters().get(CmsPublishOptions.PARAM_CONTAINERPAGE);
                String detailId = options.getParameters().get(CmsPublishOptions.PARAM_DETAIL);

                Set<CmsResource> result = Sets.newHashSet();

                if (res.getStructureId().toString().equals(detailId)) {
                    result.addAll(CmsJspTagContainer.getDetailOnlyResources(cms, res));
                }
                if (res.getStructureId().toString().equals(pageId)) {

                    I_CmsCollectorInfoFactory collectorInfoFactory = AutoBeanFactorySource.create(
                        I_CmsCollectorInfoFactory.class);
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (entry.getKey().startsWith(CmsPublishOptions.PARAM_COLLECTOR_INFO)) {
                            try {
                                AutoBean<I_CmsContentLoadCollectorInfo> autoBean = AutoBeanCodex.decode(
                                    collectorInfoFactory,
                                    I_CmsContentLoadCollectorInfo.class,
                                    entry.getValue());
                                String collectorName = autoBean.as().getCollectorName();
                                I_CmsCollectorPublishListProvider publishListProvider = null;
                                if (null != collectorName) { // registered collector
                                    publishListProvider = OpenCms.getResourceManager().getContentCollector(
                                        collectorName);
                                }
                                if (null == publishListProvider) { // unregistered collector
                                    String collectorClassName = autoBean.as().getCollectorClass();
                                    Class<?> collectorClass = Class.forName(collectorClassName);
                                    publishListProvider = (I_CmsCollectorPublishListProvider)collectorClass.newInstance();
                                }
                                if (publishListProvider == null) {
                                    continue;
                                }
                                result.addAll(publishListProvider.getPublishResources(cmsObject, autoBean.as()));
                            } catch (Exception e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                    }

                    String collectorItemsStr = options.getParameters().get(CmsPublishOptions.PARAM_COLLECTOR_ITEMS);
                    if (collectorItemsStr != null) {
                        for (String token : collectorItemsStr.split(",")) {
                            try {
                                if (CmsUUID.isValidUUID(token)) {
                                    CmsResource collectorRes = cms.readResource(
                                        new CmsUUID(token),
                                        CmsResourceFilter.ALL);
                                    if (!collectorRes.getState().isUnchanged()) {
                                        result.add(collectorRes);
                                    }
                                }
                            } catch (Exception e) {
                                LOG.error(
                                    "Error processing collector item " + token + ": " + e.getLocalizedMessage(),
                                    e);
                            }
                        }
                    }
                }
                return result;

            }

        };
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getResources(org.opencms.file.CmsObject, java.util.Map, java.lang.String)
     */
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params, String workflowId) {

        String containerpageId = params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
        String elementId = params.get(CmsPublishOptions.PARAM_CONTENT);
        String detailId = params.get(CmsPublishOptions.PARAM_DETAIL);
        Set<CmsResource> resources = new HashSet<CmsResource>();
        for (String id : new String[] {containerpageId, elementId, detailId}) {
            if (CmsUUID.isValidUUID(id)) {
                try {
                    CmsResource resource = cms.readResource(new CmsUUID(id), CmsResourceFilter.ALL);
                    resources.add(resource);
                    CmsResource parent = cms.readParentFolder(resource.getStructureId());
                    if (!parent.getState().isUnchanged()) {
                        resources.add(parent);
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }

            }
        }
        return Lists.newArrayList(resources);
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#isAutoSelectable()
     */
    public boolean isAutoSelectable() {

        return true;
    }

}

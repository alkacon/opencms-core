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

package org.opencms.ade.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsElementView;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.configuration.CmsResourceTypeConfig.AddMenuVisibility;
import org.opencms.ade.galleries.CmsGalleryService;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean.Origin;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Helper class for preparing the resource type lists for gallery and new dialog.<p>
 *
*/
public class CmsAddDialogTypeHelper {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAddDialogTypeHelper.class);

    /**
     * Creates list of resource type beans for gallery or 'New' dialog
     *
     * @param cms the CMS context
     * @param folderRootPath the current folder
     * @param checkViewableReferenceUri the reference uri to use for viewability check
     * @param viewId the view id
     * @param checkEnabled object to check whether resource types should be enabled
     *
     * @return the list of resource type beans
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResourceTypeBean> getResourceTypes(
        CmsObject cms,
        String folderRootPath,
        String checkViewableReferenceUri,
        CmsUUID viewId,
        I_CmsResourceTypeEnabledCheck checkEnabled) throws CmsException {

        CmsElementView elementView = OpenCms.getADEManager().getElementViews(cms).get(viewId);
        if (elementView == null) {
            LOG.error("Element view is null");
            return Collections.emptyList();
        }
        List<I_CmsResourceType> additionalTypes = Lists.newArrayList();
        if (elementView.getExplorerType() != null) {
            CmsElementView elemView = OpenCms.getADEManager().getElementViews(cms).get(viewId);
            if (elemView != null) {
                List<CmsExplorerTypeSettings> explorerTypes = OpenCms.getWorkplaceManager().getExplorerTypesForView(
                    elemView.getExplorerType().getName());
                for (CmsExplorerTypeSettings explorerType : explorerTypes) {
                    additionalTypes.add(OpenCms.getResourceManager().getResourceType(explorerType.getName()));
                }
            }
        }
        return internalGetResourceTypesFromConfig(
            cms,
            folderRootPath,
            checkViewableReferenceUri,
            viewId,
            additionalTypes,
            checkEnabled);

    }

    /**
     * Creates list of resource type beans for gallery or 'New' dialog
     *
     * @param cms the CMS context
     * @param folderRootPath the current folder
     * @param checkViewableReferenceUri the reference uri to use for viewability check
     * @param elementView  the view id
     * @param additionalTypes the additional types to add
     * @param checkEnabled object to check whether resource types should be enabled
     *
     * @return the list of resource type beans
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsResourceTypeBean> internalGetResourceTypesFromConfig(
        CmsObject cms,
        String folderRootPath,
        String checkViewableReferenceUri,
        CmsUUID elementView,
        List<I_CmsResourceType> additionalTypes,
        I_CmsResourceTypeEnabledCheck checkEnabled) throws CmsException {

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, folderRootPath);
        // String uri = cms.getRequestContext().removeSiteRoot(rootFolder);
        List<I_CmsResourceType> resourceTypes = new ArrayList<I_CmsResourceType>();
        Set<String> disabledTypes = new HashSet<String>();
        final Set<String> typesAtTheEndOfTheList = Sets.newHashSet();
        Set<String> typesFromConfig = Sets.newHashSet();
        Map<String, String> createPaths = Maps.newHashMap();
        Map<String, String> namePatterns = Maps.newHashMap();
        for (CmsResourceTypeConfig typeConfig : config.getResourceTypes()) {
            typesFromConfig.add(typeConfig.getTypeName());
            boolean isModelGroup = CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME.equals(
                typeConfig.getTypeName());
            try {
                AddMenuVisibility visibility = typeConfig.getAddMenuVisibility(elementView);

                if (visibility == AddMenuVisibility.disabled) {
                    continue;
                }

                if (isModelGroup || (visibility == AddMenuVisibility.fromOtherView)) {
                    typesAtTheEndOfTheList.add(typeConfig.getTypeName());
                }
                if (typeConfig.checkViewable(cms, checkViewableReferenceUri)) {
                    String typeName = typeConfig.getTypeName();
                    I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(typeName);
                    resourceTypes.add(resType);
                    if ((checkEnabled != null) && !checkEnabled.checkEnabled(cms, config, resType)) {
                        disabledTypes.add(resType.getTypeName());
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        Set<String> creatableTypes = new HashSet<String>();
        for (CmsResourceTypeConfig typeConfig : config.getCreatableTypes(cms, folderRootPath)) {
            if (typeConfig.isHiddenFromAddMenu(elementView) || disabledTypes.contains(typeConfig.getTypeName())) {
                continue;
            }
            createPaths.put(typeConfig.getTypeName(), typeConfig.getFolderPath(cms, folderRootPath));
            namePatterns.put(typeConfig.getTypeName(), typeConfig.getNamePattern(false));
            String typeName = typeConfig.getTypeName();
            creatableTypes.add(typeName);
        }

        CmsGalleryService srv = new CmsGalleryService();
        srv.setCms(cms);
        // we put the types 'imported' from other views at the end of the list. Since the sort is stable,
        // relative position of other types remains unchanged
        Collections.sort(resourceTypes, new Comparator<I_CmsResourceType>() {

            public int compare(I_CmsResourceType first, I_CmsResourceType second) {

                return ComparisonChain.start().compare(rank(first), rank(second)).result();
            }

            int rank(I_CmsResourceType type) {

                return typesAtTheEndOfTheList.contains(type.getTypeName()) ? 1 : 0;
            }
        });

        for (I_CmsResourceType addType : additionalTypes) {
            String typeName = addType.getTypeName();
            if (typesFromConfig.contains(typeName)) { //  type was already processed (although it may not be in the result list)
                continue;
            }
            CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
            if (CmsStringUtil.isEmpty(explorerType.getNewResourceUri())) {
                continue;
            }
            CmsPermissionSet permissions = explorerType.getAccess().getPermissions(
                cms,
                cms.readResource(checkViewableReferenceUri));
            String permString = permissions.getPermissionString();
            if (permString.contains("+c") && permString.contains("+v")) {
                resourceTypes.add(addType);
                creatableTypes.add(addType.getTypeName());
            }
        }
        List<CmsResourceTypeBean> results = srv.buildTypesList(resourceTypes, creatableTypes, disabledTypes, null);
        for (CmsResourceTypeBean typeBean : results) {
            if (typesFromConfig.contains(typeBean.getType())) {
                typeBean.setOrigin(Origin.config);
                typeBean.setCreatePath(createPaths.get(typeBean.getType()));
                typeBean.setNamePattern(namePatterns.get(typeBean.getType()));
            } else {
                typeBean.setOrigin(Origin.other);
            }
        }
        return results;

    }

}

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
import org.opencms.security.CmsRole;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Helper class for preparing the resource type lists for gallery and new dialog.<p>
 *
*/
public class CmsAddDialogTypeHelper {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAddDialogTypeHelper.class);

    /** All types from the ADE config previously processed. */
    private Set<String> m_allAdeTypes = Sets.newHashSet();

    /** Cached type lists. */
    private Multimap<CmsUUID, CmsResourceTypeBean> m_cachedTypes;

    /** All types from the ADE config previously included in a result list. */
    private Set<String> m_includedAdeTypes = Sets.newHashSet();

    /**
     * Creates a new instance.<p>
     */
    public CmsAddDialogTypeHelper() {
        LOG.debug("Creating type helper.");
    }

    /**
     * Gets the precomputed type list for the given view.<p>
     *
     * @param view the element view
     * @return the precomputed type list, or null if the list wasn't precomputed
     */
    public List<CmsResourceTypeBean> getPrecomputedTypes(CmsElementView view) {

        return Lists.newArrayList(m_cachedTypes.get(view.getId()));
    }

    /**
     * Creates list of resource type beans for gallery or 'New' dialog.<p>
     *
     * @param cms the CMS context
     * @param folderRootPath the current folder
     * @param checkViewableReferenceUri the reference uri to use for viewability check
     * @param elementView the element view
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
        CmsElementView elementView,
        I_CmsResourceTypeEnabledCheck checkEnabled) throws CmsException {

        if (elementView == null) {
            LOG.error("Element view is null");
            return Collections.emptyList();
        }
        List<I_CmsResourceType> additionalTypes = Lists.newArrayList();

        // First store the types in a map, to avoid duplicates
        Map<String, I_CmsResourceType> additionalTypeMap = Maps.newHashMap();

        if (elementView.getExplorerType() != null) {
            List<CmsExplorerTypeSettings> explorerTypes = OpenCms.getWorkplaceManager().getExplorerTypesForView(
                elementView.getExplorerType().getName());
            for (CmsExplorerTypeSettings explorerType : explorerTypes) {
                if (elementView.isOther() && m_includedAdeTypes.contains(explorerType.getName())) {
                    continue;
                }
                additionalTypeMap.put(
                    explorerType.getName(),
                    OpenCms.getResourceManager().getResourceType(explorerType.getName()));
            }
        }
        if (OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER) && elementView.isOther()) {
            Set<String> hiddenTypes = new HashSet<String>(m_allAdeTypes);
            hiddenTypes.removeAll(m_includedAdeTypes);
            for (String typeName : hiddenTypes) {
                if (OpenCms.getResourceManager().hasResourceType(typeName)) {
                    additionalTypeMap.put(typeName, OpenCms.getResourceManager().getResourceType(typeName));
                }
            }
        }
        additionalTypes.addAll(additionalTypeMap.values());

        return internalGetResourceTypesFromConfig(
            cms,
            folderRootPath,
            checkViewableReferenceUri,
            elementView,
            additionalTypes,
            checkEnabled);

    }

    /**
     * Precomputes type lists for multiple views.<p>
     *
     * @param cms the CMS context
     * @param folderRootPath the current folder
     * @param checkViewableReferenceUri the reference uri to use for viewability check
     * @param views the views for which to generate the type lists
     * @param check object to check whether resource types should be enabled
     */
    public void precomputeTypeLists(
        CmsObject cms,
        String folderRootPath,
        String checkViewableReferenceUri,
        List<CmsElementView> views,
        I_CmsResourceTypeEnabledCheck check) {

        Multimap<CmsUUID, CmsResourceTypeBean> result = ArrayListMultimap.create();

        // Sort list to make sure that 'Other types' view is processed last, because we may need to display
        // types filtered / removed from other views, which we only know once we have processed these views
        Collections.sort(views, new Comparator<CmsElementView>() {

            public int compare(CmsElementView view0, CmsElementView view1) {

                return ComparisonChain.start().compareFalseFirst(view0.isOther(), view1.isOther()).result();
            }
        });

        for (CmsElementView view : views) {
            try {
                result.putAll(
                    view.getId(),
                    getResourceTypes(cms, folderRootPath, checkViewableReferenceUri, view, check));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        m_cachedTypes = result;
    }

    /**
     * Function used to check if a given resource type should be excluded from the result.<p>
     *
     * @param type  the type
     *
     * @return true if the given type should be excluded
     */
    protected boolean exclude(CmsResourceTypeBean type) {

        return false;
    }

    /**
     * Creates list of resource type beans for gallery or 'New' dialog.<p>
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
        CmsElementView elementView,
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
            m_allAdeTypes.add(typeConfig.getTypeName());
            typesFromConfig.add(typeConfig.getTypeName());
            boolean isModelGroup = CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME.equals(
                typeConfig.getTypeName());
            try {
                AddMenuVisibility visibility = typeConfig.getAddMenuVisibility(elementView.getId());

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
            if (typeConfig.isHiddenFromAddMenu(elementView.getId())
                || disabledTypes.contains(typeConfig.getTypeName())) {
                continue;
            }
            createPaths.put(typeConfig.getTypeName(), typeConfig.getFolderPath(cms, folderRootPath));
            namePatterns.put(typeConfig.getTypeName(), typeConfig.getNamePattern(false));
            String typeName = typeConfig.getTypeName();
            creatableTypes.add(typeName);
        }
        m_includedAdeTypes.addAll(createPaths.keySet());
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

        Collections.sort(additionalTypes, new Comparator<I_CmsResourceType>() {

            public int compare(I_CmsResourceType type1, I_CmsResourceType type2) {

                CmsExplorerTypeSettings settings1 = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    type1.getTypeName());
                CmsExplorerTypeSettings settings2 = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    type2.getTypeName());
                return ComparisonChain.start().compare(
                    settings1.getViewOrder(true),
                    settings2.getViewOrder(true)).compare(
                        parse(settings1.getNewResourceOrder()),
                        parse(settings2.getNewResourceOrder())).result();

            }

            long parse(String order) {

                try {
                    return Integer.parseInt(order);
                } catch (NumberFormatException e) {
                    return 9999;
                }
            }
        });
        for (I_CmsResourceType addType : additionalTypes) {
            String typeName = addType.getTypeName();
            if (typesFromConfig.contains(typeName) && !elementView.isOther()) { //  type was already processed (although it may not be in the result list)
                continue;
            }
            CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
            CmsPermissionSet permissions = explorerType.getAccess().getPermissions(
                cms,
                cms.readResource(checkViewableReferenceUri));
            if (permissions.requiresControlPermission() && permissions.requiresViewPermission()) {
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

        List<CmsResourceTypeBean> filteredResults = Lists.newArrayList();
        for (CmsResourceTypeBean result : results) {
            if (exclude(result)) {
                continue;
            }
            filteredResults.add(result);
        }

        return filteredResults;

    }

}

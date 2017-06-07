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

package org.opencms.workflow;

import org.opencms.ade.publish.CmsPublishRelationFinder.ResourceMap;
import org.opencms.ade.publish.Messages;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsPublishResourceInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsPermissionInfo;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Default formatter class for publish  resources.<p>
 */
public class CmsDefaultPublishResourceFormatter implements I_CmsPublishResourceFormatter {

    /**
     * Excludes resources which have already been published.<p>
     */
    public class AlreadyPublishedValidator implements I_PublishResourceValidator {

        /**
         * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter.I_PublishResourceValidator#findInvalidResources(java.util.Set)
         */
        public Set<CmsResource> findInvalidResources(Set<CmsResource> resources) {

            Set<CmsResource> result = new HashSet<CmsResource>();
            for (CmsResource resource : resources) {
                if (resource.getState().isUnchanged()) {
                    result.add(resource);
                }
            }
            return result;
        }

        /**
         * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter.I_PublishResourceValidator#getInfoForResource(org.opencms.file.CmsResource)
         */
        public CmsPublishResourceInfo getInfoForResource(CmsResource resource) throws CmsException {

            String info;
            CmsPublishResourceInfo.Type infoType;
            CmsPublishResourceInfo infoObj;
            String publishUser = getOuAwareName(m_cms, m_cms.readUser(resource.getUserLastModified()).getName());
            Date publishDate = new Date(resource.getDateLastModified());
            info = Messages.get().getBundle(getLocale()).key(
                Messages.GUI_RESOURCE_PUBLISHED_BY_2,
                publishUser,
                publishDate);
            infoType = CmsPublishResourceInfo.Type.PUBLISHED;
            infoObj = new CmsPublishResourceInfo(info, infoType);
            return infoObj;
        }
    }

    /**
     * Validator which checks if resources are locked by someone else.<p>
     */
    public class BlockingLockedValidator implements I_PublishResourceValidator {

        /**
         * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter.I_PublishResourceValidator#findInvalidResources(java.util.Set)
         */
        @SuppressWarnings("synthetic-access")
        public Set<CmsResource> findInvalidResources(Set<CmsResource> resources) {

            CmsUser user = m_cms.getRequestContext().getCurrentUser();
            CmsLockFilter blockingFilter = CmsLockFilter.FILTER_ALL;
            blockingFilter = blockingFilter.filterNotLockableByUser(user);
            Set<CmsResource> result = new HashSet<CmsResource>();

            for (CmsResource resource : resources) {
                try {
                    List<CmsResource> blockingLocked = m_cms.getLockedResourcesWithCache(
                        resource,
                        blockingFilter,
                        m_lockedResourceCache);
                    for (CmsResource res : blockingLocked) {
                        result.add(res);
                    }
                } catch (Exception e) {
                    // error reading the resource list, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
            return result;
        }

        /**
         * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter.I_PublishResourceValidator#getInfoForResource(org.opencms.file.CmsResource)
         */
        public CmsPublishResourceInfo getInfoForResource(CmsResource resource) throws CmsException {

            String info;
            CmsPublishResourceInfo.Type infoType;
            CmsPublishResourceInfo infoObj;
            CmsLock lock = m_cms.getLock(resource);
            info = Messages.get().getBundle(getLocale()).key(
                Messages.GUI_RESOURCE_LOCKED_BY_2,
                getOuAwareName(m_cms, m_cms.readUser(lock.getUserId()).getName()),
                getOuAwareName(m_cms, lock.getProject().getName()));
            infoType = CmsPublishResourceInfo.Type.LOCKED;
            infoObj = new CmsPublishResourceInfo(info, infoType);
            return infoObj;
        }
    }

    /**
     * Compares publish resources by their sort date.<p>
     */
    public static class DefaultComparator implements Comparator<CmsPublishResource> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsPublishResource first, CmsPublishResource second) {

            return ComparisonChain.start().compare(-first.getSortDate(), -second.getSortDate()).result();
        }
    }

    /**
     * Validator which can exclude some resources from publishing and supplies a status object for the excluded resources.<p>
     */
    public static interface I_PublishResourceValidator {

        /**
         * Finds the resources which should be excluded.<p>
         *
         * @param input the set of input resources
         *
         * @return the excluded resources
         */
        Set<CmsResource> findInvalidResources(Set<CmsResource> input);

        /**
         * Gets the status information for an excluded resource.<p>
         *
         * @param resource the resource for which to get the status
         * @return the status for the resource
         * @throws CmsException if something goes wrong
         */
        CmsPublishResourceInfo getInfoForResource(CmsResource resource) throws CmsException;
    }

    /**
     * Validator which excludes resources for which the user has no publish permissions.<p>
     */
    public class NoPermissionsValidator implements I_PublishResourceValidator {

        /**
         * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter.I_PublishResourceValidator#findInvalidResources(java.util.Set)
         */
        @SuppressWarnings("synthetic-access")
        public Set<CmsResource> findInvalidResources(Set<CmsResource> resources) {

            Set<CmsResource> result = new HashSet<CmsResource>();
            Set<CmsUUID> projectIds = new HashSet<CmsUUID>();
            try {
                for (CmsProject project : OpenCms.getOrgUnitManager().getAllManageableProjects(m_cms, "", true)) {
                    projectIds.add(project.getUuid());
                }
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
            for (CmsResource resource : resources) {
                try {
                    if (!projectIds.contains(resource.getProjectLastModified())
                        && !m_cms.hasPermissions(resource, CmsPermissionSet.ACCESS_DIRECT_PUBLISH)) {
                        result.add(resource);
                    }
                } catch (Exception e) {
                    // error reading the permissions, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
            return result;
        }

        /**
         * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter.I_PublishResourceValidator#getInfoForResource(org.opencms.file.CmsResource)
         */
        public CmsPublishResourceInfo getInfoForResource(CmsResource resource) {

            String info;
            CmsPublishResourceInfo.Type infoType;
            CmsPublishResourceInfo infoObj;
            info = Messages.get().getBundle(getLocale()).key(Messages.GUI_RESOURCE_NOT_ENOUGH_PERMISSIONS_0);
            infoType = CmsPublishResourceInfo.Type.PERMISSIONS;
            infoObj = new CmsPublishResourceInfo(info, infoType);
            return infoObj;
        }
    }

    /**
     * Predicate which checks whether the current user has publish permissions for a resource.<p>
     */
    public class PublishPermissionFilter implements Predicate<CmsResource> {

        /**
         * @see com.google.common.base.Predicate#apply(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public boolean apply(CmsResource input) {

            try {
                return m_cms.hasPermissions(
                    input,
                    CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                    false,
                    CmsResourceFilter.ALL);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return true;
            }
        }

    }

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultPublishResourceFormatter.class);

    /** The publish options. */
    protected CmsPublishOptions m_options;

    /** The CMS context for this class. */
    CmsObject m_cms;

    /** Cache for locked resources. */
    private Map<String, CmsResource> m_lockedResourceCache = new HashMap<String, CmsResource>();

    /** The publish resources. */
    private List<CmsPublishResource> m_publishResources;

    /** The publish resources by id. */
    private Map<CmsUUID, CmsResource> m_resources = new HashMap<CmsUUID, CmsResource>();

    /**
     * Constructor.<p>
     *
     *
     * @param cms the CMS context to use
     */
    public CmsDefaultPublishResourceFormatter(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Returns the simple name if the ou is the same as the current user's ou.<p>
     *
     * @param cms the CMS context
     * @param name the fully qualified name to check
     *
     * @return the simple name if the ou is the same as the current user's ou
     */
    public static String getOuAwareName(CmsObject cms, String name) {

        String ou = CmsOrganizationalUnit.getParentFqn(name);
        if (ou.equals(cms.getRequestContext().getCurrentUser().getOuFqn())) {
            return CmsOrganizationalUnit.getSimpleName(name);
        }
        return CmsOrganizationalUnit.SEPARATOR + name;
    }

    /**
     * @see org.opencms.workflow.I_CmsPublishResourceFormatter#getPublishResources()
     */
    public List<CmsPublishResource> getPublishResources() {

        sortResult(m_publishResources);
        return m_publishResources;
    }

    /**
     * @see org.opencms.workflow.I_CmsPublishResourceFormatter#initialize(org.opencms.ade.publish.shared.CmsPublishOptions, org.opencms.ade.publish.CmsPublishRelationFinder.ResourceMap)
     */
    public void initialize(CmsPublishOptions options, ResourceMap resources) throws CmsException {

        m_options = options;
        Predicate<CmsResource> resourceMapFilter = getResourceMapFilter();
        if (resourceMapFilter != null) {
            resources = resources.filter(resourceMapFilter);
        }
        for (CmsResource parentRes : resources.keySet()) {
            m_resources.put(parentRes.getStructureId(), parentRes);
            for (CmsResource childRes : resources.get(parentRes)) {
                m_resources.put(childRes.getStructureId(), childRes);
            }
        }
        Map<CmsUUID, CmsPublishResourceInfo> warnings = computeWarnings();
        m_publishResources = Lists.newArrayList();
        for (CmsResource parentRes : resources.keySet()) {
            CmsPublishResource parentPubRes = createPublishResource(parentRes);
            parentPubRes.setInfo(warnings.get(parentRes.getStructureId()));
            for (CmsResource childRes : resources.get(parentRes)) {
                CmsPublishResource childPubRes = createPublishResource(childRes);
                childPubRes.setInfo(warnings.get(childRes.getStructureId()));
                parentPubRes.getRelated().add(childPubRes);
            }
            if ((m_options.getProjectId() == null) || m_options.getProjectId().isNullUUID()) {
                parentPubRes.setRemovable(true);
            }
            m_publishResources.add(parentPubRes);
        }
    }

    /**
     * Creates the publish resource warnings.<p>
     *
     * @return a map from structure ids to the warnings for the corresponding resources
     */
    protected Map<CmsUUID, CmsPublishResourceInfo> computeWarnings() {

        Map<CmsUUID, CmsPublishResourceInfo> warnings = Maps.newHashMap();
        Set<CmsResource> resourcesWithoutErrors = new HashSet<CmsResource>(m_resources.values());

        List<I_PublishResourceValidator> validators = getValidators();
        List<Set<CmsResource>> excludedSetsForValidators = Lists.newArrayList();
        for (int i = 0; i < validators.size(); i++) {
            I_PublishResourceValidator validator = validators.get(i);
            Set<CmsResource> excluded = validator.findInvalidResources(resourcesWithoutErrors);
            resourcesWithoutErrors.removeAll(excluded);
            excludedSetsForValidators.add(excluded);
        }
        for (CmsResource resource : m_resources.values()) {
            CmsPublishResourceInfo info = null;
            try {
                for (int i = 0; i < validators.size(); i++) {
                    if (excludedSetsForValidators.get(i).contains(resource)) {
                        info = validators.get(i).getInfoForResource(resource);
                        break;
                    }
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            warnings.put(resource.getStructureId(), info);
        }
        return warnings;
    }

    /**
     * Creates a publish resource bean from a resource.<p>
     *
     * @param resource the resource
     * @return the publish resource bean
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsPublishResource createPublishResource(CmsResource resource) throws CmsException {

        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, resource);
        CmsPermissionInfo permissionInfo = OpenCms.getADEManager().getPermissionInfo(m_cms, resource, null);

        String typeName;
        String detailTypeName = null;
        if (CmsJspNavBuilder.isNavLevelFolder(m_cms, resource)) {
            typeName = CmsGwtConstants.TYPE_NAVLEVEL;
        } else if (CmsResourceTypeXmlContainerPage.isModelReuseGroup(m_cms, resource)) {
            typeName = CmsGwtConstants.TYPE_MODELGROUP_REUSE;

        } else {
            typeName = resUtil.getResourceTypeName();
            detailTypeName = CmsResourceIcon.getDefaultFileOrDetailType(m_cms, resource);
        }

        CmsPublishResource pubResource = new CmsPublishResource(
            resource.getStructureId(),
            resUtil.getFullPath(),
            resUtil.getTitle(),
            typeName,
            detailTypeName,
            resource.getState(),
            permissionInfo,
            resource.getDateLastModified(),
            resUtil.getUserLastModified(),
            CmsVfsService.formatDateTime(m_cms, resource.getDateLastModified()),
            false,
            null,
            new ArrayList<CmsPublishResource>());
        return pubResource;
    }

    /**
     * Gets the workplace locale for the currently used CMS context.<p>
     *
     * @return the workplace locale
     */
    protected Locale getLocale() {

        return OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
    }

    /**
     * Gets the resource map filter.<p>
     *
     * This can be used to remove resources which shouldn't be displayed.<p>
     *
     * @return a predicate whose
     */
    protected Predicate<CmsResource> getResourceMapFilter() {

        return new PublishPermissionFilter();
    }

    /**
     * Gets the list of publish resource validators.<p>
     *
     * @return the list of publish resource validators
     */
    protected List<I_PublishResourceValidator> getValidators() {

        return Arrays.asList(
            new AlreadyPublishedValidator(),
            new NoPermissionsValidator(),
            new BlockingLockedValidator());
    }

    /**
     * Sorts the result publish resource list.<p>
     *
     * @param publishResources the list to sort
     */
    protected void sortResult(List<CmsPublishResource> publishResources) {

        Collections.sort(publishResources, new DefaultComparator());
        for (CmsPublishResource resource : publishResources) {
            Collections.sort(resource.getRelated(), new DefaultComparator());
        }
    }

}

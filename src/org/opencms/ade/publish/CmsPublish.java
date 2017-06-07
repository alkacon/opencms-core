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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsPublishResourceInfo;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsPermissionInfo;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishManager;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationPublishValidator;
import org.opencms.relations.CmsRelationValidatorInfoEntry;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * ADE publishing features.<p>
 *
 * @since 8.0.0
 */
public class CmsPublish {

    /**
     * Just for passing around resources and their related together but not mixed up.<p>
     */
    public class ResourcesAndRelated {

        /** The related resources. */
        private Set<CmsResource> m_relatedResources = new HashSet<CmsResource>();

        /** The resources. */
        private Set<CmsResource> m_resources = new HashSet<CmsResource>();

        /**
         * Constructor.<p>
         */
        public ResourcesAndRelated() {

            // empty
        }

        /**
         * Checks if the given resource is present in at least one of the sets.<p>
         *
         * @param resource the resource to test
         *
         * @return <code>true</code> if the given resource is present in at least one of the sets
         */
        public boolean contains(CmsResource resource) {

            return m_resources.contains(resource) || m_relatedResources.contains(resource);
        }

        /**
         * Returns the related resources.<p>
         *
         * @return the related resources
         */
        public Set<CmsResource> getRelatedResources() {

            return m_relatedResources;
        }

        /**
         * Returns the resources.<p>
         *
         * @return the resources
         */
        public Set<CmsResource> getResources() {

            return m_resources;
        }
    }

    /** The number of day groups. */
    protected static final int GROUP_DAYS_NUMBER = 3;

    /** The gap between session groups. */
    protected static final int GROUP_SESSIONS_GAP = 8 * 60 * 60 * 1000;

    /** The number of session groups. */
    protected static final int GROUP_SESSIONS_NUMBER = 2;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublish.class);

    /** The current cms context. */
    protected final CmsObject m_cms;

    /** The options. */
    protected final CmsPublishOptions m_options;

    /** The current user workplace locale. */
    protected final Locale m_workplaceLocale;

    /** The relation validator instance. */
    private CmsRelationPublishValidator m_relationValidator;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsPublish(CmsObject cms) {

        this(cms, new HashMap<String, String>());
    }

    /**
     * Constructor with options.<p>
     *
     * @param cms the current cms context
     * @param options the options to use
     */
    public CmsPublish(CmsObject cms, CmsPublishOptions options) {

        m_cms = cms;
        m_workplaceLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
        m_options = options;
    }

    /**
     * Constructor with default options.<p>
     *
     * @param cms the current cms context
     * @param params the additional publish parameters
     */
    public CmsPublish(CmsObject cms, Map<String, String> params) {

        this(cms, new CmsPublishOptions(params));
    }

    /**
     * Returns the simple name if the ou is the same as the current user's ou.<p>
     *
     * @param cms the CMS context
     * @param name the fully qualified name to check
     *
     * @return the simple name if the ou is the same as the current user's ou
     */
    protected static String getOuAwareName(CmsObject cms, String name) {

        String ou = CmsOrganizationalUnit.getParentFqn(name);
        if (ou.equals(cms.getRequestContext().getCurrentUser().getOuFqn())) {
            return CmsOrganizationalUnit.getSimpleName(name);
        }
        return CmsOrganizationalUnit.SEPARATOR + name;
    }

    /**
     * Checks for possible broken links when the given list of resources would be published.<p>
     *
     * @param pubResources list of resources to be published
     *
     * @return a list of resources that would produce broken links when published
     */
    public List<CmsPublishResource> getBrokenResources(List<CmsResource> pubResources) {

        List<CmsPublishResource> resources = new ArrayList<CmsPublishResource>();
        CmsPublishManager publishManager = OpenCms.getPublishManager();

        CmsPublishList publishList;
        try {
            publishList = OpenCms.getPublishManager().getPublishListAll(
                m_cms,
                pubResources,
                m_options.isIncludeSiblings(),
                true);
            if (m_options.isIncludeRelated()) {
                CmsPublishList related = publishManager.getRelatedResourcesToPublish(m_cms, publishList);
                publishList = publishManager.mergePublishLists(m_cms, publishList, related);
            }

        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            return resources;
        }

        CmsRelationPublishValidator validator = new CmsRelationPublishValidator(m_cms, publishList);
        m_relationValidator = validator;
        for (String resourceName : validator.keySet()) {
            CmsRelationValidatorInfoEntry infoEntry = validator.getInfoEntry(resourceName);
            try {
                CmsResource resource = m_cms.readResource(
                    m_cms.getRequestContext().removeSiteRoot(resourceName),
                    CmsResourceFilter.ALL);
                if (resource.getState().isDeleted()) {
                    for (CmsRelation relation : infoEntry.getRelations()) {
                        try {
                            CmsResource theResource = relation.getSource(m_cms, CmsResourceFilter.ALL);
                            CmsPublishResourceInfo info = new CmsPublishResourceInfo(
                                Messages.get().getBundle(m_workplaceLocale).key(Messages.GUI_BROKEN_LINK_ONLINE_0),
                                CmsPublishResourceInfo.Type.BROKENLINK);
                            // HACK: GWT serialization does not like unmodifiable collections :(
                            // Collections.singletonList(resourceToBean(resource, info, false, null)));
                            ArrayList<CmsPublishResource> relatedList = new ArrayList<CmsPublishResource>();
                            relatedList.add(resourceToBean(resource, info, false, null));
                            CmsPublishResource pubRes = resourceToBean(theResource, null, false, relatedList);
                            resources.add(pubRes);
                        } catch (CmsException e) {
                            // should never happen
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                } else {
                    try {
                        List<CmsPublishResource> related = new ArrayList<CmsPublishResource>();
                        for (CmsRelation relation : infoEntry.getRelations()) {
                            try {
                                CmsResource theResource = relation.getTarget(m_cms, CmsResourceFilter.ALL);
                                CmsPublishResource pubRes = resourceToBean(theResource, null, false, null);
                                related.add(pubRes);
                            } catch (CmsException e) {
                                CmsPublishResource pubRes = relationToBean(relation);
                                related.add(pubRes);
                                LOG.warn(e.getLocalizedMessage(), e);
                            }
                        }
                        CmsPublishResourceInfo info = new CmsPublishResourceInfo(
                            Messages.get().getBundle(m_workplaceLocale).key(Messages.GUI_RESOURCE_MISSING_ONLINE_0),
                            CmsPublishResourceInfo.Type.MISSING);
                        CmsPublishResource pubRes = resourceToBean(resource, info, false, related);
                        resources.add(pubRes);
                    } catch (Exception e) {
                        // should never happen
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        return resources;
    }

    /**
     * Gets the relation validator instance.<p>
     *
     * @return the relation validator
     */
    public CmsRelationPublishValidator getRelationValidator() {

        return m_relationValidator;
    }

    /**
     * Publishes the given list of resources.<p>
     *
     * @param resources list of resources to publish
     *
     * @throws CmsException if something goes wrong
     */
    public void publishResources(List<CmsResource> resources) throws CmsException {

        CmsObject cms = m_cms;
        I_CmsReport report = new CmsHtmlReport(
            cms.getRequestContext().getLocale(),
            cms.getRequestContext().getSiteRoot());
        CmsPublishManager publishManager = OpenCms.getPublishManager();
        CmsPublishList publishList = publishManager.getPublishListAll(m_cms, resources, false, true);
        OpenCms.getPublishManager().publishProject(m_cms, report, publishList);
    }

    /**
     * Creates a publish resource bean from the target information of a relation object.<p>
     *
     * @param relation the relation to use
     *
     * @return the publish resource bean for the relation target
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPublishResource relationToBean(CmsRelation relation) throws CmsException {

        CmsPermissionInfo permissionInfo = new CmsPermissionInfo(true, false, "");
        return new CmsPublishResource(
            relation.getTargetId(),
            relation.getTargetPath(),
            relation.getTargetPath(),
            CmsResourceTypePlain.getStaticTypeName(),
            null,
            CmsResourceState.STATE_UNCHANGED,
            permissionInfo,
            0,
            null,
            null,
            false,
            null,
            null);
    }

    /**
     * Removes the given resources from the user's publish list.<p>
     *
     * @param idsToRemove list of structure ids identifying the resources to be removed
     *
     * @throws CmsException if something goes wrong
     */
    public void removeResourcesFromPublishList(Collection<CmsUUID> idsToRemove) throws CmsException {

        OpenCms.getPublishManager().removeResourceFromUsersPubList(m_cms, idsToRemove);
    }

    /**
     * Creates a publish resource bean instance from the given parameters.<p>
     *
     * @param resource the resource
     * @param info the publish information, if any
     * @param removable if removable
     * @param related the list of related resources
     *
     * @return the publish resource bean
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsPublishResource resourceToBean(
        CmsResource resource,
        CmsPublishResourceInfo info,
        boolean removable,
        List<CmsPublishResource> related)
    throws CmsException {

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
            removable,
            info,
            related);
        return pubResource;
    }

}

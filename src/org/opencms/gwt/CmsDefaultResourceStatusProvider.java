/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt;

import org.opencms.ade.containerpage.CmsRelationTargetListBean;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsPermissionInfo;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.gwt.shared.CmsResourceStatusRelationBean;
import org.opencms.gwt.shared.CmsResourceStatusTabId;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsRole;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Helper class to generate all the data which is necessary for the resource status dialog(s).<p>
 */
public class CmsDefaultResourceStatusProvider {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultResourceStatusProvider.class);

    /**
     * Gets the relation targets for a resource.<p>
     *
     * @param cms the current CMS context
     * @param source the structure id of the resource for which we want the relation targets
     * @param additionalIds the structure ids of additional resources to include with the relation targets
     * @param cancelIfChanged if this is true, this method will stop immediately if it finds a changed resource among the relation targets
     *
     * @return a bean containing a list of relation targets
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsRelationTargetListBean getContainerpageRelationTargets(
        CmsObject cms,
        CmsUUID source,
        List<CmsUUID> additionalIds,
        boolean cancelIfChanged)
    throws CmsException {

        CmsRelationTargetListBean result = new CmsRelationTargetListBean();
        CmsResource content = cms.readResource(source, CmsResourceFilter.ALL);
        boolean isContainerPage = CmsResourceTypeXmlContainerPage.isContainerPage(content);
        if (additionalIds != null) {
            for (CmsUUID structureId : additionalIds) {
                try {
                    CmsResource res = cms.readResource(structureId, CmsResourceFilter.ALL);
                    result.add(res);
                    if (res.getState().isChanged() && cancelIfChanged) {
                        return result;
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        List<CmsRelation> relations = cms.readRelations(CmsRelationFilter.relationsFromStructureId(source));
        for (CmsRelation relation : relations) {
            if (relation.getType() == CmsRelationType.XSD) {
                continue;
            }
            try {
                CmsResource target = relation.getTarget(cms, CmsResourceFilter.ALL);
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(target);
                if (isContainerPage && (type instanceof CmsResourceTypeJsp)) {
                    // ignore formatters for container pages, as the normal user probably doesn't want to deal with them
                    continue;
                }
                result.add(target);
                if (target.getState().isChanged() && cancelIfChanged) {
                    return result;
                }
            } catch (CmsException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Collects all the data to display in the resource status dialog.<p>
     *
     * @param cms the current CMS context
     * @param structureId the structure id of the resource for which we want the information
     * @param contentLocale the content locale
     * @param includeTargets true if relation targets should be included
     * @param detailContentId the structure id of the detail content if present
     * @param additionalStructureIds structure ids of additional resources to include with the relation targets
     *
     * @return the resource status information
     * @throws CmsException if something goes wrong
     */
    public CmsResourceStatusBean getResourceStatus(
        CmsObject cms,
        CmsUUID structureId,
        String contentLocale,
        boolean includeTargets,
        CmsUUID detailContentId,
        List<CmsUUID> additionalStructureIds)
    throws CmsException {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        cms.getRequestContext().setLocale(locale);
        CmsResource resource = cms.readResource(structureId, CmsResourceFilter.ALL);
        String localizedTitle = null;
        Locale realLocale = null;
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(contentLocale)) {
            realLocale = CmsLocaleManager.getLocale(contentLocale);
            CmsGallerySearchResult result = CmsGallerySearch.searchById(cms, structureId, realLocale);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(result.getTitle())) {
                localizedTitle = result.getTitle();
            }
        }
        CmsResourceUtil resourceUtil = new CmsResourceUtil(cms, resource);
        List<CmsProperty> properties = cms.readPropertyObjects(resource, false);
        CmsResourceStatusBean result = new CmsResourceStatusBean();
        result.setDateCreated(CmsVfsService.formatDateTime(cms, resource.getDateCreated()));
        long dateExpired = resource.getDateExpired();
        if (dateExpired != CmsResource.DATE_EXPIRED_DEFAULT) {
            result.setDateExpired(CmsVfsService.formatDateTime(cms, dateExpired));
        }
        result.setDateLastModified(CmsVfsService.formatDateTime(cms, resource.getDateLastModified()));
        long dateReleased = resource.getDateReleased();
        if (dateReleased != CmsResource.DATE_RELEASED_DEFAULT) {
            result.setDateReleased(CmsVfsService.formatDateTime(cms, dateReleased));
        }
        String lastProject = resourceUtil.getLockedInProjectName();
        if ("".equals(lastProject)) {
            lastProject = null;
        }
        result.setLastProject(lastProject);

        result.setListInfo(CmsVfsService.getPageInfo(cms, resource));
        CmsLock lock = cms.getLock(resource);
        CmsUser lockOwner = null;
        if (!lock.isUnlocked()) {
            lockOwner = cms.readUser(lock.getUserId());
            result.setLockState(
                org.opencms.workplace.list.Messages.get().getBundle(locale).key(
                    org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_ACTION_LOCK_NAME_2,
                    lockOwner.getName(),
                    lastProject));
        } else {
            result.setLockState(
                org.opencms.workplace.list.Messages.get().getBundle(locale).key(
                    org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_ACTION_UNLOCK_NAME_0));
        }

        CmsProperty navText = CmsProperty.get(CmsPropertyDefinition.PROPERTY_NAVTEXT, properties);
        if (navText != null) {
            result.setNavText(navText.getValue());
        }
        result.setPermissions(resourceUtil.getPermissionString());
        result.setSize(resource.getLength());
        result.setStateBean(resource.getState());
        CmsProperty title = CmsProperty.get(CmsPropertyDefinition.PROPERTY_TITLE, properties);
        if (localizedTitle != null) {
            result.setTitle(localizedTitle);
            result.getListInfo().setTitle(localizedTitle);
        } else if (title != null) {
            result.setTitle(title.getValue());
        }
        result.setUserCreated(resourceUtil.getUserCreated());
        result.setUserLastModified(resourceUtil.getUserLastModified());

        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resource);
        result.setResourceType(resType.getTypeName());

        result.setStructureId(resource.getStructureId());
        if (resType instanceof CmsResourceTypeXmlContent) {
            CmsFile file = cms.readFile(resource);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
            List<Locale> locales = content.getLocales();
            List<String> localeStrings = new ArrayList<String>();
            for (Locale l : locales) {
                localeStrings.add(l.toString());
            }
            result.setLocales(localeStrings);
        }
        Map<String, String> additionalAttributes = new LinkedHashMap<String, String>();
        additionalAttributes.put(
            Messages.get().getBundle(locale).key(Messages.GUI_STATUS_PERMALINK_0),
            OpenCms.getLinkManager().getPermalink(cms, cms.getSitePath(resource), detailContentId));

        if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR)) {
            additionalAttributes.put(
                Messages.get().getBundle(locale).key(Messages.GUI_STATUS_STRUCTURE_ID_0),
                resource.getStructureId().toString());
            additionalAttributes.put(
                Messages.get().getBundle(locale).key(Messages.GUI_STATUS_RESOURCE_ID_0),
                resource.getResourceId().toString());
        }
        result.setAdditionalAttributes(additionalAttributes);

        List<CmsRelation> relations = cms.readRelations(
            CmsRelationFilter.relationsToStructureId(resource.getStructureId()));
        Map<CmsUUID, CmsResource> relationSources = new HashMap<CmsUUID, CmsResource>();

        if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
            // People may link to the folder of a container page instead of the page itself
            try {
                CmsResource parent = cms.readParentFolder(resource.getStructureId());
                List<CmsRelation> parentRelations = cms.readRelations(
                    CmsRelationFilter.relationsToStructureId(parent.getStructureId()));
                relations.addAll(parentRelations);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        // find all distinct relation sources
        for (CmsRelation relation : relations) {
            try {
                CmsResource currentSource = relation.getSource(cms, CmsResourceFilter.ALL);
                relationSources.put(currentSource.getStructureId(), currentSource);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        for (CmsResource relationResource : relationSources.values()) {
            try {
                CmsPermissionInfo permissionInfo = OpenCms.getADEManager().getPermissionInfo(
                    cms,
                    relationResource,
                    resource.getRootPath());
                if (permissionInfo.hasViewPermission()) {
                    CmsResourceStatusRelationBean relationBean = createRelationBean(
                        cms,
                        contentLocale,
                        relationResource,
                        permissionInfo);
                    CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(relationResource.getRootPath());
                    if ((site != null)
                        && !CmsStringUtil.isPrefixPath(
                            cms.getRequestContext().getSiteRoot(),
                            relationResource.getRootPath())) {
                        String siteTitle = site.getTitle();
                        if (siteTitle == null) {
                            siteTitle = site.getUrl();
                        } else {
                            siteTitle = CmsWorkplace.substituteSiteTitleStatic(
                                siteTitle,
                                OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
                        }
                        relationBean.setSiteRoot(site.getSiteRoot());
                        result.getOtherSiteRelationSources().add(relationBean);
                        relationBean.getInfoBean().setTitle(
                            "[" + siteTitle + "] " + relationBean.getInfoBean().getTitle());
                    } else {
                        result.getRelationSources().add(relationBean);
                    }

                }
            } catch (CmsVfsResourceNotFoundException notfound) {
                LOG.error(notfound.getLocalizedMessage(), notfound);
                continue;
            }
        }
        sortOtherSiteRelations(cms, result);
        if (includeTargets) {
            result.getRelationTargets().addAll(getTargets(cms, contentLocale, resource, additionalStructureIds));
            if ((detailContentId != null) && (realLocale != null)) {
                // try to add detail only contents
                try {
                    Optional<CmsResource> detailOnlyPage = CmsJspTagContainer.getDetailOnlyPage(
                        cms,
                        cms.readResource(detailContentId, CmsResourceFilter.ALL),
                        CmsJspTagContainer.getDetailContainerLocale(cms, realLocale.toString(), resource));
                    if (detailOnlyPage.isPresent()) {
                        result.getRelationTargets().addAll(
                            getTargets(
                                cms,
                                contentLocale,
                                detailOnlyPage.get(),
                                Arrays.asList(detailOnlyPage.get().getStructureId())));
                    }

                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            Iterator<CmsResourceStatusRelationBean> iter = result.getRelationTargets().iterator();
            // Remove duplicates
            Set<CmsUUID> visitedIds = Sets.newHashSet();
            while (iter.hasNext()) {
                CmsResourceStatusRelationBean bean = iter.next();
                if (visitedIds.contains(bean.getStructureId())) {
                    iter.remove();
                }
                visitedIds.add(bean.getStructureId());
            }
        }
        result.getSiblings().addAll(getSiblings(cms, contentLocale, resource));
        LinkedHashMap<CmsResourceStatusTabId, String> tabMap = new LinkedHashMap<CmsResourceStatusTabId, String>();
        Map<CmsResourceStatusTabId, CmsMessageContainer> tabs;
        CmsResourceStatusTabId startTab = CmsResourceStatusTabId.tabRelationsFrom;
        if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
            tabs = CmsResourceStatusConstants.STATUS_TABS_CONTAINER_PAGE;
        } else if (OpenCms.getResourceManager().getResourceType(resource) instanceof I_CmsLinkParseable) {
            tabs = CmsResourceStatusConstants.STATUS_TABS_CONTENT;
        } else {
            tabs = CmsResourceStatusConstants.STATUS_TABS_OTHER;
            startTab = CmsResourceStatusTabId.tabStatus;
        }
        for (Map.Entry<CmsResourceStatusTabId, CmsMessageContainer> entry : tabs.entrySet()) {
            tabMap.put(entry.getKey(), entry.getValue().key(locale));
        }

        result.setTabs(tabMap);
        result.setStartTab(startTab);
        return result;
    }

    /**
     * Sorts relation beans from other sites by site order.<p>
     *
     * @param cms the current CMS context
     * @param resStatus the bean in which to sort the relation beans
     */
    public void sortOtherSiteRelations(CmsObject cms, CmsResourceStatusBean resStatus) {

        final List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(
            cms,
            false,
            false,
            cms.getRequestContext().getOuFqn());
        Collections.sort(resStatus.getOtherSiteRelationSources(), new Comparator<CmsResourceStatusRelationBean>() {

            private Map<String, Integer> m_rankCache = Maps.newHashMap();

            public int compare(CmsResourceStatusRelationBean o1, CmsResourceStatusRelationBean o2) {

                return ComparisonChain.start().compare(rank(o1), rank(o2)).compare(
                    o1.getSitePath(),
                    o2.getSitePath()).result();

            }

            public int rank(CmsResourceStatusRelationBean r) {

                if (m_rankCache.containsKey(r.getSiteRoot())) {
                    return m_rankCache.get(r.getSiteRoot()).intValue();
                }

                int j = 0;
                int result = Integer.MAX_VALUE;
                for (CmsSite site : sites) {
                    if (site.getSiteRoot().equals(r.getSiteRoot())) {
                        result = j;
                        break;
                    }
                    j += 1;
                }

                m_rankCache.put(r.getSiteRoot(), new Integer(result));
                return result;
            }
        });
    }

    /**
     * Gets beans which represents the siblings of a resource.<p>
     *
     * @param cms the CMS ccontext
     * @param locale the locale
     * @param resource the resource
     * @return the list of sibling beans
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResourceStatusRelationBean> getSiblings(CmsObject cms, String locale, CmsResource resource)
    throws CmsException {

        List<CmsResourceStatusRelationBean> result = new ArrayList<CmsResourceStatusRelationBean>();
        for (CmsResource sibling : cms.readSiblings(resource, CmsResourceFilter.ALL)) {
            if (sibling.getStructureId().equals(resource.getStructureId())) {
                continue;
            }
            try {
                CmsPermissionInfo permissionInfo = OpenCms.getADEManager().getPermissionInfo(
                    cms,
                    sibling,
                    resource.getRootPath());
                if (permissionInfo.hasViewPermission()) {
                    CmsResourceStatusRelationBean relationBean = createRelationBean(
                        cms,
                        locale,
                        sibling,
                        permissionInfo);
                    result.add(relationBean);
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Gets the list of relation targets for a resource.<p>
     *
     * @param cms the current CMS context
     * @param locale the locale
     * @param resource the resource for which we want the relation targets
     * @param additionalStructureIds structure ids of additional resources to include with the relation target
     *
     * @return the list of relation beans for the relation targets
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResourceStatusRelationBean> getTargets(
        CmsObject cms,
        String locale,
        CmsResource resource,
        List<CmsUUID> additionalStructureIds)
    throws CmsException {

        CmsRelationTargetListBean listBean = getContainerpageRelationTargets(
            cms,
            resource.getStructureId(),
            additionalStructureIds,
            false);
        List<CmsResourceStatusRelationBean> result = new ArrayList<CmsResourceStatusRelationBean>();
        for (CmsResource target : listBean.getResources()) {
            try {
                CmsPermissionInfo permissionInfo = OpenCms.getADEManager().getPermissionInfo(
                    cms,
                    target,
                    resource.getRootPath());
                if (permissionInfo.hasViewPermission()) {
                    CmsResourceStatusRelationBean relationBean = createRelationBean(
                        cms,
                        locale,
                        target,
                        permissionInfo);
                    result.add(relationBean);
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;

    }

    /**
     * Creates a bean for a single resource which is part of a relation list.<p>
     *
     * @param cms the current CMS context
     * @param locale the locale
     * @param relationResource the resource
     * @param permissionInfo the permission info
     *
     * @return the status bean for the resource
     *
     * @throws CmsException if something goes wrong
     */
    CmsResourceStatusRelationBean createRelationBean(
        CmsObject cms,
        String locale,
        CmsResource relationResource,
        CmsPermissionInfo permissionInfo)
    throws CmsException {

        CmsListInfoBean sourceBean = CmsVfsService.getPageInfo(cms, relationResource);
        sourceBean.setMarkChangedState(true);
        sourceBean.setResourceState(relationResource.getState());

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(locale)) {
            Locale realLocale = CmsLocaleManager.getLocale(locale);
            CmsGallerySearchResult result = CmsGallerySearch.searchById(
                cms,
                relationResource.getStructureId(),
                realLocale);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(result.getTitle())) {
                sourceBean.setTitle(result.getTitle());
            }
        }
        String link = null;
        try {
            link = OpenCms.getLinkManager().substituteLink(cms, relationResource);
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        CmsResourceStatusRelationBean relationBean = new CmsResourceStatusRelationBean(
            sourceBean,
            link,
            relationResource.getStructureId(),
            permissionInfo);
        if (CmsResourceTypeXmlContent.isXmlContent(relationResource)) {
            relationBean.setIsXmlContent(true);
        }
        String sitePath = cms.getSitePath(relationResource);
        relationBean.setSitePath(sitePath);
        return relationBean;
    }

}

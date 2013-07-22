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
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.gwt.shared.CmsResourceStatusRelationBean;
import org.opencms.gwt.shared.CmsResourceStatusTabId;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

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
        boolean cancelIfChanged) throws CmsException {

        CmsRelationTargetListBean result = new CmsRelationTargetListBean();
        CmsResource content = cms.readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        boolean isContainerPage = CmsResourceTypeXmlContainerPage.isContainerPage(content);
        for (CmsUUID structureId : additionalIds) {
            try {
                CmsResource res = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                result.add(res);
                if (res.getState().isChanged() && cancelIfChanged) {
                    return result;
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        List<CmsRelation> relations = cms.readRelations(CmsRelationFilter.relationsFromStructureId(source));
        for (CmsRelation relation : relations) {
            try {
                CmsResource target = relation.getTarget(cms, CmsResourceFilter.IGNORE_EXPIRATION);
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
                LOG.error(e.getLocalizedMessage(), e);
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
        List<CmsUUID> additionalStructureIds) throws CmsException {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        cms.getRequestContext().setLocale(locale);
        CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        String localizedTitle = null;
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(contentLocale)) {
            Locale realLocale = CmsLocaleManager.getLocale(contentLocale);
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
            result.setLockState(org.opencms.workplace.list.Messages.get().getBundle(locale).key(
                org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_ACTION_LOCK_NAME_2,
                lockOwner.getName(),
                lastProject));
        } else {
            result.setLockState(org.opencms.workplace.list.Messages.get().getBundle(locale).key(
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

        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
        result.setResourceType(resType.getTypeName());
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

        List<CmsRelation> relations = cms.readRelations(CmsRelationFilter.relationsToStructureId(resource.getStructureId()));
        Map<CmsUUID, CmsResource> relationSources = new HashMap<CmsUUID, CmsResource>();

        // find all distinct relation sources 
        for (CmsRelation relation : relations) {
            CmsResource currentSource = relation.getSource(cms, CmsResourceFilter.IGNORE_EXPIRATION);
            relationSources.put(currentSource.getStructureId(), currentSource);
        }

        for (CmsResource relationResource : relationSources.values()) {
            try {
                CmsResourceStatusRelationBean relationBean = createRelationBean(cms, relationResource);
                result.getRelationSources().add(relationBean);
            } catch (CmsVfsResourceNotFoundException notfound) {
                LOG.error(notfound.getLocalizedMessage(), notfound);
                continue;
            }
        }
        if (includeTargets) {
            result.getRelationTargets().addAll(getTargets(cms, structureId, additionalStructureIds));
        }
        result.setTabs(getTabClientData(cms, resource));
        return result;
    }

    /**
     * Gets the list of relation targets for a resource.<p>
     * 
     * @param cms the current CMS context 
     * @param structureId the structure id of the resource for which we want the relation targets 
     * @param additionalStructureIds structure ids of additional resources to include with the relation target
     *  
     * @return the list of relation beans for the relation targets
     *  
     * @throws CmsException if something goes wrong 
     */
    protected List<CmsResourceStatusRelationBean> getTargets(
        CmsObject cms,
        CmsUUID structureId,
        List<CmsUUID> additionalStructureIds) throws CmsException {

        CmsRelationTargetListBean listBean = getContainerpageRelationTargets(
            cms,
            structureId,
            additionalStructureIds,
            false);
        List<CmsResourceStatusRelationBean> result = new ArrayList<CmsResourceStatusRelationBean>();
        for (CmsResource target : listBean.getResources()) {
            try {
                CmsResourceStatusRelationBean relationBean = createRelationBean(cms, target);
                result.add(relationBean);
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
     * @param relationResource the resource 
     * 
     * @return the status bean for the resource
     * 
     * @throws CmsException if something goes wrong 
     */
    CmsResourceStatusRelationBean createRelationBean(CmsObject cms, CmsResource relationResource) throws CmsException {

        CmsListInfoBean sourceBean = CmsVfsService.getPageInfo(cms, relationResource);
        String link = null;
        try {
            link = OpenCms.getLinkManager().substituteLink(cms, relationResource);
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        CmsResourceStatusRelationBean relationBean = new CmsResourceStatusRelationBean(
            sourceBean,
            link,
            relationResource.getStructureId());
        if (CmsResourceTypeXmlContent.isXmlContent(relationResource)) {
            relationBean.setIsXmlContent(true);
        }
        String sitePath = cms.getSitePath(relationResource);
        relationBean.setSitePath(sitePath);
        return relationBean;
    }

    /**
     * Determines the arrangement of tabs to display, together with their labels.<p>
     * 
     * @param cms the current CMS context
     * @param res the resource for which the dialog should be displayed 
     * @return the tab configuration for the dialog 
     */
    private LinkedHashMap<CmsResourceStatusTabId, String> getTabClientData(CmsObject cms, CmsResource res) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        LinkedHashMap<CmsResourceStatusTabId, String> result = new LinkedHashMap<CmsResourceStatusTabId, String>();
        Map<CmsResourceStatusTabId, CmsMessageContainer> tabs = getTabData(res);
        for (Map.Entry<CmsResourceStatusTabId, CmsMessageContainer> entry : tabs.entrySet()) {
            result.put(entry.getKey(), entry.getValue().key(locale));
        }
        return result;
    }

    /**
     * Determines the arrangement of tabs to display, together with their labels.<p>
     * 
     * @param res the resource for which the dialog should be displayed 
     * @return the tab configuration for the dialog 
     */
    private Map<CmsResourceStatusTabId, CmsMessageContainer> getTabData(CmsResource res) {

        Map<CmsResourceStatusTabId, CmsMessageContainer> tabs;
        if (CmsResourceTypeXmlContainerPage.isContainerPage(res)) {
            tabs = CmsResourceStatusConstants.STATUS_TABS_CONTAINER_PAGE;
        } else if (OpenCms.getResourceManager().getResourceType(res) instanceof I_CmsLinkParseable) {
            tabs = CmsResourceStatusConstants.STATUS_TABS_CONTENT;
        } else {
            tabs = CmsResourceStatusConstants.STATUS_TABS_OTHER;
        }
        return tabs;
    }

}

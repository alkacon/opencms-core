/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/Attic/CmsVfsSitemapService.java,v $
 * Date   : $Date: 2011/02/21 11:21:48 $
 * Version: $Revision: 1.17 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap;

import org.opencms.ade.sitemap.shared.CmsClientLock;
import org.opencms.ade.sitemap.shared.CmsClientProperty;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.ade.sitemap.shared.CmsPropertyModification;
import org.opencms.ade.sitemap.shared.CmsSitemapBrokenLinkBean;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapMergeInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapTemplate;
import org.opencms.ade.sitemap.shared.CmsSubSitemapInfo;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EntryType;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.adeconfig.CmsConfigurationSourceInfo;
import org.opencms.adeconfig.CmsContainerPageConfigurationData;
import org.opencms.adeconfig.CmsSitemapConfigurationData;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.xml.containerpage.CmsADEDefaultConfiguration;
import org.opencms.xml.containerpage.CmsConfigurationItem;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsDetailPageConfigurationWriter;
import org.opencms.xml.sitemap.CmsDetailPageInfo;
import org.opencms.xml.sitemap.CmsSitemapManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;

/**
 * Handles all RPC services related to the vfs sitemap.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.17 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public class CmsVfsSitemapService extends CmsGwtService implements I_CmsSitemapService {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsSitemapService.class);

    /** Serialization uid. */
    private static final long serialVersionUID = -7236544324371767330L;

    /** Session attribute name constant. */
    private static final String SESSION_ATTR_ADE_SITEMAP_CLIPBOARD_CACHE = "__OCMS_ADE_SITEMAP_CLIPBOARD_CACHE__";

    /** The navigation builder. */
    private CmsJspNavBuilder m_navBuilder;

    /**
     * Returns a new configured service instance.<p>
     * 
     * @param request the current request
     * 
     * @return a new service instance
     */
    public static CmsVfsSitemapService newInstance(HttpServletRequest request) {

        CmsVfsSitemapService service = new CmsVfsSitemapService();
        service.setCms(CmsFlexController.getCmsObject(request));
        service.setRequest(request);
        return service;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#createSubSitemap(java.lang.String, java.lang.String)
     */
    public CmsSubSitemapInfo createSubSitemap(String entryPoint, String path) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            String subSitemapPath = CmsResource.getFolderPath(path);
            CmsResource subSitemapFolder = cms.readResource(subSitemapPath);
            ensureLock(subSitemapFolder);
            String folderName = CmsStringUtil.joinPaths("/", "_config");
            String sitemapConfigName = CmsStringUtil.joinPaths(folderName, "sitemap_"
                + subSitemapFolder.getName()
                + ".config");
            String containerpageConfigName = CmsStringUtil.joinPaths(folderName, "containerpage_"
                + subSitemapFolder.getName()
                + ".config");
            if (!cms.existsResource(folderName)) {
                tryUnlock(cms.createResource(folderName, CmsResourceTypeFolder.getStaticTypeId()));
            }
            if (cms.existsResource(sitemapConfigName)) {
                sitemapConfigName = CmsADEDefaultConfiguration.getNewFileName(cms, CmsStringUtil.joinPaths(
                    folderName,
                    "sitemap_" + subSitemapFolder.getName() + "_%(number).config"));
            }
            tryUnlock(cms.createResource(sitemapConfigName, OpenCms.getResourceManager().getResourceType(
                "sitemap_config").getTypeId()));
            if (cms.existsResource(containerpageConfigName)) {
                containerpageConfigName = CmsADEDefaultConfiguration.getNewFileName(cms, CmsStringUtil.joinPaths(
                    folderName,
                    "containerpage_" + subSitemapFolder.getName() + "_%(number).config"));
            }
            tryUnlock(cms.createResource(containerpageConfigName, OpenCms.getResourceManager().getResourceType(
                "containerpage_config").getTypeId()));

            List<CmsProperty> propertyObjects = new ArrayList<CmsProperty>();
            propertyObjects.add(new CmsProperty(
                CmsPropertyDefinition.PROPERTY_ADE_SITEMAP_CONFIG,
                sitemapConfigName,
                sitemapConfigName));
            propertyObjects.add(new CmsProperty(
                CmsPropertyDefinition.PROPERTY_ADE_CNTPAGE_CONFIG,
                containerpageConfigName,
                containerpageConfigName));
            cms.writePropertyObjects(subSitemapPath, propertyObjects);
            subSitemapFolder.setType(getEntryPointType());
            cms.writeResource(subSitemapFolder);
            tryUnlock(subSitemapFolder);

            CmsSitemapClipboardData clipboard = getClipboardData();
            CmsClientSitemapEntry entry = toClientEntry(getNavBuilder().getNavigationForResource(
                cms.getSitePath(subSitemapFolder)), OpenCms.getSitemapManager().getElementPropertyConfiguration(
                cms,
                entryPoint), false);
            clipboard.addModified(entry);
            setClipboardData(clipboard);
            return new CmsSubSitemapInfo(subSitemapPath, System.currentTimeMillis());
        } catch (Exception e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getBrokenLinksToSitemapEntries(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public List<CmsSitemapBrokenLinkBean> getBrokenLinksToSitemapEntries(CmsClientSitemapEntry deleteEntry)
    throws CmsRpcException {

        List<CmsSitemapBrokenLinkBean> result = null;

        try {
            CmsObject cms = getCmsObject();
            List<CmsResource> descendands = new ArrayList<CmsResource>();
            HashSet<CmsUUID> deleteIds = new HashSet<CmsUUID>();
            CmsResource entryResource = cms.readResource(deleteEntry.getId());

            descendands.add(entryResource);
            if (entryResource.isFolder()) {
                descendands.addAll(cms.readResources(deleteEntry.getSitePath(), CmsResourceFilter.IGNORE_EXPIRATION));
            }
            for (CmsResource deleteRes : descendands) {
                deleteIds.add(deleteRes.getStructureId());
            }
            // multimap from resources to (sets of) sitemap entries 

            MultiValueMap linkMap = MultiValueMap.decorate(
                new HashMap<Object, Object>(),
                FactoryUtils.instantiateFactory(HashSet.class));
            for (CmsResource resource : descendands) {
                List<CmsResource> linkSources = getLinkSources(cms, resource, deleteIds);
                for (CmsResource source : linkSources) {
                    linkMap.put(resource, source);
                }
            }

            result = getBrokenLinkBeans(linkMap);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getChildren(java.lang.String, java.lang.String, int)
     */
    public CmsClientSitemapEntry getChildren(String entryPointUri, String root, int levels) throws CmsRpcException {

        CmsClientSitemapEntry entry = null;

        try {
            CmsObject cms = getCmsObject();

            //ensure that root ends with a '/' if it's a folder 
            CmsResource rootRes = cms.readResource(root.replaceAll("/$", ""));
            root = cms.getSitePath(rootRes);

            Map<String, CmsXmlContentProperty> propertyConfig = OpenCms.getSitemapManager().getElementPropertyConfiguration(
                cms,
                entryPointUri);

            CmsJspNavElement navElement = getNavBuilder().getNavigationForResource(root);
            entry = toClientEntry(navElement, propertyConfig, false);
            if (rootRes.isFolder() && (!isSubSitemap(navElement) || root.equals(entryPointUri))) {
                entry.setSubEntries(getChildren(root, levels, propertyConfig));
            }
        } catch (Throwable e) {
            error(e);
        }
        return entry;
    }

    /**
     * Returns the available templates.<p>
     * 
     * @return the available templates
     * 
     * @throws CmsRpcException if something goes wrong
     */
    public Map<String, CmsSitemapTemplate> getTemplates() throws CmsRpcException {

        Map<String, CmsSitemapTemplate> result = new HashMap<String, CmsSitemapTemplate>();
        CmsObject cms = getCmsObject();
        try {
            // find current site templates
            int templateId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeJsp.getContainerPageTemplateTypeName()).getTypeId();
            List<CmsResource> templates = cms.readResources(
                "/",
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(templateId),
                true);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(cms.getRequestContext().getSiteRoot())) {
                // if not in the root site, also add template under /system/
                templates.addAll(cms.readResources(
                    CmsWorkplace.VFS_PATH_SYSTEM,
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(templateId),
                    true));
            }
            // convert resources to template beans
            for (CmsResource template : templates) {
                try {
                    CmsSitemapTemplate templateBean = getTemplateBean(cms, template);
                    result.put(templateBean.getSitePath(), templateBean);
                } catch (CmsException e) {
                    // should never happen
                    log(e.getLocalizedMessage(), e);
                }
            }
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#mergeSubSitemap(java.lang.String, java.lang.String)
     */
    public CmsSitemapMergeInfo mergeSubSitemap(String entryPoint, String path) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            String subSitemapPath = CmsResource.getFolderPath(path);
            CmsResource subSitemapFolder = cms.readResource(subSitemapPath);
            ensureLock(subSitemapFolder);
            List<CmsProperty> propertyObjects = new ArrayList<CmsProperty>();
            propertyObjects.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_ADE_SITEMAP_CONFIG, "", ""));
            propertyObjects.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_ADE_CNTPAGE_CONFIG, "", ""));
            cms.writePropertyObjects(subSitemapPath, propertyObjects);
            subSitemapFolder.setType(OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeFolder.RESOURCE_TYPE_NAME).getTypeId());
            tryUnlock(subSitemapFolder);
            CmsSitemapClipboardData clipboard = getClipboardData();
            CmsClientSitemapEntry entry = toClientEntry(getNavBuilder().getNavigationForResource(
                cms.getSitePath(subSitemapFolder)), OpenCms.getSitemapManager().getElementPropertyConfiguration(
                cms,
                entryPoint), false);
            clipboard.addModified(entry);
            setClipboardData(clipboard);
            return new CmsSitemapMergeInfo(getChildren(entryPoint, subSitemapPath, 1), System.currentTimeMillis());
        } catch (Exception e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#prefetch(java.lang.String)
     */
    public CmsSitemapData prefetch(String sitemapUri) throws CmsRpcException {

        CmsSitemapData result = null;
        CmsObject cms = getCmsObject();
        CmsSitemapManager sitemapMgr = OpenCms.getSitemapManager();
        try {
            String openPath = getRequest().getParameter("path");
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(openPath)) {
                // if no path is supplied, start from root
                openPath = "/";
            }
            String entryPoint = sitemapMgr.findEntryPoint(cms, openPath);

            Map<String, CmsXmlContentProperty> propertyConfig = new HashMap<String, CmsXmlContentProperty>(
                sitemapMgr.getElementPropertyConfiguration(cms, entryPoint));
            //TODO: look if resolving macros on properties is necessary

            //            I_CmsXmlContentHandler contentHandler = CmsXmlContentDefinition.getContentHandlerForResource(cms, sitemap);
            //            CmsMacroResolver resolver = CmsXmlContentPropertyHelper.getMacroResolverForProperties(cms, contentHandler);
            //            Map<String, CmsXmlContentProperty> resolvedProps = CmsXmlContentPropertyHelper.resolveMacrosInProperties(
            //                propertyConfig,
            //                resolver);

            Map<String, CmsClientProperty> parentProperties = generateParentProperties(propertyConfig, entryPoint);

            String siteRoot = cms.getRequestContext().getSiteRoot();
            String exportRfsPrefix = OpenCms.getStaticExportManager().getDefaultRfsPrefix();
            CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
            boolean isSecure = site.hasSecureServer();

            String parentSitemap = null;
            if (!entryPoint.equals("/")) {
                parentSitemap = sitemapMgr.findEntryPoint(cms, CmsResource.getParentFolder(entryPoint));
            }
            CmsSitemapConfigurationData sitemapConfig = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
                cms,
                cms.getRequestContext().addSiteRoot(openPath));
            String noEdit = "";
            CmsNewResourceInfo defaultNewInfo = null;
            List<CmsNewResourceInfo> newResourceInfos = null;
            CmsDetailPageTable detailPages = null;
            List<CmsNewResourceInfo> resourceTypeInfos = null;
            boolean canEditDetailPages = false;
            boolean isOnlineProject = CmsProject.isOnlineProject(cms.getRequestContext().getCurrentProject().getUuid());
            if (sitemapConfig == null) {
                noEdit = "No sitemap configuration available.";
            } else {
                detailPages = new CmsDetailPageTable(sitemapConfig.getDetailPageInfo());
                if (!isOnlineProject) {
                    CmsConfigurationItem containerpageConfigItem = sitemapConfig.getTypeConfiguration().get(
                        CmsResourceTypeXmlContainerPage.getStaticTypeName());
                    if (containerpageConfigItem != null) {
                        resourceTypeInfos = getResourceTypeInfos(
                            getCmsObject(),
                            entryPoint,
                            containerpageConfigItem.getSourceFile().getStructureId());
                        defaultNewInfo = createNewResourceInfo(cms, containerpageConfigItem);
                    }
                    canEditDetailPages = !(sitemapConfig.getLastSource().isModuleConfiguration());
                    newResourceInfos = getNewResourceInfos(cms, entryPoint);
                }
            }
            if (isOnlineProject) {
                noEdit = "Can't edit sitemap in online project.";
            }
            List<String> allPropNames = getPropertyNames(cms);

            result = new CmsSitemapData(
                getDefaultTemplate(entryPoint),
                getTemplates(),
                propertyConfig,
                getClipboardData(),
                parentProperties,
                allPropNames,
                exportRfsPrefix,
                isSecure,
                noEdit,
                isDisplayToolbar(getRequest()),
                defaultNewInfo,
                newResourceInfos,
                parentSitemap,
                getRootEntry(entryPoint, propertyConfig),
                openPath,
                30,
                detailPages,
                resourceTypeInfos,
                canEditDetailPages);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#save(java.lang.String, org.opencms.ade.sitemap.shared.CmsSitemapChange)
     */
    public void save(String entryPoint, CmsSitemapChange change) throws CmsRpcException {

        try {
            saveInternal(entryPoint, change);
        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#saveSync(java.lang.String, org.opencms.ade.sitemap.shared.CmsSitemapChange)
     */
    public void saveSync(String entryPoint, CmsSitemapChange change) throws CmsRpcException {

        save(entryPoint, change);
    }

    /**
     * Creates a "broken link" bean based on a resource.<p>
     * 
     * @param resource the resource 
     * 
     * @return the "broken link" bean with the data from the resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsSitemapBrokenLinkBean createSitemapBrokenLinkBean(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsProperty titleProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, true);
        String defaultTitle = "";
        String title = titleProp.getValue(defaultTitle);
        String path = cms.getSitePath(resource);
        String subtitle = path;
        return new CmsSitemapBrokenLinkBean(title, subtitle);
    }

    /**
     * Gets the resources which link to a given (sitemap or structure) id.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the relation target resource
     * @param deleteIds set of resources to delete
     *  
     * @return the list of resources which link to the given id
     *  
     * @throws CmsException
     */
    protected List<CmsResource> getLinkSources(CmsObject cms, CmsResource resource, HashSet<CmsUUID> deleteIds)
    throws CmsException {

        List<CmsRelation> relations = cms.getRelationsForResource(resource, CmsRelationFilter.SOURCES);
        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsRelation relation : relations) {
            // only add related resources that are not going to be deleted
            if (!deleteIds.contains(relation.getSourceId())) {
                result.add(relation.getSource(cms, CmsResourceFilter.IGNORE_EXPIRATION));
            }
        }
        return result;
    }

    /**
     * Internal method for saving a sitemap.<p>
     * 
     * @param entryPoint the URI of the sitemap to save
     * @param change the change to save
     * 
     * @throws CmsException 
     */
    protected void saveInternal(String entryPoint, CmsSitemapChange change) throws CmsException {

        switch (change.getChangeType()) {
            case delete:
                delete(change);
                break;
            case remove:
                removeEntryFromNavigation(change);
                break;
            case undelete:
                undelete(change);
                break;
            default:
                applyChange(entryPoint, change);
        }
        setClipboardData(change.getClipBoardData());
    }

    /**
     * Converts a sequence of properties to a map of client-side property beans.<p>
     * 
     * @param props the sequence of properties 
     * @return the map of client properties 
     */
    Map<String, CmsClientProperty> createClientProperties(Iterable<CmsProperty> props) {

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        for (CmsProperty prop : props) {
            CmsClientProperty clientProp = createClientProperty(prop);
            result.put(prop.getName(), clientProp);
        }
        return result;
    }

    /**
     * Gets the properties of a resource as a map of client properties.<p>
     * 
     * @param cms the CMS context to use 
     * @param res the resource whose properties to read
     * @param search true if the inherited properties should be read
     *  
     * @return the client properties as a map
     * 
     * @throws CmsException if something goes wrong 
     */
    Map<String, CmsClientProperty> getClientProperties(CmsObject cms, CmsResource res, boolean search)
    throws CmsException {

        List<CmsProperty> props = cms.readPropertyObjects(res, search);
        Map<String, CmsClientProperty> result = createClientProperties(props);
        return result;
    }

    /**
     * Applys the given change to the VFS.<p>
     * 
     * @param entryPoint the sitemap uri
     * @param change the change
     * @throws CmsException if something goes wrong
     */
    private void applyChange(String entryPoint, CmsSitemapChange change /*, Map<CmsUUID, CmsResource> entryFolders */)
    throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource configFile = null;
        // lock the config file first, to avoid half done changes
        if (change.hasDetailPageInfos()) {
            CmsSitemapConfigurationData sitemapConfig = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
                cms,
                cms.getRequestContext().addSiteRoot(entryPoint));
            CmsConfigurationSourceInfo srcInfo = sitemapConfig.getLastSource();
            if (!srcInfo.isModuleConfiguration() && (srcInfo.getResource() != null)) {
                configFile = srcInfo.getResource();
            }
            ensureLock(configFile);
        }
        if (change.isNew()) {
            createNewEntry(entryPoint, change);
        } else if (change.getEntryId() != null) {
            modifyEntry(change);
        }
        if (change.hasDetailPageInfos() && (configFile != null)) {
            saveDetailPages(change.getDetailPageInfos(), configFile);
            tryUnlock(configFile);
        }
    }

    /**
     * Calculates the navPos value for the given target position.<p>
     * 
     * @param entryFolder the folder to position in
     * @param targetPosition the target position
     * 
     * @return the navPos value
     */
    private float calculateNavPosition(CmsResource entryFolder, int targetPosition/*,
                                                                                  Map<CmsUUID, CmsResource> entryFolders */) {

        CmsObject cms = getCmsObject();
        String parentPath = CmsResource.getParentFolder(cms.getSitePath(entryFolder));
        List<CmsJspNavElement> navElements = getNavBuilder().getNavigationForFolder(parentPath);
        if (navElements.size() == 0) {
            return 10;
        }
        if (navElements.size() <= targetPosition) {
            CmsJspNavElement last = navElements.get(navElements.size() - 1);
            if (last.getResource().equals(entryFolder)) {
                return last.getNavPosition();
            }
            return last.getNavPosition() + 10;

        }

        float previous = 0;
        float following = 0;
        for (int i = 0; i < navElements.size(); i++) {
            CmsJspNavElement element = navElements.get(i);

            if (element.getResource().equals(entryFolder)) {
                if (i == targetPosition) {
                    return element.getNavPosition();
                }
                targetPosition++;
                if (navElements.size() == targetPosition) {
                    CmsJspNavElement last = navElements.get(navElements.size() - 1);
                    return last.getNavPosition() + 10;
                }
                continue;
            }
            if (i == targetPosition - 1) {
                previous = element.getNavPosition();
            }
            if (i == targetPosition) {
                following = element.getNavPosition();
                break;
            }
        }
        return previous + (following - previous) / 2;
    }

    /**
     * Creates a client property bean from a server-side property.<p>
     * 
     * @param prop the property from which to create the client property
     *  
     * @return the new client property
     */
    private CmsClientProperty createClientProperty(CmsProperty prop) {

        return new CmsClientProperty(prop.getName(), prop.getStructureValue(), prop.getResourceValue());
    }

    /**
     * Creates a new page in navigation.<p>
     * 
     * @param entryPoint the site-map uri
     * @param change the new change
     * @throws CmsException if something goes wrong
     */
    private void createNewEntry(String entryPoint, CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        if (change.getParentId() != null) {
            CmsResource parentFolder = cms.readResource(change.getParentId());
            String entryPath = "";
            CmsResource entryFolder = null;
            if ("xmlredirect".equals(OpenCms.getResourceManager().getResourceType(change.getNewResourceTypeId()).getTypeName())) {
                entryPath = CmsStringUtil.joinPaths(cms.getSitePath(parentFolder), change.getName());
            } else {
                String entryFolderPath = CmsStringUtil.joinPaths(cms.getSitePath(parentFolder), change.getName() + "/");
                entryFolder = new CmsResource(
                    change.getEntryId(),
                    new CmsUUID(),
                    entryFolderPath,
                    CmsResourceTypeFolder.getStaticTypeId(),
                    true,
                    0,
                    cms.getRequestContext().getCurrentProject().getUuid(),
                    CmsResource.STATE_NEW,
                    System.currentTimeMillis(),
                    cms.getRequestContext().getCurrentUser().getId(),
                    System.currentTimeMillis(),
                    cms.getRequestContext().getCurrentUser().getId(),
                    CmsResource.DATE_RELEASED_DEFAULT,
                    CmsResource.DATE_EXPIRED_DEFAULT,
                    1,
                    0,
                    System.currentTimeMillis(),
                    0);
                entryFolder = cms.createResource(entryFolderPath, entryFolder, null, generateInheritProperties(
                    change,
                    entryFolder));
                entryPath = CmsStringUtil.joinPaths(entryFolderPath, "index.html");
            }
            byte[] content = null;
            if (change.getNewCopyResourceId() != null) {
                CmsResource copyPage = cms.readResource(change.getNewCopyResourceId());
                content = cms.readFile(copyPage).getContents();
            }
            CmsResource newRes = cms.createResource(
                entryPath,
                change.getNewResourceTypeId(),
                content,
                generateOwnProperties(change));
            if (entryFolder != null) {
                tryUnlock(entryFolder);
            }
            tryUnlock(newRes);

        }
    }

    /**
     * Creates a new resource info to a given configuration item.<p>
     * 
     * @param cms the current CMS context 
     * @param configItem the configuration item
     * 
     * @return the new resource info
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsNewResourceInfo createNewResourceInfo(CmsObject cms, CmsConfigurationItem configItem)
    throws CmsException {

        int typeId = configItem.getSourceFile().getTypeId();
        String name = OpenCms.getResourceManager().getResourceType(typeId).getTypeName();
        String title = cms.readPropertyObject(configItem.getSourceFile(), CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        String description = cms.readPropertyObject(
            configItem.getSourceFile(),
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue();

        CmsNewResourceInfo info = new CmsNewResourceInfo(
            typeId,
            name,
            title,
            description,
            configItem.getSourceFile().getStructureId());
        return info;
    }

    /**
     * Creates a resource type info bean for a given resource type.<p>
     * 
     * @param resType the resource type
     * @param copyResourceId the structure id of the copy resource
     *  
     * @return the resource type info bean
     */
    private CmsNewResourceInfo createResourceTypeInfo(I_CmsResourceType resType, CmsUUID copyResourceId) {

        String name = resType.getTypeName();
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        if (locale == null) {
            locale = new Locale("en");
        }
        String title = CmsWorkplaceMessages.getResourceTypeName(locale, name);
        return new CmsNewResourceInfo(resType.getTypeId(), name, title, null, copyResourceId);
    }

    /**
     * Deletes a resource according to the change data.<p>
     * 
     * @param change the change data
     * 
     * @throws CmsException if something goes wrong
     */
    private void delete(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource(change.getEntryId());
        ensureLock(resource);
        cms.deleteResource(cms.getSitePath(resource), CmsResource.DELETE_PRESERVE_SIBLINGS);
        tryUnlock(resource);
    }

    /**
     * Locks the given resource with a temporary, if not already locked by the current user.
     * Will throw an exception if the resource could not be locked for the current user.<p>
     * 
     * @param resource the resource to lock
     * 
     * @throws CmsException if the resource could not be locked
     */
    private void ensureLock(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsLock lock = cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            cms.lockResourceTemporary(resource);
        }
    }

    /**
     * Generates a client side lock info object representing the current lock state of the given resource.<p>
     * 
     * @param resource the resource
     * @return the client lock
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsClientLock generateClientLock(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsLock lock = cms.getLock(resource);
        CmsClientLock clientLock = new CmsClientLock();
        clientLock.setLockType(CmsClientLock.LockType.valueOf(lock.getType().getMode()));
        CmsUUID ownerId = lock.getUserId();
        if (!lock.isUnlocked() && (ownerId != null)) {
            clientLock.setLockOwner(cms.readUser(ownerId).getDisplayName(cms, cms.getRequestContext().getLocale()));
            clientLock.setOwnedByUser(cms.getRequestContext().getCurrentUser().getId().equals(ownerId));
        }
        return clientLock;
    }

    /**
     * Generates a list of property object to save to the sitemap entry folder to apply the given change.<p>
     * 
     * @param change the change
     * @param entryFolder the entry folder
     * 
     * @return the property objects
     */
    private List<CmsProperty> generateInheritProperties(CmsSitemapChange change, CmsResource entryFolder) {

        List<CmsProperty> result = new ArrayList<CmsProperty>();
        Map<String, CmsClientProperty> clientProps = change.getOwnInternalProperties();
        if (clientProps != null) {
            for (CmsClientProperty clientProp : clientProps.values()) {
                CmsProperty prop = new CmsProperty(
                    clientProp.getName(),
                    clientProp.getStructureValue(),
                    clientProp.getResourceValue());
                result.add(prop);
            }
        }

        //result.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT, change.getTitle(), change.getTitle()));
        if (change.hasChangedPosition()) {
            float navPos = calculateNavPosition(entryFolder, change.getPosition()/*, entryFolders */);
            result.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVPOS, String.valueOf(navPos), null));
        }
        return result;
    }

    /**
     * Generates a list of property object to save to the sitemap entry resource to apply the given change.<p>
     * 
     * @param change the change
     * 
     * @return the property objects
     */
    private List<CmsProperty> generateOwnProperties(CmsSitemapChange change) {

        List<CmsProperty> result = new ArrayList<CmsProperty>();

        Map<String, CmsClientProperty> clientProps = change.getDefaultFileProperties();
        if (clientProps != null) {
            for (CmsClientProperty clientProp : clientProps.values()) {
                CmsProperty prop = new CmsProperty(
                    clientProp.getName(),
                    clientProp.getStructureValue(),
                    clientProp.getResourceValue());
                result.add(prop);
            }
        }
        return result;
    }

    /**
     * Generates a list of property values inherited to the site-map root entry.<p>
     * 
     * @param propertyConfig the property configuration
     * @param root the root entry name
     * 
     * @return the list of property values inherited to the site-map root entry
     * 
     * @throws CmsException if something goes wrong reading the properties
     */
    private Map<String, CmsClientProperty> generateParentProperties(
        Map<String, CmsXmlContentProperty> propertyConfig,
        String root) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("");
        String rootRootPath = cms.getRequestContext().addSiteRoot(root);
        String parentRootPath = CmsResource.getParentFolder(rootRootPath);

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        if (parentRootPath != null) {
            List<CmsProperty> props = rootCms.readPropertyObjects(parentRootPath, true);
            for (CmsProperty prop : props) {
                CmsClientProperty clientProp = createClientProperty(prop);
                result.put(clientProp.getName(), clientProp);
            }
        }
        return result;
    }

    /**
     * Helper method for converting a map which maps sitemap entries to resources to a list of "broken link" beans,
     * which have beans representing the source of the corresponding link as children.<p>  
     * 
     * @param linkMap a multimap from sitemap entries to resources  
     * 
     * @return a list of beans representing links which will be broken 
     * 
     * @throws CmsException if something goes wrong 
     */
    @SuppressWarnings("unchecked")
    private List<CmsSitemapBrokenLinkBean> getBrokenLinkBeans(MultiValueMap linkMap) throws CmsException {

        List<CmsSitemapBrokenLinkBean> result = new ArrayList<CmsSitemapBrokenLinkBean>();
        for (CmsResource entry : (Set<CmsResource>)linkMap.keySet()) {
            CmsSitemapBrokenLinkBean parentBean = createSitemapBrokenLinkBean(entry);
            result.add(parentBean);
            Collection<CmsResource> values = linkMap.getCollection(entry);
            for (CmsResource resource : values) {
                CmsSitemapBrokenLinkBean childBean = createSitemapBrokenLinkBean(resource);
                parentBean.addChild(childBean);
            }
        }
        return result;
    }

    /**
     * Returns the sitemap children for the given path with all descendants up to the given level, ie. 
     * <dl><dt>levels=1</dt><dd>only children</dd><dt>levels=2</dt><dd>children and great children</dd></dl>
     * and so on.<p>
     * @param root the site relative root
     * @param levels the levels to recurse
     * @param propertyConfig the property configuration for sitemaps 
     * 
     * @return the sitemap children
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<CmsClientSitemapEntry> getChildren(
        String root,
        int levels,
        Map<String, CmsXmlContentProperty> propertyConfig) throws CmsException {

        List<CmsClientSitemapEntry> children = new ArrayList<CmsClientSitemapEntry>();
        int i = 0;
        for (CmsJspNavElement navElement : getNavBuilder().getNavigationForFolder(root, true)) {
            CmsClientSitemapEntry child = toClientEntry(navElement, propertyConfig, false);
            if (child != null) {
                child.setPosition(i);
                children.add(child);
                if (child.isFolderType() && ((levels > 1) || (levels == -1)) && !isSubSitemap(navElement)) {
                    child.setSubEntries(getChildren(child.getSitePath(), levels - 1, propertyConfig));
                    child.setChildrenLoadedInitially();
                }
                i++;
            }
        }
        return children;
    }

    /**
     * Returns the cached clipboard data, creating it if it doesn't already exist.<p>
     * 
     * @return the cached clipboard data
     */
    private CmsSitemapClipboardData getClipboardData() {

        CmsSitemapClipboardData cache = (CmsSitemapClipboardData)getRequest().getSession().getAttribute(
            SESSION_ATTR_ADE_SITEMAP_CLIPBOARD_CACHE);
        if (cache == null) {
            cache = new CmsSitemapClipboardData();
            getRequest().getSession().setAttribute(SESSION_ATTR_ADE_SITEMAP_CLIPBOARD_CACHE, cache);
        } else {
            CmsObject cms = getCmsObject();
            Iterator<CmsClientSitemapEntry> modIt = cache.getModifications().values().iterator();
            while (modIt.hasNext()) {
                CmsClientSitemapEntry modEntry = modIt.next();
                try {
                    CmsResource res = cms.readResource(modEntry.getId());
                    // make sure to have the proper site-path
                    modEntry.setSitePath(CmsResource.getFolderPath(cms.getSitePath(res)));
                    if (!getNavBuilder().getNavigationForResource(modEntry.getSitePath()).isInNavigation()) {
                        // remove non visible entries
                        modIt.remove();
                    }
                } catch (CmsException e) {
                    // remove entries where the resource can not be read
                    modIt.remove();
                }
            }

            Iterator<CmsClientSitemapEntry> delIt = cache.getDeletions().values().iterator();
            while (delIt.hasNext()) {
                CmsClientSitemapEntry delEntry = delIt.next();
                try {
                    CmsResource res = cms.readResource(delEntry.getId(), CmsResourceFilter.ALL);
                    if (!res.getState().isDeleted()) {
                        // make sure resource is still deleted
                        delIt.remove();
                    } else {
                        // make sure to have the proper site-path
                        delEntry.setSitePath(CmsResource.getFolderPath(cms.getSitePath(res)));
                    }
                } catch (CmsException e) {
                    // remove entries where the resource can not be read
                    delIt.remove();
                }
            }

        }
        return cache;
    }

    /**
     * Returns the default template for the given sitemap.<p>
     * 
     * @param entryPoint the sitemap URI
     * 
     * @return the default template
     * 
     * @throws CmsRpcException if something goes wrong
     */
    private CmsSitemapTemplate getDefaultTemplate(String entryPoint) throws CmsRpcException {

        return null;

    }

    /**
     * Reads all descendant resources to the given site-map entry resource.<p>
     * 
     * @param res the site-map entry resource
     * 
     * @return the descendant resources
     */
    private List<CmsResource> getDescendants(CmsResource res) {

        if (res.isFile()) {
            return Collections.<CmsResource> emptyList();
        }
        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsJspNavElement element : getNavBuilder().getNavigationForFolder(getCmsObject().getSitePath(res))) {
            result.add(element.getResource());
            result.addAll(getDescendants(element.getResource()));
        }
        return result;
    }

    /**
     * Gets the type id for entry point folders.<p>
     * 
     * @return the type id for entry point folders 
     * 
     * @throws CmsException if something goes wrong 
     */
    private int getEntryPointType() throws CmsException {

        return OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolderExtended.TYPE_ENTRY_POINT).getTypeId();
    }

    /**
     * Returns a navigation builder reference.<p>
     * 
     * @return the navigation builder
     */
    private CmsJspNavBuilder getNavBuilder() {

        if (m_navBuilder == null) {
            m_navBuilder = new CmsJspNavBuilder(getCmsObject());
        }
        return m_navBuilder;
    }

    /**
     * Returns the new resource infos.<p>
     * 
     * @param cms the current CMS context 
     * @param entryPoint the sitemap entry-point
     * 
     * @return the new resource infos
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<CmsNewResourceInfo> getNewResourceInfos(CmsObject cms, String entryPoint) throws CmsException {

        List<CmsNewResourceInfo> result = new ArrayList<CmsNewResourceInfo>();
        CmsSitemapConfigurationData config = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(entryPoint));
        for (CmsConfigurationItem configItem : config.getNewElements()) {
            result.add(createNewResourceInfo(cms, configItem));
        }

        return result;
    }

    /**
     * Converts a list of properties to a map.<p>
     * 
     * @param props the list of properties 
     * 
     * @return a map from property names to properties 
     */
    private Map<String, CmsProperty> getPropertiesByName(List<CmsProperty> props) {

        Map<String, CmsProperty> result = new HashMap<String, CmsProperty>();
        for (CmsProperty prop : props) {
            String key = prop.getName();
            result.put(key, prop.clone());
        }
        return result;
    }

    /**
     * Gets the names of all available properties.<p>
     * 
     * @param cms the CMS context 
     * @return the list of all property names 
     *  
     * @throws CmsException
     */
    private List<String> getPropertyNames(CmsObject cms) throws CmsException {

        List<CmsPropertyDefinition> propDefs = cms.readAllPropertyDefinitions();
        List<String> result = new ArrayList<String>();
        for (CmsPropertyDefinition propDef : propDefs) {
            result.add(propDef.getName());
        }
        return result;
    }

    /**
     * Gets the resource type info beans for types for which new detail pages can be created.<p>
     * 
     * @param cms the current CMS context 
     * @param entryPoint the sitemap entry-point 
     * @param copyResourceId the structure id of the copy resource
     * 
     * @return the resource type info beans for types for which new detail pages can be created 
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<CmsNewResourceInfo> getResourceTypeInfos(CmsObject cms, String entryPoint, CmsUUID copyResourceId)
    throws CmsException {

        List<CmsNewResourceInfo> result = new ArrayList<CmsNewResourceInfo>();
        CmsContainerPageConfigurationData configData = OpenCms.getADEConfigurationManager().getContainerPageConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(entryPoint));
        Map<String, CmsConfigurationItem> typeConfig = configData.getTypeConfiguration();
        for (CmsConfigurationItem item : typeConfig.values()) {
            CmsResource sourceFile = item.getSourceFile();
            I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(sourceFile.getTypeId());
            CmsNewResourceInfo info = createResourceTypeInfo(resourceType, copyResourceId);
            result.add(info);
        }
        return result;
    }

    /**
     * Reeds the site root entry.<p>
     * 
     * @param entryPoint the entry point 
     * 
     * @param propertyConfig the property configuration
     * 
     * @return the site root entry
     * 
     * @throws CmsSecurityException in case of insufficient permissions
     * @throws CmsException if something goes wrong
     */
    private CmsClientSitemapEntry getRootEntry(String entryPoint, Map<String, CmsXmlContentProperty> propertyConfig)
    throws CmsSecurityException, CmsException {

        CmsJspNavElement navElement = getNavBuilder().getNavigationForResource(entryPoint);

        CmsClientSitemapEntry result = toClientEntry(navElement, propertyConfig, true);
        if (result != null) {
            result.setPosition(0);
            result.setChildrenLoadedInitially();
            result.setSubEntries(getChildren(entryPoint, 1, propertyConfig));
        }
        return result;
    }

    /**
     * Returns a bean representing the given template resource.<p>
     * 
     * @param cms the cms context to use for VFS operations
     * @param resource the template resource
     * 
     * @return bean representing the given template resource
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsSitemapTemplate getTemplateBean(CmsObject cms, CmsResource resource) throws CmsException {

        CmsProperty titleProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false);
        CmsProperty descProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        CmsProperty imageProp = cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_ADE_TEMPLATE_IMAGE,
            false);
        return new CmsSitemapTemplate(
            titleProp.getValue(),
            descProp.getValue(),
            cms.getSitePath(resource),
            imageProp.getValue());
    }

    private boolean hasDefaultFileChanges(CmsSitemapChange change) {

        return (change.getDefaultFileId() != null) && !change.isNew();
        //TODO: optimize this!
    }

    /**
     * Returns if the sitemap entry folder needs to be modified to apply the change.<p>
     * 
     * @param change the change to apply
     * 
     * @return <code>true</code> if the sitemap entry folder needs to be modified to apply the change
     */
    private boolean hasFolderChanges(CmsSitemapChange change) {

        return !change.isNew()
            && !change.isLeafType()
            && (change.hasChangedPosition() || change.hasChangedName() || change.hasChangedInheritProperties());
    }

    private boolean hasOwnChanges(CmsSitemapChange change) {

        return !change.isNew();
        //TODO: optimize this! 
    }

    /**
     * Returns if the sitemap entry resource needs to be modified apply the change.<p>
     * 
     * @param change the change to apply
     * 
     * @return <code>true</code> if the sitemap entry resource needs to be modified to apply the change
     */
    private boolean hasPageChanges(CmsSitemapChange change) {

        return !change.isNew() && (change.hasChangedProperties());
    }

    /**
     * Checks if the toolbar should be displayed.<p>
     * 
     * @param request the current request to get the default locale from 
     * 
     * @return <code>true</code> if the toolbar should be displayed
     */
    private boolean isDisplayToolbar(HttpServletRequest request) {

        // display the toolbar by default
        boolean displayToolbar = true;
        if (CmsHistoryResourceHandler.isHistoryRequest(request)) {
            // we do not want to display the toolbar in case of an historical request
            displayToolbar = false;
        }
        return displayToolbar;
    }

    /**
     * Returns if the given nav-element resembles a sub-sitemap entry-point.<p>
     * 
     * @param navElement the nav-element
     * 
     * @return <code>true</code> if the given nav-element resembles a sub-sitemap entry-point.<p>
     * @throws CmsException if something goes wrong
     */
    private boolean isSubSitemap(CmsJspNavElement navElement) throws CmsException {

        return navElement.getResource().getTypeId() == getEntryPointType();
    }

    /**
     * Applys the given changes to the entry.<p>
     * 
     * @param change the change to apply
     * 
     * @throws CmsException if something goes wrong
     */
    private void modifyEntry(CmsSitemapChange change /*, Map<CmsUUID, CmsResource> entryFolders */)
    throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource entryPage = null;
        CmsResource entryFolder = null;

        CmsResource ownRes = null;
        CmsResource defaultFileRes = null;
        try {
            // lock all resources necessary first to avoid doing changes only half way through

            if (hasOwnChanges(change)) {
                ownRes = cms.readResource(change.getEntryId());
                ensureLock(ownRes);
            }

            if (hasDefaultFileChanges(change)) {
                defaultFileRes = cms.readResource(change.getDefaultFileId());
                ensureLock(defaultFileRes);
            }

            if ((ownRes != null) && ownRes.isFolder()) {
                entryFolder = ownRes;
            }

            if ((ownRes != null) && ownRes.isFile()) {
                entryPage = ownRes;
            }

            if (defaultFileRes != null) {
                entryPage = defaultFileRes;
            }

            if (change.isLeafType()) {
                entryFolder = entryPage;
            }

            updateProperties(cms, ownRes, defaultFileRes, change.getPropertyChanges());
            if (change.hasChangedPosition()) {
                updateNavPos(ownRes, change);
            }

            if (entryFolder != null) {
                if (change.hasNewParent() || change.hasChangedName()) {
                    String destinationPath;
                    if (change.hasNewParent()) {
                        CmsResource futureParent = cms.readResource(change.getParentId());
                        destinationPath = CmsStringUtil.joinPaths(cms.getSitePath(futureParent), change.getName());
                    } else {
                        destinationPath = CmsStringUtil.joinPaths(
                            CmsResource.getParentFolder(cms.getSitePath(entryFolder)),
                            change.getName());
                    }
                    if (change.isLeafType() && destinationPath.endsWith("/")) {
                        destinationPath = destinationPath.substring(0, destinationPath.length() - 1);
                    }
                    // only if the site-path has really changed
                    if (!cms.getSitePath(entryFolder).equals(destinationPath)) {
                        cms.moveResource(cms.getSitePath(entryFolder), destinationPath);
                    }
                    entryFolder = cms.readResource(entryFolder.getStructureId());

                }
                //cms.writePropertyObjects(cms.getSitePath(entryFolder), generateInheritProperties(change, entryFolder));
            }
        } finally {
            if (entryPage != null) {
                tryUnlock(entryPage);
            }
            if (entryFolder != null) {
                tryUnlock(entryFolder);
            }
        }
    }

    /**
     * Applys the given remove change.<p>
     * 
     * @param change the change to apply
     * 
     * @throws CmsException if something goes wrong
     */
    private void removeEntryFromNavigation(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource entryFolder = cms.readResource(change.getEntryId());
        ensureLock(entryFolder);
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(new CmsProperty(
            CmsPropertyDefinition.PROPERTY_NAVTEXT,
            CmsProperty.DELETE_VALUE,
            CmsProperty.DELETE_VALUE));
        properties.add(new CmsProperty(
            CmsPropertyDefinition.PROPERTY_NAVPOS,
            CmsProperty.DELETE_VALUE,
            CmsProperty.DELETE_VALUE));
        cms.writePropertyObjects(cms.getSitePath(entryFolder), properties);
        tryUnlock(entryFolder);
    }

    /**
     * Saves the detail page information of a sitemap to the sitemap's configuration file.<p>
     * 
     * @param detailPages
     * @param resource
     * @throws CmsException
     */
    private void saveDetailPages(List<CmsDetailPageInfo> detailPages, CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsDetailPageConfigurationWriter writer = new CmsDetailPageConfigurationWriter(cms, resource);
        writer.updateAndSave(detailPages);
    }

    /**
     * Saves the given clipboard data to the session.<p>
     * 
     * @param clipboardData the clipboard data to save
     */
    private void setClipboardData(CmsSitemapClipboardData clipboardData) {

        if (clipboardData != null) {
            getRequest().getSession().setAttribute(SESSION_ATTR_ADE_SITEMAP_CLIPBOARD_CACHE, clipboardData);
        }
    }

    /**
     * Converts a jsp navigation element into a client sitemap entry.<p>
     * 
     * @param navElement the jsp navigation element
     * @param propertyConfig the property configuration for sitemaps 
     * @param isRoot true if the entry is a root entry
     * 
     * @return the client sitemap entry
     * @throws CmsException 
     */
    private CmsClientSitemapEntry toClientEntry(
        CmsJspNavElement navElement,
        Map<String, CmsXmlContentProperty> propertyConfig,
        boolean isRoot) throws CmsException {

        CmsResource entryPage = null;
        CmsObject cms = getCmsObject();
        CmsClientSitemapEntry clientEntry = new CmsClientSitemapEntry();
        CmsResource entryFolder = null;

        CmsResource ownResource = navElement.getResource();
        CmsResource defaultFileResource = null;
        if (ownResource.isFolder()) {
            defaultFileResource = cms.readDefaultFile(ownResource);
        }

        Map<String, CmsClientProperty> ownProps = getClientProperties(cms, ownResource, false);

        Map<String, CmsClientProperty> defaultFileProps = null;
        if (defaultFileResource != null) {
            defaultFileProps = getClientProperties(cms, defaultFileResource, false);
            clientEntry.setDefaultFileId(defaultFileResource.getStructureId());
        } else {
            defaultFileProps = new HashMap<String, CmsClientProperty>();
        }

        clientEntry.setId(ownResource.getStructureId());
        if (navElement.getResource().isFolder()) {
            entryFolder = navElement.getResource();
            entryPage = defaultFileResource;
            clientEntry.setName(entryFolder.getName());
            if (entryPage == null) {
                entryPage = entryFolder;
            }
            if (!isRoot && isSubSitemap(navElement)) {
                clientEntry.setEntryType(EntryType.subSitemap);
            }
        } else {
            entryPage = navElement.getResource();
            clientEntry.setName(entryPage.getName());
            clientEntry.setEntryType(EntryType.leaf);
        }

        String path = cms.getSitePath(entryPage);
        if (entryFolder != null) {
            CmsLock folderLock = cms.getLock(entryFolder);
            clientEntry.setHasForeignFolderLock(!folderLock.isUnlocked()
                && !folderLock.isOwnedBy(cms.getRequestContext().getCurrentUser()));
        }

        clientEntry.setVfsPath(path);
        navElement.getProperties();

        clientEntry.setOwnProperties(ownProps);
        clientEntry.setDefaultFileProperties(defaultFileProps);

        clientEntry.setSitePath(entryFolder != null ? cms.getSitePath(entryFolder) : path);
        //CHECK: assuming that, if entryPage refers to the default file, the lock state of the folder 
        clientEntry.setLock(generateClientLock(entryPage));
        clientEntry.setInNavigation(isRoot || navElement.isInNavigation());
        return clientEntry;

    }

    /**
     * Tries to unlock a resource.<p>
     * 
     * @param resource the resource to unlock
     */
    private void tryUnlock(CmsResource resource) {

        try {
            getCmsObject().unlockResource(resource);
        } catch (CmsException e) {
            LOG.debug("Unable to unlock " + resource.getRootPath(), e);
        }
    }

    /**
     * Un-deletes a resource according to the change data.<p>
     * 
     * @param change the change data
     * 
     * @throws CmsException if something goes wrong
     */
    private void undelete(CmsSitemapChange change) throws CmsException {

        CmsResource deleted = getCmsObject().readResource(change.getEntryId(), CmsResourceFilter.ALL);
        if (deleted.getState().isDeleted()) {
            ensureLock(deleted);
            getCmsObject().undeleteResource(getCmsObject().getSitePath(deleted), true);
            tryUnlock(deleted);
        }

    }

    /**
     * Updates the navigation position for a resource.<p>
     * 
     * @param res the resource for which to update the navigation position
     *  
     * @param change the sitemap change
     *  
     * @throws CmsException if something goes wrong 
     */
    private void updateNavPos(CmsResource res, CmsSitemapChange change) throws CmsException {

        if (change.hasChangedPosition()) {
            CmsObject cms = getCmsObject();
            float navPos = calculateNavPosition(res, change.getPosition()/*, entryFolders */);
            CmsProperty navpos = new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVPOS, String.valueOf(navPos), null);
            cms.writePropertyObjects(res, Collections.singletonList(navpos));
        }
    }

    /**
     * Updates properties for a resource and possibly its detail page.<p>
     *     
     * @param cms the CMS context 
     * @param ownRes the resource 
     * @param defaultFileRes the default file resource (possibly null) 
     * 
     * @param propertyModifications the property modifications 
     * 
     * @throws CmsException if something goes wrong 
     */
    private void updateProperties(
        CmsObject cms,
        CmsResource ownRes,
        CmsResource defaultFileRes,
        List<CmsPropertyModification> propertyModifications) throws CmsException {

        //TODO: clean this method up

        Map<String, CmsProperty> ownProps = getPropertiesByName(cms.readPropertyObjects(ownRes, false));
        Map<String, CmsProperty> defaultFileProps = null;
        if (defaultFileRes != null) {
            defaultFileProps = getPropertiesByName(cms.readPropertyObjects(defaultFileRes, false));
        }
        Map<CmsUUID, Map<String, CmsProperty>> propsMap = new HashMap<CmsUUID, Map<String, CmsProperty>>();
        propsMap.put(ownRes.getStructureId(), ownProps);
        if ((defaultFileProps != null) && (defaultFileRes != null)) {
            propsMap.put(defaultFileRes.getStructureId(), defaultFileProps);
        }
        for (CmsPropertyModification propMod : propertyModifications) {
            Map<String, CmsProperty> propsToModify = propsMap.get(propMod.getId());
            CmsProperty propToModify = propsToModify.get(propMod.getName());
            if (propToModify == null) {
                propToModify = new CmsProperty(propMod.getName(), null, null);
            }
            String newValue = propMod.getValue();
            if (newValue == null) {
                newValue = "";
            }
            if (propMod.isStructureValue()) {
                propToModify.setStructureValue(newValue);
            } else {
                propToModify.setResourceValue(newValue);
            }
            CmsResource target = ownRes.getStructureId().equals(propMod.getId()) ? ownRes : defaultFileRes;
            cms.writePropertyObjects(target, Collections.singletonList(propToModify));
        }

    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/Attic/CmsSitemapService.java,v $
 * Date   : $Date: 2010/12/17 08:45:29 $
 * Version: $Revision: 1.45 $
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

import org.opencms.ade.sitemap.shared.CmsBrokenLinkData;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsResourceTypeInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapBrokenLinkBean;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapMergeInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapSaveData;
import org.opencms.ade.sitemap.shared.CmsSitemapTemplate;
import org.opencms.ade.sitemap.shared.CmsSubSitemapInfo;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlSitemap;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsConfigurationFileFinder;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.sitemap.CmsDetailPageConfigurationWriter;
import org.opencms.xml.sitemap.CmsDetailPageInfo;
import org.opencms.xml.sitemap.CmsDetailPageTable;
import org.opencms.xml.sitemap.CmsInternalSitemapEntry;
import org.opencms.xml.sitemap.CmsSitemapBean;
import org.opencms.xml.sitemap.CmsSitemapChangeDelete;
import org.opencms.xml.sitemap.CmsSitemapChangeEdit;
import org.opencms.xml.sitemap.CmsSitemapChangeNew;
import org.opencms.xml.sitemap.CmsSitemapChangeNewSubSitemapEntry;
import org.opencms.xml.sitemap.CmsSitemapEntry;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.CmsSitemapRuntimeInfo;
import org.opencms.xml.sitemap.CmsXmlSitemap;
import org.opencms.xml.sitemap.CmsXmlSitemapFactory;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.map.MultiValueMap;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.45 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public class CmsSitemapService extends CmsGwtService implements I_CmsSitemapService {

    /** Serialization uid. */
    private static final long serialVersionUID = -7136544324371767330L;

    /** Session attribute name constant. */
    private static final String SESSION_ATTR_ADE_SITEMAP_CLIPBOARD_CACHE = "__OCMS_ADE_SITEMAP_CLIPBOARD_CACHE__";

    /**
     * Returns a new configured service instance.<p>
     * 
     * @param request the current request
     * 
     * @return a new service instance
     */
    public static CmsSitemapService newInstance(HttpServletRequest request) {

        CmsSitemapService srv = new CmsSitemapService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        return srv;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getBrokenLinksToSitemapEntries(CmsClientSitemapEntry, java.util.List, java.util.List)
     */
    public CmsBrokenLinkData getBrokenLinksToSitemapEntries(
        CmsClientSitemapEntry deleteEntry,
        List<CmsUUID> open,
        List<CmsUUID> closed) throws CmsRpcException {

        CmsBrokenLinkData result = null;
        try {
            CmsObject cms = getCmsObject();
            List<CmsInternalSitemapEntry> entries = getEntriesForIds(cms, open);
            List<CmsInternalSitemapEntry> closedEntries = getEntriesForIds(cms, closed);
            CmsSitemapManager sitemapManager = OpenCms.getSitemapManager();
            List<CmsInternalSitemapEntry> descendants = sitemapManager.getDescendants(closedEntries, true);
            entries.addAll(descendants);

            // multimap from resources to (sets of) sitemap entries 

            MultiValueMap linkMap = MultiValueMap.decorate(
                new HashMap<Object, Object>(),
                FactoryUtils.instantiateFactory(HashSet.class));
            for (CmsInternalSitemapEntry entry : entries) {
                List<CmsResource> linkSources = getLinkSources(cms, entry.getId());
                for (CmsResource source : linkSources) {
                    linkMap.put(entry, source);
                }
            }

            List<CmsClientSitemapEntry> closedEntryTrees = new ArrayList<CmsClientSitemapEntry>();
            for (CmsUUID id : closed) {
                closedEntryTrees.add(getSubTree(cms, sitemapManager.getEntryForId(cms, id).getSitePath(cms)));
            }
            result = new CmsBrokenLinkData();
            result.setBrokenLinks(getBrokenLinkBeans(linkMap));
            result.setClosedEntries(closedEntryTrees);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getChildren(java.lang.String, java.lang.String)
     */
    public List<CmsClientSitemapEntry> getChildren(String sitemapUri, String root) throws CmsRpcException {

        List<CmsClientSitemapEntry> children = null;

        try {
            CmsObject cms = getCmsObject();
            CmsResource sitemapRes = cms.readResource(sitemapUri);
            Map<String, CmsXmlContentProperty> propertyConfig = OpenCms.getSitemapManager().getElementPropertyConfiguration(
                cms,
                sitemapRes,
                true);
            children = getChildren(root, 1, propertyConfig);
        } catch (Throwable e) {
            error(e);
        }
        return children;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getEntry(java.lang.String, java.lang.String)
     */
    public CmsClientSitemapEntry getEntry(String sitemapUri, String root) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            Map<String, CmsXmlContentProperty> propertyConfig = OpenCms.getSitemapManager().getElementPropertyConfiguration(
                cms,
                cms.readResource(sitemapUri),
                true);

            return toClientEntry(OpenCms.getSitemapManager().getEntryForUri(getCmsObject(), root), propertyConfig);
        } catch (Throwable e) {
            error(e);
        }
        return null; // we never get here 
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
     * Merges a sub-sitemap back into its parent sitemap.<p>
     * 
     * @param sitemapUri the URI of the parent sitemap
     * @param path the path in the parent sitemap at which the sub-sitemap should be merged into it 
     * 
     * @return the result of the merge operation
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    public CmsSitemapMergeInfo mergeSubSitemap(String sitemapUri, String path) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            Map<String, CmsXmlContentProperty> propertyConfig = OpenCms.getSitemapManager().getElementPropertyConfiguration(
                cms,
                cms.readResource(sitemapUri),
                true);
            // TODO: what about historical requests?
            CmsInternalSitemapEntry entry = (CmsInternalSitemapEntry)OpenCms.getSitemapManager().getEntryForUri(
                getCmsObject(),
                path);
            List<I_CmsSitemapChange> parentChanges = getChangesForMergingSubSitemap(entry);
            long timestamp = saveChangesInternal(sitemapUri, parentChanges, true);
            List<CmsClientSitemapEntry> mergedClientEntries = getChildren(path, 1, propertyConfig);
            String subSitemapId = entry.getProperties().get(CmsSitemapManager.Property.sitemap.name());
            CmsResource subSitemapRes = cms.readResource(new CmsUUID(subSitemapId));
            String subSitemapUri = cms.getSitePath(subSitemapRes);
            cms.deleteResource(subSitemapUri, CmsResource.DELETE_PRESERVE_SIBLINGS);
            return new CmsSitemapMergeInfo(mergedClientEntries, timestamp);
        } catch (Throwable e) {
            error(e);
        }
        // we can never reach this point
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
            CmsResource sitemap = cms.readResource(sitemapUri);
            CmsProperty configProp = cms.readPropertyObject(
                sitemap,
                CmsPropertyDefinition.PROPERTY_ADE_SITEMAP_CONFIG,
                true);
            String configFilePath = configProp.getValue();

            Map<String, CmsXmlContentProperty> propertyConfig = sitemapMgr.getElementPropertyConfiguration(
                cms,
                sitemap,
                true);

            I_CmsXmlContentHandler contentHandler = CmsXmlContentDefinition.getContentHandlerForResource(cms, sitemap);
            CmsMacroResolver resolver = CmsXmlContentPropertyHelper.getMacroResolverForProperties(cms, contentHandler);
            Map<String, CmsXmlContentProperty> resolvedProps = CmsXmlContentPropertyHelper.resolveMacrosInProperties(
                propertyConfig,
                resolver);
            String parentSitemap = sitemapMgr.getParentSitemap(cms, sitemapUri);
            String openPath = getRequest().getParameter("path");

            String entryPoint = sitemapMgr.getEntryPoint(cms, sitemapUri);
            CmsSitemapEntry entry = sitemapMgr.getEntryForUri(cms, entryPoint);
            CmsSitemapRuntimeInfo sitemapInfo = entry.getSitemapInfo();
            if (entry.hasSubSitemap()) {
                // the entry itself belongs to the parent sitemap, but it has at least one child because of the way
                // we create sub-sitemaps 
                sitemapInfo = ((CmsInternalSitemapEntry)entry).getSubEntries().get(0).getSitemapInfo();
            }
            CmsDetailPageTable detailPages = sitemapInfo.getDetailPageTable();

            Map<String, CmsComputedPropertyValue> parentProperties = new HashMap<String, CmsComputedPropertyValue>(
                entry.getParentComputedProperties());

            String siteRoot = OpenCms.getSiteManager().getSiteRoot(sitemap.getRootPath());
            String exportName = sitemapMgr.getExportnameForSiteRoot(siteRoot);

            String exportRfsPrefix = OpenCms.getStaticExportManager().getDefaultRfsPrefix();

            CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
            boolean isSecure = site.hasSecureServer();
            List<CmsResourceTypeInfo> resourceTypeInfos = new ArrayList<CmsResourceTypeInfo>();
            resourceTypeInfos = getResourceTypeInfos(getCmsObject(), sitemapUri);

            int maxDepth = sitemapMgr.getMaxDepth(cms, sitemap);

            result = new CmsSitemapData(
                getDefaultTemplate(sitemapUri),
                getTemplates(),
                resolvedProps,
                getClipboardData(),
                parentProperties,
                exportName,
                exportRfsPrefix,
                isSecure,
                getNoEditReason(cms, getRequest()),
                shouldDisplayToolbar(getRequest()),
                OpenCms.getResourceManager().getResourceType(CmsResourceTypeXmlContainerPage.getStaticTypeName()).getTypeId(),
                parentSitemap,
                getRoot(sitemap, parentSitemap, openPath, propertyConfig),
                sitemap.getDateLastModified(),
                openPath,
                maxDepth,
                detailPages,
                resourceTypeInfos,
                configFilePath);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#save(java.lang.String, org.opencms.ade.sitemap.shared.CmsSitemapSaveData)
     */
    public long save(String sitemapUri, CmsSitemapSaveData saveData) throws CmsRpcException {

        long timestamp = 0;
        try {
            timestamp = saveInternal(sitemapUri, saveData, true);
        } catch (Throwable e) {
            error(e);
        }
        return timestamp;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#saveAndCreateSubSitemap(java.lang.String, org.opencms.ade.sitemap.shared.CmsSitemapSaveData, java.lang.String)
     */
    public CmsSubSitemapInfo saveAndCreateSubSitemap(String sitemapUri, CmsSitemapSaveData saveData, String path)
    throws CmsRpcException {

        try {
            // don't unlock the sitemap because createSubSitemap still needs to modify it
            saveInternal(sitemapUri, saveData, false);
            return createSubSitemap(sitemapUri, path);
        } catch (Throwable e) {
            error(e);
        }
        return null; // we never reach this line 
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#saveAndMergeSubSitemap(java.lang.String, org.opencms.ade.sitemap.shared.CmsSitemapSaveData, java.lang.String)
     */
    public CmsSitemapMergeInfo saveAndMergeSubSitemap(String sitemapUri, CmsSitemapSaveData saveData, String path)
    throws CmsRpcException {

        try {
            saveInternal(sitemapUri, saveData, false);
            return mergeSubSitemap(sitemapUri, path);
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#saveSync(java.lang.String, org.opencms.ade.sitemap.shared.CmsSitemapSaveData)
     */
    public long saveSync(String sitemapUri, CmsSitemapSaveData saveData) throws CmsRpcException {

        return save(sitemapUri, saveData);
    }

    /**
     * Creates a "broken link" bean based on a sitemap entry.<p>
     * 
     * @param entry a sitemap entry 
     * 
     * @return a "broken link" bean with the data from the sitemap entry 
     * 
     */
    protected CmsSitemapBrokenLinkBean createSitemapBrokenLinkBean(CmsInternalSitemapEntry entry) {

        CmsObject cms = getCmsObject();
        String title = entry.getTitle();
        String path = entry.getSitePath(cms);
        String subtitle = path;
        return new CmsSitemapBrokenLinkBean(title, subtitle);
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
     * Retrieves a list of sitemap entries for a list of sitemap entry ids.<p>
     * 
     * @param cms the CMS context 
     * @param ids the list of sitemap entry ids 
     * 
     * @return the list of sitemap entries 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected List<CmsInternalSitemapEntry> getEntriesForIds(CmsObject cms, List<CmsUUID> ids) throws CmsException {

        List<CmsInternalSitemapEntry> entries = new ArrayList<CmsInternalSitemapEntry>();
        for (CmsUUID id : ids) {
            CmsInternalSitemapEntry entry = (CmsInternalSitemapEntry)OpenCms.getSitemapManager().getEntryForId(cms, id);
            // ignore entries which haven't been saved 
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    /**
     * Gets the resources which link to a given (sitemap or structure) id.<p>
     * 
     * @param cms the current CMS context 
     * @param targetId the relation target id
     *  
     * @return the list of resources which link to the given id
     *  
     * @throws CmsException
     */
    protected List<CmsResource> getLinkSources(CmsObject cms, CmsUUID targetId) throws CmsException {

        CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterStructureId(targetId);
        List<CmsRelation> relations = cms.readRelations(filter);
        List<CmsResource> result = new ArrayList<CmsResource>();

        for (CmsRelation relation : relations) {
            CmsResource source = relation.getSource(cms, CmsResourceFilter.DEFAULT);
            // we have to delete detail pages from the configuration file anyway, so don't warn about it  
            if (!CmsResourceTypeXmlSitemap.isSitemapConfig(source)) {
                result.add(source);
            }
        }
        return result;
    }

    /**
     * Helper method to check whether a client sitemap entry has a reference to a sub-sitemap.<p>
     * 
     * @param entry the entry to check
     * 
     * @return true if the entry has a reference to a sub-sitemap 
     */
    protected boolean hasReferenceToSitemap(CmsClientSitemapEntry entry) {

        return entry.getProperties().get(CmsSitemapManager.Property.sitemap.name()) != null;
    }

    /**
     * Helper method to check whether a sitemap entry has a reference to a sub-sitemap.<p>
     * 
     * @param entry the entry to check
     * 
     * @return true if the entry has a reference to a sub-sitemap 
     */
    protected boolean hasReferenceToSitemap(CmsSitemapEntry entry) {

        return entry.getProperties().get(CmsSitemapManager.Property.sitemap.name()) != null;
    }

    /**
     * Internal method for saving a sitemap.<p>
     * 
     * @param sitemapUri the URI of the sitemap to save
     * @param changes the changes which should be saved
     * @param unlockAfterSave if true, the sitemap will be unlocked after saving
     * 
     * @return the timestamp of the time when the sitemap was saved 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected long saveChangesInternal(String sitemapUri, List<I_CmsSitemapChange> changes, boolean unlockAfterSave)
    throws CmsException {

        long timestamp = 0;
        CmsObject cms = getCmsObject();

        // TODO: what's about historical requests?
        CmsResource sitemap = cms.readResource(sitemapUri);
        CmsXmlSitemap xml = CmsXmlSitemapFactory.unmarshal(cms, sitemap);
        // apply changes

        xml.applyChanges(cms, changes, getRequest());
        // write to VFS
        xml.getFile().setContents(xml.marshal());
        cms.writeFile(xml.getFile());
        // and unlock
        if (unlockAfterSave) {
            cms.unlockResource(sitemapUri);
        }
        timestamp = cms.readResource(sitemap.getStructureId()).getDateLastModified();
        return timestamp;
    }

    /**
     * Helper function for saving sitemap data.<p>
     * 
     * @param sitemapUri the URI of the sitemap 
     * @param saveData the data to be saved 
     * @param unlock if true, unlock the sitemap file after saving 
     * 
     * @return the timestamp of saving the sitemap file
     *  
     * @throws CmsException if something goes wrong 
     */
    protected long saveInternal(String sitemapUri, CmsSitemapSaveData saveData, boolean unlock) throws CmsException {

        long timestamp = saveChangesInternal(sitemapUri, saveData.getChanges(), unlock);
        if (saveData.getClipboardData() != null) {
            setClipboardData(saveData.getClipboardData());
        }
        if (saveData.getDetailPageInfo() != null) {
            saveDetailPages(saveData.getDetailPageInfo(), sitemapUri);
        }
        return timestamp;
    }

    /**
     * Recursively adds child entries to a client sitemap entry, whose depth is either below a maximum value or which lie along a given path.<p>
     * 
     * @param entry the entry to which child entries should be added 
     * @param root the root entry of the sitemap
     * @param path the path along which child entries should be added 
     * @param propConfig the property configuration for converting entries to client entries
     * @param depth the depth up to which children should be added
     * 
     * @throws CmsException if something goes wrong 
     */
    private void addChildrenRecursively(
        CmsClientSitemapEntry entry,
        CmsClientSitemapEntry root,
        String path,
        Map<String, CmsXmlContentProperty> propConfig,
        int depth) throws CmsException {

        CmsObject cms = getCmsObject();
        if (hasReferenceToSitemap(entry) && (entry != root)) {
            return;
        }
        List<CmsClientSitemapEntry> children = new ArrayList<CmsClientSitemapEntry>();
        for (CmsSitemapEntry subEntry : OpenCms.getSitemapManager().getSubEntries(cms, entry.getSitePath())) {
            CmsInternalSitemapEntry internalEntry = (CmsInternalSitemapEntry)subEntry;
            CmsClientSitemapEntry child = toClientEntry(subEntry, propConfig);
            children.add(child);
            if (internalEntry.getSubEntries().isEmpty() || hasReferenceToSitemap(internalEntry)) {
                child.setChildrenLoadedInitially();
            }
            if ((depth > 1) || ((path != null) && path.startsWith(subEntry.getSitePath(cms)))) {
                addChildrenRecursively(child, root, path, propConfig, depth - 1);
            }
        }
        entry.setSubEntries(children);
        //entry.setLoaded(true);

    }

    /**
     * Helper method for creating a 'new entry' change object from a sitemap entry.<p>
     * 
     * @param entry the entry for which the change object should be created 
     * @param entryPoint the entry point of the sitemap in which the sitemap entry should be inserted 
     * 
     * @return a change object 
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsSitemapChangeNew createChangeForNewSitemapEntry(CmsInternalSitemapEntry entry, String entryPoint)
    throws CmsException {

        CmsObject cms = getCmsObject();
        CmsUUID resourceId = entry.getStructureId();

        CmsResource resource = cms.readResource(resourceId);

        CmsSitemapChangeNew result = new CmsSitemapChangeNewSubSitemapEntry(
            entry.getSitePath(cms),
            entry.getPosition(),
            entry.getTitle(),
            cms.getSitePath(resource),
            entry.getNewProperties(),
            entryPoint,
            entry.getId());
        return result;
    }

    /**
     * Creates a dummy root entry for the client.<p>
     * @param entry the internal sitemap entry in the parent sitemap which references the subsitemap
     * @param propertyConfig the property configuration for sitemaps 
     * 
     * @return a dummy root entry for the sub-sitemap with the data from the super-sitemap
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsClientSitemapEntry createDummySubSitemapRoot(
        CmsInternalSitemapEntry entry,
        Map<String, CmsXmlContentProperty> propertyConfig) throws CmsException {

        CmsInternalSitemapEntry cloneEntry = CmsSitemapManager.copyAsSubSitemapRoot(getCmsObject(), entry);
        return toClientEntry(cloneEntry, propertyConfig);
    }

    /**
     * Creates a new, empty  sitemap with a given title and returns the resource object for it.<p>
     * 
     * @param sitemapUri the URI of the current sitemap  
     * 
     * @return the sitemap resource which has been created 
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsResource createNewSitemap(String sitemapUri) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsSitemapManager manager = OpenCms.getSitemapManager();
        return manager.createNewElement(cms, sitemapUri, getRequest(), CmsResourceTypeXmlSitemap.getStaticTypeName());
    }

    /**
     * Creates a resource type info bean for a given resource type.<p>
     * 
     * @param resType the resource type
     *  
     * @return the resource type info bean
     */
    private CmsResourceTypeInfo createResourceTypeInfo(I_CmsResourceType resType) {

        String name = resType.getTypeName();
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        if (locale == null) {
            locale = new Locale("en");
        }
        String title = CmsWorkplaceMessages.getResourceTypeName(locale, name);
        return new CmsResourceTypeInfo(resType.getTypeId(), name, title);
    }

    /**
     * Creates a new sub-sitemap resource from the given sitemap and path.<p>
     * 
     * @param sitemapUri the super sitemap URI
     * @param path the starting path
     * 
     * @return the sub-sitemap creation result 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    private CmsSubSitemapInfo createSubSitemap(String sitemapUri, String path) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            // TODO: what about historical requests?
            CmsInternalSitemapEntry entry = (CmsInternalSitemapEntry)OpenCms.getSitemapManager().getEntryForUri(
                getCmsObject(),
                path);

            // get the content of the sub sitemap in the form of a list of changes 
            List<I_CmsSitemapChange> subSitemapChanges = getChangesForSubSitemap(entry);

            // create the actual sub-sitemap and fill it
            CmsResource subSitemapRes = createNewSitemap(sitemapUri);
            String subSitemapUri = cms.getSitePath(subSitemapRes);
            saveChangesInternal(subSitemapUri, subSitemapChanges, true);

            // remove the entries which now belong to the sub-sitemap from the parent sitemap, 
            // and update the sub-sitemap's parent element in the parent sitemap by setting its
            // sitemap property to the resource id of the sub-sitemap 
            List<I_CmsSitemapChange> parentSitemapChanges = getChangesForParentOfSubSitemap(entry, subSitemapRes);
            long timestamp = saveChangesInternal(sitemapUri, parentSitemapChanges, true);

            String sitemapVfsPath = cms.getRequestContext().removeSiteRoot(subSitemapRes.getRootPath());
            return new CmsSubSitemapInfo(sitemapVfsPath, timestamp);

        } catch (Throwable e) {
            error(e);
        }
        // we can never reach this point
        return null;
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
        for (CmsInternalSitemapEntry entry : (Set<CmsInternalSitemapEntry>)linkMap.keySet()) {
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
     * Helper method for getting the list of changes to apply to a parent sitemap to merge it with a sub-sitemap.<p>
     * 
     * @param rootEntry the entry of the parent sitemap which has a reference to the sub-sitemap
     *  
     * @return the list of necessary changes
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<I_CmsSitemapChange> getChangesForMergingSubSitemap(CmsInternalSitemapEntry rootEntry)
    throws CmsException {

        CmsObject cms = getCmsObject();

        List<I_CmsSitemapChange> changes = new ArrayList<I_CmsSitemapChange>();

        // insert sub-sitemap entries into parent sitemap 
        for (CmsInternalSitemapEntry entry : getDescendants(rootEntry)) {
            CmsResource childRes = cms.readResource(entry.getStructureId());
            CmsSitemapChangeNew newChildChange = new CmsSitemapChangeNew(
                entry.getSitePath(cms),
                entry.getPosition(),
                entry.getTitle(),
                cms.getSitePath(childRes),
                entry.getNewProperties(),
                entry.getId());
            changes.add(newChildChange);
        }

        // remove sitemap property from the parent entry of the sub-sitemap 
        CmsResource resource = cms.readResource(rootEntry.getStructureId());
        Map<String, CmsSimplePropertyValue> newProps = new HashMap<String, CmsSimplePropertyValue>(
            rootEntry.getNewProperties());
        newProps.remove(CmsSitemapManager.Property.sitemap.name());
        CmsSitemapChangeEdit editParentChange = new CmsSitemapChangeEdit(
            rootEntry.getSitePath(getCmsObject()),
            rootEntry.getTitle(),
            cms.getSitePath(resource),
            newProps);
        changes.add(editParentChange);

        return changes;
    }

    /**
     * When a subtree of a sitemap is going to be converted to a sub-sitemap, this helper method will return the list 
     * of necessary changes in the parent sitemap.<p>
     * 
     * @param entry the entry whose descendants will be converted to a sub-sitemap 
     * @param subSitemapRes the sub-sitemap resource
     *  
     * @return the list of changes which should be applied to the parent sitemap
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<I_CmsSitemapChange> getChangesForParentOfSubSitemap(
        CmsInternalSitemapEntry entry,
        CmsResource subSitemapRes) throws CmsException {

        CmsObject cms = getCmsObject();
        List<I_CmsSitemapChange> result = new ArrayList<I_CmsSitemapChange>();
        for (CmsInternalSitemapEntry childEntry : entry.getSubEntries()) {
            result.add(new CmsSitemapChangeDelete(childEntry.getSitePath(cms)));
        }
        Map<String, CmsSimplePropertyValue> newProps = new HashMap<String, CmsSimplePropertyValue>(
            entry.getNewProperties());

        String sitemapId = subSitemapRes.getStructureId().toString();
        CmsSimplePropertyValue sitemapProp = new CmsSimplePropertyValue(sitemapId, sitemapId);
        newProps.put(CmsSitemapManager.Property.sitemap.getName(), sitemapProp);

        CmsResource originalResource = cms.readResource(entry.getStructureId());
        String originalVfsPath = cms.getRequestContext().removeSiteRoot(originalResource.getRootPath());
        result.add(new CmsSitemapChangeEdit(
            entry.getSitePath(getCmsObject()),
            entry.getTitle(),
            originalVfsPath,
            newProps));
        return result;
    }

    /**
     * When a subtree of a sitemap is going to be converted to a sub-sitemap, this helper method will return the list 
     * of necessary changes in the sub-sitemap.<p>
     * 
     * @param entry the entry whose descendants will be converted to a sub-sitemap
     * 
     * @return the list of changes which should be applied to the sub-sitemap 
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<I_CmsSitemapChange> getChangesForSubSitemap(CmsInternalSitemapEntry entry) throws CmsException {

        List<I_CmsSitemapChange> result = new ArrayList<I_CmsSitemapChange>();
        assert entry.getSubEntries().size() > 0;
        String entryPoint = entry.getSitePath(getCmsObject());
        for (CmsInternalSitemapEntry descendant : getDescendants(entry)) {
            I_CmsSitemapChange change = createChangeForNewSitemapEntry(descendant, entryPoint);
            result.add(change);
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

        CmsObject cms = getCmsObject();
        CmsSitemapEntry entry = OpenCms.getSitemapManager().getEntryForUri(cms, root);
        List<CmsClientSitemapEntry> children = new ArrayList<CmsClientSitemapEntry>();
        if (entry.getProperties().get(CmsSitemapManager.Property.sitemap.name()) != null) {
            return children;
        }
        for (CmsSitemapEntry subEntry : OpenCms.getSitemapManager().getSubEntries(cms, root)) {
            CmsClientSitemapEntry child = toClientEntry(subEntry, propertyConfig);
            children.add(child);
            if ((levels > 1) || (levels == -1)) {
                child.setSubEntries(getChildren(child.getSitePath(), levels - 1, propertyConfig));
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
            Iterator<CmsClientSitemapEntry> modIt = cache.getModifications().iterator();
            while (modIt.hasNext()) {
                CmsClientSitemapEntry modEntry = modIt.next();
                CmsSitemapEntry sitemapEntry = null;
                try {
                    sitemapEntry = OpenCms.getSitemapManager().getEntryForId(getCmsObject(), modEntry.getId());
                } catch (CmsException e) {
                    // ignore
                }
                if (sitemapEntry != null) {
                    // make sure to use the correct data
                    modEntry.setSitePath(sitemapEntry.getSitePath(getCmsObject()));
                    modEntry.setTitle(sitemapEntry.getTitle());
                    modEntry.setName(sitemapEntry.getName());
                } else {
                    // now sitemap entry with the given id, so remove it from modifications
                    modIt.remove();
                }
            }

            Iterator<CmsClientSitemapEntry> delIt = cache.getDeletions().iterator();
            while (delIt.hasNext()) {
                CmsClientSitemapEntry delEntry = delIt.next();
                CmsSitemapEntry sitemapEntry = null;
                try {
                    sitemapEntry = OpenCms.getSitemapManager().getEntryForId(getCmsObject(), delEntry.getId());
                } catch (CmsException e) {
                    // ignore
                }
                if (sitemapEntry != null) {
                    // entry must have been restored
                    delIt.remove();
                }
            }

        }
        return cache;
    }

    /**
     * Returns the default template for the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI
     * 
     * @return the default template
     * 
     * @throws CmsRpcException if something goes wrong
     */
    private CmsSitemapTemplate getDefaultTemplate(String sitemapUri) throws CmsRpcException {

        CmsSitemapTemplate result = null;
        CmsObject cms = getCmsObject();
        try {
            result = getTemplateBean(cms, OpenCms.getSitemapManager().getDefaultTemplate(cms, sitemapUri, getRequest()));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * Helper function for collecting all descendants of a sitemap entry, not including the sitemap entry itself.<p>
     * 
     * In the resulting list, entries will always appear before their sub-entries.<p>
     * 
     * @param rootEntry the entry whose descendants should be collected
     *  
     * @return the descendants of <code>rootEntry</code>
     */
    private List<CmsInternalSitemapEntry> getDescendants(CmsInternalSitemapEntry rootEntry) {

        List<CmsInternalSitemapEntry> result = new ArrayList<CmsInternalSitemapEntry>();
        LinkedList<CmsInternalSitemapEntry> entriesToProcess = new LinkedList<CmsInternalSitemapEntry>();
        entriesToProcess.addAll(rootEntry.getSubEntries());
        while (!entriesToProcess.isEmpty()) {
            CmsInternalSitemapEntry currentEntry = entriesToProcess.removeFirst();
            result.add(currentEntry);
            entriesToProcess.addAll(currentEntry.getSubEntries());
        }
        return result;
    }

    /**
     * Returns the reason why you are not allowed to edit the current resource.<p>
     * 
     * @param cms the current cms object
     * @param request the current request to get the default locale from 
     * 
     * @return an empty string if editable, the reason if not
     * 
     * @throws CmsException if something goes wrong
     */
    private String getNoEditReason(CmsObject cms, HttpServletRequest request) throws CmsException {

        CmsResourceUtil resUtil = new CmsResourceUtil(cms, getResource(cms, request));
        return resUtil.getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
    }

    /**
     * Returns the current resource, taken into account historical requests.<p>
     * 
     * @param cms the current cms object
     * @param request the current request to get the default locale from 
     * 
     * @return the current resource
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsResource getResource(CmsObject cms, HttpServletRequest request) throws CmsException {

        CmsResource resource = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(request);
        if (resource == null) {
            resource = cms.readResource(cms.getRequestContext().getUri());
        }
        return resource;
    }

    /**
     * Gets the resource type info beans for types for which new detail pages can be created.<p>
     * 
     * @param cms the current CMS context 
     * @param sitemapUri the sitemap URI 
     * 
     * @return the resource type info beans for types for which new detail pages can be created 
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<CmsResourceTypeInfo> getResourceTypeInfos(CmsObject cms, String sitemapUri) throws CmsException {

        Collection<CmsResource> creatableResources = OpenCms.getADEManager().getCreatableElements(cms, sitemapUri, null);
        List<CmsResourceTypeInfo> result = new ArrayList<CmsResourceTypeInfo>();
        for (CmsResource res : creatableResources) {
            int typeId = res.getTypeId();
            I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(typeId);
            CmsResourceTypeInfo info = createResourceTypeInfo(resourceType);
            result.add(info);
        }
        return result;
    }

    /**
     * Returns the root sitemap entry for the given sitemap.<p>
     * 
     * @param sitemapRes the sitemap resource
     * @param parent the uri of the parent sitemap 
     * @param path the path on which the sitemap should be opened (may be null) 
     * @param propertyConfig the property configuration for sitemaps  
     *
     * @return root sitemap entry
     * 
     * @throws Exception if something goes wrong 
     */
    private CmsClientSitemapEntry getRoot(
        CmsResource sitemapRes,
        String parent,
        String path,
        Map<String, CmsXmlContentProperty> propertyConfig) throws Exception {

        CmsObject cms = getCmsObject();
        // TODO: what's about historical requests?
        CmsXmlSitemap xml = CmsXmlSitemapFactory.unmarshal(cms, sitemapRes);
        CmsSitemapBean sitemap = xml.getSitemap(cms, cms.getRequestContext().getLocale());
        if (sitemap.getSiteEntries().size() == 0) {
            return null;
        }
        CmsClientSitemapEntry root = null;
        if (parent == null) {
            root = toClientEntry(sitemap.getSiteEntries().get(0), propertyConfig);
        } else {
            String entryPoint = xml.getEntryPoint(cms);
            // get the entry in the parent sitemap which references the current sitemap 
            CmsInternalSitemapEntry referencingEntry = (CmsInternalSitemapEntry)OpenCms.getSitemapManager().getEntryForUri(
                cms,
                entryPoint);
            root = createDummySubSitemapRoot(referencingEntry, propertyConfig);
        }
        addChildrenRecursively(root, root, path, propertyConfig, 2);
        return root;
    }

    /**
     * Returns the complete sub-tree for the given sitemap entry.<p>
     * 
     * @param cms the cms context to use for VFS operations
     * @param sitePath the starting path 
     * 
     * @return the sub-tree
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsClientSitemapEntry getSubTree(CmsObject cms, String sitePath) throws CmsException {

        CmsResource sitemapRes = cms.readResource(OpenCms.getSitemapManager().getSitemapForUri(cms, sitePath));
        Map<String, CmsXmlContentProperty> propertyConfig = OpenCms.getSitemapManager().getElementPropertyConfiguration(
            cms,
            sitemapRes,
            true);
        CmsClientSitemapEntry startEntry = toClientEntry(OpenCms.getSitemapManager().getEntryForUri(
            getCmsObject(),
            sitePath), propertyConfig);
        startEntry.setSubEntries(getChildren(sitePath, -1, propertyConfig));
        return startEntry;
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
     * Saves the detail page information.<p>
     * 
     * @param detailPages a list of detail page beans 
     * @param sitemapUri the sitemap URI 
     * 
     * @throws CmsException if something goes wrong 
     */
    private void saveDetailPages(List<CmsDetailPageInfo> detailPages, String sitemapUri) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsConfigurationFileFinder finder = new CmsConfigurationFileFinder(
            CmsPropertyDefinition.PROPERTY_ADE_SITEMAP_CONFIG);
        CmsResource configFile = finder.getConfigurationFile(cms, sitemapUri);
        saveDetailPages(detailPages, configFile);
    }

    /**
     * Saves the given clipboard data to the session.<p>
     * 
     * @param clipboardData the clipboard data to save
     */
    private void setClipboardData(CmsSitemapClipboardData clipboardData) {

        getRequest().getSession().setAttribute(SESSION_ATTR_ADE_SITEMAP_CLIPBOARD_CACHE, clipboardData);
    }

    /**
     * Checks if the toolbar should be displayed.<p>
     * 
     * @param request the current request to get the default locale from 
     * 
     * @return <code>true</code> if the toolbar should be displayed
     */
    private boolean shouldDisplayToolbar(HttpServletRequest request) {

        // display the toolbar by default
        boolean displayToolbar = true;
        if (CmsHistoryResourceHandler.isHistoryRequest(request)) {
            // we do not want to display the toolbar in case of an historical request
            displayToolbar = false;
        }
        return displayToolbar;
    }

    /**
     * Converts a site entry bean into a JSON object.<p>
     * 
     * @param entry the entry to convert
     * @param propertyConfig the property configuration for sitemaps 
     * 
     * @return the JSON representation, can be <code>null</code> in case of not enough permissions
     * 
     * @throws CmsException should never happen 
     */
    private CmsClientSitemapEntry toClientEntry(CmsSitemapEntry entry, Map<String, CmsXmlContentProperty> propertyConfig)
    throws CmsException {

        CmsClientSitemapEntry clientEntry = new CmsClientSitemapEntry();
        clientEntry.setId(entry.getId());
        clientEntry.setName(entry.getName());
        clientEntry.setTitle(entry.getTitle());
        String vfsPath = null;
        if (!entry.isRedirect()) {
            try {
                vfsPath = getCmsObject().getSitePath(getCmsObject().readResource(entry.getStructureId()));
            } catch (CmsVfsResourceNotFoundException e) {
                vfsPath = e.getLocalizedMessage(getCmsObject().getRequestContext().getLocale());
            }
        }
        clientEntry.setVfsPath(vfsPath);
        Map<String, CmsSimplePropertyValue> clientProperties = CmsXmlContentPropertyHelper.convertPropertySimpleValues(
            getCmsObject(),
            entry.getNewProperties(),
            propertyConfig,
            true);

        clientEntry.setProperties(clientProperties);
        clientEntry.setSitePath(entry.getSitePath(getCmsObject()));
        clientEntry.setPosition(entry.getPosition());
        return clientEntry;

    }
}

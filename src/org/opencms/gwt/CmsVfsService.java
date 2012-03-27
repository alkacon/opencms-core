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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsAvailabilityInfoBean;
import org.opencms.gwt.shared.CmsBrokenLinkBean;
import org.opencms.gwt.shared.CmsDeleteResourceBean;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.LockIcon;
import org.opencms.gwt.shared.CmsLockReportInfo;
import org.opencms.gwt.shared.CmsPrepareEditResponse;
import org.opencms.gwt.shared.CmsPrincipalBean;
import org.opencms.gwt.shared.CmsVfsEntryBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.gwt.shared.rpc.I_CmsVfsService;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;

/**
 * A service class for reading the VFS tree.<p>
 * 
 * @since 8.0.0
 */
public class CmsVfsService extends CmsGwtService implements I_CmsVfsService {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsService.class);

    /** Serialization id. */
    private static final long serialVersionUID = -383483666952834348L;

    /**
     * Processes a file path, which may have macros in it, so it can be opened by the XML content editor.<p>
     * 
     * @param cms the current CMS context 
     * @param res the resource for which the context menu option has been selected 
     * @param pathWithMacros the file path which may contain macros
     *  
     * @return the processed file path 
     */
    public static String prepareFileNameForEditor(CmsObject cms, CmsResource res, String pathWithMacros) {

        String subsite = OpenCms.getADEManager().getSubSiteRoot(cms, res.getRootPath());
        CmsMacroResolver resolver = new CmsMacroResolver();
        if (subsite != null) {
            resolver.addMacro("subsite", cms.getRequestContext().removeSiteRoot(subsite));
        }
        String path = resolver.resolveMacros(pathWithMacros).replaceAll("/+", "/");
        return path;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#deleteResource(java.lang.String)
     */
    public void deleteResource(String sitePath) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION);
            String path = null;
            try {
                path = getCmsObject().getSitePath(res);
                getCmsObject().lockResource(path);
                getCmsObject().deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
            } catch (Exception e) {
                // should never happen
                error(e);
            } finally {
                try {
                    if (path != null) {
                        getCmsObject().unlockResource(path);
                    }
                } catch (Exception e) {
                    // should really never happen
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
        } catch (CmsException e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#forceUnlock(org.opencms.util.CmsUUID)
     */
    public void forceUnlock(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsResource resource = getCmsObject().readResource(structureId);
            // get the current lock
            CmsLock currentLock = getCmsObject().getLock(resource);
            // check if the resource is locked at all
            if (currentLock.getEditionLock().isUnlocked() && currentLock.getSystemLock().isUnlocked()) {
                getCmsObject().lockResourceTemporary(resource);
            } else {
                getCmsObject().changeLock(resource);
            }
            getCmsObject().unlockResource(resource);
        } catch (CmsException e) {
            error(e);
        }

    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getAvailabilityInfo(org.opencms.util.CmsUUID)
     */
    public CmsAvailabilityInfoBean getAvailabilityInfo(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            return getAvailabilityInfo(res);
        } catch (CmsException e) {
            error(e);
            return null; // will never be reached 
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getAvailabilityInfo(java.lang.String)
     */
    public CmsAvailabilityInfoBean getAvailabilityInfo(String vfsPath) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(vfsPath, CmsResourceFilter.IGNORE_EXPIRATION);
            return getAvailabilityInfo(res);
        } catch (CmsException e) {
            error(e);
            return null; // will never be reached 
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getBrokenLinks(java.lang.String)
     */
    public CmsDeleteResourceBean getBrokenLinks(String sitePath) throws CmsRpcException {

        try {
            CmsResource entryResource = getCmsObject().readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsDeleteResourceBean result = null;

            CmsListInfoBean info = null;
            List<CmsBrokenLinkBean> brokenLinks = null;

            CmsObject cms = getCmsObject();
            String resourceSitePath = cms.getSitePath(entryResource);

            try {
                ensureSession();

                List<CmsResource> descendants = new ArrayList<CmsResource>();
                HashSet<CmsUUID> deleteIds = new HashSet<CmsUUID>();

                descendants.add(entryResource);
                if (entryResource.isFolder()) {
                    descendants.addAll(cms.readResources(resourceSitePath, CmsResourceFilter.IGNORE_EXPIRATION));
                }
                for (CmsResource deleteRes : descendants) {
                    deleteIds.add(deleteRes.getStructureId());
                }
                MultiValueMap linkMap = MultiValueMap.decorate(
                    new HashMap<Object, Object>(),
                    FactoryUtils.instantiateFactory(HashSet.class));
                for (CmsResource resource : descendants) {
                    List<CmsResource> linkSources = getLinkSources(cms, resource, deleteIds);
                    for (CmsResource source : linkSources) {
                        linkMap.put(resource, source);
                    }
                }

                brokenLinks = getBrokenLinkBeans(linkMap);
                info = getPageInfo(entryResource);

                result = new CmsDeleteResourceBean(resourceSitePath, info, brokenLinks);

            } catch (Throwable e) {
                error(e);
            }
            return result;
        } catch (CmsException e) {
            error(e);
            return null; // will never be reached 
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getChildren(java.lang.String)
     */
    public List<CmsVfsEntryBean> getChildren(String path) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            List<CmsResource> resources = new ArrayList<CmsResource>();
            resources.addAll(cms.getResourcesInFolder(path, CmsResourceFilter.DEFAULT));
            List<CmsVfsEntryBean> result = makeEntryBeans(resources, false);
            return result;
        } catch (CmsException e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getLockReportInfo(org.opencms.util.CmsUUID)
     */
    public CmsLockReportInfo getLockReportInfo(CmsUUID structureId) throws CmsRpcException {

        CmsLockReportInfo result = null;
        try {
            CmsResource resource = getCmsObject().readResource(structureId);
            List<CmsListInfoBean> lockedInfos = new ArrayList<CmsListInfoBean>();
            List<CmsResource> lockedResources = getCmsObject().getBlockingLockedResources(resource);
            if (lockedResources != null) {
                for (CmsResource lockedResource : lockedResources) {
                    lockedInfos.add(getPageInfoWithLock(lockedResource));
                }
            }
            result = new CmsLockReportInfo(getPageInfoWithLock(resource), lockedInfos);
        } catch (CmsException e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getPageInfo(org.opencms.util.CmsUUID)
     */
    public CmsListInfoBean getPageInfo(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            return getPageInfo(res);
        } catch (CmsException e) {
            error(e);
            return null; // will never be reached 
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getPageInfo(java.lang.String)
     */
    public CmsListInfoBean getPageInfo(String vfsPath) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(vfsPath, CmsResourceFilter.IGNORE_EXPIRATION);
            return getPageInfo(res);
        } catch (CmsException e) {
            error(e);
            return null; // will never be reached 
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getRootEntries()
     */
    public List<CmsVfsEntryBean> getRootEntries() throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            List<CmsResource> roots = new ArrayList<CmsResource>();
            roots.add(cms.readResource("/"));
            return makeEntryBeans(roots, true);
        } catch (CmsException e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getSitePath(org.opencms.util.CmsUUID)
     */
    public String getSitePath(CmsUUID structureId) {

        try {
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            return getCmsObject().getSitePath(resource);
        } catch (CmsException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessageContainer(), e);
            }
        }
        return null;
    }

    /**
     * Updates properties for a resource and possibly its detail page.<p>
     *     
     * @param cms the CMS context 
     * @param ownRes the resource 
     * @param propertyModifications the property modifications 
     * 
     * @throws CmsException if something goes wrong 
     */
    public void internalUpdateProperties(
        CmsObject cms,
        CmsResource ownRes,
        List<CmsPropertyModification> propertyModifications) throws CmsException {

        Map<String, CmsProperty> ownProps = getPropertiesByName(cms.readPropertyObjects(ownRes, false));
        // determine if the title property should be changed in case of a 'NavText' change
        boolean changeOwnTitle = shouldChangeTitle(ownProps);

        String hasNavTextChange = null;
        List<CmsProperty> ownPropertyChanges = new ArrayList<CmsProperty>();
        for (CmsPropertyModification propMod : propertyModifications) {
            CmsProperty propToModify = null;
            if (ownRes.getStructureId().equals(propMod.getId())) {

                if (CmsPropertyDefinition.PROPERTY_NAVTEXT.equals(propMod.getName())) {
                    hasNavTextChange = propMod.getValue();
                } else if (CmsPropertyDefinition.PROPERTY_TITLE.equals(propMod.getName())) {
                    changeOwnTitle = false;
                }
                propToModify = ownProps.get(propMod.getName());
                if (propToModify == null) {
                    propToModify = new CmsProperty(propMod.getName(), null, null);
                }
                ownPropertyChanges.add(propToModify);
            } else {
                throw new IllegalStateException("Invalid structure id in property changes!");
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
        }
        if (hasNavTextChange != null) {
            if (changeOwnTitle) {
                CmsProperty titleProp = ownProps.get(CmsPropertyDefinition.PROPERTY_TITLE);
                if (titleProp == null) {
                    titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, null);
                }
                titleProp.setStructureValue(hasNavTextChange);
                ownPropertyChanges.add(titleProp);
            }
        }
        if (!ownPropertyChanges.isEmpty()) {
            cms.writePropertyObjects(ownRes, ownPropertyChanges);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#loadPropertyData(org.opencms.util.CmsUUID)
     */
    public CmsPropertiesBean loadPropertyData(CmsUUID id) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            return internalLoadPropertyData(cms, id);
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#prepareEdit(org.opencms.util.CmsUUID, java.lang.String)
     */
    public CmsPrepareEditResponse prepareEdit(CmsUUID currentPageId, String pathWithMacros) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource currentPage = cms.readResource(currentPageId);
            String path = prepareFileNameForEditor(cms, currentPage, pathWithMacros);
            CmsResource resource = cms.readResource(path);
            ensureLock(resource);
            CmsPrepareEditResponse result = new CmsPrepareEditResponse();
            result.setRootPath(resource.getRootPath());
            result.setSitePath(cms.getSitePath(resource));
            result.setStructureId(resource.getStructureId());
            return result;
        } catch (CmsException e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#saveProperties(org.opencms.gwt.shared.property.CmsPropertyChangeSet)
     */
    public void saveProperties(CmsPropertyChangeSet changes) throws CmsRpcException {

        try {
            internalSaveProperties(changes);
        } catch (Throwable t) {
            error(t);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#substituteLinkForRootPath(java.lang.String, java.lang.String)
     */
    public String substituteLinkForRootPath(String currentSiteRoot, String rootPath) throws CmsRpcException {

        String result = null;
        try {
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            cms.getRequestContext().setSiteRoot(currentSiteRoot);
            result = OpenCms.getLinkManager().substituteLinkForRootPath(cms, rootPath);
        } catch (CmsException e) {
            error(e);
        }
        return result;
    }

    /**
     * Converts CmsProperty objects to CmsClientProperty objects.<p>
     * 
     * @param properties a list of server-side properties 
     * 
     * @return a map of client-side properties 
     */
    protected Map<String, CmsClientProperty> convertProperties(List<CmsProperty> properties) {

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        for (CmsProperty prop : properties) {
            CmsClientProperty clientProp = new CmsClientProperty(
                prop.getName(),
                prop.getStructureValue(),
                prop.getResourceValue());
            clientProp.setOrigin(prop.getOrigin());
            result.put(clientProp.getName(), clientProp);
        }
        return result;
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
    protected CmsBrokenLinkBean createSitemapBrokenLinkBean(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsProperty titleProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, true);
        String typeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        String defaultTitle = "";
        String title = titleProp.getValue(defaultTitle);
        String path = cms.getSitePath(resource);
        String subtitle = path;
        return new CmsBrokenLinkBean(title, subtitle, typeName);
    }

    /**
     * Saves a set of property changes.<p>
     *  
     * @param changes the set of property changes 
     * @throws CmsException if something goes wrong 
     */
    protected void internalSaveProperties(CmsPropertyChangeSet changes) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource target = cms.readResource(changes.getTargetStructureId());
        ensureLock(cms.getSitePath(target));
        internalUpdateProperties(cms, target, changes.getChanges());
    }

    /**
     * Helper method for creating a VFS entry bean from a resource.<p>
     *  
     * @param resource the resource whose data should be stored in the bean 
     * @param root true if the resource is a root resource
     *  
     * @return the data bean representing the resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsVfsEntryBean makeEntryBean(CmsResource resource, boolean root) throws CmsException {

        CmsObject cms = getCmsObject();
        boolean isFolder = resource.isFolder();
        String name = root ? "/" : resource.getName();
        String path = cms.getSitePath(resource);
        boolean hasChildren = false;
        if (isFolder) {
            List<CmsResource> children = cms.getResourcesInFolder(
                cms.getRequestContext().getSitePath(resource),
                CmsResourceFilter.DEFAULT);
            if (!children.isEmpty()) {
                hasChildren = true;
            }
        }
        String resourceType = OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName();

        return new CmsVfsEntryBean(path, name, resourceType, isFolder, hasChildren);
    }

    /**
     * Helper method for creating a list of VFS entry beans from a list of the corresponding resources.<p>
     * 
     * @param resources the list of resources which should be converted to entry beans 
     * @param root true if the resources in the list are root resources
     *  
     * @return the list of VFS entry beans for the resources 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected List<CmsVfsEntryBean> makeEntryBeans(List<CmsResource> resources, boolean root) throws CmsException {

        List<CmsVfsEntryBean> result = new ArrayList<CmsVfsEntryBean>();
        for (CmsResource res : resources) {
            result.add(makeEntryBean(res, root));
        }
        return result;
    }

    /**
     * Returns a bean that contains the infos for the {@link org.opencms.gwt.client.ui.contextmenu.CmsAvailabilityDialog}.<p>
     * 
     * @param res the resource to get the availability infos for
     * 
     * @return a bean for the {@link org.opencms.gwt.client.ui.contextmenu.CmsAvailabilityDialog}
     * 
     * @throws CmsRpcException if something goes wrong
     */
    private CmsAvailabilityInfoBean getAvailabilityInfo(CmsResource res) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsAvailabilityInfoBean result = new CmsAvailabilityInfoBean();

            result.setPageInfo(getPageInfo(res));

            String resourceSitePath = cms.getRequestContext().removeSiteRoot(res.getRootPath());
            result.setVfsPath(resourceSitePath);

            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(res.getTypeId());
            result.setResType(type.getTypeName());

            result.setDateReleased(res.getDateReleased());
            result.setDateExpired(res.getDateExpired());

            String notificationInterval = cms.readPropertyObject(
                res,
                CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL,
                false).getValue();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(notificationInterval)) {
                result.setNotificationInterval(Integer.valueOf(notificationInterval).intValue());
            }

            String notificationEnabled = cms.readPropertyObject(
                res,
                CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION,
                false).getValue();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(notificationEnabled)) {
                result.setNotificationEnabled(Boolean.valueOf(notificationEnabled).booleanValue());
            }

            result.setHasSiblings(cms.readSiblings(resourceSitePath, CmsResourceFilter.ALL).size() > 1);

            result.setResponsibles(getResponsibles(res.getRootPath()));

            return result;
        } catch (CmsException e) {
            error(e);
            return null; // will never be reached 
        }
    }

    /**
     * Helper method for converting a map which maps resources to resources to a list of "broken link" beans,
     * which have beans representing the source of the corresponding link as children.<p>  
     * 
     * @param linkMap a multimap from resource to resources  
     * 
     * @return a list of beans representing links which will be broken 
     * 
     * @throws CmsException if something goes wrong 
     */
    @SuppressWarnings("unchecked")
    private List<CmsBrokenLinkBean> getBrokenLinkBeans(MultiValueMap linkMap) throws CmsException {

        List<CmsBrokenLinkBean> result = new ArrayList<CmsBrokenLinkBean>();
        for (CmsResource entry : (Set<CmsResource>)linkMap.keySet()) {
            CmsBrokenLinkBean parentBean = createSitemapBrokenLinkBean(entry);
            result.add(parentBean);
            Collection<CmsResource> values = linkMap.getCollection(entry);
            for (CmsResource resource : values) {
                CmsBrokenLinkBean childBean = createSitemapBrokenLinkBean(resource);
                parentBean.addChild(childBean);
            }
        }
        return result;
    }

    /**
     * Gets the resources which link to a given structure id.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the relation target resource
     * @param deleteIds set of resources to delete
     *  
     * @return the list of resources which link to the given id
     *  
     * @throws CmsException
     */
    private List<CmsResource> getLinkSources(CmsObject cms, CmsResource resource, HashSet<CmsUUID> deleteIds)
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
     * Returns a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget}.<p>
     * 
     * @param res the resource to get the page info for
     * 
     * @return a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget}.<p>
     * 
     * @throws CmsLoaderException if the resource type could not be found
     * @throws CmsException if something else goes wrong 
     */
    private CmsListInfoBean getPageInfo(CmsResource res) throws CmsException, CmsLoaderException {

        CmsObject cms = getCmsObject();
        CmsListInfoBean result = new CmsListInfoBean();

        result.setResourceState(res.getState());

        String title = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
            result.setTitle(title);
        } else {
            result.setTitle(res.getName());
        }
        result.setSubTitle(cms.getSitePath(res));
        String secure = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_SECURE, true).getValue();
        if (Boolean.parseBoolean(secure)) {
            result.setStateIcon(CmsListInfoBean.StateIcon.secure);
        } else {
            String export = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_EXPORT, true).getValue();
            if (Boolean.parseBoolean(export)) {
                result.setStateIcon(CmsListInfoBean.StateIcon.export);
            } else {
                result.setStateIcon(CmsListInfoBean.StateIcon.standard);
            }
        }
        String resTypeName = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
        String key = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resTypeName).getKey();
        Locale currentLocale = cms.getRequestContext().getLocale();
        CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(currentLocale);
        String resTypeNiceName = messages.key(key);
        result.addAdditionalInfo(messages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_TYPE_0), resTypeNiceName);
        result.setResourceType(resTypeName);
        return result;
    }

    /**
     * Returns a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget} including the lock state.<p>
     * 
     * @param resource the resource to get the page info for
     * 
     * @return a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget}.<p>
     * 
     * @throws CmsLoaderException if the resource type could not be found
     * @throws CmsException if something else goes wrong 
     */
    private CmsListInfoBean getPageInfoWithLock(CmsResource resource) throws CmsLoaderException, CmsException {

        CmsListInfoBean result = getPageInfo(resource);
        CmsResourceUtil resourceUtil = new CmsResourceUtil(getCmsObject(), resource);
        CmsLock lock = resourceUtil.getLock();
        LockIcon icon = LockIcon.NONE;
        String iconTitle = null;
        CmsLockType lockType = lock.getType();
        if (!lock.isOwnedBy(getCmsObject().getRequestContext().getCurrentUser())) {
            if ((lockType == CmsLockType.EXCLUSIVE)
                || (lockType == CmsLockType.INHERITED)
                || (lockType == CmsLockType.TEMPORARY)) {
                icon = LockIcon.CLOSED;
            } else if ((lockType == CmsLockType.SHARED_EXCLUSIVE) || (lockType == CmsLockType.SHARED_INHERITED)) {
                icon = LockIcon.SHARED_CLOSED;
            }
        } else {
            if ((lockType == CmsLockType.EXCLUSIVE)
                || (lockType == CmsLockType.INHERITED)
                || (lockType == CmsLockType.TEMPORARY)) {
                icon = LockIcon.OPEN;
            } else if ((lockType == CmsLockType.SHARED_EXCLUSIVE) || (lockType == CmsLockType.SHARED_INHERITED)) {
                icon = LockIcon.SHARED_OPEN;
            }
        }
        if (lock.getUserId() != null) {
            iconTitle = Messages.get().getBundle().key(Messages.GUI_LOCKED_BY_1, resourceUtil.getLockedByName());
        }
        result.setLockIcon(icon);
        result.setLockIconTitle(iconTitle);
        if (icon != LockIcon.NONE) {
            result.setTitle(result.getTitle() + " (" + iconTitle + ")");
        }
        return result;
    }

    /**
     * Returns a map of principals of responsible users together with the resource path where the
     * responsibility was found.<p> 
     * 
     * @param vfsPath the path pointing on the resource to get the responsible users for
     * 
     * @return a map of principal beans
     * 
     * @throws CmsRpcException if something goes wrong
     */
    private Map<CmsPrincipalBean, String> getResponsibles(String vfsPath) throws CmsRpcException {

        Map<CmsPrincipalBean, String> result = new HashMap<CmsPrincipalBean, String>();
        List<CmsResource> parentResources = new ArrayList<CmsResource>();

        CmsObject cms = getCmsObject();
        String resourceSitePath = cms.getRequestContext().removeSiteRoot(vfsPath);
        try {
            // get all parent folders of the current file
            parentResources = cms.readPath(resourceSitePath, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            error(e);
        }

        for (CmsResource resource : parentResources) {
            String storedSiteRoot = cms.getRequestContext().getSiteRoot();
            String sitePath = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
            try {

                cms.getRequestContext().setSiteRoot("/");
                List<CmsAccessControlEntry> entries = cms.getAccessControlEntries(resource.getRootPath(), false);
                for (CmsAccessControlEntry ace : entries) {
                    if (ace.isResponsible()) {
                        I_CmsPrincipal principal = cms.lookupPrincipal(ace.getPrincipal());
                        if (principal != null) {
                            CmsPrincipalBean prinBean = new CmsPrincipalBean(
                                principal.getName(),
                                principal.getDescription(),
                                principal.isGroup());
                            if (!resource.getRootPath().equals(vfsPath)) {
                                if (resource.getRootPath().startsWith(storedSiteRoot)) {
                                    result.put(prinBean, sitePath);
                                } else {
                                    result.put(prinBean, resource.getRootPath());
                                }
                            } else {
                                result.put(prinBean, null);
                            }
                        }
                    }
                }
            } catch (CmsException e) {
                error(e);
            } finally {
                cms.getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
        return result;
    }

    /**
     * Loads the data needed for editing the properties of a resource.<p>
     * 
     * @param cms the CMS context 
     * @param id the structure id of the resource 
     * @return the data needed for editing the properties 
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsPropertiesBean internalLoadPropertyData(CmsObject cms, CmsUUID id) throws CmsException {

        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        CmsPropertiesBean result = new CmsPropertiesBean();
        CmsResource resource = cms.readResource(id);
        result.setFolder(resource.isFolder());
        result.setContainerPage(CmsResourceTypeXmlContainerPage.isContainerPage(resource));
        String sitePath = cms.getSitePath(resource);
        Map<String, CmsXmlContentProperty> propertyConfig = OpenCms.getADEManager().lookupConfiguration(
            cms,
            resource.getRootPath()).getPropertyConfigurationAsMap();
        result.setPropertyDefinitions(new LinkedHashMap<String, CmsXmlContentProperty>(propertyConfig));
        try {
            cms.getRequestContext().setSiteRoot("");
            String parentPath = CmsResource.getParentFolder(resource.getRootPath());
            CmsResource parent = cms.readResource(parentPath);
            List<CmsProperty> parentProperties = cms.readPropertyObjects(parent, true);
            List<CmsProperty> ownProperties = cms.readPropertyObjects(resource, false);
            result.setOwnProperties(convertProperties(ownProperties));
            result.setInheritedProperties(convertProperties(parentProperties));
            result.setPageInfo(getPageInfo(resource));
            List<CmsPropertyDefinition> propDefs = cms.readAllPropertyDefinitions();
            List<String> propNames = new ArrayList<String>();
            for (CmsPropertyDefinition propDef : propDefs) {
                propNames.add(propDef.getName());
            }
            CmsTemplateFinder templateFinder = new CmsTemplateFinder(cms);
            result.setTemplates(templateFinder.getTemplates());
            result.setAllProperties(propNames);
            result.setStructureId(id);
            result.setSitePath(sitePath);
            return result;
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }
    }

    /**
     * Determines if the title property should be changed in case of a 'NavText' change.<p>
     * 
     * @param properties the current resource properties
     * 
     * @return <code>true</code> if the title property should be changed in case of a 'NavText' change
     */
    private boolean shouldChangeTitle(Map<String, CmsProperty> properties) {

        return (properties == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE) == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue() == null)
            || ((properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT) != null) && properties.get(
                CmsPropertyDefinition.PROPERTY_TITLE).getValue().equals(
                properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT).getValue()));
    }
}

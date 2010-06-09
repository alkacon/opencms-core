/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/Attic/CmsSitemapService.java,v $
 * Date   : $Date: 2010/06/09 12:13:03 $
 * Version: $Revision: 1.25 $
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

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapMergeInfo;
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
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.sitemap.CmsInternalSitemapEntry;
import org.opencms.xml.sitemap.CmsSitemapBean;
import org.opencms.xml.sitemap.CmsSitemapChangeDelete;
import org.opencms.xml.sitemap.CmsSitemapChangeEdit;
import org.opencms.xml.sitemap.CmsSitemapChangeNew;
import org.opencms.xml.sitemap.CmsSitemapChangeNewSubSitemapEntry;
import org.opencms.xml.sitemap.CmsSitemapEntry;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.CmsXmlSitemap;
import org.opencms.xml.sitemap.CmsXmlSitemapFactory;
import org.opencms.xml.sitemap.I_CmsSitemapChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.25 $ 
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
    private static final String SESSION_ATTR_ADE_SITEMAP_RECENT_LIST_CACHE = "__OCMS_ADE_SITEMAP_RECENT_LIST_CACHE__";

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
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#exit(java.util.List)
     */
    public void exit(List<CmsClientSitemapEntry> recentList) throws CmsRpcException {

        try {
            if (recentList != null) {
                setRecentList(recentList);
            }
        } catch (Throwable e) {
            error(e);
        }
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
                sitemapRes);
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
                cms.readResource(sitemapUri));

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
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#mergeSubsitemap(java.lang.String, java.lang.String)
     */
    public CmsSitemapMergeInfo mergeSubsitemap(String sitemapUri, String path) {

        return null;
        //TODO: implement this 
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#prefetch(java.lang.String)
     */
    public CmsSitemapData prefetch(String sitemapUri) throws CmsRpcException {

        CmsSitemapData result = null;
        CmsObject cms = getCmsObject();
        try {
            CmsResource sitemap = cms.readResource(sitemapUri);
            Map<String, CmsXmlContentProperty> propertyConfig = OpenCms.getSitemapManager().getElementPropertyConfiguration(
                cms,
                sitemap);
            String parentSitemap = OpenCms.getSitemapManager().getParentSitemap(cms, sitemapUri);
            result = new CmsSitemapData(
                getDefaultTemplate(sitemapUri),
                getTemplates(),
                CmsXmlContentPropertyHelper.getPropertyInfo(cms, sitemap),
                getCachedRecentList(),
                getNoEditReason(cms, getRequest()),
                isDisplayToolbar(getRequest()),
                OpenCms.getResourceManager().getResourceType(CmsResourceTypeXmlContainerPage.getStaticTypeName()).getTypeId(),
                parentSitemap,
                getRoot(sitemap, parentSitemap, propertyConfig),
                sitemap.getDateLastModified());
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#save(java.lang.String, java.util.List)
     */
    public long save(String sitemapUri, List<I_CmsSitemapChange> changes) throws CmsRpcException {

        try {
            return saveInternal(sitemapUri, changes, true);
        } catch (Throwable e) {
            error(e);
        }
        return 0;
    }

    /**
     * Saves a list of changes to a sitemap and then creates a sub-sitemap of the given sitemap starting from a path.<p>
     * 
     * @param sitemapUri the URI of the parent sitemap 
     * @param changes the changes which should be applied to the parent sitemap 
     * @param path the path in the parent sitemap from which the sub-sitemap should be created 
     * 
     * @return the sub-sitemap creation result 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    public CmsSubSitemapInfo saveAndCreateSubSitemap(String sitemapUri, List<I_CmsSitemapChange> changes, String path)
    throws CmsRpcException {

        try {
            // don't unlock the sitemap because createSubSitemap still needs to modify it  
            saveInternal(sitemapUri, changes, false);

            return createSubSitemap(sitemapUri, path);
        } catch (Throwable e) {
            error(e);
        }
        return null; // we never reach this line 
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#saveSync(java.lang.String, java.util.List)
     */
    public long saveSync(String sitemapUri, List<I_CmsSitemapChange> changes) throws CmsRpcException {

        return save(sitemapUri, changes);
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
    protected long saveInternal(String sitemapUri, List<I_CmsSitemapChange> changes, boolean unlockAfterSave)
    throws CmsException {

        long timestamp = 0;
        CmsObject cms = getCmsObject();

        // TODO: what's about historical requests?
        CmsResource sitemap = cms.readResource(sitemapUri);
        CmsXmlSitemap xml = CmsXmlSitemapFactory.unmarshal(cms, sitemap);
        // apply changes
        xml.applyChanges(cms, changes);
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
        CmsUUID resourceId = entry.getResourceId();

        CmsResource resource = cms.readResource(resourceId);

        CmsSitemapChangeNew result = new CmsSitemapChangeNewSubSitemapEntry(
            entry.getSitePath(cms),
            entry.getPosition(),
            entry.getTitle(),
            cms.getSitePath(resource),
            entry.getProperties(),
            entryPoint);
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
            saveInternal(subSitemapUri, subSitemapChanges, true);

            // remove the entries which now belong to the sub-sitemap from the parent sitemap, 
            // and update the sub-sitemap's parent element in the parent sitemap by setting its
            // sitemap property to the resource id of the sub-sitemap 
            List<I_CmsSitemapChange> parentSitemapChanges = getChangesForParentOfSubSitemap(entry, subSitemapRes);
            long timestamp = saveInternal(sitemapUri, parentSitemapChanges, true);

            String sitemapVfsPath = cms.getRequestContext().removeSiteRoot(subSitemapRes.getRootPath());
            return new CmsSubSitemapInfo(sitemapVfsPath, timestamp);

        } catch (Throwable e) {
            error(e);
        }
        // we can never reach this point
        return null;
    }

    /**
     * Returns the cached recent list, creating it if it doesn't already exist.<p>
     * 
     * @return the cached recent list
     */
    @SuppressWarnings("unchecked")
    private List<CmsClientSitemapEntry> getCachedRecentList() {

        List<CmsClientSitemapEntry> cache = (List<CmsClientSitemapEntry>)getRequest().getSession().getAttribute(
            SESSION_ATTR_ADE_SITEMAP_RECENT_LIST_CACHE);
        if (cache == null) {
            cache = new ArrayList<CmsClientSitemapEntry>();
            getRequest().getSession().setAttribute(SESSION_ATTR_ADE_SITEMAP_RECENT_LIST_CACHE, cache);
        }
        return cache;

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
        Map<String, String> newProps = new HashMap<String, String>(entry.getProperties());
        newProps.put(CmsSitemapManager.Property.sitemap.getName(), subSitemapRes.getStructureId().toString());

        CmsResource originalResource = cms.readResource(entry.getResourceId());
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
        LinkedList<CmsInternalSitemapEntry> entriesToProcess = new LinkedList<CmsInternalSitemapEntry>();
        assert entry.getSubEntries().size() > 0;
        entriesToProcess.addAll(entry.getSubEntries());
        while (!entriesToProcess.isEmpty()) {
            CmsInternalSitemapEntry currentEntry = entriesToProcess.removeFirst();
            I_CmsSitemapChange change = createChangeForNewSitemapEntry(currentEntry, entry.getSitePath(getCmsObject()));
            entriesToProcess.addAll(currentEntry.getSubEntries());
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
            if (levels > 1) {
                child.setSubEntries(getChildren(child.getSitePath(), levels - 1, propertyConfig));
            }
        }
        return children;
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
     * Returns the root sitemap entry for the given sitemap.<p>
     * @param sitemapRes the sitemap resource
     * @param parent the uri of the parent sitemap 
     * @param propertyConfig the property configuration for sitemaps  
     *
     * @return root sitemap entry
     * 
     * @throws Exception if something goes wrong 
     */
    private CmsClientSitemapEntry getRoot(
        CmsResource sitemapRes,
        String parent,
        Map<String, CmsXmlContentProperty> propertyConfig) throws Exception {

        CmsObject cms = getCmsObject();
        // TODO: what's about historical requests?
        CmsXmlSitemap xml = CmsXmlSitemapFactory.unmarshal(cms, sitemapRes);
        CmsSitemapBean sitemap = xml.getSitemap(cms, cms.getRequestContext().getLocale());
        if (parent == null) {
            CmsClientSitemapEntry root = toClientEntry(sitemap.getSiteEntries().get(0), propertyConfig);
            root.setSubEntries(getChildren(root.getSitePath(), 2, propertyConfig));
            return root;
        } else {
            String entryPoint = xml.getEntryPoint(cms);
            // get the entry in the parent sitemap which references the current sitemap 
            CmsInternalSitemapEntry referencingEntry = (CmsInternalSitemapEntry)OpenCms.getSitemapManager().getEntryForUri(
                cms,
                entryPoint);
            CmsClientSitemapEntry root = createDummySubSitemapRoot(referencingEntry, propertyConfig);
            for (CmsInternalSitemapEntry topLevelEntry : sitemap.getSiteEntries()) {
                root.addSubEntry(toClientEntry(topLevelEntry, propertyConfig));
            }
            return root;
        }
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
     * Saves the given recent list to the session.<p>
     * 
     * @param recentList the recent list to save
     */
    private void setRecentList(List<CmsClientSitemapEntry> recentList) {

        getRequest().getSession().setAttribute(SESSION_ATTR_ADE_SITEMAP_RECENT_LIST_CACHE, recentList);
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
        String vfsPath;
        try {
            vfsPath = getCmsObject().getSitePath(getCmsObject().readResource(entry.getResourceId()));
        } catch (CmsVfsResourceNotFoundException e) {
            vfsPath = e.getLocalizedMessage(getCmsObject().getRequestContext().getLocale());
        }
        clientEntry.setVfsPath(vfsPath);
        Map<String, String> clientProperties = CmsXmlContentPropertyHelper.convertPropertiesToClientFormat(
            getCmsObject(),
            entry.getProperties(),
            propertyConfig);
        clientEntry.setProperties(clientProperties);
        clientEntry.setSitePath(entry.getSitePath(getCmsObject()));
        clientEntry.setPosition(entry.getPosition());
        return clientEntry;
    }

}

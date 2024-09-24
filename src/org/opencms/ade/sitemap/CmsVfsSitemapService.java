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

package org.opencms.ade.sitemap;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.CmsFunctionAvailability;
import org.opencms.ade.configuration.CmsFunctionReference;
import org.opencms.ade.configuration.CmsModelPageConfig;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCacheState;
import org.opencms.ade.detailpage.CmsDetailPageConfigurationWriter;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EntryType;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsGalleryFolderEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.ade.sitemap.shared.CmsLocaleComparePropertyData;
import org.opencms.ade.sitemap.shared.CmsModelInfo;
import org.opencms.ade.sitemap.shared.CmsModelPageEntry;
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapAttributeData;
import org.opencms.ade.sitemap.shared.CmsSitemapCategoryData;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapChange.ChangeType;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode;
import org.opencms.ade.sitemap.shared.CmsSitemapInfo;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.db.CmsAlias;
import org.opencms.db.CmsAliasManager;
import org.opencms.db.CmsRewriteAlias;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.file.types.CmsResourceTypeFolderSubSitemap;
import org.opencms.file.types.CmsResourceTypeFunctionConfig;
import org.opencms.file.types.CmsResourceTypeUnknownFolder;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsCoreService;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsIconUtil;
import org.opencms.gwt.CmsPropertyEditorHelper;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.CmsTemplateFinder;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsClientLock;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationReply;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasImportResult;
import org.opencms.gwt.shared.alias.CmsAliasInitialFetchResult;
import org.opencms.gwt.shared.alias.CmsAliasSaveValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;
import org.opencms.gwt.shared.alias.CmsRewriteAliasTableRow;
import org.opencms.gwt.shared.alias.CmsRewriteAliasValidationReply;
import org.opencms.gwt.shared.alias.CmsRewriteAliasValidationRequest;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavBuilder.Visibility;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.jsp.CmsJspTagLink;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsServlet;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.ui.apps.CmsQuickLaunchLocationCache;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeAccess;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;
import org.opencms.xml.containerpage.mutable.CmsContainerPageWrapper;
import org.opencms.xml.containerpage.mutable.CmsMutableContainer;
import org.opencms.xml.containerpage.mutable.CmsMutableContainerPage;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * Handles all RPC services related to the vfs sitemap.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public class CmsVfsSitemapService extends CmsGwtService implements I_CmsSitemapService {

    /** Helper class for representing information about a  lock. */
    protected class LockInfo {

        /** The lock state. */
        private CmsLock m_lock;

        /** True if the lock was just locked. */
        private boolean m_wasJustLocked;

        /**
         * Creates a new LockInfo object.<p>
         *
         * @param lock the lock state
         * @param wasJustLocked true if the lock was just locked
         */
        public LockInfo(CmsLock lock, boolean wasJustLocked) {

            m_lock = lock;
            m_wasJustLocked = wasJustLocked;
        }

        /**
         * Returns the lock state.<p>
         *
         * @return the lock state
         */
        public CmsLock getLock() {

            return m_lock;
        }

        /**
         * Returns true if the lock was just locked.<p>
         *
         * @return true if the lock was just locked
         */
        public boolean wasJustLocked() {

            return m_wasJustLocked;
        }
    }

    /** The path of the JSP used to download aliases. */
    public static final String ALIAS_DOWNLOAD_PATH = "/system/workplace/commons/download-aliases.jsp";

    /** The path to the JSP used to upload aliases. */
    public static final String ALIAS_IMPORT_PATH = "/system/workplace/commons/import-aliases.jsp";

    /** The show model edit confirm dialog attribute name. */
    public static final String ATTR_SHOW_MODEL_EDIT_CONFIRM = "showModelEditConfirm";

    /** Properties to remove from the copied template when creating a new sitemap entry. */
    public static final List<String> FILTER_PROPERTIES = Arrays.asList(
        new String[] {
            CmsPropertyDefinition.PROPERTY_TITLE,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION_HTML,
            CmsPropertyDefinition.PROPERTY_NAVTEXT,
            CmsPropertyDefinition.PROPERTY_NAVINFO});

    /** The galleries folder name. */
    public static final String GALLERIES_FOLDER_NAME = ".galleries";

    /** The configuration key for the functionDetail attribute in the container.info property. */
    public static final String KEY_FUNCTION_DETAIL = "functionDetail";

    /** The additional user info key for deleted list. */
    private static final String ADDINFO_ADE_DELETED_LIST = "ADE_DELETED_LIST";

    /** The additional user info key for modified list. */
    private static final String ADDINFO_ADE_MODIFIED_LIST = "ADE_MODIFIED_LIST";

    /** The lock table to prevent multiple users from editing the alias table concurrently. */
    private static CmsAliasEditorLockTable aliasEditorLockTable = new CmsAliasEditorLockTable();

    /** The table containing the alias import results. */
    private static CmsAliasImportResponseTable aliasImportResponseTable = new CmsAliasImportResponseTable();

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsSitemapService.class);

    /** The redirect recource type name. */
    private static final String RECOURCE_TYPE_NAME_REDIRECT = "htmlredirect";

    /** The redirect target XPath. */
    private static final String REDIRECT_LINK_TARGET_XPATH = "Link";

    /** Serialization uid. */
    private static final long serialVersionUID = -7236544324371767330L;

    /** The VFS path of the redirect copy page for navigation level entries. */
    private static final String SUB_LEVEL_REDIRECT_COPY_PAGE = "/system/modules/org.opencms.base/copyresources/sub-level-redirect.html";

    /** The navigation builder. */
    private CmsJspNavBuilder m_navBuilder;

    /**
     * Adds an alias import result.<p>
     *
     * @param results the list of alias import results to add
     *
     * @return the key to retrieve the alias import results
     */
    public static String addAliasImportResult(List<CmsAliasImportResult> results) {

        return aliasImportResponseTable.addImportResult(results);
    }

    /**
     * Creates a client property bean from a server-side property.<p>
     *
     * @param prop the property from which to create the client property
     * @param preserveOrigin if true, the origin will be copied into the new object
     *
     * @return the new client property
     */
    public static CmsClientProperty createClientProperty(CmsProperty prop, boolean preserveOrigin) {

        CmsClientProperty result = new CmsClientProperty(
            prop.getName(),
            prop.getStructureValue(),
            prop.getResourceValue());
        if (preserveOrigin) {
            result.setOrigin(prop.getOrigin());
        }
        return result;
    }

    /**
     * Fetches the sitemap data.<p>
     *
     * @param request the current request
     * @param sitemapUri the site relative path
     *
     * @return the sitemap data
     *
     * @throws CmsRpcException if something goes wrong
     */
    public static CmsSitemapData prefetch(HttpServletRequest request, String sitemapUri) throws CmsRpcException {

        CmsVfsSitemapService service = new CmsVfsSitemapService();
        service.setCms(CmsFlexController.getCmsObject(request));
        service.setRequest(request);
        CmsSitemapData result = null;
        try {
            result = service.prefetch(sitemapUri);
        } finally {
            service.clearThreadStorage();
        }
        return result;
    }

    /**
     * Converts a sequence of properties to a map of client-side property beans.<p>
     *
     * @param props the sequence of properties
     * @param preserveOrigin if true, the origin of the properties should be copied to the client properties
     *
     * @return the map of client properties
     *
     */
    static Map<String, CmsClientProperty> createClientProperties(Iterable<CmsProperty> props, boolean preserveOrigin) {

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        for (CmsProperty prop : props) {
            CmsClientProperty clientProp = createClientProperty(prop, preserveOrigin);
            result.put(prop.getName(), clientProp);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#changeCategory(java.lang.String, org.opencms.util.CmsUUID, java.lang.String, java.lang.String)
     */
    public void changeCategory(String entryPoint, CmsUUID id, String title, String name) throws CmsRpcException {

        try {
            name = OpenCms.getResourceManager().getFileTranslator().translateResource(name.trim().replace('/', '-'));
            CmsObject cms = getCmsObject();
            CmsResource categoryResource = cms.readResource(id);
            ensureLock(categoryResource);
            String sitePath = cms.getSitePath(categoryResource);
            String newPath = CmsStringUtil.joinPaths(CmsResource.getParentFolder(sitePath), name);
            cms.writePropertyObject(sitePath, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null));
            if (!CmsStringUtil.joinPaths("/", newPath, "/").equals(CmsStringUtil.joinPaths("/", sitePath, "/"))) {
                cms.moveResource(sitePath, newPath);
            }
        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.CmsGwtService#checkPermissions(org.opencms.file.CmsObject)
     */
    @Override
    public void checkPermissions(CmsObject cms) throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.EDITOR);
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#createCategory(java.lang.String, org.opencms.util.CmsUUID, java.lang.String, java.lang.String)
     */
    public void createCategory(String entryPoint, CmsUUID id, String title, String name) throws CmsRpcException {

        try {
            if ((name == null) || (name.length() == 0)) {
                name = title;
            }
            name = OpenCms.getResourceManager().getFileTranslator().translateResource(name.trim().replace('/', '-'));
            CmsObject cms = getCmsObject();
            CmsCategoryService catService = CmsCategoryService.getInstance();

            CmsCategory createdCategory = null;
            if (id.isNullUUID()) {
                String localRepositoryPath = CmsStringUtil.joinPaths(
                    entryPoint,
                    CmsCategoryService.getInstance().getRepositoryBaseFolderName(getCmsObject()));

                // ensure category repository exists
                if (!cms.existsResource(localRepositoryPath)) {
                    tryUnlock(
                        cms.createResource(
                            localRepositoryPath,
                            OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName())));
                }
                createdCategory = catService.createCategory(cms, null, name, title, "", localRepositoryPath);
            } else {
                CmsResource parentResource = cms.readResource(id);
                CmsCategory parent = catService.getCategory(cms, parentResource);
                createdCategory = catService.createCategory(
                    cms,
                    parent,
                    name,
                    title,
                    "",
                    cms.getRequestContext().removeSiteRoot(parentResource.getRootPath()));
            }
            tryUnlock(cms.readResource(createdCategory.getId()));
        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#createNewGalleryFolder(java.lang.String, java.lang.String, int)
     */
    public CmsGalleryFolderEntry createNewGalleryFolder(String parentFolder, String title, int folderTypeId)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsGalleryFolderEntry folderEntry = null;
        try {
            if (!cms.existsResource(parentFolder, CmsResourceFilter.IGNORE_EXPIRATION)) {
                CmsResource parent = cms.createResource(
                    parentFolder,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
                tryUnlock(parent);
            }
            String folderName = OpenCms.getResourceManager().getFileTranslator().translateResource(title);

            folderName = OpenCms.getResourceManager().getNameGenerator().getUniqueFileName(
                cms,
                parentFolder,
                folderName);
            String folderPath = CmsStringUtil.joinPaths(parentFolder, folderName);

            @SuppressWarnings("deprecation")
            CmsResource galleryFolder = cms.createResource(
                folderPath,
                folderTypeId,
                null,
                Collections.singletonList(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null)));
            folderEntry = readGalleryFolderEntry(
                galleryFolder,
                OpenCms.getResourceManager().getResourceType(galleryFolder).getTypeName());
            tryUnlock(galleryFolder);
        } catch (CmsException e) {
            error(e);
        }
        return folderEntry;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#createNewModelPage(java.lang.String, java.lang.String, java.lang.String, org.opencms.util.CmsUUID, boolean)
     */
    public CmsModelPageEntry createNewModelPage(
        String entryPointUri,
        String title,
        String description,
        CmsUUID copyId,
        boolean isModelGroup)
    throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource rootResource = cms.readResource(entryPointUri);
            CmsModelPageHelper helper = new CmsModelPageHelper(getCmsObject(), rootResource);
            CmsResource page;
            if (isModelGroup) {
                page = helper.createModelGroupPage(title, description, copyId);
            } else {
                page = helper.createPageInModelFolder(title, description, copyId);
                String configPath = CmsStringUtil.joinPaths(entryPointUri, ".content/.config");
                CmsResource configResource = cms.readResource(configPath);
                helper.addModelPageToSitemapConfiguration(configResource, page, false);
            }
            CmsModelPageEntry result = helper.createModelPageEntry(page, false, false, getWorkplaceLocale());
            OpenCms.getADEManager().waitForCacheUpdate(false);
            return result;
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#createSubSitemap(org.opencms.util.CmsUUID)
     */
    public CmsSitemapChange createSubSitemap(CmsUUID entryId) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            ensureSession();
            CmsResource subSitemapFolder = cms.readResource(entryId);
            ensureLock(subSitemapFolder);
            String sitePath = cms.getSitePath(subSitemapFolder);
            createSitemapContentFolder(cms, subSitemapFolder);
            subSitemapFolder.setType(getSubsitemapType());
            cms.writeResource(subSitemapFolder);
            tryUnlock(subSitemapFolder);
            CmsSitemapClipboardData clipboard = getClipboardData();
            CmsClientSitemapEntry entry = toClientEntry(
                getNavBuilder().getNavigationForResource(sitePath, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED),
                false);
            clipboard.addModified(entry);
            setClipboardData(clipboard);
            CmsSitemapChange result = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
            result.setUpdatedEntry(entry);
            return result;
        } catch (Exception e) {
            error(e);
        }
        return null;

    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#disableModelPage(java.lang.String, org.opencms.util.CmsUUID, boolean)
     */
    public void disableModelPage(String baseUri, CmsUUID modelPageId, boolean disabled) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource rootResource = cms.readResource(baseUri);
            CmsModelPageHelper helper = new CmsModelPageHelper(getCmsObject(), rootResource);
            String configPath = CmsStringUtil.joinPaths(baseUri, ".content/.config");
            CmsResource configResource = cms.readResource(configPath);
            helper.disableModelPage(configResource, modelPageId, disabled);
            OpenCms.getADEManager().waitForCacheUpdate(false);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#editAttributeData(org.opencms.util.CmsUUID)
     */
    public CmsSitemapAttributeData editAttributeData(CmsUUID rootId) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource root = cms.readResource(rootId);
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, root.getRootPath());
            String sitePath = cms.getSitePath(root);
            String configPath = CmsStringUtil.joinPaths(sitePath, "/.content/.config");
            CmsResource configResource = cms.readResource(configPath);
            cms.lockResourceTemporary(configResource);
            CmsListInfoBean configInfo = CmsVfsService.getPageInfo(cms, configResource);
            Map<String, CmsXmlContentProperty> definitions = config.getAttributeEditorConfiguration().getAttributeDefinitions();
            cms.getRequestContext().setAttribute(
                CmsRequestContext.ATTRIBUTE_ADE_CONTEXT_PATH,
                configResource.getRootPath());
            definitions = CmsXmlContentPropertyHelper.resolveMacrosInProperties(
                definitions,
                CmsMacroResolver.newWorkplaceLocaleResolver(cms));

            HashMap<String, String> values = new HashMap<>();
            for (String key : definitions.keySet()) {
                values.put(key, config.getAttribute(key, ""));
            }
            String unlockUrl = CmsStringUtil.joinPaths(
                OpenCms.getStaticExportManager().getVfsPrefix(),
                OpenCmsServlet.HANDLE_BUILTIN_SERVICE,
                CmsGwtConstants.UNLOCK_FILE_PREFIX,
                configResource.getStructureId().toString());
            CmsSitemapAttributeData result = new CmsSitemapAttributeData(configInfo, definitions, values, unlockUrl);
            return result;
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getAliasImportResult(java.lang.String)
     */
    public List<CmsAliasImportResult> getAliasImportResult(String resultKey) {

        return aliasImportResponseTable.getAndRemove(resultKey);
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getAliasTable()
     */
    public CmsAliasInitialFetchResult getAliasTable() throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsAliasManager aliasManager = OpenCms.getAliasManager();
            List<CmsAlias> aliases = aliasManager.getAliasesForSite(cms, cms.getRequestContext().getSiteRoot());
            List<CmsAliasTableRow> rows = new ArrayList<CmsAliasTableRow>();
            CmsAliasInitialFetchResult result = new CmsAliasInitialFetchResult();
            for (CmsAlias alias : aliases) {
                CmsAliasTableRow row = createAliasTableEntry(cms, alias);
                rows.add(row);
            }
            result.setRows(rows);

            List<CmsRewriteAlias> rewriteAliases = aliasManager.getRewriteAliases(
                cms,
                cms.getRequestContext().getSiteRoot());

            List<CmsRewriteAliasTableRow> rewriteRows = new ArrayList<CmsRewriteAliasTableRow>();
            for (CmsRewriteAlias rewriteAlias : rewriteAliases) {
                CmsRewriteAliasTableRow rewriteRow = new CmsRewriteAliasTableRow(
                    rewriteAlias.getId(),
                    rewriteAlias.getPatternString(),
                    rewriteAlias.getReplacementString(),
                    rewriteAlias.getMode());
                rewriteRows.add(rewriteRow);
            }
            result.setRewriteRows(rewriteRows);
            CmsUser otherLockOwner = aliasEditorLockTable.update(cms, cms.getRequestContext().getSiteRoot());
            if (otherLockOwner != null) {
                result.setAliasLockOwner(otherLockOwner.getName());
            }
            result.setDownloadUrl(OpenCms.getLinkManager().substituteLinkForRootPath(cms, ALIAS_DOWNLOAD_PATH));
            return result;
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getCategoryData(java.lang.String)
     */
    public CmsSitemapCategoryData getCategoryData(String entryPoint) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsResource entryPointResource = cms.readResource(entryPoint);
            String basePath = CmsStringUtil.joinPaths(
                entryPointResource.getRootPath(),
                CmsCategoryService.getInstance().getRepositoryBaseFolderName(getCmsObject()));

            List<CmsCategoryTreeEntry> entries = CmsCoreService.getCategoriesForSitePathStatic(
                cms,
                entryPoint,
                cms.getRequestContext().removeSiteRoot(basePath));
            CmsSitemapCategoryData categoryData = new CmsSitemapCategoryData();
            for (CmsCategoryTreeEntry entry : entries) {
                categoryData.add(entry);
            }

            categoryData.setBasePath(basePath);
            return categoryData;
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getChildren(java.lang.String, org.opencms.util.CmsUUID, int)
     */
    public CmsClientSitemapEntry getChildren(String entryPointUri, CmsUUID entryId, int levels) throws CmsRpcException {

        CmsClientSitemapEntry entry = null;

        try {
            CmsObject cms = getCmsObject();

            //ensure that root ends with a '/' if it's a folder
            CmsResource rootRes = cms.readResource(entryId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            String root = cms.getSitePath(rootRes);
            CmsJspNavElement navElement = getNavBuilder().getNavigationForResource(
                root,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            boolean isRoot = root.equals(entryPointUri);
            entry = toClientEntry(navElement, isRoot);
            if ((levels > 0) && (isRoot || (rootRes.isFolder()))) {
                entry.setSubEntries(getChildren(root, levels, null), null);
            }
        } catch (Throwable e) {
            error(e);
        }
        return entry;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getGalleryData(java.lang.String)
     */
    public Map<CmsGalleryType, List<CmsGalleryFolderEntry>> getGalleryData(String entryPointUri) {

        List<String> subSitePaths = OpenCms.getADEManager().getSubSitePaths(
            getCmsObject(),
            getCmsObject().getRequestContext().addSiteRoot(entryPointUri));
        List<CmsGalleryType> galleryTypes = collectGalleryTypes();
        Map<CmsGalleryType, List<CmsGalleryFolderEntry>> result = new HashMap<CmsGalleryType, List<CmsGalleryFolderEntry>>();
        for (CmsGalleryType type : galleryTypes) {
            List<CmsGalleryFolderEntry> galleries = null;
            try {
                galleries = getGalleriesForType(entryPointUri, type, subSitePaths);
            } catch (CmsException e) {
                log(e.getLocalizedMessage(), e);
            }
            result.put(type, galleries);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getModelInfos(org.opencms.util.CmsUUID)
     */
    public CmsModelInfo getModelInfos(CmsUUID rootId) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource rootResource = cms.readResource(rootId);
            CmsModelPageHelper modelPageHelper = new CmsModelPageHelper(getCmsObject(), rootResource);
            return modelPageHelper.getModelInfo();
        } catch (Throwable e) {
            error(e);
            return null; // will  never be reached
        }

    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getNewElementInfo(java.lang.String)
     */
    public List<CmsNewResourceInfo> getNewElementInfo(String entryPointUri) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            String rootPath = cms.getRequestContext().addSiteRoot(entryPointUri);
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, rootPath);
            List<CmsNewResourceInfo> result = getNewResourceInfos(cms, config, getWorkplaceLocale());
            return result;
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getResourceLink(org.opencms.util.CmsUUID, java.lang.String)
     */
    public String getResourceLink(CmsUUID baseId, String sitePath) throws CmsRpcException {

        try {
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            CmsResource baseResource = cms.readResource(baseId, CmsResourceFilter.IGNORE_EXPIRATION);
            String contextPath = cms.getSitePath(baseResource);
            cms.getRequestContext().setUri(contextPath);
            Locale locale = CmsJspTagLink.getBaseUriLocale(cms, contextPath);
            if (locale != null) {
                cms.getRequestContext().setLocale(locale);
            }
            String result = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, sitePath);
            return result;
        } catch (Exception e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#loadPropertyDataForLocaleCompareView(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public CmsLocaleComparePropertyData loadPropertyDataForLocaleCompareView(CmsUUID id, CmsUUID rootId)
    throws CmsRpcException {

        try {
            CmsLocaleComparePropertyData result = new CmsLocaleComparePropertyData();
            CmsObject cms = getCmsObject();
            CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION;
            CmsResource resource = cms.readResource(id, filter);
            CmsResource defaultFile = cms.readDefaultFile(id.toString());
            result.setDefaultFileId(defaultFile != null ? defaultFile.getStructureId() : null);
            result.setId(id);
            result.setFolder(resource.isFolder());
            List<CmsProperty> props = cms.readPropertyObjects(resource, false);
            List<CmsProperty> defaultFileProps = cms.readPropertyObjects(defaultFile, false);
            Map<String, CmsClientProperty> clientProps = createClientProperties(props, true);
            Map<String, CmsClientProperty> clientDefaultFileProps = createClientProperties(defaultFileProps, true);

            result.setOwnProperties(clientProps);
            result.setDefaultFileProperties(clientDefaultFileProps);
            List<CmsResource> blockingLocked = cms.getBlockingLockedResources(resource);

            CmsResource parent = cms.readParentFolder(resource.getStructureId());
            List<CmsResource> resourcesInSameFolder = cms.readResources(parent, CmsResourceFilter.ALL, false);
            List<String> forbiddenUrlNames = Lists.newArrayList();
            for (CmsResource resourceInSameFolder : resourcesInSameFolder) {
                String otherName = CmsResource.getName(resourceInSameFolder.getRootPath());
                forbiddenUrlNames.add(otherName);
            }
            result.setForbiddenUrlNames(forbiddenUrlNames);
            result.setInheritedProperties(createClientProperties(cms.readPropertyObjects(parent, true), true));
            result.setPath(resource.getRootPath());
            String name = CmsFileUtil.removeTrailingSeparator(CmsResource.getName(resource.getRootPath()));
            result.setName(name);
            result.setHasEditableName(!CmsStringUtil.isEmptyOrWhitespaceOnly(name) && blockingLocked.isEmpty());
            return result;
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#mergeSubSitemap(java.lang.String, org.opencms.util.CmsUUID)
     */
    @SuppressWarnings("deprecation")
    public CmsSitemapChange mergeSubSitemap(String entryPoint, CmsUUID subSitemapId) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            ensureSession();
            CmsResource subSitemapFolder = cms.readResource(subSitemapId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            ensureLock(subSitemapFolder);
            subSitemapFolder.setType(
                OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.RESOURCE_TYPE_NAME).getTypeId());
            cms.writeResource(subSitemapFolder);
            String sitePath = cms.getSitePath(subSitemapFolder);
            String sitemapConfigName = CmsStringUtil.joinPaths(
                sitePath,
                CmsADEManager.CONTENT_FOLDER_NAME,
                CmsADEManager.CONFIG_FILE_NAME);
            if (cms.existsResource(sitemapConfigName)) {
                cms.deleteResource(sitemapConfigName, CmsResource.DELETE_PRESERVE_SIBLINGS);
            }
            tryUnlock(subSitemapFolder);
            CmsSitemapClipboardData clipboard = getClipboardData();
            CmsClientSitemapEntry entry = toClientEntry(
                getNavBuilder().getNavigationForResource(
                    cms.getSitePath(subSitemapFolder),
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED),
                false);
            clipboard.addModified(entry);
            setClipboardData(clipboard);
            entry = getChildren(entryPoint, subSitemapId, 1);
            CmsSitemapChange result = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
            result.setUpdatedEntry(entry);
            return result;
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

        try {
            OpenCms.getRoleManager().checkRole(cms, CmsRole.EDITOR);
            String openPath = getRequest().getParameter(CmsCoreData.PARAM_PATH);
            if (!isValidOpenPath(cms, openPath)) {
                // if no path is supplied, start from root
                openPath = "/";
            }
            CmsQuickLaunchLocationCache.getLocationCache(getRequest().getSession()).setSitemapEditorLocation(
                cms.getRequestContext().getSiteRoot(),
                openPath);
            CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().addSiteRoot(openPath));
            Map<String, CmsXmlContentProperty> propertyConfig = new LinkedHashMap<String, CmsXmlContentProperty>(
                configData.getPropertyConfigurationAsMap());
            propertyConfig = CmsXmlContentPropertyHelper.resolveMacrosInProperties(
                propertyConfig,
                CmsMacroResolver.newWorkplaceLocaleResolver(getCmsObject()));

            Map<String, CmsClientProperty> parentProperties = generateParentProperties(configData.getBasePath());
            String siteRoot = cms.getRequestContext().getSiteRoot();
            String exportRfsPrefix = OpenCms.getStaticExportManager().getDefaultRfsPrefix();
            CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
            boolean isSecure = (site != null) && site.hasSecureServer();
            String parentSitemap = null;

            if (configData.getBasePath() != null) {
                CmsADEConfigData parentConfigData = OpenCms.getADEManager().lookupConfiguration(
                    cms,
                    CmsResource.getParentFolder(configData.getBasePath()));
                parentSitemap = parentConfigData.getBasePath();
                if (parentSitemap != null) {
                    parentSitemap = cms.getRequestContext().removeSiteRoot(parentSitemap);
                }
            }
            String noEdit = "";
            CmsNewResourceInfo defaultNewInfo = null;
            List<CmsNewResourceInfo> newResourceInfos = null;
            CmsDetailPageTable detailPages = null;
            List<CmsNewResourceInfo> resourceTypeInfos = null;
            boolean canEditDetailPages = false;
            boolean isOnlineProject = CmsProject.isOnlineProject(cms.getRequestContext().getCurrentProject().getUuid());
            String defaultGalleryFolder = GALLERIES_FOLDER_NAME;
            try {
                CmsObject rootCms = OpenCms.initCmsObject(cms);
                rootCms.getRequestContext().setSiteRoot("");
                CmsResource baseDir = rootCms.readResource(
                    configData.getBasePath(),
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                CmsProperty galleryFolderProp = cms.readPropertyObject(
                    baseDir,
                    CmsPropertyDefinition.PROPERTY_GALLERIES_FOLDER,
                    true);
                if (!galleryFolderProp.isNullProperty()
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly(galleryFolderProp.getValue())) {
                    defaultGalleryFolder = galleryFolderProp.getValue();
                }
                CmsPropertyEditorHelper.updateWysiwygConfig(propertyConfig, cms, baseDir);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }

            detailPages = new CmsDetailPageTable(configData.getAllDetailPages());
            if (!isOnlineProject) {
                newResourceInfos = getNewResourceInfos(cms, configData, getWorkplaceLocale());
                CmsResource modelResource = null;
                if (configData.getDefaultModelPage() != null) {
                    if (cms.existsResource(configData.getDefaultModelPage().getResource().getStructureId())) {
                        modelResource = configData.getDefaultModelPage().getResource();
                    } else {
                        try {
                            modelResource = cms.readResource(
                                cms.getSitePath(configData.getDefaultModelPage().getResource()),
                                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                        } catch (CmsException e) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                    }
                }
                if ((modelResource == null) && !newResourceInfos.isEmpty()) {
                    try {
                        modelResource = cms.readResource(newResourceInfos.get(0).getCopyResourceId());
                    } catch (CmsException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
                CmsFormatterConfigurationCacheState formatterState = OpenCms.getADEManager().getCachedFormatters(
                    cms.getRequestContext().getCurrentProject().isOnlineProject());
                if (modelResource != null) {
                    resourceTypeInfos = getResourceTypeInfos(
                        getCmsObject(),
                        configData.getResourceTypes(),
                        configData.getFunctionReferences(),
                        configData.getDynamicFunctionAvailability(formatterState),
                        modelResource,
                        getWorkplaceLocale());
                    try {
                        defaultNewInfo = createNewResourceInfo(cms, modelResource, getWorkplaceLocale());
                    } catch (CmsException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
                canEditDetailPages = !(configData.isModuleConfiguration());
            }
            if (isOnlineProject) {
                noEdit = Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_SITEMAP_NO_EDIT_ONLINE_0);
            }
            List<String> allPropNames = getPropertyNames(cms);
            String returnCode = getRequest().getParameter(CmsCoreData.PARAM_RETURNCODE);
            if (!isValidReturnCode(returnCode)) {
                returnCode = null;
            }
            cms.getRequestContext().getSiteRoot();
            String aliasImportUrl = OpenCms.getLinkManager().substituteLinkForRootPath(cms, ALIAS_IMPORT_PATH);
            boolean canEditAliases = OpenCms.getAliasManager().hasPermissionsForMassEdit(cms, siteRoot);
            List<CmsListInfoBean> subsitemapFolderTypeInfos = collectSitemapTypeInfos(cms, configData);

            // evaluate the editor mode
            EditorMode editorMode = CmsADESessionCache.getCache(getRequest(), getCmsObject()).getSitemapEditorMode();
            if ((editorMode == null) || (editorMode == EditorMode.compareLocales)) {
                editorMode = EditorMode.navigation;
            }

            String basePath = configData.getBasePath();

            if (!cms.existsResource(
                cms.getRequestContext().removeSiteRoot(basePath),
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                basePath = cms.getRequestContext().getSiteRoot();
            }
            Boolean showModelEditConfirm = (Boolean)getRequest().getSession().getAttribute(
                ATTR_SHOW_MODEL_EDIT_CONFIRM);
            if (showModelEditConfirm == null) {
                showModelEditConfirm = Boolean.TRUE;
            }
            boolean showLocaleComparison = !(getCmsObject().getRequestContext().getCurrentProject().isOnlineProject())
                && (site != null)
                && (site.getMainTranslationLocale(null) != null);

            result = new CmsSitemapData(
                (new CmsTemplateFinder(cms)).getTemplates(),
                propertyConfig,
                getClipboardData(),
                CmsCoreService.getContextMenuEntries(
                    cms,
                    configData.getResource().getStructureId(),
                    AdeContext.sitemapeditor,
                    new HashMap<>()),
                parentProperties,
                allPropNames,
                exportRfsPrefix,
                isSecure,
                noEdit,
                isDisplayToolbar(getRequest()),
                defaultNewInfo,
                newResourceInfos,
                createResourceTypeInfo(OpenCms.getResourceManager().getResourceType(RECOURCE_TYPE_NAME_REDIRECT), null),
                createNavigationLevelTypeInfo(),
                getSitemapInfo(basePath),
                parentSitemap,
                getRootEntry(basePath, CmsResource.getFolderPath(openPath)),
                openPath,
                30,
                detailPages,
                resourceTypeInfos,
                returnCode,
                canEditDetailPages,
                aliasImportUrl,
                canEditAliases,
                OpenCms.getWorkplaceManager().getDefaultUserSettings().getSubsitemapCreationMode() == CmsDefaultUserSettings.SubsitemapCreationMode.createfolder,
                OpenCms.getRoleManager().hasRole(cms, CmsRole.GALLERY_EDITOR),
                OpenCms.getRoleManager().hasRole(cms, CmsRole.CATEGORY_EDITOR),
                subsitemapFolderTypeInfos,
                editorMode,
                defaultGalleryFolder,
                showModelEditConfirm.booleanValue());

            CmsUUID rootId = cms.readResource("/", CmsResourceFilter.ALL).getStructureId();
            result.setSiteRootId(rootId);
            result.setLocaleComparisonEnabled(showLocaleComparison);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#prepareReloadSitemap(org.opencms.util.CmsUUID, org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode)
     */
    public String prepareReloadSitemap(CmsUUID rootId, EditorMode mode) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource res = cms.readResource(CmsADEManager.PATH_SITEMAP_EDITOR_JSP);

            CmsResource target = cms.readResource(rootId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            String targetRootPath = OpenCms.getADEManager().getSubSiteRoot(cms, target.getRootPath());
            CmsSite targetSite = OpenCms.getSiteManager().getSiteForRootPath(targetRootPath);
            CmsADESessionCache.getCache(getRequest(), getCmsObject()).setSitemapEditorMode(mode);
            if (targetSite != null) {
                cms.getRequestContext().setSiteRoot(targetSite.getSiteRoot());
                String path = cms.getRequestContext().removeSiteRoot(targetRootPath);
                String link = OpenCms.getLinkManager().substituteLink(cms, res);
                link = link + "?path=" + path;
                return link;
            }
            return null;
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#removeModelPage(java.lang.String, org.opencms.util.CmsUUID)
     */
    public void removeModelPage(String entryPointUri, CmsUUID id) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource rootResource = cms.readResource(entryPointUri);
            CmsModelPageHelper helper = new CmsModelPageHelper(getCmsObject(), rootResource);
            String configPath = CmsStringUtil.joinPaths(entryPointUri, ".content/.config");
            CmsResource configResource = cms.readResource(configPath);
            helper.removeModelPage(configResource, id);
            OpenCms.getADEManager().waitForCacheUpdate(false);
        } catch (Throwable e) {
            error(e);

        }

    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#save(java.lang.String, org.opencms.ade.sitemap.shared.CmsSitemapChange)
     */
    public CmsSitemapChange save(String entryPoint, CmsSitemapChange change) throws CmsRpcException {

        CmsSitemapChange result = null;
        try {
            result = saveInternal(entryPoint, change);
        } catch (Exception e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#saveAliases(org.opencms.gwt.shared.alias.CmsAliasSaveValidationRequest)
     */
    public CmsAliasEditValidationReply saveAliases(CmsAliasSaveValidationRequest saveRequest) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsAliasBulkEditHelper helper = new CmsAliasBulkEditHelper(cms);
        try {
            return helper.saveAliases(saveRequest);
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#savePropertiesForLocaleCompareMode(org.opencms.util.CmsUUID, java.lang.String, java.util.List, boolean)
     */
    public void savePropertiesForLocaleCompareMode(
        CmsUUID id,
        String newUrlName,
        List<CmsPropertyModification> propertyChanges,
        boolean editedName)
    throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource ownRes = cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsResource defaultFileRes = cms.readDefaultFile("" + id);
            boolean shallow = !editedName;
            try (AutoCloseable c = CmsLockUtil.withLockedResources(cms, shallow, ownRes, defaultFileRes)) {
                updateProperties(cms, ownRes, defaultFileRes, propertyChanges);
                if (editedName) {
                    String parent = CmsResource.getParentFolder(ownRes.getRootPath());
                    newUrlName = CmsFileUtil.removeTrailingSeparator(newUrlName);
                    String newPath = CmsStringUtil.joinPaths(parent, newUrlName);
                    CmsObject rootCms = OpenCms.initCmsObject(cms);
                    rootCms.getRequestContext().setSiteRoot("");
                    rootCms.moveResource(ownRes.getRootPath(), newPath);
                }
            }
        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#saveSitemapAttributes(org.opencms.util.CmsUUID, java.util.Map)
     */
    public void saveSitemapAttributes(CmsUUID rootId, Map<String, String> attributes) throws CmsRpcException {

        CmsResource configResource = null;
        try {
            CmsObject cms = getCmsObject();
            CmsResource root = cms.readResource(rootId);
            String sitePath = cms.getSitePath(root);
            String configPath = CmsStringUtil.joinPaths(sitePath, "/.content/.config");
            configResource = cms.readResource(configPath);
            CmsXmlContent configContent = CmsXmlContentFactory.unmarshal(cms, cms.readFile(configResource));
            CmsSitemapAttributeUpdater updater = new CmsSitemapAttributeUpdater(cms, configContent);
            if (updater.saveAttributesFromEditorDialog(attributes)) {
                byte[] newContentBytes = configContent.marshal();
                CmsFile file = configContent.getFile();
                file.setContents(newContentBytes);
                cms.writeFile(file);
                OpenCms.getADEManager().waitForCacheUpdate(false);
            }
        } catch (Exception e) {
            error(e);
        } finally {
            try {
                if (configResource != null) {
                    getCmsObject().unlockResource(configResource);
                }
            } catch (Exception e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }

    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#setDefaultModel(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public CmsModelInfo setDefaultModel(CmsUUID rootId, CmsUUID modelId) throws CmsRpcException {

        CmsModelInfo result = null;
        try {
            CmsObject cms = getCmsObject();
            CmsResource rootResource = cms.readResource(rootId);
            CmsModelPageHelper modelPageHelper = new CmsModelPageHelper(getCmsObject(), rootResource);
            CmsResource configFile = OpenCms.getADEManager().lookupConfiguration(
                cms,
                rootResource.getRootPath()).getResource();
            ensureLock(configFile);
            result = modelPageHelper.setDefaultModelPage(configFile, modelId);
            tryUnlock(configFile);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#setEditorMode(org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode)
     */
    public void setEditorMode(EditorMode editorMode) {

        CmsADESessionCache.getCache(getRequest(), getCmsObject()).setSitemapEditorMode(editorMode);
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#updateAliasEditorStatus(boolean)
     */
    public void updateAliasEditorStatus(boolean editing) {

        CmsObject cms = getCmsObject();
        if (editing) {
            aliasEditorLockTable.update(cms, cms.getRequestContext().getSiteRoot());
        } else {
            aliasEditorLockTable.clear(cms, cms.getRequestContext().getSiteRoot());
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#validateAliases(org.opencms.gwt.shared.alias.CmsAliasEditValidationRequest)
     */
    public CmsAliasEditValidationReply validateAliases(CmsAliasEditValidationRequest validationRequest) {

        CmsObject cms = getCmsObject();
        CmsAliasBulkEditHelper helper = new CmsAliasBulkEditHelper(cms);
        return helper.validateAliases(validationRequest);
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#validateRewriteAliases(org.opencms.gwt.shared.alias.CmsRewriteAliasValidationRequest)
     */
    public CmsRewriteAliasValidationReply validateRewriteAliases(CmsRewriteAliasValidationRequest validationRequest) {

        CmsRewriteAliasValidationReply result = new CmsRewriteAliasValidationReply();
        for (CmsRewriteAliasTableRow editedRow : validationRequest.getEditedRewriteAliases()) {
            try {
                String patternString = editedRow.getPatternString();
                Pattern.compile(patternString);
            } catch (PatternSyntaxException e) {
                result.addError(editedRow.getId(), "Syntax error in regular expression: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * Locks the given resource with a temporary, if not already locked by the current user.
     * Will throw an exception if the resource could not be locked for the current user.<p>
     *
     * @param resource the resource to lock
     *
     * @return the assigned lock
     *
     * @throws CmsException if the resource could not be locked
     */
    protected LockInfo ensureLockAndGetInfo(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        boolean justLocked = false;
        List<CmsResource> blockingResources = cms.getBlockingLockedResources(resource);
        if ((blockingResources != null) && !blockingResources.isEmpty()) {
            throw new CmsException(
                org.opencms.gwt.Messages.get().container(
                    org.opencms.gwt.Messages.ERR_RESOURCE_HAS_BLOCKING_LOCKED_CHILDREN_1,
                    cms.getSitePath(resource)));
        }
        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsLock lock = cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            cms.lockResourceTemporary(resource);
            lock = cms.getLock(resource);
            justLocked = true;
        } else if (!lock.isOwnedInProjectBy(user, cms.getRequestContext().getCurrentProject())) {
            cms.changeLock(resource);
            lock = cms.getLock(resource);
            justLocked = true;
        }
        return new LockInfo(lock, justLocked);
    }

    /**
     * Internal method for saving a sitemap.<p>
     *
     * @param entryPoint the URI of the sitemap to save
     * @param change the change to save
     *
     * @return list of changed sitemap entries
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsSitemapChange saveInternal(String entryPoint, CmsSitemapChange change) throws CmsException {

        ensureSession();
        switch (change.getChangeType()) {
            case clipboardOnly:
                // do nothing
                break;
            case remove:
                change = removeEntryFromNavigation(change);
                break;
            case undelete:
                change = undelete(change);
                break;
            default:
                change = applyChange(entryPoint, change);
        }
        setClipboardData(change.getClipBoardData());
        return change;
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
        Map<String, CmsClientProperty> result = createClientProperties(props, false);
        return result;
    }

    /**
     * Adds a function detail element to a container page.<p>
     *
     * @param cms the current CMS context
     * @param page the container page which should be changed
     * @param containerName the name of the container which should be used for function detail elements
     * @param elementId the structure id of the element to add
     * @param formatterId the structure id of the formatter for the element
     *
     * @throws CmsException if something goes wrong
     */
    private void addFunctionDetailElement(
        CmsObject cms,
        CmsXmlContainerPage page,
        String containerName,
        CmsUUID elementId,
        CmsUUID formatterId)
    throws CmsException {

        CmsContainerPageBean bean = page.getContainerPage(cms);
        List<CmsContainerBean> containerBeans = new ArrayList<CmsContainerBean>();
        Collection<CmsContainerBean> originalContainers = bean.getContainers().values();
        if ((containerName == null) && !originalContainers.isEmpty()) {
            CmsContainerBean firstContainer = originalContainers.iterator().next();
            containerName = firstContainer.getName();
        }
        boolean foundContainer = false;
        for (CmsContainerBean cntBean : originalContainers) {
            boolean isDetailTarget = cntBean.getName().equals(containerName);
            if (isDetailTarget && !foundContainer) {
                foundContainer = true;
                List<CmsContainerElementBean> newElems = new ArrayList<CmsContainerElementBean>();
                newElems.addAll(cntBean.getElements());
                CmsContainerElementBean newElement = new CmsContainerElementBean(
                    elementId,
                    formatterId,
                    new HashMap<String, String>(),
                    false);
                newElems.add(0, newElement);
                CmsContainerBean newCntBean = cntBean.copyWithNewElements(newElems);
                containerBeans.add(newCntBean);
            } else {
                containerBeans.add(cntBean);
            }
        }
        if (!foundContainer) {
            throw new CmsException(
                Messages.get().container(Messages.ERR_NO_FUNCTION_DETAIL_CONTAINER_1, page.getFile().getRootPath()));
        }
        CmsContainerPageBean bean2 = new CmsContainerPageBean(new ArrayList<CmsContainerBean>(containerBeans));
        page.writeContainerPage(cms, bean2);

    }

    /**
     * Applys the given change to the VFS.<p>
     *
     * @param entryPoint the sitemap entry-point
     * @param change the change
     *
     * @return the updated entry
     *
     * @throws CmsException if something goes wrong
     */
    private CmsSitemapChange applyChange(String entryPoint, CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource configFile = null;
        // lock the config file first, to avoid half done changes
        if (change.hasDetailPageInfos()) {
            CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().addSiteRoot(entryPoint));
            if (!configData.isModuleConfiguration() && (configData.getResource() != null)) {
                configFile = configData.getResource();
            }
            if (configFile != null) {
                ensureLock(configFile);
            }
        }
        if (change.isNew()) {
            CmsClientSitemapEntry newEntry = createNewEntry(entryPoint, change);
            change.setUpdatedEntry(newEntry);
            change.setEntryId(newEntry.getId());
        } else if (change.getChangeType() == ChangeType.delete) {
            delete(change);
        } else if (change.getEntryId() != null) {
            modifyEntry(change);
        }
        if (change.hasDetailPageInfos() && (configFile != null)) {
            saveDetailPages(change.getDetailPageInfos(), configFile, change.getEntryId(), change.getUpdatedEntry());
            tryUnlock(configFile);
        }

        return change;
    }

    /**
     * Changes the navigation for a moved entry and its neighbors.<p>
     *
     * @param change the sitemap change
     * @param entryFolder the moved entry
     *
     * @throws CmsException if something goes wrong
     */
    private void applyNavigationChanges(CmsSitemapChange change, CmsResource entryFolder) throws CmsException {

        CmsObject cms = getCmsObject();
        String parentPath = null;
        if (change.hasNewParent()) {
            CmsResource parent = cms.readResource(change.getParentId());
            parentPath = cms.getSitePath(parent);
        } else {
            parentPath = CmsResource.getParentFolder(cms.getSitePath(entryFolder));
        }
        List<CmsJspNavElement> navElements = getNavBuilder().getNavigationForFolder(
            parentPath,
            Visibility.all,
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(navElements, entryFolder, change.getPosition());
        List<CmsJspNavElement> navs = npc.getNavigationChanges();
        List<CmsResource> needToUnlock = new ArrayList<CmsResource>();

        try {
            for (CmsJspNavElement nav : navs) {
                LockInfo lockInfo = ensureLockAndGetInfo(nav.getResource());
                if (!nav.getResource().equals(entryFolder) && lockInfo.wasJustLocked()) {
                    needToUnlock.add(nav.getResource());
                }
            }
            for (CmsJspNavElement nav : navs) {
                CmsProperty property = new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_NAVPOS,
                    "" + nav.getNavPosition(),
                    null);
                cms.writePropertyObject(cms.getSitePath(nav.getResource()), property);
            }
        } finally {
            for (CmsResource lockedRes : needToUnlock) {
                try {
                    cms.unlockResource(lockedRes);
                } catch (CmsException e) {
                    // we catch this because we still want to unlock the other resources
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Builds the list info bean for a resource type which can be used as a sub-sitemap folder.<p>
     *
     * @param sitemapType the sitemap folder type
     *
     * @return the list info bean for the given type
     */
    private CmsListInfoBean buildSitemapTypeInfo(I_CmsResourceType sitemapType) {

        CmsListInfoBean result = new CmsListInfoBean();
        CmsWorkplaceManager wm = OpenCms.getWorkplaceManager();
        String typeName = sitemapType.getTypeName();
        Locale wpLocale = wm.getWorkplaceLocale(getCmsObject());
        String title = typeName;
        String description = "";
        try {
            title = CmsWorkplaceMessages.getResourceTypeName(wpLocale, typeName);
        } catch (Throwable e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        try {
            description = CmsWorkplaceMessages.getResourceTypeDescription(wpLocale, typeName);
        } catch (Throwable e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        result.setResourceType(typeName);
        result.setTitle(title);
        result.setSubTitle(description);
        return result;
    }

    /**
     * Returns all available gallery folder resource types.<p>
     *
     * @return the gallery folder resource types
     */
    @SuppressWarnings("deprecation")
    private List<CmsGalleryType> collectGalleryTypes() {

        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        Map<String, List<String>> galleryContentTypes = new HashMap<String, List<String>>();
        List<CmsGalleryType> result = new ArrayList<CmsGalleryType>();
        for (I_CmsResourceType resourceType : OpenCms.getResourceManager().getResourceTypes()) {
            if (resourceType instanceof CmsResourceTypeFolderExtended) {
                // found a configured extended folder resource type
                CmsResourceTypeFolderExtended galleryType = (CmsResourceTypeFolderExtended)resourceType;
                String folderClassName = galleryType.getFolderClassName();
                if (CmsStringUtil.isNotEmpty(folderClassName)) {
                    // only process this as a gallery if the folder name is not empty
                    try {
                        // check, if the folder class is a subclass of A_CmsGallery
                        if (A_CmsAjaxGallery.class.isAssignableFrom(Class.forName(folderClassName))) {
                            CmsGalleryType gallery = new CmsGalleryType();
                            gallery.setTypeId(resourceType.getTypeId());
                            gallery.setResourceType(resourceType.getTypeName());
                            gallery.setTitle(
                                CmsWorkplaceMessages.getResourceTypeName(wpLocale, resourceType.getTypeName()));
                            gallery.setSubTitle(
                                CmsWorkplaceMessages.getResourceTypeDescription(wpLocale, resourceType.getTypeName()));
                            gallery.setBigIconClasses(
                                CmsIconUtil.getIconClasses(resourceType.getTypeName(), null, false));
                            result.add(gallery);
                        }
                    } catch (Exception e) {
                        this.log(e.getLocalizedMessage(), e);
                    }
                }
            } else {
                List<I_CmsResourceType> galleryTypes = resourceType.getGalleryTypes();
                if ((galleryTypes != null) && !galleryTypes.isEmpty()) {
                    for (I_CmsResourceType galleryType : galleryTypes) {
                        List<String> typeList = galleryContentTypes.get(galleryType.getTypeName());
                        if (typeList == null) {
                            typeList = new ArrayList<String>();
                            galleryContentTypes.put(galleryType.getTypeName(), typeList);
                        }
                        typeList.add(resourceType.getTypeName());
                    }
                }
            }
        }
        for (CmsGalleryType galleryType : result) {
            galleryType.setContentTypeNames(galleryContentTypes.get(galleryType.getResourceType()));
        }
        return result;
    }

    /**
     * Gets the information for the available sitemap folder types.<p>
     *
     * @param cms the current CMS context
     * @param configData the configuration data for the current subsitemap
     * @return the list info beans corresponding to available sitemap folder types
     */
    private List<CmsListInfoBean> collectSitemapTypeInfos(CmsObject cms, CmsADEConfigData configData) {

        List<CmsListInfoBean> subsitemapFolderTypeInfos = new ArrayList<CmsListInfoBean>();
        List<Integer> sitemapTypeIds = CmsResourceTypeFolderSubSitemap.getSubSitemapResourceTypeIds();
        String checkPath = configData.getBasePath();
        if (checkPath != null) {
            checkPath = cms.getRequestContext().removeSiteRoot(checkPath);
        } else {
            checkPath = "/";
        }
        CmsResource checkResource = null;
        try {
            checkResource = cms.readResource(checkPath);
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }

        for (Integer typeId : sitemapTypeIds) {
            try {
                I_CmsResourceType sitemapType = OpenCms.getResourceManager().getResourceType(typeId.intValue());
                String typeName = sitemapType.getTypeName();
                CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);

                // if explorer type and check resource available, perform a permission check
                if ((explorerType != null) && (checkResource != null)) {
                    try {
                        CmsExplorerTypeAccess access = explorerType.getAccess();
                        if (!access.getPermissions(cms, checkResource).requiresControlPermission()) {
                            continue;
                        }
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                CmsListInfoBean infoBean = buildSitemapTypeInfo(sitemapType);
                subsitemapFolderTypeInfos.add(infoBean);

            } catch (CmsLoaderException e) {
                LOG.warn("Could not read sitemap folder type " + typeId + ": " + e.getLocalizedMessage(), e);
            }
        }
        return subsitemapFolderTypeInfos;
    }

    /**
     * Creates a client alias bean from a server-side alias.<p>
     *
     * @param cms the current CMS context
     * @param alias the alias to convert
     *
     * @return the alias table row
     *
     * @throws CmsException if something goes wrong
     */
    private CmsAliasTableRow createAliasTableEntry(CmsObject cms, CmsAlias alias) throws CmsException {

        CmsResource resource = cms.readResource(alias.getStructureId(), CmsResourceFilter.ALL);
        CmsAliasTableRow result = new CmsAliasTableRow();
        result.setStructureId(alias.getStructureId());
        result.setOriginalStructureId(alias.getStructureId());
        result.setAliasPath(alias.getAliasPath());
        result.setResourcePath(cms.getSitePath(resource));
        result.setKey((new CmsUUID()).toString());
        result.setMode(alias.getMode());
        result.setChanged(false);
        return result;
    }

    /**
     * Creates a navigation level type info.<p>
     *
     * @return the navigation level type info bean
     *
     * @throws CmsException if reading the sub level redirect copy page fails
     */
    private CmsNewResourceInfo createNavigationLevelTypeInfo() throws CmsException {

        String name = CmsResourceTypeFolder.getStaticTypeName();
        Locale locale = getWorkplaceLocale();
        String subtitle = Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_NAVIGATION_LEVEL_SUBTITLE_0);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(subtitle)) {
            subtitle = CmsWorkplaceMessages.getResourceTypeName(locale, name);
        }
        CmsNewResourceInfo result = new CmsNewResourceInfo(
            CmsResourceTypeFolder.getStaticTypeId(),
            name,
            Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_NAVIGATION_LEVEL_TITLE_0),
            subtitle,
            getCmsObject().readResource(SUB_LEVEL_REDIRECT_COPY_PAGE).getStructureId(),
            false,
            subtitle);
        result.setBigIconClasses(CmsIconUtil.ICON_NAV_LEVEL_BIG);
        result.setCreateParameter(CmsJspNavBuilder.NAVIGATION_LEVEL_FOLDER);
        return result;
    }

    /**
     * Creates new content elements if required by the model page.<p>
     *
     * @param cms the cms context
     * @param page the page
     * @param sitePath the resource site path
     *
     * @throws CmsException when unable to create the content elements
     */
    private boolean createNewContainerElements(CmsObject cms, CmsMutableContainerPage page, String sitePath)
    throws CmsException {

        boolean needsChanges = false;
        CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(cms, cms.addSiteRoot(sitePath));
        Locale contentLocale = OpenCms.getLocaleManager().getDefaultLocale(cms, CmsResource.getFolderPath(sitePath));
        CmsObject cloneCms = OpenCms.initCmsObject(cms);
        cloneCms.getRequestContext().setLocale(contentLocale);
        for (CmsMutableContainer container : page.containers()) {
            ListIterator<CmsContainerElementBean> iter = container.elements().listIterator();
            while (iter.hasNext()) {
                CmsContainerElementBean elem = iter.next();
                if (elem.isCreateNew() && !elem.isGroupContainer(cms) && !elem.isInheritedContainer(cms)) {
                    String typeName = OpenCms.getResourceManager().getResourceType(elem.getResource()).getTypeName();
                    CmsResourceTypeConfig typeConfig = configData.getResourceType(typeName);
                    if (typeConfig == null) {
                        throw new IllegalArgumentException(
                            "Can not copy template model element '"
                                + elem.getResource().getRootPath()
                                + "' because the resource type '"
                                + typeName
                                + "' is not available in this sitemap.");
                    }

                    CmsResource newResource = typeConfig.createNewElement(
                        cloneCms,
                        elem.getResource(),
                        CmsResource.getParentFolder(cms.getRequestContext().addSiteRoot(sitePath)));
                    CmsContainerElementBean newBean = new CmsContainerElementBean(
                        newResource.getStructureId(),
                        elem.getFormatterId(),
                        elem.getIndividualSettings(),
                        false);
                    iter.set(newBean);
                    needsChanges = true;
                }
            }
        }
        return needsChanges;
    }

    /**
     * Creates new content elements if required by the model page.<p>
     *
     * @param cms the cms context
     * @param page the page
     * @param sitePath the resource site path
     *
     * @throws CmsException when unable to create the content elements
     */
    private void createNewContainerElements(CmsObject cms, CmsXmlContainerPage page, String sitePath)
    throws CmsException {

        CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(cms, cms.addSiteRoot(sitePath));
        Locale contentLocale = OpenCms.getLocaleManager().getDefaultLocale(cms, CmsResource.getFolderPath(sitePath));
        CmsObject cloneCms = OpenCms.initCmsObject(cms);
        cloneCms.getRequestContext().setLocale(contentLocale);
        CmsContainerPageBean pageBean = page.getContainerPage(cms);
        boolean needsChanges = false;
        List<CmsContainerBean> updatedContainers = new ArrayList<CmsContainerBean>();
        for (CmsContainerBean container : pageBean.getContainers().values()) {
            List<CmsContainerElementBean> updatedElements = new ArrayList<CmsContainerElementBean>();
            for (CmsContainerElementBean element : container.getElements()) {
                if (element.isCreateNew() && !element.isGroupContainer(cms) && !element.isInheritedContainer(cms)) {
                    needsChanges = true;
                    String typeName = OpenCms.getResourceManager().getResourceType(element.getResource()).getTypeName();
                    CmsResourceTypeConfig typeConfig = configData.getResourceType(typeName);
                    if (typeConfig == null) {
                        throw new IllegalArgumentException(
                            "Can not copy template model element '"
                                + element.getResource().getRootPath()
                                + "' because the resource type '"
                                + typeName
                                + "' is not available in this sitemap.");
                    }

                    CmsResource newResource = typeConfig.createNewElement(
                        cloneCms,
                        element.getResource(),
                        CmsResource.getParentFolder(cms.getRequestContext().addSiteRoot(sitePath)));
                    CmsContainerElementBean newBean = new CmsContainerElementBean(
                        newResource.getStructureId(),
                        element.getFormatterId(),
                        element.getIndividualSettings(),
                        false);
                    updatedElements.add(newBean);
                } else {
                    updatedElements.add(element);
                }
            }

            CmsContainerBean updatedContainer = container.copyWithNewElements(updatedElements);
            updatedContainers.add(updatedContainer);
        }
        if (needsChanges) {
            CmsContainerPageBean updatedPage = new CmsContainerPageBean(updatedContainers);
            page.writeContainerPage(cms, updatedPage);
        }
    }

    /**
     * Creates a new page in navigation.<p>
     *
     * @param entryPoint the site-map entry-point
     * @param change the new change
     *
     * @return the updated entry
     *
     * @throws CmsException if something goes wrong
     */
    @SuppressWarnings("deprecation")
    private CmsClientSitemapEntry createNewEntry(String entryPoint, CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsClientSitemapEntry newEntry = null;
        if (change.getParentId() != null) {
            CmsResource parentFolder = cms.readResource(
                change.getParentId(),
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            String entryPath = "";
            CmsResource entryFolder = null;
            CmsResource newRes = null;
            byte[] content = null;
            List<CmsProperty> properties = Collections.emptyList();
            CmsResource copyPage = null;
            if (change.getNewCopyResourceId() != null) {
                copyPage = cms.readResource(change.getNewCopyResourceId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                content = cms.readFile(copyPage).getContents();
                properties = cms.readPropertyObjects(copyPage, false);
            }
            List<CmsProperty> filteredProperties = new ArrayList<CmsProperty>();
            for (CmsProperty property : properties) {
                boolean filter = false;
                if (FILTER_PROPERTIES.contains(property.getName())) {
                    filter = true;

                } else {
                    // filter localized versions also
                    for (String filterProp : FILTER_PROPERTIES) {
                        if (property.getName().startsWith(filterProp + "_")) {
                            filter = true;
                            break;
                        }
                    }
                }
                if (!filter) {
                    filteredProperties.add(property);
                }
            }
            properties = filteredProperties;
            if (isRedirectType(change.getNewResourceTypeId())) {
                entryPath = CmsStringUtil.joinPaths(cms.getSitePath(parentFolder), change.getName());
                newRes = cms.createResource(
                    entryPath,
                    change.getNewResourceTypeId(),
                    null,
                    Collections.singletonList(
                        new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, change.getName(), null)));
                cms.writePropertyObjects(newRes, generateInheritProperties(change, newRes));
                applyNavigationChanges(change, newRes);
            } else {
                String entryFolderPath = CmsStringUtil.joinPaths(cms.getSitePath(parentFolder), change.getName() + "/");
                boolean idWasNull = change.getEntryId() == null;
                // we don't really need to create a folder object here anymore.
                if (idWasNull) {
                    // need this for calculateNavPosition, even though the id will get overwritten
                    change.setEntryId(new CmsUUID());
                }

                boolean isFunctionDetail = false;
                boolean isNavigationLevel = false;
                if (change.getCreateParameter() != null) {
                    if (CmsUUID.isValidUUID(change.getCreateParameter())) {
                        isFunctionDetail = true;
                    } else if (CmsJspNavBuilder.NAVIGATION_LEVEL_FOLDER.equals(change.getCreateParameter())) {
                        isNavigationLevel = true;
                    }
                }
                String folderType = CmsResourceTypeFolder.getStaticTypeName();
                String createSitemapFolderType = change.getCreateSitemapFolderType();
                if (createSitemapFolderType != null) {
                    folderType = createSitemapFolderType;
                }

                int folderTypeId = OpenCms.getResourceManager().getResourceType(folderType).getTypeId();
                entryFolder = new CmsResource(
                    change.getEntryId(),
                    new CmsUUID(),
                    entryFolderPath,
                    folderTypeId,
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
                List<CmsProperty> folderProperties = generateInheritProperties(change, entryFolder);
                if (isNavigationLevel) {
                    folderProperties.add(
                        new CmsProperty(
                            CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                            CmsJspNavBuilder.NAVIGATION_LEVEL_FOLDER,
                            null));
                }
                entryFolder = cms.createResource(entryFolderPath, folderTypeId, null, folderProperties);
                if (createSitemapFolderType != null) {
                    createSitemapContentFolder(cms, entryFolder);
                }
                if (idWasNull) {
                    change.setEntryId(entryFolder.getStructureId());
                }
                applyNavigationChanges(change, entryFolder);
                entryPath = CmsStringUtil.joinPaths(entryFolderPath, "index.html");
                boolean isContainerPage = change.getNewResourceTypeId() == CmsResourceTypeXmlContainerPage.getContainerPageTypeIdSafely();
                if (isContainerPage && (copyPage != null)) {

                    // do *NOT* get this from the cache, because we perform some destructive operation on the XML content
                    CmsXmlContainerPage page = CmsXmlContainerPageFactory.unmarshal(
                        cms,
                        cms.readFile(copyPage),
                        true,
                        true);
                    CmsContainerPageWrapper wrapper = new CmsContainerPageWrapper(cms, page);
                    if (isFunctionDetail) {
                        String functionDetailContainer = getFunctionDetailContainerName(parentFolder);
                        if (functionDetailContainer != null) {
                            CmsUUID functionStructureId = new CmsUUID(change.getCreateParameter());
                            CmsResource functionRes = cms.readResource(
                                functionStructureId,
                                CmsResourceFilter.IGNORE_EXPIRATION);
                            CmsResource functionFormatter;
                            if (OpenCms.getResourceManager().matchResourceType(
                                CmsResourceTypeFunctionConfig.TYPE_NAME,
                                functionRes.getTypeId())) {
                                functionFormatter = cms.readResource(CmsResourceTypeFunctionConfig.FORMATTER_PATH);
                            } else {
                                functionFormatter = cms.readResource(
                                    CmsResourceTypeFunctionConfig.FORMATTER_PATH,
                                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                            }
                            if (!wrapper.addElementToContainer(
                                functionDetailContainer,
                                new CmsContainerElementBean(
                                    functionStructureId,
                                    functionFormatter.getStructureId(),
                                    new HashMap<>(),
                                    false))) {

                                throw new CmsException(
                                    Messages.get().container(
                                        Messages.ERR_NO_FUNCTION_DETAIL_CONTAINER_1,
                                        page.getFile().getRootPath()));
                            }
                        } else {
                            LOG.debug("function detail container is null for " + parentFolder.getRootPath());
                        }
                    }
                    createNewContainerElements(cms, wrapper.page(), entryPath);
                    content = wrapper.marshal();
                }
                newRes = cms.createResource(
                    entryPath,
                    copyPage != null ? copyPage.getTypeId() : change.getNewResourceTypeId(),
                    content,
                    properties);
                cms.writePropertyObjects(newRes, generateOwnProperties(change));
            }

            if (entryFolder != null) {
                tryUnlock(entryFolder);
                String sitePath = cms.getSitePath(entryFolder);
                newEntry = toClientEntry(
                    getNavBuilder().getNavigationForResource(sitePath, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED),
                    false);
                newEntry.setSubEntries(getChildren(sitePath, 1, null), null);
                newEntry.setChildrenLoadedInitially(true);
            }
            if (newRes != null) {
                tryUnlock(newRes);
            }
            if (newEntry == null) {
                newEntry = toClientEntry(
                    getNavBuilder().getNavigationForResource(
                        cms.getSitePath(newRes),
                        CmsResourceFilter.ONLY_VISIBLE_NO_DELETED),
                    false);
            }
            // mark position as not set
            newEntry.setPosition(-1);
            newEntry.setNew(true);
            change.getClipBoardData().getModifications().remove(null);
            change.getClipBoardData().getModifications().put(newEntry.getId(), newEntry);
        }
        return newEntry;
    }

    /**
     * Creates a new resource info to a given model page resource.<p>
     *
     * @param cms the current CMS context
     * @param modelResource the model page resource
     * @param locale the locale used for retrieving descriptions/titles
     *
     * @return the new resource info
     *
     * @throws CmsException if something goes wrong
     */
    private CmsNewResourceInfo createNewResourceInfo(CmsObject cms, CmsResource modelResource, Locale locale)
    throws CmsException {

        // if model page got overwritten by another resource, reread from site path
        if (!cms.existsResource(modelResource.getStructureId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
            modelResource = cms.readResource(cms.getSitePath(modelResource), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        }
        int typeId = modelResource.getTypeId();
        String name = OpenCms.getResourceManager().getResourceType(typeId).getTypeName();
        String title = cms.readPropertyObject(
            modelResource,
            CmsPropertyDefinition.PROPERTY_TITLE,
            false,
            locale).getValue();
        String description = cms.readPropertyObject(
            modelResource,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false,
            locale).getValue();

        boolean editable = false;
        try {
            CmsResource freshModelResource = cms.readResource(
                modelResource.getStructureId(),
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            editable = cms.hasPermissions(
                freshModelResource,
                CmsPermissionSet.ACCESS_WRITE,
                false,
                CmsResourceFilter.DEFAULT);
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        CmsNewResourceInfo info = new CmsNewResourceInfo(
            typeId,
            name,
            title,
            description,
            modelResource.getStructureId(),
            editable,
            description);
        info.setBigIconClasses(CmsIconUtil.getIconClasses(name, null, false));
        Float navpos = null;
        try {
            CmsProperty navposProp = cms.readPropertyObject(modelResource, CmsPropertyDefinition.PROPERTY_NAVPOS, true);
            String navposStr = navposProp.getValue();
            if (navposStr != null) {
                try {
                    navpos = Float.valueOf(navposStr);
                } catch (NumberFormatException e) {
                    // noop
                }
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }

        info.setNavPos(navpos);
        info.setDate(
            CmsDateUtil.getDate(new Date(modelResource.getDateLastModified()), DateFormat.LONG, getWorkplaceLocale()));
        info.setVfsPath(modelResource.getRootPath());
        return info;
    }

    /**
     * Creates a resource type info bean for a given resource type.<p>
     *
     * @param resType the resource type
     * @param copyResource the structure id of the copy resource
     *
     * @return the resource type info bean
     */
    @SuppressWarnings("deprecation")
    private CmsNewResourceInfo createResourceTypeInfo(I_CmsResourceType resType, CmsResource copyResource) {

        String name = resType.getTypeName();
        Locale locale = getWorkplaceLocale();
        String subtitle = CmsWorkplaceMessages.getResourceTypeDescription(locale, name);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(subtitle)) {
            subtitle = CmsWorkplaceMessages.getResourceTypeName(locale, name);
        }
        if (copyResource != null) {
            CmsNewResourceInfo info = new CmsNewResourceInfo(
                copyResource.getTypeId(),
                name,
                CmsWorkplaceMessages.getResourceTypeName(locale, name),
                CmsWorkplaceMessages.getResourceTypeDescription(locale, name),
                copyResource.getStructureId(),
                false,
                subtitle);
            info.setBigIconClasses(CmsIconUtil.getIconClasses(name, null, false));
            return info;
        } else {
            CmsNewResourceInfo info = new CmsNewResourceInfo(
                resType.getTypeId(),
                name,
                CmsWorkplaceMessages.getResourceTypeName(locale, name),
                CmsWorkplaceMessages.getResourceTypeDescription(locale, name),
                null,
                false,
                subtitle);
            info.setBigIconClasses(CmsIconUtil.getIconClasses(name, null, false));
            return info;
        }
    }

    /**
     * Helper method for creating the .content folder of a sub-sitemap.<p>
     *
     * @param cms the current CMS context
     * @param subSitemapFolder the sub-sitemap folder in which the .content folder should be created
     *
     * @throws CmsException if something goes wrong
     */
    @SuppressWarnings("deprecation")
    private void createSitemapContentFolder(CmsObject cms, CmsResource subSitemapFolder) throws CmsException {

        String sitePath = cms.getSitePath(subSitemapFolder);
        String folderName = CmsStringUtil.joinPaths(sitePath, CmsADEManager.CONTENT_FOLDER_NAME + "/");
        String sitemapConfigName = CmsStringUtil.joinPaths(folderName, CmsADEManager.CONFIG_FILE_NAME);
        if (!cms.existsResource(folderName)) {
            cms.createResource(
                folderName,
                OpenCms.getResourceManager().getResourceType(CmsADEManager.CONFIG_FOLDER_TYPE));
        }
        I_CmsResourceType configType = OpenCms.getResourceManager().getResourceType(CmsADEManager.CONFIG_TYPE);
        if (cms.existsResource(sitemapConfigName)) {
            CmsResource configFile = cms.readResource(sitemapConfigName);
            if (configFile.getTypeId() != configType.getTypeId()) {
                throw new CmsException(
                    Messages.get().container(
                        Messages.ERR_CREATING_SUB_SITEMAP_WRONG_CONFIG_FILE_TYPE_2,
                        sitemapConfigName,
                        CmsADEManager.CONFIG_TYPE));
            }
        } else {
            cms.createResource(
                sitemapConfigName,
                OpenCms.getResourceManager().getResourceType(CmsADEManager.CONFIG_TYPE));
        }

    }

    /**
     * Deletes a resource according to the change data.<p>
     *
     * @param change the change data
     *
     * @return CmsClientSitemapEntry always null
     *
     *
     * @throws CmsException if something goes wrong
     */
    private CmsClientSitemapEntry delete(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource(change.getEntryId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        ensureLock(resource);
        cms.deleteResource(cms.getSitePath(resource), CmsResource.DELETE_PRESERVE_SIBLINGS);
        tryUnlock(resource);
        return null;
    }

    /**
     * Generates a client side lock info object representing the current lock state of the given resource.<p>
     *
     * @param resource the resource
     *
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
        boolean hasTitle = false;
        if (clientProps != null) {
            for (CmsClientProperty clientProp : clientProps.values()) {
                if (CmsPropertyDefinition.PROPERTY_TITLE.equals(clientProp.getName())) {
                    hasTitle = true;
                }
                CmsProperty prop = new CmsProperty(
                    clientProp.getName(),
                    clientProp.getStructureValue(),
                    clientProp.getResourceValue());
                result.add(prop);
            }
        }
        if (!hasTitle) {
            result.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, change.getName(), null));
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
        boolean hasTitle = false;
        if (clientProps != null) {
            for (CmsClientProperty clientProp : clientProps.values()) {
                if (CmsPropertyDefinition.PROPERTY_TITLE.equals(clientProp.getName())) {
                    hasTitle = true;
                }
                CmsProperty prop = new CmsProperty(
                    clientProp.getName(),
                    clientProp.getStructureValue(),
                    clientProp.getResourceValue());
                result.add(prop);
            }
        }
        if (!hasTitle) {
            result.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, change.getName(), null));
        }
        return result;
    }

    /**
     * Generates a list of property values inherited to the site-map root entry.<p>
     *
     * @param rootPath the root entry name
     *
     * @return the list of property values inherited to the site-map root entry
     *
     * @throws CmsException if something goes wrong reading the properties
     */
    private Map<String, CmsClientProperty> generateParentProperties(String rootPath) throws CmsException {

        CmsObject cms = getCmsObject();
        if (rootPath == null) {
            rootPath = cms.getRequestContext().addSiteRoot("/");
        }
        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("");
        String parentRootPath = CmsResource.getParentFolder(rootPath);

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        if (parentRootPath != null) {
            List<CmsProperty> props = rootCms.readPropertyObjects(parentRootPath, true);
            for (CmsProperty prop : props) {
                CmsClientProperty clientProp = createClientProperty(prop, true);
                result.put(clientProp.getName(), clientProp);
            }
        }
        return result;
    }

    /**
     * Returns the sitemap children for the given path with all descendants up to the given level or to the given target path, ie.
     * <dl><dt>levels=1 <dd>only children<dt>levels=2<dd>children and great children</dl>
     * and so on.<p>
     *
     * @param root the site relative root
     * @param levels the levels to recurse
     * @param targetPath the target path
     *
     * @return the sitemap children
     */
    private List<CmsClientSitemapEntry> getChildren(String root, int levels, String targetPath) {

        List<CmsClientSitemapEntry> children = new ArrayList<CmsClientSitemapEntry>();
        int i = 0;
        for (CmsJspNavElement navElement : getNavBuilder().getNavigationForFolder(
            root,
            Visibility.all,
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
            try {
                CmsClientSitemapEntry child = toClientEntry(navElement, false);
                if (child != null) {
                    child.setPosition(i);
                    children.add(child);
                    int nextLevels = levels;
                    if ((nextLevels == 2) && (targetPath != null) && targetPath.startsWith(child.getSitePath())) {
                        nextLevels = 3;
                    }
                    if (child.isFolderType() && ((nextLevels > 1) || (nextLevels == -1)) && !isSubSitemap(navElement)) {

                        child.setSubEntries(getChildren(child.getSitePath(), nextLevels - 1, targetPath), null);
                        child.setChildrenLoadedInitially(true);
                    }
                    i++;
                }
            } catch (CmsException e) {
                LOG.error("Could not read sitemap entry.", e);
            }
        }
        return children;
    }

    /**
     * Returns the clipboard data from the current user.<p>
     *
     * @return the clipboard data
     */
    private CmsSitemapClipboardData getClipboardData() {

        CmsSitemapClipboardData result = new CmsSitemapClipboardData();
        result.setModifications(getModifiedList());
        result.setDeletions(getDeletedList());
        return result;
    }

    /**
     * Returns the deleted list from the current user.<p>
     *
     * @return the deleted list
     */
    private LinkedHashMap<CmsUUID, CmsClientSitemapEntry> getDeletedList() {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_DELETED_LIST);
        LinkedHashMap<CmsUUID, CmsClientSitemapEntry> result = new LinkedHashMap<CmsUUID, CmsClientSitemapEntry>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        CmsUUID delId = new CmsUUID(array.getString(i));
                        CmsResource res = cms.readResource(delId, CmsResourceFilter.ALL);
                        if (res.getState().isDeleted()) {
                            // make sure resource is still deleted
                            CmsClientSitemapEntry delEntry = new CmsClientSitemapEntry();
                            delEntry.setSitePath(cms.getSitePath(res));
                            delEntry.setOwnProperties(getClientProperties(cms, res, false));
                            delEntry.setName(res.getName());
                            delEntry.setVfsPath(cms.getSitePath(res));
                            delEntry.setResourceTypeName(
                                OpenCms.getResourceManager().getResourceType(res).getTypeName());
                            delEntry.setEntryType(
                                res.isFolder()
                                ? EntryType.folder
                                : isRedirectType(res.getTypeId()) ? EntryType.redirect : EntryType.leaf);
                            delEntry.setNavModeIcon(
                                CmsIconUtil.getIconClasses(
                                    delEntry.getResourceTypeName(),
                                    delEntry.getVfsPath(),
                                    false));
                            delEntry.setId(delId);
                            result.put(delId, delEntry);
                        }
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        }
        return result;
    }

    /**
     * Gets the container name for function detail elements depending on the parent folder.<p>
     *
     * @param parent the parent folder
     * @return the name of the function detail container
     */
    private String getFunctionDetailContainerName(CmsResource parent) {

        CmsObject cms = getCmsObject();
        String notFound = null;
        String containerInfo = OpenCms.getTemplateContextManager().readPropertyFromTemplate(
            cms,
            parent,
            CmsPropertyDefinition.PROPERTY_CONTAINER_INFO,
            notFound);
        if (containerInfo == notFound) {
            return null;
        }
        Map<String, String> attrs = CmsStringUtil.splitAsMap(containerInfo, "|", "="); //$NON-NLS-2$
        String functionDetailContainerName = attrs.get(KEY_FUNCTION_DETAIL);
        return functionDetailContainerName;
    }

    /**
     * Returns the galleries of the given sub site for the requested gallery type.<p>
     *
     * @param entryPointUri the sub site entry point
     * @param galleryType the gallery type
     * @param subSitePaths the sub site paths
     *
     * @return the gallery folder entries
     *
     * @throws CmsException if reading the resources fails
     */
    private List<CmsGalleryFolderEntry> getGalleriesForType(
        String entryPointUri,
        CmsGalleryType galleryType,
        List<String> subSitePaths)
    throws CmsException {

        List<CmsGalleryFolderEntry> galleries = new ArrayList<CmsGalleryFolderEntry>();
        @SuppressWarnings("deprecation")
        List<CmsResource> galleryFolders = getCmsObject().readResources(
            entryPointUri,
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryType.getTypeId()));
        for (CmsResource folder : galleryFolders) {
            try {
                if (!isInSubsite(subSitePaths, folder.getRootPath())) {
                    galleries.add(readGalleryFolderEntry(folder, galleryType.getResourceType()));
                }
            } catch (CmsException ex) {
                log(ex.getLocalizedMessage(), ex);
            }
        }
        // create a tree structure
        Collections.sort(galleries, new Comparator<CmsGalleryFolderEntry>() {

            public int compare(CmsGalleryFolderEntry o1, CmsGalleryFolderEntry o2) {

                return o1.getSitePath().compareTo(o2.getSitePath());
            }
        });
        List<CmsGalleryFolderEntry> galleryTree = new ArrayList<CmsGalleryFolderEntry>();
        for (int i = 0; i < galleries.size(); i++) {
            boolean isSubGallery = false;
            if (i > 0) {
                for (int j = i - 1; j >= 0; j--) {
                    if (galleries.get(i).getSitePath().startsWith(galleries.get(j).getSitePath())) {
                        galleries.get(j).addSubGallery(galleries.get(i));
                        isSubGallery = true;
                        break;
                    }
                }
            }
            if (!isSubGallery) {
                galleryTree.add(galleries.get(i));
            }
        }
        return galleryTree;
    }

    /**
     * Returns the modified list from the current user.<p>
     *
     * @return the modified list
     */
    private LinkedHashMap<CmsUUID, CmsClientSitemapEntry> getModifiedList() {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_MODIFIED_LIST);
        LinkedHashMap<CmsUUID, CmsClientSitemapEntry> result = new LinkedHashMap<CmsUUID, CmsClientSitemapEntry>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        CmsUUID modId = new CmsUUID(array.getString(i));
                        CmsResource res = cms.readResource(modId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                        String sitePath = cms.getSitePath(res);
                        CmsJspNavElement navEntry = getNavBuilder().getNavigationForResource(
                            sitePath,
                            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                        if (navEntry.isInNavigation()) {
                            CmsClientSitemapEntry modEntry = toClientEntry(navEntry, false);
                            result.put(modId, modEntry);
                        }
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        }
        return result;
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
     * @param configData the configuration data from which the new resource infos should be read
     * @param locale locale used for retrieving descriptions/titles
     *
     * @return the new resource infos
     */
    private List<CmsNewResourceInfo> getNewResourceInfos(CmsObject cms, CmsADEConfigData configData, Locale locale) {

        List<CmsNewResourceInfo> result = new ArrayList<CmsNewResourceInfo>();
        for (CmsModelPageConfig modelConfig : configData.getModelPages()) {
            try {
                CmsNewResourceInfo info = createNewResourceInfo(cms, modelConfig.getResource(), locale);
                info.setDefault(modelConfig.isDefault());
                result.add(info);
            } catch (CmsException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        Collections.sort(result, new Comparator<CmsNewResourceInfo>() {

            public int compare(CmsNewResourceInfo a, CmsNewResourceInfo b) {

                return ComparisonChain.start().compareTrueFirst(a.isDefault(), b.isDefault()).compare(
                    a.getNavPos(),
                    b.getNavPos(),
                    Ordering.natural().nullsLast()).result();
            }
        });
        return result;
    }

    /**
     * Gets the names of all available properties.<p>
     *
     * @param cms the CMS context
     *
     * @return the list of all property names
     *
     * @throws CmsException if something goes wrong
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
     * @param resourceTypeConfigs the resource type configurations
     * @param functionReferences the function references
     * @param dynamicFunctionRestriction
     * @param modelResource the model resource
     * @param locale the locale used for retrieving descriptions/titles
     *
     * @return the resource type info beans for types for which new detail pages can be created
     */
    private List<CmsNewResourceInfo> getResourceTypeInfos(
        CmsObject cms,
        List<CmsResourceTypeConfig> resourceTypeConfigs,
        List<CmsFunctionReference> functionReferences,
        CmsFunctionAvailability dynamicFunctionAvailability,
        CmsResource modelResource,
        Locale locale) {

        List<CmsNewResourceInfo> result = new ArrayList<CmsNewResourceInfo>();
        CmsNewResourceInfo defaultPageInfo;
        if (modelResource != null) {
            defaultPageInfo = new CmsNewResourceInfo(
                modelResource.getTypeId(),
                CmsADEManager.DEFAULT_DETAILPAGE_TYPE,
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_DEFAULT_DETAIL_PAGE_TITLE_0),
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_DEFAULT_DETAIL_PAGE_DESCRIPTION_0),
                modelResource.getStructureId(),
                false,
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_DEFAULT_DETAIL_PAGE_DESCRIPTION_0));

        } else {
            defaultPageInfo = new CmsNewResourceInfo(
                CmsResourceTypeXmlContainerPage.getContainerPageTypeIdSafely(),
                CmsADEManager.DEFAULT_DETAILPAGE_TYPE,
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_DEFAULT_DETAIL_PAGE_TITLE_0),
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_DEFAULT_DETAIL_PAGE_DESCRIPTION_0),
                null,
                false,
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_DEFAULT_DETAIL_PAGE_DESCRIPTION_0));
        }

        defaultPageInfo.setBigIconClasses(
            CmsIconUtil.getIconClasses(CmsResourceTypeXmlContainerPage.getStaticTypeName(), null, false));
        result.add(defaultPageInfo);
        for (CmsResourceTypeConfig typeConfig : resourceTypeConfigs) {
            if (typeConfig.isDetailPagesDisabled()) {
                continue;
            }
            String typeName = typeConfig.getTypeName();
            try {
                I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(typeName);
                result.add(createResourceTypeInfo(resourceType, modelResource));
            } catch (CmsLoaderException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        for (CmsFunctionReference functionRef : functionReferences) {
            if (dynamicFunctionAvailability.isDefined()) {
                if (!dynamicFunctionAvailability.checkAvailable(functionRef.getStructureId())) {
                    continue;
                }
            }
            try {
                CmsResource functionRes = cms.readResource(functionRef.getStructureId());
                String description = cms.readPropertyObject(
                    functionRes,
                    CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                    false).getValue();
                String subtitle = description;
                try {
                    CmsGallerySearchResult searchResult = CmsGallerySearch.searchById(
                        cms,
                        functionRef.getStructureId(),
                        getWorkplaceLocale());
                    subtitle = searchResult.getDescription();
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }

                CmsNewResourceInfo info = new CmsNewResourceInfo(
                    modelResource.getTypeId(),
                    CmsDetailPageInfo.FUNCTION_PREFIX + functionRef.getName(),
                    functionRef.getName(),
                    description,
                    modelResource.getStructureId(),
                    false,
                    subtitle);
                info.setCreateParameter(functionRef.getStructureId().toString());
                info.setIsFunction(true);
                info.setBigIconClasses(
                    CmsIconUtil.getIconClasses(CmsXmlDynamicFunctionHandler.TYPE_FUNCTION, null, false));
                result.add(info);
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Reeds the site root entry.<p>
     *
     * @param rootPath the root path of the sitemap root
     * @param targetPath the target path to open
     *
     * @return the site root entry
     *
     * @throws CmsSecurityException in case of insufficient permissions
     * @throws CmsException if something goes wrong
     */
    private CmsClientSitemapEntry getRootEntry(String rootPath, String targetPath)
    throws CmsSecurityException, CmsException {

        String sitePath = "/";
        if ((rootPath != null)) {
            sitePath = getCmsObject().getRequestContext().removeSiteRoot(rootPath);
        }
        CmsJspNavElement navElement = getNavBuilder().getNavigationForResource(
            sitePath,
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsClientSitemapEntry result = toClientEntry(navElement, true);
        if (result != null) {
            result.setPosition(0);
            result.setChildrenLoadedInitially(true);
            result.setSubEntries(getChildren(sitePath, 2, targetPath), null);
        }
        return result;
    }

    /**
     * Returns the sitemap info for the given base path.<p>
     *
     * @param basePath the base path
     *
     * @return the sitemap info
     *
     * @throws CmsException if something goes wrong reading the resources
     */
    private CmsSitemapInfo getSitemapInfo(String basePath) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource baseFolder = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(basePath)) {
            baseFolder = cms.readResource(
                cms.getRequestContext().removeSiteRoot(basePath),
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        } else {
            // in case of an empty base path, use base folder of the current site
            basePath = "/";
            baseFolder = cms.readResource("/");
        }
        CmsResource defaultFile = cms.readDefaultFile(baseFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        String title = cms.readPropertyObject(baseFolder, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(title) && (defaultFile != null)) {
            title = cms.readPropertyObject(defaultFile, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        }
        String description = cms.readPropertyObject(
            baseFolder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(description) && (defaultFile != null)) {
            description = cms.readPropertyObject(
                defaultFile,
                CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                false).getValue();
        }
        return new CmsSitemapInfo(
            cms.getRequestContext().getCurrentProject().getName(),
            description,
            OpenCms.getLocaleManager().getDefaultLocale(cms, baseFolder).toString(),
            OpenCms.getSiteManager().getCurrentSite(cms).getUrl()
                + OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, basePath),
            title);
    }

    /**
     * Gets the default type id for subsitemap folders.<p>
     *
     * @return the default type id for subsitemap folders
     *
     * @throws CmsRpcException in case of an error
     */
    @SuppressWarnings("deprecation")
    private int getSubsitemapType() throws CmsRpcException {

        try {
            return OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeFolderSubSitemap.TYPE_SUBSITEMAP).getTypeId();
        } catch (CmsLoaderException e) {
            error(e);
        }
        return CmsResourceTypeUnknownFolder.getStaticTypeId();
    }

    /**
     * Returns the workplace locale for the current user.<p>
     *
     * @return the workplace locale
     */
    private Locale getWorkplaceLocale() {

        Locale result = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        if (result == null) {
            result = CmsLocaleManager.getDefaultLocale();
        }
        if (result == null) {
            result = Locale.getDefault();
        }
        return result;
    }

    /**
     * Checks whether the sitemap change has default file changes.<p>
     *
     * @param change a sitemap change
     * @return true if the change would change the default file
     */
    private boolean hasDefaultFileChanges(CmsSitemapChange change) {

        return (change.getDefaultFileId() != null) && !change.isNew();
    }

    /**
     * Checks whether the sitemap change has changes for the sitemap entry resource.<p>
     *
     * @param change the sitemap change
     * @return true if the change would change the original sitemap entry resource
     */
    private boolean hasOwnChanges(CmsSitemapChange change) {

        return !change.isNew();
    }

    /**
     * Checks whether a resource is a default file of a folder.<p>
     *
     * @param resource the resource to check
     *
     * @return true if the resource is the default file of a folder
     *
     * @throws CmsException if something goes wrong
     */
    private boolean isDefaultFile(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        if (resource.isFolder()) {
            return false;
        }

        CmsResource parent = cms.readResource(
            CmsResource.getParentFolder(cms.getSitePath(resource)),
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsResource defaultFile = cms.readDefaultFile(parent, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        return resource.equals(defaultFile);
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
     * Returns if the given path is located below one of the given sub site paths.<p>
     *
     * @param subSitePaths the sub site root paths
     * @param path the root path to check
     *
     * @return <code>true</code> if the given path is located below one of the given sub site paths
     */
    private boolean isInSubsite(List<String> subSitePaths, String path) {

        boolean result = false;
        for (String subSite : subSitePaths) {
            if (path.startsWith(subSite)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Returns if the given type id matches the xml-redirect resource type.<p>
     *
     * @param typeId the resource type id
     *
     * @return <code>true</code> if the given type id matches the xml-redirect resource type
     */
    @SuppressWarnings("deprecation")
    private boolean isRedirectType(int typeId) {

        try {
            return typeId == OpenCms.getResourceManager().getResourceType(RECOURCE_TYPE_NAME_REDIRECT).getTypeId();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns if the given nav-element resembles a sub-sitemap entry-point.<p>
     *
     * @param navElement the nav-element
     *
     * @return <code>true</code> if the given nav-element resembles a sub-sitemap entry-point.<p>
     */
    private boolean isSubSitemap(CmsJspNavElement navElement) {

        return CmsResourceTypeFolderSubSitemap.isSubSitemap(navElement.getResource());
    }

    /**
     * Checks if the given open path is valid.<p>
     *
     * @param cms the cms context
     * @param openPath the open path
     *
     * @return <code>true</code> if the given open path is valid
     */
    private boolean isValidOpenPath(CmsObject cms, String openPath) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(openPath)) {
            return false;
        }
        if (!cms.existsResource(openPath)) {
            // in case of a detail-page check the parent folder
            String parent = CmsResource.getParentFolder(openPath);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(parent) || !cms.existsResource(parent)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns if the given return code is valid.<p>
     *
     * @param returnCode the return code to check
     *
     * @return <code>true</code> if the return code is valid
     */
    private boolean isValidReturnCode(String returnCode) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(returnCode)) {
            return false;
        }
        int pos = returnCode.indexOf(":");
        if (pos > 0) {
            return CmsUUID.isValidUUID(returnCode.substring(0, pos))
                && CmsUUID.isValidUUID(returnCode.substring(pos + 1));
        } else {
            return CmsUUID.isValidUUID(returnCode);
        }
    }

    /**
     * Applys the given changes to the entry.<p>
     *
     * @param change the change to apply
     *
     * @throws CmsException if something goes wrong
     */
    private void modifyEntry(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource entryPage = null;
        CmsResource entryFolder = null;

        CmsResource ownRes = null;
        CmsResource defaultFileRes = null;
        try {
            // lock all resources necessary first to avoid doing changes only half way through

            if (hasOwnChanges(change)) {
                ownRes = cms.readResource(change.getEntryId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                boolean shallow = !change.hasChangedName() && !change.hasChangedPosition() && !change.hasNewParent();

                ensureLock(ownRes, shallow);
            }

            if (hasDefaultFileChanges(change)) {
                defaultFileRes = cms.readResource(change.getDefaultFileId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                ensureLock(defaultFileRes, false);
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

            String moveSrc = null;
            String moveDest = null;

            if (entryFolder != null) {
                if (change.hasNewParent() || change.hasChangedName()) {
                    String destinationPath;
                    if (change.hasNewParent()) {
                        CmsResource futureParent = cms.readResource(
                            change.getParentId(),
                            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
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
                    if (!CmsFileUtil.removeTrailingSeparator(cms.getSitePath(entryFolder)).equals(
                        CmsFileUtil.removeTrailingSeparator(destinationPath))) {
                        moveSrc = cms.getSitePath(entryFolder);
                        moveDest = CmsFileUtil.removeTrailingSeparator(destinationPath);
                    }
                }
            }
            if ((moveDest != null) && cms.existsResource(moveDest, CmsResourceFilter.IGNORE_EXPIRATION)) {
                throw new CmsVfsResourceAlreadyExistsException(
                    org.opencms.db.generic.Messages.get().container(
                        org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                        moveDest));
            }

            updateProperties(cms, ownRes, defaultFileRes, change.getPropertyChanges());
            if (change.hasChangedPosition()) {
                updateNavPos(ownRes, change);
            }

            if (moveDest != null) {
                cms.moveResource(moveSrc, moveDest);
            }
            entryFolder = cms.readResource(entryFolder.getStructureId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);

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
     * Reads the gallery folder properties.<p>
     *
     * @param folder the folder resource
     * @param typeName the  resource type name
     *
     * @return the folder entry data
     *
     * @throws CmsException if the folder properties can not be read
     */
    private CmsGalleryFolderEntry readGalleryFolderEntry(CmsResource folder, String typeName) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsGalleryFolderEntry folderEntry = new CmsGalleryFolderEntry();
        folderEntry.setResourceType(typeName);
        folderEntry.setSitePath(cms.getSitePath(folder));
        folderEntry.setStructureId(folder.getStructureId());
        folderEntry.setOwnProperties(getClientProperties(cms, folder, false));
        folderEntry.setIconClasses(CmsIconUtil.getIconClasses(typeName, null, false));
        return folderEntry;
    }

    /**
     * Helper method for removing all locales except one from a container page.<p>
     *
     * @param page the container page to proces
     * @param localeToKeep the locale which should be kept
     *
     * @throws CmsXmlException if something goes wrong
     */
    private void removeAllLocalesExcept(CmsXmlContainerPage page, Locale localeToKeep) throws CmsXmlException {

        List<Locale> locales = page.getLocales();
        for (Locale locale : locales) {
            if (!locale.equals(localeToKeep)) {
                page.removeLocale(locale);
            }
        }
    }

    /**
     * Applys the given remove change.<p>
     *
     * @param change the change to apply
     *
     * @return the changed client sitemap entry or <code>null</code>
     *
     * @throws CmsException if something goes wrong
     */
    private CmsSitemapChange removeEntryFromNavigation(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource entryFolder = cms.readResource(change.getEntryId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        ensureLock(entryFolder);
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(
            new CmsProperty(
                CmsPropertyDefinition.PROPERTY_NAVTEXT,
                CmsProperty.DELETE_VALUE,
                CmsProperty.DELETE_VALUE));
        properties.add(
            new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVPOS, CmsProperty.DELETE_VALUE, CmsProperty.DELETE_VALUE));
        cms.writePropertyObjects(cms.getSitePath(entryFolder), properties);
        tryUnlock(entryFolder);
        return change;
    }

    /**
     * Saves the detail page information of a sitemap to the sitemap's configuration file.<p>
     *
     * @param detailPages saves the detailpage configuration
     * @param resource the configuration file resource
     * @param newId the structure id to use for new detail page entries
     * @param updateEntry the new detail page entry
     *
     * @throws CmsException if something goes wrong
     */
    private void saveDetailPages(
        List<CmsDetailPageInfo> detailPages,
        CmsResource resource,
        CmsUUID newId,
        CmsClientSitemapEntry updateEntry)
    throws CmsException {

        CmsObject cms = getCmsObject();
        if (updateEntry != null) {
            for (CmsDetailPageInfo info : detailPages) {
                if (info.getId() == null) {
                    updateEntry.setDetailpageTypeName(info.getType());
                    break;
                }
            }
        }
        CmsDetailPageConfigurationWriter writer = new CmsDetailPageConfigurationWriter(cms, resource);
        writer.updateAndSave(detailPages, newId);
    }

    /**
     * Saves the given clipboard data to the session.<p>
     *
     * @param clipboardData the clipboard data to save
     *
     * @throws CmsException if something goes wrong writing the user
     */
    private void setClipboardData(CmsSitemapClipboardData clipboardData) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        if (clipboardData != null) {
            JSONArray modified = new JSONArray();
            if (clipboardData.getModifications() != null) {
                for (CmsUUID id : clipboardData.getModifications().keySet()) {
                    if (id != null) {
                        modified.put(id.toString());
                    }
                }
            }
            user.setAdditionalInfo(ADDINFO_ADE_MODIFIED_LIST, modified.toString());
            JSONArray deleted = new JSONArray();
            if (clipboardData.getDeletions() != null) {
                for (CmsUUID id : clipboardData.getDeletions().keySet()) {
                    if (id != null) {
                        deleted.put(id.toString());
                    }
                }
            }
            user.setAdditionalInfo(ADDINFO_ADE_DELETED_LIST, deleted.toString());
            cms.writeUser(user);
        }
    }

    /**
     * Determines if the title property of the default file should be changed.<p>
     *
     * @param properties the current default file properties
     * @param folderNavtext the 'NavText' property of the folder
     *
     * @return <code>true</code> if the title property should be changed
     */
    private boolean shouldChangeDefaultFileTitle(Map<String, CmsProperty> properties, CmsProperty folderNavtext) {

        return (properties == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE) == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue() == null)
            || ((folderNavtext != null)
                && properties.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue().equals(folderNavtext.getValue()));
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
            || ((properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT) != null)
                && properties.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue().equals(
                    properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT).getValue()));
    }

    /**
     * Converts a jsp navigation element into a client sitemap entry.<p>
     *
     * @param navElement the jsp navigation element
     * @param isRoot true if the entry is a root entry
     *
     * @return the client sitemap entry
     *
     * @throws CmsException if something goes wrong
     */
    private CmsClientSitemapEntry toClientEntry(CmsJspNavElement navElement, boolean isRoot) throws CmsException {

        CmsResource entryPage = null;
        CmsObject cms = getCmsObject();
        CmsClientSitemapEntry clientEntry = new CmsClientSitemapEntry();
        CmsResource entryFolder = null;

        CmsResource ownResource = navElement.getResource();
        clientEntry.setResourceState(ownResource.getState());
        CmsResource defaultFileResource = null;
        if (ownResource.isFolder() && !navElement.isNavigationLevel()) {
            defaultFileResource = cms.readDefaultFile(ownResource, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        }

        Map<String, CmsClientProperty> ownProps = getClientProperties(cms, ownResource, false);

        Map<String, CmsClientProperty> defaultFileProps = null;
        if (defaultFileResource != null) {
            defaultFileProps = getClientProperties(cms, defaultFileResource, false);
            clientEntry.setDefaultFileId(defaultFileResource.getStructureId());
            clientEntry.setDefaultFileType(
                OpenCms.getResourceManager().getResourceType(defaultFileResource.getTypeId()).getTypeName());
            clientEntry.setDefaultFileReleased(defaultFileResource.isReleasedAndNotExpired(System.currentTimeMillis()));
        } else {
            defaultFileProps = new HashMap<String, CmsClientProperty>();
        }
        boolean isDefault = isDefaultFile(ownResource);
        clientEntry.setId(ownResource.getStructureId());
        clientEntry.setResourceTypeId(ownResource.getTypeId());
        clientEntry.setFolderDefaultPage(isDefault);
        if (navElement.getResource().isFolder()) {
            entryFolder = navElement.getResource();
            entryPage = defaultFileResource;
            clientEntry.setName(entryFolder.getName());
            if (entryPage == null) {
                entryPage = entryFolder;
            }
            if (!isRoot && isSubSitemap(navElement)) {
                clientEntry.setEntryType(EntryType.subSitemap);
                clientEntry.setDefaultFileType(null);
            } else if (navElement.isNavigationLevel()) {
                clientEntry.setEntryType(EntryType.navigationLevel);
            }
            CmsLock folderLock = cms.getLock(entryFolder);
            clientEntry.setHasForeignFolderLock(
                !folderLock.isUnlocked() && !folderLock.isOwnedBy(cms.getRequestContext().getCurrentUser()));
            if (!cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                List<CmsResource> blockingChildren = cms.getBlockingLockedResources(entryFolder);
                clientEntry.setBlockingLockedChildren((blockingChildren != null) && !blockingChildren.isEmpty());
            }
        } else {
            entryPage = navElement.getResource();
            clientEntry.setName(entryPage.getName());
            if (isRedirectType(entryPage.getTypeId())) {
                clientEntry.setEntryType(EntryType.redirect);
                CmsFile file = getCmsObject().readFile(entryPage);
                I_CmsXmlDocument content = CmsXmlContentFactory.unmarshal(getCmsObject(), file);
                I_CmsXmlContentValue linkValue = content.getValue(
                    REDIRECT_LINK_TARGET_XPATH,
                    getCmsObject().getRequestContext().getLocale());
                String link = linkValue != null
                ? linkValue.getStringValue(getCmsObject())
                : Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_REDIRECT_SUB_LEVEL_0);
                clientEntry.setRedirectTarget(link);
            } else {
                clientEntry.setEntryType(EntryType.leaf);
            }
        }
        if (entryPage.isFile()) {
            List<CmsAlias> aliases = OpenCms.getAliasManager().getAliasesForStructureId(
                getCmsObject(),
                entryPage.getStructureId());
            if (!aliases.isEmpty()) {
                List<String> aliasList = new ArrayList<String>();
                for (CmsAlias alias : aliases) {
                    String aliasPath = alias.getAliasPath();
                    aliasList.add(aliasPath);
                }
                clientEntry.setAliases(aliasList);
            }
        }
        long dateExpired = navElement.getResource().getDateExpired();
        if (dateExpired != CmsResource.DATE_EXPIRED_DEFAULT) {
            clientEntry.setDateExpired(
                CmsDateUtil.getDate(new Date(dateExpired), DateFormat.SHORT, getWorkplaceLocale()));
        }
        long dateReleased = navElement.getResource().getDateReleased();
        if (dateReleased != CmsResource.DATE_RELEASED_DEFAULT) {
            clientEntry.setDateReleased(
                CmsDateUtil.getDate(new Date(dateReleased), DateFormat.SHORT, getWorkplaceLocale()));
        }
        clientEntry.setResleasedAndNotExpired(
            navElement.getResource().isReleasedAndNotExpired(System.currentTimeMillis()));
        String path = cms.getSitePath(entryPage);
        clientEntry.setVfsPath(path);
        clientEntry.setOwnProperties(ownProps);
        clientEntry.setDefaultFileProperties(defaultFileProps);
        clientEntry.setSitePath(entryFolder != null ? cms.getSitePath(entryFolder) : path);
        clientEntry.setLock(generateClientLock(entryPage));
        clientEntry.setInNavigation(isRoot || navElement.isInNavigation());
        String type = OpenCms.getResourceManager().getResourceType(ownResource).getTypeName();
        clientEntry.setResourceTypeName(type);
        clientEntry.setVfsModeIcon(CmsIconUtil.getIconClasses(type, ownResource.getName(), false));

        if (!clientEntry.isSubSitemapType()) {
            if (clientEntry.isNavigationLevelType()) {
                clientEntry.setNavModeIcon(CmsIconUtil.ICON_NAV_LEVEL_BIG);
            } else if (defaultFileResource != null) {
                clientEntry.setNavModeIcon(
                    CmsIconUtil.getIconClasses(clientEntry.getDefaultFileType(), defaultFileResource.getName(), false));
            }
        }
        clientEntry.setPermissionInfo(OpenCms.getADEManager().getPermissionInfo(cms, ownResource, null));
        return clientEntry;
    }

    /**
     * Un-deletes a resource according to the change data.<p>
     *
     * @param change the change data
     *
     * @return the changed entry or <code>null</code>
     *
     * @throws CmsException if something goes wrong
     */
    private CmsSitemapChange undelete(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource deleted = cms.readResource(change.getEntryId(), CmsResourceFilter.ALL);
        if (deleted.getState().isDeleted()) {
            ensureLock(deleted);
            cms.undeleteResource(getCmsObject().getSitePath(deleted), true);
            tryUnlock(deleted);
        }
        String parentPath = CmsResource.getParentFolder(cms.getSitePath(deleted));
        CmsJspNavElement navElement = getNavBuilder().getNavigationForResource(
            parentPath,
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsClientSitemapEntry entry = toClientEntry(navElement, navElement.isInNavigation());
        entry.setSubEntries(getChildren(parentPath, 2, null), null);
        change.setUpdatedEntry(entry);
        change.setParentId(cms.readParentFolder(deleted.getStructureId()).getStructureId());
        return change;
    }

    /**
     * Updates the navigation position for a resource.<p>
     *
     * @param res the resource for which to update the navigation position
     * @param change the sitemap change
     *
     * @throws CmsException if something goes wrong
     */
    private void updateNavPos(CmsResource res, CmsSitemapChange change) throws CmsException {

        if (change.hasChangedPosition()) {
            applyNavigationChanges(change, res);
        }
    }

    /**
     * Updates properties for a resource and possibly its detail page.<p>
     *
     * @param cms the CMS context
     * @param ownRes the resource
     * @param defaultFileRes the default file resource (possibly null)
     * @param propertyModifications the property modifications
     *
     * @throws CmsException if something goes wrong
     */
    private void updateProperties(
        CmsObject cms,
        CmsResource ownRes,
        CmsResource defaultFileRes,
        List<CmsPropertyModification> propertyModifications)
    throws CmsException {

        Map<String, CmsProperty> ownProps = getPropertiesByName(cms.readPropertyObjects(ownRes, false));
        // determine if the title property should be changed in case of a 'NavText' change
        boolean changeOwnTitle = shouldChangeTitle(ownProps);

        boolean changeDefaultFileTitle = false;
        Map<String, CmsProperty> defaultFileProps = Maps.newHashMap();
        if (defaultFileRes != null) {
            defaultFileProps = getPropertiesByName(cms.readPropertyObjects(defaultFileRes, false));
            // determine if the title property of the default file should be changed
            changeDefaultFileTitle = shouldChangeDefaultFileTitle(
                defaultFileProps,
                ownProps.get(CmsPropertyDefinition.PROPERTY_NAVTEXT));
        }
        String hasNavTextChange = null;
        List<CmsProperty> ownPropertyChanges = new ArrayList<CmsProperty>();
        List<CmsProperty> defaultFilePropertyChanges = new ArrayList<CmsProperty>();
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
                if (CmsPropertyDefinition.PROPERTY_TITLE.equals(propMod.getName())) {
                    changeDefaultFileTitle = false;
                }
                propToModify = defaultFileProps.get(propMod.getName());
                if (propToModify == null) {
                    propToModify = new CmsProperty(propMod.getName(), null, null);
                }
                defaultFilePropertyChanges.add(propToModify);
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
            if (changeDefaultFileTitle) {
                CmsProperty titleProp = defaultFileProps.get(CmsPropertyDefinition.PROPERTY_TITLE);
                if (titleProp == null) {
                    titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, null);
                }
                titleProp.setStructureValue(hasNavTextChange);
                defaultFilePropertyChanges.add(titleProp);
            }
        }
        if (!ownPropertyChanges.isEmpty()) {
            cms.writePropertyObjects(ownRes, ownPropertyChanges);
        }
        if (!defaultFilePropertyChanges.isEmpty() && (defaultFileRes != null)) {
            cms.writePropertyObjects(defaultFileRes, defaultFilePropertyChanges);
        }
    }

}

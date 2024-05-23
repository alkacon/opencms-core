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

package org.opencms.gwt;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsGalleryDisabledTypesMode;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.CmsBroadcastMessage;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsCoreData.UserInfo;
import org.opencms.gwt.shared.CmsLockInfo;
import org.opencms.gwt.shared.CmsResourceCategoryInfo;
import org.opencms.gwt.shared.CmsReturnLinkInfo;
import org.opencms.gwt.shared.CmsTinyMCEData;
import org.opencms.gwt.shared.CmsUploadRestrictionInfo;
import org.opencms.gwt.shared.CmsUserSettingsBean;
import org.opencms.gwt.shared.CmsValidationQuery;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.i18n.CmsMessages;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsBroadcast;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.security.CmsPasswordInfo;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleManager;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.ui.CmsUserIconHelper;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContextWithAdeContext;
import org.opencms.ui.actions.I_CmsADEAction;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.contextmenu.CmsContextMenuTreeBuilder;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.dialogs.CmsEmbeddedDialogsUI;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsTreeNode;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceLoginHandler;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.containerpage.CmsADESessionCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.Buffer;
import org.apache.commons.logging.Log;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * Provides general core services.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.gwt.CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync
 */
public class CmsCoreService extends CmsGwtService implements I_CmsCoreService {

    /** The editor back-link URI. */
    private static final String EDITOR_BACKLINK_URI = "/system/workplace/commons/editor-backlink.html";

    /** The xml-content editor URI. */
    private static final String EDITOR_URI = "/system/workplace/editors/editor.jsp";

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCoreService.class);

    /** Serialization uid. */
    private static final long serialVersionUID = 5915848952948986278L;

    /** The workplace settings. */
    private CmsWorkplaceSettings m_workplaceSettings;

    /**
     * Builds the tree structure for the given categories.<p>
     *
     * @param cms the current cms context
     * @param categories the categories
     *
     * @return the tree root element
     */
    public static List<CmsCategoryTreeEntry> buildCategoryTree(CmsObject cms, List<CmsCategory> categories) {

        List<CmsCategoryTreeEntry> result = new ArrayList<CmsCategoryTreeEntry>();
        for (CmsCategory category : categories) {
            CmsCategoryTreeEntry current = new CmsCategoryTreeEntry(category);
            current.setSitePath(cms.getRequestContext().removeSiteRoot(category.getRootPath()));
            String parentPath = CmsResource.getParentFolder(current.getPath());
            CmsCategoryTreeEntry parent = null;
            parent = findCategory(result, parentPath);
            if (parent != null) {
                parent.addChild(current);
            } else {
                result.add(current);
            }
        }
        return result;
    }

    /**
     * Helper method for getting the category beans for the given site path.<p>
     *
     * @param cms the CMS context to use
     * @param sitePath the site path
     * @return the list of category beans
     *
     * @throws CmsException if something goes wrong
     */
    public static List<CmsCategoryTreeEntry> getCategoriesForSitePathStatic(CmsObject cms, String sitePath)
    throws CmsException {

        return getCategoriesForSitePathStatic(cms, sitePath, null);
    }

    /**
     * Helper method for getting the category beans for the given site path.<p>
     *
     * @param cms the CMS context to use
     * @param sitePath the site path
     * @param localCategoryRepositoryPath the categories for this repository are added separately
     * @return the list of category beans
     *
     * @throws CmsException if something goes wrong
     */
    public static List<CmsCategoryTreeEntry> getCategoriesForSitePathStatic(
        CmsObject cms,
        String sitePath,
        String localCategoryRepositoryPath)
    throws CmsException {

        List<CmsCategoryTreeEntry> result;
        CmsCategoryService catService = CmsCategoryService.getInstance();
        List<CmsCategory> categories;
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        // get the categories
        if (null == localCategoryRepositoryPath) {
            categories = catService.readCategories(cms, "", true, sitePath);
            categories = catService.localizeCategories(cms, categories, wpLocale);
            result = buildCategoryTree(cms, categories);
        } else {
            List<String> repositories = catService.getCategoryRepositories(cms, sitePath);
            repositories.remove(localCategoryRepositoryPath);
            categories = catService.readCategoriesForRepositories(cms, "", true, repositories);
            categories = catService.localizeCategories(cms, categories, wpLocale);
            result = buildCategoryTree(cms, categories);
            categories = catService.readCategoriesForRepositories(
                cms,
                "",
                true,
                Collections.singletonList(localCategoryRepositoryPath));
            categories = catService.localizeCategories(cms, categories, wpLocale);
            List<CmsCategoryTreeEntry> localCategories = buildCategoryTree(cms, categories);
            result.addAll(localCategories);
        }
        removeHiddenCategories(cms, result, entry -> false);
        return result;
    }

    /**
     * Returns the context menu entries for the given URI.<p>
     *
     * @param cms the cms context
     * @param structureId the currently requested structure id
     * @param context the ade context (sitemap or containerpage)
     * @param params the additional parameters
     *
     * @return the context menu entries
     */
    public static List<CmsContextMenuEntryBean> getContextMenuEntries(
        final CmsObject cms,
        CmsUUID structureId,
        final AdeContext context,
        Map<String, String> params) {

        Map<String, CmsContextMenuEntryBean> entries = new LinkedHashMap<String, CmsContextMenuEntryBean>();
        try {
            final List<CmsResource> resources;
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.ALL.addRequireVisible());
            // in case of sitemap editor check visibility with empty list
            if (context.equals(AdeContext.sitemapeditor)) {
                resources = Collections.emptyList();
                cms.getRequestContext().setAttribute(I_CmsDialogContext.ATTR_SITEMAP_CONFIG_RESOURCE, resource);
            } else {
                resources = Collections.singletonList(resource);
            }
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            final Map<String, String> paramsFinal = params != null ? params : new HashMap<>();
            // context to check item visibility
            I_CmsDialogContext dcontext = new I_CmsDialogContextWithAdeContext() {

                public void error(Throwable error) {

                    // not supported
                }

                public void finish(CmsProject project, String siteRoot) {

                    // not supported
                }

                public void finish(Collection<CmsUUID> result) {

                    // not supported
                }

                public void focus(CmsUUID id) {

                    // not supported
                }

                public AdeContext getAdeContext() {

                    return context;
                }

                public List<CmsUUID> getAllStructureIdsInView() {

                    return null;
                }

                public String getAppId() {

                    return context.name();
                }

                public CmsObject getCms() {

                    return cms;
                }

                public ContextType getContextType() {

                    ContextType type;
                    switch (context) {
                        case pageeditor:
                        case editprovider:
                            type = ContextType.containerpageToolbar;
                            break;
                        case sitemapeditor:
                            type = ContextType.sitemapToolbar;
                            break;
                        default:
                            type = ContextType.fileTable;
                    }
                    return type;
                }

                public Map<String, String> getParameters() {

                    return paramsFinal;
                }

                public List<CmsResource> getResources() {

                    return resources;
                }

                public void navigateTo(String appId) {

                    // not supported
                }

                public void onViewChange() {

                    // not supported
                }

                public void reload() {

                    // not supported
                }

                public void setWindow(Window window) {

                    // not supported
                }

                public void start(String title, Component dialog) {

                    // not supported
                }

                public void start(String title, Component dialog, DialogWidth width) {

                    // not supported
                }

                public void updateUserInfo() {

                    // not supported
                }
            };
            CmsContextMenuTreeBuilder builder = new CmsContextMenuTreeBuilder(dcontext);
            List<I_CmsContextMenuItem> items = new ArrayList<I_CmsContextMenuItem>();
            CmsTreeNode<I_CmsContextMenuItem> root = builder.buildAll(
                OpenCms.getWorkplaceAppManager().getMenuItemProvider().getMenuItems());
            for (CmsTreeNode<I_CmsContextMenuItem> child : root.getChildren()) {
                child.addDataInPreOrder(items);
            }
            Map<String, List<CmsContextMenuEntryBean>> submenus = new HashMap<String, List<CmsContextMenuEntryBean>>();
            for (I_CmsContextMenuItem item : items) {
                if (!item.isLeafItem()) {
                    CmsMenuItemVisibilityMode visibility = item.getVisibility(dcontext);
                    entries.put(
                        item.getId(),
                        new CmsContextMenuEntryBean(
                            visibility.isActive(),
                            true,
                            null,
                            item.getTitle(locale),
                            null,
                            CmsStringUtil.isEmptyOrWhitespaceOnly(visibility.getMessageKey())
                            ? null
                            : OpenCms.getWorkplaceManager().getMessages(locale).getString(visibility.getMessageKey()),
                            false,
                            new ArrayList<CmsContextMenuEntryBean>()));

                } else if ((item instanceof I_CmsADEAction) && ((I_CmsADEAction)item).isAdeSupported()) {
                    CmsMenuItemVisibilityMode visibility = item.getVisibility(dcontext);

                    String jspPath = ((I_CmsADEAction)item).getJspPath();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(jspPath)) {
                        jspPath = OpenCms.getLinkManager().substituteLink(cms, jspPath);
                    }
                    CmsContextMenuEntryBean itemBean = new CmsContextMenuEntryBean(
                        visibility.isActive(),
                        true,
                        jspPath,
                        item.getTitle(locale),
                        ((I_CmsADEAction)item).getCommandClassName(),
                        CmsStringUtil.isEmptyOrWhitespaceOnly(visibility.getMessageKey())
                        ? null
                        : OpenCms.getWorkplaceManager().getMessages(locale).getString(visibility.getMessageKey()),
                        false,
                        null);
                    Map<String, String> clientParams = ((I_CmsADEAction)item).getParams();
                    if (clientParams != null) {
                        clientParams = new HashMap<String, String>(clientParams);
                        for (Entry<String, String> param : clientParams.entrySet()) {
                            String value = CmsVfsService.prepareFileNameForEditor(cms, resource, param.getValue());
                            param.setValue(value);
                        }
                        itemBean.setParams(clientParams);
                    }
                    entries.put(item.getId(), itemBean);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(item.getParentId())) {
                        List<CmsContextMenuEntryBean> submenu;
                        if (submenus.containsKey(item.getParentId())) {
                            submenu = submenus.get(item.getParentId());
                        } else {
                            submenu = new ArrayList<CmsContextMenuEntryBean>();
                            submenus.put(item.getParentId(), submenu);
                        }
                        submenu.add(itemBean);
                    }
                }
            }
            List<CmsContextMenuEntryBean> result = new ArrayList<CmsContextMenuEntryBean>();
            for (I_CmsContextMenuItem item : items) {
                if (entries.containsKey(item.getId())) {
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(item.getParentId())) {
                        if (entries.containsKey(item.getParentId())) {
                            CmsContextMenuEntryBean parent = entries.get(item.getParentId());
                            if (parent.getSubMenu() != null) {
                                parent.getSubMenu().add(entries.get(item.getId()));
                            }
                        }
                    } else {
                        result.add(entries.get(item.getId()));
                    }
                }
            }
            return result;
        } catch (CmsException e) {
            // ignore, the user probably has not enough permissions to read the resource
            LOG.debug(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Returns the file explorer link prefix. Append resource site path for complete link.<p>
     *
     * @param cms the cms context
     * @param siteRoot the site root
     *
     * @return the file explorer link prefix
     */
    public static String getFileExplorerLink(CmsObject cms, String siteRoot) {

        return CmsVaadinUtils.getWorkplaceLink(
            CmsFileExplorerConfiguration.APP_ID,
            cms.getRequestContext().getCurrentProject().getUuid()
                + A_CmsWorkplaceApp.PARAM_SEPARATOR
                + siteRoot
                + A_CmsWorkplaceApp.PARAM_SEPARATOR);
    }

    /**
     * Returns the workplace link.<p>
     *
     * @param cms the cms context
     * @param structureId the structure id of the current resource
     *
     * @return the workplace link
     */
    public static String getVaadinWorkplaceLink(CmsObject cms, CmsUUID structureId) {

        String resourceRootFolder = null;

        if (structureId != null) {
            try {
                resourceRootFolder = CmsResource.getFolderPath(
                    cms.readResource(structureId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED).getRootPath());
            } catch (CmsException e) {
                LOG.debug("Error reading resource for workplace link.", e);
            }
        }
        if (resourceRootFolder == null) {
            resourceRootFolder = cms.getRequestContext().getSiteRoot();
        }
        return getVaadinWorkplaceLink(cms, resourceRootFolder);

    }

    /**
     * Returns the workplace link.<p>
     *
     * @param cms the cms context
     * @param resourceRootFolder the resource folder root path
     *
     * @return the workplace link
     */
    public static String getVaadinWorkplaceLink(CmsObject cms, String resourceRootFolder) {

        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resourceRootFolder);
        String siteRoot = site != null
        ? site.getSiteRoot()
        : OpenCms.getSiteManager().startsWithShared(resourceRootFolder)
        ? OpenCms.getSiteManager().getSharedFolder()
        : "";
        String sitePath = resourceRootFolder.substring(siteRoot.length());
        String link = getFileExplorerLink(cms, siteRoot) + sitePath;
        return link;
    }

    /**
     * Internal helper method for getting a validation service.<p>
     *
     * @param name the class name of the validation service
     *
     * @return the validation service
     *
     * @throws CmsException if something goes wrong
     */
    public static I_CmsValidationService getValidationService(String name) throws CmsException {

        try {
            Class<?> cls = Class.forName(name, false, I_CmsValidationService.class.getClassLoader());
            if (!I_CmsValidationService.class.isAssignableFrom(cls)) {
                throw new CmsIllegalArgumentException(
                    Messages.get().container(Messages.ERR_VALIDATOR_INCORRECT_TYPE_1, name));
            }
            return (I_CmsValidationService)cls.newInstance();
        } catch (ClassNotFoundException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_VALIDATOR_INSTANTIATION_FAILED_1, name), e);
        } catch (InstantiationException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_VALIDATOR_INSTANTIATION_FAILED_1, name), e);
        } catch (IllegalAccessException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_VALIDATOR_INSTANTIATION_FAILED_1, name), e);
        }
    }

    /**
     * Instantiates a class given its name using its default constructor.<p>
     *
     * Also checks whether the class with the given name is the subclass of another class/interface.<p>
     *
     *
     * @param <T> the type of the interface/class passed as a parameter
     *
     * @param anInterface the interface or class against which the class should be checked
     * @param className the name of the class
     * @return a new instance of the class
     *
     * @throws CmsException if the instantiation fails
     */
    public static <T> T instantiate(Class<T> anInterface, String className) throws CmsException {

        try {
            Class<?> cls = Class.forName(className, false, anInterface.getClassLoader());
            if (!anInterface.isAssignableFrom(cls)) {
                // class was found, but does not implement the interface
                throw new CmsIllegalArgumentException(
                    Messages.get().container(
                        Messages.ERR_INSTANTIATION_INCORRECT_TYPE_2,
                        className,
                        anInterface.getName()));
            }

            // we use another variable so we don't have to put the @SuppressWarnings on the method itself
            @SuppressWarnings("unchecked")
            Class<T> typedClass = (Class<T>)cls;
            return typedClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_INSTANTIATION_FAILED_1, className), e);
        } catch (InstantiationException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_INSTANTIATION_FAILED_1, className), e);
        } catch (IllegalAccessException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_INSTANTIATION_FAILED_1, className), e);
        }
    }

    /**
     * Implementation method for getting the link for a given return code.<p>
     *
     * @param cms the CMS context
     * @param returnCode the return code
     * @return the link for the return code
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsReturnLinkInfo internalGetLinkForReturnCode(CmsObject cms, String returnCode) throws CmsException {

        if (CmsUUID.isValidUUID(returnCode)) {
            try {
                CmsResource pageRes = cms.readResource(new CmsUUID(returnCode), CmsResourceFilter.IGNORE_EXPIRATION);
                return new CmsReturnLinkInfo(
                    OpenCms.getLinkManager().substituteLink(cms, pageRes),
                    CmsReturnLinkInfo.Status.ok);
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.debug(e.getLocalizedMessage(), e);
                return new CmsReturnLinkInfo(null, CmsReturnLinkInfo.Status.notfound);
            }
        } else {
            int colonIndex = returnCode.indexOf(':');
            if (colonIndex >= 0) {
                String before = returnCode.substring(0, colonIndex);
                String after = returnCode.substring(colonIndex + 1);

                if (CmsUUID.isValidUUID(before) && CmsUUID.isValidUUID(after)) {
                    try {
                        CmsUUID pageId = new CmsUUID(before);
                        CmsUUID detailId = new CmsUUID(after);
                        CmsResource pageRes = cms.readResource(pageId);
                        CmsResource folder = pageRes.isFolder()
                        ? pageRes
                        : cms.readParentFolder(pageRes.getStructureId());
                        String pageLink = OpenCms.getLinkManager().substituteLink(cms, folder);
                        CmsResource detailRes = cms.readResource(detailId);
                        String detailName = cms.getDetailName(
                            detailRes,
                            cms.getRequestContext().getLocale(),
                            OpenCms.getLocaleManager().getDefaultLocales());
                        String link = CmsFileUtil.removeTrailingSeparator(pageLink) + "/" + detailName;
                        return new CmsReturnLinkInfo(link, CmsReturnLinkInfo.Status.ok);
                    } catch (CmsVfsResourceNotFoundException e) {
                        LOG.debug(e.getLocalizedMessage(), e);
                        return new CmsReturnLinkInfo(null, CmsReturnLinkInfo.Status.notfound);

                    }
                }
            }
            throw new IllegalArgumentException("return code has wrong format");
        }
    }

    /**
     * Fetches the core data.<p>
     *
     * @param request the current request
     *
     * @return the core data
     */
    public static CmsCoreData prefetch(HttpServletRequest request) {

        CmsCoreService srv = new CmsCoreService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        CmsCoreData result = null;
        try {
            result = srv.prefetch();
        } finally {
            srv.clearThreadStorage();
        }
        return result;
    }

    /**
     * Recursively checks forced visibility for the entry and all its subentries.
     *
     * <p>A category is considered to have forced visibility if any of its subcategories match 'selectedCheck'.
     *
     * @param entry the category tree entry to check
     * @param selectedCheck a predicate that checks whether the parents of a category tree entry should be forced to be visible
     * @return
     */
    private static boolean checkForcedVisibility(
        CmsCategoryTreeEntry entry,
        Predicate<CmsCategoryTreeEntry> selectedCheck) {

        if (entry.getForcedVisible() == null) {
            boolean forcedVisible = selectedCheck.test(entry);
            for (CmsCategoryTreeEntry child : entry.getChildren()) {
                forcedVisible |= checkForcedVisibility(child, selectedCheck);
                // don't break out of the loop, we need to call checkForcedVisibility on everything
            }
            entry.setForcedVisible(Boolean.valueOf(forcedVisible));
        }
        return entry.getForcedVisible().booleanValue();
    }

    /**
     * FInds a category in the given tree.<p>
     *
     * @param tree the the tree to search in
     * @param path the path to search for
     *
     * @return the category with the given path or <code>null</code> if not found
     */
    private static CmsCategoryTreeEntry findCategory(List<CmsCategoryTreeEntry> tree, String path) {

        if (path == null) {
            return null;
        }
        // we assume that the category to find is descendant of tree
        List<CmsCategoryTreeEntry> children = tree;
        boolean found = true;
        while (found) {
            if (children == null) {
                return null;
            }
            // since the categories are sorted it is faster to go backwards
            found = false;
            for (int i = children.size() - 1; i >= 0; i--) {
                CmsCategoryTreeEntry child = children.get(i);
                if (path.equals(child.getPath())) {
                    return child;
                }
                if (path.startsWith(child.getPath())) {
                    children = child.getChildren();
                    found = true;
                    break;
                }
            }
        }
        return null;
    }

    /**
     * Removes hidden category tree entries.
     *
     *  <p>A category entry is considered hidden if one of its ancestors has the 'category.hidden' property with a value of 'true', and none of its subcategories have a structure id that
     *  is in 'selected'.
     *
     * @param cms the current CMS context
     * @param entries the entries to filter
     * @param selected the set of structure ids of categories whose ancestors should not be filtered (usually a set of categories already assigned to a resource)
     */
    private static void removeHiddenCategories(
        CmsObject cms,
        List<CmsCategoryTreeEntry> entries,
        Predicate<CmsCategoryTreeEntry> selectedCheck) {

        Iterator<CmsCategoryTreeEntry> iter = entries.iterator();
        while (iter.hasNext()) {
            CmsCategoryTreeEntry entry = iter.next();
            if (checkForcedVisibility(entry, selectedCheck)) {
                // this node is forced visible by one of the descendants, but there could still be other hidden children
                removeHiddenCategories(cms, entry.getChildren(), selectedCheck);
            } else {
                boolean hidden = false;
                try {
                    CmsResource resource = cms.readResource(entry.getId(), CmsResourceFilter.IGNORE_EXPIRATION);
                    CmsProperty hiddenProp = cms.readPropertyObject(
                        resource,
                        CmsPropertyDefinition.PROPERTY_CATEGORY_HIDDEN,
                        true);
                    hidden = Boolean.parseBoolean(hiddenProp.getValue());
                } catch (CmsVfsResourceNotFoundException | CmsSecurityException e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
                if (hidden) {
                    iter.remove();
                } else {
                    removeHiddenCategories(cms, entry.getChildren(), selectedCheck);
                }

            }
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#changePassword(java.lang.String, java.lang.String, java.lang.String)
     */
    public String changePassword(String oldPassword, String newPassword, String newPasswordConfirm)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsPasswordInfo passwordBean = new CmsPasswordInfo(cms);
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        try {
            passwordBean.setCurrentPwd(oldPassword);
            passwordBean.setNewPwd(newPassword);
            passwordBean.setConfirmation(newPasswordConfirm);
            passwordBean.applyChanges();
            return null;
        } catch (CmsSecurityException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return e.getMessageContainer().key(wpLocale);
        } catch (CmsIllegalArgumentException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return e.getMessageContainer().key(wpLocale);
        } catch (Exception e) {
            error(e);
            return null; // will never be executed
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#createUUID()
     */
    public CmsUUID createUUID() {

        return new CmsUUID();
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getBroadcast()
     */
    @SuppressWarnings("unchecked")
    public List<CmsBroadcastMessage> getBroadcast() {

        setBroadcastPoll();
        Set<CmsBroadcast> repeatedBroadcasts = new HashSet<CmsBroadcast>();
        OpenCms.getWorkplaceManager().checkWorkplaceRequest(getRequest(), getCmsObject());
        CmsSessionInfo sessionInfo = OpenCms.getSessionManager().getSessionInfo(getRequest().getSession());
        if (sessionInfo == null) {
            return null;
        }
        String sessionId = sessionInfo.getSessionId().toString();
        Buffer messageQueue = OpenCms.getSessionManager().getBroadcastQueue(sessionId);
        if (!messageQueue.isEmpty()) {
            CmsMessages messages = org.opencms.workplace.Messages.get().getBundle(
                OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject()));
            List<CmsBroadcastMessage> result = new ArrayList<CmsBroadcastMessage>();
            // the user has pending messages, display them all
            while (!messageQueue.isEmpty()) {

                CmsBroadcast broadcastMessage = (CmsBroadcast)messageQueue.remove();
                if ((broadcastMessage.getLastDisplay()
                    + CmsBroadcast.DISPLAY_AGAIN_TIME) < System.currentTimeMillis()) {
                    CmsUserIconHelper helper = OpenCms.getWorkplaceAppManager().getUserIconHelper();
                    String picPath = "";
                    if (broadcastMessage.getUser() != null) {
                        picPath = helper.getSmallIconPath(getCmsObject(), broadcastMessage.getUser());
                    }
                    CmsBroadcastMessage message = new CmsBroadcastMessage(
                        broadcastMessage.getUser() != null
                        ? broadcastMessage.getUser().getName()
                        : messages.key(org.opencms.workplace.Messages.GUI_LABEL_BROADCAST_FROM_SYSTEM_0),
                        picPath,
                        messages.getDateTime(broadcastMessage.getSendTime()),
                        broadcastMessage.getMessage());
                    result.add(message);
                    if (broadcastMessage.isRepeat()) {
                        repeatedBroadcasts.add(broadcastMessage.withLastDisplay(System.currentTimeMillis()));
                    }
                } else {
                    repeatedBroadcasts.add(broadcastMessage);
                }
            }
            if (!repeatedBroadcasts.isEmpty()) {
                for (CmsBroadcast broadcast : repeatedBroadcasts) {
                    messageQueue.add(broadcast);
                }
            }
            return result;
        }
        // no message pending, return null
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getCategories(java.lang.String, boolean, java.lang.String, boolean)
     */
    public List<CmsCategoryTreeEntry> getCategories(
        String fromPath,
        boolean includeSubCats,
        String refPath,
        boolean showWithRepositories,
        Set<String> selected)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        Set<CmsUUID> selectedIds = new HashSet<>();
        for (String path : selected) {
            try {
                CmsResource catResource = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
                selectedIds.add(catResource.getStructureId());
            } catch (CmsVfsResourceNotFoundException | CmsSecurityException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

        }
        return getCategoriesInternal(
            fromPath,
            includeSubCats,
            refPath,
            showWithRepositories,
            entry -> selectedIds.contains(entry.getId()));
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getCategoriesForSitePath(java.lang.String)
     */
    public List<CmsCategoryTreeEntry> getCategoriesForSitePath(String sitePath) throws CmsRpcException {

        List<CmsCategoryTreeEntry> result = null;
        CmsObject cms = getCmsObject();
        try {
            result = getCategoriesForSitePathStatic(cms, sitePath);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getCategoryInfo(org.opencms.util.CmsUUID)
     */
    public CmsResourceCategoryInfo getCategoryInfo(CmsUUID structureId) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsCategoryService catService = CmsCategoryService.getInstance();
        try {
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.ignoreExpirationOffline(cms));
            List<CmsCategory> categories = catService.readResourceCategories(cms, resource);
            List<String> currentCategories = new ArrayList<String>();
            Set<CmsUUID> selected = new HashSet<>();

            for (CmsCategory category : categories) {
                currentCategories.add(category.getPath());
                selected.add(category.getId());
            }
            return new CmsResourceCategoryInfo(
                structureId,
                CmsVfsService.getPageInfoWithLock(cms, resource),
                currentCategories,
                getCategoriesInternal(
                    null,
                    true,
                    cms.getSitePath(resource),
                    OpenCms.getWorkplaceManager().isDisplayCategoriesByRepository(),
                    entry -> selected.contains(entry.getId())));
        } catch (CmsException e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getContextMenuEntries(org.opencms.util.CmsUUID, org.opencms.gwt.shared.CmsCoreData.AdeContext)
     */
    public List<CmsContextMenuEntryBean> getContextMenuEntries(CmsUUID structureId, AdeContext context)
    throws CmsRpcException {

        List<CmsContextMenuEntryBean> result = null;
        try {
            result = getContextMenuEntries(getCmsObject(), structureId, context, new HashMap<>());
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getContextMenuEntries(org.opencms.util.CmsUUID, org.opencms.gwt.shared.CmsCoreData.AdeContext)
     */
    public List<CmsContextMenuEntryBean> getContextMenuEntries(
        CmsUUID structureId,
        AdeContext context,
        Map<String, String> params)
    throws CmsRpcException {

        List<CmsContextMenuEntryBean> result = null;
        try {
            result = getContextMenuEntries(getCmsObject(), structureId, context, params);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getLinkForReturnCode(java.lang.String)
     */
    public CmsReturnLinkInfo getLinkForReturnCode(String returnCode) throws CmsRpcException {

        try {
            return internalGetLinkForReturnCode(getCmsObject(), returnCode);
        } catch (Throwable e) {
            error(e);
            return null;

        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getResourceState(org.opencms.util.CmsUUID)
     */
    public CmsResourceState getResourceState(CmsUUID structureId) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsResourceState result = null;
        try {
            try {
                CmsResource res = cms.readResource(structureId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                result = res.getState();
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.debug(e.getLocalizedMessage(), e);
                result = CmsResourceState.STATE_DELETED;
            }
        } catch (CmsException e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getUniqueFileName(java.lang.String, java.lang.String)
     */
    public String getUniqueFileName(String parentFolder, String baseName) {

        return OpenCms.getResourceManager().getNameGenerator().getUniqueFileName(
            getCmsObject(),
            parentFolder,
            baseName);
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getUserInfo()
     */
    public UserInfo getUserInfo() {

        CmsObject cms = getCmsObject();
        CmsRoleManager roleManager = OpenCms.getRoleManager();
        boolean isAdmin = roleManager.hasRole(cms, CmsRole.ADMINISTRATOR);
        boolean isDeveloper = roleManager.hasRole(cms, CmsRole.DEVELOPER);
        boolean isCategoryManager = roleManager.hasRole(cms, CmsRole.CATEGORY_EDITOR);
        boolean isWorkplaceUser = roleManager.hasRole(cms, CmsRole.WORKPLACE_USER);
        UserInfo userInfo = new UserInfo(
            cms.getRequestContext().getCurrentUser().getName(),
            OpenCms.getWorkplaceAppManager().getUserIconHelper().getSmallIconPath(
                cms,
                cms.getRequestContext().getCurrentUser()),
            isAdmin,
            isDeveloper,
            isCategoryManager,
            isWorkplaceUser,
            cms.getRequestContext().getCurrentUser().isManaged());
        return userInfo;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getWorkplaceLink(org.opencms.util.CmsUUID)
     */
    public String getWorkplaceLink(CmsUUID structureId) throws CmsRpcException {

        String result = null;
        CmsObject cms = getCmsObject();
        try {
            String resourceRootFolder = structureId != null
            ? CmsResource.getFolderPath(
                cms.readResource(structureId, CmsResourceFilter.ALL.addRequireVisible()).getRootPath())
            : cms.getRequestContext().getSiteRoot();
            result = getVaadinWorkplaceLink(cms, resourceRootFolder);
        } catch (Exception e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getWorkplaceLinkForPath(java.lang.String)
     */
    public String getWorkplaceLinkForPath(String path) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsObject workCms = cms;
            if (path.startsWith("/sites/")) {
                workCms = OpenCms.initCmsObject(cms);
                workCms.getRequestContext().setSiteRoot("");
            }
            String currentPath = CmsResource.getParentFolder(path);
            CmsResource folder = null;
            try {
                folder = workCms.readResource(currentPath, CmsResourceFilter.IGNORE_EXPIRATION.addRequireVisible());
            } catch (CmsVfsResourceNotFoundException | CmsSecurityException e) {
                throw new CmsException(Messages.get().container(Messages.ERR_COULD_NOT_FIND_PARENT_FOLDER_1, path), e);
            }
            return getVaadinWorkplaceLink(cms, folder.getRootPath());
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#loadUserSettings()
     */
    public CmsUserSettingsBean loadUserSettings() throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsClientUserSettingConverter converter = new CmsClientUserSettingConverter(cms, getRequest(), getResponse());
        try {
            return converter.loadSettings();
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lockIfExists(java.lang.String)
     */
    public String lockIfExists(String sitePath) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        String errorMessage = null;
        try {
            if (cms.existsResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION)) {

                try {
                    ensureLock(cms.readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION));
                } catch (CmsException e) {
                    errorMessage = e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
                }

            } else {
                // check if parent folder may be locked by the current user
                String parentFolder = CmsResource.getParentFolder(sitePath);
                while ((parentFolder != null)
                    && !cms.existsResource(parentFolder, CmsResourceFilter.IGNORE_EXPIRATION)) {
                    parentFolder = CmsResource.getParentFolder(parentFolder);
                }
                if (parentFolder != null) {
                    CmsResource ancestorFolder = cms.readResource(parentFolder, CmsResourceFilter.IGNORE_EXPIRATION);
                    CmsUser user = cms.getRequestContext().getCurrentUser();
                    CmsLock lock = cms.getLock(ancestorFolder);
                    if (!lock.isLockableBy(user)) {
                        errorMessage = "Can not lock parent folder '" + parentFolder + "'.";
                    }
                } else {
                    errorMessage = "Can not access any parent folder.";
                }
            }
        } catch (Throwable e) {
            error(e);
        }

        return errorMessage;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lockIfExists(java.lang.String, long)
     */
    public String lockIfExists(String sitePath, long loadTime) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        String errorMessage = null;
        try {
            if (cms.existsResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION)) {

                try {
                    CmsResource resource = cms.readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION);
                    if (resource.getDateLastModified() > loadTime) {
                        // the resource has been changed since it was loaded
                        CmsUser user = null;
                        try {
                            user = cms.readUser(resource.getUserLastModified());
                        } catch (CmsException e) {
                            // ignore
                        }
                        CmsMessages messages = Messages.get().getBundle(
                            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
                        return user != null
                        ? messages.key(
                            Messages.ERR_LOCKING_MODIFIED_RESOURCE_2,
                            resource.getRootPath(),
                            user.getFullName())
                        : messages.key(Messages.ERR_LOCKING_MODIFIED_RESOURCE_1, resource.getRootPath());
                    }
                    ensureLock(resource);
                } catch (CmsException e) {
                    errorMessage = e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
                }

            } else {
                // check if parent folder may be locked by the current user
                String parentFolder = CmsResource.getParentFolder(sitePath);
                while ((parentFolder != null)
                    && !cms.existsResource(parentFolder, CmsResourceFilter.IGNORE_EXPIRATION)) {
                    parentFolder = CmsResource.getParentFolder(parentFolder);
                }
                if (parentFolder != null) {
                    CmsResource ancestorFolder = cms.readResource(parentFolder, CmsResourceFilter.IGNORE_EXPIRATION);
                    CmsUser user = cms.getRequestContext().getCurrentUser();
                    CmsLock lock = cms.getLock(ancestorFolder);
                    if (!lock.isLockableBy(user)) {
                        errorMessage = "Can not lock parent folder '" + parentFolder + "'.";
                    }
                } else {
                    errorMessage = "Can not access any parent folder.";
                }
            }
        } catch (Throwable e) {
            error(e);
        }

        return errorMessage;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lockTemp(org.opencms.util.CmsUUID)
     */
    public String lockTemp(CmsUUID structureId) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            try {
                ensureLock(structureId);
            } catch (CmsException e) {
                return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
            }
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lockTemp(org.opencms.util.CmsUUID, long)
     */
    public String lockTemp(CmsUUID structureId, long loadTime) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            try {
                CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                if (resource.getDateLastModified() > loadTime) {
                    // the resource has been changed since it was loaded
                    CmsUser user = null;
                    try {
                        user = cms.readUser(resource.getUserLastModified());
                    } catch (CmsException e) {
                        // ignore
                    }
                    CmsMessages messages = Messages.get().getBundle(
                        OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
                    return user != null
                    ? messages.key(Messages.ERR_LOCKING_MODIFIED_RESOURCE_2, resource.getRootPath(), user.getFullName())
                    : messages.key(Messages.ERR_LOCKING_MODIFIED_RESOURCE_1, resource.getRootPath());
                }
                ensureLock(resource);
            } catch (CmsException e) {
                return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
            }
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#prefetch()
     */
    public CmsCoreData prefetch() {

        CmsObject cms = getCmsObject();
        String navigationUri = cms.getRequestContext().getUri();
        CmsADEConfigData sitemapConfig = OpenCms.getADEManager().lookupConfigurationWithCache(
            cms,
            cms.getRequestContext().getRootUri());
        boolean toolbarVisible = CmsADESessionCache.getCache(getRequest(), getCmsObject()).isToolbarVisible();
        boolean isShowHelp = OpenCms.getADEManager().isShowEditorHelp(cms);

        CmsUUID structureId = null;

        try {
            CmsResource requestedResource = cms.readResource(
                cms.getRequestContext().getUri(),
                CmsResourceFilter.ignoreExpirationOffline(cms));
            structureId = requestedResource.getStructureId();
        } catch (CmsException e) {
            // may happen in case of VAADIN UI
            LOG.debug("Could not read resource for URI.", e);
            structureId = CmsUUID.getNullUUID();
        }
        String loginUrl = CmsWorkplaceLoginHandler.LOGIN_FORM;
        try {
            loginUrl = cms.readPropertyObject(
                cms.getRequestContext().getUri(),
                CmsPropertyDefinition.PROPERTY_LOGIN_FORM,
                true).getValue(loginUrl);
        } catch (CmsException e) {
            log(e.getLocalizedMessage(), e);
        }
        String defaultWorkplaceLink = OpenCms.getSystemInfo().getWorkplaceContext();
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        UserInfo userInfo = getUserInfo();
        String aboutLink = OpenCms.getLinkManager().substituteLink(
            getCmsObject(),
            "/system/workplace/commons/about.jsp");
        String tinyMCE = CmsWorkplace.getStaticResourceUri("/editors/tinymce/jscripts/tinymce/tinymce.min.js");
        boolean uploadDisabled = OpenCms.getWorkplaceManager().isAdeGalleryUploadDisabled(cms);
        CmsUploadRestrictionInfo uploadRestrictionInfo = OpenCms.getWorkplaceManager().getUploadRestriction().getUploadRestrictionInfo(
            cms);
        String categoryBaseFolder = CmsCategoryService.getInstance().getRepositoryBaseFolderName(cms);
        CmsGalleryDisabledTypesMode disabledTypesMode = sitemapConfig.getDisabledTypeMode(
            CmsGalleryDisabledTypesMode.mark);
        boolean hideDisabledTypes = disabledTypesMode == CmsGalleryDisabledTypesMode.hide;
        getWorkplaceSettings().getUserSettings();
        String checkReuseWarning = CmsUserSettings.getAdditionalPreference(cms, "checkReuseWarning", true);
        boolean warnWhenEditingReusedElement = Boolean.parseBoolean(checkReuseWarning);

        CmsCoreData data = new CmsCoreData(
            EDITOR_URI,
            EDITOR_BACKLINK_URI,
            loginUrl,
            OpenCms.getStaticExportManager().getVfsPrefix(),
            getFileExplorerLink(cms, cms.getRequestContext().getSiteRoot()),
            OpenCms.getSystemInfo().getStaticResourceContext(),
            CmsEmbeddedDialogsUI.getEmbeddedDialogsContextPath(),
            cms.getRequestContext().getSiteRoot(),
            OpenCms.getSiteManager().getSharedFolder(),
            cms.getRequestContext().getCurrentProject().getId(),
            cms.getRequestContext().getLocale().toString(),
            wpLocale.toString(),
            cms.getRequestContext().getUri(),
            navigationUri,
            structureId,
            new HashMap<String, String>(OpenCms.getResourceManager().getExtensionMapping()),
            CmsIconUtil.getExtensionIconMapping(),
            System.currentTimeMillis(),
            isShowHelp,
            toolbarVisible,
            defaultWorkplaceLink,
            aboutLink,
            userInfo,
            OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCmsObject()),
            OpenCms.getWorkplaceManager().isKeepAlive(),
            uploadDisabled,
            OpenCms.getADEManager().getParameters(getCmsObject()),
            uploadRestrictionInfo,
            categoryBaseFolder,
            hideDisabledTypes,
            warnWhenEditingReusedElement);
        CmsTinyMCEData tinyMCEData = new CmsTinyMCEData();
        tinyMCEData.setLink(tinyMCE);
        data.setTinymce(tinyMCEData);
        return data;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#saveUserSettings(java.util.Map, java.util.Set)
     */
    public void saveUserSettings(Map<String, String> userSettings, Set<String> edited) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsClientUserSettingConverter converter = new CmsClientUserSettingConverter(
                cms,
                getRequest(),
                getResponse());
            userSettings.keySet().retainAll(edited);
            converter.saveSettings(userSettings);
        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#setResourceCategories(org.opencms.util.CmsUUID, java.util.List)
     */
    public void setResourceCategories(CmsUUID structureId, List<String> categories) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsCategoryService catService = CmsCategoryService.getInstance();
        try {
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            ensureLock(resource);
            String sitePath = cms.getSitePath(resource);
            List<CmsCategory> previousCategories = catService.readResourceCategories(cms, resource);
            for (CmsCategory category : previousCategories) {
                if (categories.contains(category.getPath())) {
                    categories.remove(category.getPath());
                } else {
                    catService.removeResourceFromCategory(cms, sitePath, category);
                }
            }
            for (String path : categories) {
                if (!path.isEmpty()) { // Prevent adding category repositories itself.
                    catService.addResourceToCategory(cms, sitePath, path);
                }
            }
            tryUnlock(resource);
        } catch (Throwable t) {
            error(t);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#setShowEditorHelp(boolean)
     */
    public void setShowEditorHelp(boolean visible) throws CmsRpcException {

        try {
            OpenCms.getADEManager().setShowEditorHelp(getCmsObject(), visible);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#setToolbarVisible(boolean)
     */
    public void setToolbarVisible(boolean visible) throws CmsRpcException {

        try {
            ensureSession();
            CmsADESessionCache.getCache(getRequest(), getCmsObject()).setToolbarVisible(visible);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#unlock(org.opencms.util.CmsUUID)
     */
    public String unlock(CmsUUID structureId) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            tryUnlock(resource);
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#unlock(java.lang.String)
     */
    public String unlock(String sitePath) throws CmsRpcException {

        try {
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            cms.getRequestContext().setSiteRoot("");
            if (cms.existsResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION)) {
                CmsResource resource = cms.readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION);
                tryUnlock(resource);
            }
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject()));
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#validate(java.util.Map)
     */
    public Map<String, CmsValidationResult> validate(Map<String, CmsValidationQuery> validationQueries)
    throws CmsRpcException {

        try {
            Map<String, CmsValidationResult> result = new HashMap<String, CmsValidationResult>();
            for (Map.Entry<String, CmsValidationQuery> queryEntry : validationQueries.entrySet()) {
                String fieldName = queryEntry.getKey();
                CmsValidationQuery query = queryEntry.getValue();
                result.put(fieldName, validate(query.getValidatorId(), query.getValue(), query.getConfig()));
            }
            return result;
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#validate(java.lang.String, java.util.Map, java.util.Map, java.lang.String)
     */
    public Map<String, CmsValidationResult> validate(
        String formValidatorClass,
        Map<String, CmsValidationQuery> validationQueries,
        Map<String, String> values,
        String config)
    throws CmsRpcException {

        try {
            I_CmsFormValidator formValidator = instantiate(I_CmsFormValidator.class, formValidatorClass);
            return formValidator.validate(getCmsObject(), validationQueries, values, config);
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * Collect GWT build ids from the different ADE modules.<p>
     *
     * @return the map of GWT build ids
     */
    protected Map<String, String> getBuildIds() {

        List<CmsModule> modules = OpenCms.getModuleManager().getAllInstalledModules();
        Map<String, String> result = new HashMap<String, String>();
        for (CmsModule module : modules) {
            String buildid = module.getParameter(CmsCoreData.KEY_GWT_BUILDID);
            if (buildid != null) {
                result.put(module.getName(), buildid);
            }
        }
        return result;
    }

    /**
     * Helper method for locking a resource which returns some information on whether the locking
     * failed, and why.<p>
     *
     * @param structureId the structure id of the resource
     * @return the locking information
     *
     * @throws CmsException if something went wrong
     */
    protected CmsLockInfo getLock(CmsUUID structureId) throws CmsException {

        CmsResource res = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        return getLock(getCmsObject().getSitePath(res));
    }

    /**
     * Helper method for locking a resource which returns some information on whether the locking
     * failed, and why.<p>
     *
     * @param sitepath the site path of the resource to lock
     * @return the locking information
     *
     * @throws CmsException if something went wrong
     */
    protected CmsLockInfo getLock(String sitepath) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsLock lock = cms.getLock(sitepath);
        if (lock.isOwnedBy(user)) {
            return CmsLockInfo.forSuccess();
        }
        if (lock.getUserId().isNullUUID()) {
            cms.lockResourceTemporary(sitepath);
            return CmsLockInfo.forSuccess();
        }
        CmsUser owner = cms.readUser(lock.getUserId());
        return CmsLockInfo.forLockedResource(owner.getName());
    }

    /**
     * Helper method for reading and filtering categories.
     *
     * @param fromCatPath the category path to start with, can be <code>null</code> or empty to use the root
     * @param includeSubCats if to include all categories, or first level child categories only
     * @param refVfsPath the reference path (site-relative path according to which the available category repositories are determined),  can be <code>null</code> to only use the system repository
     * @param withRepositories flag, indicating if also the category repositories should be returned as category
     * @param selectedCheck a predicate that checks for categories whose ancestors should be included even if they are marked as hidden
     *
     * @return the resource categories
     *
     * @throws CmsRpcException if something goes wrong
    
     *
     * @return
     * @throws CmsRpcException
     */
    private List<CmsCategoryTreeEntry> getCategoriesInternal(
        String fromPath,
        boolean includeSubCats,
        String refPath,
        boolean showWithRepositories,
        Predicate<CmsCategoryTreeEntry> selectedCheck)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsCategoryService catService = CmsCategoryService.getInstance();

        List<String> repositories = new ArrayList<String>();
        repositories.addAll(catService.getCategoryRepositories(getCmsObject(), refPath));

        List<CmsCategoryTreeEntry> result = null;
        try {
            // get the categories
            List<CmsCategory> categories = catService.readCategoriesForRepositories(
                cms,
                fromPath,
                includeSubCats,
                repositories,
                showWithRepositories);
            categories = catService.localizeCategories(
                cms,
                categories,
                OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
            result = buildCategoryTree(cms, categories);
            removeHiddenCategories(cms, result, selectedCheck);

        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * Returns the workplace settings of the current user.<p>
     *
     * @return the workplace settings
     */
    private CmsWorkplaceSettings getWorkplaceSettings() {

        if (m_workplaceSettings == null) {
            m_workplaceSettings = CmsWorkplace.getWorkplaceSettings(getCmsObject(), getRequest());
        }
        return m_workplaceSettings;
    }

    /**
     * Internal helper method for validating a single value.<p>
     *
     * @param validator the class name of the validation service
     * @param value the value to validate
     * @param config the configuration for the validation service
     *
     * @return the result of the validation
     *
     * @throws Exception if something goes wrong
     */
    private CmsValidationResult validate(String validator, String value, String config) throws Exception {

        I_CmsValidationService validationService = getValidationService(validator);
        return validationService.validate(getCmsObject(), value, config);
    }
}

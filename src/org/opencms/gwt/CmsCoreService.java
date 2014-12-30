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

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.CmsAvailabilityInfoBean;
import org.opencms.gwt.shared.CmsBroadcastMessage;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsCoreData.UserInfo;
import org.opencms.gwt.shared.CmsLockInfo;
import org.opencms.gwt.shared.CmsResourceCategoryInfo;
import org.opencms.gwt.shared.CmsReturnLinkInfo;
import org.opencms.gwt.shared.CmsUserSettingsBean;
import org.opencms.gwt.shared.CmsValidationQuery;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsBroadcast;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.jobs.CmsPublishScheduledJob;
import org.opencms.security.CmsPasswordInfo;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleManager;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.explorer.CmsExplorerContextMenu;
import org.opencms.workplace.explorer.CmsExplorerContextMenuItem;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.explorer.menu.A_CmsMenuItemRule;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;
import org.opencms.workplace.explorer.menu.CmsMenuRule;
import org.opencms.workplace.explorer.menu.I_CmsMenuItemRule;
import org.opencms.xml.containerpage.CmsADESessionCache;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.Buffer;
import org.apache.commons.logging.Log;

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

    /** The default login URL. */
    private static final String DEFAULT_LOGIN_URL = "/system/login/index.html";

    /** The editor back-link URI. */
    private static final String EDITOR_BACKLINK_URI = "/system/modules/org.opencms.gwt/editor-backlink.html";

    /** The uri used for deleting. */
    private static final String EDITOR_DELETE_URI = "/system/workplace/commons/delete.jsp";

    /** The xml-content editor URI. */
    private static final String EDITOR_URI = "/system/workplace/editors/editor.jsp";

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCoreService.class);

    /** Serialization uid. */
    private static final long serialVersionUID = 5915848952948986278L;

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

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

        List<CmsCategoryTreeEntry> result;
        CmsCategoryService catService = CmsCategoryService.getInstance();
        // get the categories
        List<CmsCategory> categories = catService.readCategories(cms, "", true, sitePath);
        result = buildCategoryTree(cms, categories);
        return result;
    }

    /**
     * Returns the context menu entries for the given URI.<p>
     * 
     * @param cms the cms context
     * @param structureId the currently requested structure id 
     * @param context the ade context (sitemap or containerpage)
     * 
     * @return the context menu entries 
     */
    public static List<CmsContextMenuEntryBean> getContextMenuEntries(
        CmsObject cms,
        CmsUUID structureId,
        AdeContext context) {

        List<CmsContextMenuEntryBean> result = Collections.<CmsContextMenuEntryBean> emptyList();
        try {
            if (context != null) {
                cms.getRequestContext().setAttribute(I_CmsMenuItemRule.ATTR_CONTEXT_INFO, context.toString());
            }
            CmsResourceUtil[] resUtil = new CmsResourceUtil[1];
            resUtil[0] = new CmsResourceUtil(cms, cms.readResource(structureId, CmsResourceFilter.ONLY_VISIBLE));
            CmsResource resource = resUtil[0].getResource();
            if (hasViewPermissions(cms, resource)) {
                String fallbackType = resource.isFolder()
                ? CmsResourceTypeFolder.getStaticTypeName()
                : CmsResourceTypePlain.getStaticTypeName();
                String[] lookupTypes = {resUtil[0].getResourceTypeName(), fallbackType};

                for (String currentType : lookupTypes) {
                    // the explorer type settings
                    CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(currentType);
                    // only if the user has access to this resource type
                    if ((settings != null)) {
                        // get the context menu configuration for the given selection mode
                        CmsExplorerContextMenu contextMenu = settings.getContextMenu();
                        // transform the context menu into beans
                        List<CmsContextMenuEntryBean> allEntries = transformToMenuEntries(
                            cms,
                            contextMenu.getAllEntries(),
                            resUtil);
                        // filter the result
                        result = filterEntries(allEntries);
                        if (!result.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        } catch (CmsException e) {
            // ignore, the user probably has not enough permissions to read the resource
        }
        return result;
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
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_VALIDATOR_INCORRECT_TYPE_1,
                    name));
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
                throw new CmsIllegalArgumentException(Messages.get().container(
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
     * Filters the collection of menu entry beans.<p>
     * 
     * <ul>
     * <li>removes unnecessary separators</li>
     * <li>filters sub menus also</li>
     * <li>adds visible entries to the result</li>
     * </ul>
     * 
     * @see org.opencms.gwt.shared.CmsContextMenuEntryBean
     * 
     * @param allEntries the entries to filter
     * 
     * @return the filtered list of menu entries
     */
    private static List<CmsContextMenuEntryBean> filterEntries(List<CmsContextMenuEntryBean> allEntries) {

        // the resulting list
        List<CmsContextMenuEntryBean> result = new ArrayList<CmsContextMenuEntryBean>();
        CmsContextMenuEntryBean lastBean = null;

        // iterate over the list of collected menu entries to do the filtering
        for (CmsContextMenuEntryBean entry : allEntries) {
            if (entry.isVisible()) {
                // only if the entry is enabled
                if (entry.isSeparator()) {
                    if (!result.isEmpty()) {
                        // the entry is a separator and it isn't the first entry in the menu
                        if ((lastBean != null) && !lastBean.isSeparator()) {
                            // and there are no two separators behind each other
                            // add the separator
                            result.add(entry);
                        }
                    }
                } else if ((entry.getSubMenu() != null) && !entry.getSubMenu().isEmpty()) {
                    // the entry has a sub menu, so filter the entries of the sub menu
                    entry.setSubMenu(filterEntries(entry.getSubMenu()));
                    // add the entry with sub menu
                    result.add(entry);
                } else {
                    // it's a common entry, so add it
                    result.add(entry);
                }
                // store the last entry to check the separator
                lastBean = entry;
            }
        }
        // after the filtering is finished, remove the last separator if it is existent
        if (result.size() > 1) {
            if (result.get(result.size() - 1).isSeparator()) {
                result.remove(result.size() - 1);
            }
        }
        return result;
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
     * Collects the matching rules of all sub items of a parent context menu entry.<p>
     * 
     * @param cms the cms context
     * @param item the context menu item to check the sub items for
     * @param itemRules the collected rules for the sub items
     * @param resourceUtil the resources to be checked against the rules
     */
    private static void getSubItemRules(
        CmsObject cms,
        CmsExplorerContextMenuItem item,
        List<I_CmsMenuItemRule> itemRules,
        CmsResourceUtil[] resourceUtil) {

        for (CmsExplorerContextMenuItem subItem : item.getSubItems()) {

            if (subItem.isParentItem()) {
                // this is a parent item, recurse into sub items
                getSubItemRules(cms, subItem, itemRules, resourceUtil);
            } else if (CmsExplorerContextMenuItem.TYPE_ENTRY.equals(subItem.getType())) {
                // this is a standard entry, get the matching rule to add to the list
                String subItemRuleName = subItem.getRule();
                CmsMenuRule subItemRule = OpenCms.getWorkplaceManager().getMenuRule(subItemRuleName);
                if (subItemRule != null) {
                    I_CmsMenuItemRule rule = subItemRule.getMatchingRule(cms, resourceUtil);
                    if (rule != null) {
                        itemRules.add(rule);
                    }
                }
            }
        }
    }

    /**
     * Checks if the current user has view permissions on the given resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource to check
     * 
     * @return <code>true</code> if the current user has view permissions on the given resource
     */
    private static boolean hasViewPermissions(CmsObject cms, CmsResource resource) {

        try {
            return cms.hasPermissions(resource, CmsPermissionSet.ACCESS_VIEW, false, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            return false;
        }
    }

    /**
     * Returns a list of menu entry beans.<p>
     * 
     * Takes the given List of explorer context menu items and converts them to context menu entry beans.<p>
     * 
     * @see org.opencms.gwt.shared.CmsContextMenuEntryBean
     * @see org.opencms.workplace.explorer.CmsExplorerContextMenuItem
     * 
     * @param cms the cms context
     * @param items the menu items 
     * @param resUtil a resource utility array
     * 
     * @return a list of menu entries
     */
    private static List<CmsContextMenuEntryBean> transformToMenuEntries(
        CmsObject cms,
        List<CmsExplorerContextMenuItem> items,
        CmsResourceUtil[] resUtil) {

        // the resulting list
        List<CmsContextMenuEntryBean> result = new ArrayList<CmsContextMenuEntryBean>();

        // get the workplace manager
        CmsWorkplaceManager wpManager = OpenCms.getWorkplaceManager();

        // get the workplace message bundle
        CmsMessages messages = wpManager.getMessages(wpManager.getWorkplaceLocale(cms));

        for (CmsExplorerContextMenuItem item : items) {

            CmsContextMenuEntryBean bean = new CmsContextMenuEntryBean();

            if (!CmsExplorerContextMenuItem.TYPE_SEPARATOR.equals(item.getType())) {
                // this item is no separator (common entry or sub menu entry)

                // set the label to the bean
                if (item.getKey() != null) {
                    bean.setLabel(messages.key(item.getKey()));
                }

                // get the mode and set the bean
                CmsMenuItemVisibilityMode mode = CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                String itemRuleName = item.getRule();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(itemRuleName)) {
                    CmsMenuRule rule = wpManager.getMenuRule(itemRuleName);
                    if (rule != null) {
                        // get the first matching rule to apply for visibility
                        I_CmsMenuItemRule itemRule = rule.getMatchingRule(cms, resUtil);
                        if (itemRule != null) {
                            if (item.isParentItem()) {
                                // get the rules for the sub items
                                List<I_CmsMenuItemRule> itemRules = new ArrayList<I_CmsMenuItemRule>(
                                    item.getSubItems().size());
                                getSubItemRules(cms, item, itemRules, resUtil);
                                I_CmsMenuItemRule[] itemRulesArray = new I_CmsMenuItemRule[itemRules.size()];
                                // determine the visibility for the parent item

                                mode = itemRule.getVisibility(cms, resUtil, itemRules.toArray(itemRulesArray));
                            } else {
                                if (itemRule instanceof A_CmsMenuItemRule) {
                                    mode = ((A_CmsMenuItemRule)itemRule).getVisibility(cms, resUtil, item);
                                } else {
                                    mode = itemRule.getVisibility(cms, resUtil);
                                }
                            }
                        }
                    }
                }

                // set the visibility to the bean
                bean.setVisible(!mode.isInVisible());

                // set the activate info to the bean
                if (item.isParentItem()) {
                    // parent entries that have visible sub entries are always active
                    bean.setActive(true);
                } else {
                    // common entries can be activated or de-activated
                    bean.setActive(mode.isActive());
                    if (CmsStringUtil.isNotEmpty(mode.getMessageKey())) {
                        bean.setReason(messages.key(CmsEncoder.escapeXml(mode.getMessageKey())));
                    }
                }

                // get the JSP-URI and set it to the bean
                String jspPath = item.getUri();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(jspPath)) {
                    if (item.getUri().startsWith("/")) {
                        jspPath = OpenCms.getLinkManager().substituteLink(cms, item.getUri());
                    } else {
                        jspPath = OpenCms.getLinkManager().substituteLink(
                            cms,
                            CmsWorkplace.PATH_WORKPLACE + item.getUri());
                    }
                }
                bean.setJspPath(jspPath);

                String params = item.getParams();

                if (params != null) {
                    params = CmsVfsService.prepareFileNameForEditor(cms, resUtil[0].getResource(), params);
                    bean.setParams(CmsStringUtil.splitAsMap(params, "|", "="));

                }

                // get the name of the item and set it to the bean
                bean.setName(item.getName());
            }

            if (item.isParentItem()) {
                // this item has a sub menu
                bean.setSubMenu(transformToMenuEntries(cms, item.getSubItems(), resUtil));
            }

            if (CmsExplorerContextMenuItem.TYPE_SEPARATOR.equals(item.getType())) {
                // this item is a separator
                bean.setVisible(true);
                bean.setSeparator(true);
            }

            result.add(bean);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#changePassword(java.lang.String, java.lang.String, java.lang.String)
     */
    public String changePassword(String oldPassword, String newPassword, String newPasswordConfirm)
    throws CmsRpcException {

        System.out.println("changing password from " + oldPassword + " to " + newPassword);
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
    public List<CmsBroadcastMessage> getBroadcast() {

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
                CmsBroadcastMessage message = new CmsBroadcastMessage(
                    broadcastMessage.getUser() != null
                    ? broadcastMessage.getUser().getName()
                    : messages.key(org.opencms.workplace.Messages.GUI_LABEL_BROADCAST_FROM_SYSTEM_0),
                    messages.getDateTime(broadcastMessage.getSendTime()),
                    broadcastMessage.getMessage());
                result.add(message);
            }
            return result;
        }
        // no message pending, return null
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getCategories(java.lang.String, boolean, java.util.List)
     */
    public List<CmsCategoryTreeEntry> getCategories(String fromPath, boolean includeSubCats, List<String> refPaths)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsCategoryService catService = CmsCategoryService.getInstance();

        List<String> repositories = new ArrayList<String>();
        if ((refPaths != null) && !refPaths.isEmpty()) {
            for (String refPath : refPaths) {
                repositories.addAll(catService.getCategoryRepositories(getCmsObject(), refPath));
            }
        } else {
            repositories.add(CmsCategoryService.CENTRALIZED_REPOSITORY);
        }

        List<CmsCategoryTreeEntry> result = null;
        try {
            // get the categories
            List<CmsCategory> categories = catService.readCategoriesForRepositories(
                cms,
                fromPath,
                includeSubCats,
                repositories);
            result = buildCategoryTree(cms, categories);
        } catch (Throwable e) {
            error(e);
        }
        return result;
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
            CmsResource resource = cms.readResource(structureId);
            List<CmsCategory> categories = catService.readResourceCategories(cms, resource);
            List<String> currentCategories = new ArrayList<String>();
            for (CmsCategory category : categories) {
                currentCategories.add(category.getPath());
            }
            return new CmsResourceCategoryInfo(
                structureId,
                CmsVfsService.getPageInfoWithLock(cms, resource),
                currentCategories,
                getCategories(null, true, Collections.singletonList(cms.getSitePath(resource))));
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
            result = getContextMenuEntries(getCmsObject(), structureId, context);
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
            return internalGetLinkForReturnCode(returnCode);
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
                CmsResource res = cms.readResource(structureId);
                result = res.getState();
            } catch (CmsVfsResourceNotFoundException e) {
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

        return OpenCms.getResourceManager().getNameGenerator().getUniqueFileName(getCmsObject(), parentFolder, baseName);
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getWorkplaceLink(org.opencms.util.CmsUUID)
     */
    public String getWorkplaceLink(CmsUUID structureId) throws CmsRpcException {

        String result = null;
        try {
            String resourceRootFolder = structureId != null ? CmsResource.getFolderPath(getCmsObject().readResource(
                structureId).getRootPath()) : getCmsObject().getRequestContext().getSiteRoot();
            result = CmsExplorer.getWorkplaceExplorerLink(getCmsObject(), resourceRootFolder);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * Implementation method for getting the link for a given return code.<p>
     * 
     * @param returnCode the return code 
     * @return the link for the return code 
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsReturnLinkInfo internalGetLinkForReturnCode(String returnCode) throws CmsException {

        CmsObject cms = getCmsObject();

        if (CmsUUID.isValidUUID(returnCode)) {
            try {
                CmsResource pageRes = cms.readResource(new CmsUUID(returnCode));
                return new CmsReturnLinkInfo(CmsStringUtil.joinPaths(
                    OpenCms.getSystemInfo().getOpenCmsContext(),
                    cms.getSitePath(pageRes)), CmsReturnLinkInfo.Status.ok);
            } catch (CmsVfsResourceNotFoundException e) {
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
                        String pagePath = CmsResource.getFolderPath(cms.getSitePath(pageRes));
                        CmsResource detailRes = cms.readResource(detailId);
                        String detailName = cms.getDetailName(
                            detailRes,
                            cms.getRequestContext().getLocale(),
                            OpenCms.getLocaleManager().getDefaultLocales());
                        String uri = CmsStringUtil.joinPaths(pagePath, detailName);
                        return new CmsReturnLinkInfo(CmsStringUtil.joinPaths(
                            OpenCms.getSystemInfo().getOpenCmsContext(),
                            uri), CmsReturnLinkInfo.Status.ok);
                    } catch (CmsVfsResourceNotFoundException e) {
                        return new CmsReturnLinkInfo(null, CmsReturnLinkInfo.Status.notfound);

                    }
                }
            }
            throw new IllegalArgumentException("return code has wrong format");
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
        //        CmsUserSettingsBean result = new CmsUserSettingsBean();
        //        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        //        CmsMessages wpMessages = org.opencms.workplace.commons.Messages.get().getBundle(wpLocale);
        //
        //        CmsWorkplaceSettings workplaceSettings = CmsWorkplace.initAndStoreSettings(cms, getRequest().getSession());
        //        SelectOptions options = CmsPreferences.getOptionsForLanguage(
        //            workplaceSettings,
        //            workplaceSettings.getUserSettings());
        //
        //        CmsXmlContentProperty languageProp = new CmsXmlContentProperty("language",//name
        //            "string",//type
        //            "select_notnull",//widget
        //            options.toClientSelectWidgetConfiguration(),//widgetconfig
        //            null,//regex
        //            null,//ruletype
        //            null,//default
        //            wpMessages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_LANGUAGE_0),//nicename
        //            null,//description
        //            null,//error
        //            null//preferfolder
        //        );
        //        result.addSetting(wpLocale.toString(), languageProp);
        //
        //        for (int i = 0; i < 30; i++) {
        //            String propName = "User setting " + i;
        //            CmsXmlContentProperty prop = new CmsXmlContentProperty(propName,//name
        //                "string",//type
        //                "string",//widget
        //                "",//widgetconfig
        //                null,//regex
        //                null,//ruletype
        //                null,//default
        //                null,//nicename
        //                null,//description
        //                null,//error
        //                null//preferfolder
        //            );
        //
        //            result.addSetting("cow" + i, prop);
        //        }
        //        return result;

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
                while ((parentFolder != null) && !cms.existsResource(parentFolder, CmsResourceFilter.IGNORE_EXPIRATION)) {
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
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lockTempAndCheckModification(org.opencms.util.CmsUUID, long)
     */
    public CmsLockInfo lockTempAndCheckModification(CmsUUID structureId, long modification) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsResource resource = cms.readResource(structureId);
            if (resource.getDateLastModified() != modification) {
                CmsUser user = cms.readUser(resource.getUserLastModified());
                return CmsLockInfo.forChangedResource(user.getFullName());
            }
        } catch (Throwable e) {
            error(e);
        }
        try {
            return getLock(structureId);
        } catch (CmsException e) {
            return CmsLockInfo.forError(e.getLocalizedMessage());
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#ping()
     */
    public void ping() {

        // do nothing 
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#prefetch()
     */
    public CmsCoreData prefetch() {

        CmsObject cms = getCmsObject();
        String navigationUri = cms.getRequestContext().getUri();
        boolean toolbarVisible = getSessionCache().isToolbarVisible();
        boolean isShowHelp = OpenCms.getADEManager().isShowEditorHelp(cms);

        CmsUUID structureId = null;

        try {
            CmsResource requestedResource = cms.readResource(cms.getRequestContext().getUri());
            structureId = requestedResource.getStructureId();
        } catch (CmsException e) {
            throw new CmsRuntimeException(e.getMessageContainer(), e);
        }
        String loginUrl = DEFAULT_LOGIN_URL;
        try {
            loginUrl = cms.readPropertyObject(
                cms.getRequestContext().getUri(),
                CmsPropertyDefinition.PROPERTY_LOGIN_FORM,
                true).getValue(DEFAULT_LOGIN_URL);
        } catch (CmsException e) {
            log(e.getLocalizedMessage(), e);
        }
        String defaultWorkplaceLink = CmsExplorer.getWorkplaceExplorerLink(cms, cms.getRequestContext().getSiteRoot());
        CmsRoleManager roleManager = OpenCms.getRoleManager();
        boolean isAdmin = roleManager.hasRole(cms, CmsRole.ADMINISTRATOR);
        boolean isDeveloper = roleManager.hasRole(cms, CmsRole.DEVELOPER);
        boolean isCategoryManager = roleManager.hasRole(cms, CmsRole.CATEGORY_EDITOR);
        UserInfo userInfo = new UserInfo(
            cms.getRequestContext().getCurrentUser().getName(),
            isAdmin,
            isDeveloper,
            isCategoryManager);
        String aboutLink = OpenCms.getLinkManager().substituteLink(
            getCmsObject(),
            "/system/modules/org.opencms.gwt/about.jsp");
        CmsCoreData data = new CmsCoreData(
            EDITOR_URI,
            EDITOR_BACKLINK_URI,
            EDITOR_DELETE_URI,
            loginUrl,
            OpenCms.getStaticExportManager().getVfsPrefix(),
            cms.getRequestContext().getSiteRoot(),
            cms.getRequestContext().getLocale().toString(),
            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms).toString(),
            cms.getRequestContext().getUri(),
            navigationUri,
            structureId,
            new HashMap<String, String>(OpenCms.getResourceManager().getExtensionMapping()),
            System.currentTimeMillis(),
            isShowHelp,
            toolbarVisible,
            defaultWorkplaceLink,
            aboutLink,
            userInfo,
            OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCmsObject()),
            OpenCms.getWorkplaceManager().isKeepAlive(),
            OpenCms.getADEManager().getParameters(getCmsObject()));
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
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#setAvailabilityInfo(org.opencms.util.CmsUUID, org.opencms.gwt.shared.CmsAvailabilityInfoBean)
     */
    public void setAvailabilityInfo(CmsUUID structureId, CmsAvailabilityInfoBean bean) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            setAvailabilityInfo(res, bean);
        } catch (CmsException e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#setAvailabilityInfo(java.lang.String, org.opencms.gwt.shared.CmsAvailabilityInfoBean)
     */
    public void setAvailabilityInfo(String uri, CmsAvailabilityInfoBean bean) throws CmsRpcException {

        try {
            String sitePath = getCmsObject().getRequestContext().removeSiteRoot(uri);
            CmsResource resource = getCmsObject().readResource(sitePath);
            setAvailabilityInfo(resource, bean);
        } catch (CmsException e) {
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
            CmsResource resource = cms.readResource(structureId);
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
                catService.addResourceToCategory(cms, sitePath, path);
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
            getSessionCache().setToolbarVisible(visible);
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

        CmsObject cms = getCmsObject();
        try {
            if (cms.existsResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION)) {
                CmsResource resource = cms.readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION);
                tryUnlock(resource);
            }
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
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
        String config) throws CmsRpcException {

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

        CmsResource res = getCmsObject().readResource(structureId);
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
     * Returns the session cache.<p>
     * 
     * @return the session cache
     */
    private CmsADESessionCache getSessionCache() {

        if (m_sessionCache == null) {
            m_sessionCache = CmsADESessionCache.getCache(getRequest(), getCmsObject());
        }
        return m_sessionCache;
    }

    /**
     * Modifies the availability of the given resource.<p>
     * 
     * @param resource the resource whose availability should be modified 
     * @param dateReleased the date released
     * @param dateExpired the date expired
     * 
     * @throws CmsException if something goes wrong
     */
    private void modifyAvailability(CmsResource resource, long dateReleased, long dateExpired) throws CmsException {

        // modify release and expire date of the resource if needed
        getCmsObject().setDateReleased(resource, dateReleased, false);
        getCmsObject().setDateExpired(resource, dateExpired, false);
    }

    /**
     * Modifies the notification properties of the given resource.<p>
     * 
     * @param resource the resource whose notification properties should be modified 
     * @param notificationInterval the modification interval
     * @param notificationEnabled signals whether the notification is enabled or disabled
     * @param modifySiblings signals whether siblings should be also modified
     * 
     * @throws CmsException if something goes wrong
     */
    private void modifyNotification(
        CmsResource resource,
        int notificationInterval,
        boolean notificationEnabled,
        boolean modifySiblings) throws CmsException {

        List<CmsResource> resources = new ArrayList<CmsResource>();
        if (modifySiblings) {
            // modify all siblings of a resource
            resources = getCmsObject().readSiblings(resource, CmsResourceFilter.IGNORE_EXPIRATION);
        } else {
            // modify only resource without siblings
            resources.add(resource);
        }
        for (CmsResource curResource : resources) {
            String resourcePath = getCmsObject().getRequestContext().removeSiteRoot(curResource.getRootPath());
            // write notification settings
            writeProperty(
                resourcePath,
                CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL,
                String.valueOf(notificationInterval));
            writeProperty(
                resourcePath,
                CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION,
                String.valueOf(notificationEnabled));
        }

    }

    /**
     * Modifies the publish scheduled.<p>
     * 
     * Creates a temporary project and adds the given resource to it. Afterwards a scheduled job is created
     * and the project is assigned to it. Then the publish job is enqueued.<p>
     * 
     * @param resource the resource which should be scheduled for publishing  
     * @param pubDate the date when the resource should be published
     * 
     * @throws CmsException if something goes wrong
     */
    private void modifyPublishScheduled(CmsResource resource, long pubDate) throws CmsException {

        if (pubDate != CmsAvailabilityInfoBean.DATE_PUBLISH_SCHEDULED_DEFAULT) {

            CmsObject cms = getCmsObject();

            CmsUser user = getCmsObject().getRequestContext().getCurrentUser();
            Locale locale = getCmsObject().getRequestContext().getLocale();
            Date date = new Date(pubDate);

            // make copies from the admin cmsobject and the user cmsobject
            // get the admin cms object
            CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
            CmsObject cmsAdmin = action.getCmsAdminObject();
            // get the user cms object

            // set the current user site to the admin cms object
            cmsAdmin.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());

            // create the temporary project, which is deleted after publishing
            // the publish scheduled date in project name
            String dateTime = CmsDateUtil.getDateTime(date, DateFormat.SHORT, locale);
            CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(locale);
            String projectName = messages.key(
                org.opencms.workplace.commons.Messages.GUI_PUBLISH_SCHEDULED_PROJECT_NAME_2,
                new Object[] {resource.getName(), dateTime});

            // the HTML encoding for slashes is necessary because of the slashes in english date time format
            // in project names slahes are not allowed, because these are separators for organizaional units
            projectName = projectName.replace("/", "&#47;");
            // create the project
            CmsProject tmpProject = cmsAdmin.createProject(
                projectName,
                "",
                CmsRole.WORKPLACE_USER.getGroupName(),
                CmsRole.PROJECT_MANAGER.getGroupName(),
                CmsProject.PROJECT_TYPE_TEMPORARY);
            // make the project invisible for all users
            tmpProject.setHidden(true);
            // write the project to the database
            cmsAdmin.writeProject(tmpProject);
            // set project as current project
            cmsAdmin.getRequestContext().setCurrentProject(tmpProject);
            cms.getRequestContext().setCurrentProject(tmpProject);

            // copy the resource to the project
            cmsAdmin.copyResourceToProject(resource);

            // create a new scheduled job
            CmsScheduledJobInfo job = new CmsScheduledJobInfo();
            // the job name
            String jobName = projectName;
            // set the job parameters
            job.setJobName(jobName);
            job.setClassName("org.opencms.scheduler.jobs.CmsPublishScheduledJob");
            // create the cron expression
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            String cronExpr = ""
                + calendar.get(Calendar.SECOND)
                + " "
                + calendar.get(Calendar.MINUTE)
                + " "
                + calendar.get(Calendar.HOUR_OF_DAY)
                + " "
                + calendar.get(Calendar.DAY_OF_MONTH)
                + " "
                + (calendar.get(Calendar.MONTH) + 1)
                + " "
                + "?"
                + " "
                + calendar.get(Calendar.YEAR);
            // set the cron expression
            job.setCronExpression(cronExpr);
            // set the job active
            job.setActive(true);
            // create the context info
            CmsContextInfo contextInfo = new CmsContextInfo();
            contextInfo.setProjectName(projectName);
            contextInfo.setUserName(cmsAdmin.getRequestContext().getCurrentUser().getName());
            // create the job schedule parameter
            SortedMap<String, String> params = new TreeMap<String, String>();
            // the user to send mail to
            params.put(CmsPublishScheduledJob.PARAM_USER, user.getName());
            // the job name
            params.put(CmsPublishScheduledJob.PARAM_JOBNAME, jobName);
            // the link check
            params.put(CmsPublishScheduledJob.PARAM_LINKCHECK, "true");
            // add the job schedule parameter
            job.setParameters(params);
            // add the context info to the scheduled job
            job.setContextInfo(contextInfo);
            // add the job to the scheduled job list
            OpenCms.getScheduleManager().scheduleJob(cmsAdmin, job);
        }
    }

    /**
     * Sets the availability of a resource by modifying the date release, date expired, 
     * setting a scheduled publish job according to the info bean.<p>
     * 
     * Will also modify the notification settings of the resource.<p>
     * 
     * @param resource the resource to modify
     * @param bean the bean with the information of the dialog
     * 
     * @throws CmsException if something goes wrong  
     */
    private void setAvailabilityInfo(CmsResource resource, CmsAvailabilityInfoBean bean) throws CmsException {

        ensureLock(resource);
        modifyPublishScheduled(resource, bean.getDatePubScheduled());
        modifyAvailability(resource, bean.getDateReleased(), bean.getDateExpired());
        modifyNotification(
            resource,
            bean.getNotificationInterval(),
            bean.isNotificationEnabled(),
            bean.isModifySiblings());
        tryUnlock(resource);
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

    /**
     * Writes a property value for a resource.<p>
     * 
     * @param resourcePath the path of the resource
     * @param propertyName the name of the property
     * @param propertyValue the new value of the property
     * 
     * @throws CmsException if something goes wrong
     */
    private void writeProperty(String resourcePath, String propertyName, String propertyValue) throws CmsException {

        if (CmsStringUtil.isEmpty(propertyValue)) {
            propertyValue = CmsProperty.DELETE_VALUE;
        }

        CmsProperty newProp = new CmsProperty();
        newProp.setName(propertyName);
        CmsProperty oldProp = getCmsObject().readPropertyObject(resourcePath, propertyName, false);
        if (oldProp.isNullProperty()) {
            // property value was not already set
            if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                newProp.setStructureValue(propertyValue);
            } else {
                newProp.setResourceValue(propertyValue);
            }
        } else {
            if (oldProp.getStructureValue() != null) {
                newProp.setStructureValue(propertyValue);
                newProp.setResourceValue(oldProp.getResourceValue());
            } else {
                newProp.setResourceValue(propertyValue);
            }
        }

        newProp.setAutoCreatePropertyDefinition(true);

        String oldStructureValue = oldProp.getStructureValue();
        String newStructureValue = newProp.getStructureValue();
        if (CmsStringUtil.isEmpty(oldStructureValue)) {
            oldStructureValue = CmsProperty.DELETE_VALUE;
        }
        if (CmsStringUtil.isEmpty(newStructureValue)) {
            newStructureValue = CmsProperty.DELETE_VALUE;
        }

        String oldResourceValue = oldProp.getResourceValue();
        String newResourceValue = newProp.getResourceValue();
        if (CmsStringUtil.isEmpty(oldResourceValue)) {
            oldResourceValue = CmsProperty.DELETE_VALUE;
        }
        if (CmsStringUtil.isEmpty(newResourceValue)) {
            newResourceValue = CmsProperty.DELETE_VALUE;
        }

        // change property only if it has been changed            
        if (!oldResourceValue.equals(newResourceValue) || !oldStructureValue.equals(newStructureValue)) {
            getCmsObject().writePropertyObject(resourcePath, newProp);
        }
    }

}

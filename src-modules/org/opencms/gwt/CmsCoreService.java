/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsCoreService.java,v $
 * Date   : $Date: 2010/07/21 11:02:34 $
 * Version: $Revision: 1.16 $
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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsValidationQuery;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerContextMenu;
import org.opencms.workplace.explorer.CmsExplorerContextMenuItem;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;
import org.opencms.workplace.explorer.menu.CmsMenuRule;
import org.opencms.workplace.explorer.menu.I_CmsMenuItemRule;
import org.opencms.xml.sitemap.CmsSitemapManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides general core services.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.16 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync
 */
public class CmsCoreService extends CmsGwtService implements I_CmsCoreService {

    /** Serialization uid. */
    private static final long serialVersionUID = 5915848952948986278L;

    /**
     * Returns a new configured service instance.<p>
     * 
     * @param request the current request
     * 
     * @return a new service instance
     */
    public static CmsCoreService newInstance(HttpServletRequest request) {

        CmsCoreService srv = new CmsCoreService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        return srv;
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
    private static I_CmsValidationService getValidationService(String name) throws CmsException {

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
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getCategories(java.lang.String, boolean, java.util.List)
     */
    public CmsCategoryTreeEntry getCategories(String fromPath, boolean includeSubCats, List<String> refPaths)
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

        CmsCategoryTreeEntry result = null;
        try {
            result = new CmsCategoryTreeEntry(fromPath);
            // get the categories
            List<CmsCategory> categories = catService.readCategoriesForRepositories(
                cms,
                fromPath,
                includeSubCats,
                repositories);
            // convert them to a tree structure
            CmsCategoryTreeEntry parent = result;
            for (CmsCategory category : categories) {
                CmsCategoryTreeEntry current = new CmsCategoryTreeEntry(category);
                String parentPath = CmsResource.getParentFolder(current.getPath());
                if (!parentPath.equals(parent.getPath())) {
                    parent = findCategory(result, parentPath);
                }
                parent.addChild(current);
            }
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getContextMenuEntries(java.lang.String, org.opencms.gwt.shared.CmsCoreData.AdeContext)
     */
    public List<CmsContextMenuEntryBean> getContextMenuEntries(String uri, AdeContext context) throws CmsRpcException {

        List<CmsContextMenuEntryBean> result = null;
        CmsObject cms = getCmsObject();
        switch (context) {
            case containerpage:
                cms.getRequestContext().setAttribute(
                    I_CmsMenuItemRule.ATTR_CONTEXT_INFO,
                    I_CmsCoreService.CONTEXT_CONTAINERPAGE);
                break;
            case sitemap:
                cms.getRequestContext().setAttribute(
                    I_CmsMenuItemRule.ATTR_CONTEXT_INFO,
                    I_CmsCoreService.CONTEXT_SITEMAP);
                break;
            default:
                // nothing to do here
        }

        try {
            CmsResourceUtil[] resUtil = new CmsResourceUtil[1];
            resUtil[0] = new CmsResourceUtil(cms, cms.readResource(uri));

            // the explorer type settings
            CmsExplorerTypeSettings settings = null;

            // get the context menu configuration for the given selection mode
            CmsExplorerContextMenu contextMenu;

            // get the explorer type setting for the first resource
            try {
                settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resUtil[0].getResourceTypeName());
            } catch (Throwable e) {
                error(e);
            }
            if ((settings == null) || !settings.isEditable(cms, resUtil[0].getResource())) {
                // the user has no access to this resource type
                // could be configured in the opencms-vfs.xml or in the opencms-modules.xml
                return Collections.<CmsContextMenuEntryBean> emptyList();
            }
            // get the context menu
            contextMenu = settings.getContextMenu();

            // transform the context menu into beans
            List<CmsContextMenuEntryBean> allEntries = transformToMenuEntries(contextMenu.getAllEntries(), resUtil);

            // filter the result
            result = filterEntries(allEntries);

        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lock(java.lang.String)
     */
    public String lock(String uri) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            cms.lockResource(uri);
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lockTemp(java.lang.String)
     */
    public String lockTemp(String uri) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            cms.lockResourceTemporary(uri);
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lockTempAndCheckModification(java.lang.String, long)
     */
    public String lockTempAndCheckModification(String uri, long modification) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            // check time stamp
            if (cms.readResource(uri).getDateLastModified() != modification) {
                return Messages.get().container(Messages.ERR_RESOURCE_MODIFIED_AFTER_OPEN_1, uri).key();
            }
        } catch (Throwable e) {
            error(e);
        }
        // lock
        return lockTemp(uri);
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#prefetch()
     */
    public CmsCoreData prefetch() {

        CmsObject cms = getCmsObject();
        String navigationUri = CmsSitemapManager.getNavigationUri(cms, getRequest());
        CmsCoreData data = new CmsCoreData(
            OpenCms.getSystemInfo().getOpenCmsContext(),
            cms.getRequestContext().getSiteRoot(),
            cms.getRequestContext().getLocale().toString(),
            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms).toString(),
            cms.getRequestContext().getUri(),
            navigationUri);
        return data;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#translateUrlName(java.lang.String)
     */
    public String translateUrlName(String urlName) {

        String result = getCmsObject().getRequestContext().getFileTranslator().translateResource(urlName);
        result = result.replace('/', '_');
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#unlock(java.lang.String)
     */
    public String unlock(String uri) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsResource resource = cms.readResource(uri);
            CmsLock lock = cms.getLock(resource);
            if (lock.isUnlocked()) {
                return null;
            }
            cms.unlockResource(uri);
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * Performs a batch of validations and returns the results.<p>
     * 
     * @param validationQueries a map from field names to validation queries
     * 
     * @return a map from field names to validation results
     *  
     * @throws CmsRpcException if something goes wrong 
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
    private List<CmsContextMenuEntryBean> filterEntries(List<CmsContextMenuEntryBean> allEntries) {

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
        if (result.get(result.size() - 1).isSeparator()) {
            result.remove(result.size() - 1);
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
    private CmsCategoryTreeEntry findCategory(CmsCategoryTreeEntry tree, String path) {

        // we assume that the category to find is descendant of tree
        CmsCategoryTreeEntry parent = tree;
        if (path.equals(parent.getPath())) {
            return parent;
        }
        boolean found = true;
        while (found) {
            List<CmsCategoryTreeEntry> children = parent.getChildren();
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
                    parent = child;
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
     * @param item the context menu item to check the sub items for
     * @param itemRules the collected rules for the sub items
     * @param resourceUtil the resources to be checked against the rules
     */
    private void getSubItemRules(
        CmsExplorerContextMenuItem item,
        List<I_CmsMenuItemRule> itemRules,
        CmsResourceUtil[] resourceUtil) {

        for (CmsExplorerContextMenuItem subItem : item.getSubItems()) {

            if (subItem.isParentItem()) {
                // this is a parent item, recurse into sub items
                getSubItemRules(subItem, itemRules, resourceUtil);
            } else if (CmsExplorerContextMenuItem.TYPE_ENTRY.equals(subItem.getType())) {
                // this is a standard entry, get the matching rule to add to the list
                String subItemRuleName = subItem.getRule();
                CmsMenuRule subItemRule = OpenCms.getWorkplaceManager().getMenuRule(subItemRuleName);
                I_CmsMenuItemRule rule = subItemRule.getMatchingRule(getCmsObject(), resourceUtil);
                if (rule != null) {
                    itemRules.add(rule);
                }
            }
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
     * @param items the menu items 
     * @param resUtil a resource utility array
     * 
     * @return a list of menu entries
     */
    private List<CmsContextMenuEntryBean> transformToMenuEntries(
        List<CmsExplorerContextMenuItem> items,
        CmsResourceUtil[] resUtil) {

        // the resulting list
        List<CmsContextMenuEntryBean> result = new ArrayList<CmsContextMenuEntryBean>();

        // get the workplace message bundle
        CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(getCmsObject().getRequestContext().getLocale());

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
                    CmsMenuRule rule = OpenCms.getWorkplaceManager().getMenuRule(itemRuleName);
                    if (rule != null) {
                        // get the first matching rule to apply for visibility
                        I_CmsMenuItemRule itemRule = rule.getMatchingRule(getCmsObject(), resUtil);
                        if (itemRule != null) {
                            if (item.isParentItem()) {
                                // get the rules for the sub items
                                List<I_CmsMenuItemRule> itemRules = new ArrayList<I_CmsMenuItemRule>(
                                    item.getSubItems().size());
                                getSubItemRules(item, itemRules, resUtil);
                                I_CmsMenuItemRule[] itemRulesArray = new I_CmsMenuItemRule[itemRules.size()];
                                // determine the visibility for the parent item
                                mode = itemRule.getVisibility(
                                    getCmsObject(),
                                    resUtil,
                                    itemRules.toArray(itemRulesArray));
                            } else {
                                mode = itemRule.getVisibility(getCmsObject(), resUtil);
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
                        jspPath = OpenCms.getLinkManager().substituteLink(getCmsObject(), item.getUri());
                    } else {
                        jspPath = OpenCms.getLinkManager().substituteLink(
                            getCmsObject(),
                            CmsWorkplace.PATH_WORKPLACE + item.getUri());
                    }
                }
                bean.setJspPath(jspPath);

                // get the image-URI and set it to the bean
                String imagePath = item.getIcon();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(imagePath)) {
                    if (item.getIcon().startsWith("/")) {
                        imagePath = OpenCms.getLinkManager().substituteLink(getCmsObject(), item.getIcon());
                    } else {
                        imagePath = OpenCms.getLinkManager().substituteLink(
                            getCmsObject(),
                            CmsWorkplace.PATH_WORKPLACE + item.getIcon());
                    }
                }
                bean.setImagePath(imagePath);
            }

            if (item.isParentItem()) {
                // this item has a sub menu
                bean.setSubMenu(transformToMenuEntries(item.getSubItems(), resUtil));
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

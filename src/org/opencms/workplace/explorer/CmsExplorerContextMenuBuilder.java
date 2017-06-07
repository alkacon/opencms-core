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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.explorer;

import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.menu.A_CmsMenuItemRule;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;
import org.opencms.workplace.explorer.menu.CmsMenuRule;
import org.opencms.workplace.explorer.menu.CmsMenuRuleTranslator;
import org.opencms.workplace.explorer.menu.CmsMirMultiStandard;
import org.opencms.workplace.explorer.menu.I_CmsMenuItemRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Context menu builder class.<p>
 *
 * @since 6.5.6
 */
public class CmsExplorerContextMenuBuilder extends CmsWorkplace {

    /** The HTML code for a separator context menu entry. */
    private static final String HTML_SEPARATOR = "<li class=\"cmsep\"><span></span></li>";

    /** The link target parameter value. */
    private String m_paramActtarget;

    /** The resource list parameter value. */
    private String m_paramResourcelist;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsExplorerContextMenuBuilder(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsExplorerContextMenuBuilder(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Generates the context menu for the given resources.<p>
     *
     * @return html code
     */
    public String contextMenu() {

        // get the resource path list
        List<String> resourceList = CmsStringUtil.splitAsList(getParamResourcelist(), "|");

        // create a resource util object for the first resource in the list
        CmsResourceUtil[] resUtil = new CmsResourceUtil[resourceList.size()];
        for (int i = 0; i < resourceList.size(); i++) {
            try {
                resUtil[i] = new CmsResourceUtil(
                    getCms(),
                    getCms().readResource(resourceList.get(i), CmsResourceFilter.ALL));
            } catch (CmsException e) {
                // fatal error
                return "";
            }
        }

        // the explorer type settings
        CmsExplorerTypeSettings settings = null;

        // get the context menu configuration for the given selection mode
        CmsExplorerContextMenu contextMenu;

        // single or multi selection?
        boolean isSingleSelection = (resourceList.size() == 1);
        if (isSingleSelection) {
            // get the explorer type setting for the first resource
            try {
                settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resUtil[0].getResourceTypeName());
            } catch (Throwable e) {
                return "";
            }
            if ((settings == null) || !settings.isEditable(getCms(), resUtil[0].getResource())) {
                // the user has no access to this resource type
                return "";
            }
            // get the context menu configuration
            contextMenu = settings.getContextMenu();
        } else {
            // get the multi context menu configuration
            if (OpenCms.getWorkplaceManager().getMultiContextMenu() == null) {
                // no multi context menu defined, do not show menu
                return "";
            } else {
                contextMenu = OpenCms.getWorkplaceManager().getMultiContextMenu();
            }
        }

        // get an instance of the menu rule translator
        CmsMenuRuleTranslator menuRuleTranslator = new CmsMenuRuleTranslator();

        // store the mode results in a Map to optimize performance
        Map<String, CmsMenuItemVisibilityMode> storedModes = new HashMap<String, CmsMenuItemVisibilityMode>();

        StringBuffer menu = new StringBuffer(4096);

        menu.append("<div id=\"menu\">");
        buildHtmlContextMenu(
            contextMenu.getAllEntries(),
            null,
            menu,
            resUtil,
            menuRuleTranslator,
            isSingleSelection,
            storedModes);
        menu.append("</div>");
        return menu.toString();
    }

    /**
     * Returns the link target parameter value.<p>
     *
     * @return the link target parameter value
     */
    public String getParamActtarget() {

        return m_paramActtarget;
    }

    /**
     * Returns the resourcelist parameter value.<p>
     *
     * @return the resourcelist parameter value
     */
    public String getParamResourcelist() {

        return m_paramResourcelist;
    }

    /**
     * Sets the link target parameter value.<p>
     *
     * @param paramActtarget the link target parameter value to set
     */
    public void setParamActtarget(String paramActtarget) {

        m_paramActtarget = paramActtarget;
    }

    /**
     * Sets the resourcelist parameter value.<p>
     *
     * @param paramResourcelist the resourcelist parameter value to set
     */
    public void setParamResourcelist(String paramResourcelist) {

        m_paramResourcelist = paramResourcelist;
    }

    /**
     * Returns the HTML for the list of given context menu entry items.<p>
     *
     * @param contextMenuEntries the context menu entry items to loop
     * @param parent the parent context menu entry item or null if none is defined
     * @param menu the Buffer to add the HTML to
     * @param resUtil the initialized resource utility to create the context menu for
     * @param menuRuleTranslator the menu rule translator
     * @param isSingleSelection flag indicating if more than one resource is selected
     * @param storedModes caches the mode for the item rules
     */
    protected void buildHtmlContextMenu(
        List<CmsExplorerContextMenuItem> contextMenuEntries,
        CmsExplorerContextMenuItem parent,
        StringBuffer menu,
        CmsResourceUtil[] resUtil,
        CmsMenuRuleTranslator menuRuleTranslator,
        boolean isSingleSelection,
        Map<String, CmsMenuItemVisibilityMode> storedModes) {

        boolean insertSeparator = false;
        boolean firstEntryWritten = false;

        // open the menu list
        menu.append("\n<ul");
        if (parent != null) {
            // we are in a sub menu, set the id
            menu.append(" id=\"");
            menu.append(parent.getKey().hashCode());
            menu.append("\"");
        }
        menu.append(">");

        // loop the menu items
        for (CmsExplorerContextMenuItem item : contextMenuEntries) {
            // check if the current item is a sub item and collect the parent IDs
            StringBuffer parentIdsBuffer = new StringBuffer(64);
            CmsExplorerContextMenuItem pItem = item;
            boolean isFirst = true;
            while (pItem.isSubItem()) {
                // this is a sub item, collect parent IDs (used to determine which menus should be kept open)
                if (isFirst) {
                    parentIdsBuffer.append("'");
                    isFirst = false;
                } else {
                    parentIdsBuffer.append(",");
                }
                parentIdsBuffer.append(pItem.getParent().getKey().hashCode());
                pItem = pItem.getParent();
            }
            if (!isFirst) {
                parentIdsBuffer.append("'");
            }
            String parentIds = parentIdsBuffer.toString();

            if (item.isParentItem()) {
                // this is a parent item entry, first check if it is displayed at all

                CmsMenuItemVisibilityMode mode = CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                String itemRuleName = item.getRule();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(itemRuleName)) {
                    CmsMenuRule rule = OpenCms.getWorkplaceManager().getMenuRule(itemRuleName);
                    if (rule != null) {
                        // get the first matching rule to apply for visibility
                        I_CmsMenuItemRule itemRule = rule.getMatchingRule(getCms(), resUtil);
                        if (itemRule != null) {
                            // found a rule, now get the rules for all sub items
                            List<I_CmsMenuItemRule> itemRules = new ArrayList<I_CmsMenuItemRule>(
                                item.getSubItems().size());
                            getSubItemRules(item, itemRules, resUtil);
                            I_CmsMenuItemRule[] itemRulesArray = new I_CmsMenuItemRule[itemRules.size()];
                            // determine the visibility for the parent item
                            mode = itemRule.getVisibility(getCms(), resUtil, itemRules.toArray(itemRulesArray));
                        }
                    }
                }

                // only show the entry if visible sub items were found
                if (!mode.isInVisible()) {
                    if (insertSeparator) {
                        menu.append(HTML_SEPARATOR);
                        insertSeparator = false;
                    }
                    menu.append(
                        "\n<li><a class=\"x\" href=\"javascript:var ocm=1;\" onmouseover=\"window.status='';top.oSubC('");
                    menu.append(item.getKey().hashCode());
                    menu.append("'");
                    if (CmsStringUtil.isNotEmpty(parentIds)) {
                        // append the parent IDs to keep open
                        menu.append(",");
                        menu.append(parentIds);
                    }
                    menu.append(");return true;\">");
                    menu.append(key(item.getKey()));
                    menu.append("</a>");
                    // recurse into sub menu items
                    buildHtmlContextMenu(
                        item.getSubItems(),
                        item,
                        menu,
                        resUtil,
                        menuRuleTranslator,
                        isSingleSelection,
                        storedModes);
                    menu.append("</li>");
                    firstEntryWritten = true;
                }
            } else if (CmsExplorerContextMenuItem.TYPE_ENTRY.equals(item.getType())) {
                // this is a common menu entry

                // first determine name
                String itemName = key(item.getKey());

                CmsMenuRule customMenuRule = null;
                String itemRuleName = item.getRule();

                // check presence of item rule name and determine the correct rule name
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(itemRuleName)) {
                    if (isSingleSelection) {
                        // no new rule set defined, try to get it with the rule translator
                        if (menuRuleTranslator.hasMenuRule(item.getRules())) {
                            // this is a standard known rule, get the name of the matching rule set
                            itemRuleName = menuRuleTranslator.getMenuRuleName(item.getRules());
                            item.setRule(itemRuleName);
                        } else {
                            // no standard rule, create a new rule set from legacy rule String
                            customMenuRule = menuRuleTranslator.createMenuRule(item.getRules());
                            // set the rule name
                            itemRuleName = customMenuRule.getName();
                        }
                    } else {
                        // for multi context menu, use the standard rule if no rule set name was provided
                        itemRuleName = CmsMirMultiStandard.RULE_NAME;
                        if (!storedModes.containsKey(itemRuleName)) {
                            storedModes.put(itemRuleName, new CmsMirMultiStandard().getVisibility(getCms(), resUtil));
                        }
                    }
                }

                // first try to get the mode from the previously stored modes
                CmsMenuItemVisibilityMode mode = storedModes.get(itemRuleName);

                // no mode found in stored modes
                if (mode == null) {
                    // get the matching rule set
                    CmsMenuRule rule;
                    if (customMenuRule != null) {
                        rule = customMenuRule;
                    } else {
                        rule = OpenCms.getWorkplaceManager().getMenuRule(itemRuleName);
                    }
                    if (rule != null) {
                        // get the first matching rule to apply for visibility
                        I_CmsMenuItemRule itemRule = rule.getMatchingRule(getCms(), resUtil);
                        if (itemRule != null) {
                            // found a rule, get visibility mode and store it for later usage
                            if (itemRule instanceof A_CmsMenuItemRule) {
                                mode = ((A_CmsMenuItemRule)itemRule).getVisibility(getCms(), resUtil, item);
                            } else {
                                mode = itemRule.getVisibility(getCms(), resUtil);
                            }
                            storedModes.put(item.getRule(), mode);
                        }
                    }
                }
                if (mode != null) {
                    // found a visibility mode
                    if (mode.isActive()) {
                        // item is active

                        // determine link and target or item
                        String itemLink = " ";
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(item.getUri())) {
                            if (item.getUri().startsWith("/")) {
                                itemLink = getJsp().link(item.getUri());
                            } else {
                                itemLink = getJsp().link(CmsWorkplace.PATH_WORKPLACE + item.getUri());
                            }
                        }
                        String itemTarget = item.getTarget();
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(itemTarget)) {
                            itemTarget = "";
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamActtarget())
                                && (item.getUri() != null)
                                && item.getUri().startsWith("views/admin/admin-main.jsp")) {
                                itemTarget = CmsEncoder.escapeXml(getParamActtarget());
                            }
                        }

                        if (insertSeparator) {
                            menu.append(HTML_SEPARATOR);
                            insertSeparator = false;
                        }
                        StringBuffer link = new StringBuffer(128);
                        if (isSingleSelection) {
                            // create link for single resource context menu
                            link.append(" href=\"javascript:top.submitSingleAction('");
                            link.append(itemLink);
                            link.append("', '");
                            link.append(itemTarget);
                            link.append("');\"");
                        } else {
                            // create link for multi resource context menu
                            link.append(" href=\"javascript:top.submitMultiAction('");
                            link.append(itemLink);
                            link.append("');\"");
                        }
                        menu.append("\n<li><a ");
                        menu.append(link);

                        menu.append(" onmouseover=\"window.status='';top.cSubC(");
                        // append parent IDs to keep open
                        menu.append(parentIds);
                        menu.append(");return true;\"");

                        menu.append(">");
                        menu.append(itemName);
                        menu.append("</a></li>");
                        firstEntryWritten = true;
                    } else if (mode.isInActive()) {
                        // item is inactive
                        if (insertSeparator) {
                            menu.append(HTML_SEPARATOR);
                            insertSeparator = false;
                        }
                        menu.append("\n<li>");
                        menu.append("<a ");

                        menu.append(" onmouseover=\"window.status='';top.cSubC(");
                        // append parent IDs to keep open
                        menu.append(parentIds);
                        menu.append(");return true;\"");
                        // append inactive cause message if given
                        if (CmsStringUtil.isNotEmpty(mode.getMessageKey())) {
                            menu.append(" title=\"");
                            menu.append(getMessages().key(CmsEncoder.escapeXml(mode.getMessageKey())));
                            menu.append("\"");
                        }
                        menu.append(" class=\"ina\" href=\"javascript:var ocm=1;\">").append(itemName).append("</a>");
                        menu.append("</li>");
                        firstEntryWritten = true;
                    }
                }
            } else {
                // separator line, set flag to remember that a separator has been set, the separator will then
                // be written before the next visible item is appended
                if (firstEntryWritten) {
                    insertSeparator = true;
                }
            }
        } // end while
        menu.append("\n</ul>");
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(request);
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
                if (subItemRule != null) {
                    I_CmsMenuItemRule rule = subItemRule.getMatchingRule(getCms(), resourceUtil);
                    if (rule != null) {
                        itemRules.add(rule);
                    }
                }
            }
        }
    }

}
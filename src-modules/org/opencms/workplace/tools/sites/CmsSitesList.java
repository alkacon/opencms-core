/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.workplace.tools.sites;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.tools.CmsToolDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main site management view.<p>
 * 
 * @since 9.0.0 
 */
public class CmsSitesList extends A_CmsListDialog {

    /** The path of the fav icon. */
    protected static final String LIST_ICON_FAVICON = "tools/sites/icons/small/default-favicon.png";

    /** Holds - keys: site roots and values: favicon links. */
    protected static Map<String, String> m_icons = new HashMap<String, String>();

    /** A parameter name for the title of the site. */
    protected static final String PARAM_SITE_TITLE = "sitetitle";

    /** A parameter name for a comma separated list of site paths. */
    protected static final String PARAM_SITES = "sites";

    /** The favorite upload dialog. */
    private static final String DIALOG_FAV = "fav";

    /** List default action for editing a site. */
    private static final String LIST_ACTION_DEFAULT = "da";

    /** List action for editing a site. */
    private static final String LIST_ACTION_EDIT = "ea";

    /** List action for uploading a favicon. */
    private static final String LIST_ACTION_FAVICON = "fa";

    /** List action for removing a site. */
    private static final String LIST_ACTION_REMOVE = "ra";

    /** List column id for editing a site. */
    private static final String LIST_COLUMN_EDIT = "ce";

    /** List column id for the favicon. */
    private static final String LIST_COLUMN_FAVICON = "fav";

    /** List column id for the site path. */
    private static final String LIST_COLUMN_PATH = "cp";

    /** List column id for removing a site. */
    private static final String LIST_COLUMN_REMOVE = "cr";

    /** List column id for the site server's URL. */
    private static final String LIST_COLUMN_SERVER = "cs";

    /** List column id for the site name. */
    private static final String LIST_COLUMN_TITLE = "cn";

    /** Identifier for the show aliases action. */
    private static final String LIST_DETAIL_ALIASES = "ad";

    /** Identifier for the show secure sites action. */
    private static final String LIST_DETAIL_SECURE = "sd";

    /** Path to the edit icon. */
    private static final String LIST_ICON_EDIT = "tools/sites/icons/small/site-edit.png";

    /** The path of the remove icon. */
    private static final String LIST_ICON_REMOVE = "tools/sites/icons/small/site-remove.png";

    /** Identifier for the remove multi action. */
    private static final String LIST_MACTION_REMOVE = "rma";

    /** Id for the list of sites. */
    private static final String LIST_SITES_ID = "sites";

    /** A parameter name for the edit action. */
    private static final String PARAM_EDIT_ACTION = "editaction";

    /** Path to the reports. */
    private static final String PATH_REPORTS = "/system/workplace/admin/sites/reports/";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_SITES_ID,
            Messages.get().container(Messages.GUI_SITES_LIST_NAME_0),
            LIST_COLUMN_PATH,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws IOException, ServletException {

        if (getParamListAction().equals(LIST_MACTION_REMOVE)) {
            List<String> selectedSites = new ArrayList<String>();
            for (CmsListItem item : getSelectedItems()) {
                selectedSites.add(item.getId());
            }
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(PARAM_SITES, new String[] {CmsStringUtil.listAsString(selectedSites, ",")});
            params.put(PARAM_ACTION, new String[] {DIALOG_INITIAL});
            params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
            getToolManager().jspForwardPage(this, PATH_REPORTS + "remove.jsp", params);
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException {

        String site = getSelectedItem().getId();
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(PARAM_SITES, new String[] {site});
        params.put(PARAM_ACTION, new String[] {DIALOG_INITIAL});
        params.put(PARAM_SITE_TITLE, new String[] {OpenCms.getSiteManager().getSiteForSiteRoot(site).getTitle()});
        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            // forward to the edit site dialog
            params.put(PARAM_EDIT_ACTION, new String[] {CmsSitesDetailDialog.DIALOG_EDIT});
            getToolManager().jspForwardTool(this, "/sites/detail/edit", params);
        } else if (getParamListAction().equals(LIST_ACTION_DEFAULT)) {
            getToolManager().jspForwardTool(this, "/sites/detail", params);
        } else if (getParamListAction().equals(LIST_ACTION_REMOVE)) {
            // forward to the remove site dialog
            params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
            getToolManager().jspForwardPage(this, PATH_REPORTS + "remove.jsp", params);
        } else if (getParamListAction().equals(LIST_ACTION_FAVICON)) {
            // forward to the upload favorite page
            params.put(PARAM_EDIT_ACTION, new String[] {DIALOG_FAV});
            getToolManager().jspForwardTool(this, "/sites/detail/fav", params);
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        List<CmsListItem> items = getList().getAllContent();
        for (CmsListItem item : items) {
            String sitePath = item.getId();
            CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(sitePath);
            StringBuffer html = new StringBuffer(128);
            if (detailId.equals(LIST_DETAIL_ALIASES)) {
                boolean first = true;
                for (CmsSiteMatcher matcher : site.getAliases()) {
                    if (first) {
                        html.append(matcher.getUrl());
                        first = false;
                    } else {
                        html.append("<br/>" + matcher.getUrl());
                    }
                }
            }
            if (detailId.equals(LIST_DETAIL_SECURE) && site.hasSecureServer()) {
                html.append(site.getSecureUrl());
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> result = new ArrayList<CmsListItem>();
        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(getCms(), true);

        CmsObject cms = null;
        try {
            cms = OpenCms.initCmsObject(getCms());
            cms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            // noop
        }

        // clear the icons
        m_icons.clear();

        for (CmsSite site : sites) {
            if (site.getSiteMatcher() != null) {
                CmsListItem item = getList().newItem(site.getSiteRoot());
                item.set(LIST_COLUMN_PATH, site.getSiteRoot());
                item.set(LIST_COLUMN_SERVER, site.getUrl() != null ? site.getUrl() : "-");
                item.set(LIST_COLUMN_TITLE, site.getTitle() != null ? site.getTitle() : "-");
                if (cms != null) {
                    try {
                        CmsResource res = cms.readResource(site.getSiteRoot() + "/" + CmsSitesFaviconUpload.ICON_NAME);
                        m_icons.put(site.getSiteRoot(), getJsp().link(res.getRootPath()));
                    } catch (CmsException e) {
                        // noop
                    }
                }
                result.add(item);
            }
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create edit column
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_SITES_LIST_COLUMN_EDIT_NAME_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_SITES_LIST_COLUMN_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);
        // add edit action
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_SITES_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_SITES_LIST_ACTION_EDIT_HELP_0));
        editAction.setIconPath(LIST_ICON_EDIT);
        editCol.addDirectAction(editAction);
        metadata.addColumn(editCol);

        // create remove column
        CmsListColumnDefinition removeCol = new CmsListColumnDefinition(LIST_COLUMN_REMOVE);
        removeCol.setName(Messages.get().container(Messages.GUI_SITES_LIST_COLUMN_REMOVE_NAME_0));
        removeCol.setHelpText(Messages.get().container(Messages.GUI_SITES_LIST_COLUMN_REMOVE_HELP_0));
        removeCol.setWidth("20");
        removeCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        removeCol.setSorteable(false);
        // add remove action
        CmsListDirectAction removeAction = new CmsListDirectAction(LIST_ACTION_REMOVE);
        removeAction.setName(Messages.get().container(Messages.GUI_SITES_LIST_ACTION_REMOVE_NAME_0));
        removeAction.setHelpText(Messages.get().container(Messages.GUI_SITES_LIST_ACTION_REMOVE_HELP_0));
        removeAction.setIconPath(LIST_ICON_REMOVE);
        removeAction.setConfirmationMessage(Messages.get().container(Messages.GUI_SITES_LIST_MACTION_REMOVE_CONF_0));
        removeCol.addDirectAction(removeAction);
        metadata.addColumn(removeCol);

        // create favicon column
        CmsListColumnDefinition favCol = new CmsListColumnDefinition(LIST_COLUMN_FAVICON);
        favCol.setName(Messages.get().container(Messages.GUI_SITES_LIST_COLUMN_FAVICON_NAME_0));
        favCol.setHelpText(Messages.get().container(Messages.GUI_SITES_LIST_COLUMN_FAVICON_HELP_0));
        favCol.setWidth("20");
        favCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        favCol.setSorteable(false);
        // add fav icon action
        CmsListDirectAction faviconAction = new CmsListDirectAction(LIST_ACTION_FAVICON) {

            /**
             * @see org.opencms.workplace.list.A_CmsListAction#buttonHtml()
             */
            @Override
            public String buttonHtml() {

                String button = super.buttonHtml();
                String iconP = m_icons.get(getItem().getId());
                if (iconP != null) {
                    button = button.replaceAll(CmsWorkplace.getSkinUri() + getIconPath(), iconP);
                }
                return button;
            }
        };
        faviconAction.setName(Messages.get().container(Messages.GUI_SITES_LIST_ACTION_FAVICON_NAME_0));
        faviconAction.setHelpText(Messages.get().container(Messages.GUI_SITES_LIST_ACTION_FAVICON_HELP_0));
        faviconAction.setIconPath(LIST_ICON_FAVICON);
        favCol.addDirectAction(faviconAction);
        metadata.addColumn(favCol);

        // create server column
        CmsListColumnDefinition serverCol = new CmsListColumnDefinition(LIST_COLUMN_SERVER);
        serverCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        serverCol.setName(Messages.get().container(Messages.GUI_SITES_LIST_COLUMN_SERVER_NAME_0));
        serverCol.setWidth("33%");
        CmsListDefaultAction defAction = new CmsListDefaultAction(LIST_ACTION_DEFAULT);
        defAction.setName(Messages.get().container(Messages.GUI_SITES_LIST_ACTION_EDIT_NAME_0));
        defAction.setHelpText(Messages.get().container(Messages.GUI_SITES_LIST_ACTION_EDIT_HELP_0));
        serverCol.addDefaultAction(defAction);
        metadata.addColumn(serverCol);

        // create title column
        CmsListColumnDefinition titleCol = new CmsListColumnDefinition(LIST_COLUMN_TITLE);
        titleCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        titleCol.setName(Messages.get().container(Messages.GUI_SITES_LIST_COLUMN_TITLE_NAME_0));
        titleCol.setWidth("33%");
        metadata.addColumn(titleCol);

        // create path column
        CmsListColumnDefinition pathCol = new CmsListColumnDefinition(LIST_COLUMN_PATH);
        pathCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        pathCol.setName(Messages.get().container(Messages.GUI_SITES_LIST_COLUMN_PATH_NAME_0));
        pathCol.setWidth("33%");
        metadata.addColumn(pathCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add the detail action for showing aliases
        CmsListItemDetails aliasesDetails = new CmsListItemDetails(LIST_DETAIL_ALIASES);
        aliasesDetails.setAtColumn(LIST_COLUMN_SERVER);
        aliasesDetails.setVisible(false);
        aliasesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_SITES_DETAIL_LABEL_ALIASES_0)));
        aliasesDetails.setShowActionName(Messages.get().container(Messages.GUI_SITES_DETAIL_SHOW_ALIASES_NAME_0));
        aliasesDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_SITES_DETAIL_SHOW_ALIASES_HELP_0));
        aliasesDetails.setHideActionName(Messages.get().container(Messages.GUI_SITES_DETAIL_HIDE_ALIASES_NAME_0));
        aliasesDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_SITES_DETAIL_HIDE_ALIASES_HELP_0));
        // add author info item detail to meta data
        metadata.addItemDetails(aliasesDetails);

        // add the detail action for showing secure sites
        CmsListItemDetails secureDetails = new CmsListItemDetails(LIST_DETAIL_SECURE);
        secureDetails.setAtColumn(LIST_COLUMN_SERVER);
        secureDetails.setVisible(false);
        secureDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_SITES_DETAIL_LABEL_SECURE_0)));
        secureDetails.setShowActionName(Messages.get().container(Messages.GUI_SITES_DETAIL_SHOW_SECURE_NAME_0));
        secureDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_SITES_DETAIL_SHOW_SECURE_HELP_0));
        secureDetails.setHideActionName(Messages.get().container(Messages.GUI_SITES_DETAIL_HIDE_SECURE_NAME_0));
        secureDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_SITES_DETAIL_HIDE_SECURE_HELP_0));
        // add author info item detail to meta data
        metadata.addItemDetails(secureDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the remove multiple sites action
        CmsListMultiAction removeSites = new CmsListMultiAction(LIST_MACTION_REMOVE);
        removeSites.setName(Messages.get().container(Messages.GUI_SITES_LIST_MACTION_REMOVE_NAME_0));
        removeSites.setConfirmationMessage(Messages.get().container(Messages.GUI_SITES_LIST_MACTION_REMOVE_CONF_0));
        removeSites.setIconPath(ICON_MULTI_DELETE);
        removeSites.setEnabled(true);
        removeSites.setHelpText(Messages.get().container(Messages.GUI_SITES_LIST_MACTION_REMOVE_HELP_0));
        metadata.addMultiAction(removeSites);
    }
}

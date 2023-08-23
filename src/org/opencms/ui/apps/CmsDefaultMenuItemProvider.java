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

package org.opencms.ui.apps;

import org.opencms.main.CmsLog;
import org.opencms.ui.actions.CmsAboutDialogAction;
import org.opencms.ui.actions.CmsAvailabilityDialogAction;
import org.opencms.ui.actions.CmsCategoriesDialogAction;
import org.opencms.ui.actions.CmsChangeTypeDialogAction;
import org.opencms.ui.actions.CmsContextMenuActionItem;
import org.opencms.ui.actions.CmsCopyDialogAction;
import org.opencms.ui.actions.CmsCopyToProjectDialogAction;
import org.opencms.ui.actions.CmsDeleteDialogAction;
import org.opencms.ui.actions.CmsDirectPublishDialogAction;
import org.opencms.ui.actions.CmsDisplayAction;
import org.opencms.ui.actions.CmsEditCodeDialogAction;
import org.opencms.ui.actions.CmsEditContentAction;
import org.opencms.ui.actions.CmsEditDialogAction;
import org.opencms.ui.actions.CmsEditPageAction;
import org.opencms.ui.actions.CmsEditPointerAction;
import org.opencms.ui.actions.CmsEditPropertyAction;
import org.opencms.ui.actions.CmsEditSmallElementsAction;
import org.opencms.ui.actions.CmsFormEditDialogAction;
import org.opencms.ui.actions.CmsGalleryDialogAction;
import org.opencms.ui.actions.CmsGalleryOptimizeDialogAction;
import org.opencms.ui.actions.CmsHistoryDialogAction;
import org.opencms.ui.actions.CmsLinkLocaleVariantAction;
import org.opencms.ui.actions.CmsLockAction;
import org.opencms.ui.actions.CmsLockedResourcesAction;
import org.opencms.ui.actions.CmsLogoutAction;
import org.opencms.ui.actions.CmsMoveDialogAction;
import org.opencms.ui.actions.CmsPermissionDialogAction;
import org.opencms.ui.actions.CmsPrefillPageAction;
import org.opencms.ui.actions.CmsPreviewAction;
import org.opencms.ui.actions.CmsProjectDialogAction;
import org.opencms.ui.actions.CmsPropertiesDialogAction;
import org.opencms.ui.actions.CmsPublishQueueDialogAction;
import org.opencms.ui.actions.CmsPublishScheduledDialogAction;
import org.opencms.ui.actions.CmsReindexDialogAction;
import org.opencms.ui.actions.CmsRenameAction;
import org.opencms.ui.actions.CmsReplaceDialogAction;
import org.opencms.ui.actions.CmsResourceInfoAction;
import org.opencms.ui.actions.CmsRestoreDeletedAction;
import org.opencms.ui.actions.CmsSecureExportDialogAction;
import org.opencms.ui.actions.CmsSelectElementViewAction;
import org.opencms.ui.actions.CmsSeoAction;
import org.opencms.ui.actions.CmsShowLocaleAction;
import org.opencms.ui.actions.CmsSiteDialogAction;
import org.opencms.ui.actions.CmsSitemapAliasAction;
import org.opencms.ui.actions.CmsSitemapAttributeEditorAction;
import org.opencms.ui.actions.CmsSitemapEditConfigAction;
import org.opencms.ui.actions.CmsSitemapOpenParentAction;
import org.opencms.ui.actions.CmsSitemapRefreshAction;
import org.opencms.ui.actions.CmsStealLockAction;
import org.opencms.ui.actions.CmsTemplateContextsAction;
import org.opencms.ui.actions.CmsTouchDialogAction;
import org.opencms.ui.actions.CmsUndeleteDialogAction;
import org.opencms.ui.actions.CmsUndoDialogAction;
import org.opencms.ui.actions.CmsUnlinkLocaleVariantAction;
import org.opencms.ui.actions.CmsUnlockAction;
import org.opencms.ui.actions.CmsUnusedContentFinderAction;
import org.opencms.ui.actions.CmsViewInExplorerAction;
import org.opencms.ui.actions.CmsViewOnlineAction;
import org.opencms.ui.actions.CmsWorkplaceAction;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.contextmenu.CmsSubmenu;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider;
import org.opencms.xml.templatemapper.CmsTemplateMapperAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Default implementation of menu item provider.<p>
 */
public class CmsDefaultMenuItemProvider implements I_CmsContextMenuItemProvider {

    /** The advanced menu id. */
    public static final String ADVANCED_MENU_ID = "advanced";

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsDefaultMenuItemProvider.class);

    /** The menu items. */
    private static final List<I_CmsContextMenuItem> MENU_ITEMS;

    static {
        CmsSubmenu advanced = new CmsSubmenu(ADVANCED_MENU_ID, null, "%(key.GUI_EXPLORER_CONTEXT_ADVANCED_0)", 2410, 0);
        // the entries in this list will be sorted by there order property
        // for better readability please place additional entries  according to this sort order
        List<I_CmsContextMenuItem> items = Arrays.<I_CmsContextMenuItem> asList(

            // hack:  the prefill action must come first because it requires some special processing on the client
            new CmsContextMenuActionItem(new CmsPrefillPageAction(), null, 0, 0),

            new CmsContextMenuActionItem(new CmsSiteDialogAction(), null, 10, 0),
            new CmsContextMenuActionItem(new CmsEditPageAction(), null, 10, 0),
            new CmsContextMenuActionItem(new CmsEditDialogAction(), null, 50, 0),
            new CmsContextMenuActionItem(new CmsEditPointerAction(), null, 50, 0),
            new CmsContextMenuActionItem(new CmsViewOnlineAction(), null, 75, 0),
            new CmsContextMenuActionItem(new CmsEditContentAction(), null, 75, 0),
            new CmsContextMenuActionItem(new CmsPreviewAction(), null, 75, 0),
            new CmsContextMenuActionItem(new CmsDisplayAction(), null, 75, 0),
            new CmsContextMenuActionItem(new CmsSitemapOpenParentAction(), null, 75, 0),
            new CmsContextMenuActionItem(new CmsSitemapRefreshAction(), null, 100, 0),
            new CmsContextMenuActionItem(new CmsLockAction(), null, 100, 0),
            new CmsContextMenuActionItem(new CmsUnlockAction(), null, 100, 0),
            new CmsContextMenuActionItem(new CmsStealLockAction(), null, 100, 0),
            new CmsContextMenuActionItem(new CmsLockedResourcesAction(), null, 120, 0),
            new CmsContextMenuActionItem(new CmsGalleryDialogAction(), null, 130, 0),
            new CmsContextMenuActionItem(new CmsGalleryOptimizeDialogAction(), null, 131, 0),
            new CmsContextMenuActionItem(new CmsCopyToProjectDialogAction(), null, 150, 0),
            new CmsContextMenuActionItem(new CmsViewInExplorerAction(), null, 200, 0),
            new CmsContextMenuActionItem(new CmsDirectPublishDialogAction(), null, 300, 0),
            new CmsContextMenuActionItem(new CmsPublishScheduledDialogAction(), null, 400, 0),
            new CmsContextMenuActionItem(new CmsCopyDialogAction(), null, 900, 0),
            new CmsContextMenuActionItem(new CmsMoveDialogAction(), null, 1000, 0),
            new CmsContextMenuActionItem(
                new CmsEditPropertyAction(
                    CmsResourceTableProperty.PROPERTY_RESOURCE_NAME,
                    Messages.GUI_EXPLORER_RENAME_0),
                null,
                1100,
                0),
            new CmsContextMenuActionItem(new CmsRenameAction(), null, 1100, 0),
            new CmsContextMenuActionItem(new CmsDeleteDialogAction(), null, 1700, 0),
            new CmsContextMenuActionItem(new CmsReplaceDialogAction(), null, 1800, 0),
            new CmsContextMenuActionItem(new CmsUndoDialogAction(), null, 1900, 0),
            new CmsContextMenuActionItem(new CmsTemplateContextsAction(1), null, 1910, 0),
            new CmsContextMenuActionItem(new CmsShowLocaleAction(), null, 1925, 0),
            new CmsContextMenuActionItem(new CmsSelectElementViewAction(), null, 1950, 0),

            new CmsContextMenuActionItem(new CmsTemplateContextsAction(0), null, 1975, 0),

            new CmsContextMenuActionItem(new CmsEditSmallElementsAction(), null, 2000, 0),
            new CmsContextMenuActionItem(new CmsUndeleteDialogAction(), null, 2100, 0),
            new CmsContextMenuActionItem(new CmsResourceInfoAction(), null, 2200, 0),
            new CmsContextMenuActionItem(new CmsCategoriesDialogAction(), null, 2300, 0),
            new CmsContextMenuActionItem(new CmsPermissionDialogAction(), null, 2400, 0),

            advanced,
            new CmsContextMenuActionItem(new CmsTouchDialogAction(), advanced.getId(), 170, 0),

            new CmsContextMenuActionItem(new CmsAvailabilityDialogAction(), advanced.getId(), 300, 0),

            new CmsContextMenuActionItem(new CmsSecureExportDialogAction(), advanced.getId(), 500, 0),
            new CmsContextMenuActionItem(new CmsChangeTypeDialogAction(), advanced.getId(), 700, 0),
            new CmsContextMenuActionItem(new CmsFormEditDialogAction(), advanced.getId(), 800, 0),
            new CmsContextMenuActionItem(new CmsEditCodeDialogAction(), advanced.getId(), 900, 0),
            new CmsContextMenuActionItem(new CmsUnusedContentFinderAction(), advanced.getId(), 920, 0),
            new CmsContextMenuActionItem(new CmsReindexDialogAction(), advanced.getId(), 950, 0),
            new CmsContextMenuActionItem(new CmsRestoreDeletedAction(), advanced.getId(), 1000, 0),

            new CmsContextMenuActionItem(new CmsLinkLocaleVariantAction(), advanced.getId(), 1100, 0),
            new CmsContextMenuActionItem(new CmsUnlinkLocaleVariantAction(), advanced.getId(), 1150, 0),
            new CmsContextMenuActionItem(new CmsSeoAction(), advanced.getId(), 1200, 0),
            new CmsContextMenuActionItem(new CmsWorkplaceAction(), advanced.getId(), 1300, 0),

            new CmsContextMenuActionItem(new CmsSitemapAttributeEditorAction(), advanced.getId(), 1500, 0),
            new CmsContextMenuActionItem(new CmsSitemapEditConfigAction(), advanced.getId(), 1520, 0),
            new CmsContextMenuActionItem(new CmsSitemapAliasAction(), advanced.getId(), 1600, 0),
            new CmsContextMenuActionItem(new CmsTemplateMapperAction(), advanced.getId(), 1700, 0),

            new CmsContextMenuActionItem(new CmsHistoryDialogAction(), null, 2450, 0),
            new CmsContextMenuActionItem(new CmsPropertiesDialogAction(), null, 2500, 0),

            // toolbar menu entries
            new CmsContextMenuActionItem(new CmsProjectDialogAction(), null, 3100, 0),
            new CmsContextMenuActionItem(new CmsPublishQueueDialogAction(), null, 3500, 0),
            new CmsContextMenuActionItem(new CmsAboutDialogAction(), null, 3900, 0),
            new CmsContextMenuActionItem(new CmsLogoutAction(), null, 4100, 0));
        MENU_ITEMS = Collections.unmodifiableList(items);
    }

    /**
     * Creates a new instance.<p>
     */
    public CmsDefaultMenuItemProvider() {

        // default constructor, do nothing
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider#getMenuItems()
     */
    public List<I_CmsContextMenuItem> getMenuItems() {

        return MENU_ITEMS;
    }
}

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

package org.opencms.ui.actions;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleGroup;
import org.opencms.i18n.CmsLocaleGroupService;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilitySingleOnly;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.ui.sitemap.CmsLocaleLinkTargetSelectionDialog;
import org.opencms.ui.sitemap.I_CmsLocaleCompareContext;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Workplace action for the 'Link locale variant' dialog.<p>
 */
public class CmsLinkLocaleVariantAction extends A_CmsWorkplaceAction {

    /** The action id. */
    public static final String ACTION_ID = "linklocale";

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY = new CmsMenuItemVisibilitySingleOnly(
        CmsStandardVisibilityCheck.DEFAULT_DEFAULTFILE);

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLinkLocaleVariantAction.class);

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(final I_CmsDialogContext context) {

        try {
            final CmsResource resource = context.getResources().get(0);
            final CmsLocaleGroupService groupService = context.getCms().getLocaleGroupService();
            final CmsResource localizationRoot = groupService.findLocalizationRoot(resource);
            final CmsLocaleGroup localeGroup = groupService.readLocaleGroup(localizationRoot);
            CmsLocaleLinkTargetSelectionDialog dlg = new CmsLocaleLinkTargetSelectionDialog(
                context,
                new I_CmsLocaleCompareContext() {

                    public Locale getComparisonLocale() {

                        return OpenCms.getLocaleManager().getDefaultLocale(context.getCms(), getRoot());
                    }

                    public CmsLocaleGroup getLocaleGroup() {

                        return localeGroup;
                    }

                    public CmsResource getRoot() {

                        return localizationRoot;
                    }

                    public Locale getRootLocale() {

                        return OpenCms.getLocaleManager().getDefaultLocale(context.getCms(), getRoot());
                    }

                    public void refreshAll() {

                        // ignore
                    }
                });
            String title = CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_LINK_LOCALE_VARIANT_0);
            context.start(title, dlg);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            context.error(e);
        }
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return ACTION_ID;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getTitle()
     */
    public String getTitle() {

        return CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_LINK_LOCALE_VARIANT_0);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        if (resources.size() > 0) {
            CmsResource resource = resources.get(0);
            if (cms.getLocaleGroupService().getMainLocale(resource.getRootPath()) == null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
        }
        return VISIBILITY.getVisibility(cms, resources);
    }

}

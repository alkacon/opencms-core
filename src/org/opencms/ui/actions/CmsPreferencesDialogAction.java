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

package org.opencms.ui.actions;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.workplace.Messages;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;

/**
 * The user preferences dialog action.<p>
 */
public class CmsPreferencesDialogAction extends A_CmsWorkplaceAction {

    /** The action id. */
    public static final String ACTION_ID = "userpreferences";

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY = CmsStandardVisibilityCheck.MAIN_MENU;

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        try {
            CmsGwtDialogExtension dialogExtension = new CmsGwtDialogExtension(
                A_CmsUI.get(),
                new I_CmsUpdateListener<String>() {

                    public void onUpdate(List<String> updatedItems) {

                        URI uri = A_CmsUI.get().getPage().getLocation();
                        try {
                            String query = uri.getQuery();
                            final int idLength = 6;
                            String lrid = RandomStringUtils.randomAlphanumeric(idLength);
                            String prefix = "_lrid=";
                            // Add or replace _lrid parameter to force reload without triggering query about resubmitting POST parameters
                            if (query == null) {
                                query = prefix + lrid;
                            } else if (query.contains(prefix)) {
                                query = query.replaceFirst(prefix + ".{" + idLength + "}", prefix + lrid);
                            } else {
                                query = query + "&" + prefix + lrid;
                            }
                            URI newUri = new URI(
                                uri.getScheme(),
                                uri.getAuthority(),
                                uri.getPath(),
                                query,
                                uri.getFragment());
                            A_CmsUI.get().getPage().setLocation(newUri);
                        } catch (URISyntaxException e) {
                            A_CmsUI.get().getPage().reload();
                        }
                    }
                });
            dialogExtension.showUserPreferences();
        } catch (Exception e) {
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

        return getWorkplaceMessage(Messages.GUI_BUTTON_PREFERENCES_0);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return VISIBILITY.getVisibility(cms, resources);
    }
}

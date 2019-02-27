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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.A_CmsToolbarButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The toolbar button for the favorite dialog.
 */
public class CmsToolbarFavLocationButton extends A_CmsToolbarButton<CmsContainerpageHandler> {

    /**
     * Constructor.<p>
     *
     * @param handler the container page handler
     */
    public CmsToolbarFavLocationButton(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.BOOKMARKS_BUTTON, handler);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        // not used

    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsToolbarButton#onToolbarClick()
     */
    @Override
    public void onToolbarClick() {

        CmsEmbeddedDialogHandler handler = new CmsEmbeddedDialogHandler();
        Map<String, String> params = new HashMap<>();
        CmsCntPageData pageData = CmsContainerpageController.get().getData();
        CmsCoreData coreData = CmsCoreProvider.get();
        CmsUUID projectId = coreData.getProjectId();
        CmsUUID pageId = coreData.getStructureId();
        CmsUUID detailId = pageData.getDetailId();
        String siteRoot = coreData.getSiteRoot();
        params.put(CmsGwtConstants.Favorites.PARAM_DETAIL, "" + detailId);
        params.put(CmsGwtConstants.Favorites.PARAM_PAGE, "" + pageId);
        params.put(CmsGwtConstants.Favorites.PARAM_SITE, siteRoot);
        params.put(CmsGwtConstants.Favorites.PARAM_PROJECT, "" + projectId);
        handler.openDialog("org.opencms.ui.actions.CmsFavoriteDialogAction", null, new ArrayList<CmsUUID>(), params);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        // not used
    }

}

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

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsUpdateListener;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

/**
 * Displays all available app.<p>
 */
public class CmsAppHierachy implements I_CmsWorkplaceApp, I_CmsCachableApp {

    /** The serial version id. */
    private static final long serialVersionUID = -2767203655877536034L;

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    public void initUI(I_CmsAppUIContext context) {

        CmsObject cms = A_CmsUI.getCmsObject();
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        List<I_CmsWorkplaceAppConfiguration> visibleApps = Lists.newArrayList();
        for (I_CmsWorkplaceAppConfiguration appConfig : OpenCms.getWorkplaceAppManager().getWorkplaceApps()) {
            CmsAppVisibilityStatus status = appConfig.getVisibility(cms);
            if (status.isVisible()) {
                visibleApps.add(appConfig);
            }
        }
        CmsAppHierarchyBuilder hierarchyBuilder = new CmsAppHierarchyBuilder();
        for (I_CmsWorkplaceAppConfiguration app : visibleApps) {
            hierarchyBuilder.addAppConfiguration(app);
        }
        for (I_CmsAppCategory category : OpenCms.getWorkplaceAppManager().getCategories()) {
            hierarchyBuilder.addCategory(category);
        }

        CmsAppHierarchyPanel hierarchyPanel = new CmsAppHierarchyPanel(new CmsDefaultAppButtonProvider());
        hierarchyPanel.fill(hierarchyBuilder.buildHierarchy(), locale);

        context.setAppContent(hierarchyPanel);
        context.showInfoArea(false);
        context.addPublishButton(new I_CmsUpdateListener<String>() {

            public void onUpdate(List<String> updatedItems) {
                // ignore
            }
        });
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCachableApp#isCachable()
     */
    public boolean isCachable() {

        return true;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCachableApp#onRestoreFromCache()
     */
    public void onRestoreFromCache() {
        // do nothing
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    public void onStateChange(String state) {

        // nothing to do
    }
}

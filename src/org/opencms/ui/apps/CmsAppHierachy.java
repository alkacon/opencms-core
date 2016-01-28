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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

/**
 * Displays all available app.<p>
 */
public class CmsAppHierachy implements I_CmsWorkplaceApp {

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
        for (CmsAppCategory category : OpenCms.getWorkplaceAppManager().getCategories()) {
            hierarchyBuilder.addCategory(category);
        }

        CmsAppHierarchyPanel hierarchyPanel = new CmsAppHierarchyPanel(new CmsDefaultAppButtonProvider());
        hierarchyPanel.fill(hierarchyBuilder.buildHierarchy(), locale);

        context.setAppContent(hierarchyPanel);
        context.showInfoArea(false);

        Button publishButton = CmsToolBar.createButton(
            FontOpenCms.PUBLISH,
            CmsVaadinUtils.getMessageText(Messages.GUI_PUBLISH_BUTTON_TITLE_0));
        publishButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClickPublish();
            }
        });
        context.addToolbarButton(publishButton);
    }

    /**
     * Triggered when the user clicks the 'publsh' button.<p>
     */
    public void onClickPublish() {

        CmsGwtDialogExtension extension = new CmsGwtDialogExtension(A_CmsUI.get(), new I_CmsUpdateListener<String>() {

            public void onUpdate(List<String> updatedItems) {
                // ignore
            }
        });
        extension.openPublishDialog(A_CmsUI.getCmsObject().getRequestContext().getCurrentProject());
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    public void onStateChange(String state) {

        // nothing to do
    }

}

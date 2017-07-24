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

package org.opencms.ui.apps.cacheadmin;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Vaadin app for Cache Administration.<p>
 * Functions: view flex and image caches, flush cashes.<p>
 */
public class CmsCacheAdminApp extends A_CmsWorkplaceApp {

    /**Width of the progressbars to show current memory usage.*/
    static final String PROGRESSBAR_WIDTH = "200px";

    /**width of the statistic info boxes.*/
    static final String STATISTIC_INFOBOX_WIDTH = "300px";

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        // Check if state is empty -> start
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_ADMIN_TOOL_NAME_SHORT_0));
            return crumbs;
        }

        return new LinkedHashMap<String, String>(); //size==1 & state was not empty -> state doesn't match to known path
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            m_rootLayout.setMainHeightFull(false);
            return getStartComponent();
        }

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;

    }

    /**
     * Creates the component to be shown on accessing the app with some statistical information about the caches.<p>
     *
     * @return a vaadin vertical layout component
     */
    private Component getStartComponent() {

        VerticalLayout outer = new VerticalLayout();
        HorizontalLayout layout = new HorizontalLayout();

        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        //Statistic about heap space
        Panel java = new Panel();
        //        java.setWidth("400px");
        java.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_0));
        java.setContent(CmsCacheViewApp.getJavaStatisticButton().getInfoLayout());

        layout.addComponent(java);

        Panel flex = new Panel();
        //        flex.setWidth("400px");
        flex.setContent(CmsCacheViewApp.getFlexStatisticButton().getInfoLayout());

        flex.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEX_0));

        layout.addComponent(flex);

        // Disabled due to performance issues
        //        Panel image = new Panel();
        //        //        image.setWidth("400px");
        //        image.setContent(new CmsImageCacheInfoLayout());
        //        image.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGE_0));
        //
        //        layout.addComponent(image);
        layout.addComponent(new CmsFlushCache());
        outer.addComponent(layout);
        //        outer.setComponentAlignment(outer.getComponent(1), com.vaadin.ui.Alignment.TOP_LEFT);
        return outer;
    }
}

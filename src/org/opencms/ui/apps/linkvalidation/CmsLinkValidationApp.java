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

package org.opencms.ui.apps.linkvalidation;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsDefaultAppButtonProvider;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

/**
 * Class for the Link validation app.<p>
 */
public class CmsLinkValidationApp extends A_CmsWorkplaceApp {

    /**Icon for external validation. */
    private static final String ICON_EXTERNAL_VALIDATION = "apps/linkvalidation/link_extern.png";

    /**Path to internal.*/
    static final String PATH_INTERNAL_VALIDATION = "internal";

    /**Path to external.*/
    static final String PATH_EXTERNAL_VALIDATION = "external";

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        //Main page.
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_LINKVALIDATION_ADMIN_TOOL_NAME_SHORT_0));
            return crumbs;
        }

        //Deeper path
        crumbs.put(
            CmsLinkValidationConfiguration.APP_ID,
            CmsVaadinUtils.getMessageText(Messages.GUI_LINKVALIDATION_ADMIN_TOOL_NAME_SHORT_0));

        //View Flex Cache
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state) | state.startsWith(PATH_INTERNAL_VALIDATION)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_LINKVALIDATION_CHECK_INTERNAL_LINK_NAME_0));
        } else if (state.startsWith(PATH_EXTERNAL_VALIDATION)) {
            crumbs.put(
                "",
                CmsVaadinUtils.getMessageText(Messages.GUI_LINKVALIDATION_EXTERNALLINK_ADMIN_TOOL_NAME_SHORT_0));
        }
        if (crumbs.size() > 1) {
            return crumbs;
        } else {
            return new LinkedHashMap<String, String>(); //size==1 & state was not empty -> state doesn't match to known path
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (state.isEmpty()) {
            m_rootLayout.setMainHeightFull(true);
            return getInternalComponent();
        }
        if (state.startsWith(PATH_EXTERNAL_VALIDATION)) {
            m_rootLayout.setMainHeightFull(false);
            return new CmsLinkValidationExternal();
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
     * Returns the component for the internal link validation.<p>
     *
     * @return vaadin component
     */
    private HorizontalSplitPanel getInternalComponent() {

        m_rootLayout.setMainHeightFull(true);
        HorizontalSplitPanel sp = new HorizontalSplitPanel();
        sp.setSizeFull();
        CmsLinkValidationInternalTable table = new CmsLinkValidationInternalTable();
        table.setSizeFull();
        VerticalLayout leftCol = new VerticalLayout();
        leftCol.setSizeFull();
        CmsInternalResources resources = new CmsInternalResources(table);
        leftCol.addComponent(resources);
        Button externalButton = CmsDefaultAppButtonProvider.createIconButton(
            CmsVaadinUtils.getMessageText(Messages.GUI_LINKVALIDATION_EXTERNALLINK_ADMIN_TOOL_NAME_0),
            CmsVaadinUtils.getMessageText(Messages.GUI_LINKVALIDATION_EXTERNALLINK_ADMIN_TOOL_NAME_HELP_0),
            new ExternalResource(OpenCmsTheme.getImageLink(ICON_EXTERNAL_VALIDATION)));
        externalButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 8188918603077938024L;

            public void buttonClick(ClickEvent event) {

                openSubView(PATH_EXTERNAL_VALIDATION, true);
            }
        });
        leftCol.addComponent(externalButton);
        leftCol.setExpandRatio(resources, 1);
        leftCol.setComponentAlignment(externalButton, Alignment.TOP_RIGHT);
        sp.setFirstComponent(leftCol);
        sp.setSecondComponent(table);
        sp.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);
        return sp;
    }
}

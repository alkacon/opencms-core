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

package org.opencms.ui.apps.dbmanager;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsAttributeAwareApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * Class for the database synchronization app.<p>
 */
public class CmsDbSynchronizationApp extends A_CmsAttributeAwareApp {

    /**vaadin component.*/
    private Button m_refresh;

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_SYNC_NAME_0));
            return crumbs;
        }
        return new LinkedHashMap<String, String>();
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (m_refresh == null) {
            m_refresh = CmsToolBar.createButton(
                FontAwesome.REFRESH,
                CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_SYNCH_RUN_0));
            m_refresh.addClickListener(new Button.ClickListener() {

                private static final long serialVersionUID = 4980773759687185944L;

                public void buttonClick(ClickEvent event) {

                    final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
                    window.setContent(new CmsDbSynchDialog(new Runnable() {

                        public void run() {

                            window.close();
                        }
                    }));
                    window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_SYNCH_RUN_0));
                    A_CmsUI.get().addWindow(window);

                }
            });
            m_uiContext.addToolbarButton(m_refresh);
        }
        CmsDbSynchronizationView view = new CmsDbSynchronizationView(this);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            return view;
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
     * Sets the visivility of the refresh button.<p>
     *
     * @param visible true -> button is visible
     */
    protected void setRefreshButton(boolean visible) {

        m_refresh.setVisible(visible);
    }
}

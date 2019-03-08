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

package org.opencms.ui.apps.modules;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Module import dialog with two tabs, one for importing via HTTP and one for importing from the server.<p>
 */
public class CmsImportTabForm extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The button bar. */
    private HorizontalLayout m_buttons;

    /** The tab panel. */
    private TabSheet m_tabs;

    /**layout holding tabs. */
    private VerticalLayout m_start;

    /**layout for the import report. */
    private VerticalLayout m_report;

    /**
     * Creates a new instance.<p>
     *
     * @param app the module manager app instance
     */
    public CmsImportTabForm(CmsModuleApp app, Runnable run) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_tabs.addTab(
            new CmsModuleImportForm(app, m_start, m_report, run),
            CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_MODULES_TAB_IMPORT_HTTP_0));
        m_tabs.addTab(
            new CmsServerModuleImportForm(app, m_start, m_report, run),
            CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_MODULES_TAB_IMPORT_SERVER_0));
        updateButtons();
        m_tabs.addSelectedTabChangeListener(new SelectedTabChangeListener() {

            private static final long serialVersionUID = 1L;

            public void selectedTabChange(SelectedTabChangeEvent event) {

                updateButtons();
            }
        });
    }

    /**
     * Updates the button bar with the buttons provided by the currently selected tab.<p>
     */
    protected void updateButtons() {

        List<Button> buttons = ((A_CmsModuleImportForm)m_tabs.getSelectedTab()).getButtons();
        m_buttons.removeAllComponents();
        for (Button button : buttons) {
            m_buttons.addComponent(button);
        }
    }

}

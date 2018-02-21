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
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsToolBar;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class for the property definition app.<p>
 */
public class CmsDbPropertiesApp extends A_CmsWorkplaceApp {

    /**Table for properties. */
    protected CmsPropertyTable m_table;

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_PROPERTY_TOOL_NAME_0));
        return crumbs;

    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        m_rootLayout.setMainHeightFull(true);

        m_table = new CmsPropertyTable();

        TextField filter = new TextField();
        filter.setIcon(FontOpenCms.FILTER);
        filter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        filter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        filter.setWidth("200px");
        filter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                m_table.filterTable(event.getText());
            }
        });
        m_infoLayout.addComponent(filter);
        m_table.setSizeFull();
        addNewPropertyButton(m_table);
        return m_table;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Button for adding new property.<p>
     * @param table table to be updated
     */
    private void addNewPropertyButton(final CmsPropertyTable table) {

        Button add = CmsToolBar.createButton(
            FontOpenCms.WAND,
            CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_PROPERTY_NEW_CAPTION_0));
        add.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                final Window window = CmsBasicDialog.prepareWindow();
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_PROPERTY_NEW_CAPTION_0));
                window.setContent(new CmsAddPropertyDefinitionDialog(window, table));
                A_CmsUI.get().addWindow(window);
            }
        });
        m_uiContext.addToolbarButton(add);

    }
}

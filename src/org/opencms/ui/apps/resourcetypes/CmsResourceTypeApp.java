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

package org.opencms.ui.apps.resourcetypes;

import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.TextField;

/**
 * App to manage the resource types.<p>
 */
public class CmsResourceTypeApp extends A_CmsWorkplaceApp {

    /** Filter for the table.*/
    private TextField m_resourcetypeTableFilter;

    /** Table holding available resource types.*/
    private CmsResourceTypesTable m_table;

    /** Is the given resource type id free?
     * @param id to be checked
     * @return boolean
     */
    public static boolean isResourceTypeIdFree(int id) {

        try {
            OpenCms.getResourceManager().getResourceType(id);
        } catch (CmsLoaderException e) {
            return true;
        }
        return false;
    }

    /**
     * Is resource type name free.<p>
     *
     * @param name to be checked
     * @return boolean
     */
    public static boolean isResourceTypeNameFree(String name) {

        try {
            OpenCms.getResourceManager().getResourceType(name);
        } catch (CmsLoaderException e) {
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        context.addPublishButton(changes -> {});
        super.initUI(context);
    }

    /**
     * Reloads the table.<p>
     */
    public void reload() {

        m_table.init();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_resourcetypeTableFilter.getValue())) {
            m_table.filterTable(m_resourcetypeTableFilter.getValue());
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        addToolbarButtons();

        m_table = new CmsResourceTypesTable(this);
        m_resourcetypeTableFilter = new TextField();
        m_resourcetypeTableFilter.setIcon(FontOpenCms.FILTER);
        m_resourcetypeTableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_resourcetypeTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_resourcetypeTableFilter.setWidth("200px");
        m_resourcetypeTableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                m_table.filterTable(event.getText());
            }
        });
        m_infoLayout.addComponent(m_resourcetypeTableFilter);
        m_rootLayout.setMainHeightFull(true);
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
     * Adds the buttons to the toolbar.<p>
     */
    private void addToolbarButtons() {

        Button add = CmsToolBar.createButton(
            FontOpenCms.WAND,
            CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_CREATE_NEW_TYPE_0));
        add.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
                CmsNewResourceTypeDialog dialog = new CmsNewResourceTypeDialog(window, CmsResourceTypeApp.this);
                CmsMoveResourceTypeDialog moduleDialog = new CmsMoveResourceTypeDialog(dialog);
                window.setContent(moduleDialog);
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_CREATE_NEW_TYPE_0));
                A_CmsUI.get().addWindow(window);
            }
        });
        m_uiContext.addToolbarButton(add);

    }
}

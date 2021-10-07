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

import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsAttributeAwareApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsInfoButton;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.util.table.CmsBeanTableBuilder;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;

/**
 * Overview list for module information.<p>
 *
 * @param <T> the row type
 */
public class CmsModuleTable<T> extends Table {

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The module manager app instance. */
    protected CmsModuleApp m_app;

    /** The table builder. */
    protected CmsBeanTableBuilder<T> m_tableBuilder;

    /** The row counter label. */
    private CmsInfoButton m_counter;

    /** The context menu. */
    protected CmsContextMenu m_menu = new CmsContextMenu();

    /** The search box. */
    private TextField m_searchBox = new TextField();

    /**
     * Creates a new instance.<p>
     *
     * @param app the module manager app instance.<p>
     * @param rowType the row type
     * @param rows the module rows
     */
    public CmsModuleTable(CmsModuleApp app, Class<T> rowType, List<T> rows) {

        m_menu.setAsTableContextMenu(this);
        m_app = app;
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void itemClick(ItemClickEvent event) {

                CmsModuleTable.this.onItemClick(event);
            }
        });
        m_searchBox.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void textChange(TextChangeEvent event) {

                String filterString = event.getText();

                Container.Filterable container = (Container.Filterable)getContainerDataSource();
                container.removeAllContainerFilters();
                container.addContainerFilter(m_tableBuilder.getDefaultFilter(filterString));
                if ((getValue() != null)) {
                    setCurrentPageFirstItemId(getValue());
                }
            }
        });
        m_searchBox.setIcon(FontOpenCms.FILTER);
        m_searchBox.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_searchBox.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_searchBox.setWidth("200px");

        Map<String, Object> attributes = Maps.newHashMap();

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        hl.addComponent(m_searchBox);
        // hl.setComponentAlignment(m_counter, Alignment.MIDDLE_LEFT);
        attributes.put(A_CmsAttributeAwareApp.ATTR_INFO_COMPONENT, hl);
        attributes.put(A_CmsAttributeAwareApp.ATTR_MAIN_HEIGHT_FULL, Boolean.TRUE);
        List<Component> buttons = Lists.newArrayList();
        Button addModule = CmsToolBar.createButton(
            FontOpenCms.WAND,
            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_BUTTON_NEW_MODULE_0));
        addModule.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                m_app.editNewModule(CmsModuleTable.this::reload);
            }
        });
        buttons.add(addModule);

        Button importButton = CmsToolBar.createButton(
            CmsModuleApp.Icons.IMPORT,
            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_BUTTON_IMPORT_0));
        importButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                importModule();
            }
        });
        buttons.add(importButton);
        m_counter = new CmsInfoButton();
        m_counter.setWindowCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_STATISTICS_0));
        m_counter.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_STATISTICS_0));
        buttons.add(m_counter);
        attributes.put(CmsModuleApp.Attributes.BUTTONS, buttons);
        setData(attributes);
        CmsBeanTableBuilder<T> builder = CmsBeanTableBuilder.newInstance(rowType);
        m_tableBuilder = builder;
        builder.buildTable(this, rows);
        setCellStyleGenerator(builder.getDefaultCellStyleGenerator());
        setItemIconPropertyId("icon");
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setSelectable(true);
        setMultiSelect(false);
        sort(new Object[] {"name"}, new boolean[] {true});
        updateCounter();
    }

    /**
     * Opens the import module dialog.<p>
     */
    public void importModule() {

        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsImportTabForm form = new CmsImportTabForm(m_app, this::reload);
        window.setContent(form);
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_IMPORT_TITLE_0));
        A_CmsUI.get().addWindow(window);
        window.center();
    }

    /**
     * Reloads the table data.<p>
     */
    public void reload() {

        List<CmsModule> modules = OpenCms.getModuleManager().getAllInstalledModules();
        @SuppressWarnings("unchecked")
        BeanItemContainer<CmsModuleRow> container = (BeanItemContainer<CmsModuleRow>)getContainerDataSource();
        container.removeAllItems();
        List<CmsModuleRow> newRows = Lists.newArrayList();
        for (CmsModule module : modules) {
            CmsModuleRow row = new CmsModuleRow(module);
            newRows.add(row);
        }
        container.addAll(newRows);
        sort();
        updateCounter();
    }

    /**
     * Handles the table item clicks.<p>
     *
     * @param event the click event
     */
    protected void onItemClick(ItemClickEvent event) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {

            Set<String> nameSet = new LinkedHashSet<String>();

            CmsModuleRow moduleRow = (CmsModuleRow)(event.getItemId());
            select(moduleRow);
            nameSet.add(moduleRow.getModule().getName());
            if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                select(moduleRow);
                m_menu.setEntries(m_app.getMenuEntries(), nameSet);
                m_menu.openForTable(event, this);
            } else if (event.getButton().equals(MouseButton.LEFT) && "name".equals(event.getPropertyId())) {

                m_app.openModuleInfo(nameSet);
            }

        }
    }

    /**
     * Updates the row counter.<p>
     */
    private void updateCounter() {

        m_counter.replaceData(
            Collections.singletonMap(
                CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_STATISTICS_ROW_COUNT_0),
                String.valueOf(getContainerDataSource().size())));
    }

}

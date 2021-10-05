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

import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.modules.CmsModuleRow;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.util.table.CmsBeanTableBuilder;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.RowHeaderMode;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for a dialog to move resource types to modules.<p>
 */
public class CmsMoveResourceTypeDialog extends CmsBasicDialog {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMoveResourceTypeDialog.class);

    /** vaadin component.*/
    private Button m_ok;

    /** vaadin component.*/
    private Button m_cancel;

    /** vaadin component.*/
    private Table m_table;

    /** vaadin component.*/
    private CheckBox m_moveAnyway;

    /** Vaadin vomponent.*/
    private Label m_warningIcon;

    /** vaadin component.*/
    private TextField m_filter;

    /** Is schema ok.*/
    private boolean m_schemaOK = true;

    /**Vaadin component. */
    private VerticalLayout m_missingSchemaLayout;

    /**resource type.*/
    private I_CmsResourceType m_type;

    /** type content.*/
    private CmsResourceTypeXmlContent m_typeXML;

    /**
     * Public constructor.<p>
     *
     * @param dialog dialog
     */
    public CmsMoveResourceTypeDialog(CmsNewResourceTypeDialog dialog) {

        init(null);
        m_missingSchemaLayout.setVisible(false);
        m_ok.addClickListener(e -> {
            if (getModuleName() != null) {
                dialog.setModule(getModuleName(), CmsMoveResourceTypeDialog.this);
            }
        });
        m_cancel.addClickListener(e -> CmsVaadinUtils.getWindow(CmsMoveResourceTypeDialog.this).close());
    }

    /**
     * public constructor.<p>
     *
     * @param window window
     * @param type resourcetype
     */
    public CmsMoveResourceTypeDialog(final Window window, I_CmsResourceType type) {

        init(window);

        m_type = type;

        m_table.select(new CmsModuleRow(OpenCms.getModuleManager().getModule(type.getModuleName())));
        m_table.setCurrentPageFirstItemId(new CmsModuleRow(OpenCms.getModuleManager().getModule(type.getModuleName())));

        if (m_type instanceof CmsResourceTypeXmlContent) {
            m_typeXML = (CmsResourceTypeXmlContent)m_type;
            if (!OpenCms.getModuleManager().getModule(m_type.getModuleName()).getResources().contains(
                m_typeXML.getSchema())) {
                m_schemaOK = false;
                m_ok.setEnabled(false);
                m_moveAnyway.addValueChangeListener(new ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {

                        setOkButton();

                    }

                });
            }

        }

        CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
        displayResourceInfoDirectly(
            Collections.singletonList(
                new CmsResourceInfo(
                    CmsVaadinUtils.getMessageText(typeSetting.getKey()),
                    type.getModuleName(),
                    CmsResourceUtil.getBigIconResource(typeSetting, null))));
        m_ok.addClickListener(e -> updateResourceType(window));

    }

    /**
     * Filters the table.<p>
     *
     * @param text to filter
     */
    protected void filterTable(String text) {

        Container.Filterable container = (Container.Filterable)m_table.getContainerDataSource();
        container.removeAllContainerFilters();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(text)) {
            container.addContainerFilter(
                new Or(
                    new SimpleStringFilter("name", text, true, false),
                    new SimpleStringFilter("title", text, true, false)));
        }
    }

    /**
     * Get the module name.<p>
     *
     * @return module name
     */
    protected String getModuleName() {

        if (m_table.getValue() == null) {
            return null;
        }
        return ((CmsModuleRow)m_table.getValue()).getName();
    }

    /**
     * Set ok button.<p>
     */
    protected void setOkButton() {

        m_ok.setEnabled(m_moveAnyway.getValue().booleanValue());
    }

    /**
     * Update the resource type.<p>
     *
     * @param window
     */
    protected void updateResourceType(Window window) {

        if (!((CmsModuleRow)m_table.getValue()).equals(
            new CmsModuleRow(OpenCms.getModuleManager().getModule(m_type.getModuleName())))) {
            CmsModule newModule = ((CmsModuleRow)m_table.getValue()).getModule().clone();
            CmsModule oldModule = OpenCms.getModuleManager().getModule(m_type.getModuleName()).clone();

            m_type.setModuleName(newModule.getName());

            List<I_CmsResourceType> newTypes = Lists.newArrayList(newModule.getResourceTypes());
            newTypes.add(m_type);
            newModule.setResourceTypes(newTypes);
            List<CmsExplorerTypeSettings> oldSettings = new ArrayList<CmsExplorerTypeSettings>(
                oldModule.getExplorerTypes());
            CmsExplorerTypeSettings settings = new CmsExplorerTypeSettings();

            settings.setName(m_type.getTypeName());
            settings = oldSettings.get(oldSettings.indexOf(settings));
            oldSettings.remove(settings);
            List<CmsExplorerTypeSettings> newSettings = new ArrayList<CmsExplorerTypeSettings>(
                newModule.getExplorerTypes());
            newSettings.add(settings);
            oldModule.setExplorerTypes(oldSettings);
            newModule.setExplorerTypes(newSettings);

            List<I_CmsResourceType> oldTypes = Lists.newArrayList(oldModule.getResourceTypes());
            oldTypes.remove(m_type);
            oldModule.setResourceTypes(oldTypes);
            if (m_schemaOK) {
                List<String> oldResources = Lists.newArrayList(oldModule.getResources());
                oldResources.remove(m_typeXML.getSchema());
                oldModule.setResources(oldResources);

                List<String> newResources = Lists.newArrayList(newModule.getResources());
                newResources.add(m_typeXML.getSchema());
                newModule.setResources(newResources);

            }
            try {
                OpenCms.getModuleManager().updateModule(A_CmsUI.getCmsObject(), oldModule);
                OpenCms.getModuleManager().updateModule(A_CmsUI.getCmsObject(), newModule);
                OpenCms.getResourceManager().initialize(A_CmsUI.getCmsObject());
                OpenCms.getWorkplaceManager().removeExplorerTypeSettings(oldModule);
                OpenCms.getWorkplaceManager().addExplorerTypeSettings(newModule);
                OpenCms.getWorkplaceManager().initialize(A_CmsUI.getCmsObject());

            } catch (CmsException e) {
                LOG.error("Unable to move resource type", e);
            }

        }
        window.close();
        A_CmsUI.get().reload();
    }

    /**
     * Init the dialog.<p>
     *
     * @param window window
     */
    private void init(final Window window) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_warningIcon.setContentMode(ContentMode.HTML);
        m_warningIcon.setValue(FontOpenCms.WARNING.getHtml());
        if (window != null) {
            m_cancel.addClickListener(e -> window.close());
        }
        m_table.setWidth("100%");
        m_table.setHeight("100%");

        List<CmsModuleRow> rows = new ArrayList<CmsModuleRow>();
        for (CmsModule module : OpenCms.getModuleManager().getAllInstalledModules()) {
            CmsModuleRow row = new CmsModuleRow(module);
            rows.add(row);
        }
        CmsBeanTableBuilder<CmsModuleRow> builder = CmsBeanTableBuilder.newInstance(CmsModuleRow.class);
        builder.buildTable(m_table, rows);
        m_table.setCellStyleGenerator(builder.getDefaultCellStyleGenerator());
        m_table.setItemIconPropertyId("icon");
        m_table.setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        m_table.setSelectable(true);
        m_table.setVisibleColumns("name", "title");
        m_table.setSortContainerPropertyId("name");
        m_table.sort();

        m_filter.setIcon(FontOpenCms.FILTER);
        m_filter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_filter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_filter.addTextChangeListener(new TextChangeListener() {

            public void textChange(TextChangeEvent event) {

                filterTable(event.getText());

            }
        });
    }
}

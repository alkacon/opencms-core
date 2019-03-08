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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.components.CmsAutoItemCreatingComboBox;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.ui.Button;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * The form for importing a module from the application server.<p>
 */
public class CmsServerModuleImportForm extends A_CmsModuleImportForm {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsServerModuleImportForm.class);

    /** The Cancel button. */
    private Button m_cancel;

    /** The select box used to select the module. */
    private ComboBox m_moduleSelect;

    /** The OK button. */
    private Button m_ok;

    /** The site selector. */
    private CmsAutoItemCreatingComboBox m_siteSelect;

    /**
     * Creates a new instance.<p>
     *
     * @param app the module manager app
     */
    public CmsServerModuleImportForm(CmsModuleApp app, VerticalLayout start, VerticalLayout report, Runnable run) {

        super(app, start, report, run);
        IndexedContainer options = new IndexedContainer();
        options.addContainerProperty("label", String.class, "");
        m_moduleSelect.setContainerDataSource(options);
        m_moduleSelect.setItemCaptionMode(ItemCaptionMode.PROPERTY);
        m_moduleSelect.setItemCaptionPropertyId("label");
        m_moduleSelect.setNullSelectionAllowed(false);
        String moduleDir = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/modules");
        File moduleDirFile = new File(moduleDir);
        if (moduleDirFile.exists()) {
            List<File> files = Lists.newArrayList(moduleDirFile.listFiles());
            Collections.sort(files);
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }
                String path = file.getAbsolutePath();
                String name = file.getName();
                options.addItem(path).getItemProperty("label").setValue(name);
            }
        }
        m_moduleSelect.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                String path = (String)(event.getProperty().getValue());
                m_importFile = new CmsModuleImportFile(path);
                m_ok.setEnabled(false);
                validateModuleFile();
            }
        });
    }

    @Override
    protected Button getCancelButton() {

        return m_cancel;
    }

    @Override
    protected Button getOkButton() {

        return m_ok;
    }

    @Override
    protected CmsAutoItemCreatingComboBox getSiteSelector() {

        return m_siteSelect;
    }

}

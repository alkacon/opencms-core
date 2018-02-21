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

import org.opencms.main.OpenCms;

import java.io.File;

import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.v7.ui.ComboBox;

/**
 * Abstract class for the import from a folder on the server.<p>
 */
public abstract class A_CmsServerImportForm extends A_CmsImportForm {

    /**Vaadin serial id.*/
    private static final long serialVersionUID = 5493880295543227220L;

    /**
     * public constructor.<p>
     *
     * @param app which uses this form
     * @param pathToServer path where the files should be read
     * @param validate indicates if file gets validated (only possible for modules)
     */
    public A_CmsServerImportForm(I_CmsReportApp app, String pathToServer, final boolean validate) {
        super(app);
        IndexedContainer options = new IndexedContainer();
        options.addContainerProperty("label", String.class, "");
        getImportSelect().setContainerDataSource(options);
        getImportSelect().setItemCaptionMode(ItemCaptionMode.PROPERTY);
        getImportSelect().setItemCaptionPropertyId("label");
        getImportSelect().setNullSelectionAllowed(false);
        String moduleDir = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(pathToServer);
        File moduleDirFile = new File(moduleDir);
        if (moduleDirFile.exists()) {
            for (File file : moduleDirFile.listFiles()) {
                String path = file.getAbsolutePath();
                String name = file.getName();
                options.addItem(path).getItemProperty("label").setValue(name);
            }
        }

        getImportSelect().addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -8550460711407604364L;

            public void valueChange(ValueChangeEvent event) {

                String path = (String)(event.getProperty().getValue());
                m_importFile = new CmsImportFile(path);
                if (validate) {
                    getOkButton().setEnabled(false);
                    validateModuleFile();
                    return;
                }
                getOkButton().setEnabled(true);
            }
        });
    }

    /**
     * Gets a combo box for selecting the file on server.<p>
     *
     * @return a vaadin combo box
     */
    public abstract ComboBox getImportSelect();
}

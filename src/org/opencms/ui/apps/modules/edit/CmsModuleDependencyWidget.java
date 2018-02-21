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

package org.opencms.ui.apps.modules.edit;

import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsAutoItemCreatingComboBox;
import org.opencms.ui.components.CmsAutoItemCreatingComboBox.I_NewValueHandler;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.TextField;

/**
 * Widget used to edit a module dependency.<p>
 */
public class CmsModuleDependencyWidget extends FormLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The module selector. */
    private CmsAutoItemCreatingComboBox m_moduleSelect;

    /** Text field for the module version. */
    private TextField m_version;

    /**
     * Creates a new instance.<p>
     */
    public CmsModuleDependencyWidget() {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        IndexedContainer container = new IndexedContainer();
        List<String> moduleNames = Lists.newArrayList();
        for (CmsModule module : OpenCms.getModuleManager().getAllInstalledModules()) {
            String name = module.getName();
            moduleNames.add(name);
        }
        Collections.sort(moduleNames);
        for (String name : moduleNames) {
            container.addItem(name);
        }
        m_moduleSelect.setContainerDataSource(container);
        m_moduleSelect.setNewValueHandler(new I_NewValueHandler() {

            public Object ensureItem(Container cnt, Object id) {

                if (!cnt.containsId(id)) {
                    cnt.addItem(id);
                }
                return id;
            }
        });
        setWidth("100%");
    }

    /**
     * Creates a new widget instance for the given module dependency.<p>
     *
     * @param dep the module dependency
     * @return the new widget
     */
    public static CmsModuleDependencyWidget create(CmsModuleDependency dep) {

        CmsModuleDependencyWidget result = new CmsModuleDependencyWidget();
        if (dep != null) {
            result.m_moduleSelect.setValue(dep.getName());
            result.m_version.setValue(dep.getVersion().toString());
        }
        return result;
    }

    /**
     * Gets the module name.<p>
     *
     * @return the module name
     */
    public String getModuleName() {

        return (String)m_moduleSelect.getValue();
    }

    /**
     * Gets the module version.<p>
     *
     * @return the module version
     */
    public String getModuleVersion() {

        return m_version.getValue();
    }

}

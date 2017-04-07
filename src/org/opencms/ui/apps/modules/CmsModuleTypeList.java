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

import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Widget to display the list of resource / explorer types defined in a module.<p>
 */
public class CmsModuleTypeList extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The list for the explorer types. */
    private VerticalLayout m_explorerTypes;

    /** The OK button. */
    private Button m_ok;

    /** The list for the resource types. */
    private VerticalLayout m_resourceTypes;

    /**
     * Creates a new instance.<p>
     *
     * @param moduleName the module name
     */
    public CmsModuleTypeList(String moduleName) {
        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                CmsVaadinUtils.getWindow(CmsModuleTypeList.this).close();
            }
        });
        initialize(module);
    }

    /**
     * Fills the widget content.<p>
     *
     * @param module the module
     */
    public void initialize(CmsModule module) {

        boolean empty = true;
        for (I_CmsResourceType type : module.getResourceTypes()) {
            @SuppressWarnings("deprecation")
            String text = type.getTypeName() + " (ID: " + type.getTypeId() + ")";
            m_resourceTypes.addComponent(new Label(text));
            empty = false;
        }
        if (empty) {
            m_resourceTypes.addComponent(
                new Label(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_NO_RESOURCE_TYPES_0)));
        }
        empty = true;
        for (CmsExplorerTypeSettings expType : module.getExplorerTypes()) {
            String text = expType.getName();
            if (expType.getReference() != null) {
                text += " (" + expType.getReference() + ")";
            }
            m_explorerTypes.addComponent(new Label(text));
            empty = false;
        }
        if (empty) {
            m_explorerTypes.addComponent(
                new Label(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_NO_EXPLORER_TYPES_0)));
        }
    }

}

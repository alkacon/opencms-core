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
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.CmsRichTextArea;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Widget to display the list of resource / explorer types defined in a module.<p>
 */
public class CmsModuleInfoDialog extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The label displaying the module description. */
    private Label m_description;

    /** The Edit button. */
    private Button m_edit;

    /** The list for the explorer types. */
    private VerticalLayout m_explorerTypes;

    /** The panel containing the explorer types. */
    private Panel m_explorerTypesPanel;

    /** The OK button. */
    private Button m_ok;

    /** The list for the resource types. */
    private VerticalLayout m_resourceTypes;

    /**
     * Creates a new instance.<p>
     *
     * @param moduleName the module name
     */
    public CmsModuleInfoDialog(String moduleName, Consumer<String> editAction) {

        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsResourceInfo resInfo = new CmsResourceInfo(
            module.getName(),
            module.getNiceName(),
            CmsModuleApp.Icons.RESINFO_ICON);
        displayResourceInfoDirectly(Arrays.asList(resInfo));
        m_description.setContentMode(ContentMode.HTML);
        m_description.setValue(CmsRichTextArea.cleanHtml(module.getDescription(), true));
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                CmsVaadinUtils.getWindow(CmsModuleInfoDialog.this).close();
            }
        });
        m_edit.addClickListener(event -> {
            CmsVaadinUtils.getWindow(CmsModuleInfoDialog.this).close();
            editAction.accept(moduleName);

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
        Set<String> resTypeNames = Sets.newHashSet();
        for (I_CmsResourceType type : module.getResourceTypes()) {
            m_resourceTypes.addComponent(formatResourceType(type));
            resTypeNames.add(type.getTypeName());
            empty = false;
        }
        if (empty) {
            m_resourceTypes.addComponent(
                new Label(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_NO_RESOURCE_TYPES_0)));
        }
        empty = true;
        for (CmsExplorerTypeSettings expType : module.getExplorerTypes()) {
            if (resTypeNames.contains(expType.getName())) {
                continue;
            }
            m_explorerTypes.addComponent(formatExplorerType(expType));
            empty = false;
        }

        if (empty) {
            m_explorerTypesPanel.setVisible(false);
        }
    }

    /**
     * Creates the resource info box for an explorer type.<p>
     *
     * @param explorerType the explorer type
     * @return the resource info box
     */
    CmsResourceInfo formatExplorerType(CmsExplorerTypeSettings explorerType) {

        Resource icon = CmsResourceUtil.getBigIconResource(explorerType, null);
        String title = CmsVaadinUtils.getMessageText(explorerType.getKey());
        if (title.startsWith("???")) {
            title = explorerType.getName();
        }
        String subtitle = explorerType.getName();
        if (explorerType.getReference() != null) {
            subtitle += " (" + explorerType.getReference() + ")";
        }
        CmsResourceInfo info = new CmsResourceInfo(title, subtitle, icon);
        return info;
    }

    /**
     * Creates the resource info box for a resource type.<p>
     *
     * @param type the resource type
     * @return the resource info box
     */
    @SuppressWarnings("deprecation")
    CmsResourceInfo formatResourceType(I_CmsResourceType type) {

        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
        Resource icon;
        String title;
        String subtitle;
        if (settings != null) {
            icon = CmsResourceUtil.getBigIconResource(settings, null);
            title = CmsVaadinUtils.getMessageText(settings.getKey());
            if (title.startsWith("???")) {
                title = type.getTypeName();
            }
            subtitle = type.getTypeName()
                + " (ID: "
                + type.getTypeId()
                + (settings.getReference() != null ? (", " + settings.getReference()) : "")
                + ")";
        } else {
            icon = CmsResourceUtil.getBigIconResource(
                OpenCms.getWorkplaceManager().getExplorerTypeSetting("unknown"),
                null);
            title = type.getTypeName();
            subtitle = type.getTypeName() + " (ID: " + type.getTypeId() + ")";
        }
        CmsResourceInfo info = new CmsResourceInfo(title, subtitle, icon);
        return info;
    }
}

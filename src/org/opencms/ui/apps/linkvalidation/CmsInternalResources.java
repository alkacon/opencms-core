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

package org.opencms.ui.apps.linkvalidation;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.editablegroup.CmsEditableGroup;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;
import org.opencms.ui.components.fileselect.CmsPathSelectField;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the layout for choosing the resources.<p>
 */
public class CmsInternalResources extends VerticalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 6880701403593873461L;

    /**Editable resource group. */
    CmsEditableGroup m_resourcesGroup;

    /**Button to update table.*/
    Button m_okButton;

    /**Layout holding the text components for resources. */
    VerticalLayout m_resources;

    /**
     * Public constructor.<p>
     *
     * @param table linked table to be updated if button was pressed
     */
    public CmsInternalResources(final I_CmsUpdatableComponent table) {

        setHeightUndefined();
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_okButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -5668840121832993312L;

            public void buttonClick(ClickEvent event) {

                table.update(getResources());
            }
        });

        m_resourcesGroup = new CmsEditableGroup(m_resources, new Supplier<Component>() {

            public Component get() {

                return getResourceComponent(null);
            }

        }, CmsVaadinUtils.getMessageText(Messages.GUI_LINKVALIDATION_LINKS_ADDRESOURCES_0));

        m_resourcesGroup.init();

        m_resourcesGroup.addRow(getResourceComponent(null));

    }

    /**
     * Adds a resource to the form.<p>
     *
     * @param resource to be added
     */
    public void addResource(String resource) {

        m_resourcesGroup.addRow(getResourceComponent(resource));

    }

    /**
     * Clear resources.<p>
     *
     */
    public void clearResources() {

        for (I_CmsEditableGroupRow row : m_resourcesGroup.getRows()) {
            m_resourcesGroup.remove(row);
        }
    }

    /**
     * Reads out resource paths from Layout.<p>
     *
     * @return List of Strings with all entered paths
     */
    public List<String> getResources() {

        List<String> res = new ArrayList<String>();
        for (I_CmsEditableGroupRow row : m_resourcesGroup.getRows()) {
            res.add(((CmsPathSelectField)row.getComponent()).getValue());
        }
        return res;
    }

    /**
     * Get vaadin component with given path.<p>
     *
     * @param path of resource
     * @return Vaadin component
     */
    protected Component getResourceComponent(String path) {

        try {
            CmsPathSelectField field = new CmsPathSelectField();
            field.setUseRootPaths(true);
            CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            cms.getRequestContext().setSiteRoot("");
            field.setCmsObject(cms);
            if (path != null) {
                field.setValue(path);
            }
            return field;
        } catch (CmsException e) {
            //
        }
        return null;
    }

    /**
     * Adds an empty path field to layout.<p>
     *
     * @param defaultValue of new field
     */
    void addEmptyPathFieldToLayout(String defaultValue) {

        m_resourcesGroup.addRow(getResourceComponent(defaultValue));
    }
}

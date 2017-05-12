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

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsRemovableFormRow;
import org.opencms.ui.components.fileselect.CmsPathSelectField;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * Class for the layout for choosing the resources.<p>
 */
public class CmsInternalResources extends VerticalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 6880701403593873461L;

    /**Button to add new field.*/
    Button m_addResource;

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
        addEmptyPathFieldToLayout("/");

        m_addResource.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -1837905731879184454L;

            public void buttonClick(ClickEvent event) {

                addEmptyPathFieldToLayout("");
            }
        });

        m_okButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -5668840121832993312L;

            public void buttonClick(ClickEvent event) {

                table.update(getResources());
            }
        });

    }

    /**
     * Adds an empty path field to layout.<p>
     *
     * @param defaultValue of new field
     */
    void addEmptyPathFieldToLayout(String defaultValue) {

        CmsPathSelectField field = new CmsPathSelectField();
        field.setValue(defaultValue);
        CmsRemovableFormRow<CmsPathSelectField> pathField = new CmsRemovableFormRow<CmsPathSelectField>(field, "");
        pathField.setWidth("100%");
        m_resources.addComponent(pathField);
    }

    /**
     * Reads out resource paths from Layout.<p>
     *
     * @return List of Strings with all entered paths
     */
    List<String> getResources() {

        List<String> res = new ArrayList<String>();
        Iterator<Component> resourceIterator = m_resources.iterator();

        while (resourceIterator.hasNext()) {
            String fieldText = ((CmsPathSelectField)((CmsRemovableFormRow<CmsPathSelectField>)resourceIterator.next()).getComponent(
                0)).getValue();
            if (!fieldText.isEmpty() & !res.contains(fieldText)) {
                res.add(fieldText);
            }
        }
        return res;
    }
}

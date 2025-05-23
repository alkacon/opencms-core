/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.components;

import org.opencms.ui.FontOpenCms;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HasComponents;
import com.vaadin.v7.ui.HorizontalLayout;

/**
 * Removable form row.<p>
 *
 * @param <T> the filed type
 */
public class CmsRemovableFormRow<T extends Component> extends HorizontalLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Style name for the remove button. */
    public static final String REMOVE_BUTTON_STYLE = "o-formrow-remove-button";

    /** The text input field. */
    private T m_input;

    private Runnable m_remove;

    /**
     * Constructor.<p>
     *
     * @param input the input field
     * @param removeLabel the remove button label
     */
    public CmsRemovableFormRow(T input, String removeLabel) {
        setWidth("100%");
        m_input = input;
        setSpacing(true);
        input.setWidth("100%");
        addComponent(input);
        setExpandRatio(input, 1f);
        Button deleteButton = new Button("");
        deleteButton.setIcon(FontOpenCms.CUT_SMALL);
        deleteButton.addStyleName(REMOVE_BUTTON_STYLE);
        deleteButton.addStyleName(OpenCmsTheme.BUTTON_ICON);
        deleteButton.setDescription(removeLabel);
        deleteButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                removeRow();
            }
        });
        addComponent(deleteButton);
    }

    /**
     * Returns the input field.<p>
     *
     * @return the input field
     */
    public T getInput() {

        return m_input;
    }

    /**
     * Enables or diables the remove button.<p>
     *
     * @param enabled true -> remove is clickable
     */
    public void setEnabledRemoveOption(boolean enabled) {

        getComponent(1).setEnabled(enabled);
    }

    /**
     * Sets a runnable, which runs when row gets removed.<p>
     *
     * @param remove runnable
     */
    public void setRemoveRunnable(Runnable remove) {

        m_remove = remove;
    }

    /**
     * Method to remove row.<p>
     */
    void removeRow() {

        HasComponents parent = CmsRemovableFormRow.this.getParent();
        if (parent instanceof ComponentContainer) {
            ((ComponentContainer)parent).removeComponent(CmsRemovableFormRow.this);
        }
        if (m_remove != null) {
            m_remove.run();
        }
    }

}

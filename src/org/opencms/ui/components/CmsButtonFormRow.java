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

package org.opencms.ui.components;

import com.vaadin.server.FontIcon;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.HorizontalLayout;

/**
 * Form row with button.<p>
 * @param <T> T of Field
 */
public class CmsButtonFormRow<T extends Component> extends HorizontalLayout {

    /**Vaadin serial id. */
    private static final long serialVersionUID = 7691914937148837396L;

    /** The text input field. */
    private T m_input;

    /**
     * Public constructor.<p>
     *
     * @param input field
     * @param buttonIcon icon
     * @param buttonRun runnable
     * @param buttonDescription description
     */
    public CmsButtonFormRow(T input, FontIcon buttonIcon, final Runnable buttonRun, String buttonDescription) {

        setWidth("100%");
        m_input = input;
        setSpacing(true);
        input.setWidth("100%");
        addComponent(input);
        setExpandRatio(input, 1f);
        Button deleteButton = new Button("");
        deleteButton.setIcon(buttonIcon);
        deleteButton.addStyleName(CmsRemovableFormRow.REMOVE_BUTTON_STYLE);
        deleteButton.addStyleName(OpenCmsTheme.BUTTON_ICON);
        deleteButton.setDescription(buttonDescription);
        deleteButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                buttonRun.run();
            }
        });
        addComponent(deleteButton);
    }

    /**
     * Gets the input field.<p>
     *
     * @return input field
     */
    public T getInput() {

        return m_input;
    }

}

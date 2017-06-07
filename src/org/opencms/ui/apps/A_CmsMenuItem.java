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

package org.opencms.ui.apps;

import org.opencms.main.OpenCms;

import java.util.Locale;

import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

/**
 * A workplace menu item.<p>
 */
public abstract class A_CmsMenuItem implements I_CmsMenuItem {

    /** The item icon. */
    protected Resource m_icon;

    /** The label message key. */
    protected String m_labelKey;

    /**
     * Constructor.<p>
     *
     * @param labelKey the label message key
     * @param icon the icon
     */
    public A_CmsMenuItem(String labelKey, Resource icon) {

        m_labelKey = labelKey;
        m_icon = icon;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsMenuItem#getItemComponent(java.util.Locale)
     */
    public Component getItemComponent(Locale locale) {

        Button b = new Button(getLabel(locale), m_icon);
        b.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                executeAction();
            }
        });
        return b;
    }

    /**
     * Returns the label for the given locale.<p>
     *
     * @param locale the user locale
     *
     * @return the label
     */
    protected String getLabel(Locale locale) {

        return OpenCms.getWorkplaceManager().getMessages(locale).key(m_labelKey);
    }

    /**
     * Executes the item action.<p>
     */
    abstract void executeAction();

}

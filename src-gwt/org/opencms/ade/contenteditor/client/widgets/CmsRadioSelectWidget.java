/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.client.widgets;

import com.alkacon.acacia.client.widgets.I_EditWidget;

import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Provides a widget for a standard HTML form for a group of radio buttons.<p>
 * 
 * Please see the documentation of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> for a description 
 * about the configuration String syntax for the select options.<p>
 *
 * The multi select widget does use the following select options:<ul>
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getValue()}</code> for the value of the option
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#isDefault()}</code> for pre-selecting a specific value 
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getOption()}</code> for the display name of the option
 * </ul>
 * <p>
 * 
 * */
public class CmsRadioSelectWidget extends Composite implements I_EditWidget {

    private boolean m_active = true;
    private CmsRadioButtonGroup m_group = new CmsRadioButtonGroup();
    private List<CmsRadioButton> m_listRadioButtons;

    /**
     * Constructs an OptionalTextBox with the given caption on the check.<p>
     * @param config the configuration string.
     */
    public CmsRadioSelectWidget(String config) {

        m_listRadioButtons = parseconfig(config);

        // Place the check above the text box using a vertical panel.
        VerticalPanel panel = new VerticalPanel();
        Iterator<CmsRadioButton> i = m_listRadioButtons.iterator();
        while (i.hasNext()) {
            panel.add(i.next());
        }

        // All composites must call initWidget() in their constructors.
        initWidget(panel);

    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     * 
     */
    public void fireChangeEvent() {

        ValueChangeEvent.fire(this, m_group.getSelectedButton().getName());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_group.getSelectedButton().getName();
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        m_active = active;
        if (active) {
            fireChangeEvent();

        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, true);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        Iterator<CmsRadioButton> i = m_listRadioButtons.iterator();
        while (i.hasNext()) {
            CmsRadioButton rb = i.next();
            if (rb.getName().equals(value)) {
                m_group.selectButton(rb);
            }
            if (fireEvents) {
                fireChangeEvent();
            }
        }

    }

    /**
     * Helper class for parsing the configuration in to a list of Radiobuttons. <p>
     * 
     * @param config the configuration string.
     * */
    private List<CmsRadioButton> parseconfig(String config) {

        List<CmsRadioButton> list = new ArrayList<CmsRadioButton>();

        String[] labels = config.split("\\|");
        for (int i = 0; i < labels.length; i++) {

            CmsRadioButton radiobutton = new CmsRadioButton(labels[i], labels[i]);
            radiobutton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    fireChangeEvent();

                }
            });
            radiobutton.setGroup(m_group);
            if (labels[i].indexOf("*") >= 0) {
                radiobutton.setName(labels[i].replace("*", ""));
                radiobutton.setText(labels[i].replace("*", ""));
                radiobutton.setChecked(true);
            }
            list.add(radiobutton);
        }
        return list;
    }
}

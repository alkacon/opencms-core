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

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

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

    /** Default value of rows to be shown. */
    private static final int DEFAULT_ROWS_SHOWN = 10;

    /** The main panel of this widget. */
    FlowPanel m_panel = new FlowPanel();

    /** Value of the activation. */
    private boolean m_active = true;

    /** Array of all radio button. */
    private CmsRadioButton[] m_arrayRadioButtons;

    /** The default radio button set in xsd. */
    private CmsRadioButton m_defaultCheckBox;

    /** Value of the radio group. */
    private CmsRadioButtonGroup m_group = new CmsRadioButtonGroup();

    /** The scroll panel around the multiselections. */
    CmsScrollPanel m_scrollPanel = GWT.create(CmsScrollPanel.class);

    /** The parameter set from configuration.*/
    private int m_rowsToShow = DEFAULT_ROWS_SHOWN;

    /**
     * Constructs an OptionalTextBox with the given caption on the check.<p>
     * @param config the configuration string.
     */
    public CmsRadioSelectWidget(String config) {

        // generate a list of all radio button.
        List<CmsRadioButton> list = parseconfig(config);
        // move the list to the array of all radio button.
        m_arrayRadioButtons = new CmsRadioButton[list.size()];
        list.toArray(m_arrayRadioButtons);
        // add separate style to the panel.
        m_scrollPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().radioButtonPanel());
        // iterate about all radio button.
        for (int i = 0; i < m_arrayRadioButtons.length; i++) {
            // add a separate style each radio button.
            m_arrayRadioButtons[i].addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().radioButtonlabel());
            // add the radio button to the panel.
            m_panel.add(m_arrayRadioButtons[i]);
        }
        m_scrollPanel.add(m_panel);
        m_scrollPanel.setResizable(true);
        int height = (m_rowsToShow * 17);
        if (m_arrayRadioButtons.length < m_rowsToShow) {
            height = (m_arrayRadioButtons.length * 17);
        }
        m_scrollPanel.setDefaultHeight(height);
        m_scrollPanel.setHeight(height + "px");
        initWidget(m_scrollPanel);
        // All composites must call initWidget() in their constructors.

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

        String result = "";
        // check if there is a radio button selected.
        if (m_group.getSelectedButton() != null) {
            // set the name of the selected radio button.
            result = m_group.getSelectedButton().getName();
        }

        ValueChangeEvent.fire(this, result);
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

        // check if the value has changed. If there is no change do nothing.
        if (m_active == active) {
            return;
        }
        // set the new value.
        m_active = active;
        // Iterate about all radio button.
        for (int i = 0; i < m_arrayRadioButtons.length; i++) {
            // set the radio button active / inactive.
            m_arrayRadioButtons[i].setEnabled(active);
            // if this widget is set inactive.
            if (!active) {
                // deselect all radio button.
                m_arrayRadioButtons[i].setChecked(active);
            } else {
                // select the default value if set.
                if (m_defaultCheckBox != null) {
                    m_defaultCheckBox.setChecked(active);
                }
            }
        }
        // fire value change event.
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        // iterate about all the radio button.
        for (int i = 0; i < m_arrayRadioButtons.length; i++) {
            CmsRadioButton rb = m_arrayRadioButtons[i];
            // if the value is the name of a radio button active it.
            if (rb.getName().equals(value)) {
                m_group.selectButton(rb);
            }
            // fire change event.
            if (fireEvents) {
                fireChangeEvent();
            }
        }

    }

    /**
     * Helper class for parsing the configuration in to a list of Radiobuttons. <p>
     * 
     * @param config the configuration string.
     * @return List of CmsRadioButtons
     * */
    private List<CmsRadioButton> parseconfig(String config) {

        // generate an empty list off radio button.
        List<CmsRadioButton> result = new ArrayList<CmsRadioButton>();
        // split the configuration string by using the separator "|". 
        String[] labels = config.split("\\|");
        // iterate about all parts of the splitted configuration.
        for (int i = 0; i < labels.length; i++) {
            // create a new radio button with the given name and label.
            CmsRadioButton radiobutton = new CmsRadioButton(labels[i], labels[i]);
            // add click handler.
            radiobutton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    fireChangeEvent();

                }
            });
            // add this radio button to the group
            radiobutton.setGroup(m_group);
            // check if this value is default set.
            if (labels[i].indexOf("*") >= 0) {
                // rename the radio button.
                radiobutton.setName(labels[i].replace("*", ""));
                radiobutton.setText(labels[i].replace("*", ""));
                // set this radio button checked. 
                radiobutton.setChecked(true);
                m_defaultCheckBox = radiobutton;
            }
            // add this radio button to the list.
            result.add(radiobutton);
        }
        // return the list of radio button. 
        return result;
    }
}

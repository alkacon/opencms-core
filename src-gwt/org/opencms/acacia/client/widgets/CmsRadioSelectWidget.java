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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Provides a widget for a standard HTML form for a group of radio buttons.<p>
 *
 * Regarding widget configuration, see <code>{@link org.opencms.acacia.client.widgets.CmsSelectConfigurationParser}</code>.<p>
 * */
public class CmsRadioSelectWidget extends Composite implements I_CmsEditWidget {

    /** Default value of rows to be shown. */
    private static final int DEFAULT_ROWS_SHOWN = 10;

    /** The main panel of this widget. */
    FlowPanel m_panel = new FlowPanel();

    /** The scroll panel around the radio buttons. */
    CmsScrollPanel m_scrollPanel = GWT.create(CmsScrollPanel.class);

    /** Value of the activation. */
    private boolean m_active = true;

    /** The default radio button set in xsd. */
    private CmsRadioButton m_defaultRadioButton;

    /** Value of the radio group. */
    private CmsRadioButtonGroup m_group;

    /** List of all radio button. */
    private List<CmsRadioButton> m_radioButtons;

    /** The parameter set from configuration.*/
    private int m_rowsToShow = DEFAULT_ROWS_SHOWN;

    /**
     * Constructs an OptionalTextBox with the given caption on the check.<p>
     * @param config the configuration string.
     */
    public CmsRadioSelectWidget(String config) {

        // generate a list of all radio button.
        m_group = new CmsRadioButtonGroup();
        // move the list to the array of all radio button.
        m_radioButtons = parseConfiguration(config);
        // add separate style to the panel.
        m_scrollPanel.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().radioButtonPanel());
        FocusHandler focusHandler = new FocusHandler() {

            public void onFocus(FocusEvent event) {

                CmsDomUtil.fireFocusEvent(CmsRadioSelectWidget.this);
            }
        };
        // iterate about all radio button.
        for (CmsRadioButton radiobutton : m_radioButtons) {
            // add a separate style each radio button.
            radiobutton.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().radioButtonlabel());
            radiobutton.getRadioButton().addFocusHandler(focusHandler);
            // add the radio button to the panel.
            m_panel.add(radiobutton);
        }
        m_scrollPanel.add(m_panel);
        m_scrollPanel.setResizable(false);
        int lineHeight = CmsClientStringUtil.parseInt(I_CmsLayoutBundle.INSTANCE.constants().css().lineHeightBig());
        if (lineHeight <= 0) {
            lineHeight = 17;
        }
        int height = (m_rowsToShow * lineHeight);
        if (m_radioButtons.size() < m_rowsToShow) {
            height = (m_radioButtons.size() * lineHeight);
        }
        // account for padding
        height += 8;
        m_scrollPanel.setDefaultHeight(height);
        m_scrollPanel.setHeight(height + "px");
        initWidget(m_scrollPanel);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
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
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        // TODO implement this in case we want the delete behavior for optional fields
        return false;

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        // check if the value has changed. If there is no change do nothing.
        if (m_active == active) {
            // if the value is initial activated set the default value.
            if (active) {
                fireChangeEvent();
            }
            return;
        }
        // set the new value.
        m_active = active;
        // Iterate about all radio button.
        for (CmsRadioButton radiobutton : m_radioButtons) {
            // set the radio button active / inactive.
            radiobutton.setEnabled(active);
            // if this widget is set inactive.
            if (!active) {
                // deselect all radio button.
                radiobutton.setChecked(active);
            } else {
                // select the default value if set.
                if (m_defaultRadioButton != null) {
                    m_defaultRadioButton.setChecked(active);
                    fireChangeEvent();
                }
            }
        }
        // fire value change event.
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // no input field so nothing to do

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
        for (CmsRadioButton radiobutton : m_radioButtons) {
            // if the value is the name of a radio button active it.
            if (radiobutton.getName().equals(value)) {
                m_group.selectButton(radiobutton);
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
    private List<CmsRadioButton> parseConfiguration(String config) {

        // generate an empty list off radio button.
        List<CmsRadioButton> result = new LinkedList<CmsRadioButton>();

        CmsSelectConfigurationParser parser = new CmsSelectConfigurationParser(config);
        for (Map.Entry<String, String> entry : parser.getOptions().entrySet()) {
            // create a new radio button with the given name and label.
            CmsRadioButton radiobutton = new CmsRadioButton(entry.getKey(), entry.getValue());
            // add click handler.
            radiobutton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    fireChangeEvent();
                }
            });
            // add this radio button to the group
            radiobutton.setGroup(m_group);
            // check if this value is default set.
            if (entry.getKey().equals(parser.getDefaultValue())) {
                radiobutton.setChecked(true);
                m_defaultRadioButton = radiobutton;
            }
            // add this radio button to the list.
            result.add(radiobutton);
        }
        return result;
    }
}

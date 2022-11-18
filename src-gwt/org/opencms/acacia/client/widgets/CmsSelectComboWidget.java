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
import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsSelectComboBox;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * A combo box widget.<p>
 *
 * Regarding widget configuration, see <code>{@link org.opencms.acacia.client.widgets.CmsSelectConfigurationParser}</code>.<p>
 */
public class CmsSelectComboWidget extends Composite implements I_CmsEditWidget, I_CmsHasDisplayDirection {

    /** Value of the activation. */
    private boolean m_active = true;

    /** The combo box. */
    private CmsSelectComboBox m_comboBox;

    /**
     * Constructs an CmsComboWidget with the in XSD schema declared configuration.<p>
     *
     * @param config The configuration string given from OpenCms XSD.
     */
    public CmsSelectComboWidget(String config) {

        parseConfiguration(config);

        m_comboBox.getComboBox().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxPanel());
        // add some styles to parts of the combobox.
        m_comboBox.getComboBox().getOpener().addStyleName(
            I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_comboBox.getComboBox().getSelectorPopup().addStyleName(
            I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxPopup());
        m_comboBox.getComboBox().getTextBox().addStyleName(
            I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().comboBoxInput());

        m_comboBox.getSelectBox().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxPanel());
        m_comboBox.getSelectBox().setPopupResize(false);
        m_comboBox.getSelectBox().getOpener().addStyleName(
            I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_comboBox.getSelectBox().getSelectorPopup().addStyleName(
            I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());

        m_comboBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }

        });
        initWidget(m_comboBox);
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

        ValueChangeEvent.fire(this, m_comboBox.getFormValueAsString());

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsHasDisplayDirection#getDisplayingDirection()
     */
    public Direction getDisplayingDirection() {

        return m_comboBox.displayingAbove() ? Direction.above : Direction.below;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_comboBox.getFormValueAsString();
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

        if (active == m_active) {
            return;
        }
        m_active = active;
        m_comboBox.setEnabled(active);
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

        //m_selectBox.selectValue(value);
        m_comboBox.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#shouldSetDefaultWhenDisabled()
     */
    public boolean shouldSetDefaultWhenDisabled() {

        return true;
    }

    /**
     * Helper function for parsing the configuration of the combo-box.<p>
     *
     * @param config the configuration string.
     * */
    private void parseConfiguration(String config) {

        CmsSelectConfigurationParser parser = new CmsSelectConfigurationParser(config);
        m_comboBox = new CmsSelectComboBox(parser.getOptions(), false, false);
        if (parser.getDefaultValue() != null) {
            //set the declared value selected.
            m_comboBox.setFormValueAsString(parser.getDefaultValue());
        }
        fireChangeEvent();
    }
}

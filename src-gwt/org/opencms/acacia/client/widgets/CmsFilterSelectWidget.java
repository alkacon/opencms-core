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
import org.opencms.gwt.client.ui.input.CmsFilterSelectBox;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * An option of a select type widget.<p>
 *
 * Regarding widget configuration, see <code>{@link org.opencms.acacia.client.widgets.CmsSelectConfigurationParser}</code>.<p>
 */
public class CmsFilterSelectWidget extends Composite implements I_CmsEditWidget, I_CmsHasDisplayDirection {

    /** The global select box. */
    protected CmsFilterSelectBox m_selectBox = new CmsFilterSelectBox();

    /** Value of the activation. */
    private boolean m_active = true;

    /** THe default value. */
    private String m_defaultValue;

    /** The last value set through the setValue method. This is not necessarily the current widget value. */
    private String m_externalValue;

    /**
     * Constructs an CmsComboWidget with the in XSD schema declared configuration.<p>
     * @param config The configuration string given from OpenCms XSD.
     */
    public CmsFilterSelectWidget(String config) {

        parseConfiguration(config);

        // Place the check above the box using a vertical panel.
        m_selectBox.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxPanel());
        m_selectBox.setPopupResize(false);
        // add some styles to parts of the selectbox.
        m_selectBox.getOpener().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_selectBox.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_selectBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }

        });

        // All composites must call initWidget() in their constructors.
        initWidget(m_selectBox);

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

        ValueChangeEvent.fire(this, m_selectBox.getFormValueAsString());

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsHasDisplayDirection#getDisplayingDirection()
     */
    public Direction getDisplayingDirection() {

        return Direction.below;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_selectBox.getFormValueAsString();
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

        // check if value change. If not do nothing.
        if (m_active == active) {
            if (active && !m_selectBox.getFormValueAsString().equals(m_externalValue)) {
                // in case the previously set external value does not match the internal select box value fire change event
                // this is needed in case the external value didn't match any select option,
                // in this case the select box will display the first available value so fire the change to reflect that
                fireChangeEvent();
            }
            return;
        }
        // set new value.
        m_active = active;
        // set the new value to the selectbox.
        m_selectBox.setEnabled(active);
        // fire change event if necessary.
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

        Map<String, String> items = m_selectBox.getItems();
        m_selectBox.setFormValue(value, false);
        m_externalValue = value;
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
     * Helper class for parsing the configuration of the select-box. <p>
     *
     * @param config the configuration string
     * */
    private void parseConfiguration(String config) {

        CmsSelectConfigurationParser parser = new CmsSelectConfigurationParser(config);
        // set the help info first!!
        for (Entry<String, String> helpEntry : parser.getHelpTexts().entrySet()) {
            m_selectBox.setTitle(helpEntry.getKey(), helpEntry.getValue());
        }
        //set value and option to the combo box.
        m_selectBox.setItems(parser.getOptions());
        //if one entrance is declared for default.
        if (parser.getDefaultValue() != null) {
            //set the declared value selected.
            m_selectBox.selectValue(parser.getDefaultValue());
            m_defaultValue = parser.getDefaultValue();
        }
        fireChangeEvent();
    }

}

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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * This class represents a labeled checkbox which is not represented as an INPUT element in
 * the DOM, but is displayed as an image.<p>
 *
 * It can be checked/unchecked and enabled/disabled, which means 4 combinations in total.
 * So you need to supply 4 images, one for each of the combinations.<p>
 *
 * @since 8.0.0
 */
public class CmsCheckBox extends Composite
implements HasClickHandlers, I_CmsFormWidget, I_CmsHasInit, HasHorizontalAlignment, HasValueChangeHandlers<Boolean> {

    /** Type string for this widget. */
    public static final String WIDGET_TYPE = "checkbox";

    /** CSS bundle for this widget. */
    private static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The current horizontal alignment. */
    private HorizontalAlignmentConstant m_align;

    /** Toggle button which actually displays the checkbox. */
    private final CmsToggleButton m_button;

    /** The error display for this widget. */
    private final CmsErrorWidget m_error;

    /** Internal root widget to which all other components of this widget are attached. */
    private final FlowPanel m_root;

    /** The internal value of this checkbox. */
    private String m_value;

    /**
     * Default constructor which creates a checkbox without a label.<p>
     */
    public CmsCheckBox() {

        this(null);
    }

    /**
     * Public constructor for a checkbox.<p>
     *
     * The label text passed will be displayed to the right of the checkbox. If
     * it is null, no label is displayed.<p>
     *
     * @param labelText the label text
     */
    public CmsCheckBox(String labelText) {

        m_button = new CmsToggleButton();
        m_button.setUseMinWidth(false);
        m_button.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        m_button.setImageClass(CSS.checkBoxImage());
        if (labelText != null) {
            m_button.setText(labelText);
        }
        setHorizontalAlignment(ALIGN_RIGHT);

        m_root = new FlowPanel();
        m_error = new CmsErrorWidget();
        m_root.add(m_button);
        m_root.add(m_error);

        initWidget(m_root);
        addStyleName(CSS.checkBox());
        addStyleName(CSS.inlineBlock());
        m_button.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {

                ValueChangeEvent.fire(CmsCheckBox.this, changeEvent.getValue());
            }
        });
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsCheckBox(widgetParams.get("label"));
            }
        });
    }

    /**
     * Adds a click handler to the checkbox.<p>
     *
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Disables the checkbox and changes the checkbox title attribute to the disabled reason.<p>
     *
     * @param disabledReason the disabled reason
     */
    public void disable(String disabledReason) {

        m_button.disable(disabledReason);
    }

    /**
     * Enables the checkbox, switching the checkbox title attribute from the disabled reason to the original title.<p>
     */
    public void enable() {

        m_button.enable();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * Gets the toggle button used internally.<p>
     *
     * @return the toggle button
     */
    public CmsToggleButton getButton() {

        return m_button;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.BOOLEAN;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Boolean getFormValue() {

        return isChecked() ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return "" + isChecked();
    }

    /**
     * This is the alignment of the text in reference to the checkbox, possible values are left or right.<p>
     *
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#getHorizontalAlignment()
     */
    public HorizontalAlignmentConstant getHorizontalAlignment() {

        return m_align;
    }

    /**
     * Returns the internal value of this Checkbox.<p>
     *
     * @return the internal value of this Checkbox
     */
    public String getInternalValue() {

        return m_value;
    }

    /**
     * Returns the text.<p>
     *
     * @return the text as String
     */
    public String getText() {

        return m_button.getText();

    }

    /**
     * Returns true if the checkbox is checked.<p>
     *
     * @return true if the checkbox is checked
     */
    public boolean isChecked() {

        return m_button.isDown();
    }

    /**
     * Returns true if the checkbox is enabled.<p>
     *
     * @return true if the checkbox is enabled
     */
    public boolean isEnabled() {

        return m_button.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        setChecked(false);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // do nothing
    }

    /**
     * Checks or unchecks the checkbox.<p>
     *
     * @param checked if true, check the checkbox else uncheck it
     */
    public void setChecked(boolean checked) {

        m_button.setDown(checked);
    }

    /**
     * Toggles between display:inline-block and display:block.<p>
     *
     * @param inline <code>true</code> to display inline-block
     */
    public void setDisplayInline(boolean inline) {

        if (inline) {
            addStyleName(CSS.inlineBlock());
        } else {
            removeStyleName(CSS.inlineBlock());
        }
    }

    /**
     * Enables or disables the checkbox.<p>
     *
     * @param enabled if true, enable the checkbox, else disable it
     */
    public void setEnabled(boolean enabled) {

        m_button.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);
    }

    /**
     * Sets the value of the widget.<p>
     *
     * @param value the new value
     */
    public void setFormValue(Object value) {

        if (value instanceof Boolean) {
            Boolean boolValue = (Boolean)value;
            setChecked(boolValue.booleanValue());
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        setChecked(Boolean.parseBoolean(value) || "checked".equalsIgnoreCase(value));
    }

    /**
     * This is the alignment of the text in reference to the checkbox, possible values are left or right.<p>
     *
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#setHorizontalAlignment(com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant)
     */
    public void setHorizontalAlignment(HorizontalAlignmentConstant align) {

        if (align.equals(HasHorizontalAlignment.ALIGN_CENTER)) {
            // ignore center alignment
            return;
        }
        m_button.setHorizontalAlignment(align);
        m_align = align;
    }

    /**
     * Sets the internal value of this Checkbox.<p>
     *
     *  @param value the new internal value
     */
    public void setInternalValue(String value) {

        m_value = value;
    }

    /**
     * Sets the text.<p>
     *
     * @param text the text to set
     */
    public void setText(String text) {

        m_button.setText(text);
    }

    /**
     * Helper method for firing a 'value changed' event.<p>
     */
    protected void fireValueChangedEvent() {

        ValueChangeEvent.fire(this, getFormValue());
    }
}

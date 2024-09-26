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

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.widgets.CmsTypografUtil.Typograf;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsGwtLog;

import java.util.Objects;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

import elemental2.core.Global;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * Provides a display only widget, for use on a widget dialog.<p>
 *
 * */
public class CmsTextboxWidget extends Composite implements I_CmsEditWidget {

    /**
     * The UI binder interface.<p>
     */
    interface I_CmsTextboxWidgetUiBinder extends UiBinder<HTMLPanel, CmsTextboxWidget> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsTextboxWidgetUiBinder uiBinder = GWT.create(I_CmsTextboxWidgetUiBinder.class);

    /** The fader of this widget. */
    @UiField
    FocusPanel m_fadePanel;

    /**The main panel of this widget. */
    HTMLPanel m_mainPanel;

    /** The input test area.*/
    @UiField
    TextBox m_textbox;

    /** The token to control activation. */
    private boolean m_active = true;

    /** The previous value. */
    private String m_previousValue;

    /** Typograf instance used for typography, if configured. */
    private Typograf m_typograf;

    /** The value changed handler initialized flag. */
    private boolean m_valueChangeHandlerInitialized;

    /**
     * Creates a new display widget.<p>
     *
     * @param config the widget configuration
     */
    public CmsTextboxWidget(String config) {

        m_mainPanel = uiBinder.createAndBindUi(this);
        if (config != null) {
            try {
                JsPropertyMap<String> configMap = Js.cast(Global.JSON.parse(config));
                String locale = configMap.get(CmsGwtConstants.JSON_INPUT_LOCALE);
                String typograf = configMap.get(CmsGwtConstants.JSON_INPUT_TYPOGRAF);
                boolean typografEnabled = "auto".equals(typograf);
                if ((locale != null) && typografEnabled && Typograf.hasLocale(locale)) {
                    m_typograf = CmsTypografUtil.createLiveInstance(locale);
                }
            } catch (Exception e) {
                CmsGwtLog.log(e.getMessage());
            }
        }

        initWidget(m_mainPanel);
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

        // Initialization code
        if (!m_valueChangeHandlerInitialized) {
            m_valueChangeHandlerInitialized = true;
            HTMLInputElement inputElem = Js.cast(m_textbox.getElement());
            inputElem.addEventListener("input", event -> {
                if (m_typograf != null) {
                    try {
                        String oldValue = inputElem.value;
                        String newValue = CmsTypografUtil.transform(m_typograf, oldValue);
                        if (!Objects.equals(oldValue, newValue)) {
                            int pos = inputElem.selectionStart;
                            inputElem.value = newValue;
                            inputElem.setSelectionRange(pos, pos);
                        }
                    } catch (Exception e) {
                        CmsGwtLog.log(e.getMessage());
                    }
                }
                fireValueChange(false);
            });
        }
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     *
     */
    public void fireChangeEvent() {

        String result = "";
        if (m_textbox.getText() != null) {
            result = m_textbox.getText();
        }

        ValueChangeEvent.fire(this, result);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_textbox.getText();
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

        if (m_active == active) {
            return;
        }

        m_active = active;
        if (m_active) {
            getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
            getElement().focus();
        } else {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
        }
        if (active) {
            fireChangeEvent();
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        m_textbox.setName(name);

    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {

        if ((title.length() * 6.88) > m_mainPanel.getOffsetWidth()) {
            m_mainPanel.getElement().setTitle(title);
        } else {
            m_mainPanel.getElement().setTitle("");
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        m_textbox.setText(value);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        // set the saved value to the textArea
        m_textbox.setText(value);
        m_previousValue = value;
        if (fireEvents) {
            fireChangeEvent();
        }
    }

    /**
     * Fires the value change event, if the value has changed.<p>
     *
     * @param force <code>true</code> to force firing the event, not regarding an actually changed value
     */
    protected void fireValueChange(boolean force) {

        String currentValue = getValue();
        if (force || !currentValue.equals(m_previousValue)) {
            m_previousValue = currentValue;
            ValueChangeEvent.fire(this, currentValue);
        }
    }

    /**
     * Handles fade panel clicks.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_fadePanel")
    void onFadeClick(ClickEvent event) {

        m_textbox.setFocus(true);
        m_textbox.setCursorPos(m_textbox.getText().length());
    }

    /**
     * Handles text box blur.<p>
     *
     * @param event the blur event
     */
    @UiHandler("m_textbox")
    void onTextboxBlur(BlurEvent event) {

        m_mainPanel.add(m_fadePanel);
        setTitle(m_textbox.getText());
    }

    /**
     * Handles text box focus.<p>
     *
     * @param event the focus event
     */
    @UiHandler("m_textbox")
    void onTextboxFocus(FocusEvent event) {

        m_mainPanel.remove(m_fadePanel);
        setTitle("");
        CmsDomUtil.fireFocusEvent(this);
    }

    /**
     * Handles text box value change.<p>
     *
     * @param event the value change event
     */
    @UiHandler("m_textbox")
    void onTextboxValueChange(ValueChangeEvent<String> event) {

        fireValueChange(false);
    }

}

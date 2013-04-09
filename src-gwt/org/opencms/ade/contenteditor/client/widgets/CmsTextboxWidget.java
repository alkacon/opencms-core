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

import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.acacia.client.widgets.I_EditWidget;

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Provides a display only widget, for use on a widget dialog.<p>
 *  
 * */
public class CmsTextboxWidget extends Composite implements I_EditWidget {

    /** The value changed handler initialized flag. */
    private boolean m_valueChangeHandlerInitialized;

    /** The previous value. */
    private String m_previousValue;

    /** The layout bundle. */
    protected static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The fader of this widget. */
    Panel m_fadePanel = new SimplePanel();

    /**The main panel of this widget. */
    Panel m_mainPanel = new FlowPanel();

    /** The input test area.*/
    TextBox m_textbox = new TextBox();

    /** The token to control activation. */
    private boolean m_active = true;

    /** Default value. */
    private String m_defaultValue = "";

    /**
     * Creates a new display widget.<p>
     * @param config 
     */
    public CmsTextboxWidget(String config) {

        if ((config != "") || (config != null)) {
            parseConfig(config);
        }
        m_fadePanel.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_textbox.setFocus(true);
                m_textbox.setCursorPos(m_textbox.getText().length());
            }
        }, ClickEvent.getType());
        m_fadePanel.setStyleName(CSS.inputTextBoxFader());
        m_textbox.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                m_mainPanel.remove(m_fadePanel);
                setTitle("");

            }
        });
        m_mainPanel.getElement().getStyle().setMarginRight(12, Unit.PX);
        m_mainPanel.add(m_textbox);
        m_mainPanel.add(m_fadePanel);

        m_textbox.setStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().textBox());
        m_textbox.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }
        });
        m_textbox.addBlurHandler(new BlurHandler() {

            public void onBlur(BlurEvent event) {

                m_mainPanel.add(m_fadePanel);
                setTitle(m_textbox.getText());
            }
        });
        initWidget(m_mainPanel);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return null;
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        // Initialization code
        if (!m_valueChangeHandlerInitialized) {
            m_valueChangeHandlerInitialized = true;
            addDomHandler(new KeyUpHandler() {

                public void onKeyUp(KeyUpEvent event) {

                    // schedule the change event, so the key press can take effect
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        public void execute() {

                            fireValueChange(false);
                        }
                    });
                }
            }, KeyUpEvent.getType());
            addDomHandler(new BlurHandler() {

                public void onBlur(BlurEvent event) {

                    fireValueChange(false);
                }
            }, BlurEvent.getType());
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

        if (m_active == active) {
            return;
        }

        m_active = active;
        if (m_active) {
            getElement().removeClassName(I_LayoutBundle.INSTANCE.form().inActive());
            getElement().focus();
            m_textbox.setText(m_defaultValue);
        } else {
            getElement().addClassName(I_LayoutBundle.INSTANCE.form().inActive());
        }
        if (!active) {
            m_textbox.setText("");
        }
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setName(java.lang.String)
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
     * Parse the configuration String.<p>
     * @param config the configuration String
     */
    private void parseConfig(String config) {

        m_defaultValue = config;
        setValue(m_defaultValue);
    }

}

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
import org.opencms.gwt.client.I_CmsHasResizeOnShow;
import org.opencms.gwt.client.ui.input.CmsTextArea;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * Provides a display only widget, for use on a widget dialog.<p>
 *
 * */
public class CmsTextareaWidget extends Composite implements I_CmsEditWidget, HasResizeHandlers, I_CmsHasResizeOnShow {

    /** Default number of rows to display. */
    private static final int DEFAULT_ROWS_NUMBER = 5;

    /** The token to control activation. */
    private boolean m_active = true;

    /** The input test area.*/
    private CmsTextArea m_textarea = new CmsTextArea();

    /**
     * Creates a new display widget.<p>
     *
     * @param config the widget configuration string
     */
    public CmsTextareaWidget(String config) {

        // All composites must call initWidget() in their constructors.
        initWidget(m_textarea);
        int configheight = DEFAULT_ROWS_NUMBER;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(config)) {
            try {
                int rows = Integer.parseInt(config);
                if (rows > 0) {
                    configheight = rows;
                }
            } catch (Exception e) {
                // nothing to do
            }
        }
        m_textarea.setRows(configheight);
        m_textarea.getTextArea().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().textAreaBox());
        m_textarea.getTextAreaContainer().addStyleName(
            I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().textAreaBoxPanel());
        m_textarea.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }
        });
        m_textarea.addResizeHandler(new ResizeHandler() {

            public void onResize(ResizeEvent event) {

                fireResizeEvent(event);

            }
        });

    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return addHandler(handler, ResizeEvent.getType());
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
        if (m_textarea.getFormValueAsString() != null) {
            result = m_textarea.getFormValueAsString();
        }

        ValueChangeEvent.fire(this, result);
    }

    /**
     * Represents a resize event.<p>
     * @param event from text area panel
     */
    public void fireResizeEvent(ResizeEvent event) {

        ResizeEvent.fire(this, event.getWidth(), event.getHeight());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_textarea.getFormValueAsString();
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

        return getElement().isOrHasChild(element);

    }

    /**
     * @see org.opencms.gwt.client.I_CmsHasResizeOnShow#resizeOnShow()
     */
    public void resizeOnShow() {

        m_textarea.resizeOnShow();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (m_active == active) {
            return;
        }

        m_active = active;
        m_textarea.setEnabled(m_active);
        if (m_active) {
            getElement().removeClassName(org.opencms.acacia.client.css.I_CmsLayoutBundle.INSTANCE.form().inActive());
            getElement().focus();
        } else {
            getElement().addClassName(org.opencms.acacia.client.css.I_CmsLayoutBundle.INSTANCE.form().inActive());
        }
        if (!active) {
            m_textarea.setFormValueAsString("");
        }
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        m_textarea.setName(name);

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

        // set the saved value to the textArea
        m_textarea.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }
    }

}

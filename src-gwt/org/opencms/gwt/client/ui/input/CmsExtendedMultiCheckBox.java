/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.util.CmsStringUtil;

import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;

/**
 * More advanced multi check box widget which also supports ghost values and is displayed in a box.
 * This can be used for the property dialog.<p>
 */
public class CmsExtendedMultiCheckBox extends Composite
implements I_CmsFormWidget, I_CmsHasGhostValue, HasValueChangeHandlers<String>, I_CmsHasInit {

    /** The widget type used to configure this widget. */
    public static final String WIDGET_TYPE = "extended_multicheck";

    /** Flag which indicates whether this widget is in ghost mode. */
    boolean m_ghostMode;

    /** The ghost value for this widget. */
    String m_ghostValue;

    /** The internal multi-checkbox widget. */
    CmsMultiCheckBox m_widget;

    /**
     * Creates a new widget instance.<p>
     *
     * @param options a map with the check box values as keys and the check box labels as values
     */
    public CmsExtendedMultiCheckBox(Map<String, String> options) {

        m_widget = new CmsMultiCheckBox(options);
        for (CmsCheckBox checkbox : m_widget.getCheckboxes()) {
            checkbox.getButton().getElement().getStyle().setFontWeight(Style.FontWeight.NORMAL);
        }
        initWidget(m_widget);
        m_widget.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().multiCheckboxPanel());
        m_widget.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_widget.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                boolean newValueEmpty = CmsStringUtil.isEmptyOrWhitespaceOnly(event.getValue());
                if (newValueEmpty && !CmsStringUtil.isEmptyOrWhitespaceOnly(m_ghostValue)) {
                    //HACK: Postpone resetting to ghost mode, because we don't want to interfere with other event handlers
                    // for the current event
                    Timer timer = new Timer() {

                        @Override
                        public void run() {

                            m_widget.setFormValueAsString(m_ghostValue);
                            m_widget.setTextWeak(true);
                            m_ghostMode = true;
                        }
                    };
                    timer.schedule(1);
                }
                if (!newValueEmpty && m_ghostMode) {
                    m_ghostMode = false;
                    m_widget.setTextWeak(false);
                }
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

                return new CmsExtendedMultiCheckBox(widgetParams);
            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return m_widget.addValueChangeHandler(handler);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return m_widget.getApparentValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return m_widget.getFieldType();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        if (m_ghostMode) {
            return null;
        } else {
            return m_widget.getFormValue();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        if (m_ghostMode) {
            return null;
        } else {
            return m_widget.getFormValueAsString();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_widget.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_widget.reset();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        m_widget.setAutoHideParent(autoHideParent);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_widget.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_widget.setErrorMessage(errorMessage);

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        m_widget.setFormValueAsString(value);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsHasGhostValue#setGhostMode(boolean)
     */
    public void setGhostMode(boolean enable) {

        m_ghostMode = enable;
        m_widget.setTextWeak(enable);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsHasGhostValue#setGhostValue(java.lang.String, boolean)
     */
    public void setGhostValue(String value, boolean isGhostMode) {

        if (isGhostMode) {
            m_widget.setTextWeak(true);
            m_widget.setFormValueAsString(value);
            m_ghostMode = true;
        }
        m_ghostValue = value;
    }

}

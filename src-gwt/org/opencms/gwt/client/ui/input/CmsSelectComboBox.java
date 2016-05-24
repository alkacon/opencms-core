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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Hybrid select / combo box widget.<p>
 *
 * This widget behaves like a select box, until a button on it is pressed, after which the select box is transformed into a combo box.
 * The reason for this is that the combo box always displays the currently selected value itself, rather than the label from the widget configuration,
 * which may be confusing to nontechnical users in some cases.
 */
public class CmsSelectComboBox extends Composite implements I_CmsFormWidget, I_CmsHasInit {

    /** Widget type identifier for the configuration. */
    public static final String WIDGET_TYPE = "selectcombo";

    /** The panel containing the actual widgets. */
    private FlowPanel m_panel = new FlowPanel();

    /** The select box initially displayed by this widget. */
    private CmsSelectBox m_selectBox;

    /** The options for the widget. */
    private Map<String, String> m_options;

    /** The combo box (initially null). */
    private CmsComboBox m_comboBox;

    /** An error which has been set before. */
    private String m_error;

    /**
     * Creates a new widget instance.<p>
     *
     * @param options the widget options
     */
    public CmsSelectComboBox(Map<String, String> options) {
        m_options = options;
        m_selectBox = new CmsSelectBox(options, true);
        m_panel.add(m_selectBox);
        CmsPushButton comboButton = new CmsPushButton();
        comboButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        comboButton.setImageClass(I_CmsButton.PEN_SMALL);
        comboButton.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().selectComboIcon());
        m_selectBox.addWidget(comboButton);
        comboButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                enableComboMode();
            }
        });

        initWidget(m_panel);
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsSelectComboBox(widgetParams);
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getActiveWidget().getApparentValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return getActiveWidget().getFieldType();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        return getActiveWidget().getFormValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return getActiveWidget().getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return getActiveWidget().isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        getActiveWidget().reset();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {
        // do nothing
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        getActiveWidget().setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error = errorMessage;

        getActiveWidget().setErrorMessage(errorMessage);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        getActiveWidget().setFormValueAsString(value);
    }

    /**
     * Switches to combo box mode.<p>
     */
    protected void enableComboMode() {

        String value = m_selectBox.getFormValueAsString();
        m_selectBox.removeFromParent();
        m_comboBox = new CmsComboBox(m_options);
        m_panel.add(m_comboBox);
        m_comboBox.setFormValueAsString(value);
        m_comboBox.setErrorMessage(m_error);
        m_comboBox.setEnabled(m_selectBox.isEnabled());
    }

    /**
     * Gets the active widget (select or combo box).<p>
     *
     * @return the active widget
     */
    private I_CmsFormWidget getActiveWidget() {

        if (m_comboBox != null) {
            return m_comboBox;
        } else {
            return m_selectBox;
        }
    }

}

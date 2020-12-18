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
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.contextmenu.I_CmsStringSelectHandler;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.shared.CmsLinkBean;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Basic gallery widget for forms.<p>
 *
 * @since 8.0.0
 *
 */
public class CmsPrincipalSelection extends Composite
implements I_CmsFormWidget, I_CmsHasInit, HasValueChangeHandlers<String> {

    /** The dialog id. */
    public static final String DIALOG_ID = "principalselect";

    /** A counter used for giving text box widgets ids. */
    private static int idCounter;

    /** The widget type identifier for this widget. */
    private static final String WIDGET_TYPE = "groupselection";

    /** The old value. */
    protected String m_oldValue = "";

    /** The popup frame. */
    protected CmsFramePopup m_popup;

    /** The handler registration. */
    protected HandlerRegistration m_previewHandlerRegistration;

    /** The default rows set. */
    int m_defaultRows;

    /** The root panel containing the other components of this widget. */
    Panel m_panel = new FlowPanel();

    /** The container for the text area. */
    CmsSelectionInput m_selectionInput;

    /** The configuration parameters. */
    private Map<String, String> m_configuration;

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The field id. */
    private String m_id;

    /**
     * VsfSelection widget to open the gallery selection.<p>
     * @param config the configuration for this widget
     */
    public CmsPrincipalSelection(String config) {

        initWidget(m_panel);
        parseConfiguration(config);
        m_selectionInput = new CmsSelectionInput(null);
        m_id = "CmsVfsSelection_" + (idCounter++);
        m_selectionInput.m_textbox.getElement().setId(m_id);

        m_panel.add(m_selectionInput);
        m_panel.add(m_error);

        m_selectionInput.m_textbox.addMouseUpHandler(new MouseUpHandler() {

            public void onMouseUp(MouseUpEvent event) {

                m_selectionInput.hideFader();
                setTitle("");
                if (m_popup == null) {
                    open();
                } else if (m_popup.isShowing()) {
                    close();
                } else {
                    open();
                }

            }

        });
        m_selectionInput.m_textbox.addBlurHandler(new BlurHandler() {

            public void onBlur(BlurEvent event) {

                if ((m_selectionInput.m_textbox.getValue().length()
                    * 6.88) > m_selectionInput.m_textbox.getOffsetWidth()) {
                    setTitle(m_selectionInput.m_textbox.getValue());
                }
                m_selectionInput.showFader();
            }
        });
        m_selectionInput.setOpenCommand(new Command() {

            public void execute() {

                if (m_popup == null) {
                    open();
                } else if (m_popup.isShowing()) {
                    close();
                } else {
                    open();
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
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map, com.google.common.base.Optional)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams, Optional<String> defaultValue) {

                return new CmsPrincipalSelection("type=groupwidget");
            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return m_selectionInput.m_textbox.addValueChangeHandler(handler);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        if (m_selectionInput.m_textbox.getText() == null) {
            return "";
        }
        return m_selectionInput.m_textbox.getValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return (String)getFormValue();
    }

    /**
     * Returns the selected link as a bean.<p>
     *
     * @return the selected link as a bean
     */
    public CmsLinkBean getLinkBean() {

        String link = m_selectionInput.m_textbox.getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(link)) {
            return null;
        }
        return new CmsLinkBean(m_selectionInput.m_textbox.getText(), true);
    }

    /**
     * Returns the text contained in the text area.<p>
     *
     * @return the text in the text area
     */
    public String getText() {

        return m_selectionInput.m_textbox.getValue();
    }

    /**
     * Returns the text box container of this widget.<p>
     *
     * @return the text box container
     */
    public CmsSelectionInput getTextAreaContainer() {

        return m_selectionInput;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_selectionInput.m_textbox.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_selectionInput.m_textbox.setText("");
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_selectionInput.m_textbox.setEnabled(enabled);
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

        if (value == null) {
            value = "";
        }
        if (value instanceof String) {
            String strValue = (String)value;
            m_selectionInput.m_textbox.setText(strValue);
            setTitle(strValue);
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String newValue) {

        setFormValue(newValue);
    }

    /**
     * Sets the link from a bean.<p>
     *
     * @param link the link bean
     */
    public void setLinkBean(CmsLinkBean link) {

        if (link == null) {
            link = new CmsLinkBean("", true);
        }
        m_selectionInput.m_textbox.setValue(link.getLink());
    }

    /**
     * Sets the name of the input field.<p>
     *
     * @param name of the input field
     * */
    public void setName(String name) {

        m_selectionInput.m_textbox.setName(name);

    }

    /**
     * Sets the text in the text area.<p>
     *
     * @param text the new text
     */
    public void setText(String text) {

        m_selectionInput.m_textbox.setValue(text);
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {

        m_selectionInput.m_textbox.getElement().setTitle(title);
    }

    /**
     * Close the popup of this widget.<p>
     * */
    protected void close() {

        m_popup.hideDelayed();
        m_selectionInput.m_textbox.setFocus(true);
        m_selectionInput.m_textbox.setCursorPos(m_selectionInput.m_textbox.getText().length());
    }

    /**
     * Opens the popup of this widget.<p>
     * */
    protected void open() {

        m_oldValue = m_selectionInput.m_textbox.getValue();
        CmsEmbeddedDialogHandler handler = new CmsEmbeddedDialogHandler();
        handler.setStringSelectHandler(new I_CmsStringSelectHandler() {

            public void selectString(String principal) {

                if (!getFormValue().equals(principal)) {
                    setFormValueAsString(principal);
                    fireValueChange(principal);
                }
            }
        });
        handler.openDialog(DIALOG_ID, null, null, m_configuration);
    }

    /**
     * Fires the value change event.<p>
     *
     * @param value the changed value
     */
    void fireValueChange(String value) {

        ValueChangeEvent.<String> fire(m_selectionInput.m_textbox, value);
    }

    /**
     * Parses the configuration string.<p>
     *
     * @param config the configuration string
     */
    private void parseConfiguration(String config) {

        m_configuration = new HashMap<String, String>();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(config)) {
            for (String param : config.split(",")) {
                int index = param.indexOf("=");
                if ((index > 0) && (param.length() > (index + 1))) {
                    m_configuration.put(param.substring(0, index), param.substring(index + 1));
                }
            }
        }
    }
}

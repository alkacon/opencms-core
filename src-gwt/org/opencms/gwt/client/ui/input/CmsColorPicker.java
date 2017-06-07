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
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.colorpicker.CmsColorSelector;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Basic text area widget for forms.<p>
 *
 * @since 8.0.0
 *
 */
public class CmsColorPicker extends Composite implements I_CmsFormWidget, I_CmsHasInit {

    /**
     * Drag and drop event preview handler.<p>
     *
     * To be used while dragging.<p>
     */
    protected class CloseEventPreviewHandler implements NativePreviewHandler {

        /**
         * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
         */
        public void onPreviewNativeEvent(NativePreviewEvent event) {

            Event nativeEvent = Event.as(event.getNativeEvent());
            switch (DOM.eventGetType(nativeEvent)) {
                case Event.ONKEYDOWN:
                    break;
                case Event.ONMOUSEWHEEL:
                    closePopup();
                    break;
                default:
                    // do nothing
            }
        }

    }

    /** The widget type identifier for this widget. */
    private static final String WIDGET_TYPE = "colorPicker";

    /** The field to display the color. */
    protected SimplePanel m_colorField = new SimplePanel();

    /** The color value.*/
    protected String m_colorValue = "transparent";

    /** The popup to choose the color. */
    protected CmsPopup m_popup = new CmsPopup();

    /***/
    protected HandlerRegistration m_previewHandlerRegistration;
    /** The field to display the value. */
    protected SimplePanel m_textboxpanel = new SimplePanel();

    /** The x-coords of the popup. */
    protected int m_xcoordspopup;

    /** The y-coords of the popup. */
    protected int m_ycoordspopup;

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The root panel containing the other components of this widget. */
    private Panel m_panel = new FlowPanel();

    /** The internal textbox used by this widget to display the color value. */
    protected TextBox m_textboxColorValue = new TextBox();

    /** THe counter to not set the buttons more then one time. */
    int m_count;

    /**
     * Text area widgets for ADE forms.<p>
     */
    public CmsColorPicker() {

        super();

        initWidget(m_panel);
        m_panel.add(m_colorField);
        m_panel.add(m_textboxpanel);
        m_panel.add(m_error);
        m_textboxpanel.add(m_textboxColorValue);

        m_panel.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().colorPicker());
        m_textboxColorValue.addBlurHandler(new BlurHandler() {

            public void onBlur(BlurEvent event) {

                checkvalue(m_textboxColorValue.getValue());

            }
        });

        m_colorField.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                if (m_popup.isShowing()) {
                    closePopup();
                } else {
                    openPopup();
                }

            }

        }, ClickEvent.getType());

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

                return new CmsColorPicker();
            }
        });
    }

    /**
     * @param handler
     */
    public void addValueChangeHandler(ValueChangeHandler<String> handler) {

        m_textboxColorValue.addValueChangeHandler(handler);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * Returns the colorfield.<p>
     *
     * @return the colorfield
     */
    public SimplePanel getColorfield() {

        return m_colorField;
    }

    /**
     * Returns the color value textbox.<p>
     *
     * @return the color value textbox
     * */
    public TextBox getColorValueBox() {

        return m_textboxColorValue;
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

        if ((m_textboxColorValue.getText() == null) || !validateColorValue(m_textboxColorValue.getText())) {
            m_textboxColorValue.setText("");
        }
        return m_textboxColorValue.getText();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return (String)getFormValue();
    }

    /**
     * Returns the text contained in the text area.<p>
     *
     * @return the text in the text area
     */
    public String getText() {

        return m_textboxColorValue.getText();
    }

    /**
     * Returns the color value textboxpanel.<p>
     *
     * @return the color value textboxpanel
     */
    public SimplePanel getTextboxPanel() {

        return m_textboxpanel;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_textboxColorValue.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_textboxColorValue.setText("");
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

        // TODO: Auto-generated method stub

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
            if (strValue.length() > 0) {
                checkvalue(strValue);
            }
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String newValue) {

        setFormValue(newValue);
    }

    /**
     * Sets the name of the input field.<p>
     *
     * @param name of the input field
     * */
    public void setName(String name) {

        m_textboxColorValue.setName(name);

    }

    /**
     * Sets the text in the text area.<p>
     *
     * @param text the new text
     */
    public void setText(String text) {

        m_textboxColorValue.setText(text);
    }

    /**
     * Validates the inputed color value.
     * @param colorvalue the value of the color
     * @return true if the inputed color value is valid
     */
    protected boolean checkvalue(String colorvalue) {

        boolean valid = validateColorValue(colorvalue);
        if (valid) {
            if (colorvalue.length() == 4) {
                char[] chr = colorvalue.toCharArray();
                for (int i = 1; i < 4; i++) {
                    String foo = String.valueOf(chr[i]);
                    colorvalue = colorvalue.replaceFirst(foo, foo + foo);
                }
            }
            m_textboxColorValue.setValue(colorvalue, true);
            m_colorField.getElement().getStyle().setBackgroundColor(colorvalue);
            m_colorValue = colorvalue;
        }
        return valid;
    }

    /**
     * Close the popup and store the color value in the colorvalue field.<p>
     *
     */
    protected void closePopup() {

        if (m_previewHandlerRegistration != null) {
            m_previewHandlerRegistration.removeHandler();
        }
        m_previewHandlerRegistration = null;
        CmsColorSelector picker = (CmsColorSelector)m_popup.getWidget(0);
        if (checkvalue("#" + picker.getHexColor().toUpperCase())) {
            m_popup.hide();
        }

    }

    /**
     * Close the popup and store the old color value in the colorvalue field.<p>
     *
     */
    protected void closePopupDefault() {

        if (m_previewHandlerRegistration != null) {
            m_previewHandlerRegistration.removeHandler();
        }
        m_previewHandlerRegistration = null;
        if (checkvalue(m_textboxColorValue.getText())) {
            m_popup.hide();
        }

    }

    /**
     * Converts the integer value to an hex value.<p>
     * @param i the integer value
     * @return the hex string
     */
    protected String convertToHex(int i) {

        String hexString = Integer.toHexString(i);
        while (hexString.length() < 2) {
            hexString = "0" + hexString;
        }
        return hexString;
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                m_colorField.getElement().getStyle().setBackgroundColor(m_colorValue);
            }
        });
    }

    /**
     * Helper function  to open the popup.<p>
     */
    protected void openPopup() {

        m_popup.setWidth(475);
        m_popup.setAutoHideEnabled(true);
        m_popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                closePopupDefault();

            }
        });

        m_popup.addDialogClose(new Command() {

            public void execute() {

                // nothing to do all will be done in onClose();

            }
        });

        if (m_previewHandlerRegistration != null) {
            m_previewHandlerRegistration.removeHandler();

        }
        m_previewHandlerRegistration = Event.addNativePreviewHandler(new CloseEventPreviewHandler());
        m_popup.showRelativeTo(m_colorField);

        m_popup.setModal(false);
        if (m_popup.getWidgetCount() != 0) {
            m_popup.remove(m_popup.getWidget(0));
        }
        CmsColorSelector picker = new CmsColorSelector();
        try {
            picker.setHex(m_textboxColorValue.getText().replace("#", ""));
        } catch (Exception e) {
            // TODO: Auto-generated catch block
        }
        m_popup.add(picker);
        if (m_count == 0) {
            CmsPushButton close = new CmsPushButton();
            CmsPushButton cancel = new CmsPushButton();
            cancel.setText(Messages.get().key(Messages.GUI_CANCEL_0));
            cancel.setTitle(Messages.get().key(Messages.GUI_CANCEL_0));
            close.setText(Messages.get().key(Messages.GUI_OK_0));
            close.setTitle(Messages.get().key(Messages.GUI_OK_0));
            close.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    closePopup();

                }
            });
            cancel.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    closePopupDefault();

                }
            });
            m_popup.addButton(cancel);
            m_popup.addButton(close);
            m_count = 1;
        }

        m_xcoordspopup = m_popup.getPopupLeft();
        m_ycoordspopup = m_popup.getPopupTop();

    }

    /**
     * Checks if the given string is a valid colorvalue.<p>
     *
     * @param colorvalue to check
     * @return true if the value is valid otherwise false
     */
    private boolean validateColorValue(String colorvalue) {

        return colorvalue.matches("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");
    }
}

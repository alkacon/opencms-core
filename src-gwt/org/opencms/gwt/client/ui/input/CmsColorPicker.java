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
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Basic text area widget for forms.<p>
 * 
 * @since 8.0.0
 * 
 */
public class CmsColorPicker extends Composite implements I_CmsFormWidget, I_CmsHasInit {

    /** The widget type identifier for this widget. */
    private static final String WIDGET_TYPE = "colorPicker";

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The root panel containing the other components of this widget. */
    private Panel m_panel = new FlowPanel();

    /** The internal textbox used by this widget to display the color value. */
    private TextBox m_textboxColorValue = new TextBox();

    /** The field to display the color. */
    protected SimplePanel m_colorField = new SimplePanel();

    /** The field to display the value. */
    protected SimplePanel m_textboxpanel = new SimplePanel();
    /** The popup to choose the color. */
    protected CmsPopup m_popup = new CmsPopup();

    /** The color value.*/
    protected String m_colorValue = "transparent";

    /** The x-coords of the popup. */
    protected int m_xcoordspopup;

    /** The y-coords of the popup. */
    protected int m_ycoordspopup;

    /***/
    protected HandlerRegistration m_previewHandlerRegistration;

    /**
     * Text area widgets for ADE forms.<p>
     */
    public CmsColorPicker() {

        super();

        initWidget(m_panel);
        m_panel.add(m_textboxpanel);
        m_panel.add(m_colorField);
        m_panel.add(m_error);
        m_textboxpanel.add(m_textboxColorValue);

        m_panel.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().colorPicker());
        m_textboxColorValue.addBlurHandler(new BlurHandler() {

            public void onBlur(BlurEvent event) {

                checkvalue();

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
            m_colorValue = (strValue);
            if (strValue.length() > 0) {
                checkvalue();
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
     * Sets the text in the text area.<p>
     * 
     * @param text the new text
     */
    public void setText(String text) {

        m_textboxColorValue.setText(text);
    }

    /**
     * @param handler
     */
    public void addValueChangeHandler(ValueChangeHandler<String> handler) {

        m_textboxColorValue.addValueChangeHandler(handler);
    }

    /**
     * Validates the inputed color value.
     * @return true if the inputed color value is valid
     */
    protected boolean checkvalue() {

        boolean valid = validateColorValue(m_colorValue);
        if (valid) {
            m_textboxColorValue.setValue(m_colorValue, true);
            m_colorField.getElement().getStyle().setBackgroundColor(m_colorValue);
        }
        return valid;
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

    /**
     * Helper function  to open the popup.<p>
     */
    protected void openPopup() {

        m_popup.setWidth(450);
        m_popup.setHeight(280);
        m_popup.addDialogClose(new Command() {

            public void execute() {

                closePopup();

            }
        });

        if (m_previewHandlerRegistration != null) {
            m_previewHandlerRegistration.removeHandler();
        }
        m_previewHandlerRegistration = Event.addNativePreviewHandler(new CloseEventPreviewHandler());
        m_popup.showRelativeTo(m_colorField);
        if (m_popup.getWidgetCount() == 0) {
            m_popup.setModal(false);

            org.opencms.gwt.client.ui.input.colorpicker.CmsColorSelector picker = new org.opencms.gwt.client.ui.input.colorpicker.CmsColorSelector();
            try {
                picker.setHex(m_textboxColorValue.getText().replace("#", ""));
            } catch (Exception e) {
                // TODO: Auto-generated catch block
                e.printStackTrace();
            }
            m_popup.add(picker);
        }
        m_xcoordspopup = m_popup.getPopupLeft();
        m_ycoordspopup = m_popup.getPopupTop();

    }

    /**
     * Close the popup and store the color value in the colorvalue field.<p>
     * 
     */
    protected void closePopup() {

        m_previewHandlerRegistration.removeHandler();
        m_previewHandlerRegistration = null;
        org.opencms.gwt.client.ui.input.colorpicker.CmsColorSelector picker = (org.opencms.gwt.client.ui.input.colorpicker.CmsColorSelector)m_popup.getWidget(0);
        m_colorValue = "#" + picker.getHexColor();
        if (checkvalue()) {
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
                case Event.ONMOUSEMOVE:
                    break;
                case Event.ONMOUSEUP:

                    int x_coord = nativeEvent.getClientX();
                    int y_coord = (nativeEvent.getClientY() + Window.getScrollTop());

                    if (((x_coord > (m_xcoordspopup + 450)) || (x_coord < (m_xcoordspopup)))
                        || ((y_coord > ((m_ycoordspopup + 280))) || (y_coord < ((m_ycoordspopup))))) {
                        closePopup();
                    }
                    break;
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

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        // TODO: Auto-generated method stub

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
     * Returns the color value textboxpanel.<p>
     * 
     * @return the color value textboxpanel
     */
    public SimplePanel getTextboxPanel() {

        return m_textboxpanel;
    }

    /**
     * Returns the color value textbox.<p>
     * 
     * @return the color value textbox
     * */
    public TextBox getColorValueBox() {

        return m_textboxColorValue;
    }
}

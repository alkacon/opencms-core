/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/datebox/Attic/CmsDateBox.java,v $
 * Date   : $Date: 2010/10/25 12:32:05 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.input.datebox;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.input.CmsErrorWidget;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.Date;
import java.util.Map;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.Popup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * A text box that shows a date time picker widget when the user clicks on it.
 * 
 * @version $Revision: 1.9 $
 * 
 * @author Ruediger Kurz
 */
public class CmsDateBox extends Composite
implements HasValue<Date>, HasDoubleClickHandlers, I_CmsFormWidget, I_CmsHasInit {

    /** The ui-binder interface for this composite. */
    interface I_CmsDateBoxUiBinder extends UiBinder<FlowPanel, CmsDateBox> {
        // GWT interface, nothing to do here
    }

    /**
     * This inner Class implements the handlers for the date box widget.<p>
     * 
     * @version $Revision: 1.9 $
     * 
     * @author Ruediger Kurz
     * 
     * @see {@link ClickHandler}, {@link BlurHandler}, {@link KeyPressHandler}
     */
    private class DateBoxHandler implements ClickHandler, BlurHandler, KeyPressHandler {

        /** The main handler for this UI. */
        private CmsDateBoxHandler m_handler;

        /**
         * The public constructor.<p>
         * 
         * @param handler the main handler for the UI
         */
        public DateBoxHandler(CmsDateBoxHandler handler) {

            m_handler = handler;
        }

        /**
         * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
         */
        public void onBlur(BlurEvent event) {

            m_handler.onDateBoxBlur(event);

        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            m_handler.onDateBoxClick(event);

        }

        /**
         * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
         */
        public void onKeyPress(KeyPressEvent event) {

            m_handler.onDateBoxKeyPress(event);

        }
    }

    /**
     * This inner Class implements the listeners for the date box.<p>
     * 
     * @version $Revision: 1.9 $
     * 
     * @author Ruediger Kurz
     */
    private class DateBoxListener implements Listener<ComponentEvent> {

        /** The main handler for this UI. */
        private CmsDateBoxHandler m_handler;

        /**
         * The public constructor.<p>
         * 
         * @param handler the main handler for the UI
         */
        public DateBoxListener(CmsDateBoxHandler handler) {

            m_handler = handler;
        }

        /**
         * @see com.extjs.gxt.ui.client.event.Listener#handleEvent(com.extjs.gxt.ui.client.event.BaseEvent)
         */
        public void handleEvent(ComponentEvent be) {

            m_handler.onPopupClose();

        }
    }

    /**
     * This inner Class implements the handlers for the date box widget.<p>
     * 
     * @version $Revision: 1.9 $
     * 
     * @author Ruediger Kurz
     * 
     * @see {@link ClickHandler}, {@link ValueChangeHandler}, {@link BlurHandler}, {@link KeyPressHandler}
     */
    private class DateTimePickerHandler implements ClickHandler, BlurHandler, KeyPressHandler {

        /** The main handler for this UI. */
        private CmsDateBoxHandler m_handler;

        /**
         * The public constructor.<p>
         * 
         * @param handler the main handler for the UI
         */
        public DateTimePickerHandler(CmsDateBoxHandler handler) {

            m_handler = handler;
        }

        /**
         * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
         */
        public void onBlur(BlurEvent event) {

            m_handler.onTimeBlur(event);
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            m_handler.onAmPmClick(event);
        }

        /**
         * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
         */
        public void onKeyPress(KeyPressEvent event) {

            m_handler.onTimeKeyPressed(event);
        }

    }

    /**
     * This inner Class implements the listeners for the date time picker.<p>
     * 
     * @version $Revision: 1.9 $
     * 
     * @author Ruediger Kurz
     */
    private class DateTimePickerListener implements Listener<ComponentEvent> {

        /** The main handler for this UI. */
        private CmsDateBoxHandler m_handler;

        /**
         * The public constructor.<p>
         * 
         * @param handler the main handler for the UI
         */
        public DateTimePickerListener(CmsDateBoxHandler handler) {

            m_handler = handler;
        }

        /**
         * @see com.extjs.gxt.ui.client.event.Listener#handleEvent(com.extjs.gxt.ui.client.event.BaseEvent)
         */
        public void handleEvent(ComponentEvent be) {

            m_handler.onPickerValueChanged(m_picker.getValue());

        }
    }

    /** The widget type identifier for this widget. */
    public static final String WIDGET_TYPE = "datebox";

    /** The ui-binder instance. */
    private static I_CmsDateBoxUiBinder uiBinder = GWT.create(I_CmsDateBoxUiBinder.class);

    /** The dialog which contains this widget. */
    protected CmsFormDialog m_dialog;

    /** The am radio button. */
    @UiField
    CmsRadioButton m_am;

    /** The input field to show the result of picking a date. */
    @UiField
    CmsTextBox m_box;

    /** The FlowPanel containing the date box and the the time date picker widget. */
    @UiField
    FlowPanel m_dateBoxPanel;

    /** The panel for the date time picker. */
    @UiField
    FlowPanel m_dateTimePanel;

    /** The gwt date picker. */
    @UiField
    DatePicker m_picker;

    /** The pm radio button. */
    @UiField
    CmsRadioButton m_pm;

    /** The text box to input the time. */
    @UiField
    CmsTextBox m_time;

    /** The error label for the time input field. */
    @UiField
    CmsErrorWidget m_timeErr;

    /** The radio button group for am/pm selection. */
    private CmsRadioButtonGroup m_ampmGroup = new CmsRadioButtonGroup();

    /** The listener for the date box. */
    private DateBoxListener m_dateBoxListener;

    /** The parent popup to this dialog if present. */
    private PopupPanel m_parentPopup;

    /** The popup panel to show the the date time picker widget in. */
    //private CmsPopup m_popup = new CmsPopup();
    private Popup m_popup = new Popup();

    /**
     * Create a new date box widget with the date time picker.
     */
    public CmsDateBox() {

        initWidget(uiBinder.createAndBindUi(this));

        CmsDateBoxHandler pickerHandler = new CmsDateBoxHandler(this);

        DateBoxHandler dateBoxHandler = new DateBoxHandler(pickerHandler);
        m_box.addBlurHandler(dateBoxHandler);
        m_box.addClickHandler(dateBoxHandler);
        m_box.addKeyPressHandler(dateBoxHandler);

        m_am.setText(Messages.get().key(Messages.GUI_DATE_AM_0));
        m_am.setGroup(m_ampmGroup);

        m_pm.setText(Messages.get().key(Messages.GUI_DATE_PM_0));
        m_pm.setGroup(m_ampmGroup);

        DateTimePickerHandler dateTimePickerHandler = new DateTimePickerHandler(pickerHandler);
        m_am.addClickHandler(dateTimePickerHandler);
        m_pm.addClickHandler(dateTimePickerHandler);
        m_time.addBlurHandler(dateTimePickerHandler);
        m_time.addKeyPressHandler(dateTimePickerHandler);

        DateTimePickerListener dateTimePickerListener = new DateTimePickerListener(pickerHandler);

        m_picker.addListener(Events.Select, dateTimePickerListener);

        if (!CmsDateConverter.is12HourPresentation()) {
            m_pm.setVisible(false);
            m_am.setVisible(false);
        }

        m_popup.add(m_dateTimePanel);
        m_popup.setShadow(true);

        m_dateBoxListener = new DateBoxListener(pickerHandler);

        m_popup.setAutoHide(true);
        // m_popup.getIgnoreList().add(m_box.getElement());

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

                return new CmsDateBox();
            }
        });
    }

    /**
     * @see com.google.gwt.event.dom.client.HasDoubleClickHandlers#addDoubleClickHandler(com.google.gwt.event.dom.client.DoubleClickHandler)
     */
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {

        return addDomHandler(handler, DoubleClickEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.DATE;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        return getValue();
    }

    /**
     * Returns the value of the date box as String in form of a long.<p>
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        Date value = getValue();
        if (value == null) {
            return null;
        }
        return String.valueOf(getValue().getTime());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public Date getValue() {

        Date date = null;
        try {
            date = CmsDateConverter.toDate(m_box.getText());
            m_box.setErrorMessage(null);
        } catch (Exception e) {
            m_box.setErrorMessage(Messages.get().key(
                Messages.ERR_DATEBOX_INVALID_DATE_FORMAT_1,
                CmsDateConverter.cutSuffix(m_box.getText())));
        }
        return date;
    }

    /**
     * Returns the date value as formated String or an empty String if the date value is null.<p>
     * 
     * @return the date value as formated String
     */
    public String getValueAsFormatedString() {

        return CmsDateConverter.toString(getValue());
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#onOpenDialog(org.opencms.gwt.client.ui.input.form.CmsFormDialog)
     */
    public void onOpenDialog(CmsFormDialog formDialog) {

        m_dialog = formDialog;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        setValue(null);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_box.setEnabled(enabled);

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_box.setErrorMessage(errorMessage);

    }

    /**
     * Expects the value as String in form of a long.<p>
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        if (value != null) {
            try {
                long time = Long.parseLong(value);
                setValue(new Date(time));
            } catch (NumberFormatException e) {
                // if the String value is none long number make the field empty
                setValue(null);
            }
        } else {
            // if the value is <code>null</code> make the field empty
            setValue(null);
        }
    }

    /**
     * Sets the parent popup.<p>
     * 
     * @param parentPopup the parent popup to set
     */
    public void setParentPopup(PopupPanel parentPopup) {

        m_parentPopup = parentPopup;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(Date value) {

        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(Date value, boolean fireEvents) {

        m_box.setText(CmsDateConverter.toString(value));
        if (fireEvents) {
            CmsDateChangeEvent.fireIfNotEqualDates(this, getValue(), value);
        }
    }

    /**
     * Adds the body element to the popup's ignore list.<p>
     */
    protected void addBodyToIgnoreList() {

        m_popup.getIgnoreList().add(RootPanel.getBodyElement());
    }

    /**
     * Returns the current value of the date picker.<p>
     * 
     * @return the value of the date picker
     */
    protected Date getPickerValue() {

        return m_picker.getValue();
    }

    /**
     * Returns the time text field value as string.<p>
     * 
     * @return the time text field value as string
     */
    protected String getTimeText() {

        String timeAsString = m_time.getText().trim();
        if (CmsDateConverter.is12HourPresentation()) {
            if (!(timeAsString.contains(CmsDateConverter.AM) || timeAsString.contains(CmsDateConverter.PM))) {
                if (m_am.isChecked()) {
                    timeAsString = timeAsString + " " + CmsDateConverter.AM;
                } else {
                    timeAsString = timeAsString + " " + CmsDateConverter.PM;
                }
            }
        }
        return timeAsString;
    }

    /**
     * Returns the current Value of the date time picker.<p>
     * 
     * @return the current Value of the date time picker
     */
    protected Date getValueFromDateTimePicker() {

        Date date = getPickerValue();
        String time = getTimeText();
        return CmsDateConverter.getDateWithTime(date, time);
    }

    /**
     * Hides the Popup.<p>
     */
    protected void hidePopup() {

        // before hiding the date picker remove the date box popup from the auto hide partners of the parent popup
        if (m_parentPopup != null) {
            m_parentPopup.removeAutoHidePartner(m_popup.getElement());
        }
        m_popup.hide();
    }

    /**
     * Returns <code>true</code> if the date picker is currently showed <code>false</code> otherwise.<p> 
     * 
     * @return true if date picker is currently showing, false if not
     */
    protected boolean isDatePickerShowing() {

        return m_popup.isVisible();
    }

    /**
     * Removes the body element from the popup's ignore list.<p>
     */
    protected void removeBodyFromIgnoreList() {

        m_popup.getIgnoreList().remove(RootPanel.getBodyElement());
    }

    /**
     * Sets the value of the date picker.<p>
     * 
     * @param date the value to set
     * @param supressEvent signals whether the value changed event should be suppressed or not
     */
    protected void setPickerValue(Date date, boolean supressEvent) {

        if (date == null) {
            date = new Date();
        }
        m_picker.setValue(date, supressEvent);
        m_time.setText(CmsDateConverter.cutSuffix(CmsDateConverter.getTime(date)).trim());
        if (CmsDateConverter.isAm(date)) {
            m_ampmGroup.selectButton(m_am);
        } else {
            m_ampmGroup.selectButton(m_pm);
        }
    }

    /**
     * Sets an error of the time text field.<p>
     * 
     * @param errMsg the error message to set
     */
    protected void setTimeError(String errMsg) {

        m_timeErr.setText(errMsg);
    }

    /**
     * Shows the date picker popup.<p>
     */
    protected void showPopup() {

        m_popup.show(m_box.getElement(), "bl");
        m_popup.addListener(Events.Close, m_dateBoxListener);

        // after showing the date picker add the date box popup as auto hide partner to the parent popup
        if (m_parentPopup != null) {
            m_parentPopup.addAutoHidePartner(m_popup.getElement());
        }
    }

    /**
     * Synchronizes the popup e.g. for shadow repaint.<p> 
     */
    protected void syncPopup() {

        m_popup.sync(true);
    }
}

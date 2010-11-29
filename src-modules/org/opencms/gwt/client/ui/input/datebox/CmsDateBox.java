/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/datebox/Attic/CmsDateBox.java,v $
 * Date   : $Date: 2010/11/29 08:29:19 $
 * Version: $Revision: 1.14 $
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
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.input.CmsErrorWidget;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
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
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;

/**
 * A text box that shows a date time picker widget when the user clicks on it.
 * 
 * @version $Revision: 1.14 $
 * 
 * @author Ruediger Kurz
 */
public class CmsDateBox extends Composite implements HasValue<Date>, I_CmsFormWidget, I_CmsHasInit {

    /**
     * This inner class implements the handler for the date box widget.<p>
     * 
     * @version $Revision: 1.14 $
     * 
     * @author Ruediger Kurz
     */
    protected class CmsDateBoxHandler implements ClickHandler, BlurHandler, KeyPressHandler {

        /**
         * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
         */
        public void onBlur(BlurEvent event) {

            UIObject source = (UIObject)event.getSource();
            if (m_box.getElement().isOrHasChild(source.getElement())) {
                onDateBoxBlur();
            } else if (m_time.getElement().isOrHasChild(source.getElement())) {
                onTimeBlur();
            }
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            if (event.getSource() == m_box) {
                onDateBoxClick();
            } else if (event.getSource() == m_time) {
                onTimeClick();
            } else if ((event.getSource() == m_am) || (event.getSource() == m_pm)) {
                onAmPmClick();
            }
        }

        /**
         * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
         */
        public void onKeyPress(KeyPressEvent event) {

            if (event.getSource() == m_box) {
                onDateBoxKeyPress(event);
            } else if (event.getSource() == m_time) {
                onTimeKeyPressed(event);
            }
        }
    }

    /** The ui-binder interface for this composite. */
    interface I_CmsDateBoxUiBinder extends UiBinder<FlowPanel, CmsDateBox> {
        // GWT interface, nothing to do here
    }

    /** The widget type identifier for this widget. */
    public static final String WIDGET_TYPE = "datebox";

    /** The ui-binder instance. */
    private static I_CmsDateBoxUiBinder uiBinder = GWT.create(I_CmsDateBoxUiBinder.class);

    /** The am radio button. */
    @UiField
    protected CmsRadioButton m_am;

    /** The radio button group for am/pm selection. */
    protected CmsRadioButtonGroup m_ampmGroup;

    /** The auto hide parent. */
    protected I_CmsAutoHider m_autoHideParent;

    /** The input field to show the result of picking a date. */
    @UiField
    protected CmsTextBox m_box;

    /** The panel for the date time picker. */
    @UiField
    protected FlowPanel m_dateTimePanel;

    /** The gwt date picker. */
    @UiField
    protected DatePicker m_picker;

    /** The pm radio button. */
    @UiField
    protected CmsRadioButton m_pm;

    /** The text box to input the time. */
    @UiField
    protected CmsTextBox m_time;

    /** The error label for the time input field. */
    @UiField
    protected CmsErrorWidget m_timeErr;

    /** Signals whether the time field is valid or not. */
    private boolean m_isValidTime;

    /** The old value for fire event decision. */
    private Date m_oldValue;

    /** The popup panel to show the the date time picker widget in. */
    //private CmsPopup m_popup = new CmsPopup();
    private Popup m_popup;

    /**
     * Create a new date box widget with the date time picker.
     */
    public CmsDateBox() {

        initWidget(uiBinder.createAndBindUi(this));

        m_popup = new Popup();
        m_ampmGroup = new CmsRadioButtonGroup();

        m_am.setText(Messages.get().key(Messages.GUI_DATE_AM_0));
        m_am.setGroup(m_ampmGroup);
        m_pm.setText(Messages.get().key(Messages.GUI_DATE_PM_0));
        m_pm.setGroup(m_ampmGroup);

        if (!CmsDateConverter.is12HourPresentation()) {
            m_pm.setVisible(false);
            m_am.setVisible(false);
        }

        m_picker.addListener(Events.Select, new Listener<ComponentEvent>() {

            /**
             * @see com.extjs.gxt.ui.client.event.Listener#handleEvent(com.extjs.gxt.ui.client.event.BaseEvent)
             */
            public void handleEvent(ComponentEvent be) {

                onPickerValueChanged();
            }
        });

        CmsDateBoxHandler dateBoxHandler = new CmsDateBoxHandler();
        m_box.addBlurHandler(dateBoxHandler);
        m_box.addClickHandler(dateBoxHandler);
        m_box.addKeyPressHandler(dateBoxHandler);
        m_am.addClickHandler(dateBoxHandler);
        m_pm.addClickHandler(dateBoxHandler);
        m_time.addClickHandler(dateBoxHandler);
        m_time.addBlurHandler(dateBoxHandler);
        m_time.addKeyPressHandler(dateBoxHandler);

        m_popup.add(m_dateTimePanel);
        m_popup.setShadow(true);
        m_popup.setAutoHide(true);
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
            setErrorMessage(null);
        } catch (Exception e) {
            setErrorMessage(Messages.get().key(
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
     * Updates the date box when the user has clicked on the time field.<p> 
     */
    public void onTimeClick() {

        updateFromPicker();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        setValue(null);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        m_autoHideParent = autoHideParent;
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
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(Date value) {

        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(Date value, boolean fireEvents) {

        if (fireEvents) {
            CmsDateChangeEvent.fireIfNotEqualDates(this, getValue(), value);
            m_oldValue = value;
        }
        m_box.setText(CmsDateConverter.toString(value));
    }

    /**
     * Updates the updates the close behavior and sets the value of the date box to the value from the picker.<p>
     */
    protected void executeTimeAction() {

        if (!CmsDateConverter.validateTime(getTimeText())) {
            updateCloseBehavior(false);
        } else {
            updateCloseBehavior(true);
            updateFromPicker();
        }
    }

    /**
     * If the am or pm radio button is clicked update the date box from the date time picker.<p>
     */
    protected void onAmPmClick() {

        updateFromPicker();
    }

    /**
     * The date box on blur action.<p>
     * 
     * If the date box loses the focus the date time picker should be updated from the date box value.<p>
     */
    protected void onDateBoxBlur() {

        if (!m_popup.isVisible()) {
            updateFromTextBox();
        } else {
            updateCloseBehavior(true);
        }
    }

    /**
     * The date box on click action.<p>
     * 
     * If the date box is clicked the time date picker should be shown.<p>
     */
    protected void onDateBoxClick() {

        if (!m_popup.isVisible()) {
            showPopup();
        }
    }

    /**
     * The date box on key down action.<p>
     * <ul>
     * <li>If enter or tab is pressed in the date box the date time 
     * picker should be updated with the value from the date box.</li>
     * 
     * <li>If the escape key is pressed the picker should be hided.</li>
     * 
     * <li>If the up key is pressed the value should be taken from the date box.</li>
     * 
     * <li>If the down key is pressed the picker should be hided.</li>
     * </ul>
     *  
     * @param event the key down event
     */
    protected void onDateBoxKeyPress(KeyPressEvent event) {

        switch (event.getNativeEvent().getKeyCode()) {
            case KeyCodes.KEY_ENTER:
            case KeyCodes.KEY_TAB:
            case KeyCodes.KEY_ESCAPE:
            case KeyCodes.KEY_UP:
                updateFromTextBox();
                hidePopup();
                break;
            case KeyCodes.KEY_DOWN:
                showPopup();
                break;
            default:
                hidePopup();
        }

    }

    /**
     * If the value of the picker changes, the value of the date time picker should be updated.<p> 
     */
    protected void onPickerValueChanged() {

        updateFromPicker();
    }

    /**
     * If the time field loses the focus the entered time should be checked.<p>
     */
    protected void onTimeBlur() {

        checkTime(getTimeText());
    }

    /**
     * If the user presses enter in the time field the value of the 
     * picker should be updated and the the popup should be closed.<p>
     * 
     * In any other case the popup should be prevented to being closed.<p>
     * 
     * @param event the key pressed event
     */
    protected void onTimeKeyPressed(KeyPressEvent event) {

        switch (event.getCharCode()) {
            case KeyCodes.KEY_ENTER:
                updateFromPicker();
                hidePopup();
                break;
            default:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    public void execute() {

                        executeTimeAction();
                    }
                });
                break;
        }
    }

    /**
     * Validates the time and prints out an error message if the time format is incorrect.<p>
     * 
     * @param time the time String to check 
     */
    private void checkTime(String time) {

        if (!CmsDateConverter.validateTime(time)) {
            m_timeErr.setText(Messages.get().key(
                Messages.ERR_DATEBOX_INVALID_TIME_FORMAT_1,
                CmsDateConverter.cutSuffix(time)));
            updateCloseBehavior(false);
            m_popup.sync(true);
        } else {
            m_timeErr.setText(null);
            updateCloseBehavior(true);
            m_popup.sync(true);
        }
    }

    /**
     * Returns the time text field value as string.<p>
     * 
     * @return the time text field value as string
     */
    private String getTimeText() {

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
     * Hides the date time popup.<p>
     */
    private void hidePopup() {

        if (CmsDateConverter.validateTime(getTimeText())) {
            // before hiding the date picker remove the date box popup from the auto hide partners of the parent popup
            if (m_autoHideParent != null) {
                m_autoHideParent.removeAutoHidePartner(m_popup.getElement());
            }
            m_popup.hide();
        }
    }

    /**
     * Sets the value of the date picker.<p>
     * 
     * @param date the value to set
     * @param supressEvent signals whether the value changed event should be suppressed or not
     */
    private void setPickerValue(Date date, boolean supressEvent) {

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
     * Shows the date picker popup.<p>
     */
    private void showPopup() {

        updateFromTextBox();
        setErrorMessage(null);
        m_popup.show(m_box.getElement(), "bl");
        // after showing the date picker add the date box popup as auto hide partner to the parent popup
        if (m_autoHideParent != null) {
            m_autoHideParent.addAutoHidePartner(m_popup.getElement());
        }
    }

    /**
     * Sets the valid time flag.<p>
     * 
     * Only if the "current time valid state" differs from the "stored time valid state"
     * the value of the flag is changed.<p>
     * 
     * @param valid the "current time valid state"
     */
    private void updateCloseBehavior(boolean valid) {

        if (!m_isValidTime && valid) {
            m_isValidTime = true;
            m_popup.getIgnoreList().remove(RootPanel.getBodyElement());
            m_autoHideParent.removeAutoHidePartner(RootPanel.getBodyElement());
        } else if (m_isValidTime && !valid) {
            m_isValidTime = false;
            m_popup.getIgnoreList().add(RootPanel.getBodyElement());
            m_autoHideParent.addAutoHidePartner(RootPanel.getBodyElement());
        }
    }

    /**
     * Sets the value of the date box.<p>
     * 
     * @param date the new date
     */
    private void updateFromPicker() {

        Date date = m_picker.getValue();
        String timeAsString = getTimeText();
        checkTime(timeAsString);
        date = CmsDateConverter.getDateWithTime(date, timeAsString);
        setValue(date);
        CmsDateChangeEvent.fireIfNotEqualDates(this, m_oldValue, date);
        m_oldValue = date;
    }

    /**
     * Updates the picker if the user manually modified the date of the text box.<p>
     */
    private void updateFromTextBox() {

        Date date = getValue();
        setPickerValue(date, true);
        m_timeErr.setText(null);
        CmsDateChangeEvent.fireIfNotEqualDates(this, m_oldValue, getValue());
        m_oldValue = date;
    }
}

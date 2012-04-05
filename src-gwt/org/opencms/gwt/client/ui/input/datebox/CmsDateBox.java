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

package org.opencms.gwt.client.ui.input.datebox;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.Date;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * A text box that shows a date time picker widget when the user clicks on it.
 */
public class CmsDateBox extends Composite implements HasValue<Date>, I_CmsFormWidget, I_CmsHasInit, HasKeyPressHandlers {

    /**
     * This inner class implements the handler for the date box widget.<p>
     */
    protected class CmsDateBoxHandler
    implements ClickHandler, FocusHandler, BlurHandler, KeyPressHandler, ValueChangeHandler<Date>,
    CloseHandler<PopupPanel> {

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
         * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
         */
        public void onClose(CloseEvent<PopupPanel> event) {

            m_box.setPreventShowError(false);

        }

        /**
         * @see com.google.gwt.event.dom.client.FocusHandler#onFocus(com.google.gwt.event.dom.client.FocusEvent)
         */
        public void onFocus(FocusEvent event) {

            UIObject source = (UIObject)event.getSource();
            if (m_time.getElement().isOrHasChild(source.getElement())) {
                onFocusTimeBox();
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

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<Date> event) {

            onPickerValueChanged();
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

    /** The initial date shown, when the date picker is opened and no date was set before. */
    private Date m_initialDate;

    /** Signals whether the date box is valid or not. */
    private boolean m_isValidDateBox;

    /** Signals whether the time field is valid or not. */
    private boolean m_isValidTime;

    /** The old value for fire event decision. */
    private Date m_oldValue;

    /** The popup panel to show the the date time picker widget in. */
    private CmsPopup m_popup;

    /**
     * The event preview handler.<p>
     * 
     * Blurs the time box if the user clicks outside of it.<p>
     */
    private NativePreviewHandler m_previewHandler = new NativePreviewHandler() {

        /**
         * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
         */
        public void onPreviewNativeEvent(NativePreviewEvent event) {

            previewNativeEvent(event);
        }
    };

    /** Stores the preview handler. */
    private HandlerRegistration m_previewRegistration;

    /** The date used for enable and disable the text box. */
    private Date m_tmpValue;

    /**
     * Create a new date box widget with the date time picker.
     */
    public CmsDateBox() {

        initWidget(uiBinder.createAndBindUi(this));

        m_popup = new CmsPopup();
        m_ampmGroup = new CmsRadioButtonGroup();

        m_am.setText(Messages.get().key(Messages.GUI_DATE_AM_0));
        m_am.setGroup(m_ampmGroup);
        m_pm.setText(Messages.get().key(Messages.GUI_DATE_PM_0));
        m_pm.setGroup(m_ampmGroup);

        if (!CmsDateConverter.is12HourPresentation()) {
            m_pm.setVisible(false);
            m_am.setVisible(false);
        }

        CmsDateBoxHandler dateBoxHandler = new CmsDateBoxHandler();
        m_picker.addValueChangeHandler(dateBoxHandler);
        m_box.addBlurHandler(dateBoxHandler);
        m_box.addClickHandler(dateBoxHandler);
        m_box.addKeyPressHandler(dateBoxHandler);
        m_am.addClickHandler(dateBoxHandler);
        m_pm.addClickHandler(dateBoxHandler);
        m_time.addClickHandler(dateBoxHandler);
        m_time.addBlurHandler(dateBoxHandler);
        m_time.addKeyPressHandler(dateBoxHandler);
        m_time.addFocusHandler(dateBoxHandler);

        m_popup.add(m_dateTimePanel);
        m_popup.setWidth(0);
        m_popup.setModal(true);
        m_popup.removePadding();
        m_popup.setBackgroundColor(I_CmsLayoutBundle.INSTANCE.constants().css().backgroundColorDialog());
        m_popup.addCloseHandler(dateBoxHandler);
        m_popup.addAutoHidePartner(m_box.getElement());
        m_popup.setAutoHideEnabled(true);
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
     * @see com.google.gwt.event.dom.client.HasKeyPressHandlers#addKeyPressHandler(com.google.gwt.event.dom.client.KeyPressHandler)
     */
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {

        return m_box.addHandler(handler, KeyPressEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
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
        if (isEnabled()) {
            try {
                date = CmsDateConverter.toDate(m_box.getText());
                setErrorMessage(null);
            } catch (Exception e) {
                setErrorMessage(Messages.get().key(Messages.ERR_DATEBOX_INVALID_DATE_FORMAT_0));
            }
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
     * Returns <code>true</code> if the box and the time input fields don't have any errors.<p>
     * 
     * @return <code>true</code> if the box and the time input fields don't have any errors
     */
    public boolean hasErrors() {

        if (m_box.hasError() || m_time.hasError()) {
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_box.isEnabled();
    }

    /**
     * Checks if the String in the date box input field is a valid date format.<p>
     * 
     * @return <code>true</code> if the String in the date box input field is a valid date format
     */
    public boolean isValideDateBox() {

        try {
            CmsDateConverter.toDate(m_box.getText());
            return true;
        } catch (Exception e) {
            return false;
        }
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

        if (!enabled) {
            m_tmpValue = getValue();
            m_box.setFormValueAsString(Messages.get().key(Messages.GUI_INPUT_NOT_USED_0));
        } else {
            setValue(m_tmpValue);
        }
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
     * Sets the initial date shown, when the date picker is opened and no date was set before.<p>
     * 
     * @param initialDate the initial date
     */
    public void setInitialDate(Date initialDate) {

        m_initialDate = initialDate;
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

        m_tmpValue = value;
        if (fireEvents) {
            fireChange(getValue(), value);
            m_oldValue = value;
        }
        m_box.setFormValueAsString(CmsDateConverter.toString(value));
    }

    /**
     * Updates the updates the close behavior and sets the value of the date box to the value from the picker.<p>
     */
    protected void executeTimeAction() {

        if (isValidTime()) {
            updateFromPicker();
        }
        updateCloseBehavior();
    }

    /**
     * Fires the value change event if needed.<p>
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void fireChange(Date oldValue, Date newValue) {

        ValueChangeEvent.<Date> fireIfNotEqual(this, oldValue, CalendarUtil.copyDate(newValue));
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

        if (!m_popup.isShowing()) {
            updateFromTextBox(false);
        }
        updateCloseBehavior();
    }

    /**
     * The date box on click action.<p>
     * 
     * If the date box is clicked the time date picker should be shown.<p>
     */
    protected void onDateBoxClick() {

        if (!m_popup.isShowing()) {
            showPopup();
        }
    }

    /**
     * The date box on key down action.<p>
     * <ul>
     * <li>If enter or tab is pressed in the date box the date time 
     * picker should be updated with the value from the date box.</li>
     * <li>If the escape key is pressed the picker should be hided.</li>
     * <li>If the up key is pressed the value should be taken from the date box.</li>
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
                updateFromTextBox(false);
                hidePopup();
                break;
            case KeyCodes.KEY_DOWN:
                showPopup();
                break;
            default:
                hidePopup();
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    public void execute() {

                        updateCloseBehavior();
                        if (isValideDateBox()) {
                            setErrorMessage(null);
                        }
                    }
                });
                break;
        }
    }

    /**
     * Adds the preview handler.<p>
     */
    protected void onFocusTimeBox() {

        m_previewRegistration = Event.addNativePreviewHandler(m_previewHandler);
    }

    /**
     * If the value of the picker changes, the value of the date time picker should be updated.<p> 
     */
    protected void onPickerValueChanged() {

        setErrorMessage(null);
        updateFromPicker();
    }

    /**
     * If the time field loses the focus the entered time should be checked.<p>
     */
    protected void onTimeBlur() {

        if (m_previewRegistration != null) {
            m_previewRegistration.removeHandler();
        }
        checkTime();
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
     * Blurs the time box if the user clicks outside of it.<p>
     *  
     * @param event the native preview event
     */
    protected void previewNativeEvent(NativePreviewEvent event) {

        Event nativeEvent = Event.as(event.getNativeEvent());
        if ((nativeEvent.getTypeInt() == Event.ONCLICK)) {
            EventTarget target = nativeEvent.getEventTarget();
            if (!Element.is(target)) {
                return;
            }
            Element element = Element.as(target);
            if (!m_time.getElement().isOrHasChild(element)) {
                m_time.blur();
            }
        }
    }

    /**
     * Updates the auto hide partner from the parent widget.<p>
     * 
     * If there is any invalid user input the parent widget should not be closed automatically.<p>
     */
    protected void updateCloseBehavior() {

        if (isEnabled()) {
            if (!m_isValidTime && isValidTime()) {
                m_isValidTime = true;
                m_popup.setAutoHideEnabled(true);
            } else if (m_isValidTime && !isValidTime()) {
                m_isValidTime = false;
                m_popup.setAutoHideEnabled(false);
            }

            if (!m_isValidDateBox && isValideDateBox()) {
                m_isValidDateBox = true;
                if (m_autoHideParent != null) {
                    m_autoHideParent.removeAutoHidePartner(RootPanel.getBodyElement().getParentElement());
                }
            } else if (m_isValidDateBox && !isValideDateBox()) {
                m_isValidDateBox = false;
                if (m_autoHideParent != null) {
                    m_autoHideParent.addAutoHidePartner(RootPanel.getBodyElement().getParentElement());
                }
            }
        }
    }

    /**
     * Validates the time and prints out an error message if the time format is incorrect.<p>
     */
    private void checkTime() {

        if (!isValidTime()) {
            m_time.setErrorMessageWidth((m_popup.getOffsetWidth() - 32) + Unit.PX.toString());
            m_time.setErrorMessage(Messages.get().key(Messages.ERR_DATEBOX_INVALID_TIME_FORMAT_0));
        } else if (isValidTime()) {
            m_time.setErrorMessage(null);
        }
        updateCloseBehavior();
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
                m_autoHideParent.removeAutoHidePartner(getElement());
            }
            m_popup.hide();
        }
    }

    /**
     * Checks if the String in the time input field is a valid time format.<p>
     * 
     * @return <code>true</code> if the String in the time input field is a valid time format
     */
    private boolean isValidTime() {

        return CmsDateConverter.validateTime(getTimeText());
    }

    /**
     * Sets the value of the date picker.<p>
     * 
     * @param date the value to set
     * @param fireEvents signals whether the value changed event should be fired or not
     */
    private void setPickerValue(Date date, boolean fireEvents) {

        if (date == null) {
            date = new Date();
        }
        m_picker.setValue(date, fireEvents);
        m_picker.setCurrentMonth(date);
        m_time.setFormValueAsString(CmsDateConverter.cutSuffix(CmsDateConverter.getTime(date)).trim());
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

        updateFromTextBox(true);
        m_box.setPreventShowError(true);
        m_popup.showRelativeTo(m_box);
    }

    /**
     * Sets the value of the date box.<p>
     * 
     * @param date the new date
     */
    private void updateFromPicker() {

        checkTime();
        Date date = m_picker.getValue();
        String timeAsString = getTimeText();
        date = CmsDateConverter.getDateWithTime(date, timeAsString);
        setValue(date);
        setErrorMessage(null);
        fireChange(m_oldValue, date);
        m_oldValue = date;
    }

    /**
     * Updates the picker if the user manually modified the date of the text box.<p>
     * 
     * @param initial <code>true</code> if the datebox is being initially opened 
     */
    private void updateFromTextBox(boolean initial) {

        Date date = getValue();
        if (initial && (date == null)) {
            date = m_initialDate;
        }
        setPickerValue(date, false);
        m_time.setErrorMessage(null);
        fireChange(m_oldValue, getValue());
        m_oldValue = date;
    }
}

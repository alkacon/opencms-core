/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/datebox/Attic/CmsDateBoxHandler.java,v $
 * Date   : $Date: 2010/10/25 12:32:05 $
 * Version: $Revision: 1.8 $
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

import org.opencms.gwt.client.Messages;

import java.util.Date;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

/**
 * This singleton implements the handler for the whole date time piker.<p>
 * 
 * @version 0.1
 * 
 * @author Ruediger Kurz
 */
public final class CmsDateBoxHandler {

    /** The date box. */
    private CmsDateBox m_dateBox;

    /** Signals whether the time field is valid or not. */
    private boolean m_isValidTime;

    /** The old value for fire event decision. */
    private Date m_oldValue;

    /**
     * Constructor for initializing this class with the date box.<p>
     * 
     * @param dateBox the date box for this handler
     */
    protected CmsDateBoxHandler(CmsDateBox dateBox) {

        m_dateBox = dateBox;
        m_isValidTime = true;
    }

    /**
     * Updates the updates the close behavior and sets the value of the date box to the value from the picker.<p>
     */
    protected void executeTimeAction() {

        if (!CmsDateConverter.validateTime(m_dateBox.getTimeText())) {
            updateCloseBehavior(false);
        } else {
            updateCloseBehavior(true);
            updateFromDateTimePicker(m_dateBox.getValueFromDateTimePicker());
        }
    }

    /**
     * If the am or pm radio button is clicked update the date box from the date time picker.<p>
     * 
     * @param event the click event
     */
    protected void onAmPmClick(ClickEvent event) {

        updateFromDateTimePicker(m_dateBox.getValueFromDateTimePicker());
    }

    /**
     * The date box on blur action.<p>
     * 
     * If the date box loses the focus the date time picker should be updated from the date box value.<p>
     * 
     * @param event the blur event
     */
    protected void onDateBoxBlur(BlurEvent event) {

        if (!m_dateBox.isDatePickerShowing()) {
            updateFromTextBox();
        } else {
            updateCloseBehavior(true);
        }
    }

    /**
     * The date box on click action.<p>
     * 
     * If the date box is clicked the time date picker should be shown.<p>
     * 
     * @param event the on click event
     */
    protected void onDateBoxClick(ClickEvent event) {

        if (!m_dateBox.isDatePickerShowing()) {
            showDatePicker();
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
                hideDatePicker();
                break;
            case KeyCodes.KEY_DOWN:
                showDatePicker();
                break;
            default:
                hideDatePicker();
        }

    }

    /**
     * If the value of the picker changes, the value of the date time picker should be updated.<p> 
     * 
     * @param value the new date selected from the picker
     */
    protected void onPickerValueChanged(Date value) {

        updateFromDateTimePicker(value);
    }

    /**
     * If the popup closes the new value is fixed and a event should be fired.<p>
     */
    protected void onPopupClose() {

        CmsDateChangeEvent.fireIfNotEqualDates(m_dateBox, m_oldValue, m_dateBox.getValue());
    }

    /**
     * If the time field loses the focus the entered time should be checked.<p>
     * 
     * @param event the blur event
     */
    protected void onTimeBlur(BlurEvent event) {

        checkTime(m_dateBox.getTimeText());
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
                updateFromDateTimePicker(m_dateBox.getPickerValue());
                hideDatePicker();
                break;
            default:
                DeferredCommand.addCommand(new Command() {

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
            m_dateBox.setTimeError(Messages.get().key(
                Messages.ERR_DATEBOX_INVALID_TIME_FORMAT_1,
                CmsDateConverter.cutSuffix(time)));
            updateCloseBehavior(false);
            m_dateBox.syncPopup();
        } else {
            m_dateBox.setTimeError(null);
            updateCloseBehavior(true);
            m_dateBox.syncPopup();
        }
    }

    /**
     * Hides the date picker.<p>
     */
    private void hideDatePicker() {

        if (CmsDateConverter.validateTime(m_dateBox.getTimeText())) {
            m_dateBox.hidePopup();
        }
    }

    /**
     * Parses the current date box's value and shows that date.
     */
    private void showDatePicker() {

        m_dateBox.setTimeError(null);

        Date date = m_dateBox.getPickerValue();
        if (date == null) {
            Date tmpDate = new Date();
            m_dateBox.setPickerValue(tmpDate, false);
        }
        updateFromTextBox();
        m_oldValue = m_dateBox.getValue();
        m_dateBox.showPopup();
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
            m_dateBox.removeBodyFromIgnoreList();
        } else if (m_isValidTime && !valid) {
            m_isValidTime = false;
            m_dateBox.addBodyToIgnoreList();
        }
    }

    /**
     * Updates the picker if the user manually modified the date of the text box.<p>
     */
    private void updateFromTextBox() {

        Date date = m_dateBox.getValue();
        m_dateBox.setPickerValue(date, true);
        m_dateBox.setTimeError(null);
        CmsDateChangeEvent.fireIfNotEqualDates(m_dateBox, m_oldValue, m_dateBox.getValue());
    }

    /**
     * Sets the value of the date box.<p>
     * 
     * @param date the new date
     */
    private void updateFromDateTimePicker(Date date) {

        String timeAsString = m_dateBox.getTimeText();
        checkTime(timeAsString);
        date = CmsDateConverter.getDateWithTime(date, timeAsString);
        m_dateBox.setValue(date);
        CmsDateChangeEvent.fireIfNotEqualDates(m_dateBox, m_oldValue, m_dateBox.getValue());
    }
}

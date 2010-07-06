/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/datebox/Attic/CmsPickerHandler.java,v $
 * Date   : $Date: 2010/07/06 12:08:04 $
 * Version: $Revision: 1.1 $
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

import java.util.Date;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * This singleton implements the handler for the whole date time piker.<p>
 * 
 * @version 0.1
 * 
 * @author Ruediger Kurz
 */
public final class CmsPickerHandler {

    /** The date box. */
    private CmsDateBox m_dateBox;

    private Date m_oldValue;

    /**
     * A private constructor for initializing this class with the date box and the date time picker.<p>
     * 
     * @param dateBox the date box for this handler
     */
    public CmsPickerHandler(CmsDateBox dateBox) {

        m_dateBox = dateBox;
    }

    /**
     * If the am or pm radio button is clicked update the date box from the date time picker.<p>
     * 
     * @param event the click event
     */
    public void onAmPmClick(ClickEvent event) {

        updateFromDateTimePicker(m_dateBox.getPicker().getValue());
    }

    /**
     * The date box on blur action.<p>
     * 
     * If the date box loses the focus the date time picker should be updated from the date box value.<p>
     * 
     * @param event the blur event
     */
    public void onDateBoxBlur(BlurEvent event) {

        if (!isDatePickerShowing()) {
            updateDateFromTextBox();
        }
    }

    /**
     * The date box on click action.<p>
     * 
     * If the date box is clicked the time date picker should be shown.<p>
     * 
     * @param event the on click event
     */
    public void onDateBoxClick(ClickEvent event) {

        if (!isDatePickerShowing()) {
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
    public void onDateBoxKeyPress(KeyPressEvent event) {

        switch (event.getNativeEvent().getKeyCode()) {
            case KeyCodes.KEY_ENTER:
            case KeyCodes.KEY_TAB:
            case KeyCodes.KEY_ESCAPE:
            case KeyCodes.KEY_UP:
                updateDateFromTextBox();
                hideDatePicker();
                break;
            case KeyCodes.KEY_DOWN:
                showDatePicker();
                break;
            default:
                break;
        }

    }

    /**
     * If the value of the picker changes, the value of the date time picker should be updated.<p> 
     * 
     * @param event the value change event
     */
    public void onPickerValueChanged(ValueChangeEvent<Date> event) {

        updateFromDateTimePicker(event.getValue());
    }

    /**
     * If the popup closes the new value is fixed and a event should be fired.<p>
     * 
     * @param event the close event
     */
    public void onPopupClose(CloseEvent<PopupPanel> event) {

        CmsDateChangeEvent.fireIfNotEqualDates(m_dateBox, m_oldValue, m_dateBox.getValue());
    }

    /**
     * If the time field loses the focus the entered time should be checked.<p>
     * 
     * @param event the blur event
     */
    public void onTimeBlur(BlurEvent event) {

        checkTime(getCurrentTimeFieldValue());

    }

    /**
     * If the user presses enter in the time field the value of the 
     * picker should be updated and the the popup should be closed.<p>
     * 
     * In any other case the popup should be prevented to being closed.<p>
     * 
     * @param event the key pressed event
     */
    public void onTimeKeyPressed(KeyPressEvent event) {

        switch (event.getCharCode()) {
            case KeyCodes.KEY_ENTER:
                updateFromDateTimePicker(m_dateBox.getPicker().getValue());
                hideDatePicker();
                break;
            default:
                DeferredCommand.addCommand(new Command() {

                    public void execute() {

                        preventDatePickerClose();
                    }
                });

                break;
        }
    }

    /**
     * Prevents the popup to close.<p>
     */
    protected void preventDatePickerClose() {

        String timeAsString = getCurrentTimeFieldValue();
        if (!CmsDateConverter.validateTime(timeAsString)) {
            m_dateBox.getPopup().setAutoHideEnabled(false);
        } else {
            m_dateBox.getBox().setText(CmsDateConverter.toString(getValueFromDateTimePicker()));
            m_dateBox.getPopup().setAutoHideEnabled(true);
            m_dateBox.getTimeErr().setText(null);
        }
    }

    /**
     * Validates the time and prints out an error message if the time format is incorrect.<p>
     * 
     * @param time the time String to check 
     */
    private void checkTime(String time) {

        if (!CmsDateConverter.validateTime(time)) {
            m_dateBox.getTimeErr().setText(
                "Illegal time format entered: "
                    + CmsDateConverter.cutSuffix(time)
                    + ". Please insert the time in the format \"HH:mm\"");
            m_dateBox.getPopup().setAutoHideEnabled(false);
        } else {
            m_dateBox.getTimeErr().setText(null);
            m_dateBox.getBox().setErrorMessage(null);
            m_dateBox.getPopup().showRelativeTo(m_dateBox);
        }
    }

    /**
     * Returns the current time from the time field together with the am pm information.<p>
     * 
     * @return the current time from the time field together with the am pm information
     */
    private String getCurrentTimeFieldValue() {

        String timeAsString = m_dateBox.getTime().getText().trim();
        if (CmsDateConverter.is12HourPresentation()) {
            timeAsString = getTimeWithAmPmInfo(timeAsString);
        }
        return timeAsString;

    }

    /**
     * Looks up the ui field for the am pm selection and appends this info on a given String with a space before.<p>
     * 
     * @param time the time to append the am pm info
     * 
     * @return the time with the am pm info
     */
    private String getTimeWithAmPmInfo(String time) {

        if (!(time.contains(CmsDateConverter.AM) || time.contains(CmsDateConverter.PM))) {
            if (m_dateBox.getAm().isChecked()) {
                time = time + " " + CmsDateConverter.AM;
            } else {
                time = time + " " + CmsDateConverter.PM;
            }
        }
        return time;
    }

    /**
     * Returns the current Value of the date time picker.<p>
     * 
     * @return the current Value of the date time picker
     */
    private Date getValueFromDateTimePicker() {

        Date date = m_dateBox.getPicker().getValue();
        String time = getCurrentTimeFieldValue();
        return CmsDateConverter.getDateWithTime(date, time);
    }

    /**
     * Hides the date picker.<p>
     */
    private void hideDatePicker() {

        if (CmsDateConverter.validateTime(getCurrentTimeFieldValue())) {
            m_dateBox.getPopup().hide();
        }
    }

    /**
     * @return true if date picker is currently showing, false if not
     */
    private boolean isDatePickerShowing() {

        return m_dateBox.getPopup().isShowing();
    }

    /**
     * Parses the date.<p>
     * 
     * @return the date
     */
    private Date parseToDate() {

        String dateAsString = m_dateBox.getBox().getText().trim();
        Date date = null;
        try {
            date = CmsDateConverter.toDate(dateAsString);
            m_dateBox.getBox().setErrorMessage(null);
            if (isDatePickerShowing()) {
                m_dateBox.getPopup().showRelativeTo(m_dateBox);
            }
        } catch (Exception e) {
            m_dateBox.getBox().setErrorMessage("Error: wrong date format.");
        }
        return date;
    }

    /**
     * Sets the am or pm widget to the value of the given date.<p>
     * 
     * If the given date object is <code>null</code> the current date is used.<p>
     * 
     * @param date which should be used to set the am or pm information into the time widget
     */
    private void setAmPmFromBox(Date date) {

        if (date == null) {
            date = new Date();
        }
        if (CmsDateConverter.isAm(date)) {
            m_dateBox.getAmpmGroup().selectButton(m_dateBox.getAm());
        } else {
            m_dateBox.getAmpmGroup().selectButton(m_dateBox.getPm());
        }
    }

    /**
     * Sets the time widget to the value of the given date.<p>
     * 
     * If the given date object is <code>null</code> the current date is used.<p> 
     * 
     * @param date which should be used to set the time into the time widget
     */
    private void setTime(Date date) {

        if (date == null) {
            date = new Date();
        }
        String time = CmsDateConverter.getTime(date);
        m_dateBox.getTime().setText(CmsDateConverter.cutSuffix(time).trim());
    }

    /**
     * Parses the current date box's value and shows that date.
     */
    private void showDatePicker() {

        m_dateBox.getTimeErr().setText(null);

        Date date = m_dateBox.getPicker().getValue();
        if (date == null) {
            Date tmpDate = new Date();
            m_dateBox.getPicker().setCurrentMonth(tmpDate);
            m_dateBox.getPicker().setValue(tmpDate, false);
        }
        updateDateFromTextBox();
        m_oldValue = m_dateBox.getValue();
        m_dateBox.getPopup().showRelativeTo(m_dateBox);
    }

    /**
     * Updates the picker if the user manually modified the date of the text box.<p>
     */
    private void updateDateFromTextBox() {

        Date parsedDate = parseToDate();
        if (parsedDate != null) {
            m_dateBox.getPicker().setValue(parsedDate);
            m_dateBox.getPicker().setCurrentMonth(parsedDate);
        }
        setTime(parsedDate);
        setAmPmFromBox(parsedDate);
        m_dateBox.getTimeErr().setText(null);
        m_dateBox.getPopup().setAutoHideEnabled(true);
    }

    /**
     * Sets the value of the date box.<p>
     * 
     * @param date the new date
     */
    private void updateFromDateTimePicker(Date date) {

        if (date == null) {
            Date tmpDate = new Date();
            m_dateBox.getPicker().setCurrentMonth(tmpDate);
            m_dateBox.getPicker().setValue(tmpDate, false);
        }

        String timeAsString = getCurrentTimeFieldValue();
        checkTime(timeAsString);
        date = CmsDateConverter.getDateWithTime(date, timeAsString);
        m_dateBox.getBox().setText(CmsDateConverter.toString(date));
    }
}

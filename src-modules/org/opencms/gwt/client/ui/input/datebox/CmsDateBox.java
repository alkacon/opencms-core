/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/datebox/Attic/CmsDateBox.java,v $
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

import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.input.CmsErrorWidget;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsTextBox;

import java.util.Date;

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
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * A text box that shows a date time picker widget when the user clicks on it.
 * 
 * @version 0.1
 * 
 * @author Ruediger Kurz
 */
public class CmsDateBox extends Composite implements HasValue<Date>, HasDoubleClickHandlers {

    /** The ui-binder interface for this composite. */
    interface CmsDateBoxUiBinder extends UiBinder<FlowPanel, CmsDateBox> {
        // GWT interface, nothing to do here
    }

    /**
     * This inner Class implements the handlers for the date box widget.<p>
     * 
     * @version 0.1
     * 
     * @author Ruediger Kurz
     * 
     * @see {@link ClickHandler}, {@link BlurHandler}, {@link KeyPressHandler}, {@link CloseHandler}
     */
    private class DateBoxHandler implements ClickHandler, BlurHandler, KeyPressHandler, CloseHandler<PopupPanel> {

        /** The main handler for this UI. */
        private CmsPickerHandler m_handler;

        /**
         * The public constructor.<p>
         * 
         * @param handler the main handler for the UI
         */
        public DateBoxHandler(CmsPickerHandler handler) {

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
         * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
         */
        public void onClose(CloseEvent<PopupPanel> event) {

            m_handler.onPopupClose(event);

        }

        /**
         * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
         */
        public void onKeyPress(KeyPressEvent event) {

            m_handler.onDateBoxKeyPress(event);

        }
    }

    /**
     * This inner Class implements the handlers for the date box widget.<p>
     * 
     * @version 0.1
     * 
     * @author Ruediger Kurz
     * 
     * @see {@link ClickHandler}, {@link ValueChangeHandler}, {@link BlurHandler}, {@link KeyPressHandler}
     */
    private class DateTimePickerHandler implements ClickHandler, ValueChangeHandler<Date>, BlurHandler, KeyPressHandler {

        /** The main handler for this UI. */
        private CmsPickerHandler m_handler;

        /**
         * The public constructor.<p>
         * 
         * @param handler the main handler for the UI
         */
        public DateTimePickerHandler(CmsPickerHandler handler) {

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

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<Date> event) {

            m_handler.onPickerValueChanged(event);
        }
    }

    /** The ui-binder instance. */
    private static CmsDateBoxUiBinder uiBinder = GWT.create(CmsDateBoxUiBinder.class);

    /** The am radio button. */
    @UiField
    CmsRadioButton m_am;

    /** The input field to show the result of picking a date. */
    @UiField
    CmsTextBox m_box;

    /** The FlowPanel containing the date box and the the time date picker widget. */
    @UiField
    FlowPanel m_dateBoxPanel;

    /** The gwt date picker. */
    @UiField
    DatePicker m_picker;

    /** The pm radio button. */
    @UiField
    CmsRadioButton m_pm;

    /** The text box to input the time. */
    @UiField
    CmsTextBox m_time;

    /** The panel for the date time picker. */
    @UiField
    FlowPanel m_dateTimePanel;

    /** The error label for the time input field. */
    @UiField
    CmsErrorWidget m_timeErr;

    /** The popup panel to show the the date time picker widget in. */
    private CmsPopup m_popup = new CmsPopup();

    /** The radio button group for am/pm selection. */
    private CmsRadioButtonGroup m_ampmGroup = new CmsRadioButtonGroup();

    /**
     * Create a new date box widget with the date time picker.
     */
    public CmsDateBox() {

        initWidget(uiBinder.createAndBindUi(this));

        CmsPickerHandler pickerHandler = new CmsPickerHandler(this);

        DateBoxHandler dateBoxHandler = new DateBoxHandler(pickerHandler);
        m_box.addBlurHandler(dateBoxHandler);
        m_box.addClickHandler(dateBoxHandler);
        m_box.addKeyPressHandler(dateBoxHandler);

        m_am.setText("am");
        m_am.setGroup(m_ampmGroup);

        m_pm.setText("pm");
        m_pm.setGroup(m_ampmGroup);

        DateTimePickerHandler dateTimePickerHandler = new DateTimePickerHandler(pickerHandler);
        m_am.addClickHandler(dateTimePickerHandler);
        m_pm.addClickHandler(dateTimePickerHandler);
        m_time.addBlurHandler(dateTimePickerHandler);
        m_time.addKeyPressHandler(dateTimePickerHandler);
        m_picker.addValueChangeHandler(dateTimePickerHandler);

        if (!CmsDateConverter.is12HourPresentation()) {
            m_pm.setVisible(false);
            m_am.setVisible(false);
        }

        m_popup.add(m_dateTimePanel);
        m_popup.setWidth("100px");
        m_popup.addCloseHandler(dateBoxHandler);
        m_popup.setModal(false);
        m_popup.setText("Sellect date " + m_popup.isModal());
        m_popup.addAutoHidePartner(m_box.getElement());

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
     * Returns the am radio button.<p>
     *
     * @return the am radio button
     */
    public CmsRadioButton getAm() {

        return m_am;
    }

    /**
     * Returns the date picker.<p>
     *
     * @return the date picker
     */
    public DatePicker getPicker() {

        return m_picker;
    }

    /**
     * Returns the pm radio button.<p>
     *
     * @return the pm radio button
     */
    public CmsRadioButton getPm() {

        return m_pm;
    }

    /**
     * Returns the time text box widget.<p>
     *
     * @return the time text box widget
     */
    public CmsTextBox getTime() {

        return m_time;

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public Date getValue() {

        Date date = null;
        try {
            date = CmsDateConverter.toDate(m_box.getText());
        } catch (Exception e) {
            // should never happen
        }
        return date;
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
     * Returns the box.<p>
     *
     * @return the box
     */
    public CmsTextBox getBox() {

        return m_box;
    }

    /**
     * Returns the popup.<p>
     *
     * @return the popup
     */
    public CmsPopup getPopup() {

        return m_popup;
    }

    /**
     * Returns the group.<p>
     *
     * @return the group
     */
    public CmsRadioButtonGroup getAmpmGroup() {

        return m_ampmGroup;
    }

    /**
     * Returns the timeErr.<p>
     *
     * @return the timeErr
     */
    public CmsErrorWidget getTimeErr() {

        return m_timeErr;
    }
}

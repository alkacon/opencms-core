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

package org.opencms.acacia.client.widgets.serialdate;

import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsSelectBox;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The monthly pattern panel.<p>
 * */
public class CmsPatternPanelMonthly extends Composite implements HasValueChangeHandlers<String> {

    /** The UI binder interface. */
    interface I_CmsPatternPanelMonthlyUiBinder extends UiBinder<HTMLPanel, CmsPatternPanelMonthly> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsPatternPanelMonthlyUiBinder uiBinder = GWT.create(I_CmsPatternPanelMonthlyUiBinder.class);

    /** The select box for the day selection. */
    @UiField
    CmsSelectBox m_atDay;

    /** The select box for the month selection. */
    @UiField
    TextBox m_atMonth;
    /** The select box for the nummeric selection. */
    @UiField
    CmsSelectBox m_atWeek;

    /** The day month radio button. */
    @UiField(provided = true)
    CmsRadioButton m_dayMonthRadioButton;
    /** The text box for the date input. */
    @UiField
    TextBox m_everyDay;
    /** The select box for the month selection. */
    @UiField
    TextBox m_everyMonth;

    /** The days label. */
    @UiField
    Element m_labelDays;

    /** The every label. */
    @UiField
    Element m_labelEvery;

    /** The month label. */
    @UiField
    Element m_labelMonth;

    /** The months label. */
    @UiField
    Element m_labelMonths;

    /** The week day month radio button. */
    @UiField(provided = true)
    CmsRadioButton m_weekDayMonthRadioButton;

    /** Group off all radio buttons. */
    private CmsRadioButtonGroup m_group = new CmsRadioButtonGroup();

    /** The value change handler. */
    private ValueChangeHandler<String> m_handler;

    /**
     * Default constructor to create the panel.<p>
     * @param labels JSON of all needed labels
     */
    public CmsPatternPanelMonthly() {

        m_dayMonthRadioButton = new CmsRadioButton(
            "sel1",
            Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_MONTHDAY_AT_0));
        m_dayMonthRadioButton.setGroup(m_group);
        m_dayMonthRadioButton.setChecked(true);
        m_weekDayMonthRadioButton = new CmsRadioButton(
            "sel2",
            Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_WEEKDAY_AT_0));
        m_weekDayMonthRadioButton.setGroup(m_group);
        m_group.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireValueChange();
            }
        });
        initWidget(uiBinder.createAndBindUi(this));

        m_everyDay.setText("1");
        m_labelDays.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_MONTHDAY_DAY_EVERY_0));
        m_everyMonth.setText("1");
        m_labelMonths.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_MONTH_0));

        m_labelEvery.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_WEEKDAY_EVERY_0));
        m_labelMonth.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_MONTH_0));
        m_atMonth.setText("1");
        initSelectBoxes();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        m_handler = handler;
        m_atWeek.addValueChangeHandler(m_handler);
        m_atDay.addValueChangeHandler(m_handler);
        m_atMonth.addValueChangeHandler(m_handler);
        m_everyDay.addValueChangeHandler(m_handler);
        m_everyMonth.addValueChangeHandler(m_handler);
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     */
    public void fireValueChange() {

        ValueChangeEvent.fire(this, getWeekDays());
    }

    /**
     * Returns the day of month.<p>
     * @return the day of month
     *
     * */
    public String getDayOfMonth() {

        if (m_group.getSelectedButton() == m_dayMonthRadioButton) {
            return m_everyDay.getText();
        } else {
            return m_atWeek.getFormValueAsString();
        }
    }

    /**
     * Returns the interval.<p>
     * @return the interval
     * */
    public String getInterval() {

        if (m_group.getSelectedButton() == m_dayMonthRadioButton) {
            return m_everyMonth.getText();
        } else {
            return m_atMonth.getText();
        }
    }

    /**
     * Returns the week day.<p>
     * @return the week day
     * */
    public String getWeekDays() {

        if (m_group.getSelectedButton() == m_dayMonthRadioButton) {
            return "-1";
        } else {
            return m_atDay.getFormValueAsString();
        }
    }

    /**
     * Handles the at month key press event.<p>
     *
     * @param event the key press event
     */
    @UiHandler("m_atMonth")
    public void onAtMonthKeyPress(KeyPressEvent event) {

        fireValueChange();

    }

    /**
     * Handles the every day key press event.<p>
     *
     * @param event the key press event
     */
    @UiHandler("m_everyDay")
    public void onEveryDayKeyPress(KeyPressEvent event) {

        fireValueChange();

    }

    /**
     * Handles the every month key press event.<p>
     *
     * @param event the key press event
     */
    @UiHandler("m_everyMonth")
    public void onEveryMonthKeyPress(KeyPressEvent event) {

        fireValueChange();

    }

    /**
     * Sets the day of month.<p>
     * @param dayOfMonthStr the day of month
     */
    public void setDayOfMonth(int dayOfMonthStr) {

        if (m_group.getSelectedButton() == m_dayMonthRadioButton) {
            m_everyDay.setText(dayOfMonthStr + "");
        } else {
            m_atWeek.selectValue(dayOfMonthStr + "");
        }

    }

    /**
     * Sets the interval.<p>
     * @param intervalStr the interval
     */
    public void setInterval(String intervalStr) {

        if (m_group.getSelectedButton() == m_dayMonthRadioButton) {
            m_everyMonth.setText(intervalStr);
        } else {
            m_atMonth.setText(intervalStr);
        }

    }

    /**
     * Sets the week day.<p>
     *
     * @param weekDayStr the week day
     * */
    public void setWeekDay(int weekDayStr) {

        if (weekDayStr == -1) {
            m_group.selectButton(m_dayMonthRadioButton);
        } else {
            m_group.selectButton(m_weekDayMonthRadioButton);
            m_atDay.selectValue(weekDayStr + "");
        }

    }

    /**
     * Creates the 'at' selection view.<p>
     * */
    private void initSelectBoxes() {

        m_atWeek.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_atWeek.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_atWeek.addOption("1", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_1_0));
        m_atWeek.addOption("2", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_2_0));
        m_atWeek.addOption("3", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_3_0));
        m_atWeek.addOption("4", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_4_0));
        m_atWeek.addOption("5", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_5_0));
        m_atWeek.setWidth("80px");

        m_atDay.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_atDay.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_atDay.addOption("1", Messages.get().key(Messages.GUI_SERIALDATE_DAY_SUNDAY_0));
        m_atDay.addOption("2", Messages.get().key(Messages.GUI_SERIALDATE_DAY_MONDAY_0));
        m_atDay.addOption("3", Messages.get().key(Messages.GUI_SERIALDATE_DAY_TUESDAY_0));
        m_atDay.addOption("4", Messages.get().key(Messages.GUI_SERIALDATE_DAY_WEDNESDAY_0));
        m_atDay.addOption("5", Messages.get().key(Messages.GUI_SERIALDATE_DAY_THURSDAY_0));
        m_atDay.addOption("6", Messages.get().key(Messages.GUI_SERIALDATE_DAY_FRIDAY_0));
        m_atDay.addOption("7", Messages.get().key(Messages.GUI_SERIALDATE_DAY_SATURDAY_0));
        m_atDay.setWidth("100px");
    }

}

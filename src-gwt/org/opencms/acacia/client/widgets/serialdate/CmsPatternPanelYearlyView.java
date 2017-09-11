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

import org.opencms.acacia.shared.I_CmsSerialDateValue.Month;
import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsSelectBox;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * The yearly pattern panel.<p>
 * */
public class CmsPatternPanelYearlyView extends Composite implements I_CmsSerialDatePatternView {

    /** The UI binder interface. */
    interface I_CmsPatternPanelYearlyUiBinder extends UiBinder<HTMLPanel, CmsPatternPanelYearlyView> {
        // nothing to do
    }

    /** Name of the "every x. day in month" radio button. */
    private static final String DAYS_RADIOBUTTON = "everyday";

    /** Name of the "at weeks" radio button. */
    private static final String WEEKS_RADIOBUTTON = "everyweek";

    /** The UI binder instance. */
    private static I_CmsPatternPanelYearlyUiBinder uiBinder = GWT.create(I_CmsPatternPanelYearlyUiBinder.class);

    /* UI elements for "every x. day of month in year" */

    /** The text box for the date input. */
    @UiField
    CmsFocusAwareTextBox m_everyDay;
    /** The select box for the month selection. */
    @UiField
    CmsSelectBox m_everyMonth;

    /** The every radio button. */
    @UiField(provided = true)
    CmsRadioButton m_everyRadioButton;

    /* UI elements for "every x. weekday in month in year" */
    /** The select box for the day selection. */
    @UiField
    CmsSelectBox m_atDay;

    /** The select box for the month selection. */
    @UiField
    CmsSelectBox m_atMonth;
    /** The select box for the numeric selection. */
    @UiField
    CmsSelectBox m_atNumber;

    /** The at radio button. */
    @UiField(provided = true)
    CmsRadioButton m_atRadioButton;

    /** The in label. */
    @UiField
    Label m_labelIn;

    /** Group off all radio buttons. */
    private CmsRadioButtonGroup m_group;

    /** The model to read the data from. */
    private final I_CmsObservableSerialDateValue m_model;

    /** The controller to handle changes. */
    final CmsPatternPanelYearlyController m_controller;

    /** Flag, indicating if change actions should not be triggered. */
    private boolean m_triggerChangeActions = true;

    /**
     * Default constructor to create the panel.<p>
     * @param controller the controller that handles value changes.
     * @param model the model that provides the values.
     */
    public CmsPatternPanelYearlyView(CmsPatternPanelYearlyController controller, I_CmsObservableSerialDateValue model) {
        m_controller = controller;
        m_model = model;
        m_model.registerValueChangeObserver(this);

        m_group = new CmsRadioButtonGroup();
        m_everyRadioButton = new CmsRadioButton(
            DAYS_RADIOBUTTON,
            Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_EVERY_0));
        m_everyRadioButton.setGroup(m_group);
        m_everyRadioButton.setChecked(true);
        m_atRadioButton = new CmsRadioButton(
            WEEKS_RADIOBUTTON,
            Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_AT_0));
        m_atRadioButton.setGroup(m_group);
        m_group.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                if (handleChange()) {
                    m_controller.setPatternScheme(event.getValue().equals(m_atRadioButton.getName()));
                }
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        m_everyDay.setFormValueAsString("1");
        m_labelIn.setText(Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_IN_0));
        initSelectBoxes();
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDateValueChangeObserver#onValueChange()
     */
    public void onValueChange() {

        if (m_model.getPatternType().equals(PatternType.YEARLY)) {
            m_triggerChangeActions = false;
            if (null == m_model.getWeekDay()) {
                m_group.selectButton(m_everyRadioButton);
                if (!m_everyDay.isFocused()) {
                    m_everyDay.setFormValueAsString(String.valueOf(m_model.getDayOfMonth()));
                }
                m_everyMonth.selectValue(String.valueOf(m_model.getMonth()));
                m_atDay.selectValue(String.valueOf(m_controller.getPatternDefaultValues().getWeekDay()));
                m_atMonth.selectValue(String.valueOf(m_controller.getPatternDefaultValues().getMonth()));
                m_atNumber.selectValue(String.valueOf(m_controller.getPatternDefaultValues().getWeekOfMonth()));
            } else {
                m_group.selectButton(m_atRadioButton);
                m_atDay.selectValue(String.valueOf(m_model.getWeekDay()));
                m_atMonth.selectValue(String.valueOf(m_model.getMonth()));
                m_atNumber.selectValue(String.valueOf(m_model.getWeekOfMonth()));
                m_everyDay.setFormValueAsString(
                    Integer.toString(m_controller.getPatternDefaultValues().getDayOfMonth()));
                m_everyMonth.selectValue(String.valueOf(m_controller.getPatternDefaultValues().getMonth()));
            }
            m_triggerChangeActions = true;
        }

    }

    /**
     * Returns a flag, indicating if change actions should be triggered.
     * @return a flag, indicating if change actions should be triggered.
     */
    boolean handleChange() {

        return m_triggerChangeActions;
    }

    /**
     * Handler for focus event of an "at" input field.
     * @param event the focus event.
     */
    @UiHandler({"m_atMonth", "m_atDay", "m_atNumber"})
    void onAtFocus(FocusEvent event) {

        if (handleChange()) {
            m_group.selectButton(m_atRadioButton);
        }
    }

    /**
     * Handle day of month change.
     * @param event the value change event.
     */
    @UiHandler("m_everyDay")
    void onDayOfMonthChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setDayOfMonth(event.getValue());
        }
    }

    /**
     * Handler for focus event of an "every" input field.
     * @param event the focus event.
     */
    @UiHandler({"m_everyMonth", "m_everyDay"})
    void onEveryFocus(FocusEvent event) {

        if (handleChange()) {
            m_group.selectButton(m_everyRadioButton);
        }
    }

    /**
     * Handler for month changes.
     * @param event change event.
     */
    @UiHandler({"m_atMonth", "m_everyMonth"})
    void onMonthChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setMonth(event.getValue());
        }
    }

    /**
     * Handler for week day changes.
     * @param event the change event.
     */
    @UiHandler("m_atDay")
    void onWeekDayChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setWeekDay(event.getValue());
        }
    }

    /**
     * Handler for week of month changes.
     * @param event the change event.
     */
    @UiHandler("m_atNumber")
    void onWeekOfMonthChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setWeekOfMonth(event.getValue());
        }
    }

    /**
     * Creates the 'at' selection view.<p>
     * */
    private void initSelectBoxes() {

        m_atNumber.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_atNumber.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_atNumber.setWidth("80px");
        m_atNumber.addOption(
            WeekOfMonth.FIRST.toString(),
            Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_1_0));
        m_atNumber.addOption(
            WeekOfMonth.SECOND.toString(),
            Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_2_0));
        m_atNumber.addOption(
            WeekOfMonth.THIRD.toString(),
            Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_3_0));
        m_atNumber.addOption(
            WeekOfMonth.FOURTH.toString(),
            Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_4_0));
        m_atNumber.addOption(
            WeekOfMonth.LAST.toString(),
            Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_5_0));
        m_atDay.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_atDay.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_atDay.setWidth("100px");
        m_atDay.addOption(WeekDay.SUNDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_SUNDAY_0));
        m_atDay.addOption(WeekDay.MONDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_MONDAY_0));
        m_atDay.addOption(WeekDay.TUESDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_TUESDAY_0));
        m_atDay.addOption(WeekDay.WEDNESDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_WEDNESDAY_0));
        m_atDay.addOption(WeekDay.THURSDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_THURSDAY_0));
        m_atDay.addOption(WeekDay.FRIDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_FRIDAY_0));
        m_atDay.addOption(WeekDay.SATURDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_SATURDAY_0));

        m_atMonth.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_everyMonth.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_atMonth.setWidth("100px");
        m_atMonth.addOption(Month.JANUARY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JAN_0));
        m_atMonth.addOption(Month.FEBRUARY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_FEB_0));
        m_atMonth.addOption(Month.MARCH.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_MAR_0));
        m_atMonth.addOption(Month.APRIL.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_APR_0));
        m_atMonth.addOption(Month.MAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_MAY_0));
        m_atMonth.addOption(Month.JUNE.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JUN_0));
        m_atMonth.addOption(Month.JULY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JUL_0));
        m_atMonth.addOption(Month.AUGUST.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_AUG_0));
        m_atMonth.addOption(Month.SEPTEMBER.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_SEP_0));
        m_atMonth.addOption(Month.OCTOBER.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_OCT_0));
        m_atMonth.addOption(Month.NOVEMBER.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_NOV_0));
        m_atMonth.addOption(Month.DECEMBER.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_DEC_0));

        m_everyMonth.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_everyMonth.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_everyMonth.setWidth("100px");
        m_everyMonth.addOption(Month.JANUARY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JAN_0));
        m_everyMonth.addOption(Month.FEBRUARY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_FEB_0));
        m_everyMonth.addOption(Month.MARCH.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_MAR_0));
        m_everyMonth.addOption(Month.APRIL.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_APR_0));
        m_everyMonth.addOption(Month.MAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_MAY_0));
        m_everyMonth.addOption(Month.JUNE.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JUN_0));
        m_everyMonth.addOption(Month.JULY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JUL_0));
        m_everyMonth.addOption(Month.AUGUST.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_AUG_0));
        m_everyMonth.addOption(Month.SEPTEMBER.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_SEP_0));
        m_everyMonth.addOption(Month.OCTOBER.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_OCT_0));
        m_everyMonth.addOption(Month.NOVEMBER.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_NOV_0));
        m_everyMonth.addOption(Month.DECEMBER.toString(), Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_DEC_0));

    }

}

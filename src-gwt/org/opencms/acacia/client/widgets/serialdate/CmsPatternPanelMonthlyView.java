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

import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsSelectBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * The monthly pattern panel.<p>
 * */
public class CmsPatternPanelMonthlyView extends Composite implements I_CmsSerialDatePatternView {

    /** The UI binder interface. */
    interface I_CmsPatternPanelMonthlyUiBinder extends UiBinder<HTMLPanel, CmsPatternPanelMonthlyView> {
        // nothing to do
    }

    /** Name of the "every x. day in month" radio button. */
    private static final String DAYS_RADIOBUTTON = "everyday";

    /** Name of the "at weeks" radio button. */
    private static final String WEEKS_RADIOBUTTON = "workingday";

    /** The UI binder instance. */
    private static I_CmsPatternPanelMonthlyUiBinder uiBinder = GWT.create(I_CmsPatternPanelMonthlyUiBinder.class);

    /** Checkboxes for the week days. */
    List<CmsCheckBox> m_checkboxes = new ArrayList<CmsCheckBox>(I_CmsSerialDateValue.NUM_OF_WEEKDAYS);

    /* UI elements for "every day". */

    /** The day month radio button. */
    @UiField(provided = true)
    CmsRadioButton m_dayMonthRadioButton;
    /** The text box for the date input. */
    @UiField
    CmsFocusAwareTextBox m_everyDay;
    /** The select box for the month selection. */
    @UiField
    CmsFocusAwareTextBox m_everyMonth;

    /** The days label. */
    @UiField
    Element m_labelDays;

    /** The every label. */
    @UiField
    Element m_labelEvery;

    /** The months label. */
    @UiField
    Element m_everyLabelMonth;

    /* UI elements for "at week day". */

    /** The week day month radio button. */
    @UiField(provided = true)
    CmsRadioButton m_weekDayMonthRadioButton;

    /** The select box for the numeric selection. */
    @UiField
    FlowPanel m_weekPanel;

    /** The select box for the day selection. */
    @UiField
    CmsSelectBox m_atDay;

    /** The select box for the month selection. */
    @UiField
    CmsFocusAwareTextBox m_atMonth;

    /** The month label. */
    @UiField
    Element m_atLabelMonth;

    /** Group off all radio buttons. */
    CmsRadioButtonGroup m_group = new CmsRadioButtonGroup();

    /** The model to read the data from. */
    private final I_CmsObservableSerialDateValue m_model;
    /** The controller to handle changes. */
    final CmsPatternPanelMonthlyController m_controller;

    /** Flag, indicating if change actions should not be triggered. */
    private boolean m_triggerChangeActions = true;

    /**
     * Default constructor to create the panel.<p>
     * @param controller the controller that handles value changes.
     * @param model the model that provides the values.
     */
    public CmsPatternPanelMonthlyView(
        CmsPatternPanelMonthlyController controller,
        I_CmsObservableSerialDateValue model) {

        m_controller = controller;
        m_model = model;
        m_model.registerValueChangeObserver(this);

        m_dayMonthRadioButton = new CmsRadioButton(
            DAYS_RADIOBUTTON,
            Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_MONTHDAY_AT_0));
        m_dayMonthRadioButton.setGroup(m_group);
        m_dayMonthRadioButton.setChecked(true);
        m_weekDayMonthRadioButton = new CmsRadioButton(
            WEEKS_RADIOBUTTON,
            Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_WEEKDAY_AT_0));
        m_weekDayMonthRadioButton.setGroup(m_group);
        m_group.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                if (handleChange() && (event.getValue() != null)) {
                    m_controller.setPatternScheme(event.getValue().equals(m_weekDayMonthRadioButton.getName()), true);
                }
            }
        });
        initWidget(uiBinder.createAndBindUi(this));

        fillWeekPanel();
        m_everyDay.setFormValueAsString("1");
        m_everyDay.setTriggerChangeOnKeyPress(true);
        m_labelDays.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_MONTHDAY_DAY_EVERY_0));
        m_everyMonth.setFormValueAsString("1");
        m_everyMonth.setTriggerChangeOnKeyPress(true);
        m_everyLabelMonth.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_MONTH_0));

        m_labelEvery.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_WEEKDAY_EVERY_0));
        m_atLabelMonth.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_MONTHLY_MONTH_0));
        m_atMonth.setFormValueAsString("1");
        m_atMonth.setTriggerChangeOnKeyPress(true);
        initSelectBoxes();
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDateValueChangeObserver#onValueChange()
     */
    public void onValueChange() {

        if (m_model.getPatternType().equals(PatternType.MONTHLY)) {
            m_triggerChangeActions = false;
            if (null == m_model.getWeekDay()) {
                m_group.selectButton(m_dayMonthRadioButton);
                if (!m_everyDay.isFocused()) {
                    m_everyDay.setFormValueAsString(String.valueOf(m_model.getDayOfMonth()));
                }
                if (!m_everyMonth.isFocused()) {
                    m_everyMonth.setFormValueAsString(String.valueOf(m_model.getInterval()));
                }
                m_atMonth.setFormValueAsString(Integer.toString(m_controller.getPatternDefaultValues().getInterval()));
                m_atDay.selectValue(String.valueOf(m_controller.getPatternDefaultValues().getWeekDay()));
                checkExactlyTheWeeksCheckBoxes(m_controller.getPatternDefaultValues().getWeeksOfMonth());
            } else {
                m_group.selectButton(m_weekDayMonthRadioButton);
                if (!m_atMonth.isFocused()) {
                    m_atMonth.setFormValueAsString(String.valueOf(m_model.getInterval()));
                }
                m_atDay.selectValue(m_model.getWeekDay().toString());
                checkExactlyTheWeeksCheckBoxes(m_model.getWeeksOfMonth());
                m_everyDay.setFormValueAsString(
                    Integer.toString(m_controller.getPatternDefaultValues().getDayOfMonth()));
                m_everyMonth.setFormValueAsString(
                    Integer.toString(m_controller.getPatternDefaultValues().getInterval()));
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
     * Handles "at" input field focus.
     * @param event the focus event.
     */
    @UiHandler({"m_atDay", "m_atMonth"})
    void onAtFocus(FocusEvent event) {

        if (handleChange()) {
            m_group.selectButton(m_weekDayMonthRadioButton);
        }
    }

    /**
     * Handles the every day change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_everyDay")
    void onEveryDayValueChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setDayOfMonth(event.getValue());
        }

    }

    /**
     * Handles "every" input field focus.
     * @param event the focus event.
     */
    @UiHandler({"m_everyDay", "m_everyMonth"})
    void onEveryFocus(FocusEvent event) {

        if (handleChange()) {
            m_group.selectButton(m_dayMonthRadioButton);
        }
    }

    /**
     * Handles interval changes.<p>
     *
     * @param event the value change event
     */
    @UiHandler({"m_atMonth", "m_everyMonth"})
    void onIntervalValueChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setInterval(event.getValue());
        }

    }

    /**
     * Handles week day changes.
     * @param event the change event.
     */
    @UiHandler("m_atDay")
    void onWeekDayChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setWeekDay(event.getValue());
        }
    }

    /**
     * Creates a check box and adds it to the week panel and the checkboxes.
     * @param internalValue the internal value of the checkbox
     * @param labelMessageKey key for the label of the checkbox
     */
    private void addCheckBox(final String internalValue, String labelMessageKey) {

        CmsCheckBox box = new CmsCheckBox(Messages.get().key(labelMessageKey));
        box.setInternalValue(internalValue);
        box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (handleChange()) {
                    m_controller.weeksChange(internalValue, event.getValue());
                }
            }
        });
        m_weekPanel.add(box);
        m_checkboxes.add(box);

    }

    /**
     * Check exactly the week check-boxes representing the given weeks.
     * @param weeksToCheck the weeks selected.
     */
    private void checkExactlyTheWeeksCheckBoxes(Collection<WeekOfMonth> weeksToCheck) {

        for (CmsCheckBox cb : m_checkboxes) {
            cb.setChecked(weeksToCheck.contains(WeekOfMonth.valueOf(cb.getInternalValue())));
        }
    }

    /**
     * Fills the week panel with checkboxes.
     */
    private void fillWeekPanel() {

        addCheckBox(WeekOfMonth.FIRST.toString(), Messages.GUI_SERIALDATE_WEEKDAYNUMBER_1_0);
        addCheckBox(WeekOfMonth.SECOND.toString(), Messages.GUI_SERIALDATE_WEEKDAYNUMBER_2_0);
        addCheckBox(WeekOfMonth.THIRD.toString(), Messages.GUI_SERIALDATE_WEEKDAYNUMBER_3_0);
        addCheckBox(WeekOfMonth.FOURTH.toString(), Messages.GUI_SERIALDATE_WEEKDAYNUMBER_4_0);
        addCheckBox(WeekOfMonth.LAST.toString(), Messages.GUI_SERIALDATE_WEEKDAYNUMBER_5_0);
    }

    /**
     * Creates the 'at' selection view.<p>
     * */
    private void initSelectBoxes() {

        m_atDay.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_atDay.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_atDay.addOption(WeekDay.SUNDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_SUNDAY_0));
        m_atDay.addOption(WeekDay.MONDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_MONDAY_0));
        m_atDay.addOption(WeekDay.TUESDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_TUESDAY_0));
        m_atDay.addOption(WeekDay.WEDNESDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_WEDNESDAY_0));
        m_atDay.addOption(WeekDay.THURSDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_THURSDAY_0));
        m_atDay.addOption(WeekDay.FRIDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_FRIDAY_0));
        m_atDay.addOption(WeekDay.SATURDAY.toString(), Messages.get().key(Messages.GUI_SERIALDATE_DAY_SATURDAY_0));
        m_atDay.setWidth("100px");
    }

}

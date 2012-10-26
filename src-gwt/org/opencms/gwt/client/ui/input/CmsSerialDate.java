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

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.ui.input.serialdate.CmsPatternPanelDaily;
import org.opencms.gwt.client.ui.input.serialdate.CmsPatternPanelMonthly;
import org.opencms.gwt.client.ui.input.serialdate.CmsPatternPanelWeekly;
import org.opencms.gwt.client.ui.input.serialdate.CmsPatternPanelYearly;
import org.opencms.gwt.client.ui.input.serialdate.I_CmsCalendarSerialDateOptions;
import org.opencms.util.CmsStringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Basic serial date widget.<p>
 * 
 * @since 8.5.0
 * 
 */
public class CmsSerialDate extends Composite implements I_CmsFormWidget, I_CmsHasInit, HasValueChangeHandlers<String> {

    /***/
    private static final String KEY_DAILY = "1";

    /***/
    private static final String KEY_MONTHLY = "3";
    /***/
    private static final String KEY_WEEKLY = "2";
    /***/
    private static final String KEY_YEARLY = "4";

    /** The widget type identifier for this widget. */
    private static final String WIDGET_TYPE = "SerialDate";

    /** Separator for the week days String. */
    public static final char SEPARATOR_WEEKDAYS = ',';

    /** Number of milliseconds per minute. */
    public static final long MILLIS_00_PER_MINUTE = 1000 * 60;

    /** Number of milliseconds per hour. */
    public static final long MILLIS_01_PER_HOUR = MILLIS_00_PER_MINUTE * 60;

    /** Number of milliseconds per day. */
    public static final long MILLIS_02_PER_DAY = MILLIS_01_PER_HOUR * 24;

    /** Number of milliseconds per week. */
    public static final long MILLIS_03_PER_WEEK = MILLIS_02_PER_DAY * 7;

    /** The daily pattern. */
    CmsPatternPanelDaily m_dailyPattern = new CmsPatternPanelDaily();

    /** The weekly pattern. */
    CmsPatternPanelWeekly m_weeklyPattern = new CmsPatternPanelWeekly();

    /** The monthly pattern. */
    CmsPatternPanelMonthly m_monthlyPattern = new CmsPatternPanelMonthly();

    /** The yearly pattern. */
    CmsPatternPanelYearly m_yearlyPattern = new CmsPatternPanelYearly();

    /** The begin datebox. */
    CmsDateBox m_dateboxbegin = new CmsDateBox();

    /** The end datebox. */
    CmsDateBox m_dateboxend = new CmsDateBox();

    /** The end date box. */
    TextBox m_endDate = new TextBox();

    /** The start date box. */
    TextBox m_startDate = new TextBox();

    /** The times text box. */
    TextBox m_times = new TextBox();

    /** The end date. */
    Date m_endDateValue = new Date();

    /** The start date. */
    Date m_startDateValue = new Date();

    /** Array of all radio button. */
    private CmsRadioButton[] m_arrayRadiobox;

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** Value of the radio group duration. */
    private CmsRadioButtonGroup m_groupDuration = new CmsRadioButtonGroup();

    /** Value of the radio group pattern. */
    private CmsRadioButtonGroup m_groupPattern = new CmsRadioButtonGroup();

    /** The lower panel for detail duration information. */
    private Panel m_lowPanel = new FlowPanel();

    /** All radiobottons of the low panel. */
    private CmsRadioButton[] m_lowRadioButton = new CmsRadioButton[3];
    /** The mein panel for the table. */
    private Panel m_panel = new FlowPanel();
    /** The actual active pattern panel. */
    private Panel m_patterPanel = new CmsPatternPanelDaily();
    /** The duratioen selection. */
    CmsSelectBox m_duration;

    /** The serial panel. */
    private Panel m_serialPanel = new FlowPanel();
    /** The root panel containing the other components of this widget. */
    private FlexTable m_table = new FlexTable();
    /** The top Panel for detail time information. */
    private Panel m_topPanel = new FlowPanel();

    /**
     * Category field widgets for ADE forms.<p>
     */
    public CmsSerialDate() {

        super();
        setSelectVaues();
        setTopPanel();
        setLowPanel();
        m_table.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDataTabel());
        m_table.insertRow(0);
        m_table.setWidget(0, 0, m_topPanel);
        m_table.getCellFormatter().getElement(0, 0).setAttribute("colspan", "2");
        m_table.insertRow(1);
        m_table.setWidget(1, 0, m_serialPanel);
        m_table.getCellFormatter().getElement(1, 0).getStyle().setWidth(150, Unit.PX);
        m_table.setWidget(1, 1, m_patterPanel);
        m_patterPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().textAreaBoxPanel());
        for (int i = 0; i < m_arrayRadiobox.length; i++) {
            m_arrayRadiobox[i].addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().radioButtonlabel());
            m_arrayRadiobox[i].addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    changePattern();
                }
            });

            m_serialPanel.add(m_arrayRadiobox[i]);
        }

        m_table.insertRow(2);
        m_table.setWidget(2, 0, m_lowPanel);
        m_table.getCellFormatter().getElement(2, 0).setAttribute("colspan", "2");
        initWidget(m_panel);
        m_panel.add(m_table);
        m_panel.add(m_error);

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

                return new CmsSerialDate();
            }
        });
    }

    /**
     * Returns the int value of the given String or the default value if parsing the String fails.<p>
     * 
     * @param strValue the String to parse
     * @param defaultValue the default value to use if parsing fails
     * @return the int value of the given String
     */
    protected static int getIntValue(String strValue, int defaultValue) {

        int result = defaultValue;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(strValue)) {
            try {
                result = Integer.parseInt(strValue);
            } catch (NumberFormatException e) {
                // no number, use default value
            }
        }
        return result;
    }

    /**
     * Returns the long value of the given String or the default value if parsing the String fails.<p>
     * 
     * @param strValue the String to parse
     * @param defaultValue the default value to use if parsing fails
     * @return the long value of the given String
     */
    protected static long getLongValue(String strValue, long defaultValue) {

        long result = defaultValue;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(strValue)) {
            try {
                result = Long.parseLong(strValue);
            } catch (NumberFormatException e) {
                // no number, use default value
            }
        }
        return result;
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     */
    public void fireValueChange() {

        ValueChangeEvent.fire(this, getFormValueAsString());
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return selectValues();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        //nothing to do

    }

    /** Selects the right ending element*/
    public void selectEnding(int element) {

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        Map<String, String> values = new HashMap<String, String>();
        String[] split = value.split("\\|");
        for (int i = 0; i < split.length; i++) {
            int pars = split[i].indexOf("=");
            String key = split[i].substring(0, pars);
            String val = split[i].substring(pars + 1);
            values.put(key, val);
        }
        setValues(values);

    }

    /**
     * Creates a serial date entry from the given property value.<p>
     * 
     * If no matching serial date could be created, <code>null</code> is returned.<p>
     * 
     * @param values the Map containing the date configuration values
     */
    public void setValues(Map<String, String> values) {

        // first set serial date fields used by all serial types

        // fetch the start date and time

        String startLong = values.get(I_CmsCalendarSerialDateOptions.CONFIG_STARTDATE);
        m_startDateValue = new Date(getLongValue(startLong, 0));
        DateTimeFormat timeformate = DateTimeFormat.getFormat("hh:mm aa");
        m_startDate.setValue(timeformate.format(m_startDateValue));

        m_dateboxbegin.setValue(m_startDateValue);
        // the end date and time (this means the duration of a single entry)

        String endLong = values.get(I_CmsCalendarSerialDateOptions.CONFIG_ENDDATE);
        m_endDateValue = new Date(getLongValue(endLong, 0));
        m_endDate.setValue(timeformate.format(m_endDateValue));

        if (getLongValue(endLong, 0) > getLongValue(startLong, 0)) {
            // duration at least one day, calculate it
            long delta = getLongValue(endLong, 0) - getLongValue(startLong, 0);
            int test = new Long(delta / MILLIS_02_PER_DAY).intValue();
            m_duration.selectValue((test + 1) + "");
        }

        // determine the serial end type
        String endTypeStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_END_TYPE);
        int endType = getIntValue(endTypeStr, I_CmsCalendarSerialDateOptions.END_TYPE_NEVER);
        m_groupDuration.selectButton(m_lowRadioButton[endType - 1]);
        if (endType == I_CmsCalendarSerialDateOptions.END_TYPE_TIMES) {
            // end type: after a number of occurences
            String occurStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_OCCURENCES);
            m_times.setText(occurStr);
        } else if (endType == I_CmsCalendarSerialDateOptions.END_TYPE_DATE) {
            // end type: ends at a specified date
            String endDateStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_SERIAL_ENDDATE);
            long endDate = getLongValue(endDateStr, 0);
            m_dateboxend.setValue(new Date(endDate));

        }

        // now determine the serial date options depending on the serial date type

        String type = values.get(I_CmsCalendarSerialDateOptions.CONFIG_TYPE);
        int entryType = getIntValue(type, 1);
        m_groupPattern.selectButton(m_arrayRadiobox[entryType - 1]);
        changePattern();
        switch (entryType) {
            case I_CmsCalendarSerialDateOptions.TYPE_DAILY:
                // daily series entry, get interval and working days flag
                String intervalStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_INTERVAL);
                String workingDaysStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_EVERY_WORKING_DAY);
                boolean workingDays = Boolean.valueOf(workingDaysStr).booleanValue();
                m_dailyPattern.setInterval(intervalStr);
                if (workingDays) {
                    m_dailyPattern.setSelection(2);
                } else {
                    m_dailyPattern.setSelection(1);
                }

                break;
            case I_CmsCalendarSerialDateOptions.TYPE_WEEKLY:
                // weekly series entry
                intervalStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_INTERVAL);
                String weekDaysStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_WEEKDAYS);
                List<String> weekDaysStrList = CmsStringUtil.splitAsList(weekDaysStr, SEPARATOR_WEEKDAYS, true);
                m_weeklyPattern.setInterval(intervalStr);
                m_weeklyPattern.setWeekDays(weekDaysStrList);
                break;
            case I_CmsCalendarSerialDateOptions.TYPE_MONTHLY:
                // monthly series entry
                intervalStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_INTERVAL);
                String dayOfMonthStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_DAY_OF_MONTH);
                int dayOfMonth = getIntValue(dayOfMonthStr, 1);
                String weekDayStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_WEEKDAYS);
                int weekDay = getIntValue(weekDayStr, -1);
                m_monthlyPattern.setWeekDay(weekDay);
                m_monthlyPattern.setInterval(intervalStr);
                m_monthlyPattern.setDayOfMonth(dayOfMonth);

                break;
            case I_CmsCalendarSerialDateOptions.TYPE_YEARLY:
                // yearly series entry
                dayOfMonthStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_DAY_OF_MONTH);
                dayOfMonth = getIntValue(dayOfMonthStr, 1);
                weekDayStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_WEEKDAYS);
                weekDay = getIntValue(weekDayStr, -1);
                String monthStr = values.get(I_CmsCalendarSerialDateOptions.CONFIG_MONTH);
                int month = getIntValue(monthStr, 0);
                m_yearlyPattern.setWeekDay(weekDay);
                m_yearlyPattern.setDayOfMonth(dayOfMonth);
                m_yearlyPattern.setMonth(month);

                break;
            default:

        }
        selectValues();
    }

    /**
     * Selects the right view for the selected pattern.<p>
     */
    protected void changePattern() {

        if (m_groupPattern.getSelectedButton() != null) {
            String buttonName = m_groupPattern.getSelectedButton().getName();
            m_patterPanel.removeFromParent();
            if (buttonName.equals(KEY_DAILY)) {
                m_patterPanel = m_dailyPattern;
                m_dailyPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

                    public void onValueChange(ValueChangeEvent<String> event) {

                        fireValueChange();

                    }
                });
            }
            if (buttonName.equals(KEY_WEEKLY)) {
                m_patterPanel = m_weeklyPattern;
                m_weeklyPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

                    public void onValueChange(ValueChangeEvent<String> event) {

                        fireValueChange();

                    }

                });

            }
            if (buttonName.equals(KEY_MONTHLY)) {
                m_patterPanel = m_monthlyPattern;
                m_monthlyPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

                    public void onValueChange(ValueChangeEvent<String> event) {

                        fireValueChange();

                    }

                });
            }
            if (buttonName.equals(KEY_YEARLY)) {
                m_patterPanel = m_yearlyPattern;
                m_yearlyPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

                    public void onValueChange(ValueChangeEvent<String> event) {

                        fireValueChange();

                    }

                });
            }
            m_patterPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().textAreaBoxPanel());

            m_table.setWidget(1, 1, m_patterPanel);
            fireValueChange();
        }

    }

    /**
     * Selects all needed information an build the result string.<p>
     * 
     * @return the result string
     * */
    @SuppressWarnings("deprecation")
    private String selectValues() {

        String result = "";
        String type = m_groupPattern.getSelectedButton().getName();
        result += I_CmsCalendarSerialDateOptions.CONFIG_TYPE + "=" + type + "|";
        switch (Integer.parseInt(type)) {
            case (1):
                result += I_CmsCalendarSerialDateOptions.CONFIG_INTERVAL + "=" + m_dailyPattern.getIterval() + "|";
                result += I_CmsCalendarSerialDateOptions.CONFIG_EVERY_WORKING_DAY
                    + "="
                    + m_dailyPattern.getWorkingDay()
                    + "|";
                break;
            case (2):
                result += I_CmsCalendarSerialDateOptions.CONFIG_INTERVAL + "=" + m_weeklyPattern.getInterval() + "|";
                result += I_CmsCalendarSerialDateOptions.CONFIG_WEEKDAYS + "=" + m_weeklyPattern.getWeekDays() + "|";

                break;
            case (3):
                result += I_CmsCalendarSerialDateOptions.CONFIG_INTERVAL + "=" + m_monthlyPattern.getInterval() + "|";
                result += I_CmsCalendarSerialDateOptions.CONFIG_DAY_OF_MONTH
                    + "="
                    + m_monthlyPattern.getDayOfMonth()
                    + "|";
                if (!m_monthlyPattern.getWeekDays().equals("-1")) {
                    result += I_CmsCalendarSerialDateOptions.CONFIG_WEEKDAYS
                        + "="
                        + m_monthlyPattern.getWeekDays()
                        + "|";
                }
                break;
            case (4):
                result += I_CmsCalendarSerialDateOptions.CONFIG_DAY_OF_MONTH
                    + "="
                    + m_yearlyPattern.getDayOfMonth()
                    + "|";
                result += I_CmsCalendarSerialDateOptions.CONFIG_MONTH + "=" + m_yearlyPattern.getMonth() + "|";
                if (!m_yearlyPattern.getWeekDays().equals("-1")) {
                    result += I_CmsCalendarSerialDateOptions.CONFIG_WEEKDAYS
                        + "="
                        + m_yearlyPattern.getWeekDays()
                        + "|";
                }
                break;
            default:
                break;

        }
        DateTimeFormat format = DateTimeFormat.getFormat("hh:mm aa");
        Date startDate = new Date();
        Date endDate = new Date();
        m_startDateValue = m_dateboxbegin.getValue();
        startDate = format.parse(m_startDate.getText());
        m_startDateValue.setHours(startDate.getHours());
        m_startDateValue.setMinutes(startDate.getMinutes());

        endDate = format.parse(m_endDate.getText());
        m_endDateValue.setHours(endDate.getHours());
        m_endDateValue.setMinutes(endDate.getMinutes());

        result += I_CmsCalendarSerialDateOptions.CONFIG_STARTDATE + "=" + m_startDateValue.getTime() + "|";
        result += I_CmsCalendarSerialDateOptions.CONFIG_ENDDATE + "=" + m_endDateValue.getTime() + "|";

        String endtype = m_groupDuration.getSelectedButton().getName();
        switch (Integer.parseInt(endtype)) {
            case (1):
                break;
            case (I_CmsCalendarSerialDateOptions.END_TYPE_TIMES):
                if (!m_times.getText().isEmpty()) {
                    result += I_CmsCalendarSerialDateOptions.CONFIG_OCCURENCES + "=" + m_times.getText();
                }
                break;
            case (I_CmsCalendarSerialDateOptions.END_TYPE_DATE):
                if (!m_dateboxend.getValueAsFormatedString().isEmpty()) {
                    result += I_CmsCalendarSerialDateOptions.CONFIG_SERIAL_ENDDATE
                        + "="
                        + m_dateboxend.getFormValueAsString();
                }
                break;
            default:
                break;

        }

        result += I_CmsCalendarSerialDateOptions.CONFIG_END_TYPE + "=" + endtype;
        return result;
    }

    /**
     * Private function to set all the end selections.<p>
     * */
    private void setLowPanel() {

        FlexTable table = new FlexTable();
        table.insertRow(0);
        FlowPanel cell1 = new FlowPanel();
        cell1.add(new Label("Begin:"));

        cell1.add(m_dateboxbegin);
        m_dateboxbegin.addValueChangeHandler(new ValueChangeHandler<Date>() {

            public void onValueChange(ValueChangeEvent<Date> event) {

                fireValueChange();

            }
        });
        table.setWidget(0, 0, cell1);
        table.getCellFormatter().getElement(0, 0).getStyle().setWidth(200, Unit.PX);

        FlowPanel cell2 = new FlowPanel();
        CmsRadioButton sel1 = new CmsRadioButton("1", "No ending");
        m_lowRadioButton[0] = sel1;
        sel1.setGroup(m_groupDuration);
        sel1.setChecked(true);
        sel1.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDatelowPanelSelection());
        CmsRadioButton sel2 = new CmsRadioButton("2", "Ends after:");
        m_lowRadioButton[1] = sel2;
        sel2.setGroup(m_groupDuration);
        sel2.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDatelowPanelSelection());
        CmsRadioButton sel3 = new CmsRadioButton("3", "Ends at:");
        m_lowRadioButton[2] = sel3;
        sel3.setGroup(m_groupDuration);
        sel3.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDatelowPanelSelection());
        cell2.add(sel1);
        cell2.add(sel2);

        m_times.setStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().textBoxSerialDate());
        cell2.add(m_times);
        m_times.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireValueChange();

            }

        });
        m_times.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                selectEnding(1);

            }
        });
        cell2.add(new Label("times"));
        cell2.add(sel3);

        cell2.add(m_dateboxend);
        m_dateboxend.addValueChangeHandler(new ValueChangeHandler<Date>() {

            public void onValueChange(ValueChangeEvent<Date> event) {

                fireValueChange();

            }
        });
        table.setWidget(0, 1, cell2);
        m_lowPanel.add(table);
        m_lowPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDatelowPanel());
    }

    /**
     * Private function to set all possible selections.<p>
     * */
    private void setSelectVaues() {

        m_arrayRadiobox = new CmsRadioButton[4];
        m_arrayRadiobox[0] = new CmsRadioButton(KEY_DAILY, "Daily");
        m_arrayRadiobox[0].setGroup(m_groupPattern);
        m_arrayRadiobox[0].setChecked(true);
        m_arrayRadiobox[1] = new CmsRadioButton(KEY_WEEKLY, "Weekly");
        m_arrayRadiobox[1].setGroup(m_groupPattern);
        m_arrayRadiobox[2] = new CmsRadioButton(KEY_MONTHLY, "Monthly");
        m_arrayRadiobox[2].setGroup(m_groupPattern);
        m_arrayRadiobox[3] = new CmsRadioButton(KEY_YEARLY, "Yearly");
        m_arrayRadiobox[3].setGroup(m_groupPattern);
    }

    /**
     * Private function to set all the time selections.<p>
     * */
    private void setTopPanel() {

        Label l_start = new Label("Start:");

        m_startDate.setStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().textBoxSerialDate());
        m_startDate.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireValueChange();

            }
        });

        Label l_end = new Label("End:");

        m_endDate.setStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().textBoxSerialDate());
        m_endDate.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireValueChange();

            }
        });

        Label l_duration = new Label("Duration:");
        m_duration = new CmsSelectBox();
        m_duration.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireValueChange();

            }
        });

        m_duration.addOption("0", "0 Days");
        m_duration.addOption("1", "1 Days");
        m_duration.addOption("2", "2 Days");
        m_duration.addOption("3", "3 Days");
        m_duration.addOption("4", "4 Days");
        m_duration.addOption("5", "5 Days");
        m_duration.addOption("6", "6 Days");
        m_duration.addOption("7", "1 weeks");
        m_duration.addOption("8", "2 weeks");

        m_topPanel.add(l_start);
        m_topPanel.add(m_startDate);

        m_topPanel.add(l_end);
        m_topPanel.add(m_endDate);

        m_topPanel.add(l_duration);
        m_topPanel.add(m_duration);

    }

}

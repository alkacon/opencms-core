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
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.util.CmsStringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Basic serial date widget.<p>
 *
 * @since 8.5.0
 *
 */
public class CmsSerialDate extends Composite implements I_CmsFormWidget, I_CmsHasInit, HasValueChangeHandlers<String> {

    /** The UI binder interface. */
    interface I_CmsSerialDateUiBinder extends UiBinder<HTMLPanel, CmsSerialDate> {
        // nothing to do
    }

    /** Configuration key name for the serial date day of month. */
    public static final String CONFIG_DAY_OF_MONTH = "dayofmonth";

    /** Configuration key name for the serial date end type. */
    public static final String CONFIG_END_TYPE = "endtype";

    /** Configuration key name for the serial date end date and time (sets duration together with start date). */
    public static final String CONFIG_ENDDATE = "enddate";

    /** Configuration key name for the serial date daily configuration: every working day flag. */
    public static final String CONFIG_EVERY_WORKING_DAY = "everyworkingday";

    /** Configuration key name for the serial date interval. */
    public static final String CONFIG_INTERVAL = "interval";

    /** Configuration key name for the serial date month. */
    public static final String CONFIG_MONTH = "month";

    /** Configuration key name for the serial date number of occurences. */
    public static final String CONFIG_OCCURENCES = "occurences";

    /** Configuration key name for the serial date: series end date. */
    public static final String CONFIG_SERIAL_ENDDATE = "serialenddate";

    /** Configuration key name for the serial date start date and time. */
    public static final String CONFIG_STARTDATE = "startdate";

    /** Configuration key name for the serial date type. */
    public static final String CONFIG_TYPE = "type";

    /** Configuration key name for the serial date week day(s). */
    public static final String CONFIG_WEEKDAYS = "weekdays";

    /** Series end type: ends at specific date. */
    public static final int END_TYPE_DATE = 3;

    /** Series end type: ends never. */
    public static final int END_TYPE_NEVER = 1;

    /** Series end type: ends after n times. */
    public static final int END_TYPE_TIMES = 2;

    /** The key for daily. */
    public static final String KEY_DAILY = "1";

    /** The key for monthly. */
    public static final String KEY_MONTHLY = "3";

    /** The key for weekly. */
    public static final String KEY_WEEKLY = "2";

    /** The key for yearly. */
    public static final String KEY_YEARLY = "4";

    /** Number of milliseconds per minute. */
    public static final long MILLIS_00_PER_MINUTE = 1000 * 60;

    /** Number of milliseconds per hour. */
    public static final long MILLIS_01_PER_HOUR = MILLIS_00_PER_MINUTE * 60;

    /** Number of milliseconds per day. */
    public static final long MILLIS_02_PER_DAY = MILLIS_01_PER_HOUR * 24;

    /** Number of milliseconds per week. */
    public static final long MILLIS_03_PER_WEEK = MILLIS_02_PER_DAY * 7;

    /** Separator for the week days String. */
    public static final char SEPARATOR_WEEKDAYS = ',';

    /** Serial type: daily series. */
    public static final int TYPE_DAILY = 1;

    /** Serial type: monthly series. */
    public static final int TYPE_MONTHLY = 3;

    /** Serial type: weekly series. */
    public static final int TYPE_WEEKLY = 2;

    /** Serial type: yearly series. */
    public static final int TYPE_YEARLY = 4;

    /** The UI binder instance. */
    private static I_CmsSerialDateUiBinder uiBinder = GWT.create(I_CmsSerialDateUiBinder.class);

    /** The widget type identifier for this widget. */
    private static final String WIDGET_TYPE = "SerialDate";

    /** The daily pattern. */
    CmsPatternPanelDaily m_dailyPattern;

    /** The daily pattern radio button. */
    @UiField(provided = true)
    CmsRadioButton m_dailyRadioButton;

    /** The duration selection. */
    @UiField
    CmsSelectBox m_duration;

    /** The duration label. */
    @UiField
    Element m_durationLabel;

    /** The end datebox. */
    @UiField
    CmsDateBox m_endDate;

    /** The end date. */
    Date m_endDateValue = new Date();

    /** The times text box. */
    @UiField
    TextBox m_endsAfter;

    /** The ends after radio button. */
    @UiField(provided = true)
    CmsRadioButton m_endsAfterRadioButton;

    /** The ends at radio button. */
    @UiField(provided = true)
    CmsRadioButton m_endsAtRadioButton;

    /** The end date box. */
    @UiField
    TextBox m_endTime;

    /** The end time label. */
    @UiField
    Element m_endTimeLabel;

    /** The monthly pattern. */
    CmsPatternPanelMonthly m_monthlyPattern;

    /** The monthly pattern radio button. */
    @UiField(provided = true)
    CmsRadioButton m_monthlyRadioButton;

    /** The no ending radio button. */
    @UiField(provided = true)
    CmsRadioButton m_noEndingRadioButton;

    /** The pattern options panel. */
    @UiField
    SimplePanel m_patternOptions;

    /** The begin datebox. */
    @UiField
    CmsDateBox m_startDate;

    /** The start date label. */
    @UiField
    Element m_startDateLabel;

    /** The start date. */
    Date m_startDateValue = new Date();

    /** The start date box. */
    @UiField
    TextBox m_startTime;

    /** The start time label. */
    @UiField
    Element m_startTimeLabel;

    /** The weekly pattern. */
    CmsPatternPanelWeekly m_weeklyPattern;

    /** The weekly pattern radio button. */
    @UiField(provided = true)
    CmsRadioButton m_weeklyRadioButton;

    /** The yearly pattern. */
    CmsPatternPanelYearly m_yearlyPattern;

    /** The yearly pattern radio button. */
    @UiField(provided = true)
    CmsRadioButton m_yearlyRadioButton;

    /** The active flag. */
    private boolean m_active;

    /** Value of the radio group duration. */
    private CmsRadioButtonGroup m_groupDuration;

    /** Value of the radio group pattern. */
    private CmsRadioButtonGroup m_groupPattern;

    /** The previous value. */
    private String m_previousValue;

    /** The used time format. */
    private DateTimeFormat m_timeFormat;

    /** Flag indicating the widget value is currently being changed. */
    private boolean m_changingValues;

    /**
     * Category field widgets for ADE forms.<p>
     */
    public CmsSerialDate() {

        // create pattern panels

        m_dailyPattern = new CmsPatternPanelDaily();
        m_dailyPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeIfChanged();

            }
        });
        m_weeklyPattern = new CmsPatternPanelWeekly();
        m_weeklyPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeIfChanged();

            }

        });
        m_monthlyPattern = new CmsPatternPanelMonthly();
        m_monthlyPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeIfChanged();

            }

        });
        m_yearlyPattern = new CmsPatternPanelYearly();
        m_yearlyPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeIfChanged();

            }

        });

        // provide and initialize radio buttons
        m_groupPattern = new CmsRadioButtonGroup();
        m_dailyRadioButton = new CmsRadioButton(KEY_DAILY, Messages.get().key(Messages.GUI_SERIALDATE_TYPE_DAILY_0));
        m_dailyRadioButton.setGroup(m_groupPattern);

        m_weeklyRadioButton = new CmsRadioButton(KEY_WEEKLY, Messages.get().key(Messages.GUI_SERIALDATE_TYPE_WEEKLY_0));
        m_weeklyRadioButton.setGroup(m_groupPattern);

        m_monthlyRadioButton = new CmsRadioButton(
            KEY_MONTHLY,
            Messages.get().key(Messages.GUI_SERIALDATE_TYPE_MONTHLY_0));
        m_monthlyRadioButton.setGroup(m_groupPattern);

        m_yearlyRadioButton = new CmsRadioButton(KEY_YEARLY, Messages.get().key(Messages.GUI_SERIALDATE_TYPE_YEARLY_0));
        m_yearlyRadioButton.setGroup(m_groupPattern);
        m_groupPattern.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                changePattern();
            }
        });

        m_groupDuration = new CmsRadioButtonGroup();
        m_noEndingRadioButton = new CmsRadioButton(
            "1",
            Messages.get().key(Messages.GUI_SERIALDATE_DURATION_ENDTYPE_NEVER_0));
        m_noEndingRadioButton.setGroup(m_groupDuration);
        m_noEndingRadioButton.setChecked(true);
        m_noEndingRadioButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                fireChangeIfChanged();

            }
        });

        m_endsAfterRadioButton = new CmsRadioButton(
            "2",
            Messages.get().key(Messages.GUI_SERIALDATE_DURATION_ENDTYPE_OCC_0));
        m_endsAfterRadioButton.setGroup(m_groupDuration);
        m_endsAfterRadioButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                if (m_endsAfter.getText().isEmpty()) {
                    m_endsAfter.setValue("1");
                }
                fireChangeIfChanged();

            }
        });

        m_endsAtRadioButton = new CmsRadioButton(
            "3",
            Messages.get().key(Messages.GUI_SERIALDATE_DURATION_ENDTYPE_DATE_0));
        m_endsAtRadioButton.setGroup(m_groupDuration);
        m_endsAtRadioButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_endDate.setValue(new Date());
                fireChangeIfChanged();

            }
        });

        // bind the ui
        initWidget(uiBinder.createAndBindUi(this));
        // init labels
        m_startTimeLabel.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_TIME_STARTTIME_0));
        m_endTimeLabel.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_TIME_ENDTIME_0));
        m_durationLabel.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_TIME_DURATION_0));
        m_startDateLabel.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_TIME_STARTDATE_0));

        // init duration select
        m_duration.getOpener().addStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_duration.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());

        m_duration.addOption("0", Messages.get().key(Messages.GUI_SERIALDATE_DURATION_DURATION_SAMEDAY_0));
        m_duration.addOption("1", Messages.get().key(Messages.GUI_SERIALDATE_DURATION_DURATION_FIRST_0));
        m_duration.addOption("2", Messages.get().key(Messages.GUI_SERIALDATE_DURATION_DURATION_SECOND_0));
        m_duration.addOption("3", Messages.get().key(Messages.GUI_SERIALDATE_DURATION_DURATION_THIRD_0));
        m_duration.addOption("4", Messages.get().key(Messages.GUI_SERIALDATE_DURATION_DURATION_FOURTH_0));
        m_duration.addOption("5", Messages.get().key(Messages.GUI_SERIALDATE_DURATION_DURATION_FIFTH_0));
        m_duration.addOption("6", Messages.get().key(Messages.GUI_SERIALDATE_DURATION_DURATION_SIXTH_0));
        m_duration.addOption("7", Messages.get().key(Messages.GUI_SERIALDATE_DURATION_DURATION_ONEWEEK_0));
        m_duration.addOption("8", Messages.get().key(Messages.GUI_SERIALDATE_DURATION_DURATION_TWOWEEK_0));

        try {
            m_timeFormat = DateTimeFormat.getFormat(
                org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_DATEBOX_TIME_PATTERN_0));
        } catch (@SuppressWarnings("unused") Exception e) {
            // in case the pattern is not available, fall back to standard en pattern
            m_timeFormat = DateTimeFormat.getFormat("hh:mm aa");
        }
        m_endTime.setValue(m_timeFormat.format(new Date()));
        m_startTime.setValue(m_timeFormat.format(new Date()));
        m_startDate.setValue(new Date());

        m_startDate.getTextField().getTextBoxContainer().addStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().calendarStyle());
        m_endDate.getTextField().getTextBoxContainer().addStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().calendarStyle());

        m_groupPattern.selectButton(m_dailyRadioButton);
        m_active = true;
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
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
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
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
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
     * Cleared all fields for the inactive view.<p>
     */
    public void clearFealds() {

        m_startDate.setFormValueAsString("");
        m_endDate.setFormValueAsString("");
        m_startTime.setValue("");
        m_endTime.setValue("");
    }

    /**
     * Represents a value change event.<p>
     */
    public void fireValueChange() {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                fireChangeIfChanged();
            }
        });
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
     * Handles the duration change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_duration")
    public void onDurationChange(ValueChangeEvent<String> event) {

        fireChangeIfChanged();
    }

    /**
     * Handles the end date change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_endDate")
    public void onEndDateChange(ValueChangeEvent<Date> event) {

        m_groupDuration.selectButton(m_endsAtRadioButton);
        fireChangeIfChanged();
    }

    /**
     * Handles the ends after change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_endsAfter")
    public void onEndsAfterChange(ValueChangeEvent<String> event) {

        fireChangeIfChanged();
    }

    /**
     * Handles the ends after focus event.<p>
     *
     * @param event the focus event
     */
    @UiHandler("m_endsAfter")
    public void onEndsAfterFocus(FocusEvent event) {

        m_groupDuration.selectButton(m_endsAfterRadioButton);
    }

    /**
     * Handles the ends after key press event.<p>
     *
     * @param event the key press event
     */
    @UiHandler("m_endsAfter")
    public void onEndsAfterKeyPress(KeyPressEvent event) {

        fireChangeIfChanged();
    }

    /**
     * Handles the end time change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_endTime")
    public void onEndTimeChange(ValueChangeEvent<String> event) {

        fireChangeIfChanged();
    }

    /**
     * Handles the start date change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_startDate")
    public void onStartDateChange(ValueChangeEvent<Date> event) {

        fireChangeIfChanged();
    }

    /**
     * Handles the start time change event.<p>
     *
     * @param event the change event
     */
    @UiHandler("m_startTime")
    public void onStartTimeChange(ValueChangeEvent<String> event) {

        fireChangeIfChanged();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        //nothing to do

    }

    /**
     * Sets the radio buttons active or inactive.<p>
     * @param active true or false to activate or deactivate
     * */
    public void setActive(boolean active) {

        if (active != m_active) {
            m_active = active;
            m_dailyRadioButton.setEnabled(active);
            m_weeklyRadioButton.setEnabled(active);
            m_monthlyRadioButton.setEnabled(active);
            m_yearlyRadioButton.setEnabled(active);

            m_noEndingRadioButton.setEnabled(active);
            m_endsAfterRadioButton.setEnabled(active);
            m_endsAtRadioButton.setEnabled(active);

            if (active) {
                m_groupPattern.selectButton(m_dailyRadioButton);
                m_groupDuration.selectButton(m_noEndingRadioButton);
            } else {
                m_groupPattern.deselectButton();
                m_groupDuration.deselectButton();
            }
            m_dailyPattern.setActive(active);
        }
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

        //m_error.setText(errorMessage);

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        if (!value.isEmpty()) {
            CmsDebugLog.getInstance().printLine("Setting value: " + value);
            Map<String, String> values = new HashMap<String, String>();
            String[] split = value.split("\\|");
            for (int i = 0; i < split.length; i++) {
                int pars = split[i].indexOf("=");
                String key = split[i].substring(0, pars);
                String val = split[i].substring(pars + 1);
                values.put(key, val);
            }
            setValues(values);
            CmsDebugLog.getInstance().printLine("Value set");
        }
        m_previousValue = value;
    }

    /**
     * Selects the right view for the selected pattern.<p>
     */
    protected void changePattern() {

        if (m_groupPattern.getSelectedButton() != null) {
            String buttonName = m_groupPattern.getSelectedButton().getName();
            // m_patterPanel.removeFromParent();
            if (buttonName.equals(KEY_DAILY)) {
                m_patternOptions.setWidget(m_dailyPattern);
            } else if (buttonName.equals(KEY_WEEKLY)) {
                m_patternOptions.setWidget(m_weeklyPattern);
            } else if (buttonName.equals(KEY_MONTHLY)) {
                m_patternOptions.setWidget(m_monthlyPattern);
            } else if (buttonName.equals(KEY_YEARLY)) {
                m_patternOptions.setWidget(m_yearlyPattern);
            }
            fireChangeIfChanged();
            CmsDebugLog.getInstance().printLine("Pattern changed");
        }
    }

    /**
     * Checks if the current value has changed an fires the change event.<p>
     */
    protected void fireChangeIfChanged() {

        if (!m_changingValues && m_active) {
            String current = getFormValueAsString();
            if (!current.equals(m_previousValue)) {
                m_previousValue = current;
                ValueChangeEvent.fire(this, current);
            }
        }
    }

    /**
     * Returns the duration button for type.<p>
     *
     * @param type the type
     *
     * @return the button
     */
    private CmsRadioButton getDurationButtonForType(int type) {

        CmsRadioButton duration = null;
        switch (type) {
            case END_TYPE_NEVER:
                duration = m_noEndingRadioButton;
                break;
            case END_TYPE_TIMES:
                duration = m_endsAfterRadioButton;
                break;
            case END_TYPE_DATE:
                duration = m_endsAtRadioButton;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return duration;
    }

    /**
     * Returns the pattern button for type.<p>
     *
     * @param type the type
     *
     * @return the button
     */
    private CmsRadioButton getPatternButtonForType(int type) {

        CmsRadioButton pattern = null;
        switch (type) {
            case TYPE_DAILY:
                pattern = m_dailyRadioButton;
                break;
            case TYPE_WEEKLY:
                pattern = m_weeklyRadioButton;
                break;
            case TYPE_MONTHLY:
                pattern = m_monthlyRadioButton;
                break;
            case TYPE_YEARLY:
                pattern = m_yearlyRadioButton;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return pattern;
    }

    /**
     * Selects all needed information an build the result string.<p>
     *
     * @return the result string
     * */
    @SuppressWarnings("deprecation")
    private String selectValues() {

        CmsDebugLog.getInstance().printLine("Selecting values");
        String result = "";
        String type = "1";
        if (m_groupPattern.getSelectedButton() != null) {
            type = m_groupPattern.getSelectedButton().getName();
        }
        result += CONFIG_TYPE + "=" + type + "|";
        switch (Integer.parseInt(type)) {
            case (TYPE_DAILY):
                result += CONFIG_INTERVAL + "=" + m_dailyPattern.getInterval() + "|";
                result += CONFIG_EVERY_WORKING_DAY + "=" + m_dailyPattern.getWorkingDay() + "|";
                break;
            case (TYPE_WEEKLY):
                result += CONFIG_INTERVAL + "=" + m_weeklyPattern.getInterval() + "|";
                result += CONFIG_WEEKDAYS + "=" + m_weeklyPattern.getWeekDays() + "|";

                break;
            case (TYPE_MONTHLY):
                result += CONFIG_INTERVAL + "=" + m_monthlyPattern.getInterval() + "|";
                result += CONFIG_DAY_OF_MONTH + "=" + m_monthlyPattern.getDayOfMonth() + "|";
                if (!m_monthlyPattern.getWeekDays().equals("-1")) {
                    result += CONFIG_WEEKDAYS + "=" + m_monthlyPattern.getWeekDays() + "|";
                }
                break;
            case (TYPE_YEARLY):
                result += CONFIG_DAY_OF_MONTH + "=" + m_yearlyPattern.getDayOfMonth() + "|";
                result += CONFIG_MONTH + "=" + m_yearlyPattern.getMonth() + "|";
                if (!m_yearlyPattern.getWeekDays().equals("-1")) {
                    result += CONFIG_WEEKDAYS + "=" + m_yearlyPattern.getWeekDays() + "|";
                }
                break;
            default:
                break;

        }
        Date startDate = new Date();
        Date endDate = new Date();
        m_startDateValue = m_startDate.getValue();
        if (m_startDateValue == null) {
            m_startDateValue = new Date();
        }

        switch (Integer.parseInt(m_duration.getFormValueAsString())) {
            case (0):
                m_endDateValue.setTime(m_startDateValue.getTime() + (0 * MILLIS_02_PER_DAY));
                break;
            case (1):
                m_endDateValue.setTime(m_startDateValue.getTime() + (1 * MILLIS_02_PER_DAY));
                break;
            case (2):
                m_endDateValue.setTime(m_startDateValue.getTime() + (2 * MILLIS_02_PER_DAY));
                break;
            case (3):
                m_endDateValue.setTime(m_startDateValue.getTime() + (3 * MILLIS_02_PER_DAY));
                break;
            case (4):
                m_endDateValue.setTime(m_startDateValue.getTime() + (4 * MILLIS_02_PER_DAY));
                break;
            case (5):
                m_endDateValue.setTime(m_startDateValue.getTime() + (5 * MILLIS_02_PER_DAY));
                break;
            case (6):
                m_endDateValue.setTime(m_startDateValue.getTime() + (6 * MILLIS_02_PER_DAY));
                break;
            case (7):
                m_endDateValue.setTime(m_startDateValue.getTime() + (7 * MILLIS_02_PER_DAY));
                break;
            case (8):
                m_endDateValue.setTime(m_startDateValue.getTime() + (8 * MILLIS_02_PER_DAY));
                break;
            default:
                throw new IllegalArgumentException();
        }

        startDate = m_timeFormat.parse(m_startTime.getText());
        m_startDateValue.setHours(startDate.getHours());
        m_startDateValue.setMinutes(startDate.getMinutes());
        endDate = m_timeFormat.parse(m_endTime.getText());
        m_endDateValue.setHours(endDate.getHours());
        m_endDateValue.setMinutes(endDate.getMinutes());

        CmsDebugLog.getInstance().printLine("New Endtime: " + m_endDateValue.getTime());
        result += CONFIG_STARTDATE + "=" + m_startDateValue.getTime() + "|";
        result += CONFIG_ENDDATE + "=" + m_endDateValue.getTime() + "|";
        String endtype = "1";
        if (m_groupDuration.getSelectedButton() != null) {
            endtype = m_groupDuration.getSelectedButton().getName();
        }
        switch (Integer.parseInt(endtype)) {
            case (1):
                break;
            case (END_TYPE_TIMES):
                if (!m_endsAfter.getText().isEmpty()) {
                    result += CONFIG_OCCURENCES + "=" + m_endsAfter.getText() + "|";
                }
                break;
            case (END_TYPE_DATE):
                if (!m_endDate.getValueAsFormatedString().isEmpty()) {
                    result += CONFIG_SERIAL_ENDDATE + "=" + m_endDate.getFormValueAsString() + "|";
                }
                break;
            default:
                break;

        }

        result += CONFIG_END_TYPE + "=" + endtype;
        return result;
    }

    /**
     * Creates a serial date entry from the given property value.<p>
     *
     * If no matching serial date could be created, <code>null</code> is returned.<p>
     *
     * @param values the Map containing the date configuration values
     */
    private void setValues(Map<String, String> values) {

        m_changingValues = true;
        // first set serial date fields used by all serial types

        // fetch the start date and time

        String startLong = values.get(CONFIG_STARTDATE);
        m_startDateValue = new Date(getLongValue(startLong, 0));
        m_startTime.setValue(m_timeFormat.format(m_startDateValue));

        m_startDate.setValue(m_startDateValue);
        // the end date and time (this means the duration of a single entry)

        String endLong = values.get(CONFIG_ENDDATE);
        m_endDateValue = new Date(getLongValue(endLong, 0));
        m_endTime.setValue(m_timeFormat.format(m_endDateValue));
        CmsDebugLog.getInstance().printLine("Step 1");
        if (getLongValue(endLong, 0) > getLongValue(startLong, 0)) {
            // duration at least one day, calculate it
            long delta = getLongValue(endLong, 0) - getLongValue(startLong, 0);
            int test = (int)(delta / MILLIS_02_PER_DAY);
            m_duration.selectValue((test) + "");
        }
        CmsDebugLog.getInstance().printLine("Step 1.5");
        // determine the serial end type
        String endTypeStr = values.get(CONFIG_END_TYPE);
        int endType = getIntValue(endTypeStr, END_TYPE_NEVER);
        CmsDebugLog.getInstance().printLine("Setting end type to: " + endType);
        m_groupDuration.selectButton(getDurationButtonForType(endType));
        if (endType == END_TYPE_TIMES) {
            // end type: after a number of occurences
            String occurStr = values.get(CONFIG_OCCURENCES);
            CmsDebugLog.getInstance().printLine("Setting occurrences to: " + occurStr);
            m_endsAfter.setText(occurStr);
        } else if (endType == END_TYPE_DATE) {
            // end type: ends at a specified date
            String endDateStr = values.get(CONFIG_SERIAL_ENDDATE);
            long endDate = getLongValue(endDateStr, 0);
            m_endDate.setValue(new Date(endDate));

        }
        CmsDebugLog.getInstance().printLine("Step 2");
        // now determine the serial date options depending on the serial date type

        String type = values.get(CONFIG_TYPE);
        int entryType = getIntValue(type, 1);
        m_groupPattern.selectButton(getPatternButtonForType(entryType));
        CmsDebugLog.getInstance().printLine("Step 3");
        String intervalStr = values.get(CONFIG_INTERVAL);
        CmsDebugLog.getInstance().printLine(CONFIG_INTERVAL + ": " + intervalStr);
        switch (entryType) {
            case TYPE_DAILY:
                // daily series entry, get interval and working days flag
                CmsDebugLog.getInstance().printLine("Setting daily");
                String workingDaysStr = values.get(CONFIG_EVERY_WORKING_DAY);
                CmsDebugLog.getInstance().printLine(CONFIG_EVERY_WORKING_DAY + ": " + workingDaysStr);
                boolean workingDays = Boolean.valueOf(workingDaysStr).booleanValue();
                m_dailyPattern.setInterval(intervalStr);
                CmsDebugLog.getInstance().printLine("Interval has been set");
                m_dailyPattern.setWorkingDaySelection(workingDays);
                break;
            case TYPE_WEEKLY:
                // weekly series entry
                CmsDebugLog.getInstance().printLine("Setting weekly");
                String weekDaysStr = values.get(CONFIG_WEEKDAYS);
                List<String> weekDaysStrList = CmsStringUtil.splitAsList(weekDaysStr, SEPARATOR_WEEKDAYS, true);
                m_weeklyPattern.setInterval(intervalStr);
                m_weeklyPattern.setWeekDays(weekDaysStrList);
                break;
            case TYPE_MONTHLY:
                // monthly series entry
                CmsDebugLog.getInstance().printLine("Setting monthly");
                String dayOfMonthStrMonthly = values.get(CONFIG_DAY_OF_MONTH);
                int dayOfMonthMonthly = getIntValue(dayOfMonthStrMonthly, 1);
                String weekDayStrMonthly = values.get(CONFIG_WEEKDAYS);
                int weekDayMonthly = getIntValue(weekDayStrMonthly, -1);
                m_monthlyPattern.setWeekDay(weekDayMonthly);
                m_monthlyPattern.setInterval(intervalStr);
                m_monthlyPattern.setDayOfMonth(dayOfMonthMonthly);

                break;
            case TYPE_YEARLY:
                // yearly series entry
                CmsDebugLog.getInstance().printLine("Setting yearly");
                String dayOfMonthStr = values.get(CONFIG_DAY_OF_MONTH);
                int dayOfMonth = getIntValue(dayOfMonthStr, 1);
                String weekDayStr = values.get(CONFIG_WEEKDAYS);
                int weekDay = getIntValue(weekDayStr, -1);
                String monthStr = values.get(CONFIG_MONTH);
                int month = getIntValue(monthStr, 0);
                m_yearlyPattern.setWeekDay(weekDay);
                m_yearlyPattern.setDayOfMonth(dayOfMonth);
                m_yearlyPattern.setMonth(month);

                break;
            default:

        }
        m_changingValues = false;

        //      selectValues();
    }

}

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

import org.opencms.acacia.client.widgets.I_CmsEditWidget;
import org.opencms.acacia.shared.CmsSerialDateUtil;
import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.Month;
import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;
import org.opencms.acacia.shared.rpc.I_CmsSerialDateService;
import org.opencms.acacia.shared.rpc.I_CmsSerialDateServiceAsync;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Composite;

/** Controller for the serial date widget, being the widget implementation itself. */
public class CmsSerialDateController extends Composite
implements I_CmsEditWidget, I_ChangeHandler, I_StatusUpdateHandler {

    /** Data wrapper for the default values to set when changing the pattern, dependent on the event's start date. */
    public static class PatternDefaultValues {

        /** The date used for the initialization. */
        private final Date m_date;
        /** The default month. */
        private final Month m_month;
        /** The default day of month. */
        private final int m_dayOfMonth;
        /** The default week day. */
        private final WeekDay m_weekDay;
        /** The default week of month. */
        private final WeekOfMonth m_weekOfMonth;
        /** The default interval. */
        private final int m_interval;
        /** The default weeks of month. */
        private Collection<WeekOfMonth> m_weeksOfMonth;

        /**
         * Default constructor.
         * @param startDate the date, that determines the default values.
         */
        @SuppressWarnings("deprecation")
        protected PatternDefaultValues(Date startDate) {
            m_date = startDate;
            m_month = null == startDate ? Month.JANUARY : Month.values()[startDate.getMonth()];
            m_dayOfMonth = null == startDate ? 1 : startDate.getDate();
            m_weekDay = null == startDate ? WeekDay.SUNDAY : WeekDay.values()[startDate.getDay()];
            m_weekOfMonth = null == startDate ? WeekOfMonth.FIRST : WeekOfMonth.values()[(startDate.getDate() / 7)];
            Collection<WeekOfMonth> weeksOfMonth = new ArrayList<>(1);
            weeksOfMonth.add(m_weekOfMonth);
            m_weeksOfMonth = weeksOfMonth;
            m_interval = 1;
        }

        /**
         * Returns the date used for the initialization.
         * @return the date used for the initialization.
         */
        public Date getDate() {

            return m_date;
        }

        /**
         * Returns the default day of month.
         * @return the default day of month.
         */
        public int getDayOfMonth() {

            return m_dayOfMonth;
        }

        /**
         * Returns the default interval.
         * @return the default interval.
         */
        public int getInterval() {

            return m_interval;
        }

        /**
         * Returns the default month.
         * @return the default month.
         */
        public Month getMonth() {

            return m_month;
        }

        /**
         * Returns the default week day.
         * @return the default week day.
         */
        public WeekDay getWeekDay() {

            return m_weekDay;
        }

        /**
         * Returns the default week of month.
         * @return the default week of month.
         */
        public WeekOfMonth getWeekOfMonth() {

            return m_weekOfMonth;
        }

        /**
         * Returns the default weeks of month.
         * @return the default weeks of month.
         */
        public Collection<WeekOfMonth> getWeeksOfMonth() {

            return m_weeksOfMonth;
        }
    }

    /**
     * The status update timer.<p>
     * Status update takes place at most every 500 milliseconds.
     * Whenever re-update is triggered during the time waiting, the timer is reset,
     * i.e., it is waited for 500 milliseconds again.
     */
    protected static class StatusUpdateTimer extends Timer {

        /** The single status update timer that is allowed. */
        private static StatusUpdateTimer m_timer;
        /** The handler to call for the validation. */
        private I_StatusUpdateHandler m_handler;

        /**
         * Constructor.<p>
         * @param handler the handler to call for the status update.
         */
        StatusUpdateTimer(I_StatusUpdateHandler handler) {
            m_handler = handler;
        }

        /**
         * Trigger the status update after a certain time out.
         * @param handler the status update handler, that actually performs the status update.
         */
        public static void updateStatus(I_StatusUpdateHandler handler) {

            if (null != m_timer) {
                m_timer.cancel();
            }
            m_timer = new StatusUpdateTimer(handler);
            m_timer.schedule(500);
        }

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            m_handler.updateStatus();
            m_timer = null;
        }

    }

    /** Confirmation dialog that should be shown when exceptions are removed when the value changes. */
    private class CmsExceptionsDeleteConfirmDialog {

        /** The confirmation dialog that is shown. */
        CmsConfirmDialog m_dialog;
        /** The command to execute on confirmation. */
        Command m_cmd;

        /** The value to reset the exceptions */
        final CmsSerialDateValue m_value;

        /**
         * Default constructor.
         * @param value the value where the exceptions should be reset.
         */
        CmsExceptionsDeleteConfirmDialog(final CmsSerialDateValue value) {
            m_value = value;
            m_dialog = new CmsConfirmDialog(
                Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_EXCEPTION_DIALOG_CAPTION_0),
                Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_EXCEPTION_DIALOG_MESSAGE_0));
            m_dialog.setOkText(Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_DIALOG_YES_BUTTON_0));
            m_dialog.setCloseText(Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_DIALOG_NO_BUTTON_0));
            m_dialog.setHandler(new I_CmsConfirmDialogHandler() {

                public void onClose() {

                    handleClose();

                }

                public void onOk() {

                    handleOk();

                }
            });
        }

        /**
         * Show the dialog.
         * @param okCmd the command to execute when "ok" is clicked.
         */
        public void show(final Command okCmd) {

            m_cmd = okCmd;
            m_dialog.center();
        }

        /**
         * Method called when the dialog is closed not performing the action.
         */
        void handleClose() {

            m_view.onValueChange();
            m_dialog.hide();
        }

        /**
         * Method called when the dialog is closed performing the action.
         */
        void handleOk() {

            m_value.clearExceptions();
            m_cmd.execute();
            m_dialog.hide();
        }
    }

    /** Confirmation dialog to show if the series binding is removed. */
    private class CmsRemoveSeriesBindingConfirmDialog {

        /** The confirmation dialog that is shown. */
        CmsConfirmDialog m_dialog;
        /** The command to execute on confirmation. */
        Command m_cmd;

        /** The value to reset the exceptions */
        final CmsSerialDateValue m_value;

        /**
         * Default constructor.
         * @param value the value where the exceptions should be reset.
         */
        CmsRemoveSeriesBindingConfirmDialog(final CmsSerialDateValue value) {
            m_value = value;
            m_dialog = new CmsConfirmDialog(
                Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_SERIES_BINDING_DIALOG_CAPTION_0),
                Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_SERIES_BINDING_DIALOG_MESSAGE_0));
            m_dialog.setOkText(Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_DIALOG_YES_BUTTON_0));
            m_dialog.setCloseText(Messages.get().key(Messages.GUI_SERIALDATE_CONFIRM_DIALOG_NO_BUTTON_0));
            m_dialog.setHandler(new I_CmsConfirmDialogHandler() {

                public void onClose() {

                    handleClose();

                }

                public void onOk() {

                    handleOk();

                }
            });
        }

        /**
         * Show the dialog.
         * @param okCmd the command to execute when "ok" is clicked.
         */
        public void show(final Command okCmd) {

            m_cmd = okCmd;
            m_dialog.center();
        }

        /**
         * Method called when the dialog is closed not performing the action.
         */
        void handleClose() {

            m_view.onValueChange();
            m_dialog.hide();
        }

        /**
         * Method called when the dialog is closed performing the action.
         */
        void handleOk() {

            m_value.clearExceptions();
            m_cmd.execute();
            m_dialog.hide();
        }

    }

    /** Default date format. */
    public static final String DEFAULT_DATE_FORMAT = "EEEE, MMMM dd, yyyy";

    /** The RPC service for the serial date widget. */
    private static I_CmsSerialDateServiceAsync SERVICE;

    /** Flag, indicating if the widget is currently active. */
    private boolean m_active;

    /** RPC call action to get dates. */
    private final CmsRpcAction<Collection<CmsPair<Date, Boolean>>> m_getDatesAction;

    /** RPC call action to get the current status. */
    private final CmsRpcAction<CmsPair<Boolean, String>> m_statusUpdateAction;

    /** The pattern controllers. */
    private final Map<PatternType, I_CmsSerialDatePatternController> m_patternControllers = new HashMap<>();

    /* Date and time formats */

    /** Format with date only. */
    DateTimeFormat m_dateFormat = DateTimeFormat.getFormat(
        Messages.get().keyDefault(Messages.GUI_SERIALDATE_DATE_FORMAT_0, DEFAULT_DATE_FORMAT));

    /** The view */
    final CmsSerialDateView m_view;

    /** The model */
    final CmsSerialDateValue m_model;

    /** The default values to use when switching the pattern. They depend on the start date of the events. */
    private PatternDefaultValues m_patternDefaultValues;

    /** The confirmation dialog for deletion of exceptions. */
    private CmsExceptionsDeleteConfirmDialog m_exceptionConfirmDialog;

    /** The confirmation dialog for deleting the binding to another series. */
    private CmsRemoveSeriesBindingConfirmDialog m_removeSeriesBindingConfirmDialog;

    /**
     * Category field widgets for ADE forms.<p>
     */
    public CmsSerialDateController() {

        m_model = new CmsSerialDateValue();
        m_view = new CmsSerialDateView(this, m_model);
        initWidget(m_view);
        setPatternDefaultValues(m_model.getStart());
        initPatternControllers();
        m_getDatesAction = new CmsRpcAction<Collection<CmsPair<Date, Boolean>>>() {

            @Override
            public void execute() {

                getService().getDates(m_model.toString(), this);

            }

            @Override
            protected void onResponse(Collection<CmsPair<Date, Boolean>> result) {

                CmsSerialDateController.this.m_view.showCurrentDates(result);
            }
        };
        m_statusUpdateAction = new CmsRpcAction<CmsPair<Boolean, String>>() {

            @Override
            public void execute() {

                getService().getStatus(m_model.toString(), this);

            }

            @Override
            protected void onResponse(CmsPair<Boolean, String> result) {

                m_view.setManagementButtonEnabled(Objects.equals(Boolean.TRUE, result.getFirst()));
                m_view.setStatus(result.getSecond());

            }

        };
        m_active = true;
        m_exceptionConfirmDialog = new CmsExceptionsDeleteConfirmDialog(m_model);
        m_removeSeriesBindingConfirmDialog = new CmsRemoveSeriesBindingConfirmDialog(m_model);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_ChangeHandler#conditionallyRemoveExceptionsOnChange(com.google.gwt.user.client.Command, boolean)
     */
    @Override
    public void conditionallyRemoveExceptionsOnChange(Command action, boolean showDialog) {

        if (m_model.hasExceptions() && showDialog) {
            m_exceptionConfirmDialog.show(action);
        } else {
            action.execute();
        }
    }

    /**
     * Show the dates for managing exceptions.
     */
    public void executeShowDatesAction() {

        m_getDatesAction.execute();

    }

    /**
     * Returns the controller for the currently active pattern.
     * @return the controller for the currently active pattern.
     */
    public I_CmsSerialDatePatternController getPattern() {

        return m_patternControllers.get(m_model.getPatternType());
    }

    /**
     * Returns the default values to set for the patterns dependent on the current start date.
     * @return the default values to set for the patterns dependent on the current start date.
     */
    @Override
    public PatternDefaultValues getPatternDefaultValues() {

        return m_patternDefaultValues;
    }

    /**
     * Returns the widget to place in the pattern panel.
     * @return the widget to place in the pattern panel.
     */
    public I_CmsSerialDatePatternView getPatternView() {

        return getPattern().getView();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_model.toString();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        return getElement().isOrHasChild(element);
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_ChangeHandler#removeExceptionsOnChange(com.google.gwt.user.client.Command)
     */
    @Override
    public void removeExceptionsOnChange(Command cmd) {

        conditionallyRemoveExceptionsOnChange(cmd, true);

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (active != m_active) {
            m_active = active;
            m_view.setActive(active);
        }

    }

    /**
     * Set the flag, indicating if the events are displayed as "current" till they end (true) or only till they start (false).
     * @param isCurrentTillEnd the flag, indicating if the events are displayed as "current" till they end (true) or only till they start (false).
     */
    public void setCurrentTillEnd(Boolean isCurrentTillEnd) {

        if (m_model.isCurrentTillEnd() ^ ((null != isCurrentTillEnd) && isCurrentTillEnd.booleanValue())) {
            m_model.setCurrentTillEnd(isCurrentTillEnd);
            valueChanged();
        }
    }

    /**
     * Set the end time.
     * @param date the end time to set.
     */
    public void setEndTime(final Date date) {

        if (!Objects.equals(m_model.getEnd(), date)) {
            m_model.setEnd(date);
            valueChanged();
        }

    }

    /**
     * Set the duration option.
     * @param value the duration option to set ({@link EndType} as string).
     */
    public void setEndType(final String value) {

        final EndType endType = EndType.valueOf(value);
        if (!endType.equals(m_model.getEndType())) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    switch (endType) {
                        case SINGLE:
                            m_model.setOccurrences(0);
                            m_model.setSeriesEndDate(null);
                            break;
                        case TIMES:
                            m_model.setOccurrences(10);
                            m_model.setSeriesEndDate(null);
                            break;
                        case DATE:
                            m_model.setOccurrences(0);
                            m_model.setSeriesEndDate(m_model.getStart() == null ? new Date() : m_model.getStart());
                            break;
                        default:
                            break;
                    }
                    m_model.setEndType(endType);
                    valueChanged();
                }
            });
        }

    }

    /**
     * Toggle between single events and series.
     * @param isSeries flag, indicating if we want a series of events.
     */
    public void setIsSeries(Boolean isSeries) {

        if (null != isSeries) {
            final boolean series = isSeries.booleanValue();
            if ((null != m_model.getParentSeriesId()) && series) {
                m_removeSeriesBindingConfirmDialog.show(new Command() {

                    public void execute() {

                        m_model.setParentSeriesId(null);
                        setPattern(PatternType.DAILY.toString());

                    }
                });
            } else {
                setPattern(series ? PatternType.DAILY.toString() : PatternType.NONE.toString());
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // not necessary

    }

    /**
     * Set the occurrences. If the String is invalid, the occurrences will be set to "-1" to cause server-side validation to fail.
     * @param occurrences the interval to set.
     */
    public void setOccurrences(String occurrences) {

        int o = CmsSerialDateUtil.toIntWithDefault(occurrences, -1);
        if (m_model.getOccurrences() != o) {
            m_model.setOccurrences(o);
            valueChanged();
        }
    }

    /**
     * Set the serial pattern type.
     * @param patternType the pattern type to set.
     */
    public void setPattern(String patternType) {

        final PatternType type = PatternType.valueOf(patternType);
        if (type != m_model.getPatternType()) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    EndType oldEndType = m_model.getEndType();
                    m_model.setPatternType(type);
                    m_model.setIndividualDates(null);
                    m_model.setInterval(getPatternDefaultValues().getInterval());
                    m_model.setEveryWorkingDay(Boolean.FALSE);
                    m_model.clearWeekDays();
                    m_model.clearIndividualDates();
                    m_model.clearWeeksOfMonth();
                    m_model.clearExceptions();
                    if (type.equals(PatternType.NONE) || type.equals(PatternType.INDIVIDUAL)) {
                        m_model.setEndType(EndType.SINGLE);
                    } else if (oldEndType.equals(EndType.SINGLE)) {
                        m_model.setEndType(EndType.TIMES);
                        m_model.setOccurrences(10);
                        m_model.setSeriesEndDate(null);
                    }
                    m_model.setDayOfMonth(getPatternDefaultValues().getDayOfMonth());
                    m_model.setMonth(getPatternDefaultValues().getMonth());
                    if (type.equals(PatternType.WEEKLY)) {
                        m_model.setWeekDay(getPatternDefaultValues().getWeekDay());
                    }
                    valueChanged();
                }
            });
        }

    }

    /**
     * Set the serial end date.
     * @param date the serial end date.
     */
    public void setSeriesEndDate(Date date) {

        if (!Objects.equals(m_model.getSeriesEndDate(), date)) {
            m_model.setSeriesEndDate(date);
            valueChanged();
        }

    }

    /**
     * Set the start time.
     * @param date the start time to set.
     */
    public void setStartTime(final Date date) {

        if (!Objects.equals(m_model.getStart(), date)) {
            removeExceptionsOnChange(new Command() {

                public void execute() {

                    m_model.setStart(date);
                    setPatternDefaultValues(date);
                    valueChanged();
                }
            });
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setValue(java.lang.String, boolean)
     */
    public void setValue(String value, boolean fireEvent) {

        if (!m_model.toString().equals(value)) {
            m_model.setValue(value);
            setPatternDefaultValues(m_model.getStart());
            if (fireEvent) {
                valueChanged();
            }
        }
    }

    /**
     * Sets the whole day flag.
     * @param isWholeDay flag, indicating if the event lasts whole days.
     */
    public void setWholeDay(Boolean isWholeDay) {

        if (m_model.isWholeDay() ^ ((null != isWholeDay) && isWholeDay.booleanValue())) {
            m_model.setWholeDay(isWholeDay);
            valueChanged();
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_ChangeHandler#sizeChanged()
     */
    public void sizeChanged() {

        CmsDomUtil.resizeAncestor(m_view.getParent());
    }

    /**
     * Updates the exceptions.
     * @param exceptions the exceptions to set
     */
    public void updateExceptions(SortedSet<Date> exceptions) {

        SortedSet<Date> e = null == exceptions ? new TreeSet<Date>() : exceptions;
        if (!m_model.getExceptions().equals(e)) {
            m_model.setExceptions(e);
            m_view.updateExceptions();
            valueChanged();
            sizeChanged();
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_StatusUpdateHandler#updateStatus()
     */
    public void updateStatus() {

        m_statusUpdateAction.execute();
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_ChangeHandler#valueChanged()
     */
    @Override
    public void valueChanged() {

        StatusUpdateTimer.updateStatus(this);
        m_view.onValueChange();
        ValueChangeEvent.fire(this, m_model.toString());
    }

    /**
    * Returns the RPC service for serial dates.
    * @return the RPC service for serial dates.
    */
    I_CmsSerialDateServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsSerialDateService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.contenteditor.CmsSerialDateService.gwt");
            ((ServiceDefTarget)SERVICE).setServiceEntryPoint(serviceUrl);
        }
        return SERVICE;
    }

    /**
     * Sets the default pattern values dependent on the provided start date.
     * @param startDate the date, the default values are determined with.
     */
    void setPatternDefaultValues(Date startDate) {

        if ((m_patternDefaultValues == null) || !Objects.equals(m_patternDefaultValues.getDate(), startDate)) {
            m_patternDefaultValues = new PatternDefaultValues(startDate);
        }
    }

    /**
     * Initialize the pattern controllers.
     */
    private void initPatternControllers() {

        m_patternControllers.put(PatternType.NONE, new CmsPatternPanelNoneController());
        m_patternControllers.put(PatternType.DAILY, new CmsPatternPanelDailyController(m_model, this));
        m_patternControllers.put(PatternType.WEEKLY, new CmsPatternPanelWeeklyController(m_model, this));
        m_patternControllers.put(PatternType.MONTHLY, new CmsPatternPanelMonthlyController(m_model, this));
        m_patternControllers.put(PatternType.YEARLY, new CmsPatternPanelYearlyController(m_model, this));
        // m_patternControllers.put(PatternType.INDIVIDUAL, new CmsPatternPanelIndividualController(m_model, this));
    }

}